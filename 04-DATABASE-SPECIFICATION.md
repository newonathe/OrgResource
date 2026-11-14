# Complete Database Specification

## Shared mapping and constraints

Nine entity types are selected. Organization name, categories, storage labels and units are configuration or bounded strings, not artificial entities. Every primary key uses javax.persistence.Entity, Id and GeneratedValue(strategy=IDENTITY). Mutable entities use Version. Timestamps are server assigned UTC Instant values mapped to DATETIME(3). Foreign keys use ManyToOne(fetch=LAZY), JoinColumn with the nullability shown and no cascading delete. Explicitly annotate EnumType.STRING. A mapped superclass may carry common fields but is not a tenth entity.

All inverse one-to-many navigation is optional and if implemented uses mappedBy, LAZY, no REMOVE cascade and orphanRemoval=false. No ManyToMany or OneToOne entity relationship is needed except the unique nullable reversalOf foreign key (map ManyToOne plus database unique constraint). Do not serialize these graphs. createdBy and updatedBy point to AppUser and may be null only for the audited SYSTEM bootstrap or scheduler. An ordinary request always has a real resolved actor. AppUser may reference itself as creator during bootstrap, or SYSTEM/null with an AuditEvent.

All repositories extend JpaRepository<Entity,Long> and JpaSpecificationExecutor<Entity>. Used inherited methods are findById(Long), findAll(Specification,Pageable), save(Entity), flush(), delete(Entity), existsById(Long) and count(Specification). A component invokes delete only under the entity-specific safe-deletion predicate. Specifications build parameterized equality/range/status predicates against actual mapped field paths, never concatenate user JPQL. Lists use a whitelist of fields described in05. Unmentioned inherited methods are not exposed as generic REST operations.

Uniqueness: Department.code, AppUser.username, Project.code, Resource.code, ResourceRequirement(project_id,resource_id), Allocation.requestKey, InventoryTransaction(resource_id,commandKey), InventoryTransaction.reversalOf, NotificationLog.eventKey and nullable NotificationLog.providerSid. Codes are trimmed and uppercased Locale.ROOT; usernames trimmed and lowercased Locale.ROOT; unique DB collation must agree. Resource.code DB width is30, Project.code30, Department.code20, username50. Specification text limits override inferred sample values. Restrict all foreign-key deletion; no cascade can bypass history.

Indexes: every FK; Allocation(requirement_id,decision_status,start_at,end_at); ResourceRequirement(resource_id,status); InventoryTransaction(resource_id,id) and (allocation_id,id); NotificationLog(status,next_attempt_at,id); AuditEvent(target_type,target_id,id). Cross-row sums and role/active-reference constraints are component rules under locks, not Bean Validation annotations. Schema tests must confirm unique constraints and actual DB types. No negative quantity or illegal state is made safe merely by JPA annotations.

## Department

Purpose: Flat accountable unit inside the one organization. Why necessary: Needed to consolidate demand and assign responsibility without tenancy or hierarchy.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| version | Long | BIGINT | No | Version; nonnegative; server managed | Expected version checked on updates |
| updatedAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Last changed instant |
| updatedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM | Last actor |
| code | String | VARCHAR(20) | No | 3..20 uppercase A-Z0-9_ unique | Permanent human reference |
| name | String | VARCHAR(100) | No | NotBlank; max100 | Display name |
| description | String | VARCHAR(1000) | Yes | max1000 | Optional purpose |
| active | Boolean | BOOLEAN | No | Default true | New references require active department |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); updatedBy -> AppUser (optional FK, LAZY, RESTRICT)

Repository: DepartmentRepository. Entity type Department; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: Optional<Department> findByCode(String code). Purpose: Detect duplicate normalized code. Query: Derived code equality.

Lifecycle and deletion: Hard delete only when no user, project or resource references exist. Otherwise409 DEPENDENCY_EXISTS; update active=false only when no active operational dependency.

## AppUser

