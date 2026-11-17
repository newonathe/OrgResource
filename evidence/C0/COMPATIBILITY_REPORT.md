# C0 Supported Runtime Compatibility Report

Date: 2026-11-17 (Asia/Manila)

Status: **C0 compatibility experiment passed; Course A unchanged**

## Git and source boundary

- C0 branch: `feat/extended-c-c0-compat-2026-11-17`
- Exact protected A3 parent: `7f8c0746da18fd54b47f430ac1435ac36e605d67` (`feat/course-final-a3-freeze-2026-11-17`)
- Implementation commit tested from a clean `git archive`: `86b3983171cd8aa439ded5aeed5f74baec546aba`
- A3 branch and source SHA were rechecked after fetch and remain unchanged. PR #12 is still open; C0 is kept as a separate draft PR targeting the A3 branch.
- Implementation is isolated under `experiments/extended-c-runtime-compat/`; `scripts/verify-c0.ps1` creates its own disposable loopback MySQL instance/schema. No Course A code, dependency files, schema, or release artifact was changed.

## A3 provenance gate

The accessible A3 receipt, manifest, source archives, executable JAR, and as-built DOCX were checked before C0 implementation. All 10 artifact byte counts and SHA-256 values matched both manifest files. The complete-source ZIP (571 entries) and Java/Eclipse source ZIP (74 entries) passed CRC verification. The DOCX passed package CRC and XML parsing, and contains the declared A3 source SHA. The complete-source archive contains no `.git`, `node_modules`, build output, protected workstation credential script, or local database files.

Full artifact hashes and checks are recorded in [`A3_PROVENANCE_CHECK.md`](A3_PROVENANCE_CHECK.md). This was an independent integrity check, not a rerun of A3 builds or a separate-machine acceptance claim.

## Compatibility stack observed

| Component | Effective version / runtime |
|---|---|
| Java | OpenJDK 21.0.3 |
| Spring Boot | 4.1.2 |
| Spring Framework | 7.0.10 |
| Jersey | 4.0.3 |
| Spring Security | 7.1.2 |
| Hibernate ORM | 7.4.13.Final |
| Jakarta Validation API / provider | 3.1.1 / Hibernate Validator 9.1.5.Final |
| Javax Validation API / provider | `javax.validation:validation-api` 2.0.1.Final / Apache BVal 2.0.6 |
| MySQL Connector/J / actual server | 9.7.0 / MySQL 8.0.46 |
| Flyway | 12.4.0 |
| Retrofit / Gson / OkHttp | 2.11.0 / 2.11.0 / 4.12.0 |

The observed runtime provider line was:

`javax=org.apache.bval.jsr.ApacheValidatorFactory/org.apache.bval.jsr.ValidatorImpl`

`jakarta=org.springframework.validation.beanvalidation.LocalValidatorFactoryBean/org.hibernate.validator.internal.engine.ValidatorImpl`

The test unwraps Spring's `LocalValidatorFactoryBean` to verify the actual Jakarta provider. Both validation namespaces were loaded in the same JVM; a Jakarta-only substitution was not used for the literal Javax checks.

## UC01–UC12 results

| Case | Result | Executed evidence |
|---|---|---|
| UC01 — HTTP, JSON, React static assets | **PASS** | Actual Jersey HTTP JSON responses; packaged React JavaScript and CSS return HTTP 200; API authentication denial is JSON. |
| UC02 — Jersey to Spring component to JPA | **PASS** | Autowired component path persisted a Department; a separate JDBC connection read the committed row; normalized uniqueness conflict returned controlled HTTP 409. |
| UC03 — literal Javax validation | **PASS** | Javax DTO checked by Apache BVal; valid/invalid values, controlled HTTP 400, and an independent Jakarta validation probe executed. |
| UC04 — Javax/JPA/database boundary | **PASS** | Manual Javax validation rejected before save; bypassing it reached the real MySQL CHECK constraint; rollback left no row. The database failure was not misreported as a Javax JPA constraint exception. |
| UC05 — Spring Security and CSRF | **PASS** | Anonymous 401, viewer write 403, missing CSRF 403, editor CSRF-protected POST 201, JSON denials, and session ID rotation verified over HTTP. |
| UC06 — MySQL persistence/integrity | **PASS** | Flyway created exactly five business InnoDB tables plus its metadata table; four foreign keys, an actual FK rejection, rollback, repositories, JPQL initialization, and aggregate queries were exercised. |
| UC07 — Retrofit/Gson and scheduling | **PASS** | Retrofit/Gson made loopback FakeTwilio and Twilio-protocol requests; form encoding and Basic authorization were checked; simulated 503 made one request with no retry; external URLs were rejected; scheduler tick was observed with SMS disabled. |
| UC08 — five original route families | **PASS** | HTTP creates exercised Department, Project, Resource, Requirement, and Allocation route families; a fictional contact persisted in MySQL but was excluded from the response DTO. |
| UC09 — transactional inventory | **PASS** | Holds, expiry boundary, pending cleanup, issue without double deduction, return/consume effects, rollback, and competing approvals were exercised. Two quantity-4 approvals against stock 5 resulted in one approval and one conflict; available quantity was 1. |
| UC10 — route compatibility and organization scope | **PASS** | Legacy and versioned Department routes saw the same row; forged organization header could not expose the other fixture organization. |
| UC11 — combined runtime | **PASS** | Same-JVM Javax/Jakarta provider identities printed and asserted; no `javax.persistence` API appeared; scheduled method was observed. |
| UC12 — clean extraction/package/secret check | **PASS WITH TOOL LIMITATION** | `git archive` at `86b3983` was extracted without `.git`; clean extraction ran this verifier end to end, rebuilt the frontend and JAR, passed UC01–UC11, then tested packaged UI/API, authenticated create, application stop/restart, and persistence. Targeted source credential-pattern scan found no embedded passwords, keys, or tokens. `gitleaks` was not installed, so no gitleaks result is claimed. |

