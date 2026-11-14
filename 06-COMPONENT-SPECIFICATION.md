# Component and Method Specification

## Shared public method boundary

Method signatures below are specification signatures, not application source code. Actor is a resolved immutable {userId,role,departmentId,system} value supplied by the trusted resolver; clients never construct it. Window is {startAt,endAt}; quantities Long. Each OpEndpointIDRequest is exactly the DTO in05. Id methods take Long id. A query DTO contains exactly the corresponding filter keys with OffsetDateTime parsed to Instant. Page<T> here means the public page DTO, not exposing Spring internals. Every public method validates DTO with javax Validator before business checks, checks actor/active/object permissions, and maps domain exceptions through the standard error catalogue. Nonexistent references404; invalid fields400; disallowed action403; lifecycle and invariant failures409.

Dependencies follow the listed repository calls plus PermissionComponent, AuditComponent and Clock. Inventory writes additionally call AvailabilityComponent.calculateLocked. Report and shortage components call the same pure balance/sweep calculations, not controllers. Notification external transport is only TwilioIntegrationComponent. Component methods do not make internal HTTP calls to their own REST API. All Write methods are public proxy-invoked Transactional(rollbackFor=Exception.class); Read are readOnly consistent snapshots. No self-invocation is relied on to start a new transaction. Exceptions abort the whole command before any success DTO is returned.

## AllocationComponent

Responsibility: One requested quantity and its immutable approved schedule/recipient.; List visible allocations with bounded filters.; Retrieve one Allocation with its derived public fields.; Hard delete only REQUESTED by requester or HEAD/OFFICER, with no movements or notifications; append audit. Approved/rejected/cancelled history retained;409 directs cancel/release.; Commit requested quantity without moving stock.; Reject a pending request without deleting history.; Cancel pending or entirely unissued approved allocation.; Release some or all unissued approved remainder.; Record ISSUE against this allocation.; Record RETURN against this allocation.; Record CONSUMPTION against this allocation.

Dependencies: AllocationRepository, AuditEventRepository, InventoryTransactionRepository, ResourceRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 06C request