Purpose: Member identity and access/contact preferences. Why necessary: Provides accountable requesters, approvers, recipients and actors.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| version | Long | BIGINT | No | Version; nonnegative; server managed | Expected version checked on updates |
| updatedAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Last changed instant |
| updatedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM | Last actor |
| username | String | VARCHAR(50) | No | 3..50 lowercase a-z0-9._- unique | Stable local reference; not a password |
| displayName | String | VARCHAR(100) | No | NotBlank max100 | Person label; fictional in demo |
| role | Role | VARCHAR(40) | No | ADMIN OFFICER HEAD CUSTODIAN MEMBER | One fixed role |
| department | Department | BIGINT FK | Yes | FK RESTRICT; required except ADMIN/OFFICER | Home department |
| active | Boolean | BOOLEAN | No | Default true | Inactive cannot act or receive new responsibility |
| phoneE164 | String | VARCHAR(15) | Yes | Pattern plus followed by8..15 digits; max16 | Optional normalized contact; self/admin only |
| smsConsent | Boolean | BOOLEAN | No | Default false | Operational SMS opt in |
| consentAt | Instant | DATETIME(3) | Yes | Required when consent true; server timestamp | Most recent affirmative consent |
| consentVersion | String | VARCHAR(30) | Yes | max30; required when consent true | Notice version such as OP_SMS_V1 |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); updatedBy -> AppUser (optional FK, LAZY, RESTRICT); department -> Department (optional FK, LAZY, RESTRICT)

Repository: AppUserRepository. Entity type AppUser; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: Optional<AppUser> findByUsername(String username). Purpose: Resolve known username and detect duplicate. Query: Derived username equality.

Lifecycle and deletion: Hard delete only if no operational or actor references anywhere. Otherwise deactivate using update; deny deactivation if assigned unresolved custody or sole active custodian, until reassigned. Never delete last active ADMIN.

## Project

Purpose: Dated departmental activity with internal planning approval. Why necessary: Requirements need one accountable owner and bounded schedule.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| version | Long | BIGINT | No | Version; nonnegative; server managed | Expected version checked on updates |
| updatedAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Last changed instant |
| updatedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM | Last actor |
| code | String | VARCHAR(30) | No | 3..30 uppercase A-Z0-9_- unique | Stable project reference |
| name | String | VARCHAR(150) | No | NotBlank max150 | Project name |
| description | String | VARCHAR(2000) | Yes | max2000 | Scope description |
| department | Department | BIGINT FK | No | FK RESTRICT; active at create | Accountable unit |
| owner | AppUser | BIGINT FK | No | FK RESTRICT; active same department | Project lead |
| startAt | Instant | DATETIME(3) | No | Strictly less than endAt | Planning start |
| endAt | Instant | DATETIME(3) | No | After startAt; max90-day project duration | Planning end |
| status | ProjectStatus | VARCHAR(40) | No | DRAFT SUBMITTED APPROVED IN_PROGRESS COMPLETED CANCELLED | Internal lifecycle |
| statusReason | String | VARCHAR(1000) | Yes | max1000; required for return/cancel/waiver | Latest decision reason; full changes in audit |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); updatedBy -> AppUser (optional FK, LAZY, RESTRICT); department -> Department (required FK, LAZY, RESTRICT); owner -> AppUser (required FK, LAZY, RESTRICT)

Repository: ProjectRepository. Entity type Project; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: Optional<Project> findByCode(String code). Purpose: Detect duplicate project reference. Query: Derived code equality.

Lifecycle and deletion: Hard delete only DRAFT with no requirements/allocations. Historical project uses cancel or completed archival; no cascading delete.

## Resource

