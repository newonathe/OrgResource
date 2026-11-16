# CP35 — Complete user journeys and public API contract candidate for ONE OrgResource

> **2026-11-16 SECURITY/PROVIDER correction:** [CP40](67_CP40_PROVIDER_TRIAL_SECURITY_AND_DEADLINE_REALITY_CHECK.md) removes the ungrounded `intendedUserId` invite field from fictional P0, specifies the bearer-token limitation, and updates SMS status: the current Twilio free trial does **not** establish capability to send personalized reminder bodies. No real Twilio dispatch is proven; use disabled/in-app or FakeTwilio simulated until account-specific and instructor gates are reviewed.


**Dated:** 2026-11-16 PHT. **Classification:** DESIGN RECOMMENDATION, research-document contract; **NO routes or UI implemented by this document**. Derived from [CP31](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md), [CP32](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md), [CP33](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md), [CP34 schema](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md), the original 12-page proposal, actual pinned O14 controller/DTO source, and CP29 [52](52_CP29_PUBLIC_ROUTE_DTO_ROLE_ERROR_CONTRACT_CANDIDATE.md). Current Canvas, supported runtime and provider constraints remain external gates.

## 1. Product boundaries and route authority

**ONE PRODUCT:** one same-origin browser UI, one database, one user login/session, one deployment line. **Two coherent HTTP surfaces, not two final applications:**
- `/api`: **26 genuine protected classroom/Jersey handlers** with original request JSON/Long ID/Philippine-local timestamps/unwrapped response and original component meanings, secured by server session + explicitly selected active organization. Authentication/tenant scoping are documented security extensions; preserve observed course behavior wherever compatible with immutable history.
- `/api/v2`: one secure versioned public JSON API with UUID IDs, UTC RFC3339 times, envelope, ETag/version, per-domain idempotency, RBAC and tenant-safe queries. Routes are listed below. No old anonymous `/api` exposed publicly.
- Original five public-source entity classes keep true JPA persistence and `@Autowired` controller→Component. Additional identity/audit tables are justified operational infrastructure, not fabricated institutional entities.

**Every old operation is separately indexed as ARC-D01..ARC-A09** in [CP33 §2](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md); no repeat transcription of 26 response DTOs replaces that route-by-route authoritative source. `A09` actually clears consent/contact and repeated execution succeeds in O14 source; keep it active.

## 2. HTTP syntax, standard errors and replay semantics

### 2.1 Request common rules

- Production HTTPS same origin, JSON UTF-8 for data, `Cache-Control:no-store` for auth and personal responses. Restrictive CORS, origin/referrer policy, `X-Content-Type-Options:nosniff`.
- Anonymous `GET /api/v2/auth/csrf` creates limited pre-auth cookie/CSRF pair, returns `{data:{csrfToken},requestId}`. Session cookie `HttpOnly;Secure;SameSite=Lax` in production. Browser keeps CSRF in memory, no password, recovery token or cookie value in local storage.
- All state-mutating public POST (including auth) requires the current CSRF and same-origin check; 403 on missing/mismatch. Domain POST also requires `Idempotency-Key` (random >=128-bit, receipt 24h), but auth/credential/redeem flows do **not** require generic business idempotency receipts. A lost authentication response must be recovered via `GET /session` instead of blind repeated credential side effects.
- Existing-resource public mutations require strong `If-Match: "<version>"` and return new ETag. Missing 428, stale 412. Creates omit If-Match. Identical committed domain replay with matching Idempotency-Key can return earlier receipt before stale-version denial **after current principal/role is re-authorized**; mismatched body with same key =409.
- New physical movements (INITIAL, RECEIVE, ADJUST, ISSUE, RETURN, CONSUME) additionally have stable UUID `operationId` persisted indefinitely, `UNIQUE(org_id,operationId)`. No automatic new operationId on UI timeout. Same operation and fingerprint -> no second mutation; different ->409.
- Public IDs lowercase UUID, old protected `/api` internal IDs numeric `Long`. The DB IDs are globally unique internal keys; a foreign org's numeric ID guessed or supplied must return a tenant-safe refusal. Never claim that two distinct tenant rows literally share the same globally unique DB PK.
- Timestamp public inputs must contain explicit offset, saved UTC. Invalid DST-style ambiguous local PHT interpretation is not assumed; old API's naive times always interpret in `Asia/Manila`.
- JSON schema disallows unknown writable fields (e.g., caller sets status/total/role/tenant), null where forbidden, floating inventory quantities, wrong-case enums, duplicate JSON keys and malformed UUID. Use bounded body and list sizes. For reasons/notes trim and validate codepoint length before storing.

