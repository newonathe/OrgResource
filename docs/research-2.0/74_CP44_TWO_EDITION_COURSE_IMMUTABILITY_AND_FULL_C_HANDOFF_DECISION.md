# CP44 — Owner-authoritative two-edition final delivery: course-complete first, full C built on top

> **CP45 INDEPENDENT CRITIQUE / NO MORE GENERIC RESEARCH (2026-11-16):** [Final four-pass targeted check](76_CP45_FINAL_TARGETED_PRE_CODEX_VERIFICATION_AND_RESEARCH_STOP.md) verified Twilio course-slide Virtual Phone, separated A's **immutable finished artifact** from B's potentially necessary **reviewed source adaptations** and B's **independent executed academic parity**, and identified submission/Dec5 contingency. Byte-identity of every *B* file is **not** automatically mandated by the user's requirement that the already finished *A* not be changed; original behavior parity and transparent changes are non-negotiable. A remains independently runnable even if B fails modern-runtime proof. T2 provider demo needs permitted real Twilio account; local FakeTwilio is not such proof. The remaining validation requires real code execution, provider permission or student action, not another speculative planning pass.


**Date:** 2026-11-16 Asia/Manila. **USER DECISION (verbatim meaning):** Choose **full Option C** while keeping **FULL compliance** with all actual course lessons, the supplied syllabus and the ESP final-project specification, **including older course technology when necessary**. Prepare **TWO deliverables**: (A) a self-contained, fully finished strict course version and (B) the full extended C version that **builds on, does not alter or invalidate, the finished course version**. The user intends to explain both in their submission. **This explicitly supersedes** prior plan's "only one submitted final artifact", "modernize all course modules before course final", and "production acceptance before academic final" order. The broader product can still offer ONE coherent user-facing experience; there are now TWO explicit release editions, not competing interpretations of course compliance.

**Authority:** 3-page `09_ESP_PROJECT_ASSIGNMENT.pdf` and 9-page `08_CSCI180_SYLLABUS.pdf` govern course requirements. Original 12-page `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` governs the particular proposed academic workflows (subject to traceable amendments); actual O14 five entity classes, 26 Jersey handlers and eight React screens are code precedent. Later CP31–CP43 are design/research material; this user's new two-edition instruction supersedes any contradictory statement in them. No extra imagined Canvas rubric/instructor waiver.

**Status:** **PLANNING/HANDOFF ONLY.** No code, compilation, integration test, Git branch merge/tag, Windows local folder write, live Twilio send, cloud deployment or Canvas submission was performed by this decision document. No student-authored video, signed authorship certificate or instructor approval is inferred.

## 1. Nontechnical end goal

**Version A — OrgResource Course Final:** a working, portable, demonstrable local app for one fictional student organization: departments plan projects; projects state needs; available physical supplies/equipment are requested, approved, issued, returned/consumed; shortages and due reminders are shown. The original five connected entities, GET/POST API, validation, database, Retrofit/Twilio and schedule, complete Postman, Eclipse-friendly source and image receive/serve meet the printed materials. It is **not** an Internet-facing service; original Java8/Boot2/Jersey2/Javax implementation may remain if real tests show compliance.

**Version B — OrgResource Extended (full C):** take A as an independently verifiable ancestor and add real login, fictional organization workspaces, five roles, member invitations/recovery, auditable inventory, concurrency/replay protection, an expanded UI, secure v2 functions and verified release/backup arrangements, without turning the class version into a Jakarta-only, unavailable or non-running requirement. Full C **does not replace, rewrite or downgrade A**; any changes necessary for the enhanced runtime occur only in B's separately owned extension code/branch after freeze, with an explicit diff/regression report. Prefer genuinely additive modules and adapters; if integration requires modifying protected course source or running an unsupported public legacy runtime, **STOP for user review instead of silently doing it**.

## 2. Git and artifact topology — ONE lineage, TWO deliverables

