# ORGRESOURCE — C3 CODEX MASTER: C2 INTEGRITY CLOSURE + COMPLETE SECURED COURSE ROUTE PARITY

**Date:** 2026-11-18 PHT · **Agent:** Free Codex / Luna model at highest reasoning actually available · **Task type:** SOFTWARE IMPLEMENTATION, TESTING, DEBUGGING, PACKAGING, COMMIT/PUSH/PR ONLY. **Codex must NOT undertake market/vendor/academic research or choose architecture.** Planning/research has already supplied design/evidence; narrow dependency/error investigation to implement approved contracts is allowed.

**Repository:** private \`newonathe/OrgResource\`.
**Current legitimate implementation parent:** \`feat/extended-c-c2-ledger-atomicity-2026-11-17\` at audited SHA \`c8b6e76c249f99f3642c1513ddd7d55b584e027a\`, draft PR #15, base C1 feature branch; latest actually tested code SHA \`aa2f30f7239316f8be11968fff4a41e665e710de\`. Refresh GitHub before changes. GitHub run \`44150537948\` PASS on C2 HEAD for 3 distinct jobs: Java 21+MySQL Extended C, Java 8+MySQL Course A, React. C2 report lists **27 backend integration test methods**. These results DO NOT prove full Course A route parity or production readiness.
**Protected Course Final A source:** \`7f8c0746da18fd54b47f430ac1435ac36e605d67\` on A3 PR #12; NEVER alter its actual \`backend/\`, \`frontend/\`, original Course A Postman/release sources/archives or historical tags. Extended C branch must descend from this exact A source lineage, independently pass required course functions in **the modern JVM**, and never falsely claim dormant A archives satisfy C compliance.

**REQUIRED compact source intake:** \`AGENTS.md\` from actual C2 branch, \`evidence/C2/PHASE_REPORT.md\`, source-review register \`docs/academic-final/13_C2_SOURCE_AUDIT_AND_C3_RELEASE_GATES_2026-11-18.md\` from \`docs/course-a-final-spec-build-handoff-2026-11-16\`; original exact contract \`02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md\`, \`07_COURSE_A_EXECUTABLE_SPEC_FREEZE_2026-11-16.md\` (same docs branch); relevant original Course A five controllers/Components+DTO, image and reminder code on frozen K; research CP34/35/36/41 only sections for changed behavior. Avoid whole research archive or prior 319K master rereads. Cite source at actual SHA in acceptance report.

## 0 — Fresh safe Git audit and scope authorization

BEFORE edits:
\`\`\`bash
git status --short --branch
git remote -v
git fetch origin --prune
git branch -vv
git ls-remote --heads origin
git worktree list
git log -12 --oneline --decorate
\`\`\`
Inspect PR #15 head/base, remote CI/test count, actual current \`AGENTS.md\`, and whether the prompt/docs branch has newer revisions. Branch from latest verified C2 HEAD to \`feat/extended-c-c3-course-parity-2026-11-18\` (or collision-free name). New draft PR target C2 feature branch unless merged. Preserve dirty/unrelated work in safe separate worktree if necessary; NEVER reset/force-push/auto-stash/merge main/drop or reset shared MySQL. No paid provider trials/cloud accounts or Canvas.

**Engineering objective:** One substantial dependency-coherent C3 cycle, three INTERNAL checkpoints, no repeated tiny prompts. **Hard constraint:** exact course fidelity, tenant safety and ledger correctness cannot be reduced to save tokens. Optimize verified finished code per walltime/token/tool call/storage; one primary integrator; optionally maximum 2 real narrowly bounded subagents IF available in free Luna (fast read-only 26-route/DTO diff audit; medium read-only security/concurrency or npm advisory audit). No pretend model selection or spawned agents, no conflicting same-file writers. Shared POM/migration/security/CI owned by primary.

## 1 — C3-A: close confirmed C2 contract defects FIRST, do not bury them

**A1 — permanent physical operation receipt vs short HTTP key (MATERIAL):**
C2 \`InventoryService.priorOperation\` and \`operationReceipt\` reject a committed physical \`operationId\` after 24h \`idempotency_key.expires_at\`, whereas approved CP34/CP41 requires **permanent** stock movement identity/ledger replay (same operationId+same fingerprint after reauthorization) independently of ephemeral HTTP idempotency. C2 \`C2_LEDGER02\` currently tests the incorrect 409 as expected: correct the implementation **AND replace the erroneous oracle** with the source-derived permanent behavior.

Implement a clean split:
- HTTP \`Idempotency-Key\` receipt is 24h, actor+route+body-fingerprint scoped, and may be pruned after expiry. Its prior serialized HTTP response is NOT a forever ledger record. A different key does not authorize duplicate stock writes.
- \`stock_movement\` is **permanent append-only**, with \`UNIQUE(org, operationPublicId)\`, immutable actor, action/resource/allocation, fingerprint/quantity/deltas and occurredAt. For an authorized physical operationId after HTTP receipt expiry or cleanup, reconstruct a **durable movement-fact receipt** (e.g. operationId, kind, quantity, deltaTotal, deltaIssued, resourceId, allocationId, occurredAt) from committed movement, not invented former availableQuantity/current ETag/requestId. If a full identical prior HTTP response is retained, prove correct source provenance. You may use a narrow additive V4 migration if stable actor/route fields are genuinely missing; NEVER edit V3 in place. Document the historical durable receipt response type as a deliberate v2 contract extension.
- A same operationId+same request fingerprint returns the prior durable physical movement result **after rechecking current role, active org, valid membership and tenant**. Same UUID with changed action/resource/quantity/fingerprint →409. Original bodyless A04/A05/A06 remain source-style duplicate-state 409. Suspended or foreign actor cannot read/replay. Approve/cancel (no movement) need only 24h HTTP idempotency semantics.
- No infinite receipt/stock table accumulation from expired *HTTP-only* rows: bounded safe prune of expired idempotency_key when no active transaction, no deletion of permanent movement; index and retention tested. Never delete a stock event to save space.
- Test after 25h, after HTTP receipt rows pruned, after JVM restart, wrong actor/role, changed operation fingerprint, same/different request key, collision, concurrent identical vs distinct ops. No double stock. Do not return 409 **solely** because an HTTP receipt expired if a matching physical movement exists.

**A2 — input/error correctness:** \`InventoryResource\` passes nullable body to \`C1RequestValidator.validate\`; Javax Validator rejects null invocation; static review indicates a potential uncontrolled 500. Test actual POST empty body, missing fields, malformed JSON, invalid UUID/enum/quantity, oversize request, absent/invalid If-Match and idempotency header, forbidden tenant. Ensure source-appropriate 400/409/412/415/422/428, sanitized 500 only genuinely unexpected. Do not weaken existing tests.

**A3 — source-compatible lists and aggregate correctness:** C2 \`C1Service.listProjects(null)\` incorrectly returns empty while frozen A \`ProjectComponent.list(null)\` returns all; similarly Q02/A02 null filter behavior and A02 status filter missing. R02 type filter missing; Resource GET currently loops per resource with repeated SQL aggregate. Fix tenant-scoped list/filter semantics with one sampled PHT instant and bounded grouped aggregate for resource counts. No global inherited JPA \`findAll\` in user-request path; original legacy GET JSON remains array and optional filter behavior; v2 list may have bounded cursor. Add two-org duplicate names/filter tests. Do not add new frontend features here.

**A4 — 12 npm high advisories:** FIRST run \`npm audit --json\` and \`npm audit --omit=dev --json\` against the actual \`frontend/package-lock.json\`, record sanitized advisory ID, installed/fixed version, direct/transitive chain, production-vs-build/dev exposure, and whether a compatible fix is available. The user and C2 report observed **12 high** from \`npm ci\`, but individual IDs were not yet independently verified; do not invent affected packages or claim all 12 browser-exploitable. Apply narrow compatible patch/minor updates ONLY when supported by actual audit and full React/Playwright/build tests; preserve accurate package-lock, do not \`npm audit fix --force\`, blanket suppress, delete runtime dependency or rewrite React to silence CLI output. If a high advisory has no safe fix, create explicit owner-reviewed risk entry with mitigations and block public production release; **local C3 development can continue** if no exploit path arises from local operation. Keep the original academic source K untouched—any frontend changes must be separately staged for Extended C only or deferred to C4 as documented.

**C3-A hard stop:** If the 24h vs permanent physical ledger discrepancy cannot be corrected without breaking atomicity/tenant isolation, STOP dependent route ports, push minimal reproducible failure and CONDITIONAL verdict; don't bury it in C3 tests.

## 2 — C3-B: COMPLETE live, protected original 26 Jersey operations on Extended C

**Actual C2 source coverage:** 15/26 academic business routes currently present: D01,D02,P01,P02,R01,R02,Q01,Q02,A01,A02,A03,A04,A05,A06,A07. This is source-static count, not a claim all currently pass complete A parity. Implement the EXACT 11 absent:
\`\`\`
D03 POST /api/departments/{id}/update            D04 POST /api/departments/{id}/delete
P03 POST /api/projects/{id}/update               P04 POST /api/projects/{id}/delete
R03 POST /api/resources/{id}/update              R04 POST /api/resources/{id}/delete
R05 GET  /api/resources/{id}/availability
Q03 POST /api/requirements/{id}/update           Q04 POST /api/requirements/{id}/delete
A08 POST /api/allocations/{id}/delete            A09 POST /api/allocations/{id}/revoke-sms-consent
\`\`\`
Also correct the other 15's source-compatible query/response semantics (particularly P02/Q02/A02/R02) and run full route-by-route tests. The original 26 = 25 historical proposal routes + later A09, not 26 original proposal statements. **Do not count the 2 images as original.**

Use the frozen A exact \`backend/src/main/java/app/rest/controllers/{Department,Project,Resource,Requirement,Allocation}Controller.java\`, \`backend/src/main/java/app/components/*Component.java\`, \`backend/src/main/java/app/dto/ApiDtos.java\`, and relevant course tests as source-specific implementation oracles. Actual shared backing is the Extended C MySQL JPA five entities and C2 ledger, not duplicate mock storage. Every original Jersey \`@Path\` delegates via \`@Autowired\` Spring Component; real \`javax.validation\` runs, Hibernate/JPA/JPQL/MySQL and scheduler/Retrofit remain executable.

**Exact compatibility boundary:** Original \`/api\` uses Long numeric IDs, original relative path/method/query/JSON, unwrapped success \`{...}\` or \`[...]\`, errors \`{code,message}\`, PHT local YYYY-MM-DDTHH:mm:ss dates. Create=201, actions/eligible delete=200, lists=200. Do **NOT** silently demand public-v2-only org public UUID, \`operationId\`, \`Idempotency-Key\`, \`If-Match\` or extra JSON fields for original methods. The ORIGINAL legacy R03 retains \`expectedTotalQuantity\` compare-and-set, emits an internal permanent ADJUST when physical T changes, and rejects stale 409; legacy zero-stock never-moved R04 genuinely physically deletes when no dependencies, historically moved T0 gets explicit ledger safety 409 deviation. Original A04/A05/A06 no-body transition state guards use internal operation IDs, one actual movement, repeat 409. No duplicate mutation on ambiguous resend.
**Security extension** clearly documented: session auth, CSRF on POST, explicit selected active org, allowed role and server-derived tenant, foreign numeric ID privacy 404, default-deny. No unscoped repository lookups.

**Per-family acceptance rules:**
- Department: normalized per-org unique name, update conflict, delete only without Projects.
- Project: P02 unfiltered returns same-org projects; filter validates foreign Dept 404. P03 name change allowed after approval but period locked after historically approved allocation; date ordering and PHT semantics; delete refuses Requirements; concurrency stale semantics.
- Resource: type immutable, list optional type/availability accurate; R03 only name/T/expectedTotalQuantity and ledger ADJUST, reject below H+I, no double stock. R04 virgin T0 truly deletes if no Requirements/History/Image, moved-to-T0 is PROTECTED_HISTORY; R05 actual effective H/I sampled once.
- Requirement: Q02 optional project filter, accurate covered/shortage; Q03 rejects quantity below effective coverage or max PENDING batch and if project ended; Q04 forbids any Allocation history.
- Allocation: A02 optional requirementId AND status filters; create consent/E.164 and before-end rules; A03–A07 real transaction; A08 only PENDING/CANCELLED never approved history (\`approvedAt==null\`); A09 repeated-success clears contact and consent without changing T/H/I, safe even after prior send and cannot withdraw already-sent provider request.
- For future v2 paths, role matrix as CP35 (ADMIN isn't CUSTODIAN; LEAD only assigned projects, MEMBER own verified membership-linked request). If assignment/borrower-binding is not implemented, fail closed and mark route unavailable rather than invent role grants.

**PRIVACY requirement** for Extended C: existing C1/C2 \`allocation.phone_number\` stores raw optional contact when consent=true. Because C is intended for later real users, do not make that a publicly deployed capability. Implement narrowly scoped at-rest protection from approved design (AES-256-GCM per-row random nonce, configurable keyId/secret external to Git, authenticated encryption, no logs/JSON, fail closed when key missing), OR for local no-live-SMS configuration reject/storage-drop consent-bearing phone with explicit compatibility limitation and public release blocker. Do not pretend raw-at-rest contact is production-ready. A later V4 migration must reject/flag any unknown preexisting non-null plaintext contact instead of silently reading it; clean fictional C3 DB must test actual encrypted store/no plaintext, E.164 validation, wrong key failure, A09 erasure and cross-org denial. Do NOT modify previous Flyway V1–V3 migration checksums, and no key in source, logs, GitHub CI, ZIP, mock fixtures from real students.

**IMAGE CLO9 beyond original 26:** Port two additional actual HTTP interfaces:
- \`POST /api/resources/{id}/image\` multipart field \`file\`, real PNG/JPEG magic + MIME, 1–2,097,152 bytes, bounded streaming, no SVG/URL/path/filename use; 200 JSON \`{resourceId,contentType,sizeBytes,sha256}\`.
- \`GET /api/resources/{id}/image\` exact binary JPEG/PNG, safe cache/nosniff and privacy headers, 404 absent. Existing Resource row nullable \`MEDIUMBLOB\`/validated MIME, no sixth business entity or filesystem upload directory. Real MySQL/restart byte-identical, 70KiB+, 2MiB boundary, invalid/cross-org, auth/CSRF and corrupt image rejection.
- Protected \`/api\` image paths maintain Course A semantics except authenticated/tenant-safe. If public \`/api/v2/orgs/{id}/resources/{id}/image\` is also implemented, require OWNER/CUSTODIAN change, \`If-Match\`+CSRF and scoped authenticated GET; do not let it bypass legacy image validation or consume unlimited disk.

**Schema rule:** use one carefully reviewed additive Flyway V4 (and V5 ONLY if independently necessary). No modification of V1/V2/V3 checksums; fresh C3 schema and explicit migration limitations tested. No extra Redis, queue, new media entity, fake SSO, invented official Ateneo affiliation, partial returns or slot booking.

## 3 — C3-C: actual expiry/reminder integration and full source acceptance

C2 \`C1ReminderScheduler\` only increments a disabled tick. \`C1Service.cleanupExpiredPending\` currently handles only PENDING. Port actual Course A behavior into the modern Extended C JVM:
- Bounded \`@Scheduled\` expiry scanning PENDING+APPROVED whose Project.endAt<=sampledNow; org+Project+Resource+Requirement+Allocation lock order and per-row transaction; cancel EXPIRED exactly once, preserve previously approvedAt; effective APPROVED hold is *already zero* at end boundary, no phantom movement. ISSUED never auto-returned. No scheduler changes Resource.totalQuantity or inserts ISSUE/RETURN/CONSUME ledger movement.
- Due reminder for ISSUED reusable only, due window [now,now+24h), consent + E.164 + no prior attempt, atomic bounded claim, SEND OUTSIDE stock/DB transaction, record outcome SIMULATED/PROVIDER_ACCEPTED/REJECTED/UNKNOWN/SUPPRESSED with truthful no automatic replay of unknown outcome. Safe restart/stale-claim result, one scheduler tick actual observed. Transaction privilege/tenant scope and privacy encryption must remain enforced.
- Actual Retrofit/Gson outbound HTTP to separate local FakeTwilio and official Twilio Messages REST form-encoded/Basic-auth loopback test servers; keep real network mode DISABLED by default, **NO real Twilio call or recipient/secret**, no provider acceptance/device delivery claim from local stub. Use the frozen A \`ReminderComponent\`, \`ReminderDispatcher\`, \`ReminderScheduler\`, \`ExpiryScheduler\`, \`TwilioRestTransport\` source as behavioral examples; adapt for supported modern runtime and real encrypted contacts.
- For local tests without configured encrypted contact or provider, suppression/disabled outcome is honest. Never send raw private phone to an external service or log it.

**Actual runnable academic compatibility test:** Execute **all 28** current A endpoints from one importable Postman/Newman reference with a safe AUTH+CSRF+explicit-org prelude, preserving the exact 26 individual original request method/body/query/response contracts and 2 additional image operations. New resource allocations must operate against the SAME Extended C MySQL stock ledger and session; no mock /api and real /api/v2 side systems. Test error statuses and real negative role/foreign-tenant cases. Need automated dynamic IDs and reusable fictional test fixtures, no hardcoded already-used IDs. Run at least:
- 26-by-ID route inventory static+live parity; response keys exact to frozen A, including A09 redaction and R03 stale CAS.
- 2 image operations byte-for-byte and restart; no false JSON on image GET.
- All five business entity types runtime CREATE, READ, eligible genuine DELETE; protected history 409.
- Resource reusable T10 Q6 q4 and consumable T100 Q40 q30, expiry edge T10 H3 I2 expired H4 = available5, no double movement, concurrent approve/issue, rollback.
- Explicit account/org selection, session CSRF, cross-org IDOR on all 28 routes, no phone/raw tokens/passwords in API/logs; role denial.
- Scheduling actual invocation, disabled FakeTwilio/official loopback, source-equivalent status/error output.
- Jersey @Path, injected @Autowired Component, literal javax validation, Spring Data JPA/Hibernate/MySQL and custom JPQL, Retrofit/Gson, JSON, MySQL real runtime and Postman, bounded file HTTP. Do not treat presence of imports or a Java8 A artifact as C passes.
- Real packaged Java21 JAR root static React and API on same process, MySQL restore/restart data retention and source-preserved A Java8 61-test check.
- **Frontend caveat:** Existing A Academic Glass 8 screens may be served statically but still be wired to old unauthenticated assumptions; C3 does **not** claim fully usable secured UI. C4 will perform full connected UI, visual/UX and browser acceptance.
- npm audits before and after any compatible patch; summarize actual counts and residual high advisories, public release BLOCKED if high production runtime exposures unresolved.

**Quality/security falsification:**
- Do any full GET lists reveal another org? Are results bounded or controlled? Are movement balances nonnegative, strictly reconciled and constant under concurrent reads?
- Does GET /api/projects without filter return same-org rows, not empty? Does A02 filter by status and requirement independently?
- Does any legacy /api POST demand a newly invented operationId?
- Does the send scheduler call HTTP while holding a DB lock? Does an UNKNOWN result cause retry? Does PHT/UTC cause expiry to move by 8 hours?
- Could a stale, suspended, switched-org or wrong-role session read, act, or replay stock operations?
- Does missing/oversized/malformed multipart safely fail? Are bytes persisted >64KiB? Any XSS/SVG/unsafe file name?
- Can a nonzero stock history migration/backfill invent a movement? Does direct SQL UPDATE/DELETE history fail?
- Are records/receipt keys/old phone bytes still stored indefinitely without retention plan? Prevent unnecessary indefinite growth.
- Are Maven and npm security findings classified, not dismissed because \`npm ci\` exits zero?

## 4 — Efficient execution / STOP / Git

**ONE C3 cycle** with **three dependency-coherent checkpoints**, but safe intermediate commits:
- C3-A: correct permanent operation receipts, error/filters, npm advisory classification; targeted MySQL & HTTP regression. Hard stop if ledger correction cannot be safe.
- C3-B: all 11 missing source routes, shared transactions, image and consent privacy, new Flyway migration, protected CRUD; focused domain + tenant tests.
- C3-C: real expiry/notification simulator and official stub protocol, complete 28-route Newman and final Java21/MySQL/React/Java8/CLI Playwright CI+portable packaging. No C4 view redesign or C5 cloud.

Use one primary writer/integrator; optional limited real helper only for read-only route/stock audit or package advisory test work if truly supported/beneficial. Targeted file reads/compilation, one final full gate rather than repeated whole builds. Preserve source, no secrets/database archives/cache/build binaries in Git; exact test scripts reusable on isolated disposable MySQL 8 with separate runtime (append-only) and migration credentials. Do not run destructive operations on user-owned/MySQL or external accounts. No public boot2 runtime.

**Required report:** \`evidence/C3/PHASE_REPORT.md\`: baseline exact SHAs/PR ancestry, 21-file prior C2 audit closure with F01–F12 statuses, route inventory 26+2 with exact live tests and negative cases, movement/replay retention vs 24h HTTP key separation, contact encryption/fail-closed evidence, V4 checksum/strict migrations, actual Java21 and Java8/Jersey/MySQL/Javax/JPQL/Retrofit @Scheduled proof, npm audit package-by-package and production exposure, MySQL DB reconciliation+concurrency+rollback+restarts, 28-route dynamic Newman counts and CI jobs, security/telemetry/storage limitations, B full UI still not complete, source A K unchanged, final local SHA=verified remote SHA, PR URL, next C4 dependencies.

**Git process mandatory:** inspect \`git diff --check\`, all changed paths and secret/caches. Stage ALL legitimate code/tests/safe docs/migrations/Postman/CI, commit/push to new C3 feature branch, verify \`git rev-parse HEAD\` equals remote \`git ls-remote\`, open/update draft PR against latest C2 implementation branch if unmerged; no force/auto merge/tag/Canvas/publish/deploy/paid trial/real SMS.

**Verdict exactly one:**
- \`C3 VERIFIED — 26+2 COURSE ROUTES/IMAGE/SCHEDULER/PROTOCOL LIVE, C2 HARDENING PASS; READY FOR C4 REVIEW\`
- \`C3 CONDITIONAL — EXACT FAILED/UNTESTED/SECURITY GATES\`
- \`C3 BLOCKED — MATERIAL COURSE/LEDGER/PRIVACY FAILURE\`.

**STOP after C3. No C4 UI rewrite, C5 public hosting, real provider/send or student submission.** Begin implementing the stated code/tests now; do not spend this cycle on a new general research phase.
