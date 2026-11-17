# OrgResource — Canonical Academic Requirements: Read First

> **FINAL TWO-EDITION OVERRIDE (CP44–CP46, 2026-11-16):** This academic canonical document originated in already-uploaded **draft PR #9**, and is consolidated here without merging that PR. Read [Final consolidated handoff](../research-2.0/77_CP46_FINAL_GITHUB_HANDOFF_RELEASE_INDEX.md), [A-first / B-extended decision](../research-2.0/74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md), and the [single active Codex master prompt](../research-2.0/75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt). **A** is a local strict course version; **B** is separate enhanced C descended from frozen A. Any prior wording implying one final release, modernizing A before course acceptance, or production testing before A completion is **historical/superseded**. A image testing is safe local single-organization; B must enforce authorization/tenant isolation. No application tests or course submission were performed by this document copy.


**Prepared:** 2026-11-16 (Asia/Manila)  
**Status:** SOURCE-VERIFIED REQUIREMENTS / EXECUTION STILL OPEN  
**Purpose:** Consolidate the *actual course requirements*, the previously proposed 25-operation OrgResource design, the actual 26-operation O14 implementation, and the user's conservative final-product decisions into one precise academic contract. These are specifications, **not** proof that newly planned features have been implemented or accepted.

## Source and decision authority

1. **COURSE REQUIREMENT:** 09_ESP_PROJECT_ASSIGNMENT.pdf, pp. 1–3, is the explicit final-project specification. The original wording says “RetroJit”; 08_CSCI180_SYLLABUS.pdf p. 2 CLO8 and supplied course examples establish the relevant Retrofit/Gson API. Do not invent a technology called RetroJit.
2. **SYLLABUS FACT:** 08_CSCI180_SYLLABUS.pdf, pp. 2–4 and 6, specifies CLO1–CLO10, including HTTP image receive/serve at CLO9, the project rubric, Canvas submission, and the Certificate of Authorship/outside-help acknowledgment. **Dates may change on Canvas**, but the user has fixed the provided two documents as the written feature scope for planning. This does not waive real Canvas submission.
3. **PROJECT DESIGN HISTORY:** 10_ORGRESOURCE_CONCISE_PROPOSAL.docx, 12 pp., defines the original five-domain-entity, 25-operation proposal and its Component methods. It is not a new syllabus or proof of execution.
4. **VERIFIED SOURCE:** O14 implementation: feat/o14-academic-final-acceptance-2026-11-15 at 2dd336ed39cecd417a8bce3388b303e688e95dee, open draft PR #8. Includes actual five JPA classes, 26 Jersey handlers, React eight-screen frontend, local scheduler, FakeTwilio Retrofit/Gson implementation, and previous Java/MySQL/UI acceptance evidence. The O14 old Java8/Boot2.1 executable must NOT be exposed as an Internet production service.
5. **USER DECISION, UPDATED by CP44:** two independently runnable editions from one Git lineage: Course Final A finished and frozen FIRST; Extended C B derives from A and independently re-proves original course functions. Both precede actual student's Canvas submission where completed. The original five core business types and their 26 source operations remain genuine, not replaced by mocks, screenshots, paper-only files or a separate irrelevant backend.
6. **CONSERVATIVE IMPLEMENTATION DECISION:** Include authentic image HTTP receive/store/serve and a real Twilio Messages API Retrofit/Gson adapter, alongside the existing local FakeTwilio. Live SMS, enrollment, purchases and cloud deployment remain separate unauthorized actions.
7. **RESEARCH REFERENCE:** docs/research-2.0/70_CP43_FIXED_WRITTEN_COURSE_SCOPE_AND_HANDOFF_GO_NO_GO.md; 69_CP42_FINAL_OWNER_DECISION_REVIEW_AND_CURRENT_BUILD_DELTA.md; 64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md; 68_CP41_SOURCE_TO_SQL_INVARIANT_GUARDS_AND_PROOF_CASES.md; 66_CP39_PINNED_CI_TRIAGE_AND_LAST_CONTRACT_CORRECTIONS.md. Earlier research drafts conflict; cite the most recent explicit adjudication rather than combining contradictory models.

## Scope distinctions — essential to accurate grading

