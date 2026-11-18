# OrgResource — Complete copy-ready Codex master prompts: C9, C10, C11


**ADDITIVE HOSTED ATENEO DEMO GATE (USER DECISION, 2026-11-19):** Read the entirety of docs/execution/08_ATENEO_HOSTED_DEMO_REQUIREMENTS.md, 09_DEMO_SEED_ALGORITHM_AND_ACCEPTANCE.md and 10_CODEX_DEMO_DATA_IMPLEMENTATION_PROMPT.md, and data/reference/ateneo-public-organizations-2026.json and data/reference/ateneo-public-programs-2026.json (public program names factual only), plus all existing course/evidence sources. Treat C9-D deterministic ledger/source/provenance/privacy/performance regression, C10-D **separately authorized** public hosted read-only or isolated trial + one-time seeded 12-org demo + off-host backup/restore, and C11-D independent factual-versus-simulated data truth/no-unfounded-adoption review as mandatory hosted-release subgates. Their complete prompts are in document 10. This adds to the phase prompt and the full QUALITY_EVIDENCE_CONTINUOUS_GATE.md; it does not omit prior mandatory criteria. No hosted population/deployment/test is claimed by this documentation requirement.

Edition 2026-11-19. Future **proposed** prompts. Read AGENTS.md and the complete docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md. A documented risk deferral must not be misrepresented as a passed gate. User approval for prior stage is not permission for a later stage; no default authorization to merge, host, spend, use real data, contact people, enable live SMS or submit to Canvas.

---

# PROMPT C9 — Failure correctness, algorithmic/storage/build performance and CI robustness

## Entry and boundaries

You are the C9 Codex coordinator. **Start only after explicit owner authorization of C9 and review of C8 acceptance**. Confirm current repository newonathe/OrgResource, HEAD, branch, worktree, remote and PR graph. Base from the exact approved C8 release candidate (resolve from C8 report; do not guess SHA or branch) with no uncommitted edits. Course Final A is immutable at 7f8c0746da18fd54b47f430ac1435ac36e605d67. Never edit Course A backend/frontend or original course examples, archived academic evidence, Flyway V1–V5 files or historical release hashes. Inspect affected transitive source, not patch-only evidence.

Read in full AGENTS.md, QUALITY_EVIDENCE_CONTINUOUS_GATE.md, 00–06 execution docs, C6/C7/C8 reports/decisions, exact Extended Maven POM, Node package-lock, Java source/repositories, transaction services, migration scripts, React API + test clients, Postman/Newman and Playwright tests, .github/workflows/verify.yml, scripts/package-extended-c.ps1, scripts/verify-extended-c1.ps1, scripts/verify-extended-c-recovery.ps1, all reported warnings. Recheck course assignment/syllabus/sample code for any compatibility change considered.

## Objective

Demonstrate that the approved product's data, APIs and UI survive adversarial failures, that validation is not accidentally conditional on ordered/mocked tests, and that measurable improvements can accelerate development, CI, queries and storage **without removing evidence or functionality**. This phase is a reliability/performance optimization pass; no new product features, public deployment, money, live messages or user-owned database migration.

## Sequential work contract

1. Capture baseline source hashes, exact package tool versions, CI run and test roster. Build source coverage and affected-file closure; identify which existing C1IdentityAcceptanceTest methods share state via PER_CLASS and which can be independently executed without hidden preconditions.
2. Define and implement independent disposable database fixtures or verified resets per high-risk case; preserve existing full suite and negative tests. Evidence must show at least two critical cases run isolated and in reversed order without relying on another test to create data. A mock cannot establish MySQL correctness.
3. Establish **mathematical stock invariants** from actual current code/schema:
   - total_physical >= 0 and bounded by validated numeric limits;
   - for each resource, ledger-derived total equals saved physical total;
   - approved holds and outstanding reusable custody are counted exactly once;
   - availability = physical total minus approved held quantity minus issued reusable custody, adjusted by exact domain state semantics; if negative or inconsistent fail closed;
   - movement and audit histories cannot be updated/deleted by runtime grants;
   - receipt replay does not duplicate movement or alter total;
   - issued reusable custody does not automatically disappear on expiry;
   - tenant-scoped foreign keys prevent cross-org references.
   If real code uses different subtleties, document and reconcile with C6/C8 contract before editing; do not impose an invented formula.
