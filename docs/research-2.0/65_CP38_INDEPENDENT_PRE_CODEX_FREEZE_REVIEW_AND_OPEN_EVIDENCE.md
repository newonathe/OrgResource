# CP38 — Independent pre-Codex design freeze review, consolidated decisions and open evidence

**Review date:** November 16, 2026 (Asia/Manila). **Scope:** independent paper/specification review of CP31–CP37 and actual academic O14/source requirements, with official published framework/MySQL facts. **Verdict:** **DESIGN-COVERAGE READY FOR OWNER REVIEW; OWNER SIGN-OFF OPEN; RUNTIME FEASIBILITY NOT PROVEN; PRODUCT/COURSE ACCEPTANCE NOT RUN.** This is not an instruction or permission for Codex, cloud, paid trial, real Twilio, repository merge or Canvas. Final publication only in research PR #7.

## 1. What is decisively settled by primary sources and user

**COURSE REQUIREMENT** from original `09_ESP_PROJECT_ASSIGNMENT.pdf` 3pp: groups 1–3, useful Ateneo-community service, Jersey REST+JSON+Spring annotation controller→`@Autowired` Component, JPA/Spring Data, Javax Validation, Retrofit/Twilio interpreted with actual syllabus/course samples, Scheduling, ≥5 justified entities, GET read and POST submit, create/read and runtime deletion for applicable entities, full Postman, Word proposal, skeleton export/video and final recorded discussion/peer review/Canvas. Printed proposal Oct30, skeleton Nov13/14, final **Dec5 2026**, subject to actually verified Canvas updates.

**SYLLABUS FACT** from original `08_CSCI180_SYLLABUS.pdf` 9pp: CLO9 includes HTTP receive/serve images but assessment mapping conflicts between CLO9 and CLO10; Certificate of Authorship and explicit outside-help acknowledgment required; the syllabus does not itself state a blanket generative AI rule.

