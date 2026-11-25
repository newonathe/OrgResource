# Report 38 — actual reporting and recovery correction; project and lifecycle candidates

2026-11-25 PHT. Live PR47/53/54/55, issue50, root/continuity/graph, route register and actual exact-head CI logs inspected. Reused AGENTS/quality and source mapping; inline model, no independent reviewer. Source review relevant exact handler/service/DTO/worker and test methods as recorded in candidate evidence; actual patches inspected. No local runtime executed this turn. No product changes; original failure remains in ledger. Test-only changes, new fixtures fictional and disposable. No private bytes or hosted access. R09 remains false.

```json
{
  "reporting": {
    "sha": "b3eb16e5ddf757e67cfa8296eb56f9f340ef632c",
    "pr": 54,
    "run": 45485647999,
    "job": 138770176863,
    "tests": [
      45,
      46,
      47
    ],
    "routes": [
      "R042",
      "R052",
      "R057"
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
  "restore": {
    "sha": "76a8e687581bd1b70582822a38b657744e22d387",
    "pr": 55,
    "run": 45486540380,
    "job": 138773351738,
    "maven": 103,
    "identity": 78,
    "playwright": 19,
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "all_jobs_success": true,
    "verdict": "VERIFIED_BOUNDED",
    "stage1b": {
      "mysql": "8.0.46",
      "orgs": 8,
      "allocations": 960,
      "sql_bytes": 1034301,
      "backup_ms": 143,
      "restore_ms": 396,
      "network_none": true,
      "independent_uuid": true,
      "exact_rows": true,
      "original_unchanged": true,
      "tamper_refused": true,
      "nonempty_refused": true
    },
    "stage1c": {
      "archive_bytes": 1034355,
      "backup_ms": 424,
      "restore_ms": 871,
      "negative_cases": 15,
      "empty_sessions": true,
      "exact_rows": true,
      "cleanup": true
    },
    "stage1d": {
      "archive_bytes": 1039695,
      "exact_non_session_rows": true,
      "cleanup": true
    },
    "cause": "prior transient initialization socket race hypothesis, not conclusively reproduced"
  },
  "cand": {
    "sha": "852f2f29f58065e9cf581cb70d58f642da343c1f",
    "base": "afdfefca995949ba917bb4ab3f99dfd14cbebd1b",
    "pr": 54,
    "runs": [
      [
        45488537632,
        "Verify",
        "in_progress",
        null
      ]
    ],
    "report": "evidence/C8/R03_PROJECT_READ_LEAD_AND_IMAGE_DELETE_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "routes": [
      "R039",
      "R059",
      "R061",
      "R062",
      "R064",
      "R072"
    ],
    "tests": [
      55,
      61,
      65,
      75
    ],
    "observed_red": "NOT_RUN"
  },
  "life": {
    "sha": "a034fb1c81ff1521c9bc9c0c32561c9c2cde515c",
    "base": "76a8e687581bd1b70582822a38b657744e22d387",
    "pr": 55,
    "runs": [
      [
        45488706073,
        "Verify",
        "queued",
        null
      ],
      [
        45488706396,
        "R07-R08 isolated preparation",
        "queued",
        null
      ]
    ],
    "report": "evidence/C9/R07_RETURN_EXPIRY_AND_STALE_CLAIM_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "tests": [
      32
    ],
    "observed_red": "NOT_RUN",
    "scope": "same-process persisted CLAIMED/DISPATCHING stale recovery; gated concurrent return and expiry, same key replay; NOT actual restart/forced overlap/deadlock/lost network response"
  }
}
```

PR54 reporting b3eb16e5ddf757e67cfa8296eb56f9f340ef632c CI45485647999 job138770176863 actual 103 Maven/78 identity/19 Playwright, zero failures/errors/skips, 3/3 success. PR55 recovery correction 76a8e687581bd1b70582822a38b657744e22d387 full CI45486540380 job138773351738 actual same suite pass, Stage1B MySQL8.0.46 8org/960 allocations networkless independent UUID exact rows SQL1034301 bytes 143/396ms; Stage1C actual AES-GCM archive1034355 bytes 15 negative cases, 424/871ms; Stage1D actual restored archive1039695 bytes and cleanup; prior startup race hypothesis retained, correction verified bounded. New PR54 852f2f29f58065e9cf581cb70d58f642da343c1f R039/R059/R061/R062/R064/R072 #55/61/65/75 exact project allocation DTO/canDelete, custodian detail and assignment403, lead options scoped equality, department keys, image bytes/version removal, two new same-org role fixtures. CI45488537632 pending. New PR55 a034fb1c81ff1521c9bc9c0c32561c9c2cde515c #32 persisted CLAIMED/DISPATCHING stale worker to UNKNOWN, equal-cutoff refusal and no re-claim/outcome; gated concurrent expiry/return, final states/one RETURN/net issued0/physical5, same key replay exact scoped rows. CI45488706073 and45488706396 pending; same-process interruption model, no actual restart/forced interleaving/deadlock/lost-response. Prior workspace45486716110 job138773948560 and identity45487476059 job138776580407 still active. Route register now 73 executed addenda/16 pending/4 original gaps R033/R034/R036/R037; 0/93 independent; residuals retained. Next consume actual receipts; CSRF/login/recovery, residual route oracles, actual deadlock/lost-response/restart, packaged HTTP; reapply PR55 final patch to PR54 and combine PR53, verify exact integrated CI before R09. No production, migration, frozen edition changes or Claude launch.
