# Single-pass C8 corrective task for Codex / next implementer

**Source:** Read NEXT_CHAT_START_HERE.md and AGENTS.md first. Check live branch refs and PR #25 before editing. Read the actual Jersey sources, security middleware, inventory services, bounded contract, 93-route inventory and evidence ledger, restricted IR4 receipts if available locally, and release/academic docs.

Goal: complete the bounded C8 gate through actual evidence, not another abstract plan. The new route inventory is static source coverage, not acceptance. In one coordinated workstream:
- Verify 93 declared routes against packaged Jersey handler registration; identify missing/extra routes and DTO/media mismatches.
- For each route create independently reviewed authentication/tenant/membership/role/object/lifecycle/field/error/DB-effect oracle. Group equivalent middleware cases carefully, then add targeted HTTP+MySQL positive/negative evidence for meaningful exceptions; do not infer equivalence or recast 22,073 historical unrun cases.
- Audit role changes, revoked memberships, OWNED pending requests, LEAD scoping, borrower links/consent redaction, privacy, reporting, image paths, receipts/replay, concurrency, restart and rollback. For denied high-risk mutations assert unchanged relevant database state.
- Correct defects with minimal source changes; use realistic fictional fixtures; rerun relevant tests and exact-head CI. Preserve Course A and Flyway history.
- Record exact SHA, commands, observed failures and passes, open gates and a genuine separate independent review. Do not claim acceptance if a mandatory item remains.
- In parallel, implement a distinctly opt-in additive hosted demo loader ONLY after approval of the design in docs/deployment/LIVE_C8_DEMO_IMPORT_DECISION_2026-11-23.md. The local-only importer MUST remain fail-closed. Never run against Northflank or alter the live DB before manifest hash, isolated backup/restore and owner approval.
- Keep NEXT_CHAT_START_HERE.md, ledger and per-file evidence current and push source-safe work to the draft PR branch. Promote to staging only through a tested, explicitly approved release path. Do not merge or claim deployed just because a build passed.

Mandatory exit report: source SHAs, changed paths, exact tests run, route/gate matrix, gaps, independent reviewer outcome, CD/run status, rollback, private-only artifact requests and remaining blockers. User wants rapid iterative completion; avoid repetitive cycles by aggregating verified changes in one reviewable batch.
