# V2 verification, Postman, performance, privacy and release gates
**Status:** PROVISIONAL TEST PLAN. **No V2 tests performed** as of 2026-11-15. Academic V1 remote **reports** tests G08; do not equate with independent execution. Sources C01–C03/G08, E02–E05, F01–F26.

## Test pyramid and required artifacts
| Gate | Tests, adversarial cases and expected proof | Pass condition |
|---|---|---|
| T0 academic preservation | Java8 original build, 5 entity schemas, 26-route Postman, Retrofit simulator, scheduler, 201/200/400/404/409, exported source, CLO9/Twilio question | Reproducible actual run plus source hashes; course statuses truthful |
| T1 V2 contract/unit | Typed schema validation, statuses, stock and outstanding ledger pure functions, half-open time overlap, authorization permutations, idempotency hash behavior | All tests pass with deterministic clocks; no business rule skipped |
| T2 DB transaction and migration | Composite tenant FK direct insert attacks, unique names, last owner, rollback fault after movement, schema init from empty, migration with prior sample version, partial commit | DB constraint rejects cross-tenant, rollback exact event counts |
| T3 property-based model | 10k+ generated action sequences across 2 orgs, compare service to independent ledger/availability oracle, never negative T/I, no unaccounted movement | Zero invariant violation, seed/counterexample saved for reproduction |
| T4 concurrency | 100 parallel conflicting reserve/issue/return/consume/expire cases against actual target DB; two q4 approvals on T5 yields one successful commit; idempotency duplicate response lost | No double movement, correct 409 conflict, retry-safe audit |
| T5 API/Newman | Collection with every OpenAPI route, good/bad/no-auth/wrong-org/ownerless, paginated filters, malformed dates/JSON, stale versions, 429, 503 | Every route has positive+negative assertion, actual server HTTP evidence |
| T6 browser E2E | React real API, 360/768/1440 widths, light/dark, slow/failed network, accessible field labels, keyboard/modals, responsive navigation | No clipped/overlapped controls, no blind retries, all real state server-backed |
| T7 security | OWASP ASVS baseline, IDOR/BOLA all IDs, XSS, CSRF, SSRF, invite replay, enumeration, cookie settings, secret/PII logs, dependency CVE | No P0/high-severity unresolved exploit; independent review where feasible |
| T8 availability/free quotas | Provider-specific CPU/storage, data sizes, 25 concurrent/30-minute load, quota simulation, cold start and no payment overrun | Meet [09](09_QUALITY_SRS.md) target **when measurable**; fail closed without spending |
| T9 disaster recovery | Stop process, drop simulated primary fixture, restore encrypted offsite backup to alternate clean runtime, reconcile rows/hash and key recovery | All invariants stable; RPO/RTO measured, not merely claimed |
| T10 public demo abuse | 2+ concurrent demo guests, 24h expiry/reset, no real contacts, cross-sandbox request, fake network failure and public non-affiliation | No cross-sandbox access, no external communications, no shared writable tenant |
| T11 post-graduation support | New maintainer from docs alone installs/rebuilds/restores versioned current source using no secret in repo; rollback after migration | Reproduction log/evidence signed off |

## Deterministic Postman order
1. Acquire separate synthetic principal/session for org A OWNER, org B OWNER, A CUSTODIAN, A LEAD and outsider.
2. GET `/session` and memberships, verify org scope; outsider A list 404.
3. Create org A/B via signed-in policy, invite+redeem A roles, test token replay.
4. POST A Resource reusable q5 and Consumable q100, duplicate name409, create same name in B success.
5. POST A Project A [09,12), Project B [11,13), Project C [12,14) and requirements.
6. POST allocation A q4 PENDING, approve A→APPROVED; GET available A now and per windows.
7. POST B overlapping q3: CAPACITY_CONFLICT409. POST C nonoverlap q5 approved while A unissued.
8. Issue A q4 at injected clock09:00; verify I4, T5, C issue at clock12 blocked until returns.
9. Return A q3, verify I1 and PARTIALLY_RETURNED; repeat same idempotency key no double; return last q1, verify I0.
10. Issue C q5 at 12 after A fully returned; return all; verify event ledger and planned inventory.
11. Approve paper q30, consume once -> T70, repeat consume409 and unchanged headroom; test cancel separate approved hold.
12. Test two approval races using isolated fixtures and external concurrent runner; verify exactly one and MySQL/SQLite invariant.
13. Run expiry at end boundary: unfulfilled APPROVED expires; ISSUED not auto-returned.
14. Try 20+ cross-tenant ID/role substitutions, stale versions, malformed dates, invalid unit, overlarge payload.
15. Export A, inspect no B fields or raw secrets, import into empty equivalent DB, assert stock/event checksum.
16. Simulate POST lost HTTP response and GET operation receipt; no duplicate business event.
17. Verify serialized activity/audit with redacted actor IDs, no email/SMS sent; daily provider quota fail produces 503 not a paid auto-upgrade.

