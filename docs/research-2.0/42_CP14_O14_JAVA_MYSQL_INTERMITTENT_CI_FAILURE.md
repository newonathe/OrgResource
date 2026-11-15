# CP14 follow-up — reproducible academic MySQL test failure at O14 project-end boundary

**Research date:** 2026-11-15 PHT. **EVIDENCE VERIFIED directly from GitHub Actions jobs and exact test/query Java sources; ROOT CAUSE UNKNOWN.** No code changes, rerun, CI fix, or implementation claimed. O14 academic baseline remains draft/unmerged; this report exists only in draft PR#7 research docs.

## Observed GitHub workflow contrast, exact same underlying code

- [Push Verify 43732859018](https://github.com/newonathe/OrgResource/actions/runs/43732859018), SHA `f224e000361bb2214a59234634faa784855f1e61`: React typecheck/test/lint/build **PASS**; Java 8/MySQL job `132868965724` **FAIL**. Maven summary: `Tests run: 55, Failures: 1, Errors: 0, Skipped: 0`; `ResourceListAggregationMySqlTest.oneClockSampleAndOneGroupedQueryProduceExactAvailabilityAtProjectEndBoundary` at **line 107**, `java.lang.AssertionError: expected:<5> but was:<8>`. MySQL container reported `8.0.46`. This is a genuinely run integration test failure, not a documentation lint failure.

- [Pull request Verify 43732864033](https://github.com/newonathe/OrgResource/actions/runs/43732864033), same SHA `f224e000…`: Java 8/MySQL job `132868981895` **PASS** including exact class (`Tests run: 1, Failures: 0`) and full Maven (`Tests run: 55, Failures: 0`). Thus the commit's full CI status cannot honestly be summarized as uniformly green.

- Later CP14 SHA `b42a9ad4b946b3da6bc785fa998a0704cd3a1fdb` [push run 43733219846](https://github.com/newonathe/OrgResource/actions/runs/43733219846) **PASS** and [PR run 43733225845](https://github.com/newonathe/OrgResource/actions/runs/43733225845) **PASS**. These successful runs do NOT retroactively invalidate or explain the earlier observed failure. The research PR diff at CP14 contains **46 Markdown files under `docs/research-2.0/` only**—no product code change.

## Exact academic source observed

- `backend/src/test/java/app/components/ResourceListAggregationMySqlTest.java` SHA blob `d94d558f2ae2859f84a43c7ef2cc32d56c67a94c` is identical at the research branch and O14 PR#8 SHA `2dd336ed…`. Test uses fixed `NOW=LocalDateTime.of(2026,11,15,12,0)` in `Asia/Manila`, resets/stubs `@MockBean Clock`, creates reusable `Resource(total=10)`, an active+4h `APPROVED(q3)`, `ISSUED(q2)`, and separate ending-now `APPROVED(q4)` on same reusable resource. It expects availability `10-3-2=5` because ending-now hold expires at strict `endAt > now`; failure returned `8` (as if effective q3 approved hold absent, but this is a logical inference from arithmetic, not proven SQL root cause). Consumable/untouched assertions follow; test never reached them in failed run.

- `ResourceComponent.list()` obtains **one** `LocalDateTime.now(philippineClock)`, runs `AllocationRepository.sumResourceCommitments(resourceIds,APPROVED,ISSUED,now,...)`; grouped JPQL `SUM(CASE WHEN a.status = :approved AND a.requirement.project.endAt > :now THEN a.quantity ELSE 0 END)` plus `SUM(CASE WHEN status=ISSUED THEN quantity ELSE 0 END)`. Source compares entity project end time to injected clock. No evidence yet establishes whether the wrong sum arose from injection/mocking, JDBC type/session timezone, query semantics, test data visibility/order, cache, or another cause.

- **Important distinction:** This report does NOT prove a production resource overbooking defect or a vulnerability; it proves a nonuniform original test outcome at a project-end boundary. An unsound original test can undermine A-FINAL reproducibility and must be investigated and classified before academic evidence is certified.

## Narrow engineering investigation contract (for separately authorized Codex, NOT instructions to execute here)

**CI-01** same exact SHA, Docker MySQL8.0.46 and test JVM, run full 55-test suite repeatedly in isolated new DBs (proposed 20+ repetitions), both push/PR workflows or equivalent in safe dry-run. Record run IDs, test order, runner TZ, JVM TZ, MySQL `@@session.time_zone`, `@@global.time_zone`, schema datetime type, and exact test seed. Any failure is real; don't hide it with retries.

**CI-02** run `ResourceListAggregationMySqlTest` alone, paired after every other affected DB test, and full suite with fixed/random order. Compare fixture cleanup and shared Spring test `@MockBean Clock` context lifecycle. A test passing in isolation but failing in suite indicates a candidate isolation/order issue, not confirmed until evidence.

**CI-03** on failure, log redacted **actual** injected `Instant`, `ZoneId`, computed `now`, each relevant allocation ID/status/quantity/project endAt directly from MySQL, and grouped JPQL result *inside same transaction*. Assert project endAt `NOW+4h` is really greater than computed NOW before grouped query, compare separate `sumEffectiveHolds` and `sumIssuedCustody` results.

**CI-04** run strict boundary `endAt=NOW-1µs`, `=NOW`, `=NOW+1µs` and stable +4h regardless of host time; assert q4 expired q3 live q2 issued. Check JDBC/DB temporal mapping and persistence flush. Do not change the expected 5 without source/course design approval.

**CI-05** if a deterministic defect is proven, prepare minimal reviewed engineering patch/test fixtures on its own CODE branch and seek user signoff; this research chat must never edit original O14 app code or merge PRs. If only intermittent, leave acceptance **FAIL/FLAKY** until reproducibly resolved; no green run suppression or skipped test.

## Impact on research readiness and estimate

**O14 baseline academic acceptance remains OPEN** on source re-run determinism, despite CP14 both Verify runs green. The original five entities, 26 Jersey handlers and actual course stack remain required and unchanged. P-ACCEPT, A-FINAL, private final Canvas submit not passed. Research-next step still R0–R10/owner choices, but this adds a concrete **CI-reproducibility blocker** to execution validation. CP14 estimated **3–7** research passes; due new CI-specific research/acceptance follow-up, conservatively **3–8 focused passes remain** if no more material discoveries. Not a deadline or complete prebuild E2E claim.

## Source links

[Original O14 test at pinned SHA](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/backend/src/test/java/app/components/ResourceListAggregationMySqlTest.java), [ResourceComponent.java](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/backend/src/main/java/app/components/ResourceComponent.java), [AllocationRepository.java](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/backend/src/main/java/app/repositories/AllocationRepository.java), GitHub Actions runs linked above. Full test-source and two job logs independently retrieved in this CP14 research session; no external tool run or app modification.