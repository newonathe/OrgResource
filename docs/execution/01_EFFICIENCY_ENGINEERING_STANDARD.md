# OrgResource — Efficiency engineering standard without quality tradeoffs

Status: DESIGN RECOMMENDATION, not measured implementation. This document supplements AGENTS.md and the **entire** QUALITY_EVIDENCE_CONTINUOUS_GATE.md. The gate wins whenever a technique conflicts with evidence, source authority, privacy, correctness or academic compliance.

## 1. Optimization objective and decision algorithm

We want high-quality deliverables per review cycle, short safe iteration time, low token cost, fast and repeatable builds, appropriate storage, and maintainable algorithms. Because these objectives compete, use constrained multi-objective selection rather than promising simultaneous absolute minima.

Hard constraints H: frozen Course A and original materials untouched; immutable earlier migrations; security boundaries; real MySQL source of truth; every mandatory test preserved; complete source coverage register; independently evidenced behavior; no unauthorized actions; no hidden failure. Reject any proposal violating H.

Among H-compliant proposals, optimize **measured** (a) elapsed wall-clock to accepted vertical slice, (b) total agent input+output tokens and rework, (c) critical-path CI duration including setup, (d) packaged runtime bytes and backed-up SQL bytes, (e) query/transaction latency, and (f) complexity/maintenance cost. Report baseline versus candidate; never multiply unmeasured estimates into a fake "efficiency score." Pareto-dominated candidates are rejected. Where benefits conflict, use risk-based priority and explicit owner decision.

### Engineering selection rule

1. Enumerate acceptance criteria and risks **before** selecting optimizations.
2. Profile current source and behavior; identify the bottleneck and measurement environment.
3. Compare the smallest viable change, more ambitious change and no-change baseline.
4. Choose changes with clear evidence and low change surface first; no speculative large rewrite.
5. Rerun affected functional, denial, data-integrity and compatibility tests.
6. Compare cold/warm build and query/storage measurements; roll back optimizations that violate H or have no convincing value.
7. Capture file-level evidence and independent objections in the phase report.

## 2. Token efficiency protocol

### A. Maintain canonical context, avoid narrative duplication

Use the **current** immutable per-phase context packet: exact refs, authority list, changed paths and their hashes, contract IDs, test IDs, open decisions, failed assertions, evidence URLs. Pass links plus focused source slices and *unmodified exact high-risk files* to the relevant subagent. Do not paste unrelated full historical archives into every worker. Preserve complete original documents in Git; a concise index is a locator, not a substitute for mandatory reading.

Coverage register fields: source_path | ref/blob_sha | size/lines | relevance | FULL / RANGES / PATCH / METADATA / UNREAD / INACCESSIBLE | reviewed ranges | reviewer | date | dependent interfaces | confidence/risks | next action. A worker must open files it is assigned to inspect, including relevant unchanged callers, SQL and tests.

### B. Retrieval policy

1. Enumerate tree and changed paths once per exact SHA; cache path/hash inventory in pass evidence. Reuse prior content **only when SHA unchanged and its past review level is sufficient**.
2. Retrieve path-specific relevant code and transitive dependencies; review complete high-risk functions/contracts, not just grep matches. Use diff-first for low-risk repeat inspections; FULL review for new or safety-critical components.
3. Keep a fixed instruction header and compact, structured task packet. Include full linked QA gate as mandatory source reading; do not inaccurately describe a summary as compliance with its entire text.
4. Use schemas, tables and test identifiers for machine-checkable status. Avoid repeated speculative agent brainstorming after decisions are locked.
5. Escalate only genuine blockers or contradictions. Batch independent design choices into one decision log and continue unrelated safe work.

### C. Model and reasoning-effort routing

**Binding refined default:** See docs/execution/12_TOKEN_BUDGETED_SUBAGENT_ROUTING.md, 13_PHASE_SPECIFIC_SUBAGENT_PROMPTS.md and .codex/config.toml. Default = no subagent; when justified, gpt-6-luna/low project default with precise scope. Actual named roles: Luna low/medium map/source, Terra medium API/UI/ops, Sol medium ledger/auth/reviewer, Sol high only exceptional serious unresolved risk or C11 mandatory independent release veto. Two concurrent and default four distinct sessions per phase INCLUDING reviewer. A model not available in current Codex runtime blocks spawning until verified low-cost substitute; no silent expensive parent inheritance. This section below gives generic risk guidance, and is **superseded for exact dispatch by 12/13**. Never lower QA.


