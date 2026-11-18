# OrgResource — Token-budgeted subagent routing, exact models, reasoning levels and spawn limits

**Edition:** 2026-11-19. **Status:** OWNER REQUEST / DESIGN RECOMMENDATION; Codex runtime model selection and token spending have **not been exercised** in this documentation PR. This policy corrects the previous permissive default that allowed many high-reasoning agents to be spawned. **No evidence, quality, course compliance or test gate is reduced.** Read complete AGENTS.md, docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md, execution files 00–11, this file and 13_PHASE_SPECIFIC_SUBAGENT_PROMPTS.md, plus the relevant original course files, source code and phase master prompt.

## 1. Actual Codex configuration, what it does and what it cannot promise

The repository now contains a **project-scoped** `.codex/config.toml` (for trusted Codex projects) with:
```toml
[agents]
enabled = true
default_subagent_model = "gpt-6-luna"
default_subagent_reasoning_effort = "low"
max_concurrent_threads_per_session = 2
```
This gives any unspecified spawned Codex subagent a cheaper default and limits *concurrent open subagent threads* to two. It does **not** limit how many agents may be spawned in total, guarantee spend, prevent an explicit override by the orchestrator, or override higher-priority managed/account settings. **Therefore enforce additional written controls in Sections 2–7 and check effective runtime config before starting a phase.** The main/root model is not forcibly downgraded by this file. Project config may require the user to mark the project trusted in Codex.

**Model availability:** The model names below exist in OpenAI's public catalog as of the dated lookup, but this does **not** prove entitlement/availability in this user's specific Codex account. Query the models shown by the actual Codex runtime, or run a read-only negligible-cost probe, before first use. If requested model/effort is unsupported, **do not silently inherit an expensive parent model**: block spawning, record `MODEL_UNAVAILABLE`, select the least-expensive *actually available* model with comparable validated capability via an explicit task-specific model/effort override, and record that in the dispatch. If no suitable model exists, parent does the task serially or asks owner. Do not claim effective model config from a documentation file alone.

**External primary sources (looked up 2026-11-19):**
- https://learn.chatgpt.com/docs/agent-configuration/subagents — project `.codex/agents/*.toml` custom agents, model/effort and read-only role config, inheritance, concurrent agent behavior.
- https://learn.chatgpt.com/docs/config-file/config-reference — `agents.default_subagent_model`, `agents.default_subagent_reasoning_effort`, `agents.max_concurrent_threads_per_session` settings.
- https://developers.openai.com/api/docs/models/gpt-6-luna — GPT-6 Luna low-cost focused work.
- https://developers.openai.com/api/docs/models/gpt-6.1-terra — GPT-6.1 Terra precise scoped tasks.
- https://developers.openai.com/api/docs/models/gpt-6.1-sol — GPT-6.1 Sol stronger code/security analysis.
- https://developers.openai.com/api/docs/guides/model-selection — model-tier selection and empirical comparisons.
- https://developers.openai.com/api/docs/guides/responses-multi-agent — parallelism can raise token use and is unhelpful for serial mutable work. This API surface's behavior is distinct from local Codex role TOML; do not mix configuration semantics.

## 2. STRICT spawn algorithm: default ZERO