- **WRITTEN MANDATORY:** Jersey REST, JSON, Spring annotations with Controller delegating to injected Component, JPA/Spring Data repositories, Javax Validation, Retrofit-related external service including Twilio, scheduling, >=5 justified entities, GET retrieval/POST submission, runtime create/read/delete (not initializer only), Postman for all calls, final logic, academic submission/recording and genuine peer-review/COA obligations.
- **SYLLABUS OUTCOME / CONSERVATIVE INCLUDED FEATURE:** Actual image receive and serve over HTTP (CLO9); do not mark present in O14.
- **PROJECT DESIGN COMMITMENT:** Original Department/Project/Resource/ResourceRequirement/Allocation, original 25 operations plus A09 consent-withdrawal amendment, short JSON fields, whole-batch allocation and effective current holds.
- **USER-PREFERRED PRODUCT EXTENSION:** authenticated organization-private workspaces, roles, history/ledger, real recovery, same-source MySQL, one React UI, secure supported runtime and zero *mandatory* paid services. These are NOT explicit assignment requirements and MUST NOT substitute for any written course item.
- **OPERATING SAFETY:** Public demonstration must use fictional data and must not claim Ateneo affiliation/adoption. No production deployment before supported runtime, tenant isolation, HTTPS, cost/backup and recovery acceptance. No perpetual free uptime promise.
- **ACADEMIC ACTIONS:** Cannot be automated/fabricated: real recording, signed truthful COA, actual peer reviews if there are other group members, Canvas submission receipt, any instructor approval. Do not silently assume the prior proposal or submissions were accepted.

## Verified academic baseline (not new tests from this documentation pass)

| Evidence | Actual status | Precise boundary |
|---|---|---|
| Five O14 JPA @Entity classes | SOURCE VERIFIED | Department, Project, Resource, ResourceRequirement, Allocation |
| Five O14 Jersey Controller families | SOURCE VERIFIED | 26 handlers: D4+P4+R5+Q4+A9; original 25 + separately added A09 |
| Java 8 / MySQL + React baseline | PREVIOUS CI PASS | O14 reports 55 backend and 15 frontend tests, CI Java/MySQL and packaged JAR smoke |
| Browser / Newman | AUTHOR-REPORTED LOCAL PASS | O14 reports 5 Playwright, 96 Newman requests / 142 assertions; no fresh rerun here |
| CLO9 actual image HTTP | NOT IMPLEMENTED IN O14 | Required by chosen conservative final-contract coverage |
| Real Twilio Messages API adapter | NOT IMPLEMENTED IN O14 | FakeTwilio simulator != real Twilio protocol adapter or actual provider acceptance |
| Full auth / organization isolation | NOT IMPLEMENTED IN O14 | Public production remains BLOCKED |
| Supported one-runtime literal Javax + modern Jakarta | NOT RUN / TECHNICAL BLOCKER | Research candidate, requires actual executable proof |
| Production public $0 host, off-host DR, TLS | NOT RUN / ACCOUNT-DEPENDENT | Cannot be certified from documents |
| Final proposal updated to match product | NOT DONE | Original 12-page proposal is historical, not final as-built specification |
| Recording, COA, peer/Canvas submission | STUDENT ACTION NOT VERIFIED | Never claim completed from generated documents |

**Historic technical caveat:** Research identified an earlier identical-source failing/passing MySQL test (available=8 when expected 5). Cause is UNKNOWN, not fixed merely because later CI is green. See CP39. Do not alter the expected value or disable the test.

## Latest execution handoff — 2026-11-16

