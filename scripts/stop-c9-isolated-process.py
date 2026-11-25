#!/usr/bin/env python3
"""CI-only pidfd cleanup of an application launched in a known disposable netns."""
import os
import pathlib
import re
import select
import signal
import subprocess
import sys


def identity(pid):
    proc = pathlib.Path("/proc") / str(pid)
    # /proc/<pid>/stat comm can contain spaces and parentheses. Field 22
    # (starttime) is index 19 after removing fields 1 and 2.
    fields = (proc / "stat").read_text().rsplit(") ", 1)[1].split()
    return fields[19], proc.stat().st_uid, os.readlink(proc / "ns/net")


def stop(pid, ticks, uid, namespace):
    if pid <= 1 or uid <= 0 or not ticks.isdecimal() or not re.fullmatch(r"net:\[\d+\]", namespace):
        raise ValueError("invalid identity")
    try:
        fd = os.pidfd_open(pid)
    except ProcessLookupError:
        return
    try:
        poll = select.poll()
        poll.register(fd, select.POLLIN)
        if poll.poll(0):
            return
        try:
            actual = identity(pid)
        except FileNotFoundError:
            if poll.poll(100):
                return
            raise
        if actual != (ticks, uid, namespace):
            raise ValueError("process identity mismatch")
        # A pidfd pins the process identity even if its numeric PID is reused.
        for sig in (signal.SIGTERM, signal.SIGKILL):
            try:
                signal.pidfd_send_signal(fd, sig)
            except ProcessLookupError:
                return
            if poll.poll(6000):
                return
        raise RuntimeError("application process did not exit")
    finally:
        os.close(fd)


def self_test():
    child = subprocess.Popen([sys.executable, "-c", "import time; time.sleep(60)"])
    try:
        ticks, uid, namespace = identity(child.pid)
        for bad_ticks, bad_uid, bad_namespace in [
            (str(int(ticks) + 1), uid, namespace),
            (ticks, uid + 1, namespace),
            (ticks, uid, "net:[0]"),
        ]:
            try:
                stop(child.pid, bad_ticks, bad_uid, bad_namespace)
            except ValueError:
                pass
            else:
                raise RuntimeError("incorrect identity accepted")
            if child.poll() is not None:
                raise RuntimeError("incorrect identity signaled")
        stop(child.pid, ticks, uid, namespace)
        child.wait(timeout=5)
        print("C9_RUNTIME_PIDFD_GUARD_OK: wrong_start_uid_namespace_refused=true verified_child_stopped=true")
    finally:
        if child.poll() is None:
            child.kill()
            child.wait()


if __name__ == "__main__":
    try:
        if os.environ.get("GITHUB_ACTIONS") != "true" or os.environ.get("RUNNER_OS") != "Linux" or os.getuid() == 0:
            raise ValueError("CI runner required")
        if not hasattr(os, "pidfd_open") or not hasattr(signal, "pidfd_send_signal"):
            raise RuntimeError("pidfd unavailable")
        if sys.argv[1:] == ["--self-test"]:
            self_test()
        elif len(sys.argv) == 4:
            record = pathlib.Path(sys.argv[1]).read_text().split()
            if len(record) != 2 or not all(x.isdecimal() for x in record):
                raise ValueError("invalid PID record")
            stop(int(record[0]), record[1], int(sys.argv[2]), sys.argv[3])
        else:
            raise ValueError("invalid arguments")
    except Exception as exc:
        print("C9_RUNTIME_PIDFD_FAILURE:" + type(exc).__name__, file=sys.stderr)
        sys.exit(1)