Signature: AllocationView request(Op06CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: One requested quantity and its immutable approved schedule/recipient. Input fields and return DTO are exactly those in05 operation06C. Permission: project owner or HEAD in own department; recipient active in that department.

1. Canonicalize requestKey payload; resolve requirement/project/resource and lock resource then requirement.
2. Check identical-key replay first; different hash409. Verify active demand and APPROVED/IN_PROGRESS project, authorized requester and same-department recipient.
3. Require endAt > Clock.instant(); a current interval is allowed, a wholly past commitment is not.
4. Require quantity <= unallocated requirement demand; REQUESTED allocations do not reserve stock or count as approved coverage.
5. Copy requirement interval, fix requester/recipient, set REQUESTED, save and audit; do not autoapprove.

Repository calls: AllocationRepository.save, AllocationRepository.findById, AllocationRepository.findByRequestKey, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, AllocationRepository.findByRequirement_Project_Id, AllocationRepository.findByRequirement_Id, AllocationRepository.findOverlappingApproved, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; AvailabilityComponent.calculateLocked for capacity commands; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED, IDEMPOTENCY_KEY_REUSED, RESOURCE_TYPE_MISMATCH, RESOURCE_OVERALLOCATION, REQUIREMENT_COVERAGE_EXCEEDED, INSUFFICIENT_PHYSICAL_STOCK, REVERSAL_DEPENDENCY_CONFLICT. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 06L list

Signature: Page<AllocationView> list(Op06LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible allocations with bounded filters. Input fields and return DTO are exactly those in05 operation06L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: AllocationRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 06R get

Signature: AllocationView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one Allocation with its derived public fields. Input fields and return DTO are exactly those in05 operation06R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: AllocationRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 06D delete

Signature: NoContent delete(Long id, Op06DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Hard delete only REQUESTED by requester or HEAD/OFFICER, with no movements or notifications; append audit. Approved/rejected/cancelled history retained;409 directs cancel/release. Input fields and return DTO are exactly those in05 operation06D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: AllocationRepository.findById, AllocationRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### A01 approve

Signature: AllocationView approve(Long id, OpA01Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Commit requested quantity without moving stock. Input fields and return DTO are exactly those in05 operationA01. Permission: assigned CUSTODIAN different from requester.

1. Resolve pool; acquire resource then requirement/allocation locks and re-read actor, expectedVersion and REQUESTED state.
2. Verify active requirement/resource and approved/in-progress project; requester/recipient references remain active.
3. Require endAt > Clock.instant(); a current interval is allowed, a wholly past commitment is not.
4. Sum existing approved effective quantities for this requirement; reject if adding quantity exceeds demand.
5. Rebuild balances and current pool commitments including overdue outstanding. Sweep reusable interval or compute global consumable ATP.
6. Require the whole requested quantity fits; no silent partial approval. If not, keep REQUESTED and return409 with available/competing IDs.
7. Set APPROVED, decidedBy/At/reason; increment resource revision and audit. No SMS is sent for every approval.

Repository calls: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; AvailabilityComponent.calculateLocked for capacity commands; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, INVALID_STATE, RESOURCE_OVERALLOCATION, REQUIREMENT_COVERAGE_EXCEEDED, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### A02 reject

Signature: AllocationView reject(Long id, OpA02Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Reject a pending request without deleting history. Input fields and return DTO are exactly those in05 operationA02. Permission: assigned CUSTODIAN different from requester.

1. Lock resource and re-read request; verify actor/version and REQUESTED state.
2. Require reason; set REJECTED with decidedBy/At and audit. It held no quantity to release.

Repository calls: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### A03 cancel

Signature: AllocationView cancel(Long id, OpA03Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Cancel pending or entirely unissued approved allocation. Input fields and return DTO are exactly those in05 operationA03. Permission: requester/own HEAD, assigned CUSTODIAN or OFFICER.

1. Lock pool and re-read version/state.
2. Allow REQUESTED or APPROVED only; approved cancellation requires issued/consumed net0 and no outstanding.
3. Set CANCELLED and reason, releasing effective approved commitment; bump resource revision and audit. Rejected and resolved history is not cancelled.

Repository calls: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### A04 release

Signature: AllocationView release(Long id, OpA04Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Release some or all unissued approved remainder. Input fields and return DTO are exactly those in05 operationA04. Permission: requester/own HEAD, assigned CUSTODIAN or OFFICER.

1. Lock pool and re-read APPROVED allocation/version and ledger counters.
2. Require positive quantity <= R; never release issued or consumed coverage.
3. Increase releasedUnissued, retain approved historical supply, bump version and audit exact released amount and reason.

Repository calls: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### A05 issueResources

Signature: InventoryTransactionView issueResources(Long id, OpA05Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Record ISSUE against this allocation. Input fields and return DTO are exactly those in05 operationA05. Permission: assigned CUSTODIAN.

1. Resolve allocation/pool; call InventoryTransactionComponent.record with server-filled resource/allocation/type fields, preserving the commandKey.
2. Acquire resource lock and perform identical-key replay check.
3. Require reusable and project APPROVED/IN_PROGRESS; startAt <= now < endAt; q <= unissued R.
4. Require physical S >= q plus other active-now unissued commitments; report conflicts if not.
5. Derive immutable signed movement, recheck balances/counters, save with new resource revision and append audit in one transaction.

Repository calls: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save, InventoryTransactionRepository.save, InventoryTransactionRepository.findByResource_IdAndCommandKey.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; InventoryTransactionComponent.record for convenience movement routes; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, RESOURCE_TYPE_MISMATCH, INSUFFICIENT_PHYSICAL_STOCK, RESOURCE_OVERALLOCATION, IDEMPOTENCY_KEY_REUSED, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### A06 returnResources

Signature: InventoryTransactionView returnResources(Long id, OpA06Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Record RETURN against this allocation. Input fields and return DTO are exactly those in05 operationA06. Permission: assigned CUSTODIAN.

1. Resolve allocation/pool; call InventoryTransactionComponent.record with server-filled resource/allocation/type fields, preserving the commandKey.
2. Acquire resource lock and perform identical-key replay check.
3. Require reusable, outstanding >= q and inspection USABLE or DAMAGED; allow returns after project cancellation/end.
4. Apply O-to-S for usable or O-to-B for damaged; update resolution counters and include impacted commitments for damaged stock.
5. Derive immutable signed movement, recheck balances/counters, save with new resource revision and append audit in one transaction.

Repository calls: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save, InventoryTransactionRepository.save, InventoryTransactionRepository.findByResource_IdAndCommandKey.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; InventoryTransactionComponent.record for convenience movement routes; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, RESOURCE_TYPE_MISMATCH, INSUFFICIENT_PHYSICAL_STOCK, RESOURCE_OVERALLOCATION, IDEMPOTENCY_KEY_REUSED, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### A07 consumeResources

Signature: InventoryTransactionView consumeResources(Long id, OpA07Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Record CONSUMPTION against this allocation. Input fields and return DTO are exactly those in05 operationA07. Permission: assigned CUSTODIAN.

1. Resolve allocation/pool; call InventoryTransactionComponent.record with server-filled resource/allocation/type fields, preserving the commandKey.
2. Acquire resource lock and perform identical-key replay check.
3. Require consumable, project APPROVED/IN_PROGRESS, within its requirement interval and q <= unconsumed R.
4. Require physical P >= q plus all other remaining consumable commitments. Reduce P and own reservation equally; no issue-then-consume duplication.
5. Derive immutable signed movement, recheck balances/counters, save with new resource revision and append audit in one transaction.

Repository calls: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save, InventoryTransactionRepository.save, InventoryTransactionRepository.findByResource_IdAndCommandKey.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; InventoryTransactionComponent.record for convenience movement routes; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, RESOURCE_TYPE_MISMATCH, INSUFFICIENT_PHYSICAL_STOCK, RESOURCE_OVERALLOCATION, IDEMPOTENCY_KEY_REUSED, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## AuditComponent

Responsibility: Append-only actor/action/change record including non-stock decisions.; List visible audit-events with bounded filters.; Retrieve one AuditEvent with its derived public fields.; Never physically delete audit history. Delete endpoint returns409 IMMUTABLE_HISTORY. A correction is a new NOTE referencing the original id in its reason, not editing the original.

Dependencies: AuditEventRepository, PermissionComponent, Clock, DTO mapper.

### 09C appendNote

Signature: AuditEventView appendNote(Op09CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Append-only actor/action/change record including non-stock decisions. Input fields and return DTO are exactly those in05 operation09C. Permission: OFFICER/ADMIN; NOTE only through public create.

1. Check OFFICER/ADMIN and restrict public action to NOTE.
2. Resolve allowlisted target, generate targetSummary/requestId and reject supplied before/after/actor/time fields.
3. Persist immutable note with reason; do not recursively audit the AuditEvent insertion.

Repository calls: AuditEventRepository.save, AuditEventRepository.findById, AuditEventRepository.findByTargetTypeAndTargetIdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 09L list

Signature: Page<AuditEventView> list(Op09LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible audit-events with bounded filters. Input fields and return DTO are exactly those in05 operation09L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: AuditEventRepository.findAll.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 09R get

Signature: AuditEventView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one AuditEvent with its derived public fields. Input fields and return DTO are exactly those in05 operation09R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: AuditEventRepository.findById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 09D delete

Signature: NoContent delete(Long id, Op09DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Never physically delete audit history. Delete endpoint returns409 IMMUTABLE_HISTORY. A correction is a new NOTE referencing the original id in its reason, not editing the original. Input fields and return DTO are exactly those in05 operation09D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: AuditEventRepository.findById, AuditEventRepository.delete.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED, IMMUTABLE_HISTORY. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## AvailabilityComponent

Responsibility: Explain present/future capacity for one resource and interval.

Dependencies: AllocationRepository, InventoryTransactionRepository, ResourceRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### Q01 query

Signature: AvailabilityView query(OpQ01Query query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Explain present/future capacity for one resource and interval. Input fields and return DTO are exactly those in05 operationQ01. Permission: organization read.

1. Validate active resource and present/future <=90-day interval; capture asOf once.
2. Read ledger plus all approved resource allocations in one repeatable-read snapshot, derive balances and commitment intervals.
3. Sweep grouped endpoints or calculate consumable ATP; return raw negative deficit, clamped allocatable capacity, conflicting IDs and return-dependent warning.

Repository calls: ResourceRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## DepartmentComponent

Responsibility: Flat accountable unit inside the one organization.; List visible departments with bounded filters.; Retrieve one Department with its derived public fields.; Hard delete only when no user, project or resource references exist. Otherwise409 DEPENDENCY_EXISTS; update active=false only when no active operational dependency.; Versioned update of permitted Department fields.

Dependencies: AuditEventRepository, DepartmentRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 01C create

Signature: DepartmentView create(Op01CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Flat accountable unit inside the one organization. Input fields and return DTO are exactly those in05 operation01C. Permission: ADMIN.

1. Normalize code/name; validate unique code, ADMIN and nonblank fields.
2. Persist active department and append CREATE audit; no stock or hierarchy exists.

Repository calls: DepartmentRepository.save, DepartmentRepository.findById, DepartmentRepository.findByCode, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 01L list

Signature: Page<DepartmentView> list(Op01LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible departments with bounded filters. Input fields and return DTO are exactly those in05 operation01L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: DepartmentRepository.findAll.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 01R get

Signature: DepartmentView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one Department with its derived public fields. Input fields and return DTO are exactly those in05 operation01R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: DepartmentRepository.findById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 01D delete

Signature: NoContent delete(Long id, Op01DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Hard delete only when no user, project or resource references exist. Otherwise409 DEPENDENCY_EXISTS; update active=false only when no active operational dependency. Input fields and return DTO are exactly those in05 operation01D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: DepartmentRepository.findById, DepartmentRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 01U update

Signature: DepartmentView update(Long id, Op01URequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Versioned update of permitted Department fields. Input fields and return DTO are exactly those in05 operation01U. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle.
2. Setting active=false requires no active AppUser, active Resource or project outside COMPLETED/CANCELLED referencing this department; otherwise409 DEPENDENCY_EXISTS.
3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced.
4. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval.
5. Apply metadata change and append before/after audit atomically; return updated version.

Repository calls: DepartmentRepository.findById, DepartmentRepository.save, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, DUPLICATE_RECORD, DEPENDENCY_EXISTS, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## InventoryTransactionComponent

Responsibility: Immutable physical/custody delta and correction event.; List visible inventory-transactions with bounded filters.; Retrieve one InventoryTransaction with its derived public fields.; Never physically delete a posted movement. Delete endpoint returns409 IMMUTABLE_HISTORY with reversal route. No permission to clear the ledger through REST.; Append constrained reversal of a latest event.

Dependencies: AllocationRepository, AuditEventRepository, InventoryTransactionRepository, ResourceRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 07C record

Signature: InventoryTransactionView record(Op07CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Immutable physical/custody delta and correction event. Input fields and return DTO are exactly those in05 operation07C. Permission: assigned CUSTODIAN; OFFICER for recount/reversal.

1. Resolve pool and optional allocation; take resource lock, canonicalize commandKey/hash and return identical replay before effect application.
2. Dispatch the declared type through the exact typed movement rules in02; verify operator and required/forbidden references and inspection.
3. Rebuild ledger balances and allocation counters; check nonnegative physical/custody balances, eligible interval/state and type-specific reservation rules.
4. Derive signed effect vector, increment resource revision, flush and capture resourceRevisionAfter; insert immutable transaction and append audit in the same transaction.
5. Return derived balances and impacted commitment IDs; observed damage/loss may create a reported future planning deficit.

Repository calls: InventoryTransactionRepository.save, InventoryTransactionRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, InventoryTransactionRepository.findByResource_IdAndCommandKey, InventoryTransactionRepository.findByReversalOf_Id, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc, ResourceRepository.lockById, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; AvailabilityComponent.calculateLocked for capacity commands; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED, IDEMPOTENCY_KEY_REUSED, RESOURCE_TYPE_MISMATCH, RESOURCE_OVERALLOCATION, REQUIREMENT_COVERAGE_EXCEEDED, INSUFFICIENT_PHYSICAL_STOCK, REVERSAL_DEPENDENCY_CONFLICT. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 07L list

Signature: Page<InventoryTransactionView> list(Op07LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible inventory-transactions with bounded filters. Input fields and return DTO are exactly those in05 operation07L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: InventoryTransactionRepository.findAll.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 07R get

Signature: InventoryTransactionView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one InventoryTransaction with its derived public fields. Input fields and return DTO are exactly those in05 operation07R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: InventoryTransactionRepository.findById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 07D delete

Signature: NoContent delete(Long id, Op07DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Never physically delete a posted movement. Delete endpoint returns409 IMMUTABLE_HISTORY with reversal route. No permission to clear the ledger through REST. Input fields and return DTO are exactly those in05 operation07D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: InventoryTransactionRepository.findById, InventoryTransactionRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED, IMMUTABLE_HISTORY. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### T01 reverse

Signature: InventoryTransactionView reverse(Long id, OpT01Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Append constrained reversal of a latest event. Input fields and return DTO are exactly those in05 operationT01. Permission: OFFICER.

1. Resolve original and lock its resource; verify original is not REVERSAL and no existing reversal.
2. Check commandKey replay; require original.resourceRevisionAfter equals current resource.revision and no later ledger event; otherwise409 dependency conflict.
3. Negate all original signed deltas and allocation counter effects; recheck balances, coverage, lifecycle and commitment capacity.
4. Insert REVERSAL linked to original, increment resource revision/capture it after flush, append correction audit; never modify original.

Repository calls: InventoryTransactionRepository.findById, InventoryTransactionRepository.findByReversalOf_Id, InventoryTransactionRepository.findByResource_IdAndCommandKey, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, InventoryTransactionRepository.save, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; AvailabilityComponent.calculateLocked for capacity commands; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, REVERSAL_DEPENDENCY_CONFLICT, IDEMPOTENCY_KEY_REUSED, RESOURCE_OVERALLOCATION, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## NotificationComponent

Responsibility: Durable return reminder event/outbox and provider outcome.; List visible notifications with bounded filters.; Retrieve one NotificationLog with its derived public fields.; Hard delete only unattempted QUEUED/SKIPPED/CANCELLED rows with attemptCount0, by OFFICER/ADMIN; record AuditEvent. Otherwise409 IMMUTABLE_HISTORY. Cancellation of queued reminder is explicit and prevents sending.; Cancel a reminder before any send begins.; Run one due outbox record through the same dispatcher as scheduler.; Fetch provider status for a known SID; no new message.; Queue a known not-accepted failure within bounded attempts.

Dependencies: AllocationRepository, AppUserRepository, AuditEventRepository, InventoryTransactionRepository, NotificationLogRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 08C enqueueManual

Signature: NotificationLogView enqueueManual(Op08CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Durable return reminder event/outbox and provider outcome. Input fields and return DTO are exactly those in05 operation08C. Permission: assigned CUSTODIAN or OFFICER; server recomputes due eligibility.

1. Resolve allocation and recipient; require authorized actor, reusable outstanding custody and requested kind currently in reminder window.
2. Compute unique eventKey from kind/allocation/endAt/recipient. Existing key returns existing view200, never a second event.
3. Read current consent/contact; create QUEUED with nextAttemptAt now or SKIPPED with reason, append audit, never call Twilio here.

Repository calls: NotificationLogRepository.save, NotificationLogRepository.findById, NotificationLogRepository.findByEventKey, NotificationLogRepository.findDue, NotificationLogRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED, NOTIFICATION_NOT_ELIGIBLE. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 08L list

Signature: Page<NotificationLogView> list(Op08LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible notifications with bounded filters. Input fields and return DTO are exactly those in05 operation08L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: NotificationLogRepository.findAll.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 08R get

Signature: NotificationLogView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one NotificationLog with its derived public fields. Input fields and return DTO are exactly those in05 operation08R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: NotificationLogRepository.findById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 08D delete

Signature: NoContent delete(Long id, Op08DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Hard delete only unattempted QUEUED/SKIPPED/CANCELLED rows with attemptCount0, by OFFICER/ADMIN; record AuditEvent. Otherwise409 IMMUTABLE_HISTORY. Cancellation of queued reminder is explicit and prevents sending. Input fields and return DTO are exactly those in05 operation08D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: NotificationLogRepository.findById, NotificationLogRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### N01 cancel

Signature: NotificationLogView cancel(Long id, OpN01Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Cancel a reminder before any send begins. Input fields and return DTO are exactly those in05 operationN01. Permission: recipient for own, assigned CUSTODIAN, OFFICER or ADMIN.

1. Lock notification and re-read version; only QUEUED/SKIPPED with attemptCount0 can cancel.
2. Set CANCELLED and reason, clear nextAttemptAt, audit; SENDING/accepted/unknown cannot be recalled.

Repository calls: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### N02 dispatch

Signature: NotificationLogView dispatch(Long id, OpN02Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Run one due outbox record through the same dispatcher as scheduler. Input fields and return DTO are exactly those in05 operationN02. Permission: OFFICER or ADMIN; restricted live allowlist.

1. Call claim(id,asOf) in a short transaction; recheck QUEUED,due,consent,recipient and outstanding custody.
2. After committing SENDING and an attempt marker, perform Retrofit send outside any DB transaction.
3. Call finish(id,result) in a new transaction; bounded retry only on known not-accepted outcome, unknown outcomes never auto-resend.

Repository calls: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save, AppUserRepository.findById, AllocationRepository.findById, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: SPLIT. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, PROVIDER_OUTCOME_UNKNOWN, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### N03 refresh

Signature: NotificationLogView refresh(Long id, OpN03Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Fetch provider status for a known SID; no new message. Input fields and return DTO are exactly those in05 operationN03. Permission: recipient for own, OFFICER or ADMIN.

1. Read visible log; require LIVE profile and stored providerSid, or FAKE fixture SID in fake mode.
2. Fetch exact SID through TwilioIntegrationComponent.fetchStatus outside DB transaction; require SID matches.
3. Lock log and persist mapped status/error in a fresh transaction; never regress terminal delivery or enqueue a new send.

Repository calls: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save, NotificationLogRepository.findById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: SPLIT. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### N04 retry

Signature: NotificationLogView retry(Long id, OpN04Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Queue a known not-accepted failure within bounded attempts. Input fields and return DTO are exactly those in05 operationN04. Permission: OFFICER or ADMIN.

1. Lock log/version; allow FAILED only with providerSid null and a stored allowlisted known-not-accepted error; attempts <3.
2. Revalidate consent, due kind and outstanding custody; set QUEUED,nextAttemptAt to now plus backoff; audit.
3. UNKNOWN, ACCEPTED, DELIVERED and UNDELIVERED cannot be retried as the same event; no blind duplicate SMS.

Repository calls: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, INVALID_STATE, PROVIDER_OUTCOME_UNKNOWN, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## ProjectComponent

Responsibility: Dated departmental activity with internal planning approval.; List visible projects with bounded filters.; Retrieve one Project with its derived public fields.; Hard delete only DRAFT with no requirements/allocations. Historical project uses cancel or completed archival; no cascading delete.; Versioned update of permitted Project fields.; Move internal project to SUBMITTED.; Move internal project to APPROVED.; Move internal project to DRAFT.; Move internal project to IN_PROGRESS.; Move internal project to COMPLETED.; Move internal project to CANCELLED.; Atomically replace full unissued project schedule.

Dependencies: AllocationRepository, AuditEventRepository, InventoryTransactionRepository, ProjectRepository, ResourceRepository, ResourceRequirementRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 03C create

Signature: ProjectView create(Op03CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Dated departmental activity with internal planning approval. Input fields and return DTO are exactly those in05 operation03C. Permission: MEMBER owner or HEAD in own department.

1. Resolve active department and owner, require owner in department and authorized actor.
2. Validate offset schedule, duration and unique code; force DRAFT, no client state override.
3. Save and audit the planning record.

Repository calls: ProjectRepository.save, ProjectRepository.findById, ProjectRepository.findByCode, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 03L list

Signature: Page<ProjectView> list(Op03LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible projects with bounded filters. Input fields and return DTO are exactly those in05 operation03L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: ProjectRepository.findAll.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 03R get

Signature: ProjectView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one Project with its derived public fields. Input fields and return DTO are exactly those in05 operation03R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: ProjectRepository.findById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 03D delete

Signature: NoContent delete(Long id, Op03DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Hard delete only DRAFT with no requirements/allocations. Historical project uses cancel or completed archival; no cascading delete. Input fields and return DTO are exactly those in05 operation03D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: ProjectRepository.findById, ProjectRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 03U update

Signature: ProjectView update(Long id, Op03URequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Versioned update of permitted Project fields. Input fields and return DTO are exactly those in05 operation03U. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle.
2. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced.
3. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval.
4. Apply metadata change and append before/after audit atomically; return updated version.

Repository calls: ProjectRepository.findById, ProjectRepository.save, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, DUPLICATE_RECORD, DEPENDENCY_EXISTS, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### P01 submit

Signature: ProjectView submit(Long id, OpP01Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Move internal project to SUBMITTED. Input fields and return DTO are exactly those in05 operationP01. Permission: owner/HEAD.

1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state.
2. Require DRAFT, at least one active valid requirement and all schedules within project.
3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Repository calls: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, VERSION_CONFLICT, RESOURCE_OVERALLOCATION, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### P02 approve

Signature: ProjectView approve(Long id, OpP02Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Move internal project to APPROVED. Input fields and return DTO are exactly those in05 operationP02. Permission: OFFICER.

1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state.
2. Require SUBMITTED; internal planning approval does not approve university funding or allocate inventory.
3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Repository calls: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, VERSION_CONFLICT, RESOURCE_OVERALLOCATION, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### P03 returnDraft

Signature: ProjectView returnDraft(Long id, OpP03Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Move internal project to DRAFT. Input fields and return DTO are exactly those in05 operationP03. Permission: OFFICER.

1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state.
2. Require SUBMITTED, reason, and no approved allocations; draft return is an internal decision.
3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Repository calls: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, VERSION_CONFLICT, RESOURCE_OVERALLOCATION, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### P04 start

Signature: ProjectView start(Long id, OpP04Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Move internal project to IN_PROGRESS. Input fields and return DTO are exactly those in05 operationP04. Permission: owner/HEAD or OFFICER.

1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state.
2. Require APPROVED and startAt <= now < endAt.
3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Repository calls: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, VERSION_CONFLICT, RESOURCE_OVERALLOCATION, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### P05 complete

Signature: ProjectView complete(Long id, OpP05Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Move internal project to COMPLETED. Input fields and return DTO are exactly those in05 operationP05. Permission: OFFICER.

1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state.
2. Require IN_PROGRESS or APPROVED; no REQUESTED allocation, no unissued approved remainder or outstanding custody.
3. For unfulfilled demand require waiveUnfulfilled=true plus a reason; write waived quantities in audit, not stock.
4. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Repository calls: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, VERSION_CONFLICT, RESOURCE_OVERALLOCATION, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### P06 cancel

Signature: ProjectView cancel(Long id, OpP06Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Move internal project to CANCELLED. Input fields and return DTO are exactly those in05 operationP06. Permission: owner/HEAD for DRAFT/SUBMITTED; OFFICER for APPROVED/IN_PROGRESS.

1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state.
2. Require not COMPLETED/CANCELLED and reason; lock complete affected resource set in ascending order.
3. Set REQUESTED allocations CANCELLED; approved with no supply become CANCELLED, partially supplied approvals release their remaining R. Preserve reusable custody and allow return/loss afterward.
4. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Repository calls: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, INVALID_STATE, VERSION_CONFLICT, RESOURCE_OVERALLOCATION, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### P07 reschedule

Signature: ProjectView reschedule(Long id, OpP07Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Atomically replace full unissued project schedule. Input fields and return DTO are exactly those in05 operationP07. Permission: OFFICER.

1. Read affected requirements/resources; sort IDs and lock all parents before rows. Re-read complete graph and versions.
2. Require no issue/consumption ever recorded for any allocation of this project (including reversed events); reject incomplete/extra requirement-window IDs.
3. Validate new project/requirement intervals and construct proposed snapshots for every requested/approved allocation.
4. Exclude exactly the old affected commitments, then sweep all proposed replacements together with all unaffected and overdue holds; reject any deficit.
5. Save all schedules and audit old/new windows, bump resource revisions; entire command rolls back on any conflict.

Repository calls: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save, AllocationRepository.save, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; AvailabilityComponent.calculateLocked for capacity commands; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, SCHEDULE_LOCKED, VERSION_CONFLICT, RESOURCE_OVERALLOCATION, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## ReportingComponent

Responsibility: Current explainable ledger stock by resource.; Approved allocations due in a selected future window.; Outstanding reusable custody past its expected end.; Observed planned demand by resource for one department/horizon.

Dependencies: AllocationRepository, DepartmentRepository, InventoryTransactionRepository, ResourceRepository, ResourceRequirementRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### R01 inventory

Signature: InventoryReport inventory(OpR01Query query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Current explainable ledger stock by resource. Input fields and return DTO are exactly those in05 operationR01. Permission: organization report read; contact data excluded.

1. Load bounded resource page and ledger rows; reconstruct balances including reversal deltas.
2. Return each pool/unit separately and invariant/reconciliation flags; never repair automatically.

Repository calls: ResourceRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### R02 upcoming

Signature: AllocationReport upcoming(OpR02Query query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Approved allocations due in a selected future window. Input fields and return DTO are exactly those in05 operationR02. Permission: organization report read; contact data excluded.

1. Validate present/future max90-day window and permitted department filter.
2. Load approved allocations through Specification, derive unissued/outstanding and current risk; paginate by id.

Repository calls: AllocationRepository.findAll, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### R03 overdue

Signature: AllocationReport overdue(OpR03Query query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Outstanding reusable custody past its expected end. Input fields and return DTO are exactly those in05 operationR03. Permission: organization report read; contact data excluded.

1. Capture now; query approved reusable allocations with endAt <= now through Specification.
2. Derive outstanding from ledger and retain only >0; paginate after filtering so totalElements is accurate.

Repository calls: AllocationRepository.findAll, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### R04 departmentDemand

Signature: DepartmentReport departmentDemand(OpR04Query query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Observed planned demand by resource for one department/horizon. Input fields and return DTO are exactly those in05 operationR04. Permission: organization report read; contact data excluded.

1. Validate department and max90-day horizon.
2. Load eligible requirements/projects, aggregate requested/approved/supplied by resource/unit, with no mixed-unit grand total.

Repository calls: DepartmentRepository.findById, ResourceRequirementRepository.findAll, AllocationRepository.findByRequirement_Id, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## RequirementComponent

Responsibility: Demand for one resource during one project window.; List visible requirements with bounded filters.; Retrieve one ResourceRequirement with its derived public fields.; Hard delete only when no allocation or movement references. Otherwise withdrawal through update with reason, only after all operational obligations are resolved.; Versioned update of permitted ResourceRequirement fields.

Dependencies: AllocationRepository, AuditEventRepository, InventoryTransactionRepository, ResourceRepository, ResourceRequirementRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 05C create

Signature: ResourceRequirementView create(Op05CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Demand for one resource during one project window. Input fields and return DTO are exactly those in05 operation05C. Permission: project owner or HEAD in own department.

1. Read project/resource IDs, lock resource then re-read project; validate owner/head and DRAFT/SUBMITTED project.
2. Require active resource, one project-resource pair, quantity cap and interval within project.
3. Persist ACTIVE demand; if project was SUBMITTED return it to DRAFT and audit both changes.

Repository calls: ResourceRequirementRepository.save, ResourceRequirementRepository.findById, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, ResourceRequirementRepository.findByResource_IdAndStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 05L list

Signature: Page<ResourceRequirementView> list(Op05LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible requirements with bounded filters. Input fields and return DTO are exactly those in05 operation05L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: ResourceRequirementRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 05R get

Signature: ResourceRequirementView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one ResourceRequirement with its derived public fields. Input fields and return DTO are exactly those in05 operation05R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: ResourceRequirementRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 05D delete

Signature: NoContent delete(Long id, Op05DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Hard delete only when no allocation or movement references. Otherwise withdrawal through update with reason, only after all operational obligations are resolved. Input fields and return DTO are exactly those in05 operation05D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: ResourceRequirementRepository.findById, ResourceRequirementRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 05U update

Signature: ResourceRequirementView update(Long id, Op05URequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Versioned update of permitted ResourceRequirement fields. Input fields and return DTO are exactly those in05 operation05U. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle.
2. Distinguish withdrawal from demand edits: quantity/window/priority/notes edits require DRAFT/SUBMITTED project and no approved allocation or movement; WITHDRAWN is allowed after all history is operationally resolved, without deleting it. Cancel REQUESTED rows and release no stock implicitly. projectId/resourceId cannot change after creation.
3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced.
4. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval.
5. Apply metadata change and append before/after audit atomically; return updated version.

Repository calls: ResourceRequirementRepository.findById, ResourceRequirementRepository.save, AuditEventRepository.save, ResourceRepository.lockById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, DUPLICATE_RECORD, DEPENDENCY_EXISTS, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## ResourceComponent

Responsibility: Pool of interchangeable whole units under organizational control.; List visible resources with bounded filters.; Retrieve one Resource with its derived public fields.; Hard delete only with no requirements, allocations or transactions. Archive requires no pending/active unissued commitment or outstanding custody; keep ledger forever in demo.; Versioned update of permitted Resource fields.

Dependencies: AllocationRepository, AuditEventRepository, InventoryTransactionRepository, ResourceRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 04C create

Signature: ResourceView create(Op04CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Pool of interchangeable whole units under organizational control. Input fields and return DTO are exactly those in05 operation04C. Permission: assigned CUSTODIAN or OFFICER assigning an active custodian.

1. Check resource control basis and authorization note, active department and active CUSTODIAN.
2. Create metadata with zero ledger stock; reject supplied quantity, writable condition totals or stock deltas.
3. Save and audit; actual opening stock requires RECEIPT.

Repository calls: ResourceRepository.save, ResourceRepository.findById, ResourceRepository.findByCode, ResourceRepository.lockById, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 04L list

Signature: Page<ResourceView> list(Op04LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible resources with bounded filters. Input fields and return DTO are exactly those in05 operation04L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: ResourceRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 04R get

Signature: ResourceView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one Resource with its derived public fields. Input fields and return DTO are exactly those in05 operation04R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: ResourceRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 04D delete

Signature: NoContent delete(Long id, Op04DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Hard delete only with no requirements, allocations or transactions. Archive requires no pending/active unissued commitment or outstanding custody; keep ledger forever in demo. Input fields and return DTO are exactly those in05 operation04D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: ResourceRepository.findById, ResourceRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 04U update

Signature: ResourceView update(Long id, Op04URequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Versioned update of permitted Resource fields. Input fields and return DTO are exactly those in05 operation04U. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle.
2. Acquire resource lock; custodian reassignment is OFFICER only with reason, and cannot rewrite existing allocation recipients.
3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced.
4. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval.
5. Apply metadata change and append before/after audit atomically; return updated version.

Repository calls: ResourceRepository.findById, ResourceRepository.save, AuditEventRepository.save, ResourceRepository.lockById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; AvailabilityComponent.calculateLocked for capacity commands; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, DUPLICATE_RECORD, DEPENDENCY_EXISTS, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## SchedulingComponent

Responsibility: Deterministically run reminder scan once without changing clock.

Dependencies: AllocationRepository, NotificationLogRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### J01 runOnce

Signature: JobRunView runOnce(OpJ01Request input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Deterministically run reminder scan once without changing clock. Input fields and return DTO are exactly those in05 operationJ01. Permission: ADMIN/OFFICER, DEMO profile only.

1. Require DEMO profile; capture Clock, invoke NotificationComponent.scanReminders(asOf).
2. Return enqueue/skip/existing/error counts; no implicit dispatch or physical stock change.
3. LIVE authenticated deployment does not register this demonstration route.

Repository calls: AllocationRepository.findAll, NotificationLogRepository.findByEventKey, NotificationLogRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: SPLIT. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, INVALID_STATE. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## ShortageComponent

Responsibility: Explain each project requirement coverage and conditional sourcing gap.; Consolidate competing departments and peak sourcing capacity.

Dependencies: AllocationRepository, InventoryTransactionRepository, ProjectRepository, ResourceRepository, ResourceRequirementRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### Q02 projectSummary

Signature: ShortageView projectSummary(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Explain each project requirement coverage and conditional sourcing gap. Input fields and return DTO are exactly those in05 operationQ02. Permission: project read scope.

1. Load project and all active requirements in a consistent read snapshot.
2. For each resource calculate remaining demand and available candidate capacity; do not remove existing approved commitments except when measuring already-covered demand.
3. Return unallocated quantities, physical supply and risk flags separately; never sum isolated candidate gaps.

Repository calls: ProjectRepository.findById, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Id, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### Q03 consolidate

Signature: DemandView consolidate(Long id, OpQ03Query query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Consolidate competing departments and peak sourcing capacity. Input fields and return DTO are exactly those in05 operationQ03. Permission: organization read.

1. Validate bounded horizon and resource; load active requirements and eligible project states.
2. Compute unapproved N per requirement, existing K commitments and ledger capacity in one snapshot.
3. For reusable sweep K plus all N intervals together; for consumable subtract all reserved stock globally and sum selected unallocated demand.
4. Return peak units and affected segments/projects/departments with reason flags; no allocation or procurement side effect.

Repository calls: ResourceRepository.findById, ResourceRequirementRepository.findByResource_IdAndStatus, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## UserComponent

Responsibility: Member identity and access/contact preferences.; List visible users with bounded filters.; Retrieve one AppUser with its derived public fields.; Hard delete only if no operational or actor references anywhere. Otherwise deactivate using update; deny deactivation if assigned unresolved custody or sole active custodian, until reassigned. Never delete last active ADMIN.; Versioned update of permitted AppUser fields.

Dependencies: AppUserRepository, AuditEventRepository, PermissionComponent, Clock, DTO mapper and AuditComponent.

### 02C create

Signature: AppUserView create(Op02CRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Member identity and access/contact preferences. Input fields and return DTO are exactly those in05 operation02C. Permission: ADMIN.

1. Validate ADMIN, unique normalized username and allowed role; check active department unless organization-wide role.
2. Ignore no security fields: reject actor/password/consent timestamps; initial SMS consent false unless a separate self consent update is made after creation.
3. Save user and append redacted CREATE audit without phone.

Repository calls: AppUserRepository.save, AppUserRepository.findById, AppUserRepository.findByUsername, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, DUPLICATE_RECORD, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 02L list

Signature: Page<AppUserView> list(Op02LQuery query, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: List visible users with bounded filters. Input fields and return DTO are exactly those in05 operation02L. Permission: authorized organization read; contact/audit/notification scope rules apply.

1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.
2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Repository calls: AppUserRepository.findAll.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 02R get

Signature: AppUserView get(Long id, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Retrieve one AppUser with its derived public fields. Input fields and return DTO are exactly those in05 operation02R. Permission: same read scope as list.

1. Validate positive id and active actor; retrieve or404.
2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Repository calls: AppUserRepository.findById.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: READ. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 02D delete

Signature: NoContent delete(Long id, Op02DRequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Hard delete only if no operational or actor references anywhere. Otherwise deactivate using update; deny deactivation if assigned unresolved custody or sole active custodian, until reassigned. Never delete last active ADMIN. Input fields and return DTO are exactly those in05 operation02D. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

1. Resolve target and authorize; require expectedVersion for versioned rows.
2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.
3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.
4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Repository calls: AppUserRepository.findById, AppUserRepository.delete, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, DEPENDENCY_EXISTS, INVALID_STATE, VERSION_CONFLICT, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

### 02U update

Signature: AppUserView update(Long id, Op02URequest input, Actor actor). NoContent means void and maps204 only on real deletion.

Purpose/input/output: Versioned update of permitted AppUser fields. Input fields and return DTO are exactly those in05 operation02U. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle.
2. Prevent deleting/deactivating/demoting last active ADMIN. Role/department transfer is refused while user owns a nonterminal project, is a resource custodian or has unresolved allocation custody; reassign resource custody through its own officer command first.
3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced.
4. Only self or ADMIN may change contact; only self can grant consent, notice version OP_SMS_V1 and consentAt are server recorded. Role/department/active edits require ADMIN; never trust a role change from the ordinary self-contact DTO. Revocation clears consent flag/metadata and cancels queued sends at dispatch recheck.
5. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval.
6. Apply metadata change and append before/after audit atomically; return updated version.

Repository calls: AppUserRepository.findById, AppUserRepository.save, AuditEventRepository.save.

Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; DTO mapping happens before transaction closes.

Transaction: WRITE. Exceptions: BAD_REQUEST, VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, INTERNAL_ERROR, NOT_FOUND, VERSION_CONFLICT, DUPLICATE_RECORD, DEPENDENCY_EXISTS, INVALID_STATE, RETRY_REQUIRED. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure.

## Internal component methods

PermissionComponent.require(Actor actor, Action action, TargetContext target): void. No transaction starts here; participates in caller. Re-read active AppUser/department/resource role as appropriate, compare owner/department/custodian IDs, enforce non-self approval and state-specific permission, throw FORBIDDEN or UNAUTHENTICATED. Dependencies AppUserRepository.findById and target context already loaded. Action is an internal allowlisted enum of API operation IDs, not client permission expressions. ActorResolver.resolve(headers,principal): Actor runs before controller delegation, validates profile and active user; never uses body actor IDs.

AuditComponent.append(String action,String targetType,Long targetId,RedactedSnapshot before,RedactedSnapshot after,String reason,Actor actor,String requestId): AuditEventView. Participates in existing WRITE transaction, never REQUIRES_NEW. Validate action/target allowlist and sizes; generate targetSummary and server time; remove phone/secret/provider credential fields; AuditEventRepository.save; no recursion. Errors roll back the main command. Failed/refused actions go to safe operational logs with requestId, not a false committed AuditEvent. Public appendNote uses09C and its own WRITE transaction.

AvailabilityComponent.balanceLocked(Long resourceId): Balance. Require caller already holds Resource lock and write transaction; InventoryTransactionRepository.findByResource_IdOrderByIdAsc; sum exact signed vectors with overflow checks; verify reusable S/O/B nonnegative and consumable O/B zero. Group allocation net counters; validate counter equations. Balance is an immutable value, never an entity. Corrupt data raises INTERNAL_ERROR with safe diagnostics and no automatic repair.

AvailabilityComponent.calculateLocked(Long resourceId,Window window,Instant asOf,Set<Long> excludedAllocationIds,List<ProposedCommitment> replacements): AvailabilityView. Caller owns WRITE resource lock; load ResourceRepository.findById, full approved allocations with AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus and ledger via balanceLocked. Validate the internal exclusion list is only affected records owned by the command; clients cannot send exclusions. Build R/O contributions, merge replacements, sweep endpoints ending before starting; return minimum, all conflict segments, physical quantity and dependence. Throws BAD_REQUEST/NOT_FOUND/INTERNAL_ERROR. Does not approve, mutate, or call Twilio. Read query uses the same pure sweep over a consistent snapshot rather than acquiring a write lock.

ShortageComponent.remaining(ResourceRequirement requirement,List<Allocation> allocations,LedgerSnapshot ledger): RequirementCoverage. Pure function using persisted statuses/effective quantities and signed counters; no repository calls. Return D,C,N,supplied,outstanding; reject impossible coverage as corruption. Used by projectSummary and consolidate, ensuring per-request and aggregate reports share arithmetic.

InventoryTransactionComponent.applyTypedMovement(ValidatedMovement input,LockedResource context,Actor actor): InventoryTransactionView. Internal helper inside record transaction; no extra proxy/transaction assumed. Select exact effect vector in02, never client deltas. LOSS with allocation affects O/resolution, without allocation affects S; DAMAGE storage only; RETURN inspected; adjustments officer only. Check type/quantity/counters and stock before insert, bump parent version, flush, save immutable vector and audit. Convenience issue/return/consume invoke public record on the injected separate component with full typed DTO. reverse invokes the same effect writer after dependency fence and reversal checks. No nested public method annotation is relied on through self-calls.

NotificationComponent.scanReminders(Instant asOf): ScanResult. Single-process scan, no long all-rows transaction. Query AllocationRepository.findAll(Specification decisionStatus APPROVED and resource.type REUSABLE) in bounded pages, derive outstanding via InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc. If outstanding>0 and now in [endAt-24h,endAt), enqueue RETURN_DUE; if now>=endAt enqueue OVERDUE. Calls enqueueEvent in a separate transactional bean/proxy per event; catch/report one record failure without stopping remaining events. No stock mutation. Event key suppresses repeats across scans/restarts; recipient fixed allocation.recipient.

NotificationComponent.enqueueEvent(Long allocationId,NotificationKind kind,Instant asOf,Actor system): NotificationLogView. WRITE per event. Re-read current allocation/custody, consent/contact and due predicate; findByEventKey then insert unique key. On unique race roll back and re-read in a new READ transaction; do not continue a failed transaction. Save QUEUED or SKIPPED, with no provider call. CREATED/EXISTING/SKIPPED counts are explicit result values. Reversal of last return may make a reminder eligible again but an existing event key prevents a repeat for the same due instant.

NotificationComponent.claim(Long notificationId,Instant asOf): Optional<DispatchEnvelope>. Short WRITE. NotificationLogRepository.lockById; require QUEUED and nextAttemptAt<=asOf, attempts<3; re-read allocation and recipient under same transaction, recompute outstanding. If returned/cancelled/no longer due or consent absent set CANCELLED/SKIPPED, append audit and return empty. Otherwise set SENDING, increment attempt count, append STARTED attempt with timestamp, commit and return an in-memory destination/body/SID-free envelope. The full number is never persisted in log. Caller sends only after claim commits. The existing STARTED attempt entry is completed in place exactly once, while subsequent attempts append; operational attempt rows are an exception to append-only outcome completion, not editable by public API.

NotificationComponent.finish(Long notificationId,SendResult result): NotificationLogView. New WRITE. Lock log, require same SENDING attempt; map accepted SID/status, known rejection or UNKNOWN exactly as07, complete attempt outcome, clear/set nextAttemptAt, save audit. A process crash after possible send is recovered as UNKNOWN, not automatically QUEUED. If finish itself fails, durable SENDING remains recoverable; do not roll back the already committed allocation or resend inline.

NotificationComponent.dispatchDue(): void. Outside DB transaction, fetch up to20 due IDs via NotificationLogRepository.findDue(QUEUED,Clock.instant(),PageRequest), call claim per ID through a separate transactional proxy, then TwilioIntegrationComponent.sendSms, then finish. Single runner; each exception logged by request/event id and remaining IDs continue. Timeout ambiguity becomes UNKNOWN. Scheduling never sends raw resource lists or full user data.

TwilioIntegrationComponent.sendSms(String destinationE164,String body): SendResult and fetchStatus(String providerSid): ProviderStatus. No DB transaction or repository. Select FAKE/LIVE fixed configured host only; construct verified Retrofit form request, set secret Authorization header server side, execute with bounded timeouts, parse safe provider result into accepted/rejected/unknown. Do not invent provider idempotency. fetchStatus is read-only external GET. Retry decisions are made by NotificationComponent, not automatically by the HTTP client. Full contract in07.

SchedulingComponent.scan(): void fixedDelay60000, and dispatch(): void fixedDelay5000. Each takes a local AtomicBoolean guard, captures Clock, calls the NotificationComponent methods on injected proxies, releases guard in finally, logs counts/errors; no business state update merely because an interval ended. SchedulingComponent.runOnce(OpJ01Request,Actor) is the DEMO route in05, calls same scan logic once and returns counts. A separate maintenance recovery step marks stale SENDING older than120seconds UNKNOWN; no auto-resend. NotificationLogRepository.findAll with status/time Specification and lockById supplies candidates.

ReportingComponent snapshot methods inR01..R04 use a shared READ transaction and existing pure balance/coverage methods, apply scope and bounded filters, include asOf and explicit units. Avoid pagination before derived overdue filtering; compute totalElements on the filtered set within demo bounds. No saved report entity or unsupported repository method is invented.

DemoSeedBootstrap.initialize(): void, PostConstruct, DEMO profile only. Validate DEMO profile. If demo.bootstrap-admin.enabled call injected SeedComponent.bootstrapAdmin() first; if demo.seed.enabled then call injected SeedComponent.seedScenario(), both through Spring proxies. Print the returned nonsecret bootstrap user ID for the ordered Postman setup. Do not put Transactional on a self-invoked PostConstruct method and expect it to start a transaction.

SeedComponent.bootstrapAdmin(): Long. WRITE transaction, DEMO profile only, explicit ORGRESOURCE_BOOTSTRAP_ADMIN_ENABLED=true; default false. AppUserRepository.findByUsername("demo.admin") returns an existing active ADMIN id on repeat. If absent, require AppUserRepository.count(empty Specification)==0; otherwise abort rather than injecting a privileged identity into an existing dataset. Save one active ADMIN with username demo.admin, displayName Demo Administrator, department null, phone null, consent false, createdBy SYSTEM/null; append SEED audit and return generated id. All timestamps/role values are server assigned. This method never resets data or creates anonymous user-registration routes. A shared/live profile cannot enable it. DemoSeedBootstrap invokes it before optional full fixture seed; seedScenario requires this known administrator already exists.

SeedComponent.seedScenario(): SeedResult. WRITE transaction using inherited findById/save and code lookups on the nine repositories plus ordinary domain commands through components after dependency bootstrap. Create the SYSTEM/bootstrap administrator and three fictional departments/users first. Use stable codes and fixed UUID command/request keys, receipt opening stock through normal movement rules, then projects/requirements/allocations in08. Seeds use injected Clock-relative future instants. If sentinel project exists verify entire fixture shape and skip; if partial mismatch abort rather than duplicate. Never reset user data. Disable by default outside DEMO. Mock contact absent, consent false, LIVE SMS never seeded. Initial notification SKIPPED sample may be created only after issuing eligible fictional custody under a frozen test clock; no fake ACCEPTED/DELIVERED record.
