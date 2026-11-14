# OrgResource — Repository rules for Codex and other coding agents

1. Read docs/ORGRESOURCE_MASTER_HANDOFF.md completely, and relevant original course assignment/syllabus/concise proposal and code, before changing interfaces. Source authority: assignment > syllabus/current verified Canvas > user decisions > original course examples > 25-route proposal > reviewed amendments.
2. User develops in VS Code; produce Eclipse-compatible Java source export, actual MySQL backend, real React UI.
3. Exactly five JPA entities: Department, Project, Resource, ResourceRequirement, Allocation. 25 original routes plus proposed A09 consent withdrawal. Do NOT add StockMovement, a unit field, expanded availability JSON, overdue reminder extension, fake Admin auth or second backend.
4. Java backend is course-compatible Java 8 Spring Boot/Jersey, Spring @Autowired components, JPA/Spring Data/Javax, Retrofit/Gson, @Scheduled, MySQL. Modern React/TypeScript/Vite frontend has independent Node toolchain.
5. Stock math and Allocation transitions defined in master. Database transaction is source of truth. No double consume, no overbook, no unsafe deletion, no fake DB effects.
6. Implement actual responsive UI with loading/empty/error/retry and keyboard/mobile states, real server data, no fabricated analytics.
7. Local-only fictional data, SMS disabled by default; no real contacts or provider credentials in logs, commits, screenshots or simulator.
8. Inspect git status/remotes and existing user edits. One writer per file/worktree. No unauthorized push/merge/reset/database drop/Canvas submission.
9. Every phase report actual files, commands, tests PASS/FAIL/BLOCKED/NOT_RUN, schema/HTTP/UI proof, warnings, diff, commit status and next gate. Never claim static code or sample JSON is runtime proof.
10. Stop on essential blocker; otherwise implement the smallest tested slice without repeatedly seeking permission for routine local edits.
