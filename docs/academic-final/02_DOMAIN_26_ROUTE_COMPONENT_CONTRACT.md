# OrgResource — Five-entity domain, all 26 original operations, Component contracts

> **FINAL TWO-EDITION OVERRIDE (CP44–CP46, 2026-11-16):** This academic canonical document originated in already-uploaded **draft PR #9**, and is consolidated here without merging that PR. Read [Final consolidated handoff](../research-2.0/77_CP46_FINAL_GITHUB_HANDOFF_RELEASE_INDEX.md), [A-first / B-extended decision](../research-2.0/74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md), and the [single active Codex master prompt](../research-2.0/75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt). **A** is a local strict course version; **B** is separate enhanced C descended from frozen A. Any prior wording implying one final release, modernizing A before course acceptance, or production testing before A completion is **historical/superseded**. A image testing is safe local single-organization; B must enforce authorization/tenant isolation. No application tests or course submission were performed by this document copy.


**Version:** A-CANON-1.0, 2026-11-16. **Status:** O14 SOURCE-VERIFIED contract; final secured one-product runtime not implemented.  
**Direct source:** O14 SHA 2dd336ed39cecd417a8bce3388b303e688e95dee, backend/src/main/java/app/entities, dto/ApiDtos.java, five rest/controllers, five components, repositories, app/config/ApiExceptionMapper.java, and O14 ROUTE_ACCEPTANCE_MATRIX.md. **Proposal differences** are explicitly distinguished. All URLs have prefix /api from JerseyConfig.

## 1. Domain model and exact stored fields

The original course entity types have generated positive Long IDs; relationships are lazy JPA FKs and JSON responds with IDs rather than recursive entity graphs. Tables: department, project, resource, resource_requirement, allocation; currently InnoDB. Do not replace these with 18 unnamed generic tables.

| Table / purpose | Fields in actual O14 JPA source | Constraints, relationships and data rules |
|---|---|---|
| **department** / team responsible for a project | id Long PK; name String(100) NOT NULL; name_key String(100) NOT NULL | uk_department_name_key; indexed name_key. Component normalizes case/trim key, rejects duplicate normalized name. |
| **project** / activity and resource-usage period | id Long PK; department_id Long FK NOT NULL; name String(100) NOT NULL; start_at LocalDateTime NOT NULL; end_at LocalDateTime NOT NULL | fk_project_department; endAt>startAt. Created with startAt in future; name-only update may be allowed after history; approved-history locks date changes. Philippine wall-time in O14. |
| **resource** / interchangeable equipment/supply pool | id Long PK; name String(100); name_key String(100); type ResourceType(REUSABLE/CONSUMABLE); total_quantity Integer | All non-null, normalized unique name_key, type immutable after create; 0..1,000,000 total. Total means owned usable pool for reusable, remaining physical stock for consumable. availableQuantity is **DERIVED**, not stored. |
| **resource_requirement** / unique project resource demand | id Long PK; project_id Long FK; resource_id Long FK; quantity Integer | All non-null; unique(project_id,resource_id), indexed FKs; quantity 1..1,000,000. allocatedQuantity and shortageQuantity are **DERIVED**; do not add persistent duplicate totals. |
| **allocation** / request, custody and lifecycle | id Long PK; requirement_id Long FK; quantity Integer; borrower_name String(100); phone_number String(20) nullable; sms_consent Boolean; status enum; created_at LocalDateTime; approved_at, issued_at, returned_at, consumed_at, cancelled_at nullable; cancellation_reason nullable; reminder_claimed_at nullable; reminder_outcome enum; reminder_http_status Integer nullable; provider_message_sid String(64) nullable | Quantity 1..1,000,000; borrower label nonblank; phone private; consent defaults false; status initial PENDING; createdAt mandatory; history protected. Public read DTO **never contains raw phoneNumber**. |
 
