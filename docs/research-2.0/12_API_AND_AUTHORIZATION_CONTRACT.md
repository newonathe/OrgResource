# V2 HTTP API and authorization contract
**Status:** PROVISIONAL RECOMMENDATION — NO ROUTES IMPLEMENTED. **Date:** 2026-11-15. Sources C01–C03/G05–G09 and proposed F01–F26.

## Route conventions
Base `/api/v2`, HTTPS only in any public environment. Resource identifiers are opaque UUIDs; `orgId` is tenant lookup only, never authorizes. `Authorization` = secure session-cookie model after selected auth provider verification; include anti-CSRF token on browser mutations, Secure/HttpOnly/SameSite cookie, restrictive CORS. Every POST/PATCH state-changing route requires `Idempotency-Key` for non-idempotent transitions and `If-Match: "<version>"` where stale write significant. Do **not** claim original CSCI assignment GET/POST-only contract governs this separate V2 API: V1 remains GET/POST, V2 may use standard POST/PATCH to represent semantics. `?limit=1..100&cursor=opaque` for lists; never return unbounded membership or events.

### Successful/error response grammar
```json
{"data":{"id":"b45c4ec5-5b58-45f2-91b7-b6f94278e319","status":"APPROVED","version":7},"requestId":"req_c789"}
```
```json
{"code":"CAPACITY_CONFLICT","message":"Not enough resource capacity in the requested interval.","requestId":"req_c789","details":{"requested":4,"available":1}}
```
On 401 no identity, 403 identity lacks tenant action, 404 resource not found **or not visible to the tenant**, 409 domain conflict (CAPACITY_CONFLICT, LAST_OWNER, WRONG_STATE, IDEMPOTENCY_MISMATCH, DATES_LOCKED), 412 stale `If-Match`, 422 invalid business input, 429 rate limit, 503 unavailable/free quota exhausted, 500 unexpected with generic message only. No stacktrace, provider token or raw phone/email leak. Unknown IDs return 404 without disclosing foreign tenant existence.

## Primary endpoint matrix (proposed; each endpoint needs actual implementation/contract test)
| Route | Role | Purpose, request fields | Response / negative test |
|---|---|---|---|
| `GET /session` | signed-in | own identity, active org memberships | 200 or 401 |
| `POST /orgs` | signed-in + creation policy | `name,slug?` | 201 org + OWNER, 409 slug |
| `GET /orgs` | signed-in | list only own ACTIVE memberships | 200 cursor list |
| `GET /orgs/{orgId}` | member | summary, no other tenant fields | 200/404 |
| `POST /orgs/{orgId}/invitations` | ADMIN,OWNER | invite email, role, expiry | 201 invite token once, 409/403/429 |
| `POST /invitations/redeem` | signed-in verified | token | 200 membership, single-use 409 |
| `POST /orgs/{orgId}/invitations/{id}/revoke` | ADMIN/OWNER | expected version | 200/412 |
| `GET /orgs/{orgId}/members` | member | paginated minimal public profile, no export of raw emails by default | 200/404 |
| `POST /orgs/{orgId}/members/{id}/role` | OWNER/ADMIN | role + expected version | 200, 403 forbidden grant, 409 LAST_OWNER |
| `POST /orgs/{orgId}/members/{id}/suspend` | OWNER/ADMIN | reason, version | 200/409 LAST_OWNER |
| `POST /orgs/{orgId}/transfer-ownership` | OWNER | target member id | 202 pending |
| `POST /orgs/{orgId}/accept-transfer` | target ADMIN | transfer token/reference | 200 new OWNER; never ownerless |
| `GET /orgs/{orgId}/resources` | member | type, search, cursor/limit | 200 list, 404 hidden |
| `POST /orgs/{orgId}/resources` | CUSTODIAN/ADMIN | name,kind,unitLabel,initialQty | 201, 422/409 |
| `GET /orgs/{orgId}/resources/{id}` | member | detail, availableNow, computed custody, version | 200/404 |
| `GET /orgs/{orgId}/resources/{id}/availability` | member | startAt,endAt ISO | 200 headroom + atRisk, bad window 422 |
| `POST /orgs/{orgId}/resources/{id}/receive` | CUSTODIAN | quantity,reason | 200+ledger ID, duplicate safe |
| `POST /orgs/{orgId}/resources/{id}/reconcile` | CUSTODIAN | newTotal,reason,version | 200, 409 bound,412 stale |
| `POST /orgs/{orgId}/resources/{id}/archive` | ADMIN/CUSTODIAN | version | 200/409 active obligations |
| `GET /orgs/{orgId}/projects` | member | start/end, cursor | 200 |
| `POST /orgs/{orgId}/projects` | LEAD+ | name,startAt,endAt,description? | 201,422 |
| `POST /orgs/{orgId}/projects/{id}/update` | owning LEAD/ADMIN | name,description,dates if uncommitted | 200/409 DATES_LOCKED |
| `POST /orgs/{orgId}/requirements` | LEAD+ | projectId,resourceId,quantity | 201/409 duplicate/404 |
| `POST /orgs/{orgId}/requirements/{id}/update` | owning LEAD+ | quantity,version | 200/409 under covered |
| `GET /orgs/{orgId}/allocations` | member limited | status,project,requirement,cursor | 200 redacted list |
| `POST /orgs/{orgId}/allocations` | LEAD+ | requirementId,quantity,borrowerMemberId? | 201 pending |
| `POST /orgs/{orgId}/allocations/{id}/approve` | CUSTODIAN | no body, If-Match, idempotency | 200 approved or 409 capacity |
| `POST /orgs/{orgId}/allocations/{id}/issue` | CUSTODIAN | If-Match, idempotency | 200 issued + custody event or 409 |
| `POST /orgs/{orgId}/allocations/{id}/return` | CUSTODIAN | `quantity,condition:USABLE`, If-Match | 200 partial/full, 422 unsupported damaged |
| `POST /orgs/{orgId}/allocations/{id}/consume` | CUSTODIAN | no body | 200 consumed or 409 |
| `POST /orgs/{orgId}/allocations/{id}/cancel` | LEAD owner/CUSTODIAN | reason,version | 200 cancelled /409 fulfilled |
| `GET /orgs/{orgId}/activity` | member | cursor/limit; scoped notification feed | 200 |
| `GET /orgs/{orgId}/audit` | ADMIN | cursor/limit, permitted redacted fields | 200/403 |
| `POST /orgs/{orgId}/exports` | ADMIN/OWNER | format/scope | 202 export job or bounded 200 signed file; no raw secret |
| `POST /orgs/{orgId}/close` | OWNER | reason,version | 202 CLOSING/409 issued |
| `GET /operations/{id}` | submitting principal | idempotency/operation status | 200 completed/pending; 404 outsider |

