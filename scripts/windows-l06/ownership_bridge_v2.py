"""H03 v2 pure computation transport; parent alone publishes ownership.json.

No file transport, migration, mapping transition or recovery operation here.
Both sibling modules are explicitly loaded from pinned source paths under -I -B.
This is not a sandbox; the trusted Python runtime and dependencies remain gates.
"""
import base64
import hashlib
import types
import json
from pathlib import Path
import sys


def load_sibling(name):
    path = Path(__file__).with_name(name + ".py")
    module = types.ModuleType(name)
    module.__file__ = str(path)
    module.__package__ = ""
    sys.modules[name] = module
    # -B only suppresses bytecode writes. Compile measured source bytes explicitly
    # so a stale or unchecked-hash sibling .pyc cannot replace the pinned source.
    source = path.read_bytes()
    if not 0 < len(source) <= 65536:
        raise ValueError("C9W_JOURNAL_MODULE_SIZE")
    exec(compile(source, str(path), "exec"), module.__dict__)
    return module


v1 = load_sibling("ownership_journal")
v2 = load_sibling("ownership_journal_v2")
SCHEMA = "orgresource.windows-l06-ownership-bridge.v2"
TRANSPORT = "orgresource.windows-l06-ownership-transform.v2"
MAX_REQUEST = 32768
MAX_ENVELOPE = 65536
BASE = {"schema", "operation", "token", "root", "rootId", "controlId", "context"}
EXTRA = {"init": set(), "read": set(),
         "transition": {"expectedSha256", "resource", "state", "fields"}}


def digest(raw):
    return hashlib.sha256(raw).hexdigest()


def validate_request(req):
    v1.require(type(req) is dict)
    op = req.get("operation")
    v1.require(type(op) is str and op in EXTRA)
    v1.shape(req, BASE | EXTRA[op])
    v1.require(req["schema"] == SCHEMA)
    v2.fresh(req["token"], req["root"], req["rootId"], req["controlId"], req["context"])
    # Until the native absence/namespace contracts are accepted, allow only
    # work creation and bounded profile transitions. No deletion effect is performed.
    if op == "transition":
        v1.require(v1.match(req["expectedSha256"], r"[a-f0-9]{64}"))
        resource, state = req["resource"], req["state"]
        v1.require(type(resource) is str and resource in ("work", *v2.ROLES))
        v1.require(type(state) is str and state in v1.STATES)
        if resource == "work":
            v1.require(state in ("intent", "created"))
        v1.require(type(req["fields"]) is dict and
                   set(req["fields"]) <= {"id", "name", "sid", "storage", "evidence"})
    v1.require(len(json.dumps(req, ensure_ascii=True, separators=(",", ":")).encode("ascii")) <= MAX_REQUEST)
    return req


def apply_request(req, current):
    validate_request(req)
    op = req["operation"]
    if op == "init":
        v1.require(current is None)
        new = v2.fresh(req["token"], req["root"], req["rootId"], req["controlId"], req["context"])
    else:
        v1.require(current is not None)
        record = v2.parse(current)  # v1 is refused; never converted
        v1.require(v2.encode(record) == current)  # exact canonical bytes only
        for k in ("token", "root", "rootId", "controlId"):
            v1.require(record["ownership"][k] == req[k])
        v1.require(record["context"] == req["context"])
        if op == "read":
            return None, {"sha256": digest(current), "journal": record}
        v1.require(req["expectedSha256"] == digest(current))
        new = v2.transition(record, req["context"], req["resource"], req["state"], **req["fields"])
    replacement = v2.encode(new)
    return replacement, {"sha256": digest(replacement)}


def transform(raw):
    v1.require(type(raw) is bytes and 0 < len(raw) <= MAX_ENVELOPE)
    env = json.loads(raw.decode("utf-8", errors="strict"), object_pairs_hook=v1.strict_pairs)
    v1.shape(env, ("schema", "request", "current"))
    v1.require(env["schema"] == TRANSPORT)
    encoded = env["current"]
    current = None
    if encoded is not None:
        v1.require(type(encoded) is str and 0 < len(encoded) <= ((v2.MAX_BYTES + 2)//3)*4)
        current = base64.b64decode(encoded, validate=True)
        v1.require(base64.b64encode(current).decode("ascii") == encoded and 0 < len(current) <= v2.MAX_BYTES)
    replacement, response = apply_request(env["request"], current)
    result = {"replacement": None if replacement is None else base64.b64encode(replacement).decode("ascii"),
              "response": response}
    output = json.dumps(result, ensure_ascii=True, separators=(",", ":")).encode("ascii")
    v1.require(len(output) <= MAX_ENVELOPE)
    return output


def main():
    try:
        v1.require(len(sys.argv) == 1)
        sys.stdout.buffer.write(transform(sys.stdin.buffer.read(MAX_ENVELOPE + 1)) + b"\n")
        return 0
    except Exception:
        sys.stderr.write("C9W_JOURNAL_V2_BRIDGE_REFUSED\n")
        return 1


if __name__ == "__main__":
    sys.exit(main())
