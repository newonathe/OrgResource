# OrgResource — Quality-first, speed/token/storage-efficient engineering and scoped-agent policy

**Version:** 2026-11-16. **Status:** USER WORKFLOW DECISION to be applied in future phase prompts; no claim that parallel agents have been invoked here.

## 1. Optimization objective and constraints

Maximize **correct, secure, complete, maintainable, source-traceable, tested, remotely preserved software per total token, tool-call, time and storage cost**. Speed and token savings NEVER justify silently breaking course compliance, inventory integrity, privacy, or a tested acceptance gate.

**Do the smallest COMPLETE dependency-coherent batch**, not the smallest possible edit and not an unchecked giant rewrite. Prefer a medium/large vertical slice when the interfaces, tests and dependencies are already fixed. Split only when a risk, dependency, provider authorization, or acceptance boundary makes the combined batch unsafe.

Avoid unnecessary elaborate explanations/research in Codex. The research chat owns open-ended research and external verification; this planning chat owns phase boundaries/prompts/research tracking; Codex owns code/tests/release work. Agent roles do not override project authority.

## 2. Adaptive delegation — only if actual subagent tools are available

Do not claim to have spawned subagents when no execution interface exists. If Codex has supported delegations, use **at most a few nonoverlapping scoped workers**, not a swarm. Select a model and effort **proportionate to ambiguity/impact**; the exact model names depend on actual tool availability, so use capability tiers rather than fabricate a concrete model setting.

| Work class | Recommended capability/effort | Safely delegated task | Owner/verification |
|---|---|---|---|
| Mechanical inventory, static route diff, path scan, test-output extraction, formatting | Fast/light, low–medium reasoning | Read-only file map, deterministic scripts, source path report; no architecture edits | Primary integrates and spot-checks exact source |
| Bounded UI/DTO/Postman/module tests with frozen contract | Balanced, medium reasoning | One file family/module with exact input, acceptance and no overlap | Primary verifies snapshot, typecheck/browser assertions |
| Entity/repository/migration implementation with approved schema | Strong coding, high reasoning | One tenant/entity vertical slice with SQL FKs, transaction tests | Primary inspects actual MySQL effects/concurrency |
| Cross-cutting authentication/CSRF, inventory concurrency, literal javax/Jakarta, high-risk deploy/DR | Highest available, high/extra-high reasoning | One tightly bounded security/compatibility spike with explicit stop tests | Primary reviews vulnerabilities, rollback and source claims |
| Independent QA/security critique | Strong reasoning, high effort | **Read-only** adverse test review of already committed change; no competing writer | Primary applies corrections, reruns tests |

**Delegation contract:** Every worker receives (a) exact source SHA, (b) small relevant file paths, (c) written requirements/IDs, (d) ownership of specific files, (e) prohibited changes, (f) test command and expected result, (g) output format and (h) explicit stop condition. There MUST be exactly one writer for each path/worktree; avoid concurrent writes to shared POM, schema migrations, AGENTS.md or root configs. Parallelize independent *read-only* inspections freely when cheap; implementation batches only if branches/files are disjoint.

**Trade-off rule:** Do NOT summon a worker if setup/context/report/merge overhead exceeds the bounded work. A 10-line update rarely needs a subagent; a cross-component auth/stock release may merit two specialists and a central integrator.

## 3. How to size implementation phases

- **Small exception slice:** unknown security/compatibility/root-cause risk; bounded throwaway proof, concrete go/no-go, no product migration. Example: supported Javax/Jakarta provider + Jersey/JPA single-JVM experiment.
- **Medium coherent slice (default):** one fully working user journey, backend+DB+UI+tests together. Example: org-scoped Resource CRUD, role checks and negative cross-tenant tests.
- **Large coherent slice (when dependencies are stable):** combine tightly connected transitions/DB/UI and end-to-end acceptance. Example: full APPROVED→ISSUED→RETURNED reusable lifecycle with physical audit and browser flow. Do not prematurely bundle unrelated provider deployment or major framework upgrade.
- **Never micro-slice** each entity field, route annotation or individual test into a new paid agent turn when a single reviewed medium batch is safer and cheaper.
- **Never giant-batch** unfinished authentication, DDL rewrite, inventory locks, public deployment and academic submission in one unreviewed Codex pass.
- Gate after each change to the source of truth: schema/transaction architecture, security trust boundary, executable runtime stack, or public deployment. Routine tested leaf changes can be combined without separate owner approval.

## 4. Minimum read and tool budget

