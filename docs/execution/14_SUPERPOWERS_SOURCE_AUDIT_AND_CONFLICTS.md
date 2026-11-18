# OrgResource — independent source audit of obra/superpowers and its applicability

**Research date:** 2026-11-19 PHT. **Upstream repository:** https://github.com/obra/superpowers at exact Git commit **22f0b463e7a5e579904dd6211a520321035526f7**, plugin manifest version **7.0.2** (released 2026-11-03, recorded in its RELEASE-NOTES.md). MIT licensed, copyright (c) 2025 Jesse Vincent. Sources are third-party research, NOT course authority, NOT an installed plugin, NOT a benchmark of OrgResource, and not a completed application feature.

**Authoritative OrgResource boundaries:** current CSCI 180.05 assignment and syllabus; supplied original class examples; protected Course Final A **7f8c0746da18fd54b47f430ac1435ac36e605d67**; the entire repository AGENTS.md and docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md; explicit user decisions; accepted current Extended source. Superpowers contributes optional *methods*, never a replacement for academic requirements, independent source/evidence review, privacy, MySQL, or owner authority.

## 1. Source inventory and review coverage

The upstream Git tree at the stated SHA is **236 blobs** and the Git recursive tree was returned as untruncated. This is a **metadata inventory**, not full semantic review of all 236. Relevant selected files were fetched from exact GitHub contents. SOURCE_REVIEW_LEVEL means what this pass inspected; `RELEVANT_RANGES` means entire contents were retrieved but only the exposed/relevant portions were analyzed; it is not a claim of exhaustive line-by-line understanding.

| Exact upstream path(s) at SHA above | Source review | What material facts it established |
| --- | --- | --- |
| README.md; .codex-plugin/plugin.json; LICENSE | RELEVANT_RANGES / FULL manifest+license | Official Codex plugin install path, skill workflow, source license; current plugin version and no actual user installation proof |
| RELEASE-NOTES.md | RELEVANT_RANGES | v7.0.2 date, native execution and SDD cost/compaction improvements, documented methodology changes; historical vendor eval numbers are vendor evidence, NOT OrgResource benchmarks |
| AGENTS.md | FULL | Upstream contribution rules, not policies binding OrgResource; advises actual problem, exact sources and owner review, rejects fabricated claims |
| skills/using-superpowers/SKILL.md; references/codex-tools.md | FULL relevant rules | Skill triggering, Codex plugin and spawn model+effort controls; V2 clean-context spawn `fork_turns: none`, no recursive worker reviews and event-driven waiting; runtime tool availability must be checked |
| skills/brainstorming/SKILL.md | RELEVANT_RANGES | Existing context before questions, approval/decision flow, short vs full specs, avoid redundant conversations |
| skills/writing-plans/SKILL.md | RELEVANT_RANGES | Plans capture exact decisions, files/contracts/tests, checkable steps, self-review and ratio of plan detail to actual task; groups may be logically cohesive |
| skills/executing-plans/SKILL.md | RELEVANT_RANGES | **INLINE/NATIVE** mode: one implementer session, TDD at each task, persistent ledger, one final independent branch review; saves per-task subagent overhead |
| skills/subagent-driven-development/SKILL.md; implementer-prompt.md; task-reviewer-prompt.md | RELEVANT_RANGES | **SDD** mode: fresh implementer+reviewer per task, precise model and effort required, task review checks spec and quality, expense grows per task; workers explicitly MUST NOT spawn their own reviewer |
| skills/subagent-driven-development/references/batching.md | FULL | Batch *small same-shape* changes into one dispatch/review, avoid one child per trivial file |
| skills/subagent-driven-development/references/preflight.md | FULL | Pre-dispatch overlap/interface conflict table and rulings |
| skills/subagent-driven-development/references/resuming.md | FULL | Durable plan-ID ledger, resume first incomplete task after compaction |
| skills/subagent-driven-development/references/fix-loop.md | FULL | Maximum five fix rounds and scoped re-review; severe defects cannot be hidden by a cap in OrgResource |
| skills/subagent-driven-development/references/final-review.md; cannot-verify.md | FULL | Exact full-branch Git diff package, single fix wave then scoped re-review, independently resolve unchanged-code risk |
| skills/subagent-driven-development/references/handling-reports.md; re-review-prompt.md | SOURCE_RETRIEVAL_NOT_DONE | Should be retrieved during actual SDD execution; not used as authority for details here |
| skills/subagent-driven-development/scripts/sdd-workspace; task-brief; review-package; skills/executing-plans/scripts/task-start; task-done | METADATA / TEST_DESCRIPTIONS | Helpers exist, but NOT executed or reviewed line by line in this environment; do not vendor/run blindly |
| skills/test-driven-development/SKILL.md | RELEVANT_RANGES | Watch RED failure for correct reason → minimal GREEN → refactor; run full suite at acceptance. Apply to new/changed Extended code; cannot delete protected academic history |
| skills/systematic-debugging/SKILL.md | RELEVANT_RANGES | Reproduce & trace root cause before fixes, form specific testable hypothesis, avoid random multi-change thrash |
| skills/verification-before-completion/SKILL.md | FULL | Fresh commands and exit/output for any passing/completion claim; independent from prior CI |
| skills/using-git-worktrees/SKILL.md | FULL | Detect existing isolation, prefer native worktree, verify ignored workspace and baseline tests, avoid accidental main edits |
| skills/dispatching-parallel-agents/SKILL.md | FULL | Delegate only independent problems with disjoint files/state; not a mandate to spawn many |
| skills/requesting-code-review/SKILL.md; receiving-code-review/SKILL.md | RELEVANT_RANGES | Specific review at boundary and evidence-based dispute resolution; no blind accepting review suggestions |
| skills/finishing-a-development-branch/SKILL.md | RELEVANT_RANGES | Verify actual suite, explicit owner decision for branch integration, safe cleanup only |
| skills/diagnosing-superpowers/SKILL.md | RELEVANT_RANGES | Provenance-based incident/session analysis when agent workflow fails or becomes expensive |
| skills/writing-skills/SKILL.md | RELEVANT_RANGES | Skills are code-shaping instruction systems, need scenario RED/GREEN evaluation; avoid making untested "mandatory" skill copies |
| tests/claude-code/test-subagent-driven-development.sh; test-executing-plans-scripts.sh; test-sdd-workspace.sh; tests/codex-plugin-sync/test-sync-to-codex-plugin.sh | RELEVANT_RANGES | Upstream has tests, describes isolated ledger/task scripts and plugin packaging; **none run here** |
| docs/superpowers/specs/2026-06-10-strict-cost-sdd-design.md; docs/superpowers/specs/2026-07-30-codex-efficiency-fixes-design.md | RELEVANT_RANGES | Design experiments about costs, duplicate depth-2 reviews and repeated waits; not proven universal savings or applicable baselines to this project |
| .github/ additional tests and other upstream docs/source | METADATA_ONLY or NOT_READ | Not needed for bounded policy synthesis now, must inspect if copying/altering executable upstream scripts or claiming complete upstream review |

