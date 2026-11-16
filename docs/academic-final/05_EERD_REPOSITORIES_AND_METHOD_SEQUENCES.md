# OrgResource — Original five-domain EERD, all custom repository methods and Component sequences

**Version:** A-CANON-1.0, 2026-11-16. **Status:** Direct O14 source-based technical specification; no fresh database/test execution.  
**Sources:** O14 SHA 2dd336ed39cecd417a8bce3388b303e688e95dee: five JPA entities, five repositories, Components, schedulers and Jersey controllers. Original proposal P7–P12 supplies historical descriptions. The exact Java source, not the older proposal's anticipated method names, governs the VERIFIED O14 implementation.

## 1. Original five-entity ER relationship diagram

~~~mermaid
erDiagram
    DEPARTMENT ||--o{ PROJECT : owns
    PROJECT ||--o{ RESOURCE_REQUIREMENT : needs
    RESOURCE ||--o{ RESOURCE_REQUIREMENT : requested
    RESOURCE_REQUIREMENT ||--o{ ALLOCATION : receives

    DEPARTMENT {
      bigint id PK
      varchar name
      varchar name_key UK
    }
    PROJECT {
      bigint id PK
      bigint department_id FK
      varchar name
      datetime start_at
      datetime end_at
    }
    RESOURCE {
      bigint id PK
      varchar name
      varchar name_key UK
      varchar type
      integer total_quantity
    }
    RESOURCE_REQUIREMENT {
      bigint id PK
      bigint project_id FK
      bigint resource_id FK
      integer quantity
    }
    ALLOCATION {
      bigint id PK
      bigint requirement_id FK
      integer quantity
      varchar borrower_name
      varchar phone_number
      boolean sms_consent
      varchar status
      datetime created_at
      datetime approved_at
      datetime issued_at
      datetime returned_at
      datetime consumed_at
      datetime cancelled_at
      varchar cancellation_reason
      datetime reminder_claimed_at
      varchar reminder_outcome
      integer reminder_http_status
      varchar provider_message_sid
    }
~~~

**Cardinality:** Department 1→many Project; Project 1→many ResourceRequirement; Resource 1→many ResourceRequirement; ResourceRequirement 1→many Allocation. All four child FK fields are NOT NULL. A Project/Resource pair is unique per requirement. Name keys on Department and Resource are globally unique in O14, but MUST become tenant-scoped if multi-org support is implemented in final product. Unique/index details in 02. Diagram shows the physical O14 domain, **not** the proposed future multi-organization infrastructure schema.

## 2. All custom repository methods in actual O14 source

All repositories inherit Spring Data JpaRepository basic save, saveAndFlush, findById, findAll, delete and flush methods. The entries below list the **additional methods**, including exact parameter types and purpose. Unlike the proposal, O14 uses nameKey to normalize case instead of existsByNameIgnoreCase.

| Repository | Exact additional method / parameters | Actual job / critical condition |
|---|---|---|
| DepartmentRepository | existsByNameKey(String nameKey):boolean | Case/whitespace-normalized Department duplicate detection |
| ProjectRepository | findByDepartment_Id(Long departmentId):List<Project> | Optional Department filtering |
| ProjectRepository | existsByDepartment_Id(Long departmentId):boolean | Reject Department delete when projects exist |
| ProjectRepository | lockById(Long id):Optional<Project> | JPQL SELECT by id under PESSIMISTIC_WRITE for coherent transaction |
| ResourceRepository | existsByNameKey(String nameKey):boolean | Unique Resource name on create |
| ResourceRepository | existsByNameKeyAndIdNot(String nameKey,Long id):boolean | Unique name on update, excluding same record |
| ResourceRepository | findByType(ResourceType type):List<Resource> | Resource type list filter |
| ResourceRepository | lockById(Long id):Optional<Resource> | JPQL pessimistic exclusive resource lock |
| ResourceRequirementRepository | findByProject_Id(Long projectId):List<ResourceRequirement> | Requirement Project filter |
| ResourceRequirementRepository | existsByProject_Id(Long projectId):boolean | Reject referenced Project delete |
| ResourceRequirementRepository | existsByResource_Id(Long resourceId):boolean | Reject referenced Resource delete |
| ResourceRequirementRepository | existsByProject_IdAndResource_Id(Long projectId,Long resourceId):boolean | Reject duplicate Project–Resource pair |
| ResourceRequirementRepository | lockById(Long id):Optional<ResourceRequirement> | JPQL pessimistic lock of Requirement |
| AllocationRepository | findByRequirement_Id(Long requirementId):List<Allocation> | Filter Allocation by Requirement and inspect history |
| AllocationRepository | findByStatus(AllocationStatus status):List<Allocation> | Filter Allocation status |
| AllocationRepository | findByRequirement_IdAndStatus(Long requirementId,AllocationStatus status):List<Allocation> | Intersect both list filters |
| AllocationRepository | existsByRequirement_Id(Long requirementId):boolean | Protect Requirement with any Allocation history |
| AllocationRepository | lockById(Long id):Optional<Allocation> | JPQL pessimistic lock by ID |
| AllocationRepository | countHistoricallyApproved(Long projectId):Long | Count Allocation where Project matches and approvedAt != null; prevents date edits |
| AllocationRepository | maxPendingQuantity(Long requirementId,AllocationStatus pending):Integer | Largest pending amount; prevents lowering Q below a still-valid pending request |
| AllocationRepository | sumCoveredQuantity(Long requirementId,List<AllocationStatus> completedStatuses,AllocationStatus approved,LocalDateTime now):Long | Sum completed quantities plus APPROVED whose Project.endAt>now |
| AllocationRepository | sumEffectiveHolds(Long resourceId,AllocationStatus approved,LocalDateTime now):Long | Sum current effective APPROVED reservation for one Resource |
| AllocationRepository | sumIssuedCustody(Long resourceId,AllocationStatus issued):Long | Sum outstanding ISSUED custody for a Resource |
| AllocationRepository | sumResourceCommitments(List<Long> resourceIds,AllocationStatus approved,AllocationStatus issued,LocalDateTime now,Collection<AllocationStatus> countedStatuses):List<Object[]> | Grouped JPQL H and I per Resource for list availability |
| AllocationRepository | findExpiredCandidateIds(Collection<AllocationStatus> openStatuses,LocalDateTime now,Pageable pageable):List<Long> | Bounded PENDING/APPROVED candidates for expiry worker, ordered id |
| AllocationRepository | findReminderCandidateIds(AllocationStatus issued,ResourceType reusable,ReminderOutcome neverAttempted,LocalDateTime now,LocalDateTime within24Hours,Pageable pageable):List<Long> | Bounded consented ISSUED reusable reminders with end in [now,+24h) and no prior claim |
| AllocationRepository | findStaleReminderClaimIds(ReminderOutcome claimed,LocalDateTime cutoff,Pageable pageable):List<Long> | Bounded CLAIMED reminder recovery candidates before cutoff |

**Security-critical production discrepancy:** All O14 findAll, findById, name uniqueness, findByType, findByStatus, lockById and aggregate JPQL are **GLOBAL**, not organization filtered. They are correct as original single-demo workspace methods but NOT safe unchanged for future authenticated multi-tenant product. The actual SQL/JPQL tenant rewrite and DB constraints must follow a separate reviewed migration and failure tests (research CP41), never merely add a client-side filter.

## 3. Components: essential web-facing method sequences

The final Word proposal must describe these methods with exact executed steps and errors. The sequences below are the direct O14 behavioral specification; future one-product changes must be documented separately rather than silently represented as original O14.

### DepartmentComponent
- create(rawName): normalize/trim display and nameKey; check duplicate nameKey; persist Department; return DepartmentView 201.
- list(): findAll and return array without child entity serialization.
- update(id,rawName): validate positive ID; load target; normalize new key, reject another existing key; save/return.
- delete(id): load target; if ProjectRepository.existsByDepartment_Id(id), 409; delete; return MessageView.

### ProjectComponent
- create(departmentId,rawName,startAt,endAt): validate present/future start and end>start; load Department or 404; persist and return ProjectView.
- list(departmentId): positive optional filter; findByDepartment_Id or findAll; return short Project DTOs.
- update(id,rawName,startAt,endAt): acquire Project lock; compare actual dates; changed dates require valid future start and NO historically approved Allocation; name-only changes allowed; persist and return.
- delete(id): lock Project; require no ResourceRequirement; physically delete and return message.

### ResourceComponent
- create(rawName,type,totalQuantity): validate 0..1,000,000, type, normalized unique name; persist; return view with available=T.
- list(type): find Resource rows; sample one Clock instant; execute grouped JPQL to compute H/I for all result IDs; return derived ResourceView[]; do not issue one aggregate SQL query per item.
- getAvailability(id): load Resource, sample one Clock instant, aggregate effective holds and issued custody, compute availability via checked arithmetic.
- update(id,rawName,totalQuantity,expectedTotalQuantity): validate supplied totals; pessimistic lock row; compare current stored total to expected (stale 409), check new unique key, compute H/I; refuse reducing T below committed amount; persist+flush; return derived view.
- delete(id): pessimistic lock; reject any Requirement and any physical stock>0; delete eligible row and flush. **Future extra ledger means add historical movement guard**, not deletion of past stock evidence.

### RequirementComponent
- create(projectId,resourceId,quantity): lock/recheck relevant parent IDs; require project not ended, resource exists, quantity range, unique pair; persist; return covered/shortage derived.
- list(projectId): validate filter; load subset/all; sample Clock; aggregate coverage per requirement, return derived Q/covered/shortage.
- update(id,quantity): lock parent state; reject ended Project, insufficient quantity for effective covered and largest still-PENDING request; persist and return.
- delete(id): lock relevant parents/Requirement; refuse if any Allocation history; delete otherwise.

### AllocationComponent
- create(requirementId,quantity,borrowerName,phoneNumber,smsConsent): validate positive/reference/quantity/name/E164 when consented; lock/recheck Project→Resource→Requirement; reject ended Project and q>Requirement Q; persist PENDING with createdAt, no reserved stock; redacted view 201.
- list(requirementId,status): optional filters intersect, one sampled Clock instant, return redacted AllocationView[].
- approve(id): lock parent chain and Allocation, require PENDING and Project end future; compute effective covered and availability; require q<=uncovered Q and q<=stock; set APPROVED/approvedAt; commit.
- issue(id): require APPROVED REUSABLE and active Project interval; set ISSUED/issuedAt, **T unchanged**.
- returnItems(id): require ISSUED REUSABLE; only after physical whole usable batch return; set RETURNED/returnedAt; may happen after end.
- consume(id): require APPROVED CONSUMABLE and active Project; subtract q from Resource.totalQuantity and set CONSUMED/consumedAt in SAME transaction; no duplicate consumption.
- cancel(id): require PENDING/APPROVED; set CANCELLED/cancelledAt and USER_REQUEST or EXPIRED according to endAt; physical total unchanged.
- delete(id): require never approved (approvedAt null), PENDING or CANCELLED; physically remove; protect historically approved rows.
- revokeSmsConsent(id): lock Allocation, set smsConsent=false and phoneNumber=null, idempotently return redacted view, no stock effect.
- expireIfEligible(id): worker entry; lock/recheck open PENDING/APPROVED and Project.endAt<=now; CANCELLED/EXPIRED, no issued custody mutation; safe repeat.

### Scheduler / reminder / external Components
- ExpiryScheduler.scanExpiredAllocations(): @Scheduled fixed delay configurable default 3600000 ms, bounded open candidate ID scan, delegated per-row transactional expiry; errors isolated/logged without global crash.
- ReminderScheduler.scanUpcomingReturns(): @Scheduled default hourly, skip when DISABLED; bounded consented ISSUED reusable due candidates and dispatcher; scan uses [now,now+24h).
- ReminderScheduler.reconcileStaleClaims(): @Scheduled recover CLAIMED older than configured threshold to UNKNOWN, **not** automatic resend.
- ReminderComponent.claimOne(id): under row lock verify reminder eligibility/consent/unclaimed/E164; set claimedAt and CLAIMED; persist before network call; invalid E164 suppressed.
- ReminderComponent.prepareForDispatch(id): lock/recheck current state and consent, suppress if invalid; build fictional-safe due message; return one dispatch payload.
- ReminderDispatcher.dispatchOne(id): claim → prepare → call transport OUTSIDE database transaction → recordOutcome; unknown network state is UNKNOWN not automatically retried.
- ReminderComponent.recordOutcome(id,result): lock and save SIMULATED/PROVIDER_ACCEPTED/REJECTED/UNKNOWN with optional HTTP code/SID, no stale detached entity overwrite.
- FakeTwilioTransport.send(...): actual Retrofit/Gson HTTP to a loopback service; acceptance is SIMULATED and not proof of carrier delivery.
- Future official TwilioRestTransport and ResourceImageComponent: NOT IMPLEMENTED in O14; exact requirements/tests in 01–03, no inaccurate as-built method description.

## 4. Required updates to the final as-built proposal

The printed assignment specifically asks for fields/type/nullability/validation, Component method steps, and custom repository method/parameters. This document plus 02 provide the **source-verified O14 baseline** for the final revised Word proposal. The proposal must ultimately be rebuilt FROM THE FINAL IMPLEMENTATION SHA and document new organization/auth/media/ledger fields and all new custom repository methods, without calling paper plans executed code.

Keep source-to-proposal references, revision date and differences to the original 25-route Word document explicit. No fabricated group sign-off or Canvas receipt.
