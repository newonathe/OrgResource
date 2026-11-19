"""Build a deterministic C8 fictional event plan without a database connection."""

import argparse
import hashlib
import json
import random
import uuid
from collections import Counter
from datetime import date, datetime, time, timedelta
from pathlib import Path

VERSION = "orgresource-c8-plan-v1"
NAMESPACE = uuid.UUID("b0a41678-9fe3-4a0a-bd14-bdc6f0505989")
OPEN = date(2025, 12, 1)
CLOSE = date(2026, 11, 18)
AS_OF = date(2026, 11, 19)
EXPECTED_TIERS = {
    "FLAGSHIP_SANGGUNIAN": 1,
    "FLAGSHIP_MISA": 1,
    "FEATURED_SHOWCASE": 14,
    "STANDARD_COA_DIRECTORY": 56,
    "STANDARD_LIONS_DIRECTORY": 24,
}
TEAM_NAMES = ("Equipment", "Training", "Community", "Media", "Operations", "Logistics")
REUSABLE_NAMES = ("Portable projector", "Audio kit", "Folding stand", "Tool case", "Display frame")
CONSUMABLE_NAMES = ("Card sleeves", "Paper sheets", "Label strips", "Packing tape", "Marker set")
WORK_NAMES = ("Workshop rehearsal", "Volunteer practice", "Setup exercise", "Training session", "Equipment review")


def canonical(value):
    return (json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":"), allow_nan=False) + "\n").encode()


def stable_id(seed, organization, kind, index):
    return str(uuid.uuid5(NAMESPACE, f"{VERSION}/{seed}/{organization}/{kind}/{index}"))


def local(day, hour):
    return datetime.combine(day, time(hour)).isoformat(timespec="seconds")


def parse_json(raw):
    def unique_fields(fields):
        result = {}
        for key, value in fields:
            if key in result:
                raise ValueError(f"Repeated JSON property: {key}")
            result[key] = value
        return result

    return json.loads(raw, object_pairs_hook=unique_fields)


def read_source(path, expected_sha256):
    if path.stat().st_size > 512 * 1024:
        raise ValueError("The local directory manifest exceeds 512 KiB.")
    raw = path.read_bytes()
    if hashlib.sha256(raw).hexdigest() != expected_sha256:
        raise ValueError("The local directory manifest does not match its approved SHA-256.")
    source = parse_json(raw)
    if (source.get("version") != 1 or source.get("scope") != "LOCAL_ONLY_SIMULATED_ACTIVITY"
            or source.get("demo_window") != {"start": OPEN.isoformat(), "end": CLOSE.isoformat(),
                                             "as_of": AS_OF.isoformat(), "zone": "Asia/Manila"}):
        raise ValueError("Unexpected source version, scope or simulation window.")
    organizations = source["organization_references"]
    keys = [row["reference_key"] for row in organizations]
    if (len(keys) != 96 or len(set(keys)) != 96
            or len(source["historical_exclusions"]) != 8
            or set(keys) & set(source["historical_exclusions"])
            or Counter(row["tier"] for row in organizations) != EXPECTED_TIERS):
        raise ValueError("The roster does not match the approved 96-organization cohort.")
    for row in organizations:
        if set(row) != {"reference_key", "directory_label", "planning_aliases", "directory_section",
                        "institution_kind", "tier", "source_url", "profile_url"}:
            raise ValueError("Unexpected fields in the local directory reference.")
        if row["source_url"] not in {"https://ateneorecweek.org/coa-m-orgs", "https://ateneorecweek.org/lions-orgs"}:
            raise ValueError("Unapproved directory source.")
    if set(source["tiers"]) != set(EXPECTED_TIERS):
        raise ValueError("Unexpected volume tier.")
    return source


