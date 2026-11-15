# OrgResource repository rules

## Current O14 scope: academic baseline only

1. Treat O14 as an interim, course-traceable technical baseline, not the final product freeze. Finish its acceptance, documentation, reproducible artifacts, evidence, feature-branch commit/push and PR. Do not begin the production expansion within O14.
2. Read the current README, relevant authoritative assignment/syllabus/proposal sections, current implementation, and only the relevant sections of `docs/ORGRESOURCE_MASTER_HANDOFF.md`. The master handoff and `docs/complete-handoff/` are historical planning material; use current source/evidence over stale phase prompts.
3. The current academic implementation has five JPA entities and 25 proposal operations plus A09. Preserve their route and behavior traceability. The assignment requires **at least five justified entity types**, not a maximum; five is the current academic-baseline contract, not a permanent product cap.
4. Preserve the course implementation in this baseline: Java 8-compatible Spring Boot/Jersey, `javax`, Spring `@Autowired` components, JPA/Spring Data/MySQL, Javax Validation, Retrofit/Gson, scheduling, and current Postman definitions. Do not publicly expose the legacy Java 8/Spring Boot 2.1 runtime as a supported production service.
5. The present inventory rules, status transitions, stock formulas, validation, transaction locks, safe deletion, reminders, and UI behavior remain binding for O14. Do not change functioning modules without a verified defect or requirement.
6. Keep current demo data fictional. SMS stays disabled by default; simulator results are not delivery. Never print or commit database passwords, provider credentials, personal contacts, private student information, or private local configuration.
7. Do not claim Canvas review/submission, instructor approval, authorship certification, peer review, video recording, external-user acceptance, hosted deployment, or a real SMS unless the corresponding event actually occurred.
8. Inspect Git status/remotes before edits. Preserve unrelated work. For O14 use the existing feature branch, push without force, and open/update its PR against the O11/O13 branch. Do not merge, tag a final release, submit to Canvas, or deploy without explicit authorization.
9. Evidence must name actual commands, observed outcomes, files, SHAs and limitations. Use PASS/FAIL/BLOCKED/NOT_RUN/UNRESOLVED precisely. Do not treat annotations, static checks, screenshots, CI, or simulator responses as proof of unrelated behavior.

## Later production track

1. After O14, pause production implementation until the user supplies the next separately reviewed master prompt. This direction changes the ultimate objective but is not authorization to start a production rewrite in O14.
2. The target is a potential multi-organization product. Production work may add justified domain entities, routes, tenancy, authentication/session/account recovery, organization roles/memberships, audit history, migrations, backups/recovery, operations, and a supported runtime when the next approved prompt requires them. Do not interpret the O14 five-entity baseline or original 25 routes as a maximum; preserve their academic traceability and demonstrate the required course behavior.
3. `docs/research-2.0/` and its separate PR are proposed research, not automatically approved implementation requirements. Do not merge, cherry-pick, or silently implement that branch during O14. Reconcile it against the next reviewed prompt.
4. Do not deploy publicly or expose the Java 8/Spring Boot 2.1 academic runtime. A supported production architecture and any dual-track boundary must be validated before public operation; no serverless rewrite may erase the required Java coursework.
5. Do not invent Ateneo endorsement, stakeholder interviews, pilots, organization membership, institutional inventory, or adoption. The public demonstration, if later approved, must use synthetic data and have a documented cost and recovery plan.

## Working practice

- Prefer current source contracts and smallest coherent changes; preserve business behavior and test relevant failure/rollback cases.
- Reuse installed tools and scripts. Do not add dependencies, caches, build outputs, or duplicate release systems without a demonstrated need.
- Do not reset/delete any existing MySQL schema. Use only the guarded fictional `orgresource_test` schema for integration acceptance.
- Verify with command-line Playwright only; never invoke the in-app browser/Chrome-control plugins on this Windows installation.
- Finish independent, authorized work without repeated confirmation requests. Stop only for a real blocker or an action the user reserved for themselves.
