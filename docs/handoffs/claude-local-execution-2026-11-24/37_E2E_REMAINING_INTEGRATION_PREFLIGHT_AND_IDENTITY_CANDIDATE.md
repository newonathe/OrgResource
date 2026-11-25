# Report 37 — remaining E2E work, integration preflight and identity candidate

2026-11-25 PHT. Actual live PR47/53/54/55, issue50/26/27/34, graph/ownership, route register, R07 report29, exact source/session DTO and patch inspection. Current model inline, no independent reviewer. No local container execution this pass. No production/migration/frozen changes or protected data access. R09 dependencies retained.

## Exact receipts and dry-run
```json
{
  "receipt": {
    "sha": "76a8e687581bd1b70582822a38b657744e22d387",
    "pr": 55,
    "run": 45486540376,
    "job": 138773351759,
    "verdict": "VERIFIED_BOUNDED",
    "selected": [
      [
        "concurrency",
        1,
        20
      ],
      [
        "rollback",
        1,
        12
      ],
      [
        "reversed",
        2,
        12
      ],
      [
        "measurement",
        1,
        19
      ],
      [
        "measurement_repeat2",
        1,
        18
      ],
      [
        "measurement_repeat3",
        1,
        18
      ]
    ],
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "cleanup_all": true,
    "measured": 450,
    "warmups": 75,
    "fixture": {
      "resources": 1001,
      "allocations": 1002,
      "requirements": 101
    },
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
      "first_warmup_ms"
    ],
    "measurements": [
      [
        [
          11.032,
          14.984,
          19.183,
          20.888,
          15.725
        ],
        [
          15.05,
          19.332,
          28.421,
          37.24,
          20.945
        ],
        [
          9.916,
          12.168,
          16.668,
          18.277,
          15.821
        ],
        [
          6.756,
          9.365,
          11.89,
          35.09,
          9.3
        ],
        [
          14.609,
          17.632,
          20,
          22.272,
          43.124
        ]
      ],
      [
        [
          10.899,
          13.88,
          20.776,
          21.16,
          19.249
        ],
        [
          15.106,
          18.949,
          23.778,
          30.22,
          22.911
        ],
        [
          9.179,
          11.128,
          15.946,
          20.898,
          23.166
        ],
        [
          7.047,
          7.951,
          9.589,
          11.625,
          7.982
        ],
        [
          13.312,
          15.286,
          22.324,
          30.891,
          16.739
        ]
      ],
      [
        [
          10.554,
          13.727,
          18.09,
          18.9,
          21.158
        ],
        [
          14.786,
          17.367,
          22.375,
          24.268,
          21.211
        ],
        [
          9.451,
          10.925,
          14.237,
          14.591,
          14.939
        ],
        [
          6.115,
          7.181,
          9.721,
          10.67,
          9.705
        ],
        [
          13.764,
          15.836,
          18.651,
          20.618,
          16.547
        ]
      ]
    ],
    "heap_samples": [
      [
        255479528,
        122351984
      ],
      [
        101798992,
        166299232
      ],
      [
        212256640,
        259257440
      ]
    ],
    "innodb_estimate": [
      294912,
      950272
    ],
    "body_bytes": [
      15124,
      70925,
      13864,
      242,
      2022
    ],
    "scope": "test-context serial HTTP, no packaged or cold-start/SLA claim; not proof of Stage1B correction"
  },
  "candidate": {
    "sha": "afdfefca995949ba917bb4ab3f99dfd14cbebd1b",
    "base": "7ff4f8d7bbeb1e83068f380ebfcaa3216a4f727b",
    "pr": 54,
    "runs": [
      [
        45487476059,
        "Verify",
        "in_progress",
        null
      ]
    ],
    "report": "evidence/C8/R03_IDENTITY_SESSION_READ_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "routes": [
      "R032",
      "R085",
      "R086",
      "R087"
    ],
    "tests": [
      6
    ],
    "observed_red": "NOT_RUN"
  },
  "integration": {
    "base52": "d3e8a8550e00e3bb9423d84f51f8141783e71fd1",
    "pr53": "d2cf2fa7da1fb71ab1c8bd3ab2029dea320beb12",
    "pr54_at_preflight": "7ff4f8d7bbeb1e83068f380ebfcaa3216a4f727b",
    "pr55": "76a8e687581bd1b70582822a38b657744e22d387",
    "pr55_branchpoint": "a1ba49aecce96ea24e82b0e2de4e1f210221aaa7",
    "delta_counts": [
      27,
      17,
      7
    ],
    "shared_path": "extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java",
    "test_blobs": [
      "d96a5d1cf7891009ffce7378fc23d3cf51170aad",
      "f737106571242c67e651b777f150e4f67be190c7",
      "6a64b6fd36c0f43506ffbb53697ba1f0d3db36bc"
    ],
    "hunks": 4,
    "exact_base_reproduction": true,
    "unique_anchor_counts": [
      1,
      1,
      1,
      1
    ],
    "ordered_tests_before_after": [
      78,
      78
    ],
    "state": "SOURCE_ONLY_DRY_RUN",
    "notes": "No combined source written or run; refresh final heads; PR53 27 changed paths disjoint, PR55 7 paths relative to branchpoint (6 disjoint); use exact patches/expected SHA; do not merge main."
  }
}
```

