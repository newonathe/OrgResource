# OrgResource — independent evidence and falsifiable acceptance for model routing and multi-agent decisions

**Research date:** 2026-11-19 PHT. This is an evidence register and a plan for authorized evaluation, not performance measurements of OrgResource.

## 1. Primary research relevant to GOAT claims

**PREPRINT / NOT independently reproduced:** Bai et al., *How Do AI Agents Spend Your Money? Analyzing and Predicting Token Consumption in Agentic Coding Tasks* (2026), arXiv:2604.22750, https://arxiv.org/abs/2604.22750. The paper studies observed agentic coding token consumption and reports substantial run-to-run variability, heavy input-token cost, and limited correlation between more spending and higher accuracy. This supports measuring *all turns and retries*, not just a one-message completion. It does NOT identify the optimal GPT-6 routing for OrgResource.

**PREPRINT / NOT independently reproduced:** Sarel Weinberger & Amir Hozez, *Same Task, Different Work: Prompt-Induced Waste in Coding Agents* (submitted 2026-08-02, revised 2026-08-06), arXiv:2608.01347, https://arxiv.org/abs/2608.01347. Reports that prompt wording and harness behavior can change time/tool/token expenditure without changing task success. This challenges simplistic requirements such as “think exhaustively in every step” because unbounded self-checking may add waste. It does NOT justify relaxing OrgResource’s mandatory independent safety/evidence tests.

**PREPRINT / NOT independently reproduced:** Han Li, HanHaoNing Li, Ziqian Jiang & Yiling Lou, *When Sub-Agents Work in Parallel: The Promises and Pitfalls of Dynamic Concurrency in Long-Horizon Coding Tasks* (2026), arXiv:2610.10263, https://arxiv.org/abs/2610.10263. Controlled comparison of multi-agent concurrency in coding tasks, with benefits and failures differing across agents, task horizons and decomposition. Its findings do NOT prove zero subagents is always best or that every parallel subagent is harmful; choose based on independent decomposition and measured overhead.

**WORKSHOP PAPER / NOT independently reproduced:** *Do Context Files Help Coding Agents? A Two-Agent Ablation Study on Real Repositories* (2026), https://aclanthology.org/2026.realm-1.5/. Reports limited benefit of generic repository context files in its evaluated agent/repository sample. It cautions against treating a huge AGENTS.md as automatic correctness. This does not prove context is useless for OrgResource; exact task contracts, source citations and executable tests remain necessary.

**UPSTREAM TOOL MAKER DIRECT EVIDENCE:** https://github.com/obra/superpowers/blob/22f0b463e7a5e579904dd6211a520321035526f7/docs/superpowers/specs/2026-07-30-codex-efficiency-fixes-design.md describes observed duplicate worker-initiated reviewer spawns, wait polling overhead and five evidence-targeted improvements. The upstream pinned `skills/executing-plans/SKILL.md` explicitly offers a cheaper Native path with one final reviewer. Vendor's historical experiments have task/provider limitations; they are not performance results of this repo.

**CREATOR'S HYPOTHESIS, NOT BENCHMARK:** Daniyar Kazi public caption 2026-10-04 https://imginn.com/p/DeFS0Fes8yu/ describes GOAT model+effort routing and its own simulated costs. The uploaded MP4 frames/audio remain NOT_RUN. See `17_GOAT_VIDEO_EVIDENCE_REGISTER_PENDING.md`.

**OFFICIAL CURRENT CONFIG DOCS:** https://learn.chatgpt.com/docs/agent-configuration/subagents and https://learn.chatgpt.com/docs/config-file/config-reference specify project-level model+effort and max concurrent threads. Current Codex support and actual resolved agent model require a runtime probe, especially on Windows/client versions; repo-local TOML is not execution evidence. API model prices, e.g. https://developers.openai.com/api/docs/pricing, do not necessarily match subscription Codex token usage or effective allowances.

## 2. What evidence presently justifies

