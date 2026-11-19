# C8 workspace API contract

The five new list operations are GET under `/api/v2/workspace`. Jersey delegates through `@Autowired C1Component` to read-only C1Service transactions and scoped Spring Data queries. Responses are `{data:{items,nextCursor},requestId}` and private/no-store. The five original list response DTOs keep their fields. The workspace endpoints are additive and the 26 academic business operations and two image operations retain their original wire contracts.

| Route suffix | Query filters | Authorization | Item |
|---|---|---|---|
| departments | search by normalized name | Selected current membership | DepartmentView |
| projects | departmentId, period (ACTIVE/UPCOMING/ENDED), search by project or department | Selected current membership | ProjectView plus departmentName |
| resources | type, search by normalized name | Selected current membership | ResourceView with server-calculated availability |
| requirements | projectId, search by project or resource | Selected current membership | RequirementView with coverage, shortage, Project/Resource labels, resourceType, projectStartAt and projectEndAt |
| allocations | requirementId, projectId, status | Selected OWNER, ADMIN or CUSTODIAN | AllocationView plus the nested workspace Requirement view, without contact value |

All support limit default 50, maximum 100, and cursor. The database returns up to limit plus one rows ordered by immutable numeric primary key ascending. The extra row only signals continuation; the cursor position is the last returned item. A new greater-key row can appear in later pages. Rows committed later with keys behind the current boundary are visible after refresh from page one. This is live keyset traversal. It has no cross-request database snapshot.

A cursor includes an encoded position and a checksum of the current workspace, list and normalized filters. The checksum binds accidental reuse of server cursors. It is not an authorization token or secret signature. Each request independently checks current membership and row scope before reading. Null nextCursor is the only end-of-results signal. Deletion of the prior anchor does not stall next-page SQL.

| Condition | Result |
|---|---|
| Anonymous or stale credentials | 401 AUTH_REQUIRED |
| No selected workspace | 409 ORG_CONTEXT_REQUIRED |
| Selected membership missing/suspended or opaque cursor scope mismatch | 404 NOT_FOUND |
| Current active role cannot read Allocations | 403 FORBIDDEN |
| Nonpositive filter ID or search longer than 100 characters | 400 VALIDATION_FAILED |
| Missing or foreign filter entity | 404 NOT_FOUND |
| Page size outside 1–100 | 400 INVALID_PAGE_SIZE |
| Malformed cursor or nonpositive decoded position | 400 INVALID_CURSOR |
| Unreconciled inventory | 500 INVENTORY_INTEGRITY_FAILURE |

The resource page selects scalar columns and never includes the image BLOB. Its independent ledger and lifecycle aggregate queries use only visible resource IDs, then apply the same reconciliation as the compatibility list. Requirement coverage is batched for visible IDs using the existing approved/issued/returned/consumed expression. Existing versioned stock, image, governance, reporting and compatibility mutations are unchanged.

## Overview and Project history

GET `/api/v2/workspace/overview` returns tenant-wide counts for Departments, active Projects, Resource types and Requirements with shortages. It samples the Manila business clock once in a consistent read transaction. Resource capacity is the first five reconciled records by ID. Calendar preview contains up to ten non-ended Projects by start time and ID. OWNER, ADMIN and CUSTODIAN receive tenant-wide Allocation status counts and the latest five batches by creation time and ID. MEMBER and LEAD receive null for the entire Allocation block. Preview limits are visible in React and lead to the corresponding full paged list. These fields contain neither contacts nor provider details.

The Allocation page's projectId filter scopes through the persisted Requirement, and the server checks the selected tenant's Project before executing the page query. The Project dialog uses this filter for independent history navigation. Project period filters use the existing start-inclusive, end-exclusive contract and have their own cursor binding.

The Project dialog also pages Requirements independently using projectId. The workspace Requirement projection joins scalar Project and Resource fields under the same organization, with no image payload. Allocation context is fetched and coverage computed using only the unique Requirement IDs from the current Allocation page. The React relationship selectors use the matching workspace list with server search, cursor navigation and a retained current selection. The 28 academic compatibility routes and responses are unchanged.
