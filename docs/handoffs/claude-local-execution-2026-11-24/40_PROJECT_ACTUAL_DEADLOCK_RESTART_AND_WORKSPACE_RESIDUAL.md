# Report 40 — project actual; deadlock/restart and workspace residual candidates

2026-11-25 PHT. Live PR47/53/54/55, issue50, root/continuity/graph, exact job logs, relevant source and candidate patches reviewed. Inline model; no local runtime or independent reviewer. New deliberate deadlock candidate failed selected case; no claim of success, preserve prior actual rollback. Source candidates no runtime/migration/frozen changes. No hosted or private access.

```json
{
  "project": {
    "sha": "852f2f29f58065e9cf581cb70d58f642da343c1f",
    "pr": 54,
    "run": 45488537632,
    "job": 138780332013,
    "tests": [
      55,
      61,
      65,
      75
    ],
    "routes": [
      "R039",
      "R059",
      "R061",
      "R062",
      "R064",
      "R072"
    ],
    "maven": 103,
    "identity": 78,
    "playwright": 19,
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "all_jobs_success": true,
    "verdict": "VERIFIED_BOUNDED"
  },
  "repeat": {
    "sha": "18f47cc762f5bcf0ed857350b177c4ccdf035ff1",
    "pr": 55,
    "run": 45490328267,
    "job": 138786607431,
    "verdict": "VERIFIED_BOUNDED",
    "selected": [
      [
        "concurrency",
        1
      ],
      [
        "rollback",
        1
      ],
      [
        "reversed",
        2
      ],
      [
        "measurement",
        1
      ],
      [
        "measurement_repeat2",
        1
      ],
      [
        "measurement_repeat3",
        1
      ]
    ],
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "cleanup_all": true,
    "measured": 450,
    "warmups": 75,
    "jar_bytes": 72486427,
    "bundle_bytes": 463308,
    "workloads": [
      "resource_page",
      "allocation_page",
      "requirement_page",
      "project_page",
      "overview"
    ],
    "columns": [
      "min_ms",
      "p50_ms",
      "p95_ms",
      "max_ms"
    ],
    "measurements": [
      [
        [
          17.926,
          20.339,
          26.777,
          52.047
        ],
        [
          20.585,
          24.213,
          30.277,
          30.324
        ],
        [
          12.936,
          15.929,
          29.22,
          58.593
        ],
        [
          8.346,
          10.454,
          20.165,
          113.409
        ],
        [
          18.606,
          20.448,
          153.055,
          346.932
        ]
      ],
      [
        [
          15.085,
          19.796,
          59.086,
          71.779
        ],
        [
          22.758,
          28.171,
          53.629,
          103.183
        ],
        [
          14.521,
          17.894,
          44.676,
          69.546
        ],
        [
          15.637,
          17.58,
          36.742,
          53.19
        ],
        [
          21.18,
          35.163,
          55.307,
          123.562
        ]
      ],
      [
        [
          16.625,
          20.05,
          26.183,
          190.481
        ],
        [
          18.698,
          23.85,
          47.434,
          50.183
        ],
        [
          13.382,
          15.55,
          25.354,
          50.486
        ],
        [
          8.551,
          10.278,
          12.557,
          14.275
        ],
        [
          17.294,
          20.739,
          26.587,
          50.978
        ]
      ]
    ],
    "body_bytes": [
      15124,
      70925,
      13864,
      242,
      2022
    ],
    "scope": "test-context HTTP; not packaged probe"
  },
  "cand": {
    "sha": "9646ff743c6678af90aa357ee761ac11ed366dff",
    "base": "3a1e46cddf0cf1bd519b6c493205727b9310a97a",
    "pr": 54,
    "runs": [],
    "report": "evidence/C8/R03_WORKSPACE_RESIDUAL_ORACLE_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "routes": [
      "R032",
      "R085",
      "R086",
      "R087",
      "R088",
      "R089",
      "R090",
      "R091",
      "R092",
      "R093"
    ],
    "tests": [
      6,
      61
    ],
    "observed_red": "NOT_RUN"
  },
  "reliability": {
    "sha": "849a1284ef0b5ff641f1a933d2aedb60627ddf7a",
    "base": "18f47cc762f5bcf0ed857350b177c4ccdf035ff1",
    "pr": 55,
    "runs": [
      [
        45491262809,
        "Verify",
        "in_progress",
        null
      ],
      [
        45491263013,
        "R07-R08 isolated preparation",
        "completed",
        "failure"
      ]
    ],
    "report": "evidence/C9/R07_ACTUAL_DEADLOCK_AND_LOST_RESPONSE_RESTART_CANDIDATE_2026-11-25.md",
    "acceptance": "FAILED_DEDICATED_FULL_PENDING",
    "scope": "actual reverse-order SQL lock blocker, intended observed deadlock HTTP503 exact rollback and retry; loopback upstream201/downstream EOF, stop/start same schema, same actor replay and exact domain dump. Not actual reminder claim restart.",
    "actual_failure": {
      "run": 45491263013,
      "job": 138789899851,
      "case": "rollback",
      "tests": 1,
      "failures": 1,
      "errors": 0,
      "skips": 0,
      "detail": "Dedicated runner filtered diagnostic to summary; exact assertion not yet known. Full job138789912276 pending. Concurrency case1 passed and cleanup confirmed. Remaining cases did not execute."
    }
  }
}
```

PR54 project 852f2f29f58065e9cf581cb70d58f642da343c1f CI45488537632 job138780332013 actual 103 Maven/78 identity/19 Playwright zero failures/errors/skips, 3/3 pass and cleanup. Route register 89 executed addenda/4 pending original auth candidate; 10 previously executed rows have residual candidate, no independent acceptance. PR55 18f47cc762f5bcf0ed857350b177c4ccdf035ff1 dedicated CI45490328267 job138786607431 actual six selected cases and cleanup, 450 measured/75 warmups; repeated test-context metrics Report40 (overview p95 153.055/55.307/26.587ms), not packaged proof. New PR55 849a1284ef0b5ff641f1a933d2aedb60627ddf7a #24 actual reverse-order InnoDB blocker/HTTP503/rollback/retry candidate and Stage1D loopback response drop (upstream201/downstream EOF) plus distinct packaged JVM restart same schema/same actor replay/exact domain comparison. Dedicated CI45491263013 job138789899851 observed rollback case one failure, no errors/skips; concurrency passed/cleanup; exact failed assertion suppressed by runner, full CI45491262809 job138789912276 pending; do not infer root cause or executed lost-response/restart. New PR54 9646ff743c6678af90aa357ee761ac11ed366dff #6/#61 frozen selected owner read and write409, actual foreign requirement/project filters, exact workspace nested DTO/overview counts and preview candidate; discover CI by SHA. Prior full lifecycle45489076064, auth45490152665, packaged45490328103 still active. Next inspect full failure logs; correct only actual failure, add safe diagnostics if needed; verify packaged and auth, remaining real persisted reminder restart, then combine latest exact refs and final CI. R09=false.
