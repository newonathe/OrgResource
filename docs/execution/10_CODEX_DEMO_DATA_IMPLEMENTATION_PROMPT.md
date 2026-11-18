# Codex prompts — Ateneo reference-backed hosted demo dataset (C6-D, C8-D, C9-D, C10-D, C11-D)

**2026-11-19. DRAFT FOR OWNER REVIEW, NOT AUTOMATIC PERMISSION TO IMPLEMENT OR DEPLOY.** This set attaches directly to C6–C11. It never supersedes AGENTS.md, the complete QUALITY_EVIDENCE_CONTINUOUS_GATE.md or the existing phase master prompts in 04_CODEX_PROMPTS_C6_C8.md and 05_CODEX_PROMPTS_C9_C11.md. Read both full master prompts for your phase and the complete relevant addendum below. If they conflict, STOP, document and reconcile through source authority/user decision; do not silently weaken either.


> **MANDATORY SUPERSESSION (USER DECISION 2026-11-19):** Every C6-D/C8-D/C9-D/C10-D/C11-D prompt below must also read `docs/execution/11_EXPANDED_CAMPUS_ROSTER_AND_HIGH_VOLUME_PLAN.md`, `data/reference/ateneo-recweek-2026-expanded-directory.json` and `data/demo/ateneo-volume-profiles-2026.json` **in full**. The former 12-org 6+6 source sample remains historical v1; the actual first hosted release target is **96** verified 2026 directory entries (72 COA-M RecWeek directory mixed governance/organization types + 24 LIONS 2026 point-person/profile references), with eight historic-only references excluded. The main directory entry count is NOT COA accreditation count and 24 LIONS is NOT proven exhaustive. **Sanggunian and MISA are dedicated maximum-volume fictional showcase tenants**, followed by 14 featured high-volume scenario entries selected by design, not proven membership rank. The 21,060–56,260 requests target is an example load, not real usage, subject to actual MySQL/backup/runtime independent tests. Older 12-count text below is superseded only regarding default full fixture count and allocation volume; all earlier privacy, ledger, QA, role, deployment and original-course restrictions remain. No real data or actual installed generator is claimed.

## Common required instructions for every one of these prompts

Repository: newonathe/OrgResource. Protected frozen academic source **7f8c0746da18fd54b47f430ac1435ac36e605d67**. C5 report base **9755ef63bd4c02a7042cb48823bf759e609357cd**, released/packaged code base **14576b57fff44c6f0112fd37d8c237ca0122efb4**, quality-standard PR #19 head **29efe86298431db16a780ae2e582b2276a699530**, execution plan PR #21. All refs are planning-time historical; refresh Git live first. **Course A must remain identical, original slides/labs/examples never rewritten, Extended work separate.** Class lesson concepts remain authoritative for academic behavior, and optional product enhancements remain additive.

**At each phase:**
1. Before work, read root AGENTS.md and **every section** of docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md; entire 00–11 execution packet, especially 08_ATENEO_HOSTED_DEMO_REQUIREMENTS.md and 09_DEMO_SEED_ALGORITHM_AND_ACCEPTANCE.md, plus exact phase master prompt. Read current assignment and syllabus, course material relevant to endpoints/JPA/GET/POST/Retrofit and as-built 28-route contracts; inspect actual C5/current Extended code, tests, scripts, migrations and QA reports. Never claim the source ZIP or unattached video frames were read if not.
2. Refresh branch, PR head/base, git status/remotes, file path/sha inventory, transitive closure, MySQL environment and tool versions. Refuse unexpected dirty or shared/user-owned database.
3. Treat data/reference/ateneo-public-organizations-2026.json as **historical v1 12-source example only**, and data/reference/ateneo-recweek-2026-expanded-directory.json as the **96-reference 2026 expanded release directory** (72 mixed COA-M directory page entries plus 24 LIONS corroborated profiles/point-person references), and data/reference/ateneo-public-programs-2026.json as **12 real publicly described program titles across six organizations**, with their source URLs/date. Program context is factual, but no project occurrence date or attached inventory transaction is verified; never attach simulated stock movement to an actual named public program as if it occurred. No genuine inventory/person/usage history exists. Reverify public sources on implementation/deployment date. Require real source for any additional outside-LIONS independent name. Do not treat COA-M membership count conflict (homepage 55 / profile 53) as reconciled fact.
4. Generate event activity **only as transparent, isolated, explicitly synthetic demo data**. UI/API/export always disclose simulation and no affiliation/real adoption; zero real person, contact, phone, email/SMS, logo without permission or borrowed image.
5. Use deterministic bounded algorithms with actual MySQL 8 ledger/service acceptance and tenant isolation, guarded explicit import into new allowlisted demo DB, no surprise re-seed/reset, correct temporal event transitions. Separate identity/security wall-clock and historical business clock.
6. Choose real available model and reasoning effort based on risk, independent reviewer at high/extra-high, deterministic tooling for file hashes/manifest. Assign one author per file, one migration owner, disjoint agent worktrees, integration owner. No unapproved next phase or public action.
7. Every changed file, affected caller/route/DTO/SQL/test must have exact blob SHA, reviewed ranges, reason, test and falsification. Every requirement maps to code and actual observed receipt, never a planned-only PASS. Report cold/warm elapsed time, database/storage/bundle bytes only when measured.
8. Follow privacy principles under RA 10173, scope/permission and source-license review; do not scrape membership lists, real contact information, student IDs or restricted rosters. A public directory is not authorization for operational data or branding.
9. **Do not reduce audit depth or acceptance criteria to satisfy time/token limits; complete all safe possible work and mark anything blocked or not run.** Never merge, force-push, spend, deploy publicly, send live SMS, contact organizations, submit Canvas or modify frozen A without explicit separate authority. STOP at assigned pass boundary.