4. Execute concurrent and failure-injection scenarios in real MySQL 8/packaged Java 21: two approvals of last available quantity; two issues; simultaneous return and expiry; backdated or ended project; lost HTTP response before commit and after commit; duplicate operation ID same/different payload/actor; idempotency HTTP receipt pruning with permanent movement fallback; server restart between claim and response; injected exception between quantity and movement; rollback after DB deadlock; tx retry; migration refusal against legacy nonzero stock/plaintext contact; key loss; scheduler dispatch/consent race; interrupted reminder claim becomes UNKNOWN; no unauthorized actual message.
5. Test validation boundaries: null/missing/oversized fields, malformed UUID/JSON, Unicode/normalized duplicate names, integer overflow, timezone/date end boundary, page cursor forgery, inactive membership, foreign ID, stale If-Match, missing/expired Idempotency-Key, unknown routes, PDF/polyglot/oversized image payloads and actual PNG/JPEG decoder refusal.
6. Test all declared browser workflows with packaged source, navigation and restart, desktop/tablet/mobile, keyboard/reduced motion/screen-reader semantics as feasible, role denials, stale/unavailable resources, loading/empty/error/retry/partial/uncertain-write. Verify no new operation ID is created on retry of uncertain stock mutation.
7. Profile baseline before modifying code: 3 repetition cold/warm Maven verify, npm ci and build, Playwright, JAR launch, SQL query/transaction latency and EXPLAIN on representative indexed tenant queries, Java heap/CPU under declared fictional fixture, JAR/client bundle size, MySQL table/index bytes, movement rows and encrypted dump size. Record OS/tool versions and variance, no fabricated speedup claims.
8. Select only evidence-backed improvements: SQL predicate/index tuning, elimination of N+1, cursor keyset, bounded batches, duplicate compiled artifact work, cache keys from lockfiles/POM/JDK, faster fixture setup, narrower L0 feedback and controlled CI parallelism. Never trade integrity or cold build provenance for speed; ensure no hidden cache artifact substitutes for the clean verification step.
9. Restrict code changes to measured hot paths, assigned tests and performance scripts. Record algorithmic complexity before/after with data size and index plan. New Flyway migration requires unique sequence and dedicated schema owner; old migration checksums remain intact. Do not truncate ledger/audit/backup records to shrink storage. Retention policy applies only to explicitly expirable data and must prove no loss of durable replay.
10. Integrate disjoint workers: database/invariants agent (highest reasoning); CI/build agent (medium/high); frontend bundle/UX reviewer (medium/high); independent security/false-positive reviewer (highest reasoning). One writer per file/branch, no conflicting package-lock/POM edits. The critic receives actual source and tests, not implementation assurances.
11. Rerun ALL required branch gates on the final source SHA: Course A Java 8/MySQL + bundled executable, Extended Java 21 real MySQL including source/test suite, 28 amended academic operations via authenticated Newman, React typecheck/lint/unit/build, packaged Chromium e2e, grants/migrations/rollback and source hashes. Report skipped/failed checks and runner logs precisely.
12. Compare before/after metrics with equal fixture/env. Revert any optimization whose benefit is below observed noise or which degrades functional/correctness/accessibility/recovery gates. Independent reviewer must challenge false positives, test order dependence and unobserved workloads.

## Exact evidence outputs

Write evidence/C9/PHASE_REPORT.md, PER_FILE_SOURCE_AUDIT.csv, TEST_ISOLATION_MATRIX.csv, FAILURE_INJECTION_MATRIX.csv, STOCK_LEDGER_EQUATIONS.md, CONCURRENCY_RECEIPTS.md, SQL_QUERY_PLANS.md, BUILD_SPEED_BASELINE_AND_DELTA.csv, STORAGE_BASELINE_AND_DELTA.csv, CI_GATE_RECEIPTS.md, SECURITY_PRIVACY_REVIEW.md, INDEPENDENT_CHALLENGE.md, DEFECT_DECISIONS.md and C10_EXACT_HANDOFF.md. Each report row requires exact file/hash/range, command, OS/toolchain, fixture, expected and observed HTTP/database state, exit code, date, run SHA and verdict. Token/model costs only if tool actually exposes them; else NOT_AVAILABLE.

## C9 exit criteria

No unresolved P0/P1 transactional/security/integrity failures; independent test isolation sufficient to disprove suite-order false positives; all mandatory clean CI/integration/browser tests execute and pass at final SHA; no course/migration alterations; measured performance/storage improvement changes only retained when supported; independent reviewer has attempted at least one substantive counterexample per high-risk domain. If any required gate is not executable, mark BLOCKED/NOT_RUN and stop. No C10 or deployment without separate owner authorization.

