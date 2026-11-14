# OrgResource Planning Kickoff

I plan to build the OrgResource Project as my CSCI 180.05 Enterprise Systems Programming final project. Use this ChatGPT Project to research, plan, specify, pressure-test, and prepare a complete handoff. I will bring the reviewed plan to Codex and do the actual implementation there in a sequential build process. This chat is for planning; do not represent proposed work as code that exists or has passed verification.

## Begin with source intake

Read the attached source documents: the manifest, course corpus, existing OrgResource drafts, public-research archive, syllabus, professor's final-project assignment, previous proposal, and timestamped audio transcripts from the course videos. The raw videos are intentionally not attached because video upload and analysis are unreliable for this workflow. Do not ask me to upload them again as a default. Make a coverage matrix showing what you inspected, what is extracted text, what is machine-transcribed, what is only metadata, and what remains unreadable or ambiguous. Map equal hashes to one canonical file while retaining every original path. Cite filenames, page/section, transcript timestamp, and source URL. Do not claim complete coverage unless the matrix proves it.

The upload set also contains two `COMPLETE_SOURCE_NONVIDEO` ZIP files. Together they preserve the exact bytes and original relative paths of every original non-video filesystem file, including nested ZIPs. Treat the course corpus and consolidated context document as the search/index layer; inspect the archived original when an exact source matters. An archive's presence does not prove that each entry was read. The transcript is machine-generated and covers audio only. The course Project cannot inspect video frames or screen contents. Use slides, labs, code, and the course corpus to corroborate technical details. If a necessary claim depends only on a screen detail from a recording, cite the timestamp and ask me to inspect the local original; do not claim to have watched it.

Create a conflict and decision register before accepting any prior design. Reconcile the earlier nine-entity specification against the five-entity concise proposal (Department, Project, Resource, ResourceRequirement, Allocation), differing operation inventories, and contrary statements in earlier audits. Check each against the actual assignment and syllabus. Neither design is final by default. Preserve sound ideas, reject unnecessary breadth, and explain the chosen feasible scope.

Keep these issues explicit until evidence or a documented assumption resolves them:

- The assignment prints “RetroJit”; the syllabus and examples show Retrofit/GSON. Do not invent an API.
- The final Twilio proof path is unclear: FakeTwilio simulation, provider test environment, or live messaging. Do not silently substitute one or send real messages.
- CLO9/image serving is a course outcome; the assignment's tools list does not separately define an image feature.
- Runtime deletion must be reconciled with preserving transaction history.
- Demo identity is not production authentication.
- Confirm group roster, current Canvas deadlines, what was actually submitted, and what actually runs.
- Do not treat the syllabus's one-week wording as an approved group-project extension.

## Research beyond the supplied drafts

Use supplied materials as evidence and course constraints, not as a ceiling on solution space. Explore credible problem framings, architecture, stacks, delivery paths, and development processes. Use dated web research and primary sources to verify current version compatibility, official API behavior, security/privacy needs, applicable Philippine guidance if real data or messaging is involved, operating/hosting costs, and other material claims. Cite direct links near claims. Compare realistic alternatives against course compliance, schedule, technical risk, cost, learning value, maintainability, privacy, testability, and the 5 December deadline. Do not invent Ateneo workflows, user demand, interviews, adoption, impact, costs, or provider behavior. Do not add fashionable infrastructure without a clear reason.

## Plan against the actual remaining time

The planning date is 11 November 2026, Asia/Manila. The supplied assignment lists Part II skeleton target 13 November, Canvas submission 14 November, final implementation due 5 December, and Part I due 30 October. The syllabus says dates may change and tells students to check Canvas. First determine which milestones remain open and what Canvas status must confirm. An artifact in the folder is not proof it was submitted. Make an immediate recovery plan for the skeleton milestone only if it is still relevant, then a credible complete-scope plan to 5 December. Protect a working, coherent core ahead of optional UI and expansion.

## Required planning outputs

1. Source coverage, evidence hierarchy, conflict register, and unresolved-source list.
2. Grounded product/problem framing, credible alternatives, intended users, boundaries, evidence gaps, and a value statement without unsupported Ateneo claims.
3. Course requirement traceability matrix: exact requirement and source locator, design element, implementation evidence, and acceptance check.
4. Complete product specification: actors, requirements, workflows, business rules, states, calculations, edge cases, errors, and acceptance criteria.
5. Justified minimal domain model and ERD: field types, nullability, validation, relations, indexes, uniqueness, deletion/retention, transactions, concurrency, and audit/history.
6. Course-compatible, version-pinned architecture and stack: project structure, controller/component/repository responsibilities, JSON contract, API catalogue, Retrofit/Twilio boundary, scheduling, image/CLO9 decision, compatibility risks, and alternatives.
7. Realistic security/privacy design for the prototype, including identity limits, secret handling, data minimization, authorization, failures, and conditions for live operation.
8. DevOps and delivery plan: source control, solo-student branch/commit/review method, reproducible Eclipse/build setup, dependency pinning, local MySQL, configuration/secrets, CI that can actually run with the chosen stack, backups/logging, project export, and demo/deployment decision. State cost/platform assumptions.
9. Staged SDLC/backlog with dependencies, effort bands, critical path, skeleton/final gates, dated schedule, risks, and fallback cuts. Keep optional work behind the functioning database/API/demo core.
10. Verification plan, executable Postman order, fictional seed data, skeleton demo script, final demo checklist, and evidence to collect. Distinguish planned, implemented, and verified.
11. Independent critique and revised decision log addressing model complexity, availability math, races, inventory conservation, validation, duplicate reminders, provider failure, data retention, scope, course compatibility, schedule, and unsupported claims.
12. Staged Codex handoff: ordered small phases with exact inputs, expected files, dependencies, manual actions, compile/run checks, acceptance criteria, and pause points. Do not replace specifics with “implement normally.”

Provide thorough specifications, not vague checklists. Cite course claims and current external facts. Distinguish fact, interpretation, recommendation, assumption, and open question. Ask only essential questions; when information is missing, state a provisional assumption, its consequence, and what evidence resolves it, then continue.

Start with source intake and the highest-risk conflicts/milestone questions. Then continue with work that does not depend on my answers; do not stop after an intake report.