Select **actual available** Codex models at run time, never assert an unverified model SKU exists. Classify work:
- Low-risk formatting/manifest/index updates: fastest available capable model, low/medium reasoning; deterministic scripts preferred over LLM.
- File graph mapping, routine API/UI implementation after exact contract: medium/high reasoning, one specialist owning disjoint files.
- Transaction safety, cross-tenant auth, privacy, crypto, migration, recovery, concurrency: strongest available code/reasoning model at high or extra-high effort, independent second reviewer.
- Course interpretation, architectural contradiction, threat analysis, release verdict: high or extra-high, independent critic without implementation-assistant conclusions.
- Reproducing commands, hashes, diffs, tests, dependency inventories: tooling/scripts, **not** a token-heavy agent.

Upgrade model only when ambiguity, risk, complexity or prior error demonstrates necessity. Downshift when repetitive work is fully specified and assertions can be tested. Do not route high-risk decisions to low reasoning just to minimize tokens.

### D. Parallelism

Parallelize **disjoint read-only audits** and independent test design. Allow at most TWO concurrent subagents total, with four distinct session invocations per phase by default; implementation owners must have disjoint paths, compatible DTO/migration contracts and independent branches/worktrees. A single integration owner owns interfaces and integration tests. A single migration owner serializes Flyway files and schema changes. A single reviewer independently challenges work after integration. More agents may increase throughput only when merge/review/token overhead is measured lower than serial cost.

### E. Context cut-off and retry

Every agent receives bounded scope plus required full gate; if scope exceeds context/tool budget, return an exact continuation manifest with last reviewed path/ranges, pending items and no false PASS. Split by dependency boundary, not arbitrary token count. Failed tests receive one reproduction plus focused debugging; retain causal evidence instead of repeatedly re-running entire suites without change.

## 3. Build-speed protocol

### Test lanes (the full gate remains mandatory before phase acceptance)

**L0, fast local deterministic:** format/diff integrity; static typing; isolated unit cases for changed code; SQL/parser/lint as applicable. Useful as feedback, **never phase acceptance**.

**L1, affected integration:** MySQL transaction, role/tenant, HTTP, migration, browser contract; run on disposable loopback environment. Affected critical transitive dependencies are included.

**L2, full branch gate:** clean Java 8 Course A lane, clean Java 21/MySQL 8 Extended suite, 28 Extended academic operations, React typecheck/lint/unit/build, packaged Playwright journeys, dependency audit and negative security cases. Required by QA gate for phase acceptance.

**L3, release-grade:** isolated clean extraction/source hashes, full real integration and independent negative/recovery/deployment tests, staging network path and off-host restore once those phases are authorized. L3 does not substitute for missing user-only academic actions.

Run L0 and L1 in a targeted inner loop; run L2 at commit/PR phase gate and after any risk-bearing correction; L3 before release. Do not skip L2/L3 to meet a perceived speed target. Use cache keys from lockfiles/POM/JDK/Node/toolchain and cache **dependencies**, never stale build outputs/evidence. Enforce clean builds where provenance matters.

### Cold/warm measurements

Record machine/runner CPU, RAM, OS, JDK, Node/npm, MySQL, network/cache hit, commit SHA, commands and 3 repetitions when practical. Capture p50 and range for: npm ci, typecheck, tests, Vite build, Maven verify, MySQL startup/migrate, Newman, Playwright, package extraction and full CI critical path. Distinguish cold installation from warm incremental loop and GitHub concurrent runner wall clock. If samples not collected, mark NOT_RUN.

### Intelligent optimization candidates

- Maven incremental local compilation and dependency caching; final clean Maven verify still mandatory.
- npm ci from committed lockfile; audited dependency cache; build static frontend once per exact source SHA and verify it is the JAR-bundled output.
- Scripted reproducible fixture provisioning and bounded test data; minimize manual setup while preserving independent test isolation.
- Run read-only jobs in parallel; serialize MySQL mutations and migrations with explicit test environments.
- Control expensive Playwright repeated setup via isolated fixtures where safe, while preserving negative tests and clean end-to-end pass.
- On failure, rerun precisely failed job to debug; re-run full required branch gate after correction.
- Prefer a single Java/Spring application and one database over new services/queues/caches unless a measured bottleneck justifies added operations risk.