**Authz matrix:** OWNER all tenant admin; ADMIN member/invite/project except transfer final ownership or direct owner removal; CUSTODIAN resource writes/stock/approvals/fulfillment; LEAD project CRUD+requirements+requests (restrict to own projects unless permission explicitly granted); MEMBER tenant catalog and own custody; no tenant member may call `/orgs/{other}`; OPERATOR never gets tenant data by default. Tests enumerate 5 roles × mutation classes × 2 tenants × suspended/expired conditions.

## Idempotency semantics
Before execution, validate principal+tenant scope and claim `idempotency_key` unique (org, user, normalized route, hashed key). Same key+body hash returns same prior HTTP status/body; same key/different hash yields 409. On transaction conflict, no response cache that masks failed state. On network uncertainty, `GET /operations/{id}` or idempotency lookup confirms outcome. Retain receipt 24h proposed; clients must not assume safe replay after expiry—use read-back of real resource. Never replay direct payment/SMS (not in P0).

## API security and versioning
No public unrestricted `/api/v1` legacy routes; academic service remains loopback/restricted. Versioned OpenAPI must be generated from contract tests with schema `additionalProperties:false` for writes, required fields/nullable conventions and examples. Emit normalized PHT calendar display in UI but UTC API. API migration incompatible changes = `/v3` with dated deprecation; use `Cache-Control:no-store` on sensitive responses, CSP, and application limits. Provider-specific errors are not exposed as raw stack/provider payloads.

## CP2 API and identity contract correction — research-only 2026-11-15
**MC-09 correction:** The initial statement that a session cookie belongs in the `Authorization` header is not an executable HTTP contract. A browser sends a cookie in `Cookie`, with HttpOnly/Secure/SameSite policies; state-changing requests protected by Spring Security CSRF token header and refresh lifecycle. [Official CSRF/SPA reference](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html). No actual auth provider, session or middleware has been implemented.
**MC-10 absolute course continuity:** The original 25 proposed GET/POST operations plus A09 (26 existing Jersey handlers), five JPA entities, @Autowired components, Javax Validation, MySQL, Retrofit/Gson/Twilio simulator and scheduling must stay verifiably runnable in the course executable. Proposed `/api/v2` UUID IDs, 422 responses and changed return states are not backward-compatible automatically. Before any modernization, research must produce/approve a 26-row method/path/input/error/status/state mapping; explicit owner consent required for any public-runtime namespace boundary. Do not internet-expose old unauthenticated `/api` routes.
**MC-07 ownership and invitation:** `POST /orgs` may create only a private fictional unverified workspace, not an official Ateneo organization. Real verified email is NOT available by assumption; synthetic one-time invitations must not masquerade as actual verified ownership. ADMIN cannot grant OWNER, remove the last OWNER or self-escalate; owner transfer, revoke, concurrent demotion and lost owner recovery remain underspecified.
**MC-12 tenant operation receipts:** `GET /operations/{id}`, exported documents/downloads, event/activity/audit and invite redemption must verify bound tenant, principal and current role. Cross-org and disabled-member results must conceal existence. Irreversible physical actions need durable operation uniqueness beyond HTTP receipt retention.
**Return semantics:** Academic original returns entire batch. V2 partial returns only after explicit scope approval with EERD and test update. All public V2 routes NOT IMPLEMENTED, no endpoint test executed here.

## Exact missing compatibility contract — 2026-11-15 research
The canonical course source [O14 route matrix](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/evidence/O14/ROUTE_ACCEPTANCE_MATRIX.md) plus attached 12-page concise proposal establishes **26** live routes. The explicit [30-row-by-row study](30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md) finds: 10 original routes have no direct V2 endpoint, 15 have materially changed DTO/path/permission/state semantics and R04 became soft archive, not delete. Original `Department` and departmentId-filtered projects are absent; V2 time-window availability is not original global reservation logic; A09 revoke-sms-consent is absent. **No direct drop-in V2 contract established.** V2 remains research-only. Before approving rewrite, create explicit user decision preserving 26 original real GET/POST routes, errors/Long identifiers/whole-batch states and source, with a separate secured public compatibility boundary or signed intentional differences. Do not internet-expose old unauthenticated routes.
