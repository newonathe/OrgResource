# O14 route-by-route acceptance matrix

**Scope:** current academic baseline of five JPA entities, 25 operations from the concise proposal, plus A09 as a separate amendment. The assignment requires at least five justified entities; five is not a maximum for a later reviewed production product. A route is not marked PASS because an annotation exists: its controller forwarding, request/response shape, and executed HTTP or MySQL acceptance evidence are listed below.

## Source and evidence shorthand

- **API contract:** [`docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md`](../../docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md)
- **O1:** `postman/OrgResource-O1-Skeleton.postman_collection.json`
- **O3:** `postman/OrgResource-O3-CRUD.postman_collection.json`
- **D3:** `postman/OrgResource-O3-Department-Update.postman_collection.json`
- **O4/O5:** `postman/OrgResource-O4-O5-Allocation-Expiry.postman_collection.json`
- **O14-DI:** `postman/OrgResource-O14-Dependency-Integrity.postman_collection.json`
- Collection request names and assertions below refer to the actual collection files. The executed request/assertion totals are recorded in [`FINAL_ACCEPTANCE_REPORT.md`](FINAL_ACCEPTANCE_REPORT.md).

## Original 25 operations

| ID | HTTP operation | Controller → Spring component | Request | Successful response | Executed HTTP acceptance evidence |
|---|---|---|---|---|---|
| D01 | `POST /api/departments` | `DepartmentController.create` → `DepartmentComponent.create` | `CreateDepartment {name}` | `DepartmentView`, 201 | O1 “Create Department”; O3 “01 Create Department fixture” assert 201, positive generated ID and persistence. |
| D02 | `GET /api/departments` | `DepartmentController.list` → `DepartmentComponent.list` | none | `List<DepartmentView>`, 200 | O1 “List Departments”; O14-DI verifies fixture presence after rejected deletion and absence after successful deletion. |
| D03 | `POST /api/departments/{id}/update` | `DepartmentController.update` → `DepartmentComponent.update` | `UpdateDepartment {name}` | `DepartmentView`, 200 | D3 rename/normalization, case-only rename, duplicate 409, missing 404, blank name 400, and invalid ID tests. |
| D04 | `POST /api/departments/{id}/delete` | `DepartmentController.delete` → `DepartmentComponent.delete` | none | `MessageView {message}`, 200 | O1 safe unreferenced deletion; O14-DI referenced Department 409 + GET unchanged, then eligible deletion 200 + GET absent. |
| P01 | `POST /api/projects` | `ProjectController.create` → `ProjectComponent.create` | `CreateProject {departmentId,name,startAt,endAt}` | `ProjectView`, 201 | O1 “Create Project”; O3 “02 Create future Project fixture” assert 201 and Department link. |
| P02 | `GET /api/projects?departmentId={id}` | `ProjectController.list` → `ProjectComponent.list` | optional `departmentId` | `List<ProjectView>`, 200 | O1 list; O3 “06 P02 confirms Project persisted”; O14-DI filtered list verifies Project remains after 409 and is absent after delete. |
| P03 | `POST /api/projects/{id}/update` | `ProjectController.update` → `ProjectComponent.update` | `UpdateProject {name,startAt,endAt}` | `ProjectView`, 200 | O3 “05 P03 updates Project” and “06 P02 confirms Project persisted”; `ProjectComponentTest` covers historical name-only edit, date rules, approval lock, invalid/missing ID and unchanged failure. |
| P04 | `POST /api/projects/{id}/delete` | `ProjectController.delete` → `ProjectComponent.delete` | none | `MessageView {message}`, 200 | O3 “07 P04 protects referenced Project” 409 and “16 P04 deletes unreferenced Project” 200; O14-DI repeats 409 + GET unchanged and eligible 200 + GET absent. |
| R01 | `POST /api/resources` | `ResourceController.create` → `ResourceComponent.create` | `CreateResource {name,type,totalQuantity}` | `ResourceView`, 201 | O1 create Resource; O3 “03 Create Resource fixture” assert 201 and fictional stock/type. No `unit` field is required. |
| R02 | `GET /api/resources?type={type}` | `ResourceController.list` → `ResourceComponent.list` | optional `type` | `List<ResourceView>`, 200 | O1 filtered list/availability; O3 “10 R02 confirms stale request changed nothing”; O14-DI confirms Resource persists after blocked deletion and is absent after eligible deletion. |
| R03 | `POST /api/resources/{id}/update` | `ResourceController.update` → `ResourceComponent.update` | `UpdateResource {name,totalQuantity,expectedTotalQuantity}` | `ResourceView`, 200 | O3 “08 R03 updates Resource” and “09 R03 rejects stale expected total” 409; “10 R02” confirms stale request did not mutate. `ResourceComponentTest` covers duplicate, invalid, commitment floor, type immutability and unchanged failure. |
| R04 | `POST /api/resources/{id}/delete` | `ResourceController.delete` → `ResourceComponent.delete` | none | `MessageView {message}`, 200 | O3 “11 R04 protects referenced Resource”, “17 R04 protects positive stock”, and “19 R04 deletes eligible Resource”; O14-DI confirms referenced zero-stock Resource returns 409, survives, then deletes after its Requirement is removed. |
| R05 | `GET /api/resources/{id}/availability` | `ResourceController.availability` → `ResourceComponent.getAvailability` | positive resource ID | `ResourceAvailabilityView {resourceId,availableQuantity}`, 200 | O1 “Get Resource Availability” checks the exact two-field response; O4/O5 “Reusable stock is available after return” and “Verify consumable quantity and availability” check lifecycle math. |
| Q01 | `POST /api/requirements` | `RequirementController.create` → `RequirementComponent.create` | `CreateRequirement {projectId,resourceId,quantity}` | `RequirementView`, 201 | O1 create Requirement; O3 “04 Create Requirement fixture” asserts parent IDs and shortage. |
| Q02 | `GET /api/requirements?projectId={id}` | `RequirementController.list` → `RequirementComponent.list` | optional `projectId` | `List<RequirementView>`, 200 | O1 list; O3 “15 Q02 confirms Requirement absent”; O14-DI verifies presence after rejected delete and absence after eligible delete. |
| Q03 | `POST /api/requirements/{id}/update` | `RequirementController.update` → `RequirementComponent.update` | `UpdateRequirement {quantity}` | `RequirementView`, 200 | O3 “12 Q03 updates Requirement” checks coverage/shortage; “13 Q03 rejects invalid quantity” checks 400. `RequirementComponentTest` covers coverage, largest pending request, ended Project, missing/invalid IDs and unchanged failures. |
| Q04 | `POST /api/requirements/{id}/delete` | `RequirementController.delete` → `RequirementComponent.delete` | none | `MessageView {message}`, 200 | O3 “14 Q04 deletes Requirement” and “15 Q02 confirms Requirement absent”; O14-DI verifies dependent Allocation produces 409 without mutation, then deletes after A08. |
| A01 | `POST /api/allocations` | `AllocationController.create` → `AllocationComponent.create` | `CreateAllocation {requirementId,quantity,borrowerName,phoneNumber?,smsConsent}` | redacted `AllocationView`, 201 | O1 “Create Allocation” checks PENDING and phone redaction; O4/O5 creates reusable, consumable and never-approved pending fixtures; O14-DI creates a contact-free fixture. |
| A02 | `GET /api/allocations?requirementId={id}&status={status}` | `AllocationController.list` → `AllocationComponent.list` | optional filters | `List<AllocationView>`, 200 | O1 “List Allocations” checks persisted, redacted row; O4/O5 checks redaction/consent; O14-DI verifies A08 removal. |
| A03 | `POST /api/allocations/{id}/approve` | `AllocationController.approve` → `AllocationComponent.approve` | none | `AllocationView`, 200 | O4/O5 “Approve reusable Allocation” and “Approve consumable Allocation” check status; real-MySQL lifecycle/concurrency tests verify reservation and no over-allocation. |
| A04 | `POST /api/allocations/{id}/issue` | `AllocationController.issue` → `AllocationComponent.issue` | none | `AllocationView`, 200 | O4/O5 “Issue reusable Allocation” succeeds; repeat/type/active-window failure behavior is checked by lifecycle tests. |
| A05 | `POST /api/allocations/{id}/return` | `AllocationController.returnItems` → `AllocationComponent.returnItems` | none | `AllocationView`, 200 | O4/O5 “Return reusable Allocation” and “Repeat return is refused”; MySQL lifecycle tests verify custody release and historical coverage. |
| A06 | `POST /api/allocations/{id}/consume` | `AllocationController.consume` → `AllocationComponent.consume` | none | `AllocationView`, 200 | O4/O5 “Consume consumable Allocation”, “Repeat consume is refused”, and post-consumption total/availability check; MySQL test verifies transaction rollback. |
| A07 | `POST /api/allocations/{id}/cancel` | `AllocationController.cancel` → `AllocationComponent.cancel` | none | `AllocationView`, 200 | O4/O5 “Cancel pending Allocation”; lifecycle/expiry MySQL tests check status, reason, and races with cancel/consume. |
| A08 | `POST /api/allocations/{id}/delete` | `AllocationController.delete` → `AllocationComponent.delete` | none | `MessageView {message}`, 200 | O4/O5 deletes never-approved cancelled Allocation and rejects returned/consumed history (409); O14-DI deletes its never-approved Allocation before removing the Requirement. |