- Adopt **bounded, source-complete contracts**, deterministic tooling, task-sliced test-first work and evidence-anchored review.
- Use an inexpensive **rule table** to select model AND effort for each task; do not pay a separate LLM router call unless deterministic rules are demonstrably insufficient.
- Default to Native inline execution for tightly coupled OrgResource feature slices; permit selective SDD for independently parallelizable and costly-to-get-wrong tasks after explicit choice.
- Compare model/router outcomes by test correctness and actual complete cost including retry/compaction/child contexts, not per-token price alone.
- Honor course original academic implementation and full Quality Evidence Gate regardless of model tier.

## 3. Falsifiable evaluation protocol (future owner-authorized and budget-limited)

Pre-register a representative **six-task** safe disposable test battery:
1. M1: Git hash, manifest alias/duplicate check (script oracle; model ideally not used).
2. M2: Course/source fact-to-exact-page matrix on a bounded 2026 roster subset (exact factual oracle, no inference of org inventory).
3. M3: React page validation change with fixed DTO and Playwright oracle (no real people).
4. M4: Java Jersey/JSON route change in Extended with real MySQL foreign-tenant/validation/rollback oracle.
5. M5: Inventory concurrency/idempotency test correction with stock movement SQL oracle and hidden defect.
6. M6: CI/cache or backup-size inefficiency with measured cold/warm build and integrity/restore oracle.

Use identical prior approved source SHA, task contracts, environment and acceptance for each compared route. Safety-sensitive testing is isolated to disposable loopback databases with no live Twilio or external hosting. Comparison conditions: A (inline one standard model), B (deterministic router model+effort), C (selective SDD for independently parallel task only). Historical high-agent logs may be analyzed if actually available, without pretending they were replicated; no forced high-cost A/B if user lacks budget.

Record each task run as:
`TASK_ID, BASE_SHA, MODEL_REQUESTED, MODEL_EFFECTIVE, EFFORT_REQUESTED, EFFORT_EFFECTIVE, ROOT_TOKENS, CHILD_TOKENS, CONTEXT_CACHE_TOKENS, RETRIES, TOOL_CALLS, WALL_SECONDS, ACCEPTED_ASSERTIONS, NEGATIVE_ASSERTIONS, P0_P1, SOURCE_REVIEW_COVERAGE, COURSE_FREEZE_SHA, RESULT`.

When Codex doesn't expose a field, record `NOT_AVAILABLE`, never infer. Three repetitions where time/budget permits, report median/range and independent oracle results; single runs are exploratory only, not a statistically established optimal route. Reject a candidate that has incomplete safety/quality gates, regardless of low token cost.

## 4. Bounded preflight/postflight gates

**Preflight:** actual Git ref and source; owner-approved phase; exact replayable test oracle; available model allowlist and reasoning efforts; course A frozen; tool permissions and approved budget; expected source sizes and no duplicate tasks. Task prompt must contain exact input+output+tests+permissions so a cheaper model is *capable*.

**Execution:** default one inline model, TDD and atomic testable changes, root evidence ledger; no nested agents; maximum concurrent children 2 and phase child sessions 4 when selective delegation is approved; no automatic high/max escalation; no prompt-induced repetitive broad self-critique or loop without new evidence.

**Postflight:** actual test receipts on current SHA, all requirements traced and source ranges recorded, independent reviewer examines one exact diff and critical dependencies, real whole-branch suite at L2/L3 where mandatory; inspect regression and build/storage metrics before any cost claim. A branch failing an essential gate cannot be promoted by selecting a more favorable denominator.

**Decision:** route only when it produces lower measured total cost per accepted compliant task without material correctness or wall-time regression. Record unresolved uncertainty and owner decisions. Do not claim “GOAT optimal” from the author’s illustrative cost figures.

## 5. Source and originality

This analysis summarizes attributed public works and source paths, does not import their executable artifacts. Prior OrgResource documents still require complete CSCI academic-source review before code changes. Video visuals and audio are not independently analyzed and remain a named blocker. This document does not itself run a model, spend an external token budget, generate Ateneo records, compile software or verify user entitlement.
