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
