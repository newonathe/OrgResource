# OrgResource — Initial coverage register, conflict ledger and unresolved acceptance evidence

Edition 2026-11-19. **Planning-time partial source register, NOT C6 full semantic audit.** This register must not be construed as evidence that all repository lines or files were inspected. Companion 07_C5_EXACT_TREE_METADATA_INVENTORY.csv enumerates **661 Git blob paths** on C5 report head 9755ef63bd4c02a7042cb48823bf759e609357cd with exact Git SHA and byte size, each explicitly tagged METADATA / UNREVIEWED_BY_THIS_INVENTORY. It is deterministic Git metadata, not manual review. New planning branch files are not in that older C5 inventory by design.

## 1. Coverage boundaries and examined evidence

Scope / source | Observed status at planning | Depth / evidence | What is NOT claimed
--- | --- | --- | ---
GitHub repository and C5 / PR19 refs | Live connector checked on 2026-11-19 | Ref/PR metadata | Current local worktree state not independently accessible via GitHub
C5 tracked tree: 661 blob paths | COMPLETE metadata inventory uploaded separately | METADATA only | No 661-file manual semantic audit
AGENTS.md on C5 and PR #19 | FULL source read | GitHub fetch_file content | Not automatically effective on main
docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md on PR #19 | FULL source read | Exact source content | Not an automatic GitHub branch protection rule or retrospective certificate
evidence/C1–C5 PHASE_REPORT.md | Selected full/relevant ranges inspected | Prior turn connector, reported execution | No independent local rerun during this planning upload
docs/EXTENDED_C_RELEASE.md | FULL source read | GitHub fetch_file | No public operator or off-host exercise
.github/workflows/verify.yml | FULL source read | GitHub fetch_file | Green CI alone cannot certify all behavior
extended/backend/pom.xml | FULL source read | GitHub fetch_file | Effective runtime dependency tree not independently rerun
extended/backend/src/main/resources/application.properties | FULL source read | GitHub fetch_file | Hosting/proxy operational correctness not exercised
extended/backend/src/main/java/edu/orgresource/extended/security/C1SecurityConfiguration.java | FULL source read | GitHub fetch_file | External penetration testing not run
extended/backend/src/main/java/edu/orgresource/extended/security/C1SameOriginFilter.java | FULL source read | GitHub fetch_file | Trusted HTTPS reverse-proxy behavior unverified
extended/backend/src/main/java/edu/orgresource/extended/security/C1AccountSessionFilter.java | FULL source read | GitHub fetch_file | Staging request path not exercised
extended/backend/src/main/java/edu/orgresource/extended/identity/C1AttemptThrottle.java | FULL source read | GitHub fetch_file | Real distributed attack protection unverified
extended/backend/src/main/java/edu/orgresource/extended/scheduler/C1ReminderScheduler.java | FULL source read | GitHub fetch_file | Live provider delivery not tested
extended/backend/src/main/java/edu/orgresource/extended/service/C3ReminderService.java | FULL source read | GitHub fetch_file | Real carrier delivery not tested
extended/frontend/package.json | FULL source read | GitHub fetch_file | Latest registry advisory status not independently checked in current upload
extended/frontend/vite.config.ts | FULL source read | GitHub fetch_file | Public deployment behavior not checked
extended/backend/src/main/java/edu/orgresource/extended/service/InventoryService.java | Relevant ranges/structure inspected | Prior turn source excerpts | Full 867-line semantic audit and all callers NOT DONE
extended/backend/src/main/java/edu/orgresource/extended/service/C1Service.java | Relevant ranges/structure inspected | Prior turn source excerpts | Full 464-line semantic audit NOT DONE
extended/backend/src/main/java/edu/orgresource/extended/security/C1OrganizationContext.java | Relevant ranges/structure inspected | Prior turn source excerpts | Full inter-module security review NOT DONE
extended/frontend/src/pages/ResourcesPage.tsx | Relevant ranges/structure inspected | Prior turn source excerpts | Full 384-line UI audit NOT DONE
extended/frontend/src/pages/AllocationsPage.tsx | Relevant ranges/structure inspected | Prior turn source excerpts | Full UI and E2E audit NOT DONE
extended/frontend/src/api/extended.ts | Relevant ranges/structure inspected | Prior turn source excerpts | Every request/response schema still needs exact test reconciliation
Other Extended executable paths and test sources | Listed in C5 exact tree | METADATA | Need C6 full/range source reading with paths and SHA
Original assignment PDF and syllabus PDF | File supplied in ChatGPT Project; earlier repository academic mapping inspected | SOURCE_EXISTS; exact page-by-page review in THIS upload NOT_RUN | C6 must fetch original current documents and inspect rendered course pages when necessary
Video transcripts and original archives | Project supplies transcripts and non-video ZIPs; repository manifests exist | SOURCE_EXISTS / applicable source needs fresh C6 audit | Do not claim video frames reviewed or all archive entries read
Course live Canvas announcements | No connection or student verification here | NOT_RUN / STUDENT_ACTION | No current due date or instructor acceptance claim beyond printed assignment

## 2. Minimal authority matrix and conflict candidates

