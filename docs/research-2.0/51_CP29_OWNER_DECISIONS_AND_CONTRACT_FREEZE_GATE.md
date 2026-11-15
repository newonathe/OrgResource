# CP29 — Owner decision adjudication and conditional specification-freeze gate

**Research date:** 2026-11-16 (Asia/Manila). **STATUS: PROPOSED CHOICES ONLY — NO OWNER SIGN-OFF.** No code, migration, testing, deployment, provider enrollment, merge or Canvas action was performed. **CP29 is not a product contract freeze** without authorization.

## Authority and source fidelity
- **COURSE REQUIREMENT:** `09_ESP_PROJECT_ASSIGNMENT.pdf` (3pp): all actual prior classroom tools, Jersey REST, JSON, Spring `@Autowired` controller→component, JPA/Spring Data, Javax Validation, Retrofit-related external service at least Twilio, scheduled jobs, >=5 justified entities, GET/POST CRUD, Postman all calls, student Canvas/video; printed December 5 final.
- **SYLLABUS FACT:** `08_CSCI180_SYLLABUS.pdf` CLO5 MySQL/JPA/Hibernate, CLO6 JPQL, CLO7 `javax.validation`, CLO8 Retrofit/Gson, CLO9 receive/serve images, CLO10 final; CLO9 assessment mapping conflict; Certificate of Authorship and outside-assistance acknowledgment.
- **ORIGINAL PROPOSAL:** `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` (12pp), five entities, 25 GET/POST operations, global holds, whole-batch return and consume. **O14 separately implements A09** for SMS consent revocation; original proposal must not be retroactively described as 26 operations.
- **VERIFIED ACADEMIC CODE:** draft PR #8 exact `2dd336ed39cecd417a8bce3388b303e688e95dee`. Controllers/DTO components are a separate evidence layer from the proposal; source details below are newly rechecked in CP29.
- **USER DECISIONS:** D-018–D-026 and D-031: two runnable application tracks, keep original academic technology, no mandatory charges, full production P-ACCEPT *before* independent academic A-FINAL and student-only Canvas, distinct research/Codex implementation stages. The request to upload research is **not** an authorization to code.
- **CURRENT RESEARCH:** CP27 [48](48_CP27_CANONICAL_PRECODING_HANDOFF_DRAFT.md), [49](49_CP27_APPROVAL_EVIDENCE_AND_CODEX_STAGES.md); CP28 [50](50_CP28_RESEARCH_ACCEPTANCE_REVIEW.md).

## Source-confirmed amendments not captured by original 12-page proposal
| Finding | Exact O14 source | Implication |
|---|---|---|
| MC78 — academic R03 resource update adds `expectedTotalQuantity` | `backend/src/main/java/app/dto/ApiDtos.java` UpdateResource, `ResourceController.java`, `ResourceComponent.update` | Reject stale totals with 409. Original proposal named only `name,totalQuantity`; treat CAS as an O14 amendment, not a proposed input invented by CP29 |
| MC79 — academic resource deletion requires zero total and zero requirements | `ResourceComponent.delete` | Original proposal only explicitly mentioned no requirements. Extra safety guard is source fact, not automatically a public requirement |
| MC80 — academic Q03 protects largest PENDING request as well as coverage | `RequirementComponent.update` | Public P0 must decide pending invariants, not copy the weaker prose literally |
| MC81 — academic A08 requires `approvedAt==null` | `AllocationComponent.delete` | A historically approved CANCELLED record is protected from deletion |
| MC82 — academic expiry is `CANCELLED` with `cancellationReason=EXPIRED` | `AllocationComponent.expireIfEligible`; `AllocationStatus` | Do not introduce a standalone EXPIRED allocation status merely because old public V2 draft listed one |
| MC83 — academic output includes `expiredPendingCleanup` | `ApiDtos.AllocationView` | There is an explicit source-supported distinction between persisted status and effective end-of-project hold; public response candidate must represent it truthfully |

None establishes that O14 was independently retested in CP29.

## Decision states
- **ESTABLISHED USER DECISION:** the existing explicit project constraints, including two active executables and release order. No re-vote required.
- **RECOMMENDED DEFAULT / UNSIGNED:** small public fictional P0, scope/ownership/DB/API choices below. This is a proposal, not owner approval.
- **ONLY RUNTIME/EXTERNAL:** exact Jersey+Boot+Security compatibility, public $0 host, real TLS, clean restore, actual Canvas/Twilio/CLO9 and real user adoption. Source docs cannot produce those results.

