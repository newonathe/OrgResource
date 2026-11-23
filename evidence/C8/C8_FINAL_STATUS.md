# C8 bounded technical acceptance status

## 2026-11-24 candidate evidence reconciliation

**C8 STILL BLOCKED.** [Exact CI run 45220719360](https://github.com/newonathe/OrgResource/actions/runs/45220719360) succeeded for Java 8/Course A, React, and Extended C Java 21/MySQL/Newman/Playwright at SHA `64d44cb6d64027b2e26860f250a255b4fa3c0e1a`. Source-level oracles cover 93/93 routes; @Order(59) and @Order(63) passed shared anonymous and POST middleware negative classes. Two executed Extended Newman collections generated **60 requests and 110 assertions**, 0 failures; **35 method/path routes** have verified partial HTTP-status assertion evidence. The [reconciliation matrix](C8_93_ROUTE_EVIDENCE_RECONCILIATION_2026-11-24.md) maps all 93 source policies and Java/Postman candidate references, explicitly leaves route-specific authorized/denied and SQL/field acceptance `NOT_RECONCILED`, and requires independent reviewer confirmation. No C8 security sign-off, merge, live Northflank import or C9 authorization is inferred.


**C8 BLOCKED — SPECIFIC MANDATORY PROOF OR CORRECTION REMAINS.**

**November 23 source-coverage update (draft audit PR, no product runtime changes):** The added [last 29-route source oracle](C8_LEGACY_WORKSPACE_SOURCE_ORACLE_2026-11-23.md) completes the *source-only policy inventory* to **93/93 routes**, verified against the annotated inventory/handler names with a CI drift check. **This does not establish any of the required authorized HTTP, tenant/object/role refusal, denied-write SQL, independent reviewer or hosted database acceptance receipts. C8 remains BLOCKED.** Earlier chronological 31/64 source-count checkpoints below are retained for provenance.
 The owner-approved v1 gate applies to the 93 Jersey operations. The historical candidate ledger remains intact with 199 passes, 48 reviewed structural exclusions and 22,073 unrun cases. Those unrun cases are not a mandatory total under v1. [Acceptance contract](BOUNDED_AUTHORIZATION_CONTRACT_V1.md)

The restricted evidence packet contains the exact route inventory, local Java 21/MySQL checks, independent review and open operation-level gaps. Directed tests cover Project Lead assignment and owned work, cross-tenant refusal and linked-borrower consent. Final authorization-policy review and meaningful positive and negative execution still need closure for multiple route families. Prior stock concurrency and 118,867-row local restore evidence retain their documented scope. The protected Course A source and artifacts remain unchanged.

The first two CI attempts on the contract commit failed while extracting the Java 8 download, before Java tests began. Both Extended and React jobs passed. The workflow now verifies the official Temurin Java 8 archive against its published SHA-256 before handing it to setup-java and checks out the exact PR head for all three jobs. [Provisioning failures](https://github.com/newonathe/OrgResource/actions/runs/44727842454) · [Draft PR #25](https://github.com/newonathe/OrgResource/pull/25)

C9 remains on hold. Public release, hosting, live messaging, reuse of restricted course materials and academic submission remain separate.

## 2026-11-23 continuation (does not close C8)

A fresh source-level enumeration found 93 distinct HTTP method/path declarations across 15 Jersey resource classes. The tracked inventory and independent-test-required review ledger now live at [C8_ROUTE_INVENTORY.json](C8_ROUTE_INVENTORY.json) and [C8_ROUTE_REVIEW_LEDGER.md](C8_ROUTE_REVIEW_LEDGER.md); CI checks annotation drift via scripts/verify-c8-routes.mjs. This static test does not establish packaged route registration, a route policy oracle, positive/negative HTTP cases, denied-write DB postconditions, or independent review. **C8 remains BLOCKED.**

The prior exact-feature-sha GitHub Actions run at c0409a98a3bd passed all three jobs (Java 8, Extended C and React): https://github.com/newonathe/OrgResource/actions/runs/45062378023 . This does not yet establish that the new inventory check passed on its subsequent commit. Northflank separately reported a successful build of staging SHA 89c28614e24f; which container is currently running still needs inspection. Detailed restricted C8 receipts remain local-only; do not relabel earlier unrun cases as passed.

## Authorization boundary checkpoint (2026-11-23)

At feature SHA `5a14714e868c4fa9588b213f555f461f640bb690`, the Java 21 isolated MySQL CI step passed the added source-derived **88 protected Jersey route anonymous HTTP denial** integration test (see [run 45065909845](https://github.com/newonathe/OrgResource/actions/runs/45065909845)). The test supplies pre-auth CSRF on protected POSTs and verifies 401/AUTH_REQUIRED, a secret-nondisclosing envelope and unchanged business-table counts. This is **a single authentication-boundary negative class**; it is not evidence of successful operations, full role-policy mapping, tenant/object denials or independent authorization review. Public routes (5) remain outside this loop. Exact full CI job outcome was pending at the status-write time.

A source-reviewed pre-execution policy oracle for 20 Identity/Governance operations is recorded in [C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md](C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md); separate review and actual route-specific receipts are open. **C8 remains BLOCKED.** Do not retroactively change historical IR4 ledger cases.

## 2026-11-23 incremental source review and hosted UI issue

- Source policy oracles now documented for 31/93 annotated operations: 20 identity/governance + 8 Project Work + 3 Reporting; 62 routes remain to source-review. Counts are preparatory review only; individual receipts and independent acceptance remain UNMAPPED.
- Prior C8 governance refusal and 88-route anonymous barrier tests were fully green at bb3f1a8d ([run](https://github.com/newonathe/OrgResource/actions/runs/45067408499)). C8 remains BLOCKED despite these tests.
- Hosted hard-refresh loading display was nearly blank and used a single 10-second metadata timeout. UX/transient-retry correction e72e32b2 was pushed to feature, not yet verified full-green or deployed at this note. Strict demo classification remains fail-closed; hosted workspaces must not show before public metadata confirmation.

## Hosted refresh CI/release result (2026-11-23)

Feature source d16485790539618373577d52ea7322453c903655 passed all three GitHub Actions jobs ([run 45069092871](https://github.com/newonathe/OrgResource/actions/runs/45069092871)), including browser recovery after temporary provenance endpoint 503 and fail-closed rejection of malformed classification. The tested fix was selectively promoted as staging commit 32a4687bd745bfd37b547867e5a72ae1ea623794. Northflank build status was still pending at this note; running image and user hard-refresh proof remain unverified. UI fix does not close C8 authorization. Formal gate remains BLOCKED.
