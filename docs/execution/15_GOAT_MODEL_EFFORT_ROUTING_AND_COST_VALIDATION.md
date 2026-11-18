# OrgResource — GOAT-style model+effort router, using evidence and no extra router agent

**Edition 2026-11-19. Status:** DESIGN RECOMMENDATION / USER DIRECTION to reduce wasted tokens without lowering quality. This file is a **decision specification**, not a running model router or a benchmark. Rule data: [data/config/orgresource-evidence-router-policy-2026.json](../../data/config/orgresource-evidence-router-policy-2026.json). Research record: [14_SUPERPOWERS_SOURCE_AUDIT_AND_CONFLICTS.md](14_SUPERPOWERS_SOURCE_AUDIT_AND_CONFLICTS.md). Read complete `AGENTS.md`, `QUALITY_EVIDENCE_CONTINUOUS_GATE.md`, docs/execution 00–14, current phase master prompts and exact original course source first. Underlying Codex model/effort TOML support must be checked at runtime.

## 1. Clarification of the public GOAT comparison

A public Daniyar Kazi / @donimas Instagram caption (2026-10-04, https://imginn.com/p/DeFS0Fes8yu/) describes:
- JUNIOR: one expensive high-effort model all tasks, reported **$5.10**;
- MIDDLE: choose model manually, high effort all tasks, **$0.75**;
- SENIOR: cheapest first, verify, escalate when needed, **$0.45**;
- GOAT: router chooses **both model and effort per task**, **$0.32**.

Its own qualifier is **“Simulated workload, real prices. Not a benchmark.”** There is no established per-task output quality/latency, user token plan, Codex model allowlist, repeat-run confidence, exact prompt/read tokens or failed-task costs. **Do not treat $0.32 as a tested achievable OrgResource cost or $5.10-to-$0.32 as proven relative improvement.** The uploaded MP4 is not visually/aurally inspected; do not attribute unobserved on-screen values, charts or timestamps to that video. Follow up when local video access works; a verified caption is not a video-frame review.

**Practical claim worth testing:** a *deterministic low-overhead decision table* can assign cheap capable models to well-specified low-risk tasks and stronger reasoning to high-risk/ambiguous tasks. Do not spend a model call to choose a model if a few rules suffice. The router is a task-selection policy; it is not a magical autonomous agent.

## 2. Non-negotiable optimization order

Hard constraints, in priority order:
1. Academic source authority, supplied class semantics and immutable Course Final A SHA 7f8c0746da18fd54b47f430ac1435ac36e605d67;
2. No unauthorized live persons, provider traffic, spending/hosting, shared DB or Canvas action;
3. Correct identity/roles/tenant separation, security/privacy, secret keys and backup integrity;
4. Transactional MySQL stock conservation, append-only movement/audit, durable idempotency and safe migrations;
5. Functional full user workflows, API/React/Postman/retrofit and accessibility;
6. Complete source audit, independent evidence and mandatory real positive/negative tests at each gate.

Only after these hard constraints are satisfied optimize *measured* total cost, wall-clock accepted feature turnaround, cold/warm build/CI, query p95 and artifact/DB/backup bytes. **If a cheaper model passes fewer tests or needs more correction turns, it is worse, even if its token price is lower.** Neither a low token bill nor a fast typecheck is proof of completeness.

## 3. Router decision procedure (zero extra LLM calls by default)

For each approved task, the root writes one dispatch row:

1. **Authority classification:** COURSE REQUIREMENT / USER DECISION / VERIFIED SOURCE / DESIGN RECOMMENDATION / ASSUMPTION / OPEN QUESTION. Unknown mandatory decisions STOP.
2. **Trust/risk class:** metadata/mechanical, source fact, routine API/UI/ops, security/tenant/stock/recovery, or architecture/course/release veto.
3. **Input sufficiency:** exact Git sha, owned files and transitive dependencies, contract fields/types/state/HTTP errors/SQL equation/Clock, expected negative tests/DB postcondition. If this is under-specified, root first completes the plan; an expensive model is not a substitute for an exact spec.
4. **Execution style:** script first when deterministic; otherwise **inline Native** (Superpowers `executing-plans`), with TDD and a progress ledger. Spawn a worker only for proven isolation/parallelism benefit. Never auto-spawn from a feature label.
5. **Model + reasoning effort:** choose the explicit row in the policy JSON and check the exact actual Codex spawn allowlist and supported effort; no unsupported model or silent parent-default high effort.
6. **Acceptance:** RED test must fail for correct reason, GREEN/REFACTOR, affected positive/negative tests, exact source/HTTP/SQL evidence; root runs full L2/L3 at phase acceptance and one independent integrated reviewer. P0/P1 or mandatory NOT_RUN blocks claim.
7. **Measure:** actual tokens and cost fields *only if exposed*, retries/tool calls, wall time, CI time, storage. Source PDF/ZIP reading and hashed review remain mandatory when relevant.
8. **Escalation:** first reproduce exact discrepancy and identify root cause. Upgrade only the specific decision/task class; do not rerun whole phase or spawn a review swarm. If agent or context limits reached, stop with an exact progress/coverage manifest.

Decision table:

| Task class | Preferred executor | Model, effort if a model is used | Why this route | Escalation |
| --- | --- | --- | --- | --- |
| Git tree, hash/CSV joins, enum counts, deterministic demo records | Scripts, zero model | N/A | Objective mechanical work | Script failure + one focused repair |
| Exact small bounded edit, fixed source mapping | Root inline or `org_map` | gpt-6-luna, **low** | Low consequence, direct assertions | Terra/medium only on observed nontrivial failure |
| Public primary-source/campus roster fact tracing | Root inline or `org_source` | gpt-6-luna, **medium** | Larger reading, straightforward categorical proof | Sol for material course/privacy ambiguity |
| Approved Java API/React/CI vertical slice | Root inline or targeted `org_api`/`org_ui`/`org_ops` | gpt-6.1-terra, **medium** | Fixed contracts, real tests | Sol medium for cross-cutting fault |
| Auth/tenant/ledger/Flyway/backup safety | Root inline plus risk-targeted `org_auth`/`org_ledger` | gpt-6.1-sol, **medium** | Higher-consequence state and concurrency | One Sol/high on reproduced difficult P0/P1 |
| Architecture/course conflict/independent C11 veto | Root review or **one** `org_critical` | gpt-6.1-sol, **high** | Genuine judgment, irreversibility | Owner decision, never autonomous swarm |

**Caution:** This is a **candidate** routing table, not proof a model can complete each row. Do not default all complexity to Luna low and then repeatedly retry; that can cost more. Low effort is appropriate only after the plan supplies exact decisive details. The user uses Codex rather than raw Responses API; API list price does **not** necessarily equal user's Codex token or subscription accounting.

## 4. Mode selection — adapt Superpowers, do not auto-copy its most expensive flow

### NATIVE / INLINE, preferred

- Root executes each cohesive subtask of an approved phase with test-first and a persistent ledger; **no implementer agent per task**.
- Before each bounded change, freeze the contract, filenames and expected failing assertion. Maintain current `BASE_SHA` for later actual diff review.
- Execute RED→GREEN→REFACTOR with real environment; run focused L0/L1 in inner loop; at phase end, run complete L2/L3 as required.
- One separate competent reviewer examines the **whole integrated slice/branch** with source/requirement matrix, BASE→HEAD diff and relevant high-risk callers; independent red-team cases are preserved. For extraordinary auth/ledger risks, conduct an additional targeted boundary review as QA requires.
- Never run ahead into the next C6–C11 implementation phase without owner authority. A fixed plan approved for one phase does not automatically approve public deployment, SMS or Course A edits.
- Review cost: one fresh reviewer plus its exact source package, not reviewers for every trivial file. This aligns with upstream `executing-plans` and user's prior agent-overuse concern.

### SUBAGENT-DRIVEN, strictly opt-in

- Choose only when task independence and clean context materially improve expected completion/review correctness enough to offset fresh context charges, or when a specific high-risk task truly needs independent per-task review.
- All child prompts set **model AND reasoning effort** explicitly, use the precise C6–C11 task bank, isolate context (`fork_turns: none` where supported), no recursive children. Small same-shape edits are batched.
- Parent maintains exact source/contract/ledger, one reviewer per coherent diff; max 2 concurrent, default max 4 distinct spawned sessions **per owner-approved phase**, including reviewer. If full SDD plan requires more than that limit, it is **not authorized under current OrgResource budget policy**; switch to Native or request explicit budget approval.
- Use Git-isolated worktrees and single writer per Flyway/file. Multi-agent parallelism must be proved genuinely independent; a big job is not necessarily parallelizable.

### More precise low-model prompts

Low model assignment receives a **more complete plan**, not a shorter vague instruction. Every task prompt states the accepted current exact files/SHAs and all relevant transitive paths, schema/DTO, state equation, roles, error classes, tests and actual expected postcondition, ownership, bounded output and stop conditions. That detail costs some input tokens but reduces expensive failed attempts. Avoid duplicating the full unneeded past corpus inside each child context: authoritative files remain readable with exact SHA, and a worker opens the mandatory full QA gate itself.

## 5. Practical tasks and expected routing

- **C6 course/source reconciliation:** deterministic file/ZIP/hash inventory in script; source contradictions Luna medium with root review; one independent Sol-medium risk reviewer at C6 gate. The original assignment and syllabus outrank third-party methods.
- **C7 permissions:** root/Terra UI changes only after Sol-medium backend verifies role/cookie/CSRF/tenant contract; one Sol-medium integrated review; Sol-high only if a reproducible security flaw needs expert judgment. No separate reviewer for each DTO.
- **C8 96-org demo:** use code to generate 21,060–56,260 **fictional** requests, **not** 56k language-model calls. Data transaction/identity code Sol-medium, API/UI Terra-medium as assigned; one integrated review of real MySQL/React source. All real organization identities from dated sources; Sanggunian student government remains distinct; no actual usage claim.
- **C9 performance:** deterministic cold/warm build and MySQL EXPLAIN first, then Terra-medium targeted script/CI optimization; Sol-medium only on concrete failing SQL/stock proof. Never optimize by deleting history, tests or full fixture verification.
- **C10 host/recovery:** separate authorization for hosting; Terra-medium release script with real HTTPS; Sol-medium independent off-host restore only if off-host environment exists. No hallucinated RPO/RTO.
- **C11 final:** root packages exact source/binary/test evidence; one independent Sol-high final release/academic critic. No per-component army of reviewers.

## 6. Experiment: test before claiming the GOAT route is best

At the first authorized suitable phase, pre-register **at least six comparable source-grounded task classes** (metadata, course source, React, Java API, stock transaction, CI), with fixture and acceptance tests. Use separate identical baseline branches/fixtures only if resource budget allows. Compare:
A. Previous ad hoc multi-agent/high-effort approach (historical logs only if accessible); B. root-native inline with fixed model; C. proposed rule-based router with role-specific choices; D. selective two-agent parallel only for truly disjoint paths. No test criterion may change between variants.

Collect actual model IDs, efforts, prompts, input/cached/output/reasoning tokens where exposed, retries, spawned sessions, **end-to-end acceptance wall time**, accepted work count, F0/P0/P1 defects, real MySQL/HTTP/Postman/React/CI gates and binary/DB bytes. Evaluate median and range (3 or more repetitions when feasible), whether test oracle catches injected defects and whether a smaller model silently misses a domain case. **If baseline logs are unavailable, run an honest prospective comparison or mark historical cost comparison NOT_AVAILABLE.** Without reproducible results, the current routing is a defensible recommendation, not a measured optimal solution.

No prompt-file count or claim of “absolute efficiency” establishes correctness. Stop and revert a cheaper choice when QA deteriorates or total retry/review/integration cost is higher. Do not auto-launch expensive evaluations without owner authorization and current token budget.

## 7. Native Codex configuration and limits

Current previous PR #21 project-scope `.codex/config.toml` uses `[agents] default_subagent_model = "gpt-6-luna", default_subagent_reasoning_effort = "low", max_concurrent_threads_per_session = 2`. The docs at https://learn.chatgpt.com/docs/agent-configuration/subagents and https://learn.chatgpt.com/docs/config-file/config-reference describe these settings, but they do not guarantee this user's Codex installation recognizes role names/model IDs or hard-cap lifetime token spend. Some client versions also differ in sandbox override behavior, so verify **effective** model+effort by a cheap read-only controlled probe, not by TOML intent alone.

When using Superpowers' subagent flow, its own Codex tools reference demands explicit model+effort on **every spawn**, clean context forks and no redundant worker reviewer. For efficiency and budget, choose Native by default and respect the role defaults only when real subagents are justified.

## 8. Hard STOP rules

Never: silently install third-party plugin; auto-start SDD on every task; recursively dispatch workers; use all agents because defined; present public creator cost figures as production benchmark; silently downgrade course/student privacy review; mutate frozen Course A or old Flyway; claim real Ateneo adoption from simulated history; omit real MySQL/browser/backup tests; pay/deploy/live SMS/merge without owner authority; falsify model cost or tool receipts.

When model unavailable, evidence incomplete, high-risk source unread, mandatory test failed, source contract conflicts, unexpected branch SHA or budget cap reached: **record exact blocker and current ledger/source coverage; return BLOCKED/NOT_RUN with next action**. Do not invent an expensive continuation, and do not claim a pass.
