#!/usr/bin/env python3
"""Private mixed recovered API oracle; invoked only within the guarded local runner."""
import importlib.util
import json
import os
import pathlib
import re
import stat
import sys
import uuid

NOTICE = "DEMO ENVIRONMENT — Workspaces identified as demo organizations contain simulated accounts, projects, inventory and activity. Existing untagged workspaces retain their own data. Public directory labels do not imply participation, adoption or endorsement."
PHASE = "INTAKE"

def require(ok):
    if not ok:
        raise ValueError("oracle mismatch")

def phase(name):
    global PHASE
    PHASE = name
    print("R06B3B_MIXED_API_PHASE:" + name, flush=True)

def private_file(name):
    path = pathlib.Path(os.environ[name])
    require(path.is_absolute() and path.resolve(strict=True) == path)
    info = path.stat()
    require(stat.S_ISREG(info.st_mode) and info.st_uid == os.getuid() and stat.S_IMODE(info.st_mode) == 0o600)
    require(path.parent.stat().st_uid == os.getuid() and stat.S_IMODE(path.parent.stat().st_mode) == 0o700)
    return path

def canonical_uuid(value):
    return isinstance(value, str) and str(uuid.UUID(value)) == value

def validate_expected(doc):
    require(set(doc) == {"metadata", "ownerAccountId", "ownerOrgIds", "original", "demo", "foreignOrgId"})
    meta = doc["metadata"]
    require(set(meta) == {"synthetic", "datasetId", "demonstrationAsOf", "notice"})
    require(meta["synthetic"] is True and meta["notice"] == NOTICE)
    require(re.fullmatch(r"orgresource-c8-plan-v1-[0-9a-f]{12}-[0-9a-f]{12}", meta["datasetId"]) is not None)
    require(re.fullmatch(r"\d{4}-\d{2}-\d{2}", meta["demonstrationAsOf"]) is not None)
    ids = doc["ownerOrgIds"]
    require(isinstance(ids, list) and len(ids) == len(set(ids)) == 97 and all(canonical_uuid(x) for x in ids))
    require(canonical_uuid(doc["ownerAccountId"]) and canonical_uuid(doc["foreignOrgId"]) and doc["foreignOrgId"] not in ids)
    for label in ("original", "demo"):
        org = doc[label]
        require(set(org) == {"id", "slug", "displayName", "workspaceIds", "modernIds", "detail"})
        require(org["id"] in ids and isinstance(org["slug"], str) and org["slug"] and isinstance(org["displayName"], str) and org["displayName"])
        ws, modern, detail = org["workspaceIds"], org["modernIds"], org["detail"]
        require(isinstance(ws, list) and 0 < len(ws) <= 100 and len(ws) == len(set(ws)) and all(type(x) is int and x > 0 for x in ws))
        require(isinstance(modern, list) and len(modern) == len(ws) == len(set(modern)) and all(canonical_uuid(x) for x in modern))
        require(set(detail) == {"id", "name", "type", "totalQuantity", "availableQuantity", "version"})
        require(detail["id"] in modern and detail["type"] in ("REUSABLE", "CONSUMABLE") and isinstance(detail["name"], str) and detail["name"])
        require(all(type(detail[k]) is int for k in ("totalQuantity", "availableQuantity", "version")))
        require(0 <= detail["availableQuantity"] <= detail["totalQuantity"] <= 1000000 and detail["version"] >= 0)
    require(doc["original"]["id"] != doc["demo"]["id"])
    require(doc["original"]["displayName"] == "Isolated Original Workspace")
    require(doc["original"]["detail"]["name"] == "Existing fictional projector")
    require(doc["original"]["detail"]["totalQuantity"] == doc["original"]["detail"]["availableQuantity"] == 0)
    require(len(doc["original"]["workspaceIds"]) == 1)
    return doc