**Extra O14 enums (not extra JPA entities):** ResourceType={REUSABLE,CONSUMABLE}; AllocationStatus={PENDING,APPROVED,ISSUED,RETURNED,CONSUMED,CANCELLED}; CancellationReason={USER_REQUEST,EXPIRED}; ReminderOutcome={NONE,CLAIMED,SUPPRESSED,SIMULATED,PROVIDER_ACCEPTED,REJECTED,UNKNOWN}. Do not call cancellation reason a seventh allocation status.

**Original proposal vs actual:** P12 lists Allocation status/reminderAttempted only, and P5 sample JSON included phoneNumber. O14 adds actual timestamps/reminder outcome and redacts phoneNumber. P8 originally described an approved hold until action; O14 excludes expired APPROVED holds from stock before worker cleanup; record this deliberately rather than falsifying proposal provenance.

## 2. Exact current API request/response DTOs

- DepartmentView: {id,name}; Create/UpdateDepartment: {name}.
- ProjectView: {id,departmentId,name,startAt,endAt}; CreateProject: {departmentId,name,startAt,endAt}; UpdateProject: {name,startAt,endAt}; no departmentId update.
- ResourceView: {id,name,type,totalQuantity,availableQuantity}; CreateResource: {name,type,totalQuantity}; UpdateResource: {name,totalQuantity,expectedTotalQuantity}. **expectedTotalQuantity** is an O3 stale-write amendment, absent from original proposal, and must not be silently removed.
- ResourceAvailabilityView: {resourceId,availableQuantity}.
- RequirementView: {id,projectId,resourceId,quantity,allocatedQuantity,shortageQuantity}; CreateRequirement: {projectId,resourceId,quantity}; UpdateRequirement: {quantity}.
- AllocationView: {id,requirementId,quantity,borrowerName,contactPresent,smsConsent,status,createdAt,approvedAt,issuedAt,returnedAt,consumedAt,cancelledAt,cancellationReason,expiredPendingCleanup,reminderAttempted,reminderOutcome}. No raw phoneNumber. CreateAllocation: {requirementId,quantity,borrowerName,phoneNumber?,smsConsent?}; optional phone must match E.164 when consent true; submitted false consent discards phone in stored row.
- MessageView: {message:"Record deleted."}; ErrorView: {code,message}.
- O14 uses Philippine local datetime JSON formatted YYYY-MM-DDTHH:mm:ss. A future authenticated v2 UUID/UTC interface is a **separate reviewed product contract**; do not accidentally change original numeric-ID JSON without documented versioning.

## 3. Complete operation-to-Component mapping (25 proposal + A09)

**HTTP convention:** create 201; list/get, updates, state actions and eligible delete 200; typed JSON on success. Body shown only for JSON POSTs; actions with no body have no new unapproved JSON requirement. All controllers are actual Jersey @Path + Spring @Component, using @Autowired injected Component and RequestValidation for create/update DTO.

