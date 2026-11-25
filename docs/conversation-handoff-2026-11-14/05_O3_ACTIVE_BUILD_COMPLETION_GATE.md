# Exact O3 in-progress build: preconditions, implementation tasks, acceptance and handoff procedure

## Critical status boundary
When this continuity pack was requested, user wrote: **“okay actually right now, the prev prompt is still being built, after that finishes, I want to transfer to a new conversation here in this project and continue there what we are doing here right now.”** The preceding prompt is the optimized complete-O3 Codex implementation prompt authored in the conversation. Its work is **IN PROGRESS**, not reported finished. No new O3 remote branch/commit was visible when this pack's GitHub audit checked main at 16e781c. Another agent may push during/after this pack creation. Never assume the current observed 13/25 route count remains correct once Codex finishes. The next conversation must first refresh all remote state and inspect new code/tests/diff.

## O3 objective
Finish 6 missing CRUD method/path pairs plus expired APPROVED coverage correction:
- P03 Project POST /api/projects/{id}/update.
- P04 Project POST /api/projects/{id}/delete.
- R03 Resource POST /api/resources/{id}/update.
- R04 Resource POST /api/resources/{id}/delete.
- Q03 Requirement POST /api/requirements/{id}/update.
- Q04 Requirement POST /api/requirements/{id}/delete.
- Correct `AllocationRepository.sumCoveredQuantity` / `RequirementComponent.view`: APPROVED expired at Project end not covered; ISSUED/RETURNED/CONSUMED historical coverage persists; PENDING/CANCELLED excluded.
- Expected result after actual code/tests: original routes 13+6=19/25; remaining A03–A08 six original routes. A09 proposed 26th still separate, not part of original count.

## Authority and source reading protocol
Inspect git status/remote/local branches/PRs before edits, then `AGENTS.md`, `evidence/O3/ROUTE_PARITY.md`, `evidence/O3/PHASE_REPORT.md`, `backend/README.md`; read focused docs/complete-handoff 04/05/06/09 sections and only relevant source. Do not repeatedly reread duplicate 188k master and 2MB course corpus. Original controlling PDF/DOCX as needed for changed API behavior. Older nine-entity 68-route contracts never source of truth. User wants actual code, not a new plan.

## Exact Project implementation
P03 body `{"name":"Updated Workshop","startAt":"<future Philippine local datetime>","endAt":"<later Philippine local datetime>"}`. Preserve existing Project DTO `{id,departmentId,name,startAt,endAt}`. Positive path id; 404 missing; name normalized 1–100; dates nonnull end>start. If either proposed date differs from existing, new start must be future; if dates unchanged, allow only name change even when original Project dates already past. Do not change Department ID. Reject date changes when **any Allocation under any Requirement of Project has nonnull `approvedAt`**, even if status CANCELLED; use aggregate historical count, not currently approved count. Error 409 PROJECT_DATES_LOCKED. Ensure actual transaction/lock ordering is safe against concurrent approval; tests for Project edit vs approval race ideally in O4 if approval not yet implemented.
P04: 404 target missing; 409 DEPENDENCY_EXISTS if ANY ResourceRequirement references Project; physically remove only unreferenced; no cascade/history deletion; 200 `{"message":"Record deleted."}`.

Inspect actual `Project.java`, `ProjectController.java`, `ProjectComponent.java`, `ProjectRepository.java`, `ResourceRequirementRepository.java`, `AllocationRepository.java`, `ApiDtos.java`, `ApiExceptionMapper.java`, and test fixtures. Do not add unused DTO fields/routes. Existing P01/P02 must remain unchanged and pass regressions.

## Exact Resource implementation
R03 body `{"name":"A4 paper sheets","totalQuantity":120,"expectedTotalQuantity":70}`. `expectedTotalQuantity` required numeric 0–1e6; check stored total under Resource pessimistic lock. Mismatch => 409 STALE_RESOURCE_QUANTITY, no mutation. Name 1–100 normalized, database named unique constraint handles racing duplicates with controlled 409 DUPLICATE_NAME. Resource type immutable, not in request. Current commitments H/I calculated under safe lock and consistent clock:
REUSABLE newTotal >= H+I; CONSUMABLE newTotal >= H. Update must not silently reduce below obligations. Database authoritative and transactional. Return established Resource DTO with recomputed available (not persist derived fields). Duplicate check under concurrent requests backed by unique constraint.
R04: 404 absent, 409 DEPENDENCY_EXISTS for any Requirement references; 409 RESOURCE_NOT_EMPTY if totalQuantity>0, even if no references; only zero-stock unreferenced physical delete; no cascade.

## Exact Requirement implementation
Q03 body `{"quantity":8}`, positive id, missing 404, qty 1–1e6, Project and Resource FKs immutable. Project end guard -> 409 PROJECT_ENDED. Compute current effective coverage from correct expiry-aware JPQL and `MAX` largest one PENDING quantity. New required >= max(covered,maxPending); do NOT sum all pending. If violated 409 QUANTITY_BELOW_COMMITMENTS and original row unchanged. Use Project→Resource lock when required for concurrency with Approval/Requirement races; revalidate current values. Return existing `RequirementView` with current allocated and shortage.
Q04: 404 missing, 409 DEPENDENCY_EXISTS if any Allocation historical reference; physically delete only when no Allocation; preserve FK integrity.

