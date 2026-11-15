# Fresh GitHub implementation and CI inspection — O3–O13
**Status: REMOTE CODE INSPECTED; SUCCESSFUL CI OBSERVED; LOCAL TEST EVIDENCE REPORTED; O14 NOT VERIFIED.** **Audit date: 2026-11-15, Asia/Manila.** Git source commit `fd34a8a66242f795dda758d45f220f2c331472db` on `feat/o11-o13-system-acceptance-release-2026-11-15` / PR #6; [repo](https://github.com/newonathe/OrgResource), [PR #6](https://github.com/newonathe/OrgResource/pull/6), [phase report](https://github.com/newonathe/OrgResource/blob/fd34a8a66242f795dda758d45f220f2c331472db/evidence/O11-O13/PHASE_REPORT.md). Branch not merged; default `main` old O1.

## Reopened actual production source, not only summaries
| Source | Direct observed code contract | What remains unproven |
|---|---|---|
| `backend/pom.xml` | Target Java8, Boot2.1.0, Jersey2.26, Retrofit2.4 and Gson converter2.1, MySQL connector8.0.13 | Full transitive support/security in 2026 and independent clean local build not tested here |
| `backend/src/main/java/app/config/JerseyConfig.java` | `@ApplicationPath("/api")` registers five controller classes and Jackson + error mapper | UI-hostable authenticated production boundary is not present |
| `backend/src/main/java/app/rest/controllers/AllocationController.java` | Jersey `@Path("/allocations")`, `@GET` list and eight POSTs including A09 consent withdrawal, Spring `@Autowired` component delegation | Actual each route live request not independently executed here |
| `backend/src/main/java/app/components/AllocationComponent.java` | Create PENDING, approve, issue, return whole-batch, consume once, cancel, guarded delete, revoke SMS, `expireIfEligible`; fixed `@Transactional(isolation=READ_COMMITTED)`, project→resource→requirement→allocation lock helpers; clock injected; consume `total` decrement and status in one transaction | Real concurrent MySQL trials/scheduled job not independently rerun; V1 remains single-org no authentication/ledger |
| `backend/src/main/java/app/repositories/AllocationRepository.java` | JPQL sums effective APPROVED holds (end>now), reusable ISSUED custody, effective requirement covered; bounded expired and reminder candidate queries, row lock annotations | Performance/EXPLAIN and 2026 security not independently tested |
| `backend/src/main/java/app/components/ResourceComponent.java` | Resource create/list/availability/update/delete; `READ_COMMITTED` update, expectedTotal guard, computed holds/issued | No independent negative-case/new race execution by this researcher |
| `backend/src/main/java/app/components/ExpiryScheduler.java` | Actual `@Scheduled(fixedDelayString=...)`, `@ConditionalOnProperty`, bounded PageRequest, separate component `expireIfEligible` invocation | Whether enabled in every runtime/environment and actual durable run tested only by saved reports |
| `backend/src/main/java/app/components/ReminderScheduler.java` and `ReminderComponent.java` | Separate scheduled scan/recover, `claimOne/prepareForDispatch/recordOutcome/markStaleClaimUnknown`, short transactions, eligibility/time-window checks | Actual one-and-only external HTTP in runtime not independently triggered |
| `backend/src/main/java/app/reminders/FakeTwilioTransport.java` | Retrofit + Gson converter, sends to loopback-only HTTP via dummy Basic header, validates accepted reply, distinguishes `SIMULATED` / `UNKNOWN` / `REJECTED`, disables OkHttp automatic retries | Twilio production acceptance/delivery not proven nor authorized |
| `frontend/package.json`, `frontend/src` | React19.3.1, Router7.18.5, Vite8.3.8, TS5.9.3, actual React source in pages/shell/api and tests | Full pixel-by-pixel responsive browser audit not performed here |
| `docs/design/ACADEMIC_GLASS_COMMONS_V1.md` | Written locked art direction v5, UI guide | No unlocked alternative should silently replace it |
| `docs/compliance/CLO9_IMAGE_DECISION.md` | Source-grounded unresolved image receiving/serving decision; no image feature claimed | Canvas/instructor verification required |

**Current V1 is truly present as Java/React source**, unlike old planning-only state. It is **not yet a publicly secure multi-tenant product**. Counts from actual controller sources and `evidence/O11-O13/ROUTE_PARITY.md`: **25 original routes plus separate A09 = 26 documented implemented method/path handlers**. This is **static source coverage**, not independently executed functional parity.

