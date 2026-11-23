#!/usr/bin/env python3
"""C9 prep: read-only MySQL measurements on a disposable C8 intermediate CI fixture.

Does not inspect the private 96-org plan, change indexes, or establish C9 acceptance.
"""
import json
import os
import re
import statistics
import subprocess
import sys

SCHEMA = "orgresource_c8_additive_ci_fixture_intermediate"
READER = "c8_add_ro_intermediate"


class Refusal(Exception):
    pass


def require(condition, code):
    if not condition:
        raise Refusal(code)


def mysql(sql):
    require(sql.startswith(("SELECT ", "SHOW GRANTS", "EXPLAIN FORMAT=JSON SELECT ",
                            "EXPLAIN ANALYZE SELECT ")), "C9_SELECT_ONLY_STATEMENT_REQUIRED")
    require(";" not in sql and "\0" not in sql, "C9_SINGLE_STATEMENT_REQUIRED")
    cmd = ["docker", "exec", "--env", "MYSQL_PWD=" + os.environ["C9_READONLY_PASSWORD"],
           os.environ["MYSQL_CONTAINER"], "mysql", "--host=127.0.0.1", "--protocol=TCP",
           "--user=" + READER, "--database=" + SCHEMA, "--batch", "--raw",
           "--skip-column-names", "--connect-timeout=5", "--execute", sql]
    try:
        result = subprocess.run(cmd, check=True, capture_output=True, text=True, timeout=30)
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, OSError):
        raise Refusal("C9_DISPOSABLE_MYSQL_QUERY_FAILED") from None
    return result.stdout.strip()


def scalar(sql):
    result = mysql(sql)
    require(result and "\n" not in result and result != "NULL", "C9_UNEXPECTED_SQL_SCALAR")
    return result


def access_plan(sql):
    try:
        data = json.loads(mysql("EXPLAIN FORMAT=JSON " + sql))
    except ValueError:
        raise Refusal("C9_JSON_EXPLAIN_INVALID") from None
    operators = []

    def visit(value):
        if isinstance(value, list):
            for part in value:
                visit(part)
        elif isinstance(value, dict):
            if "table_name" in value:
                operators.append({
                    "table": value["table_name"],
                    "access": value.get("access_type"),
                    "index": value.get("key"),
                    "estimate_rows_per_scan": value.get("rows_examined_per_scan")})
            for part in value.values():
                visit(part)

    visit(data)
    require(bool(operators), "C9_EXPLAIN_MISSING_TABLE_OPERATORS")
    return operators


def elapsed(sql):
    tree = mysql("EXPLAIN ANALYZE " + sql)
    matches = re.findall(r"actual time=([0-9]+(?:\.[0-9]+)?)\.\.([0-9]+(?:\.[0-9]+)?)",
                         tree)
    require(bool(matches), "C9_EXPLAIN_ANALYZE_TIMING_MISSING")
    ms = float(matches[0][1])
    require(0 <= ms < float("inf"), "C9_BAD_ITERATOR_DURATION")
    return ms