The JUnit suite executed **11 tests, 0 failures, 0 errors, 0 skipped**. The fixture cleanup also asserted that all five business tables contained zero C0 test rows after each test.

## Commands and observed results

Executed from the clean extraction of commit `86b3983171cd8aa439ded5aeed5f74baec546aba`:

```powershell
.\scripts\verify-c0.ps1
```

Observed: frontend `npm ci` (0 vulnerabilities), typecheck, 15 Vitest tests across 3 files, lint, and production build passed; Maven `clean verify` passed; the 11 MySQL-backed Java tests passed; the packaged application served React assets and JSON, accepted an authenticated CSRF-protected Department create, and returned that persisted Department after application restart. Overall script output: `C0 PASS`.

Generated clean-extraction JAR: 71,706,941 bytes; SHA-256 `2a3bb23da9722ae9197e10a7e611fd18272d007833cd12cfa0eeb0d25533f29`. This is an experiment artifact in the temporary clean extraction, not a Course A release artifact and not committed.

GitHub Actions run [`44049599015`](https://github.com/newonathe/OrgResource/actions/runs/44049599015) completed successfully for implementation SHA `86b3983`. Its existing React typecheck/test/lint/build job and Course A Java 8/isolated-MySQL test and JAR smoke job passed. This workflow does not run the isolated C0 Java 21 experiment; C0-specific proof is the local clean-extraction run above.

## Failures, corrections, and limitations

- No UC01–UC12 acceptance failure remains in the final run, and no material incompatibility remained in the tested isolated stack.
- During bring-up, Boot did not run Flyway until the Boot Flyway integration starter was added; Jersey 4 instance registration did not expose resources until changed to class registration; lazy entity relationships required explicit loading/projections; and a Spring Security matcher initially allowed viewer writes. Each issue was corrected and covered by the passing MySQL/HTTP suite.
- Maven printed non-fatal HTTP 401 warnings while scanning metadata from two private/custom feeds; dependencies resolved and verification succeeded. Maven Central resolution and final build passed.
- MySQL's temporary root account was initialized without a password only inside the randomly named loopback-only test datadir. The application used a separate random-password test user; generated credentials were process-local, not printed or committed. SMS was disabled and no external provider was contacted.
- `gitleaks` was unavailable. A targeted scan of the C0 source and harness found no embedded credentials; this is not represented as a gitleaks scan.
- The experiment does not prove separate-machine compatibility, production capacity, real Twilio delivery, or instructor acceptance. It is not an approved Course A migration or a decision to begin C1.

## Git and PR

- Implementation source commit tested from a clean archive: `86b3983171cd8aa439ded5aeed5f74baec546aba`.
- The report and A3 provenance evidence were committed afterward on the same feature branch; the final branch tip is available in the PR metadata and final handoff.
- Draft PR #13: [C0 isolated supported-runtime compatibility proof](https://github.com/newonathe/OrgResource/pull/13), targeting `feat/course-final-a3-freeze-2026-11-17`.
- No merge, tag, public deployment, live SMS, Canvas submission, or C1 implementation was performed.

## Verdict

**C0 PASS — isolated supported-runtime compatibility evidence is reproducible from a clean extraction.** The Course Final A3 source remains frozen and unchanged. Review this report and [`C0_COMPATIBILITY_DECISION_INPUT.md`](../../docs/engineering/C0_COMPATIBILITY_DECISION_INPUT.md) before separately authorizing any later phase.
