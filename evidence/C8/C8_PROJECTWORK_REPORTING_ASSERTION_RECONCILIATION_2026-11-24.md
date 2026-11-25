# C8 Project Work + Reporting: 11-route assertion-level reconciliation

**Date:** 2026-11-24 PHT. **Status:** SOURCE/TEST-ASSERTION MAPPED, **NOT independently accepted**. The baseline is verified successful CI SHA `51b2bfff808a04800e6adba4f80811f35e7e0f06` ([run #45225676030](https://github.com/newonathe/OrgResource/actions/runs/45225676030), 63 ordered C1Identity acceptance tests). This branch introduces `@Order(64)` and must pass **its own** exact-head CI before any claim about that test's execution. No private IR4 receipts available; no independent reviewer.

## Source policy and test sources

- [Project Work and Reporting oracle](C8_PROJECT_WORK_REPORTING_ORACLE_2026-11-23.md), [93-route source index](C8_93_ROUTE_EVIDENCE_CANDIDATE_INDEX_2026-11-24.json), [`ProjectWorkResource`](../../extended/backend/src/main/java/edu/orgresource/extended/rest/ProjectWorkResource.java), [`ProjectWorkComponent`](../../extended/backend/src/main/java/edu/orgresource/extended/service/ProjectWorkComponent.java).
- [`C1IdentityAcceptanceTest`](../../extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java): ordered methods #45, #46, #47 (reporting), #53, #55, #57 (Project Work/lead scope), and new #64 (real-foreign-object refusals and SQL effects). **These are actual asserted HTTP/SQL calls observed in source**, rather than inference from method names.
- The 12-request C6a Newman collection passed 20 status/schema assertions in the same earlier CI, including audit/activity and CSV/JSON exports. This is partial status evidence, not route authorization sign-off.

| ID | HTTP handler and key policy | Existing executable assertions at baseline SHA | New #64 / unresolved work |
| --- | --- | --- | --- |
| R042 | GET `/activity`; OWNER/ADMIN; selected path org | 45: 200/403/404 role, selected-org, foreign; 46: source pagination, cursor, redaction, 500 fail-closed | No #64. Review bounded audit/movement DB read-only receipts and disallowed field set |
| R052 | GET `/audit`; OWNER/ADMIN; selected path org | 45: 200/403/404 role, selection, foreign; 46: 100/105 tie pagination, secret target redaction, limits | No #64. Record no-write DB proof and exact response-field/role coverage |
| R057 | POST `/exports`; OWNER/ADMIN, CSRF; read-only | 45: 200/403/404 and CSRF; 47: CSV/JSON datasets, denied 400/413, CSV injection mitigation, PII redaction, tenant isolation and transaction snapshot | No #64. Independent export SQL no-write/negative response review |
| R060 | POST Project delete; OWNER/ADMIN/assigned LEAD | 55: 409 linked then 200 after cleanup | 64: CUSTODIAN 403, unassigned LEAD/foreign-existing Project 404, SQL unchanged; also check stale/version and Project references |
| R063 | POST Project update; OWNER/ADMIN/assigned LEAD | 55: assigned LEAD 200, 428 missing, 404 foreign-assignment; 57: demotion race | 64: CUSTODIAN 403, unassigned LEAD/foreign-existing Project 404, 428/412, SQL values unchanged |
| R065 | GET Project page; OWNER/ADMIN/CUSTODIAN/assigned LEAD | 55: LEAD assigned-only page and hidden other Project | 64: CUSTODIAN 200, MEMBER 403, unassigned LEAD empty, foreign org 404; examine org/role-bound cursors |
| R066 | POST Requirement delete; OWNER/ADMIN/assigned LEAD | 55: 409 when allocated; 200 after allowed cleanup | 64: CUSTODIAN 403, unassigned LEAD/foreign-existing Requirement 404, 428/412, counts+row SQL unchanged |
| R067 | POST Requirement update; OWNER/ADMIN/assigned LEAD | 55: assigned LEAD valid 200, stale 412 | 64: CUSTODIAN 403, unassigned LEAD/foreign-existing Requirement 404, 428/412 and row SQL unchanged; assess allocation coverage semantics |
| R068 | GET Requirement by ID; scoped to Project | 55: assigned LEAD 200 | 64: CUSTODIAN and assigned LEAD 200 + ETag; MEMBER 403, unassigned LEAD/foreign 404; assess exact field set |
| R069 | GET Requirement page; scoped to Project and LEAD | 55: assigned LEAD page 200, other-Project filtered 404 | 64: CUSTODIAN 200, MEMBER 403, unassigned LEAD empty and outsider 404; inspect page/filters/cursor |
| R070 | POST Requirement create; assigned Project and same-org Resource | 55: assigned LEAD 201, other Project 404 | 64: OWNER 201, CUSTODIAN 403, unassigned LEAD 404, true cross-org Project/Resource 404, denied SQL row/stock/audit stable; ended Project/invalid quantity remain review |

## New test's bounded SQL invariant

Test #64 creates two real fictional orgs, valid Project/Resource/Requirement rows in each, an assigned and unassigned LEAD, a CUSTODIAN and MEMBER. After establishing authorized reads and assigned-LEAD role, it snapshots full two-tenant Project names/versions/lead assignments and Requirement project/resource/quantity/versions. It then submits current-role, foreign-existing-object, missing/stale If-Match refusals and checks the entire snapshot plus Allocation, stock_movement, idempotency_key and audit_event totals unchanged. This assertion covers denied requests **only**, with positives from #55, and still needs exact CI proof.

## Remaining closeout for these 11

- Reconcile all source assertions against the precise v1 policy oracle, real response codes, actual fixture rows and failure-order semantics. Dynamic path construction is represented by its source expression rather than a literal-route-string search.
- Confirm role/field and denied-write SQL requirements where mapped tests have only status-level evidence: especially Reporting GET audit/activity read-only effects, export privacy/limits, bounded ProjectWork cursors and Project/Requirement lifecycle constraints.
- Reconcile restricted IR4 receipts privately and obtain a **different reviewer** to inspect the source, fixtures, all 11 operation notes and CI execution. Do not equate green CI or assistant review with independent sign-off.
- Maintain C8 `BLOCKED` until all mandatory route acceptance evidence for the full 93-route contract is accepted. P0 Northflank private-plan full clone rehearsal and release authorization remain separate.
