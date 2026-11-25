# Report 36 — borrower/governance actual, workspace candidate and recovery startup correction

2026-11-25 PHT. Applied existing AGENTS/GOAT/Superpowers constraints; inline execution; no independent reviewer claimed. Read live PR47/53/54/55, issue50, root/continuity/graph, source index, exact workspace handler/service/DTO/context and relevant tests; inspected exact two-file patches. Actual borrower/governance receipts from exact job logs. Actual full PR55 failure preserved; do not relabel as pass or version mismatch. Corrective candidate narrowly gates socket readiness on final container process, no new network or permissions. Failed temporary restorer logs absent, so exact causal race remains hypothesis. Other three recovery scripts use socket and loopback TCP. Local container returned ClientError; no local tests claimed. Source-only R03 additions preserve all existing role/status assertions; no new fixtures. Independent review and private input remain blocked. R07/R08 formal dependency remains open. No production/migration edits, hosted access, merge, deploy or Claude launch.

```json
{
  "receipts": [
    {
      "sha": "74b2450ddd508d5e07a387fa7c1212b42d52779a",
      "pr": 54,
      "run": 45483605533,
      "job": 138762991854,
      "tests": [
        54
      ],
      "routes": [
        "R051",
        "R053",
        "R054",
        "R055",
        "R056",
        "R058"
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
    {
      "sha": "9f374d77addd36b9c5985f25d2e874116970aa2d",
      "pr": 54,
      "run": 45484467643,
      "job": 138766024990,
      "tests": [
        60,
        76
      ],
      "maven": 103,
      "identity": 78,
      "playwright": 19,
      "failures": 0,
      "errors": 0,
      "skips": 0,
      "all_jobs_success": true,
      "verdict": "VERIFIED_BOUNDED"
    }
  ],
  "failure": {
    "sha": "c11f47a23a82b0dac4afa8d5d4234280eb16f67e",
    "pr": 55,
    "run": 45484557514,
    "job": 138766342243,
    "maven": 103,
    "identity": 78,
    "failures": 0,
    "errors": 0,
    "skips": 0,
    "playwright": "NOT_RUN",
    "verdict": "TEST_FAILED",
    "phase": "Stage1B cross-server recovery startup",
    "actual": "ERROR 2002 socket missing then C9_CROSS_SERVER_WRONG_MYSQL_VERSION",
    "cause": "startup temporary socket race hypothesis; failed restorer logs unavailable"
  },
  "fix": {
    "sha": "76a8e687581bd1b70582822a38b657744e22d387",
    "pr": 55,
    "base": "c11f47a23a82b0dac4afa8d5d4234280eb16f67e",
    "runs": [
      [
        45486540380,
        "Verify",
        "in_progress",
        null
      ],
      [
        45486540376,
        "R07-R08 isolated preparation",
        "in_progress",
        null
      ]
    ],
    "report": "evidence/C9/R07_CROSS_SERVER_STARTUP_RACE_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "observed_red": "actual original failure; targeted reproduction NOT_RUN"
  },
  "cand": {
    "sha": "7ff4f8d7bbeb1e83068f380ebfcaa3216a4f727b",
    "base": "b3eb16e5ddf757e67cfa8296eb56f9f340ef632c",
    "pr": 54,
    "runs": [
      [
        45486716110,
        "Verify",
        "queued",
        null
      ]
    ],
    "report": "evidence/C8/R03_WORKSPACE_READ_PRESERVATION_CANDIDATE_2026-11-25.md",
    "acceptance": "CI_PENDING",
    "routes": [
      "R088",
      "R089",
      "R090",
      "R091",
      "R092",
      "R093"
    ],
    "tests": [
      61,
      62
    ],
    "observed_red": "NOT_RUN"
  }
}
```

PR54 borrower 74b2450ddd508d5e07a387fa7c1212b42d52779a CI45483605533 job138762991854 and governance 9f374d77addd36b9c5985f25d2e874116970aa2d CI45484467643 job138766024990 actual checkout/logs, each 103 Maven/78 identity/19 Playwright, zero failures/errors/skips, 3/3 jobs success and cleanup; bounded receipts accepted. PR55 c11f47a full Verify45484557514 job138766342243 actual 103 Maven/78 identity pass but Stage1B ERROR2002 missing socket and misleading WRONG_MYSQL_VERSION; Playwright NOT_RUN. Prior dedicated repeated baseline remains verified bounded. Source cross-server script accepts socket-only temporary server; hypothesis not proven without restorer logs. New 76a8e687581bd1b70582822a38b657744e22d387 requires container PID1 mysqld plus authenticated SQL, separates version-query failure; same version/UUID/network/row/negative/cleanup oracles, 90 attempts unchanged. New runs [[45486540380,"Verify","in_progress",null],[45486540376,"R07-R08 isolated preparation","in_progress",null]]. Other three restore scripts require TCP plus socket; unchanged. New PR54 7ff4f8d7bbeb1e83068f380ebfcaa3216a4f727b extends #61/#62 R088–R093 all-role and home/away grouped 12-table equality, 12 individual stale cursor/role/read/suspension comparisons, exact resource/requirement/privileged overview keys. CI45486716110 queued. Prior reporting CI45485647999 job138770176863 at additive step, actual full receipt pending. Local container unavailable, no local shell/red execution or independent review. Check reporting and both new candidates actual logs first, then remaining R03/R07/R08 gaps, final combined source, R09=false.
