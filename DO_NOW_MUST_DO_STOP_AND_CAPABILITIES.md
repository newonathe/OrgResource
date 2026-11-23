# OrgResource — Do NOW, MUST DO, STOP DOING, and what ChatGPT can ACTUALLY execute

**Fresh practical reassessment: November 23, 2026, Asia/Manila.** This is a decision/action document, not another redesign. It independently prioritizes verifiable deliverables and execution access. Use [CURRENT_STATUS_AND_NEXT_ACTIONS.md](CURRENT_STATUS_AND_NEXT_ACTIONS.md) for the exact operational gates, the [Northflank runbook](docs/deployment/P0_NORTHFLANK_MANUAL_JOB_COMMAND_2026-11-23.md) for executable host commands and the [course source pack](02_MASTER_CONTEXT_DOSSIER.md) / local original assignment and syllabus for actual requirements. **Re-fetch GitHub and check Canvas before acting; a dated file cannot prove later events.**

## The important distinction — two different finishes

1. **ACADEMIC REQUIREMENT** (CSCI 180.05): Complete and demonstrate the scoped course application, required technologies, controller/component/repository and HTTP behaviors, documentation/Postman, and actual submission/peer-review/video/authorship tasks. The course materials give a **December 5, 2026 final implementation date**, subject to current Canvas announcements. The course assignment does **not** itself require a 96-organization cloud dataset, a public multi-tenant service or Northflank.
2. **USER DECISION** (priority P0): Safely publish and accept an **existing Northflank demo** with the exact previously approved 96-organization **fictional** cohort while preserving all existing database records and login. This is a separate chosen release milestone, with operational prerequisites that CI cannot waive.
3. **DESIGN RECOMMENDATION**: Treat DEC-D01 department affiliation/catalog/request workflow and further C8 authorization hardening as later product work, unless a **verified course requirement or demonstrable P0 security defect** compels it now. The existence of a proposal does not imply acceptance or deployment.

**DO NOT collapse the three into an undefined “everything is complete” status.** The current actual hosted result is **`LIVE_IMPORT_NOT_RUN`**.

## 1. What is already DONE — do not redo it without evidence of a defect

