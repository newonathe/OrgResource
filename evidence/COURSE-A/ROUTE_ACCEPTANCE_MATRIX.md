# Course Final A route acceptance matrix

This matrix maps every current HTTP operation to actual runtime request names and the complementary Java/MySQL tests. Newman request names are from the collections listed below and were executed by scripts/run-api-acceptance.ps1. The separate 28-operation Postman reference is importable but is not the ordered runner. Its Department folder was independently run with Newman after correcting its URL representation.

## Current contract

| ID | Endpoint | Runtime success evidence | Refusal/edge evidence |
|---|---|---|---|
| D01 | POST /api/departments | O1: 1. Create Department; O3 CRUD: 01 Create Department fixture | O1: 12. Reject Normalized Duplicate Department; D03 Department Update: 5. Reject normalized duplicate name |
| D02 | GET /api/departments | O1: 2. List Departments | O14: 7. Confirm Department survived refused delete; 23. Confirm Department was deleted |
| D03 | POST /api/departments/{id}/update | D03 Department Update: 3. Rename and normalize display name; 4. Allow case-only rename | D03 Department Update: 5–8 and 11 cover normalized duplicate, unknown, blank, nonpositive, and malformed ID |
| D04 | POST /api/departments/{id}/delete | O1: 14–15 create and delete unreferenced Department; O14: 22–23 delete and confirm absence | O14: 6–7 referenced delete refused and record preserved |
| P01 | POST /api/projects | O1: 3. Create Project; O3 CRUD: 02 Create future Project fixture | ProjectComponent tests cover invalid period, missing Department, and date boundaries |
| P02 | GET /api/projects | O1: 4. List Projects by Department; O3 CRUD: 06. P02 confirms Project persisted | O3 CRUD: 06 verifies the created record and parent ID after prior creation |
| P03 | POST /api/projects/{id}/update | O3 CRUD: 05. P03 updates Project | ProjectComponent tests cover historical name-only edit, invalid period, missing ID, and approval-locked date change |
| P04 | POST /api/projects/{id}/delete | O3 CRUD: 16. P04 deletes unreferenced Project; O14: 18–19 delete and confirm absence | O3 CRUD: 07; O14: 8–9 referenced delete refused and record preserved |
| R01 | POST /api/resources | O1: 5. Create Resource; O3 CRUD: 03 Create Resource fixture | O1: 13. Reject Negative Resource Quantity |
| R02 | GET /api/resources | O1: 6. List Resources by Type; O3 CRUD: 10 confirms stale update did not mutate the Resource | O4/O5: 22 confirms physical and available consumable quantities |
| R03 | POST /api/resources/{id}/update | O3 CRUD: 08. R03 updates Resource; 18 sets stock to zero | O3 CRUD: 09 rejects stale total; 10 confirms unchanged value; component tests cover duplicate, invalid quantity, and commitments |
| R04 | POST /api/resources/{id}/delete | O3 CRUD: 19 deletes eligible Resource; O14: 20–21 deletes and confirms absence | O3 CRUD: 11 referenced and 17 positive-stock protection; O14: 10–11 referenced delete refused and Resource preserved |
| R05 | GET /api/resources/{id}/availability | O1: 7. Get Resource Availability | O4/O5: 13 verifies reusable availability after return; MySQL aggregate tests cover holds/custody and expiry boundary |
| Q01 | POST /api/requirements | O1: 8. Create Requirement; O3 CRUD: 04 Create Requirement fixture | RequirementComponent tests cover missing parents, duplicate pair, and ended Project |
| Q02 | GET /api/requirements | O1: 9. List Requirements by Project; O3 CRUD: 15 confirms deleted Requirement absent | O3 CRUD: 15 confirms the delete result |
| Q03 | POST /api/requirements/{id}/update | O3 CRUD: 12. Q03 updates Requirement | O3 CRUD: 13 rejects invalid quantity; RequirementComponent tests cover ended Project, coverage floor, and maximum live pending request |
| Q04 | POST /api/requirements/{id}/delete | O3 CRUD: 14–15 delete and confirm absence; O14: 16–17 delete and confirm absence | O14: 12–13 refuses deletion with Allocation history and confirms survival |
| A01 | POST /api/allocations | O1: 10. Create Allocation; O4/O5: 05 and 17 create reusable/consumable requests | O1: 10–11 verify PENDING state and redacted response; lifecycle and component tests cover invalid request data |
| A02 | GET /api/allocations | O1: 11. List Allocations by Requirement and Status; O4/O5: 12 GET Allocation remains redacted | O4/O5: 12 verifies phone redaction and query visibility |
| A03 | POST /api/allocations/{id}/approve | O4/O5: 06 reusable and 18 consumable approval | O4/O5 exercises subsequent type-specific transitions; MySQL lifecycle/concurrency tests cover stock, coverage, locking, and rollback |
| A04 | POST /api/allocations/{id}/issue | O4/O5: 07 issue reusable Allocation | O4/O5: 19 refuses issue for consumable; transition test covers wrong state |
| A05 | POST /api/allocations/{id}/return | O4/O5: 08 return reusable Allocation | O4/O5: 09 refuses repeated return; MySQL lifecycle tests cover late-return behavior |
| A06 | POST /api/allocations/{id}/consume | O4/O5: 20 consume consumable Allocation | O4/O5: 21 refuses repeated consume; 19 refuses consumable issue; MySQL tests verify atomic single decrement |
| A07 | POST /api/allocations/{id}/cancel | O4/O5: 24 cancel never-approved request | The same flow checks cancellation reason and then deletion eligibility |
| A08 | POST /api/allocations/{id}/delete | O4/O5: 25 deletes never-approved cancelled Allocation; O14: 14–15 delete and confirm absence | O4/O5: 26–27 protect returned and consumed history; O14 covers never-approved delete |
| A09 | POST /api/allocations/{id}/revoke-sms-consent | O4/O5: 10 revoke consent | O4/O5: 11 idempotent repeated revocation; 12 confirms contact redaction |
| IMAGE-01 | POST /api/resources/{id}/image | Course A Image HTTP: POST multipart PNG upload | ResourceImageHttpMySqlTest covers bad media/bytes, invalid part, bounds, dimensions, and persistence |
| IMAGE-02 | GET /api/resources/{id}/image | Course A Image HTTP: GET stored image; verified media type, nonzero Content-Length, cache and nosniff headers | ResourceImageHttpMySqlTest compares exact uploaded/retrieved bytes and validates missing-image behavior |