### 2.2 Response formats

**Public success:** `{"data":T,"requestId":"req_<opaque>","page"?:{"nextCursor":null|"<opaque>","limit":20}}`. Public list data is array. Every mutable object has public `id,orgId,version`; no JPA internals, session secrets, raw phone or other tenant records. 201 create, 200 GET/normal state mutation, 204 logout. Typed GET includes ETag. Do not use `202` for synchronous committed stock.

**Public error:** `{"code":"SOME_STABLE_CODE","message":"Safe user-facing explanation.","requestId":"req_<opaque>","details"?:{"fields":[{"field":"quantity","reason":"OUT_OF_RANGE"}]}}`; no SQL, stack trace, provider credentials, raw invite token, phone or foreign-tenant existence. 400 malformed header/JSON/UUID; 401 anonymous; 403 forbidden/CSRF; 404 hidden foreign or missing object; 409 business state/duplicate/replay mismatch; 412 stale If-Match; 422 input semantics; 428 missing If-Match; 429 throttle; 503 unavailable dependency; 500 generic internal. The original `/api` uses its existing `{code,message}` error DTO and original source status codes where possible.

**Stable example error codes:** `AUTH_REQUIRED,CSRF_INVALID,ORG_CONTEXT_REQUIRED,FORBIDDEN,NOT_FOUND,VALIDATION_FAILED,DUPLICATE_NAME,DUPLICATE_REQUIREMENT,PROJECT_ENDED,OUTSIDE_PROJECT_PERIOD,INVALID_ALLOCATION_STATE,INSUFFICIENT_STOCK,REQUIREMENT_ALREADY_COVERED,QUANTITY_BELOW_COMMITMENTS,HISTORY_PROTECTED,STALE_VERSION,PRECONDITION_REQUIRED,IDEMPOTENCY_CONFLICT,LAST_OWNER,INVITE_INVALID,RESOURCE_ARCHIVED,NO_STOCK_CHANGE,DEPENDENCY_UNAVAILABLE`. Full per-route error mapping below specifies at least a positive and a representative negative.

### 2.3 Public DTOs and nullability