## 4. Storage and runtime efficiency protocol

Measure; never make fixed limits without workload evidence. Report both logical and physical bytes, row counts, media bytes, binary/JAR/client bundle bytes, archive bytes, MySQL data and index footprints, post-cleanup retention, and encrypted backup overhead. Include expected growth scenarios as ASSUMPTIONS until instrumented.

- **Data integrity:** canonical stock movement ledger stays append-only; use indexed queries and grouped availability with EXPLAIN ANALYZE, tenant predicates, keyset cursor when appropriate; avoid N+1 and unbounded SELECT. Do not delete movement/audit facts for superficial storage gains.
- **Retention:** define policy for session rows, short-term idempotency HTTP receipts, outstanding claims, logs, exported downloads, invitations and temporary artifacts. Preserve permanent movement identity/replay semantics. Purge only explicitly expirable classes with tests and audit.
- **Media:** keep bounded validated PNG/JPEG, actual decoded image checks and safe headers; evaluate image and backup growth. Compression or thumbnail options require explicit binary fidelity and course image compatibility decisions.
- **Indexes:** inspect actual EXPLAIN plans for tenant/list/history queries and write amplification. Avoid adding indexes without workload/plan evidence; index all relevant foreign/scoping columns as justified.
- **Build artifacts:** exclude node_modules, target, dist, temporary SQL, secrets and screenshots from Git source unless evidence policy explicitly requires small reviewed fixture images. Keep manifests, hashes and reproducible build instructions. Store large test artifacts in bounded retention CI artifact storage.
- **Backups:** snapshot complete intended records, preserve contact-envelope key IDs and Flyway history, validate restore, separately store keys. Do not treat compression or bounded 64 MiB backup implementation as general-scale readiness.
- **Complexity:** prefer explicit transactional, bounded algorithmic complexity over micro-optimized opaque code; record complexity with resource count R, allocation count A, movement count M, row page size k. Paged queries ideally scale to page and index seeks, not full-table per-row loops.

## 5. Required measurements and falsification

Measurement ID | Baseline | Candidate | Instrument | Test/acceptance | Decision
--- | --- | --- | --- | --- | ---
EFF-TOK | tokens/retries per accepted requirement | same | tool/agent usage reports | all mandatory gates unchanged | KEEP/FIX/REVERT
EFF-BUILD | cold/warm wall clock and CI critical path | same | timestamps/logs | clean provenance and complete tests | KEEP/FIX/REVERT
EFF-STOR | JAR/bundle/source ZIP, DB/index, backup bytes | same | file hashes, MySQL size queries | no data loss / no missing history | KEEP/FIX/REVERT
EFF-DB | p50/p95 query and mutation latency, rows examined | same | real MySQL EXPLAIN and timed HTTP | exact tenants/rows/invariants | KEEP/FIX/REVERT
EFF-UX | browser load and core task completion | same | Playwright/network traces | keyboard/mobile/error/retry equal or better | KEEP/FIX/REVERT
EFF-REVIEW | review rework, PR conflict time | same | phase report | mandatory second review retained | KEEP/FIX/REVERT

Do not write percentage speedups without comparable samples; baseline absent = NOT_RUN. Declare threshold decisions before benchmarking. A performance regression that breaks an acceptance check blocks the phase. Use complexity/cost analysis only where relevant to the implemented workload.

## 6. Things that are expressly NOT optimizations

Do not: shrink source review to just changed lines while ignoring high-risk callers; claim filenames are semantic review; run mocks instead of MySQL; mute lint/audit errors; delete or weaken assertions; reorder tests to hide issues; delete audit records or Flyway history; rewrite Course Final A or class examples; launch one huge parallel PR; compress all requirements into an ambiguous one-sentence prompt; defer P0/P1 defects silently; overengineer microservices/Redis/queues with no measured need; copy unverified AI output as proof; force major dependency upgrades without regression evidence.

**Stop condition:** If an optimization's behavior cannot be measured, if source evidence is insufficient, or if test reproducibility is compromised, record as NOT_RUN/BLOCKED and preserve the known-correct baseline.
