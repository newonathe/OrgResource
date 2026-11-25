# Actual R07/R08 baseline and R03 stock receipt / borrower candidate

2026-11-25 PHT. PR55 actual [CI45482691119 job138759730608](https://github.com/newonathe/OrgResource/actions/runs/45482691119/job/138759730608) checkout 30f8d06 UTC20:37:49.6840241Z; selected concurrency at20:38:34.9044668Z, rollback20:38:48.7416933Z, reversed20:39:03.6662360Z, measurement20:39:26.1121325Z, all zero errors/failures/skips, all cleanup, final marker20:39:26.4175069Z. Exact receipt (numeric bytes, ms, seconds):

```json
{
  "sha": "30f8d06aa60a65377bad2e8df4443ca860be4e80",
  "pr": 55,
  "run": 45482691119,
  "job": 138759730608,
  "verdict": "VERIFIED_BOUNDED",
  "selected": [
    [
      "concurrency",
      1,
      27
    ],
    [
      "rollback",
      1,
      13
    ],
    [
      "reversed",
      2,
      14
    ],
    [
      "measurement",
      1,
      22
    ]
  ],
  "failures": 0,
  "errors": 0,
  "skips": 0,
  "cleanup_all": true,
  "metric_columns": [
    "workload",
    "min_ms",
    "p50_ms",
    "p95_ms",
    "max_ms",
    "body_bytes_min_equals_max"
  ],
  "metrics": [
    [
      "resource_page",
      13.086,
      15.964,
      21.212,
      22.539,
      15124
    ],
    [
      "allocation_page",
      16.714,
      20.438,
      25.676,
      36.447,
      70925
    ],
    [
      "requirement_page",
      10.401,
      12.442,
      15.891,
      20.786,
      13864
    ],
    [
      "project_page",
      7.185,
      8.463,
      9.776,
      11.289,
      242
    ],
    [
      "overview",
      16.685,
      17.948,
      21.885,
      25.258,
      2022
    ]
  ],
  "warmups_each": 5,
  "samples_each": 30,
  "fixture": {
    "resources": 1001,
    "allocations": 1002,
    "requirements": 101
  },
  "heap_before": 288394104,
  "heap_after": 117553080,
  "innodb_data_estimate": 294912,
  "innodb_index_estimate": 950272,
  "jar_bytes": 72486427,
  "bundle_file_bytes": 463308,
  "npm_ci_s": 4,
  "npm_build_s": 3,
  "java": "21.0.13",
  "node": "24.22.3",
  "maven": "3.10.0",
  "scope": "serial synthetic test-context HTTP, no SLA/hosted/private/peak heap claim"
}
```

R07 actual independent/reverse selected cases proved. R08 150 samples serial loopback real MySQL, correctness and scoped equality enforced. Heap before/after affected by GC, not peak/leak metric; storage whole fresh schema estimate; bundle uncompressed; single npm/build/Maven samples, no cold/warm repetitions; test-context HTTP, not packaged JAR latency. Full Verify pending. No formal R07/R08 closure while R03 open. PR54 actual stock [CI45480855446 job138753279268](https://github.com/newonathe/OrgResource/actions/runs/45480855446/job/138753279268) checkout 590275c UTC20:27:05.8709800Z, 78 identity and103 Maven zero failures/errors/skips UTC20:29:51/52, cleanup20:38:31, 19 Playwright20:41:20; all3 jobs. Borrower candidate source exact, no actual CI yet; required key subset and nullable timestamp whitelist, not strict null serialization proof. PR55 dedicated 30f8d06aa60a65377bad2e8df4443ca860be4e80 CI45482691119 job138759730608 actual exact checkout, selected concurrency1/rollback1/reversed2/measurement1, zero failures/errors/skips, all four schema/account cleanup, marker R0708_PREPARATION_OK. R07 individual/reverse proof verified bounded; R08 actual 150 authenticated HTTP measured +25 warmups, all response and scoped row assertions, five p50/p95 workloads and actual size/heap/build metrics (see report33). Single synthetic test-context, not packaged JAR/hosted/private/repeated baseline, no SLA. PR54 stock 590275cf195d44eee05d8ad4902b8ff9482c1941 actual CI45480855446 job138753279268 exact checkout 103 Maven/78 identity/19 Playwright, zero failures/errors/skips, 3/3 jobs, #78 25 refusals bounded. New borrower 74b2450ddd508d5e07a387fa7c1212b42d52779a extends #54 with actual second linked allocation, borrower cursor cross-person/tenant/limit, exact permitted DTO keys+required fields, active same-name vs suspended directory, ineligible creation409 and scoped consent refusals. CI [[45483605533,"in_progress",null]]. PR54 allocation and PR55 full Verify pending. Check PR54 allocation CI45481804571 job138756620003, borrower candidate [[45483605533,"in_progress",null]] (discover by SHA if absent), and PR55 full Verify45482690895 job138759730694. Dedicated PR55 receipt already actual, reuse; reconcile remaining R07 deadlock/expiry-return/lost-response/claim restart proof against source and existing receipts; R08 repeated cold/warm and packaged measurements scope; finish remaining R03 governance/read/privacy, reconcile stacked PR55 base drift and PR53/54/55 final source, full regression. R07/R08 formal dependencies still open; R09=false.