| DTO | Required fields on create/update, unless ? | Public view includes / derived properties |
|---|---|---|
| Department | create `name:trimmed[1..100]`; update name | id,orgId,name,version |
| Project | create `departmentId UUID,name[1..100],startAt RFC3339,endAt RFC3339,leadMembershipId? UUID`; edit name,startAt,endAt,leadMembershipId? | id,orgId,departmentId,leadMembershipId?,name,startAt,endAt,**timePhase** UPCOMING/IN_PROGRESS/ENDED,version |
| Resource | create `name[1..100],kind REUSABLE|CONSUMABLE,unitLabel[1..24],initialQuantity:int[0..1e6],operationId UUID`; edit name only; `kind` serializes to O14 SQL `resource.type`, not a new `resource.kind` column | id,orgId,name,kind,unitLabel,totalQuantity,availableQuantity,approvedEffectiveHolds,issuedOutstanding,status ACTIVE/ARCHIVED,version |
| Requirement | create `projectId UUID,resourceId UUID,quantity:int[1..1e6]`; edit quantity | id,orgId,projectId,resourceId,quantity,coveredQuantity,shortageQuantity,version |
| Allocation | create `requirementId UUID,quantity:int[1..1e6],borrowerMembershipId UUID` for reusable; optional for consumable. **Server-derived** `borrowerNameSnapshot` from the authorized same-org borrower's current fictional display label; when no consumable borrower is specified use current authenticated requester's display label. | id,orgId,requirementId,quantity,borrowerMembershipId?,borrowerNameSnapshot,status,created/approved/issued/returned/consumed/cancelledAt?,cancellationReason?,expiredPendingCleanup,isEffectiveHold,version |
| Inventory action | Receive `quantity:int[1..1e6],reason[1..500],operationId UUID`; Adjust `newTotal:int[0..1e6],reason[10..500],operationId UUID`; Issue/Return/Consume `operationId UUID` | ResourceView + movement receipt (kind,deltaTotal,deltaIssued,at,operationId) |
| Invitation | role ADMIN/CUSTODIAN/LEAD/MEMBER, expiresAt <=24h from creation; **no intendedUserId in P0** | invitation id,orgId,role,status; 256-bit secret token shown only **once** to inviter, stored hashed |
| Ownership transfer | initiate `toMembershipId UUID`; accept `transferId UUID` | transfer id,status,from/to membership,createdAt,acceptedAt?,version |

**Persistence vs transport compatibility:** modern v2's `ResourceView.kind` maps to SQL `resource.type`; `AllocationView.borrowerNameSnapshot` maps to SQL `allocation.borrower_name`. Original protected academic JSON remains `type` and `borrowerName`; its PHT local timestamps are converted explicitly. The previous CP34 aliases `resource.kind` and `borrower_name_snapshot` were physical-name errors, corrected by the actual O14 [five-entity source-column crosswalk](contracts/O14_TO_UNIFIED_FIVE_ENTITY_COLUMN_MAPPING_CANDIDATE.json). Reusing unchanged O14 Java `javax.persistence` annotations in modern Hibernate is **not assumed**.

**Read-only derived values:** `H` counts effective APPROVED until exclusive project end, `I` issued reusable outstanding, `covered` counts effective APPROVED + ISSUED/RETURNED/CONSUMED, `available=T-H-I` reusable or `T-H` consumable. Project can be UPCOMING and permit APPROVE; ISSUE/CONSUME require IN_PROGRESS, RETURN allowed after end. No persisted Project.status. All quantities whole-unit checked arithmetic.

## 3. Full public v2 route manifest (candidate)

All paths start `/api/v2`. Under `/orgs/{orgId}`, active membership/org status must be checked from server session and DB; org ID in URL is not an authorization credential.

### 3.1 Account/session/identity

| Method / path | Input / output | Authorized actor and exact guard |
|---|---|---|
| GET `/auth/csrf` | no body → token + pre-auth session | anonymous; no-store, bounded pre-auth cookie |
| POST `/auth/login` | username,password → SessionView | pre-auth CSRF; generic 401; rotate session/CSRF |
| POST `/auth/logout` | empty →204 | authenticated CSRF, invalidates server session |
| POST `/auth/recover` | username,oneTimeCode,newPassword → safe confirmation | pre-auth CSRF, one-use, credential_version++, invalidate all sessions, no account enumeration |
| GET `/session` | SessionView: accountId,displayName, activeOrgId?, memberships[] with roles | authenticated; never hashes/phone/tokens |
| GET `/orgs` | OrgSummary[] of active memberships only | authenticated; no public organization registry |
| POST `/session/active-org` | orgId UUID → SessionView | authenticated, ACTIVE membership, rotate/revalidate context, CSRF |
| POST `/invitations/redeem` | anonymous: token,username,newPassword,displayName; authenticated: token | 256-bit bearer token, hashed+single-use and <=24h, no intendedUserId; new account 201 plus one-time initial recovery codes shown **only once** to the newly registered account and secure login; existing account join 200 and no new recovery codes; no identity verification claim |
| GET `/account` | AccountView | authenticated; account-only safe fields |
| POST `/account/password` | currentPassword,newPassword →200 | authenticated recent/reauth + CSRF; credential_version++ and all sessions except rotated current invalidated |
| POST `/account/recovery-codes/rotate` | currentPassword → one-time codes once | authenticated recent/reauth, old codes revoked; no raw secret logs |
| POST `/account/disable` | currentPassword →200 + logout | if user is effective last owner anywhere, 409 LAST_OWNER; account status DISABLED and sessions invalidated atomically |