**Source-coverage truth:** All 15 upstream SKILL.md paths were located and their relevant rules retrieved; many long documents were **not independently audited line by line**. This review does not assert that every original, test, design document or video frame has been examined. Further review is mandatory before bundling any upstream implementation/hook.

## 2. Specific improvements that match OrgResource

| Superpowers technique | Evidence from upstream | Adapted OrgResource contract | Acceptance |
| --- | --- | --- | --- |
| Source-first brainstorming | brainstorming + writing-plans | C6 source and requirements/decision reconciliation, no redundant questions that sources already answer; do not reopen owner-decided demo and academic lane | Approval and SOURCE_COVERAGE + CONFLICTS |
| Native inline implementation | executing-plans | **DEFAULT** for most coherent C7/C8/C9 slices; one root implementer plus one independent review, exact TDD per changed behavior | Actual tests and independent branch diff |
| Opt-in SDD | subagent-driven-development | **EXCEPTION** for separate substantial independent tasks when cost/risk assessment beats inline, explicit owner per phase and max role limits | Actual measured no. spawns/tokens/retries, tested output |
| Exact per-task plan and interface | writing-plans | Plans have precise changed paths, DTO/types, MySQL invariants, 28 academic route tests, denial/crash/rollback and clean acceptance, grouped into sensible vertical slices | Requirement→code→test→evidence trace |
| RED/GREEN/REFACTOR | test-driven-development | New/changed Extended Java+React scripts tests first and actual failing behavior observed before fix; never rewrite Java8 frozen A to satisfy a new test | Recorded failing test reason, passing and full gate |
| Root-cause bug fixing | systematic-debugging | Reproduce, isolate faulty dependency, compare known working course example, hypothesis, minimal fix and regression | Source/probe reproduction |
| Plan-scoped ledger | SDD references/resuming + executing | Per-phase progress file and source SHA independent of chat compaction; avoid redoing finished tasks or resetting history | Resumption does not duplicate commits/tasks |
| Review package with BASE→HEAD | SDD task-reviewer prompt/review-package | Hand critic one exact Git diff with commit list and stat plus confirmed high-risk transitive callers; do not re-upload entire history | Reviewer source-specific findings |
| Review bounded by risk | task reviewer / final review | Routine single reviewer, severe auth/ledger risk gets targeted extra verification. Reviewer never pre-judged to downrank finding | P0/P1 veto preserved |
| Fix-loop breaker | fix-loop | Limit circular repeated agent retries and record root cause; at cap **BLOCK** unresolved mandatory P0/P1 instead of upstream's permissive parking | No silent unresolved critical findings |
| Native worktrees | using-git-worktrees | Follow Codex native isolation when available; frozen A source untouched; avoid duplicate worktree on Windows | Exact branches/worktrees and baseline source hash |
| No recursive review agents | implementer-prompt | Explicit ban; root alone authorizes review seat and phase actions | Spawn ledger has no worker-spawned descendants |
| Fresh verification | verification-before-completion | Full real MySQL 8/Jersey/Newman/Playwright/backup/actual host commands on final SHA, no green-by-prose | Actual tool receipts |
| Skills tested as behavior | writing-skills | New local routing methodology needs comparative short scripted scenarios and red-team outcomes, not only readable docs | Pre-registered router evaluation |

