# OrgResource — next tasks, remaining work and delivery gates

## USER-ORDERED P0 — Populate existing Northflank with exact approved 96-workspace demo

**Latest owner decision, 2026-11-23:** This is the active main engineering priority. [Issue #26](https://github.com/newonathe/OrgResource/issues/26) and [runnable/preflight safety runbook](../deployment/P0_NORTHFLANK_DEMO_POPULATION_2026-11-23.md). C8 14-route stock evidence work stays open, but follows prerequisites for this hosted demo. **LIVE_IMPORT_NOT_RUN.**

- [x] Source finding: original `--c8-local-demo-import` is fresh-schema-only; existing Northflank DB is populated. Never use it there.
- [x] Source change `6c7fb265`: complete SHA-verified mixed-original/demo cohort classifier with fail-closed cases + tests; awaits exact runtime proof.
- [x] Source change `aa757ef9`: dedicated **read-only** target and exact-private-plan preflight CLI + negative tests; awaits CI and actual DB execution.
- [ ] Check CI at both commits and fix only real build/test defects; verify final SHA and original unit + browser suites.
- [ ] Secure exact private manifest/plan in authorized operator runtime; SHA-256 verify `57f1f0c6...` and `3b0f54da...`. Do not commit.
- [ ] Verify actual Northflank MySQL/schema, running image, original OWNER+org with **read-only** preflight.
- [ ] Complete native backup, off-host encrypted backup and independent isolated RESTORE with original-row/owner login proof.
- [ ] Build distinct additive importer preserving existing owner/org, safely replaying 96 workspaces (3/8 isolated test first, full approved fixture only after).
- [ ] Test idempotency/refusal on repeated/partial import, synthetic provenance, all stock/receipts/role isolation, resource capacity and original data unchanged.
- [ ] Obtain independent review; perform a controlled manual Northflank job only with real verified prerequisites; inspect actual website and log counts.
- [ ] Course A separate owner Canvas/demo/authorship obligations; no impact on immutable source.

**Note:** Code pushes, feature CI and a preflight are not proof that data exists on the deployed website. No Northflank database mutation was performed by this assistant session.

---

## Latest execution addendum — two bounded cycles, 2026-11-23

**Current PR #25 source audit head before this synchronization:** `5b1c4826d8d5f91faab1eb609ad29c94fdec033b` (subsequent documentation commit will advance it). No merge, no staging deployment, no production MySQL changes.

- [x] **CYCLE 1 — diagnostic code pushed:** `afd5682b7b230fb82d694d83028c37f90224240d` adds exact browser POST 201/selected project-resource pair checks without changing application logic. [Evidence and open diagnostic](../../evidence/C8/C8_CYCLE_01_REQUIREMENT_POST_DIAGNOSTIC_2026-11-23.md). Original [CI 45070699372](https://github.com/newonathe/OrgResource/actions/runs/45070699372) FAILED at the prior Playwright requirement-persist poll. Follow-up [CI 45071413400](https://github.com/newonathe/OrgResource/actions/runs/45071413400) **SUCCEEDED on exact code SHA `afd5682b7`** (Extended Java/MySQL 62 tests; Vitest 36; Newman 90+20 assertions; Playwright 18/18; separate Course A and React jobs green). **NOT a confirmed deterministic repair or full C8 acceptance.**
- [x] **CYCLE 2 — 33 route policy rows SOURCE_ONLY:** [24 InventoryResource + 9 AllocationResource oracle](../../evidence/C8/C8_INVENTORY_ALLOCATION_SOURCE_ORACLE_2026-11-23.md) pushed at `5b1c4826`. All 33 exact HTTP method/path keys matched the existing 93-route source inventory; not runtime-accepted. Draft policy coverage increases **31 → 64 of 93**; remaining source-oracle routes **62 → 29**.
- [x] **NEXT-01a:** Inspect final exact-code-SHA CI #45071413400: three jobs SUCCESS; 18/18 Playwright. Evidence is the hosted job, not a separate local rerun.
- [ ] **NEXT-01b:** Recheck/repeat formerly failing Requirement create workflow on a fresh isolated database; compare exact request IDs/response and persisted pairs to exclude intermittent selector/notification behavior. One green run does not prove reliability.
- [ ] **NEXT-02:** Execute the 14 named stock-route authorization cases `I11/I12/I15–I19 + L03–L09`, match actual tests/SQL to source oracle, positive+meaningful denied existing-target cases, current MySQL postconditions. Independent review REQUIRED.
- [ ] **NEXT-03:** Source-oracle remaining **29**: Department 4, Project 4, ResourceRequirement 4, Resource 7, V2Department 3, WorkspacePage 6, DemoMetadata 1. Reconcile earlier 31 and newly drafted 33 with independently verified per-route acceptance.
- [ ] **NEXT-04:** Keep C8 BLOCKED; verify final three-job CI on pushed SHA, independently review, update PR, and wait for owner authorization before C9. Finish Course A Canvas/Dec 5 academic obligations separately.
- [ ] **OWNER ONLY:** Private credential rotation, actual hosted image/refresh verification and rights/restore gate before 96-org demo import. No secrets to GitHub.

**Next copy-ready batch:** [C8 stock evidence prompt](2026-11-23_CODEX_NEXT_C8_STOCK_EVIDENCE.md). **Status: SOURCE_ONLY/NOT_RUN, not formal C8 acceptance.**

---

**Snapshot:** 2026-11-23, Asia/Manila. **Type:** public-safe planning handoff; no new implementation or runtime tests performed by this document.
**Repository:** `newonathe/OrgResource` | **C8 draft:** [PR #25](https://github.com/newonathe/OrgResource/pull/25) | **reviewed feature head:** `a51f3477abbbbe02b9b70e6dd0686d73c90222a2` | **base:** C7 branch `b4ca5cd1b8a2a128f997f4dbd5e6848cf2b39d0b`.
**Latest observed feature CI:** [45069688159](https://github.com/newonathe/OrgResource/actions/runs/45069688159), `success` on the reviewed head, three jobs. Green CI is not authorization or release acceptance.
**Staging branch checkpoint:** `deploy/northflank-c8-staging` at `32a4687bd745bfd37b547867e5a72ae1ea623794`; successful build/live serving image requires independent verification.
**Frozen academic A:** `7f8c0746da18fd54b47f430ac1435ac36e605d67`; must not be altered.
**Authority:** [assignment](../../ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf) (3 pages), [syllabus](../../ChatGPT-Project-Pack/UPLOAD/08_CSCI180_SYLLABUS.pdf), [concise proposal](../../ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx); `AGENTS.md`; `docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md`; `NEXT_CHAT_START_HERE.md`; and the C8 bounded contract. Check Canvas; submitted status not inferred from source files.

## Current state: do not conflate lanes

| Lane | Current evidence | Verdict / next gate |
| --- | --- | --- |
| Course Final A | Separate five-JPA-entity Java 8/Jersey/React, 28 amended operations, A3 technical reports and prior successful Java/MySQL/Postman/browser receipts | **Technically evidenced at frozen SHA, academic acceptance NOT VERIFIED.** Verify original release archive and perform real student-run demo/Canvas obligations. |
| C6 source audit | Draft PR #24, partial source/path evidence and release concerns | **BLOCKED / draft**; not an independent final acceptance. |
| C7 security/privacy | Draft PR #22, changes feeding C8 | **Draft/unmerged**; final exact acceptance must be reconciled. |
| C8 Extended domain | Draft PR #25, 93 annotated Jersey operations over 15 classes; 88 protected anonymous denial cases tested; 31/93 route policy oracles drafted (20 identity/governance, 8 Project Work, 3 Reporting). 62 further oracles outstanding. | **BLOCKED** under `evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md`: real packaged route + per-operation authorized/meaningful denied/MySQL proof mapping and independent review are incomplete. |
| Northflank staging | Earlier successful GitHub CI and selectively promoted staging SHA; public HTTPS demonstrated previously | **Live running-image SHA and hard-refresh walkthrough not independently verified.** No public release claim. |
| C9–C11 | Plans/prompts exist under `docs/execution`; C9 not started in current C8 reports | **NOT AUTHORIZED/NOT RUN**, pending C8 gate and separate later approvals. |

## Ranked remaining TODOs

### P0 — owner / course lane, independent of optional Extended C
- [ ] **ACADEMIC-01 (student, immediately):** Check current Canvas announcements, submission modules/receipts, Part I proposal and Part II skeleton status, final due date, whether 24/27 November early presentation is available/approved, and current final recording rules. Printed deadline: **2026-12-05**; Canvas can change it.
- [ ] **ACADEMIC-02 (student with Codex assist for reproducible checks):** Locate frozen A3 deliverables and hashes; execute a fresh non-destructive Java 8 + MySQL startup, five table inspection, 28-route Postman/negative tests and demo rehearsal on isolated fictional data. Preserve dated commands, logs, actual results; no invented successful execution.
- [ ] **ACADEMIC-03 (student):** Complete and check actual academic export, working final video or specifically approved early presentation, instructor-viewable link, Certificate of Authorship and truthful acknowledgement of outside/AI assistance, peer-review requirement if applicable, and Canvas upload receipts. No assistant may fabricate any.
- [ ] **SECURITY-01 (owner privately):** Rotate previously exposed staging DB/recovery credentials, verify secrets no longer appear in bootstrap logs or accessible artifacts, keep protected contact-encryption keys available, verify scoped runtime grants. Never paste secret values in GitHub or this record.
- [ ] **HOST-01 (owner/operator):** Verify what SHA/image digest Northflank actually runs, successful build and rollout, direct hard refresh of public page, metadata GET behavior, responsive layout and fail-closed handling with sanitized evidence. No cloud DB mutation or import as a diagnostic shortcut.

### P0 — C8 mandatory acceptance, next Codex batch
- [ ] **C8-01:** Fetch fresh C8 PR head, base, CI, staging and working tree; read `AGENTS.md`, mandatory quality gate, bounded authorization contract, route inventory/ledger and existing oracles. Reconcile exact head SHA after this documentation commit.
- [ ] **C8-02:** Reconcile all **93 annotated operations** to the actually registered Jersey routes and packaged HTTP handlers, verbs, media types, delegates, and five-public/88-protected split. The source inventory and anonymous barrier prove narrower claims.
- [ ] **C8-03:** Write source-derived role/tenant/object/lifecycle/field/response/database-effect oracle for the **62 not yet source-oracled**, in safe batches: InventoryResource (24) + AllocationResource (9) first; then Department (4), Project (4), ResourceRequirement (4), Resource (7), V2Department (3), WorkspacePage (6), DemoMetadata (1). Review the prior 31 oracles for fidelity.
- [ ] **C8-04:** For each protected route, map **allowed success** and meaningful **real existing-object refusal** to exact source SHA, fixture, HTTP status/body and sensitive-field redaction; for denied high-risk writes, verify MySQL before/after including allowed audit effects. Public routes require intended behavior/privacy and abuse checks.
- [ ] **C8-05:** Target tenant mismatch, membership revocation, role/demotion, assigned vs unassigned LEAD, linked vs unrelated borrower, stale If-Match, stock overbooking, receipt replay/expiry, rollback/concurrency/restart, consent, image, reporting/export. Build only missing targeted tests; reproduce any actual defect before changing code.
- [ ] **C8-06:** Reconcile existing *restricted local* IR4 receipts against the public 93-route ledger only when evidence and SHA genuinely match. Preserve historical 199 executed passes, 48 exclusions, 22,073 unrun case classifications (not an automatic mandatory 22,320-case rerun).
- [ ] **C8-07:** Obtain genuinely separate, authorized independent review of route oracles, fixture, source patches and actual receipts. If unavailable: `INDEPENDENT_REVIEW_NOT_RUN`, C8 BLOCKED. Self-review is not independent.
- [ ] **C8-08:** Final exact-SHA course A Java 8/MySQL, Extended Java 21/MySQL, Newman, packaged Playwright and React CI; record all jobs/steps, explicit skipped/failed checks, source tree comparisons and untouched prior Flyway checksums. Push public-safe evidence and summarize PR #25; no merge.
- [ ] **C8-09:** C8 explicit acceptance record and owner authorization required before C9. If any mandatory v1 row remains, report BLOCKED with the next precise missing test.

### P1 — deferred until C8, ownership and safety gates
- [ ] **DEMO-01:** Reconcile approved private 96-label/public-name manifest with permitted fictional history, its exact hash and data rights. Keep raw restricted evidence, real roster, local import receipts and secrets off public GitHub. No fabricated organization activity.
- [ ] **DEMO-02:** Perform independent isolated backup/off-host restore and agreed retention/backup-scale checks **before** any controlled hosted import; do not run the current local-only importer on Northflank.
- [ ] **C9 (separate approval):** Failure injection, measured SQL plans/latency and CI/test isolation/performance using an actually produced and verified synthetic fixture; optimize only measured bottlenecks. See `evidence/C8/C9_IMPLEMENTATION_HANDOFF.md` and `docs/execution/05_CODEX_PROMPTS_C9_C11.md`.
- [ ] **C10 (separate hosting/recovery authority):** Authorized staging TLS/proxy/CSRF verification, least-privilege runtime/migration accounts, operator runbook, logging, off-host encrypted restore and measured RPO/RTO. Previously staged service is not C10 acceptance.
- [ ] **C11 (separate approval):** Independent release/security/academic audit, exact-hash packaging, disaster-recovery validation, scope and rights/privacy approval, explicit owner release GO/NO-GO. No production release by default.

## Recommended calendar / contingency

- **Nov 23–24:** Student verifies Canvas; confirm frozen academic deliverables and full demonstration path; privately rotate compromised secrets; run hosted read-only hard-refresh verification. Codex begins C8 route-oracle/evidence batch only.
- **Nov 25–27:** Prioritize actual local academic rehearsal/export and resolve early-presentation option if available; continue C8 33-route Inventory/Allocation oracle and targeted proof. Do not let Extended scope displace grading requirements.
- **Nov 28–Dec 2:** Complete technical acceptance evidence needed for course final, record/edit genuine demo with permission checks; continue C8 review if schedule permits.
- **Dec 3–4:** Freeze and verify submission artifact/reproducibility/links; student submits with buffer if Canvas agrees.
- **Dec 5:** Printed course final due date. No assumption a syllabus one-week policy grants a project extension.
- **After academic gate:** C8 acceptance first; then separately approved C9, C10, C11. Cut optional hosted 96-organization scale, new analytics and cosmetic improvements before sacrificing course functionality or required academic evidence.

## Output and Git workflow for every subsequent OrgResource session

1. Start by reading `NEXT_CHAT_START_HERE.md`, `AGENTS.md`, the mandatory quality standard and the latest handoff; fetch live Git branch/PR/CI state. State exact **observed** status, not historical descriptions alone.
2. Publish **repository-safe** task outputs, Codex prompts, updated unchecked TODOs, change receipts and next handoff to the active authorized feature/draft PR branch. Check `git status`, staged diff and branch after work; verify remote head. Keep all private course binaries/archived original materials, restricted evidence, credentials, recovery codes, real student/contact data and raw DB dumps out of public commits.
3. Document completed/blocked/not-run separately. Use a fresh dated handoff or maintain canonical task links, so stale status isn't silently treated as current. Do not merge, reset, force push, publish, deploy, spend, send real SMS, change visibility or submit Canvas without the appropriate explicit approval.
4. End every handoff with **what changed**, **what was tested with real evidence**, **what is left**, and **the exact next Codex prompt**.

**Next copy-ready prompt:** [2026-11-23_CODEX_NEXT_C8_BATCH.md](2026-11-23_CODEX_NEXT_C8_BATCH.md).

### Evidence referenced
- `NEXT_CHAT_START_HERE.md` (2026-11-23)
- `evidence/C8/C8_FINAL_STATUS.md`, `C8_ROUTE_REVIEW_LEDGER.md`, `BOUNDED_AUTHORIZATION_CONTRACT_V1.md`, `C8_CLOSURE_BATCH_2026-11-23.md`, `C9_IMPLEMENTATION_HANDOFF.md`
- `docs/CODEX_NEXT_C8_OBJECT_SCOPE.md`
- GitHub PR #25 and successful [CI 45069688159](https://github.com/newonathe/OrgResource/actions/runs/45069688159); staging branch SHA as observed
- Original course final assignment pp. 1–3, syllabus pp. 4–6 and as-built academic release guides

**Verdict:** The next action is **C8 bounded acceptance continuation while finishing student-only course delivery**. No code was written or verified in preparing this tracker.
