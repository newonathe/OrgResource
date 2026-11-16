# OrgResource repository agent instructions — Course Final A then Extended C B

> **Branch and evidence boundary:** This updated root AGENTS.md applies on the documentation/research branch; it does not change O14 PR #8 or main. The original expanded agent guide appeared in separate academic draft PR #9 and was consolidated with CP44–46 two-edition decisions. Preparing this document does NOT authorize app-code changes, provider actions, branch merges, or student submission.


**Updated:** 2026-11-16. **Applies to coding assistants and research agents where relevant.** Historical rules such as `exactly five forever` apply only to the academic O14 scope; the **final user decision is TWO RELEASES** in one lineage: strict independently runnable Course Final A (first) and full Extended C B derived from immutable A (second). Do not retroactively change historical evidence. This root policy is present on a documentation branch; it is not proof any application implementation accepted it.

## 1. Sources and course preservation

- Read the original 09_ESP_PROJECT_ASSIGNMENT.pdf and 08_CSCI180_SYLLABUS.pdf as written academic authority; 10_ORGRESOURCE_CONCISE_PROPOSAL.docx is historical project design. No invented new Canvas rubric. The real Canvas submission and syllabus's COA/outside-assistance obligations remain student-only.
- **START HERE:** `docs/research-2.0/77_CP46_FINAL_GITHUB_HANDOFF_RELEASE_INDEX.md` → `docs/research-2.0/74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md` → **single active Codex prompt** `docs/research-2.0/75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt`. Then `docs/academic-final/00_READ_FIRST_AND_DECISION_BOUNDARIES.md`, 01, 02, 05, 03, 04, 06 as needed. Older CP37/71 standalone stages are historical after CP44–46.
- Preserve **actual working** Jersey REST/JSON + Spring @Autowired controller→Component + JPA/Spring Data/Hibernate/MySQL/JPQL + literal Javax Validation + Retrofit/Gson official Twilio-compatible REST (alongside local FakeTwilio) + scheduling + five original domain entity families + runtime create/read/safe delete + all original 25 routes plus A09 + Postman. Include actual HTTP image receive/serve to cover syllabus CLO9. No Jakarta-only, Node-only, serverless-only or mock demonstration substituted for these course capabilities.
- The assignment requires **AT LEAST five** justified entity types, not exactly five forever. Department, Project, Resource, ResourceRequirement and Allocation are real and sufficient for A; justified organizational identity/session/ledger/media tables may be added in B, **not** as new professor mandates.
- **Release A:** preserve/complete real local academic Java 8/Jersey2/Javax compatibility if it passes, with original five domains/26 routes plus needed course images and Twilio. Freeze A's SHA/artifacts/test evidence independently. **Release B:** descend from frozen A and build ONE enhanced C product with one authoritative MySQL inventory, one React UI, secure multi-org identity and ledger, a supported production runtime, and independent actual course-parity tests. Do not publicly expose Boot2.1. A can remain old; B may require reviewed adaptations after a bounded executable Javax/Jakarta/Jersey/JPA compatibility gate. Never say A's archived source alone proves B's course compliance.

## 2. Agent role boundaries

- Research ChatGPT: external/academic research, independent technical verification, source ledger/SRS/EERD/ADR, research-only documentation commits.
- Prompt-planning ChatGPT: review phase evidence, reconcile approved decisions, compose accurate implementation-ready scoped master prompts.
- Codex: **BUILD/TEST/DEBUG/RELEASE engineering only**. Inspect existing source and dependency graph as necessary; do not perform open-ended research, choose architecture/provider, invent requirements or change course stack independently.
- Real model/subagent delegation is optional ONLY if available in the execution environment. Do not claim invocation otherwise. Prefer bounded read-only low-effort workers for inventory/static checks; medium-effort for fixed leaf modules; high/extra-high for security, transaction races and compatibility. One file/worktree writer, disjoint scopes, independent integration verification. Avoid delegation overhead on trivial tasks.
- Full efficiency/quality sizing and template: docs/academic-final/04_AGENT_AND_EFFICIENCY_POLICY.md. Prioritize correct/secure/complete work, then maximize delivered tested quality per token, wall time, storage and tool calls. Prefer medium-to-large dependency-coherent batches, not micro-edits or giant unchecked rewrites.

## 3. Git and testing mandatory for build phases

1. Fresh remote/local audit (worktree, origin, branches, heads, open PR bases and current CI) BEFORE edits. Do not branch from old main when implementation is on unmerged O14/feature chain.
2. Read affected exact source/approved contract ONCE. Preserve unrelated user changes; no reset, force-push, auto-merge or destructive database actions.
3. Use isolated fictional test schemas only. No real campus student contact, institutional integrations or fabricated pilots, no default real SMS, no live account/provider sends or cloud purchase without separate authorization.
4. Implement only the authorized dependency-coherent phase; source of truth is actual relational transactions, backend authorization, correct response and history state. Never weaken tests to get green.
5. Run relevant focused unit/integration/real MySQL/browser/Postman/security tests; check actual database postconditions; one full appropriate gate at end. Report PASS/FAIL/BLOCKED/NOT_RUN, not invented success.
6. Inspect diff and secrets, commit/push **all legitimate task-related source/tests/concise evidence** to correctly based feature branch, verify remote SHA equals local HEAD and open/update PR. **Do not merge to main automatically.**
7. Report exact paths, test commands/env/versions/results, commit/remote SHA, PR link, defects/risks and next gate. Do not claim author signatures, real recordings, Canvas receipt, Twilio delivery, independent audits or permanent free cloud uptime absent actual evidence.

## 4. Product-specific correctness and scope

- O14 whole-batch reusable/consumable T/H/I availability and allocated/shortage calculations, half-open Project times, effective holds, one-time consumption, never-auto-return issued custody, safe deletion, SMS consent/unknown send handling and privacy are regression contracts.
- Protected original APIs must remain real and be tenant/role scoped in final product; O14 old repository queries were global and cannot simply be copied into a multi-org runtime. New v2 interfaces must use same MySQL and business transaction model.
- Academic Glass Commons v5 UI identity is locked; maintain readable light/dark, keyboard, mobile, reduced motion and real connected workflows.
- No speculative microservices, Kubernetes, Redis, payment/finance, campus SSO, partial returns, QR or future booking unless separately approved. $0 mandatory cost is an operating constraint, NOT a guaranteed lifetime provider promise.
- Printed final academic date is **2026-12-05**. **Course A acceptance and immutable freeze MUST precede B implementation and production acceptance.** Preserve and submit a verified A on time even if B is incomplete; identify supplemental B truthfully. Disclose infeasible deadlines rather than fabricate a submission or extension.

**Safety/phase rule:** Documentation and research approvals do not automatically authorize application implementation, stack migration, cloud trial, provider send, destructive restore, tag/merge or Canvas submission. Each engineering prompt supplies its own precise scope and gate.