def organization_plan(ref, limits, seed):
    key = ref["reference_key"]
    randomizer = random.Random(int.from_bytes(hashlib.sha256(f"{VERSION}/{seed}/{key}".encode()).digest()))
    identifier = lambda kind, index: stable_id(seed, key, kind, index)
    tier = ref["tier"]
    # Full C8 fixture uses the lower endpoint of each approved range.
    counts = {name: pair[0] for name, pair in limits.items()}
    departments = [{"id": identifier("department", index), "name": f"Demo {TEAM_NAMES[index % len(TEAM_NAMES)]} {index + 1:02d}"}
                   for index in range(counts["departments"])]
    people = [{"id": identifier("persona", index), "display_name": f"Simulated persona {index + 1:02d}",
               "role": ("OWNER", "CUSTODIAN", "LEAD", "MEMBER")[min(index, 3)]}
              for index in range(counts["personas"])]
    resources = []
    for index in range(counts["resource_types"]):
        reusable = index % 5 < 3
        labels = REUSABLE_NAMES if reusable else CONSUMABLE_NAMES
        resources.append({"id": identifier("resource", index), "kind": "REUSABLE" if reusable else "CONSUMABLE",
                          "name": f"Demo {labels[index % len(labels)]} {index + 1:03d}", "initial_quantity": 0})
    project_count = counts["projects"]
    projects = []
    requirements = []
    allocations = []
    events = []
    resource_demand = Counter()
    requirement_demand = Counter()
    allocation_no = 0
    offsets = list(range(12))
    randomizer.shuffle(offsets)
    allocations_each, extra = divmod(counts["allocation_requests"], project_count)
    for index in range(project_count):
        month_offset = offsets[index % 12]
        year, month = (2025, 12) if month_offset == 0 else (2026, month_offset)
        start = date(year, month, 3 + randomizer.randrange(12))
        end = start + timedelta(days=2)
        if index == project_count - 1:
            start, end = date(2026, 11, 17), date(2026, 11, 22)
        project = {"id": identifier("project", index), "department_id": departments[index % len(departments)]["id"],
                   "name": f"Demo {WORK_NAMES[index % len(WORK_NAMES)]} {index + 1:03d}",
                   "start_at": local(start, 9), "end_at": local(end, 17),
                   "record_at": local(start - timedelta(days=1), 10)}
        projects.append(project)
        chosen = randomizer.sample(resources, 3)
        project_requirements = []
        for slot, item in enumerate(chosen):
            requirement = {"id": identifier("requirement", index * 3 + slot), "project_id": project["id"],
                           "resource_id": item["id"], "quantity": 0}
            requirements.append(requirement)
            project_requirements.append(requirement)
        for request_index in range(allocations_each + (index < extra)):
            slot = request_index % len(chosen)
            resource = chosen[slot]
            requirement = project_requirements[slot]
            quantity = 1 + randomizer.randrange(3)
            allocation = {"id": identifier("allocation", allocation_no), "requirement_id": requirement["id"],
                          "resource_id": resource["id"], "borrower_id": people[3 + allocation_no % (len(people) - 3)]["id"],
                          "quantity": quantity}
            allocations.append(allocation)
            requirement_demand[requirement["id"]] += quantity
            resource_demand[resource["id"]] += quantity
            planned = [("REQUEST", project["record_at"], people[0]["id"])]
            variant = request_index % 12
            if variant == 0:
                if end <= CLOSE:
                    planned.append(("EXPIRE", project["end_at"], people[0]["id"]))
            elif variant == 1:
                planned.append(("CANCEL", local(start, 8), people[1]["id"]))
            else:
                planned.append(("APPROVE", local(start, 8), people[1]["id"]))
                if variant == 2 and end <= CLOSE:
                    planned.append(("EXPIRE", project["end_at"], people[0]["id"]))
                elif variant != 2:
                    if resource["kind"] == "REUSABLE":
                        planned.append(("ISSUE", local(start, 10), people[1]["id"]))
                        if variant != 3:
                            returned = start + timedelta(days=1)
                            planned.append(("RETURN", local(returned, 12), people[1]["id"]))
                    else:
                        planned.append(("CONSUME", local(start, 10), people[1]["id"]))
            for step, (kind, stamp, actor) in enumerate(planned):
                events.append({"id": identifier("operation", allocation_no * 5 + step), "allocation_id": allocation["id"],
                               "occurred_at": stamp, "action": kind, "actor_id": actor})
            allocation_no += 1
    for requirement in requirements:
        demand = requirement_demand[requirement["id"]]
        requirement["quantity"] = max(2, demand + min(demand, 4))
    for resource in resources:
        resource["initial_quantity"] = resource_demand[resource["id"]] + 12 + randomizer.randrange(19)
    events.sort(key=lambda event: (event["occurred_at"], event["id"]))
    return {"reference_key": key, "id": identifier("organization", 0),
            "directory_label": ref["directory_label"], "tier": tier,
            "departments": departments, "personas": people, "projects": projects, "resources": resources,
            "requirements": requirements, "allocations": allocations, "events": events}


