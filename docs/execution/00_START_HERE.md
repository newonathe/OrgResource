# OrgResource — Evidence-first, token-efficient, production-oriented execution packet

Edition: 2026-11-19 (Asia/Manila). Status: DESIGN RECOMMENDATION and USER-DIRECTED planning upload; **not implementation, authorization to deploy, release certification, or a replacement for current course instructions**.

## Start here and authority

1. Read repository AGENTS.md completely and docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md completely. Both govern every subsequent audit, research, plan, prompt, implementation, test, PR, release, and independent review. This packet adds operational specificity and never relaxes either document.
2. Read this index, then 01_EFFICIENCY_ENGINEERING_STANDARD.md, 02_SUBAGENT_ORCHESTRATION.md, 03_PASS_DEPENDENCIES_AND_ACCEPTANCE.md, 04_CODEX_PROMPTS_C6_C8.md and 05_CODEX_PROMPTS_C9_C11.md, 06_COVERAGE_AND_DECISION_REGISTER.md and 08–10 Ateneo demo-data requirements/prompts plus the sourced public-organization reference JSON. Read the entire quality gate. The prompts are execution proposals pending a separate authorized phase.
3. Read the current CSCI 180.05 assignment, syllabus, actual supplied lab/slides/code, the original course proposal and final as-built records. The assignment is authoritative for assigned work, the syllabus for course context, and current Canvas announcements are a **STUDENT CHECK**. Distinguish supplied course fact from our design recommendations.
4. Relevant current Git references (refresh immediately before use):
   - **Protected Course Final A**: 7f8c0746da18fd54b47f430ac1435ac36e605d67. Immutable source and academic artifact reference.
   - Extended C5 implementation/evidence branch: feat/extended-c-c5-release-recovery-2026-11-18 at 9755ef63bd4c02a7042cb48823bf759e609357cd. C5 exact packaged executable SOURCE SHA: 14576b57fff44c6f0112fd37d8c237ca0122efb4. Report-only head is later; do not confuse its CI with packaged binary provenance.
   - Proposed comprehensive quality policy PR #19 at 29efe86298431db16a780ae2e582b2276a699530. This planning branch is based on it. PR #19 is draft, not merged. New policies remain for owner review.
   - GitHub main is an early Java skeleton and is **not** the Extended product implementation baseline.
5. Use current source and reviewed decisions as the engineering baseline; historical draft designs are evidence, not automatic binding instruction. Never pretend that a ZIP inventory equals a full semantic file review or that audio transcripts reveal recording frames.

## Distinct release targets

**ACADEMIC A:** Course Final A stays frozen; course Java 8, Spring Boot/Jersey, Spring components with @Autowired, javax validation, JPA/Spring Data/MySQL, Retrofit/Gson, scheduling, JSON, GET/POST conventions, real React and Postman are preserved. Existing verified academic operations and Resource image behavior must not regress. The assignment's five-entity minimum is a requirement, while the frozen Course A has exactly five: Department, Project, Resource, ResourceRequirement, Allocation. The 26 business + 2 image operation set is a documented amended as-built contract, not a claim that the original assignment itself specifically mandates 28 endpoints. The printed final date is 2026-12-05; current Canvas and student-only obligations must be checked separately.

**EXTENDED C:** A separate Java 21/MySQL 8 product lane under extended/backend and extended/frontend. It may add security, tenant governance, ledger storage, supporting migration tables and coherent workflows without editing course example files or the frozen academic implementation. Supporting Extended persistence tables are not retroactively reclassified as extra Course A JPA domain entities. Existing Flyway V1–V5 checksums remain unchanged.

**HOSTED DEMONSTRATION:** The owner wants a populated hosted showcase with real publicly documented Ateneo 2026 organization listing identities (curated COA-M and LIONS) and fully **fictional/simulated** operational history of approximately eleven months. The UI, API and exports disclose simulation and no association/adoption; no personal information, live provider, unlicensed logos, existing organizational data, or fabricated historical usage. The full implementation and hosting remain NOT_RUN; see 08–10 and the public reference JSON.

**PUBLIC PRODUCTION:** A future, explicitly approved deployment with a defined public-user scope, HTTPS/hosting, monitored operations, privacy, incident/recovery provisions and independently reproducible acceptance. It is **BLOCKED / NOT RUN** today. A local release candidate is not public production.

## Non-negotiable ordering: quality first, optimization second

Priority is lexicographic: (1) verified course compliance and academic integrity; (2) privacy/security and irreversible data safety; (3) transactional/data correctness; (4) required full workflow completion and accessibility; (5) reproducible operational reliability; (6) performance, build speed, resource footprint, storage and token cost. Optimize (6) only while the earlier properties remain satisfied. Never achieve speed by removing tests, hiding warnings, compressing evidence, mutating course sources, skipping security, lowering assertions, or asserting unknown results.

"Absolute" is a target for disciplined optimization, not a defensible numeric promise without benchmark baselines, defined work, workload and environment. Benchmark changes; state sample sizes, cold/warm conditions and regressions; use bounded pass/fail acceptance.

## Source-derived readiness at planning time