**VERIFIED SOURCE** at O14 SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`: actual Java8/Boot2.1/Jersey2 application with five named JPA entities, actual `resource_requirement` SQL table, 26 Jersey handlers (original 25 plus A09), MySQL, React and FakeTwilio. O14 PR#8 remains unmerged; `main` was O1 baseline SHA `16e781c...` when checked. O14 55 Java/15 UI tests observed passing in GitHub CI; 96 Newman/142 assertions and five browser journeys are author-reported local runs, not independently rerun here. One earlier same-SHA workflow failed availability expected5 actual8 and passed in another workflow; cause unknown.

**USER DECISION:** ONE user-facing product with real applicable course execution and modern safe resource operations; all users fictional in P0, no invented Ateneo approval, no mandatory recurring spend. Production acceptance before academic final under present instruction; no automatic main merge or Codex handoff until reviewed plan.

## 2. Independent audit of newly prepared canonical artifacts

| Document | Independent value | Corrects or completes |
|---|---|---|
| [CP31 master](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md) | Entire source→requirements→domain→API→stack→DevOps→tests→deadline packet | Earlier sprawling V2 drafts, non-authoritative nine-entity/68-operation materials |
| [CP32 adjudication](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md) | Future project approval vs actual issue period, anonymous invitation onboarding, API envelope, zero INITIAL, true A09 | Material source-draft contradictions |
| [CP33 acceptance](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md) | ARC-D01..A09, inventory/security/browser/deploy tests, independent risk critique | “Route exists” vs “route actually works” |
| [CP34 schema](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md) | Exact 18-table candidate, physical resource_requirement, FKs, ledger, clean-install data provenance | Invented migration or phantom movement history |
| [CP35 API/UI](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md) | Full public route/role/DTO and UI journey manifest; account disable, transfer revocation, unarchive, image quota | Missing identity/user details beyond old eight screens |
| [CP36 course adapter](63_CP36_LEGACY_COMPATIBILITY_ATOMICITY_AND_CUTOVER_GATES.md) | Original 26 no-body POSTs and Resource CAS reconciled with server movement IDs; honest changed retry semantics | False assertion that original JSON can require new `operationId` |
| [CP37 prompts](64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md) | Real copy-ready S0–S10 staged prompts with dependencies, receipt format and STOP gates | Prior vague handoff abstracts; no actual authorization |

**Mechanical internal-source check:** new CP34–CP36 relative Markdown links were resolved against the tracked research branch and none were missing at that inspection. Final index and current branch SHA must be checked again after this addendum; a link audit is not semantic validation. All items above are documents, not Java tests.

## 3. Recommended adopted-for-planning defaults (NOT user signature)

1. **Product:** one secured fictional independent OrgResource, single MySQL and same-origin React UI, modular-monolith preference; original 26 Jersey paths remain protected and genuine. No institutional endorsement.
2. **Stock:** T/H/I global reservation (no future overlap optimization); whole-batch reusable return; consumable once; project UPCOMING may approve, ISSUE/CONSUME only during active interval, RETURN even after end; status PENDING/APPROVED/ISSUED/RETURNED/CONSUMED/CANCELLED, scheduler CANCELLED reason EXPIRED only.
3. **SQL:** fresh clean schema (original five exact table names) plus justified user/membership/recovery/invitation/owner transfer, movement/audit/idempotency/session and one resource image; 18 candidate tables, not guaranteed fixed until actual ORM version. No invented import of O14's fictional data.
4. **Identity:** operator-only fictional first OWNER, invite-only account creation/join, ACTIVE memberships, roles OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER, two-party accepted owner transfer and controlled freeze on unrecoverable sole-owner identity. Server sessions/cookie/CSRF, one-time hashed recovery, no fake verified campus identity.
5. **API:** original /api numeric/PHT/unwrapped JSON and exact original bodyless operations behind sessions; public /api/v2 UUID/UTC/enveloped/ETag/idempotency, durable movement IDs, tenant safe filtered reads, bounded pagination. Native stock creates and transitions share the one authoritative domain.
6. **History:** only never-used zero Resource may be physically deleted; moved Resource retained and archived only when T/H/I zero, unarchive with checks; **explicit known R04 edge deviation** needing owner/instructor interpretation, no fake parity.
7. **Media/SMS:** secure bounded resource image upload/read/delete for conservative CLO9, 2MiB file/20MiB org recommended, local FakeTwilio simulated mode default, optional provider-mode only with real authorized evidence; A09 repeated consent revoke supported and no future send.
8. **Operations:** private PR CI, signed/reproducible source ZIP/JAR, protected CD only after review; candidate free host conditional on real eligible quotas, trusted HTTPS, independent encrypted backup/clean restore, no paid fallback or perpetual uptime guarantee.
9. **Release:** 26 Postman + public route negative cases, stock/tenant/CSRF/race/idempotency/browser/restore, production P-ACCEPT then independent course A-FINAL then student Canvas, explicit NO-GO on failure and deadline escalation.
10. **Academic integrity:** original course examples used for compatibility, timestamped audio transcript not raw video frame, no fake surveys/pilot/peer/COA/Canvas; acknowledge actual outside help honestly.

These defaults are **recommended**, not falsely called individually approved by the owner. This closes research-choice ambiguity sufficiently to seek a single high-level design review instead of a 22-question form.

## 4. Material independent critiques not paper-resolvable by writing more pages

| Severity | Exact remaining fact/choice | Risk | Required real evidence or decision |
|---|---|---|---|
| BLOCKER | Supported Boot4/Jersey/Jakarta coexistence with **literal javax.validation** and real entity `saveAndFlush` behavior | False course compliance and unsafe Frankenstein runtime | UC01–UC12 in isolated authorized runtime, real provider class, effective POM and tests |
| BLOCKER | Academic known inconsistent JPQL/resource boundary test (expected 5, got 8 once) | Invalid availability/test determinism under deadline | Actual isolated/full rerun with clock, DB session TZ, query rows, suite order |
| BLOCKER | December 5 course submission vs production-first requirement and cloud eligibility | Deadline may pass while researching feasibility | Confirm current Canvas; if needed owner explicitly revises release order, no implied extension |
| HIGH | Exact original R04 physical deletion after historical movement vs append-only ledger FK | Course edge parity conflict | Written owner/instructor interpretation, or integrity-preserving revised schema + tests |
| HIGH | Original compatibility API accepts no If-Match or caller operationId | Legacy CRUD may have stale-write or ambiguous create repeats even while public v2 is protected | Keep privileged use; test original observed semantics; public clients use safe v2 |
| HIGH | Unsupported public access to original Java8/Boot2 code | Security vulnerability/missing tenant auth | No internet exposure; modern accepted runtime and SCA before release |
| HIGH | Initial invited identity is token-possession, not campus identity | Stolen invite may be redeemed by wrong person | One-use hash+expiry+rate limit, manual verified offline channel where possible; no false officer title |
| HIGH | Production resource allocation on behalf of a borrower with no borrower acceptance | Real borrower accountability can be misrepresented | Mark prototype operator request; no real deployment until policy/consent workflow validated |
| HIGH | Live provider/SMS, quota, TLS renewal and restore availability | False zero-cost/operational guarantees | Real terms/account and managed procedure test; NO-GO if fails |
| HIGH | CLO9, Twilio and all declared JPA entities' CRUD interpretation | Grading uncertainty | Authenticated current Canvas / written instructor direction; no invented waiver |
| HIGH | Original source archives: one-path discrepancy, semantic inspection incomplete | False “all originals fully reviewed” assertion | Original ZIP exact path diff; audit only critical uninspected sources, no raw-video claims |
| MEDIUM | Image BLOBs inflate free DB backup | Hosting/DR size limits | Enforce 2MiB/file/20MiB-org and test compressed backup size/restoration |
| MEDIUM | Org-root lock serializes all in-org writers | May cap throughput | Run synthetic concurrency/performance; correct small P0 prioritized |
| MEDIUM | 18 tables, roles and public endpoints for solo academic deadline | Scope and regression risk | Strict sequence and protect core functionality; defer nonessential enhancements only |
| MEDIUM | Search, exports, logs and auth PII | Leakage even with correct row table scoping | Cross-org list/filter/history/image/receipt/export security tests and redacted logs |

**Important nuance:** the assignment explicitly says **Javax Validation**, but does not explicitly say `saveAndFlush` automatic JPA listener integration must occur. The direct JPA persistence validation is a rigorous proposed evidence gate, grounded partly in attached course examples/research, not a fabricated explicit sentence from the assignment. Likewise the syllabus's image mapping conflict remains a real source ambiguity. No outside knowledge can grant instructor approval.

## 5. Source coverage limits

**Exact directly consulted this review:** original assignment 3pp; syllabus CLO/assessment/policy; concise proposal 12pp; actual O14 Java entity classes, POM, relevant Component/controller/DTO source; latest PR #7 CP31–CP37 and O14 PR#8 source/test findings; official Boot/Hibernate/MySQL docs. The two original `COMPLETE_SOURCE_NONVIDEO` ZIPs were **previously recorded as CRC+SHA audited for 1,227 outer / 1,370 nested file entries**, but that audit was not re-executed here; not all original files were semantically individually read. Course validation PPT and javax PDF direct Project reads were attempted here but failed, so do not claim these were newly inspected. Timestamped audio transcripts contain no video frames; no raw video visual content was observed.

**Vendor facts checked Nov 16 2026:** Boot4.1.2 requires Java17+ and has a Jersey starter; Hibernate Validator9.1.6 is Jakarta and lists Boot4.1 compatibility; Validator6.2.6 retains `javax.*` but limited-support. Official MySQL8.4 documents require compatible indexed FK keys; CHECK cannot use subqueries or other tables; `MATCH` must be avoided. These vendor facts do **not** prove single-app runtime, account entitlement or that an old javax provider participates in modern JPA callbacks. Sources:
- https://docs.spring.io/spring-boot/system-requirements.html
- https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html
- https://hibernate.org/validator/releases/9.1/
- https://hibernate.org/validator/releases/6.2/
- https://dev.mysql.com/doc/refman/8.4/en/create-table-foreign-keys.html
- https://dev.mysql.com/doc/refman/8.4/en/create-table-check-constraints.html

## 6. Exact owner/instructor decisions remaining; reduce interruptions

**Owner high-level planning review** (do not infer signatures): accept ONE product with original protected 26 and public v2, global/whole stock rules, invited fictional identity, clean schema + append-only ledger, conservative image/Twilio simulator, no mandatory spend, and the known R04 safety extension. This is one coherent design package; a user may amend a material rule before handoff. **Already decided:** one product and product-first acceptance order, unless expressly changed.

**Student/instructor check:** current Canvas final module/deadline; whether the project itself must implement CLO9 image; whether local FakeTwilio suffices or actual authorized provider API necessary; how to judge safety-driven R04 deviation; actual group/peer/video/COA access; whether course demonstrator needs automatic JPA flush validation. Student should confirm before claiming A-FINAL; no message or submission sent.

**External runtime proof:** S1 UNIFIED-COMPAT-01 after owner later authorizes Codex; no amount of additional docs can prove compilation/integration. **External provider proof:** account-specific OCI/free host/Actions quotas, TLS and backup restore only after explicitly permitted access. These cannot be frozen as “passed” at the paper stage.

**Pre-Codex documentation stop condition:** do not commission endless CP surveys; treat [CP31–CP38] as the review candidate baseline. New document revision only for actual primary evidence, a demonstrable contradiction, or a material owner decision amendment. Implementation prompts [CP37](64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md) remain **SEALED**.

## 7. Exact release/go-no-go register

| Gate | Evidence required | Current result |
|---|---|---|
| P0-SOURCE | Checked source hierarchy and actual O14 26/five classes; original ZIP coverage honest | SOURCE-BASED, archive path reconciliation OPEN |
| P0-DESIGN | CP31–CP38 coherence, defaults, per-route requirements/tests, copy-ready stage prompts | CANDIDATE PAPER COMPLETE, **OWNER REVIEW OPEN** |
| P1-COMPAT | Java/Spring/Jersey/MySQL and literal javax actual runtime UC01–12 | **NOT RUN** |
| P2-IMPLEMENT | One product secured domain/API/ledger/UI, tested versioned schema | **NOT IMPLEMENTED** |
| P3-OPERATE | Authorized host, $0 proof where possible, CI/CD, HTTPS and independent DR | **NOT RUN / ACCOUNT UNKNOWN** |
| P4-P-ACCEPT | independent production security, history, backup and UI evidence | **NOT RUN** |
| P5-A-FINAL | same product actual full course/Postman/source/recording acceptance | **NOT RUN / CURRENT CANVAS UNKNOWN** |
| P6-CANVAS | true student COA/peer/video/submission and receipt | **NOT VERIFIED** |

**Overall truthful verdict:** **pre-Codex research specification consolidated, critically reviewed and ready for one substantive owner pass**, not a signed contract or verified unified stack. No Codex handoff or code change. If owner accepts, later authorize ONLY the isolated first compatibility proof—not full build by implication. If a critical proof fails, stop dependent stages, record exact evidence and present a transparent alternative.

## 8. Independent final integrity checklist (document QA, not execution)

- [x] User's one-product decision supersedes old two-product drafts.
- [x] Source five JPA names and 25+1 current operation count recorded.
- [x] Historical nine-entity/68-route draft explicitly not current.
- [x] UPCOMING approval vs in-period issue/consume distinguished.
- [x] Exactly-once physical ledger semantics and legacy server-issued IDs distinguished.
- [x] Clean fictional install chosen over invented legacy data import.
- [x] Public auth, invitation, ownership, session, role, user actions and recovery specified.
- [x] Public v2 success/error envelope, date/UUID/version/replay and extra GET detail specified.
- [x] Secure real image design and Twilio simulator/provider claims separated.
- [x] CI/CD, TLS/backup/restore, cost/privacy risks and NO-GO acceptance specified.
- [x] 26 original route positive/negative and synthetic tests specified.
- [x] 11 staged Codex prompts sealed and source-referenced.
- [ ] Owner review signature/explicit approval.
- [ ] Actual current Canvas/instructor directions.
- [ ] JPA javax/Jakarta supported runtime test.
- [ ] O14 flaky test root cause.
- [ ] Exact original ZIP path-set reconciliation and course-critical uninspected source follow-up.
- [ ] Real host/Twilio/CI entitlement, build, test, restore, video and submission evidence.

This checklist states content and review completeness; unchecked items are real unknowns, not tasks to claim complete without observation.
