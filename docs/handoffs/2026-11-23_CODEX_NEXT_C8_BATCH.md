# Copy-ready Codex prompt — OrgResource C8 next acceptance batch

> **Planning output, not completed implementation.** Snapshot 2026-11-23 (Asia/Manila). This is the next bounded work cycle for C8; it deliberately does **not** authorize C9, merging, release, hosted data import or Canvas submission.
>
> Paired tracker: [2026-11-23_ORGRESOURCE_STATUS_AND_TODO.md](2026-11-23_ORGRESOURCE_STATUS_AND_TODO.md).

## Paste everything below into Codex

You are the OrgResource **C8 route-authorization closeout coordinator** working in `newonathe/OrgResource`. Your immediate task is **one bounded C8 batch**: make the highest-risk InventoryResource and AllocationResource policies source-precise, and add/match missing real MySQL+HTTP evidence, while preserving working code and capturing an exact continuation. C8 is **BLOCKED**, and must stay BLOCKED unless the owner-approved mandatory v1 contract is actually satisfied for all 93 operations and independently reviewed. Do not start C9.

### Immutable context and preflight

- C8 draft PR: `#25`; starting branch `feat/extended-c-c8-domain-workflows-2026-11-19`; historical inspected pre-handoff HEAD `a51f3477abbbbe02b9b70e6dd0686d73c90222a2`. Documentation commits may have advanced the branch, so fetch **current** remote HEAD, working tree, PR base/diff, and exact green/failing CI; do not use the snapshot SHA as a reset target.
- C8 follows draft C7 branch `feat/extended-c-c7-security-privacy-2026-11-19`. Frozen Course Final A source `7f8c0746da18fd54b47f430ac1435ac36e605d67` must stay byte/behavior unchanged. Previous Flyway migrations must retain their checksum bytes.
- The academic assignment is authoritative on the Java 8/Jersey/MySQL/Retrofit/Javax/5-entity course track; Extended Java 21 is a separate optional product track.
- Inspect actual repo tooling before choosing commands, dependencies or test environments. Do not copy generic examples or overwrite local edits.
- Read in full: `AGENTS.md`, `docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md`, `NEXT_CHAT_START_HERE.md`, `evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md`, `evidence/C8/C8_FINAL_STATUS.md`, `evidence/C8/C8_ROUTE_INVENTORY.json`, `evidence/C8/C8_ROUTE_REVIEW_LEDGER.md`, both dated C8 policy oracle files, `evidence/C8/IR4_TECHNICAL_STATUS.md`, `docs/CODEX_NEXT_C8_OBJECT_SCOPE.md` and the paired TODO tracker.
- Then read all affected real `InventoryResource` and `AllocationResource` Jersey handlers, security filters, their delegated components/services, repository queries and tests. Inspect `C1JerseyConfiguration`, `C1OrganizationContext`, relevant security configuration and tenant/ledger SQL. Account for dependency and caller effects, not only direct diffs.
- The public reference says **93** annotated operations, **88** protected and **5** public; **20** identity/governance and **11** Project Work/Reporting have draft source policy oracles, leaving **62** source-review oracles. The first target batch is **InventoryResource (24) and AllocationResource (9)** = **33**. Verify these counts against code; do not mechanically trust them.
- Prior exact CI at pre-handoff head `a51f3477...`: [run 45069688159](https://github.com/newonathe/OrgResource/actions/runs/45069688159) reported completed success. This does not mean route policy is fully accepted. Prior staging branch `32a4687b...` is different from feature and is not evidence of the currently running Northflank image.

### Authorization and review contract (prepare BEFORE running requests)

For each of the 33 target HTTP method + concrete template-path pairs, record a **source-derived oracle** with:
1. Java handler, HTTP method/path, media types, controller-to-component/service/repository delegation and actual test hook.
2. Public or authenticated classification; current account/activity and selected organization; path organization, membership and role requirements; assigned LEAD and borrower linkage restrictions where relevant.
3. Existing-target object ownership and visibility, full allowed/disallowed fields, data minimization/redaction, lifecycle state, stale version/If-Match, idempotency/precondition behavior and invalid-body handling.
4. Expected success/refusal status, structured error, safe disclosure, exact affected database tables/rows/versions and permitted audit effects. Use real source as authority, not inferred role matrix.
5. One authorized success fixture and one meaningful existing-object denial fixture (foreign tenant, insufficient role, unassigned LEAD, unrelated borrower, revoked membership, etc.) where applicable; state any route-specific exception rather than forcing nonsensical tests.
6. Actual command + expected response and DB postconditions, explicit evidence link and status `SOURCE_ONLY`, `VERIFIED`, `NOT_RUN`, `BLOCKED` or `TEST_FAILED`. A green CI job with an unrelated assertion does not count.

Build the oracle using a **fixed set of fictional accounts, two separate organizations, active/revoked memberships, assigned/unassigned LEAD, linked/unlinked borrower, existing resources/requirements/allocations with controlled statuses**, and isolated disposable MySQL 8 data. Do not use any hosted owner, real student roster, private original manifest, real telephone number or Twilio credential.

### Execution sequence and stop conditions

1. **Git and source snapshot:** enumerate exact C8 HEAD and modified files, PR #25 upstream, current CI status and stage reference. Create a clean authorized working branch/worktree only if needed and preserve other work; do not reset/force-push.
2. **Inventory validation:** reconcile current Jersey registration/annotation list with all 93 inventory rows; verify that the 33 target routes are in the actual packaged route set and that the existing 88-route anonymous deny test covers its specified shared denial class only. Do not declare 88 route-specific authorized tests passed.
3. **Before-code oracle review:** write/extend the 33-row policy/field/DB-effect matrix and independently challenge assumptions, including selected org vs path org, permission change between read and write, stale If-Match, request replay and status changes. Reuse actual source and the approved borrower/LEAD decisions; record contradictions.
4. **Map existing evidence honestly:** inspect the local restricted IR4 packet **only if authorized and actually accessible**, and exact test source/CI logs. Map a prior receipt only when its fixture, tested revision, executed command, HTTP/SQL assertions and authorization factors match the oracle. Otherwise mark UNMAPPED. Never publish private raw receipts/identities.
5. **Test missing risk cases:** add focused independent real Java 21 + MySQL8 acceptance tests, actual HTTP/JSON and relevant SQL before/after assertions for allowed/denied existing-target cases. Prioritize stock approve→issue→return/consume, approve races, cancel/delete protection, cross-tenant protected reads, borrower's own consent withdrawal vs unrelated person, receipt replay after expiry, revoked/membership/demotion, concurrent resource updates, transaction rollback and out-of-order status transitions.
6. **Defect-first corrections only:** if a test exposes a real product defect, isolate a reproducible pre-fix failure; patch only the smallest approved affected source, preserve public route contracts and privacy, add a regression and rerun. Do not weaken CSRF/role checks, ledger accounting, immutable history, migrations or tests to get green.
7. **Independent critic:** request a genuinely separate reviewer using the repo's current supported workflow, if available and authorized. Supply actual policy table + diffs + test outputs; require counterexamples and a per-route verdict. If absent, mark `INDEPENDENT_REVIEW_NOT_RUN` and keep C8 blocked.
8. **Exact-build run:** inspect existing documented verification scripts and use the real appropriate ones for the isolated MySQL acceptance, Extended Java 21/Maven, Newman, packaged Chromium/Playwright, React typecheck/unit/lint/build, and separately frozen Course A Java 8/MySQL. Check final commit SHA and CI job steps after the last file change, not before. Do not fabricate test commands/results.
9. **Publish safe evidence:** update the C8 route review ledger and a dated batch phase report with exact method/path, oracle evidence, source paths and reviewed ranges, commands, exit codes, result, DB effects, snapshots, CI URLs, independent-critic verdict, remaining route families and required approvals. Update `NEXT_CHAT_START_HERE.md`, the paired TODO tracker and PR #25 **only after checking current content and staged diffs**. Push safe source/docs to the current authorized feature branch. Keep archives, raw local evidence, secrets and restricted manifests off GitHub.
10. **Stop with a gate:** After one coherent 33-route batch, report the precise accepted/unaccepted tests and route mappings. Continue in a subsequent independently reviewed batch for the remaining 29 not yet source-oracled, plus reconciliation of the 31 previously drafted source oracles. Do not declare C8 accepted until **all 93** fulfill the owner-approved bounded v1 evidence contract with independent review.

### Additional separate owner checkpoints (do not silently perform)

- A student must check the **2026-12-05** printed final date against live Canvas and finish Course Final A academic artifact/export/demo/link/certification/peer-review/submission evidence. Preserve the frozen A3 source. Prior academic test evidence is not proof of Canvas upload.
- Staging SHA `deploy/northflank-c8-staging` must be correlated with the **actually running** Northflank container and a real hard-refresh/metadata GET/desktop-mobile check before declaring the hosted startup UX resolved. A completed GitHub Actions workflow is insufficient.
- Previously exposed staging database/recovery credentials must be rotated through private account operations; do not surface secrets in an agent log.
- The planned 96-label demo requires approved private source manifest, fictional-only generated records, intact data-rights review, and independently proven isolated/off-host backup and restore. **Do not execute** the local importer against hosted Northflank.
- C9 profiling and index changes, C10 release/off-host operations and C11 acceptance all require distinct later gate/authorization. Never merge main, force push, change repository visibility, deploy, spend, use live contact/SMS or submit Canvas by inference.

### Mandatory end-of-cycle answer format

Return:
1. **Exact source snapshot**: branch, HEAD, parent, PR #25, CI run links and whether CI ran on final SHA.
2. **Changed files and public-safe Git links**; tested vs untested files.
3. **33-route batch results** with audited successes, denials, real DB effects, newly reproduced defects and all unresolved `NOT_RUN/BLOCKED`; explicitly distinguish policy-oracled from evidence-verified.
4. **Outstanding TODO list**, including remaining 29 source oracles and 31 prior oracle reconciliations, independent review, hosted owner safety, Course A academic manual steps and C9–C11.
5. **A single copy-ready next Codex prompt** for the next smallest safe acceptance batch.
6. **Explicit current C8 verdict:** BLOCKED unless independently proven against every mandatory row of `BOUNDED_AUTHORIZATION_CONTRACT_V1.md`.

**Do not reduce audit depth or acceptance criteria to satisfy time/token limits; complete all safe possible work and mark anything blocked or not run.**