---

# C6-D — Public Ateneo data fact and seed feasibility audit (READ-ONLY)

**START:** User explicitly authorizes C6 audit of demo requirement alongside original C6 master prompt. Create docs/evidence only on approved read-only audit branch. No code or DB changes.

**MISSION:** Establish which organization facts can safely be referenced, which features exist in the Extended source and exact contract/authority required to produce a realistic 11½-month **fictional** usage history.

**READ FIRST:** All common instructions; RecWeek 2026 directory, all 72 COA-M page entries and current source links for 24 separately corroborated 2026 LIONS entries (with remaining eight historic-only explicitly not confirmed), LIONS source, OSA branding/resources references, NPC law/IRR, existing program code and root course material. Inspect full relevant source including: extended/backend/src/main/java/edu/orgresource/extended/identity/C1BootstrapService.java, Organization.java, UserAccount.java, Membership.java; domain/Department.java, Project.java, Resource.java, ResourceRequirement.java, Allocation.java, AllocationStatus.java, ResourceType.java; service/InventoryService.java FULL relevant functions; available org creation/governance APIs; old Flyway V1–V5; source of transactional event clock; tests/postman/workflow; current demo/release/backup scripts; frontend metadata/banner/navigation. Audit material callers and transitive dependencies. Do not rely on this summary alone.

**TASKS:**
1. Verify each public organization name, profile URL, listing classification, source publication/retrieval date and the 12 public program labels separately; flag moves/renames, 55/53 count conflict, any incomplete rights/endorsement questions. Record source hash/snippet without archiving entire copyrighted site.
2. Prove current schema supports 96 correctly typed isolated demo tenants with current first-owner bootstrap restrictions. Determine the smallest safe importer design: separate new demo DB + controlled operator process and real business service or an audited fixture-only import seam; no direct invalid ledger writes.
3. Record exact API semantics and business state transitions by source/line, with proper project time / UTC conversion / idempotency / stock constraints, no guessed method names. Determine what can be backdated safely without bypassing audit and whether a new immutable simulated effective-time column is needed.
4. Design relationship between public organization reference table/manifest and real Organization row; validate whether supporting metadata needs a new Flyway migration and API, while Course A stays untouched.
5. Design two profiles: tiny CI fixture and full hosted 96-reference 11½-month fixture with MISA/Sanggunian flagships and 14 featured tiers; propose tested counts/rough storage/insert complexity as ASSUMPTION with no invented real demand. Set accepted behavior and abuse limits.
6. Threat model: impersonating an Ateneo student/org, false endorsement/real-use impression, shared guest mutation, personal-data leakage, auto-clearing real DB, forgeable isDemo flag, provider message accidentally enabled.
7. Independent critic reads sources and checks any source/classification claim and architecture/risk decisions without using author reassurance.
8. Produce full paths/source-level risk/role/DB/algorithms and an executable C8-D owner decision questionnaire (DEC-13–DEC-19). No C8 implementation before approved boundaries.

