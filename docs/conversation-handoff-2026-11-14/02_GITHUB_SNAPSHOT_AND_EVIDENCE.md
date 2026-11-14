# GitHub repository: verified snapshot, current code and evidence boundaries

AUDIT SNAPSHOT: 2026-11-14, approximately 23:35 Asia/Manila at handoff request. This was a connected GitHub **READ** of private repo state. Local Codex workspace and uncommitted files inaccessible. **Audit again before decisions.**

## Repository and branch state
- Owner/repo: `newonathe/OrgResource` (PRIVATE).
- Default branch: `main`.
- Remote HEAD `main` = `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`, commit subject `fix: support PowerShell 5 in skeleton demo launcher`, committed 2026-11-14 13:56:55 UTC.
- Prior commits: `f576cf8dda939c67ac7c0e69934e0e7699f8d18c` demo preflight; `39482cca36d1f147cadec25c2ed9a93e23ec3739` D03 update; `acc301992da820089ff9b2567ee095241cf8c307` O1 baseline.
- Remote branches at audit: `main`; `demo/o2-recording-kit-2026-11-14` head `89d61934b145a9139085d782dd4ddfc38f17b474`.
- PR #1 open DRAFT, title “O2: prepare one-click Java skeleton video demo (no agent needed)”, head old demo branch and base `acc3019`; historical/overlapping alternative video-kit. DO NOT blindly merge with latest main scripts.
- GitHub Actions workflow run count: zero at audit; CI success not claimed.
- Repo tree had 434 entries, not a complete exhaustive semantic code review of all entries. No `frontend/` app source. 34 `.java` files including two Java test classes; no `evidence/O4/` on `main`.
- While this overlay was being written, user stated ongoing Codex O3 build. It may create/push new feature branches/commits after the audit. Use no static count once it finishes.

## Actual source present in `backend/`
Configuration:
`pom.xml` Spring Boot parent `2.1.0.RELEASE`, `java.version=1.8`, Jersey `2.26`, starter Jersey/data-jpa/validation/test, Jersey Jackson 2.26, `mysql:mysql-connector-java:8.0.13`; Maven wrapper and Eclipse .project/.classpath/.settings; `application.properties` server binds 127.0.0.1:8080, DB URL/user/password from ORG_DB_* environment, Hibernate MySQL57Dialect, InnoDB, local-only `ddl-auto=update`, connection pool max 5, open-in-view false.
Actual app: `backend/src/main/java/app/SpringBootApp.java` SpringBootApplication and EnableScheduling (annotation exists; no actual reminder/expiry jobs yet).
Jersey: `app/config/JerseyConfig.java` @ApplicationPath("/api"), registers five controller classes, exception mapper and Jackson.
Clock: `app/config/ClockConfiguration.java` injected Asia/Manila clock.
Entities: Department, Project, Resource, ResourceRequirement, Allocation; enums AllocationStatus, ResourceType, CancellationReason, ReminderOutcome.
Repositories: DepartmentRepository, ProjectRepository, ResourceRepository, ResourceRequirementRepository, AllocationRepository.
Components: DepartmentComponent, ProjectComponent, ResourceComponent, RequirementComponent, AllocationComponent, NameNormalizer, RequestValidation, ApiException.
Controllers: DepartmentController, ProjectController, ResourceController, RequirementController, AllocationController.
DTOs: `app/dto/ApiDtos.java`, public fields for narrow JSON; AllocationView intentionally redacts raw phoneNumber and provides contactPresent.
Tests: `backend/src/test/java/app/components/DepartmentComponentTest.java`, `backend/src/test/java/app/config/ApiExceptionMapperTest.java`, total eight tests recorded by O3 phase.

## 13/25 original routes present on current audited main
D01 POST /api/departments (create)
D02 GET /api/departments (list)
D03 POST /api/departments/{id}/update (newly implemented in O3)
D04 POST /api/departments/{id}/delete (guarded)
P01 POST /api/projects (create)
P02 GET /api/projects?departmentId= (list/filter)
R01 POST /api/resources (create)
R02 GET /api/resources?type= (list/filter)
R05 GET /api/resources/{id}/availability (exact 2-field JSON)
Q01 POST /api/requirements (create)
Q02 GET /api/requirements?projectId= (list/filter)
A01 POST /api/allocations (create PENDING only)
A02 GET /api/allocations?requirementId=&status= (list/filter)
MISSING at audit: P03/P04, R03/R04, Q03/Q04, A03-A08 and optional A09. Total exactly 13 original routes currently present; the current Codex O3 job targets 19/25 and is NOT YET VERIFIED. These are method/path pairs, not mere document entries.