**Offline bootstrap (not HTTP):** idempotent local command provisions fictional Organization + one OWNER on empty schema only. Never leave Internet `POST /orgs` or operator content-impersonation backdoor. It must be separately logged and stopped after initial successful bootstrap.

### 3.2 Membership and ownership

| Method/path under `/orgs/{orgId}` | Input/output | Permission and principal negative |
|---|---|---|
| GET `/members` | limit/cursor → minimally redacted MemberView[] | OWNER/ADMIN; org-scoped |
| GET `/borrowers` | limit/cursor, optional bounded search and projectId (required for LEAD) → ACTIVE same-org BorrowerOption[] **{membershipId,displayName} only** | OWNER/ADMIN/CUSTODIAN or LEAD assigned to specified same-org Project; no passwords/phone/roles/recovery codes; allows reusable allocation without ADMIN member-list privilege |
| POST `/invitations` | Invite DTO →201, one-time secret | OWNER can invite all non-owner roles; ADMIN can invite CUSTODIAN/LEAD/MEMBER, not ADMIN/OWNER |
| POST `/invitations/{id}/revoke` | If-Match → 200 invitation state | OWNER/ADMIN with grant authority; used/expired cannot reissue |
| POST `/members/{id}/role` | role + If-Match → MemberView | OWNER changes non-OWNER; ADMIN cannot grant ADMIN/OWNER or modify OWNER |
| POST `/members/{id}/suspend` | If-Match → MemberView | OWNER/ADMIN non-OWNER; cannot orphan last OWNER; current sessions lose org access |
| POST `/transfer-ownership` | toMembershipId → pending transfer | ONLY current effective OWNER, recipient ACTIVE ADMIN, one pending/org, reauth recent |
| POST `/accept-transfer` | transferId + If-Match → accepted transfer | EXACT target ACTIVE ADMIN, still ACTIVE owner, atomic OWNER→ADMIN swap, no owner gap |
| POST `/transfers/{id}/revoke` | If-Match → REVOKED transfer | initiating OWNER, same org, only PENDING; expired also cannot be revived |

**Transfer lifecycle:** pending transfer may be explicitly revoked by initiating OWNER using `POST /transfers/{id}/revoke` and automatically marked EXPIRED after `expiresAt` by a bounded scheduler/transaction. Reissue uses a new transfer after previous terminal state; no duplicate ACTIVE owners during transfer. **No unsupported functionality inferred:** public self-serve organization creation, official Ateneo title validation, verified email, impersonation, permanent org close and automatic sole-owner replacement are explicitly deferred.

### 3.3 Original five domain families with modern data shapes