**OUTPUT:** evidence/C6/DEMO_SOURCE_AND_RIGHTS.csv, DEMO_CODE_RELEVANCE_AND_COVERAGE.csv, DEMO_PROVENANCE_CONFLICTS.md, DEMO_DB_AND_STATE_CONTRACT.md, DEMO_RISK_AND_PRIVACY.md, DEMO_FIXTURE_PERFORMANCE_HYPOTHESES.md, DEMO_ACCEPTANCE_TRACE.csv, DEMO_INDEPENDENT_CRITIQUE.md, DEMO_DECISION_REGISTER.md, DEMO_C8_D_HANDOFF.md. Every file indexed in C6 PHASE_REPORT. Gate BLOCKED on missing high-risk source reading or unresolved user source/data authority.

**STOP:** Read-only C6-D complete with owner decision request; no code/seed/migration/test claim.

---

# C8-D — Complete one-time, deterministic, sourced and privacy-safe demo implementation

**START:** Only after owner authorizes C8 and the exact **C8-D** slice, C6 public data provenance/risk questions resolved, and C7 identity/security contract approved. Base at exact accepted C7/C8 ancestor from reports; do not pick early main. New correctly based branch and one-writer ownership. Run full preflight from common instructions.

**MISSION:** Deliver in Extended product (not Course A) the complete approved seeded-demo vertical slice: actual 2026 public org reference names/categories with citations; deterministic labeled fictional department/project/resource/requirement/allocation histories; correct MySQL stock/audit; a secure import and user-visible long historical story; no real user or official Ateneo adoption claims.

**SPECIFIC CONTRACT AND SOURCE READING:**
- Input: data/reference/ateneo-public-organizations-2026.json plus data/reference/ateneo-public-programs-2026.json, each checked and immutably versioned; public program names are explanatory organization context only, not actual tracked project/stock history. 96 2026 directory **name/listing facts** only (72 COA-M page entries including other institution types; 24 verified 2026 LIONS), eight historic-only LIONS names excluded. If an org is not verifiable as listed, refuse or documented owner-approved replacement; never substitute fabricated 'COA/LIONS' classification. Treat LIONS as independent category, no phantom third category.
- Deterministic generator: fixed as-of 2026-11-19, historical period 2025-12-01 to 2026-11-18, Manila timezone; stable external keys and sub-seeds per org/project/month. Canonical ordered event graph SHA-256, strict schema validation and dry-run. No actual email, SMS, PII or images/brand marks. Use a **separate** actual runtime safe random password flow for synthetic users, not fixed seed passwords.
- **Expanded full fixture (mandatory design):** all 96 sourced references, with tier-specific exact bounds in data/demo/ateneo-volume-profiles-2026.json: Sanggunian 2,500–4,500 and MISA 2,000–4,000 synthetic allocation requests; 14 featured 600–1,400 each; 56 standard COA-page entities 120–400; 24 verified LIONS 60–240, for a global target 21,060–56,260 (NOT actual usage). Project/resource/persona counts also vary per tier; build inventory and appended ledger movements only by real validated domain transitions. Small CI profile retains 2–3 orgs/10–40 requests. Full fixture mandatory for release/restore and cannot be masked by tiny suite. Counts are design targets, never real observed figures or mandatory exact numbers if invariant and environment measurements justify adjustment. Include at least some allocated, returned, consumed, cancelled and current active states. Do not create impossible expired holds or future actions.
- Org semantics: C1BootstrapService bootstrap refuses nonempty user/org tables; do **not** loop it twelve times. Use a new reviewed fixture provisioner or supported governance creation flow with explicit demo-only environment guard and authenticated operator, not direct calls to production admin endpoint bypassing authorization.
- Database and time: maintain proper role memberships, tenant FKs, auditable non-personal actor IDs, original five domain entities intact; implement any new metadata in a newly numbered Extended Flyway script with schema constraints and dedicated migration reviewer. Always use real business transition invariants with safe simulated business Clock in an isolated import process; normal web auth/session/crypto use real current time. Never UPDATE existing stock_movement or audit_event. If normal services cannot support historical playback, stop and design/approve a dedicated safe seam with exact tests, not patch around invariants.
- New API contract: server-readable `demoDataset` metadata with dataset ID, source snapshot date, simulation window and flags, verified organization listing link per org, and no secrets; response shape/method/status/error and auth must be frozen from C6-D. Keep all existing academic GET/POST route behavior. Preserve current production domain semantics and test changes at exact SHA.
- UI: server-driven global immutable demo banner, source note on org, clear "fictional inventory and use" in resource/transactions/chart screens and exports; fictional dates in history; hover and screen-reader descriptions, responsive and keyboard. Distinguish Read-only Demo from authorized interactive demo, ensure public access cannot mutate other demo tenants or access real data. Make analytics labels read "SIMULATED" and not institutional adoption/actual savings.
- Import CLI: `--dry-run` side-effect-free; allowlisted fresh demo schema and immutable dataset ID check, explicit operator initialization token; finite memory/batch size and failure recovery; idempotent no-op on same dataset, explicit STOP on mismatched dataset; READY only after independent ledger/database/source consistency checks; fail closed if authentic source not checked or source branding rights unclear. Startup cannot auto-reset old data.
- Tests: all DEM-* rows from 09_DEMO_SEED_ALGORITHM_AND_ACCEPTANCE.md, plus C8 existing academic route/MySQL/React/concurrency/role/Playwright requirements; failing source truth or display attribution blocks hosted inclusion. Use real MySQL fresh isolated schemas; independent code and factual review.
- Efficiency: deterministic tools/scripts for manifests and hashes, high reasoning backend+DB for ledger, medium for UI, highest separate critic. No concurrent writers on Flyway/service. Keep full fixture build under CI architecture with tiny fixture for fast feedback and full profile for release/nightly acceptance; record real cold/warm build time and full import/restore bytes, not fabricated optimum.