ID | Authority label | Record | Reconciliation needed
--- | --- | --- | ---
COURSE-A-FREEZE | USER DECISION / CODE REFERENCE | Frozen A SHA 7f8c0746da18fd54b47f430ac1435ac36e605d67 | Verify Git tree hashes of protected directories at every Extended pass; no mutation of original course materials
COURSE-CORE | COURSE REQUIREMENT | Java/Jersey, Spring @Autowired components, JSON, JPA/MySQL, Javax Validation, Retrofit/Gson and scheduling; GET for retrieval/POST for submission; five justified domain entity types minimum | Confirm exact wording from assignment/syllabus/current Canvas before academic claims
COURSE-ROUTES | CODE REFERENCE / REVIEWED AMENDMENT | Course A as-built counts 26 business + 2 Resource image operations; original earlier proposal had 25 business operations, with A09 later amendment | Do not confuse original proposal count with as-built or claim the syllabus mandates exactly 28
VERSION-LANES | USER DECISION / CODE REFERENCE | Course A Java 8/Jersey 2/javax; Extended separate Java 21/Jersey 4 plus javax/jakarta support | Independently verify effective dependency tree, class-example compatibility and regression checks
ENTITY-LANES | DESIGN RECOMMENDATION / CODE REFERENCE | Course A five domain entities; Extended has supporting identity, session, audit, stock movement and receipt tables | Do not change frozen A entity count, silently reclassify C tables as A domain
API-ROLE | OPEN QUESTION | Five supported Extended roles, but borrower-account and LEAD project assignment model incomplete | C6 policy contract, C7 security, C8 assigned workflow
STOCK | CODE REFERENCE / VERIFIED RISK MODEL | Physical totals + approved holds + issued custody + movement ledger; receipt replay | Full formal invariant proof, row locks, ledger grants and concurrency on C6/C9
MIGRATE | CODE REFERENCE / OPEN QUESTION | Flyway V1–V5 immutable; legacy nonzero stock and legacy plaintext contacts fail closed | Confirm supported upgrade target and rejection behavior, no fabricated legacy history
SMS | CODE REFERENCE / OPEN QUESTION | Disabled, fake and virtual Twilio modes; reports do not claim real delivery | Decide if actual messaging belongs to future product, student checks course interpretation
COURSE-CANVAS | OPEN QUESTION | Printed assignment date 2026-12-05, may be changed in Canvas | Student verification and academic evidence
DEPLOY | OPEN QUESTION | Local release and same-computer separate MySQL recovery C5 only | Authorization, HTTPS, external host, operator, real off-host recovery
ADVISORY | REPORTED_ONLY / OPEN QUESTION | C5 reports 12 high development dependency records related to Picomatch and zero production dependency findings | Recheck current advisory/version/lockfile without assuming patch availability
STORAGE | OPEN QUESTION | Backup script 64 MiB raw SQL bound and local snapshot fixture | Measure realistic size and independent recovery capacity before claims
EFFICIENCY | DESIGN RECOMMENDATION | No absolute token/build/storage optimum is empirically established | Benchmark baseline, independent proof for any improvement

## 3. C6 required follow-through

1. Regenerate tree inventory at exact authorized head and retain C5 metadata inventory as immutable historical reference.
2. Compare Git object hashes to avoid re-reviewing identical files without reason; preserve exact previous review depth and close newly affected transitive dependencies.
3. Create one complete source-coverage register from COURSE SOURCE ZIPs + tracked trees + connector documents with explicit origin identity, duplicate flags, relevance and reviewed ranges.
4. Locate every Course A route and Extended C endpoint, DTO, component, repository/SQL, tests, Postman and UI; field/role/state/error/transaction/clock semantics become exact trace rows.
5. Independently run source-dependent tests on **the exact implementation commit** if the environment safely supports disposable loopback MySQL and real browser; mark unexecuted acceptance separately.
6. Audit original actual course assignment, syllabus, relevant lecture slides/lab code and timestamps. Do not infer visual content of unattached recordings or invent instructor approval.
7. Verify developer/product infrastructure requirements are user decisions and avoid connecting paid services or opening public ports without authorization.
8. Write explicit true/false/unknown readiness by domain, risk priority, correction owner and next gate. Critic must independently challenge code and evidence before C6 accepted.

## 4. Evidence strength and truthful reporting

Source inspection provides evidence of *what code says*, not proof it runs or behaves correctly under all conditions. GitHub CI provides evidence of a workflow executed on a specific SHA, constrained by its tests/fixtures/host. A published C5 report is reported historical evidence, which may be verified by retrieving linked CI and artifacts. A source/CI claim cannot stand in for a real deployment, off-host restore, provider delivery, actual adoption or student academic action.

At the end of every future pass, append residual unreviewed paths and unsupported claims instead of closing them by wording. A truly comprehensive audit pursues full path accountability while reporting incomplete inspection honestly.

## 5. First next action

Run C6 from a fresh, approved base and generate the full coverage/contract/evidence register, then request owner review of C6 scope and DEC-01–DEC-12 before C7. This planning upload itself does not authorize C6 execution.
