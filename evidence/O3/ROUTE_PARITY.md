# OrgResource route parity audit

Audit date: 2026-11-14 (Asia/Manila)

## Authority and findings

The course assignment requires Jersey REST/JSON, Spring-managed components injected with `@Autowired`, JPA/Spring Data repositories, Javax Validation, GET retrieval, POST submissions, runtime create/read coverage for every entity, and runtime row creation/deletion. The syllabus maps Jersey, Postman, persistence, JPQL, validation, Retrofit/Gson, and image handling to CLO3–CLO9. The concise proposal defines 25 operations for the five-entity model. The reviewed handoff adds A09 consent withdrawal as an optional privacy route; that addition is not in the original 25-route proposal.

The starter backend originally implemented 12 of 25 proposal routes. This slice adds D03, bringing the backend to 13 of 25 original routes. The optional A09 is still absent, so 13 of the handoff's 26 listed routes remain open. The syllabus CLO9 image requirement remains unresolved against the proposal; this audit adds no image route or entity.

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
| P03 | `POST /api/projects/{id}/update` | Not implemented |
| P04 | `POST /api/projects/{id}/delete` | Not implemented |
| R01 | `POST /api/resources` | Implemented |
| R02 | `GET /api/resources?type={type}` | Implemented |
| R03 | `POST /api/resources/{id}/update` | Not implemented |
| R04 | `POST /api/resources/{id}/delete` | Not implemented |
| R05 | `GET /api/resources/{id}/availability` | Implemented |
| Q01 | `POST /api/requirements` | Implemented |
| Q02 | `GET /api/requirements?projectId={id}` | Implemented |
| Q03 | `POST /api/requirements/{id}/update` | Not implemented |
| Q04 | `POST /api/requirements/{id}/delete` | Not implemented |
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

1. Finish Project update/delete (date-history constraints and dependent Requirements).
2. Finish Resource update/delete (expected-quantity guard, allocation commitments, nonempty and dependency checks).
3. Finish Requirement update/delete (coverage and allocation-history constraints).
4. Implement and transactionally verify the Allocation lifecycle in the specified order: approval, issue, return, consume, cancel, guarded delete. Treat A09 consent withdrawal separately as optional.
5. Continue with the assignment-required Retrofit/Twilio adapter, scheduling, real responsive UI, migrations, release checks, and final source export according to the complete handoff.