- C5 reports 44 Java/MySQL tests, 24 frontend unit tests, 48 Newman requests / 90 assertions, 11 Chromium journeys, hash-checked packaging and a second-process local MySQL restore. Classify as REPORTED_ONLY unless independently examined and rerun on the exact intended SHA.
- C5 explicitly reports OFF-HOST recovery NOT_RUN; public hosting, reverse proxy/TLS and live provider delivery NOT_RUN; Picomatch-related development dependency risk OPEN. The frontend production-dependency audit reportedly had zero vulnerabilities at C5; do not transform this into an unconditional security claim.
- Current deferred product decisions: borrower-account linkage, LEAD assignment boundaries, some modern v2 CRUD, full history pagination, audit browsing/export, retention/archive policy. Decide which are launch-blocking by an explicit user workflow contract, rather than presuming every optional function is required for an academic submission.
- No user interviews, institutional deployment, real adoption, real SMS, cost validation or production performance measures are established.

## Files in this packet

| File | Purpose / operational deliverable |
| --- | --- |
| 01_EFFICIENCY_ENGINEERING_STANDARD.md | Token-aware, fast build and storage algorithm, cache discipline, measurable baselines, optimization selection, anti-shortcut tests |
| 02_SUBAGENT_ORCHESTRATION.md | Agent contracts, model/reasoning selection rules, parallel boundaries, review independence, prompt/evidence exchange |
| 03_PASS_DEPENDENCIES_AND_ACCEPTANCE.md | Correctly grouped C6–C11 passes, dependencies, scope cuts, risk registers, hard exit gates and measurement plan |
| 04_CODEX_PROMPTS_C6_C8.md | Ready-to-copy full scoped master prompts for C6 read-only audit, C7 identity/security, C8 coherent domain/UI workflow slices |
| 05_CODEX_PROMPTS_C9_C11.md | Ready-to-copy full scoped master prompts for C9 reliability/performance/CI, C10 staging/off-host recovery, C11 independent release |
| 06_COVERAGE_AND_DECISION_REGISTER.md | Known source paths, inspection levels, verified/reported distinction, unresolved decisions, exact next evidence |
| 07_C5_EXACT_TREE_METADATA_INVENTORY.csv | Full 661-path C5 Git blob inventory with SHA/size and honest metadata-only semantic-review status |
| 08_ATENEO_HOSTED_DEMO_REQUIREMENTS.md | Hosted public Ateneo org references + prominently identified eleven-month synthetic history, security/privacy/source safeguards |
| 09_DEMO_SEED_ALGORITHM_AND_ACCEPTANCE.md | Deterministic historical event graph, guarded demo import, tenant/ledger invariants and DEM-* acceptance matrix |
| 10_CODEX_DEMO_DATA_IMPLEMENTATION_PROMPT.md | Copy-ready C6-D/C8-D/C9-D/C10-D/C11-D audit, implementation, hosting and release prompts |
| ../data/reference/ateneo-public-organizations-2026.json | **Historic v1 research sample** of 12 source-backed org name+classification facts; superseded for full hosted target |
| ../data/reference/ateneo-public-programs-2026.json | Twelve factual public program titles across six organizations, with URLs; **not** dates, asset ownership, or usage history |
| 11_EXPANDED_CAMPUS_ROSTER_AND_HIGH_VOLUME_PLAN.md | Full 2026 directory-coverage goal, 96 source entries, mixed governance roles and heavyweight MISA/Sanggunian synthetic data plan |
| ../data/reference/ateneo-recweek-2026-expanded-directory.json | **v2 expanded:** 72 COA-M directory-page entries (not 72 accredited organizations), 24 verified 2026 LIONS names, eight historical-only unconfirmed |
| ../data/demo/ateneo-volume-profiles-2026.json | Proposed deterministic 96-tenant simulation load tiers; no actual generated or hosted rows |

## Governance and explicit authorization

The user's instruction to upload this planning packet to GitHub authorizes a **review branch and draft PR containing documentation**, not code implementation, main merge, public deployment, paid services, institution-facing claims, live SMS, shared database changes or Canvas submission. Preserve all existing branches, unfinished work and historical results. Plan and report without pretending that proposed pass criteria are already met. A later human approval initiates each implementation phase, in the order specified by dependency and stop rules.

## Required implementation-result envelope

Every subsequent authorized pass returns: exact repo/branch/base/head and immutable A tree hashes; files touched with reviewed source ranges; requirements→code→test→evidence matrix; errors and adversarial counterexamples; reproducible commands/tool versions/runtime; passed/failed/skipped/not-run counts; CI URLs; cold/warm build and storage metrics when measured; priority-ordered P0/P1/P2 defects; fallback and rollback; independent critique; explicit approval-required actions; and full next-pass copy-ready handoff. **No empty "all good" certificate and no fabricated tools/tests.**

## Superseding 2026-11-19 source-coverage / volume decision

The earlier hosted 12-org sample is research v1. The newly approved **planning target** is as many verified public organizations as feasible, currently **96 2026 directory entities** across **72 COA-M 2026 page entries of mixed institutional types** and **24 LIONS 2026 point-person/profile names**, with eight older-only LIONS entries pending verification and excluded from default seed. Sanggunian is student government, MISA a COA directory-listed organization, both heavy synthetic flagships; fourteen featured showcases are selected by demo workflow breadth **not** actual member rankings. Versioned source and volume config are linked above. Full 96-tenant import/test/deployment remains NOT_RUN and is separately authorized in C8-D/C10-D.
