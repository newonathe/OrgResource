# OrgResource route parity after O4–O5

Audit date: 2026-11-15 (Asia/Manila)

This is the current implementation map for the five-entity, 25-operation proposal. “Implemented” means the route exists in the current backend source; runtime proof is separately identified in `PHASE_REPORT.md`. A09 is a handoff amendment and remains outside the original 25-route count. CLO9 image handling remains unresolved against the proposal; no image route or entity is inferred here.

| ID | Method and route | Status |
|---|---|---|
| D01 | `POST /api/departments` | Implemented |
| D02 | `GET /api/departments` | Implemented |
| D03 | `POST /api/departments/{id}/update` | Implemented |
| D04 | `POST /api/departments/{id}/delete` | Implemented |
| P01 | `POST /api/projects` | Implemented |
| P02 | `GET /api/projects?departmentId={id}` | Implemented |
| P03 | `POST /api/projects/{id}/update` | Implemented |
| P04 | `POST /api/projects/{id}/delete` | Implemented |
| R01 | `POST /api/resources` | Implemented |
| R02 | `GET /api/resources?type={type}` | Implemented |
| R03 | `POST /api/resources/{id}/update` | Implemented |
| R04 | `POST /api/resources/{id}/delete` | Implemented |
| R05 | `GET /api/resources/{id}/availability` | Implemented |
| Q01 | `POST /api/requirements` | Implemented |
| Q02 | `GET /api/requirements?projectId={id}` | Implemented |
| Q03 | `POST /api/requirements/{id}/update` | Implemented |
| Q04 | `POST /api/requirements/{id}/delete` | Implemented |
| A01 | `POST /api/allocations` | Implemented |
| A02 | `GET /api/allocations?requirementId={id}&status={status}` | Implemented |
| A03 | `POST /api/allocations/{id}/approve` | Implemented |
| A04 | `POST /api/allocations/{id}/issue` | Implemented |
| A05 | `POST /api/allocations/{id}/return` | Implemented |
| A06 | `POST /api/allocations/{id}/consume` | Implemented |
| A07 | `POST /api/allocations/{id}/cancel` | Implemented |
| A08 | `POST /api/allocations/{id}/delete` | Implemented |
| A09 | `POST /api/allocations/{id}/revoke-sms-consent` | Implemented amendment; not included in 25 |

**Count:** 25/25 original routes implemented; A09 separately implemented (1/1).

The implementation still uses exactly five JPA entities: Department, Project, Resource, ResourceRequirement, and Allocation. The Spring expiry scheduler selects only bounded PENDING/APPROVED candidate IDs, then rechecks each under the Project → Resource → Requirement → Allocation lock order. ISSUED allocations are never auto-returned.
