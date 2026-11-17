# CP46 — FINAL GitHub planning handoff, source/decision index and release gate

> **LATEST CURRENT AUDIT-FIRST AUTHORITY (2026-11-17):** [CP48 GitHub critical source/PR audit](79_CP48_FRESH_GITHUB_INDEPENDENT_AUDIT_AND_SOCRATIC_CRITIQUE_2026-11-17.md) + [CP49 full audit-first P00–P19 Codex implementation plan](80_CP49_COMPLETE_AUDIT_FIRST_SOCRATIC_PHASE_BY_PHASE_BUILD_AND_CODEX_PROMPTS.md) + [603-path inventory](contracts/CP48_GITHUB_TRACKED_PATH_AUDIT_REGISTER.csv) SUPERSEDE the old assumption that the next Codex step is to create A1/A2 or start from O14. PR11 A1/A2 has real code/CI; PR12 A3 source/CI success; release ZIP/JAR/DOCX manifest remain independently uninspected externally. Codex next does P00/P01 read-only audit and P02 A3 release-verification; only after actual A freeze starts C0. All source studies and critiques are documented with review-depth boundaries, not false claims of 603 files fully read.


> **FINAL RESEARCH CLOSEOUT — CP47 (2026-11-16):** The concise [what remains / next action checklist](78_CP47_FINAL_NEXT_ACTIONS_RESEARCH_CLOSED.md) is the stop-point and first thing to read when returning. General research is closed; the scope and two-release source authority here remain binding. Exact build/test evidence is still Codex work, not something established by this planning upload.


**Date:** 2026-11-16, Asia/Manila · **State:** `PLANNING_PACKET_PUBLISHED / CODE_NOT_YET_CHANGED / A_RELEASE_NOT_BUILT / B_RELEASE_NOT_BUILT / CODEX_NOT_SENT`.

This is the **one entrypoint for subsequent Codex work** in the existing private repository `newonathe/OrgResource`. It consolidates earlier research CP31–CP45 and the independently prepared academic-specific **draft PR #9** specification into a coherent **two-edition** contract. It documents what was **actually published to GitHub**; it is not a claim that the software passes any new tests, that either edition exists, or that the user has submitted it to Canvas.

## 1. Final binding decisions

**USER DECISION:** Deliver **TWO INDEPENDENTLY RUNNABLE EDITIONS** from **ONE actual Git development history**, in this order:

| | Release A — `OrgResource-Course-Final` | Release B — `OrgResource-Extended-C` |
|---|---|---|
| Meaning | The **fully finished course project** for the supplied syllabus and ESP final-project assignment | The user's **full Option C** enhanced product built from **the exact frozen A source revision** |
| Dependencies | A must run/qualify **without** B | B starts only **after A is fully tested and frozen** |
| Stack | Truly demonstrated course-compatible Jersey/JSON/`javax.validation`/Spring/JPA/Hibernate/MySQL/JPQL/Retrofit/Gson/scheduler; use **old genuine course stack if necessary** | Maintained supported runtime **if experimentally compatible** with all real course functions; never silently replace Javax/Jersey or publicly expose unsupported Boot2.1 |
| Functional scope | Original 5 domain entities; 26 actual original source routes (25 proposal+O14 A09); course image receive/store/serve, real Twilio REST adapter+FakeTwilio, complete Postman; current 8 UI pages | Retain **real execution** of those original operations + full multi-organization accounts/roles, ledger/idempotency, secure new v2 API, expanded screens, operations and recovery |
| Evidence | Own exact code SHA, independent runnable JAR+source archive, accurate proposal addendum, MySQL/runtime/HTTP/Postman/test evidence, full course acceptance | Own source SHA, independent install, proof that it **descends from frozen A**, before/after source diff, **independent repeat of course tests on B**, additional security/ledger/host/restore acceptance |
| Release guarantee | **Never rewritten** once frozen; preserving its standalone source/binary is non-negotiable | B source may need transparently reviewed adaptations; that is not the same as changing frozen A. If course behavior cannot be preserved, B fails acceptance and A still remains usable |

**COURSE AUTHORITY:** [original syllabus](../../ChatGPT-Project-Pack/UPLOAD/08_CSCI180_SYLLABUS.pdf) and [original three-page ESP assignment](../../ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf). [Original 12-page proposal](../../ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx) is project-design history (not a third grading rubric). The **course asks for at least five justified entities**, not an arbitrary 18-table minimum; it requires genuine runtime create/read/delete, Jersey Controller→`@Autowired` Component, Javax Validation, real JPQL/MySQL, JSON and complete Postman, Retrofit/Twilio external connection and scheduling. The syllabus's CLO9 lists image receive/serve, conservatively included in A. The professor has **not prescribed two independent graded submissions**, certified official delivery or approved a provider account.

**COURSE FACTS vs USER ADDITIONS:** A/C separation, the original 26-count, full C multi-org, five roles, public v2 API, permanent stock ledger, HTTP idempotency, hosted service, independent encrypted restore, and the two release names are user/design requirements, **not invented instructor mandates**. The source-specific [E01–E39 extra-scope register](73_PRE_CODEX_S0_S2_IN_CHAT_SCOPE_AUTHORITY_AND_ALL_EXTRAS_REGISTER.md) explains every major added/deferred feature.

