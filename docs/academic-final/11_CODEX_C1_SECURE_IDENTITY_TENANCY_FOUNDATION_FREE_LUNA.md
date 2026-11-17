# OrgResource — C1 Codex Master Prompt: Secure Identity, Organizations, Memberships and Governance

**Date:** 2026-11-17 Asia/Manila. **Agent:** Codex, free Luna, highest reasoning available. **Responsibility:** implement, debug, test, package, document actual evidence, commit/push/PR. **NO new open-ended product/vendor/market/hosting research or autonomous stack redesign.**

**Repository:** private \`newonathe/OrgResource\`.
**Implementation base:** latest \`feat/extended-c-c0-compat-2026-11-17\` (draft PR #13), last audited HEAD \`e492aeb24623476b05ad6d1ba9f4c9792e1bd105\`. **C0 code commit independently exercised in a local clean extraction:** \`86b3983171cd8aa439ded5aeed5f74baec546aba\`; subsequent commits are docs/evidence. Refresh before use.
**Protected Course A exact source K:** \`7f8c0746da18fd54b47f430ac1435ac36e605d67\` (PR #12).
**Research reference:** \`docs/orgresource2-research-continuity-2026-11-15\` at last audited \`dfda6434450c3e57a621bf23373ce6f4f68aa148\`. Source-contract files: \`docs/research-2.0/32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md\`, \`59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md\`, \`61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md\`, \`62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md\`, \`63_CP36_LEGACY_COMPATIBILITY_ATOMICITY_AND_CUTOVER_GATES.md\`, \`74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md\`. Use source-specific sections only when needed; latest CP44+ user two-edition decision governs older ONE-version descriptions.

## MISSION — ONE MEDIUM/LARGE COHERENT BUILD

Turn the now-tested isolated C0 Java21/Boot4/Jersey4/MySQL/Javax+Jakarta proof into the **maintained Extended C backend foundation**. Implement genuine local authentication, durable secure server sessions, invitations, offline fictional-organization bootstrap, five organization roles, membership/ownership state, one-use account recovery, and default-deny tenant authorization. Implement real HTTP endpoints and real MySQL tests. **No public exposure**, no real Ateneo users/claims, no source changes to Course A.

C1 IS NOT the full Extended C release: original 28 Course A routes and domain inventory/ledger/image/notification will be independently completed and tested on Extended C in later C2–C6. **Do not mark those unimplemented functions passing** because the isolated C0 demonstrated five representative route families.

### Core priorities

1. Absolute quality, safety, course preservation and test falsifiability.
2. Work per token/tool call/walltime: dependency-coherent checkpoints, target relevant files, one primary integrator, focused tests then one full acceptance, no repeated 1.2 MB research reads.
3. Lean storage/memory: one Maven backend, one MySQL source of truth, no microservice/Redis/queue/Kubernetes/OIDC provider, no generated binaries or secrets committed.
4. Leave working Course A's five entities, complete 28 routes, Java8/Jersey2/Javax and release hash **unchanged**. C will eventually independently reprove actual Jersey/JSON/Autowired/JPA/MySQL/Javax/Retrofit/Gson/scheduling and all 28 APIs; a dormant A copy is NOT sufficient.

## 0 — FRESH GIT/PR AUDIT; CRITICAL STOP

Before edits run \`git status --short --branch\`, \`git remote -v\`, \`git fetch origin --prune\`, \`git branch -vv\`, \`git ls-remote --heads origin\`, \`git log -12 --oneline --decorate\`, \`git worktree list\`. Inspect PR #13 current HEAD/base and CI; C0 specific Java21 tests were **reported passing locally**, whereas the existing GitHub workflows at last audit ran only Course A Java8 and React. Do not claim CI re-ran C0/C1 merely because the older workflow is green.

Branch from latest correct C0 implementation HEAD into new \`feat/extended-c-c1-identity-tenancy-2026-11-17\`; PR base should be PR #13 unless it has merged/rebased. Do **not** branch from ancient \`main\`, research/docs branch or Course A directly. Preserve uncommitted/unrelated files; no reset/force-push/automatic stash/merge/tag/deploy. Read source root \`AGENTS.md\` then newer \`AGENTS.md\` in the academic docs branch via \`git show\`; explicitly apply latest two-edition authority over legacy “O14-only” root wording.

**Falsification checkpoint:** \`git diff K -- backend frontend postman scripts/package-release.ps1 scripts/start-portable-demo.ps1\` must show **no changes to Course A-owned application/release code** throughout C1. Changes/additions in new Extended C module and a narrowly scoped new verification script are allowed.

## 1 — AUTHORITATIVE TECHNICAL BOUNDARY

Adopt the successful *tested candidate versions* for this LOCAL Extended C build, without new version selection:
Java 21, Spring Boot 4.1.2, Jersey 4.0.3, Spring Security 7.1.2 managed by Boot, JPA/Hibernate/MySQL, Flyway, Jakarta Validation and **genuine literal \`javax.validation\` API 2.0.1.Final + Apache BVal 2.0.6** explicitly bootstrapped. Retrofit/Gson remain in the module even though no real provider messages will be sent.

The C0 test established real Javax DTO validation via Apache BVal, *explicit/manual* Javax entity check before save, MySQL CHECK, but did **not** establish automatic \`javax.validation\` JPA lifecycle callbacks. Preserve the truthful distinction, including tests. Do not falsely relabel \`jakarta.validation\` as Javax or claim provider library presence proves integration.

**Source location:** Promote/refactor the working C0 experiment into **one** stable module \`extended/backend/\` (or the repository's clearly equivalent single long-term module), retaining traceability to commit \`86b3983\` and avoiding two competing maintained codebases. The old experiment may be removed from the *new C1 branch only* after the same code/tests are preserved in the new location; the immutable C0 commit/PR always retains its proof. If a rename is the cleanest option use \`git mv\` and update the verifier, build path, package references and \`edu.orgresource.c0\` production package names once. Do not copy all code into a second live module; no unused test-only account fallback in deployable runtime.

**Database:** disposable dedicated Extended C MySQL/InnoDB schema, no production or Course A import, no copying fictional A rows or plaintext contacts. C0 V1 migrations were experimental and never production-applied: establish an explicit, clean **new Extended C V1 baseline** using versioned reviewed Flyway migrations and validate with \`ddl-auto=validate\`. Never silently rewrite a migration already used in a real shared installation. All generated IDs/public UUIDs and data come from database/app-controlled code, not caller-granted org authority. Dates/timestamps for new identity records: UTC \`DATETIME(6)\`, with offset-aware public JSON where applicable; do not change original A's PHT timestamps. For C1 replace C0's probe-only `hibernate.jdbc.time_zone=Asia/Manila` with explicit UTC JDBC/session configuration and verify round-trips across Manila/UTC plus year 2040. MySQL `DATETIME(6)` stores wall values without automatic timezone conversion; tests must prove no drift.

## 2 — CHECKPOINT C1-A: REAL AUTH/ORG RELATIONAL FOUNDATION

Create proper entities/repos/migrations for exactly the identity/governance set plus existing five domain families needed for compatibility:

- \`organization\`: internal BIGINT PK, public UUID \`public_id\` binary-exact unique, unique normalized \`slug\`, display_name, \`ACTIVE|FROZEN\`, optimistic version, created/updated UTC.
- \`user_account\`: internal PK, public UUID, normalized unique \`username_key\` (no fabricated email), display name, \`ACTIVE|DISABLED\`, \`credential_version\`, timestamps.
- \`local_credential\`: user_id PK/FK, adaptive salted encoded password hash, changed_at; NEVER persist plaintext.
- \`account_recovery_code\`: user_id FK, unique SHA-256 of cryptographically random >=128-bit saved code, used/revoked/expiry, timestamps. Show raw secrets only once over appropriate authenticated/response contexts; never in logs/source/audit. At least one actual one-use/replay test.
- \`membership\`: public UUID, (org_id,user_id) unique, one role \`OWNER|ADMIN|CUSTODIAN|LEAD|MEMBER\`, \`ACTIVE|SUSPENDED|LEFT\`, version, UTC timestamps. Unambiguous composite \`UNIQUE(org_id,id)\` for same-tenant references.
- \`invitation\`: org_id, inviter membership ID (same org), intended role (NOT OWNER), SHA-256 of a cryptographically random **256-bit bearer token**, status \`INVITED|ACCEPTED|REVOKED|EXPIRED\`, expiry at most 24 h, used-by account FK, version; NO \`intendedUserId\` and no fake identity verification.
- \`ownership_transfer\`: org_id, initiating OWNER membership, target ACTIVE ADMIN membership, status \`PENDING|ACCEPTED|REVOKED|EXPIRED\`, version, expiry and acceptedAt. Enforce **at most one PENDING transfer per org** using an actually tested MySQL generated nullable pending-org column + unique index (or exactly equivalent working schema). Do not use \`UNIQUE(org_id,status)\` that rejects multiple historical terminal transfers.
- \`audit_event\`: append-only redacted actor/action/target/time/request ID; no recovery/invite/password/phone secrets; no destructive cascade.
- Spring Session JDBC **official library-managed MySQL schema**, 2 tables; use matching dependency/version, not an invented hand-coded session DDL.
- Existing FIVE core families still have true JPA mappings and organization_id columns. Add true FK from each core organization_id to Organization, plus composite same-org FKs for Project→Department, Requirement→Project/Resource and Allocation→Requirement. Preserve physical \`resource_requirement\`, \`resource.type\`, \`allocation.borrower_name\` names. Do not introduce inventory event ledger/idempotency/image tables yet merely to reach “18 tables”; those are later justified phases.

Every mandatory child org/parent composite FK uses NOT NULL keys and same signed types; use \`ON DELETE RESTRICT\`, no orphan/cascade historical loss. DB-level unique constraints enforce normalized names and membership races. Check version optimistic concurrency, numeric quantity constraints and all table indexes in actual MySQL. Add schema migration negative tests for cross-org FK, duplicate username/slug/member, invalid role/state and new join paths.

**Offline bootstrap:** an explicit \`--bootstrap\`/CLI one-time operator action for a *fictional* organization and its single initial OWNER, accepting secret through a safe local prompt or protected process setting. Atomic creation of org+account+credential+ACTIVE OWNER+initial one-time recovery material, idempotent/refuse if already initialized, no HTTP \`POST /orgs\`, no public “first registrant becomes owner”, no permanent admin backdoor or account impersonation. Allow separate **test-only** fictional second-org bootstrap through controlled fixtures. No real affiliations/users.

## 3 — CHECKPOINT C1-B: HTTP SESSION AUTH + RECOVERY + TENANT BOUNDARIES

Implement real Spring Security/Spring Session JDBC, same-origin session-cookie authentication, no permanent test users. Production intent cookie \`HttpOnly\`, \`Secure\` under HTTPS, host-only, SameSite=Lax, bounded idle+absolute lifetime; HTTP loopback test profile may relax Secure ONLY for test and is never exposed publicly.

- \`GET /api/v2/auth/csrf\`: anonymous pre-auth session and fresh token, no-store, JSON \`{data:{csrfToken},requestId}\`.
- \`POST /api/v2/auth/login\`: JSON username/password, CSRF and same-origin; generic 401 (no enumeration), rate-limited 429, rotate session ID and CSRF; disabled account denied.
- \`POST /api/v2/auth/logout\`: CSRF, session invalidation, 204; old session denied.
- \`POST /api/v2/auth/recover\`: username + saved one-time code + new password, pre-auth CSRF, atomic code use and credential-version increment; generic privacy-safe answer; revoke prior sessions; used/expired/replayed code denied (including race).
- \`GET /api/v2/session\`: only authenticated account's ID, display name, ACTIVE memberships and activeOrgId if valid; never raw secret/hash/phone.
- \`GET /api/v2/orgs\`: authenticated user's actual active organizations ONLY, not public enumeration.
- \`POST /api/v2/session/active-org\`: authenticated CSRF, select only ACTIVE own membership in ACTIVE org, update session context securely and invalidate stale privilege; do not trust org ID in user header or client claims.
- \`GET /api/v2/account\`; \`POST /api/v2/account/password\` requiring current password, rotate session and revoke prior credential-version sessions; \`POST /api/v2/account/recovery-codes/rotate\` requiring current password, revoke old codes, issue new only once; \`POST /api/v2/account/disable\` requiring current password and last-owner guard across ALL orgs before disabling global account.
- \`POST /api/v2/invitations/redeem\`: valid still-unconsumed bearer token, <=24h; anonymous token+username+password+displayName creates local account+membership and shows initial recovery codes once; authenticated existing account + token joins as member; single winner in concurrent redeems, no duplicate \`(org_id,user_id)\`, never claims verified Ateneo affiliation.

JSON semantics: public success \`{data:T,requestId}\`, controlled \`{code,message,requestId}\` errors, sensible 200/201/204/400/401/403/404/409/422/429; original Course A \`/api\` remains different unwrapped contract and must not silently be replaced. CSRF required for **all cookie-authenticated POST** including login, logout, recovery and invitations. Domain \`Idempotency-Key\` scheme belongs to later C2/C3; do not require generic domain receipts for auth actions. No token/password in URL/query, localStorage, logs or raw audit.

Authorization source is fresh trusted session user ID PLUS DB \`user_account.status=ACTIVE\`, \`credential_version\`, ACTIVE membership, ACTIVE organization and current role; revalidate on each protected route/read/write, including replay. URL org IDs are selectors, not authentication. Foreign numeric/UUID IDs return privacy-preserving 404; visible wrong-role returns 403. \`FROZEN\` org permits only authorized read; mutating domain/governance requests refused (except explicit authorized recovery flows later). Create a single reusable **server-side org authorization service**; never rely only on Jersey annotations or JS hiding buttons.

**Throttle:** implement bounded effective one-process login/recovery/invite attempt protection (per normalized account and source where safe), with 429 and no user enumeration; document that single-instance in-memory throttling is NOT adequate proof for multiple public replicas and must be reviewed at hosting acceptance. No Redis just for this local phase.

## 4 — CHECKPOINT C1-C: FIVE-ROLE GOVERNANCE AND OWNERSHIP

Use the published CP35 role/route table exactly, guarded by current DB rights; do not add unresearched campus-identity policy. Implement:
- \`GET /api/v2/orgs/{orgId}/members\` with bounded cursor/list and safe fields: OWNER/ADMIN only.
- \`POST /api/v2/orgs/{orgId}/invitations\`: OWNER can grant ADMIN/CUSTODIAN/LEAD/MEMBER; ADMIN only CUSTODIAN/LEAD/MEMBER; no OWNER invitation; one-time token returned only to authorized inviter in no-store response.
- \`POST /api/v2/orgs/{orgId}/invitations/{id}/revoke\`: allowed grantor, current still INVITED only.
- \`POST /api/v2/orgs/{orgId}/members/{id}/role\`: OWNER may change non-OWNER member roles; ADMIN may only change lower non-ADMIN roles; cannot grant OWNER by update or demote last OWNER.
- \`POST /api/v2/orgs/{orgId}/members/{id}/suspend\`: appropriate current OWNER/ADMIN, no last-owner orphaning; suspended user loses access immediately. Don't manufacture unsuspend route outside approved contract.
- \`POST /api/v2/orgs/{orgId}/transfer-ownership\`: initiating current effective OWNER selects ACTIVE ADMIN in same org; one PENDING; recent reauthentication, bounded expiry.
- \`POST /api/v2/orgs/{orgId}/accept-transfer\`: only named still-ACTIVE ADMIN target; under one org-root locked transaction atomically promote target to OWNER, demote initiating OWNER to ADMIN and accept transfer. **Treat old-owner demotion policy as the documented CP35 synthetic-workspace design choice for C1, not verified real student-org policy.** Never leave zero owners.
- \`POST /api/v2/orgs/{orgId}/transfers/{id}/revoke\`: only initiating OWNER on a still-PENDING transfer; bounded expiry worker marks expired PENDING safely/idempotently.

Use strongly validated \`If-Match\` for mutable existing objects (missing 428, stale 412), new ETag/versions after commit; don't overbuild unrelated domain idempotency/ledger before C2. Role or member changes lock **Organization first**, then involved membership IDs ascending, then transfer/invite; re-check ACTIVE account/member/org and role under same transaction. LAST_OWNER is enforced even when account disable races with member demotion/transfer across MULTIPLE organizations (lock all affected orgs ascending). Audit actor/target/action/outcome without secrets. No arbitrary physical deletions of membership/identity/audit history.

C0's `availableQuantity` helper currently clamps negative stock-availability arithmetic to zero; it must NOT be copied as evidence of an intact invariant in later C2. Record this source-level risk as an explicit C2 regression and require a controlled inconsistent-state failure rather than silently hiding negative derived quantities. Do not perform the C2 ledger rewrite in C1.

Do not implement unrelated borrower search, Project lead assignment, inventory stock, image migration, full original 28 endpoints, emails, SSO, external identity confirmation or public onboarding in C1. They belong to subsequent domain and UI phases; do not claim the product is complete.

## 5 — ACCEPTANCE, NEGATIVE TESTS, TRUE EVIDENCE

Prefer a **single reusable real MySQL/HTTP integration harness** evolved from \`scripts/verify-c0.ps1\` and renamed to a stable \`scripts/verify-extended-c1.ps1\`; use a new disposable loopback-only MySQL schema/instance, fresh Flyway migrations, never a personal/shared DB or Course A schema. Reuse existing HTTP/CSRF/test fixture helpers rather than copying many standalone test harnesses. Never use H2-only or mocked repository tests as the sole proof.

Mandatory falsification matrix (name tests with IDs):
- **ID01** offline bootstrap atomic one-owner and no public org-create path; repeat/restart and two-racer first-bootstrap exactly one owner.
- **ID02** Javax DTO path + Jakarta validator coexist and Jersey→Autowired→JPA/MySQL still PASS; old A untouched.
- **ID03** seven identity tables + audit + five actual core tables + two actual Spring Session tables with correct FK/unique and Flyway version; cross-org FK direct SQL rejection.
- **ID04** anonymous 401, wrong-role 403, foreign org/foreign object 404, FROZEN write refusal, non-admin member-list denial, correct org switch/revocation.
- **ID05** preauth CSRF, login CSRF, correct login session/CSRF rotation, missing/wrong CSRF denial, same-origin denial, logout invalidation, cookie security test/prod configurations.
- **ID06** single account in two orgs with different roles, forged org header/ID never grants authority, activeOrg changes only with membership; role/suspend effects immediate even on old live session.
- **ID07** >=15-char single-factor password policy, adaptive salted hashes, generic wrong-user/password, throttle 429; no raw passwords in DB/API/log.
- **ID08** save recovery codes once and hashed, password recovery consumes one exactly once, concurrent/repeat/restart denial, revokes earlier sessions/credential_version; code rotation invalidates previous codes.
- **ID09** anonymous invite creates a NEW account+membership with recovery codes once; existing account redeem joins same-org exactly once, 256-bit token hashed, 24h boundary/revoked/expired/cross-org/role escalation denial.
- **ID10** OWNER/ADMIN grant restrictions; MEMBER/LEAD/CUSTODIAN can't issue invitations or self-promote; no OWNER role via invite/role update.
- **ID11** ownership transfer wrong recipient/expired/replayed/foreign org refused, atomic accepted target owner+old owner admin, no gap, only one pending by DB unique under race.
- **ID12** simultaneous owner-changing operations and account disable cannot leave zero effective ACTIVE OWNER; no cross-org accidental disable; preserve other org memberships.
- **ID13** server-side session persists restart where selected; revoked sessions stay revoked after restart; idle/absolute expiry actually enforced, no sensitive session data leak.
- **ID14** no stale C0 hardcoded test users in normal runtime, every \`/api\` and \`/api/v2\` path denies anonymous, old C0 representative domain routes remain protected; full 28 Course A route parity is NOT RUN in C1.
- **ID15** migration clean install + Java21 Maven test/build + packaged HTTP startup + realistic login/invite/recovery/owner turnover; all database and state invariants persist after restart; no embedded credentials; exact source SHA/DB version evidence.
- **ID16** actual Course A Java8 CI and artifact/source K unchanged; C1 files are new Extended C scope only. No cloud/provider SMS or academic submission.

For concurrent tests use deterministic latches/barriers and separate transactions; no "sleep and hope" test. Assert actual rows/versions, not HTTP success only. Confirm no history lost, cleanup touches only disposable fixture schema, and logs redact token/password/recovery/body. If a required security/SQL/schema mechanism fails, STOP dependent code, preserve minimal reproducible failure and truthful status; no weakening assertions to green.

**New Extended C CI:** at last C0 audit existing GitHub "Verify" workflow ran only Course A Java8 and React, **not C0**. Provide a reproducible C1 verifier. Add a narrowly scoped Java21/MySQL CI job ONLY if it can be run without unapproved paid capacity; clearly mark C1 remote CI NOT_RUN if not. Avoid both duplicating the full React build in multiple jobs and excessive CI cycles. Local all-gates acceptance should run at a clean code commit and be reproducible on another authorized environment.

## 6 — TOKEN/QUALITY CHECKPOINTS, OPTIONAL REAL SUBAGENTS

One medium-to-large dependency-coherent cycle with internal checkpoints, not 20 independently prompted microtasks:
- **C1-A:** promote working C0 foundation, schema/migrations, offline bootstrap, source preservation, focused MySQL tests.
- **C1-B:** login/session/CSRF/recovery/invites and negative security; focused HTTP tests.
- **C1-C:** five-role governance, transfer/last owner, concurrency and full acceptance, safe clean extraction.

Use available subagents ONLY if the actual free Codex environment exposes real tools/models and delegation reduces overhead. At most 2: fast read-only migration/route audit; medium isolated tests/security review with no shared writers. Primary highest-reasoning agent owns source edits to shared POM/security/config/migrations, review, lock order, Git and final report. Do not invent "spawned" agents. If no subagent capability, execute sequentially.

Read targeted \`AGENTS.md\`, C0 report, 32/61/62 source sections and directly touched Java/SQL only; use search/grep for precise method contract. Avoid exhaustive research/old 319k handoff/history. Early targeted tests; one final clean full gate. Short, meaningful evidence; no duplicated long documentation. If full coherent scope cannot finish due actual blockers, push safe completed work and mark C1 CONDITIONAL with explicit remaining endpoints, but do NOT mislabel incomplete auth as secure or enable public ingress.

## 7 — REPO, PR, REPORT AND STOP

Create \`evidence/C1/PHASE_REPORT.md\` with:
- A3 source K, C0 base SHAs/PR #13, C1 branch and exact commit, source ownership diff against A; actual effective Java21/library versions and unsupported provider caveats.
- Schema version/tables/constraints/composite FK/sessions, exact functional endpoints with request/response/auth/negative test coverage.
- Actual MySQL and HTTP integration test names/counts/results, schema/transaction/CSRF/throttle/session restart/role-invite/recovery/last-owner race evidence.
- Course A compatibility boundary: original five + 26+2 preserved at K; C1 only representative protected routes; DON'T claim C already academic full-parity.
- Security audit (secrets, auth/session, IDOR, rate limits), performance/DB query plan where measured only, remaining production limits.
- Clean build, packaged JAR, startup reproducibility, honest CI distinction.
- Final local and verified remote SHAs, new draft PR URL, exactly next phase C2 dependencies.

Before committing: inspect full status and \`git diff --check\`; no secret source, test data dumps, compiled JAR, \`target/dist/node_modules\`, copied A release ZIP, private phones, raw passwords/invite or recovery tokens. Commit ALL task-related source, migrations, tests, minimal run scripts/docs. Push without force, verify remote SHA, open/update **one draft C1 PR against PR #13 branch** unless actually merged. No main merge, A tag, cloud deployment, paid auth provider, real email/SMS, official Ateneo claim, destructive DB reset, Canvas submission.

Use exactly one verdict:
- \`C1 VERIFIED — IDENTITY AND TENANT GOVERNANCE READY FOR C2\`
- \`C1 CONDITIONAL — EXACT UNPASSED OR UNTESTED SECURITY GATES\`
- \`C1 BLOCKED — MATERIAL AUTH/DB INTEGRITY FAILURE\`.

**Stop after C1. Do NOT implement stock movement ledger, all 28 migrated routes, v2 inventory workflows, additional UI views, public hosting, or C2.** These are subsequent separately reviewed engineering batches. Begin actual C1 coding/testing now; Codex is the builder, not the research/architecture-selection chat.