| Method/path under `/orgs/{orgId}` | Body / result | Allowed roles | Negative |
|---|---|---|---|
| POST `/departments` | CreateDepartment →201 | OWNER/ADMIN | duplicate name 409 |
| GET `/departments` | limit,cursor → DepartmentView[] | ACTIVE member | foreign rows absent |
| GET `/departments/{id}` | → DepartmentView | ACTIVE member | foreign ID 404 |
| POST `/departments/{id}/update` | UpdateDepartment+If-Match →200 | OWNER/ADMIN | stale 412 |
| POST `/departments/{id}/delete` | If-Match → deletion receipt | OWNER/ADMIN | referenced Project 409 |
| POST `/projects` | CreateProject →201 | OWNER/ADMIN only | foreign Department 404; invalid dates 422 |
| GET `/projects`, GET `/projects/{id}` | departmentId?,limit,cursor →ProjectView(s) | ACTIVE member; role row scope | foreign 404 |
| POST `/projects/{id}/update` | UpdateProject+If-Match →200 | OWNER/ADMIN, assigned LEAD | changed dates after approval 409 |
| POST `/projects/{id}/delete` | If-Match → deletion receipt | OWNER/ADMIN, assigned LEAD | any Requirements 409 |
| POST `/resources` | CreateResource+operationId →201 | OWNER/CUSTODIAN | duplicate/invalid type 409/422 |
| GET `/resources`, GET `/resources/{id}` | kind?,status?,limit,cursor → ResourceView(s) | ACTIVE member | foreign 404 |
| POST `/resources/{id}/update` | name+If-Match →200 | OWNER/CUSTODIAN | stale 412 |
| POST `/resources/{id}/delete` | If-Match → physical delete receipt | OWNER/CUSTODIAN | any movement/requirements/positive stock 409 |
| POST `/resources/{id}/archive` | If-Match →status ARCHIVED | OWNER/CUSTODIAN | T>0 or H>0 or I>0 =>409 |
| POST `/resources/{id}/unarchive` | If-Match →status ACTIVE | OWNER/CUSTODIAN | name key conflict or org frozen =>409 |
| GET `/resources/{id}/availability` | → availability,holds,issued sampled at | ACTIVE member | foreign 404 |
| POST `/resources/{id}/receive` | quantity,reason,operationId+If-Match →200 + MovementReceipt | OWNER/CUSTODIAN | replay fingerprint mismatch 409 |
| POST `/resources/{id}/adjust` | newTotal,reason,operationId+If-Match →200 + MovementReceipt | OWNER/CUSTODIAN | below commitments 409; same total 422 |
| GET `/resources/{id}/movements` | cursor,limit →MovementView[] | OWNER/CUSTODIAN | no cross-org |
| POST `/requirements` | CreateRequirement →201 | OWNER/ADMIN, assigned LEAD | duplicate pair 409 |
| GET `/requirements`, GET `/requirements/{id}` | projectId?,limit,cursor →RequirementView(s) | visible Project actor | foreign 404 |
| POST `/requirements/{id}/update` | quantity+If-Match→200 | OWNER/ADMIN, assigned LEAD | below covered/largest pending 409 |
| POST `/requirements/{id}/delete` | If-Match→delete receipt | OWNER/ADMIN, assigned LEAD | any allocation 409 |
| POST `/allocations` | CreateAllocation→201 PENDING | OWNER/ADMIN, assigned LEAD | invalid/ended Project 409 |
| GET `/allocations`, GET `/allocations/{id}` | requirementId?,status?,cursor,limit→AllocationView(s) | OWNER/ADMIN/CUSTODIAN, LEAD own assigned project, MEMBER own borrow only | foreign 404 |
| POST `/allocations/{id}/approve` | If-Match, empty business body→APPROVED | OWNER/CUSTODIAN | q>available/shortage 409 |
| POST `/allocations/{id}/issue` | operationId+If-Match→ISSUED | OWNER/CUSTODIAN | wrong kind/outside interval 409 |
| POST `/allocations/{id}/return` | operationId+If-Match→RETURNED | OWNER/CUSTODIAN | not ISSUED/partial 409 |
| POST `/allocations/{id}/consume` | operationId+If-Match→CONSUMED | OWNER/CUSTODIAN | wrong kind/outside interval 409 |
| POST `/allocations/{id}/cancel` | reason+If-Match→CANCELLED | OWNER/CUSTODIAN; assigned LEAD own request | ISSUED/RETURNED/CONSUMED 409 |
| POST `/allocations/{id}/delete` | If-Match→delete receipt | OWNER/ADMIN; assigned LEAD only if originator | any approved history 409 |
| POST `/allocations/{id}/revoke-sms-consent` | If-Match→updated allocation redacted | OWNER/CUSTODIAN; borrower with verified binding for own | clears encrypted contact; same Idempotency-Key retry returns receipt after auth, new op with stale ETag 412, new op with current ETag on already revoked is 200 no-op without version increment; legacy A09 repeated 200 remains |

