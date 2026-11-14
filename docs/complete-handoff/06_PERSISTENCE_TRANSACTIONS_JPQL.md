# 06 — Java components, repositories, JPQL, locking, concurrency, atomicity

## Course-compatible structure

Jersey JAX-RS @Component Controller (@Path, @GET, @POST, @Consumes JSON, @Produces JSON) -> @Autowired Spring Component (validation, business rule, @Transactional) -> JpaRepository (derived and @Query JPQL) -> MySQL InnoDB. Controller does NOT directly update ORM graph. DTO request/response classes avoid exposing phoneNumber or bidirectional JPA relations and prevent clients writing server-computed fields.

Jersey ResourceConfig register each @Component controller individually to work in packaged archives. Example @ApplicationPath("/api") + controller @Path("/departments"), no double prefix. @EnableScheduling on app, but do NOT copy SchedulingApp teaching example's exclusions disabling DataSource/JPA. Spring @Transactional uses proxy semantics; do not rely on self-invocation or @PostConstruct proxied transaction. Use a separate Spring bean for @Transactional expiry-one and reminder-claim methods.

## Repository contracts

DepartmentRepository extends JpaRepository<Department,Long>:
- existsByNameKey(String nameKey).

ProjectRepository:
- findByDepartment_Id(Long departmentId).
- existsByDepartment_Id(Long departmentId).
- Optional<Project> lockById(Long id) @Lock(PESSIMISTIC_WRITE) and explicit JPQL; verify actual behavior.

ResourceRepository:
- existsByNameKey(String nameKey).
- findByType(ResourceType type).
- Optional<Resource> lockById(Long id) @Lock(PESSIMISTIC_WRITE), explicit query.

ResourceRequirementRepository:
- findByProject_Id(Long projectId).
- existsByProject_Id(Long projectId).
- existsByResource_Id(Long resourceId).
- existsByProject_IdAndResource_Id(Long projectId,Long resourceId).
- Appropriate locked query or reload for update/delete, respecting Project then Resource.

AllocationRepository:
- findByRequirement_Id(Long requirementId).
- findByStatus(AllocationStatus status).
- findByRequirement_IdAndStatus(Long requirementId,AllocationStatus status).
- existsByRequirement_Id(Long requirementId).
- sumEffectiveHolds(resourceId,approved,now).
- sumIssuedCustody(resourceId,issued).
- sumCoveredQuantity(requirementId,completedStatuses,approved,now).
- maxPendingQuantity(requirementId,pending).
- countHistoricallyApproved(projectId).
- findExpiredCandidateIds(now,limit).
- findReminderCandidateIds(now,limit).
- explicit lockById of Allocation.
Do NOT redeclare JpaRepository inherited save, findById, findAll, delete unnecessarily.
JPQL references Java property names, not physical SQL columns.
Aggregates SUM(Integer) should be Long; handle null as zero with safe long arithmetic. Empty collection repository returns empty list; ordinary scalar aggregate can return null.

## Candidate JPQL (must compile and run on actual Spring Data/JPA stack)

Effective holds:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.resource.id = :resourceId " +
       "AND a.status = :approved " +
       "AND a.requirement.project.endAt > :now")
Long sumEffectiveHolds(@Param("resourceId") Long resourceId,
                       @Param("approved") AllocationStatus approved,
                       @Param("now") LocalDateTime now);
~~~

Issued custody:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.resource.id = :resourceId " +
       "AND a.status = :issued")
Long sumIssuedCustody(@Param("resourceId") Long resourceId,
                      @Param("issued") AllocationStatus issued);
~~~

Requirement covered:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.id = :requirementId " +
       "AND (a.status IN :completed " +
       "OR (a.status = :approved " +
       "AND a.requirement.project.endAt > :now))")
Long sumCovered(@Param("requirementId") Long requirementId,
                @Param("completed") Collection<AllocationStatus> completed,
                @Param("approved") AllocationStatus approved,
                @Param("now") LocalDateTime now);
~~~
Completed = ISSUED, RETURNED, CONSUMED exactly. Verify 'IN :completed' binding syntax and no empty collection (always 3 values).

Max pending:
~~~java
@Query("SELECT MAX(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.id = :requirementId " +
       "AND a.status = :pending")
Integer maxPendingQuantity(@Param("requirementId") Long requirementId,
                           @Param("pending") AllocationStatus pending);
~~~

Historical approved:
~~~java
@Query("SELECT COUNT(a) FROM Allocation a " +
       "WHERE a.requirement.project.id = :projectId " +
       "AND a.approvedAt IS NOT NULL")
Long countHistoricallyApproved(@Param("projectId") Long projectId);
~~~

Expiry candidate query:
~~~java
@Query("SELECT a.id FROM Allocation a " +
       "WHERE a.status IN :unfulfilled " +
       "AND a.requirement.project.endAt <= :now " +
       "ORDER BY a.id")
List<Long> findExpiredCandidateIds(
  @Param("unfulfilled") Collection<AllocationStatus> unfulfilled,
  @Param("now") LocalDateTime now,
  Pageable pageable);
~~~
unfulfilled = PENDING, APPROVED. Use PageRequest.of(0, boundedNumber) API version compatible with chosen Spring Data. Query candidates then process one ID per small transaction; lock after lookup and recheck.

