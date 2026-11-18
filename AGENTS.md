# OrgResource implementation rules

## Protected Course Final A

- `7f8c0746da18fd54b47f430ac1435ac36e605d67` is the immutable Course Final A source and artifact reference. Never modify its implementation, release artifacts, or academic history while working on Extended C.
- Preserve the course stack and functionality on that source: Java 8, Spring Boot/Jersey, `javax` validation, JPA/Spring Data/MySQL, Retrofit/Gson, scheduling, and React.
- Read the assignment, syllabus, and relevant lessons from `ChatGPT-Project-Pack/UPLOAD/` before changing a course behavior. Keep GET for retrieval, POST for submissions, Jersey controllers delegating through Spring `@Autowired` to components, JSON DTOs and controlled errors, and executable Postman cases for the course routes.
- Distinguish Course A evidence from Extended C evidence. Do not claim Extended C has full Course A route parity unless it is independently verified.

## Extended C implementation

- Work only on the currently authorized phase and its correctly based feature branch. C0–C2 do not authorize C3 or a broad product rewrite.
- Preserve C1 identity, sessions, CSRF, explicit organization selection, database-backed roles, and tenant isolation. Reuse the maintained `extended/backend/` architecture and the five existing domain entities.
- Use Java 21 and actual MySQL 8 for Extended C compatibility and transactional acceptance. Do not treat Java 8 CI or mocked/H2 database results as proof of Extended C behavior.
- Keep stock movements and audit history append-only for the runtime account. Use separate elevated migration/fixture credentials only for migrations and isolated test setup. Test actual MySQL grants, constraints, rollback, concurrency, replay, and restart behavior.
- Use only fictional data. Never print, commit, or expose database/provider secrets, personal contact details, real student data, or real SMS. SMS remains disabled unless an explicitly authorized simulator test requires it.
- Inventory totals, movements, holds, issued custody, and idempotency receipts must remain transactionally reconciled. Reject corrupt invariants; never clamp negative availability into a fabricated success.
- Use only fresh, disposable, loopback MySQL instances and schemas created by the guarded acceptance scripts, including their generated `orgresource_c1_*` / `orgresource_c2_*` test schemas. Never reset, migrate, or delete a shared/user-owned or Course A database.
- Preserve unrelated work. Inspect Git status/remotes before edits; never reset, force-push, or merge automatically. Exclude secrets, databases, caches, `target/`, `dist/`, and `node_modules/` from commits.
- Report only commands actually run and results actually observed. Mark failures, blocked checks, and unrun checks precisely. Do not claim Canvas submission, instructor approval, recording, public deployment, or real provider delivery without evidence.
- Do not start a later implementation phase, merge, deploy publicly, create paid resources, send real SMS, or submit Canvas without separate explicit authorization.

## Local computer-use constraint

- Never invoke the in-app Browser or Chrome-control plugins on this Windows installation. The embedded Chromium GPU process crashes when an in-app browser session starts. Use command-line Playwright or non-GUI checks for local web verification. Retry browser plugins only after the user confirms an app update fixed the GPU crash.

## Continuous source-completeness and independent-evidence gate — EVERY future cycle

- **Mandatory read-first policy:** [`docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md`](docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md). Apply it to every audit, research pass, plan, implementation, PR, correction, test, CI, migration, package, and release, not only final acceptance.
- Before editing, enumerate and reconcile the exact current Git refs/PRs, source authorities, all changed paths and material transitive dependencies; maintain an explicit reviewed/not-reviewed source register and requirement→implementation→test→evidence matrix. Review real source, not summaries alone.
- Socratically challenge each significant file/route/role/SQL/DTO/state change with concrete failure and adversarial tests, including cross-tenant behavior, concurrency, lost response, restart, boundary validation, integrity, privacy, rollback, compatibility, accessibility and operator recovery.
- Never use absolute labels such as `no gaps`, `100% comprehensive`, `fully proven` or `production-ready` without bounded acceptance definitions and independent evidence. Mark SOURCE_ONLY, REPORTED_ONLY, NOT_RUN, BLOCKED, test failures, deferred decisions and incomplete file inspections truthfully. Green CI alone is not proof of every property.
- Every implementation-phase report must record per-file review depth, reviewed ranges, exact SHAs, reproducible commands and observed results, evidence gaps and independently critical second-pass review. The next Codex prompt must carry this gate forward explicitly. Do not shorten or skip mandatory criteria merely because the task is large; report material blockers and request specific owner authority when needed.
- This policy supplements all existing Course A immutability, academic integrity, privacy and no-merge/no-paid/no-deploy/no-live-SMS boundaries. It does not rewrite historical evidence or constitute automatic GitHub branch protection.
