# 10 — GitHub, agent coordination, CI, backlog, packaging, demonstration and release

## Repository and file layout

Proposed repo: newonathe/OrgResource (not created in this handoff). Prefer private until user approves publication. Verify actual remote before push.

OrgResource/
  AGENTS.md
  CLAUDE.md
  README.md
  .gitignore
  .editorconfig
  .github/workflows/
  backend/pom.xml
  backend/.mvn/, backend/mvnw, backend/mvnw.cmd
  backend/src/main/java/app/SpringBootApp.java
  backend/src/main/java/app/config/
  backend/src/main/java/app/entities/
  backend/src/main/java/app/repositories/
  backend/src/main/java/app/components/
  backend/src/main/java/app/rest/controllers/
  backend/src/main/java/app/rest/errors/
  backend/src/main/java/app/dto/request/
  backend/src/main/java/app/dto/response/
  backend/src/main/java/app/integration/
  backend/src/main/java/app/scheduling/
  backend/src/main/resources/application.properties
  backend/src/main/resources/static/
  backend/src/test/java/
  frontend/package.json, package-lock.json, vite.config.ts, tsconfig.json
  frontend/src/api,types,components,features,styles,tests
  frontend/e2e
  postman/
  docs/ORGRESOURCE_MASTER_HANDOFF.md
  docs/source-audit, decisions, implementation-status, test-plan, proposal-changes
  scripts/
  evidence/skeleton,integration,frontend,release

Only create modules as actual phase needs. Do not generate empty fake app modules or README claiming features.

## Agent responsibilities and worktree isolation

Codex primary: actual course source/environment, Java/JPA/Jersey, DB, transaction engine, time/scheduler/Retrofit, Java tests, export/packaging.
Claude Code primary: independent read-only reviews, React shell and UI/UX in separate worktree, browser tests, user-facing quality, approved targeted fixes.
Roles can swap after explicit task assignment. No two writers on same worktree/file. Source of truth 'docs/ORGRESOURCE_MASTER_HANDOFF.md'; AGENTS and CLAUDE are small references, not competing specs.
If one agent opens PR on feature/backend-skeleton, other agent can review without changing branch, or use distinct branch. No merge to main, no push or destructive git operation without user permission. Check git status/remote before every task and log commit SHA.

## Branches and PR checklists

Branches may be feature/backend-skeleton, feature/crud, feature/allocation-engine, feature/scheduler, feature/ui-shell, feature/ui-workflows, test/full-regression. Use Git worktrees when parallel.
PR includes scope, source evidence, changed files, API/DB contract changes, commands executed, tests PASS/FAIL/NOT_RUN, actual screenshots only if real UI, security review, regressions, reversible rollback, reviewer signoff; no fake checks.

## CI build policy

GitHub Actions: backend JDK 8 + Maven verify, isolated MySQL 8.0 InnoDB service and test profile as needed; frontend Node compatible with Vite, npm ci, lint, unit, build; browser E2E with actual backend when available. Use current official actions versions verified at implementation (2026 docs show actions/checkout@v7, setup-java@v6, setup-node version verify). Never pin deprecated syntax blindly or leak credentials.
Do not claim CI green as proof of tests not configured. Real DB integration tests must use actual MySQL engine, not only in-memory H2. Java compile target class major version 52 for Java 8, verify javap. Frontend output must be copied into backend static classpath and packaged JAR tested. Credentials from GitHub Actions secrets only for actual providers; default SMS DISABLED.
Suggested jobs: java-build, java-db-integration, frontend-build, browser-e2e, release-packaging (later). Avoid Docker deployment, cloud registry, Kubernetes, Redis, tracing, payment API, microservices without requirements.

## Dated backlog (printed source deadlines; current Canvas unverified)

ORG001 Source+environment inspection (Nov13 P0).
ORG002 Java/Maven/Jersey bootstrap (Nov13 P0).
ORG003 Five JPA entities/DB (Nov13 P0).
ORG004 Repositories/query smoke test (Nov13 P0).
ORG005 Five controllers/components JSON basic create-read (Nov13 P0).
ORG006 Exported Eclipse-compatible source ZIP + genuine skeleton video (Nov14 P0).
ORG007 CRUD, validation, safe deletion (Nov15-17 P1).
ORG008 Transactional Allocation and stock math (Nov18-22 P1).
ORG009 Clock/Project expiry scheduling (Nov23-24 P1).
ORG010 Retrofit/Gson/FakeTwilio reminders (Nov24-26 P1).
ORG011 React shell/design system (parallel after API stable P1).
ORG012 Departments/Projects/Resources UI (after CRUD P1).
ORG013 Requirements/Allocations UI (after lifecycle P1).
ORG014 Dashboard/reminder UI (after SMS P1).
ORG015 All-routes Postman, Java regression and concurrency (Nov27-30 P1).
ORG016 Browser E2E, accessibility, responsive checks (Nov27-Dec2 P1).
ORG017 Conditional image feature only if required and confirmed.
ORG018 UI static bundling, Eclipse import and full release export (Dec1-3 P1).
ORG019 Final proposal/source audit, Certificate of Authorship and external help (Dec1-3 P1).
ORG020 Final video, actual evidence, release and academic submission by printed Dec5, subject Canvas confirmation.
Dates are proposed sequence, not actual completed milestones.