**Artifacts required:** Postman JSON collection/env (no credentials), CLI runner results, transaction logs, DB query/lock tests, test fixtures with seed/clock, HTML UI screenshots, a11y scan + manual keyboard notes, benchmark hardware/provider plan, backup restore SHA manifest, dependency SBOM and incident report. None of these V2 assets should be labeled existing.

## Acceptance and failure-response details
- Separate tests for API logic **and** actual persistent DB, not mocked `repository.findAll()` only.
- Property cases include zero/maximum/negative invalid quantities, interval adjacency, end==start invalid, approval at project end, orphan FK, duplicate idempotency hash, two org membership changes mid-request, crash after transaction commit before HTTP response.
- Release fails if a test yields negative effective inventory without `atRisk` documented, a missing org predicate, a usable partial return incorrectly closes all custody, an uninvolved org sees audit data, demo guest can email real users, or an account is billed.
- A failed provider quota gate yields planned local-only fallback, not pretending the product is production-ready.
- Manual gate includes owner review of scope/branding/cost and any future authorized org representative only for an optional real pilot; do **not** invent stakeholder UAT now.

## CI versus local test distinction
Github workflow checks from PR #6 are **real remotely reported successful CI**. Playwright/local packaged-JAR and MySQL/Newman runs in G08 are **recorded local evidence**, not independently rerun here. V2 tests are **all planned/unrun**. This distinction must be replicated in phase reports and release notes.

## 2026-11-15 binding final gate order and evidence schema (USER DECISION)
**T0 academic source preservation at O14 is only the early baseline, NOT final academic acceptance.** Run V2 `T1..T11` against the actual supported binary, target relational DB and (if publicly claiming production) actual zero-fee HTTPS host, then independently perform the final original-stack A-FINAL. The mandated exact order is:
```
O14 BASELINE + preserved course artifact → production engineering
→ real T1..T11 evidence → P-ACCEPT PASS
→ A-FINAL literal academic re-verification AC01..AC17 PASS
→ STUDENT manual Canvas submission
```

| Release gate | Hard PASS evidence, not merely a plan or old CI |
|---|---|
| **P-01 source/runtime** | Commit+build SHA, supported BOM/Java/Jersey/JPA/Security, clean reproducible release and resolved critical advisories |
| **P-02 auth/tenant** | Real sessions/CSRF (if cookie), owner/revocation, every route wrong-org negative, direct FK database rejection, sensitive-log/secret scan |
| **P-03 correctness** | Generated stock invariant tests, 100 conflicting real DB race attempts, booking/custody edge cases, lost-response idempotency, scheduler two-worker/restart protection |
| **P-04 API/UI** | Real HTTP Postman positive/negative for all in-scope V2 APIs, integrated v5 Playwright, keyboard/contrast/360–1440px/failure states, no frontend mock-only pass |
| **P-05 live operations** | Verified account free/eligible, actual public TLS certificate and host smoke when hosted, quotas and fail-closed no charge, off-host encrypted backup **and clean restore**, RPO/RTO measured, secrets/keys and incident rollback demonstrated |
| **P-06 human review** | Student/technical reviewer signs dated PASS with full receipts for all gates; all critical/high risks closed or fail |
| **A-FINAL after P-ACCEPT** | Clean rebuild and actual real 5-entity CRUD, Jersey2, Javax, JPA, Retrofit/Gson FakeTwilio simulation, Spring scheduling, Postman collection, original video/source export, CLO9 decision, Certificate of Authorship and assistance; verify student-only Canvas |
| **CANVAS last** | Student alone uploads after P-ACCEPT and A-FINAL; no fabricated confirmation |

Record each test as `gateId, sourceSha, artifactSha, env, command, timestampUTC, outcome(PASS|FAIL|BLOCKED|NOT_RUN), evidenceLink, reviewer, residualRisk`. FAIL/BLOCKED/NOT RUN are **not** PASS; no exception from December deadline. Dec3 P-ACCEPT/Dec4 A-FINAL/Dec5 Canvas are aggressive **risk checkpoints**, not completion guarantees. A local-only functional demo is not live technical production acceptance if live deployment is required. [18](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md), [25](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md).

## Research vs executable E2E gates (2026-11-15)
**NEW R-ACCEPT gate BEFORE any Codex product phase:** R0–R10 [29](29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md) must be researched, reconciled and user-reviewed. Pre-build documents can specify exact expected endpoint/status/row/SQL/lock/role behavior but **cannot truthfully report integration, actual host, restore, security, GUI or concurrency results**. A controlled compatibility spike is engineering and requires separate user permission; it may be the first *bounded* Codex activity after research signoff, not a disguised research-only E2E pass. Until then, production P-ACCEPT and independent original-course A-FINAL are NOT RUN; original stack is nonnegotiable.