## 2. One active Codex handoff (do not follow historical prompt order)

**ACTIVE copy-ready prompt: [75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt](75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt).**  
**Decision/phase authority: [CP44 two-edition plan](74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md) → [CP45 targeted final QA](76_CP45_FINAL_TARGETED_PRE_CODEX_VERIFICATION_AND_RESEARCH_STOP.md) → this CP46 final index.**

**HISTORICAL / SUPERSEDED:** `docs/research-2.0/64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md` and `71_CODEX_MASTER_HANDOFF_STAGED_EXECUTION_AND_LOCAL_WINDOWS_SYNC.txt` contain earlier one-release/production-first or standalone-S0 stage orders and **must NOT be executed as the current master instructions**. CP31–CP43 remain evidence and detailed contracts **only where compatible with CP44–CP46**.

**Codex executable milestones after separate authorization** (no separate preliminary Codex open-ended research pass):
1. `F0` — re-pin actual branches, source SHA and CI; safely derive A feature branch from **academic O14 PR #8**, not earlier main. Do not alter that pinned baseline.
2. `A1–A2` — implement real remaining academic work and close defects; use course-compatible Javax/Jersey stack, no speculative modernization. Run **all** actual five-entity/26-route/MySQL/Postman/image/Twilio/schedule/UI and portable-build checks.
3. `A3` — only when real independent A acceptance PASSES, freeze A source SHA, complete immutable package and verified hash manifest. Never alter the frozen A ref/artifacts.
4. `C0` — start B branch **from A's frozen source SHA** and run bounded supported-runtime `javax`/Jakarta/Jersey/Spring/JPA/React feasibility spike. **HARD STOP** on incompatible proof with exact logs; A remains safe.
5. `C1–C7` — build full C in dependency-coherent increments; run real negative org/role and stock concurrency tests, verify all 26 original operations execute on B, repeat original **A academic acceptance on B**, pass separate security/release/backup tests, then freeze independent B deliverable.
6. `S10` — student personally makes actual recording, validates real group roster/peer obligations, signs truthful Certificate of Authorship and discloses research/AI/Codex help, uploads permitted package(s) to Canvas and preserves receipt. Codex produces a **checklist**, not fabricated acts.

**Hard gates**: A acceptance failure; C0 compatibility failure; any proposed modification to A-owned source within B that changes course semantics (needs explicit owner review); real provider test/send; money; public-hosting access; destructive database work; Git merges/main; student submission. Do **not** mark automatic PASS, skip failed tests, re-label `jakarta.validation` as `javax.validation`, or claim that a dormant archived A inside B proves B is course-compliant.

## 3. Canonical academic specification consolidated in this branch

These seven source-grounded documents were **already published in separate draft PR #9** (based on an earlier research snapshot). They have been copied to the research branch **without merging or closing PR #9**, and each has a current two-edition precedence banner:

| Read order | File | Purpose |
|---|---|---|
| 1 | [00 — Read first](../academic-final/00_READ_FIRST_AND_DECISION_BOUNDARIES.md) | Written source hierarchy and verified-vs-not-run boundary |
| 2 | [01 — Academic compliance](../academic-final/01_WRITTEN_REQUIREMENTS_AND_COMPLIANCE.md) | Requirements matrix incl explicit course vs O14 proposal vs optional extras |
| 3 | [02 — Domain and 26 operations](../academic-final/02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md) | All five real core entities, real JSON and route/Component mapping |
| 4 | [05 — EERD and repositories](../academic-final/05_EERD_REPOSITORIES_AND_METHOD_SEQUENCES.md) | Actual relationships, original custom repository methods and ordered method sequences |
| 5 | [03 — Tests/demo/submission](../academic-final/03_TEST_POSTMAN_DEMO_AND_SUBMISSION.md) | 26 concrete positive/negative cases, Postman, full academic and student evidence |
| 6 | [04 — Agent efficiency](../academic-final/04_AGENT_AND_EFFICIENCY_POLICY.md) | Scoped engineering, real evidence, no fake parallel work and safe Git boundaries |
| 7 | [06 — Independent static QA](../academic-final/06_SOURCE_QA_AND_CHANGE_LOG.md) | Actual source references, discrepancies, reported test limits and historical audit |

