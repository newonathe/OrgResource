# Codex next execution prompt — C8 object scope, remaining route oracle and acceptance

Date: 2026-11-23 (Asia/Manila). Authorized C8 PR branch: `feat/extended-c-c8-domain-workflows-2026-11-19`.

## Objective

Continue immediately from the current verified C8 authentication and governance evidence, without reopening the overall architecture. Close the **remaining route-specific** authorization and MySQL postcondition proof in the **owner-approved bounded contract v1**, preserving all working application behavior. Do not mistake the green 88-route anonymous barrier for role/object/tenant authorization acceptance.

## Required read-first sources

- `NEXT_CHAT_START_HERE.md`, `AGENTS.md`, `evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md`
- `evidence/C8/C8_ROUTE_INVENTORY.json`, `evidence/C8/C8_ROUTE_REVIEW_LEDGER.md`
- `evidence/C8/C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md`, `evidence/C8/C8_FINAL_STATUS.md`
- `extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java` (ID01–ID17, C2/C3/C7 and C8Scope–C8DemoDisclosure, new anonymous/governance tests)
- `extended/backend/src/main/java/edu/orgresource/extended/config/C1JerseyConfiguration.java`
- `extended/backend/src/main/java/edu/orgresource/extended/security/C1SecurityConfiguration.java`, `C1OrganizationContext.java`
- `extended/backend/src/main/java/edu/orgresource/extended/rest/*Resource.java` and delegates in services/repositories
- Restricted IR4 evidence only in the authorized local workspace; do not upload the packet, credentials or original demo manifest publicly.

Before doing anything, fetch current GitHub feature/staging HEAD, PR #25 base, and exact CI results; SHA values in prior handoffs are checkpoint snapshots.

## Scope and sequence

1. Verify the existing **93 Jersey registered operations**, **5 public** and **88 protected** routes against the tracked source inventory, relevant HTTP tests and latest exact-SHA CI. The anonymous gate is one refusal class only. Confirm the last governance existing-target denial test actually passed.
2. Review the **remaining 73** routes not yet covered by the 20-operation identity/governance source policy oracle. Work family by family, starting with **InventoryResource (24), ProjectWorkResource (8), AllocationResource (9), ReportingResource (3)**. Then cover DepartmentResource (4), ProjectResource (4), ResourceRequirementResource (4), ResourceResource (7), V2DepartmentResource (3), WorkspacePageResource (6), and DemoMetadataResource (1). Together these sum to 73.
3. For every operation, write the source-based oracle **before** running the request: public/authenticated, selected vs path organization, role, active membership and user, existing target and object scope, allowed fields, lifecycle/version/If-Match, expected HTTP status/code and response data, exact permitted database effects, missing/stale/replayed/foreign target behavior.
4. Reconcile existing ID/C2–C8/Jersey/Playwright/Newman receipts with each oracle by **exact source SHA, actual fixture, command, observed status, body/redaction and database postcondition**. Do not reclassify historical 22,073 unrun matrix cases as passed; bounded v1 is authoritative, but no mandatory behavior is waived.
5. Implement narrowly targeted missing positive and meaningful negative tests on an **isolated MySQL 8** schema and packaged Java 21 backend. For each protected operation include authorized success and refusal with a **real existing fictional object**; for high-risk writes assert unchanged relevant SQL rows/versions, stock movements, audit events and receipts where policy requires it. Use varying identities: foreign tenant, MEMBER, unassigned LEAD, assigned LEAD, revoked membership, CUSTODIAN, ADMIN, OWNER, borrower linked/unlinked. Include consent/contact redaction, selected-vs-path org, stale If-Match, duplicate request IDs, concurrency/restart/rollback/expiry when relevant.
6. Only fix a product defect after reproducing it. Do not relax authorization, inventory invariants, roles, redaction, CSRF or migration history to make a test green. Keep original Java 8 Course A implementation untouched.
7. Have a genuinely separate reviewer review oracle/fixture/patch/receipts. Record reviewer identity/role or mark INDEPENDENT_REVIEW_NOT_RUN. Self-review under another label is not independent review.
8. Run the exact SHA through Course A Java 8/MySQL, Extended C Java 21/MySQL/Newman/Playwright and React typecheck/test/lint/build. Record GitHub links, passed/failed/skipped steps, database checks and any GitHub provisioning failures. Preserve old failure receipts.
9. Update C8 route ledger, full risk/decision status, per-file source review, `NEXT_CHAT_START_HERE.md` and PR #25 with accurate PASS/FAIL/BLOCKED/NOT_RUN, then push public-safe work to feature. Promote to staging only after green source CI and explicit controlled release decision.

## Non-negotiable restrictions

Never merge PR #25 to main; reset or migrate the existing Northflank owner database; publish raw IR4/private source material, recovery codes or credentials; send live SMS; claim independent approval without a real reviewer; run the local-only C8 demo importer against Northflank; insert the 96-organization workload without approved manifest hash and independently verified off-host backup/restore. The exact 96 public labels and fictional history remain a separate future live-data gate.

## Exit packet

- Source and final CI SHA + links; full changed-file review and protected Course A comparison
- 93-operation route policy and evidence matrix; 20+73 source review outcome
- Meaningful per-operation positive/negative/DB receipt mapping, tested tenant/role/object factors
- New defects corrected with before/after evidence; unresolved blockers and independent-review status
- Running hosting SHA verification status and demo import readiness (separate)
- Explicit C8 technical GO/NO-GO; C8 may only be ACCEPTED when every mandatory v1 check and independent review is proven.

This prompt is executable planning guidance, not a claim the requested work was performed.