## Remaining owner-separated queue

- Chat: consume actual CI; R03 residual directed gaps, avoid redoing 22,320 historical combinations. Original-gap rows before new #6: R032/033/034/036/037/039/059/061/062/064/072/085/086/087. #6 addresses four, leaves ten original-gap rows plus residuals even on executed routes. Inspect actual oracle before declaring bounded closure. Private IR4 and independent acceptance separate.
- Chat: R07 resolve Stage1B actual correction receipt; remaining deadlock/return-expiry/lost-response/claim-restart evidence on real disposable MySQL; reuse #23/24, #32/42 consent and stage1D receipt. R08 package HTTP and reproducible build evidence, no unapproved SLO. Prior repeat baseline retained. R09 refresh actual source heads, integrate only approved feature/handoff branch, exact protected blobs, run full Verify and dedicated gate against combined head, update local commands/prompt/index/graph, no Claude launch until ready.
- Claude: L00 preflight; L03 native Windows package and nested L01/L02; L00B private path/hash; L04 standalone 96; L05 restricted IR4; L06 exact approved mixed full positive under POSIX/WSL with owner consent; L08 sanitized handback. L07 separate operator/off-host proof.
- Owner/reviewer: G01 per-route independent approval #34; G02 exact Northflank clone/backup/off-host and separate owner-approved live import #26 (NOT_RUN); G03 C9 exit; G04 C10 and G05 C11 separate authorization; G06 DEC-D01 #27 proposed only, no V8. Student G07 independent actual Canvas deadline, frozen Course A private DOCX/render, Windows/Eclipse, final hashes/rights, own video/authorship/peer/instructor and submission. Printed deadline Dec 5 2026, Canvas unverified.

PR55 76a8e687581bd1b70582822a38b657744e22d387 dedicated CI45486540376 job138773351759 exact logs: individual concurrency/rollback, reverse two and three measurement cases passed, six cleanup markers, 450 measured/75 warmups; scoped metrics report37. Full Verify45486540380 job138773351738 still active; dedicated job does not exercise Stage1B. Reporting45485647999 job138770176863 and workspace45486716110 job138773948560 still active. New PR54 afdfefca995949ba917bb4ab3f99dfd14cbebd1b #6 R032/R085/R086/R087 actual source-directed exact account/session/org/member DTO, real nonmember/suspended/frozen/unknown selection404, stable cookie/scoped rows, frozen org excluded from list but membership retained in session; fixture frozen state restored finally. Candidate CI [[45487476059,"Verify","in_progress",null]] discover by SHA if absent. Integration preflight exact PR52->53 (27 files), PR52->54 (17), branchpoint a1ba49ae->55 (7): only shared path C1IdentityAcceptanceTest; exact 4 PR55 hunks reproduce old-base PR55 bytes and apply uniquely to 7ff4f8d7 with all 78 tests retained; dry-run only, rerun against final SHA. 93 route register prior candidate states 70 executed addenda/9 pending/14 original gaps; independent0/93. Remaining R03 targeted residuals, R07 actual return/expiry/deadlock/lost-response/claim-restart, R08 packaged HTTP, R09 integrate + final actual CI and pin handoff. R04/R06B remote bounded complete; L00/L03/L01/L02/L00B/L04/L05/L06/L08 local, L07 operator; G01 independent, G02 host P0, G03 C9 exit, G04/G05 separate approvals, G06 proposed, G07 student. R09=false.
