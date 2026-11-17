# OrgResource C1 Phase Report

**Date:** 2026-11-17  
**Result:** C1-A, C1-B, and C1-C implementation gates PASS locally. Draft PR #14 is open; C1-specific remote Java 21/MySQL CI is not configured and is not claimed.

## Source lineage and Git

| Item | Verified value |
|---|---|
| Protected Course Final A source | `7f8c0746da18fd54b47f430ac1435ac36e605d67` |
| C0 implementation parent | `feat/extended-c-c0-compat-2026-11-17` at `e492aeb24623476b05ad6d1ba9f4c9792e1bd105` |
| Authoritative C1 instructions | `docs/course-a-final-spec-build-handoff-2026-11-16` at `a5f8656d79b1c0843ba84bf051419102cca28c38`, file `docs/academic-final/11_CODEX_C1_SECURE_IDENTITY_TENANCY_FOUNDATION_FREE_LUNA.md` |
| C1 branch | `feat/extended-c-c1-identity-tenancy-2026-11-17` |
| Tested implementation commit | `61cd551a230420d3c4516d42daa92cae3058170d` |
| Draft PR | [#14: C1 secure identity, organizations, and governance](https://github.com/newonathe/OrgResource/pull/14), base is the C0 branch |
| C0 PR | [#13](https://github.com/newonathe/OrgResource/pull/13), remains open and unchanged |

The C1 branch was created from the exact C0 parent. The C0 experiment is promoted into one maintained `extended/backend/` module on this branch; the immutable C0 commit and PR remain intact. A comparison against the protected Course A SHA found no changes in Course A backend, frontend, Postman, acceptance evidence, or release paths.

## Implemented checkpoints

### C1-A — relational foundation and offline bootstrap: PASS

- Reused the proven Java/Jersey/MySQL compatibility backend and promoted it to `extended/backend/`.
- Added the reviewed Extended C Flyway baseline and the official Spring Session JDBC MySQL migration. The schema has 8 identity/governance/audit tables, the 5 existing domain tables, and Spring Session's 2 library-managed tables, all InnoDB. Flyway also owns its metadata table.
- Added organization ownership to the five domain families and composite same-tenant foreign keys for project/department, requirement/project/resource, and allocation/requirement relationships.
- Added a one-time `--bootstrap` operator path. It creates the fictional organization, account, credential, active OWNER membership, audit entry, and hashed recovery codes atomically; later bootstrap attempts are refused. There is no public organization-creation or first-registrant owner route.
- Database URLs, application credentials, and bootstrap password are process settings. The verifier creates random disposable credentials and does not print them.

### C1-B — authentication, sessions, recovery, tenant isolation: PASS

- Added database-backed accounts and BCrypt credentials; generic authentication/recovery outcomes; bounded login throttling; pre-authentication CSRF; same-origin and CSRF checks on cookie-authenticated mutations; session/CSRF rotation; logout; password change; and account recovery using hashed, expiring, one-use recovery codes.
- Added JDBC-persisted sessions with 30-minute idle and 12-hour absolute limits, credential-version checks, and revocation across password recovery/change. Production cookie configuration is HttpOnly, host-only, Secure, and SameSite=Lax; the loopback test profile alone relaxes Secure.
- Added server-derived active organization and role context. Caller-provided organization headers/claims do not grant tenant access; membership, account, and organization state are rechecked against MySQL.
- Hikari initializes every MySQL connection with session timezone `+00:00`; Hibernate JDBC timezone is UTC. The application normalizes the JVM default to UTC before persistence setup and keeps its business clock explicitly Asia/Manila. Acceptance verifies `DATETIME(6)` round-trips with microsecond precision in 2040 and the packaged application preserves a 2040 project through its Manila-argument to UTC-argument restart.

### C1-C — invitations, governance, ownership and audit: PASS

- Added single-use 256-bit invitation bearer tokens stored by SHA-256 hash, a maximum 24-hour lifetime, invite/redeem/revoke flows, and both new-account and existing-account redemption.
- Added the five roles `OWNER`, `ADMIN`, `CUSTODIAN`, `LEAD`, and `MEMBER`; OWNER/ADMIN grant limits; strong version preconditions; membership suspension; ownership transfer; and audited actor/target/outcome records without credential or token material.
- Organization-first row locking and ordered membership/transfer locks protect invitation redemption, role changes, transfers, suspension, disable, and last-owner invariants. Tests use concurrent requests and inspect persisted outcomes.
- The verifier binds the service to `127.0.0.1`; no public deployment, external identity provider, email, or real SMS is enabled.

## MySQL and HTTP acceptance results

Command: `scripts/verify-extended-c1.ps1`  
Result: **PASS** on a fresh loopback-only MySQL 8.0.46 data directory and disposable `orgresource_c1_*` schema. Flyway applied and validated V1 and V2; Hibernate started against MySQL with `ddl-auto=validate`. The run also built and exercised the packaged Spring Boot JAR, React assets, authentication/governance flows, restart persistence, session survival/revocation, and recovery-code replay denial. The script stopped the Java process and isolated MySQL on completion; the temporary datadir remains under the local Temp directory.

All acceptance methods in `extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java` passed:

| Test | Result | Verified behavior |
|---|---|---|
| ID01 | PASS | Concurrent offline bootstrap creates exactly one fictional OWNER; no public org creation or repeat bootstrap |
| ID02 | PASS | Literal Javax and Jakarta validation, Jersey/Spring injection, JPA, HTTP, and persisted MySQL record |
| ID03 | PASS | 15 application/session tables, InnoDB, Flyway, unique/check/FK constraints, generated pending-transfer uniqueness, cross-tenant FK rejection |
| ID04 | PASS | Anonymous and wrong-role denial, foreign-tenant isolation, frozen-organization write denial |
| ID05 | PASS | Pre-auth CSRF, login rotation, wrong-origin/token denial, logout, production/test cookie settings |
| ID06 | PASS | Multi-organization membership, forged org header ignored, immediate role/suspension enforcement |
| ID07 | PASS | BCrypt storage, generic login failures, bounded throttling |
| ID08 | PASS | Hashed one-use recovery, replay denial, password/session version changes and concurrent recovery winner |
| ID09 | PASS | Hashed 256-bit invitation, expiry, new/existing account redemption, replay/revoke denial and concurrent single winner |
| ID10 | PASS | OWNER/ADMIN grant limits, no self-promotion or OWNER invitation, ETag 428/412 preconditions |
| ID11 | PASS | One pending ownership transfer, wrong-recipient/replay/revoke/expiry denial, atomic owner swap |
| ID12 | PASS | Concurrent owner transfer/account disable cannot leave the organization ownerless; other-org membership preserved |
| ID13 | PASS | JDBC session persistence and idle/absolute expiry |
| ID14 | PASS | Anonymous denial for the five representative legacy domain list families and selected v2 routes; no C0 fallback users |
| ID15 | PASS | Clean Flyway initialization, bundled UI and expected schema metadata |
| ID16 | PASS | SMS disabled; account response/audit do not expose passwords, hashes, or recovery codes |
| ID17 | PASS | UTC MySQL session, PHT business clock, and exact JPA/MySQL `DATETIME(6)` round-trip including year 2040 |

Maven result: **17 tests, 0 failures, 0 errors, 0 skipped**. The verifier also performed an actual packaged-JAR restart test with the first Java process launched using `Asia/Manila` and the next using `UTC`; the persisted 2040 project values, department, session, and revoked-session state remained correct.

## Frontend and effective runtime versions

The full verifier ran `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, and `npm run build`: all PASS; Vitest reported 3 files / 15 tests passed and npm reported 0 vulnerabilities. The production frontend assets were bundled into the backend JAR and returned successfully over localhost HTTP.

Effective versions from the generated Maven dependency tree and executed environment:

- Java 21.0.3; Spring Boot 4.1.2; Jersey 4.0.3; Spring Security 7.1.2; Spring Session JDBC 4.1.1.
- Hibernate ORM 7.4.13.Final; Hibernate Validator 9.1.5.Final; Jakarta Validation 3.1.1; literal Javax Validation 2.0.1.Final with Apache BVal 2.0.6.
- MySQL Server 8.0.46; Connector/J 9.7.0; Flyway MySQL 12.4.0; Retrofit 2.11.0; Gson 2.11.0.
- Node 24.14.1; npm 11.11.0.

The Java 21/MySQL portion ran locally against real MySQL. The current GitHub `Verify` workflow exercises the Course A Java 8 and React gates, not this C1 Java 21/MySQL suite. At report preparation, the draft PR's standard checks were still running; their completion does not substitute for C1 remote acceptance.

## Scope boundary and remaining limitations

- C1 protects representative legacy domain routes and implements identity/governance; it does **not** re-run or claim all 28 Course A operations as Extended C acceptance. Full Course A parity is explicitly not a C1 gate. The Course A source and release files remain untouched.
- No real Ateneo affiliation verification, MFA, email delivery, SMS delivery, public hosting, or external provider integration was added. Test SMS is disabled. The verifier and service bind to loopback only.
- Login throttling is process-local and resets on restart. This local single-instance foundation is not approved for public deployment.
- No C2 functionality has started. C1-specific GitHub Actions for Java 21/MySQL are not configured; local evidence is the only C1 acceptance run.
- No automatic merge, release tag, public deployment, real message, or Canvas submission occurred.

## Evidence and next phase

- Acceptance source: `extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java`.
- Reproducible local gate: `scripts/verify-extended-c1.ps1`.
- Flyway migrations: `extended/backend/src/main/resources/db/migration/`.
- C1 implementation: `extended/backend/src/main/java/edu/orgresource/extended/identity/`, `rest/`, and `security/`.
- Tested code commit: `61cd551a230420d3c4516d42daa92cae3058170d`; PR #14 is the draft review target.
- **Next phase after owner review:** C2 only. C1 stops here as authorized.
