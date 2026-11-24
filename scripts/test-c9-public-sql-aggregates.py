#!/usr/bin/env python3
"""Independent literal aggregate oracle for public third-server fixture only."""
import importlib.util
import os
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("sql_oracle", ROOT / "build-c9-private-mixed-expectations.py")
oracle = importlib.util.module_from_spec(spec)
spec.loader.exec_module(oracle)

def main():
    assert os.environ.get("GITHUB_ACTIONS") == "true" and os.getuid() != 0
    projection, schema, server = sys.argv[1:]
    rows = oracle.parse_projection(pathlib.Path(projection).read_text(encoding="utf-8"))
    assert len(rows) == 7
    assert [r for r in rows if r["kind"] == "server"] == [
        dict(kind="server", schema=schema, uuid=server, version="8.0.46")]
    orgs = {r["slug"]:r for r in rows if r["kind"] == "org"}
    assert set(orgs) == {"projection-alpha","projection-beta"}
    alpha,beta = orgs["projection-alpha"],orgs["projection-beta"]
    assert alpha["id"] != beta["id"]
    assert all(all(r[k] is None for k in ("dataset","reference","source","plan","asOf")) for r in orgs.values())
    assert (alpha["ownerRole"],alpha["ownerStatus"],beta["ownerRole"],beta["ownerStatus"]) == ("OWNER","ACTIVE",None,None)
    owner = [r for r in rows if r["kind"] == "owner"]
    assert len(owner) == 1 and owner[0]["status"] == "ACTIVE" and owner[0]["credentialed"] == 0
    resources = [r for r in rows if r["kind"] == "resource"]
    assert [r["dbId"] for r in resources] == [1,2,3]
    assert len({r["id"] for r in resources}) == 3
    expected = [
        (alpha["id"],"Projection supply","REUSABLE",10,4,10,3,3,0,7),
        (alpha["id"],"Projection paper","CONSUMABLE",10,2,10,0,0,2,None),
        (beta["id"],"Projection supply","CONSUMABLE",7,2,7,0,0,0,7),
    ]
    keys = ("org","name","type","totalQuantity","version","ledgerTotal","ledgerIssued","issued","approvedCount")
    for row, want in zip(resources,expected):
        assert tuple(row[k] for k in keys) == want[:-1]
        detail = oracle.stable_detail(row)
        if want[-1] is None:
            assert detail is None
        else:
            assert detail == dict(id=row["id"],name=want[1],type=want[2],totalQuantity=want[3],
                                  version=want[4],availableQuantity=want[-1])
    print("R06B3B_PUBLIC_SQL_AGGREGATES_OK:actual_mysql=true signed_deltas=true issued_returned=true consumed=true approved_count=true tenant_separate=true")
if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print("R06B3B_PUBLIC_SQL_AGGREGATES_FAILED:"+type(exc).__name__,file=sys.stderr)
        sys.exit(1)