Always include: "Do not reduce audit depth or acceptance criteria to satisfy time/token limits; complete all safe possible work and mark anything blocked or not run."

---

# PROMPT C10 — Minimum viable secured staging, independent off-host recovery and operations

## Entry and permitted actions

Run only after explicit approval for C10 **and separate approval for any hosting or spending**. C9 pass and public release scope DEC-01–DEC-11 must be formally resolved. Refresh current HEAD/source SHA, branch/worktree/PRs, verify immutable A and V1–V5, inspect all deployment-related source and logs. New stage must remain isolated from the course release, shared/user databases and real personal data. If owner has not approved public staging, perform complete read-only setup plans and loopback test recipes only; mark actual hosting/network/independent-machine gates BLOCKED or NOT_RUN.

Read fully AGENTS.md, QUALITY_EVIDENCE_CONTINUOUS_GATE.md, all packet files 00–06, source C5–C9 reports/decisions, docs/EXTENDED_C_RELEASE.md, deployment/security/product source, actual database grants, restore scripts, backup limits, cookie/security filters, current relevant host/operator primary-source docs and course evidence.

## Hosting and operations design

1. Select minimum viable topology for the approved real-user scope: one Java 21 process serving bundled React and Jersey, separately secured MySQL 8, standard HTTPS termination, encrypted backup storage with independent recovery keys, restricted inbound/outbound network and a named operator. Prefer one instance unless concurrency/availability measurements demand more. Document hosting location, domain, data region, cost, expected load, service limits and alternatives; do not guess provider promises.
2. Define environment: APP_HOST/APP_PORT and trusted proxy boundary, migration/runtime DB URLs and distinct least-privileged principals, secure random bootstrap credentials, protected contact encryption key/key ID, protected backup key, SMS default DISABLED, time zone policies, log retention, operator secrets store and approved incident contact. No passwords in Git, CI logs, release ZIP or browser screenshots.
3. Analyze actual server.address=127.0.0.1, __Host- cookie, Secure/HttpOnly/SameSite, C1SameOriginFilter request.getScheme/serverPort/serverName behavior behind reverse proxy. Implement the smallest explicit safe proxy configuration; never trust user-supplied X-Forwarded headers unless only injected by authenticated boundary. Test HTTPS, redirects, forced HTTP, wrong origin, forged Host/proxy headers, CSRF, session rotation/expiry, cross-tenant data access. No c1-test HTTP cookies on public ingress.
4. Define migration deployment order: encrypted verified pre-upgrade snapshot, quiesce writes, external migration identity only, Flyway validation/checksums, maintenance/rollback plan, runtime least-privilege check (audit_event and stock_movement INSERT-only modifications), health check and controlled availability restoration. Rehearse on disposable staging, not production data.
5. Design health endpoint and synthetic monitor without leaking secrets or database details; logs distinguish denied requests, true unknown reminder outcomes, transaction retry and integrity failure. Configure alert thresholds from measured baseline. Produce incident runbook for DB unavailable, disk full, migration partial failure, contact key unavailable, backup restore failure, TLS expiry, corrupted image and account compromise.
6. Design backup from current script: explicit max 64 MiB raw SQL is an existing technical limit, not production capacity assurance. Benchmark actual dump size growth and choose fail-closed behavior when exceeding bound; if approved production workload demands larger data, design streaming authenticated encryption/chunked verified restores without weakening ciphertext integrity and snapshot consistency. Never load unbounded SQL in process memory.
7. Perform **off-host** recovery: produce encrypted backup from approved fictional dataset, move archive through protected independent storage, restore on separately provisioned machine/service MySQL and independent Java instance using protected keys. Do not use same host/two ports as substitute. Verify clean empty target precondition, Flyway V1–Vn history, all expected tables/constraints, append-only grants, passwords and memberships, session-loss policy, Resource image SHA, immutable stock movements, ledger total/holds/issued balance, idempotency replay after receipt pruning, project boundaries, encrypted contact key IDs and failed-key refusals. Capture source+target hashes and dates without secrets.
8. Adversarial restore: flipped ciphertext, missing/wrong key, partial SQL import, interrupted MySQL restore, missing contact key, stale/old operator account, target existing tables, SQL privilege denial. Show target isolation and safe retry from new empty schema, no alteration to original workspace. Define actual RTO/RPO and measure restoration; if owner's RTO/RPO is unspecified, status remains OPEN, never claim an objective met.
9. Independently prove a new operator can bootstrap clean staging from source/JAR/runbook: separate migration/runtime account, no local developer secrets, login/role/tenant denial, authenticated browser journeys and restart, backup and restore. Explain required private credential handoff without including values in report.
10. If live Twilio is separately approved, first obtain consent and lawful use policy, provider account/test resources, expense authorization and Philippine sender restrictions from actual dated provider documentation. Create explicit opt-in live path tests and outcome semantics (provider acceptance is not handset delivery). If not approved, keep SMS disabled and test no provider request.
11. Security reviewer independently tries public exposure, header forgery, invalid origin, stale sessions, secret logging, backup unauthorized access, restore tamper and alert failure. Rerun full C9 functionality and Course A tree parity on deployment source SHA before any public release claim.