**Design issue now resolved:** CP29 public A09 absence is superseded; A09 exists in actual O14, so a functional protected route is mandatory and a public equivalent is included.

### 3.4 Supporting data and image endpoints

| Route | Response/limits | Role and constraints |
|---|---|---|
| GET `/orgs/{orgId}/activity` | bounded tenant activity feed, redacted | ACTIVE member with row scope |
| GET `/orgs/{orgId}/audit` | bounded audit metadata | OWNER/ADMIN |
| POST `/orgs/{orgId}/exports` | format CSV or TENANT_JSON, bounded synchronous artifact/stream | OWNER/ADMIN, redacted/excludes credentials/session/tokens/phone |
| GET `/orgs/{orgId}/operations/{operationId}` | **durable physical stock receipt only** | authenticated original actor or OWNER/CUSTODIAN, same org; not every expired generic HTTP command |
| POST `/orgs/{orgId}/resources/{id}/image` | multipart `file` JPEG/PNG/WebP only, <=2 MiB, bounded dimensions →ImageView | OWNER/CUSTODIAN, CSRF+If-Match+Idempotency-Key; replace one image atomically |
| GET `/orgs/{orgId}/resources/{id}/image` | actual image bytes with correct MIME, private cache/nosniff | ACTIVE member allowed resource visibility; no public hotlink |
| POST `/orgs/{orgId}/resources/{id}/image/delete` | If-Match →200 delete receipt | OWNER/CUSTODIAN; no image -> 404, org-scope |
  
**Image is a conservative planning route for syllabus CLO9, NOT a retroactive original O14 operation or a confirmed instructor requirement.** Resource image POST/delete `If-Match` refers to **Resource.version**, and successful change increments Resource.version atomically with image.version, preventing an ambiguous resource-versus-image ETag. **Capacity candidate:** maximum 2MiB bytes/file, at most one image per Resource and 20MiB total current image bytes per fictional organization; check quota under organization lock. Do not claim quota is a law or a measured hosting limit. Do not name original image controller as present; it isn't in O14. Prevent path traversal, upload bombs, HTML/SVG and external URL fetching. Avoid personal images in seed.

**GET details:** the new v2 ID-detail endpoints are **additional public routes** needed by a connected UI; O14's original list-only GET handlers remain unchanged and are not falsely counted as separate academic routes.

## 4. Complete browser screen and interaction contract

The eight original O14 screens are source precedent: Overview, Departments, Projects, Resources, Requirements, Allocations, Reminders, About. Final ONE product adds security/operations experiences without another public application.

| UI section/route (suggested HashRouter or framework-safe equivalent) | Primary visible job | Actual real API required |
|---|---|---|
| `/login` | CSRF setup, login, recovery flow, no verification claims | auth/csrf,auth/login,auth/recover |
| `/invite` | Redeem manually delivered bearer token, new/existing account paths; on new account show generated recovery codes once and require explicit save-offline acknowledgement, never log/copy to local storage | invitations/redeem; no token in URL logs or analytics |
| `/workspace` | Select ACTIVE organization; clear org context on logout | GET orgs, POST active-org, GET session |
| `/overview` | Visible accurate stock, effective holds/issued and upcoming project cards | resources/requirements/projects activeOrg-scoped |
| `/departments` | Browse/add/edit/eligible delete with refusal explanation | Departments CRUD |
| `/projects` | Future/active/ended projects, date validation, assigned lead | Projects CRUD, timePhase derived |
| `/resources` | Stock table, availability, receive/adjust, movement timeline, image | resources detail,receive,adjust,movements,image |
| `/requirements` | Project demands, shortage and linked requested quantities | Requirements CRUD |
| `/allocations` | Request→approve→issue/return/consume/cancel; reusable borrower selector from minimal ACTIVE member options (not admin-only MemberView), due reminders | Allocations CRUD, GET borrowers, status transitions, operations |
| `/members` | OWNER/ADMIN invitation and role governance, explicit locks | members,invites,role,suspend,transfer |
| `/account` | Password/recovery/disable, active sessions, fictional profile | account,session,credential commands |
| `/activity` | Who changed stock/history without exposing secrets | activity,audit,stock movements |
| `/exports` | Download scoped fictional records, separate backup explanation | exports |
| `/reminders` | Read consent/attempt outcome, SIMULATED vs PROVIDER_ACCEPTED vs UNKNOWN; no false delivered | allocations view, scheduler state |
| `/about` | Independent fictional demo disclaimer, course provenance, version/build info | static UI, no official Ateneo logo/endorsement |

