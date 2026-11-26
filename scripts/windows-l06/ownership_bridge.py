"""H03 journal computation bridge.

CLI computes from supplied bytes only; never reads or writes journal files.
The native parent holds the lock and performs durable publication in-process.
The file-transport function is retained only for portable fictional tests.
"""
import base64
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


MAX_ENVELOPE = 65536
TRANSPORT = "orgresource.windows-l06-ownership-transform.v1"


def transform(raw):
    """Pure envelope: return proposed replacement bytes, never perform file I/O."""
    _j.require(type(raw) is bytes and 0 < len(raw) <= MAX_ENVELOPE)
    env = json.loads(raw.decode("utf-8", errors="strict"), object_pairs_hook=_j.strict_pairs)
    _j.shape(env, ("schema", "request", "current"))
    _j.require(env["schema"] == TRANSPORT)
    req = validate_request(env["request"])
    _j.require(len(json.dumps(req, ensure_ascii=True, separators=(",", ":")).encode("ascii")) <= MAX_REQUEST)
    encoded = env["current"]
    current = None
    if encoded is not None:
        _j.require(type(encoded) is str and 0 < len(encoded) <= ((_j.MAX_BYTES + 2) // 3) * 4)
        current = base64.b64decode(encoded, validate=True)
        _j.require(base64.b64encode(current).decode("ascii") == encoded)
        _j.require(0 < len(current) <= _j.MAX_BYTES)
    replacement, response = apply_request(req, current)
    proposed = None if replacement is None else base64.b64encode(_j.encode(replacement)).decode("ascii")
    result = {"replacement": proposed, "response": response}
    output = json.dumps(result, ensure_ascii=True, separators=(",", ":")).encode("ascii")
    _j.require(len(output) <= MAX_ENVELOPE)
    return output


def main():
    try:
        # The CLI has no path arguments, file transport, or write authority.
        _j.require(len(sys.argv) == 1)
        result = transform(sys.stdin.buffer.read(MAX_ENVELOPE + 1))
        sys.stdout.buffer.write(result + b"\n")
        return 0
    except Exception:
        sys.stderr.write("C9W_JOURNAL_BRIDGE_REFUSED\n")
        return 1


if __name__ == "__main__":
    sys.exit(main())
