# OrgResource — Superpowers-adapted execution contract and copy-ready root Codex handoff

**Edition:** 2026-11-19 PHT. **Status:** DRAFT PLANNING for owner review; **no application implementation, Codex execution, plugin installation or hosted deployment** from this PR. Use with original complete docs/execution/04_CODEX_PROMPTS_C6_C8.md, 05_CODEX_PROMPTS_C9_C11.md, 08–11 Ateneo demo prompts, 12–13 explicit subagent contracts, [14 upstream source audit](14_SUPERPOWERS_SOURCE_AUDIT_AND_CONFLICTS.md) and [15 GOAT router policy](15_GOAT_MODEL_EFFORT_ROUTING_AND_COST_VALIDATION.md). Above all read root AGENTS.md and **the entire QUALITY_EVIDENCE_CONTINUOUS_GATE.md**. The academic CSCI 180.05 assignment, syllabus, supplied examples and frozen Java8 Course Final A outrank a third-party tool.

## 0. Design choice: NATIVE execution by default; no silent SDD

The user experienced token exhaustion with many high-model subagents. We therefore default to Superpowers-style `executing-plans`: root makes a complete plan, executes an approved bounded phase inline with test-first steps and a persistent ledger, then obtains **one independent final review of the integrated phase diff**, with extra *targeted* proof as required for identity, stock, privacy or release-risk. We do NOT automatically run `subagent-driven-development` (fresh implementer and fresh reviewer for every micro task). If the owner selects SDD for an unusually independent set of tasks, use full Superpowers skill through user-installed official plugin, strict OrgResource task router and budget cap.

**Implementation of this document is a recommendation, not an installed Superpowers runtime.** In Codex App/CLI the README at pinned obra/superpowers commit points to the official Codex plugin marketplace: in Codex `/plugins` → search **Superpowers** → install only by explicit user action. The main repository is not being forked or silently vendored. User must confirm exact installed plugin version; official marketplace may lag upstream 7.0.2. No guarantee that a branch instruction alone auto-triggers external skills.

**Per-phase authorization:** The existing C6–C11 grouped passes are retained. An approved phase contains smaller checkable tasks. Each task has exact precondition/postcondition and tests, but each task does **not** summon a new agent. New feature changes obey Java8 frozen source and Extended C Java21/MySQL8 distinct lanes. The 96-source-reference Ateneo dataset is explicitly synthetic operational data with real public organization names; no fake adoption.

## 1. Global technical state and authority to refresh before each pass

- Private GitHub: newonathe/OrgResource. Never assume older `main` is current implementation. Source of truth is owner-approved phase branch and exact Git SHA.
- Protected Course Final A: `7f8c0746da18fd54b47f430ac1435ac36e605d67` immutable, original Java8/Jersey/Spring/Javax JPA MySQL/Retrofit Gson/scheduler/React; 26 amended business + 2 image operations as-built. Current assignment/syllabus and Canvas student check determine academic obligations; class source files preserved.
- Extended C5 report head: `9755ef63bd4c02a7042cb48823bf759e609357cd`; exact C5 package/tested CODE SHA `14576b57fff44c6f0112fd37d8c237ca0122efb4`. Current C6+ must refresh approved ancestor and don't confuse report SHA with code artifact.
- AGENTS.md and proposed `QUALITY_EVIDENCE_CONTINUOUS_GATE.md` inherited from PR #19 (historical head `29efe86298431db16a780ae2e582b2276a699530`); current task code branch based on stacked docs PR #21. Policies are proposed in draft PRs, not GitHub-enforced or automatically merged.
- Course stack/version equivalence must be established by actual project ZIP/slides/source, not assuming modern 2026 mainstream defaults apply to Java8.
- Known missing public gates: authorized hosted URL/proxy/TLS, off-host restore, fully populated mock demo, real data ownership/privacy/operator, actual measured GOAT savings; C5 local reported evidence does not resolve them.

## 2. Mandatory plan container per phase