## 3. Conflicts and decisions — OrgResource authority prevails

**A. Upstream SDD fresh worker+reviewer for every task versus user token budget.** DECISION: default inline/native execution; use SDD only for an approved independent-task group. No default swarm. This reduces context rebuild and is explicitly recognized by upstream as the cheapest execution option.

**B. Superpowers examples call tasks “2–5 minutes.”** DECISION: keep fine-grained *testable steps* within a cohesive build pass; do not create a separate agent for each file/step. The grouped C6–C11 phase/vertical-slice plan governs acceptance.

**C. Upstream SDD continues through plan tasks with its own judgment; OrgResource gates owner approval per phase and forbids external side effects.** DECISION: continuous execution only **within** authorized phase and consent; always stop before C7/C8 next phase, merge, paid hosting, live SMS, real data, shared DB, Canvas.

**D. Upstream fix loop may park real load-bearing issues at cap.** DECISION: unresolved P0/P1 or mandatory tested failure => BLOCKED, never a release success; stop escalating/spending, preserve exact evidence and return for corrective plan/owner decision.

**E. TDD rhetoric says delete implementation if written before test.** DECISION: protect immutable Course Final A and historical evidence. For new Extended work/fixes use RED→GREEN observed tests, but never delete, rewrite or reset academic source to satisfy a generic external rule. Existing correctness evidence remains independently auditable.

**F. Upstream reviewer is told not to re-run the implementer's full suite.** DECISION: reviewer should avoid costly duplicate *full runs* but independently validate **specific high-risk claims** via real executable tests where required by QA gate. Root executes complete L2/L3 acceptance on final SHA; no mocks as real MySQL proof.

**G. Upstream plugin auto-trigger and broad brainstorming can conflict with this project's authority.** DECISION: do not silently install or fully delegate project policy to an external plugin. If user installs official Codex Superpowers, root first reads AGENTS.md and all gate docs and adapts skills inside these non-negotiable boundaries. Conflicting skills trigger a STOP/recorded authority decision.

**H. User demands visual frame-by-frame inspection of uploaded Daniyar MP4.** Actual uploaded path `/mnt/data/Daniyar_AI_Engineer_Big_Tech_video_no_watermark (1).mp4` exists as upload reference, but local Python/CaaS video processing calls returned ClientError. **No direct audio or frame review completed, no timestamps observed, and video-to-public-caption identity not proven.** The public creator caption is separately sourced (see §4). The future review gate is frame-indexed sampling or full-frame computational sweep plus manual representative/scene-change inspection and audio transcript; mark video visuals/audio NOT_RUN until environment permits. Do not claim “watched.”

## 4. GOAT creator claim — independently checked only as far as source permits

A public Instagram-caption mirror of `@donimas` / Daniyar Kazi, posted **2026-10-04** at https://imginn.com/p/DeFS0Fes8yu/ says:
- JUNIOR: one expensive model at max effort all six example tasks, claimed cost US$5.10
- MIDDLE: manually choose model with effort always high, US$0.75
- SENIOR: cheapest first, verify, escalate on failure, US$0.45
- GOAT: router selects model and effort per task, US$0.32
- Crucial original qualifier: **“Simulated workload, real prices. Not a benchmark.”**

**EVIDENCE STATUS:** Source-derived social-media caption, NOT tested OrgResource cost, NOT independently reproduced six tasks, NOT proof that the video visually depicts the same data. No measured output validity, model-price basis, input-token distribution, reasoning-token exposure, agent retries or Codex subscription/account fees were supplied with the caption. Do not turn the US$0.32 into a predicted budget or percent cost saving.

**Supported idea:** model/effort are two distinct routing variables; a clear task contract may lower needed reasoning effort. **Unsupported jump:** claiming a cheap model is always correct, or paying for a separate LLM “router” is necessarily cheapest. Apply a **zero-model-call deterministic router** first, review actual Codex supported models, target real test-evidenced cost per accepted task. More detailed contracts can sometimes cost fewer total retry tokens, but this is an experimental hypothesis.

## 5. Current operational status

- Source/GitHub research: **VERIFIED SOURCE CONTENT**, limited to paths/ranges above, pinned upstream SHA.
- Creator GOAT caption: **RETRIEVED EXTERNAL POST TEXT**, direct video confirmation pending.
- Video frames/audio: **NOT_RUN / BLOCKED BY LOCAL TOOL**.
- Official Superpowers plugin installation in the user's Codex: **NOT_RUN** and requires user action.
- Upstream scripts/helpers: **NOT_EXECUTED**; no local integration or tests performed.
- Proposed OrgResource router/skills adaptation: **PLAN/CONFIG ONLY** until authorized Codex execution and actual acceptance.

**Do not copy upstream source wholesale.** MIT copying is permissible with attribution and retained license, but copying every hook/script/skill increases maintenance and context, may duplicate functionality, and is unnecessary if the official plugin is installed. This PR creates a small OrgResource-native overlay and instruction links; no upstream executable code is vendored.