Purpose: Pool of interchangeable whole units under organizational control. Why necessary: Defines the identity, type, unit and accountable custodian shared by all requirements.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| version | Long | BIGINT | No | Version; nonnegative; server managed | Expected version checked on updates |
| updatedAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Last changed instant |
| updatedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM | Last actor |
| code | String | VARCHAR(30) | No | 3..30 uppercase A-Z0-9_- unique | Stable resource reference |
| name | String | VARCHAR(150) | No | NotBlank max150 | Resource display name |
| description | String | VARCHAR(2000) | Yes | max2000 | Handling notes |
| type | ResourceType | VARCHAR(40) | No | REUSABLE CONSUMABLE; locked after reference | Allocation arithmetic class |
| category | String | VARCHAR(50) | No | NotBlank max50 | Simple organization vocabulary |
| unit | String | VARCHAR(20) | No | NotBlank max20; fixed after reference | Whole base unit such as piece sheet roll |
| storageLocation | String | VARCHAR(200) | No | NotBlank max200 | Normal store description; one pool location |
| controlBasis | ControlBasis | VARCHAR(40) | No | OWNED AUTHORIZED | Who may manage the resource |
| authorizationNote | String | VARCHAR(1000) | Yes | max1000; required for AUTHORIZED | Authority description without claiming university permission |
| department | Department | BIGINT FK | No | FK RESTRICT active | Responsible department |
| custodian | AppUser | BIGINT FK | No | FK RESTRICT active CUSTODIAN | Designated stock/approval operator |
| revision | Long | BIGINT | No | Nonnegative server counter;increment under resource lock for every pool command | Dependency fence independent of JPA flush timing;not a stock quantity |
| active | Boolean | BOOLEAN | No | Default true | Archive rather than erase history |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); updatedBy -> AppUser (optional FK, LAZY, RESTRICT); department -> Department (required FK, LAZY, RESTRICT); custodian -> AppUser (required FK, LAZY, RESTRICT)

Repository: ResourceRepository. Entity type Resource; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: Optional<Resource> findByCode(String code). Purpose: Detect duplicate pool reference. Query: Derived code equality.

Custom method: Optional<Resource> lockById(Long id). Purpose: Serialize all commands affecting this pool; write transaction required. Query: JPQL select r from Resource r where r.id = :id; @Lock(PESSIMISTIC_WRITE).

Lifecycle and deletion: Hard delete only with no requirements, allocations or transactions. Archive requires no pending/active unissued commitment or outstanding custody; keep ledger forever in demo.

## ResourceRequirement

Purpose: Demand for one resource during one project window. Why necessary: Separates desired supply from an approved promise and permits partial allocations.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| version | Long | BIGINT | No | Version; nonnegative; server managed | Expected version checked on updates |
| updatedAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Last changed instant |
| updatedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM | Last actor |
| project | Project | BIGINT FK | No | FK RESTRICT; active planning project | Demanding activity |
| resource | Resource | BIGINT FK | No | FK RESTRICT active | Requested pool |
| quantity | Long | BIGINT | No | Min1 Max1000000 | Total desired whole base units |
| startAt | Instant | DATETIME(3) | No | Within project; start less than end | Required beginning |
| endAt | Instant | DATETIME(3) | No | Within project | Required end even for demand-timing of consumables |
| priority | Priority | VARCHAR(40) | No | LOW NORMAL HIGH | Display/sort priority only |
| notes | String | VARCHAR(2000) | Yes | max2000 | Justification |
| status | RequirementStatus | VARCHAR(40) | No | ACTIVE WITHDRAWN | Demand visibility; not fulfillment status |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); updatedBy -> AppUser (optional FK, LAZY, RESTRICT); project -> Project (required FK, LAZY, RESTRICT); resource -> Resource (required FK, LAZY, RESTRICT)

Repository: ResourceRequirementRepository. Entity type ResourceRequirement; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: List<ResourceRequirement> findByProject_IdOrderByIdAsc(Long projectId). Purpose: Load complete project requirement set. Query: Derived project.id predicate sorted id.

Custom method: List<ResourceRequirement> findByResource_IdAndStatus(Long resourceId, RequirementStatus status). Purpose: Load active demand for consolidation; project-status filter in component. Query: Derived resource.id and status equality.

