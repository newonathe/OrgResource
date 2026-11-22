#!/usr/bin/env python3
"""Offline, fail-closed inspection of an approved synthetic C8 plan. No database I/O."""
import argparse
import hashlib
import json
from collections import Counter
from pathlib import Path

SOURCE_SHA = "57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2"
FULL_SHA = "3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c"
KINDS = ("departments", "personas", "projects", "resources", "requirements", "allocations", "events")
PROFILES = {
    "full": ("LOCAL_ONLY_DRY_RUN", 96, 21060),
    "small": ("LOCAL_ONLY_SMALL_TEST", 3, 36),
    "intermediate": ("LOCAL_ONLY_INTERMEDIATE_TEST", 8, 960),
}
FULL_COUNTS = {"departments": 248, "personas": 462, "projects": 713, "resources": 1312,
               "requirements": 2139, "allocations": 21060, "events": 67370}
PRIVATE_KEYS = {"phone", "phone_number", "email", "password", "secret", "token",
                "auth_token", "contact", "real_name", "mobile"}


def unique_pairs(pairs):
    obj = {}
    for name, value in pairs:
        if name in obj:
            raise ValueError("Duplicate JSON key: " + name)
        obj[name] = value
    return obj


def no_private_fields(value):
    if isinstance(value, dict):
        for k, v in value.items():
            if k.lower() in PRIVATE_KEYS:
                raise ValueError("Unapproved private field found: " + k)
            no_private_fields(v)
    elif isinstance(value, list):
        for row in value:
            no_private_fields(row)


def preflight(plan, profile):
    scope, org_count, alloc_count = PROFILES[profile]
    if plan.get("generator") != "orgresource-c8-plan-v1" or plan.get("synthetic") is not True:
        raise ValueError("Wrong generator or plan is not explicitly synthetic")
    if plan.get("scope") != scope:
        raise ValueError("The chosen local-only profile does not match the plan")
    if plan.get("window") != {"start": "2025-12-01", "end": "2026-11-18",
                              "as_of": "2026-11-19", "zone": "Asia/Manila"}:
        raise ValueError("Simulated time window mismatch")
    if profile == "full" and plan.get("source_sha256") != SOURCE_SHA:
        raise ValueError("Approved directory-source digest mismatch")
    orgs = plan.get("organizations")
    if not isinstance(orgs, list) or len(orgs) != org_count:
        raise ValueError("Unexpected organization count")
    refs, ids, counts = set(), set(), Counter()
    for org in orgs:
        key, label = org.get("reference_key"), org.get("directory_label")
        if not isinstance(key, str) or not key or key in refs:
            raise ValueError("Duplicate/invalid organization reference")
        refs.add(key)
        if not isinstance(label, str) or not label.strip() or len(label) > 120:
            raise ValueError("Invalid organization label")
        if profile != "full" and not label.startswith("Fictional Workspace "):
            raise ValueError("Test profile has a non-fictional organization label")
        for kind in KINDS:
            records = org.get(kind)
            if not isinstance(records, list):
                raise ValueError("Missing " + kind + " records")
            counts[kind] += len(records)
            for record in records:
                identifier = record.get("id") if isinstance(record, dict) else None
                if not isinstance(identifier, str) or identifier in ids:
                    raise ValueError("Missing/repeated deterministic record ID")
                ids.add(identifier)
        for person in org["personas"]:
            if not person.get("display_name", "").startswith("Simulated persona "):
                raise ValueError("Unapproved personal display name")
        for kind in ("departments", "projects", "resources"):
            if any(not row.get("name", "").startswith("Demo ") for row in org[kind]):
                raise ValueError("Non-fictional " + kind + " name")
    if counts["allocations"] != alloc_count or dict(counts) != plan.get("counts"):
        raise ValueError("Allocation or row summary mismatch")
    if profile == "full" and (dict(counts) != FULL_COUNTS or len(plan.get("final_states", {})) != 6):
        raise ValueError("Approved full workload/state counts mismatch")
    no_private_fields(plan)
    return counts


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plan", type=Path, required=True)
    parser.add_argument("--sha256", required=True)
    parser.add_argument("--profile", choices=tuple(PROFILES), required=True)
    args = parser.parse_args()
    if len(args.sha256) != 64 or any(c not in "0123456789abcdef" for c in args.sha256):
        raise ValueError("Expected lowercase SHA-256 is invalid")
    if args.plan.stat().st_size > 32 * 1024 * 1024:
        raise ValueError("Plan larger than 32 MiB")
    raw = args.plan.read_bytes()
    digest = hashlib.sha256(raw).hexdigest()
    if digest != args.sha256:
        raise ValueError("Hash mismatch: plan refused before JSON parsing")
    if args.profile == "full" and digest != FULL_SHA:
        raise ValueError("Full plan differs from approved historical dry-run")
    plan = json.loads(raw, object_pairs_hook=unique_pairs)
    counts = preflight(plan, args.profile)
    print(json.dumps({"result": "PREFLIGHT_PASSED", "profile": args.profile,
                      "orgs": PROFILES[args.profile][1], "allocations": counts["allocations"],
                      "sha256": digest, "database_writes": 0, "live_import_authorized": False}, sort_keys=True))
    print("LIMIT: No DB authorization, host capacity, import, stock replay or recovery tested.")


if __name__ == "__main__":
    main()
