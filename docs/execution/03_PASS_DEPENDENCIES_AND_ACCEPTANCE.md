# OrgResource — C6–C11 optimized pass dependency graph, acceptance and risk ledger

Edition: 2026-11-19. Proposed sequencing, **not completed work or a fixed claim that exactly six passes suffice**. The next authorized phase begins only when owner approvals and the QA gate allow it. Current source reference: C5 report 9755ef63bd4c, packaged code 14576b57fff4, proposed QA policy PR #19 (29efe8629843). Refresh all SHAs immediately before execution.

## 0. Why six grouped passes

Grouping by cohesive architecture boundary reduces rework and integration time while retaining atomic acceptance. We group (C6) audit/contracts, (C7) identity/security/privacy, (C8) domain/API/UI vertical workflows, (C9) reliability/performance/CI, (C10) deployment/recovery, and (C11) independent final freeze. Each phase has independent risk gates and one reviewable PR; a large phase must be split into sub-PRs when high-risk changes cease to be independently testable. The grouping is an initial plan, never authority to compress or skip evidence. If C6 finds new P0/P1s, revise boundaries; do not hide them.

### Dependency and concurrency map

C6 source/contract audit (read-only; every relevant source path registered)
    → owner resolves release-scope/contract decisions
    → C7 auth/security/privacy guarantees
    → C8 product-complete workflows using C7 roles and transaction contracts
    → C9 failure/load/build/storage engineering against stable C8 workflows
    → C10 staging public-network engineering + independent off-host restore
    → C11 adversarial acceptance + release dossier + owner go/no-go

C7 and C8 may have read-only design spikes in parallel; their **mutations** are sequential when sharing identity/schema/API contract. C9 profiling baselines may begin read-only during C6–C8. C10 infrastructure decisions may be researched independently, but public provisioning, paid services and live SMS require separate permission. C11 release sign-off cannot precede all mandatory gates.

### Scope tracks

- **A / course lane:** frozen Course A remains unchanged; independently verify all assignment-mandated course behavior and as-built amendments. Academic final student checklist separate.
- **C / Extended local product:** secure fictional local workflow. C7–C9 can be completed without hosting.
- **P / public production:** C10–C11 require explicit public-product scope, hosting/privacy/operator decisions and authorization; cannot be labeled passed solely by local C5.

## C6 — Evidence-first comprehensive audit, exact acceptance baseline and decisions

**Scope:** Read-only audit + documentation; no feature implementation, code redesign, migration, release or new dependency. Fresh Git remote, 19 previously open PRs (recheck total), branch ancestry, file trees, all active source paths and material transitive dependencies, freeze checks, course assignment/syllabus/course-code authority, all 26 business + 2 image amended operations, v2 public routes, UI workflows, migration versions V1–V5, tests, CI scripts, storage/build baselines.

**Parallel:** source/course lane, backend/database lane, frontend lane, CI/ops lane; independent critic after outputs. Each source audited by actual path/hash and FULL/RANGES/PATCH/METADATA classification. A file tree inventory is metadata only; don't claim all text read.

**Outputs:** COVERAGE.csv, CONFLICTS.md, COURSE_MATRIX.csv, ROLE_ENDPOINT_TENANT.csv, DOMAIN_STATE_AND_MIGRATION.md, TRACEABILITY.csv, P0_P1_P2.md, EFFICIENCY_BASELINE.csv, RED_TEAM_REVIEW.md, DECISION_LOG.md, C6_PHASE_REPORT.md and exact proposed C7 code contracts. Use course-source page/line references where available, never retrofit unsupported course requirements.

**Exit:** Every relevant file is inventoried with reason and inspection level; all active/high-risk paths and transitive closure reviewed to defensible depth; all route/state/role requirements traced or marked open; independent reviewer completed; no unacknowledged material conflict; working build baseline and source hashes recorded or explicitly NOT_RUN. Owner signs the release-scope decisions before C7. C6 is BLOCKED if essential review coverage is unavailable.

## C7 — Secure tenant identity, authorization and privacy foundation

**Scope:** One coherent auth and governance security boundary: path/session/organization policy, roles (OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER), multi-org selection, suspended/demoted user behavior, foreign IDs, invitation/recovery, CSRF/same-origin, rate-limit strategy suitable for the chosen deployment, privacy retention/encryption/key lifecycle, independent threat model and dependency remediation decision. Integrate role semantics with current academic and v2 routes without modifying A.

**Contract gates:** exact role×route matrix; 401 vs 403 vs safe 404; current-account/membership check; session absolute 12h and idle 30m behavior as implemented; rotating session/CSRF on organization selection; generic login and recovery; bounded invite expiry; least-privilege migration/runtime grants. Any change to these values must be an explicit USER DECISION and independently tested.

**Evidence:** actual MySQL and HTTP tests for wrong origin, CSRF, suspended/demoted membership, cross-tenant IDs, owner transfer race, concurrent recovery, invalid/expired invitation, zero-live-SMS, secrets omitted, restart/session expiry. Review reverse-proxy origin assumptions as design until hosted. Per-file audit, cold/warm build and dependency audit.

