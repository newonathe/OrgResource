"""H03 bounded journal bridge.

Only the verified native parent may invoke the Windows CLI while holding C9ControlLock.
This process does NOT independently establish that lock or authorize resource effects.
It never creates/deletes a work directory, profile, drive mapping or process.
"""
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import stat
import sys

# Explicit sibling import also works under Python isolated mode (-I).
_spec = importlib.util.spec_from_file_location(
    "c9_ownership_journal", Path(__file__).with_name("ownership_journal.py")
)
_j = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_j)
SCHEMA = "orgresource.windows-l06-ownership-bridge.v1"
MAX_REQUEST = 32768
BASE = {"schema", "operation", "token", "root", "rootId", "controlId"}
EXTRA = {
    "init": set(),
    "read": set(),
    "transition": {"expectedSha256", "resource", "state", "fields"},
    "plan": {"expectedSha256", "observed"},
}


def digest(raw):
    return hashlib.sha256(raw).hexdigest()


def validate_request(req):
    _j.require(type(req) is dict)
    op = req.get("operation")
    _j.require(type(op) is str and op in EXTRA)
    _j.shape(req, BASE | EXTRA[op])
    _j.require(req["schema"] == SCHEMA)
    _j.fresh(req["token"], req["root"], req["rootId"], req["controlId"])
    if op in ("transition", "plan"):
        _j.require(_j.match(req["expectedSha256"], r"[a-f0-9]{64}"))
    if op == "transition":
        _j.require(type(req["resource"]) is str and req["resource"] in (*_j.ROLES, "work", "mapping"))
        _j.require(type(req["state"]) is str and req["state"] in _j.STATES)
        _j.require(type(req["fields"]) is dict)
        # Avoid collisions with positional arguments and reject non-schema keys.
        _j.require(set(req["fields"]) <= {"id", "name", "sid", "drive"})
    return req


def parse_request(raw):
    _j.require(type(raw) is bytes and 0 < len(raw) <= MAX_REQUEST)
    return validate_request(json.loads(raw.decode("utf-8", errors="strict"), object_pairs_hook=_j.strict_pairs))


def plain(info, directory):
    if not (stat.S_ISDIR(info.st_mode) if directory else stat.S_ISREG(info.st_mode)):
        return False
    if getattr(info, "st_file_attributes", 0) & 0x441400:
        return False
    if not directory and (info.st_nlink != 1 or not 0 < info.st_size <= _j.MAX_BYTES):
        return False
    return True


def same_file(a, b):
    return (a.st_ino != 0 and b.st_ino != 0 and
            (a.st_dev, a.st_ino, a.st_size) == (b.st_dev, b.st_ino, b.st_size))


def read_current(path):
    try:
        before = path.lstat()
    except FileNotFoundError:
        return None
    _j.require(plain(before, False))
    with path.open("rb") as f:
        opened = os.fstat(f.fileno())
        _j.require(plain(opened, False) and same_file(before, opened))
        raw = f.read(_j.MAX_BYTES + 1)
        after = os.fstat(f.fileno())
        _j.require(plain(after, False) and same_file(opened, after) and len(raw) == after.st_size)
    end = path.lstat()
    _j.require(plain(end, False) and same_file(after, end))
    _j.parse(raw)
    return raw


def bind(record, req):
    for field in ("token", "root", "rootId", "controlId"):
        _j.require(record[field] == req[field])


def apply_request(req, current):
    """Pure transition, returns (replacement record or None, response)."""
    validate_request(req)
    op = req["operation"]
    if op == "init":
        _j.require(current is None)
        record = _j.fresh(req["token"], req["root"], req["rootId"], req["controlId"])
        return record, {"sha256": digest(_j.encode(record))}
    _j.require(current is not None)
    record = _j.parse(current)
    bind(record, req)
    old_sha = digest(current)
    if op == "read":
        return None, {"sha256": old_sha, "journal": record}
    _j.require(req["expectedSha256"] == old_sha)
    if op == "transition":
        new = _j.transition(record, req["resource"], req["state"], **req["fields"])
        return new, {"sha256": digest(_j.encode(new))}
    return None, {"sha256": old_sha, "plan": _j.recovery_plan(record, req["observed"])}


def execute_request(req, path):
    """File transport under the caller-held, independently verified control lock."""
    validate_request(req)
    path = Path(path)
    _j.require(path.name == "ownership.json" and plain(path.parent.lstat(), True))
    current = read_current(path)
    replacement, response = apply_request(req, current)
    if replacement is not None:
        # Recheck immediately before publication. This is not an OS-level CAS:
        # serialization requires the native parent's held lock.
        _j.require(read_current(path) == current)
        _j.save(path, replacement, initial=current is None)
        _j.require(read_current(path) == _j.encode(replacement))
    return response


def main():
    try:
        _j.require(os.name == "nt")
        req = parse_request(sys.stdin.buffer.read(MAX_REQUEST + 1))
        root = Path(req["root"])
        control = root / ("orgresource-c9w-control-" + req["token"])
        _j.require(plain(root.lstat(), True) and plain(control.lstat(), True))
        # Never create the parent or lock. The native parent must hold it;
        # this metadata check is only an additional shape refusal.
        lock_info = (control / "ownership.lock").lstat()
        _j.require(stat.S_ISREG(lock_info.st_mode) and lock_info.st_nlink == 1
                   and lock_info.st_size == 0 and not (getattr(lock_info, "st_file_attributes", 0) & 0x441400))
        result = execute_request(req, control / "ownership.json")
        # Parent captures privately. No exception text, paths or input echoes.
        sys.stdout.write(json.dumps(result, ensure_ascii=True, separators=(",", ":")) + "\n")
        return 0
    except Exception:
        sys.stderr.write("C9W_JOURNAL_BRIDGE_REFUSED\n")
        return 1


if __name__ == "__main__":
    sys.exit(main())