## CP5 identity + DB acceptance matrix addition (2026-11-15)
Before auth design can be accepted, specify and later run ID-01..15 in [32](32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md): concurrent offline owner bootstrap, generic throttled login, recovery one-time+credential_version/session invalidation after restart, CSRF login/logout and mutation tests, session fixation, BOLA multi-tenant role switching, ADMIN/last-owner race, two-party transfer expiration/concurrency, invite replay/identity ambiguity, cross-org composite FK raw SQL, disabled account invalidation, DATETIME(6) 2040/timezone roundtrip, Spring Session JDBC persistence+logout, and preserved Java8/Jersey2/Javax/Retrofit/MySQL/26 original route regression. **ALL V2 tests remain NOT RUN; source/legal documentation is NOT executable proof.**

## CP5 adversarial ID-16 addition (2026-11-15)
Global user disable across two orgs: a sole owner of both may not be disabled while either org would become ownerless; test partial alternative co-owners and simultaneous other-OWNER demotion. Must lock all affected org rows deterministically and use effective ACTIVE OWNER membership + ACTIVE user_account; prove zero partial writes, no stale sessions, no unwanted effect on unrelated organization. **NOT RUN.** [32](32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md).

## CP6 DB-01..14 source-based future tests (2026-11-15)
[33](33_TENANT_MYSQL_RESERVATIONS_LEDGER_AND_GLOBAL_COMPATIBILITY.md) defines 14 **NOT RUN** MySQL integrity tests: same-resource approve race, tenant A/B isolation, wrong-org department FK, same-org wrong-resource movement FK, conditional stock movement allocation null, reusable whole-batch transition algebra, consumable double-use prevention, temporal event-sweep counterexample (only if approved), RECEIVE retry after 48h, 100-thread race + deadlocks/timeouts, below-custody stock reduction, closure-vs-approve, tenant name uniqueness, fresh database schema/migration/backup restore. Explicit original A-FINAL must continue matching five source entities and all 26 courses routes, `javax.validation`, Postman, Retrofit/Gson/Twilio and scheduler. Research arithmetic cannot be labeled E2E proof.

## CP8 security/provider actual proof obligations (2026-11-15)
Before any compliant supported public Java architecture acceptance, execute (ONLY after separate Codex authorization) [VA-01..VA-12](35_VALIDATOR_PROVIDER_SECURITY_AND_SUPPORTED_RUNTIME_GATES.md): exact O14 Maven tree and packaged JAR; true Javax provider loaded with modern Spring/Jersey; negative old-style DTO validations incl nested; native Jakarta coexistence/bypass checks; CVE-2026-93879/EL interpolation input control; SBOM transitive/maintenance review; actual MySQL/Jersey/@Autowired/Retrofit-GSON/scheduled behavior; security tenant/CSRF/400/401/403; lifecycle cleanup; unchanged original five entities/26 routes Postman; JVM/ARM support. **Every CP8 runtime/security test NOT RUN.** Do not replace course technologies with only Jakarta or expose Boot2 publicly.

## CP9 split Validation API execution gates (2026-11-15)
Future authorized actual verification [36](36_VALIDATION_DUAL_API_AND_LIBERTY_CVE_SOURCE_COVERAGE.md) PC-01..06 includes 2 Javax/Jakarta API artifact class and GAV inspection, HV6 old Jakarta2 transitive exclusion, explicit provider resolution, real invalid Javax DTO HTTP400, native Jersey4 Jakarta validation and Spring auth/CSRF, SBOM and IBM/Open Liberty patch distinctions, JPA/MySQL/Retrofit/Gson/scheduler/26 original course API behavior, clean restart and unchanged academic baseline. **ALL NOT RUN**, not research proof.

## CP10 mandatory dual-validation and Twilio evidence separation (2026-11-15)
[37](37_COURSE_SLIDES_TWILIO_VIRTUAL_AND_ENTITY_VALIDATION_AUDIT.md) adds **VA-13..15**: invalid original-style entity `javax` constraints rejected on JPA save/flush **without DTO validator path**, 0 invalid rows/rollback; explicit actual Javax lifecycle/manual adapter on ALL write paths including scheduler; real invalid DTO 400 and persistence failure with no stack trace or misleading 404. Original O14 course source entity annotations verified; future supported modern behavior **NOT RUN**. **T-A FakeTwilio:** local HTTP simulated acceptance, **T-B Twilio API→Virtual Phone:** real provider API/virtual inbox, **T-C:** actual carrier/handset receipt. Evidence must be labeled by level; instructor/Canvas decision OPEN, no actual provider access. Add 30-day trial/templating/Console-availability stopline; user must separately approve account and any charges. Course teaching Jersey controller/JPA/MySQL/Retrofit/Spring annotation preserved, not swapped. All CP10 executable tests NOT RUN.
