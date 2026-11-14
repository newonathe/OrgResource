# Permanent user execution preferences — token-efficient, substantial builds and push-every-cycle

This is the authoritative standing policy to embed in EVERY *future* Codex prompt after O3. This policy was explicitly clarified after the earlier long O3 coding prompt was already running.

## Key user instructions (verbatim excerpts)
1. “note that I am using Luna 6 Model at extra high reasoning mode to build all these which is a lower model with lower capabilities and reasoning power so EVERYTHING in the prompt has to be VERY SPECIFIC. but still, I WANT TO BE VERY TOKEN EFFICIENT ON BUILDING ALL THESE, Detailed, thorough, precise, accurate, complete but not overdoing it, overcomplicating or going overboard. I also want it to be very fast, scalable, and storage efficient in terms of the actual build and the actual software. Also, again, VERY TOKEN EFFICIENT IN THE BUILD WITH NO WASTAGE.”
2. “okay do that for this slice but for the next ones, I WANT TO BALANCE BIG BUILD AND TOKEN EFFICIENCY. Goal still is to be full maximum token efficiency but sometimes u tend to overscope it down to specific tasks to the point that it takes way too long to build which costs more cause there's so many complications. also always audit github repo before doing anything then tell in the prompt to push everything always.”
3. “okay actually right now, the prev prompt is still being built, after that finishes, I want to transfer to a new conversation here in this project and continue there what we are doing here right now.”

Do NOT assert anything independent about the named model's capabilities. Translate the requested care into **deterministic contracts, exact paths, test matrices, state transitions and failure paths**, not long meandering commentary.

## Primary optimization objective
Maximize: **correct, tested, remotely preserved, functional software produced per total agent tokens / tool calls / elapsed sessions**.

Optimize TOTAL COST, not only shortest prompt length. A 200-word prompt causing 10 rounds of follow-ups, 10 repeated audits, 10 builds and 10 missing edge cases is less efficient than one well-specified, coherent phase. Conversely, a 30k-token prompt rehashing all course files and specifying 14 unrelated cycles in one shot is also waste.

## How to size tasks
Select the **largest dependency-coherent implementation batch** that can be coded, tested and debugged with bounded context in the current coding session. Preferred after O3: O4+O5 as one cycle; O6+O7 coordinated; O8–O10 functional UI; O11–O14 QA/release. Internally checkpoint real tests after logical subcomponents but do not stop after one route/page and force expensive new prompts unless necessary. Isolate high-risk concurrency and external-provider steps within same batch with acceptance gates; don't merge unrelated infrastructure. Agent can make multiple local commits as needed but one pushed remote branch/PR per coherent cycle (or sensible stable subcommits).