def run():
    require(os.getuid() != 0 and os.environ.get("GITHUB_ACTIONS") != "true")
    require(os.environ.get("C9_RUNTIME_ISOLATED") == "true" and os.environ.get("C9_CONTROL_PRIVATE_MIXED") == "true")
    expected = validate_expected(json.loads(private_file("C9_MIXED_EXPECTED").read_text()))
    state = pathlib.Path(os.environ["C9_RUNTIME_STATE"])
    require(state.is_absolute() and state.parent.resolve(strict=True) == state.parent)
    require(state.parent.stat().st_uid == os.getuid() and stat.S_IMODE(state.parent.stat().st_mode) == 0o700)
    spec = importlib.util.spec_from_file_location("c9_api", pathlib.Path(__file__).with_name("verify-c9-recovered-api.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    Client = module.Client
    if sys.argv[1:] == ["login"]:
        phase("METADATA")
        anon = Client()
        metadata, _ = anon.request("GET", "/api/v2/demo/metadata", 200)
        require(metadata == expected["metadata"])
        anon.request("GET", "/api/v2/session", 401)
        phase("OWNER_LOGIN")
        client = Client().login("additive-original-owner", os.environ["C9_RUNTIME_OWNER_PASSWORD"])
        orgs, _ = client.request("GET", "/api/v2/orgs", 200)
        require(len(orgs) == 97 and {x["id"] for x in orgs} == set(expected["ownerOrgIds"]) and all(x["role"] == "OWNER" for x in orgs))
        def check_session(selected):
            session, _ = client.request("GET", "/api/v2/session", 200)
            require(session["accountId"] == expected["ownerAccountId"] and session["activeOrgId"] == selected)
            memberships = session["memberships"]
            require(len(memberships) == 97 and {x["organizationId"] for x in memberships} == set(expected["ownerOrgIds"]))
            require(all(x["role"] == "OWNER" and x["status"] == "ACTIVE" for x in memberships))
        check_session(None)
        client.request("GET", "/api/resources", 409, expected_code="ORG_CONTEXT_REQUIRED")
        for label, other_label in (("original", "demo"), ("demo", "original")):
            phase(label.upper() + "_READ")
            org = expected[label]
            client.select(org["id"])
            check_session(org["id"])
            legacy, _ = client.request("GET", "/api/resources", 200)
            require(len(legacy) == len(org["workspaceIds"]) and {x["id"] for x in legacy} == set(org["workspaceIds"]))
            require({x["publicId"] for x in legacy} == set(org["modernIds"]))
            page, _ = client.request("GET", "/api/v2/workspace/resources?limit=100", 200)
            require(page["nextCursor"] is None and [x["id"] for x in page["items"]] == org["workspaceIds"])
            require({x["publicId"] for x in page["items"]} == set(org["modernIds"]))
            root = "/api/v2/orgs/" + org["id"] + "/resources"
            modern, _ = client.request("GET", root + "?limit=100", 200)
            require(modern["nextCursor"] is None and len(modern["items"]) == len(org["modernIds"]))
            require({x["id"] for x in modern["items"]} == set(org["modernIds"]))
            detail, etag = client.request("GET", root + "/" + org["detail"]["id"], 200)
            require(detail == org["detail"] and etag == '"v' + str(detail["version"]) + '"')
            client.request("GET", root + "/" + expected[other_label]["detail"]["id"], 404)
            client.request("GET", "/api/v2/orgs/" + expected["foreignOrgId"] + "/resources", 404)
            client.request("POST", "/api/v2/session/active-org", 404, {"organizationId": expected["foreignOrgId"]})
            check_session(org["id"])
        phase("SAVE_SESSION")
        client.refresh()
        with os.fdopen(os.open(state, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600), "w") as f:
            json.dump({"cookie": client.cookie(), "csrf": client.csrf}, f)
        print("R06B3B_MIXED_API_LOGIN_OK:exact_owner_scope=true original_demo_reads=true foreign_refused=true", flush=True)
    elif sys.argv[1:] == ["logout"]:
        phase("LOGOUT")
        saved = json.loads(private_file("C9_RUNTIME_STATE").read_text())
        require(canonical_uuid(saved["cookie"]) and isinstance(saved["csrf"], str) and saved["csrf"])
        client = Client()
        client.csrf = saved["csrf"]
        headers = {"Cookie": "ORGSESSION=" + saved["cookie"]}
        client.request("POST", "/api/v2/auth/logout", 204, headers=headers)
        client.request("GET", "/api/v2/session", 401, headers=headers)
        print("R06B3B_MIXED_API_LOGOUT_OK", flush=True)
    else:
        raise ValueError("mode")

if __name__ == "__main__":
    try:
        run()
    except Exception as exc:
        print("R06B3B_MIXED_API_FAILED:" + PHASE + " " + type(exc).__name__, file=sys.stderr)
        if isinstance(exc, RuntimeError):
            match = re.fullmatch(r"(?:GET|POST) /api/[^ ]+ expected=([0-9]{3}) actual=([0-9]{3})", str(exc))
            if match:
                print("R06B3B_MIXED_HTTP_STATUS:expected=" + match[1] + " actual=" + match[2], file=sys.stderr)
        sys.exit(1)