Lifecycle and deletion: Hard delete only when no allocation or movement references. Otherwise withdrawal through update with reason, only after all operational obligations are resolved.

## Allocation

Purpose: One requested quantity and its immutable approved schedule/recipient. Why necessary: Allows a requirement to be satisfied in several separately auditable commitments.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| version | Long | BIGINT | No | Version; nonnegative; server managed | Expected version checked on updates |
| updatedAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Last changed instant |
| updatedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM | Last actor |
| requirement | ResourceRequirement | BIGINT FK | No | FK RESTRICT ACTIVE | Links demand to pool/project |
| quantity | Long | BIGINT | No | Min1 Max1000000 | Requested and if approved committed quantity |
| releasedUnissued | Long | BIGINT | No | Default0;0..quantity;cannot reduce issued coverage | Explicitly released portion |
| startAt | Instant | DATETIME(3) | No | Snapshot requirement.startAt | Committed start |
| endAt | Instant | DATETIME(3) | No | Snapshot requirement.endAt | Committed end and reusable due time |
| requestedBy | AppUser | BIGINT FK | No | FK RESTRICT active at create | Actual actor requester |
| recipient | AppUser | BIGINT FK | No | FK RESTRICT active same project department | Fixed custody holder / consumption recipient |
| decisionStatus | AllocationDecision | VARCHAR(40) | No | REQUESTED APPROVED REJECTED CANCELLED | Approval decision separate from ledger progress |
| decidedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; required on approve/reject | Designated custodian decision actor |
| decidedAt | Instant | DATETIME(3) | Yes | Required on approve/reject | Decision timestamp |
| decisionReason | String | VARCHAR(1000) | Yes | max1000; required rejection/cancellation | Decision/release reason; full history in audit |
| requestKey | String | CHAR(36) | No | UUID canonical36;global unique | Create replay guard |
| payloadHash | String | CHAR(64) | No | SHA256 char64 server derived | Detect key reuse with changed input |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); updatedBy -> AppUser (optional FK, LAZY, RESTRICT); requirement -> ResourceRequirement (required FK, LAZY, RESTRICT); requestedBy -> AppUser (required FK, LAZY, RESTRICT); recipient -> AppUser (required FK, LAZY, RESTRICT); decidedBy -> AppUser (optional FK, LAZY, RESTRICT)

Repository: AllocationRepository. Entity type Allocation; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: Optional<Allocation> findByRequestKey(String requestKey). Purpose: Replay original create or reject changed payload. Query: Derived unique requestKey equality.

Custom method: List<Allocation> findByRequirement_Resource_IdAndDecisionStatus(Long resourceId, AllocationDecision status). Purpose: Current approved pool commitments; cannot filter only finite overlap because overdue holds extend. Query: Derived requirement.resource.id and decisionStatus equality.

Custom method: List<Allocation> findByRequirement_Project_Id(Long projectId). Purpose: Cancel/reschedule/complete entire project safely. Query: Derived requirement.project.id equality.

Custom method: List<Allocation> findByRequirement_Id(Long requirementId). Purpose: Coverage and dependent-row checks. Query: Derived requirement.id equality.

Custom method: List<Allocation> findOverlappingApproved(Long resourceId, Instant start, Instant end, AllocationDecision approved). Purpose: Finite scheduled overlap inspection only; merge overdue custody found through approved pool scan, never use alone for safe approval. Query: JPQL select a from Allocation a where a.requirement.resource.id = :resourceId and a.decisionStatus = :approved and a.startAt < :end and a.endAt > :start; bind AllocationDecision.APPROVED as extra enum parameter approved.

Lifecycle and deletion: Hard delete only REQUESTED by requester or HEAD/OFFICER, with no movements or notifications; append audit. Approved/rejected/cancelled history retained;409 directs cancel/release.

## InventoryTransaction

