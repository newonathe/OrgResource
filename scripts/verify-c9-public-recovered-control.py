#!/usr/bin/env python3
"""Public fictional restored original control; no private cohort or logged bodies."""
import json
import os
import pathlib
import sys
import importlib.util
spec = importlib.util.spec_from_file_location("c9_api", pathlib.Path(__file__).with_name("verify-c9-recovered-api.py"))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
Client, check = module.Client, module.check

def run():
    check(os.environ.get("C9_RUNTIME_ISOLATED") == "true", "ISOLATION")
    check(os.environ.get("C9_CONTROL_PUBLIC_ONLY") == "true", "PUBLIC_FIXTURE_ONLY")
    state = pathlib.Path(os.environ["C9_RUNTIME_STATE"])
    original = os.environ["C9_CONTROL_ORIGINAL"]
    foreign = os.environ["C9_CONTROL_FOREIGN"]
    resource = os.environ["C9_CONTROL_RESOURCE"]
    org_path = "/api/v2/orgs/" + original
    if sys.argv[1:] == ["login"]:
        anonymous = Client()
        metadata, _ = anonymous.request("GET", "/api/v2/demo/metadata", 200)
        check(metadata == dict(synthetic=False, datasetId=None, demonstrationAsOf=None, notice=None), "ORIGINAL_METADATA")
        anonymous.request("GET", "/api/v2/session", 401)
        client = Client().login("additive-original-owner", os.environ["C9_RUNTIME_OWNER_PASSWORD"])
        orgs, _ = client.request("GET", "/api/v2/orgs", 200)
        check(len(orgs) == 1 and orgs[0]["id"] == original and orgs[0]["role"] == "OWNER", "EXACT_OWNER_SCOPE")
        client.request("GET", "/api/resources", 409, expected_code="ORG_CONTEXT_REQUIRED")
        client.select(original)
        rows, _ = client.request("GET", "/api/resources", 200)
        check(len(rows) == 1 and rows[0]["name"] == "Existing fictional projector" and rows[0]["totalQuantity"] == 0, "ORIGINAL_RESOURCE")
        page, _ = client.request("GET", "/api/v2/workspace/resources", 200)
        check(len(page["items"]) == 1 and page["items"][0]["name"] == "Existing fictional projector", "WORKSPACE_RESOURCE")
        detail, etag = client.request("GET", org_path + "/resources/" + resource, 200)
        check(detail["id"] == resource and detail["totalQuantity"] == detail["availableQuantity"] == 0 and etag == '"v0"', "RESOURCE_DETAIL")
        client.request("GET", "/api/v2/orgs/" + foreign + "/resources", 404)
        client.request("POST", "/api/v2/session/active-org", 404, {"organizationId": foreign})
        client.request("GET", "/api/v2/session", 200)
        client.refresh()
        with os.fdopen(os.open(state, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), "w") as f:
            json.dump({"cookie": client.cookie(), "csrf": client.csrf}, f)
        print("R06B3B_CONTROL_API_LOGIN_OK", flush=True)
    elif sys.argv[1:] == ["logout"]:
        saved = json.loads(state.read_text())
        client = Client()
        client.csrf = saved["csrf"]
        headers = {"Cookie": "ORGSESSION=" + saved["cookie"]}
        client.request("POST", "/api/v2/auth/logout", 204, headers=headers)
        client.request("GET", "/api/v2/session", 401, headers=headers)
        print("R06B3B_CONTROL_API_LOGOUT_OK", flush=True)
    else:
        raise ValueError("mode")

if __name__ == "__main__":
    try:
        run()
    except Exception as exc:
        # No response body, credential, UUID or arbitrary exception text.
        print("R06B3B_CONTROL_API_FAILED:" + type(exc).__name__, file=sys.stderr)
        if isinstance(exc, RuntimeError):
            # Existing Client includes path IDs in status failure; strip them.
            import re
            print(re.sub(r"[0-9a-fA-F-]{36}", "[id]", str(exc)), file=sys.stderr)
        sys.exit(1)
