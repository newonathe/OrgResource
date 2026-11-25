# C8 Identity and Governance — Source-Reviewed Route Policy Oracle (20/93)

**Scope/date:** 2026-11-23, Asia/Manila. **Status: SOURCE-REVIEWED / INDEPENDENT REVIEW AND ROUTE-LEVEL ACCEPTANCE OPEN.**

This is a pre-execution policy oracle derived from the checked-in `C1SecurityConfiguration`, `C1AccountSessionFilter`, `C1OrganizationContext`, four Jersey resources (`C1AuthResource`, `C1SessionResource`, `C1InvitationResource`, `C1GovernanceResource`) and `C1GovernanceService`. It covers exactly **20 of 93** annotated Jersey operations. It is **not** independent acceptance, and historical test-method names are candidate evidence links only until an authorized second reviewer checks actual receipts, fixtures, status and database postconditions.

**Rules common to all:** Spring Security allows only the explicit public routes below; every other `/api/**` requires a current authenticated principal. Non-public writes use same-origin/CSRF protection. The account/session filter checks active user, matching credential version and absolute session lifetime. For organization-scoped governance, the path organization is authoritative (rather than an unrelated selected workspace); an active account and membership are required. Governance mutations require a non-frozen organization. No source review may be substituted for a real HTTP/SQL test.

## Route-by-route policy oracle

| HTTP route | Actor / scope / distinct preconditions | Mandatory observable checks | Candidate existing acceptance tests (verify receipts) |
| --- | --- | --- | --- |
| GET `/api/v2/auth/csrf` | Public; obtains pre-auth or current-session CSRF token | 200 envelope, session-bound token, no private records | ID05 |
| POST `/api/v2/auth/login` | Public, valid login body, normalized username, rate limit, real credential | 200 on success with explicit org selection later; generic 401 AUTH_INVALID; 429 on throttling; session fixation defense | ID05, ID07, C1FIX01 |
| POST `/api/v2/auth/recover` | Public; username + one-time code + replacement password; throttle | Generic 200 whether valid or invalid; valid code one-use, account credential/session revocation; no identity enumeration | ID08, C7RecoveryWithoutCredential |
| POST `/api/v2/auth/logout` | Authenticated current session and valid CSRF | 204, session revoked; stale cookie cannot regain access | ID05, C1FIX01 |
| GET `/api/v2/session` | Active authenticated account | 200 scoped session; no password, recovery token or private data | ID05, ID06 |
| GET `/api/v2/orgs` | Active authenticated account | 200 active memberships only; no unrelated org disclosure | ID06, C1FIX01 |
| POST `/api/v2/session/active-org` | Authenticated; active membership in requested org; CSRF | 200 for authorized selection; 404 for revoked/foreign org; session and CSRF rotation; selected org not assumed at login | ID06, C1FIX01, C3_SESSION01 |
| GET `/api/v2/account` | Authenticated account identity (does not require selected org) | 200 current account only; no password hash or recovery codes | ID16 |
| POST `/api/v2/account/password` | Authenticated user, current password, replacement password, CSRF | 200 after hash replacement; older sessions revoked; wrong current password/refusal leaves credential unchanged | ID08, ID13; map actual password-change case |
| POST `/api/v2/account/recovery-codes/rotate` | Authenticated user, current password, CSRF | 200 new one-time codes; old batch invalidated; no raw codes in logs | ID08; confirm rotation-specific receipt |
| POST `/api/v2/account/disable` | Authenticated user, current password, CSRF; last-owner protection | 204, invalidated account/sessions; cannot orphan organization owner | ID12; verify account-disable-specific receipt |
| POST `/api/v2/invitations/redeem` | Public bearer token, or signed-in existing account; 24-hour invitation bounds and throttling | 201 new account (one-time recovery codes) or 200 existing; invalid/replayed/revoked token refused; no cross-org grant | ID09 |
| GET `/api/v2/orgs/{orgId}/members` | Active path-org OWNER or ADMIN | 200 bounded membership page; nonmember 404; other roles 403; page cursor scoped | ID10, C8ScopeUsesPathMembership |
| POST `/api/v2/orgs/{orgId}/invitations` | Active path-org OWNER may invite non-OWNER; ADMIN may invite only MEMBER/LEAD/CUSTODIAN; CSRF | 201 token returned only to authorized caller, 403 low-role/forbidden grants; check invitation/audit insertion | ID09, ID10 |
| POST `/api/v2/orgs/{orgId}/invitations/{invitationId}/revoke` | OWNER/ADMIN with grant over role, existing pending invitation, strong matching If-Match, CSRF | 200 updated row and ETag; 428 missing version, 412 stale, 409 expired/revoked; denial leaves invitation unchanged | ID09, ID10 |
| POST `/api/v2/orgs/{orgId}/members/{memberId}/role` | OWNER can alter other non-owner roles; ADMIN may alter lower roles only. No self-change, owner assignment or owner-target mutation. Valid If-Match. | 200 and ETag on authorized change; 403 forbidden; 428/412 version errors; Lead removal clears assignments atomically | ID10, C7GovernanceChecksPermissionBeforeTargetVersion, C8ProjectLeadAssignment |
| POST `/api/v2/orgs/{orgId}/members/{memberId}/suspend` | OWNER/ADMIN only; no self/OWNER; ADMIN cannot suspend ADMIN; active target; strong matching If-Match | 200 and ETag, session/membership revocation effect, Lead assignment cleanup; denials leave role/status intact | ID10, ID12, C8ScopeUsesPathMembership |
| POST `/api/v2/orgs/{orgId}/transfer-ownership` | Current path-org OWNER, verified current password; active ADMIN recipient; no live pending transfer; CSRF | 201 pending transfer with bounded expiry; 401 incorrect password, 403 wrong actor; no unintended owner role mutation yet | ID11, ID12 |
| POST `/api/v2/orgs/{orgId}/accept-transfer` | Only designated active ADMIN recipient; pending transfer, valid If-Match, still-valid OWNER sender; CSRF | 200 atomic reciprocal role swap and transfer acceptance; 404 outsider, 428/412 bad version; exactly one OWNER afterwards | ID11, ID12 |
| POST `/api/v2/orgs/{orgId}/transfers/{transferId}/revoke` | Initiating current OWNER only, pending transfer, valid If-Match, CSRF | 200 revoked with ETag, same ownership; 403 other actors, 428/412, 409 expired/closed; no role mutation | ID11 |

