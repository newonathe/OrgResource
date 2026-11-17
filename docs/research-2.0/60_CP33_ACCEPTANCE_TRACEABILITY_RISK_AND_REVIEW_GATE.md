# CP33 — Final pre-Codex acceptance traceability, independent risk review and review gate

**Dated:** 2026-11-16 PHT. **Artifact type:** planning/QA specification ONLY. **Not:** executed tests, validated deployment, repo merge, verified university adoption, recording, Canvas submission, or request to invoke Codex.

**Read-first order:** [CP31 unified planning packet](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md) → [CP32 adjudication/fixes](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md) → **this CP33** → [CP30 latest user goal](54_CP30_ONE_UNIFIED_ORGRESOURCE_DECISION_AND_CONFLICT_REGISTER.md) and [course matrix](55_CP30_ONE_PRODUCT_COURSE_AND_PRODUCTION_COMPLIANCE_MATRIX.md) → exact original assignment/syllabus/12-page proposal → pinned O14 academic source. If CP29/old v2 contradicts CP32, **do not take the old row as current design authority**.

## 1. Finish definition: four separate propositions

| Verdict | Meaning | Status at this audit |
|---|---|---|
| Planning content coverage | All major areas are documented, route/business semantics reconciled, tests specified, concrete default design recommended | **YES — candidate packet CP31+CP32+CP33**, still owner review |
| Technical feasibility | Maintained runtime can actually run Jersey/Spring/MySQL/JPA with literal javax, modern auth, 26 protected routes and same DB | **NOT TESTED** |
| Engineering acceptance | Unified code actually implemented, full tests and deploy/restore performed | **NOT DONE** |
| Academic acceptance/submission | Instructor/Canvas directions checked, actual demo/group obligations/COA/peer done and receipt obtained | **NOT VERIFIED** |

These statements must never be collapsed into “all done.” The user's request is to **complete planning before Codex**, not to fabricate downstream executable evidence.

## 2. Exact existing academic route and required future acceptance IDs

