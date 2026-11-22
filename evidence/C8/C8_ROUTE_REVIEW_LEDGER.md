# C8 source route and authorization evidence ledger

**As of:** 2026-11-23 (Asia/Manila) — **SOURCE INVENTORY VERIFIED; TECHNICAL ACCEPTANCE BLOCKED.**


**Latest source-only coverage, 2026-11-23:** [Inventory + Allocation oracle (33 method/path pairs)](C8_INVENTORY_ALLOCATION_SOURCE_ORACLE_2026-11-23.md), source reviewed at SHA `5b1c4826`. Added to [Project Work + Reporting (11)](C8_PROJECT_WORK_REPORTING_ORACLE_2026-11-23.md) and [identity/governance (20)](C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md), this yields **64/93 SOURCE-ORACLED, zero accepted on the strength of these drafts, 29 still missing source policy oracles**. These 33 were matched to the existing JSON annotated inventory; packaged route registration is not proven by this comparison. For **all 93**, meaningful authorized/denied HTTP, exact MySQL before/after effects, restricted IR4 receipt reconciliation and independent review remain UNMAPPED / NOT_RUN in this work. **C8 BLOCKED.** Prior C8 browser CI failure is tracked in [cycle 1](C8_CYCLE_01_REQUIREMENT_POST_DIAGNOSTIC_2026-11-23.md). No hosted classification fix can substitute for authorization acceptance.

This document inventories 93 source-annotated Jersey endpoints. It is not proof of packaged registration, authenticated behavior, row-level scope, or route authorization. The bounded contract remains [BOUNDED_AUTHORIZATION_CONTRACT_V1.md](BOUNDED_AUTHORIZATION_CONTRACT_V1.md), and acceptance remains [C8_FINAL_STATUS.md](C8_FINAL_STATUS.md).


