"""H03 v2 durable observation binding, pure SOURCE_ONLY candidate.

A record is evidence data, not authorization. Native observations, secure publication,
namespace exclusivity, actual absence and acceptance are separate gates. The v1
module and its tested bytes are preserved. No conversion of legacy records, no
file writes, no native effects, no automatic recovery plan.
"""
import copy
import json
import ntpath
import ownership_journal as v1

SCHEMA = "orgresource.windows-l06-ownership.v2"
ROLES = v1.ROLES
RESOURCES = ("work", *ROLES, "mapping")
MAX_BYTES = v1.MAX_BYTES
require, shape, match = v1.require, v1.shape, v1.match


def validate_context(ctx):
    shape(ctx, ("userSid", "authenticationId", "sessionId", "logonTime", "bootId"))
    # Canonical decimal user SID, bounded identifier authority and subauthorities.
    require(match(ctx["userSid"], r"S-1-(?:0|[1-9][0-9]{0,14})(?:-(?:0|[1-9][0-9]{0,9})){1,15}"))
    parts = ctx["userSid"].split("-")
    require(int(parts[2]) < 2**48 and all(int(p) < 2**32 for p in parts[3:]))
    require(match(ctx["authenticationId"], r"[a-f0-9]{16}") and ctx["authenticationId"] != "0"*16)
    require(type(ctx["sessionId"]) is int and 0 < ctx["sessionId"] < 2**32)
    # Decimal FILETIME string (avoids cross-language JSON number precision loss).
    require(match(ctx["logonTime"], r"[1-9][0-9]{0,18}") and int(ctx["logonTime"]) < 2**63)
    # Null is explicit unavailable evidence. No fabricated UUID or uptime estimate.
    b = ctx["bootId"]
    require(b is None or (match(b, r"[a-f0-9]{32}") and b != "0"*32))
    return ctx


def storage(value):
    shape(value, ("packagesPath", "packagesId", "packageId", "appDataId"))
    require(v1.root_path(value["packagesPath"]) and ntpath.basename(value["packagesPath"]) == "Packages")
    ids = [value[k] for k in ("packagesId", "packageId", "appDataId")]
    require(all(v1.identity(i) for i in ids))
    require(len(set(ids)) == 3 and len({i.split(":")[0] for i in ids}) == 1)
    return value


def item(base, resource):
    return base["profiles"][resource] if resource in ROLES else base[resource]


def expected_target(base):
    return "\\??\\" + ntpath.join(base["root"], "orgresource-c9w-" + base["token"])


def validate(record):
    shape(record, ("schema", "context", "ownership", "profileStorage", "mappingBinding", "removalEvidence"))
    require(record["schema"] == SCHEMA)
    validate_context(record["context"])
    base = v1.validate(record["ownership"])
    root_id, control_id, work_id = base["rootId"], base["controlId"], base["work"]["id"]
    require(root_id[:16] == control_id[:16] and (work_id is None or work_id[:16] == root_id[:16]))
    shape(record["profileStorage"], ROLES)
    shape(record["removalEvidence"], RESOURCES)
    shared_parent = None
    seen_children = {root_id, control_id, work_id}
    for role in ROLES:
        state = item(base, role)["state"]
        captured = record["profileStorage"][role]
        if state in ("none", "intent"):
            require(captured is None)
        else:
            storage(captured)
            parent = (captured["packagesPath"], captured["packagesId"])
            require(shared_parent is None or shared_parent == parent)
            shared_parent = parent
            require(captured["packagesId"] not in seen_children)
            for k in ("packageId", "appDataId"):
                require(captured[k] not in seen_children)
                seen_children.add(captured[k])
    binding = record["mappingBinding"]
    state = base["mapping"]["state"]
    if state == "none":
        require(binding is None)
    else:
        shape(binding, ("target", "workId", "observedTargets", "mappedWorkId"))
        require(binding["target"] == expected_target(base) and binding["workId"] == work_id)
        require(v1.identity(work_id))
        if state == "intent":
            require(binding["observedTargets"] is None and binding["mappedWorkId"] is None)
        else:
            require(type(binding["observedTargets"]) is list and binding["observedTargets"] == [binding["target"]])
            require(binding["mappedWorkId"] == work_id)
    for resource in RESOURCES:
        state = item(base, resource)["state"]
        evidence = record["removalEvidence"][resource]
        if state != "removed":
            require(evidence is None)
        elif resource in ROLES:
            # Bounded observation only. This is not complete profile/registry absence.
            shape(evidence, ("kind", "hresult", "packagesId"))
            require(evidence["kind"] == "api_success_and_package_absent" and
                    type(evidence["hresult"]) is int and evidence["hresult"] == 0 and
                    evidence["packagesId"] == record["profileStorage"][resource]["packagesId"])
        elif resource == "mapping":
            # This declaration still needs a qualified native absence oracle.
            shape(evidence, ("kind", "drive", "target", "workId"))
            require(evidence["kind"] == "namespace_absence" and
                    evidence["drive"] == base["mapping"]["drive"] and
                    evidence["target"] == binding["target"] and evidence["workId"] == work_id)
        else:
            shape(evidence, ("kind", "id"))
            require(evidence["kind"] == "work_absence" and evidence["id"] == work_id)
    return record


