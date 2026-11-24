#!/usr/bin/env python3
"""Verify actual read-only projection of the restored PUBLIC two-original fixture."""
import importlib.util
import json
import os
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("sql_oracle", ROOT / "build-c9-private-mixed-expectations.py")
oracle = importlib.util.module_from_spec(spec)
spec.loader.exec_module(oracle)

def main():
    assert os.environ.get("GITHUB_ACTIONS") == "true" and os.getuid() != 0
    projection, schema, server, owner, original, foreign, resource = sys.argv[1:]
    rows = oracle.parse_projection(pathlib.Path(projection).read_text(encoding="utf-8"))
    assert len(rows) == 5
    by_kind = {kind: [row for row in rows if row["kind"] == kind] for kind in ("server","owner","org","resource")}
    assert by_kind["server"] == [{"kind":"server","schema":schema,"uuid":server,"version":"8.0.46"}]
    assert by_kind["owner"] == [{"kind":"owner","id":owner,"status":"ACTIVE","credentialed":1}]
    assert len(by_kind["org"]) == 2
    orgs = {row["id"]: row for row in by_kind["org"]}
    assert set(orgs) == {original, foreign}
    for oid, slug, name, role, status in (
        (original,"isolated-original-workspace","Isolated Original Workspace","OWNER","ACTIVE"),
        (foreign,"isolated-other-original","Other Original Org",None,None),
    ):
        assert orgs[oid] == dict(kind="org",id=oid,slug=slug,displayName=name,status="ACTIVE",
            dataset=None,reference=None,source=None,plan=None,asOf=None,ownerRole=role,ownerStatus=status)
    assert len(by_kind["resource"]) == 1
    row = by_kind["resource"][0]
    assert type(row["dbId"]) is int and row["dbId"] > 0
    assert row == dict(kind="resource",org=original,dbId=row["dbId"],id=resource,name="Existing fictional projector",
        type="REUSABLE",totalQuantity=0,version=0,ledgerTotal=0,ledgerIssued=0,issued=0,approvedCount=0)
    assert oracle.stable_detail(row) == dict(id=resource,name="Existing fictional projector",type="REUSABLE",totalQuantity=0,version=0,availableQuantity=0)
    # Prove the real original-only projection is rejected as a full mixed cohort,
    # even when the schema/fingerprint check is made to pass in-memory. No SQL
    # rows or approved tags are invented or changed.
    import hashlib
    mixed_schema = schema.removesuffix("_restore") + "_mixed"
    mixed_rows = [dict(r,schema=mixed_schema) if r["kind"]=="server" else r for r in rows]
    fingerprint = hashlib.sha256((mixed_schema+"\0"+server).encode()).hexdigest()
    try:
        oracle.build(mixed_rows,mixed_schema,server,owner,original,fingerprint)
    except ValueError:
        pass
    else:
        raise AssertionError("original-only accepted as mixed")
    print("R06B3B_PUBLIC_SQL_PROJECTION_OK:actual_mysql=true original_only=true exact_owner_resource=true mixed_rejected=true")

if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print("R06B3B_PUBLIC_SQL_PROJECTION_FAILED:" + type(exc).__name__,file=sys.stderr)
        sys.exit(1)