| ID | HTTP method and path | Input / output | Controller → Component; decisive domain rule |
|---|---|---|---|
| **D01** | POST /api/departments | {name} → DepartmentView 201 | DepartmentController.create → DepartmentComponent.create; trim/normalize, case-insensitive unique |
| **D02** | GET /api/departments | none → DepartmentView[] 200 | DepartmentController.list → DepartmentComponent.list |
| **D03** | POST /api/departments/{id}/update | {name} → DepartmentView 200 | DepartmentController.update → DepartmentComponent.update; duplicate conflict |
| **D04** | POST /api/departments/{id}/delete | no body → MessageView 200 | DepartmentController.delete → DepartmentComponent.delete; 409 if projects exist |
| **P01** | POST /api/projects | {departmentId,name,startAt,endAt} → ProjectView 201 | ProjectController.create → ProjectComponent.create; referenced department, end after future start |
| **P02** | GET /api/projects | optional ?departmentId → ProjectView[] 200 | ProjectController.list → ProjectComponent.list; filtered by department |
| **P03** | POST /api/projects/{id}/update | {name,startAt,endAt} → ProjectView 200 | ProjectController.update → ProjectComponent.update; dates locked after approved history, name-only change allowed |
| **P04** | POST /api/projects/{id}/delete | no body → MessageView 200 | ProjectController.delete → ProjectComponent.delete; cannot delete with requirements |
| **R01** | POST /api/resources | {name,type,totalQuantity} → ResourceView 201 | ResourceController.create → ResourceComponent.create; unique normalized name; immutable type |
| **R02** | GET /api/resources | optional ?type=REUSABLE/CONSUMABLE → ResourceView[] 200 | ResourceController.list → ResourceComponent.list; per-resource H/I availability aggregate |
| **R03** | POST /api/resources/{id}/update | {name,totalQuantity,expectedTotalQuantity} → ResourceView 200 | ResourceController.update → ResourceComponent.update; locked stale compare-and-set and commitment floor |
| **R04** | POST /api/resources/{id}/delete | no body → MessageView 200 | ResourceController.delete → ResourceComponent.delete; no requirement, totalQuantity=0 (future ledger must protect any history) |
| **R05** | GET /api/resources/{id}/availability | id → ResourceAvailabilityView 200 | ResourceController.availability → ResourceComponent.getAvailability; recomputed actual stock |
| **Q01** | POST /api/requirements | {projectId,resourceId,quantity} → RequirementView 201 | RequirementController.create → RequirementComponent.create; project not ended, unique pair |
| **Q02** | GET /api/requirements | optional ?projectId → RequirementView[] 200 | RequirementController.list → RequirementComponent.list; coverage and shortage derived |
| **Q03** | POST /api/requirements/{id}/update | {quantity} → RequirementView 200 | RequirementController.update → RequirementComponent.update; minimum covered and max pending; project not ended |
| **Q04** | POST /api/requirements/{id}/delete | no body → MessageView 200 | RequirementController.delete → RequirementComponent.delete; protected when any Allocation exists |
| **A01** | POST /api/allocations | {requirementId,quantity,borrowerName,phoneNumber?,smsConsent?} → redacted AllocationView 201 | AllocationController.create → AllocationComponent.create; quantity<=requirement, parent project not ended, state PENDING, no stock hold |
| **A02** | GET /api/allocations | optional ?requirementId and ?status → AllocationView[] 200 | AllocationController.list → AllocationComponent.list; filters combine; phone never returned |
| **A03** | POST /api/allocations/{id}/approve | no body → AllocationView 200 | AllocationController.approve → AllocationComponent.approve; PENDING, endAt>now, remaining demand and physical availability; APPROVED hold |
| **A04** | POST /api/allocations/{id}/issue | no body → AllocationView 200 | AllocationController.issue → AllocationComponent.issue; APPROVED REUSABLE, within active project, no second stock deduction |
| **A05** | POST /api/allocations/{id}/return | no body → AllocationView 200 | AllocationController.returnItems → AllocationComponent.returnItems; ISSUED REUSABLE, all usable items confirmed, late return permitted |
| **A06** | POST /api/allocations/{id}/consume | no body → AllocationView 200 | AllocationController.consume → AllocationComponent.consume; APPROVED CONSUMABLE during active project; deduct physical T once atomically |
| **A07** | POST /api/allocations/{id}/cancel | no body → AllocationView 200 | AllocationController.cancel → AllocationComponent.cancel; only PENDING/APPROVED; reason USER_REQUEST if before end, EXPIRED if ended |
| **A08** | POST /api/allocations/{id}/delete | no body → MessageView 200 | AllocationController.delete → AllocationComponent.delete; only never-approved PENDING/CANCELLED with approvedAt null |
| **A09 (amendment)** | POST /api/allocations/{id}/revoke-sms-consent | no body → AllocationView 200 | AllocationController.revokeSmsConsent → AllocationComponent.revokeSmsConsent; idempotently phoneNumber=null, smsConsent=false, no stock effect |