def fresh(token, root, root_id, control_id, context):
    return validate({"schema": SCHEMA, "context": copy.deepcopy(context),
                     "ownership": v1.fresh(token, root, root_id, control_id),
                     "profileStorage": {role: None for role in ROLES},
                     "mappingBinding": None,
                     "removalEvidence": {resource: None for resource in RESOURCES}})


def encode(record):
    raw = json.dumps(validate(record), sort_keys=True, separators=(",", ":"), ensure_ascii=True).encode("ascii")
    require(0 < len(raw) <= MAX_BYTES)
    return raw


def parse(raw):
    require(type(raw) is bytes and 0 < len(raw) <= MAX_BYTES)
    return validate(json.loads(raw.decode("utf-8", errors="strict"), object_pairs_hook=v1.strict_pairs))


def transition(record, context, resource, state, **fields):
    """Retain v1 transition ordering; bind added evidence atomically in one record."""
    new = parse(encode(record))
    require(validate_context(context) == new["context"])
    require(type(resource) is str and resource in RESOURCES and type(state) is str and state in v1.STATES)
    base = new["ownership"]
    expected = set()
    if state == "removed":
        expected = {"evidence"}
    elif (resource, state) == ("work", "created"):
        expected = {"id"}
    elif resource in ROLES and state == "intent":
        expected = {"name"}
    elif resource in ROLES and state == "created":
        expected = {"sid", "storage"}
    elif (resource, state) == ("mapping", "intent"):
        expected = {"drive"}
    elif (resource, state) == ("mapping", "created"):
        expected = {"observedTargets", "mappedWorkId"}
    require(set(fields) == expected)
    legacy = {k: v for k, v in fields.items() if k in {"id", "name", "sid", "drive"}}
    # Existing v1 transition validates state ordering, role uniqueness and dependencies.
    new["ownership"] = v1.transition(base, resource, state, **legacy)
    if resource in ROLES and state == "created":
        new["profileStorage"][resource] = copy.deepcopy(fields["storage"])
    elif (resource, state) == ("mapping", "intent"):
        new["mappingBinding"] = {"target": expected_target(base), "workId": base["work"]["id"],
                                 "observedTargets": None, "mappedWorkId": None}
    elif (resource, state) == ("mapping", "created"):
        new["mappingBinding"]["observedTargets"] = copy.deepcopy(fields["observedTargets"])
        new["mappingBinding"]["mappedWorkId"] = fields["mappedWorkId"]
    if state == "removed":
        new["removalEvidence"][resource] = copy.deepcopy(fields["evidence"])
    return validate(new)


def compare_observation(record, observed):
    """Exact comparison, NEVER an executable cleanup selection.

    Null boot witness, legacy data, unknown/partial state and mismatches refuse.
    Caller-provided assertions do not become trusted observations by passing this function.
    """
    validate(record)
    base, context = record["ownership"], record["context"]
    shape(observed, ("token", "root", "rootId", "controlId", "context", "workId", "profiles", "mapping"))
    require(context["bootId"] is not None and validate_context(observed["context"]) == context)
    for field in ("token", "root", "rootId", "controlId"):
        require(observed[field] == base[field])
    work = base["work"]
    require(work["state"] in ("created", "removed"))
    require(observed["workId"] == (work["id"] if work["state"] == "created" else None))
    profiles = {}
    for role in ROLES:
        p = item(base, role)
        require(p["state"] not in ("intent", "removing"))
        if p["state"] == "created":
            profiles[p["name"]] = {"sid": p["sid"], "storage": record["profileStorage"][role]}
    require(type(observed["profiles"]) is dict and observed["profiles"] == profiles)
    m = base["mapping"]
    require(m["state"] not in ("intent", "removing"))
    mapping = None if m["state"] != "created" else dict(record["mappingBinding"], drive=m["drive"])
    require(observed["mapping"] == mapping)
    # Do not conflate equality of supplied data with an OS-level absence/ownership proof.
    return {"identityComparison": "MATCH", "effectsAuthorized": False, "workDeletion": False,
            "completeProfileAbsenceProven": False}
