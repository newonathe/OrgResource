#!/usr/bin/env python3
"""Local disposable mixed-runtime PIDFD guard, not a generic process killer.
The guarded parent must independently verify its created Docker ID/labels/UUID.
"""
import importlib.util
import os
import pathlib
import re
import select
import signal
import stat
import sys

spec = importlib.util.spec_from_file_location("c9_pidfd", pathlib.Path(__file__).with_name("stop-c9-isolated-process.py"))
common = importlib.util.module_from_spec(spec)
spec.loader.exec_module(common)

def require(condition):
    if not condition:
        raise ValueError("guard refused")

def private_work(path):
    require(path.is_absolute() and path.resolve(strict=True) == path)
    require(re.fullmatch(r"orgresource-c9-local-[a-f0-9]{12}", path.name) is not None)
    for directory in (path, path.parent):
        info = directory.lstat()
        require(stat.S_ISDIR(info.st_mode) and info.st_uid == os.getuid() and stat.S_IMODE(info.st_mode) == 0o700)

def read_record(work):
    private_work(work)
    record = work / "mixed-runtime.pid"
    fd = os.open(record, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK)
    with os.fdopen(fd, "r", encoding="ascii") as file:
        info = os.fstat(file.fileno())
        require(stat.S_ISREG(info.st_mode) and info.st_uid == os.getuid() and stat.S_IMODE(info.st_mode) == 0o600 and 0 < info.st_size <= 128)
        words = file.read(129).split()
    require(len(words) == 2 and all(re.fullmatch(r"[0-9]+", word) for word in words))
    pid, ticks = int(words[0]), words[1]
    require(pid > 1 and pid != os.getpid() and pid != os.getppid())
    return pid, ticks

def command_matches(pid, jar, java):
    proc = pathlib.Path("/proc") / str(pid)
    cmd = (proc / "cmdline").read_bytes().split(b"\0")
    require(cmd and cmd[-1] == b"")
    cmd.pop()
    require(cmd == [os.fsencode(java), b"-Xmx768m", b"-Duser.timezone=Asia/Manila",
                    b"-jar", os.fsencode(jar), b"--spring.flyway.enabled=false"])
    require((proc / "exe").resolve(strict=True) == java.resolve(strict=True))

def stop(work, namespace, java):
    pid, ticks = read_record(work)
    jar = work / "verified.jar"
    info = jar.lstat()
    require(stat.S_ISREG(info.st_mode) and info.st_uid == os.getuid() and stat.S_IMODE(info.st_mode) == 0o600)
    require(java.is_absolute() and java.is_file())
    require(re.fullmatch(r"net:\[\d+\]", namespace) is not None and namespace != os.readlink("/proc/self/ns/net"))
    try:
        fd = os.pidfd_open(pid)
    except ProcessLookupError:
        # No live numeric process to signal. Parent still waits for supervisor.
        return
    try:
        poll = select.poll()
        poll.register(fd, select.POLLIN)
        if poll.poll(0):
            return
        try:
            require(common.identity(pid) == (ticks, os.getuid(), namespace))
            command_matches(pid, jar, java)
        except FileNotFoundError:
            if poll.poll(100):
                return
            raise
        for sig in (signal.SIGTERM, signal.SIGKILL):
            try:
                signal.pidfd_send_signal(fd, sig)
            except ProcessLookupError:
                return
            if poll.poll(6000):
                return
        raise RuntimeError("process did not exit")
    finally:
        os.close(fd)

def run():
    require(sys.platform == "linux" and os.getuid() != 0 and os.environ.get("GITHUB_ACTIONS") != "true")
    require(hasattr(os, "pidfd_open") and hasattr(signal, "pidfd_send_signal"))
    if sys.argv[1:] == ["--self-test"]:
        # Only a newly spawned owned child; no arbitrary PID input.
        common.self_test()
        print("R06B3B_LOCAL_PIDFD_SELF_TEST_OK", flush=True)
    elif len(sys.argv) == 4:
        stop(pathlib.Path(sys.argv[1]), sys.argv[2], pathlib.Path(sys.argv[3]))
        print("R06B3B_LOCAL_PIDFD_STOP_OK", flush=True)
    else:
        raise ValueError("arguments")

if __name__ == "__main__":
    try:
        run()
    except Exception as exc:
        print("R06B3B_LOCAL_PIDFD_REFUSED:" + type(exc).__name__, file=sys.stderr)
        sys.exit(1)