## What the GitHub Actions workflow truly does
`.github/workflows/verify.yml` has backend job on `ubuntu-latest` with MySQL 8.0.46 service, Temurin **Java 8**, Node **24**, `npm --prefix ../frontend ci/typecheck/test/lint/build`, Maven `-Pbundle-react-ui clean verify`, actual `java -jar ... --spring.profiles.active=test` smoke, `curl` root/static asset and five API GET collections. It uploads the Java8 executable JAR artifact. Second frontend job repeats npm CI/typecheck/unit/lint/build.

Observed workflow result for PR#6 HEAD `fd34a8...`: **Verify / run 43683241632 SUCCESS**. This establishes remote CI job success as GitHub reported; it does NOT by itself prove full Postman lifecycle mutations, Playwright browser E2E, 100-way MySQL concurrency, Twilio HTTP, live SMS, student video, Canvas submission or O14.

## Local test and release evidence categories
The latest `evidence/O11-O13/PHASE_REPORT.md` claims local acceptance (author-reported evidence, researcher did **not** rerun): 55 Maven JDK8 tests, 15 frontend unit tests, 5 Playwright browser flows, 73 Newman calls/105 assertions, React+Java8 JAR smoke, Eclipse/source ZIP packaging and SHA manifest. `ROUTE_PARITY.md` contains detailed four-collection table: O1 15/29, O3 CRUD 20/30, O3 Department 11/19, O4/O5 27/27 => total 73/105. These are **saved execution claims**, not an independently observed local workstation action. The CI workflow does compile/build/test/bundle/smoke, but does **not** explicitly invoke Newman or Playwright as part of its YAML.

| Phase | Current evidence classification |
|---|---|
| O1/2 | Old source and demo scripts; O2 video **USER REPORTED** recorded in historical continuity, actual MP4/Canvas receipt **NOT VERIFIED** |
| O3 | CRUD updates and code present, old O3 PR open, code now included in downstream PR stack; CI build observed |
| O4/O5 | Allocation lifecycle and expiry source present; phase reports tests, not independently rerun |
| O6/O7 | FakeTwilio Retrofit, scheduled reminders and frontend foundations exist in source |
| O8–O10 | React views and Academic Glass v5 source present; current PR #5 open/unmerged |
| O11/O13 | Test/release scripts/phase report present; CI successfully reported at HEAD `fd34...` |
| O12 | Conditional syllabus CLO9 unresolved; no upload image API proved |
| **O14** | **NOT VERIFIED / NOT CLAIMED COMPLETE**; real academic final demonstration, Canvas receipt, authorship disclosure and instructor-accepted gate unverified |

## Security/production gap
The original system is loopback, local trusted operator, single organization, fictional data, no real tenant identity/RBAC, Spring Boot 2.1/Java8 with old dependency support profile, and SMS simulator only. Neither GitHub Actions success nor completed UI makes it safe to publish as a real org platform. New V2 authentication, tenant scopes, immutable event ledger and backup/restore are **design proposals** in research SRS, not V1 implementation.

## Precise next acceptance checks without implementation
Reconfirm current PR#2–#7 bases/heads after any new coding commits; compare route definitions from actual controllers and Swagger/Postman contracts, inspect migration strategy and asset-build manifests; when user authorizes, independently run clean Java8 JAR + MySQL + Postman+Playwright and targeted concurrency/privacy test on exact commit; verify O14/Canvas separately. Until then course build evidence status = CI SUCCESS, local detailed test = REPORTED, product-wide compliance = PARTIAL/UNRESOLVED.

## 2026-11-15 fresh live repo recheck for changed pre-Canvas mandate
Private GitHub rechecked **before** applying research docs:
- `main` `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`; branch listing returned O3, O2, O4–O13 academic branches, old continuity, research docs and main; **no O14 branch**. No release records observed from GitHub API query.
- PR #2–#7 all OPEN/unmerged; PR#6 O11/O13 head `fd34a8a66242f795dda758d45f220f2c331472db`, PR#7 DRAFT docs head `fb29c38ceded8ffd69d389b9bb18bd648f8e86c1` at audit start. PR#7 base is #5's O8/O10 branch, not #6, hence no assumption all code is included in research PR.
- GitHub `Verify` run 43683241632 for academic PR#6 and 43686833986 for research PR#7 had `success` conclusion. Directly reopened O11/O13 local phase report: **author reports** Java55, UI15, Playwright5, Newman73 requests/105 assertions; these have NOT been independently rerun by researcher.
- The attached original assignment PDF prints final Dec5. Syllabus lists CLO9 receive/serve images and authorship/outside help; instructor/Canvas interpretations unverified.
- **No evidence O14 complete**, no V2 tenant/secure auth product code, hosted release, free account, tested off-host DR or V2 acceptance. A CI green commit is not production engineering PASS.
**Change:** this latest audit now feeds O14 baseline → pre-Canvas P-ACCEPT → A-FINAL → student's manual submission. [18](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md) and [25](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md). No implementation activity performed.
