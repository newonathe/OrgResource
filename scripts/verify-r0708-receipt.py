#!/usr/bin/env python3
"""Validate exact selected JUnit receipts; emit only bounded public-safe metrics."""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

CONCURRENCY = "C2_CONCURRENCY01_twoApprovalsAndTwoIssuesCannotDoubleCommitStock"
ROLLBACK = "C2_IDEMP02_failureBetweenQuantityAndMovementRollsBackEverything"
MEASURE = "C8WorkspacePagesReturnEveryResourceAndAllocationWithScopedContinuation"
EXPECTED = {"concurrency": ([CONCURRENCY], ["concurrency"]),
            "rollback": ([ROLLBACK], ["rollback"]),
            "reversed": ([ROLLBACK, CONCURRENCY], ["rollback", "concurrency"]),
            "measurement": ([MEASURE], [])}
def require(ok, message):
    if not ok:
        raise SystemExit(message)
def main():
    label, log_path, xml_path = sys.argv[1:]
    methods, markers = EXPECTED[label]
    root = ET.parse(xml_path).getroot()
    require(root.tag == "testsuite" and root.get("name") == "edu.orgresource.extended.C1IdentityAcceptanceTest", "Wrong test suite")
    require(root.get("tests") == str(len(methods)) and all(root.get(k) == "0" for k in ("failures", "errors", "skipped")), "Unexpected test count or result")
    require([t.get("name") for t in root.findall("testcase")] == methods, "Wrong selected methods or execution order")
    log = Path(log_path).read_text(errors="replace")
    require(re.findall(r"(?m)^R07_CASE_START:([a-z]+)\s*$", log) == markers, "Wrong actual method marker order")
    if label == "measurement":
        pattern = r"(?m)^R08_HTTP:(\w+) warmups=5 samples=30 min_ms=(\d+\.\d{3}) p50_ms=(\d+\.\d{3}) p95_ms=(\d+\.\d{3}) max_ms=(\d+\.\d{3}) body_bytes_min=(\d+) body_bytes_max=(\d+)\s*$"
        rows = list(re.finditer(pattern, log))
        require([m[1] for m in rows] == ["resource_page", "allocation_page", "requirement_page", "project_page", "overview"], "Missing/duplicate workload")
        for m in rows:
            times = list(map(float, m.groups()[1:5]))
            require(0 <= times[0] <= times[1] <= times[2] <= times[3] and 0 < int(m[6]) <= int(m[7]), "Invalid measurement")
            print(m[0].strip())
        fixture = re.findall(r"(?m)^R08_FIXTURE:resources=1001 allocations=1002 requirements=101 heap_before=\d+ heap_after=\d+ innodb_data_estimate=\d+ innodb_index_estimate=\d+\s*$", log)
        require(len(fixture) == 1, "Missing fixture receipt")
        print(fixture[0].strip())
    print(f"R0708_SELECTED_TESTS_OK:{label} count={len(methods)} failures=0 errors=0 skipped=0")
if __name__ == "__main__":
    main()