**DELIVERABLES:**
1. Exact versioned public fact manifest and original fictional scenario catalog / generator code.
2. Guarded import program, SQL/Java tests, actual schema and API metadata contract, developer/operator docs, source and dataset checksum.
3. Complete role-safe UI with permanent disclosure, time-series illustrative chart and full cursor traversal on approved history pages, with responsive accessibility and rate-limited guest preview if approved.
4. evidence/C8-DEMO files in 09 spec, including full per-file review and all actual test receipts, independent critic, stop conditions and recovery.
5. Approved C10-D runbook specifying **separate** hosted demo DB and immutable version.

**EXIT:** Source identity accurate and provenance complete, realistic but explicitly synthetic date history populated on disposable full fixture, canonical generator reproducible, all stock/tenant/role/clock and public disclosure tests actually pass, no actual persons/branding/real provider traffic, academic frozen hash identical, no remaining P0/P1. If not, BLOCKED with exact missing tests and no hosting claim. Stop before C9.

---

# C9-D — Prove simulation correctness at scale and without hidden shortcuts

**START:** Authorized C9 and C8-D exact code/data manifest accepted. Read common sources/full phase prompts. No new org names without dated public source and approved metadata update.

**OBJECTIVE:**
- Reconstruct and test full generator with two fresh schemas; same seed yields exact logical graph hash; different seed changes hash and cannot modify first dataset.
- Validate each of 96 current-directory demo tenants, roles and all as-of-time state invariants. Recompute stock movements from raw immutable SQL independently and compare service availability, holds and issued amounts, resource total; assert consumed/returned idempotency.
- Run burst/concurrent requests, retry after lost response, invalid state and bad time bounds, SQL deadlock and interrupted importer, same-dataset no-op, mismatched dataset refusal, shared/Course A DB target refusal.
- Verify very long historical timeline, correct keyset cursor and frontend charts with real MySQL; no unbounded eager history or N+1 queries; p95 measured with repeatable fixture and EXPLAIN; benchmark DB/data/backup bytes and cold/warm load.
- Recheck proof that synthetic organization info is displayed on API/export/browser and no platform user can mistake sample allocation for a real custody record.
- Run independent second-pass critic on source truth, UI disclosure and behavior, plus complete C9 and Course A regressions; report exact SHA and executed commands.
- Choose optimizations only when measured; preserve full fixture/logical history and all mandatory checks.

**OUTPUT:** evidence/C9/DEMO_INVARIANT_REPLAY_REPORT.md, DEMO_PRIVACY_PROVENANCE_REVIEW.md, DEMO_PERFORMANCE_MYSQL_EXPLAIN.csv, DEMO_IMPORT_FAILURE_MATRIX.csv, DEMO_BROWSER_CHECKS.md, DEMO_SECOND_REVIEW.md, full C9 PHASE_REPORT. STOP before C10; no hosting authorization assumed.

---

# C10-D — Authorized hosted public showroom with source-linked demo data

**START:** Explicit C10 authorization **plus separate owner permission for internet hosting/service/cost**, validated C8-D and C9-D package/manifest/restore contract, resolved DEMO owner decisions DEC-13–DEC-19. Without authorization, prepare only a local read-only runbook and mark public actions NOT_RUN/BLOCKED. Follow the entire C10 master prompt and source gate, actual provider/hosting dated docs.

