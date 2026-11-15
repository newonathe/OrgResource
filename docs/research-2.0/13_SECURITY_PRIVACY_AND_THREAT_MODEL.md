# V2 security, Philippine privacy and abuse threat model
**Status:** PROVISIONAL RECOMMENDATION; **no pen test, privacy sign-off or deployment**. **Date:** 2026-11-15. Sources E02–E05, G06–G10.

## Trust boundaries
Browser ↔ HTTPS reverse proxy ↔ authenticated app/API ↔ tenant-authorized service ↔ database, separate identity provider and backups. Any public hostname is attacker-reachable. Client-provided `orgId`, role, status, stock or price is **untrusted**. Org membership can change during active session; each permission-sensitive operation must check current membership state. Academic V1 is non-authenticated/local-only and **must not be promoted directly to public**.

### Threat matrix and test gate
| T | Abuse entry / impact | Control & required evidence |
|---|---|---|
| T01 IDOR/BOLA | Change another org's resourceId/organizationId in URL, query, JSON; leak/edit | Tenant predicate in every DB query, composite FK, role check, both GET and POST tests; return opaque 404 |
| T02 privilege escalation | Member sends `role=OWNER` or forged school email | Only authenticated current owner transfer, verified invite redemption, two-admin policy, never campus domain-as-proof |
| T03 CSRF/session theft | Forged browser POST using cookies | Secure+HttpOnly+SameSite, CSRF protection, expiry/revocation, origin validation, session rotation |
| T04 brute-force/user enumeration | Guess invites/accounts/forgot-password | Rate-limit by account/IP, generic responses, opaque random 128+bit tokens stored hashed, bounded expiry |
| T05 injection and query bugs | Resource name/filter crafted as SQL/HTML | Parameterized JPA/SQL, output HTML escaping, strict JSON size/fields, CSP, fuzz corpus |
| T06 time/capacity race | 2 approval requests both read same available | Single resource lock/serialized atomic transaction, precise interval semantics and race test |
| T07 ledger manipulation | Direct DB update/backdated movement hides missing stock | DB least privilege, append-only permission boundary, hashes/backup snapshots, reconciliation alerts; hash alone not immutable |
| T08 replay on POST timeout | Duplicate issue, consume, return | Persistent idempotency-key binding + body hash, operation receipt query, retry exactly-once test |
| T09 public demo shared-state leak | Visitor changes data visible to later strangers | Read-only or per-session expiring tenant, no actual emails/phones, rate limit/reset and visitor disclosure |
| T10 secret leakage | Logs/build artifacts/PR show provider key or JWT | CI secret scanning, env-based secret injection, redacted error/logs, no raw tokens in analytics |
| T11 old dependencies | Java8/Boot2.1 internet exposed vulnerable | Frozen course V1 local; supported V2 dependency matrix, automated CVE gate and patch runbook |
| T12 backup theft/loss | Off-site export leak/unrestorable crypto | AES-256 authenticated encryption, key separate from backup, checksum/restore drills, key-loss scenario |
| T13 denial-of-service/cost abuse | Flood queries exhaust free quotas | Input/pagination limits, org/storage quotas, per-IP/user throttles, provider hard fail-closed, no paid auto-upgrade |
| T14 file upload exploit | Path traversal/malware/large images | NO upload in P0; if CLO9 feature for course, isolated size/mime/image decoding and filename protections |
| T15 officer turnover hijack | Last owner lost, fabricated affiliation request | Two owners/admins, transfer acceptance and evidence, disputed org freeze, no unilateral email-domain claim |

## Philippine RA10173 compliance planning
The **Data Privacy Act of 2012** (RA 10173, E02) and implementing rules E03 apply to controllers/processors of personal data within their scope. This design cannot designate Ateneo as controller or processor without a real agreement. OrgResource owner/operator may have statutory obligations if real persons enrolled; relationships, legal bases, responsible contact, retention, security and incident reporting require formal review before P3. A privacy notice should specify purpose, legal basis, categories (account identifier, verified email, membership, custody audit), recipients/processors, residency/transfer if applicable, retention limits, subject rights, contact and incident channels. Obtain consent only where it is the appropriate lawful basis; do not claim consent alone solves all issues. Avoid national IDs, addresses, phone numbers, minors' data, sensitive categories, public rosters and unnecessary tracking in P0.

