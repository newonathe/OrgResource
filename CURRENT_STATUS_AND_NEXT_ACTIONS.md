# OrgResource — current status, remaining work and Codex continuation

> **Pre-Claude continuation authority (2026-11-24 PHT):** For a new ChatGPT project chat with "continue", first fetch live GitHub and read [the Claude handoff continuation packet on PR #47](https://github.com/newonathe/OrgResource/tree/docs/claude-local-execution-handoff-2026-11-24/docs/handoffs/claude-local-execution-2026-11-24), especially 14_CONTINUE_PRE_CLAUDE.md and 12_EXECUTION_GRAPH.json. Do not start Claude before R09. R01 b62ff3c6 [CI 45425950575](https://github.com/newonathe/OrgResource/actions/runs/45425950575) and R06A c9133376 [CI 45427270775](https://github.com/newonathe/OrgResource/actions/runs/45427270775) verified actual receipts. R06B1 fresh local bootstrap/actual C5 restore/SELECT-only POSIX baseline preparation candidate now authored; exact private plan and importer NOT_RUN. #34 BLOCKED; #26 LIVE_IMPORT_NOT_RUN. No merge/deploy or later phase. Preserve frozen Course A and V1–V7.

> **C9 Stage 1D follow-up (2026-11-24 PHT):** ec8600f5 exact [CI #45419347291](https://github.com/newonathe/OrgResource/actions/runs/45419347291) verified; docs-head cf823697 repeated proof [#45423253554](https://github.com/newonathe/OrgResource/actions/runs/45423253554). Added changed-payload 409 and fresh MEMBER-write denial tests and safe pidfd cleanup guard, new candidate runtime PENDING. Handoff PR #47 is separate docs-only; use latest exact reviewed source, not main. Full private mixed rehearsal requires POSIX baseline compatibility and a guarded local transport; no host. #34 BLOCKED, #26 LIVE_IMPORT_NOT_RUN.

> **C9 Stage 1D candidate (2026-11-24 PHT):** [Recovered application acceptance contract](evidence/C9/C9_STAGE1D_RECOVERED_APPLICATION_2026-11-24.md). Test-only integration; runtime NOT RUN at authoring. Stage 1C remains verified. Actual mixed intermediate metadata must fail closed; normal restored authenticated browser flow remains unproven. No merge, host, V8/index/C10 or independent acceptance.

> **C9 Stage 1C — VERIFIED CI on the bounded fictional fixture, November 24, 2026 PHT:** [draft PR #45](https://github.com/newonathe/OrgResource/pull/45), tested source `53edf52bb9443fc4c7d8aebdddb15ce76b37f882`, [run #45342955553](https://github.com/newonathe/OrgResource/actions/runs/45342955553), all three jobs SUCCESS. Actual C5 archive **1,034,356 bytes** (1,034,302 SQL payload + 54 format bytes), runner-observed backup/restore **569/1,270 ms**, 15/15 refusals, independent networkless MySQL8 server, 18-table row equality, populated session and attribute excluded, original source unchanged and private workdir/restorer removed. See [Stage 1C executed receipt](evidence/C9/C9_STAGE1C_ENCRYPTED_C5_ARCHIVE_2026-11-24.md). C8 #34 BLOCKED and hosted #26 LIVE_IMPORT_NOT_RUN. Next proposed C9 Stage 1D: recovered packaged JAR startup, authentication/session retirement, durable replay and scoped authorization on a separate disposable instance with a closed, isolated test network; obtain scope authorization before starting. Preserve the Stage 1B/1C networkless proof. Full private-96, Windows protected-login rerun, off-host restoration and independent review remain OPEN. Documentation-only newer head needs its own CI check. No merge, host, SMS, V8/index or C10.

> **HISTORICAL Stage 1B checkpoint (superseded by Stage 1C above) — 2026-11-24 PHT:** C9 is explicitly **USER-AUTHORIZED for bounded preparatory work**. Stacked [draft PR #41](https://github.com/newonathe/OrgResource/pull/41) introduced 5 read-only MySQL8 SQL analogues, [#42](https://github.com/newonathe/OrgResource/pull/42) added actual native resource-balance shape, [#43](https://github.com/newonathe/OrgResource/pull/43) tested **1,034,299 bytes** of C5-shaped disposable eight-org SQL with same-server fresh-schema restore, and **[draft PR #44](https://github.com/newonathe/OrgResource/pull/44)** adds a **second, networkless, MySQL8 container and separate data directory** for cross-server SQL restore. Its test source SHA `e226b83433afda04d2f4aa426890795dff912407` passed [all three GitHub Actions jobs #45323657473](https://github.com/newonathe/OrgResource/actions/runs/45323657473) with 91 Maven/66 ordered, 60 Newman/110 assertions and 19 Playwright. The second server had **different UUID**, network mode `none`, **zero published ports** and row-byte equality on **18 tables**; this regenerated eight-org fixture's raw SQL was **1,034,301 bytes**, backup/restore command wall **167/412 ms** (not SLA). [Stage 1B evidence](evidence/C9/C9_STAGE1B_NETWORKLESS_CROSS_SERVER_RESTORE_2026-11-24.md) distinguishes the measured and missing facts. **Actual AES-GCM archive, private approved full-96 import/backup capacity, off-host recovery, concurrent/API/browser large-fixture timing and independent acceptance remain NOT_RUN/BLOCKED.** [C8 issue #34](https://github.com/newonathe/OrgResource/issues/34) remains BLOCKED (93/93 source pointers ≠ independent signoff), [hosted P0 #26](https://github.com/newonathe/OrgResource/issues/26) is `LIVE_IMPORT_NOT_RUN`. No merge, deployment, host DB, provider SMS, V8/index migration, C10 or academic submission. Older C8/C9 head references below are retained only as dated history. Any documentation-only commit after this verified source SHA needs **its own CI check**.

> **ACTIONABLE FIRST READ (2026-11-23):** [DO_NOW_MUST_DO_STOP_AND_CAPABILITIES.md](DO_NOW_MUST_DO_STOP_AND_CAPABILITIES.md) — fresh priority reset, exact actions ChatGPT can execute today, host-only blockers, academic obligations and explicit STOP list. The older detailed material below is reference, not a command to restart planning. **`LIVE_IMPORT_NOT_RUN`.**

> **SCOPED EVIDENCE UPDATE — 2026-11-24 PHT (VERIFY LIVE GITHUB FIRST):** Draft [PR #39](https://github.com/newonathe/OrgResource/pull/39) at `763542d4bd1f0fd6d21ba373bc28d54b23a0126d` has [exact-head three-job green CI #45251381998](https://github.com/newonathe/OrgResource/actions/runs/45251381998): Java8 Course A, React, Extended Java21/MySQL 91 Maven tests/66 ordered, synthetic additive/recovery tests, 60 Newman requests/110 assertions and 19 Playwright. Its 20 identity/governance assertions follow earlier 44 source-to-test mappings (64/93 navigation at that commit). Stacked [draft PR #40](https://github.com/newonathe/OrgResource/pull/40) adds 29 legacy/workspace source-to-assertion pointers, reaching **93/93 navigation-mapped**, but **0/93 independently accepted from this reconciliation**. PR #40's latest SHA/CI must be checked separately; this paragraph is not a green receipt for any later commit. [Issue #34](https://github.com/newonathe/OrgResource/issues/34) remains BLOCKED pending actual per-route factor and private IR4 independent review. [Issue #26](https://github.com/newonathe/OrgResource/issues/26) remains `LIVE_IMPORT_NOT_RUN`; no Northflank access, full private plan rehearsal, backup/restore, deploy, merge or C9 *acceptance* (authorization to begin C9 was granted on November 24; see the newer note below). The 2026-11-23 hosted-P0 release checklist below is still pending and its earlier SHA is a dated historical reference.

> **C9 USER DECISION / NON-RELEASE PROBE — 2026-11-24 PHT:** The user explicitly authorized C9. Stacked [draft PR #41](https://github.com/newonathe/OrgResource/pull/41) begins C9 Stage 0 with a GitHub Actions disposable MySQL8 **SELECT-only** query-plan/timing probe on the already generated **eight-workspace synthetic** C8 fixture; five SQL analogues trace to the checked-in workspace repositories. The [C9 evidence contract](evidence/C9/C9_STAGE0_ISOLATED_QUERY_MEASUREMENT_2026-11-24.md) distinguishes estimated plan rows and measured server iterator times from full-size/HTTP performance; final exact-head CI and actual metrics require fresh verification. This user decision permits preparatory C9 work; it does **not** complete the independent [C8 gate #34](https://github.com/newonathe/OrgResource/issues/34), establish accepted private full-96 performance, approve indexes/migrations, authorize C10, or change [hosted P0 #26](https://github.com/newonathe/OrgResource/issues/26) `LIVE_IMPORT_NOT_RUN`. No merge, live DB, deployment, SMS or public private-plan publication.

**Canonical handoff: November 23, 2026 (Asia/Manila).** This is the **current priority and status register**. Read this file first in each new ChatGPT/Codex conversation, then the linked source files. Historical notes lower in `NEXT_CHAT_START_HERE.md`, old handoffs, and CI log excerpts may describe earlier pending/failed runs; **this file takes precedence for status only**, and must be refreshed against GitHub/Northflank before taking action.

## 0. Decision and scope (USER DECISION)

**Release priority:** load the exact **96 approved directory-labeled fictional organizations** and simulated 12-month project/inventory/allocation history into the **existing Northflank OrgResource deployment**. Preserve the original active OWNER account, original organization, and all other pre-existing V7 data. Finish acceptance on the actual hosted instance. **No further optional product or cosmetic work** before this P0 release. The user plans to resume with Codex when available and wants an autonomous, safe, sequential execution plan.

**Current release verdict: `LIVE_IMPORT_NOT_RUN`; NOT HOST-ACCEPTED.** Do not confuse code/CI, private file validation, a mock/clone import, or an operator-provided attestation string with a live deployment/import.

## 1. GitHub verified state (VERIFIED REPOSITORY FACT; 2026-11-23)

- Repository **PUBLIC**: https://github.com/newonathe/OrgResource
- Main release candidate: draft, **unmerged** [PR #25](https://github.com/newonathe/OrgResource/pull/25), branch `feat/extended-c-c8-domain-workflows-2026-11-19`.
- **Historical 2026-11-23 three-job-green runtime SHA (superseded for current source-audit CI by the update above):** `9df7099145e324f326106870b1b3fd0788d5510f`, [CI run #45155946849](https://github.com/newonathe/OrgResource/actions/runs/45155946849). Passed: Java 8 + isolated MySQL/frozen Course A, React typecheck/lint/build/tests, and Extended C Java 21 + isolated MySQL/Newman/Playwright. Includes responsive sidebar `OrgResource` one-line fix tested at nine widths, additive C8 import guards, V2 pre-existing-row HMAC baseline and independent postflight. **These tests used isolated small/intermediate fixtures, not the exact full 96-org plan or production DB.**
- Most recent *pre-handoff* feature HEAD `22cda4d20f88809cd0577b1c8fea0fbbb8b1335e` added public-safe dataset manifest and handoff documentation. [Its CI run #45164554545](https://github.com/newonathe/OrgResource/actions/runs/45164554545) was **in progress when this handoff was prepared**. This document and its GitHub commit will create an even newer documentation-only HEAD; check each exact SHA independently before labeling it green.
- Frozen Course A boundary: `7f8c0746da18fd54b47f430ac1435ac36e605d67`. Do not modify or overwrite the frozen Course A sources or claim approval/academic submission.
- At the original 2026-11-23 snapshot, open issues noted: [#26 P0 full demo import](https://github.com/newonathe/OrgResource/issues/26); [#27 DEC-D01 department model](https://github.com/newonathe/OrgResource/issues/27). Refresh issues/PRs for later changes.
- **CODE FREEZE:** Do not make incidental improvements. If a real blocker is observed, identify the failing evidence, make the smallest isolated fix on PR #25, and rerun all required CI. Do not merge into main without explicit owner consent.

## 2. Exact private source: found and verified (VERIFIED FILE EVIDENCE)

The user has recovered and uploaded `fictional-events-v1.local.json`. Its reported content and checksum were independently validated in this ChatGPT Project and recorded in the [public-safe manifest](docs/deployment/C8_PRIVATE_DATASET_MANIFEST_2026-11-23.md):

| Property | Exact expectation |
| --- | --- |
| Byte size | `19265850` |
| SHA-256 | `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c` |
| Roster source SHA-256 | `57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2` |
| Dataset ID | `orgresource-c8-plan-v1-57f1f0c68004-20deeda9e2af` |
| Classification | `synthetic=true`, `scope=LOCAL_ONLY_DRY_RUN` |
| Directory-label cohorts | 96 total: 72 COA-M + 24 LIONS |
| Planned data | 248 departments, 713 projects, 1,312 resources, 2,139 requirements, 462 fictional personas, 21,060 allocations, 67,370 historical allocation events |
| Final state distribution | PENDING 212; APPROVED 211; ISSUED 1,123; RETURNED 8,479; CONSUMED 5,699; CANCELLED 5,336 |

**Critical:** This repo is **PUBLIC**; the raw 19.3 MB JSON was **NOT** published to it. Do not upload it as a PR/issue/release attachment, workflow artifact, secret file, Docker layer or commit; no plan regeneration, scraping or invented names. The original file remains under owner control/ChatGPT attachment; Codex cannot assume an attached ChatGPT file is automatically in its workspace. Operator must obtain file securely and recheck size/SHA on disk and inside the temporary import job. Owner may separately create a PRIVATE GitHub repo, but **private repo creation/upload is not confirmed** and is optional if direct operator-private transfer works.

## 3. P0 blockers — only remaining work before live release

| ID | Priority | Next action | Evidence required | Status/owner |
| --- | --- | --- | --- | --- |
| P0-01 | BLOCKER | Access the **actual authenticated Northflank project** and inspect deployed image SHA, MySQL8, V7 schema/Flyway, existing OWNER/org UUIDs, runtime limits and safe network path | Redacted operator inspection receipt, no tokens | **OPEN — owner/authorized operator** |
| P0-02 | BLOCKER | Check Northflank 2026-10-30 incident notices and **rotate/check stored secrets**, including MySQL/deployment/API credentials as applicable; verify site connectivity | Redacted audit/rotation and health evidence | **OPEN — owner/authorized operator** |
| P0-03 | BLOCKER | Make native + encrypted **off-host backups**, **restore independently** to isolated MySQL8 clone, verify original account/schema/data | Completed backup ID, off-host copy and independently successful restore receipt | **OPEN — owner/authorized operator** |
| P0-04 | BLOCKER | Privately stage unchanged JSON and **full-size rehearse all 96 orgs on the restored clone**, from the reviewed release image | Original SHA, completed clone import + detailed read-only postflight, capacity/performance and recovery proof | **OPEN — Codex + operator** |
| P0-05 | BLOCKER | Confirm planned directory-label public-display/endorsement rights, correct synthetic notices, no actual participant data/SMS, and maintenance/rollback approval | Owner-reviewed go/no-go | **OPEN — owner** |
| P0-06 | BLOCKER | On original target, capture original **V2 HMAC baseline BEFORE writes**, verify exact target plan, stop web writes, run owner-approved single import | Pre-import CAPTURE receipt, schema/server/owner fingerprint, manual import completion receipt | **NOT RUN — operator** |
| P0-07 | BLOCKER | Run independent **SELECT-only original V2 VERIFY + exact source-derived postflight**, original-owner login and actual live browser/API tenant tests | Original data unchanged; all 96 exact labels, histories, states, stock checks and access-control negatives; dated/redacted receipts | **NOT RUN — operator + Codex** |
| P0-08 | BLOCKER | Close maintenance after all gates, verify production bundle (sidebar fix), review merge/deploy explicitly with owner | Live URL tests, deployed SHA, signed owner decision | **NOT APPROVED — owner** |

**Do not claim any item completed from CI.** In particular, an exact-plan fingerprint and a passing isolated synthetic test prove neither restore recoverability nor live deployment. If any import is partial, **STOP**; do not rerun blindly on a contaminated database. Restore only through a reviewed process.

## 4. Can be completed in a normal ChatGPT + connected GitHub chat

- Audit remote branch, issue/PR, repository source and actual GitHub Actions logs.
- Edit/publicly commit **non-sensitive code, tests, public-safe docs**, update issue/PR and README/handoff links, and verify CI status. Avoid unrelated code because P0 is frozen.
- Examine an uploaded private JSON locally, independently verify size/hash, schema/aggregates and safe sample fixtures. Do **not** copy private source to public GitHub or assume a previous attachment is present in every future Codex runtime.
- Prepare **operator-ready commands, decision checklist, exact expected outputs, acceptance table and copy-ready Codex continuation prompt**.
- Review proposed department feature, academic compliance, API and access-control tests, and identify verified gaps **without presenting the design as implemented**.

## 5. Cannot complete from GitHub-only ChatGPT access

- Read/rotate Northflank tenant secrets, run privileged jobs, modify a live MySQL addon, produce an authentic native backup + independently verified restore, transfer the private plan into Northflank, conduct the actual full 96 replay/import, validate live browser sessions or approve merge/deployment **without owner-operated host access**.
- Create or populate a new PRIVATE repo unless a connected tool with those permissions is available and the owner authorizes it. **Do not create a public raw-data substitute.**
- Test users' real interactions, claim a measured campus pilot/usage, obtain permission from Ateneo, or infer instructor/Canvas approval.
- Promise asynchronous completion from a chat; Codex later must execute and record evidence.

These are access/evidence boundaries, not requests to rewrite the importer. No passwords/API keys/DB dumps in ChatGPT, GitHub comments or public logs.

## 6. Existing executable operator runbook and acceptance contract

Read [P0 Northflank manual job runbook](docs/deployment/P0_NORTHFLANK_MANUAL_JOB_COMMAND_2026-11-23.md) **in full**. It contains the required environment variables, private job transfer and checks, exact SHA preflight, target fingerprint and confirmation phrase, SELECT-only HMAC CAPTURE/VERIFY, import, postflight, backup attestations, SMS-disabled settings, restoration handling and live acceptance.

The existing CLI flags are:

~~~text
java -jar "$JAR" --c8-hosted-demo-preflight
java -jar "$JAR" --c8-hosted-original-baseline   # CAPTURE before; VERIFY afterward
java -jar "$JAR" --c8-hosted-demo-import
java -jar "$JAR" --c8-hosted-demo-postflight
~~~

All flags and environment settings must be verified against **the actual deployed Java 21 artifact and operator job**. The hosted importer itself requires an existing private V2 baseline in VERIFY mode, checks before writes and rechecks afterward. Separate SELECT-only verification is still mandatory. The file exceeds Northflank's documented small secret-file cap; use a private authenticated job-file transfer, not env-var/secret-file embedding. No real SMS.

## 7. DEFERRED until after P0 (DESIGN RECOMMENDATION, NOT IMPLEMENTED)

- [DEC-D01 department affiliation and within-org cross-department resource catalog](docs/decisions/DEC_D01_DEPARTMENT_VISIBILITY_AND_REQUESTS_2026-11-23.md), [issue #27](https://github.com/newonathe/OrgResource/issues/27). A membership may belong to one *primary department within an organization* under the proposed design, and one user can have multiple organizations; this is NOT yet an accepted schema/API/privacy decision. **Do not add V8 migration or change approved V7 import schema during P0.**
- Additional product features, SMS/Twilio integration, performance polish, alternate architecture, outside integrations, public campus adoption claims and ambitious UI redesign.
- Extended C security/authorization full matrix or any feature beyond the verified bounded tests must be independently assessed for a production-ready product; passing the existing smoke tests is not proof of complete audit coverage. Confirm course assignment, syllabus and current Canvas announcements before academic submission. Record honest AI assistance/Certificate of Authorship where instructed.

## 8. Copy-ready Codex execution prompt (paste only after opening the repo)

> You are continuing OrgResource. First read **CURRENT_STATUS_AND_NEXT_ACTIONS.md**, then **NEXT_CHAT_START_HERE.md**, **docs/deployment/C8_PRIVATE_DATASET_MANIFEST_2026-11-23.md**, **docs/deployment/P0_NORTHFLANK_MANUAL_JOB_COMMAND_2026-11-23.md**, the C8 importer/preflight/baseline/postflight source and the current GitHub PR #25/issue #26. Check the latest remote HEAD, jobs and branch status; do not rely on stale historical “CI pending” paragraphs. The goal is to import the **exact already-approved private 19,265,850-byte/96-organization plan** into the **existing owner-controlled Northflank OrgResource**, preserving *every original row* and existing login. Reverify SHA `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c` and the plan schema locally. Do not publicly upload the JSON; obtain it privately from the operator. Use the code freeze and avoid redesigns. Before any live database mutation, verify host access and user authorization, rotate/review secrets, independently back up + restore MySQL8 to a separate clone, demonstrate the exact full-plan rehearsal and independent postflight, capture the **original live target** V2 original-row HMAC BEFORE writes, verify image/DB compatibility and maintenance gating, then run one approved import and separate original VERIFY/postflight. On failure STOP and recover from backup. Verify original account, exact 96 orgs and all record histories, tenant isolation, synthetic labeling, no SMS, sidebar and responsive UI. Keep PR #25 unmerged until specifically approved. If blocked by absent host connection, **do not fake execution**: finish only safe repository tasks and report the precise access/action needed. Update this file, PR #25, issue #26 and sanitized evidence with actual observed facts. Issue #27 is explicitly deferred. Do not claim done until live proof exists.

## 9. Update protocol for future sessions

At **end of every work session**: (1) inspect exact HEAD and CI outcome; (2) replace the status rows in **this canonical file**, marking only proof-backed items complete with dated links; (3) update **NEXT_CHAT_START_HERE.md** pointer and PR #25/issue #26; (4) record the latest *actual* import state `LIVE_IMPORT_NOT_RUN` / `PARTIAL_STOP` / `LIVE_VERIFIED` with evidence; (5) leave owner-only secrets and private data outside public GitHub. Avoid duplicating ever-growing “current” paragraphs at the start of stale notes.

**No production import, host security rotation, backup/restore, full-96 clone rehearsal or approved live release has yet been established in the GitHub evidence reviewed for this handoff.**