**Route count:** D4 + P4 + R5 + Q4 + A9 = **26**; GET 6 and POST 20. The proposal has 25 (A09 added after). Never claim A09 appears in the original 12-page proposal.

## 4. Shared validation and HTTP failure contract

- 400 VALIDATION_FAILED: invalid JSON/parameters/positive ID, blank/overlong names, invalid enum, quantity out of bounds, invalid period, missing consent phone, malformed URL parameter.
- 404 NOT_FOUND: actual absent entity ID or route; do not treat an ID from another organization as a successful lookup in future secure product.
- 409 DUPLICATE_NAME, DUPLICATE_REQUIREMENT, DEPENDENCY_EXISTS, RESOURCE_NOT_EMPTY, STALE_RESOURCE_QUANTITY, QUANTITY_BELOW_COMMITMENTS, PROJECT_DATES_LOCKED, PROJECT_ENDED, REQUIREMENT_ALREADY_COVERED, INSUFFICIENT_STOCK, RESOURCE_TYPE_MISMATCH, INVALID_ALLOCATION_STATE, OUTSIDE_PROJECT_PERIOD, HISTORY_PROTECTED and comparable documented domain conflicts.
- 503 RETRY_REQUIRED: recognized MySQL lock contention/deadlock/timeout; the caller must refresh/reconcile, **not blindly replay ambiguous POST**.
- 500 INTERNAL_ERROR: sanitized unexpected failure. Do not leak SQL, credentials, stack trace or private contacts into public JSON.
- Actual O14 error response is {code,message}; original proposal's example only contained {message}; preserve the source-documented amendment. Future secure v2 may add 401/403/412/422/429 and CSRF errors separately; exact final schema is a reviewed v2 contract, not retroactive O14 behavior.

## 5. Inventory math, statuses, time and edge behavior

Let T = persisted Resource.totalQuantity; H = sum Allocation.quantity where status APPROVED and Project.endAt > one sampled now; I = sum reusable ISSUED quantity regardless of overdue; C = sum quantity of ISSUED, RETURNED, CONSUMED and effective APPROVED for a given Requirement. Then:

- Available REUSABLE = T − H − I.
- Available CONSUMABLE = T − H.
- Requirement allocatedQuantity (O14 API field) = C; shortageQuantity = max(0, requirement.quantity − C).
- PENDING, CANCELLED do not hold stock or cover demand.
- APPROVED immediately reserves and covers while effective; can be approved before Project.startAt when endAt > now.
- ISSUE: APPROVED REUSABLE during startAt <= now < endAt, H→I, T unchanged and available remains stable.
- RETURN: ISSUED REUSABLE → RETURNED after full usable batch actually returned (even after Project end); I decreases, available increases, historical C unchanged.
- CONSUME: APPROVED CONSUMABLE during active project → CONSUMED, T decreases exactly quantity; H decreases by same amount, so available initially remains unchanged; C remains covered.
- CANCEL: PENDING/APPROVED → CANCELLED; no physical T change; release effective hold and remove pending/approved coverage.
- EXPIRE: worker cancels open PENDING/APPROVED when Project.endAt<=now; ISSUED remains in custody; effective hold is already zero at boundary before cleanup.
- A08 delete never-approved history only. A09 consent clearing never alters inventory.

**Reusable concrete oracle:** initial T10, requirement Q6, approved batch4 => available6, covered4; issue => available6, covered4; return => available10, covered4. Repeated return must 409, no physical mutation.  
**Consumable oracle:** initial T100, requirement Q40, approved batch30 => available70, covered30; consume => T70, available70, covered30; repeated consume 409 with no second deduction.  
**Flaky-boundary oracle:** T10, effective APPROVED q3 + ISSUED q2 + approved q4 whose project ends exactly now => available **5**, not 8. This exact case was once flaky under MySQL CI and must be explicitly retested; root cause unproven.

