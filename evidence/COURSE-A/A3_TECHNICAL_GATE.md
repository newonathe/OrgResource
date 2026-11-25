# Course Final A3 technical gate

**Recorded:** 2026-11-17 (Asia/Manila)
**Implementation parent:** `af2a405ebe893268c54f463ec37da6a37cf6b917` (`feat/course-final-a1-a2-2026-11-17`)
**Purpose:** record actual local acceptance observed during A3 preparation. The exact frozen source SHA, remote CI, artifact hashes, and packaged-runtime checks are recorded in the external `A3_RELEASE_RECEIPT.md` generated after commit and packaging. This document alone does not declare the release frozen.

## Local toolchain and database

- Temurin JDK `1.8.0_512`; Maven Wrapper `3.10.0`; Node.js 20.19+ and npm were used. Credentials came from the protected local `%LOCALAPPDATA%\OrgResourceDev\Set-OrgResourceTestEnvironment.ps1`; no password was displayed or copied into the repository.
- MySQL 8 service was available. The integration tests use only the guarded fictional `orgresource_test` schema. No schema reset/drop/truncate was run.
- The demonstration SQL uses the selected schema and is read-only. The student presentation is directed to a separate, newly created empty local `orgresource` schema.

## Executed gates

| Gate | Command and scope | Observed result |
|---|---|---|
| Java 8 + MySQL | From `backend`: `.\mvnw.cmd -B -ntp -Pbundle-react-ui clean verify` | **PASS** — 61 tests; 0 failures, 0 errors, 0 skipped; Maven reported `BUILD SUCCESS`. Spring Data/JPQL initialized against MySQL. Image HTTP, reminder/lifecycle, and persistence tests ran. Maven logged a PKIX warning for the retired jvnet metadata host and an HTTP 401 from the Spring repository metadata endpoint; dependency resolution completed and the build passed. |
| Frontend | From `frontend`: `npm ci`; `npm run typecheck`; `npm test`; `npm run lint`; `npm run build` | **PASS** — typecheck, lint, and production build succeeded; 15 tests across 3 files passed. `npm audit` reported 0 vulnerabilities after the compatible development-tool update. `npm ci` emitted a deprecation notice for `whatwg-encoding`; no install failure. |
| Newman acceptance | `scripts/run-api-acceptance.ps1` | **PASS** — six ordered acceptance collections, 100 requests, 151 assertions, 0 failures. Collections cover O1, O3 CRUD, Department update, O4/O5, O14 dependency protection, and image HTTP. The route reference itself was not run as one sequential 28-request collection. |
| Postman v2.1 reference | Newman Department-folder smoke using `postman/OrgResource-Course-A-28-Routes.postman_collection.json` and `postman/OrgResource-Course-A-Local.postman_environment.json` | **PASS** — imported/executed 4 requests, 8 assertions, 0 failures. All 28 reference operations have standard string URLs and are enumerated in `evidence/COURSE-A/ROUTE_ACCEPTANCE_MATRIX.md`. This focused run does not claim all 28 requests can be run sequentially; several are mutually exclusive or destructive. |
| Browser acceptance | From `frontend`: `npm run e2e`, using CLI Playwright against the local Java application | **PASS** — 6 Playwright tests passed: ambiguous-create response handling, client navigation, connected CRUD, responsive light/dark viewports, pointer/reduced-motion behavior, and packaged-JAR assets/hash navigation/five APIs. No in-app browser was used. |
| Visual review | CLI-generated Playwright screenshots from the run above | **PASS, scoped** — inspected all eight screen views in desktop-light and mobile-light captures plus the error view. No clipping/overlap or hierarchy/contrast defect was observed in those captures. This was not a physical-device test or a complete manual inspection of every theme/state combination. |
| MySQL demonstration query | `scripts/course-a-demo.sql`, with the client explicitly selecting `orgresource_test` | **PASS** — read-only output showed exactly the five expected InnoDB tables (`allocation`, `department`, `project`, `resource`, `resource_requirement`) and four foreign-key relationships. Observed counts at execution: Department 56, Project 52, Resource 81, Requirement 67, Allocation 64. All listed content was fictional; no borrower name, phone number, or provider credential was selected. Counts reflect accumulated acceptance fixtures and are not a clean demo dataset. |

The image HTTP acceptance exercised PNG multipart upload and raw-byte retrieval, metadata, size/hash, cache and `nosniff` headers, and safe deletion of its own test fixture. The MySQL image test verifies stored/retrieved byte equality. Exact per-operation mapping and evidence boundaries are in the [route acceptance matrix](ROUTE_ACCEPTANCE_MATRIX.md).

## Scope and acceptance limits

- Current academic scope is five JPA entities and 28 operations: 25 original proposal operations, A09 as a separately recorded amendment, and two image operations. The image content remains on `Resource`; no sixth entity or image table was added.
- The original proposal remains preserved. The [as-built proposal](../../docs/academic-final/COURSE_FINAL_A_AS_BUILT.md) is a technical companion and records the implemented contract, source limits, and student-only obligations.
- The local image endpoint demonstrably receives and serves validated image bytes. Whether CLO9 must be satisfied inside OrgResource and whether an instructor accepts this scope remain **NOT_VERIFIED**. The syllabus mapping conflict is documented in `docs/compliance/CLO9_IMAGE_DECISION.md`.
- FakeTwilio remains a simulator. Retrofit/Gson protocol tests prove only a fictional loopback HTTP exchange. No live Twilio request, charge, SMS, carrier delivery, or handset receipt occurred.
- Separate-machine setup, fresh-account setup, independent Eclipse import, Canvas instructions/deadline, group roster, instructor approval, presentation/recording, peer review, authorship certificate, and Canvas submission were **NOT_RUN**. The student must verify and perform applicable academic actions personally.
- The populated acceptance database, local Playwright, and local build are not evidence of institutional deployment, production suitability, or a clean demonstration dataset.

## A3 freeze items tracked outside this source gate

After the source commit, the external release receipt records the source and remote SHA, PR/CI, artifact names/sizes/SHA-256, archive inspection and extraction, exact packaged-JAR HTTP/database checks, DOCX rendering review, and unresolved manual actions. Those checks must pass before calling A3 technically accepted or the source frozen. No merge, final tag/release, public deployment, real SMS, or Canvas submission is authorized by this technical gate.
