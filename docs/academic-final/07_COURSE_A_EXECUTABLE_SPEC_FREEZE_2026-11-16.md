# OrgResource Course Final A — executable academic specification and measured acceptance gates

**Authority/date:** 2026-11-16 Asia/Manila. **Status:** REQUIREMENTS RECONCILED / IMPLEMENTATION NOT PERFORMED BY THIS FILE. **Scope:** Course Final A only, then an independent freeze. Extended C follows from A's exact source revision on a new branch. This document narrows execution; it does not invent instructor approval or replace the source-derived academic references.

## 0. Read order, precedence and source protection

1. Original \`09_ESP_PROJECT_ASSIGNMENT.pdf\` pp.1–3: actual final project specification.
2. Original \`08_CSCI180_SYLLABUS.pdf\` pp.2–4, 6: CLOs, rubric, submission and academic integrity.
3. Historical \`10_ORGRESOURCE_CONCISE_PROPOSAL.docx\` pp.1–12: FIVE proposed entities and TWENTY-FIVE original operations, method steps, fields and workflow. Actual implementation amendments are not retroactively attributed to the proposal.
4. Current frozen **reference**, not frozen completed release: O14 \`feat/o14-academic-final-acceptance-2026-11-15\` at \`2dd336ed39cecd417a8bce3388b303e688e95dee\`, open PR#8. Re-audit for a newer head before work. Do not branch from \`main\` O1.
5. Current canonical \`docs/academic-final/00\` (read first), \`01\` (written matrix), \`02\` (26 operations/domain), \`05\` (EERD/repos/method steps), \`03\` (acceptance), \`04\` (efficiency), \`06\` (source QA).
6. Latest user/research decisions: \`docs/research-2.0/74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md\`, \`77_CP46_FINAL_GITHUB_HANDOFF_RELEASE_INDEX.md\`, \`78_CP47_FINAL_NEXT_ACTIONS_RESEARCH_CLOSED.md\`.

**Supersession:** Academic pages originally authored as “ONE final product” are inherited source-coverage material; their statements that modern C must be done before academic acceptance are **not binding**. The newest explicit USER DECISION is **A fully finished and independently runnable first, Extended C built from immutable A second, student Canvas action last**. This is two editions in one source lineage, not two different course projects. Earlier one-runtime modern S1-first prompts are superseded for A.

**Source-vs-addition labels:** [CR] assignment requirement, [SF] syllabus learning outcome/fact, [PD] project-specific historical proposal, [UD] user-selected conservative inclusion, [EV] actually existing O14 source, [NEW] to implement, [NOT_RUN] execution not observed, [STUDENT] human-only. Use these everywhere.

## 1. Course-compliant A: fixed functional boundary

**[CR]** Actual Java/Spring Boot, five justified JPA entities, MySQL/InnoDB/Hibernate and Spring Data repositories with JPQL; actual Jersey \`@Path\` JSON GET/POST, Controller delegates to \`@Autowired\` Spring Component; genuine \`javax.validation\`; Retrofit + Gson external JSON/HTTP (assignment spells “RetroJit” while syllabus CLO8 uses Retrofit/Gson); scheduled expiry/reminder behavior; runtime create/read/delete for each of the five original business entity families; importable Postman definitions for **every** implemented REST call.

**[EV/PD]** Keep Department, Project, Resource, ResourceRequirement, Allocation; existing 26 Jersey handlers = D01–D04, P01–P04, R01–R05, Q01–Q04, A01–A09. The original proposal has 25; A09 privacy withdrawal is a separately traceable later amendment. There are 20 POST/6 GET. Preserve numeric IDs, O14 PHT-local JSON \`YYYY-MM-DDTHH:mm:ss\`, typed error \`{code,message}\`, structured DTOs and no raw phone in read responses.

**[SF+UD/NEW]** Real HTTP resource-image reception, DB storage and serving as a **separate two-operation extension**, not as part of the original 26. Real Twilio Messages API-compatible Retrofit/Gson transport **alongside** the existing working FakeTwilio simulator; protocol tests are mandatory, provider-level tests only with explicit owner authorization and an eligible no-fee account. No real carrier SMS is necessary or authorized by this document.

**[EV]** Preserve the eight actually working React Academic Glass screens, theme, responsive/keyboard/reduced-motion behavior and same-process Java static bundle. Course A remains a local/loopback-only demonstrator; do not publicly expose Java8/Boot2.1.

**[UD]** No extra Organization/Membership/Session/ledger schema in A, no speculative identity migration, no future-slot booking/partial returns/finance/QR, no cloud signup/paid service. All C extensions begin only after verified A freeze.

## 2. Data contract and actual end-to-end calculation oracles

**Five tables/EERD:** \`department(id,name,name_key)\`; \`project(id,department_id,name,start_at,end_at)\`; \`resource(id,name,name_key,type,total_quantity)\`; \`resource_requirement(id,project_id,resource_id,quantity)\`; \`allocation(id,requirement_id,quantity,borrower_name,phone_number,sms_consent,status,created_at,approved_at,issued_at,returned_at,consumed_at,cancelled_at,cancellation_reason,reminder_claimed_at,reminder_outcome,reminder_http_status,provider_message_sid)\`. Consult exact O14 entity Java annotations and canonical \`02\` and \`05\` for type/length/nullable metadata; do not generate SQL exclusively from this shorthand. Non-core image columns in \`resource\` may be nullable. **No sixth business entity needed** for A images.

**Physical T:** \`Resource.totalQuantity\`. **Effective H:** APPROVED q with parent \`Project.endAt > sampledNow\`. **Outstanding I:** REUSABLE ISSUED q, including overdue. Availability REUSABLE = T−H−I; CONSUMABLE = T−H. Requirement coverage C = effective APPROVED + ISSUED + RETURNED + CONSUMED; shortage=max(0,Q−C). Do NOT store duplicate available/covered totals.

**Period:** use Philippine clock and one sampled instant per logical computation; \`startAt <= now < endAt\` active; approval can occur before start if project end is still in the future; issue/consume only active; return late permitted. An ended APPROVED hold stops reserving *before* scheduler cleanup; this is a documented O14 amendment to the initial proposal.

**State:** PENDING no hold; APPROVED reserves once; REUSABLE issue moves H→I without physical stock deduction; full usable return moves ISSUED→RETURNED and releases I; CONSUMABLE consume reduces physical T exactly once and marks CONSUMED in same transaction; cancel PENDING/APPROVED only; scheduler marks ended open allocations CANCELLED/EXPIRED; ISSUED not auto-returned. Never delete historically approved/issued/consumed records. A09 revocation idempotently clears \`phone_number\` and consent, no stock effect.

**Reusable oracle:** T10 requirement Q6, pending q4 availability10/covered0; approved availability6/covered4; issued availability6/covered4; returned availability10/covered4. **Consumable oracle:** T100 requirement Q40 pending q30 availability100/covered0; approved available70/covered30; consumed physical T70/available70/covered30. Duplicate consume/return -> 409 with unchanged DB. Competing q4 approvals vs T5 -> exactly one success; effective hold ending exactly now absent; overdue issued still blocks stock.

**Original EERD:** Department 1→N Project; Project 1→N ResourceRequirement; Resource 1→N ResourceRequirement; ResourceRequirement 1→N Allocation. Unique normalized department/resource names; unique (project_id,resource_id), foreign-key constraints, historical delete guards, no cascading historical loss. See canonical \`05\` for exact 27 custom repository method signatures.

## 3. Exact operations and HTTP contract

**Original 26:** 
\`\`\`
D01 POST /api/departments              D02 GET  /api/departments
D03 POST /api/departments/{id}/update  D04 POST /api/departments/{id}/delete
P01 POST /api/projects                 P02 GET  /api/projects
P03 POST /api/projects/{id}/update     P04 POST /api/projects/{id}/delete
R01 POST /api/resources                R02 GET  /api/resources
R03 POST /api/resources/{id}/update    R04 POST /api/resources/{id}/delete
R05 GET  /api/resources/{id}/availability
Q01 POST /api/requirements             Q02 GET  /api/requirements
Q03 POST /api/requirements/{id}/update Q04 POST /api/requirements/{id}/delete
A01 POST /api/allocations              A02 GET  /api/allocations
A03 POST /api/allocations/{id}/approve A04 POST /api/allocations/{id}/issue
A05 POST /api/allocations/{id}/return  A06 POST /api/allocations/{id}/consume
A07 POST /api/allocations/{id}/cancel  A08 POST /api/allocations/{id}/delete
A09 POST /api/allocations/{id}/revoke-sms-consent
\`\`\`
Use **exact request/response** and Component names from \`02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md\`; this concise list does not replace its complete field/path/error table. Success create=201, read/update/action/delete=200, returned JSON; invalid input=400, absent=404, domain conflict=409, applicable MySQL transient lock errors=503, sanitized unexpected failure=500. Do not retry non-idempotent unknown-outcome POST blindly. O14 extra CAS \`expectedTotalQuantity\` on Resource update and phone privacy remain actual amendments. Avoid deleting legitimate zero-stock Resource only to claim all-entity CRUD; distinguish eligible deletion from protected-history refusal.

## 4. New IMAGE-A academic interface [UD interpretation of SF CLO9]

Chosen minimum A interface, supplement to 26:
- \`POST /api/resources/{id}/image\`: multipart/form-data field **file** only, no arbitrary URL or filesystem filename. Successful 200 JSON \`{resourceId,contentType,sizeBytes,sha256}\` (hash for response/test may be derived; no redundant persisted hash necessary). Reject invalid IDs/missing Resource 404; missing/empty file, unsupported MIME/magic, mismatched type, SVG, truncated content or >2,097,152-byte payload -> controlled 400 or 413 as documented.
- \`GET /api/resources/{id}/image\`: success 200 **raw bytes** with \`Content-Type: image/png\` or \`image/jpeg\`, no JSON wrapper; 404 for absent resource or absent image; safe no-cache, no path traversal/remote fetch.
- Persist **optional** image bytes and validated MIME as columns on existing Resource JPA table; no new entity, no filesystem path and no original API path replacement. Use MySQL column type actually capable of >=2MiB (e.g., properly tested MEDIUMBLOB rather than accidental 64KiB BLOB); avoid logging bytes/names.
- Jersey 2.26 compatible multipart feature/dependency registered once; use bounded input streaming before storing; validate PNG/JPEG magic as well as declared media type. Test 70KiB+ and 2MiB boundary, direct SQL existence, exact SHA-256 of retrieved bytes, repeat replacement, restart persistence and invalid input. **Course A is strictly loopback/fictional:** no public upload or real private images; Extended C later applies tenant auth and upload limits in its own runtime.

If the existing course-source image example offers a different compatible mechanism, retain these observable HTTP/data/security outcomes and document the specific source-grounded implementation adaptation; no new product architecture research.

## 5. New TWILIO-A academic interface [CR+UD]

Preserve existing Retrofit/Gson FakeTwilio loopback HTTP and scheduled claim/dispatch/outcome path; no real send by default. Implement separate official Twilio Messages REST protocol transport:
\`POST /2010-04-01/Accounts/{AccountSid}/Messages.json\` as \`application/x-www-form-urlencoded\`, \`From\`, \`To\`, \`Body\`, HTTP Basic auth from injected configuration only, timeouts and JSON SID/error parsing. No real account, phone, provider URL or credential in source/test logs. Local protocol test server distinct from FakeTwilio verifies path/auth/form/status/timeout/unreadable response, ambiguous outcome no auto-replay, and durable truthful outcome classification. Mandatory configured mode defaults to disabled; simulator outcome != provider acceptance != handset delivery. Authenticated official provider test or Virtual Phone remains **BLOCKED/NOT_RUN** without separately authorized account; do not procure one or claim cost-free SMS.

## 6. Scope-complete QA and deliverables

**Gate A1 (engineering):** verified missing image and Twilio adapter implemented, O14 regression existing business behavior unchanged, deterministic clock/aggregation failure investigated with actual MySQL evidence (older same-SHA test expected 5 but once got 8; no assertion weakening/skip). Run focused Java8 and real MySQL tests, provider stub test, image actual bytes test. Stop on material new regression.

**Gate A2 (acceptance):** actual Java8 Maven verification, real MySQL schema+FK+JPQL and rollback, dynamic-ID Newman requests for **all 28** current A operations (26 original + 2 images), full five entity eligible create/read/delete and negative protected history, whole-batch inventory concurrency, scheduler, FakeTwilio, Twilio protocol stub, React eight pages/mobile/light-dark and Playwright, clean source ZIP extraction and executable JAR HTTP/HTML/images, persistence restart. Each row has test name, expected, observed, SHA, command, status; distinguish SOURCE_VERIFIED, REPORTED_PASS, PASS, FAIL, NOT_RUN, BLOCKED.

**Gate A3 (separate release authorization):** only AFTER A2 truly passes, make immutable \`OrgResource-Course-Final\` source revision and independently runnable JAR+complete source ZIP+Java/Eclipse source ZIP+all Postman collections and environment sample+SHA256SUMS+actual as-built Word proposal addendum+authorship/recording checklist. No tag/merge/Canvas automatically; user review needed. A image/Twilio adapter is part of A evidence. Extended C branch then starts from exactly A's frozen Git SHA; never mutate its release.

**Student-only:** actual recording accessible to the course instructor (or qualifying actual early presentation), authentic group roster and peer-review responsibilities, truthful student-signed Certificate of Authorship identifying AI/research/Codex/outside help, final Canvas submission and receipt. Printed 2026-12-05; do not invent extension, acceptance, activity, signature or grade.

## 7. Quality/efficiency for FREE Luna Codex implementation

- Codex only builds, tests, debugs, packages and pushes. Research ChatGPT owns research/external verification. Prompt-planning chat owns the next build scope and checks findings.
- Quality/correctness/security/completeness are hard constraints. Optimize **working tested software / tokens, wall-time, tool calls and storage**; choose dependency-coherent medium/large batches, not one-annotation micro-turns or unrestricted multi-subsystem rewrite.
- Use OPTIONAL subagents only if the **actual free Codex workspace supports them**; never assert they exist. One primary writer/integrator. Suggested at most two bounded helpers: low/medium read-only static route/test audit, medium isolated image/Postman test reviewer; primary/highest available effort handles provider transport and concurrency. No conflicting edits, same-source shared POM ownership or duplicated full-suite runs.
- Each helper receives exact SHA, tiny file list, test/expected results, no cross-scope edits, concise output. If unavailable or overhead exceeds task, single-agent build is optimal.
- Read root AGENTS + 00/01/02/05/03/04/this 07 and the 5 directly affected source families **once**; don't reread 1.2MB research tree or 319k historical master without a specific contradiction. Focused tests during edits; one full relevant acceptance after.
- Safe Git every task: fresh remote/local branch+PR audit, preserve dirty/unrelated work, isolate feature branch from correct O14 head, no reset/force/auto-merge; stage all legitimate code/tests/docs, exclude secrets/DB/target/dist/node_modules, commit+push, verify remote SHA and create/update PR. No public legacy deployment, real SMS, paid service, destructive non-test schema operation or Canvas action.