NPC E04 describes **conditional** mandatory personal-data-breach reporting involving serious-risk criteria; the published 72-hour rule for qualifying incidents must not be shortened to “all incidents must notify” or used as an excuse not to log other security incidents. Have incident classification and a real authorized operator before processing actual identities. The precise NPC reporting portal/procedure may change; check it at incident time. No legal advice/approval is claimed.

## Retention and data subject rights (provisional)
- Fictional public demo: rotate/reset at most 24h; no real identifiers stored or external messages.
- Pending invitation: expire <=7 days, delete token hash after 30-day abuse window absent obligation; period requires privacy review.
- Auth session: short-lived (proposed idle <=12h, absolute <=7d), active membership revalidation; token never logged.
- Idempotency receipts: 24h, sanitized responses only; transaction events retained separately.
- Audit/stock history: 12 months after org close as a **proposed operational ceiling**, not an asserted statutory mandate. Legal retention exceptions and dispute holds require scoped policy; pseudonymize actors where possible.
- Member request for access/export/correction/deletion: authenticated workflow, separate immutable transaction event from editable profile, disclose lawful retention exceptions.
- No raw phone number in V2 P0 and no live Twilio. Frozen V1 SMS simulator remains course-only, disabled by default.

## Release security checklist
1. Authentication setup & reset/recovery penetration tests; secure cookies, CSRF and current membership.
2. Every endpoint exhaustive cross-tenant + privilege test; SQL FK reject malformed cross-tenant row.
3. 100 concurrent adverse stock mutations; aggregate invariants and idempotency survive crash/restart.
4. Audit and secret scan, dependency CVE scan, OWASP ASVS 5.0.0 chosen level **target**, external test independent of author where possible.
5. Data notice/version + backup encryption and offsite restore; check provider subprocessors and region.
6. Public demo read-only/isolated and verified independent disclaimer, no official logos/seals.
7. Written go/no-go record; lack of a qualifying free host/maintainer/role authority means **DO NOT ACCEPT REAL USERS**.

**Status of all checks: NOT RUN for V2.**

## CP2 security and ownership evidence boundaries — 2026-11-15
**Verified current state:** no V2 public identity provider, verified-email service, tenant authentication, protected recovery or organization ownership validation has been demonstrated. O14 course backend is unauthenticated and MUST remain local/private, never directly published merely because Jersey2.x has a newer patch.
**Proposed synthetic technical demo only:** fictional scoped test accounts and controlled opaque one-time invitations may verify authorization code when implemented. This does NOT establish real email verification, official campus affiliation, identity recovery, a stakeholder pilot or privacy approval. Real users require actual governance and personal-data safeguards.
**Production acceptance must verify:** Jersey filter protection on every route; login/logout/session rotation/CSRF; account enumeration and invite abuse controls; suspension stops requests immediately; cross-tenant read/write/exports/operation receipts always refuse; ADMIN cannot self-promote, last owner preserved under racing changes. Authentication and DB composite FKs both required, not substitutes.
**Stock/race risk:** choose actual MySQL transaction isolation and lock order, validate 100-way capacity races and retries past expired receipt. Do not treat SHA-chained movements as immutable absent DB grants, trusted checkpoints and independently restorable encrypted backups.
**Material stack boundary:** Boot4/Jersey4 with Jakarta cannot automatically execute old `javax.validation` annotations. No supported fully literal same-public-runtime combination has been proven. [CP2 compatibility report](28_STRICT_COURSE_STACK_COMPATIBILITY_AND_CP2_CONFLICTS.md) = architecture BLOCKED pending explicit approval and tests. All V2 security tests currently NOT RUN.
