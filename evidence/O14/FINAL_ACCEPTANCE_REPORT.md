# O14 Interim Academic Baseline Acceptance Report

**Status:** Acceptance work in progress; final artifact, clean-extraction, and remote PR evidence will be appended after verification.
**Date:** 2026-11-15 (Asia/Manila)
**Starting source commit:** `fd34a8a66242f795dda758d45f220f2c331472db`
**Branch:** `feat/o14-academic-final-acceptance-2026-11-15`
**Implementation parent:** O11/O13 feature branch, PR #6.
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

## 5. Pre-package verification observed

These local checks were executed against the O11/O13 implementation source whose backend and frontend feature code O14 preserves. The exact committed O14 release gates will be rerun below after the report and source are committed.

| Gate | Command or action | Result |
|---|---|---|
| Java 8 backend | `backend\\mvnw.cmd -B -Pbundle-react-ui clean verify` with protected local test environment | **PASS** — 55 tests, 0 failures, 0 errors, 0 skipped; Java 8u512. Non-blocking dependency-repository warnings were observed. |
| Frontend | `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, `npm run build` | **PASS** — 15 tests across 3 test files; typecheck, lint, and build passed. `npm ci` reported 7 moderate advisories; dependencies were not upgraded. |
| Browser acceptance | `npm run e2e` with actual local Java/MySQL service | **PASS** — 5 Playwright journeys passed, including connected persistence, ambiguous POST response handling, browser-history filters, responsive light/dark screenshots, and motion controls. |
| Visual screenshot review | Inspected all six generated O8–O10 contact sheets: desktop/mobile light and dark, detail/error states, and tablet allocations | **PASS with ordinary density** — no obvious clipping or overlap was visible; compact mobile navigation and responsive forms/lists rendered. This is visual evidence from captured local pages, not hosted or physical-device acceptance. |
| Postman/Newman | O1, O3, D3, O4/O5/A09, and O14 dependency-integrity collections | **PASS** — 96 requests and 142 assertions, zero failures. O14 suite: 23 requests/37 assertions. Re-run on the final O14 JAR remains required. |
| MySQL schema | Read-only inspection of protected `orgresource_test` | **PASS** — exactly five tables, all InnoDB; rows observed: department 32, project 26, resource 50, resource_requirement 36, allocation 33. FK counts: 1 allocation, 1 project, 2 resource_requirement. No phone/contact fields or row values were displayed. |
| Local UI screenshots | `evidence/O8-O10/screenshots/*.jpg` | **PASS** — actual Playwright captures were reviewed. The screenshots show synthetic fictional records only. |

The O14 dependency suite confirmed guarded deletes return conflict and preserve referenced records before deleting only the eligible fixtures. These tests used the isolated fictional test schema; no existing non-test database was reset or dropped.

## 6. Final package and independent extraction gates

**PENDING at this report revision.** The following results must be appended after running the final scripts on the committed O14 source:

- Exact committed HEAD and remote branch SHA.
- Final `scripts/package-release.ps1` result, Java 8 toolchain, test count, generated artifact names, sizes, SHA-256, and external `RELEASE-MANIFEST.json`.
- Clean extraction checks for both archives, including the Java source ZIP's Maven Wrapper and Eclipse metadata; source rebuild result; independent-machine/Eclipse status.
- Actual bundled-JAR root, JS/CSS asset, HashRouter refresh, JSON API, Postman, fictional create/GET persistence across restart, and safe cleanup smoke results.
- Final O14 pull request URL/base and CI status.

The portable setup guide documents a separate empty `orgresource` schema and a least-privileged local account. A separate computer and newly provisioned schema were not claimed as verified at this revision. Never point the portable first-run instructions at `orgresource_test` or any shared/personal database.

## 7. Privacy, safety, and non-production boundary

- The O14 test data is fictional. No private database password or personal contact was printed or committed.
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

**Not yet issued.** Until the pending exact-commit release gates pass, this report does not designate the baseline ready. Even after technical gates pass, the appropriate designation is **ACADEMIC BASELINE CONDITIONALLY READY** while CLO9, Twilio acceptance, Canvas directions, and student-only obligations remain unresolved. This is not “FINAL PRODUCT ACCEPTED.”
