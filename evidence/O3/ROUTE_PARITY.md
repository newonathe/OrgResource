# OrgResource route parity audit

Audit date: 2026-11-15 (Asia/Manila)

## Authority and findings

The course assignment requires Jersey REST/JSON, Spring-managed components injected with `@Autowired`, JPA/Spring Data repositories, Javax Validation, GET retrieval, POST submissions, runtime create/read coverage for every entity, and runtime row creation/deletion. The syllabus maps Jersey, Postman, persistence, JPQL, validation, Retrofit/Gson, and image handling to CLO3–CLO9. The concise proposal defines 25 operations for the five-entity model. The reviewed handoff adds A09 consent withdrawal as an optional privacy route; that addition is not in the original 25-route proposal.

The O1 backend implemented 12 of the 25 proposal routes. D03 raised that count to 13; the O3 CRUD slice below adds P03/P04, R03/R04 and Q03/Q04, bringing the current backend to 19 of 25 original routes. The six unimplemented original routes are A03–A08. Optional handoff-only A09 remains separately unimplemented, so seven of the handoff's 26 listed routes remain open. The syllabus CLO9 image requirement remains unresolved against the proposal; this audit adds no image route or entity.

Course source alignment for this slice: the searchable course corpus and timestamped video transcripts demonstrate Jersey `@Path`/`@POST`, Spring `@Component`/`@Autowired`, Spring Data repository use, and Javax Validation. The transcripts capture spoken audio, not on-screen code. The implementation follows those teaching patterns; it does not reprocess the videos.

## Route-by-route status

`Implemented` means present in the current backend source. `Added in D03 slice` identifies the route introduced here. `Not implemented` means it remains future work; a route specification or Postman template is not implementation proof.

| ID | Method and route | Status |
|---|---|---|
| D01 | `POST /api/departments` | Implemented |
| D02 | `GET /api/departments` | Implemented |
| D03 | `POST /api/departments/{id}/update` | Added in D03 slice; verified |
| D04 | `POST /api/departments/{id}/delete` | Implemented |
| P01 | `POST /api/projects` | Implemented |
| P02 | `GET /api/projects?departmentId={id}` | Implemented |
| P03 | `POST /api/projects/{id}/update` | Implemented in O3 CRUD; verified |
| P04 | `POST /api/projects/{id}/delete` | Implemented in O3 CRUD; verified |
| R01 | `POST /api/resources` | Implemented |
| R02 | `GET /api/resources?type={type}` | Implemented |
| R03 | `POST /api/resources/{id}/update` | Implemented in O3 CRUD; verified |
| R04 | `POST /api/resources/{id}/delete` | Implemented in O3 CRUD; verified |
| R05 | `GET /api/resources/{id}/availability` | Implemented |
| Q01 | `POST /api/requirements` | Implemented |
| Q02 | `GET /api/requirements?projectId={id}` | Implemented |
| Q03 | `POST /api/requirements/{id}/update` | Implemented in O3 CRUD; verified |
| Q04 | `POST /api/requirements/{id}/delete` | Implemented in O3 CRUD; verified |
| A01 | `POST /api/allocations` | Implemented |
| A02 | `GET /api/allocations?requirementId={id}&status={status}` | Implemented |
| A03 | `POST /api/allocations/{id}/approve` | Not implemented |
| A04 | `POST /api/allocations/{id}/issue` | Not implemented |
| A05 | `POST /api/allocations/{id}/return` | Not implemented |
| A06 | `POST /api/allocations/{id}/consume` | Not implemented |
| A07 | `POST /api/allocations/{id}/cancel` | Not implemented |
| A08 | `POST /api/allocations/{id}/delete` | Not implemented |
| A09 | `POST /api/allocations/{id}/revoke-sms-consent` | Optional handoff addition; not implemented |

## Why D03 was selected

D03 is the next smallest useful route slice: it completes Department's four-operation CRUD family, depends only on the existing Department entity, repository, normalizer, component, validation and error mapping, and does not change allocation or stock state. The controller delegates to `DepartmentComponent` as required. The component validates positive IDs, normalizes display/key values, excludes the current row from duplicate checks, and returns the existing public Department DTO. The database's named unique constraint remains the concurrency authority; a violation of that specific constraint maps to `409 DUPLICATE_NAME`.

## Next dependency-aware work

1. O3 gates passed. Implement A03–A08 as the next dependency-ordered slice: approval, issue, return, consume, cancel and guarded delete, using Project → Resource → Requirement → Allocation locks and rollback-safe transactions.
2. Keep optional A09 consent withdrawal separately tracked from the original 25-route contract.
3. Continue with scheduling, Retrofit/Gson SMS, the React UI, release acceptance and packaging according to their focused handoff sections.
4. Resolve the CLO9 image discrepancy only from an authoritative instructor/Canvas/source decision; do not infer it from this route audit.