Purpose: Immutable physical/custody delta and correction event. Why necessary: Only authority for physical stock; original observations must remain traceable.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| resource | Resource | BIGINT FK | No | FK RESTRICT | Affected pool |
| allocation | Allocation | BIGINT FK | Yes | FK RESTRICT;required ISSUE RETURN CONSUMPTION and custody LOSS | Operational custody/consumption reference |
| type | TransactionType | VARCHAR(40) | No | RECEIPT ISSUE RETURN CONSUMPTION DAMAGE LOSS ADJUSTMENT_INCREASE ADJUSTMENT_DECREASE REVERSAL | Movement meaning |
| quantity | Long | BIGINT | No | Min1 Max1000000 | Magnitude; not signed client delta |
| inspection | Inspection | VARCHAR(40) | Yes | USABLE DAMAGED;required RETURN only | Return condition split across separate transactions |
| reason | String | VARCHAR(1000) | No | NotBlank max1000 | Physical/correction explanation |
| occurredAt | Instant | DATETIME(3) | No | Server Clock, no client backdating | Observed recording time |
| deltaStored | Long | BIGINT | No | Derived signed;for consumables same as physical delta | Usable storage delta |
| deltaOutstanding | Long | BIGINT | No | Derived signed;zero for consumables | Reusable custody delta |
| deltaDamaged | Long | BIGINT | No | Derived signed;zero for consumables | Quarantine delta |
| deltaIssued | Long | BIGINT | No | Derived signed for linked allocation | Net issued/consumed counter delta |
| deltaResolved | Long | BIGINT | No | Derived signed for linked allocation | Reusable returned/lost resolution delta |
| deltaGoodReturned | Long | BIGINT | No | Derived signed | Usable return count delta |
| deltaDamagedReturned | Long | BIGINT | No | Derived signed | Damaged return count delta |
| deltaLost | Long | BIGINT | No | Derived signed | Custody lost count delta |
| commandKey | String | CHAR(36) | No | UUID36 unique together with resource | Replay identity |
| payloadHash | String | CHAR(64) | No | SHA256 char64 server derived | Canonical command fingerprint |
| reversalOf | InventoryTransaction | BIGINT FK | Yes | FK RESTRICT unique;non-reversal original only | Exactly one correction of original |
| correctionReference | InventoryTransaction | BIGINT FK | Yes | FK RESTRICT;informational only | Present correction after old event cannot be reversed |
| resourceRevisionAfter | Long | BIGINT | No | Positive;captured after resource revision flush | Dependency fence for latest-event reversal |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); resource -> Resource (required FK, LAZY, RESTRICT); allocation -> Allocation (optional FK, LAZY, RESTRICT); reversalOf -> InventoryTransaction (optional FK, LAZY, RESTRICT); correctionReference -> InventoryTransaction (optional FK, LAZY, RESTRICT)

Repository: InventoryTransactionRepository. Entity type InventoryTransaction; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: List<InventoryTransaction> findByResource_IdOrderByIdAsc(Long resourceId). Purpose: Reconstruct balances/effects in stable order under resource lock. Query: Derived resource.id equality sorted id.

Custom method: Optional<InventoryTransaction> findByResource_IdAndCommandKey(Long resourceId, String commandKey). Purpose: Stock command replay guard. Query: Derived equality on both fields.

Custom method: Optional<InventoryTransaction> findByReversalOf_Id(Long originalId). Purpose: Reject second reversal of same original. Query: Derived reversalOf.id equality.

Custom method: List<InventoryTransaction> findByAllocation_IdOrderByIdAsc(Long allocationId). Purpose: Derive issued/resolved counters and custody history. Query: Derived allocation.id equality sorted id.

Lifecycle and deletion: Never physically delete a posted movement. Delete endpoint returns409 IMMUTABLE_HISTORY with reversal route. No permission to clear the ledger through REST.

## NotificationLog

