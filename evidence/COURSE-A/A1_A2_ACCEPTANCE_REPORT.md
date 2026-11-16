# Course Final A1 + A2 Acceptance Report

**Date:** 2026-11-17 (Asia/Manila)  
**Implementation parent:** `feat/o14-academic-final-acceptance-2026-11-15` at `2dd336ed39cecd417a8bce3388b303e688e95dee`  
**Implementation commit:** `f355210637ab73b3553b8ee73c6d458926da51ad`  
**Feature branch:** `feat/course-final-a1-a2-2026-11-17`  
**Pull request:** [#11 — Course A1+A2: add image API and Twilio protocol acceptance](https://github.com/newonathe/OrgResource/pull/11), draft, open, targeting the O14 implementation branch  
**Remote verification:** `origin/feat/course-final-a1-a2-2026-11-17` resolved to `f355210637ab73b3553b8ee73c6d458926da51ad` at the time of this report.  
**Scope boundary:** A1+A2 only. A3 immutable freeze has not begun.

## Result

The local A1+A2 technical acceptance gates executed in this workspace passed. The implementation adds two Resource image HTTP operations and a separately configured Twilio Messages protocol transport while retaining the existing five JPA entities and 25 proposal operations plus A09. The full bundled Java/React release was exercised against the isolated local MySQL test schema. No live SMS, public deployment, Canvas submission, instructor approval, or recorded presentation is claimed.

Course/instructor interpretation of CLO9 image scope and whether a local protocol-compatible Twilio exchange satisfies the assignment's provider wording remain **NOT_VERIFIED**. The current Canvas rubric was not checked. The course sources contain a documented CLO mapping inconsistency; implementation evidence does not settle that academic interpretation.

## Changes accepted in A1+A2

- `POST /api/resources/{id}/image` receives a bounded PNG/JPEG multipart upload; `GET /api/resources/{id}/image` serves the stored bytes. Image bytes and media type are nullable columns on the existing `resource` row. There is no sixth JPA entity or image table.
- Upload validation checks the declared media type against the decoded image/signature, rejects unsupported or malformed content, enforces a 2 MiB byte limit and a 16,777,216-pixel limit, and returns controlled errors. Retrieval returns the exact stored bytes and media type with no-store/nosniff headers.
- Hibernate 5.3 enhancement enables lazy image-property initialization. The actual MySQL test confirms ordinary and pessimistically locked Resource reads do not load the BLOB until its accessor is used; ordinary Resource JSON does not serialize image bytes.
- The new Retrofit/Gson Twilio Messages transport uses the provider-shaped HTTPS Messages endpoint, URL-encoded form data and HTTP Basic authentication. It is disabled by default, reads credentials from environment configuration, restricts non-TLS use to loopback test servers, bounds timeouts, disables redirects/retries, and distinguishes provider acceptance from delivery. A response-loss/timeout or ambiguous server outcome is not automatically retried.
- FakeTwilio remains available for local lifecycle testing. No Twilio account, real SMS, charge, or carrier-delivery claim was used.
- Current Postman v2.1 collection contains 28 operations: 25 proposal operations, A09 consent withdrawal, and the two image operations. Historical nine-entity/68-operation collections remain historical.
- Current README, course matrix, CLO9/Twilio decision records, package/start instructions, and acceptance scripts were reconciled to the Course A implementation. O14 historical evidence was preserved.

## Route and data model acceptance

| Contract | Observed result |
|---|---|
| JPA domain entities | Exactly `Department`, `Project`, `Resource`, `ResourceRequirement`, `Allocation` |
| MySQL base tables | `allocation`, `department`, `project`, `resource`, `resource_requirement` (exact `SHOW TABLES` set) |
| Original proposal operations | 25 retained |
| A09 consent withdrawal | One separately documented addition |
| Course A image operations | Two separately documented additions |
| Current operation total | 28 |
| Resource image storage | Nullable `MEDIUMBLOB` and content-type column on existing Resource table; no extra entity/table |

The complete route-to-controller/component/DTO/Postman/acceptance mapping is in the current route parity and compliance evidence. A route annotation alone was not treated as proof; HTTP/Postman and database behavior were executed for the acceptance paths listed below.

## Executed verification

All results below are from executions for this A1+A2 continuation; historical O14 totals were not reused as new results.

| Gate | Command / method | Observed result |
|---|---|---|
| Java + MySQL backend | `cd backend; .\mvnw.cmd -B -ntp -Pbundle-react-ui clean verify` using Temurin Java 8u512, Maven Wrapper 3.10.0, and the protected `orgresource_test` MySQL configuration | **PASS** — 61 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`. Included the actual MySQL image HTTP, allocation/reminder lifecycle, and persistence tests. |
| Focused image acceptance | `ResourceImageHttpMySqlTest` through Maven against local MySQL and HTTP | **PASS** — exact five-table schema; BLOB type/length/SHA checks; byte-for-byte PNG and JPEG retrieval; >64 KiB payload; exact 2 MiB accepted and 2 MiB+1 rejected; malformed, MIME mismatch, SVG, truncated files, missing part/resource cases; lazy-load and update-preservation checks. |
| Twilio protocol | `TwilioRestTransportTest` and `ReminderLifecycleMySqlTest` | **PASS** — loopback stub observed endpoint, form fields and Basic-auth protocol; accepted/rejected/malformed/lost/timeout outcomes and no retry were exercised; `PROVIDER_ACCEPTED` persisted without being labeled delivered. |
| Frontend checks | `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, `npm run build` in `frontend` | **PASS** — typecheck, tests (3 files / 15 tests), lint and production build passed. |
| Postman/Newman | `scripts/run-api-acceptance.ps1` against the packaged Java application | **PASS** — 100 requests and 151 assertions, zero failures across O1, O3, O4/O5, O14 dependency-integrity and Course A image HTTP suites. The image suite separately passed 4 requests / 9 assertions. |
| Browser E2E | Existing Playwright E2E suite run against the packaged JAR using CLI/headless Chromium | **PASS** — 6 tests, including connected UI workflows, uncertain-submit/no-duplicate behavior, browser history, all eight screens in light/dark desktop/mobile layouts, pointer preference, packaged root/assets/hash refresh and five JSON list APIs. Screenshots were written under a temporary directory and visually inspected; none were committed. |
| Release packaging | `scripts/package-release.ps1 -ArtifactsDirectory <external release folder>` | **PASS** — created executable JAR, complete tracked-source ZIP, Java/Eclipse source ZIP, SHA-256 file and release manifest. The final package was rebuilt from the final committed source; exact current sizes/hashes are in `RELEASE-MANIFEST.json` and `SHA256SUMS.txt` beside the release files. |
| Clean source extraction | Extracted both release ZIPs into fresh temporary directories; full source: `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, `npm run build`, then `backend\mvnw.cmd -B -ntp -Pbundle-react-ui clean verify`; Java/Eclipse archive: `mvnw.cmd -B -ntp -DskipTests package` | **PASS** — full-source frontend checks passed (3 files / 15 tests); extracted Java 8/MySQL build passed all 61 tests (0 failures/errors/skips); standalone Java/Eclipse archive compiled with Maven Wrapper; extracted bundled JAR contained the React entry point. The extracted-built JAR started with process environment configuration against the isolated local test schema; `/`, its JavaScript/CSS assets, and all five list APIs returned HTTP 200. Archive inspection found no `.env`, protected workstation helper, literal configured DB password, unsafe path or current build cache. The source archive preserves the already-tracked historical O1 skeleton handoff files. |
| Packaged runtime and restart | Started the actual release JAR with `scripts/start-release-demo.ps1`; used HTTP to create a unique fictional Department, stopped Java with a console Ctrl+C event, restarted the same JAR, queried the same ID/name, then deleted only that exact test record | **PASS** — record persisted after restart and was then absent after cleanup. Java exited; Windows `MySQL80` remained **Running**. |
| GitHub CI | GitHub Actions `Verify`, run `43940334562`, for implementation commit `f355210...` | **PASS** — completed with `success`. |
| Git hygiene | `git diff --check`; branch/remote SHA check | **PASS** — implementation commit was pushed, then this report was committed and pushed separately on the same feature branch. The release manifest records the exact packaged HEAD. No force-push or merge was used. |

The package also passed local checks that the executable JAR contains the bundled React entry point and assets and that the current v2.1 Postman collection parses as a standard collection with 28 requests. The packaged-JAR Playwright and Newman suites verified the actual served assets and APIs, rather than treating archive inspection as runtime proof.

## Issues encountered and disposition

- The first image Newman run used an unsupported Postman URL shape and a response-size property not present in the installed runtime. The collection was corrected to standard URL strings and a `Content-Length` check; the final image suite and full Newman runner then passed.
- An earlier MySQL lifecycle test had a fixed-date fixture race with the scheduled cleanup task in a reused Spring context. The test now dirties that context after the class, and the full 61-test suite passed afterward.
- A code review found that `@Basic(fetch = LAZY)` alone did not prove that image bytes stayed unloaded. Hibernate bytecode enhancement was added, and the MySQL test now checks actual unloaded/loaded state and exact stored bytes. The final suite passed.
- `npm ci` reports 7 moderate audit findings and zero high/critical findings in development/build tooling. Dependencies were not upgraded outside the frozen A1+A2 scope. Review them in a separate dependency-maintenance change.
- Maven emitted nonfatal dependency metadata PKIX/HTTP 401 warnings while resolving repositories; dependency resolution, compilation, tests and packaging completed successfully.
- No test failure remains unresolved in the executed local gates. The initial failures above are recorded rather than represented as if the first attempt passed.

## Portability and evidence boundaries

- The checked release was built with Temurin Java 8u512, Maven Wrapper 3.10.0, Node 24.14.1 and npm 11.11.0 on this Windows workstation.
- The application JAR served React and JSON APIs locally, and a real fictional test record survived a Java restart. The local restart test used the protected workstation setup for the isolated `orgresource_test` database.
- Both source archives were extracted and rebuilt locally as recorded above. A separate physical machine, a clean independent MySQL installation/schema/account, and a fresh-room submission rehearsal were **NOT_RUN**. Do not describe workstation acceptance as independent-machine acceptance.
- Database credentials remain in the protected local environment script; no password is included in source, command output, release artifacts, or this report. No real personal contact data was used.
- No raw course video was reprocessed. Existing transcript/source-pack evidence was used; no video re-upload is needed for this handoff.
- Canvas rubric/due-date verification, instructor acceptance, group membership confirmation, peer review, student Certificate of Authorship, actual demonstration/recording/link access, and Canvas upload are **MANUAL STUDENT ACTION / NOT_VERIFIED**. No signature, recording, receipt, rating, or approval is claimed.
- CLO9 is **PASS for implemented local image receive/store/serve behavior** and **NOT_VERIFIED for whether the course requires it in OrgResource or considers it accepted**. See `docs/compliance/CLO9_IMAGE_DECISION.md`.
- Twilio is **PASS for locally tested Retrofit/Gson protocol behavior** and **NOT_VERIFIED for provider account, live service, real delivery, or instructor acceptance**. No real SMS was sent. See `docs/compliance/TWILIO_COURSE_DECISION.md`.

## Release inventory

The final post-report package is located at `../OrgResource-Course-A-Release/` relative to the repository directory. It contains:

- `OrgResource.jar` — executable backend with bundled React UI.
- `OrgResource-complete-source.zip` — tracked source at the feature branch's final packaged commit.
- `OrgResource-java-source.zip` — Java source, tests, Maven Wrapper, and Eclipse metadata.
- `SHA256SUMS.txt` and `RELEASE-MANIFEST.json` — artifact sizes/hashes and exact source commit/toolchain.

The full-source archive's own digest is recorded in the adjacent manifest/checksum file to avoid a self-referential hash inside the archive. Generated release artifacts are not committed to Git.

## Final status

**Technical A1+A2 status: PASS for the executed local acceptance gates.**  
**Academic/instructor sign-off: PENDING / NOT_VERIFIED.**  
**Git:** feature branch pushed; PR #11 is draft/open, based on the verified O14 implementation branch. The implementation SHA is recorded above; the final packaged feature-branch SHA is recorded in the adjacent release manifest and verified against the remote branch after the report push.
**A3:** not started; stop here for the user's review.
