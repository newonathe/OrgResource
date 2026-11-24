#!/usr/bin/env python3
"""R08 actual packaged HTTP, public eight-org restored fixture only.
Called inside the existing Stage1D guarded network namespace. No secrets or bodies
in stdout; metrics are serial end-to-end local HTTP including JSON decoding.
"""
import importlib.util
import json
import math
import os
import pathlib
import sys
import time
import urllib.request
import uuid

def require(condition, code):
    if not condition:
        raise RuntimeError(code)

def main():
    require(os.environ.get("C9_RUNTIME_ISOLATED") == "true", "ISOLATION")
    require(os.environ.get("EXT_SMS_ENABLED") == "false" and os.environ.get("EXT_REMINDER_MODE") == "DISABLED", "TRANSPORT")
    require(len(sys.argv) == 3, "ARGS")
    org = str(uuid.UUID(sys.argv[1]))
    expected_path = pathlib.Path(sys.argv[2])
    state_path = pathlib.Path(os.environ["C9_RUNTIME_STATE"])
    require(expected_path.parent == state_path.parent and expected_path.name == "r08-counts.tsv", "EXPECTED_PATH")
    require(expected_path.is_file() and not expected_path.is_symlink() and expected_path.stat().st_mode & 0o077 == 0, "EXPECTED_MODE")
    counts = dict((key, int(value)) for key, value in (line.split("\t") for line in expected_path.read_text().splitlines()))
    require(set(counts) == {"department", "project", "resource", "resource_requirement", "allocation", "PENDING", "ISSUED", "CONSUMED"}, "COUNTS")
    require(all(counts[k] > 0 for k in ("department", "project", "resource", "resource_requirement", "allocation")), "NONEMPTY")
    require(counts["allocation"] <= 960, "FIXTURE_BOUND")
    module_path = pathlib.Path(__file__).with_name("verify-c9-recovered-api.py")
    spec = importlib.util.spec_from_file_location("c9_runtime_api", module_path)
    api = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(api)
    owner = api.Client().login(api.OWNER, os.environ["C9_RUNTIME_OWNER_PASSWORD"])
    orgs, _ = owner.request("GET", "/api/v2/orgs", 200)
    require(any(row["id"] == org and row["role"] == "OWNER" for row in orgs), "OWNER_SCOPE")
    owner.select(org)
    session, _ = owner.request("GET", "/api/v2/session", 200)
    require(session["activeOrgId"] == org, "SELECTED_ORG")
    cases = [
        ("resource_page", "resources?limit=100", "resource", 100),
        ("allocation_page", "allocations?limit=100", "allocation", 100),
        ("requirement_page", "requirements?limit=50", "resource_requirement", 50),
        ("project_page", "projects?limit=50", "project", 50),
        ("overview", "overview", None, None),
    ]
    print("R08_PACKAGED_FIXTURE:" + " ".join(f"{k}={v}" for k,v in sorted(counts.items())), flush=True)
    for repeat in range(1, 4):
        for label, suffix, table, limit in cases:
            timings, sizes = [], []
            first = None
            for index in range(-5, 30):
                request = urllib.request.Request(api.BASE + "/api/v2/workspace/" + suffix, headers={"Accept":"application/json"})
                start = time.perf_counter_ns()
                with owner.opener.open(request, timeout=15) as response:
                    require(response.status == 200 and response.geturl() == request.full_url, "HTTP_STATUS_OR_REDIRECT")
                    raw = response.read()
                data = json.loads(raw)["data"]
                elapsed = (time.perf_counter_ns() - start) / 1e6
                if table is not None:
                    require(len(data["items"]) == min(limit, counts[table]), "PAGE_COUNT")
                    require(len({row["id"] for row in data["items"]}) == len(data["items"]), "PAGE_DUPLICATE")
                    require(bool(data["nextCursor"]) == (counts[table] > limit), "PAGE_CURSOR")
                else:
                    require(data["departmentCount"] == counts["department"], "OVERVIEW_DEPARTMENTS")
                    for status, key in (("PENDING","pending"),("ISSUED","issued"),("CONSUMED","consumed")):
                        require(data["allocations"][key] == counts[status], "OVERVIEW_STATUS")
                if index == -5:
                    first = elapsed
                if index >= 0:
                    timings.append(elapsed)
                    sizes.append(len(raw))
            timings.sort()
            print(f"R08_PACKAGED_HTTP:repeat={repeat} workload={label} warmups=5 samples=30 min_ms={timings[0]:.3f} p50_ms={timings[math.ceil(.5*len(timings))-1]:.3f} p95_ms={timings[math.ceil(.95*len(timings))-1]:.3f} max_ms={timings[-1]:.3f} first_warmup_ms={first:.3f} body_min={min(sizes)} body_max={max(sizes)}", flush=True)
    owner.request("POST", "/api/v2/auth/logout", 204)
    print("R08_PACKAGED_HTTP_OK:repeats=3 measured=450 warmups=75 same_jvm=true public_fixture=true", flush=True)

if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print("R08_PACKAGED_FAILURE:" + (str(exc) if isinstance(exc, RuntimeError) else type(exc).__name__), file=sys.stderr)
        sys.exit(1)