The 28-route reference's Department folder also passed Newman locally: 4 requests, 8 assertions, 0 failures. It is not counted in the aggregate runner total.

## Executed aggregate gates

| Gate | Actual command | Observed result |
|---|---|---|
| Backend | JDK 8 plus protected orgresource_test configuration; backend Maven Wrapper with -B -ntp -Pbundle-react-ui clean verify | PASS, 61 tests, 0 failures, 0 errors, 0 skipped. This initializes the current JPQL repositories against MySQL. |
| Frontend | npm ci; npm run typecheck; npm test; npm run lint; npm run build | PASS; 15 frontend tests across 3 files. A compatible typescript-eslint patch removed the previously observed seven moderate development-tool advisories; npm audit now reports 0 vulnerabilities. |
| Newman | scripts/run-api-acceptance.ps1 | PASS; six collections, 100 requests, 151 assertions, 0 failures. Fixture IDs are generated. The runner does not reset the database. |
| Browser | frontend: npm run e2e | PASS; 6 Playwright checks, including connected workflows, navigation, screenshot matrix, pointer/reduced-motion, and packaged-JAR assets/APIs. Command-line Playwright only. |
| SQL walkthrough | scripts/course-a-demo.sql against protected local orgresource_test | PASS; read-only execution showed five entity tables, all InnoDB, four FK relationships, and exact counts. No contact number or borrower name was selected. |

The UI screenshot test ran against the populated integration database; the large lists and overview metrics reflect accumulated fictional acceptance fixtures. This is not a clean demonstration dataset. Use a new empty orgresource database for a clean presentation; do not delete or reset orgresource_test.

## Evidence limits

- Newman acceptance exercises successful and refused paths listed above, but deep concurrency, rollback, scheduler timing, inventory edge boundaries, image negative cases, and Twilio transport outcomes rely on the Java/MySQL tests rather than the 28-route reference.
- Newman aggregate counts are the executed O1/O3/Department-update/O4-O5/O14-dependency/image execution collections. They are not a claim that the 28-route reference itself was run end-to-end.
- The reference collection has intentionally mutually exclusive and destructive requests; run the ordered acceptance script for full fixture-based acceptance.
- Local evidence does not establish instructor acceptance, a course decision on CLO9 scope, live Canvas status, a real Twilio provider response, actual SMS delivery, separate-machine acceptance, or a student-recorded demonstration.