## Separately tracked A09 amendment

| ID | HTTP operation | Controller → component | Request | Response | Executed acceptance evidence |
|---|---|---|---|---|---|
| A09 | `POST /api/allocations/{id}/revoke-sms-consent` | `AllocationController.revokeSmsConsent` → `AllocationComponent.revokeSmsConsent` | none | redacted `AllocationView`, 200 | O4/O5 “Revoke SMS consent”, repeated revocation idempotency, and GET redaction check. The route changes contact/consent only; it is not part of the proposal’s original 25. |

## Shared behavior and evidence limits

- All controller methods are `@Component` Jersey resources with `@Autowired` Spring component forwarding. Request DTO methods call `RequestValidation`; lifecycle/delete methods validate domain state in components.
- Create responses are 201; successful list/update/lifecycle/delete responses are 200. Controlled validation/malformed path errors are JSON 400, missing records JSON 404, and known domain/dependency conflicts JSON 409. Exact body codes are defined in the API contract.
- Failed-update persistence checks are in O3 Postman; refused dependent deletes and unchanged rows are in O14-DI; allocation rollback, concurrency, database locking, time boundaries and restart persistence are in the MySQL test evidence.
- The executable collections are v2.1. `OrgResource-Current-26-Routes.postman_collection.json` is a reference and is not run as one ordered sequence. The old nine-entity `OrgResource-Demo`/`OrgResource-Negative` collections are historical and are excluded.
- No route provides image upload or retrieval. This is the separate **UNRESOLVED** CLO9 question; it is not hidden as a passing route.