## Documents and acceptance

Create evidence/C10/: TOPOLOGY_AND_RISK.md, ENVIRONMENT_AND_NETWORK_CONTRACT.md, SECURE_PROXY_AND_COOKIE_TESTS.md, DB_GRANT_AND_MIGRATION_RUNBOOK.md, BACKUP_KEY_AND_RETENTION_POLICY.md, OFF_HOST_RESTORE_RECEIPTS.md, RPO_RTO_MEASUREMENTS.csv, MONITORING_INCIDENT_RUNBOOK.md, FIRST_TIME_OPERATOR_WALKTHROUGH.md, PER_FILE_AUDIT.csv, THREAT_REVIEW.md, CI_AND_COURSE_FREEZE.md, PHASE_REPORT.md, C11_EXACT_HANDOFF.md. Record exact host class without sensitive address, tool versions, hash, commands and actual outcomes. Mark hosting and off-host NOT_RUN if unavailable.

C10 passes only after independently reproducible authorized staging through actual intended HTTPS origin, healthy monitored app, defensive negative auth/CSRF/tenant tests, separated runtime/migration permissions, successful off-host restore with independently verified logical integrity, agreed measurable recovery objective met, no high-risk open issue, and owner-approved live service scope. Default label without these is BLOCKED; no public release by implication. Stop before C11 and require explicit phase authorization.

Always include: "Do not reduce audit depth or acceptance criteria to satisfy time/token limits; complete all safe possible work and mark anything blocked or not run."

---

# PROMPT C11 — Independent final verification, production decision, source/binary release freeze

## Authorization and source freeze

Run only after explicit owner authorization for C11. Check reviewed C10 implementation SHA and C9/C10 evidence, Git remote, branch ancestry, current PRs, no dirty changes, source tags and hashes; preserve Course Final A exactly as at 7f8c0746da18fd54b47f430ac1435ac36e605d67, preserve Flyway V1–V5 checksums and every previously approved migration. Base final release review on the **exact deployed/tested** artifact and current source commit, never on a report-only HEAD with different content. No unapproved code changes; discovered defects require a corrective PR and full rerun of affected + global gates.

Read full AGENTS.md and QUALITY_EVIDENCE_CONTINUOUS_GATE.md; 00–06 packet; all accepted phase reports C6–C10 and independent reviews; original course assignment/syllabus and class examples; current Canvas/student checklist; source, migrations, API and UI; CI logs; current dependency audit against exact lockfiles; artifact release and restore records. Every claim must cite real source/evidence with exact SHA or class/page/file locator.

## Final red-team and traceability sequence

