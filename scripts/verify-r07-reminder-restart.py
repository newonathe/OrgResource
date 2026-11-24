#!/usr/bin/env python3
"""Isolated Stage1D post-restore reminder-restart fixture/read; no transport."""
import datetime
import importlib.util
import json
import os
import pathlib
import sys
import time
from zoneinfo import ZoneInfo

def check(condition, code):
    if not condition:
        raise RuntimeError(code)

def main():
    check(os.environ.get("C9_RUNTIME_ISOLATED") == "true", "ISOLATION")
    check(os.environ.get("EXT_SMS_ENABLED") == "false" and os.environ.get("EXT_REMINDER_MODE") == "DISABLED", "DISABLED_TRANSPORT")
    check(len(sys.argv) == 2 and sys.argv[1] in ("seed", "read"), "MODE")
    spec = importlib.util.spec_from_file_location("c9_runtime_api", pathlib.Path(__file__).with_name("verify-c9-recovered-api.py"))
    api = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(api)
    state = pathlib.Path(os.environ["C9_RUNTIME_STATE"]).with_name("reminder-ids.json")
    owner = api.Client().login(api.OWNER, os.environ["C9_RUNTIME_OWNER_PASSWORD"])
    owner.select(os.environ["C9_RUNTIME_ORG"])
    if sys.argv[1] == "seed":
        dept, _ = owner.request("POST", "/api/departments", 201, {"name":"Fictional restart reminder department"})
        resource, _ = owner.request("POST", "/api/resources", 201, {"name":"Fictional restart reminder resource","type":"REUSABLE","totalQuantity":3})
        start = datetime.datetime.now(ZoneInfo("Asia/Manila")) + datetime.timedelta(seconds=10)
        end = start + datetime.timedelta(hours=2)
        project, _ = owner.request("POST", "/api/projects", 201, {"departmentId":dept["id"],"name":"Fictional restart reminder project","startAt":start.replace(tzinfo=None).isoformat(timespec="seconds"),"endAt":end.replace(tzinfo=None).isoformat(timespec="seconds")})
        requirement, _ = owner.request("POST", "/api/requirements", 201, {"projectId":project["id"],"resourceId":resource["id"],"quantity":3})
        ids = []
        for suffix in (11,12,13):
            allocation, _ = owner.request("POST", "/api/allocations", 201, {"requirementId":requirement["id"],"quantity":1,"borrowerName":f"Fictional restart {suffix}","phoneNumber":f"+155555501{suffix}","smsConsent":True})
            aid = allocation["id"]
            ids.append(aid)
            approved, _ = owner.request("POST", f"/api/allocations/{aid}/approve", 200)
            check(approved["status"] == "APPROVED", "APPROVAL")
        delay = max(0, (start - datetime.datetime.now(ZoneInfo("Asia/Manila"))).total_seconds() + 1)
        check(delay <= 12, "BOUNDED_ACTIVATION_WAIT")
        time.sleep(delay)
        for aid in ids:
            issued, _ = owner.request("POST", f"/api/allocations/{aid}/issue", 200)
            check(issued["status"] == "ISSUED" and issued["smsConsent"] and issued["contactPresent"] and issued["reminderOutcome"] == "NONE", "ISSUED_CONTACT")
        with os.fdopen(os.open(state, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), "w") as out:
            json.dump(ids,out)
    else:
        ids = json.loads(state.read_text())
        rows, _ = owner.request("GET", "/api/allocations?status=ISSUED", 200)
        for aid, expected in zip(ids, ("UNKNOWN","UNKNOWN","CLAIMED"), strict=True):
            matches = [r for r in rows if r["id"] == aid]
            check(len(matches) == 1, "READ_TARGET")
            row = matches[0]
            check(row["reminderOutcome"] == expected and row["smsConsent"] and row["contactPresent"] and row["status"] == "ISSUED", "READ_RECOVERY_OUTCOME")
    owner.request("POST","/api/v2/auth/logout",204)
    print("R07_REMINDER_RESTART_" + sys.argv[1].upper() + "_OK", flush=True)

if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print("R07_REMINDER_RESTART_FAILURE:" + (str(exc) if isinstance(exc, RuntimeError) else type(exc).__name__), file=sys.stderr)
        sys.exit(1)