## Exact coverage correction
Current `AllocationRepository.sumCoveredQuantity(requirementId,statuses)` includes expired APPROVED, unlike `sumEffectiveHolds(resourceId,approved,now)`. Replace with safe JPQL equivalent:
```java
@Query("SELECT SUM(a.quantity) FROM Allocation a "
 + "WHERE a.requirement.id = :requirementId "
 + "AND (a.status IN :completed "
 + "OR (a.status = :approved AND a.requirement.project.endAt > :now))")
Long sumCovered(
  @Param("requirementId") Long requirementId,
  @Param("completed") Collection<AllocationStatus> completed,  // ISSUED,RETURNED,CONSUMED
  @Param("approved") AllocationStatus approved,
  @Param("now") LocalDateTime now);
```
Actual name can differ; preserve compatibility and correct callers. Validate JPQL compiles/executes on Spring Boot 2.1/Hibernate/MySQL and no empty collection; SUM null->0; Clock Asia/Manila. At exactly endAt APPROVED no longer coverage, ISSUED etc remain. Shortage max(0,Q-Covered). Do NOT implement expiry scheduled cleanup here; that is O5. Do NOT claim query fixed from static text only.

## Minimal full acceptance matrix for O3
P03: permitted rename; future date edit; same historical dates name-only; bad dates/name/id; missing Project 404; historical approvedAt prevents date edit; test DB unchanged on refusal.
P04: dependent Requirement 409 and row retained; unreferenced deletion 200 and row absent; missing 404.
R03: valid name+qty; duplicate normalizer/collation 409; no implicit type mutation; negative/>1e6 400; stale expected 409 unchanged; total below H/I blocked; revised availability correct; read after commit consistent.
R04: nonzero stock 409; referenced even zero stock 409; zero unreferenced delete; missing.
Q03: valid, below 1/>1e6, after Project end, below covered, below largest live pending, unrelated PENDING no sum; rejected state unchanged.
Q04: any Allocation including CANCELLED/RETURNED history blocks; no refs permits; missing.
COVERAGE: pre-end approved counted, exact-end approved not counted, complete statuses still count, pending/cancelled zero, aggregate empty zero, shortage correct, time fixed.
REGRESSION: Java8 Maven tests/clean package; O1 and D03 previous Newman tests; new HTTP/Postman dynamic generated IDs; live MySQL if accessible; no invented success if remote environment cannot connect local 3308; no H2 substitution as real MySQL.
SECURITY: secrets protected, raw phone not exposed, controller->component separation, errors JSON correct, no destructive local DB reset or dropping tables.
GIT: feature branch current remote+local audit; safe task-related commits pushed, remote SHA verified, PR created/updated as appropriate, **no automatic merge to main**; all explicit user uncommitted data preserved.

## Files of special interest
Backend `src/main/java/app/{components/ProjectComponent,components/ResourceComponent,components/RequirementComponent,components/AllocationComponent,config/ApiExceptionMapper,dto/ApiDtos,entities/Project,entities/Resource,entities/ResourceRequirement,entities/Allocation,repositories/ProjectRepository,repositories/ResourceRepository,repositories/ResourceRequirementRepository,repositories/AllocationRepository,rest/controllers/ProjectController,rest/controllers/ResourceController,rest/controllers/RequirementController}.java`.
Existing two tests; new tests under `backend/src/test/java`.
Actual Java project lives under repo `backend/` so adapt path.
Evidence: `evidence/O3/ROUTE_PARITY.md`, `evidence/O3/PHASE_REPORT.md`, new non-destructive test reports; `postman/OrgResource-O1-Skeleton.postman_collection.json` and `postman/OrgResource-O3-Department-Update.postman_collection.json`. Avoid overwriting O1 historical reports.

## Exactly how new conversation should judge O3 Codex response
1. Ask nothing if GitHub results contain the needed new branch, commits and tests. Read newest remote O3 report and diff.
2. Determine `main` vs active feature branch vs old demo branch. A newly pushed feature branch can have O3 even if main still shows 13/25. Don't mistakenly say no work solely from main.
3. Identify exact endpoints from real `@Path`+`@POST/@GET` source; route table alone not enough.
4. Inspect Java code for silent errors/lock order, destructive deletes, transaction semantics, coverage/time, error mapping, schema validity.
5. Evaluate test evidence by type (unit, real MySQL, live HTTP/Newman, concurrency); count PASS/FAIL/NOT_RUN/BLOCKED separately. Verify recorded statuses from report/JSON but don't pretend rerun local app.
6. Identify outstanding bugs. Return an O3 gate decision: PASS (code and required checks verified); CONDITIONAL if code passes subset but real DB/browser absent; FAIL if correctness flaw; BLOCKED if missing tools.
7. On PASS move to the larger O4+O5 combined prompt. If not PASS, produce ONE cohesive targeted repair prompt, then reassess. Do not fracture six CRUD endpoints into six repetitive sessions unless a real blocker requires it.
8. Check that Codex **pushed** the actual code and evidence; if missing, instruct push feature branch and verify SHA/PR.
9. Do not treat user-reported O2 video as a GitHub source file or Canvas submission receipt.

## User's optimized O3 prompt was previously delivered
It contained fixed architecture, 17 execution rules, exact file read order, performance/storage requirements, O3 Task A..G, focused tests and one end-of-batch regression; promised current 19/25 if passed. This file preserves the essential behavioral contract. Any slight variation in task sequencing is acceptable if coverage and tests are complete.