## Required beginning of EVERY future coding prompt — audit
1. GitHub connected remote repo \`newonathe/OrgResource\` recent main/feature commits, branches, open/merged PRs, CI runs, files/diff, issues if relevant. **Fresh** audit, not earlier snapshot.
2. Local agent git status, HEAD, current branch, working tree, remotes, uncommitted work, merge bases. Do not lose local user changes.
3. Identify latest implemented route count, exact code and reports; previous phase status, failing tests, missing source/DB setup.
4. Read relevant source/primary authority and latest phase report, not all 188k+ files repeatedly.
5. One short precise scoped plan, then write actual code. Do not stop at planning.

## Token-saving engineering protocol
- Read \`AGENTS.md\`, latest relevant evidence/route parity, compact source sections once per session. Read duplicated master only one location, targeted headings only after initial complete architecture grounding. Inspect actual file being changed.
- Avoid repeated full repo scans unless Git changed, repeated full Maven/npm builds on every edit, duplicate files, duplicate docs, reprinting huge logs or rewriting successful modules.
- Batch similar endpoints/tests/validation while sharing setup and fixtures. Targeted unit/integration tests during development; one appropriate full regression at cycle gate.
- No generic "give me permission" checks for safe routine local changes; no fictional background execution. Complete unblocked work in this response/session. Stop for real blocker or unsafe destructive action only.
- Do not use many agents/worktrees for trivial independent modules; parallelize only when ownership strongly separated and integration benefit outweighs overhead.
- Prefer direct code reading and existing tests to broad speculative web browsing. Use updated official sources only where API/provider/security versions actually matter.
- Report concise meaningful verdict: starting HEAD, changed files, features/routes, exact commands/results, failures, confirmed pushed SHA/PR, next cycle. Avoid copy-pasting giant planning document in each final report.
- Use exact data and interface contracts including malformed input/empty/404/409, concurrency, locks, PHT, and state machine to reduce low-capability coding mistakes.

## Software speed / scalability / storage
- One Java8/Jersey/SpringBoot app + one MySQL InnoDB + one React/Vite client. No microservices, Redis, Kafka, managed SaaS, SSR, second Node API or additional database.
- Correct reservation/coverage/total values before clever caching. One authoritative DB; narrow JSON DTOs, no phone responses. Persist only essential fields, compute derived quantities on demand.
- Preserve DB constraints and appropriate indexes for actual predicates; avoid redundant indexes, N+1 list queries, unbounded scans, needless object graphs, repeated query phases inside transactions.
- Use short DB transactions with correct lock order; no remote HTTP while DB lock. Avoid double-commit, failure partial state, blind network-retry writes.
- Use bound batch sizes in schedulers; no immediate need to tune default pool of five without measurements. No invented performance SLA, benchmark, traffic or dataset.
- Frontend: strict typed API, efficient requests, reusable simple components, loading/error/empty, no unnecessary CSS/UI libraries or animations, mobile/desktop/keyboard. Actual user-perceived responsiveness with real data.
- Storage: .gitignore node_modules/target/cache/dumps/secrets/logs, no repetitive source ZIP packaging every implementation cycle; generate final exports at release and preserve necessary historical evidence.
- Portability: Java8/Eclipse-compatible source export, and pinned Node/Vite dependencies at actual UI phase.

## Mandatory push at end of EACH cycle
User explicitly requested “push everything always” and the assistant interpreted as automatic push to **feature branch**. Therefore future Codex prompts MUST authorize/require:
1. Review all git changes and stage **all legitimate task-related** code/tests/config/docs/Postman/test-evidence. Preserve user unrelated/untracked files.
2. Exclude passwords, .env, DPAPI files, private phone/student data, node_modules, target, DB dumps, caches, raw course ZIPs unauthorized for repo.
3. Execute appropriate checks; label failed/unrun honestly.
4. Commit descriptively; push to remote feature branch, verify actual remote SHA and branch, open/update PR if available.
5. For partial/blocking work, push safe WIP to clearly labeled feature branch with truthful status; no fake acceptance.
6. Do NOT auto-merge into \`main\`, force-push, reset/discard existing work, publish repo, change Git history destructively, delete/reset MySQL, submit Canvas, or send real SMS without separate authorization.
7. If remote auth fails, report actual failure and preserved local state, not “pushed successfully”.
8. Initial GitHub remote audit in ChatGPT planning and in Codex coding environment separately; the first does not replace the second.

## Integration and agent roles
ChatGPT (this Project) = project supervisor and connected GitHub read-only verifier/prompt author. Codex (user runs separately) = code editor. Avoid claiming code has been changed from a planning response. If user opts for another coding agent, avoid two writers to same files/worktree. Review/merge is separate human gate. Git pushes are intended to preserve code, not auto-accept it.

## Output form for next large-cycle prompt
Use a tightly organized single Codex prompt:
- Scope and starting audit.
- Authority & minimal read paths.
- Exact endpoints/events/fields/state transitions.
- Precise algorithms/locks/DB queries/errors.
- Grouped technical tasks with dependencies.
- Shared performance/storage constraints.
- Concrete test matrix (positive/negative/concurrency/rollback/real HTTP).
- Files or package boundaries; no unnecessary additional architecture.
- Evidence and Git commit/push/PR.
- Done/partial/blocked decision gate and continuation.
Avoid huge duplication of root master; point to exact docs. Avoid overly narrow per-method sessions. Larger testable group accepted if all required gates pass.

## Next likely prompt
After O3 passes, deliver **O4+O5 combined transactional allocation lifecycle AND expiry scheduler implementation**. O6+O7 then, etc. If O3 fails, one scoped repair batch; no entire app rewrite. Ensure new prompt always starts with live GitHub audit and ends with automatic feature-branch push mandate.