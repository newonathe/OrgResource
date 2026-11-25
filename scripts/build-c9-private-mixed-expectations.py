#!/usr/bin/env python3
"""Transform guarded private SQL projection into an independent API expectation.
No network/database access, no values on stdout, never invoke with real data in CI.
"""
import hashlib
import importlib.util
import json
import os
import pathlib
import re
import stat
import sys

SOURCE = "57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2"
PLAN = "3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c"
spec = importlib.util.spec_from_file_location("oracle", pathlib.Path(__file__).with_name("verify-c9-private-mixed-api.py"))
oracle = importlib.util.module_from_spec(spec)
spec.loader.exec_module(oracle)
require = oracle.require

def stable_detail(row):
    for k in ("totalQuantity", "version", "ledgerTotal", "ledgerIssued", "issued", "approvedCount"):
        require(type(row[k]) is int)
    total, issued = row["totalQuantity"], row["issued"]
    require(0 <= total <= 1000000 and row["version"] >= 0 and row["approvedCount"] >= 0 and issued >= 0)
    require(row["ledgerTotal"] == total and row["ledgerIssued"] == issued)
    require(row["type"] in ("REUSABLE", "CONSUMABLE"))
    require(row["type"] == "REUSABLE" or issued == 0)
    require(total >= issued)
    if row["approvedCount"]:
        return None
    return {k: row[k] for k in ("id", "name", "type", "totalQuantity", "version")} | {
        "availableQuantity": total - issued if row["type"] == "REUSABLE" else total
    }

def build(rows, schema, server_uuid, owner_id, original_id, fingerprint):
    kinds = {"server", "owner", "org", "resource"}
    require(all(isinstance(r, dict) and r.get("kind") in kinds for r in rows))
    servers = [r for r in rows if r["kind"] == "server"]
    owners = [r for r in rows if r["kind"] == "owner"]
    orgs = [r for r in rows if r["kind"] == "org"]
    resources = [r for r in rows if r["kind"] == "resource"]
    require(re.fullmatch(r"orgresource_c9_local_[0-9a-f]{12}_mixed", schema) is not None)
    require(oracle.canonical_uuid(server_uuid) and oracle.canonical_uuid(owner_id) and oracle.canonical_uuid(original_id))
    require(hashlib.sha256((schema + "\0" + server_uuid).encode()).hexdigest() == fingerprint)
    require(servers == [{"kind": "server", "schema": schema, "uuid": server_uuid, "version": "8.0.46"}])
    require(owners == [{"kind": "owner", "id": owner_id, "status": "ACTIVE", "credentialed": 1}])
    require(len(orgs) == 98 and len({r["id"] for r in orgs}) == 98 and all(oracle.canonical_uuid(r["id"]) for r in orgs))
    require(len({r["slug"] for r in orgs}) == 98)
    tagged = [r for r in orgs if r["dataset"] is not None]
    originals = [r for r in orgs if r["dataset"] is None]
    require(len(tagged) == 96 and len(originals) == 2)
    require(all(r["source"] == SOURCE and r["plan"] == PLAN and r["status"] == "ACTIVE" for r in tagged))
    require(len({r["reference"] for r in tagged}) == 96 and all(r["reference"] for r in tagged))
    require(len({r["dataset"] for r in tagged}) == len({r["asOf"] for r in tagged}) == 1)
    require(all(all(r[k] is None for k in ("source", "plan", "reference", "asOf")) for r in originals))
    original = next(r for r in originals if r["id"] == original_id and r["slug"] == "isolated-original-workspace")
    foreign = next(r for r in originals if r["slug"] == "isolated-other-original")
    require(original["id"] != foreign["id"] and original["status"] == foreign["status"] == "ACTIVE")
    owned = tagged + [original]
    require(all(r["ownerRole"] == "OWNER" and r["ownerStatus"] == "ACTIVE" for r in owned))
    require(foreign["ownerRole"] is None and foreign["ownerStatus"] is None)
    require(len({r["dbId"] for r in resources}) == len({r["id"] for r in resources}) == len(resources))
    require(all(type(r["dbId"]) is int and r["dbId"] > 0 and oracle.canonical_uuid(r["id"]) for r in resources))
    by_org = {r["id"]: [] for r in orgs}
    for row in resources:
        by_org[row["org"]].append(row)
    require(not by_org[foreign["id"]])
    def select(org):
        items = sorted(by_org[org["id"]], key=lambda r: r["dbId"])
        if not 0 < len(items) <= 100:
            return None
        details = [d for row in items if (d := stable_detail(row)) is not None]
        if not details:
            return None
        return {k: org[k] for k in ("id", "slug", "displayName")} | {
            "workspaceIds": [r["dbId"] for r in items],
            "modernIds": [r["id"] for r in items],
            "detail": details[0]
        }
    # Representative chosen deterministically, not based on an HTTP result.
    original_view = select(original)
    demo = next((view for org in sorted(tagged, key=lambda r: r["id"]) if (view := select(org)) is not None), None)
    require(original_view is not None and demo is not None)
    result = {
        "metadata": {"synthetic": True, "datasetId": tagged[0]["dataset"], "demonstrationAsOf": tagged[0]["asOf"], "notice": oracle.NOTICE},
        "ownerAccountId": owner_id,
        "ownerOrgIds": sorted(r["id"] for r in owned),
        "original": original_view, "demo": demo, "foreignOrgId": foreign["id"]
    }
    return oracle.validate_expected(result)

def parse_projection(text):
    # MySQL raw batch uses LF record separators. JSON strings may contain
    # literal Unicode line separators; splitlines() would corrupt them.
    return [json.loads(line) for line in text.split("\n") if line]

def run():
    require(os.getuid() != 0 and os.environ.get("GITHUB_ACTIONS") != "true")
    require(os.environ.get("C9_CONTROL_PRIVATE_MIXED") == "true")
    projection = oracle.private_file("C9_MIXED_PROJECTION")
    require(0 < projection.stat().st_size <= 16 * 1024 * 1024)
    target = pathlib.Path(os.environ["C9_MIXED_EXPECTED"])
    require(target.is_absolute() and target.parent == projection.parent and target != projection)
    rows = parse_projection(projection.read_text(encoding="utf-8"))
    doc = build(rows, os.environ["C9_MIXED_SCHEMA"], os.environ["C9_MIXED_SERVER_UUID"],
                os.environ["C9_MIXED_OWNER"], os.environ["C9_MIXED_ORIGINAL"], os.environ["C9_MIXED_FINGERPRINT"])
    with os.fdopen(os.open(target, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600), "w", encoding="utf-8") as out:
        json.dump(doc, out, ensure_ascii=False)
    print("R06B3B_MIXED_EXPECTATIONS_OK:exact_owner_scope=true independent_sql=true stable_detail=true", flush=True)

if __name__ == "__main__":
    try:
        run()
    except Exception as exc:
        print("R06B3B_MIXED_EXPECTATIONS_FAILED:" + type(exc).__name__, file=sys.stderr)
        sys.exit(1)