Use original source and code to resolve a discrepancy, not historical notes alone. [O14 academic PR #8](https://github.com/newonathe/OrgResource/pull/8) at pin `2dd336ed39cecd417a8bce3388b303e688e95dee` is **currently not merged**. [Academic PR #9](https://github.com/newonathe/OrgResource/pull/9) remains **draft/unmerged**; its source material was reused transparently. [Research PR #7](https://github.com/newonathe/OrgResource/pull/7) remains **draft/unmerged**. Only this documentation branch has new handoff edits in the present task.

## 4. Detailed C product contracts — consult only as required

- [CP31 full master scope](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md), [CP32 conflicts](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md), [CP33 acceptance](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md).
- [CP34 database](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md) and [CP41 actual 35-column physical crosswalk and tenant/ledger invariants](68_CP41_SOURCE_TO_SQL_INVARIANT_GUARDS_AND_PROOF_CASES.md); original physical columns include `resource.type` and `allocation.borrower_name`. Proposed 18-table design is a **candidate**, never a syllabus requirement.
- [CP35 role/API/UI](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md), [CP36 legacy compatibility](63_CP36_LEGACY_COMPATIBILITY_ATOMICITY_AND_CUTOVER_GATES.md), [CP39 historical CI flake](66_CP39_PINNED_CI_TRIAGE_AND_LAST_CONTRACT_CORRECTIONS.md), [CP40 free-provider limitations](67_CP40_PROVIDER_TRIAL_SECURITY_AND_DEADLINE_REALITY_CHECK.md), and [CP43 strict written course interpretation](70_CP43_FIXED_WRITTEN_COURSE_SCOPE_AND_HANDOFF_GO_NO_GO.md).
- [Plain-English current screen vs future](72_PLAIN_ENGLISH_FINAL_PRODUCT_EACH_SCREEN_AND_REMAINING_WORK.md), [scope changes register](73_PRE_CODEX_S0_S2_IN_CHAT_SCOPE_AUTHORITY_AND_ALL_EXTRAS_REGISTER.md).
- Core five source `backend/src/main/java/app/entities/`, five controllers `backend/src/main/java/app/rest/controllers/`, actual course artifacts `ChatGPT-Project-Pack/UPLOAD/`. Original archived 9-entity/68-route plan is historic, **not today's active academic contract**.

## 5. Real evidence still OPEN, not more planning research

| Dependency | Real proof required | Owner/agent boundary |
|---|---|---|
| Exact A build and original 26+image runtime | Java/Jersey/`javax.validation`, MySQL/JPQL, safe delete, complete Newman's collection, image byte check, schedulers and clean portable start | Codex during A1–A3; NO new PASS claimed |
| Historical `ResourceListAggregationMySqlTest` expected 5 got 8 once | Reproduce source SHA/clock/JPA/SQL and fix without deleting/changing expected correctness | Codex A1; root cause OPEN |
| Twilio | Official transport+local protocol test; course slides show Twilio **Virtual Phone** provider scenario | No unauthorized provider trial/real recipient/charge. Optional authorized Virtual Phone evidence where eligible; if none, provider-tier `BLOCKED` |
| C modern Javax/Jakarta and course Jersey parity | B C0 executable dependency/start/JPA/HTTP/same-source tests, then B full 26 academic regression | Codex AFTER A freeze; fail-stop |
| Multi-org ledger/backups and truly zero required fees | MySQL/HTTP/Playwright/independent restore/test, actual hosting account availability with explicit consent | Not proven by research; no public Boot2 or paid host |
| Original source archive completeness | Project-only 11/12 COMPLETE_SOURCE_NONVIDEO ZIP source and original path/hash reconcile one recorded outer-file discrepancy | **Not mirrored to this GitHub branch**, no complete ZIP byte audit claimed in this task |
| Human coursework | Real student video or legitimate early presentation, actual COA and outside/AI assistance acknowledgment, real other-member peer reviews when applicable, legitimate final Canvas receipt | Student-only; not automated or claimed |

**Real repository coverage:** The **syllabus PDF, ESP assignment PDF, 12-page original DOCX, 03 course corpus and 06 timestamped audio transcript** are present at `ChatGPT-Project-Pack/UPLOAD/`. The *two COMPLETE_SOURCE_NONVIDEO ZIPs* and standalone *Twilio SMS Notification Setup.pptx* are **not in the current repository tree**. They remain Project materials; this packet DOES NOT claim to have uploaded every original attachment. Relevant slide facts were directly checked in the original Project slide through its extracted text. No raw video frames were seen.

## 6. Submission boundaries and safe local folder syncing

**Printed final date:** **2026-12-05**, no inferred extension. Primary course A must be available and submitted on time even if B's ambitious production extension is unfinished; never label unfinished B as complete. Prepare two independently named artifact folders (A primary; B supplemental); if platform permits only one file, a combined outer ZIP with both folders can be considered once actual upload limits are known. **No evidence** of instructor approval for multiple graded submissions, current Canvas receipt or real completed video exists here.

**Git:** `main` is older; `feat/o14-academic-final-acceptance-2026-11-15` is current academic source; `docs/orgresource2-research-continuity-2026-11-15` is planning; draft PR #9 academic canonical docs have been separately published. Do not merge/touch main in planning. Codex Cloud cannot synchronize a Windows local repo. On a real local machine, inspect exact `git remote -v`, `git status --short --branch`, `git worktree list` and checkout before `git fetch origin --prune`; only `git pull --ff-only` for a **clean** explicitly desired branch, or a new sibling worktree. No automatic reset/clean/stash, and no invented local path.

**Final handoff status:** Documentation is **review-ready** and GitHub-published; the two applications are **NOT BUILT**, no new runtime test is PASS, and the Codex master prompt remains **SEALED** pending a new explicit user instruction. Document upload is not software deployment, student submission, or a course grade.