1. Fresh local+remote Git audit ONCE at start of every Codex build task (head, PR dependency/base, worktree changes).
2. Read root AGENTS.md, current exact source contract, only the direct affected files. Read historical 319k-char master only for an actual unresolved contradiction. Do not load the whole 1.25 MB research corpus repeatedly.
3. Use indexed file searches, path-targeted fetch, source symbols and diffs; reuse context instead of copy-pasting entire files back into the model.
4. Make a narrow implementation plan *internally*, then code. No token-consuming generic product research or repeated permission questions in Codex.
5. Run focused tests during development. One full relevant gate near completion (plus rerun if correction changed covered code), not a complete clean build after every small edit.
6. Review diff and actual errors; fix observed issues rather than broad speculative refactoring.
7. Keep final evidence a concise PASS/FAIL/BLOCKED/NOT_RUN table with exact commands and SHA; don't generate 100 pages of redundant self-narration.

## 5. Quality and correctness gates are not optional

**Source accuracy:** Distinguish course requirement from syllabus learning outcome, user-added product feature and historical proposal. Quote or paraphrase precisely with location. No invented Ateneo policy/users/stock/provider behavior.

**Data correctness:** All stock/demand values derived from one MySQL authority; actual transactions, FK constraints, deterministic clock, stable lock ordering, no duplicate consumption/return, history protection, explicit idempotency. Test rollback/concurrency by querying persisted DB after refresh.

**Security:** Backend authorization per organization/current membership and role; CSRF/session, secrets/cookies, tenant-scoped repositories, private media, abuse controls. Zero exposure of legacy unsupported Java8/Boot2 app to public ingress. Fail closed on unknown authorization or server failure. Do not use public demo as a gateway to private records.

**Course:** Five original entities and 26 operations real, Jersey+JSON+Autowired Components, Spring/JPA/JPQL/MySQL, literal javax, Retrofit/Gson/FakeTwilio and official Twilio protocol adapter, scheduling, Postman all actual REST routes, safe runtime deletes, actual image receive/serve. Extra tech may not substitute.

**UI:** Keep Academic Glass v5, strong readable light/dark contrast, responsive 360/768/1440, keyboard, focus, reduced motion, real API error/empty states and server-controlled state.

**Evidence:** A source annotation does not equal an executed test, reported historical pass does not equal a fresh run, provider mock does not equal actual official acceptance, and no automated tool can sign authorship or submit Canvas on behalf of the student without explicit authorization and genuine evidence.

## 6. Storage and performance discipline

- One modular monolith and one authoritative transactional relational database unless measured evidence requires otherwise.
- No microservices, Kubernetes, event broker, Redis, paid monitoring or speculative services solely to be fashionable.
- Do not persist derived availability, totals or redundant DTO copies unless a measured correctness/performance requirement proves necessary.
- Bound lists/pagination, limits, scheduler batches, timeouts, upload size and queries; avoid N+1; choose indexes from actual access and constraints. Keep log retention bounded and redact contacts.
- Never commit node_modules, target, dist, logs, screenshots en masse, DB dumps, local protected secrets or compiled artifacts unless an explicitly reviewed delivery exception exists.
- Prefer one canonical spec and linked evidence over multiple near-duplicate history rewrites; preserve historical originals with clear supersession rather than deletion.
- Free-tier quotas and off-host backup security are actual release gates; never promise “lifetime guaranteed free”.

## 7. Mandatory implementation Git workflow

For Codex BUILD tasks only:
1. Audit remote/local before editing; check PR bases, latest head and ownership of worktree.
2. Use correctly based feature branch from reviewed source, never outdated main by accident.
3. Preserve unrelated changes; one owner per file; no reset, destructive DB action or force-push.
4. Implement all authorized related code/tests/docs in the agreed batch.
5. Execute relevant tests, inspect diff and check secrets; do not disable tests to achieve green.
6. Commit and push all legitimate source/tests/concise evidence; verify local SHA matches remotely fetched SHA.
7. Create/update appropriately based PR; report exact URL, CI status, failures, dependency and next gate. **Never auto-merge**, purchase, enroll cloud, send SMS, deploy, tag final, or submit Canvas without separate permission.
8. If genuinely blocked, push only safe completed work with a truthful BLOCKED report; do not invent completion.

For RESEARCH/docs tasks: same source control hygiene but **documentation-only** on research branch or its own docs branch, not application code.

## 8. Agent-output template (compact, information-rich)

- Baseline: source SHA / feature branch / target PR.
- Authorized scope and file ownership.
- Code added/changed: paths, interfaces, migrations, security boundary.
- Tested: exact commands + observed results, real DB/browser/API/provider stub distinctions.
- Failure tests and rollback: executed or NOT_RUN, with root-cause evidence.
- Quality/security/stack compliance diff and major unsolved blocker.
- Storage/runtime effect only if actually measured.
- Final commit and verified remote SHA, PR link, next coherent build.
- NO unsupported claim of course grade, official Ateneo adoption, live SMS delivery, hosted uptime, legal sign-off or Canvas receipt.

**Policy is not an execution log:** this document specifies how future agents should be scoped. This documentation pass did not create or run external model subagents.