At the start of **every** phase, root must first construct a source inventory and work DAG without any subagents using deterministic Git/scripts. Then:
1. **Default = ZERO spawned agents**. If root can execute a bounded task directly at no greater expected cost, execute serially; the existence of a named agent file is never authorization to spawn.
2. Minimum evidence for a spawn: a named non-overlapping work package with precise inputs/outputs, contract frozen, path ownership, a reproducible falsification/acceptance check, and a cost-benefit explanation against root-only handling. A generic 'audit whole repo' or 'review everything' spawn is prohibited.
3. **Concurrency cap = TWO subagents**, regardless of model. **Distinct-worker count default cap = FOUR spawned sessions for an entire phase, inclusive of the independent reviewer.** A one-shot phase should normally use **ONE** worker and **ONE** later independent reviewer (two total). C8 can have three distinct writers for separate disjoint files, plus one reviewer, but do not use four just because budget permits it.
4. **No recursive or nested spawning.** Each subagent's instructions forbid recruiting helpers. Integration owner is the root (no duplicative integration agent); source-map/metadata should be deterministic scripts before even considering `org_map`.
5. **No swarm at startup:** first dispatch at most **ONE** worker. A second may start only after the root has verified independent paths, shared API/SQL contracts, and clear critical-path benefit. Never run two agents to analyze the same exact files from identical prompts in parallel merely to create output volume.
6. Read-only reviews run **after** work artifacts exist, as a distinct bounded phase and context. Use `org_review` Sol-medium for normal independent QA. Use `org_critical` Sol-high only for documented P0/P1, a hard course/architecture contradiction, failed mandatory security/stock proof, or the final C11 high-risk release veto. One expensive critic, not three. Do not default to `xhigh` or `max`.
7. **No automatic retry or escalation loop.** If an agent fails, first inspect the exact error, contract and evidence. Retry once with targeted correction and same effort if appropriate. For ambiguous high-risk failure, escalate **only the affected bounded subproblem** by one effort tier/model after a recorded rationale. Never rerun the entire phase with a stronger model without evidence.
8. **Mandatory acceptance survives budget exhaustion:** when the checkpoint triggers, return `BLOCKED_BY_BUDGET` with full completed/remaining review inventory, exact commands and test IDs, and stop. No claim of PASS for unrun work. Owner may allocate further budget or allow root to continue a later authorized pass.
9. **No autonomous continuation:** finishing C6 is not permission to start C7. PRs, source hashes and owner approvals are phase-specific; no auto-merge, network deployment, live SMS, user-data resets or paid actions.

## 3. Explicit role-to-model matrix (configured, conditional on runtime support)

| Custom role (file in `.codex/agents/`) | Exact initial model | Reasoning | Sandbox | Decision surface | When to invoke |
| --- | --- | --- | --- | --- | --- |
| `org_map` | `gpt-6-luna` | low | read-only | Metadata inventory/CSV if script fails | Optional, normally **no agent** |
| `org_source` | `gpt-6-luna` | medium | read-only | Source-grounded academic/public org fact inventory | Only when source interpretation benefits from language model |
| `org_api` | `gpt-6.1-terra` | medium | workspace-write | Assigned Extended API/DTO/services, **not** stock/crypto | After exact contract freeze |
| `org_ui` | `gpt-6.1-terra` | medium | workspace-write | Assigned React pages/client/accessibility | After API contract freeze |
| `org_ledger` | `gpt-6.1-sol` | medium | workspace-write | SQL, holds, movement, migration, replay | Precise invariant and safe branch defined |
| `org_auth` | `gpt-6.1-sol` | medium | workspace-write | Identity/tenancy/cookies/recovery | C7 authorized, high-risk owner |
| `org_ops` | `gpt-6.1-terra` | medium | workspace-write | Build/CI/backup/operator code | Targeted script and measured problem |
| `org_review` | `gpt-6.1-sol` | medium | read-only | Independent falsification of integrated changes | Required meaningful second-pass review |
| `org_critical` | `gpt-6.1-sol` | high | read-only | Exception-only serious P0/P1 or C11 veto | Requires `ESCALATION_REASON` or C11 |

**Reasoning controls are literal values**, not assumed equivalent to model capability or model-specific pricing. `org_ledger` and `org_auth` at Sol-medium require full explicit contracts, mathematical assertions and actual MySQL negative tests. If these fail to support safe decisions, use the expensive Sol-high reviewer on only that unresolved scope. Do not use high to compensate for an ambiguous prompt. A low-cost worker does not decide final security architecture or release.

## 4. Token and build spend protocol