## Priority owner adjudication matrix
| ID | CP29 recommended default | Alternative and security/complexity tradeoff | Approval prerequisite |
|---|---|---|---|
| OD01 | Retain independently runnable O14 Java8/Jersey2/Javax and supported Jakarta public app; never present public as literal course replacement | One namespace rewrite would erase literal classroom proof | Already two-executable USER DECISION; owner accepts detailed boundary |
| OD02 | Global immediate APPROVED holds, no future time-window optimization | Temporal bookings require peak-time algorithm and late-return protection | Owner |
| OD03 | Whole-batch reusable usable return, whole consumable use; no partial/loss/damage | Partial fulfillment needs additional ledger and statuses | Owner |
| OD04 | Effective APPROVED holds cease at exclusive project end; PENDING/APPROVED scheduler persists CANCELLED/EXPIRED reason; ISSUED custody never auto-returned | Proposal wording says hold persists until transition; actual O14 JPQL excludes ended | Owner, and academic discrepancy explained for final |
| OD05 | Operator-only **fictional** organization and first OWNER provisioning, no public self-claim | Self-signup first OWNER risks real org impersonation | Owner |
| OD06 | Two-party accepted transfer to ACTIVE ADMIN; old OWNER demoted to ADMIN atomically | Two simultaneous OWNERs until later step is simpler availability but different authority | Owner |
| OD07 | Compromised/lost sole-owner credentials => freeze tenant pending explicit offline fictional recovery; no operator content impersonation | Automatic operator takeover is unsafe; freeze hurts availability | Owner |
| OD08 | Local adaptive password, saved one-time recovery material, secure JDBC session; no email/SMS | Independent OIDC may require provider terms, identity claims, and integration | Owner; COMPAT-01 tests separate |
| OD09 | Preserve all academic 26, public resource archiving (R04) + academic-only A09, public history-safe deletion rules | Public drop-in parity impossible without SMS, security and audit compromises | Owner |
| OD10 | New public `BIGINT` internals + `CHAR(36)` opaque UUID public IDs initially, UTC `DATETIME(6)` | `BINARY(16)` more compact but increases ORM/schema complexity | Owner and real DDL tests |
| OD11 | Java17+/Java21 candidate with Boot4/Jersey4/Jakarta/MySQL8.4 in disposable local COMPAT-01 | Alternate supported runtime only if tested and separately reviewed | Explicit local COMPAT-01 authorization, not full build |
| OD12 | Only qualifying no-mandatory-charge VM/MySQL/HTTPS/off-account restore; no paid fallback | Self-hosted DR/ops more labor than managed service | Authorized provider eligibility proof later |
| OD13 | CLO9 image need not be presumed waived; check current Canvas/instructor | Historical audio calls images optional, syllabus CLO9 present | Student/instructor |
| OD14 | FakeTwilio local HTTP is **not** provider Virtual Phone; require current instructor acceptance or authorized provider test | Provider test may be unavailable on free PH account | Student/instructor |
| OD15 | Reproducible O14 JAR + both source ZIPs + manifest + SHA256, clean extraction | Git CI-JAR alone cannot prove all release files | Actual release execution |
| OD16 | Consider **COMPAT-01 only**, isolated disposable code, 8 tests, stop on failure | Full product build would be premature | **Owner explicit permission not yet given** |
| OD17 | Separate owner authorization for full implementation after actual COMPAT results and signed P0/API/DDL/governance | Auto-expanding experiment is not authorized | Owner |
| OD18 | Actual current Canvas and student COA/video/peer when applicable | Published syllabus policy cannot establish a Canvas receipt/extension | Student |
| OD19 NEW | Retain audit tombstone and never hard-delete a row with movement/approval history or mandatory FK dependency | Blanket hard delete would corrupt history; pure soft-delete needs states/filters | Owner |
| OD20 NEW | After disaster restoration, public ingress remains closed until previous sessions/invites/recovery secrets are invalidated/reissued and admin owner regains controlled access | Blind session restore may re-enable stale identities | Owner and actual DR test |
| OD21 NEW | Canonical public API uses GET/POST, version `If-Match`, per-mutation idempotency, separate durable `operationId` for physical movements | Using the historical V2 route table imports temporal/partial behavior | Owner; contract [52] |
| OD22 NEW | Current-role permission table distinguishes ADMIN governance from CUSTODIAN physical stock; OWNER all within tenant but cannot bypass transfer/last-owner | Blanket ADMIN stock authority unreviewed; one-role membership is easier | Owner; actual auth tests |

## Contract-freeze eligibility
**COMPAT-01 proposed GO WITH CONDITIONS:** Only if owner explicitly authorizes a *local isolated synthetic* experiment; pins exact source/version candidates and test obligations; no cloud or public network service, no purchasing, no O14 edits, no surprise alternative backend. Actual integration proof cannot be paper-frozen.

**Full public build NO-GO:** Before any product PR, sign OD02–OD10 and OD19–OD22 (or record explicit alternative); freeze route DTO/role/error and exact DDL/migration schema; run and review COMPAT-01; confirm supported architecture and sufficient zero-cost provider proof plan. Later account enrollment and P-ACCEPT may be authorized separately.

**P-ACCEPT NO-GO:** No hosted secure auth/tenancy, actual MySQL ledger/race, usable real UI, trusted HTTPS certificate renewal, cost control and independent encrypted off-account restore = no production PASS.

**A-FINAL NO-GO:** Before a real P-ACCEPT (user's binding sequence) and original academic Javax/Jersey/Retrofit/MySQL/Postman/Twilio/CLO9 acceptance; student video/COA/Canvas actual evidence. Never claim grader approval from CI.

## Paper-resolvable items versus proofs requiring code
**Paper:** exact global/whole, expiry consistency, roles/ownership, history/delete, UUID and time, route DTO/errors, retry IDs, DR session/keys and access policy, documentation precedence, acceptance matrices.

**Actual local code:** Boot4/Jersey4 native Jakarta compile, servlet/Jersey security/CSRF, JSON, Hibernate MySQL, MySQL row locks and FK/transaction handling, Javax JPA invalid persistence in academic executable.

**Actual authorized external:** OCI eligibility/cost/availability, host HTTPS real cert renewal, off-provider encrypted restore, current Canvas/Twilio/CLO9 instructions and student submission, any genuine institutional usage claim.

## CP29 proposed approval text (NOT SIGNED)
> Approve the fictional-only public P0 defaults and the exact candidate security/DB/API contracts referenced here solely as the design baseline for a narrow local compatibility test. Approve COMPAT-01 separately if desired. Do not authorize a full public application build, hosted provisioning, academic modifications, merging or Canvas submission through this research approval.

**Next step:** owner reviews concise decisions; any modifications are recorded as a dated decision amendment. Only after explicit choice is a version marked `FROZEN FOR IMPLEMENTATION`.