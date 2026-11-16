# CP37 — SEALED copy-ready Codex implementation prompts (S0–S10)

> **2026-11-16 external/source correction:** [CP40](67_CP40_PROVIDER_TRIAL_SECURITY_AND_DEADLINE_REALITY_CHECK.md) specifies tested external limits: Twilio trial permits predefined templates/verified recipients and expires; OCI may reclaim idle free VM; private GitHub Actions usage is quota/billing-bound. S1 must also test static web assets alongside real Jersey routes; S3 P0 invite tokens are unverified bearers, no intendedUserId; S5 FakeTwilio proves protocol only. Prompts remain **SEALED**.


**Date:** 2026-11-16 PHT. **Status:** PLANNING ARTIFACT ONLY, **not** an instruction to execute in this research chat or permission to start Codex. These are a sequential handoff toolkit for **later explicit owner authorization**, after reading the pre-Codex review packet CP31–CP36. No code, test, hosting, merge, payment, SMS, Canvas or credential action was performed by preparing these prompts.

## Universal constraints for every later stage

The project is **ONE OrgResource application**, one UI, one authoritative MySQL ledger, actual original five JPA entities and the full 25 proposed + O14 A09 = 26 protected Jersey operations, true applicable course technologies, modern tenant/auth safety, fictional data, no mandatory recurring payment. No official Ateneo integrations or adoption claims. Course evidence is original `09_ESP_PROJECT_ASSIGNMENT.pdf` (3pp), `08_CSCI180_SYLLABUS.pdf` (9pp), `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` (12pp), course code and slides, O14 pinned SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`. Current Canvas has priority for changed directions but must actually be checked by student. **Never** modify original O14 baseline, merge main, change GitHub permissions, deploy, purchase services, send real SMS, assume an instructor waiver, invent real accounts/user studies, or continue to subsequent stage without separate approval.

Required read order for ANY stage: [CP31 master](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md) → [CP32 conflicts](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md) → [CP33 acceptance](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md) → [CP34 schema](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md) → [CP35 public API and UI](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md) → [CP36 legacy atomicity](63_CP36_LEGACY_COMPATIBILITY_ATOMICITY_AND_CUTOVER_GATES.md) → latest CP30 one-product authority [54](54_CP30_ONE_UNIFIED_ORGRESOURCE_DECISION_AND_CONFLICT_REGISTER.md) and [55](55_CP30_ONE_PRODUCT_COURSE_AND_PRODUCTION_COMPLIANCE_MATRIX.md). Directly inspect the actual files rather than blindly trusting these summaries.

**Evidence format on every step:** `stage, source SHA, base SHA, scope, exact commands, environment versions, test cases, expected/actual, passing/failing/untested, affected files, risk introduced, rollback, CI run URL, artifacts, unresolved owner decisions, next safe stage`. Do not claim a test ran if only source or an old report exists. Use individual small PRs and no main merge without owner approval.

---

## S0 — Source and branch baseline; READ-ONLY

**Paste to Codex later, not now:**

> Read the OrgResource CP31–CP37 read-first hierarchy and the actual CSCI 180.05 assignment, syllabus, proposal, slides, source archive inventory, O14 Java test sources and active PR trees. In **read-only** mode, pin exact main, #2–#8 bases/heads, research branch and O14 commit SHAs. Verify 26 Jersey handlers, five actual JPA classes/table names (including `resource_requirement`), academic DTOs, existing tests and workflows. Reconcile any additional source divergences against CP32–CP36 without writing application code. Produce a source-coverage register, PR dependency graph, accurate CI/local evidence ledger and change impact proposal. Never merge/modify main, touch user databases or use provider credentials. Stop and report any contradictory authority or missing file before S1. Deliver exact references and hashes; classify source inspected vs observed execution vs report. Request separate authorization before any experiment.

**Gate:** independent source-correct read-only baseline and no writes; S1 still not authorized automatically.

## S1 — UNIFIED-COMPAT-01: ONE-runtime local feasibility, isolated branch only

> On an **authorized isolated disposable experiment branch**, without touching O14 or external accounts, prove a maintained Java/Jersey/Spring/MySQL implementation candidate for ONE OrgResource application. Use official released versions pinned from an actual effective dependency graph, with Java 17+ (prefer Java 21 if verified), Boot 4.x/Jersey 4.x/Jakarta stack **as hypothesis only**, and a real, explicit `javax.validation` path. Execute: UC01 real start+GET/POST Jersey HTTP **plus bundled React static asset path and API authorization coexistence in one JVM**; UC02 `@Autowired` controller→Component→JPA/MySQL; UC03 invalid javax DTO; UC04 invalid javax-annotated JPA entity with actual saveAndFlush and transparent automatic-vs-explicit validation claim; UC05 Jakarta native validation+auth+CSRF; UC06 original five entities+JPQL/rollback; UC07 Retrofit/Gson FakeTwilio HTTP+bounded scheduling; UC08 **static inventory/crosswalk of all 26 original handler signatures** plus **one real executable representative secured Jersey route per original entity family** (five selected probes), NOT the full 26-route product implementation; UC09 stock T/H/I whole-batch and clock boundaries; UC10 one DB used by legacy/public routes; UC11 effective dependencies/SBOM and known vulnerabilities; UC12 isolated reproducibility/teardown. Record logs, SQL and actual class/package names, no manual bypass masked as JPA auto validation. If any source-required validation or stack capability cannot be proven, mark FAIL/PARTIAL and STOP. Do not substitute an unsupported runtime or proceed to full implementation on a CI-green-only assumption.

**Hard stop:** all **bounded spike** UC cases must be evidence-classified before architecture decision. **Full execution of all 26 legacy operations is explicitly S5 acceptance**, not a prerequisite to starting S2; do not mislabel five representative runtime probes as 26-route compliance. No full product build until owner reviews the critical cross-runtime experiment and chooses supported architecture or a transparent alternative.

## S2 — Versioned architecture, API and DDL contract reconciliation

> Only after accepted S1, compare actual dependency and SQL evidence with CP34 and CP35. Produce an architecture decision record with exact Java/Boot/Jersey/Hibernate ORM/Validator/JDBC/React/Node/MySQL/Session versions, transitive compatibility, support and caveats; no presumed javax auto event listener. Produce a versioned DDL migration plan from **empty** database (not speculative O14 fake-data import), parent/child composite FKs, `resource_requirement` physical name, enums, constraints/indexes, no bad MATCH clauses, password/session/recovery structures from exact official library version, and ledger idempotency. Include a machine-readable positive/negative per-route OpenAPI/DTO/error/role manifest for 26 legacy + all v2/identity/image operations. Freeze source/code mapping, JSON, date conversion, expiry and error contracts. Any field/role/route left open is a STOP for its dependent feature; submit owner decision diff without silently choosing a historic V2 rule. No broad product code until scope approved.

**Gate:** reviewed exact selected stack and migration/API design, not proof of runtime just because prose is complete.

## S3 — Real schema, accounts, identity and cross-tenant security

> Using the approved S2 design, implement MySQL migrations, five core source-equivalent entities and justified infrastructure tables, server-side sessions+CSRF, anonymous preauth CSRF, login/logout/recovery, hashed <=24h 256-bit one-use bearer invites (NO intendedUserId or campus-identity claim), anonymous fictional registration and existing-account join, invitation and ownership-transfer expiry/revocation, offline fictional first-owner bootstrap, ACTIVE membership/one-role RBAC, explicit activeOrg, owner transfer and last-owner prevention. Every read/write in original `/api` and public `/api/v2` must scope current authenticated org and role on server-side SQL+Components. Implement no `@ateneo.edu` verified assertion. Negative tests: anonymous401, wrong-role403, foreign404, forged org header, cross-tenant FK raw insert, CSRF fixation/reuse, membership suspension immediate denial, reset+disable+owner-transfer races. Keep test fixtures fictional. Deliver migration logs and exact negative security results; no public ingress or credentials in PR logs.

**Gate:** SEC-01..05 and SCHEMA tenant constraints pass on actual MySQL, or STOP.

## S4 — Whole-unit resource transactions and durable stock history

> Implement exact global P0 semantics from CP32–CP34: Department→Project→ResourceRequirement links; T/H/I reusable vs consumable; upcoming Project may approve while issue/consume only active; return may occur after end; one timestamp per transaction; six allocation statuses with CANCELLED/EXPIRED reason, and effective endAt hold before scheduler cleanup. One transactional authoritative Resource.total, same-org locks/FKs, long checked arithmetic, Requirement coverage and largest pending guard. Append immutable INITIAL/RECEIPT/ADJUST/ISSUE/RETURN/CONSUME movements, permanent org-scoped operationId uniqueness, and 24h separate idempotency receipts. No ghost zero INITIAL, no T/I negative, never auto-return. Test q4+q4/T5 with 100+ repeats, rollback midway, deadlocks, two workers, 48h replay after restart and expiry. Record exact SQL row counts, guard errors and reconciliation. No partial/damaged/lost returns or future-slot booking unless separately approved.

**Gate:** INV-01..08, SCHEMA-06..14 and transaction integration tests pass, no skipped or quarantined failures.

## S5 — Exact original 26 secured Jersey routes, v2, Retrofit/scheduler/image

> Implement both surfaces in **ONE app** with one shared Component/domain transaction layer. Protect every O14 `/api` source method with session, CSRF, activeOrg, keep original Long IDs and unchanged business JSON/HTTP response except disclosed security/history differences. Use server-generated movement identities for old bodyless A04/A05/A06 and R01/R03, preserve stale Resource CAS and A09 repeat-safe contact clearing. Public v2 uses UUID/UTC/envelope, role/version/Idempotency-Key and operationId; explicit GET by-ID pages and resource history receipts. Implement bounded FakeTwilio Retrofit/Gson and scheduling; provider disabled by default, and never assume current 30-day Twilio trial supports arbitrary personalized SMS or unverified recipients (see CP40); any authorized permitted template/provider mode is separate acceptance, never claim delivered from simulator. Implement real resource-image multipart receive+serve and safe delete (max 2MiB, MIME sniff, no SVG/external fetch, tenant isolation). Execute all 26 ARC cases, COMPAT-L01..16, actual Newman JSON and DB postconditions, image error tests, consent send/revoke races, 2 scheduler workers. Do not email/text real numbers or claim Twilio real provider proof without authorization.

**Gate:** actual protected 26 route source parity, public cross-API same DB, no mock, no unchecked image/phone exposure. Instructor CLO9/Twilio questions remain owner/external unless written direction available.

## S6 — Connected accessible web user experience

> Build the ONE React UI with login/recovery/invite, org selection, Overview, Departments, Projects, Resources stock receive/adjust/movements/images, Requirements, Allocations transitions, Members/transfer, Account, Activity/Audit/Exports, Reminders and About. O14 eight pages are source precedent; do not import obsolete unapproved nine-entity 68-route client. All UI state driven by actual secured API/MySQL; no mock-only successful flow. Use modern type-safe client with exact DTO/errors/ETag/CSRF and stable operationId on retry. Safe 401 re-login, 403 deny, 404 foreign hidden, 409 stock refusal, 412 stale review, 503 dependency and ambiguous network outcome resolution. Test login->whole reusable return, consumable once, owner transfer, org switch, live image and simulated reminder. Check 360/768/1440 widths, touch/keyboard, loading/empty/error, dark/light and reduced motion. Record actual Playwright/browser screenshots, not AI-generated substitute for evidence.

**Gate:** connected end-to-end journey tests and a real independent visual boundary inspection pass.

## S7 — Safe release/CI/CD/host and proven independent DR

> **Only with explicit account/hosting approval** and proven zero-mandatory-cost quota plan: configure PR CI least-privileged read-only, pin build/tool versions, real MySQL integration + Newman + Playwright + source ZIP/hash and dependency scan, release artifact with SHA manifest, protected deployment job only after authorized review. Never deploy untrusted PR with prod secrets or persist a general-purpose runner on prod host. Candidate free OCI ARM/MySQL is conditional on real region capacity/billing/quota; stop rather than charge. Run one protected Java service and private MySQL behind trusted HTTPS, firewall and secret management. Prove certificate issue/automatic renew/reload, deploy rollback, restart, privacy logging, cost shutoff, off-provider encrypted backup and **clean independent restore** with revoked old sessions/recovery tokens and reconciled ledger. Record actual RPO/RTO not prewritten promises. If capacity/restore impossible at $0, P-ACCEPT FAIL/BLOCKED; do not quietly switch to paid or expose Boot2.

**Gate:** real infrastructure, no secrets/leaked tenant data, DR proof and external fee receipt.

## S8 — Independent production P-ACCEPT

> On the exact release SHA and actual approved target, execute independent security/tenant/stock/permission/browser/Newman/race/rollback/restart/restore/cost/TLS/idempotency/rate-limit and auth-account recovery checks. Audit all source-critical flows against CP33–CP36, including negative 401/403/404 and authorized resource images. Show evidence commands/run IDs, screenshots and an explicit table PASS/FAIL/BLOCKED/NOT_RUN; do not claim production readiness from docs, source tests or one green CI. Stop on unauthorized cross-org read, duplicate physical movement, missing auth/CSRF/SSL, bad DR or unapproved billing. Do not invent real Ateneo adoption, student signatures or official Twilio delivery.

**Gate:** human-reviewed actual P-ACCEPT; unresolved blocking defects prohibit next stage under current user sequencing.

## S9 — Independent academic A-FINAL on SAME product

> After owner-approved P-ACCEPT, independently re-execute original 25 proposal + A09 secured handlers, controller→@Autowired Component, JPA five entities, custom Spring Data/JPQL, direct literal javax DTO/entity validation, GET/POST runtime create/read/eligible delete, true Retrofit/Gson HTTP/FakeTwilio versus provider-mode distinction, bounded scheduler, applicable CLO9 image, complete importable Postman/Newman, clean Java/Eclipse source export and bundled UI on a clean non-test database. Review exact source SHA, 55-test historical O14 baseline with known intermittent expected5/actual8 caveat, final new tests and recorded course-specific changes. Prepare source ZIP/JAR/checksums and a script for actual student-operated presentation. Current Canvas/instructor changes remain external, no automatic academic compliance claim.

**Gate:** reviewer classifies every course requirement with actual evidence, unknown school directions marked open; no phantom instructor approval.

## S10 — Student-only final academic responsibilities (not a Codex action)

> Generate a **checklist only**, never fabricate or perform student authorship, video discussion, actual peer reviews, campus course enrollment, instructor acceptance or Canvas upload. Student checks current Canvas, verifies group members, performs actual demonstration/recording or valid early presentation, ensures instructor video access, completes truthfully signed Certificate of Authorship and AI/external assistance acknowledgement, each real peer review and actual submission receipt. The assignment's printed final due is December 5, 2026; current Canvas may change it. The completion record is the real receipt and accessible artifacts, not an agent assertion.

**Gate:** only student/instructor evidence may mark academic submission complete.

---

## Final separate-authorization rule

These prompts are **sealed, not dispatched**. The current user request asks research/planning to be reviewed **before** Codex. A stage starts only when the user explicitly releases it and prior stage evidence meets the gate. If the unified runtime fails at S1, present alternatives with concrete course and security consequences; never proceed directly to S3–S10. Preserve small, reviewed commits/PRs and the right to decline an infeasible free host. No stage is certified performed by the existence of these paragraphs.