**LATEST C3 EXECUTION HANDOFF — 2026-11-18:** C2 is in draft PR [#15](https://github.com/newonathe/OrgResource/pull/15) at `c8b6e76c249f99f3642c1513ddd7d55b584e027a`, verified GitHub run `44150537948` green for Java21/MySQL + Java8/MySQL + React, **not a production or entire course parity pass**. Direct read of its 21 changed files and source-dependent controls found the 24h HTTP receipt/permanent stock operationId mismatch, null-body 500 possibility, source list/filter omissions, raw optional phone at rest, absent 11 original handlers+2 image handlers, and simulated heartbeat only for reminders. User also reports 12 high npm advisories, individual packages not independently enumerated. Read [13 C2 independent file register](13_C2_SOURCE_AUDIT_AND_C3_RELEASE_GATES_2026-11-18.md) then [14 Codex C3 next implementation-only master](14_CODEX_C3_SECURED_COURSE_PARITY_AND_HARDENING_FREE_LUNA.md). This supersedes the prior C2 prompt for the NEXT engineering phase. Course A K immutable; Codex only implements/tests/pushes; no C4 or public release.

**LATEST C2 IMPLEMENTATION MASTER (2026-11-17):** C1 implementation appears on draft [PR #14](https://github.com/newonathe/OrgResource/pull/14) at `7c349a6a818f2aafd21598e76d3eb8b9faa9a8ef`. Its 17 Java21/MySQL tests are **local Codex-reported execution**, NOT run by current green GitHub Java8/React workflow. Direct source review found auto-select-first-org in `C1SessionService.establish`, contradicting approved CP32/CP35 explicit org selection. The next build is [12 — C2 atomic inventory, ledger and replay with mandatory C1 identity correction and true Extended C CI](12_CODEX_C2_STOCK_LEDGER_ATOMICITY_FREE_LUNA.md); only build/tests, no general research or C3/public hosting. Course A K remains protected.

**LATEST C1 IMPLEMENTATION MASTER (2026-11-17):** C0 implementation is now in draft [PR #13](https://github.com/newonathe/OrgResource/pull/13) at `e492aeb24623476b05ad6d1ba9f4c9792e1bd105`, based on frozen A3 K. The C0 report describes 11 local MySQL-backed Java tests and a clean extraction PASS (existing remote CI on C0 branch still validates only Course A/React). C0's A3 provenance report hashes external release artifacts; this index does not independently rehash them. **The next approved engineering plan is [11 — C1 secure identity, membership and tenancy implementation](11_CODEX_C1_SECURE_IDENTITY_TENANCY_FOUNDATION_FREE_LUNA.md)**. Codex builds/tests only; no external research or stack selection, no C2, public deployment, real outreach, merge, charge or Canvas action. Course A K remains immutable; C0 experiment remains historical source evidence.

**LATEST C0 EXECUTION MASTER (2026-11-17):** After Course A3 source K (`7f8c0746da18fd54b47f430ac1435ac36e605d67`) passes its release-receipt evidence check, execute [10 — C0 implementation-only supported-runtime compatibility proof](10_CODEX_C0_COMPATIBILITY_PROOF_FREE_LUNA.md) on a NEW feature branch **from exact K**, never altering frozen A. The initial preflight must report inaccessible A3 external release hashes honestly; C0 is an isolated executable experiment, not a supported production release or approved C1. Research ChatGPT owns research; Codex builds/tests only. No cloud/provider/Canvas actions are authorized.

**A FIRST, THEN EXTENDED C.** After reviewing the completed source-derived 00–06 packet, read [07 Course A executable freeze](07_COURSE_A_EXECUTABLE_SPEC_FREEZE_2026-11-16.md) and [08 Free-Luna Codex A1+A2 master prompt](08_CODEX_A1_A2_MASTER_PROMPT_FREE_LUNA.md). These are the **active Course A1+A2 execution specifications**; now that A1+A2 passed the latest source/CI gates, the subsequent **A3-only release master** is [09 — Codex A3 source-and-artifact freeze](09_CODEX_A3_COURSE_FINAL_FREEZE_MASTER_FREE_LUNA.md). All supersede the older modern-runtime-S1-first and all-in-one A+B prompt sequence for the Course A engineering cycles. This does not mark A built or grant automatic deployment/merge/Canvas actions. A3 release freeze is a separate reviewed gate after actual A2 acceptance; Extended C follows from exact A source SHA.

## This canonical packet

1. 01_WRITTEN_REQUIREMENTS_AND_COMPLIANCE.md — complete written academic acceptance requirements, authority, evidence and grading-safe judgments.
2. 02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md — accurate existing five-entity fields, relationships, routes/DTOs/forwarding, source vs desired behavior, stock math, states and protected operations.
3. 03_TEST_POSTMAN_DEMO_AND_SUBMISSION.md — positive/negative and concurrent acceptance, real service/image/Twilio checks, proposal/Eclipse/export/recording/Canvas actions.
4. 04_AGENT_AND_EFFICIENCY_POLICY.md — mandatory quality and token/speed/storage optimization, optional scoped subagent role/model policy, safe Git workflows.
5. 05_EERD_REPOSITORIES_AND_METHOD_SEQUENCES.md — original five-entity ER relationships, all 27 custom repository methods and exact Component flows.
6. 06_SOURCE_QA_AND_CHANGE_LOG.md — static source coverage audit, corrected conflicts and remaining runtime proof gates.
7. Root AGENTS.md on THIS documentation branch is corrected for the latest requirement hierarchy; the original O14 application branch is untouched.

**Supersession:** For *academic requirement interpretation*, this packet is the concise canonical reference. It does not delete historical proposals or replace CP34–CP41 provisional production schema/API decisions. Any future source contradiction requires a dated diff and owner review; do not silently amend.

## Current remote state and git boundary

- Main at 16e781c837dd7e1f0481fb7a01e49c27d80a91e8 is the earlier O1 source, not full app.
- O14 at 2dd336ed39cecd417a8bce3388b303e688e95dee is the actual academic implementation baseline.
- Research docs parent used here: 14635f5f3c9e213636f39b544c5627fcdbe3a0c2 on docs/orgresource2-research-continuity-2026-11-15.
- This is **documentation-only**: no Java, SQL, React, tests, cloud, credentials or academic submission were changed/performed.
- All implementation stages remain separately scoped; Codex builds/tests, research chat researches/verifies, this planning space defines and tracks exact handoffs. No background or subagent work is claimed when not invoked.