Before writing implementation or spawning:
1. **Name phase and approved release scope:** C6 read-only audit, C7 auth/privacy, C8 domain/API/UI/deterministic demo, C9 reliability/build/storage, C10 authorized staging/recovery, C11 independent release. State exclusions and owner decisions.
2. **Refresh exact source graph:** Git remote/branch/base/head/PR tree, clean/detached worktree, file/hash inventory, all changed paths, unchanged high-risk callers. Record SOURCE_COVERAGE FULL/RANGES/PATCH/METADATA/UNREAD/INACCESSIBLE.
3. **Read authorities fully:** AGENTS.md, quality gate, course original assignment/syllabus/examples, accepted previous phase contract, appropriate 04/05 and demo 08–11 prompts, the exact actual source and tests. Do not silently skip originals due to tokens.
4. **Produce short design/contract:** alternatives and risk, exact endpoints/methods/DTO fields/errors, roles/tenant, domain states and dates, MySQL column/FK/transaction/clock, UI states, deployment and security implications, named decisions/approval. Preserve prior Course A source and V1–V5 checksums.
5. **Map tasks by vertical functional dependency:** one bounded, testable slice per group, e.g. role enforcement + auth UI + denial tests; stock approve/issue/return + ledger/receipts + UI; full 96-demo importer + labeling/pagination + deterministic SQL. Inside each slice, write small RED/GREEN steps but do not independently delegate each one.
6. **Preflight DAG conflict scan:** for every two tasks sharing file/API/SQL, list producer→consumer interface, owner and serial sequence; any incomplete contract = STOP_CONTRACT_UNRESOLVED. SQL migration owner is unique.
7. **Select executor using zero-call router:** deterministic script, Native inline root, or scoped subagent if justified; explicit allowed model and reasoning, no automatic double-review. Validate capability in actual Codex.
8. **Write task acceptance:** exact test file, negative case, expected HTTP status and actual MySQL postcondition, lost-response/restart/rollback, role isolation, image bytes, Postman, accessibility, backup, frozen A SHA; status starts NOT_RUN.
9. **Budget checkpoint:** tokens if exposed, phase spawn count, wall-clock and retries; no rule may drop an acceptance gate. When exhausted: ledger and BLOCKED_BY_BUDGET, not invented DONE.

The resulting plan should have one source of truth. Its interface fields are not re-invented by each worker.

## 3. Continuous execution within ONE authorized phase (copy-ready root instructions)

**ROOT CODEX PROMPT — read and execute exactly the currently approved phase, no speculative next phase**

> You are implementing ONLY the specifically owner-approved OrgResource phase shown in current approved planning contract. First read root AGENTS.md and COMPLETE docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md; read 00 root index, 01–03 core method, the relevant 04 or 05 approved phase prompt, and relevant 08–11 Ateneo demo addendum. For this new routing design read 14–18 once, then pass a compact, exact accepted contract to any specialist. Read the current original course assignment/syllabus and code examples relevant to any changed behavior; fetch exact Git remote/base/head and actual sources/tests. Record inaccessible sources and STOP for an indispensable missing item. Treat Course Final A 7f8c0746da18fd54b47f430ac1435ac36e605d67 and old Flyway migrations as immutable. No next phase, merges, paid hosting, live SMS, real people, shared schema or Canvas without separate explicit authorization.
>
> Execute using **NATIVE inline** test-first implementation by default. Do not summon an implementer/reviewer for each tiny plan step. Use one reviewable, bounded coherent vertical slice at a time, with exact file/DTO/role/SQL/error/timeline contract, affected callers, test-first RED observed for correct reason, minimal GREEN and refactor. Maintain a source/hash and task/commit/test ledger identified by plan path. For every high-risk change test actual Java21/MySQL8/HTTP/React with negative tenant/auth/ledger/rollback/clock/idempotency cases, and preserve and independently test Java8 Course A academic semantics and as-built 28 operations. Use only deterministic scripted generation for all synthetic demo rows, never an agent per row.
>
> For any source or task requiring an external model, run the zero-extra-call deterministic model+effort route in data/config/orgresource-evidence-router-policy-2026.json and 15_GOAT_MODEL_EFFORT_ROUTING_AND_COST_VALIDATION.md. Require fully specified inputs, named exact model+effort from an actual available Codex allowlist, one writer per file, no nested agents and at most 2 concurrent/4 distinct phase sessions including review. Do not silently inherit a high parent model or assume plugin installation.
>
> Before acceptance run all mandatory full branch gate tests on the final exact SHA and inspect commands/output/exits; create source coverage + requirement→code→test→evidence matrix, plan-scoped ledger, observed performance/storage, independent critical whole-branch review of exact BASE→HEAD diff and high-risk unchanged callers. If critic finds P0/P1, correct and rerun gates; if repeated fix loop reaches agreed cap, return BLOCKED with exact repro and next owner decision. Classify every claim VERIFIED/REPORTED_ONLY/SOURCE_ONLY/TEST_FAILED/NOT_RUN/BLOCKED/DEFERRED_WITH_APPROVAL. STOP at the approved phase owner gate with a complete next Codex handoff; no autonomous release.

