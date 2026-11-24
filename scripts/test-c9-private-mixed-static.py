#!/usr/bin/env python3
"""Public-safe static and small pure negative checks. No fake full cohort."""
import ast
import importlib.util
import os
import pathlib
import subprocess
import sys
import tempfile

ROOT = pathlib.Path(__file__).resolve().parent

def load(filename, name):
    spec = importlib.util.spec_from_file_location(name, ROOT / filename)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

def reject(action):
    try:
        action()
    except (ValueError, OSError, KeyError, TypeError):
        return
    raise AssertionError("negative accepted")

def main():
    assert os.environ.get("GITHUB_ACTIONS") == "true"
    for name in ("verify-c9-private-mixed-api.py", "build-c9-private-mixed-expectations.py", "stop-c9-private-mixed-process.py"):
        ast.parse((ROOT / name).read_text())
    oracle = load("build-c9-private-mixed-expectations.py", "sql_oracle")
    assert oracle.parse_projection('{"name":"Fictional\u2028name"}\n') == [{"name":"Fictional\u2028name"}]
    row = dict(id="fictional", name="fictional", type="REUSABLE", totalQuantity=5,
               version=0, ledgerTotal=5, ledgerIssued=2, issued=2, approvedCount=0)
    assert oracle.stable_detail(row)["availableQuantity"] == 3
    assert oracle.stable_detail(dict(row, approvedCount=1)) is None
    for bad in (dict(row, ledgerTotal=4), dict(row, ledgerIssued=0), dict(row, type="CONSUMABLE"), dict(row, totalQuantity=-1)):
        reject(lambda: oracle.stable_detail(bad))
    reject(lambda: oracle.build([], "wrong", "wrong", "wrong", "wrong", "wrong"))
    guard = load("stop-c9-private-mixed-process.py", "local_guard")
    with tempfile.TemporaryDirectory() as tmp:
        root = pathlib.Path(tmp)
        work = root / "orgresource-c9-local-0123456789ab"
        work.mkdir(mode=0o700)
        record = work / "mixed-runtime.pid"
        record.write_text("1 123\n")
        record.chmod(0o600)
        reject(lambda: guard.read_record(work))
        record.write_text("99999 123\n")
        record.chmod(0o644)
        reject(lambda: guard.read_record(work))
        record.unlink()
        record.symlink_to(root / "missing")
        reject(lambda: guard.read_record(work))
    # Actual CI invocation refuses before any private file/PID work.
    for name, args, marker in (
        ("stop-c9-private-mixed-process.py", ["--self-test"], "R06B3B_LOCAL_PIDFD_REFUSED:"),
        ("verify-c9-private-mixed-api.py", ["login"], "R06B3B_MIXED_API_FAILED:INTAKE"),
        ("build-c9-private-mixed-expectations.py", [], "R06B3B_MIXED_EXPECTATIONS_FAILED:"),
    ):
        result = subprocess.run([sys.executable, str(ROOT / name), *args], capture_output=True, text=True, timeout=10)
        assert result.returncode != 0 and marker in result.stderr
    print("R06B3B_PRIVATE_STATIC_REFUSALS_OK:python_ast=true pure_balance=true malformed_pid_records=true actual_ci_private_refused=true")

if __name__ == "__main__":
    main()
