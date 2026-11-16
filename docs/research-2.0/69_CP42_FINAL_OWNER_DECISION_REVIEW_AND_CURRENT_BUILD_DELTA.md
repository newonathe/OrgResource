# CP42 — Final owner decision review and plain-language current-build comparison

**Date:** 2026-11-16, Asia/Manila. **Purpose:** actionable last paper review **before** any Codex handoff, not another conceptual specification. **State:** `READY_FOR_OWNER_REVIEW / NOT_SIGNED_OFF / NOT_IMPLEMENTED_AS_NEW_PRODUCT / S0-S10_SEALED`. This document does not itself authorize S0, S1, source edits, branch merges, hosted deployment, provider contact, purchases or academic submission.

## 1. Nontechnical end goal

**OrgResource is intended to be ONE reliable application for keeping track of an organization's shared supplies and equipment as it prepares and runs projects.** A user can see available items, create a project, record what it needs, ask to reserve resources, approve a request, see who has borrowed equipment, mark its return or consumption, and notice upcoming returns/shortages. An organization should not be able to see another organization's private records. Appropriate staff should have different permissions. Past inventory changes should be explainable, not silently overwritten.

**Fictional demonstration example:** Organization A has 10 reusable chairs. A project needs eight. A request starts PENDING and reserves nothing. Once APPROVED, eight are held and two remain available. ISSUED means eight are in custody, not newly deducted from T; RETURNED restores their availability. For 50 consumable cups, approving 20 temporarily holds 20; CONSUMED reduces physical stock to 30 exactly once. These are **illustrative product rules**, not measured school inventory or validated community demand.

The target is **not** an Ateneo-operated system, official identity directory, institutional inventory integration, procurement/finance ERP or a demonstrated adoption/pilot. Initial data/identities remain fictional. Real student data/actual use require later real operator authorization and privacy/accountability procedures. A visually attractive web interface already exists; the aim is to preserve useful flows and improve trust, correctness and safety, **not build two separate products**.

## 2. Verified current build, as opposed to the plan

