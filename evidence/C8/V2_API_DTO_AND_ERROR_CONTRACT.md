# C8 workspace API contract

All five new operations are GET under `/api/v2/workspace`. Jersey delegates through `@Autowired C1Component` to read-only C1Service transactions and scoped Spring Data queries. Responses are `{data:{items,nextCursor},requestId}` and private/no-store. The five original list response DTOs keep their fields. The workspace endpoints are additive and the 26 academic business operations and two image operations retain their original wire contracts.

| Route suffix | Query filters | Authorization | Item |
|---|---|---|---|
| departments | search by normalized name | Selected current membership | DepartmentView |
| projects | departmentId, search by project or department | Selected current membership | ProjectView |
| resources | type, search by normalized name | Selected current membership | ResourceView with server-calculated availability |
| requirements | projectId, search by project or resource | Selected current membership | RequirementView with coverage and shortage |
| allocations | requirementId, status | Selected OWNER, ADMIN or CUSTODIAN | AllocationView without contact value |

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
