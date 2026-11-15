# CP29 — Candidate public v2 REST/DTO, roles and negative acceptance contract

**2026-11-16. DESIGN RECOMMENDATION — NOT APPROVED AND NOT IMPLEMENTED.** The 26 academic routes remain active in **separate** O14 at SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`; this is a proposed *public capability crosswalk*, NOT a drop-in REST compatibility claim.

## 1. Grounding and purpose
Source-of-truth `09_ESP_PROJECT_ASSIGNMENT.pdf` and original `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` (25 routes), plus O14 five source controllers and `backend/src/main/java/app/dto/ApiDtos.java` (26 routes with A09). The original uses Long IDs, 200/201, Philippine-local date strings, unwrapped DTO arrays and `@Autowired` Spring components; public proposals use UUID IDs, tenant membership, UTC RFC3339 timestamps, version and idempotency controls. Never silently update or internet-expose original academic `/api`.

## 2. Proposed public protocol invariants
- Base `/api/v2`; domain routes `/api/v2/orgs/{orgId}/...`, `orgId` UUID **is never authorization**. Jersey REST with Spring-injected service layer. HTTPS public, restrictive same-origin CORS.
- Browser authentication: server-side session in `Cookie`, not `Authorization`; CSRF token in an HTTP header for POSTs, including login/logout; no credentials in URL.
- Public body IDs: canonical lower-case UUID strings; DB may use BIGINT internals. Names trimmed, nonempty, max **100 characters** for Department/Project/Resource; Resource kind `REUSABLE|CONSUMABLE`; allocation/requirement quantity integer **1..1,000,000**; initial stock **0..1,000,000**.
- Public times: offset-aware RFC3339 e.g. `2026-11-20T01:00:00Z`, stored UTC `DATETIME(6)`; UI displays PHT; `endAt>startAt`, actual active interval **[startAt,endAt)**. No time-window reservation reuse in public P0.
- Public success: `{"data":<object-or-array>,"requestId":"req_..."}`; lists also `page:{"nextCursor":null|opaque,"limit":integer}`; no raw JPA serialization. Create 201, GET/update/transitions 200. Identity logout 204; no fake 202 for synchronous work.
- Public errors: `{"code":"CAPACITY_CONFLICT","message":"Not enough available stock.","requestId":"req_...","details":{...}}`. 400 syntax/missing mandatory headers, 401 missing session, 403 current actor forbidden/CSRF, 404 absent **or foreign tenant**, 409 state/capacity/duplicate/durable-ID mismatch, 412 stale `If-Match`, 422 semantic validation, 428 missing required version precondition (candidate; explicit review), 429 throttled, 503 dependency/quota, sanitized 500. No SQL/password/tokens/secret leakage.
- All public mutation POSTs require high-entropy `Idempotency-Key` with actor+route+body hash; same key/payload within retention returns prior result, differing payload 409. All versioned update/status transitions require `If-Match: "<integer-version>"` and return a new `ETag: "<integer-version>"`; stale 412; missing 428. Create without existing object omits If-Match.
- **Physical movement POSTs** (RECEIVE, ADJUST, ISSUE, RETURN, CONSUME) additionally include a *client-established, stable, random* `operationId` UUID in body. It is unique per organization and persisted with immutable movement and fingerprint beyond the 24h HTTP receipt. Repeat after 48h/restart does **not** make a new event.
- Every list needs bounded `limit=1..100` (default 20), stable opaque cursor and no cross-tenant inference. Suspended users lose even read access in that org.
- Public permissions (proposal): `OWNER` full own-tenant normal CRUD, not a bypass for two-party OWNER transfer; `ADMIN` member/Department/project governance, NOT physical stock by default; `CUSTODIAN` resource and allocation physical custody/approval; `LEAD` assigned own project/requirements/requests; `MEMBER` authorized catalog and own borrower custody. A credential can have different roles in different orgs; a membership has one role at a time. No `@ateneo.edu` role inference.

## 3. Named request DTOs (proposal)
| DTO | Fields / validation | Notes |
|---|---|---|
| CreateDepartment | `name:string[1..100]` | normalized unique name per org |
| UpdateDepartment | `name:string[1..100]` | If-Match required |
| CreateProject | `departmentId:UUID,leadMembershipId:UUID,name:string[1..100],startAt:RFC3339,endAt:RFC3339` | lead must be active org member with eligible role; time future on creation recommended |
| UpdateProject | `name,startAt,endAt,leadMembershipId?` | Department immutable; date locked with approved/completed history |
| CreateResource | `name:string[1..100],kind:enum,unitLabel:string[1..24],initialQuantity:integer[0..1e6],operationId:UUID` | Initial ledger credit must be durable; type/unit cannot change after movement |
| UpdateResource | `name:string[1..100]` | Name change only; total update uses `AdjustStock` |
| AdjustStock | `newTotal:integer[0..1e6],reason:string[10..500],operationId:UUID` | no below H+I; exact original total audit |
| ReceiveStock | `quantity:integer[1..1e6],reason:string[1..500],operationId:UUID` | append-only credit |
| CreateRequirement | `projectId:UUID,resourceId:UUID,quantity:integer[1..1e6]` | same-tenant unique project/resource |
| UpdateRequirement | `quantity:integer[1..1e6]` | cannot fall below covered quantity or largest live PENDING request |
| CreateAllocation | `requirementId:UUID,quantity:integer[1..1e6],borrowerMembershipId:UUID|null` | required for REUSABLE, optional CONSUMABLE; current active org member, no real phone |
| Issue/Return/Consume | `operationId:UUID` | no partial quantity; return all usable; require If-Match |
| CancelAllocation | `reason:string[1..300]` | applicant LEAD own request or CUSTODIAN/OWNER if policy approves; status guarded |
| DeleteEligible | no business payload | If-Match, no dependent/approved/movement records |
| Invite | `role:ADMIN|CUSTODIAN|LEAD|MEMBER,intendedUserId?:UUID,expiresAt:RFC3339` | actual delivery is manual, invite possession not verified identity |
| OwnershipTransfer | `toMembershipId:UUID` | recipient ACTIVE ADMIN, two-party accept; transfer policy open |

**Approval question:** whether `operationId` is required on resource creation where initial quantity is zero; recommendation is always required for consistent replay. The abstract public POST contract above is not a statement of the academic original request DTO.

## 4. Named response DTOs (proposal)
| DTO | Required fields |
|---|---|
| DepartmentView | `id,orgId,name,version` |
| ProjectView | `id,orgId,departmentId,leadMembershipId,name,startAt,endAt,status,version` |
| ResourceView | `id,orgId,name,kind,unitLabel,totalQuantity,availableQuantity,issuedOutstanding,version,status` |
| ResourceAvailabilityView | `resourceId,at,availableQuantity,approvedEffectiveHolds,issuedOutstanding` |
| RequirementView | `id,orgId,projectId,resourceId,quantity,coveredQuantity,shortageQuantity,version` |
| AllocationView | `id,orgId,requirementId,quantity,borrowerMembershipId,status,version,createdAt,approvedAt,issuedAt,returnedAt,consumedAt,cancelledAt,cancellationReason,expiredPendingCleanup,isEffectiveHold` (date fields nullable) |
| MovementView | `id,resourceId,allocationId|null,operationId,kind,quantity,deltaTotal,deltaIssued,occurredAt` |
| MessageView | `message` for truly successful eligible delete; `resource archive` returns ResourceView |
| SessionView | `accountId,activeMemberships[{orgId,role}],csrfToken?` (never password, hash, token, contact) |

For `AllocationView`, an APPROVED row with `endAt<=now` remains **persisted APPROVED** until a scheduler transaction; `expiredPendingCleanup=true,isEffectiveHold=false`. The same effective predicate must drive `ResourceView.availableQuantity` and `RequirementView.coveredQuantity`. No false "still reserved" label in UI.

## 5. Public domain route crosswalk — 26 academic operation IDs
**Common negative tests on every listed route:** 401 anonymous; 403 unauthorized role; 404 foreign-tenant ID/hidden resource; 400 malformed UUID/JSON; 422 invalid input; no mutation/rollback on failed transaction; 412 stale version where If-Match applies. Actual course O14 responses remain unchanged.

| Academic ID | Public method and suffix under `/api/v2/orgs/{orgId}` | Body / query; success DTO and HTTP | Allowed actor | Specific negative case / intentional delta |
|---|---|---|---|---|
| D01 | POST `/departments` | CreateDepartment → DepartmentView 201 | OWNER/ADMIN | 409 normalized duplicate |
| D02 | GET `/departments` | limit,cursor → DepartmentView[] 200 | any ACTIVE member | no foreign org rows |
| D03 | POST `/departments/{id}/update` | UpdateDepartment + If-Match → DepartmentView 200 | OWNER/ADMIN | 409 duplicate; 412 stale |
| D04 | POST `/departments/{id}/delete` | If-Match, no body → MessageView 200 | OWNER/ADMIN | 409 any project/FK/history; possible audit tombstone |
| P01 | POST `/projects` | CreateProject → ProjectView 201 | OWNER/ADMIN; LEAD for self if policy | 404 foreign Department/LEAD, 422 date; lead ownership check |
| P02 | GET `/projects` | departmentId?,limit,cursor → ProjectView[] 200 | active member, row-scope where appropriate | 404 foreign filter, no cross-org |
| P03 | POST `/projects/{id}/update` | UpdateProject + If-Match → ProjectView 200 | OWNER/ADMIN; assigned LEAD | 409 dates locked when approved/completed |
| P04 | POST `/projects/{id}/delete` | If-Match → MessageView 200 | OWNER/ADMIN; assigned LEAD if eligible | 409 requirements, allocations, audit-history dependencies |
| R01 | POST `/resources` | CreateResource → ResourceView 201 and INITIAL movement | OWNER/CUSTODIAN | 409 duplicate; no double INITIAL on retry |
| R02 | GET `/resources` | kind?,limit,cursor → ResourceView[] 200 | active member | computed T-H-I/T-H, no page leakage |
| R03 | POST `/resources/{id}/update` | UpdateResource + If-Match → ResourceView 200 | OWNER/CUSTODIAN | Type/total not arbitrarily edited; separate receive/adjust operations **public delta** |
| R04 | POST `/resources/{id}/archive` | If-Match → ResourceView(status ARCHIVED) 200 | OWNER/CUSTODIAN | 409 live allocation/custody; **archive instead of original hard delete — owner approval needed** |
| R05 | GET `/resources/{id}/availability` | no booking window → ResourceAvailabilityView 200 | active member | current global stock only; endAt predicate consistency |
| Q01 | POST `/requirements` | CreateRequirement → RequirementView 201 | OWNER/ADMIN; assigned LEAD | 409 duplicate pair; 404 foreign FK |
| Q02 | GET `/requirements` | projectId?,limit,cursor → RequirementView[] 200 | member with visible project | exact covered/shortage and effective end rule |
| Q03 | POST `/requirements/{id}/update` | UpdateRequirement+If-Match → RequirementView 200 | OWNER/ADMIN; assigned LEAD | 409 below covered or largest PENDING request |
| Q04 | POST `/requirements/{id}/delete` | If-Match → MessageView 200 | OWNER/ADMIN; assigned LEAD | 409 any Allocation history |
| A01 | POST `/allocations` | CreateAllocation → AllocationView PENDING 201 | OWNER/ADMIN; assigned LEAD | 409 project ended, invalid borrower role/tenant |
| A02 | GET `/allocations` | requirementId?,status?,limit,cursor → AllocationView[] 200 | OWNER/ADMIN/CUSTODIAN; assigned LEAD/borrower scoped | never leak arbitrary borrower's private details |
| A03 | POST `/allocations/{id}/approve` | If-Match, no payload → AllocationView APPROVED 200 | OWNER/CUSTODIAN | 409 q>available/remaining requirement; concurrency one winner |
| A04 | POST `/allocations/{id}/issue` | Issue(operationId)+If-Match → AllocationView ISSUED 200 | OWNER/CUSTODIAN | 409 wrong type, outside project window, duplicated physical event |
| A05 | POST `/allocations/{id}/return` | Return(operationId)+If-Match → AllocationView RETURNED 200 | OWNER/CUSTODIAN | 409 not issued; **whole usable batch only**, no partial body |
| A06 | POST `/allocations/{id}/consume` | Consume(operationId)+If-Match → AllocationView CONSUMED 200 | OWNER/CUSTODIAN | 409 wrong kind/end/project; exactly one T decrement |
| A07 | POST `/allocations/{id}/cancel` | CancelAllocation+If-Match → AllocationView CANCELLED 200 | OWNER/CUSTODIAN; request-owner LEAD | 409 issued/completed/historically approved policy |
| A08 | POST `/allocations/{id}/delete` | DeleteEligible+If-Match → MessageView 200 | OWNER/ADMIN; assigned requester LEAD if policy | 409 historically approved, movement/history or bound FK; audited tombstone |
| A09 | **NO public P0 endpoint** | Academic `POST /api/allocations/{id}/revoke-sms-consent` preserved in separate O14 | academic only | **Intentional public delta**; no public SMS/phone collection, requires owner course/public boundary signoff |

**Source discrepancy note:** O14 has `expectedTotalQuantity` on R03, extra zero-stock check for R04 academic hard delete, largest pending Q03 protection, historical-approved A08 refusal, and `CANCELLED/EXPIRED` as cancellation reason; do not overwrite those actual academic code behaviors.

## 6. Additional public inventory routes (NOT included in academic 26 count)
| Method/path | Body → output | Permission and key gates |
|---|---|---|
| POST `/resources/{id}/receive` | ReceiveStock+If-Match → ResourceView and MovementView, 200 | OWNER/CUSTODIAN; client operationId; no duplicate +q after 48h |
| POST `/resources/{id}/adjust` | AdjustStock+If-Match → ResourceView and MovementView, 200 | OWNER/CUSTODIAN; no T below effective holds+issued |
| GET `/resources/{id}/movements` | cursor,limit → MovementView[],200 | OWNER/CUSTODIAN only in minimal P0 |
| GET `/activity` | cursor,limit → redacted tenant activity list | member with permitted scope |
| GET `/audit` | cursor,limit → redacted audit list | OWNER/ADMIN |
| POST `/exports` | scope: TENANT_JSON|CSV → bounded export manifest/body | OWNER/ADMIN; no raw credential/session/recovery fields |
| GET `/operations/{operationId}` (under org) | operation receipt/result or 404 | actor/role and tenant bound; prevents response uncertainty leak |

**Export is NOT an automatic backup:** independent encrypted DB backup and clean restore are separately required for P-ACCEPT.

## 7. Candidate identity routes under `/api/v2` (not course routes)
| Method/path | Input/response | Guard and specific negative |
|---|---|---|
| POST `/auth/login` | username/password → SessionView; rotate session;200 | 401 generic,429 throttle,403 missing CSRF as decided |
| POST `/auth/logout` | CSRF, no body →204 | session invalid immediately and after restart |
| POST `/auth/recover` | username+one-time saved code+new password →200 | reused/expired fails; credential_version++, all prior sessions invalid |
| GET `/session` | SessionView or401 | current user+active memberships only, no disabled membership disclosure beyond policy |
| GET `/orgs` | active org membership list,200 | no tenant existence leak |
| POST `/orgs/{orgId}/invitations` | Invite→201 token once no-store | OWNER/ADMIN cannot grant OWNER |
| POST `/invitations/redeem` | token→membership 200 | authenticated user; exactly one success under concurrent redeem |
| POST `/orgs/{orgId}/invitations/{id}/revoke` | If-Match→200 | OWNER/ADMIN; used/expired cannot reopen |
| GET `/orgs/{orgId}/members` | cursor/limit→members 200 | member with approved redaction; no full email/phone |
| POST `/orgs/{orgId}/members/{id}/role` | role+If-Match→200 | role escalation and last effective OWNER denied |
| POST `/orgs/{orgId}/members/{id}/suspend` | If-Match→200 | current role checked, global account status effective |
| POST `/orgs/{orgId}/transfer-ownership` | toMembershipId→PENDING 200 | OWNER, target ACTIVE ADMIN, one pending per org |
| POST `/orgs/{orgId}/accept-transfer` | transferId+If-Match→200 | target exact ACTIVE ADMIN accepts; atomic OWNER swap; recovery variant owner approval |

No `POST /api/v2/orgs` self-service creation in proposed fictional P0.

## 8. Explicit per-route acceptance template
For **each** route above:
1. Positive: permitted user, valid JSON/headers, expected HTTP and exact DTO schema, expected DB count and audit/ledger movement count.
2. Invalid DTO: missing/extra/nullable/length/boundary values, 400 or 422 according selected grammar, unchanged DB.
3. Role: one forbidden role returns 403; outsider returns 401; foreign tenant returns 404, no timing/metadata leak where feasible.
4. Version/idempotency: duplicate identical request result stable; changed body with reused idempotency key 409; stale If-Match 412; missing If-Match 428 if chosen.
5. State and concurrency: expiry boundary, late return, competing q4 approvals T5, two scheduler workers, failure after movement insert, duplicate over 48h.
6. Browser/API: receipt-driven unknown POST outcome, no blind creation of new operationId; UI must not claim success before commit.

**No public route, DTO, permission, SQL migration, Postman/Newman or server from this document exists yet. All public test cases are NOT RUN.**