This is a reusable root-level prompt only after a specific owner-authorized phase/contract is supplied. A downstream worker must get a concrete filled dispatch envelope, never infer missing names/methods from the generic text.

## 4. Proven TDD/diagnosis/review order

For each implementation behavior:
1. **Reproduce current state** with source citation and baseline test. For a bug, first identify root cause via stack/SQL/log and compare a real working pattern.
2. **Write test** for the exact new state or bug correction. Test must run against meaningful code and use real MySQL8 where persistence/concurrency matters.
3. **RED:** run test and observe expected failure for the intended behavior; typo/compilation failure is not a valid RED. Record command, exit code and relevant output.
4. **GREEN:** minimal authorized change, no speculative architecture or unrelated rewrite. Re-run test; record actual status, DB after-state.
5. **REFACTOR:** improve names/duplication/performance without behavior change; rerun relevant test.
6. **Anti-duplicate:** never add a new actor/receipt/stock movement on retry when response status uncertain; use durable idempotency logic and original operation ID.
7. **Pre-task commit/review source base:** save BASE SHA; upon group integration make task review package with actual `git log BASE..HEAD`, `git diff --stat BASE..HEAD`, full contextual diff; verify the diff includes all commits (never assume HEAD~1).
8. **Review:** root self-review at each task, fresh independent critic for integrated coherent slice; targeted extra review on P0/P1 risk when needed. Critic tests specification correctness **and** code quality, no prejudged exceptions.
9. **Final L2/L3:** clean Java8/MySQL, Java21/MySQL, 28 academic endpoints, React type/lint/unit/build, packaged Chromium, security and recovery test matrix appropriate to phase. Unknown/unavailable remains NOT_RUN/BLOCKED, even if focused tests pass.
10. **Ledger before continuation:** Task ID, plan path, base/head SHA, exact files/ranges, test IDs/results, remaining defects and observed environment; compaction resumes first incomplete task.

**Plan ledger location recommendation** (adjust to approved repo conventions): use Git-ignored `.superpowers/sdd/<unique-plan-id>/progress.md` for working notes, and commit concise evidence/phase output only upon approved phase report. Do not run upstream helper scripts until source reviewed and worktree isolated; they can be installed by the official plugin after user's explicit action. Don't delete existing worktrees or overwrite another plan's ledger.

## 5. Phase-by-phase implementation packets

### C6 — Source audit and correctness baseline (READ ONLY)

Inputs: all relevant original course ZIP/assignment/syllabus/transcripts as applicable, 26+2 operation mapping, C5 current branches and code, public 2026 COA-M/LIONS references and 96-company tier config. Native root deterministic Git inventory and source conflict reconciliation, optional Luna/medium fact specialist if actual cost benefit, one Sol/medium independent source/security/ledger critique. Required outputs: actual full path coverage, course compliance, current routes/DTO/roles/states/SQL, exact independent test receipts or NOT_RUN, public source/rights distinction, phase risk register and approved C7 contract. **No C7 code.**

### C7 — Security and identity (one coherent backend/React/SQL authorization boundary)

Inputs: accepted C6 auth role×route matrix, current C1 security/identity source, sessions/recovery/cookies/CSRF/tenant predicates and real MySQL. Root Native or single Sol/medium auth specialist with Terra/medium UI only if files disjoint and DTO fixed; one independent review after integration. RED real MySQL role/tenant/foreign ID/demotion/replay/CSRF/host cases; finish green full L2. No site exposure; public TLS remains NOT_RUN until C10. Deliver secure role gate and exact C8 handoff.

### C8 — End-to-end approved product and deterministic Ateneo showcase

Group approved slices by transaction boundary: entity/requirement/project, allocation approval/custody, inventory ledger/receipt, full-history pagination/audit, 96 public org reference / heavy **fictional** MISA+Sanggunian demo import and source truth notice. One migration owner, contract-fixed API and UI. Real MySQL transaction and independent audit: total/held/issued/available conservation, state timeline, duplicate retry, cross-org ID, consent/expiry. Public sourced names + 2025-12 through 2026-11 fictional use are **always explicitly labeled**; no actual school adoption claim. Generate rows via deterministic algorithm, not language model sessions. Tiny fixture for fast inner loop, full 96 fixture for final acceptance if approved. Progress ledger and independent complete integrated review; no hosting before C10.

### C9 — Adversarial reliability, algorithms, storage and CI

Evidence-driven profiling before edits. Root/Terra/medium CI or source query tuning with Sol/medium only if real SQL defect. Compare 3 run cold/warm builds and fixture with exact environment; review index plans/keyset pagination, 64 MiB backup bound, artifact/DB bytes, no N+1 or unbounded reads. RED/GREEN for bug; repeat race, crash, lost-response, restore tamper and test-order isolation. Full phase L2 cannot be shortened because L0 is fast. Independent review of any optimization's correctness and measured gains.