def main():
    require(os.environ.get("GITHUB_ACTIONS") == "true", "C9_CI_ONLY")
    require(os.environ.get("C9_DB_SCHEMA") == SCHEMA, "C9_DISPOSABLE_SCHEMA_REQUIRED")
    require(os.environ.get("C9_READONLY_USER") == READER, "C9_READ_ONLY_ROLE_REQUIRED")
    require(bool(os.environ.get("C9_READONLY_PASSWORD")), "C9_READ_ONLY_PASSWORD_REQUIRED")
    require(bool(re.fullmatch("[0-9a-f]{64}", os.environ.get("MYSQL_CONTAINER", ""))),
            "C9_DOCKER_CONTAINER_ID_REQUIRED")
    require(scalar("SELECT CURRENT_USER()") == READER + "@%",
            "C9_AUTHENTICATED_READ_ONLY_ROLE_REQUIRED")
    grants = mysql("SHOW GRANTS").splitlines()
    select_grant = "GRANT SELECT ON " + chr(96) + SCHEMA + chr(96) + ".* TO "
    require(len(grants) == 2 and
            any(re.fullmatch(r"GRANT USAGE ON \*\.\* TO .+", g) for g in grants) and
            any(g.startswith(select_grant) for g in grants) and
            all(not re.search(r"\b(INSERT|UPDATE|DELETE|ALL PRIVILEGES|CREATE|ALTER|DROP)\b", g)
                for g in grants), "C9_READ_ONLY_GRANTS_REQUIRED")
    version = scalar("SELECT VERSION()")
    require(version.startswith("8.0."), "C9_EXPECTED_MYSQL8")
    tagged = int(scalar("SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL"))
    allocations = int(scalar(
        "SELECT COUNT(*) FROM allocation a JOIN organization o "
        "ON o.id=a.organization_id WHERE o.demo_plan_sha IS NOT NULL"))
    require((tagged, allocations) == (8, 960), "C9_INTERMEDIATE_FIXTURE_REQUIRED")
    require(int(scalar(
        "SELECT COUNT(*) FROM allocation a JOIN organization o "
        "ON o.id=a.organization_id WHERE o.demo_plan_sha IS NULL")) == 0,
        "C9_ORIGINAL_ROWS_NOT_EXPECTED")
    org = scalar(
        "SELECT o.id FROM organization o JOIN allocation a ON a.organization_id=o.id "
        "WHERE o.demo_plan_sha IS NOT NULL GROUP BY o.id "
        "ORDER BY COUNT(a.id) DESC,o.id LIMIT 1")
    require(bool(re.fullmatch(r"[1-9][0-9]*", org)), "C9_INVALID_FIXTURE_ORG")
    asof = scalar("SELECT CAST(MIN(demo_as_of) AS CHAR) FROM organization "
                  "WHERE demo_plan_sha IS NOT NULL")
    require(bool(re.fullmatch(r"\d{4}-\d{2}-\d{2}", asof)), "C9_INVALID_AS_OF")
    # Trace: C1Service workspace pages -> ResourceRepository, AllocationRepository,
    # ResourceRequirementRepository; overview -> AllocationRepository;
    # ledger balance -> AllocationRepository.INVENTORY_LEDGER_AGGREGATE.
    # These are hand-authored SQL analogues, NOT Hibernate's generated SQL.
    queries = {
        "resource_page": (
            f"SELECT r.id,r.type,r.total_quantity FROM resource r "
            f"WHERE r.organization_id={org} AND r.id>0 ORDER BY r.id LIMIT 51"),
        "allocation_page": (
            f"SELECT a.id,a.requirement_id,a.status FROM allocation a "
            f"JOIN resource_requirement q ON q.id=a.requirement_id "
            f"AND q.organization_id=a.organization_id "
            f"JOIN project p ON p.id=q.project_id AND p.organization_id=a.organization_id "
            f"WHERE a.organization_id={org} AND a.id>0 ORDER BY a.id LIMIT 51"),
        "requirement_page": (
            f"SELECT q.id,p.name,r.name FROM resource_requirement q "
            f"JOIN project p ON p.id=q.project_id AND p.organization_id=q.organization_id "
            f"JOIN resource r ON r.id=q.resource_id AND r.organization_id=q.organization_id "
            f"WHERE q.organization_id={org} AND q.id>0 ORDER BY q.id LIMIT 51"),
        "stock_ledger_aggregate": (
            f"SELECT m.resource_id,SUM(m.delta_total),SUM(m.delta_issued) "
            f"FROM stock_movement m WHERE m.organization_id={org} "
            f"GROUP BY m.resource_id ORDER BY m.resource_id LIMIT 51"),
        "overview_status_counts": (
            f"SELECT SUM(a.status='PENDING'),SUM(a.status='ISSUED'),"
            f"SUM(a.status='ISSUED' AND p.end_at<='{asof} 00:00:00'),"
            f"SUM(a.status='CONSUMED') FROM allocation a "
            f"JOIN resource_requirement q ON q.id=a.requirement_id "
            f"AND q.organization_id=a.organization_id "
            f"JOIN project p ON p.id=q.project_id AND p.organization_id=a.organization_id "
            f"WHERE a.organization_id={org}"),
    }
    rows = []
    for name, query in queries.items():
        operators = access_plan(query)
        elapsed(query)  # warm-up before measuring three executions
        samples = [elapsed(query) for _ in range(3)]
        rows.append({
            "name": name, "operators": operators, "samples": len(samples),
            "median_server_iterator_ms": round(statistics.median(samples), 3),
            "max_server_iterator_ms": round(max(samples), 3)})
    print("C9_ISOLATED_READ_ONLY_MEASUREMENT_OK: " + json.dumps({
        "engine": version, "fixture": "fictional intermediate (8 orgs/960 allocations)",
        "sql": "SQL analogues, not exact Hibernate SQL",
        "time_scope": "MySQL EXPLAIN ANALYZE top iterator, not HTTP/SLA",
        "queries": rows}, sort_keys=True, separators=(",", ":")))
    print("C9_ACCEPTED_FULL_96_PERFORMANCE=NOT_RUN; C9_INDEX_CHANGES=NONE; "
          "C8_INDEPENDENT_ACCEPTANCE=BLOCKED")


if __name__ == "__main__":
    try:
        main()
    except (Refusal, KeyError) as exc:
        print(str(exc) if isinstance(exc, Refusal) else "C9_REQUIRED_ENVIRONMENT_MISSING",
              file=sys.stderr)
        sys.exit(1)
