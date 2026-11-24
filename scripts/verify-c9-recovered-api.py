#!/usr/bin/env python3
"""Stage 1D only: real packaged API, fixed loopback inside a disposable netns.
Private JSON state bridges source and restored processes. Never log bodies/cookies.
"""
import http.cookiejar
import json
import os
import pathlib
import secrets
import sys
import urllib.error
import urllib.request
import uuid

BASE = "http://127.0.0.1:8085"
STATE = pathlib.Path(os.environ["C9_RUNTIME_STATE"])
OWNER = "additive-original-owner"

def check(condition, case):
    if not condition:
        raise RuntimeError(case)

def ok(case):
    print("C9_RUNTIME_ASSERTION_OK:" + case, flush=True)

class Client:
    def __init__(self):
        self.jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(
            urllib.request.ProxyHandler({}), urllib.request.HTTPCookieProcessor(self.jar))
        self.csrf = None

    def request(self, method, path, expected, body=None, headers=None, expected_code=None):
        check(path.startswith("/api/"), "API_PATH")
        h = {"Accept": "application/json"}
        if method == "POST":
            h.update({"Content-Type": "application/json", "Origin": BASE})
            if self.csrf:
                h["X-CSRF-TOKEN"] = self.csrf
        h.update(headers or {})
        req = urllib.request.Request(BASE + path,
            data=json.dumps(body).encode() if body is not None else None,
            headers=h, method=method)
        try:
            response = self.opener.open(req, timeout=15)
        except urllib.error.HTTPError as failure:
            response = failure
        with response:
            raw = response.read()
            status = response.status
            etag = response.headers.get("ETag")
        check(status == expected, f"{method} {path.split('?')[0]} expected={expected} actual={status}")
        parsed = json.loads(raw) if raw else None
        if expected_code is not None:
            check(isinstance(parsed, dict) and parsed.get("code") == expected_code, "ERROR_CODE_" + expected_code)
        return (parsed.get("data", parsed) if isinstance(parsed, dict) else parsed), etag

    def refresh(self):
        data, _ = self.request("GET", "/api/v2/auth/csrf", 200)
        self.csrf = data["csrfToken"]

    def login(self, username, password):
        self.refresh()
        data, _ = self.request("POST", "/api/v2/auth/login", 200,
                               {"username": username, "password": password})
        check(data["session"]["activeOrgId"] is None, "LOGIN_NO_IMPLICIT_ORG")
        self.csrf = data["csrfToken"]
        return self

    def select(self, org):
        data, _ = self.request("POST", "/api/v2/session/active-org", 200, {"organizationId": org})
        check(data["activeOrgId"] == org, "ACTIVE_ORG_MISMATCH")
        self.refresh()

    def cookie(self):
        cookies = [c.value for c in self.jar if c.name == "ORGSESSION"]
        check(len(cookies) == 1 and str(uuid.UUID(cookies[0])) == cookies[0], "SESSION_COOKIE_MISSING")
        return cookies[0]

def invite(owner, org, role, username):
    password = "C9-runtime-" + secrets.token_hex(24)
    data, _ = owner.request("POST", org + "/invitations", 201, {"role": role})
    guest = Client()
    guest.refresh()
    redeemed, _ = guest.request("POST", "/api/v2/invitations/redeem", 201, {
        "bearerToken": data["bearerToken"], "username": username,
        "displayName": username, "password": password})
    return password, redeemed["membershipId"]

def seed():
    owner = Client().login(OWNER, os.environ["C9_RUNTIME_OWNER_PASSWORD"])
    org_id = os.environ["C9_RUNTIME_ORG"]
    owner.select(org_id)
    orgs, _ = owner.request("GET", "/api/v2/orgs", 200)
    check(any(o["id"] == org_id and o["role"] == "OWNER" for o in orgs), "OWNER_SCOPE")
    foreign = next(o["id"] for o in orgs if o["id"] != org_id)
    org = "/api/v2/orgs/" + org_id
    cust_name = "c9-runtime-custodian"
    suspended_name = "c9-runtime-suspended"
    cust_password, cust_id = invite(owner, org, "CUSTODIAN", cust_name)
    suspended_password, suspended_id = invite(owner, org, "MEMBER", suspended_name)
    cust = Client().login(cust_name, cust_password)
    cust.select(org_id)
    name = "C9 recovery receipt fixture"
    owner.request("POST", "/api/resources", 201,
                  {"name": name, "type": "CONSUMABLE", "totalQuantity": 0})
    resources, _ = cust.request("GET", org + "/resources?limit=100", 200)
    rows = [r for r in resources["items"] if r["name"] == name]
    check(len(rows) == 1, "RECEIPT_RESOURCE_UNIQUE")
    resource = rows[0]["id"]
    path = org + "/resources/" + resource
    before, etag = cust.request("GET", path, 200)
    check(before["totalQuantity"] == 0 and etag, "ZERO_STOCK_ETAG")
    op = str(uuid.uuid4())
    body = {"operationId": op, "quantity": 1}
    key = secrets.token_hex(32)
    cust.request("POST", path + "/receive", 201, body, {"If-Match": etag, "Idempotency-Key": key})
    members, _ = owner.request("GET", org + "/members", 200)
    for member_id, suffix, payload in [
        (cust_id, "/role", {"role": "MEMBER"}), (suspended_id, "/suspend", {})]:
        member = next(m for m in members["items"] if m["id"] == member_id)
        owner.request("POST", org + "/members/" + member_id + suffix, 200, payload,
                      {"If-Match": '"v' + str(member["version"]) + '"'})
    cust.request("GET", org + "/operations/" + op, 403)
    receipt, _ = owner.request("GET", org + "/operations/" + op, 200)
    check(receipt["kind"] == "RECEIPT" and receipt["operationId"] == op, "SOURCE_RECEIPT")
    owner.request("GET", "/api/v2/session", 200)
    owner.request("GET", "/api/v2/demo/metadata", 503)
    state = {"org": org_id, "foreign": foreign, "resource": resource, "operation": op,
             "etag": etag, "key": key, "oldSession": owner.cookie(),
             "custPassword": cust_password, "suspendedPassword": suspended_password}
    with os.fdopen(os.open(STATE, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), "w") as f:
        json.dump(state, f)
    ok("source-real-authenticated-session-and-committed-receipt")