### C10 — Authorized HTTPS staging and independent off-host recovery

Requires user separately approves provider/cost/public staging. Native root/Terra-medium ops, risk-targeted Sol-medium restore only if truly second independent host/storage exists. Set TLS/proxy trust/cookie/CSRF restrictions, separate Flyway migration and DML accounts, secrets/backup encryption, operator runbook, monitoring/alerts and rollback. Demo public preview is read-only/isolated, visibly synthetic; no actual organization accounts, personal contact or live SMS. Perform **actual off-host** encrypted restoration and rehearse wrong key/tamper, safe empty target, stock and images. Record observed RPO/RTO. If access absent mark NOT_RUN, no production claim.

### C11 — Independent final bounded acceptance and owner decision

Exact tested source/binary SHA, final Course A immutability and original academic obligations independently reviewed, full product route/tenant/stock/React, source claims, demo truth, HTTPS/restore and CI receipts. One Sol-high independent reviewer for the complete branch, not dozens of parallel critics. Return separate verdicts for ACADEMIC A / EXTENDED LOCAL / STAGING / PUBLIC DEMO; P0/P1 unresolved blocks applicable target. Owner controls merge/deploy/Canvas; STOP with handoff.

## 6. Copy-ready subagent dispatcher when exceptions are justified

> TASK_ID: [filled from approved phase]
> ROLE AND MODEL: [actual chosen role, verified model and reasoning effort]
> OWNER APPROVAL AND SHA: [exact Git values]
> MISSION: [one coherent set of tightly related functions and one acceptance artifact]
> READ FIRST: complete AGENTS.md and QA gate; actual course authority; specific source path/hash/ranges; accepted preceding contract and phase prompt.
> ALLOWED WRITES: [exact files]; FORBIDDEN: [exact source and other workers' files, Course A, Flyway V1–V5, personal data, shared DB].
> CONTRACT: [each method/path, fields/types/validation, role×tenant×state, SQL movement equation, concurrency/clock, errors, UI states and idempotency].
> WORK: [ordered RED/expected failure→GREEN/refactor→positive and negative cases].
> ACCEPTANCE: [actual commands/test IDs/expected HTTP+DB state, full phase gate owned by root].
> RETURN: [source range audit, observed exit codes, blocker status, concise report path, no invented tokens].
> STOP: [end of this task, no new agents, no next phase or external action].
>
> **DO NOT dispatch a lower-cost model with unresolved bracket fields.** Populate exact values first; otherwise no worker spawn. More precise task inputs enable lower model/effort but cannot permit reduced source review or testing.

## 7. Decision and failure handling

- Unsupported model/plugin or no actual Codex tools → Native root, **not** simulated subagent outputs.
- Green CI on old SHA → REPORTED_ONLY historical, not acceptance for new source.
- Reviewer finds high-risk unchanged dependencies outside diff → inspect those source paths, unlike blindly treating diff-only review as complete.
- Five stalled correction attempts under upstream SDD → stop spending, record symptoms/root-cause evidence and **BLOCKED** for any unresolved mandatory P0/P1; no parking to declare release successful.
- No real off-host restore, actual hosted source data, or legally usable real person/inventory data → NOT_RUN/BLOCKED, no implied university deployment.
- Real video absent from direct frame review → keep video evidence NOT_RUN, no description of unobserved visuals or audio.
- Source-level disagreements between academic assignment and modern product improvements → preserve frozen academic lane, offer additive compatible Extended feature only with course tests and user decision.

## 8. Verification before changing source further

This document does not invoke tools by itself. The first authorized Codex task must confirm plugin installation/version if user opted in, effective project-level configuration, actual model allowlist, current Git SHA, real CI environment and comprehensive current source coverage. A source-only policy PR is VERIFIED only as a Git object, never as an executable tested system.

## Final audit companion (2026-11-19)

The complete bounded research/source verification and remaining C6 entry conditions are in [19_FINAL_SOURCE_AUDIT_AND_C6_ENTRY_GATE.md](19_FINAL_SOURCE_AUDIT_AND_C6_ENTRY_GATE.md). Corrected custom-agent reviewer sandbox inheritance, official Codex model/effort keys and true video metadata barriers are part of that audit. All implementation/user data/public hosting/CI-runtime completion claims remain subject to actual executed evidence on the exact code SHA, never resolved by this planning document.