Reminder candidate selection should JOIN through Requirement/Resource/Project and filter ISSUED, ResourceType.REUSABLE, consent=true, phone not null, reminderClaimedAt null and endAt within [now,now+24h). Query returns IDs only; selection is NOT final eligibility proof. Recheck after locking Allocation and using same clock sample.

Pessimistic locks:
~~~java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Resource r WHERE r.id = :id")
Optional<Resource> lockById(@Param("id") Long id);
~~~
Analogous Project/Allocation. Test Hibernate SQL generated and race results; do not assume all lock methods compile by reading source only.

## Correct lock order and transaction boundaries

Inventory mutating commands:
1. Determine associated Project/Resource identifiers (prelookup can be stale; never trust after locks).
2. Start Spring transactional method (prefer verified READ_COMMITTED with InnoDB).
3. Lock Project row.
4. Lock Resource row.
5. Reload/refresh Requirement, Allocation and related associations, and lock Allocation when changing it.
6. Sample one Clock instant in Asia/Manila.
7. Re-evaluate current state, FK identity, project dates, available H/I and Requirement coverage.
8. Persist all related modifications.
9. Commit; on refusal or exception rollback.

Resource total update locks Resource only and computes obligations under that lock. Reminder-only claim/revoke locks Allocation without then acquiring Project/Resource WRITE locks. This avoids reverse lock-order cycle; inventory commands lock Project/Resource first, then Allocation. Queries that read parents without write locks in SMS are advisory; final pre-dispatch recheck cannot absolutely eliminate send-return race once outbound call starts.
Revalidate entities after waiting for lock. JPA's persistence context can retain stale prelock instances; explicit refresh/re-query/clear as appropriate, verified in tests. Ensure completion statuses/timestamps included in fresh DB aggregate (flush/refresh), and compare immediate DTO to a fresh GET after commit.

Approve:
- Require PENDING and Project.endAt > now; early start allowed.
- require alloc.quantity <= availableResource AND <= Requirement.quantity - covered.
- Save APPROVED and approvedAt once.
- Failures leave PENDING unchanged.

Issue:
- Require APPROVED, REUSABLE, startAt <= now < endAt.
- Save ISSUED/issuedAt, no stock total decrement.

Return:
- Require ISSUED REUSABLE; allow late; mark RETURNED/returnedAt; no stock total adjustment.
- Officer declares whole batch usable, not objective physical proof.

Consume:
- Require APPROVED, CONSUMABLE, active period, underlying Resource.total >= quantity.
- Resource.totalQuantity -= quantity; Allocation.status=CONSUMED; consumedAt=now in SAME transaction.
- After commit availability remains old available as hold disappears and stock drops same q.
- Force test exception after one write and prove both rollback.

Cancel:
- PENDING/APPROVED only; after expiry reason EXPIRED, otherwise USER_REQUEST. Preserve approvedAt. No second physical stock decrease.
Delete:
- Only PENDING or CANCELLED and approvedAt null; actual DB deletion.
Requirement update:
- Project and Resource locks; newQ >= max(covered,largest live pending); refuse Project ended.
Project date update:
- Project lock and check countHistoricallyApproved>0; changed dates require new future start/end; unchanged dates allow name-only.
Project/Resource/Requirement create and delete must respect same lock conventions for races. Department delete vs Project create depends on DB FK and appropriate locking; catch recognized integrity collision and map to 409 when supportable, not generic success.

## Race tests

1. Resource five units, two PENDING Allocation requests qty four for different Requirements: parallel APPROVE, exactly one success, available >=0.
2. Same Requirement qty six, two PENDING qty four, different rows: parallel APPROVE, exactly one success, covered<=6.
3. Concurrent consume and Resource stock correction with expectedTotalQuantity stale: one correct winner, no overwritten stock.
4. Concurrent Requirement update and approval: never coverage > demand.
5. Project date edit and approval: historical approval prevents date change if approval happens first; if date changed first, subsequent approval must use new times.
6. Expiry cleanup vs return/issue: lock and reload; no fraudulent physical return.
7. Reminder result recording vs RETURN must not overwrite state via stale detached entity.
8. Concurrent Resource create with same normalized name: DB unique index wins, no duplicate.
9. Concurrent Requirement create for same pair: DB unique index wins.
10. Lost HTTP response after successful COMSUME: retry state CONSUMED refused; never double deduct. (Correct spelling CONSUME in source code.)

## Error/rollback semantics

Validation exceptions from request => 400; server-generated DTO constraint defect => 500; missing parent/target => 404; business conflict => 409; recognized lock deadlock/timeout => 503 or mapped safe retry; unexpected DB integrity internal defect => 500 sanitized unless known duplicate/reference collision; never print SQL stack in public JSON.
A rejected business operation should not commit partial writes. A missing HTTP response DOES NOT establish DB rollback; client must re-fetch.
No automatic POST retries by frontend or scheduler after uncertain commit. Ensure exception classes are unchecked or @Transactional(rollbackFor=...) for checked domain exceptions as appropriate.

## Test evidence

For each state-changing operation check HTTP code, persisted state, and independent invariant. Sequential Postman can exercise route behavior; only real parallel DB tests prove concurrency. Test Java compiler/JPA initialization/SQL/timeout details with actual environment, not just this document.
