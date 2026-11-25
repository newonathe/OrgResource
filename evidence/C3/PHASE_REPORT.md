# OrgResource Extended C3 Phase Report

- **Report date:** 2026-11-18 (Asia/Manila)
- **C3 implementation branch:** `feat/extended-c-c3-course-parity-2026-11-18`
- **C3 code/test SHA:** `b8b2d3a70c113c7a12b20ed7d0f2fa930d3586df`
- **Draft PR:** [#16](https://github.com/newonathe/OrgResource/pull/16), based on C2
- **C3 verification run:** [GitHub Actions 44180307800](https://github.com/newonathe/OrgResource/actions/runs/44180307800) — all three jobs passed
- **Protected Course Final A:** `7f8c0746da18fd54b47f430ac1435ac36e605d67` (unchanged; ancestor of C3)

## Result

C3 implementation and local/remote acceptance gates passed for the 26 original/amended Jersey operations and two Resource image operations. Course A source, its Java 8 implementation and the original five academic entities were preserved. No C4 work, merge, public deployment, Canvas submission, or real SMS was performed.

The npm audit still reports 12 high-severity affected package records in the development/build dependency tree. The production dependency audit is clean. Because the configured registry did not provide a compatible Picomatch patch and npm's proposed fixes require major downgrades, no forced dependency change was made. This remains a public-production release blocker; it does not invalidate the isolated local/CI C3 acceptance.

## Source and Git audit

- C2 parent: `feat/extended-c-c2-ledger-atomicity-2026-11-17` at `c8b6e76c249f99f3642c1513ddd7d55b584e027a`.
- C2 PR #15 was open with a clean merge state and targeted the C1 branch. C3 correctly branches from C2, not `main` or the research/docs branch.
- C3 code commit `b8b2d3a70c113c7a12b20ed7d0f2fa930d3586df` was verified equal to the remote C3 branch SHA when the successful CI run completed.
- Draft PR #16 targets `feat/extended-c-c2-ledger-atomicity-2026-11-17`; it remains unmerged.
- C3 requirements were read from documentation branch `docs/course-a-final-spec-build-handoff-2026-11-16`, SHA `4a137b8acb4d1dda486bf03a4151835613240fd5`, including `13_C2_SOURCE_AUDIT_AND_C3_RELEASE_GATES_2026-11-18.md` and `14_CODEX_C3_SECURED_COURSE_PARITY_AND_HARDENING_FREE_LUNA.md`.
- The changed source paths are confined to `extended/backend`, the C3 verifier/Postman/CI files, and this C3 evidence. No tracked Course A `backend/`, `frontend/`, release artifact, or historic course-source file was changed. The V1–V3 migration files were not edited.

## C2 audit findings F01–F12

| Finding | Result | Evidence / remaining boundary |
|---|---|---|
| F01 — operation receipt after 24-hour HTTP receipt expiry | **PASS** | Permanent movement remains authoritative after bounded HTTP receipt pruning; same operation/body returns a factual movement receipt, changed bodies remain conflicts. `C3_RECEIPT01` and packaged restart checks. |
| F02 — malformed/missing request validation | **PASS** | Empty body, malformed JSON/UUID, missing idempotency key, absent precondition, and malformed ETag return controlled client errors without stock mutation. `C3_INPUT01–03`; Jersey JSON/parameter exception mappers. |
| F03 — legacy query/filter parity | **PASS** | Tenant-scoped unfiltered and filtered lists for departments, projects, resources, requirements and allocations; A02 accepts status and requirement filters independently or together. `C3_PARITY01`, `C3_SECURITY02`, and Newman. |
| F04 — private contact at rest | **PASS, with key-operations boundary** | AES-256-GCM contact ciphertext uses per-value nonce and authenticated tenant/allocation context; responses omit contacts; A09 erases encrypted contact data. V4 refuses a legacy plaintext-contact schema. Key is external configuration (`EXT_CONTACT_ENCRYPTION_KEY`); local/CI tests generate a transient key at runtime. No fixed contact key remains in source. |
| F05 — expiry/reminder lifecycle | **PASS for local simulator/stubs** | Bounded scheduled expiry handles PENDING/APPROVED; issued reusable custody is not auto-returned. Reminder dispatch uses durable claims and actual Retrofit/Gson loopback HTTP. Real provider network traffic and delivery are not claimed. |
| F06 — 26 academic operations plus two images | **PASS** | All 28 route IDs below were present in the authenticated Newman contract and exercised against the Extended C server. |
| F07 — receipt storage bounds | **PASS** | Expired completed HTTP idempotency rows are pruned in a bounded delete; movement history remains. `C3_RECEIPT01`. |
| F08 — resource-list aggregate efficiency | **IMPLEMENTED; benchmark not run** | List availability uses grouped tenant-scoped aggregation rather than a per-resource aggregate loop. No scale/load benchmark is claimed. |
| F09 — independent test isolation | **PARTIAL** | The 38-method integration suite remains an ordered `PER_CLASS` MySQL workflow. C3 added distinct negative, migration, session, privacy, image, scheduler and receipt cases, but did not refactor the whole suite into independent schemas per test. |
| F10 — 12 npm audit findings | **OPEN PUBLIC-RELEASE RISK** | Itemized below. All are build/test tooling-chain findings; `npm audit --omit=dev` reports zero. Compatible remediation was not available from the configured package registry without breaking major downgrades. |
| F11 — role/tenant isolation and session changes | **PASS** | Real MySQL-backed HTTP checks cover role denial, cross-tenant IDs, explicit organization selection, session rotation, CSRF rotation, membership authorization and exact 12-hour expiry. |
| F12 — production boundary | **NOT CLAIMED** | Extended C is not a production deployment: no full Extended C UI, off-host backup/restore acceptance, real provider delivery, or public runtime is included. Continue only to C4 review; do not treat C3 as public-release approval. |

## Original/amended operation traceability: 26 + 2

The original 26 comprise the 25 proposal operations plus amended operation A09. `IMAGE-01` and `IMAGE-02` are additional syllabus image operations and are not counted in the original 26. Every row maps to an actual Jersey handler and a Newman request executed in the successful run; refused-delete, stale-version and alternate-filter requests are additional checks, not extra operations.

| ID | Jersey operation | Newman request / outcome |
|---|---|---|
| D01 | `POST /api/departments` | `D01 POST Department` — create |
| D02 | `GET /api/departments` | `D02 GET Departments` — list |
| D03 | `POST /api/departments/{id}/update` | `D03 POST Department update` — update |
| D04 | `POST /api/departments/{id}/delete` | `D04 POST eligible Department delete` — delete |
| P01 | `POST /api/projects` | `P01 POST Project` — create |
| P02 | `GET /api/projects?departmentId=…` | `P02 GET Projects` — scoped/filterable list |
| P03 | `POST /api/projects/{id}/update` | `P03 POST Project update` — update |
| P04 | `POST /api/projects/{id}/delete` | `P04 POST eligible Project delete` — delete |
| R01 | `POST /api/resources` | `R01 POST reusable Resource` — create |
| R02 | `GET /api/resources?type=…` | `R02 GET Resources by type` — filtered list |
| R03 | `POST /api/resources/{id}/update` | `R03 POST Resource update`; `R03-STALE` asserts stale-version refusal |
| R04 | `POST /api/resources/{id}/delete` | `R04-REFUSED` asserts nonempty refusal; `R04` deletes eligible empty resource |
| R05 | `GET /api/resources/{id}/availability` | `R05 GET Resource availability` — server-derived inventory |
| Q01 | `POST /api/requirements` | `Q01 POST main Requirement` — create |
| Q02 | `GET /api/requirements?projectId=…` | `Q02 GET Requirements by project` — filtered list |
| Q03 | `POST /api/requirements/{id}/update` | `Q03 POST Requirement update` — update |
| Q04 | `POST /api/requirements/{id}/delete` | `Q04-REFUSED` asserts reference protection; `Q04` deletes eligible unreferenced requirement |
| A01 | `POST /api/allocations` | `A01 POST reusable Allocation`; consumable and cancellation fixtures also exercise create |
| A02 | `GET /api/allocations?status=…&requirementId=…` | Combined, status-only and requirement-only requests verify each filter |
| A03 | `POST /api/allocations/{id}/approve` | `A03 POST approve reusable Allocation`; consumable approval also exercised |
| A04 | `POST /api/allocations/{id}/issue` | `A04 POST issue reusable Allocation` — reusable stock/custody transition |
| A05 | `POST /api/allocations/{id}/return` | `A05 POST return reusable Allocation` — custody return |
| A06 | `POST /api/allocations/{id}/consume` | `A06 POST consume consumable Allocation` — physical stock transition |
| A07 | `POST /api/allocations/{id}/cancel` | `A07 POST cancel Allocation` — cancellation |
| A08 | `POST /api/allocations/{id}/delete` | `A08-REFUSED` asserts issued-history protection; `A08` deletes eligible cancelled record |
| A09 | `POST /api/allocations/{id}/revoke-sms-consent` | `A09 POST revoke SMS consent` — contact/consent erasure |
| IMAGE-01 | `POST /api/resources/{id}/image` multipart `file` | `IMAGE-01 POST PNG upload` — validated database-backed PNG |
| IMAGE-02 | `GET /api/resources/{id}/image` | `IMAGE-02 GET PNG bytes` — binary response; byte-for-byte rechecked after restart |

The Postman v2.1 collection is `postman/OrgResource-Extended-C3-28-Operations.postman_collection.json`. The local environment template has no saved credentials. `scripts/run-extended-c3-newman.ps1` creates a temporary secret-typed environment, restricts the target to loopback, and removes it at exit. Successful run: **48 requests, 90 assertions, 0 failures**.

## Added behavior and security evidence

- **C2 durable replay:** receipt pruning removes expired HTTP idempotency rows but never stock movement. Tests verify same-operation replay after expiry/pruning and no second physical stock change. The packaged JAR verifier also checks receipt read/replay after restart.
- **Controlled input failures:** Jersey/Jackson/parameter failures become JSON client errors. Missing/invalid idempotency and `If-Match` inputs leave both resource quantity and movement count unchanged.
- **Tenant-scoped CRUD:** the original route handlers delegate to Spring components and use scoped repository methods; foreign numeric IDs resolve as not found. MEMBER writes are denied. A02 list filters cover each optional parameter combination.
- **Contact privacy:** contact numbers are E.164 validated, encrypted at rest, excluded from response DTOs/logging, tenant/allocation-bound, and removed when consent is withdrawn. V4 rejects legacy plaintext contact rows before applying the migration and the integration test verifies the legacy schema/row remains untouched after that refusal.
- **Images:** PNG/JPEG are bounded to 2 MiB and 16 megapixels, signature and decode validated, stored on the existing Resource row, and returned with safe content headers. Cross-tenant access and malformed content are denied. The packaged verifier compares uploaded and post-restart bytes exactly.
- **Lifecycle/protocol:** PENDING and APPROVED expiration is bounded and transactional; ISSUED reusable allocations stay in custody. Reminder claims are persisted before dispatch; simulator/provider outcomes are distinguished. The Retrofit/Gson transport is exercised against loopback FakeTwilio and Twilio-compatible form/Basic-auth stubs only. Production SMS is disabled by default; no real provider request or device delivery was made.
- **Session lifetime regression (requested addition):** `C1SessionService.selectOrganization()` rotates the session and CSRF token without rewriting `SESSION_CREATED_AT` or `LAST_REAUTH_AT`. `C1AccountSessionFilter` rejects a session at `elapsed >= 12h`. `C3_SESSION01` is an actual HTTP/MySQL test: it ages the persisted session, switches to a member organization, asserts new session ID and CSRF, rejects old CSRF and nonmember selection, verifies member read/member write denial, then tests expiry at exactly 12 hours and removal from Spring Session JDBC. The packaged verifier independently confirms authenticated session and selected organization persistence across JAR restart.

## Runtime and test results

| Gate | Command/environment | Result |
|---|---|---|
| Extended local backend and packaged acceptance | `pwsh -NoProfile -File scripts/verify-extended-c1.ps1 -SkipFrontend`; Java `21.0.3`; isolated MySQL `8.0.46`; Maven Wrapper `clean verify` | **PASS** — 38 tests; 0 failures/errors/skips; clean Flyway V1–V4; packaged JAR and React/API smoke; isolated database stopped and retained only under the temporary test directory. |
| MySQL migrations/invariants | Same local verifier; MySQL `8.0.46` | **PASS** — actual Flyway/JPA/MySQL, tenant constraints, role grants, transaction rollback, concurrency and restart persistence. Negative legacy-upgrade tests intentionally log Flyway failure and assert no schema/row mutation. |
| Newman acceptance | Local verifier; Newman `6.2.3`; packaged loopback JAR | **PASS** — 48 requests, 90 assertions, 0 failures. |
| Frontend | CI run `44180307800`; `npm ci`, typecheck, unit tests, lint, build | **PASS** — all steps green. Earlier full local frontend gate also passed; C3 did not edit frontend source or lockfile. |
| Course A Java 8 | CI run `44180307800`; Java 8, isolated MySQL, bundled JAR smoke | **PASS** — all job steps green. Course A source is unchanged. |
| Extended C Java 21 and Newman | CI run `44180307800`; MySQL `8.0.46`, ephemeral encryption key, packaged JAR | **PASS** — Maven/MySQL integration tests and authenticated 28-operation Postman collection succeeded. |
| CLI Playwright smoke | CLI Playwright only; no browser-control plugin; desktop `1440×900`, mobile `375×812` against the packaged React route | **PASS, smoke scope only** — `/#/projects` returned HTTP 200 with Projects heading, no page errors or horizontal overflow. This is not a complete Extended C UI acceptance; that remains C4 scope. |
| `git diff --check` | Before commits | **PASS** — clean. |

Effective Extended C runtime versions observed in the dependency tree: Java `21.0.3`; Spring Boot `4.1.2` / Spring Framework `7.0.10`; Jersey `4.0.3`; Spring Session JDBC `4.1.1`; Hibernate ORM `7.4.13.Final`; MySQL Connector/J `9.7.0`; literal `javax.validation:validation-api 2.0.1.Final` and Jakarta Validation `3.1.1` both present; Retrofit/Gson converter `2.11.0` / Gson `2.11.0`; OkHttp `4.12.0`. MySQL runtime validation was against `8.0.46`. Course A remains its separate Java 8 compatibility lane.

V4 migration SHA-256: `7F94FBBCDF1C8D0E3B7D0F76BB888D4E9E54EDD4BABB9F447274CF99AB50D0CC`.

### CI correction history

The first PR run (`44177636832`) passed Java 8 and React and passed the Extended Java 21 Maven test step, but Newman login failed because CI used a different fictional username from the user created by the integration suite. Workflow fixtures were aligned with the actual isolated test account. Run `44178844494` then passed all jobs. After removing the fixed test encryption key and generating it ephemerally, run `44180307800` also passed all three jobs on code SHA `b8b2d3a70c113c7a12b20ed7d0f2fa930d3586df`.

The Java 21 test log's failed Flyway migration entries belong to explicit fail-closed migration tests for unreconciled/legacy plaintext data. Surefire reports all 38 tests passed. CI also emits non-failing GitHub runner deprecation/Ubuntu migration annotations for the existing `actions/checkout@v4` and `actions/setup-node@v4` actions.

## npm audit: package-by-package

Executed in `frontend/` against the committed lockfile:

- `npm audit --json`: **12 high, 0 critical/moderate/low** affected package records.
- `npm audit --omit=dev --json`: **0 vulnerabilities** in production dependencies.
- The 12 entries trace to the Picomatch ReDoS advisory `GHSA-fc5q-39xc-vjpf`, range `picomatch <=4.0.7`, through build/test tooling. They are not 12 distinct advisories and are not evidence of a browser-runtime exploit.
- `npm audit fix --dry-run` only offered `--force` changes that downgrade Vite/Vitest/TypeScript-ESLint related packages across major versions. `npm view picomatch@4.0.8 version` returned registry `404`; no compatible patched version was available from this configured registry at audit time. No blind upgrade, override, suppression or lockfile rewrite was applied.

| Affected entry | Installed | Reported chain / npm remediation |
|---|---:|---|
| `@typescript-eslint/eslint-plugin` | 8.73.0 | `parser` → `typescript-estree` → `tinyglobby` → `picomatch`; automatic compatible path reported, but full tree remains affected. |
| `@typescript-eslint/parser` | 8.73.0 | `typescript-estree` → `tinyglobby` → `picomatch`; automatic compatible path reported, but full tree remains affected. |
| `@typescript-eslint/type-utils` | 8.73.0 | `typescript-estree`/`utils` → `tinyglobby` → `picomatch`; automatic compatible path reported, but full tree remains affected. |
| `@typescript-eslint/typescript-estree` | 8.73.0 | `tinyglobby` → `fdir`/`picomatch`; npm suggests major `typescript-eslint` downgrade to 8.47.0. |
| `@typescript-eslint/utils` | 8.73.0 | `typescript-estree` → `tinyglobby`/`picomatch`; automatic compatible path reported, but full tree remains affected. |
| `@vitejs/plugin-react` | 6.3.0 | `vite` → `tinyglobby`/`picomatch`; npm suggests major plugin downgrade to 5.2.0. |
| `fdir` | 6.5.0 | Depends on vulnerable `picomatch`; an automatic update was reported but current graph remains vulnerable. |
| `picomatch` | 4.0.7 | Root advisory `GHSA-fc5q-39xc-vjpf`; npm suggests breaking Vite downgrade rather than a compatible patch. |
| `tinyglobby` | 0.2.17 | `fdir` and `picomatch`; npm suggests major Vitest downgrade to 3.1.4. |
| `typescript-eslint` | 8.73.0 | Its ESLint chain reaches vulnerable `tinyglobby`/`picomatch`; npm suggests major downgrade to 8.47.0. |
| `vite` | 8.3.8 | `tinyglobby`/`picomatch`; npm suggests major downgrade to 6.2.7. |
| `vitest` | 5.1.0 | `vite`, `tinyglobby` and `picomatch`; npm suggests major downgrade to 3.1.4. |

The frontend lockfile/package manifest was not changed for this advisory. Keep public production release blocked until the owner reviews and resolves the tooling risk.

## Changed areas

- `extended/backend/src/main/java/edu/orgresource/extended/rest/` and `service/`: missing D/P/R/Q/A methods, safe query semantics, scoped roles and image operations.
- `extended/backend/src/main/java/edu/orgresource/extended/security/`, `integration/`, `scheduler/`: immutable session lifetime, contact encryption, loopback reminder protocol and bounded scheduled lifecycle.
- `extended/backend/src/main/resources/db/migration/V4__c3_contact_images_and_reminders.sql`: fail-closed contact privacy, Resource image and reminder persistence.
- `extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java`: 38 combined HTTP/MySQL integration methods, including the requested exact-boundary session-switch regression.
- `postman/OrgResource-Extended-C3-28-Operations.postman_collection.json`, `postman/OrgResource-Extended-C3-Local.postman_environment.json`, and `scripts/run-extended-c3-newman.ps1`: importable collection and safe loopback runner.
- `scripts/verify-extended-c1.ps1`: isolated MySQL, ephemeral key, packaged restart and byte-persistence checks.
- `.github/workflows/verify.yml`: separate Java 21/MySQL/Newman C3 job; preserves Java 8 Course A and frontend CI.

## Remaining limits and next dependency

1. **Owner review required:** 12 high npm audit package records remain in dev/build tooling; production audit is clean, but do not publish publicly until an owner-reviewed compatible fix/risk acceptance exists.
2. Full Extended C user interface and accessibility acceptance are not part of C3; proceed to C4 only when authorized.
3. Production contact-key provisioning/rotation and off-host restore are not proven here. The key must be supplied securely outside source and GitHub logs.
4. Loopback FakeTwilio/Twilio-compatible tests prove protocol formatting and local behavior only. No provider acceptance, carrier delivery or live SMS is claimed.
5. Test suite independence is partial because the ordered `PER_CLASS` integration flow remains.
6. No merge, public release, live provider request, Canvas submission, or Extended C4 implementation was performed.

**C3 verdict:** **C3 VERIFIED — 26+2 COURSE ROUTES/IMAGE/SCHEDULER/PROTOCOL LIVE, C2 HARDENING PASS; READY FOR C4 REVIEW.** Public production release remains blocked by the unresolved development-tool audit risk and the production/off-host operational work listed above.