Purpose: Durable return reminder event/outbox and provider outcome. Why necessary: Decouples SMS from stock commits and suppresses repeated reminders.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| version | Long | BIGINT | No | Version; nonnegative; server managed | Expected version checked on updates |
| updatedAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Last changed instant |
| updatedBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM | Last actor |
| allocation | Allocation | BIGINT FK | No | FK RESTRICT reusable APPROVED with outstanding custody | Reminder target |
| recipient | AppUser | BIGINT FK | No | FK RESTRICT;allocation recipient | Operational recipient |
| kind | NotificationKind | VARCHAR(40) | No | RETURN_DUE OVERDUE | Only two V1 event types |
| eventKey | String | VARCHAR(160) | No | max160 unique;server derived | kind:allocationId:endAt:recipientId |
| status | NotificationStatus | VARCHAR(40) | No | QUEUED SENDING ACCEPTED DELIVERED UNDELIVERED FAILED UNKNOWN SKIPPED CANCELLED | Dispatch/provider outcome |
| dueAt | Instant | DATETIME(3) | No | Server derived | First eligible delivery time |
| nextAttemptAt | Instant | DATETIME(3) | Yes | Required QUEUED | Bounded retry time |
| attemptCount | Integer | INT | No | 0..3 | Transport attempts;zero when skipped |
| destinationMasked | String | VARCHAR(30) | Yes | max30 | No full number snapshot |
| providerSid | String | VARCHAR(64) | Yes | max64 unique when present | Accepted external message identity |
| providerStatus | String | VARCHAR(40) | Yes | max40 | Raw safe provider status |
| lastErrorCode | String | VARCHAR(80) | Yes | max80 | Sanitized category/code;no credentials |
| lastAttemptAt | Instant | DATETIME(3) | Yes | Set when attempt starts | Recovery and diagnostics |
| cancelReason | String | VARCHAR(1000) | Yes | max1000 | Staleness/consent/manual cancellation reason |
| attempts | List<NotificationAttempt> | separate collection table | No | ElementCollection append only;max3 | Each embedded attempt records instant outcome error SID |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT); updatedBy -> AppUser (optional FK, LAZY, RESTRICT); allocation -> Allocation (required FK, LAZY, RESTRICT); recipient -> AppUser (required FK, LAZY, RESTRICT)

Repository: NotificationLogRepository. Entity type NotificationLog; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: Optional<NotificationLog> findByEventKey(String eventKey). Purpose: Deduplicate reminder generation across restarts/scans. Query: Derived eventKey equality.

Custom method: List<NotificationLog> findDue(NotificationStatus status, Instant now, Pageable page). Purpose: Fetch bounded QUEUED candidates; lock each before claim. Query: JPQL select n from NotificationLog n where n.status = :status and n.nextAttemptAt <= :now order by n.id.

Custom method: Optional<NotificationLog> lockById(Long id). Purpose: Claim reminder or serialize completion/cancellation. Query: JPQL select n from NotificationLog n where n.id = :id; @Lock(PESSIMISTIC_WRITE).

Lifecycle and deletion: Hard delete only unattempted QUEUED/SKIPPED/CANCELLED rows with attemptCount0, by OFFICER/ADMIN; record AuditEvent. Otherwise409 IMMUTABLE_HISTORY. Cancellation of queued reminder is explicit and prevents sending.

## AuditEvent

Purpose: Append-only actor/action/change record including non-stock decisions. Why necessary: Stock history alone cannot explain approvals, edits, cancellations, or deletions.

| Field | Java type | Database type | Nullable | Constraints | Meaning |
|---|---|---|---|---|---|
| id | Long | BIGINT | No | Generated IDENTITY; positive; immutable | Surrogate primary key |
| createdAt | Instant | DATETIME(3) | No | UTC millisecond; server assigned | Creation instant |
| createdBy | AppUser | BIGINT FK | Yes | FK RESTRICT; null only SYSTEM bootstrap/jobs | Creator actor; null never means an unauthenticated public request |
| action | String | VARCHAR(80) | No | max80 fixed allowlist or NOTE | Server command action |
| targetType | String | VARCHAR(160) | No | One of nine entity names;NOTE allows Project/Resource/Department | Historical typed reference |
| targetId | Long | BIGINT | No | Positive;real target at creation | May survive a permitted target deletion |
| targetSummary | String | VARCHAR(300) | No | max300 server generated | Historical code/name identity |
| beforeJson | String | TEXT | Yes | TEXT max16000;server redacted | Prior public business values |
| afterJson | String | TEXT | Yes | TEXT max16000;server redacted | Resulting public business values |
| reason | String | VARCHAR(1000) | Yes | max1000;required NOTE and corrective actions | Human explanation |
| requestId | String | CHAR(36) | No | UUID36 server assigned | Diagnostic request correlation |

Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.