**UX behavior invariant:** all mutations show pending spinner/disable duplicate submit, never optimistic invent stock, show HTTP/domain refusal, stale 412 reload/review, 429/503 graceful, unknown response query stable operationId before retry. On org switch, cancel/clear old org lists/detail state and reauthorize all API calls; never cache cross-tenant data in local storage. On logout or disable, clear all sensitive UI state. Non-owner accounts must never see working admin controls. Every core flow works with keyboard, reduced motion, 360/768/1440 widths, high contrast, light/dark where chosen and mobile touch.

**Required demo narratives (synthetic only):** (1) secure first owner/organization and invite; (2) Department→Project→Resource→Requirement→PENDING→APPROVED→ISSUED→RETURNED; (3) consumable CONSUMED once; (4) T5 q4/q4 conflict; (5) expiry end instant; (6) two org isolation; (7) member governance and secure recovery; (8) actual image HTTP and bounded reminder simulator; (9) restart and separate clean restore, not just export.

## 5. Explicit exclusions, non-answers and decision precedence

Excluded from P0: time-overlap future slot booking, partial/damaged/lost returns, campus SSO and official OSA/CFMO integrations, unrestricted self-registration, real verified officer status, accounting/procurement/payments, browser offline writes, live SMS as core dependency, permanent FREE 24x7 claim, public multi-tenant signup, partial warehouse transfers, unbounded media library.

This contract is precise enough to **prepare** typed OpenAPI/DTOs and stepwise Codex prompts, but those files are **NOT BUILT**. It does not prove Spring Boot4/Jersey4 + literal javax + native JPA lifecycle, OCI capacity, actual Twilio account, safe independent restore, Cypress/Playwright results or course instructor sign-off.

**Caution on one role:** each Membership has one role; OWNER is the deliberate course demo “all functions” actor. LEAD is project-scoped; ADMIN cannot physically approve/issue stock unless separately granted CUSTODIAN role, which is **not supported simultaneously** in this minimal model. If an organization requires combined ADMIN+CUSTODIAN roles, a multi-role scheme is a future explicitly authorized design change; do not silently grant all privileges.

## 6. Acceptance linkage and outstanding owner/external gates

- Original API and static Postman count → [CP33 ARC-D01..ARC-A09](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md), actual O14 five controllers at pinned SHA.
- Stock/ledger/tenant DB → [CP34 SCHEMA-01..16](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md).
- Security/identity/UI/hosting → [CP33 SEC/DB/UI/OPS](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md).
- Runtime compatibility UC01..12, current Canvas/CLO9 and live Twilio mode, hosting/account quotas and student actual peer/video/COA/receipt remain **NOT RUN/UNKNOWN**. Not closed by writing a route.
- **Read precedence:** current user goal and source course > CP30 latest one product > CP32 exact conflict adjudication > CP34 physical schema > this CP35 public API/UI > CP33 QA checks > CP31 consolidated descriptions > historical CP29/older V2. Where this document expands the v2 surface or sets policy, those are recommended new decisions pending owner review, not secretly retroactive agreements.

**Verdict:** one coherent public route, identity and user-workflow **candidate** exists. No production route/UI/test was implemented in this research-only action.