**TARGET:**
- A securely hosted separate DEMO instance/database with all 96 current-source cited and correctly typed org reference names, complete eleven-month **simulated** story and obvious disclosure.
- Public preview only sanitized read-only synthetic data; no default reused owner password; if interactive trial is authorized, isolate one tenant sandbox per user, strict quota, consent-free fictional data and TTL cleanup separate from immutable public showcase.
- TLS/secure cookies/origin/same-site rules, least-privilege MySQL runtime, guarded migration/fixture operator credentials, protected backup keys, independent off-host restore and named operator. No actual organization tenant/adoption claims.
- Initialization workflow: from clean schema, validate Flyway, source/dataset hashes, run operator dry run, explicitly import once, independently reconcile all stock/roles/history, set READY, verify exposed UI and source attribution. On failure, take site offline and restore into isolated fresh target; never silently regenerate history or delete user data.
- Distinct real production (empty for real customers) should never be auto-filled with the demo; copy fake data only if administrator explicitly opts to create a demo tenant after documented isolation/notice.
- Verify original school organization names publicly only; avoid use of Ateneo seal/official logos without rights. No live Twilio, no contacts. Screenshots use fictional accounts only and state DEMO.
- Test true hosted ingress, 403/CSRF/tenant, complete browser history, guest write denial, rate limits and audit, resets/restart, source/dataset metadata, index/DB size, monitoring and incident response, off-host restore into independent machine. If no actual host/second machine, classify NOT_RUN.
- Name, date, SHA, commands, deployment cost and rollback must reflect actual test environment, not assumptions.

**OUTPUT:** evidence/C10/DEMO_HOSTED_SOURCE_PROVENANCE.md, DEMO_ONCE_IMPORT_RECEIPTS.md, DEMO_ROLE_AND_GUEST_SECURITY.md, DEMO_BROWSER_LABELED_SCREENS.md, DEMO_DATASIZE_AND_OPERATOR.md, DEMO_OFF_HOST_RECOVERY.md, PER_FILE_SOURCE_AUDIT.csv, THREAT_REVIEW.md, PHASE_REPORT.md, and C11-D handoff. Zero unresolved P0/P1, owner-approved public conditions or BLOCKED. Never public deploy or incur expense without separate approval. STOP.

---

# C11-D — Independent product-data and ethical-source release gate

**START:** Authorized C11 full prompt and full QA gate; exact hosted executable SHA + source manifest + public fact roster version verified; C10-D host and independent restore receipts on intended environment.

**INDEPENDENT CHALLENGE:** A separate strong critic must:
1. Validate every actual organization name/listing against dated exact public source and check stale/removed organizations and unconsented official marks.
2. Test if an uninformed guest could mistakenly infer real Ateneo usage, historical adoption, org approval, real assets, or named student's activity from UI/export/API. Any such material misrepresentation is a release blocker; fix design and retest.
3. Recompute deterministic full event graph, compare current MySQL ledger, verify no negative availability, no role/tenant boundary breach, no impossible project dates, no replay duplicates; inspect forensic source of every stock movement.
4. Verify sealed demonstrator DB uses fictional personas, no real contact, SMS disabled and no public admin secrets; cannot modify other visitor state.
5. Test C10 hosted source/HTML/API/headers/TLS as actual user, source display, 11-month chart, mobile/keyboard/accessibility and data backup/off-host restore, no false stats.
6. Ensure entire mandatory C11 master report and academic CSCI course regression gate pass on final exact SHA; frozen A unchanged and student-only obligations not invented.

**RELEASE RULE:** A hosted sample may be called "production-ready demo" only after actual public security, complete synthetic data reconciliation, source rights/privacy review, independent recovery and quality evidence have been accepted for the exact SHA and explicit scope. The phrase "used by Ateneo organizations for almost a year" is forbidden unless there is actual verified lawful evidence of real organizational operation; describe it as "a simulation of approximately eleven months of resource-management workflows using publicly listed organization references." No automatic promotion, merge or deployment from this prompt.

**OUTPUT:** evidence/C11/DEMO_FACT_TRUTH_AUDIT.csv, DEMO_GUEST_MISREPRESENTATION_AUDIT.md, DEMO_LEDGER_REPLAY_AND_ROLE_MATRIX.md, DEMO_HOSTED_INDEPENDENT_BROWSING.md, DEMO_BACKUP_RESTORE.md, FINAL_PUBLIC_DEMO_GO_NO_GO.md and complete normal C11 independent report. Report false/untested findings honestly. Stop and present owner release decision.