**Verified from GitHub source at 2026-11-16:** `main` SHA `16e781c837dd7e1f0481fb7a01e49c27d80a91e8` is an earlier baseline. The more developed **O14 academic build** is in a separate UNMERGED draft PR [#8](https://github.com/newonathe/OrgResource/pull/8), branch SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`. The latest planning documents are in a separate UNMERGED draft PR [#7](https://github.com/newonathe/OrgResource/pull/7). This planning continuation did **not change either application branch, main or deployed systems**.

Directly read `README.md`, `frontend/src/App.tsx`, eight actual React page source files, `backend/src/main/java/app/rest/controllers/AllocationController.java`, five original JPA entity classes, five original JPA repository classes, `evidence/O14/FINAL_ACCEPTANCE_REPORT.md`, and course compliance document. O14 actually contains:
- **Eight connected academic pages:** Overview, Allocations, Resource Library, Projects, Requirements, Departments, Reminder Activity, and About, with mobile responsive/light-dark controls and loading/error states.
- **Five JPA business entities** and the **26 Jersey route signatures** (original concise proposal's 25 plus A09 revoke SMS consent), Spring Components, MySQL and Javax-era course stack.
- Project requirement shortages and reusable/consumable whole-batch allocation states, local reminder scheduling and separately running **FakeTwilio** simulation, disabled by default; real SMS not demonstrated.
- O14's evidence report records local Java/MySQL, React, Playwright, Newman and release checks as PASS, but that is **historical reported/local evidence**, and this P42 review did not rerun them; a historical exact-source MySQL aggregation test had differing same-SHA CI outcomes [CP39](66_CP39_PINNED_CI_TRIAGE_AND_LAST_CONTRACT_CORRECTIONS.md).
- **Not in O14:** real authentication/account recovery, organization isolation, member roles, immutable physical stock-movement ledger, image receiving/serving, hardened Internet deployment, encrypted off-provider clean restore, verified zero-cost hosting, real Twilio provider account or real adopted student data.
- The O14 academic runtime uses Java 8/Spring Boot 2.1 and is **NOT safe to expose publicly** as the production product. Its code is a reference/regression target, not an instruction to launch it on the Internet.

**No implementation changes during CP31–CP42.** Research files and spec corrections alone do not create UI screens, Java controllers, migrated tables or tests.

## 3. Exact change list: O14 current application → proposed ONE unified product

| Change | Existing O14 | Proposed P0 | Source/decision classification |
|---|---|---|---|
| Product and UI | One local academic workspace, 8 pages | Same resource-planning product with secure identity, org switch, members, stock history, images, account and operational screens; keep sensible existing visual direction | **USER DECISION** one product; **DESIGN RECOMMENDATION** expanded flows |
| Org scope | One fictional organization implicitly; no logins | Multiple fictional self-contained organizations; no cross-org visibility | **RECOMMENDATION**, not yet built |
| Permissions | Anyone accessing local service can operate | OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER; authorized login, session, CSRF, one-time invitation/recovery, controlled ownership transfer | **RECOMMENDATION**, not yet built |
| Academic API | 26 original numeric-ID, PHT-local `/api` routes | All 26 remain functional, but **protected by login/org/permissions**; new versioned public `/api/v2` for UUIDs/UTC/stronger version and replay control | **USER DECISION** original real behavior; secure wrapper **RECOMMENDATION** |
| Five domain types | Existing five `@Entity` classes/SQL tables | Keep five domain concepts, add justified infrastructure tables; 18 physical tables currently **candidate only**, not an irreversible table count | **COURSE REQUIREMENT** at least five; **RECOMMENDATION** additions |
| Shared inventory | Current T/H/I availability and allocation status queries | Preserve global whole-batch rules, strengthen exact concurrency controls, add immutable RECEIVE/ADJUST/ISSUE/RETURN/CONSUME ledger with provable reconciliation | **RECOMMENDATION** |
| Returns and borrowing | Free-text borrower name, no verified borrower membership; full returns | Public reusable requests select same-org ACTIVE membership; immutable name snapshot, staff-safe lookup; **no false campus identity claim**; still whole-batch returns in P0 | **RECOMMENDATION** |
| Historical deletion | Eligible zero-stock Resource can be physically deleted even if prior unrelated stock adjustments occurred | Delete truly never-used zero-stock Resource; refuse deletion of stock-history-linked row and offer archive; **intentional legacy R04 difference** needs owner/instructor judgment | **CONFLICT/OPEN COURSE GATE** |
| Images | None | One private limited resource image receive/serve route to conservatively cover syllabus CLO9 | **RECOMMENDATION; COURSE INTERPRETATION OPEN** |
| Reminders | Local FakeTwilio accepted simulator; disabled default | In-app due flags and truthful simulator; live/provider optional and separately approved, not mandatory or falsely called delivered | **COURSE-TECH REQUIREMENT** integration; **INSTRUCTOR GATE** provider |
| Hosting | Developer/local-only Java8/Boot2; no public credentials | Maintained compatible runtime verified by S1; TLS, backup, recovery, conditional zero-fee eligible hosting | **USER DECISION** no mandatory fees; **S1/ACCOUNT BLOCKER** |
| Academic delivery | Source, Postman, release/demo guide and O14 reports | Real final product used for acceptance; genuine student recording, authorship acknowledgment, submission/peer tasks based on current Canvas | **COURSE/USER DECISION**, manual verification open |

**UI changes are proposals, not edits to current screens.** The old source SQL columns `resource.type` and `allocation.borrower_name` are mapped to new public JSON names `kind` and `borrowerNameSnapshot` without falsely changing the original physical columns; see [CP41](68_CP41_SOURCE_TO_SQL_INVARIANT_GUARDS_AND_PROOF_CASES.md). The old O14 repositories are global and **cannot be copied unchanged** into a multi-org system.

## 4. P42 concise decision checklist (NOT the user's signature)

**Already binding from explicit prior user direction — preserve unless user changes:**
- [x] `D42-01 USER DECISION` **ONE** product/UI with one authoritative transactional MySQL inventory; no separate fake academic and real public application.
- [x] `D42-02 USER DECISION` Preserve original actual course functionality with five core entities and all 26 current source operations genuinely running, no paper-only substitute.
- [x] `D42-03 USER DECISION` No **mandatory** ongoing paid service; no unsupported “free forever” availability promise.
- [x] `D42-04 USER DECISION` Technical production P-ACCEPT precedes separate academic A-FINAL, unless user later expressly revises the sequence.
- [x] `D42-05 USER DECISION / SAFETY` This research phase authorizes **planning only**, not Codex, provider sends, real-data import, automatic merge to main or deployment.

**Owner's proposed P0 review — coherent defaults, NOT independently approved in this file:**
- [ ] `D42-06 RECOMMENDATION` Global resource holds, APPROVED hold effective until exclusive Project.endAt, whole-batch issue/return or consume, no future-slot booking or partial damaged/lost items.
- [ ] `D42-07 RECOMMENDATION` Multi-org fictional accounts, five named roles, 24h bearer invitations, server sessions+CSRF, last-owner/transfer protections, unverified non-Ateneo identities.
- [ ] `D42-08 RECOMMENDATION` One new public v2 API alongside the 26 original **authenticated** Jersey routes; stock ledger, permanent movement replay guard, UTC/public UUID API, protected history and encrypted optional contacts.
- [ ] `D42-09 RECOMMENDATION` Fresh production-schema install from explicit migrations and synthetic data (no stealth import or deletion of original O14 schema); 18 physical tables remain a candidate subject to S1/S2.
- [ ] `D42-10 RECOMMENDATION` Conservative real image receive/serve, app due indicators, Retrofit/Gson FakeTwilio simulator; no live SMS without provider/instructor/consent/cost approval.
- [ ] `D42-11 DECISION REQUIRED` Explicitly accept safety-driven original Resource R04 edge difference (formerly-used now-zero must archive/refuse physical deletion) **subject to instructor requirements**, or require alternate history-preserving design.
- [ ] `D42-12 RECOMMENDATION` Retain existing academic visual direction and practical eight-screen flows, add only necessary secure/stock/identity screens, avoid speculative ERP extras.
- [ ] `D42-13 ACCEPTANCE BOUNDARY` Even if technically production-style ready with fictional data, no real school organizational adoption, real personal-data processing or official university integration without independent operator/policy approval.

**Owner review can mark each as ACCEPT/AMEND/DEFER.** Marking a checklist is evidence only when the user actually provides that decision; the research tool author did not check these boxes on their behalf. Owner decision approval and Codex-stage authorization remain distinct.

## 5. Blockers, external verifications and remaining source questions

| Gate | Evidence type and precise next action | Status |
|---|---|---|
| `G42-1` **Current Canvas/instructor** | Student checks live Canvas on deadline and asks if CLO9 image must be in this project, whether supplied FakeTwilio fulfills “at the very least Twilio,” how safe-history R04 meets actual delete requirement; verify November milestones/recording/peer/COA | **OPEN**, no access/response assumed |
| `G42-2` **Modern stack** | AFTER separate authorization, run isolated S1 actual maintained Java+Jersey+Spring+JPA+MySQL+literal `javax.validation` and Jakarta, Retrofit FakeTwilio and bundled React in one JVM; report source/compiled behavior | **BLOCKED/NOT RUN** |
| `G42-3` **O14 CI bug** | Run test in isolated/full suite with DB rows, injected clock, SQL params; same-source push failed expected5 actual8 while PR passed; not a diagnosed fix | **OPEN/NOT REPRODUCED HERE** |
| `G42-4` **Host, backups, fees** | Only after owner-account authorization check actual Always Free capacity, GitHub billing/quota, TLS, clean encrypted independent recovery; stop before payment | **UNKNOWN** |
| `G42-5` **Original source archive** | Historical 1,241 filesystem entries incl 15 video vs prior non-video ZIP 1,227 outer; +1 path/hash exact ZIP reconciliation when original bytes accessible. History states generated CSV may account for it but not verified | **OPEN, noncritical to S0 if source coverage limitation explicit** |
| `G42-6` **Original O14 merge status** | GitHub shows `main` earlier, O14 PR#8 unmerged; don't report current main as modern app or as O14 deployed | **VERIFIED REPO FACT** |
| `G42-7` **Real-world borrower responsibility** | Public v2 staff selects members but not an actual borrower acknowledgment or campus identity proof; only fictional use permitted initially | **FUTURE REAL-WORLD GATE** |

**Schedule risk:** printed assignment final **December 5, 2026**; current date Nov16, **19 calendar days**. This is a high-risk path for full supported runtime, secure two-API app, identity/ledger/UI, zero-cost production recovery and then independent course acceptance. No extension or delivered submission has been verified. The user's production-before-academic order remains recorded, not silently reversed.

## 6. P42 independent verdict and exact next passes

**VERDICT `P42-PAPER-READY-FOR-OWNER-REVIEW`:** Course and O14 source commitments reconciled to one proposed design; material CP41 source/SQL and tenant-isolation mistakes corrected; remaining blockers are primarily owner/external authorization or runtime proof, not missing broad research chapters. **Do not label `P42-OWNER-APPROVED`, `S1-PASS`, `P-ACCEPT`, `A-FINAL`, course submission or production readiness.**

1. **Owner now:** Read this one file and mark D42-06..13 ACCEPT/AMEND/DEFER. Existing D42-01..05 recorded as prior user decisions, not re-solicited.
2. **Student P43:** Check current authenticated Canvas and actual past submission receipts; optionally send the drafted course question (CP40 §6) after personal review. No research agent has done so.
3. **Codex S0** (requires separately stated owner authorization): SOURCE/PR/CI audit only; no edits.
4. **Codex S1** (separate explicit authorization after S0): disposable local technology feasibility experiment; STOP if literal course technologies or one-runtime integration cannot be demonstrated.
5. **Codex S2 onward:** only after actual S1 outcome and owner architecture adoption. Follow [sealed S0–S10 prompts](64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md); full 26 original execution belongs to S5, not circularly to S1. Main merge, real provider use, public host and final submission each require their own authorization/gates.

**No automatic follow-on CP43/implementation action was triggered by writing P42.** This is the compact owner review artifact to close paper planning and focus next work on real evidence.

## Primary source basis

- Actual current O14 [root README at pinned O14](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/README.md), [O14 report at pinned O14](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/evidence/O14/FINAL_ACCEPTANCE_REPORT.md), `frontend/src/App.tsx` and eight React pages, `backend/src/main/java/app/entities/` five classes, `backend/src/main/java/app/repositories/` five interfaces and Jersey controller sources at O14 SHA `2dd336ed...`.
- Original current assignment `09_ESP_PROJECT_ASSIGNMENT.pdf` pp.1–3; syllabus `08_CSCI180_SYLLABUS.pdf` p.2 (CLO3–10), p.6 academic integrity; concise five-entity proposal `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` pp.1–12.
- [CP31 master](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md), [CP32 conflicts](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md), [CP33 acceptance](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md), [CP34 SQL](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md), [CP35 API](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md), [CP36 legacy](63_CP36_LEGACY_COMPATIBILITY_ATOMICITY_AND_CUTOVER_GATES.md), [CP37 sealed](64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md), [CP38 independent critique](65_CP38_INDEPENDENT_PRE_CODEX_FREEZE_REVIEW_AND_OPEN_EVIDENCE.md), [CP39 CI evidence](66_CP39_PINNED_CI_TRIAGE_AND_LAST_CONTRACT_CORRECTIONS.md), [CP40 external facts](67_CP40_PROVIDER_TRIAL_SECURITY_AND_DEADLINE_REALITY_CHECK.md), and [CP41 physical/tenant corrections](68_CP41_SOURCE_TO_SQL_INVARIANT_GUARDS_AND_PROOF_CASES.md).