1. Freeze explicit launch contract: supported roles, organizations, workflows, domain states, endpoints, exact data schema and retention, backup/recovery goals, supported client platforms and accessibility, security/privacy commitments, hosting and messaging boundaries. Compare documented promises with actual implementation. Any unavailable promised feature is a BLOCKER unless owner formally re-scopes and updates docs.
2. Generate final exhaustive scoped source register: active files and transitive closure reviewed FULL or RANGES; all changed PR paths with line-level semantic review, unchanged high-risk dependencies inspected; archives/binaries listed with hash and relevance; unattached video screen content not claimed. Preserve course authority/conflict register and every accepted decision.
3. Independently reconstruct every academic Course A requirement and 26 business+2 image as-built contract; verify protected A SHA/trees and Java 8 build/skeleton/final requirements, Jersey/Spring/JPA/Javax/MySQL/Retrofit/Gson/scheduling/GET/POST/Postman, course UI and image bytes. Distinguish course Java 8 lane from Extended Java 21 and optional product features. Student-only Canvas/recording/authorship obligations remain NOT_VERIFIED until student supplies proof.
4. Independently reconstruct Extended identity/tenant permissions and complete route matrix; test anonymous, every role, wrong/suspended tenant, demotion, membership swap, expired/replayed invites, credential recovery, CSRF/origin/sessions, public proxy TLS/cookie behavior and data minimization.
5. Independently reconstruct stock invariant proof: physical total, holds, issued reusable custody, permanent movements, replay receipts, safe deletion/history, consumed stock, aborted transaction, concurrent writer, scheduler expiry and contact consent. Assert SQL rows before/after mutation and no double event. Intentionally test one malicious malformed request per handler class.
6. Exercise all approved product user journeys from packaged deployed origin using actual browser: desktop/tablet/mobile, keyboard/reduced motion, loading/error/partial/stale/uncertain-write, image correctness, role controls, login/organization/recovery/stock workflow and restart. Include role and accessibility audit with independent review.
7. Re-run clean source extraction and build with exact toolchain: Course A Java 8/MySQL, Extended Java 21/MySQL, React typecheck/lint/test/build, Newman 28 Extended academic operations, full Playwright and negative security tests; verify zero skips for required tests and inspect false-positive risks/CI shell exit handling. Compare release JAR and source ZIP manifest hashes and migrations. If deterministic binary output is unproven, report it as such; source provenance must still be verified.
8. Verify dependency advisory audit with dated source; no unresolved unacceptable high risk. Test actual staging HTTPS and monitor alerts if public release promised. Test rollback from deployment candidate and independently reproduced off-host restore with source/target checksums and known key custody. Verify agreed RTO/RPO; no real paid/provider traffic without recorded owner authorization.
9. Perform independent second-pass critique by a separate strongest-capable reviewer not involved in implementation. Critic should challenge scope omissions, role misclassification, seeded data realism, module integration, data loss, migration backward-compatibility, perf/storage metrics, runtime secrets, false-green CI and operator usability. Reproduce P0/P1 counterexample where safe; require fix/retest.
10. Present owner with go/no-go matrix with separate ACADEMIC A, EXTENDED LOCAL C, STAGING and PUBLIC PRODUCTION columns, each status VERIFIED / CONDITIONAL / BLOCKED / NOT_RUN. Human approval for deployment, merges and Canvas remains separate. Prepare exact next release/corrective instructions and rollback references.

## Deliverables

Under evidence/C11/: SOURCE_AND_CHANGE_COVERAGE.csv, FINAL_COURSE_COMPLIANCE.csv, FINAL_EXTENDED_CONTRACT.csv, FINAL_ROLE_TENANT_MATRIX.csv, DATA_INVARIANTS_AND_NEGATIVE_TESTS.md, API_POSTMAN_BROWSER_RECEIPTS.md, SECURITY_PRIVACY_SUPPLYCHAIN.md, CLEAN_BUILD_CI_REPORT.md, BUNDLE_STORAGE_PERFORMANCE.md, OFF_HOST_RESTORE_AND_ROLLBACK.md, INDEPENDENT_RED_TEAM_REPORT.md, SOURCE_BINARY_HASH_MANIFEST.md, FINAL_RISK_AND_DEFER_REGISTER.md, STUDENT_ONLY_ACADEMIC_CHECKLIST.md, PRODUCTION_GO_NO_GO.md, PHASE_REPORT.md, and if rejected, CODEX_CORRECTIVE_HANDOFF.md.

Every deliverable must provide source path/ref/range, criterion, actual observed command/tool/exit code, expected and actual outcome, owner, severity, remediation, missing evidence and dated link. Avoid anonymous 'passed all checks' claims and internal reasoning or fabricated subagent evidence. Unexecuted checks must say NOT_RUN, missing public infrastructure BLOCKED, unresolved optional design accepted only as DEFERRED_WITH_APPROVAL.

## Final decision rule

Release can be described as VERIFIED for a **bounded approved launch scope** only if every mandatory requirement has independent runtime/source/CI evidence on the final SHA; all P0 are resolved, all P1 resolved or explicitly risk accepted where appropriate; course/frozen A unmodified; security/privacy/tenant/ledger and release artifact gates pass; actual public TLS/infrastructure and off-host restore observed if public production claimed; runbook and rollback proven; owner explicitly authorizes deployment/merge. Any violation yields BLOCKED or CONDITIONAL with named limits and no release action.

Do not promote a local candidate to production merely because CI is green. Do not independently deploy, merge, push to main, create paid resources, send real SMS or submit to Canvas. Stop after report and owner decision request.

Always include: "Do not reduce audit depth or acceptance criteria to satisfy time/token limits; complete all safe possible work and mark anything blocked or not run."
