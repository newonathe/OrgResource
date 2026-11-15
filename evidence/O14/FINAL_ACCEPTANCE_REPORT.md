# O14 Interim Academic Baseline Acceptance Report

**Status:** Technical evidence passed; academic sign-off remains pending.
**Date:** 2026-11-15 (Asia/Manila)
**Starting source commit:** `fd34a8a66242f795dda758d45f220f2c331472db`
**O14 implementation/test commit:** `286c5560d3b69dec11b47fbeea73d659507e8154`
**Branch:** `feat/o14-academic-final-acceptance-2026-11-15`
**Implementation parent:** O11/O13 feature branch, PR #6.
**O14 PR:** [#8](https://github.com/newonathe/OrgResource/pull/8), stacked on the O11/O13 branch.
**Scope:** Interim academic baseline only. This is not final product acceptance or production readiness.

## 1. Direction and source authority

O14 reconciles the current academic implementation with the assignment, syllabus, submitted proposal, and the user's updated release direction. The assignment requires **at least five justified entity types**; the present five-entity model is the traceable academic baseline, not a permanent product maximum. Any production expansion is deferred until a separate reviewed prompt. The separate OrgResource 2.0 research branch/PR is proposed research and is not an implementation authorization.

The current application is the Java 8-compatible Spring Boot 2.1/Jersey backend with MySQL, Retrofit/Gson, scheduled jobs, and an eight-screen React/TypeScript/Vite interface. Its current contract is five entities, the 25 proposal operations, and the separately documented A09 consent-withdrawal amendment. Historical nine-entity/68-operation documents are explicitly labeled as non-current.

Current Canvas instructions could not be accessed in this environment. This report does not claim Canvas review, instructor acceptance, academic sign-off, or submission.

## 2. Scope and changed deliverables

O14 changes update current-state and academic-compliance documentation, record unresolved CLO9 and Twilio decisions, add route-level acceptance traceability and a dependency-integrity Postman suite, provide portable first-run and student-operated demo instructions, and correct the release artifact workflow. No production feature, authentication, tenant system, image subsystem, or new domain entity was implemented.

See `evidence/O14/ROUTE_ACCEPTANCE_MATRIX.md`, `docs/compliance/FINAL_COURSE_COMPLIANCE_MATRIX.md`, `docs/compliance/CLO9_IMAGE_DECISION.md`, `docs/compliance/TWILIO_COURSE_DECISION.md`, `docs/PORTABLE_FIRST_RUN.md`, and `docs/DEMO_SCRIPT_O14.md`.

## 3. Entity and route contract

The implementation has exactly five JPA domain entities: `Department`, `Project`, `Resource`, `ResourceRequirement`, and `Allocation`. Local MySQL inspection found exactly five InnoDB tables: `department`, `project`, `resource`, `resource_requirement`, and `allocation`. Observed FK counts were one from `allocation`, one from `project`, and two from `resource_requirement`.

The API acceptance matrix maps all 26 operations (25 original proposal operations plus A09) to controller, component, request/response, Postman, and runtime evidence. A route is not marked successful merely because an annotation exists. See the matrix for operation-specific statuses and evidence.

## 4. Academic compliance decisions

| Area | Status | Evidence and boundary |
|---|---|---|
| CLO1 Eclipse/debugger workflow | **NOT_RUN** | Java source export includes Eclipse metadata. Import/debug in an independent Eclipse installation was not performed. |
| CLO2 Java project and libraries | **PASS** for source/build evidence | Java 8 Maven project and dependencies are present; independent Eclipse import remains NOT_RUN. |
| CLO3 Spring Boot/Jersey | **PASS** for implementation evidence | Jersey resources delegate to Spring `@Autowired` components; route acceptance is recorded separately. |
| CLO4 Postman | **PASS** for local executable evidence | Current v2.1 collections were run with Newman. Import in the student's own Postman and live presentation remain student actions. |
| CLO5 JPA/Hibernate/MySQL | **PASS** for observed local evidence | Java/MySQL tests and direct schema inspection ran against fictional `orgresource_test`; separate-machine acceptance was not run. |
| CLO6 JPQL | **PASS** for exercised query evidence | Coverage/availability and scheduler query behavior are covered in the backend tests. |
| CLO7 Javax Validation | **PASS** for exercised validation behavior | DTO validation and controlled HTTP failures are covered by tests/collections. |
| CLO8 Retrofit/Gson | **PASS** for simulator HTTP behavior | Retrofit/Gson communicates with local FakeTwilio. No real provider call, SMS, or delivery is claimed. |
| CLO9 image receive/serve | **UNRESOLVED** | Syllabus p. 2 states image receiving/serving; CLO mapping on pp. 3–4 conflicts. The assignment/proposal do not clearly scope an OrgResource image operation. Canvas was unavailable. See the decision record; no speculative image feature was added. |
| CLO10 final application | **PASS** for implementation evidence | The current application is implemented and tested; CLO9 scope, course grading, and instructor acceptance are not established by this report. |
| Assignment external-service/Twilio wording | **UNRESOLVED** | The assignment says external services, “at the very least Twilio”; current implementation verifies a local FakeTwilio simulator only. Instructor/current Canvas acceptance of that substitute is unknown. See `docs/compliance/TWILIO_COURSE_DECISION.md`. |
| Assignment minimum of five entities | **PASS** for baseline | Five justified entities are implemented. “At least five” is not interpreted as a maximum. |
| Recording, group discussion, peer review, authorship, Canvas | **MANUAL STUDENT ACTION** | No recording, signatures, evaluations, Canvas upload/receipt, or approval was fabricated or performed. |

The full requirement-by-requirement source locators and remaining actions are in `docs/compliance/FINAL_COURSE_COMPLIANCE_MATRIX.md`.

## 5. Java 8 and frontend verification

The O14 packaging script ran on source commit `286c5560d3b69dec11b47fbeea73d659507e8154`, using Temurin 8u512 and the protected local `orgresource_test` settings. Maven resolved the course dependencies despite non-blocking metadata TLS/401 warnings.

| Gate | Command or action | Result |
|---|---|---|
| Java 8 backend | `scripts/package-release.ps1` ran `backend\mvnw.cmd -B -Pbundle-react-ui clean verify` with protected local test environment | **PASS** — 55 tests, 0 failures, 0 errors, 0 skipped; Java 8u512. Non-blocking dependency-repository warnings were observed. |
| Frontend | `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, `npm run build` | **PASS** — 15 tests across 3 test files; typecheck, lint, and build passed. `npm ci` reported 7 moderate advisories; dependencies were not upgraded. |
| Browser acceptance | `npm run e2e`; Vite frontend connected to the packaged JAR's local Java/MySQL API | **PASS** — 5 Playwright journeys passed, including connected persistence, ambiguous POST response handling, browser-history filters, responsive light/dark screenshots, and motion controls. |
| Visual screenshot review | Inspected all six generated O8–O10 contact sheets: desktop/mobile light and dark, detail/error states, and tablet allocations | **PASS** — no obvious clipping or overlap was visible; long mobile lists are naturally dense. This is visual evidence from captured local pages, not hosted or physical-device acceptance. |
| Postman/Newman | O1, O3, D3, O4/O5/A09, and O14 dependency-integrity collections against the packaged JAR | **PASS** — 96 requests and 142 assertions, zero failures. O14 suite: 23 requests/37 assertions. |
| MySQL schema | Read-only inspection of protected `orgresource_test` after Newman and browser acceptance | **PASS** — exactly five tables, all InnoDB; rows observed: department 36, project 30, resource 55, resource_requirement 41, allocation 38. FK counts: 1 allocation, 1 project, 2 resource_requirement. No phone/contact fields or row values were displayed. |
| Local UI screenshots | `evidence/O8-O10/screenshots/*.jpg` | **PASS** — actual Playwright captures were reviewed. The screenshots show synthetic fictional records only. |

The O14 dependency suite confirmed guarded deletes return conflict and preserve referenced records before deleting only the eligible fixtures. These tests used the isolated fictional test schema; no existing non-test database was reset or dropped.

Within the Java suite, `FakeTwilioTransportTest` exercised a loopback HTTP endpoint, dummy authorization, expected form fields, response parsing, rejection, and ambiguous failure without retry. The MySQL lifecycle tests also exercised the Spring expiry scheduler. The reminder dispatcher test is a controlled simulator test; this did not send real SMS or prove handset delivery.

## 6. Release, runtime, and clean-extraction acceptance

| Gate | Actual check | Result |
|---|---|---|
| Release package | `scripts/package-release.ps1` on the O14 branch with Java 8; built executable JAR, complete-source ZIP, Java/Eclipse source ZIP, SHA-256 list, and JSON manifest | **PASS**. Exact artifact names, sizes, SHA-256 values, tool versions, branch, and source commit are recorded in the adjacent `deliverables/O14/RELEASE-MANIFEST.json` and `SHA256SUMS.txt`. Those files are generated from the final committed O14 source. |
| Complete-source ZIP | Extracted to a new temporary directory; verified tracked React/Java source, lockfile, Maven Wrapper, and tests; ran `npm ci`, typecheck, 15 frontend tests, lint, build, and Maven verify | **PASS** — 55 Java tests, 0 failures/errors/skips. Private environment/credential file paths were absent. |
| Java/Eclipse source ZIP | Extracted to a separate new temporary directory; checked `.project`, `.classpath`, `.settings`, Maven Wrapper, POM, main and test source; ran `mvnw -B verify` on Java 8 | **PASS** — 55 Java tests, 0 failures/errors/skips. Independent Eclipse UI import/debug remains **NOT_RUN**. |
| Packaged JAR static/UI assets | `GET /` and the referenced bundled JavaScript and CSS | **PASS** — HTTP 200 for HTML, JS, and CSS. Observed bundles were 350,974-byte JS and 39,713-byte CSS. |
| Packaged JAR API | `GET /api/departments`, `/projects`, `/resources`, `/requirements`, and `/allocations` | **PASS** — HTTP 200 JSON arrays; before acceptance suites added their fixtures, observed counts were 32, 26, 50, 36, and 33. |
| Packaged JAR browser routing | Headless Playwright opened `http://127.0.0.1:8080/#/projects`, confirmed the Projects heading, refreshed, and confirmed it again | **PASS** — HashRouter deep link and refresh work when served by the bundled JAR. No in-app browser plugin was used. |
| Packaged JAR persistence | Created a fictional Department (ID 420), confirmed by GET, stopped Java, restarted the same JAR, confirmed the same record, then deleted and verified absence | **PASS** — data survived restart; only the O14-created fixture was removed. MySQL remained available. |
| O14 portable launcher guard | Ran the launcher with the isolated `orgresource_test` URL | **PASS** — rejected the test schema before startup; no database mutation occurred. |
| Empty-schema first run | New `orgresource` schema/account on a separate computer | **NOT_RUN** — the safe procedure is documented; no administrator account or independent machine was available. |
| GitHub | O14 branch pushed; draft [PR #8](https://github.com/newonathe/OrgResource/pull/8) opened against the O11/O13 feature branch | **PASS** for branch/PR existence. The exact final source commit, remote SHA, and artifact hashes are in the adjacent manifest after final repackaging. No merge, tag, Canvas submission, or deployment occurred. |

The portable setup guide documents a separate empty `orgresource` schema and a least-privileged local account. A separate computer and newly provisioned schema were not claimed as verified at this revision. Never point the portable first-run instructions at `orgresource_test` or any shared/personal database.

## 7. Privacy, safety, and non-production boundary

- The O14 test data is fictional. No private database password or real personal contact was printed or committed.
- The portable script prompts for a password without echo, restricts the URL to local `orgresource`, binds to loopback, disables reminders, and does not delete/reset data.
- FakeTwilio is a simulator. No live SMS, real Twilio delivery, or actual mobile receipt is claimed.
- The Java 8/Spring Boot 2.1 academic runtime is not a supported public service and must not be exposed publicly.
- O14 does not implement the deferred multi-organization product requirements: authentication/session recovery, tenant isolation, memberships/RBAC, a stock/audit ledger, production migrations, backup/recovery operations, supported public hosting, or a proven $0 operating configuration.
- `docs/research-2.0/` and its separate research PR remain proposed material and are outside this release.

## 8. Manual student actions

1. Check current Canvas for due date, rubric, submission module, CLO9/image direction, Twilio expectation, and recording requirements.
2. Confirm the actual project group and current presentation/early-presentation rules.
3. Personally operate and record any required demonstration/group discussion; verify the instructor can access the real recording link if applicable.
4. Complete the Certificate of Authorship and honestly acknowledge AI/external help.
5. Complete applicable peer evaluation with actual group members.
6. Upload the correct final deliverables and verify Canvas receipt.

## 9. Verdict

**ACADEMIC BASELINE CONDITIONALLY READY.** The current course-aligned implementation, source exports, local tests, API acceptance, and packaged-JAR runtime checks passed. CLO9 image scope and whether the FakeTwilio simulator satisfies the assignment's Twilio wording remain **UNRESOLVED** pending current Canvas or instructor direction. Independent first-run provisioning, current Canvas instructions, recording, group obligations, and student academic actions remain open. This is not **FINAL PRODUCT ACCEPTED** and does not authorize the production rewrite.
