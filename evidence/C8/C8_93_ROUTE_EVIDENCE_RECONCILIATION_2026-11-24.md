# C8 93-route evidence reconciliation — candidate index and finite closeout

**Date:** 2026-11-24 (PHT). **Status:** `CANDIDATE_INDEX_COMPLETE / FORMAL_C8_ACCEPTANCE_BLOCKED`. **Source HEAD audited:** `64d44cb6d64027b2e26860f250a255b4fa3c0e1a` (draft PR #32). The present audit contains no product runtime or hosted database changes.

> **Interpretation rule:** `POSTMAN` and `JAVA-LITERAL` below mean **a possible evidence pointer**, never an independently checked per-route positive/negative/SQL receipt. `FAMILY` refers to related tested workflows; it is not proof the particular handler executed. A blank cell does not mean an endpoint was never tested: tests build paths dynamically and private IR4 receipts are inaccessible.

## What was verified versus what remains

| Dimension | Evidence and exact result | C8 closure meaning |
| --- | --- | --- |
| Jersey source inventory | `C8_ROUTE_INVENTORY.json`: **93** distinct method/path pairs, **38 GET + 55 POST**, 15 Jersey classes | Source identity counted; packaged handler and delegate parity still merits reviewer check |
| Policy preparation | 20 identity/governance + 11 Project Work/reporting + 33 Inventory/Allocation + 29 legacy/workspace = **93 candidate source policy oracles** | Coverage of draft policies, not independent policy approval |
| Shared anonymous boundary | Java `@Order(59)`: **88 protected routes**, 401 and business-row counts; [exact-sha CI](https://github.com/newonathe/OrgResource/actions/runs/45220719360) | One shared negative class only; the five intentionally public operations require public behavior/privacy cases |
| Shared authenticated POST boundary | Java `@Order(63)`: **55 POSTs × three requests = 165**, hostile/missing Origin, missing CSRF and 14 MySQL table-count checks; same CI | Middleware denial proof; not role, tenant, object or allowed-field policy proof |
| Targeted Java run | **63** `C1IdentityAcceptanceTest` methods and **88** total Maven tests, zero failures/errors/skips; 38 frontend Vitest and 19 Playwright passed | Executed tests exist; their route-wise receipts/DB assertions must be mapped |
| Extended Newman execution | **35/93** distinct route signatures across 60 requests; exact CI logs: **48 requests / 90 assertions, 0 failed** plus **12 requests / 20 assertions, 0 failed**. Every request has an explicit HTTP-status expectation, verified against its collection script. | **Verified partial status-level receipts for 35 routes**; role/object/SQL/field controls still need review |
| Direct Java path literal matching | **26/93** route strings occur as complete quoted path literals in non-generic Java test methods; **14** overlap Postman; union **47**; **46** neither | *Static text presence only*; Java dynamic route construction is common. No claim that 46 routes are untested |
| Formal v1 route receipt mapping | **0 newly accepted** by this index | Every route still needs a reviewer-verified authorized case, meaningful refusal or appropriate public behavior, fields and effects; use prior receipts where demonstrably valid |
| Separate independent reviewer and restricted IR4 reconciliation | Not performed in this work | Mandatory gate stays OPEN |

## Newman status-level receipt upgrade

**VERIFIED CI FACT:** [exact source run 45220719360](https://github.com/newonathe/OrgResource/actions/runs/45220719360) executed the entire 48-request C3 collection (90 assertions, zero failed) and 12-request C6a collection (20 assertions, zero failed). Source inspection confirms all 48 C3 names map to an explicit top-level expected HTTP status and all 12 C6a requests assert an explicit status. The **35 distinct matching method/path routes therefore have at least one executed HTTP-status assertion**. The JSON index records the request name and expected status for each matching route. These are **PARTIAL per-route receipts**, and this work cannot infer approved role boundaries, tenant/object targets, full response fields, denied-write SQL invariants or separate reviewer approval merely from Newman success.

## Assertion-level deep review, batch 1 (2026-11-24)

The [Project Work + Reporting 11-route assertion reconciliation](C8_PROJECTWORK_REPORTING_ASSERTION_RECONCILIATION_2026-11-24.md) inspects Java test sources #45–47, #53, #55 and #57, and maps their real request/SQL assertions to 11 source-policy handlers. New `@Order(64)` adds a real two-tenant Project/Requirement denied-write SQL invariant; it is **not executed until the current branch's exact CI passes**. These are **source-inspected partial route receipts**, pending independent reviewer, restricted IR4 cross-check and remaining policy factors. No operation is promoted to formally accepted from the audit alone.

## Assertion-level source reconciliation, batch 2 (33 Inventory/Allocation operations)

The [Inventory/Allocation reconciliation](C8_INVENTORY33_ASSERTION_RECONCILIATION_2026-11-24.md) connects the source oracle I01–I24 and L01–L09 to actual named Java assertions and the executed legacy Allocation Newman requests, including role/state, borrower redaction, idempotency, physical ledger and foreign-tenant evidence. The accompanying JSON index has route-specific `inventory_33_assertion_reconciliation` entries, with remaining missing subfactors explicitly identified. New `@Order(65)` directly tests previously unexercised **lead-options** and requires exact-head CI; it is not claimed green in this document.

**Detailed source-to-test mapping now covers 44/93 operations across two review batches (11 Project Work/Reporting + 33 Inventory/Allocation).** This measures review navigation, **not** formal acceptance. Remaining review-mapping families: 20 identity/governance and 29 legacy/workspace. Independent per-route acceptance remains 0/93 from this audit, and the C8 contract stays BLOCKED.

## Assertion-level source reconciliation, batch 3 (20 Identity/Governance operations)

The [Identity/Governance 20-route assertion reconciliation](C8_IDENTITY20_ASSERTION_RECONCILIATION_2026-11-24.md) cross-references actual `C1AuthResource`, `C1SessionResource`, `C1InvitationResource`, `C1GovernanceResource` and service contracts against individual executed `C1IdentityAcceptanceTest` assertions. It includes 20 route IDs (**R029–R036, R041, R077–R087**), exact Java `@Order` source ranges, authorized/denied statuses, selected SQL/field evidence, and honest missing factors. Importantly, **R082/member-directory already has a positive cursor page in `@Order(20)`**—it is not an untested handler. One gap was a nondeterministic account-disable success in `@Order(12)`; new directed `@Order(66)` was added in draft PR #39 to test deterministic non-last-owner 204, incorrect-current-password 401, post-disable session revocation, credential version, OWNER preservation and audit count. **Do not mark that new test executed until PR #39 final-head CI passes.**

**Cumulative detailed source-to-test navigation now covers 64/93 routes (11 Project Work/Reporting + 33 Inventory/Allocation + 20 Identity/Governance). The remaining review family is 29 Legacy/Workspace routes. This is still 0/93 independently accepted by this reconciliation; private IR4, individual final receipts and separate reviewer approval remain OPEN.**
## How to read the route table

`P` = **executed Newman request name(s)** matching HTTP method and normalized path with a verified status assertion; `J` = exact full-path **literal occurrence** in a named ordered Java test, with generic anonymous/middleware loops excluded; `F` = related **family review leads**. The `J` and `F` columns are pointers only; `P` proves a specific status test ran, and nothing beyond its asserted checks. Request names and full Java method names are retained in [the companion JSON index](C8_93_ROUTE_EVIDENCE_CANDIDATE_INDEX_2026-11-24.json). The audit does **not** treat a path mention as an assertion, or 165 middleware refusals as 55 completed individual authorization policies.

All rows inherit `route-specific positive = NOT_RECONCILED`, `meaningful negative = NOT_RECONCILED`, `SQL/field/privacy = NOT_RECONCILED`, `independent review = NOT_PERFORMED`. This records **unmapped evidence**, not absence of past tests. Follow the four oracle links: [I](C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md), [P](C8_PROJECT_WORK_REPORTING_ORACLE_2026-11-23.md), [V](C8_INVENTORY_ALLOCATION_SOURCE_ORACLE_2026-11-23.md), [L](C8_LEGACY_WORKSPACE_SOURCE_ORACLE_2026-11-23.md).

| ID | HTTP + path | Oracle | P: Extended Postman candidates | J: Java exact literal @Order | F: Java family leads |
| --- | --- | :---: | --- | --- | --- |
| R001 | `POST /api/allocations/{id}/approve` | V | A03, A03-CONSUMABLE | — | #20, #21, #22, #23, #29 |
| R002 | `POST /api/allocations/{id}/cancel` | V | A07 | — | #20, #21, #22, #23, #29 |
| R003 | `POST /api/allocations/{id}/consume` | V | A06 | — | #20, #21, #22, #23, #29 |
| R004 | `POST /api/allocations/{id}/delete` | V | A08-REFUSED, A08 | — | #20, #21, #22, #23, #29 |
| R005 | `POST /api/allocations/{id}/issue` | V | A04 | — | #20, #21, #22, #23, #29 |
| R006 | `POST /api/allocations/{id}/return` | V | A05 | — | #20, #21, #22, #23, #29 |
| R007 | `POST /api/allocations/{id}/revoke-sms-consent` | V | A09 | — | #20, #21, #22, #23, #29 |
| R008 | `GET /api/allocations` | V | A02, A02-STATUS, A02-REQUIREMENT | 20, 21, 22, 23, 25 +10 | #20, #21, #22, #23, #29 |
| R009 | `POST /api/allocations` | V | A01, A01-CONSUMABLE, A01-CANCEL | 20, 21, 22, 23, 25 +10 | #20, #21, #22, #23, #29 |
| R010 | `POST /api/departments/{id}/delete` | L | D04 | — | #29, #33, #61, #62 |
| R011 | `POST /api/departments/{id}/update` | L | D03 | — | #29, #33, #61, #62 |
| R012 | `GET /api/departments` | L | D02 | 1, 2, 4, 5, 9 +21 | #29, #33, #61, #62 |
| R013 | `POST /api/departments` | L | PREP-01, D01 | 1, 2, 4, 5, 9 +21 | #29, #33, #61, #62 |
| R014 | `POST /api/projects/{id}/delete` | L | P04 | — | #29, #33, #55, #61, #62 |
| R015 | `POST /api/projects/{id}/update` | L | P03 | — | #29, #33, #55, #61, #62 |
| R016 | `GET /api/projects` | L | P02 | 20, 21, 22, 23, 25 +14 | #29, #33, #55, #61, #62 |
| R017 | `POST /api/projects` | L | PREP-02, P01 | 20, 21, 22, 23, 25 +14 | #29, #33, #55, #61, #62 |
| R018 | `POST /api/requirements/{id}/delete` | L | Q04-REFUSED, Q04 | — | #29, #33, #55, #61 |
| R019 | `POST /api/requirements/{id}/update` | L | Q03 | — | #29, #33, #55, #61 |
| R020 | `GET /api/requirements` | L | Q02 | 20, 21, 22, 23, 25 +11 | #29, #33, #55, #61 |
| R021 | `POST /api/requirements` | L | PREP-04, PREP-06, Q01 | 20, 21, 22, 23, 25 +11 | #29, #33, #55, #61 |
| R022 | `GET /api/resources/{id}/availability` | L | R05 | — | #29, #31, #33, #39, #61 |
| R023 | `POST /api/resources/{id}/delete` | L | R04-REFUSED, R04 | — | #29, #31, #33, #39, #61 |
| R024 | `GET /api/resources/{id}/image` | L | IMAGE-02 | — | #29, #31, #33, #39, #61 |
| R025 | `POST /api/resources/{id}/image` | L | IMAGE-01 | — | #29, #31, #33, #39, #61 |
| R026 | `POST /api/resources/{id}/update` | L | R03, R03-STALE | — | #29, #31, #33, #39, #61 |
| R027 | `GET /api/resources` | L | R02 | 6, 18, 20, 21, 22 +26 | #29, #31, #33, #39, #61 |
| R028 | `POST /api/resources` | L | PREP-03, PREP-05, R01 | 6, 18, 20, 21, 22 +26 | #29, #31, #33, #39, #61 |
| R029 | `POST /api/v2/account/disable` | I | — | 12 | #5, #6, #8, #12, #13 |
| R030 | `POST /api/v2/account/password` | I | — | 8 | #5, #6, #8, #12, #13 |
| R031 | `POST /api/v2/account/recovery-codes/rotate` | I | — | 8 | #5, #6, #8, #12, #13 |
| R032 | `GET /api/v2/account` | I | — | 16 | #5, #6, #8, #12, #13 |
| R033 | `GET /api/v2/auth/csrf` | I | AUTH-01, AUTH-05 | 5, 7, 8, 9, 18 +4 | #5, #7, #8, #18, #48 |
| R034 | `POST /api/v2/auth/login` | I | AUTH-02 | 5, 7, 18, 45, 47 | #5, #7, #8, #18, #48 |
| R035 | `POST /api/v2/auth/logout` | I | — | 5, 18 | #5, #7, #8, #18, #48 |
| R036 | `POST /api/v2/auth/recover` | I | — | 8, 48 | #5, #7, #8, #18, #48 |
| R037 | `GET /api/v2/csrf` | L | — | 61 | #61, #62 |
| R038 | `GET /api/v2/demo/metadata` | L | — | 58 | #58 |
| R039 | `GET /api/v2/departments/{id}` | L | — | — | #61, #62 |
| R040 | `POST /api/v2/departments` | L | — | 61 | #61, #62 |
| R041 | `POST /api/v2/invitations/redeem` | I | — | 9 | #9 |
| R042 | `GET /api/v2/orgs/{organizationId}/activity` | P | C6A-02, C6A-03 | — | #45, #46, #47, #51, #58 |
| R043 | `POST /api/v2/orgs/{organizationId}/allocations/{allocationId}/approve` | V | — | — | #20, #21, #22, #23, #24 |
| R044 | `POST /api/v2/orgs/{organizationId}/allocations/{allocationId}/cancel` | V | — | — | #20, #21, #22, #23, #24 |
| R045 | `POST /api/v2/orgs/{organizationId}/allocations/{allocationId}/consume` | V | — | — | #20, #21, #22, #23, #24 |
| R046 | `POST /api/v2/orgs/{organizationId}/allocations/{allocationId}/delete` | V | — | — | #20, #21, #22, #23, #24 |
| R047 | `POST /api/v2/orgs/{organizationId}/allocations/{allocationId}/issue` | V | — | — | #20, #21, #22, #23, #24 |
| R048 | `POST /api/v2/orgs/{organizationId}/allocations/{allocationId}/return` | V | — | — | #20, #21, #22, #23, #24 |
| R049 | `GET /api/v2/orgs/{organizationId}/allocations/{allocationId}` | V | — | — | #20, #21, #22, #23, #24 |
| R050 | `GET /api/v2/orgs/{organizationId}/allocations` | V | — | — | #20, #21, #22, #23, #24 |
| R051 | `POST /api/v2/orgs/{organizationId}/allocations` | V | — | — | #20, #21, #22, #23, #24 |
| R052 | `GET /api/v2/orgs/{organizationId}/audit` | P | C6A-01 | — | #45, #46, #47, #51, #58 |
| R053 | `POST /api/v2/orgs/{organizationId}/borrowed-allocations/{allocationId}/revoke-sms-consent` | V | — | — | #20, #21, #22, #23, #24 |
| R054 | `GET /api/v2/orgs/{organizationId}/borrowed-allocations/{allocationId}` | V | — | — | #20, #21, #22, #23, #24 |
| R055 | `GET /api/v2/orgs/{organizationId}/borrowed-allocations` | V | — | — | #20, #21, #22, #23, #24 |
| R056 | `GET /api/v2/orgs/{organizationId}/borrowers` | V | — | — | #20, #21, #22, #23, #24 |
| R057 | `POST /api/v2/orgs/{organizationId}/exports` | P | C6A-04, C6A-05, C6A-06, C6A-07 | — | #45, #46, #47, #51, #58 |
| R058 | `GET /api/v2/orgs/{organizationId}/operations/{operationId}` | V | — | — | #20, #21, #22, #23, #24 |
| R059 | `GET /api/v2/orgs/{organizationId}/projects/{projectId}/allocations` | V | — | — | #20, #21, #22, #23, #24 |
| R060 | `POST /api/v2/orgs/{organizationId}/projects/{projectId}/delete` | P | — | — | #53, #55, #57 |
| R061 | `POST /api/v2/orgs/{organizationId}/projects/{projectId}/lead-assignment` | V | — | — | #20, #21, #22, #23, #24 |
| R062 | `GET /api/v2/orgs/{organizationId}/projects/{projectId}/lead-options` | V | — | — | #20, #21, #22, #23, #24 |
| R063 | `POST /api/v2/orgs/{organizationId}/projects/{projectId}/update` | P | — | — | #53, #55, #57 |
| R064 | `GET /api/v2/orgs/{organizationId}/projects/{projectId}` | V | — | — | #20, #21, #22, #23, #24 |
| R065 | `GET /api/v2/orgs/{organizationId}/projects` | P | — | — | #53, #55, #57 |
| R066 | `POST /api/v2/orgs/{organizationId}/requirements/{requirementId}/delete` | P | — | — | #53, #55, #57 |
| R067 | `POST /api/v2/orgs/{organizationId}/requirements/{requirementId}/update` | P | — | — | #53, #55, #57 |
| R068 | `GET /api/v2/orgs/{organizationId}/requirements/{requirementId}` | P | — | — | #53, #55, #57 |
| R069 | `GET /api/v2/orgs/{organizationId}/requirements` | P | — | — | #53, #55, #57 |
| R070 | `POST /api/v2/orgs/{organizationId}/requirements` | P | — | — | #53, #55, #57 |
| R071 | `POST /api/v2/orgs/{organizationId}/resources/{resourceId}/adjust` | V | — | — | #20, #21, #22, #23, #24 |
| R072 | `POST /api/v2/orgs/{organizationId}/resources/{resourceId}/image/delete` | V | — | — | #20, #21, #22, #23, #24 |
| R073 | `GET /api/v2/orgs/{organizationId}/resources/{resourceId}/movements` | V | — | — | #20, #21, #22, #23, #24 |
| R074 | `POST /api/v2/orgs/{organizationId}/resources/{resourceId}/receive` | V | — | — | #20, #21, #22, #23, #24 |
| R075 | `GET /api/v2/orgs/{organizationId}/resources/{resourceId}` | V | — | — | #20, #21, #22, #23, #24 |
| R076 | `GET /api/v2/orgs/{organizationId}/resources` | V | — | — | #20, #21, #22, #23, #24 |
| R077 | `POST /api/v2/orgs/{orgId}/accept-transfer` | I | — | — | #9, #10, #11, #12, #50 |
| R078 | `POST /api/v2/orgs/{orgId}/invitations/{invitationId}/revoke` | I | — | — | #9, #10, #11, #12, #50 |
| R079 | `POST /api/v2/orgs/{orgId}/invitations` | I | — | — | #9, #10, #11, #12, #50 |
| R080 | `POST /api/v2/orgs/{orgId}/members/{memberId}/role` | I | — | — | #9, #10, #11, #12, #50 |
| R081 | `POST /api/v2/orgs/{orgId}/members/{memberId}/suspend` | I | — | — | #9, #10, #11, #12, #50 |
| R082 | `GET /api/v2/orgs/{orgId}/members` | I | — | — | #9, #10, #11, #12, #50 |
| R083 | `POST /api/v2/orgs/{orgId}/transfer-ownership` | I | — | — | #9, #10, #11, #12, #50 |
| R084 | `POST /api/v2/orgs/{orgId}/transfers/{transferId}/revoke` | I | — | — | #9, #10, #11, #12, #50 |
| R085 | `GET /api/v2/orgs` | I | AUTH-03 | 1, 6, 47 | #5, #6, #8, #12, #13 |
| R086 | `POST /api/v2/session/active-org` | I | AUTH-04 | 4, 6, 9, 18, 36 +3 | #5, #6, #8, #12, #13 |
| R087 | `GET /api/v2/session` | I | — | 5, 6, 8, 9, 13 +2 | #5, #6, #8, #12, #13 |
| R088 | `GET /api/v2/workspace/allocations` | L | — | — | #52, #61, #62 |
| R089 | `GET /api/v2/workspace/departments` | L | — | — | #52, #61, #62 |
| R090 | `GET /api/v2/workspace/overview` | L | — | 61, 62 | #52, #61, #62 |
| R091 | `GET /api/v2/workspace/projects` | L | — | — | #52, #61, #62 |
| R092 | `GET /api/v2/workspace/requirements` | L | — | — | #52, #61, #62 |
| R093 | `GET /api/v2/workspace/resources` | L | — | — | #52, #61, #62 |

## Closeout execution order — **no more blind broad tests**

1. **Reconcile rather than re-run everything.** For each R001–R093, inspect its specified oracle and candidate Java/Postman source; trace an actual route invocation and assertion. Enter an **exact remote commit, CI job, named test or Newman request, role/fixture, real existing object, response status/code/headers, field restrictions, relevant SQL before/after or immutable receipt, and reviewer name/date** in the restricted acceptance ledger. Do not copy credentials, contact payloads or private case receipts into the public repository. Review prior IR4 199 recorded passes and 48 structural exclusions by original IDs; the 22,073 historically unrun cases are not a v1 required test count.
2. **Issue only uncovered directed cases**, grouped by oracle family rather than automatically making 93 new tests. First inventory/allocation **33** (stock/ledger states, path membership, borrowed self-only, receipt replay, concurrency); then Project Work/reporting **11** (assigned lead, project rights, exports/redaction); then legacy/workspace/demo **29** (selected org vs path, role asymmetry, image, cursors, demo classification); then identity/governance **20** (auth, ownership, invitations, account recovery, versioned writes). This order is a *recommended review order*, not a claim all cases are absent.
3. **For protected GET:** require valid authorized response and meaningful denied/current-member foreign or object/role case, plus allowed field set and no disclosure or writes. **For protected POST:** require authorized state change, meaningful denied same-org role or real foreign object, lifecycle/precondition/field checks, exact affected MySQL counts and values, no forbidden ledger/audit/receipt mutations; keep concurrency and restart receipts where applicable. **For the five public endpoints:** verify intended behavior, privacy, invalid/expired cases and no unintended mutation. Use the actual HTTP verb: legacy deletes are POST.
4. **Independent review gate:** before acceptance a different reviewer checks oracle correctness, the route/handler mapping (including packaged dispatch, media types and delegates), each fixture and receipt, false positives, tenant/object/field and SQL controls, and unresolved mandatory findings. Record REVIEWED/PASS per route only on demonstrated evidence. Reviewer must have access to the restricted IR4 packet privately; if unavailable, mark that access unresolved.
5. **Final exact SHA:** rerun Java 8/MySQL Course A, React, Extended C Java 21/MySQL/Newman/Playwright (not a source-only check), ensure frozen Course A unchanged, resolve all mandatory ledger findings and explicitly approve the accepted revision. Do not label C8 accepted from this candidate index.

## Track separation / stop gates

- **Formal C8**: remains `BLOCKED`; the existence of this source-to-test index closes only the *discovery* task. The source-only oracle counts, Java test pass and Newman's green job are real but do not grant a route-wise independent sign-off.
- **P0 Northflank demo**: [issue #26](https://github.com/newonathe/OrgResource/issues/26) requires verified private 96-workspace source, hosted inventory, independent off-host backup/restore, 96-workspace clone rehearsal and explicit owner approval. `LIVE_IMPORT_NOT_RUN`. It is a **separate host/operator gate** and must not be satisfied by the 3/8-workspace CI fixtures.
- **C9**: remains a separately authorized performance/scale phase after the required C8 acceptance. Proposed DEC-D01 department visibility stays unimplemented and must not modify V7 import prerequisites.
- **Academic**: frozen Course A and actual assignment/Canvas obligations are independently tracked; neither C8 nor Northflank is automatically required for the course submission.

**Evidence taxonomy:** VERIFIED REPOSITORY FACT = exact inventory, concrete test source, and CI; DESIGN RECOMMENDATION = review order and closeout workflow; OPEN QUESTION = per-route acceptance receipts, independent reviewer, IR4 private packet and hosted live verification.