## Critical path

Environment -> working Java/Jersey/DB -> five real entity tables/repos -> controllers/JSON -> skeleton export+video.
Final backend: CRUD -> transaction correctness -> time/expiry -> external Retrofit/SMS -> integration tests.
Final frontend: API DTO contract -> React shell -> CRUD screens -> Allocation workflows -> dashboard/reminders -> actual browser tests -> bundled UI.
Agent may parallelize frontend shell after DTOs stable, but testing must be rerun when shared API changed.

## Build and release gates

Gate0 actual source/toolchain and repository state documented.
Gate1 one Jersey->Component->Repository->DB POST/GET observed.
Gate2 five mapped entities and real skeleton export/demo.
Gate3 all original CRUD/validation/deletion correct.
Gate4 concurrent approvals, consume rollback, stock formulas correct.
Gate5 clock/expiration and actual scheduler.
Gate6 outbound Retrofit/Gson HTTP to simulator and honest statuses.
Gate7 React UI integrates real API with correct UX states.
Gate8 comprehensive Postman, DB concurrency, browser E2E.
Gate9 packaged JAR / and /api serve, clean Eclipse import/export, source ZIP and checksums.
Gate10 video/source/assistance/Canvas submission reviewed; actual receipt only if verified.
Every gate can be PASS/FAIL/BLOCKED/NOT_RUN, not "looks good".

## Packaging required

- OrgResource-Eclipse-Project.zip (Java source, pom.xml and any verified portable .project/.classpath/.settings needed, wrapper, no secrets, import guide).
- OrgResource-Full-Source.zip (Java + React + docs + Postman + scripts + lockfile; no generated caches, private lecture archives unless authorized).
- Runnable Java JAR with React production static assets, if bundled build verified.
- All-Routes and Demo Postman collection/environment, non-secret.
- README/setup/run, API/DB/UI/docs, actual tests and logs, latest decision/conflict register, Video link/file, screenshots only when genuine, SHA-256 manifest, exact commit.
- Current course/syllabus final deliverables and Canvas instructions manual verification.
An Eclipse source ZIP is NOT the same as executable JAR. Maven-based Eclipse m2e import can suffice only if accepted; if literal .project export required, generate and actually test portable metadata or disclose limitation.

Package flow:
1. Freeze reviewed Git commit, inspect diff/secret scan.
2. Java JDK8 Maven clean verify + actual MySQL tests.
3. Node npm ci, frontend unit/lint/build.
4. Copy dist assets to Java src/main/resources/static (or configured asset path) and repackage.
5. Run resulting JAR, verify real GET / and GET /api/departments and HashRouter, browser E2E.
6. Build two source ZIPs with exact include/exclude list.
7. Re-extract to temp, check source/config, try clean import, compute SHA-256.
8. Record results, video/Canvas status, limitations. No invented success.

## Contingency cuts

Protect course mandatory five entities/Jersey/JSON/Spring JPA/Javax/Retrofit/Schedule/Postman/Export first. User-selected real UI remains final required scope, but polish/animations/analytics charts can be cut before functional screens. Defer sixth movement ledger, auth/public hosting, procurement, partial damaged returns, automatic anticipated-return booking, third-party campus integration, cloud deploy, complex observability. Conditional image feature only if course interpretation requires; do not pretend waived. Do not substitute fake data or stubs for required final logic.

## Risk register

R1 Legacy JDK/JAR/Maven incompatibility (critical): actual env + course code inspection, portable fallback.
R2 MySQL auth/driver/tables fail: real server checks and schema inspection.
R3 Jersey path /api/api or static root broken: explicit registration/mapping smoke.
R4 JPA derived query typo: startup+query tests.
R5 Approval concurrency stock overbooking: Resource locks + parallel DB tests.
R6 Requirement overcoverage: same lock+JPQL test.
R7 Double consumable deduction: single atomic transaction, repeat refusal and rollback.
R8 Stale stock update: expectedTotalQuantity guarded under lock.
R9 Expiry frees issued custody falsely: status/calc tests.
R10 SMS duplicate/lost response: claim/outcome persisted and no auto retry.
R11 Real Twilio auth in FakeTwilio logs: dummy only.
R12 Frontend fake-success/UTC shift: typed client and PHT tests.
R13 React bundled / served incorrectly: packaged JAR smoke.
R14 Export cannot clean import: early cross-workspace import; reveal limitation.
R15 Video fabricated or unreadable: genuine capture script and checks.
R16 Course Canvas updates unknown: student check.
R17 AI assistance misunderstood: truthful acknowledgment.
R18 ZIP source archive completeness unverified: never claim every original entry reviewed.
R19 Git worktree conflict: one writer per file and reviewed branch merge.
R20 Scope inflation/UI distraction from skeleton deadline: prioritize P0 gates.