## Source-backed role decision details

- **Invitation grant:** `C1GovernanceService.requireGrant` refuses OWNER as a grant target; OWNER may grant any non-owner role, ADMIN may grant non-OWNER/non-ADMIN roles.
- **Membership administration:** no self role-change/suspension, no ordinary OWNER role assignment, ADMIN cannot act on ADMIN targets.
- **Ownership transfer:** recipient must currently be an active ADMIN; acceptance by that recipient performs the role swap inside one transaction.
- **Versioned operations:** governance `requireVersion` accepts a strong quoted `"vN"` If-Match, with 428 missing, 400 malformed and 412 stale. For real refusal tests, pass a *valid existing target* before evaluating permission order.
- **Non-disclosure:** absent/inaccessible organization membership is 404, a valid membership with insufficient role is 403 where service logic so specifies. Do not guess exact behavior of a different delegate without an execution receipt.

## Mandatory evidence still to collect

- Exact final SHA and actual test receipts for **each** operation, not only a method name in an old test suite.
- Protected-route 401 anonymous and non-mutation check: the proposed C8AnonymousGate test covers **88** of **93** registered endpoints; report it as PASS only after the exact-SHA isolated MySQL job executes successfully. Public routes require their own positive/privacy checks.
- Positive and meaningful role/foreign-org/target/version denials, including SQL before/after for each high-risk write. Cross-tenant existing target, suspended member, demotion, different browser session and replay are separate scenarios.
- Independent reviewer confirmation of the role oracle and receipt mapping. No separate reviewer has accepted this oracle.
- Restricted IR4 evidence, which is absent from this chat, must be inspected by an authorized reviewer privately.

**Classification:** VERIFIED SOURCE FACT (specific delegate/annotation content), DESIGN TEST RECOMMENDATION (new route gate), OPEN QUESTION (per-operation independent acceptance), USER DECISION (fictional data and restricted publishing). No production or live-demo readiness claim.
