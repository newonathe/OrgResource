# Report 42 — auth, packaged and deadlock actual; restart and integrated candidate

2026-11-25 PHT. Live root/start/graph/issue and PR53/54/55 read; exact job logs and relevant original source reviewed, no local execution or independent reviewer. Candidate branches public-safe, no production or protected migration/frozen changes. Prior failures retained. Integration draft separate from main; no gate closure on metadata. Corrected receipt field is a source diagnosis because original failure logged class only.

```json
{
  "auth": {
    "sha": "3a1e46cddf0cf1bd519b6c493205727b9310a97a",
    "pr": 54,
    "run": 45490152665,
    "job": 138785983133,
    "tests": [
      5,
      7,
      8,
      36
    ],
    "routes": [
      "R033",
      "R034",
      "R036",
      "R037"
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
  "packaged": {
    "sha": "18f47cc762f5bcf0ed857350b177c4ccdf035ff1",
    "run": 45490328103,
    "job": 138786607428,
    "maven": 103,
    "identity": 78,
    "playwright": 19,
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "all_jobs_success": true,
    "verdict": "VERIFIED_BOUNDED",
    "scope": "actual packaged recovered JVM, serial HTTP including JSON decoding; same-JVM three repeats; not cold start, not hosted",
    "fixture": {
      "allocation": 120,
      "department": 2,
      "project": 6,
      "resource": 20,
      "resource_requirement": 18,
      "PENDING": 0,
      "ISSUED": 6,
      "CONSUMED": 32
    },
    "jar_bytes": 72486427,
    "jar_sha256": "76e5977499edb6a01a0c6de95ce166c2975d08f70bcdbd57680049d1af32c10a",
    "measured": 450,
    "warmups": 75,
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
      "max_ms",
      "first_warmup_ms",
      "body_min",
      "body_max"
    ],
    "measurements": [
      [
        [
          14.307,
          17.333,
          216.93,
          239.523,
          51.793,
          3125,
          3125
        ],
        [
          22.71,
          29.93,
          58.133,
          58.724,
          88.075,
          75742,
          75742
        ],
        [
          10.303,
          11.846,
          19.808,
          21.821,
          16.138,
          5167,
          5167
        ],
        [
          9.33,
          10.758,
          20.184,
          43.637,
          60.119,
          1050,
          1050
        ],
        [
          13.866,
          17.915,
          278.85,
          367.342,
          68.158,
          1865,
          1868
        ]
      ],
      [
        [
          8.092,
          10.503,
          261.872,
          342.266,
          59.932,
          3125,
          3125
        ],
        [
          16.252,
          18.032,
          77.608,
          346.354,
          17.59,
          75742,
          75742
        ],
        [
          7.696,
          9.128,
          61.361,
          133.894,
          20.018,
          5167,
          5167
        ],
        [
          6.679,
          8.336,
          101.745,
          126.218,
          7.383,
          1050,
          1050
        ],
        [
          11.817,
          13.367,
          23.05,
          38.051,
          15.598,
          1868,
          1868
        ]
      ],
      [
        [
          6.382,
          11.598,
          71.404,
          98.18,
          12.416,
          3125,
          3125
        ],
        [
          13.295,
          16.998,
          37.204,
          75.91,
          20.5,
          75742,
          75742
        ],
        [
          5.794,
          7.986,
          12.995,
          13.08,
          7.539,
          5167,
          5167
        ],
        [
          5.272,
          6.105,
          8.416,
          67.512,
          6.115,
          1050,
          1050
        ],
        [
          11.512,
          13.207,
          19.637,
          19.961,
          17.786,
          1868,
          1868
        ]
      ]
    ]
  },
  "deadlock": {
    "sha": "ef634ce7469f0b94b09ddcef097ad14a8c6e0206",
    "dedicated_run": 45491974383,
    "dedicated_job": 138792386449,
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
          16.21,
          18.84,
          50.289,
          52.883
        ],
        [
          20.501,
          26.356,
          33.532,
          59.849
        ],
        [
          13.686,
          15.622,
          69.765,
          136.115
        ],
        [
          8.075,
          10.176,
          14.065,
          61.781
        ],
        [
          18.673,
          21.297,
          39.251,
          47.853
        ]
      ],
      [
        [
          15.063,
          19.272,
          29.742,
          34.174
        ],
        [
          21.958,
          25.931,
          78.434,
          178.223
        ],
        [
          14.21,
          16.051,
          67.553,
          75.218
        ],
        [
          9.05,
          10.951,
          99.203,
          125.324
        ],
        [
          18.81,
          21.975,
          52.173,
          78.878
        ]
      ],
      [
        [
          14.51,
          20.5,
          67.654,
          73.541
        ],
        [
          18.799,
          22.24,
          329.784,
          358.303
        ],
        [
          14.378,
          16.464,
          74.45,
          192.585
        ],
        [
          9.182,
          11.012,
          212.882,
          214.954
        ],
        [
          20.318,
          23.368,
          197.825,
          206.936
        ]
      ]
    ],
    "full_run": 45491974364,
    "full_job": 138792387677,
    "full_maven": 103,
    "full_identity": 78,
    "full_maven_failures": 0,
    "actual_marker": "R07_ACTUAL_DEADLOCK_OK:observed_lock_wait=true mysql_counter_increased=true http503=true exact_rollback=true same_key_retry=true replay_preserved=true",
    "verdict": "VERIFIED_BOUNDED",
    "full_verdict": "FAILED_LATER_STAGE1D",
    "lost_response_marker": "C9_RUNTIME_ASSERTION_OK:lost-response-upstream-201-downstream-eof",
    "restart_ready": "C9_RUNTIME_JAR_READY:source-restarted",
    "failure": "C9_RUNTIME_FAILURE:KeyError after new JVM readiness, exact line suppressed; replay equality/remaining restore and browser NOT_RUN"
  },
  "reminder": {
    "sha": "87a9aae65b8406cc716b473dbbf57283a98da302",
    "base": "ef634ce7469f0b94b09ddcef097ad14a8c6e0206",
    "report": "evidence/C9/R07_REMINDER_JVM_RESTART_CANDIDATE_2026-11-25.md",
    "runs": [
      [
        45492673291,
        "Verify",
        "in_progress",
        null
      ],
      [
        45492673308,
        "R07-R08 isolated preparation",
        "completed",
        "success"
      ]
    ],
    "scope": "post-archive-proof target clone only, three real issued encrypted-consent allocations, SQL stages two stale and one fresh states while stopped, distinct restart scheduler, exact unchanged fields/rows and versions, disabled transport; not actual transport crash",
    "acceptance": "CI_PENDING",
    "correction": {
      "sha": "de6d2ed2787b2c3baf5a480dbc2756eeefa3a1cb",
      "report": "evidence/C9/R07_REMINDER_SHELL_QUOTING_CORRECTION_2026-11-25.md",
      "reason": "Source-only self-review: literal backticks inside shell SQL; CHAR(96), no oracle relaxation",
      "runs": [
        [
          45492735209,
          "Verify",
          "in_progress",
          null
        ],
        [
          45492735233,
          "R07-R08 isolated preparation",
          "in_progress",
          null
        ]
      ]
    }
  },
  "correction": {
    "sha": "5afd326f4bd4e03c6027a9a639de682a1996d5e1",
    "base": "de6d2ed2787b2c3baf5a480dbc2756eeefa3a1cb",
    "report": "evidence/C9/R07_RESTART_RECEIPT_DTO_CORRECTION_2026-11-25.md",
    "runs": [
      [
        45493345999,
        "Verify",
        "queued",
        null
      ],
      [
        45493345989,
        "R07-R08 isolated preparation",
        "in_progress",
        null
      ]
    ],
    "reason": "Source identifies OperationView.action vs MovementReceipt.kind, exact failed line not logged; new private original data and ETag equality, safe exception class/line; post-prune oracle unchanged",
    "acceptance": "CI_PENDING"
  },
  "integration": {
    "pr": 56,
    "branch": "test/r09-integrated-evidence-candidate-2026-11-25",
    "base54": "9646ff743c6678af90aa357ee761ac11ed366dff",
    "pr53": "d2cf2fa7da1fb71ab1c8bd3ab2029dea320beb12",
    "pr55": "5afd326f4bd4e03c6027a9a639de682a1996d5e1",
    "initial_sha": "c68252db41f562d0230269844794048471dac0cf",
    "sha": "9d0902b4e8ea2045a30a4ff4a524e213a3b6a7b3",
    "initial_tree": "80a03ebfdceb5389f16c97d8ecbe7c9b505c0fd3",
    "initial_delta": 46,
    "initial_blobs": 44,
    "shared_java_blob": "d4153e2f428c5f97dc0f4b1862826155e82f8820",
    "hunks": 7,
    "ordered_tests": 78,
    "initial_runs": [
      [
        45493192571,
        "Verify",
        "in_progress",
        null
      ],
      [
        45493192652,
        "R07-R08 isolated preparation",
        "completed",
        "skipped"
      ]
    ],
    "correction_blobs": [
      "c0371dfe7f7190ffd34da3800cf7f05ab5dd4b81",
      "3f8f3d70b635d2abf1001a8b9e45d00be8a68a7c"
    ],
    "report": "evidence/C9/R09_INTEGRATED_EVIDENCE_CANDIDATE_2026-11-25.md",
    "correction_report": "evidence/C9/R09_INTEGRATED_RESTART_RECEIPT_CORRECTION_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "dedicated_note": "workflow exact head_ref guard skips PR56; source PR55 dedicated evidence retained, final integrated dedicated scope decision open; full Verify runs on draft"
  }
}
```

