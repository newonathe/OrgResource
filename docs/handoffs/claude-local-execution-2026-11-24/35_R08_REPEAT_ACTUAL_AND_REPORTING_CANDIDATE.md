# Report 35 — R08 repeated actual measurements and R03 reporting candidate

2026-11-25 PHT. Read live PR47/53/54/55, root/continuity, task graph, AGENTS, quality gate and GOAT/implementation/verification skills. Inline current model; no independent reviewer or local executor claimed. Exact prior scopes retained. Actual logs at [dedicated repeat](https://github.com/newonathe/OrgResource/actions/runs/45484557507/job/138766342054) and [full initial Verify](https://github.com/newonathe/OrgResource/actions/runs/45482690895/job/138759730694), checkout, selected test and cleanup markers, test summaries and 19 Playwright. New reporting candidate has no executed receipt yet; actual PR54 diff inspected (two paths, test plus report). ReportingResource and ReportingComponent source reviewed fully; relevant test ranges and scoped helper reviewed; no production changes. Earlier source-path guesses 404 corrected from original file index. Source-only test-first additional assertions; no red or independent review claimed.

## Actual receipts

```json
{
  "receipt": {
    "sha": "c11f47a23a82b0dac4afa8d5d4234280eb16f67e",
    "pr": 55,
    "run": 45484557507,
    "job": 138766342054,
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
        17
      ],
      [
        "reversed",
        2,
        19
      ],
      [
        "measurement",
        1,
        28
      ],
      [
        "measurement_repeat2",
        1,
        28
      ],
      [
        "measurement_repeat3",
        1,
        27
      ]
    ],
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "cleanup_all": true,
    "workloads": [
      "resource_page",
      "allocation_page",
      "requirement_page",
      "project_page",
      "overview"
    ],
    "metric_columns": [
      "min_ms",
      "p50_ms",
      "p95_ms",
      "max_ms",
      "first_warmup_ms"
    ],
    "measurements": [
      [
        "measurement",
        [
          [
            19.537,
            23.984,
            32.696,
            41.516,
            26.262
          ],
          [
            26.385,
            30.598,
            36.984,
            37.273,
            39.241
          ],
          [
            18.289,
            20.929,
            27.29,
            28.414,
            22.288
          ],
          [
            12.541,
            13.734,
            17.211,
            28.223,
            14.353
          ],
          [
            26.146,
            28.492,
            38.267,
            71.678,
            27.376
          ]
        ],
        [
          185073560,
          106545552
        ]
      ],
      [
        "measurement_repeat2",
        [
          [
            19.721,
            26.576,
            28.996,
            29.141,
            34.45
          ],
          [
            27.475,
            31.001,
            43.816,
            53.125,
            36.809
          ],
          [
            17.544,
            20.354,
            24.398,
            25.419,
            29.545
          ],
          [
            11.201,
            13.187,
            17.705,
            20.642,
            13.081
          ],
          [
            25.322,
            27.877,
            49.389,
            58.333,
            29.114
          ]
        ],
        [
          168728968,
          116565576
        ]
      ],
      [
        "measurement_repeat3",
        [
          [
            21.664,
            25.616,
            30.174,
            31.316,
            26.028
          ],
          [
            28.626,
            32.757,
            52.009,
            66.497,
            40.43
          ],
          [
            18.229,
            20.335,
            24.475,
            24.792,
            26.647
          ],
          [
            12.163,
            14.445,
            21.835,
            23.528,
            18.786
          ],
          [
            26.059,
            30.87,
            62.926,
            75.911,
            29.412
          ]
        ],
        [
          339054768,
          122459320
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
    "measured_requests": 450,
    "warmups": 75,
    "fixture": {
      "resources": 1001,
      "allocations": 1002,
      "requirements": 101
    },
    "innodb_data_estimate": 294912,
    "innodb_index_estimate": 950272,
    "jar_bytes": 72486427,
    "bundle_file_bytes": 463308,
    "npm_ci_s": 3,
    "npm_build_s": 5,
    "scope": "serial synthetic test-context HTTP; three fresh schemas and JVMs on same CI server; no packaged/cold-start/SLA/peak heap/hosted/private inference"
  },
  "regression": {
    "sha": "30f8d06aa60a65377bad2e8df4443ca860be4e80",
    "pr": 55,
    "run": 45482690895,
    "job": 138759730694,
    "maven": 103,
    "identity": 78,
    "playwright": 19,
    "failures": 0,
    "all_jobs_success": true,
    "verdict": "VERIFIED_BOUNDED"
  },
  "cand": {
    "sha": "b3eb16e5ddf757e67cfa8296eb56f9f340ef632c",
    "base": "9f374d77addd36b9c5985f25d2e874116970aa2d",
    "pr": 54,
    "runs": [
      [
        45485647999,
        "Verify",
        "in_progress",
        null
      ]
    ],
    "report": "evidence/C8/R03_REPORTING_READ_ONLY_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "routes": [
      "R042",
      "R052",
      "R057"
    ],
    "tests": [
      45,
      46,
      47
    ],
    "observed_red": "NOT_RUN"
  }
}
```

First warmup not true cold-start, heap before/after not peak/leak, InnoDB sizes estimates, same-server cache possible, no aggregate percentile pooling. Runtime fixture synthetic and test-context HTTP, no packaged JAR measurement yet. No new build repetition claim: one npm install/build and three Maven invocations (first package; others tests). R07/R08 formal dependency preserved. Independent IR4, L06 private positive, final integration and R09 open. No private inputs, hosted DB, SMS, publication, merge or protected changes. Next: PR55 c11f47a23a82b0dac4afa8d5d4234280eb16f67e dedicated CI45484557507 job138766342054 actual logs: individual concurrency1/rollback1/reversed2/three measurements1 each; all zero failures/errors/skips and six cleanup markers; 450 measured/75 warmups, five workloads x three fresh schemas/JVMs, actual per-repeat p50/p95/first-warmup/response/heap/storage/build metrics report35. PR55 initial full Verify30f8d06 CI45482690895 job138759730694 actual 103 Maven/78 identity/19 Playwright, all 3 jobs success. New PR54 b3eb16e5ddf757e67cfa8296eb56f9f340ef632c R042/R052/R057 tests #45–47, 33 scoped comparison call sites (loops multiply requests), exact 12-table preservation on existing refusals and permitted reporting, source-allowed event fields, exact resource export keys; no product/migration change. Candidate runs [[45485647999,"Verify","in_progress",null]] (discover by SHA if absent). Prior borrower45483605533 job138762991854, governance45484467643 job138766024990 and PR55 repeat full Verify45484557514 job138766342243 pending at last check. Consume completed logs first; no new tests inferred. Next selected-workspace/read gaps, R07 return-expiry/deadlock/claim-restart/lost-response reconciliation and R08 packaged HTTP/build repetitions; integrate final PR53/54/55 exact source without overlap or merge/main; R09=false.