**Timezone:** O14 uses a Clock in Asia/Manila and LocalDateTime without offset. Future public API may use UTC instants; convert explicitly at boundary and test DST/zone serialization, no phantom date shifts. Half-open active project [startAt,endAt). Reminder due window [now,now+24h), with exact upper bound excluded.

## 6. Actual Component responsibility and lock/transaction requirements

| Component / scheduler | Contract / important steps |
|---|---|
| DepartmentComponent | normalize/unique name, save/list/rename/delete only without Project references |
| ProjectComponent | lock/recheck Department and Project, validate date ordering, refuse date changes after historically approved Allocation, keep name-only permitted, delete only without requirements |
| ResourceComponent | preserve type, normalize name, perform expectedTotalQuantity compare-and-set, query effective H/I, refuse T below commitments, read-only list uses one clock sample and grouped JPQL, delete eligible zero stock/unreferenced |
| RequirementComponent | enforce same project/resource pair uniqueness, project period, quantity >= effective coverage and largest still-PENDING request; refuse deletion when Allocation history exists |
| AllocationComponent | lock Project then Resource then Requirement then Allocation in consistent order; recheck state/parent freshness, stock/demand capacity, atomically commit lifecycle and stock; preserve approvedAt history; protect against duplicate consume/return |
| ExpiryScheduler | actual @Scheduled hourly configurable bounded ID scan; per-Allocation transactional recheck and EXPIRED cancellation; never auto-return issued |
| ReminderComponent / Scheduler / Dispatcher | actual @Scheduled bounded ISSUED reusable due scan; consent/E.164/never-attempted guard; claim commit, eligibility recheck, external HTTP **outside** DB lock/transaction, outcome commit, stale claim UNKNOWN without auto-replay |
| FakeTwilioTransport | Retrofit/Gson loopback server; SIMULATED != real provider; disabled default |
| **Future required TwilioRestTransport** | official Twilio Messages REST form transport with injected credentials, timeout/errors and no live send without authorization; new code NOT in O14 |
| **Future required ResourceImage component** | bounded authenticated upload/store/serve, content sniff, tenant isolation and no SVG; new code NOT in O14 |

**Transaction correctness:** update/approval/read of commitments must operate against consistent actual MySQL state under locks/isolation; if a repository is tenant-global, DO NOT reuse unchanged in multi-org product. Rejected changes leave persisted state unchanged; tests must check after refresh, not only response.

**Source reconciliation warning:** The initial proposal's statement “approved remains reserved until action” conflicts with current O14 effective-time query. The canonical current contract preserves O14 immediate expiration of an APPROVED hold at Project.endAt, with worker cleanup eventually setting CANCELLED/EXPIRED. Document as amendment, not retroactive proposal text.

## 7. Final product security/extension compatibility boundary

**This section is NOT a new course requirement**. The user wants the final course-submitted product to support actual secure multi-org use:

- Every original /api handler must be executed in an authenticated, server-chosen active organization; path IDs and repo queries must be org-scoped; 401/403/404 boundaries and CSRF/rate limit tested. Five original semantics, method shapes and names remain available.
- Public /api/v2 APIs may use UUID, UTC, membership/role, ETag, idempotency keys, protected histories and safe stock ledger, but must call the SAME business components/transactions and MySQL data as original /api. No second synchronized fake product.
- Name uniqueness becomes per-org where justified, with composite tenant FKs and physical movement guards. Never infer school identity from @ateneo.edu address.
- Source-derived O14 fields resource.type and allocation.borrower_name must not be silently misnamed in migrations. See research CP41 actual SQL crosswalk.
- Add justified infrastructure models while preserving five original entity families; assignment says minimum five, not forever exactly five. Do not reduce course CRUD to screenshots.
- No public Java8/Boot2.1 exposure. Supported runtime with literal javax + Jakarta coexistence remains a measured compatibility gate, NOT already proven.