Actual billed-token quotas, price tiers, Codex usage limits and per-session model availability are private/runtime-specific; these files cannot set hard token caps. The following are **owner-reviewable planning checkpoints**, not fake measured token consumption:
- **Class M0 (deterministic scripts):** model tokens **zero**. Git tree lists, hash/CSV joins, JSON/TOML syntax checks, distinct IDs, formula arithmetic, diff file lists and test command launch are scripts.
- **Class M1 (Luna low/medium):** start with an input/read budget target of ~12,000 model tokens and an output budget target of ~2,500. Pass complete authoritative QA sources via file reads **as required**, not fabricated summaries. When sources exceed this target, persist a concrete coverage checkpoint and continue through approved bounded work instead of skipping sources. No auto repeat/restart.
- **Class M2 (Terra medium):** initial target ~25,000 input/read + ~4,000 output tokens; a fixed contract with small file set. Stage long logs/artifacts as files and return links/compact receipt tables, not thousands of repeated log lines.
- **Class M3 (Sol medium):** initial target ~35,000 input/read + ~5,000 output tokens for one high-risk contract, with source/transaction breadth. No double-read for identical SHA with sufficient prior verified semantic review. Tight actual postcondition tests.
- **Class M4 (Sol high):** exceptional initial target ~45,000 input/read + ~6,000 output tokens; single issue or bounded C11 release slice, no broad exploratory redesign.
- These are **soft alert thresholds only**. Never truncate an essential audit, reduce semantic source inspection, remove required tests, or claim acceptance to fit a number. Only stop with a coverage-complete continuation manifest and explicit unrun tests if available budget really runs short. If actual usage unavailable report `NOT_AVAILABLE`, never manufacture metrics.
- Token efficiency is cost **per accepted feature and test evidence**, not smallest text output. A more detailed initial lower-model contract can be cheaper than repeated failed tasks. Optimize total initial+retry+integration+review consumption and critical-path time.
- The coordinator must record `WORKER_ID, ASSIGNED_MODEL, REQUESTED_EFFORT, EFFECTIVE_MODEL, EFFECTIVE_EFFORT, SOURCE_BYTES_OPENED, INPUT_TOKENS, OUTPUT_TOKENS, REASONING_TOKENS, RETRIES, TOOL_CALLS, DURATION, ACCEPTANCE_STATUS, REWORK_REASON` when exposed, or `NOT_AVAILABLE` otherwise; never infer from file byte size.

## 5. Prompt specificity scales inversely with model cost

**Lower model → more explicit task contract, smaller ambiguity surface.** Every spawn request, even an `org_map` or `org_ui` run, requires:
1. Exact phase, owner approval, Git repo and **current** base/head SHA and immutable A SHA.
2. A finite objective with a measurable end state and **one bounded output artifact**. For larger grouped passes, split into coherent DTO/transaction/UI vertical slices, not one request per trivial file.
3. Exact read path list and SHA/ranges or authoritative discovery rule; **exact writable path whitelist** and forbidden paths. Only one writer per file and migration version.
4. Full read-first rules: AGENTS.md, the **entire** QA gate, relevant original course material and full phase-specific master prompt; no summaries replacing these.
5. Contract table with exact method/path, DTO keys/types and validation, role×tenant×state, MySQL columns/indexes/transaction equation, idempotency key/retry/restart and errors, UI states and dates as relevant. Any unknown field must be `STOP_CONTRACT_UNRESOLVED`.
6. Ordered steps, calls/commands and expected **actual** result with independently verifiable source/HTTP/DB evidence. Do not hand-wave 'implement tests'.
7. Detailed negative/adversarial checks and preservation gates. Status labels for unexecuted commands; source citations with file+line/sha.
8. Permission boundaries: no unauthorized public deployment, paid resources, live provider, data imports, merge, Canvas, source rewrites or unrelated work.
9. Exact output path/schema with file SHA, range, changed methods, expected and observed tests, defects, residual, per-role cost.
10. Explicit finish/STOP condition; no next-pass work or nested agents.

