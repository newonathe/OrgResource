# Report 41 — lifecycle actual, documented deadlock-counter correction, integration preflight

2026-11-25 PHT. Exact job logs and source ranges reviewed, MySQL official documentation verified, correction patch only. Prior reports remain historical. No local runtime or independent reviewer. No product, migrations or frozen changes. No private/hosted access.

```json
{
  "life": {
    "sha": "b48b289f4ddc3dd1fb0ac786487d188921c6909c",
    "run": 45489076064,
    "job": 138782207514,
    "maven": 103,
    "identity": 78,
    "playwright": 19,
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "all_jobs_success": true,
    "verdict": "VERIFIED_BOUNDED",
    "scope": "#32 actual same-process persisted claim state recovery and gated return/expiry, not process restart"
  },
  "failure": {
    "sha": "849a1284ef0b5ff641f1a933d2aedb60627ddf7a",
    "run": 45491262809,
    "job": 138789912276,
    "dedicated_run": 45491263013,
    "dedicated_job": 138789899851,
    "verdict": "FAILED",
    "actual": "78 identity/103 Maven tests, 1 failure, zero errors/skips. #24 line1501 rs.next false on SHOW GLOBAL STATUS LIKE 'Innodb_deadlocks', before blocker/HTTP lock cycle; lost-response/restart and packaged stages not reached."
  },
  "corrected": {
    "sha": "ef634ce7469f0b94b09ddcef097ad14a8c6e0206",
    "base": "849a1284ef0b5ff641f1a933d2aedb60627ddf7a",
    "runs": [
      [
        45491974364,
        "Verify",
        "queued",
        null
      ],
      [
        45491974383,
        "R07-R08 isolated preparation",
        "queued",
        null
      ]
    ],
    "report": "evidence/C9/R07_DEADLOCK_COUNTER_CORRECTION_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "source_authority": [
      "https://dev.mysql.com/doc/refman/8.0/en/innodb-information-schema-metrics-table.html",
      "https://dev.mysql.com/doc/refman/8.0/en/information-schema-innodb-metrics-table.html"
    ]
  },
  "integration": {
    "pr53": "d2cf2fa7da1fb71ab1c8bd3ab2029dea320beb12",
    "pr54": "9646ff743c6678af90aa357ee761ac11ed366dff",
    "pr55": "ef634ce7469f0b94b09ddcef097ad14a8c6e0206",
    "base55": "a1ba49aecce96ea24e82b0e2de4e1f210221aaa7",
    "deltaCounts": [
      27,
      21,
      15
    ],
    "shared": "extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java",
    "testBlobs": [
      "d96a5d1cf7891009ffce7378fc23d3cf51170aad",
      "39ccc6e9852c41f54367b569761898a255dbb70b",
      "f21cd6b6b0646322eefafd95dcd71b2798e3011c"
    ],
    "hunks": 7,
    "exact_base_reproduction": true,
    "unique_anchors": [
      1,
      1,
      1,
      1,
      1,
      1,
      1
    ],
    "tests_before_after": [
      78,
      78
    ],
    "state": "SOURCE_ONLY_DRY_RUN"
  }
}
```

R07 lifecycle b48b289f4ddc3dd1fb0ac786487d188921c6909c full CI45489076064 job138782207514 exact actual 103 Maven/78 identity/19 Playwright pass, 3/3 success and cleanup, #32 same-process stale claim worker and gated return/expiry bounded verified; no actual reminder restart. R07 deadlock candidate 849a1284ef0b5ff641f1a933d2aedb60627ddf7a full CI45491262809 job138789912276 actual 1 failure/0 errors/0 skips of103 Maven/78 identity; #24 line1501 SHOW GLOBAL STATUS LIKE 'Innodb_deadlocks' returned no row before blocker/HTTP cycle; dedicated45491263013 failure corroborated. MySQL8.0 official manual documents enabled lock_deadlocks INNODB_METRICS. Correction ef634ce7469f0b94b09ddcef097ad14a8c6e0206 uses COUNT/STATUS query twice, explicit enabled/existence and increased count; all original rollback/retry/deadlock assertions preserved, no configuration changes. Runs [[45491974364,"Verify","queued",null],[45491974383,"R07-R08 isolated preparation","queued",null]] discover by SHA if absent. No actual deadlock or response drop/JVM restart reached on failed head. Updated integration dry run PR53/54/55 delta27/21/15 paths, only shared Java test, seven unique hunks reproduce exact PR55 base bytes and apply to PR54 latest preserving78 tests; not written/run. R03 89 executed addenda/4 auth pending plus residual candidate; prior auth45490152665, packaged45490328103, residual45491538911 pending. Next consume actual corrected CI and prior pending logs, actual reminder restart (separate from durable operation restart), resolve residuals, integrate final exact refs/full CI, R09=false.