**Exit:** no P0/P1 auth, cross-tenant or privacy finding open; restricted runtime cannot change stock/audit history; update route/UI contracts agree; exact SHA has independent critical review and CI. If deployment is not authorized, public network gates remain NOT_RUN.

## C8 — Core real-user workflow and API/UI domain completeness

**Scope:** Implement *approved* coherent vertical slices under Extended without modifying Course A. Resolve whether borrowers are authenticated members or officer-entered labels, whether LEAD has assigned-project access, which v2 CRUD operations and history/export/archival functions are launch-mandatory. Avoid inventing Ateneo integration or actual institution workflows. Maintain five academic business families and Extended support storage.

**Vertical-slice order:** (1) borrower/custody identity and role contract if approved; (2) project/requirement/allocation authorization and lifecycle; (3) resource stock receive/adjust/issue/return/consume; (4) bounded history pagination/audit visibility and image retirement; (5) end-to-end screen usability and accessible states. May group (2)+(3) in one PR **only** if one shared transaction/API acceptance suite covers all state changes.

**Invariants:** total quantity >= 0; available derived from physical totals, current approved holds, and outstanding reusable custody; pending never reserves; approval reserves once; issue transfers hold to custody, return releases custody once; consumable issue/consumption semantics follow the exact implemented state machine and recorded decisions; expiry releases eligible holds while issued custody remains; immutable movement/operation receipt prevents replayed stock mutation. Never mask negative stock or synthesize migration history.

**Evidence:** real MySQL concurrent approvals/issues/returns, rollback and retry-after-lost-response, tenant FK enforcement, full route contract/Newman, React desktop/tablet/mobile, keyboard/reduced-motion, role-denied controls and server denials, loading/error/empty/stale/partial/uncertain states, organization switch and restart, exact image bytes. Independent reviewer challenges workflow and invariants.

**Exit:** every **declared release** workflow has exact contract and positive/negative/restart proof; no orphaned UI or endpoint; full historic cursor traversal if mandated; user-approved optional deferrals listed with owner and consequence. Full v2 surface is not automatically necessary unless included in approved release contract; cannot promise it then defer it silently.

## C9 — Adversarial correctness, quantified performance and build/storage engineering

**Scope:** Rationalize test independence and false-positive risks; add safety/performance regression tests; measure actual bottlenecks; optimize queries/algorithms, CI critical path, artifact size and data retention without violating integrity. Preserve C7/C8 behavior. Do not add infrastructure to improve unmeasured speed.

**Target cases:** same-resource dual writers; stale ETags; idempotency key with changed body; receipt pruning/replay; database deadlock and transaction rollback; scheduler race with consent revocation; interrupted provider claim unknown; malformed input/oversized images; pagination cursor edges; MySQL upgrade/restart and injected failures; thousands-of-rows bounded list/query checks at fixture scale agreed in advance; network lost-before/after-commit; browser reload and backend restart.

**Metrics:** exact JDK/Node/MySQL/npm, OS/runner, SHA, cold/warm Maven/npm/Playwright times, CI wall clock critical path, test pass/skips, p50/p95 SQL/HTTP timings under defined fictional fixture, index/rows-examined, artifact and DB bytes, backup SQL size, tokens used if exposed. Compare actual candidate with baseline; report error bars/measurement limitation. Avoid made-up '99% faster' claims.

**Exit:** all required independent tests pass on exact SHA; real SQL plans support chosen indexes; no confirmed high-risk concurrent/corrupt/duplicate behavior; no material degradation; all optimizations retain clean JAR source provenance; final full gate rerun after last optimization.

## C10 — First-run staging deployment, trusted network and off-host disaster recovery

**Scope:** Only after specific owner authorization, implement minimal one-instance deployment/staging with HTTPS, secure cookies, same-origin and proxy invariants, restricted DB and secrets, monitoring/logging/alert routing, health checks, backup rotation, archive/key storage separation, migration rollback and independent off-host recovery. Include real operational capacity/privacy decisions. No public exposure of trial environment without owner approval.

**Gate:** clean schema bootstrap from private credentials, no test profile on internet, correct secure cookie __Host- semantics, proxy trusted only when explicitly configured; actual 401/403/CSRF/origin and tenant tests through intended HTTPS host; committed+uncommitted crash recovery, realistic DB data size; backup ciphertext tamper/wrong key/refusal, failed partial restore isolated to disposable target, re-grant append-only permissions; restored stock balances, movements, replay, contacts/images, membership/session treatment; independent machine/storage and operator.

**Exit:** authorized staging is operable by a separate person using documented steps; off-host recovery has proven an agreed RPO/RTO and key custodian procedure; no secrets in logs/repo/archives; failures and rollback tested; monitoring detects injected outage; hosted boundaries are recorded, with live SMS kept disabled unless separately authorized and validated.

## C11 — Independent release audit, complete evidence and owner go/no-go