Relationships: createdBy -> AppUser (optional FK, LAZY, RESTRICT)

Repository: AuditEventRepository. Entity type AuditEvent; ID type Long. Standard inherited methods and Specification queries above apply.

Custom method: List<AuditEvent> findByTargetTypeAndTargetIdOrderByIdAsc(String targetType, Long targetId). Purpose: Audit history for one referenced record. Query: Derived targetType and targetId equality sorted id.

Lifecycle and deletion: Never physically delete audit history. Delete endpoint returns409 IMMUTABLE_HISTORY. A correction is a new NOTE referencing the original id in its reason, not editing the original.

## NotificationAttempt embeddable

NotificationAttempt is an Embeddable, not an entity. It contains ordinal Integer1..3, attemptedAt Instant nonnull, outcome String max40 nonnull, errorCode String max80 nullable, providerSid String max64 nullable. ElementCollection join table notification_attempt(notification_log_id FK, ordinal, attempted_at, outcome, error_code, provider_sid), unique(log_id,ordinal), ordered ordinal. Log attemptCount must equal collection size. Append within the same short notification-result transaction; no phone, body, secret or raw exception is stored. No independent CRUD endpoint exists because this is a value-owned part of NotificationLog, not a separate entity.

## Enum catalogue

Role: ADMIN, OFFICER, HEAD, CUSTODIAN, MEMBER.

ResourceType: REUSABLE, CONSUMABLE.

ControlBasis: OWNED, AUTHORIZED.

ProjectStatus: DRAFT, SUBMITTED, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED.

RequirementStatus: ACTIVE, WITHDRAWN.

Priority: LOW, NORMAL, HIGH.

AllocationDecision: REQUESTED, APPROVED, REJECTED, CANCELLED.

TransactionType: RECEIPT, ISSUE, RETURN, CONSUMPTION, DAMAGE, LOSS, ADJUSTMENT_INCREASE, ADJUSTMENT_DECREASE, REVERSAL.

Inspection: USABLE, DAMAGED.

NotificationKind: RETURN_DUE, OVERDUE.

NotificationStatus: QUEUED, SENDING, ACCEPTED, DELIVERED, UNDELIVERED, FAILED, UNKNOWN, SKIPPED, CANCELLED.

## Field and DTO distinctions

Entity properties representing relationships become xxxId Long in request/response DTOs; direct actor fields are never accepted. Primitive booleans and counters use wrapper validation where null can otherwise be silently converted. Responses include derived stock/coverage fields defined in05 but those are not persisted JPA properties. No derived Spring Data query may refer to them. AuditEvent beforeJson/afterJson uses a fixed redacted DTO serialization, not arbitrary entity serialization; no PII/credentials/consent phone values in snapshots. InventoryTransaction signed delta fields are server generated. Their zero values are explicit and never null. NotificationLog full destination is read fresh from recipient before send and is not stored in attempts.

## Repository query validation

Compile/start every repository under the pinned Spring Data version. The explicitly documented findOverlappingApproved query has four arguments including approved; bind each with Param. It is useful for reporting but insufficient for overdue safety. All List-derived methods are intentionally bounded by the stated demo size and loaded in batches per pool; paginated public queries use Specifications. If an additional custom query is introduced by implementation it must be added to this catalogue and the contract references before review.
