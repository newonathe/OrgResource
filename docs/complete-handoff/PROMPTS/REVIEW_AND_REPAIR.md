# Copy-ready independent review, repair, repo-resume and final challenge prompts

## A — Claude Code independent READ ONLY review of Codex work

Review the current OrgResource implementation against docs/ORGRESOURCE_MASTER_HANDOFF.md, the original current CSCI 180.05 assignment, syllabus, proposal and course examples. Inspect actual Git diff, test output and touched source files. DO NOT WRITE OR COMMIT during this review.

Check:
- Is source Java 8-compatible legacy Spring Boot/Jersey/Javax, not Spring MVC/Jakarta substitution?
- Exactly five entity types? Correct FKs/indexes/unique names/project-resource pair? Validation/nulls/ranges?
- Controllers truly delegate via @Autowired components, no business logic in Jersey?
- Are all original 25 route paths/methods preserved plus proposed A09? Only two-field availability response? No mandatory Resource unit?
- Do project active/expires/timezone rules work? Is historical approvedAt protected?
- Does stock math avoid double hold/consume? Are RETURNED resources available while old Requirement remains fulfilled?
- Does concurrency overbook resource or Requirement? Does stale expectedTotalQuantity guard actually reject? Does rollback work with checked exceptions and lock order?
- Are Project edits, Requirement reductions and deletions race safe?
- Does SMS genuinely use Retrofit+Gson+actual form HTTP, one claim, optional actual provider, fake acceptance not delivery, no real secrets in simulator logs?
- Is @Scheduled genuinely invoked, without disabled DB auto configuration?
- Is React UI real API-backed, responsive, keyboard accessible, with error/loading/stale flows and no fake numbers? Are datetime-local values PHT without UTC shift? Is public /api independent of static route?
- Is Maven/Eclipse-compatible source import actually verified or only documented? Does packaged JAR serve UI and API?
- Are Postman and browser tests run against actual DB or only mocks?
- Is original course-required video/export genuinely present? Any false PASS or fake screenshots? Any credentials/student identifiers in public repo?
For each finding: severity BLOCKER/HIGH/MEDIUM/LOW, exact file/line or repro, violated requirement/invariant, smallest fix, test to prove fix, and whether already demonstrated. End ACCEPT/REJECT/CONDITIONAL with explicit NOT_RUN evidence. Do not introduce unrelated framework rewrite.

## B — Codex targeted repair of accepted findings

Read the independent review report, inspect current branch, reproduce each accepted BLOCKER/HIGH defect, modify smallest set of relevant files, add regression test, execute real check, inspect diff and side effects. Preserve 5 entity schema/route contract unless explicit approved amendment. Do not change unrelated UI design or framework. Report before/after result, test failures and commit; no unauthorized merge/push.

## C — Resume once GitHub newonathe repository exists

Inspect exact verified remote newonathe/OrgResource (or actual name), main, all relevant PRs and feature branches, latest accepted commit, working-tree conflicts, docs master, AGENTS/CLAUDE. Don't trust earlier chat statements as proof current code exists. Gather environment/build/DB/test evidence, route coverage, UI implementation, known blockers, changed files and PR tests. Produce truthful implementation status and next smallest unblocked phase with copy-ready prompt. Do not merge/push/change unrelated user repositories.

## D — Full release adversarial audit (read only)

Treat project as actual course software, not proposal. Test or inspect independently where tools permit:
1. Can all registered Jersey routes execute, not just appear in source?
2. Can all five tables persist through real MySQL and restart?
3. Does Resource lock prevent concurrent stock overbook?
4. Does Requirement lock/coverage prevent concurrent excess demand?
5. Can stale Resource corrections overwrite consume?
6. Can one Allocation be issued/returned/consumed twice?
7. Can cancelled/expired approvals still reserve?
8. Can overdue ISSUED equipment incorrectly become available?
9. Can Project dates change after historical approval?
10. Can physical deletion erase approvals/completed history?
11. Can JPQL SUM/null or flush timing produce wrong totals?
12. Can DB failure partially commit consumable deduction?
13. Can scheduler duplicate SMS claims after restart?
14. Can returned/revoked Allocation still be dispatched once HTTP started (document race)?
15. Can fake SMS accidentally log real credentials?
16. Can public responses expose destination?
17. Can frontend clients set computed IDs/status?
18. Can UI claim success after lost POST response?
19. Does PHT datetime-local preserve wall-time semantics?
20. Does UI survive reload/backend restart?
21. Does UI work keyboard and mobile?
22. Does packaged JAR serve /, /assets and /api?
23. Can Java source import into Eclipse-compatible tool without absolute paths?
24. Does source ZIP contain all build files/lockfile?
25. Does Postman route template map actual 26 paths and dynamic fixture?
26. Are all course technologies evidenced, not just compiled dependencies?
27. Are any unrun tests reported PASS?
28. Is video genuine?
29. Are all proposal amendments visible and scholarly assistance acknowledged?
30. Does source manifest overclaim complete raw ZIP or video review?
Report source, actual commands, PASS/FAIL/BLOCKED/NOT_RUN, exact defect severity/repro, remediation, verdict. No edits absent authorization.
