"""H03 ownership records and recovery selection; no OS deletion or process control.
Native caller must hold its independent control lock and supply verified observations.
"""
import json
import ntpath
import os
import re
import uuid
from pathlib import Path

SCHEMA = "orgresource.windows-l06-ownership.v1"
ROLES = ("source", "restore", "mixed", "jobtest")
STATES = {"none", "intent", "created", "removing", "removed"}
DRIVES = {"Q:", "R:", "U:", "V:", "W:"}
MAX_BYTES = 16384


def require(ok):
    if not ok:
        raise ValueError("C9W_RECOVER_REFUSED_OWNERSHIP")


def shape(obj, keys):
    require(type(obj) is dict and set(obj) == set(keys))


def match(value, pattern):
    return type(value) is str and re.fullmatch(pattern, value, re.ASCII) is not None


def identity(value):
    # Native adapter must obtain volume/file identifiers from real handles.
    return match(value, r"[0-9a-f]{16}:[0-9a-f]{32}")


def root_path(value):
    if not match(value, r"[A-Za-z]:\\[^\x00-\x1f/:*?\"<>|]+"):
        return False
    parts = value[3:].split("\\")
    return all(p and p not in (".", "..") and not p.endswith((" ", ".")) for p in parts) and ntpath.normpath(value) == value


def strict_pairs(pairs):
    result, folded = {}, set()
    for key, value in pairs:
        require(key.casefold() not in folded)
        folded.add(key.casefold())
        result[key] = value
    return result


def parse(raw):
    require(type(raw) is bytes and 0 < len(raw) <= MAX_BYTES)
    return validate(json.loads(raw.decode("utf-8", errors="strict"), object_pairs_hook=strict_pairs))


def validate(j):
    shape(j, ("schema", "token", "root", "rootId", "controlId", "work", "profiles", "mapping"))
    require(j["schema"] == SCHEMA and match(j["token"], r"[a-f0-9]{12}"))
    require(root_path(j["root"]) and identity(j["rootId"]) and identity(j["controlId"]))
    require(j["rootId"] != j["controlId"])
    work = j["work"]
    shape(work, ("state", "id"))
    require(type(work["state"]) is str and work["state"] in STATES)
    require(work["id"] is None if work["state"] in {"none", "intent"} else identity(work["id"]))
    require(work["id"] not in (j["rootId"], j["controlId"]))
    shape(j["profiles"], ROLES)
    names, sids = set(), set()
    for p in j["profiles"].values():
        shape(p, ("state", "name", "sid"))
        require(type(p["state"]) is str and p["state"] in STATES)
        if p["state"] == "none":
            require(p["name"] is None and p["sid"] is None)
            continue
        require(match(p["name"], r"orgresource\.c9\.[a-f0-9]{12}") and p["name"] not in names)
        names.add(p["name"])
        if p["state"] == "intent":
            require(p["sid"] is None)
        else:
            require(match(p["sid"], r"S-1-15-2(?:-[0-9]{1,10}){7}") and p["sid"] not in sids)
            require(all(int(x) <= 4294967295 for x in p["sid"].split("-")[4:]))
            sids.add(p["sid"])
    m = j["mapping"]
    shape(m, ("state", "drive"))
    require(type(m["state"]) is str and m["state"] in STATES)
    require(m["drive"] is None if m["state"] == "none" else type(m["drive"]) is str and m["drive"] in DRIVES)
    resources = [*j["profiles"].values(), m]
    if work["state"] in {"none", "intent"}:
        require(all(r["state"] == "none" for r in resources))
    if work["state"] in {"removing", "removed"}:
        require(all(r["state"] in {"none", "removed"} for r in resources))
    return j


def fresh(token, root, root_id, control_id):
    return validate({"schema": SCHEMA, "token": token, "root": root, "rootId": root_id,
                     "controlId": control_id, "work": {"state": "none", "id": None},
                     "profiles": {r: {"state": "none", "name": None, "sid": None} for r in ROLES},
                     "mapping": {"state": "none", "drive": None}})


def transition(j, resource, state, **fields):
    # Return a new record; validate entire record before and after; immutable identities.
    j = parse(encode(j))
    require(resource in (*ROLES, "work", "mapping"))
    item = j["profiles"][resource] if resource in ROLES else j[resource]
    old = item["state"]
    require((old, state) in {("none", "intent"), ("intent", "created"),
                             ("created", "removing"), ("removing", "removed")})
    allowed = {("work", "created"): {"id"}, ("mapping", "intent"): {"drive"}}
    if resource in ROLES:
        expected = {"name"} if state == "intent" else {"sid"} if state == "created" else set()
    else:
        expected = allowed.get((resource, state), set())
    require(set(fields) == expected)
    item.update(fields, state=state)
    return validate(j)


def encode(j):
    return json.dumps(validate(j), sort_keys=True, separators=(",", ":"), ensure_ascii=True).encode("ascii")


def save(path, j, *, initial=False):
    """Atomic snapshot publication under a caller-held lock in a verified control dir.
    Native ACL, lock ownership, handle identity and power-loss durability need real tests.
    Never delete a pre-existing staging file or overwrite an initial record.
    """
    path = Path(path)
    raw = encode(j)
    tmp = path.with_name(path.name + "." + uuid.uuid4().hex + ".tmp")
    if initial and path.exists():
        raise FileExistsError(path.name)
    with tmp.open("xb") as f:
        f.write(raw)
        f.flush()
        os.fsync(f.fileno())
    if initial:
        # Exclusive publication; no replacing a record even if caller violates lock protocol.
        os.link(tmp, path)
        tmp.unlink()
    else:
        if not path.is_file():
            raise FileNotFoundError(path.name)
        os.replace(tmp, path)


def recovery_plan(j, observed):
    """Select identities, never perform effects. Observations must be native/lock-bound.
    Exact known active profiles and mapping only; an ambiguous intent/removal stops all.
    Completed/removed resources must be absent; missing created resources are ambiguous.
    """
    validate(j)
    shape(observed, ("token", "root", "rootId", "controlId", "workId", "profiles", "mapping"))
    for k in ("token", "root", "rootId", "controlId"):
        require(observed[k] == j[k])
    work = j["work"]
    require(work["state"] in {"created", "removed"})
    require(observed["workId"] == (work["id"] if work["state"] == "created" else None))
    profiles = {}
    for p in j["profiles"].values():
        require(p["state"] not in {"intent", "removing"})
        if p["state"] == "created":
            profiles[p["name"]] = p["sid"]
    require(type(observed["profiles"]) is dict and observed["profiles"] == profiles)
    m = j["mapping"]
    require(m["state"] not in {"intent", "removing"})
    mapping = {"drive": m["drive"], "target": ntpath.join(j["root"], "orgresource-c9w-" + j["token"])} if m["state"] == "created" else None
    require(observed["mapping"] == mapping)
    return {"profiles": profiles, "mapping": mapping, "workDeletion": False}