**Existing GitHub source fact:** private `newonathe/OrgResource`; draft [O14 PR#8](https://github.com/newonathe/OrgResource/pull/8), academic SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`; draft [research PR#7](https://github.com/newonathe/OrgResource/pull/7); `main` earlier source. Do NOT assume PR#8 or PR#7 is merged or treat `main` as current O14.

**Proposed, NOT yet created Git refs (must only be created by authorized Codex later):**
1. From verified O14 source, create `feat/course-final-strict-2026` (or collision-free equivalent). Complete academic gaps here without adding C-only accounts/tenants/18-table schema/unsupported runtime. Preserve original O14 operations and design.
2. Once and **only once** A passes actual academic tests, freeze reviewed source SHA and a dedicated immutable release artifact and hash manifest. Suggested annotated ref `orgresource-course-final-2026` (**name only; no tag exists yet**). Protect freeze from force rewriting and changes.
3. Branch `feat/orgresource-c-extended-2026` **from the exact frozen course-final source SHA**, not older main or a speculative unrelated implementation. Add C-only modules/assets/config. Keep A's source/archive portable and independently runnable; introduce an explicit `course_baseline_manifest.json` with A commit SHA, SHA256 artifact hashes and the original course file-path map.
4. During C, enforce `git diff` of original course paths against frozen A: expect none if strict append-only composition is feasible. If a change in B to an A-owned path is genuinely unavoidable, **never rewrite A**; record exact reason/old/new behavior, seek owner authorization, and prove all A tests still pass on the untouched A artifact and the matched C behavior. Do not lie that byte identity holds when it doesn't.
5. The **extended production Internet service may NOT publicly expose** unchanged Boot2.1 course endpoints. If one modern supported JVM cannot truly execute required literal javax stack and original 26 behavior, consider one coherent product with private/internal adapters or another supported design **only after experimental proof and owner adoption**. A's academic jar can always remain local and separately runnable. Source ancestry alone does **not** establish that C fully executes A's course stack: C must have its own explicit course-compatibility test matrix and label limitations.

**Release A package:** `OrgResource-Course-Final` (source export incl Eclipse metadata / Maven wrapper, executable jar, complete verified frontend source if applicable, Postman collections+environment using fictional data, clean-install guide, actual requirement-to-test matrix, screenshots/results, integrity manifest SHA, course-aligned proposal addendum if needed, student-only demo/COA checklist).

**Release B package:** `OrgResource-Extended-C` (separately runnable full enhanced code, original frozen A source/release reference and integrity manifest, source ancestry/diff, additive feature specification, new Postman/API maps, migrations/rollback, security/tenant/ledger/backup/host evidence, exact tested app SHA, installation/demo instructions, explicit course parity report, external test/cost limitations and conditional services).

**Submission packaging:** provide **two clearly named independent artifacts** plus one concise readme differentiating them. If Canvas supports only one upload, package them in one clearly labeled outer ZIP with two distinct subfolders; if separate attachments or links are supported, the student may submit both. Do not assume Canvas accepts two **separate grading submissions**, upload counts, special bonus, instructor approval or deadline extension without observed UI evidence. The user submits and retains actual receipt. No real Git tags/releases exist merely from this plan.

## 3. Strict A / course-compliance checklist — NO modern-vendor detour required

These are features of the **class requirement and its particular proposal**, NOT a requirement to upgrade Java8/Boot2 to current releases:

| ID | Authority and feature | Test/criterion for marking finished |
|---|---|---|
| A01 | Assignment p1: meaningful student-organization service, group 1–3; proposal pp1–12: original named 5 domain concepts and whole-batch workflows | Honest fictional problem and group roster; real Department→Project→Requirement→Allocation workflows |
| A02 | Assignment p1, syllabus p2: Jersey REST, JSON GET/POST, `@Autowired` Controller→Component, JPA/Spring Data/Hibernate/MySQL, JPQL | Compiled actual five controllers, five core JPA entities, repositories and query tests, real MySQL source/row evidence |
| A03 | Assignment p1 / syllabus CLO7: literal **javax.validation** | Run valid/invalid DTO + JPA entity cases and clearly distinguish explicit bean validation from automatic ORM event validation; never substitute Jakarta-only and call it Javax |
| A04 | Assignment p1: >=5 justified entity types, real runtime create/read/delete for ALL | Five domain families individually demonstrated create/read/eligible physical delete with appropriate protected-history rejection; zero data imported from an unauthorized actual organization |
| A05 | Proposal pp1–10 and source O14: 25 explicit operations + A09 revocation = **26 actually existing** business handlers | Keep all 26 actual annotated Jersey handlers, original request/response/validation/error meanings and tests (unless exact justified source correction); no mock endpoints |
| A06 | Original proposal p10: hourly scheduled due reminders; assignment "at the very least Twilio", syllabus CLO8 Retrofit/Gson | Real scheduled service and Retrofit/Gson loopback FakeTwilio test; **also implement actual official Twilio Messages REST adapter**, DISABLED by default, tested contract/status/error via safe local fake provider endpoint; if permitted authorized official test credentials/Virtual Phone are available, prove no-delivery official acceptance separately. **Do not claim an actual Twilio provider request passed without real provider evidence.** No real number or paid SMS without explicit approval. |
| A07 | Syllabus p2 CLO9 and p3 image-to-database teaching unit: receive/serve images | Small authentic image upload and byte-for-byte HTTP GET with Content-Type and size/MIME safety, stored via MySQL; preferably attach optional image bytes/metadata to **Resource** without introducing an unnecessary 6th business entity. Additional image GET/POST operations are real and added to Postman (do not miscount them as original 26). |
| A08 | Assignment p1/pp1–2 Postman and full Word proposal details | Postman covers EVERY existing original plus new image and any other REST operation; proposal/spec addendum accurately lists names, inputs/outputs/errors, components, entities, validation and repositories; correct links |
| A09 | Syllabus CLO1/2 Eclipse/Java project organization | Portable Java/Eclipse source with real independent import+debug or clearly marked NOT_RUN; no fabricated Eclipse screenshot |
| A10 | Assignment final Dec5, recording/peer/email, optional UI bonus; syllabus p6 signed COA and assistance attribution | Actual local execution, source deliverable, genuine student video/discussion/link, student-signed truthful certificate, outside/AI help acknowledged, peer reviews ONLY of actual other group members, real Canvas receipt. None are produced automatically by ChatGPT/Codex. |
| A11 | Existing academic O14 reported tests/known flake | Rerun Java/MySQL tests, frontend tests, browser journeys, Newman, empty-schema first-run; investigate known ResourceListAggregationMySqlTest expected5 actual8 intermittent CI, no skipped failures; pin actual commands/environment SHA and resulting outputs |
| A12 | Local course environment vs public security | JDK8 or actual source-compatible Java stack acceptable if course code is truly executed; **bind loopback only**, separate fictional schema and no real SMS, don't publish obsolete Boot2.1 to Internet |
| A13 | Original proposal exact rules and meaning | Whole-batch returned/consumed exactly once, correct PHT timestamps, never overdraft stock, create/read/delete valid states; document original proposal vs O14 scheduler expiry and A09 amendments transparently |

**Course disclaimer:** the syllabus's project CLO number mapping differs from its explicit image CLO, and the printed project spec doesn't list image upload explicitly. This A plan **includes** it conservatively to ensure all taught features. The exact assignment mentions Twilio but doesn't explicitly require carrier delivery; a protocol fake **alone** cannot be claimed a real provider request. If provider-side authorization/evidence unavailable, label official Twilio proof BLOCKED, not secretly claim FULL PROVIDER TEST PASSED. These honest evidence distinctions do not prohibit authoring a complete provider adapter.

## 4. Full C / additional product acceptance — after A freeze, without contaminating A

The user explicitly selects the full approved C goal in the [E01–E39 change/exclusion register](73_PRE_CODEX_S0_S2_IN_CHAT_SCOPE_AUTHORITY_AND_ALL_EXTRAS_REGISTER.md). These are **user-selected additional product features**, NOT instructor mandates.

- **Multi-org safe identity**: Login/logout, session and CSRF, local credentials, one-use recovery codes, no fabricated Ateneo account verification, invitations, ACTIVE org/membership and first-owner bootstrap.
- **Governance**: Five roles OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER, member management, assigned lead/borrower lookup, one pending ownership transfer, last-owner protection, revocation/suspension.
- **Domain continuity**: ORIGINAL five domain types and all original 26 source operations remain executable and testable as part of C's protected **course-compatibility acceptance**, not merely copies in ZIP. Original source behaviors retain distinct response contracts where modern safety requires adapters; no public anonymous old Jersey API.
- **Transactions**: Physical T/H/I inventory, global holds and whole-batch release, stock movement permanent operation IDs and immutable receipts, tenant-aware SQL, concurrency/idempotency, timestamp handling, no partial/lost/damaged returns in P0.
- **Public API**: additional secured `/api/v2` with UUID/UTC/version/ETag/CSRF, typed errors, originals preserved as protected `/api`. Justified support tables and explicit migrations; **18 physical tables remains candidate not course mandate**.
- **Connected UI**: preserve existing eight-screen look/meaning, add Login, Invite, Workspace, Members, Account, Activity/Audit, Exports and stock-photo/new inventory operations; no mock-only "success" screens.
- **Twilio/image**: all A tests intact, real course-compatible image receiving/serving, local FakeTwilio and guarded official Twilio transport; no false delivery status.
- **Operations**: staged CI/CD, actual real-MySQL, Newman, browser and security tests, conditionally eligible $0 host, strict no-paid-service without approval, least-privileged secrets, independently verified encrypted backup+restore, reproducible release.
- **Real-world boundary**: fictional group only until real permissions; no campus SSO, university asset owner claims, enrollment verification, interviews or measured adoption.

**C fail-stop triggers:** any regression of A's verified course semantics, unreachable 26 real controllers, cross-org read/write, ledger inventory mismatch, duplicate physical stock mutation, unsupported Javax/Jakarta interoperability, obsolete Boot2 publicly exposed, unapproved charges, provider messages to real numbers, missing clean restore, false provider/Canvas evidence, or inability to produce two separate runnable artifacts. Report exact blocker to owner in this chat and keep A deliverable protected. Never claim "C is also fully course compliant" merely because A is included as a PDF/source copy.

## 5. New implementation sequence — supersedes old production-first course-last stages

**PLANNING DONE IN CHAT**: S0 direct source audit, paper S1 compatibility candidates and S2 design/route/domain specifications. That is design, NOT runtime PASS.

**Only after owner explicitly hands the final dual-build package to Codex:**
- **F0 — pin source:** independent O14 SHA, course docs, 26 route source, current actual tests, controlled safe Git branch. No main merge or local data changes.
- **A1 — finish course code first:** address known O14 flaky SQL/date issue, implement minimum image DB/upload/serve, official Twilio REST adapter with disabled default, thorough course/lab/assignment compatibility. DO NOT modernize course Java8 just for fashion; no production accounts/tenants/ledger detours.
- **A2 — full course acceptance:** run actual real MySQL/Javax/JPQL/Jersey all endpoints, all 5 eligible CRUD/delete, Retrofit+Gson FakeTwilio+provider contract, image bytes, scheduling, Postman/Newman, React, independent clean export and demo; no missing course case, no artificial PASS.
- **A3 — freeze independent Version A:** immutable source SHA, tag/artifact/manifest, runnable source+JAR+Postman+demo guide and truthful course matrix; **stop if course proof fails**. This first acceptance gate supersedes earlier production-first chronology.
- **C0 — actual extension compatibility spike:** after A freeze, new B branch from exact A SHA, test supported Java/Jersey/JPA/Javax bridging and UI in bounded isolated disposable runtime, **without touching A**. On fail, return design evidence to ChatGPT and stop C build, leaving A ready.
- **C1–C6 — implement C increments:** supported auth/org scoped DB; stock ledger and concurrency; compatible 26 route bridge + new v2 + Twilio and image; new connected UI; hardened release/backup; complete negative/full tests, per-increment PR and course parity matrix.
- **C7 — independent C production acceptance:** verify course matrix **AGAIN on C** with actual executable 26 routes, real class technologies and image + Retrofit, plus role/org/ledger/backup tests. Never label C full compliant without actual proof.
- **C8 — freeze Version B:** independent artifact+SHA and A-linked preservation report; avoid fake 24/7/zero-cost guarantee.
- **S10 student-only:** two clearly labeled artifacts/README/optional combined ZIP as actual Canvas allows, truthful student recording/COA/outside assistance/peer and actual Canvas receipt.

**Milestone approvals:** one Codex master build can contain these phases, but it MUST stop on A3 failed acceptance, C0 interoperability failure, course-file mutation necessity, real provider use, incurred fees, hosted deploy or final merges. Do not demand 14 separate research prompts for routine phases; do not auto-assume permission on dangerous action.

## 6. Submission statement — DRAFT for student to verify, not an instructor approval claim

> **OrgResource — Two-Version Submission.** I am providing (1) **OrgResource Course Final**, an independently runnable implementation focused on the required CSCI 180.05 syllabus, ESP project specification, and original OrgResource proposal; and (2) **OrgResource Extended**, a separately runnable, expanded version built from the verified course-final source baseline. The extended edition adds optional features, while the original course edition remains preserved, testable and available for assessment. I have included separate source/build instructions, Postman documentation, evidence reports and a change comparison to identify the required functions and the additional features. The extended version is supplemental and is not intended to replace any required course functionality. Any external tools or AI assistance are identified in the accompanying acknowledgement and Certificate of Authorship documentation.

This is **future-facing copy**. Do NOT use the past-tense statements "I am providing"/"I have included" until both actual deliverables and attachments exist and have been checked. Do NOT invent instructor permission or imply two separately graded attempts. Each submission must include a truthful personally completed authorship certification (syllabus p6). The original proposal lists one member; if this remains solo, peer-review-other-members is inapplicable, not fabricated.

## 7. Decision register and present status

| Decision | Status |
|---|---|
| Two deliverables, Course Final then full Extended C | **USER APPROVED, current directive** |
| Course-compatible/older framework allowed and literal academic technology mandatory | **USER APPROVED** |
| Course source/release freeze and no silent course regression | **USER APPROVED** |
| Full C product additions E01–E37 (with E38–E39 expressly excluded/deferred) | **USER APPROVED at goal level** (individual safety/hosting/account constraints remain) |
| Independent full C runtime / one vs two internal processes | **DESIGN HYPOTHESIS**, requires C0 experiment; no proven selection |
| Source A fully compliant/runnable and frozen | **NOT BUILT/NOT VERIFIED YET** |
| Source C truly preserves execution of academic code | **NOT BUILT/NOT VERIFIED YET** |
| Two artifacts actually packaged/submitted | **NOT DONE** |
| Course Twilio provider-side request/account proof | **NOT VERIFIED** |
| Actual original O14 local compiled test history | **HISTORICAL REPORT**, new end-to-end checks NOT RUN |
| Final date Dec 5, 2026 | **COURSE WRITTEN DATE**; do not infer extension |
| Code, branch, tag, merge, hosted deploy, paid provider, Windows sync | **NONE PERFORMED BY THIS DECISION PASS** |

**Only coherent final answer after Codex handoff is two independently runnable releases with a demonstrated ancestor/preservation relation, separate verified evidence and honest course mapping, not a promise or two paper-only mockups.**
