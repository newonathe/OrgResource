# Report 39 — workspace/identity actual, authentication and packaged HTTP candidates

2026-11-25 PHT. Live PR47/53/54/55, issue50, root/continuity/graph, exact actual CI logs, source and candidate patch inspected. Inline model, no independent reviewer, no local runtime. No product/migration/frozen changes. Corrected prior compilation failure remains historical. The dedicated run does not execute the new lifecycle test. Packaged probe reuses existing isolated environment; no private or hosted access.

```json
{
  "workspace": {
    "sha": "7ff4f8d7bbeb1e83068f380ebfcaa3216a4f727b",
    "pr": 54,
    "run": 45486716110,
    "job": 138773948560,
    "tests": [
      61,
      62
    ],
    "routes": [
      "R088",
      "R089",
      "R090",
      "R091",
      "R092",
      "R093"
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
  "identity": {
    "sha": "afdfefca995949ba917bb4ab3f99dfd14cbebd1b",
    "pr": 54,
    "run": 45487476059,
    "job": 138776580407,
    "tests": [
      6
    ],
    "routes": [
      "R032",
      "R085",
      "R086",
      "R087"
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
    "sha": "b48b289f4ddc3dd1fb0ac786487d188921c6909c",
    "pr": 55,
    "run": 45489075914,
    "job": 138782206434,
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
          13.242,
          16.552,
          32.953,
          35.311
        ],
        [
          19.204,
          21.305,
          44.503,
          177.888
        ],
        [
          12.43,
          13.953,
          22.368,
          25.348
        ],
        [
          8.454,
          9.289,
          27.542,
          153.985
        ],
        [
          17.456,
          19.63,
          77.541,
          167.158
        ]
      ],
      [
        [
          14.565,
          17.27,
          22.268,
          23.376
        ],
        [
          19.363,
          21.757,
          35.621,
          41.305
        ],
        [
          12.329,
          14.671,
          35.204,
          38.138
        ],
        [
          7.762,
          9.377,
          53.584,
          67
        ],
        [
          17.452,
          19.858,
          57.475,
          130.955
        ]
      ],
      [
        [
          13.639,
          17.111,
          22.966,
          31.09
        ],
        [
          19.197,
          22.575,
          27.089,
          27.913
        ],
        [
          12.692,
          14.9,
          18.688,
          25.728
        ],
        [
          8.209,
          9.194,
          12.144,
          12.195
        ],
        [
          17.587,
          20.164,
          34.377,
          38.831
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
    "scope": "same isolated test-context workloads; does not execute #32"
  },
  "cand": {
    "sha": "3a1e46cddf0cf1bd519b6c493205727b9310a97a",
    "base": "852f2f29f58065e9cf581cb70d58f642da343c1f",
    "pr": 54,
    "runs": [
      [
        45490152665,
        "Verify",
        "in_progress",
        null
      ]
    ],
    "report": "evidence/C8/R03_AUTH_CSRF_LOGIN_RECOVERY_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "routes": [
      "R033",
      "R034",
      "R036",
      "R037"
    ],
    "tests": [
      5,
      7,
      8,
      36
    ],
    "observed_red": "NOT_RUN"
  },
  "packaged": {
    "sha": "18f47cc762f5bcf0ed857350b177c4ccdf035ff1",
    "base": "b48b289f4ddc3dd1fb0ac786487d188921c6909c",
    "pr": 55,
    "runs": [
      [
        45490328267,
        "R07-R08 isolated preparation",
        "in_progress",
        null
      ],
      [
        45490328103,
        "Verify",
        "in_progress",
        null
      ]
    ],
    "report": "evidence/C9/R08_PACKAGED_HTTP_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "scope": "same-JVM warm repeat in existing networkless Stage1D restored public 8-org fixture, selected tagged org SQL counts; exact domain equality after measurement, 450 measured/75 warmups proposed, not executed yet"
  },
  "integration": {
    "base55": "a1ba49aecce96ea24e82b0e2de4e1f210221aaa7",
    "pr54": "3a1e46cddf0cf1bd519b6c493205727b9310a97a",
    "pr55": "18f47cc762f5bcf0ed857350b177c4ccdf035ff1",
    "test_blobs": [
      "d96a5d1cf7891009ffce7378fc23d3cf51170aad",
      "ef9823118ff5ab1080a2d39a67ddc51209560750",
      "fb16708f40e4a476b4a56e77ea81a26c5832b613"
    ],
    "hunks": 6,
    "exact_base_reproduction": true,
    "unique_anchor_counts": [
      1,
      1,
      1,
      1,
      1,
      1
    ],
    "ordered_tests_before_after": [
      78,
      78
    ],
    "state": "SOURCE_ONLY_DRY_RUN"
  }
}
```

PR54 workspace 7ff4f8d7bbeb1e83068f380ebfcaa3216a4f727b CI45486716110 job138773948560 and identity afdfefca995949ba917bb4ab3f99dfd14cbebd1b CI45487476059 job138776580407 both actual 103 Maven/78 identity/19 Playwright zero failures/errors/skips, 3/3 success and cleanup. PR55 lifecycle helper correction b48b289f4ddc3dd1fb0ac786487d188921c6909c dedicated CI45489075914 job138782206434 actual six selected cases/cleanup, 450 measured/75 warmups, no #32 execution by dedicated job; full CI45489076064 job138782207514 still active. New PR54 3a1e46cddf0cf1bd519b6c493205727b9310a97a R033/R034/R036/R037 #5/7/8/36 and shared login helper exact DTO, two independent CSRF sessions, login/recovery generic error/envelope, identity and session rows, successful recovery audit, alias correlation; CI45490152665 pending. New PR55 18f47cc762f5bcf0ed857350b177c4ccdf035ff1 R08 packaged measurement in existing Stage1D recovered networkless JVM, actual owner/SQL scope, five endpoints and 3x30 samples+5 warmups, same-JVM not cold or hosted; exact domain equality remains required. CI45490328103 and45490328267 pending. Renewed integration dry-run all six PR55 Java hunks reproduce exact prior-base bytes and uniquely apply to latest PR54, preserve78 tests; actual combined branch not written. Remaining project CI45488537632 job138780332013 still active, lifecycle actual full pending. All 93 routes now have executed addendum or candidate; final route count depends on prior status overlap. Independent 0/93, residuals open. Next consume actual CI; actual lost-response/deadlock/process restart evidence and residual R03; packaged measurement verify; final integrated exact heads and R09=false.