---

## General engine-wide STOP and evidence classification

Any incorrect sourced org listing, unsourced third "independent" grouping, real personal identity/phone, unsupported actual historical inventory claim, falsified transactional history without DEMO badge, unauthorized brand logo, unsafe schema target, Course A mutation, unauthorized network action, failed role denial, negative stock, replay mismatch, missing required real MySQL test, public-origin security failure or off-host recovery not run while claiming production blocks acceptance. Mark VERIFIED only to bounded criteria with independently inspectable source and actual execution receipts; otherwise SOURCE_ONLY / REPORTED_ONLY / TEST_FAILED / NOT_RUN / BLOCKED / DEFERRED_WITH_APPROVAL. No fabricated model, efficiency or empirical impact claim.

## Expanded reference and volume addendum — explicit acceptance integrated into EACH phase

**C6-D source analyst:** For the **complete 72-entry 2026 COA-M directory page**, verify all names and file entries, compare different published COA accreditation totals (53/55/56) and distinguish ordinary organizations, student government, COA umbrella, student-service bodies, publications and other arms. Inspect every **2026 LIONS** point-person (24 unique references) and available organization profile; separately inventory eight historic-only 2026-unconfirmed names and research additional 2026 names where possible. Correct metadata with exact primary URLs and dated citation; never call this a complete accredited roster when it includes nonmembers or unknowns. Confirm MISA (COA directory) and Sanggunian (student government), and record 14 featured selections as load design rather than empirical membership ranks. Return fully machine-checkable SOURCE_REGISTER with unique ID, governance kind, evidence, current vs historical and no duplicate seed tenant, and DB/clock feasibility.

**C8-D implementer:** Use the v2 96-reference roster and tier config, strict validated schema and deterministic sorted org-by-month event graph. Maintain full org-independent RNG sub-seeds, stable logical hashes, exact transactional invariant checks, protected history and no real PII. Sanggunian and MISA get the richest plausible **fictional** timelines at their configured 2,500–4,500 and 2,000–4,000 request target ranges, with other 14 featured 600–1,400 and remaining cohorts 60–400 per tier. Large project/resource/persona counts use tier config, not repeated 12-org fixed sample values. The hosted public view shows clear synthetic and source badges; Sanggu is not listed as ordinary COA member. Only demo-only allowlisted fresh MySQL schema may be populated, first-owner bootstrap is not looped, old migrations/course code not edited. Provide tiny/whole/stress profiles and complete UI-API-Newman-MySQL-Playwright QA receipts on exact SHA. No current implementation claim.

**C9-D performance/negative reviewer:** Run the generated full 96-org instance and independently recompute ledger every tenant, flagships paged over thousands of requests, cross-tenant denial between MISA/Sanggunian/others, role and public guest checks, invalid source-year classification, repeat-import safe no-op, 11-month clock validity, no provider traffic and all existing Course A gates. Measure MySQL query p50/p95, indices, source/JAR/DB bytes, cold/warm import, current backup raw SQL 64 MiB bound, memory and actual off-host restore dependencies. Reject any shortcut by truncating history, hiding tests or using just small fixture as full proof.

**C10-D hosted operator:** Requires separate explicit hosting and cost approval; deploy a segregated empty demo database, one-time import 96 current-source tenants and independently verify MISA/Sanggu full volume, originals/source truth banners, HTTPS reverse proxy/access controls, read-only public demo and off-host full-size backup/restore. Any 64 MiB backup failure is a BLOCKER demanding evidence-based bounded streaming/size handling; cannot simply cut all data or disable checks. No shared guest-writable organization and no real contact/SMS.

**C11-D independent release critic:** Independently verify all 72+24 fact entries, LIONS roster still incomplete if no source proving exclusivity, correct Sanggunian type, never display real rank label for featured cohort, 96 source-linked entries populated in actual hosted database, thousands of fictional records in each flagship without any real-org usage implication, no real people, all policy/role/ledger/performance/recovery requirements executed on actual artifact SHA. If not, BLOCKED/NOT_RUN and no production-ready claim. The independent reviewer should attempt deliberate cross-tenant leakage, provenance confusion, spoofed government role, impossible timestamps and dishonest historical activity inference.

**Absolute evidence rule:** The source and volume JSON are manifests/config, not database rows or tests. Do not claim actual seed generation, deployed datasets, full 96-org semantic audit, code build, CI or course compliance from these files alone. They become implementation evidence only after separately authorized Codex work and independent executed receipts.