**Scope:** Freeze feature set, confirm exact SHA, rerun clean-source full suite and high-risk adversarial scenarios, independent security/privacy/course compatibility critique, validate packages/hashes/runbooks/restore, verify browser access and a realistic fictional end-to-end user journey, plus final release decisions. No new features without opening a new corrective gated cycle.

**Hard requirements:** no P0; no unaccepted P1; no mandatory NOT_RUN; all course frozen trees unchanged; no old migration checksum changes; dependencies assessed and security findings accepted only by documented owner risk decision; full role/tenant/route matrix; real database and browser evidence; verified public HTTPS and recovery if public release claimed; output exact source/binary hashes and reproducible commands; owner approval to merge/deploy (which does not itself follow from this packet).

**Exit:** bounded FINAL_VERIFIED for the approved public release contract only, or CONDITIONAL/BLOCKED with evidence. Release artifacts must be available to owner and rollback demonstrable. Academic submission remains student-only with current Canvas checks and truthful Certificate of Authorship/outside assistance.

## Decision register — required before affected implementation

ID | Classification | Decision needed | Conservative default and impact
--- | --- | --- | ---
DEC-01 | USER DECISION | Is first launch local pilot/demo or externally reachable public product? | Local fictional demo; public P0 remains BLOCKED
DEC-02 | USER DECISION | Borrower identity: managed accounts vs officer-entered label? | Label only in academic demo, no inference of real custody identity
DEC-03 | USER DECISION | Exact LEAD project assignment/visibility semantics? | Preserve existing restrictive verified roles; defer expanded assignment-based feature
DEC-04 | USER DECISION | Which v2 CRUD/history/audit/export features mandatory for first release? | Current supported workflows only; never advertise unbuilt features
DEC-05 | USER DECISION | Hosting/provider/budget/region and named operator? | No deployment or paid expense
DEC-06 | USER DECISION | Data retention, contact encryption key custodians and backup RPO/RTO? | Fictional data only; no public rollout
DEC-07 | USER DECISION | Live SMS and real contact collection authorized? | Disabled, fictitious contacts only
DEC-08 | OPEN QUESTION | Picomatch and other actual current lockfile advisories and compatible fixes? | Public release blocked pending dated audit and reviewed disposition
DEC-09 | COURSE REQUIREMENT / OPEN QUESTION | Current Canvas final instructions, instructor's CLO9/Twilio interpretation? | Student confirms; preserve frozen evidence and exact course examples
DEC-10 | OPEN QUESTION | Supported loaded legacy DB migration behavior (nonzero stock/plaintext contact)? | Fail closed; do not manufacture ledger or contact history
DEC-11 | USER DECISION | Required performance/backup size targets and realistic projected volumes? | Baseline measurements first; no ungrounded thresholds
DEC-12 | USER DECISION | Repository branch/PR integration, who authorizes each phase? | Draft branches, no merge; each implementation phase separately approved

## Contingency cuts

Cut in this order if schedule or risk constrains work: new analytics → new exports → broad v2 symmetry beyond committed workflows → cosmetic transitions → live SMS → public hosted deployment. **Never** cut frozen academic behavior, role/tenant enforcement, safe stock math, critical validation, source integrity, independent security review, mandatory tests, backup honesty or student academic obligations. An optional cut is real only after changing the declared release scope and explicit owner acceptance; never redefine failures away.

## Risks

R-01 Unreviewed transitive paths → incomplete audit → C6 source register + independent critic.
R-02 Parallel file overlap → lost changes/invalid integration → worktree ownership, path lock, one integration writer.
R-03 Agent model hallucination → fake version/passes → actual capability discovery, primary source read, observed command receipts.
R-04 Huge pass → over-budget context and unverifiable result → split at vertical transaction boundary with complete continuation state.
R-05 Unbounded archive/DB growth → restore/test delays → measurements and retention plan, no ledger deletion.
R-06 Security dependency vulnerability → public risk → dated exact lockfile audit and remediation tests.
R-07 Public proxy mismatch → invalid cookie/CSRF or bypass → real staging host and denial tests.
R-08 Stale backup key → unrecoverable contacts → key inventory, independent off-host drill.
R-09 Course conflict → academic noncompliance → frozen A isolation, course evidence matrix and Canvas student check.
R-10 Research-to-product overclaim → institutional/personal data risk → fictional dataset and clearly labeled assumptions.

## Per-pass approval checklist

- Exact source/PR SHA and course A freeze verified.
- AGENTS.md and full continuous gate read by coordinator and every agent.
- Every changed file and high-risk dependency listed with review depth.
- Course requirement / user decision / design / assumption labels correct.
- Data/role/tenant and API contract precise.
- Risk-driven negative/concurrency/crash/privacy tests specified and executed where mandatory.
- Independent critic records counterexamples and residual uncertainty.
- Full clean gates with actual command receipts and CI SHAs.
- Token/build/storage profiling only where measured; cannot replace acceptance.
- Owner sees P0/P1/P2 and approves next phase. No automatic merge, public deployment, paid resources, live contacts, real SMS, or academic submission.