def recovered():
    s = json.loads(STATE.read_text())
    org = "/api/v2/orgs/" + s["org"]
    resource = org + "/resources/" + s["resource"]
    operation = org + "/operations/" + s["operation"]
    Client().request("GET", "/api/v2/session", 401,
                     headers={"Cookie": "ORGSESSION=" + s["oldSession"]})
    ok("actual-pre-backup-cookie-retired")
    owner = Client().login(OWNER, os.environ["C9_RUNTIME_OWNER_PASSWORD"])
    owner.select(s["org"])
    check(owner.cookie() != s["oldSession"], "NEW_SESSION_NOT_DISTINCT")
    ok("restored-owner-login-and-selection")
    first, current_etag = owner.request("GET", resource, 200)
    check(bool(current_etag), "RESTORED_CURRENT_ETAG")
    receipt, _ = owner.request("GET", operation, 200)
    check(first["totalQuantity"] == 1 and first["availableQuantity"] == 1 and
          receipt["kind"] == "RECEIPT" and receipt["operationId"] == s["operation"], "RESTORED_STOCK_OR_RECEIPT")
    replay, _ = owner.request("POST", resource + "/receive", 200,
                             {"operationId": s["operation"], "quantity": 1},
                             {"If-Match": s["etag"], "Idempotency-Key": s["key"]})
    check(replay["kind"] == "RECEIPT" and replay["operationId"] == s["operation"], "REPLAY_IDENTITY")
    # New key isolates durable operation-ID fingerprint from temporary HTTP key reuse.
    owner.request("POST", resource + "/receive", 409,
                  {"operationId": s["operation"], "quantity": 2},
                  {"If-Match": current_etag, "Idempotency-Key": secrets.token_hex(32)},
                  expected_code="OPERATION_ID_REUSED")
    ok("durable-changed-payload-conflict")
    after, after_etag = owner.request("GET", resource, 200)
    moves, _ = owner.request("GET", resource + "/movements?limit=10", 200)
    check(after == first and after_etag == current_etag and
          after["totalQuantity"] == 1 and after["availableQuantity"] == 1 and
          len(moves["items"]) == 1 and moves["items"][0]["operationId"] == s["operation"],
          "REPLAY_CHANGED_STOCK")
    ok("durable-replay-no-stock-duplication")
    cust = Client().login("c9-runtime-custodian", s["custPassword"])
    cust.select(s["org"])
    cust.request("GET", operation, 403)
    cust.request("POST", resource + "/receive", 403,
                 {"operationId": s["operation"], "quantity": 1},
                 {"If-Match": s["etag"], "Idempotency-Key": s["key"]})
    cust.request("POST", resource + "/receive", 403,
                 {"operationId": str(uuid.uuid4()), "quantity": 1},
                 {"If-Match": current_etag, "Idempotency-Key": secrets.token_hex(32)})
    ok("demoted-member-fresh-write-refused")
    cust.request("GET", "/api/v2/orgs/" + s["foreign"] + "/resources", 404)
    ok("demoted-member-and-foreign-tenant-refused")
    suspended = Client().login("c9-runtime-suspended", s["suspendedPassword"])
    visible, _ = suspended.request("GET", "/api/v2/orgs", 200)
    check(visible == [], "SUSPENDED_MEMBERSHIP_VISIBLE")
    suspended.request("POST", "/api/v2/session/active-org", 404, {"organizationId": s["org"]})
    suspended.request("GET", operation, 404)
    ok("restored-suspended-member-refused")
    owner.request("GET", "/api/v2/demo/metadata", 503)
    ok("mixed-fixture-metadata-fails-closed")

if __name__ == "__main__":
    try:
        check(os.environ.get("C9_RUNTIME_ISOLATED") == "true", "CI_RUNTIME_GUARD")
        check(sys.argv[1:] in [["seed"], ["recovered"]], "MODE")
        (seed if sys.argv[1] == "seed" else recovered)()
    except Exception as exc:
        # A missing response field or setup error is a failure, not a negative pass.
        if isinstance(exc, RuntimeError):
            print("C9_RUNTIME_FAILURE:" + str(exc), file=sys.stderr)
        else:
            print("C9_RUNTIME_FAILURE:" + type(exc).__name__, file=sys.stderr)
        sys.exit(1)