R03 final auth 3a1e46cddf0cf1bd519b6c493205727b9310a97a CI45490152665 job138785983133 103 Maven/78 identity/19 Playwright all pass, 3/3 cleanup. Route register 93/93 bounded executed addenda; independent0/93 and residuals open, residual45491538911 pending. R08 packaged 18f47cc762f5bcf0ed857350b177c4ccdf035ff1 full CI45490328103 job138786607428 exact 103/78/19 and cleanup pass, actual 450 timed+75 warmups on selected tagged workspace 120 allocations/20 resources/18 requirements/6 projects/2 departments; p95 by workload repeats in Report42, JAR72486427 bytes SHA76e5977499edb6a01a0c6de95ce166c2975d08f70bcdbd57680049d1af32c10a; same JVM not cold or hosted. R07 deadlock ef634ce7469f0b94b09ddcef097ad14a8c6e0206 dedicated45491974383 job138792386449 all selected pass, full45491974364 job138792387677 actual R07_ACTUAL_DEADLOCK_OK and 103/78 zero failures; later Stage1D upstream201/downstreamEOF and distinct restarted JAR ready, then KeyError, full run failed, restore/browser NOT_RUN. Corrected source 5afd326f4bd4e03c6027a9a639de682a1996d5e1 uses temporary OperationView.action, exact initial upstream data/ETag equality and safe exception line; durable MovementReceipt.kind unchanged, CI45493345999/45493345989 pending. Reminder restart candidate 87a9aae65b8406cc716b473dbbf57283a98da302, shell quoting self-review correction de6d2ed2787b2c3baf5a480dbc2756eeefa3a1cb (CHAR(96)); post-original archive proof target clone only, staged stale/fresh claims across distinct actual JVM, actual CI pending. Integrated draft PR56 initial c68252db41f562d0230269844794048471dac0cf, parent PR54 9646ff743c6678af90aa357ee761ac11ed366dff, exact PR53 27 and PR55 17 nonshared blobs +7 reconciled Java hunks (78 ordered tests), 46 path delta all allowed, corrected latest 9d0902b4e8ea2045a30a4ff4a524e213a3b6a7b3 carries exact PR55 correction blobs and report; initial Verify45493192571 pending, dedicated skipped by branch guard, latest runs discover by SHA. Final combined proof not yet available. Next consume actual corrected full logs, reminder restart and residual, align exact latest refs, verify integrated runtime and decide dedicated final scope, R09=false. No merge/deploy/Claude launch.