**2026-11-23 coverage supplement:** [20 identity/governance operations source policy oracle](C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md). A new dynamic HTTP integration test exercised 88 protected operations anonymously with 401 and unchanged business table counts on source SHA `5a14714e868c` ([backend step in run 45065909845](https://github.com/newonathe/OrgResource/actions/runs/45065909845)); the whole CI run was not finished when recorded. This is one shared anonymous-denial factor, **not** per-route authorized behavior or independent policy acceptance. Existing per-operation receipt states stay UNMAPPED pending review.

Private IR4 case ledger and independent receipts were not made available in this chat. Previously reported passes should be mapped to concrete scenarios and exact SHAs by an authorized reviewer before reuse.

Source-of-truth JSON: [C8_ROUTE_INVENTORY.json](C8_ROUTE_INVENTORY.json). CI drift check: [verify-c8-routes.mjs](../../scripts/verify-c8-routes.mjs). **The 64 source-oracled routes have SOURCE_ONLY policy drafts; the remaining 29 are UNREVIEWED for policies. Every route below still has positive/negative receipts UNMAPPED, database postconditions UNMAPPED and independent reviewer NONE for this bounded acceptance ledger.** These labels do not assert that past tests were absent; their mapping remains unverified.

| Class | HTTP | Route | Java handler | Triage hint (not a permission oracle) |
| --- | --- | --- | --- | --- |
| AllocationResource | POST | `/api/allocations/{id}/approve` | `approve` | HIGH: mutation/tenant/role/DB effect |
| AllocationResource | POST | `/api/allocations/{id}/cancel` | `cancel` | HIGH: mutation/tenant/role/DB effect |
| AllocationResource | POST | `/api/allocations/{id}/consume` | `consume` | HIGH: mutation/tenant/role/DB effect |
| AllocationResource | POST | `/api/allocations/{id}/delete` | `delete` | HIGH: mutation/tenant/role/DB effect |
| AllocationResource | POST | `/api/allocations/{id}/issue` | `issue` | HIGH: mutation/tenant/role/DB effect |
| AllocationResource | POST | `/api/allocations/{id}/return` | `returnAllocation` | HIGH: mutation/tenant/role/DB effect |
| AllocationResource | POST | `/api/allocations/{id}/revoke-sms-consent` | `revokeSmsConsent` | HIGH: mutation/tenant/role/DB effect |
| AllocationResource | GET | `/api/allocations` | `list` | SCOPED READ: auth/tenant/redaction |
| AllocationResource | POST | `/api/allocations` | `create` | HIGH: mutation/tenant/role/DB effect |
| DepartmentResource | POST | `/api/departments/{id}/delete` | `delete` | HIGH: mutation/tenant/role/DB effect |
| DepartmentResource | POST | `/api/departments/{id}/update` | `update` | HIGH: mutation/tenant/role/DB effect |
| DepartmentResource | GET | `/api/departments` | `list` | SCOPED READ: auth/tenant/redaction |
| DepartmentResource | POST | `/api/departments` | `create` | HIGH: mutation/tenant/role/DB effect |
| ProjectResource | POST | `/api/projects/{id}/delete` | `delete` | HIGH: mutation/tenant/role/DB effect |
| ProjectResource | POST | `/api/projects/{id}/update` | `update` | HIGH: mutation/tenant/role/DB effect |
| ProjectResource | GET | `/api/projects` | `list` | SCOPED READ: auth/tenant/redaction |
| ProjectResource | POST | `/api/projects` | `create` | HIGH: mutation/tenant/role/DB effect |
| ResourceRequirementResource | POST | `/api/requirements/{id}/delete` | `delete` | HIGH: mutation/tenant/role/DB effect |
| ResourceRequirementResource | POST | `/api/requirements/{id}/update` | `update` | HIGH: mutation/tenant/role/DB effect |
| ResourceRequirementResource | GET | `/api/requirements` | `list` | SCOPED READ: auth/tenant/redaction |
| ResourceRequirementResource | POST | `/api/requirements` | `create` | HIGH: mutation/tenant/role/DB effect |
| ResourceResource | GET | `/api/resources/{id}/availability` | `availability` | SCOPED READ: auth/tenant/redaction |
| ResourceResource | POST | `/api/resources/{id}/delete` | `delete` | HIGH: mutation/tenant/role/DB effect |
| ResourceResource | GET | `/api/resources/{id}/image` | `getImage` | SCOPED READ: auth/tenant/redaction |
| ResourceResource | POST | `/api/resources/{id}/image` | `uploadImage` | HIGH: mutation/tenant/role/DB effect |
| ResourceResource | POST | `/api/resources/{id}/update` | `update` | HIGH: mutation/tenant/role/DB effect |
| ResourceResource | GET | `/api/resources` | `list` | SCOPED READ: auth/tenant/redaction |
| ResourceResource | POST | `/api/resources` | `create` | HIGH: mutation/tenant/role/DB effect |
| C1SessionResource | POST | `/api/v2/account/disable` | `disable` | HIGH: mutation/tenant/role/DB effect |
| C1SessionResource | POST | `/api/v2/account/password` | `changePassword` | HIGH: mutation/tenant/role/DB effect |
| C1SessionResource | POST | `/api/v2/account/recovery-codes/rotate` | `rotateRecoveryCodes` | HIGH: mutation/tenant/role/DB effect |
| C1SessionResource | GET | `/api/v2/account` | `account` | SCOPED READ: auth/tenant/redaction |
| C1AuthResource | GET | `/api/v2/auth/csrf` | `csrf` | PUBLIC CANDIDATE; privacy/abuse review |
| C1AuthResource | POST | `/api/v2/auth/login` | `login` | PUBLIC CANDIDATE; privacy/abuse review |
| C1AuthResource | POST | `/api/v2/auth/logout` | `logout` | HIGH: mutation/tenant/role/DB effect |
| C1AuthResource | POST | `/api/v2/auth/recover` | `recover` | PUBLIC CANDIDATE; privacy/abuse review |
| V2DepartmentResource | GET | `/api/v2/csrf` | `csrf` | PUBLIC CANDIDATE; privacy/abuse review |
| DemoMetadataResource | GET | `/api/v2/demo/metadata` | `get` | PUBLIC CANDIDATE; privacy/abuse review |
| V2DepartmentResource | GET | `/api/v2/departments/{id}` | `getDepartment` | SCOPED READ: auth/tenant/redaction |
| V2DepartmentResource | POST | `/api/v2/departments` | `createDepartment` | HIGH: mutation/tenant/role/DB effect |
| C1InvitationResource | POST | `/api/v2/invitations/redeem` | `redeem` | PUBLIC CANDIDATE; privacy/abuse review |
| ReportingResource | GET | `/api/v2/orgs/{organizationId}/activity` | `activity` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/allocations/{allocationId}/approve` | `approve` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/allocations/{allocationId}/cancel` | `cancel` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/allocations/{allocationId}/consume` | `consume` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/allocations/{allocationId}/delete` | `deleteProjectAllocation` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/allocations/{allocationId}/issue` | `issue` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/allocations/{allocationId}/return` | `returnAllocation` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/allocations/{allocationId}` | `allocationState` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/allocations` | `allocationPage` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/allocations` | `createLinkedAllocation` | HIGH: mutation/tenant/role/DB effect |
| ReportingResource | GET | `/api/v2/orgs/{organizationId}/audit` | `audit` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/borrowed-allocations/{allocationId}/revoke-sms-consent` | `revokeBorrowerConsent` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/borrowed-allocations/{allocationId}` | `borrowedDetail` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/borrowed-allocations` | `borrowedList` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/borrowers` | `borrowerOptions` | SCOPED READ: auth/tenant/redaction |
| ReportingResource | POST | `/api/v2/orgs/{organizationId}/exports` | `export` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/operations/{operationId}` | `operationReceipt` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/projects/{projectId}/allocations` | `projectAllocations` | SCOPED READ: auth/tenant/redaction |
| ProjectWorkResource | POST | `/api/v2/orgs/{organizationId}/projects/{projectId}/delete` | `deleteProject` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/projects/{projectId}/lead-assignment` | `assignLead` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/projects/{projectId}/lead-options` | `leadOptions` | SCOPED READ: auth/tenant/redaction |
| ProjectWorkResource | POST | `/api/v2/orgs/{organizationId}/projects/{projectId}/update` | `updateProject` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/projects/{projectId}` | `projectAccess` | SCOPED READ: auth/tenant/redaction |
| ProjectWorkResource | GET | `/api/v2/orgs/{organizationId}/projects` | `projects` | SCOPED READ: auth/tenant/redaction |
| ProjectWorkResource | POST | `/api/v2/orgs/{organizationId}/requirements/{requirementId}/delete` | `deleteRequirement` | HIGH: mutation/tenant/role/DB effect |
| ProjectWorkResource | POST | `/api/v2/orgs/{organizationId}/requirements/{requirementId}/update` | `updateRequirement` | HIGH: mutation/tenant/role/DB effect |
| ProjectWorkResource | GET | `/api/v2/orgs/{organizationId}/requirements/{requirementId}` | `requirement` | SCOPED READ: auth/tenant/redaction |
| ProjectWorkResource | GET | `/api/v2/orgs/{organizationId}/requirements` | `requirements` | SCOPED READ: auth/tenant/redaction |
| ProjectWorkResource | POST | `/api/v2/orgs/{organizationId}/requirements` | `createRequirement` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/resources/{resourceId}/adjust` | `adjust` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/resources/{resourceId}/image/delete` | `removeResourceImage` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/resources/{resourceId}/movements` | `movements` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | POST | `/api/v2/orgs/{organizationId}/resources/{resourceId}/receive` | `receive` | HIGH: mutation/tenant/role/DB effect |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/resources/{resourceId}` | `resourceState` | SCOPED READ: auth/tenant/redaction |
| InventoryResource | GET | `/api/v2/orgs/{organizationId}/resources` | `resourcePage` | SCOPED READ: auth/tenant/redaction |
| C1GovernanceResource | POST | `/api/v2/orgs/{orgId}/accept-transfer` | `acceptTransfer` | HIGH: mutation/tenant/role/DB effect |
| C1GovernanceResource | POST | `/api/v2/orgs/{orgId}/invitations/{invitationId}/revoke` | `revokeInvitation` | HIGH: mutation/tenant/role/DB effect |
| C1GovernanceResource | POST | `/api/v2/orgs/{orgId}/invitations` | `invite` | HIGH: mutation/tenant/role/DB effect |
| C1GovernanceResource | POST | `/api/v2/orgs/{orgId}/members/{memberId}/role` | `changeRole` | HIGH: mutation/tenant/role/DB effect |
| C1GovernanceResource | POST | `/api/v2/orgs/{orgId}/members/{memberId}/suspend` | `suspend` | HIGH: mutation/tenant/role/DB effect |
| C1GovernanceResource | GET | `/api/v2/orgs/{orgId}/members` | `members` | SCOPED READ: auth/tenant/redaction |
| C1GovernanceResource | POST | `/api/v2/orgs/{orgId}/transfer-ownership` | `startTransfer` | HIGH: mutation/tenant/role/DB effect |
| C1GovernanceResource | POST | `/api/v2/orgs/{orgId}/transfers/{transferId}/revoke` | `revokeTransfer` | HIGH: mutation/tenant/role/DB effect |
| C1SessionResource | GET | `/api/v2/orgs` | `organizations` | SCOPED READ: auth/tenant/redaction |
| C1SessionResource | POST | `/api/v2/session/active-org` | `activeOrganization` | HIGH: mutation/tenant/role/DB effect |
| C1SessionResource | GET | `/api/v2/session` | `session` | SCOPED READ: auth/tenant/redaction |
| WorkspacePageResource | GET | `/api/v2/workspace/allocations` | `allocations` | SCOPED READ: auth/tenant/redaction |
| WorkspacePageResource | GET | `/api/v2/workspace/departments` | `departments` | SCOPED READ: auth/tenant/redaction |
| WorkspacePageResource | GET | `/api/v2/workspace/overview` | `overview` | SCOPED READ: auth/tenant/redaction |
| WorkspacePageResource | GET | `/api/v2/workspace/projects` | `projects` | SCOPED READ: auth/tenant/redaction |
| WorkspacePageResource | GET | `/api/v2/workspace/requirements` | `requirements` | SCOPED READ: auth/tenant/redaction |
| WorkspacePageResource | GET | `/api/v2/workspace/resources` | `resources` | SCOPED READ: auth/tenant/redaction |

## Mandatory closeout per operation

1. Review handler, filter and actual delegates; establish actor, selected/path organization, membership, role, object scope, allowed fields, lifecycle, status/errors and database effects **before** tests.
2. Execute an authorized request and a meaningful refusal with existing fictional targets (where applicable). Confirm response and redaction.
3. For denied mutations, compare relevant database state before/after; account for permitted audit effects explicitly. Exercise stale versions, replay, revocations, cross-tenant cases, recovery, stock and exports where relevant.
4. Record exact SHA, fixture, command, result and independently checked receipt. Map legacy IR4 proof only when scope matches. Keep C8 BLOCKED if a mandatory item is NOT_RUN/FAIL/BLOCKED.
5. Confirm packaged Jersey registration independently: this script checks source declarations only.

**Coverage:** 15 resource classes; 93 annotated HTTP operations; no duplicate verb/path found in this source snapshot.