def verify_organization(org):
    resources = {row["id"]: row for row in org["resources"]}
    projects = {row["id"]: row for row in org["projects"]}
    requirements = {row["id"]: row for row in org["requirements"]}
    allocations = {row["id"]: row for row in org["allocations"]}
    people = {row["id"]: row["role"] for row in org["personas"]}
    all_ids = ([org["id"]] + [row["id"] for kind in ("departments", "personas", "projects", "resources", "requirements",
                                                    "allocations", "events") for row in org[kind]])
    if len(all_ids) != len(set(all_ids)):
        raise ValueError("Repeated deterministic object ID.")
    totals = {key: row["initial_quantity"] for key, row in resources.items()}
    holds, issued, covered = Counter(), Counter(), Counter()
    states = {}
    for event in org["events"]:
        record = allocations[event["allocation_id"]]
        required = requirements[record["requirement_id"]]
        project = projects[required["project_id"]]
        key, quantity = record["resource_id"], record["quantity"]
        action, at = event["action"], event["occurred_at"]
        state = states.get(record["id"])
        reusable = resources[key]["kind"] == "REUSABLE"
        if key != required["resource_id"] or record["borrower_id"] not in people or event["actor_id"] not in people:
            raise ValueError("Invalid relationship in the planned operations.")
        if not (OPEN <= date.fromisoformat(at[:10]) <= CLOSE) or at < project["record_at"]:
            raise ValueError("An operation falls outside the planned time window.")
        if action == "REQUEST" and state is None and at < project["end_at"] and people[event["actor_id"]] == "OWNER":
            state = "PENDING"
        elif action == "APPROVE" and state == "PENDING" and at < project["end_at"] and people[event["actor_id"]] == "CUSTODIAN":
            holds[key] += quantity
            covered[required["id"]] += quantity
            state = "APPROVED"
        elif action in ("ISSUE", "CONSUME") and state == "APPROVED" and project["start_at"] <= at < project["end_at"]:
            holds[key] -= quantity
            if action == "ISSUE" and reusable:
                issued[key] += quantity
                state = "ISSUED"
            elif action == "CONSUME" and not reusable:
                totals[key] -= quantity
                state = "CONSUMED"
            else:
                raise ValueError("A stock operation conflicts with the resource kind.")
        elif action == "RETURN" and state == "ISSUED":
            issued[key] -= quantity
            state = "RETURNED"
        elif action == "CANCEL" and state == "PENDING":
            state = "CANCELLED"
        elif action == "EXPIRE" and state in ("PENDING", "APPROVED") and at >= project["end_at"]:
            if state == "APPROVED":
                holds[key] -= quantity
                covered[required["id"]] -= quantity
            state = "CANCELLED"
        else:
            raise ValueError(f"Invalid planned transition {state} to {action}.")
        if (min(totals[key], holds[key], issued[key], covered[required["id"]]) < 0
                or holds[key] + issued[key] > totals[key] or covered[required["id"]] > required["quantity"]):
            raise ValueError("Planned operations exceed physical stock or Requirement coverage.")
        states[record["id"]] = state
    if set(states) != set(allocations):
        raise ValueError("An allocation is missing its operation history.")
    return Counter(states.values())


def build(source, source_hash, seed):
    orgs = [organization_plan(row, source["tiers"][row["tier"]], seed)
            for row in sorted(source["organization_references"], key=lambda item: item["reference_key"])]
    statuses = Counter()
    months = set()
    for org in orgs:
        statuses.update(verify_organization(org))
        months.update(event["occurred_at"][:7] for event in org["events"])
    counts = {kind: sum(len(org[kind]) for org in orgs) for kind in (
        "departments", "personas", "projects", "resources", "requirements", "allocations", "events")}
    if counts["allocations"] != 21060 or len(months) != 12 or len(statuses) != 6:
        raise ValueError("The dry run missed a workload, calendar or state gate.")
    return {"generator": VERSION, "source_sha256": source_hash, "seed": seed,
            "dataset_id": f"{VERSION}-{source_hash[:12]}-{hashlib.sha256(seed.encode()).hexdigest()[:12]}",
            "scope": "LOCAL_ONLY_DRY_RUN", "synthetic": True, "window": source["demo_window"],
            "counts": counts, "final_states": dict(statuses), "organizations": orgs}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--source-sha256", required=True)
    parser.add_argument("--seed", required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if not (1 <= len(args.seed) <= 80 and args.seed.isascii() and args.seed.isprintable()):
        raise ValueError("The seed must be 1–80 printable ASCII characters.")
    plan = build(read_source(args.source, args.source_sha256), args.source_sha256, args.seed)
    payload = canonical(plan)
    if args.output.exists():
        if args.output.read_bytes() != payload:
            raise ValueError("The destination already holds a different plan. Use a new path.")
    else:
        with args.output.open("xb") as output:
            output.write(payload)
    print(json.dumps({"organizations": len(plan["organizations"]), **plan["counts"], "states": plan["final_states"]}, sort_keys=True))
    print(f"{len(payload)} bytes | SHA-256 {hashlib.sha256(payload).hexdigest()} | dry run only")


if __name__ == "__main__":
    main()