- **VERIFIED FILE EVIDENCE:** The original approved `fictional-events-v1.local.json` was recovered by the owner and independently checked in ChatGPT against **19,265,850 bytes** and SHA-256 `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`. It contains the planned 96 organizations, 248 departments, 713 projects, 1,312 resources, 2,139 requirements, 462 synthetic personas, 21,060 allocations and 67,370 historical events. This is **not** a statement that the file is staged on Northflank or available automatically to Codex. See the [safe dataset manifest](docs/deployment/C8_PRIVATE_DATASET_MANIFEST_2026-11-23.md).
- **VERIFIED GITHUB FACT:** Runtime release candidate SHA `9df7099145e324f326106870b1b3fd0788d5510f` passed all three jobs in [CI 45155946849](https://github.com/newonathe/OrgResource/actions/runs/45155946849), covering frozen Java 8/MySQL, React, and Extended C isolated MySQL/Newman/Playwright. The screenshot-reported sidebar wordmark fix passed the browser regression. **It is not proof that the fix is deployed.**
- **SOURCE IMPLEMENTED:** Hosted additive importer, SHA/target preflight, all-pre-existing-row V2 HMAC baseline gate, read-only postflight, small/intermediate isolated replay/negative tests and operator runbook exist in draft [PR #25](https://github.com/newonathe/OrgResource/pull/25). Their success on the **full private 96-plan** against a restored actual database clone has **NOT** been demonstrated.
- **DOCUMENTED:** public-safe manifest, handoffs, issues [#26 P0](https://github.com/newonathe/OrgResource/issues/26) and [#27 proposed departments](https://github.com/newonathe/OrgResource/issues/27). **PR #25 draft/unmerged**. Frozen Course A SHA `7f8c0746da18fd54b47f430ac1435ac36e605d67` unchanged in this release track.

## 2. DO NOW, IN THIS NORMAL CHAT — I can personally execute with available tools

| Order | Action I CAN execute here | Exactly what proves completion | Stop condition |
| --- | --- | --- | --- |
| NOW-01 | **Check current remote HEAD and CI; fetch failed job logs; use connected GitHub to rerun failed jobs, or minimally patch an actual source/configuration error on draft PR.** | Exact run/commit link and all required jobs passed | Repeated external Maven/CDN outage or evidence of no source defect; do not chase indefinitely |
| NOW-02 | **Inspect repository code, migrations, importer/preflight/postflight and actual private JSON when accessible; compare schema, IDs, totals, state chronology, owner-preservation rules and expected resource constraints.** | Concrete findings, reproduction or deterministic check, and source locations | No raw source available in execution workspace; never pretend a regex/snippet scan ran the full importer |
| NOW-03 | **Audit known high-risk code paths**: same-org and cross-org authorization, credential/error handling, concurrency/stock invariants, hardcoded secrets, public synthetic disclosure, auto-SMS off, and protected original data; add only targeted regression tests for observed gaps. | Findings and passing CI for exact new SHA, or explicit limitations | No real defect, no extra code |
| NOW-04 | **Prepare/verify the academic completion evidence checklist** against the original assignment and syllabus: Jersey REST/JSON, Spring `@Autowired`, JPA/Spring Data, Javax Validation, external client/Retrofit-GSON/Twilio demonstration level, scheduling, minimum five justified entities, GET/POST, runtime create/delete, Postman definitions, proposal and actual final demo. | Requirement → implemented file → actual evidence → remaining task table; unresolved interpretation to instructor/Canvas | Never claim a course item was submitted, a peer-reviewed submission occurred or instructor approved without records |
| NOW-05 | **Update GitHub source of truth**, README, PR/issue and a copy-ready Codex handoff; prepare safe operator commands and redacted acceptance template. | Verified GitHub commit/links | Do not append duplicative historical handoffs or publish raw file |
| NOW-06 | **Analyze files and prepare a private local-only rehearsal strategy** or sanitized test fixtures; run code only where execution tooling actually exists, or use GitHub CI for reproducible tests. | Actual test output and target environment recorded | Local unavailable environment is not a successful database run |

**REAL TOOL BOUNDARY:** In this chat I have a **connected GitHub integration** that can read/write repository files, create commits, update issues/PRs, inspect Actions logs and request reruns. I can use search/retrieval and uploaded-file inspection. I do **not** currently have an authenticated Northflank operator session or direct credentials/connection to its MySQL database; there is no available tool that transforms GitHub write access into Northflank access. Source auditing ≠ running the live system. Cloud browser *control* is a Work-mode capability, not available in this ordinary chat. No asynchronous execution after the reply.

### Immediate CI truth (2026-11-23)

- Current *pre-triage* draft-PR HEAD `9facb967b916a071c8d1e751ca6b356dc5d6e3e8`; its first [CI 45165728747](https://github.com/newonathe/OrgResource/actions/runs/45165728747) failed **during Maven 3.10.0 ZIP download** (`wget` from `repo.maven.apache.org`), before the Extended C test step could execute. React and Java 8 passed. A **targeted failed-job rerun was requested in this session**; Actions run attempt 2 was **in progress** when this document was drafted. This is an observed external download failure, not evidence that the current Java source is incorrect, and not yet a passing latest-HEAD validation.
- Previous documentation SHA `9b71fd0703cd153547abf15f5e28ccd3afb5a73f` [CI 45165646218](https://github.com/newonathe/OrgResource/actions/runs/45165646218) **passed** all three jobs. SHA `22cda4d20f88809cd0577b1c8fea0fbbb8b1335e` [CI 45164554545](https://github.com/newonathe/OrgResource/actions/runs/45164554545) **passed**. Each newly authored documentation commit requires its **own** outcome check.

## 3. MUST DO to complete the chosen 96-organization HOSTED demo — NOT possible solely from current chat

| Sequence | Critical work | Who/where | Completion receipt |
| --- | --- | --- | --- |
| HOST-01 | Confirm actual Northflank project/account, deployed Java 21 image/SHA, MySQL 8/Flyway V7, target org/owner identity, privileges, build/runtime quotas, server fingerprint, and whether web traffic can be paused | **Authorized Northflank operator** (Codex can guide/automate *if* granted access) | Redacted inventory proof, correct target and compatible image |
| HOST-02 | Review Northflank's published Oct 2026 security incident and **rotate/review actual stored credentials** as applicable; confirm dependent application still connects | **Operator** | Secret rotation/audit and successful health evidence, no secret values in logs |
| HOST-03 | Make native and separate **encrypted off-host backup**; independently restore into an **isolated** MySQL 8 instance; verify old OWNER and every original record against baseline | **Operator + access to actual DB/backup services** | Backup IDs, completed independent restore, integrity/rollback receipt |
| HOST-04 | Privately transfer unchanged **exact JSON** (verified checksum) into a sealed manual-job workspace and run **full-size all-96 rehearsal** on restored clone; measure memory, speed, database growth and failures | **Operator/Codex with clone and private source** | All 96 exact names/IDs and 248/713/1312/2139/462/21060/67370 totals validated via independent postflight |
| HOST-05 | Confirm rights to display actual directory *labels*, explicit fictional/synthetic notices, no institutional endorsement, no live personal data/SMS; approve downtime/rollback/cost before live change | **Owner** | Specific GO/NO-GO decision |
| HOST-06 | In controlled maintenance window: capture original target **V2 HMAC BEFORE writes**, run read-only preflight and exact fingerprint/plan verification; execute the guarded importer **once** | **Authorized operator** | Signed target/backup evidence, sanitized import completion record |
| HOST-07 | Independent SELECT-only V2 VERIFY + postflight, original OWNER login, all 96 workspaces, correct stock/history/events/states, denied unauthorized access, browser responsiveness and exports | **Operator/Codex with actual host** | Dated live receipts, no unexplained original-row change |
| HOST-08 | Release compatible frontend/backend deployment (sidebar included), remove temporary secret plan, confirm rollback, costs and monitoring; keep PR #25 unmerged until separately approved | **Operator + owner** | Live URL + actual image SHA + accepted release decision |

**No shortcuts:** If off-host restore or full private-plan clone rehearsal cannot be demonstrated, **NO LIVE IMPORT**. No blind rerun of a partial import. Never paste passwords/tokens/DB dumps into the public repository. A private GitHub repo for the raw file is **OPTIONAL**; a secure direct operator file transfer is sufficient if supported.

## 4. MUST DO for ACADEMIC submission — the original course is a separate track

**COURSE REQUIREMENT / SYLLABUS FACT (from the original assignment as transcribed in the supplied course corpus and syllabus):**
- Final implementation was listed for **December 5, 2026**; **student must verify the current Canvas instructions**, announcements, submission status and any approved presentation slot. The assignment identifies earlier proposal and implementation stages as well; do not falsely claim late or earlier submissions occurred.
- Show the application implementing the required course technologies and operations, its live HTTP/DB behavior, and the **Postman definition for every REST call**. Run the relevant Postman scenarios on the actual deliverable, not only against mocks. Include final export/packaging and a readable recorded demonstration/group discussion with actual instructor access as specified.
- Complete the required **student-authored** peer-review ratings/comments for other group members where applicable; a solo-group interpretation may need instructor confirmation. Supply an actual **Certificate of Authorship** and honest outside/AI-assistance acknowledgment. Do not invent signatures, peer reviews, interviews, instructor approval, Canvas submissions, or video footage.
- Review the current course interpretation of Twilio/Retrofit/GSON, scheduling and image HTTP functionality, and the approved **Course A** proof versus optional Extended C. The assignment does not require a 96-org dataset or multi-tenant cloud deployment. If hosted demo is delayed, **do not sacrifice a valid smaller course presentation**.

**ChatGPT can prepare requirements mapping, Postman test plans, narratives, README, submission checklist and review documents, and can edit source. The owner/student must actually submit, record, review and certify their own work.**

## 5. STOP DOING NOW — explicit scope cuts

- **STOP** repeatedly rewriting architecture, requirements or handoffs without new evidence; these are already documented. Maintain **one current decision page** and update observed status only.
- **STOP** adding features, dashboards, animations, UX polish or department schema/V8 during the 96-org P0 release. **DEC-D01 (#27) is DEFERRED**, not secretly implemented.
- **STOP** rebuilding/re-scraping the verified approved dataset, inventing organization names or historical results. Use the exact privately held 19.3 MB JSON.
- **STOP** asking for raw videos again or claiming video frames were inspected; timestamped transcripts are audio-only. Actual academic/demo video must be recorded/checked by the student.
- **STOP** trying to publish the private JSON, credentials, original DB backup, contacts or full plan in the **PUBLIC** GitHub repo. Safe checksum/aggregate metadata is enough.
- **STOP** treating a passing CI, a log statement or an unverified backup attestation as live hosting success. Do not auto-start import on app startup; no unnecessary scheduled retries.
- **STOP** changing frozen Course A, merging PR #25, deploying, sending real Twilio/SMS, incurring paid hosting charges, or changing live MySQL before proper approval and evidence.
- **STOP** treating every old draft objective as a binding requirement. Academic compliance and the specifically requested hosted demo are the current goals.
- **STOP** indefinite CI churn if failures are reproducible **external downloads** and the tested source is unchanged; record the infrastructure limitation, retry when appropriate and move to higher-value checks.

## 6. Shortest execution path from THIS moment

**ChatGPT / current session:** (1) inspect CI retry and identify real source failures if any; (2) do one bounded importer+private-plan compatibility/source/security audit; (3) verify course-critical evidence gaps; (4) commit only actual fixes/evidence and refresh this one action page. **No more speculative feature work.**

**Owner + Codex when access is available:** (5) privately stage approved JSON; (6) identify live target, rotate/check secrets, prove independently restorable backup; (7) full 96-plan rehearsal on restored clone; (8) maintenance + pre-import V2 capture; (9) single live import; (10) independent source-derived postflight, account/browser signoff; (11) approved deploy/merge, removal of temporary source, monitoring. Codex cannot complete host-only steps **until it has authorized host access**.

**In parallel, student:** check Canvas/submission requirements, make actual final demo/video and honest peer-review/authorship evidence; do not let optional 96 scale delay required coursework.

## 7. Precise next-Codex prompt

> Read **DO_NOW_MUST_DO_STOP_AND_CAPABILITIES.md** first, then **CURRENT_STATUS_AND_NEXT_ACTIONS.md**, the current repo source, **docs/deployment/C8_PRIVATE_DATASET_MANIFEST_2026-11-23.md** and **docs/deployment/P0_NORTHFLANK_MANUAL_JOB_COMMAND_2026-11-23.md**. Fetch latest PR #25, issues #26/#27 and **actual exact-SHA CI**; a first-attempt Maven download failure may have been retried. Prioritize the **owner-selected 96-organization hosted demo** without sacrificing separate Dec 5 academic deliverables. Use only the previously approved private `fictional-events-v1.local.json` (19,265,850 bytes; SHA-256 `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`). Do not commit raw data publicly. No feature creep, no frozen Course A changes, no paid costs/SMS/merge/deploy without approval. Investigate only actual blockers; for host work prove target, security review, off-host backup, isolated restore, **full 96 clone rehearsal**, explicit release approval, pre-import original V2 capture, single controlled import, independent V2/postflight and real-browser/OWNER acceptance. Stop on any failed gate and preserve data. Record exact receipts, update this page and issue #26. If Northflank access is absent, DO NOT imply production work completed; report the specific access needed, finish only safe ChatGPT/GitHub tasks. Issue #27 is later. Do not invent course submissions or other student evidence.

## 8. Status refresh rule

At every continuation: first re-check latest HEAD, run attempt/conclusion, PR merge/deploy and evidence for real host state. Replace only this file's current-status lines and task states; don't duplicate a new “next” section in six files. Update issue #26 and PR #25 with a short link. **Until independent production evidence is present: `LIVE_IMPORT_NOT_RUN`.**