## Code-level behavior and defects confirmed from direct reads
- `DepartmentComponent.create`: normalize display/key, reject duplicate names, DB save, DTO. `update`: positive ID, normalized uniqueness, `saveAndFlush`, named DB constraint handles race. `delete`: rejects if referenced by Project. Department controller @Path, GET/POST, injected component.
- `ProjectComponent.create`: name, future start, end > start, valid Department FK, save. `list`: optionally filter by Department ID. `ProjectController` currently only POST/GET; O3 must add update/delete.
- `ResourceComponent.create/list/getAvailability`: validates nonnegative stock, ResourceType, normalized uniqueness. Available reusable = total - effective APPROVED holds - ISSUED custody; consumable = total - effective APPROVED holds. Throws 500 for impossible negative/invariant values rather than silently clamping. Mutating Resource update/delete absent before O3.
- `RequirementComponent.create/list/view`: quantity 1..1e6, Project and Resource references, Project end guard, unique Project/Resource pair. Coverage computed via AllocationRepository; shortage from qty minus sum.
- `AllocationComponent.create/list`: records PENDING qty<=requirement qty, project not ended, borrowerName, optional E.164-like contact retained only when consent true. Redacted API DTO; lists by requirement/status. Does not yet approve/reserve/issue/return/consume/cancel/delete at audit.
- **KNOWN DEFECT:** `AllocationRepository.sumCoveredQuantity` sums `APPROVED,ISSUED,RETURNED,CONSUMED` unconditionally and erroneously counts expired APPROVED status for coverage. `sumEffectiveHolds` correctly filters expired Project end. O3 MUST correct coverage JPQL with time bound and fixed-clock tests.
- Error mapper at O3 handles named Department unique conflict, Jersey unknown-route 404, malformed route param 400, javax validation and generic errors. More exhaustive error handling remains a target; no claim all concurrent failures map correctly.
- Data-protection boundary: application loopback-only, no real auth, fictional data, raw phone not serialized; not production secured.
- Current system is a genuine Java skeleton with recorded successful local runtime, not complete application.

## Recorded evidence under `evidence/O1`
`PHASE_REPORT.md`: JDK Temurin 8u512; Maven Wrapper 3.3.4/Maven 3.10.0; Spring Boot 2.1.0; MySQL 8.0.46; five InnoDB tables; 4 FKs and 3 uniqueness constraints. O1 final Newman recorded 15 requests, 29 assertions, 0 failed. Restart persistence verified in saved HTTP evidence; phone not leaked. Clean source archive Maven build recorded successful. Originally zero Java test files in O1; later O3 added eight tests. The existence of evidence files is NOT a fresh independent rerun in the new ChatGPT conversation.

`evidence/O3/PHASE_REPORT.md`: 8 Java tests passing, D03 Newman 11 requests/19 assertions passing, O1 regression 15 requests/29 assertions passing, JSON 404 unknown route and 400 malformed ID. Clean extracted `OrgResource-Java-Skeleton-2026-11-14.zip` built/tested, 47 files, 37,507 bytes, SHA-256 `875E361A2D7E50649A7383EA6E6A808002892FD3BE4D5923BDA3CB929BF91B30`. Actual Eclipse IDE GUI import not verified. Source ZIP is not the final complete app.
`evidence/O1/O1-SKELETON-DEMO-PREFLIGHT-2026-11-14.md`: Java8 launcher and database SQL script successful on user workstation. Protected local credentials use `%LOCALAPPDATA%\OrgResourceDev`; app 127.0.0.1:8080, MySQL 127.0.0.1:3308 `orgresource` using `orgresource_dev`. The two Workbench profiles on 3306/3307 are NOT this DB. SQL is read-only. Running O1 focused Newman collection produced two requests/four passing assertions. Passwords never committed.
`evidence/O1/VIDEO_NOT_CREATED.md` and O3 phase report say no O2 video as of those older checks; user LATER said video recording DONE. Treat the older notes as stale.

## O2 demo commands already used by user
From repo root in VS Code terminal:
```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-skeleton-demo.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-skeleton-demo-sql.ps1
npm exec --yes --package=newman -- newman run postman\OrgResource-O1-Skeleton-Demo.postman_collection.json --reporters cli
```
OBS Window Capture recorded only VS Code while user read ChatGPT privately. User reported finishing the video. Do not recreate O2 by default.

## New conversation audit sequence
At time of continuation:
1. Connected GitHub remote: fetch repo metadata, main recent commits, all relevant branches, PRs and changed files, CI checks.
2. Identify which branch Codex used for active O3, and compare to baseline and main. If no remote push, say no remote evidence and ask user for Codex output or inspect local via available connector where authorized.
3. Read new O3 `PHASE_REPORT`, `ROUTE_PARITY`, actual changed Java source, tests, Postman collections, repository code and locks. Verify content matches prompt and report. Saved test PASS is evidence but not identical to independently rerunning.
4. Count original routes exact method/path 25; track A09 separate. Never infer code works from route table alone.
5. Review correctness: transactions, error mapping, expected stock precondition, date-history protection, coverage expiry, query behavior, tests and DB/HTTP evidence, no secrets or unrelated changes.
6. Return acceptance verdict `PASS/CONDITIONALLY_READY/BLOCKED/FAIL` with exact gaps, then prepare next prompt accordingly.
7. Do not merge to main automatically or ignore feature-branch head merely because `main` is older.