# O11 route acceptance parity

**Scope:** 25 original API operations plus separately identified A09 consent revocation. **Count: 26 current operations.** The 26-request reference is `postman/OrgResource-Current-26-Routes.postman_collection.json`; it is a route reference, not an ordered mutation runner.

| ID | Operation | Route | Executable acceptance collection |
|---|---|---|---|
| D01 | Create Department | `POST /api/departments` | O1 skeleton |
| D02 | List Departments | `GET /api/departments` | O1 skeleton |
| D03 | Update Department | `POST /api/departments/{id}/update` | O3 Department Update |
| D04 | Delete Department | `POST /api/departments/{id}/delete` | O1 skeleton; O3 Department Update |
| P01 | Create Project | `POST /api/projects` | O1 skeleton; O3 CRUD |
| P02 | List Projects | `GET /api/projects` | O1 skeleton; O3 CRUD |
| P03 | Update Project | `POST /api/projects/{id}/update` | O3 CRUD |
| P04 | Delete Project | `POST /api/projects/{id}/delete` | O3 CRUD |
| R01 | Create Resource | `POST /api/resources` | O1 skeleton; O3 CRUD |
| R02 | List Resources | `GET /api/resources` | O1 skeleton; O3 CRUD |
| R03 | Update Resource | `POST /api/resources/{id}/update` | O3 CRUD |
| R04 | Delete Resource | `POST /api/resources/{id}/delete` | O3 CRUD |
| R05 | Read Resource Availability | `GET /api/resources/{id}/availability` | O1 skeleton; O4/O5 |
| Q01 | Create Requirement | `POST /api/requirements` | O1 skeleton; O3 CRUD |
| Q02 | List Requirements | `GET /api/requirements` | O1 skeleton; O3 CRUD |
| Q03 | Update Requirement | `POST /api/requirements/{id}/update` | O3 CRUD |
| Q04 | Delete Requirement | `POST /api/requirements/{id}/delete` | O3 CRUD |
| A01 | Create Allocation | `POST /api/allocations` | O1 skeleton; O4/O5 |
| A02 | List Allocations | `GET /api/allocations` | O1 skeleton; O4/O5 |
| A03 | Approve Allocation | `POST /api/allocations/{id}/approve` | O4/O5 |
| A04 | Issue Allocation | `POST /api/allocations/{id}/issue` | O4/O5 |
| A05 | Return Allocation | `POST /api/allocations/{id}/return` | O4/O5 |
| A06 | Consume Allocation | `POST /api/allocations/{id}/consume` | O4/O5 |
| A07 | Cancel Allocation | `POST /api/allocations/{id}/cancel` | O4/O5 |
| A08 | Delete Allocation | `POST /api/allocations/{id}/delete` | O4/O5 |
| A09 | Revoke SMS consent | `POST /api/allocations/{id}/revoke-sms-consent` | O4/O5 |

## Executed evidence

The local run was performed through Newman 6.2.3 against the Java service connected to MySQL `orgresource_test`; credentials were loaded only into the PowerShell process by the protected workstation setup.

| Collection | Requests | Assertions | Result |
|---|---:|---:|---|
| `OrgResource-O1-Skeleton.postman_collection.json` | 15 | 29 | PASS |
| `OrgResource-O3-CRUD.postman_collection.json` | 20 | 30 | PASS |
| `OrgResource-O3-Department-Update.postman_collection.json` | 11 | 19 | PASS |
| `OrgResource-O4-O5-Allocation-Expiry.postman_collection.json` | 27 | 27 | PASS |
| **Total** | **73** | **105** | **PASS** |

Command sequence is reproducible with `scripts/run-api-acceptance.ps1`. Newman accepted all four collections as executable Collection v2.1 documents and ran their request and assertion scripts. The generic current-route reference collection was not executed as a single sequence because it includes conflicting lifecycle states; each operation is exercised by the phase-specific collections listed above.
