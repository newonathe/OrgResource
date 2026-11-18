# OrgResource — Intelligent subagent orchestration and mandatory handoff contracts

Edition 2026-11-19. Status: proposed policy.

**SUPERSEDING STRICT TOKEN-COST CONTROL (USER DECISION):** Read in full docs/execution/12_TOKEN_BUDGETED_SUBAGENT_ROUTING.md and docs/execution/13_PHASE_SPECIFIC_SUBAGENT_PROMPTS.md plus the now-existing project .codex/config.toml and nine .codex/agents/*.toml agent profiles. **ZERO default spawns**, up to **TWO concurrent** agents and **FOUR distinct sessions per phase**, reviewer included, unless owner-approved exceptional evidence requires more. Each subagent has an explicit model+reasoning+read/owned write paths+exact task/test/stop prompt. The earlier generic strong/high per-lane recommendations below are superseded for **actual routine dispatch** by the configured per-role choices: Luna-low/medium source, Terra-medium API/UI/ops, Sol-medium ledger/auth and independent review, Sol-high **only** exceptional critical/C11 review. No automatically spawned three-agent startup and no autonomous nested spawning. If Codex account cannot use a configured model, STOP and verify actual options before substitute. Full quality gate unchanged.
 Applies to future authorized Codex phases; does not claim that subagents were spawned, available or tested during this documentation-only upload.

## A. Orchestrator responsibilities

The coordinator has final responsibility for course authority, integration correctness, user permissions, phase gates, source coverage, evidence quality and truthful status. Subagents cannot independently approve release, merge, deploy, spend, send SMS, change frozen Course A, mutate shared databases or launch the next pass. Delegation reduces duplicated work but **never delegates accountability**.

At each fresh pass the coordinator must:
1. Inspect actual Git remote, parent/head SHA, PR diff, worktree, CI, affected transitive dependencies and original source authority.
2. Read AGENTS.md and the entire docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md, then the relevant packet files and course examples. Every subagent must read the entire gate too.
3. Define risk-scoped work packages with exact files, interfaces, acceptance tests, output artifacts, forbidden operations and a STOP boundary.
4. Pre-agree common version/DTO/role/SQL/error, receipt and tenant contracts, so agents never invent incompatible interfaces.
5. Assign one writer per shared file and one owner for each Flyway migration; require independent worktrees/branches for simultaneous writers.
6. Obtain output with line/path-level evidence, inspect actual work, challenge findings and run integrated real-MySQL/HTTP/browser gates.
7. Record disagreements, unresolved risks, approvals and a complete continuation handoff.

## B. Model selection policy

Codex/model availability and supported reasoning controls can differ by account/session; **query actual available options** and write the selected model identifier + reasoning setting in the phase report. These are capability tiers, not a promise that a particular SKU exists.

| Job | Preferred effort/capability | Reason |
| --- | --- | --- |
| Scripted inventory, Git tree and hashes, CSV join, manifest validation | Tools/script; no generative worker unless required | Deterministic and lowest token/error cost |
| Narrow docs formatting, routine paths, fixture data with established contract | Fastest capable model, low/medium | Work is mechanically verifiable |
| Per-feature frontend/API wiring and noncritical test expansion | Competent code model, medium/high | Needs interface comprehension |
| SQL transaction/ledger/concurrency, crypto/authentication, migrations, recovery | Strongest available code-reasoning model, high/extra-high | Invariant or irreversible-risk work |
| Academic course/syllabus conflict adjudication | Strong reasoning, high, source-grounded | Avoid fabricated compliance |
| Security threat, independent adversarial review and release veto | Different strongest available reasoning agent, high/extra-high | Reduce correlated implementation bias |
| CI, packaging, performance profiling, bounded storage | Strong code/tooling model, medium/high | Real command and environment reasoning |

Escalate when test failures are ambiguous, multi-file decisions interact, proof is incomplete or security/privacy risk rises. Downshift only once exact contracts and falsifiable tests are fixed. Do not lower reasoning to satisfy a token quota. Record per-worker wall-clock, token input/output (if exposed), tool usage, retries and acceptance outcomes; NOT_AVAILABLE when tool does not expose them.

## C. Worker lanes and ownership

### Lane S — Source/academic compliance analyst (read-only)
Inputs: course PDF, syllabus, slides/labs/examples, source ZIP manifests, frozen A tree, C5 tree and gate. Outputs: source coverage register; course and amendment conflict ledger; exact file/page/course authority; 26 business + 2 image trace matrix; verified/not-run boundaries. No code writes or unsupported statements about unseen video frames. Configured gpt-6-luna medium for bounded source facts; root escalates genuine course conflict.

### Lane B — Backend/auth/security and HTTP reviewer
Inputs: Extended identity/security/rest/service sources; migrations and session behavior; role policy. Outputs: role×endpoint×tenant matrix, attack cases, actual controllers/DTO/SQL links, bounded changes and regression tests. Changes only under assigned backend paths. Configured gpt-6.1-sol medium for scoped auth/security, escalate to high only for concrete unresolved P0/P1.

### Lane D — MySQL/inventory/algorithm reviewer
Inputs: InventoryService, related repositories/entities, Flyway V1–V5, CI MySQL, backup/restoration scripts. Outputs: stock/hold/issued equations, SQL locking and grant matrix, migration safety, idempotency replay and concurrency tests, EXPLAIN and storage results. Sole writer for schema modifications; never edits old migration checksum. Configured gpt-6.1-sol medium for verified stock contracts, escalate to high only on unresolved significant risk.

### Lane F — Frontend/accessibility reviewer
Inputs: extended/frontend React pages/context/client/types, style rules, Playwright and original academic UI. Outputs: complete task flows by role, truthful partial/loading/error/uncertain states, keyboard/reduced motion/mobile tests, API/contract discrepancies. Owned frontend files only. Configured gpt-6.1-terra medium for fully specified frontend/UI work.

### Lane R — Build, release, performance, operations
Inputs: workflow verify.yml, POM/package-lock, scripts, deployment decision, logs and artifact manifests. Outputs: clean reproducible build, cold/warm metrics, dependency graph, risk review, environment/TLS/restore/runbook artifacts. Does not create paid resources/deploy publicly. Configured gpt-6.1-terra medium for measured ops; escalate single hard recovery/security issue if evidence requires.

### Lane X — Independent adversarial critic (read-only and separate)
Receives **source and artifacts, not implementer's reassuring conclusion alone**. Reconstructs contract independently, searches counterexamples, reproduces high-impact failures when safe, classifies claims SOURCE_ONLY/REPORTED_ONLY/TEST_FAILED/NOT_RUN, finds missing file transitive dependencies and false-positive tests. Configured gpt-6.1-sol medium independent review normally; one gpt-6.1-sol high critic for documented P0/P1 or final C11 gate. xhigh is exceptional, not default. Any unresolved P0/P1 vetoes acceptance.

### Lane I — Integration coordinator (one writer)
Checks change conflicts, merges only locally into authorized test branch/worktree when allowed, reconciles DTOs/schema/roles/API, runs exact gates and records release provenance. Responsible for consolidated reports and next-pass prompt; not a replacement for independent critic.

## D. Parallelism scheduler (risk-based DAG)

- Phase C6 read-only audit: S, B/D read-only, F and R may work concurrently on disjoint path sets, while X reviews their work **after** artifacts exist. Consolidation by I.
- C7 identity/security: B writes auth/security; D owns any SQL changes; F can write disjoint UI auth flows after contract freeze; R audits dependencies read-only. X evaluates merged behavior.
- C8 vertical workflows: D owns ledger/schema, B writes domain API/services, F writes pages only **after** API contract freeze. Parallel file changes allowed; integrate and test the whole transaction+UI slice before accepting another slice.
- C9 performance/CI: R owns workflow/scripts; D may own index/query improvement; F may own bundle/accessibility performance in separate paths. No simultaneous conflicting lockfile/POM editing.
- C10 staging/recovery: R owns deployment/runbook; D owns safe restore/data validation scripts; B reviews reverse proxy/session; X runs adversarial independent acceptance.
- C11 release: X leads source/behavior falsification, I runs full clean gate and packages, S reviews academic separation; no new feature changes.

A dependency edge is mandatory for overlapping source paths, shared contracts, Flyway migrations, schema migrations, integration tests and release branch mutations. Do not run five writers just because five agents exist. Start with ZERO workers. Dispatch ONE well-scoped necessary specialist after a demonstrated benefit; a second only for disjoint parallel files under frozen contracts, capped at TWO concurrent and FOUR distinct sessions per phase (including later independent critic). Never spawn extra just because agents exist. See Sections 12/13.

## E. Worker dispatch template (mandatory fields, no placeholders in real issued task)

MISSION: one named phase, one output artifact and falsifiable goal.
AUTHORITY: exact course/QA documents, user decision IDs, approved scope.
REPOSITORY: newonathe/OrgResource; remote; exact base/head SHA; branch; PR; expected clean worktree.
FILES: exhaustive assigned read paths, files authorized for edit, unchanged high-risk dependencies requiring audit, all explicitly forbidden paths.
CONTRACT: endpoints/methods/DTOs/permissions/status codes/ETags/idempotency/SQL columns and equations/clock; explicitly identify unresolved interface decisions.
WORK: exact ordered steps; allowed parallel work; STOP on cross-owner dependencies and unapproved behavior.
EVIDENCE: paths, code line ranges, tests/scripts/fixtures/expected database postconditions/HTTP response, CI hashes; actual result classifications.
RED TEAM: at least one counterexample for each auth, integrity, concurrency, loss/replay, malformed input, rollback and recovery area relevant to scope.
RESOURCE: assigned model and reasoning tier, expected risk profile, bounded tools, token/build/storage measurements if provided; never override correctness.
OUTPUT: per-file KEEP/FIX/DEFER table; requirement-test matrix; diff summary; executed commands and exit codes; classifications; risks, alternative and handoff.
BOUNDARY: never merge, force push, deploy, spend, send live SMS, alter frozen A, edit old migrations, use real data, touch shared MySQL, modify other agents' owned paths or claim unrun results.

## F. Exact worker report exchange

Return the following structured payload as Markdown tables/CSV files (or corresponding machine-valid JSON where tests can validate it):
- RUN_ID, PHASE, REPOSITORY, BASE_SHA, HEAD_SHA, BRANCH, READ_GATE_SHA, DATE_PHT, WORKER_ROLE, MODEL_REPORTED, REASONING_EFFORT_REPORTED.
- SOURCE_REGISTER rows: file, blob_SHA, inspected start/end, inspection_level, dependency, reason and outstanding range.
- CHANGE_REGISTER rows: file, blob_SHA, affected method/schema, old→new, owner, risk, new tests, passing evidence, verdict.
- TRACE rows: requirement_ID, authority locator, implementation locator, expected assertion, evidence/run SHA, status.
- TEST_RECEIPTS rows: command, environment/toolchain, fixture, exit_code, passed, failed, skipped, actual postcondition, attached artifact.
- COUNTEREXAMPLES rows: threat/failure, attempted reproduction, actual behavior, severity, fix/test/blocked reason.
- METRICS rows: cold/warm wall time and condition, agent tokens if exposed, artifact/DB bytes, query performance; NA/NOT_RUN where unavailable.
- UNRESOLVED rows: P0/P1/P2, impact, required decision/approval, owner, due, contingency.
- HANDOFF: exact next base/head and complete copy-ready prompt for the next authorized stage (or STOP).

**Reject worker output** if a changed high-risk file has no reviewed range, if all-green is inferred from a prior commit, if a source is quoted without retrieval, if tests are merely proposed, if model claims lack availability evidence, or if a worker declares it has run unexecuted commands. The coordinator independently checks at least one material claim from each worker against primary code/evidence.

## G. Prompt compiler and quality verifier

For each phase, generate an exact worker contract from its corresponding full master prompt in 04/05 **and the role-and-phase task in 13**, applying all strict model/spawn ceilings in 12. Expand concrete refs/paths from fresh Git, then validate: (1) gate read instruction present, (2) accurate immutable A, (3) allowed/forbidden edits, (4) source review closure, (5) course/Extended route distinction, (6) exact feature contracts, (7) negative/concurrency/crash/restore tests, (8) independent critique, (9) no unauthorized actions, (10) exact evidence format and stop rule. If any is absent, fail dispatch preflight.

Do not merely send "audit this" or "make production-ready" to an agent. Conversely, do not replicate every irrelevant 660-file document in every worker prompt. The full QA standard plus complete task-specific specifications and precise source locators are required; worker must fetch and read authoritative sources itself.

## H. Correlated-error prevention

Implementer and reviewer use independent reasoning passes and different working context where available. Do not let reviewer modify source while auditing. Require a counterexample attempt and cite its actual test status. A second agent repeating the first agent's claims without source review is not independent verification. Re-open changed SHA and rerun mandatory tests after any corrective change.

## I. Critical stop conditions

Stop phase acceptance for: failing mandatory test; unresolved material course conflict; unexpected Course A tree delta; V1–V5 checksum drift; cross-tenant data leak; stock math mismatch; irreversible double-commit risk; shared database target; secrets in Git/logs; missing recovery key for an attempted restore; incomplete high-risk source review; unproven mandatory public deployment/restore; unauthorized release action; incomplete source and acceptance evidence. Document the partial result, exact blocker and next safe action.