**Reject generic prompts** such as 'make backend production-ready', 'audit all files', 'maximize optimization', 'do comprehensive pass with 4 reviewers' or 'fix everything'. Use the complete phase-specific prompt bank in `13_PHASE_SPECIFIC_SUBAGENT_PROMPTS.md` and fill its runtime fields from actual Git before dispatch. A brief root-to-agent instruction referencing a complete file is only acceptable when the agent is required to open and read that file itself and the exact runtime contract is supplied.

## 6. Phase spawn plans — this is an UPPER LIMIT, not a quota

| Phase | Sequential/default roles | MAX concurrent | Maximum distinct spawned agent sessions | Required quality veto |
| --- | --- | ---: | ---: | --- |
| C6 | `org_source` Luna medium; `org_review` Sol medium after source contract; deterministic inventory in root | 1 | 2 | Root reconciles course authority, full coverage; critic challenges gaps |
| C7 | `org_auth` Sol medium; `org_ui` Terra medium only if separate UI; `org_review` Sol medium after integration | 2 | 3 | Security/cross-tenant/real MySQL evidence; escalate one hard failure only |
| C8 | `org_api` Terra medium; `org_ledger` Sol medium if stock/migration; `org_ui` Terra medium after DTO freeze; `org_review` Sol medium | 2 | 4 | Complete core + seeded demo acceptance; independently prove data math |
| C9 | `org_ops` Terra medium; `org_ledger` Sol medium **only** for measured SQL defect; `org_review` Sol medium | 2 | 3 | Test independence, concurrency, storage/build measurements |
| C10 | `org_ops` Terra medium; `org_ledger` Sol medium if off-host DB restore work; `org_review` Sol medium | 2 | 3 | Security/proxy/off-host recovery; deployment requires separate approval |
| C11 | `org_source` Luna medium only for source re-verification, then `org_critical` Sol high independent veto | 1 | 2 | Final independently checked actual SHA/host/release; no 'green CI means ready' |

**Escalation exception:** one additional expert subagent or one higher reasoning tier **only** when a concrete unresolved P0/P1 after reproduction or failed requisite review makes the phase impossible without it, with owner-approved budget exception or documented preauthorized budget policy. Preserve current QA standard but do not summon a swarm. Root may perform corrective work itself.

**C8 demo work:** The planned 96-entry public roster, heavy MISA/Sanggunian **simulated** history and 21,060–56,260 fictional allocation target are a coherent domain/DB/UI slice, **not a justification for 96 subagents**. Run deterministic generators and real MySQL tests, not one LLM call per row, and do not infer university adoption. Any large-data importer requires separate operator authority and isolated demo-only schema.

## 7. Preflight, escalation, and accounting

Dispatch preflight fails if: missing exact current source SHA, QA not read, original-course separation undefined, unbounded task, no explicit allowed/forbidden writes, missing role/contract/test/schema, unsupported model/effort, current open agents exceed cap, nested-agent attempt, two writers for one file, unauthorized deployment/change, unavailable token account data treated as a number, or a high-risk task delegated to Luna merely to reduce cost.

Root should maintain `evidence/<PHASE>/AGENT_DISPATCH_LEDGER.csv` and `AGENT_EXECUTION_RECEIPTS.md` **only when that phase actually runs**. One row per agent: spawn reason, disjointness proof, exact prompt path/SHA, requested/effective model+effort, start/end, output manifest, claimed vs observed tests, token count if available, retry/escalation and parent gate. No code implementation, agent session, budget telemetry, quality pass or test execution is proven by these planning documents.

The independent critic should read exact source/test receipts and challenge at least one substantive counterexample per domain; do not pay for three reviewers to repeat the same green tests. If critic finds P0/P1, fix and rerun affected/global acceptance before owner review. Use the earlier `02_SUBAGENT_ORCHESTRATION.md` for output schema and the full quality gate for all requirements.

**Bottom line:** **zero agents by default, max two concurrent, usually one specialist and one sequential independent reviewer, concrete model+effort and complete phase-specific prompt every time, escalation only with evidence; quality and academic constraints unchanged.**