**Original controller inventory:** five actual controller classes on O14 SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`. The **25 proposed operations + A09 = 26** current source handlers must remain genuine authenticated/Jersey endpoints on ONE future product. Their original domain DTO and protected-path semantics differ from public v2 envelopes/versions. A `26` static Postman folder list is **not** equivalent to 26 proven HTTP requests.

| Test | Existing academic operation | Future positive live proof | Must-fail negative/persistence proof |
|---|---|---|---|
| ARC-D01 | POST /api/departments | Create Department 201, actual row | duplicate normalized key 409, foreign tenant cannot create via forged context |
| ARC-D02 | GET /api/departments | List correct scoped departments, JSON [] | anonymous 401, org B data absent |
| ARC-D03 | POST /api/departments/{id}/update | Change name, return view | duplicate 409 and no partial update |
| ARC-D04 | POST /api/departments/{id}/delete | Delete unreferenced Department, row gone | linked Project 409, FK intact |
| ARC-P01 | POST /api/projects | Future start/end, real same-org Department FK, 201 | start<=clock or end<=start 400, foreign dept hidden |
| ARC-P02 | GET /api/projects?departmentId= | Scoped list/filter | foreign Department cannot leak/filter |
| ARC-P03 | POST /api/projects/{id}/update | Name change; date change only when allowed | dates locked after any historical approval, unchanged on 409 |
| ARC-P04 | POST /api/projects/{id}/delete | Delete Project without Requirements | referenced Project 409, no cascade |
| ARC-R01 | POST /api/resources | Create kind, quantity, correct view/ledger initial | invalid total/kind, duplicate normalized name; no double INITIAL |
| ARC-R02 | GET /api/resources?type= | Filter and compute T-H-I/T-H accurately | no org leakage; invalid filter handled |
| ARC-R03 | POST /api/resources/{id}/update | `name,totalQuantity,expectedTotalQuantity`, atomic CAS + change delta | stale expected 409; below holds/custody 409; ledger invariant |
| ARC-R04 | POST /api/resources/{id}/delete | Zero unused/unreferenced record physically deleted | referenced or nonzero 409; prior-movement zero 409 safety extension documented |
| ARC-R05 | GET /api/resources/{id}/availability | Exact `resourceId,availableQuantity` | q4+q4 T5 race still shows available1; no phantom hold |
| ARC-Q01 | POST /api/requirements | Same-org Project/Resource pair, 201 | duplicate pair or ended Project refused; raw FK cross-tenant rejected |
| ARC-Q02 | GET /api/requirements?projectId= | Exact covered and shortage | expired APPROVED removed from coverage before cleanup |
| ARC-Q03 | POST /api/requirements/{id}/update | Quantity updated if >= covered and largest pending | below either 409, old quantity retained |
| ARC-Q04 | POST /api/requirements/{id}/delete | Empty Requirement deleted | any allocation history 409 |
| ARC-A01 | POST /api/allocations | PENDING created with free-text borrower, optional consent | invalid/ended Requirement; no borrowed quantity yet |
| ARC-A02 | GET /api/allocations?requirementId=&status= | exact statuses/filter, redacted phone | no phone body/foreign borrower exposure |
| ARC-A03 | POST /api/allocations/{id}/approve | UPCOMING or IN_PROGRESS until end; q <= shortage & available | exact at-end 409; competing q4 T5 exactly one success |
| ARC-A04 | POST /api/allocations/{id}/issue | REUSABLE, IN_PROGRESS, APPROVED→ISSUED, I increases | before start/at end/wrong kind/wrong state 409, no duplicate ISSUE |
| ARC-A05 | POST /api/allocations/{id}/return | ISSUED→RETURNED whole usable, including after project end, I decreases | prior to issue or repeat 409; no T double credit |
| ARC-A06 | POST /api/allocations/{id}/consume | CONSUMABLE, IN_PROGRESS, APPROVED→CONSUMED, T decreases once | wrong kind/outside time/repeat 409; rollback on failed flush |
| ARC-A07 | POST /api/allocations/{id}/cancel | PENDING or APPROVED→CANCELLED; release effective hold | ISSUE/RETURN/CONSUME cannot cancel; history unchanged |
| ARC-A08 | POST /api/allocations/{id}/delete | Only never-approved eligible PENDING/CANCELLED may delete | historically approved even cancelled 409 |
| ARC-A09 | POST /api/allocations/{id}/revoke-sms-consent | Clears consent/contact and future eligibility | unauthorized ID denied; no in-flight guaranteed-unsend claim |

**All ARC cases are currently defined as future unified-product execution checks; no new ARC results have been run.** O14 source/local reports and existing CI results are separate historical evidence.

## 3. Scenario and quality matrix

| Case | Fixed fixture/trigger | Exact acceptance |
|---|---|---|
| INV-01 | Reusable T5, two q4 APPROVE in parallel with q4 shortage on two valid projects | Exactly 1 committed; other 409; H4/I0/T5/available1; requirement coverage tied to winner |
| INV-02 | APPROVED reusable q4, then ISSUE during active interval | H0/I4/T5/available1; one ISSUE movement; duplicate operationId returns receipt not +4 |
| INV-03 | RETURN q4 after Project ended | H0/I0/T5/available5; covered q4 remains; one RETURN event; repeat refused |
| INV-04 | Consumable T5 APPROVED q3 and CONSUME | T2/H0/available2; one CONSUME movement delta_total=-3 |
| INV-05 | APPROVED allocation ends exactly at endAt before scheduler | H0, no coverage, status still APPROVED with expiredPendingCleanup true; ISSUE/CONSUME refused |
| INV-06 | Scheduler once/two workers/restart | Exactly one PENDING/APPROVED→CANCELLED(EXPIRED), no physical movement, ISSUED unchanged |
| INV-07 | RECEIVE q3 with same operationId after 48h/restart/HTTP receipt expiry | T grows only 3 once; mismatched fingerprint 409; audit exactly one physical write |
| INV-08 | Resource R03 total CAS with same number and renamed display | No zero-delta ghost stock movement; original expectedTotalQuantity preserved |
| SEC-01 | No session; wrong role; org B guessed UUID and numeric ID in protected /api | 401/403/404 as appropriate; no tenant data in body, list or filter, no write |
| SEC-02 | Login preauth CSRF, role update, logout, recovery and global disable | CSRF denial for mutation, session fixation prevented, revoked session denied on next request |
| SEC-03 | Anonymous invite redemption using saved one-time token | New account+membership exactly once; no verified identity claim; race one winner |
| SEC-04 | Existing account redeems org B invite | User has separate role per org; switching activeOrg doesn't leak A; no duplicate membership |
| SEC-05 | OWNER transfer, last-owner disable, reset race across 2 orgs | At least one effective ACTIVE OWNER per ACTIVE org, atomic transfer or whole rollback |
| DB-01 | Deliberately inconsistent org/resource/requirement/allocation triple inserted via SQL | FK rejects, no orphan or cross-tenant movement |
| DB-02 | Invalid javax annotated DTO and actual JPA entity invalid saveAndFlush in selected backend | Genuine javax violation evidence and transaction rollback, independently characterize automatic vs bridged checking |
| DB-03 | Immutable ledger from INITIAL/RECEIPT/ADJUST/ISSUE/RETURN/CONSUME | Reconstructed T/I match stored Resource/allocations after randomized sequences |
| UI-01 | UI connected to real API/MySQL, reload, slow/failed network | No mocked acceptance, user sees loading/error/retry/unknown response; no blind new operationId |
| UI-02 | 360/768/1440px, light/dark, keyboard and reduced motion | All core actions reachable, no overlap/clipping, proper focus/semantic labels |
| MEDIA-01 | Upload/read actual sample JPG/PNG/WebP; corrupt, oversized, wrong MIME | Allowed image works, malformed/traversal/HTML/oversized denied, no public org leak |
| INT-01 | Retrofit/Gson to separate FakeTwilio plus scheduled due reminder | Real loopback HTTP, parse, single claim, SIMULATED/UNKNOWN not DELIVERED |
| INT-02 | Actual authorized Twilio account test (if permitted/required) | Exact provider mode/request/result recorded, no real carrier status fabricated |
| OPS-01 | Private CI and reviewed release with SHA-pinned artifact | Build, Java/MySQL/Playwright/Newman, redacted checks, no deploy on untrusted PR |
| OPS-02 | TLS cert issue/renew/reload, host restart, private MySQL port | Real hosted readiness and proper alerts; no “always free forever” proof claim |
| OPS-03 | Off-provider encrypted backup and clean restore | Restored FK/ledger/auth material checked; old sessions/invites/recovery invalid; measurable RPO/RTO |
| ACADEMIC-01 | Clean source/JAR/ZIP/Java import/real Postman/video/COA/peer/Canvas | Real student evidence and original instructions, actual receipt, no fabricated authorship |

All cases have status **NOT EXECUTED ON A UNIFIED PRODUCT**. A case may be marked PASS only with exact source SHA, environment, command/test evidence, expected/actual, run logs, and reviewer classification. “It is in the specification” is **not** a passing test.

## 4. UI and workflow coverage completion

The existing academic O14 React UI has Overview, Departments, Projects, Resources, Requirements, Allocations, Reminders and About. **A unified production design also needs** login, invitation redemption, session/account settings, organization switching, member/role management, owner-transfer review, stock receive/adjust and activity/exports. These may be nested screens/dialogs within the same site, not separate products; their omission cannot be excused by saying the original eight pages exist. **No separate campus/OSA/CFMO portal** or true official identity is permitted.

Required P0 journeys:
1. Offline fictional operator initializes Organization A and first owner exactly once → account login/CSRF → workspace selection.
2. Owner issues role-scoped invitation → new fictional account redeems, logs in → receives one permitted membership.
3. Owner creates Department/Project, custodian creates Resource, lead requests Requirement/Allocation; owner/custodian approves, issues, receives whole return and sees audit ledger and availability.
4. Consumable request approved/consumed, stock deducted once; second retry/reload cannot deduct again.
5. Different user/org B tries guessed IDs and export/image/operation receipts and never sees Org A data.
6. Owner reviews member suspension/transfer; recovery and disabled session stop unauthorized actions.
7. Scheduled expired PENDING/APPROVED records reconcile without erasing ISSUED custody; reminder simulator records accurate outcome.
8. Export scoped data, upload authenticated image, restart service, restore clean DB in isolated target and verify stock/auth; record all real outcomes.

## 5. Physical data integrity and retention acceptance

**Candidate 18 tables:** original Department, Project, Resource, Requirement, Allocation plus Organization, UserAccount, LocalCredential, RecoveryCode, Membership, Invitation, OwnershipTransfer, StockMovement, AuditEvent, IdempotencyKey, Spring Session + attributes, ResourceImage. Exact SQL migration **not implemented**. Detailed proposed columns in [CP31 §7](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md) and [CP32 §E](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md), with historic [CP29 schema](53_CP29_MYSQL_IDENTITY_HISTORY_DR_CONTRACT_CANDIDATE.md) retained only for non-superseded fields.

**Integrity constraints that must be SQL-enforced where possible:** FK same org for Department→Project, Project/Resource→Requirement, Requirement+Resource→Allocation, Allocation+Resource→Movement; unique normalized names within tenant, unique project-resource Requirement, single account membership per tenant, unique `(org_id,operationId)`, compatible immutable history. Multi-row balance and sole effective owner need transactional service checks and adversarial concurrency trials, not ordinary CHECK. Public migration must explicitly transition O14 Philippine-local times and historical records without inventing membership provenance. No dangerous `ON DELETE CASCADE` for audit/movements. Encrypted contacts optional/disabled by default and not restored as plaintext logs.

**Security retention:** passwords adaptive hashed, recovery/invite tokens hashed one-time, sessions revocable, no public email or officer verification claims. Pending invitation expiration and audit/export retention policies must be documented in a concrete later DB migration/ops plan and satisfy privacy minimization. P0 uses synthetic data by default. Organization status ACTIVE/FROZEN; no unimplemented closure lifecycle. Resource ARCHIVE only if safe per policy and still discoverable to authorized audit/exports.

## 6. Independent critique of CP31/CP32 final planning

| Severity | Critique / remaining uncertainty | Why it cannot be paper-certified | Required decision or proof |
|---|---|---|---|
| BLOCKER | Literal javax Bean Validation vs supported modern ORM's native Jakarta lifecycle | Vendor compatibility table is not actual OrgResource runtime | Isolated UC01–UC12; no full build if fail |
| BLOCKER | New signed one-product requirement and Dec5 production-first sequence conflict with limited time | Deployment depends on accounts, host capacity, unpredictable integration | If no compatibility GO by Nov19, explicit user schedule tradeoff; preserve O14 |
| BLOCKER | Zero-spend 24/7 production with unrestricted carrier SMS and perpetual free CD cannot be guaranteed | External quotas/terms/trial expiry | No paid upgrade; fail closed and state constraints |
| HIGH | Public resource deletion after historical stock movement diverges from original course's eligible zero-resource deletion | Integrity vs exact parity cannot both be asserted without an approved interpretation | Demonstrate at least never-used deletion; document conflict and review |
| HIGH | Authenticated numeric compatibility API is a necessary behavior extension | Old unprotected API must not be published unchanged | Compare exact original 26 behavior under activeOrg session |
| HIGH | Original O14 flaky availability fixture expected5/actual8 | Underlying SQL/JVM/clock/test order cause not established | Repeat with isolated DB, inspect injected clock, query and rows; no test skip |
| HIGH | Uncertain provider SMS after consent revocation already preparing send | External send cannot be rolled back | Best-effort before HTTP, UNKNOWN honest, no auto retry |
| HIGH | Original source ZIP entry discrepancy and raw videos not inspectable | Byte-manifest does not equal full semantic review | Path-set diff; original local video frame inspection only if needed |
| HIGH | Free recovery may depend on storage/account/key access not in source | Backup existence != successful restore | Independent clean restore and external account proof |
| MEDIUM | Excess complexity of 18-table model/identity/member workflow for one course deadline | Candidate schema unbuilt | Defer optional features only; preserve mandatory functional/security checks |
| MEDIUM | Snipe-IT/spreadsheet alternative not empirically benchmarked | Research describes public features, not same-fixture hands-on run | Treat OrgResource as academic hypothesis; optional later evaluation |

**New critical insight:** preserving a named “original 26 route” catalog is insufficient when legacy request fields encode unverified borrowers. It needs a safe account-scoped adapter that keeps **the original behavior's meaning** without allowing a client-supplied name or numeric tenant context to create an authenticated identity. CP32 fixes this at the design level; it still needs executable proof.

## 7. Reproducible evidence register and source limitations

- COURSE: `ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf`, `08_CSCI180_SYLLABUS.pdf`, `10_ORGRESOURCE_CONCISE_PROPOSAL.docx`. Course source details checked against CP31 and direct file reading. Printed proposal Oct30, skeleton Nov13/14, final Dec5. Current Canvas is NOT accessible.
- USER: latest CP30 single-product decision [54](54_CP30_ONE_UNIFIED_ORGRESOURCE_DECISION_AND_CONFLICT_REGISTER.md), superseding older two-final-products texts.
- ACADEMIC SOURCE: PR #8 SHA `2dd336ed...` holds actual O14 five entities, 26 Jersey controllers and Java8/Boot2.1 backend. It remains draft/unmerged; `main` O1 at `16e781c...`. [O14 report](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/evidence/O14/FINAL_ACCEPTANCE_REPORT.md) reports 55 Java,15 UI,96 Newman/142 assertions,5 Playwright. CI observed Java/frontend green at pinned SHA, not all reported local suites re-run independently.
- CONTRARY EVIDENCE: original O14 availability test had one same-SHA push failure expected5/actual8 and PR success; root cause unresolved. [CP14 report](42_CP14_O14_JAVA_MYSQL_INTERMITTENT_CI_FAILURE.md).
- SOURCE CORPUS: two original COMPLETE_SOURCE_NONVIDEO ZIPs earlier hash-audited (1,227 outer files,1,370 nested files), but CP31 did **not** independently rerun raw ZIP path reconciliation or semantically inspect every member. The +1 count difference against historical dossier remains OPEN. Timestamped V03 audio transcripts do not show video frames; do not claim recording-view evidence.
- VENDOR: dated official Boot/Jersey/Hibernate Validator/Twilio/OCI/GitHub Actions/IP TLS research [CP30 §56](56_CP30_SINGLE_RUNTIME_AND_ZERO_COST_CICD_FEASIBILITY.md) and CP32. These facts do **not** prove private account eligibility or complete end-to-end operation.

## 8. Decision freeze sheet (all paper defaults prepared, human/real-world gates distinct)

| Ref | Recommended design baseline | Evidence state |
|---|---|---|
| BD-01 | One public product, one authoritative MySQL, one UI/release line, preferred one JVM | USER DECIDED, one-JVM unproven |
| BD-02 | Original five JPA types + all 26 genuine protected course routes, Javax/Twilio/scheduling/Postman | USER/course required |
| BD-03 | Fictional invite-only organizations, Department distinct, global holds/whole usable returns | Recommended |
| BD-04 | Future approval allowed until end; issue/consume only active window; no Project persisted workflow state | Source-aligned recommended |
| BD-05 | OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER; offline first owner; two-party transfer | Recommended |
| BD-06 | Anonymous one-use invite registration plus authenticated join, no verified-campus claim | Recommended |
| BD-07 | Public v2 envelope, UUID/UTC, explicit active org, cookie/CSRF, version/Idempotency-Key | Recommended |
| BD-08 | 18-table candidate, composite tenant FKs, durable stock ledger, strict stock history | Recommended; SQL unbuilt |
| BD-09 | Minimal secured real image upload/read and honest reminder adapter modes | Recommended; course/external verification needed |
| BD-10 | Private quota-capped CI/CD, free-host candidates, encrypted off-provider restore, no paid fallback | User constraint + recommended; actual external gate |
| BD-11 | Full product-first technical acceptance before academic final and student Canvas | USER DECIDED, severe schedule risk |
| BD-12 | No original O14 source edits, main merge, Codex handoff, cloud creation or SMS from this research phase | Active control |

**Owner review:** BD-03 through BD-10 are coherent **recommended selections**, not a claim the user individually signed every sentence. A later substantive contrary instruction updates this table with date and affected acceptance, not silent auto-revision.

## 9. Pre-Codex completion and future execution stage gates

**Now / research stage P0 (this work):** CP31, CP32, CP33 source-grounded read-first packet; every original route and major workflow has a traceable acceptance ID, known contradictions adjudicated, alternatives and costs/risks distinguished. No Codex invoked. This can be declared **paper-level consolidated** (review candidate) but **NOT independently owner-signed or physically proven**.

**Future (only after explicit handoff):**
- S0 read-only exact SHA/corpus/PR audit, no code.
- S1 isolated supported ONE-runtime UC01–UC12, real compiled/effective dependencies, real DB/Jersey/Javax and security, stop-on-fail.
- S2 architecture/DDL/API migration freeze after S1 evidence.
- S3 identity/tenant, S4 ledger/transactions, S5 26 routes/schedulers/provider/image, S6 full connected UI, S7 private CD/host/TLS/DR.
- S8 P-ACCEPT independent runtime and restored hosted state.
- S9 A-FINAL same product, every source course requirement and real student demo.
- S10 student Canvas/COA/peer/recording, actual receipt.

**Deadline:** original assignment final **Dec 5, 2026**; current Canvas can change it. If production-first sequence has not proven feasible by **Nov 19**, raise explicit schedule/architecture decision immediately; do not assume an extension, an implementation pass, or production readiness. Optional media flourishes, time-window optimization, damaged/partial returns, SSO, payment, analytics and school integrations remain deferrals. Academic code/security/stock correctness/traceability cannot be marked accepted without tests.

## 10. Final independent verdict

**PRE-CODEX PAPER COVERAGE:** broadly complete, with explicit default choices, corrected contract contradictions, route/acceptance traceability and bounded future experiments. **NO-GO for calling runtime compatibility “proven,” production “ready,” or course submission “done.”** **NO-GO for broad Codex build handoff** before explicit authorization and supported compatibility evidence. No additional unbounded research survey is necessary to *define* a candidate architecture; actual engineering and external observation supply the outstanding truth.

This file adds independent test/critique coverage and does not modify the originally submitted proposal, user’s academic baseline or any executable source.
