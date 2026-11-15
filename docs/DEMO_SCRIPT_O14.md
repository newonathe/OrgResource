# OrgResource student-operated demonstration plan

This is a real-use guide, not a recording or proof that a presentation happened. The assignment print copy requests a recorded group discussion for the final submission and describes an early-presentation alternative before exam week. Check current Canvas before choosing which path to use. Do not claim a group member, contribution, test, or outcome that was not actually observed.

## Before recording or presenting

1. **MANUAL STUDENT ACTION:** Open current Canvas and confirm the active rubric, submission module, due date, whether the recorded group discussion or early-presentation alternative applies, and any file/recording requirements.
2. Confirm who is actually in the group. Decide who will operate the computer and who will explain each part.
3. Follow [`PORTABLE_FIRST_RUN.md`](PORTABLE_FIRST_RUN.md) with a new local `orgresource` schema and fictional account/data. Do not record on a personal/shared schema or use real phone numbers.
4. Build and run the exact O14 JAR; confirm `/`, one `/#/projects` refresh, and all five list APIs. In MySQL Workbench, run `USE orgresource; SHOW TABLES;` and check the five expected table names.
5. Use names with a fresh fictional prefix such as `DEMO-2026-11-15-A` (change the date/suffix for the real recording). Make one Department, one Project, a reusable Resource, a consumable Resource, and a Requirement for each.
6. For issue/consume, Project start must be in the future when created and the action is allowed only during its active interval. Create the Project early enough that it becomes active before those steps. Do not alter the clock or edit database dates to fake an active Project.
7. If demonstrating FakeTwilio, use the simulator configuration and fictional reserved test destination only. The simulator must be the actual loopback service. Label every result accurately; never say a text was delivered.
8. Close unrelated apps, private notifications, credential dialogs, and real contact data. Keep passwords and student IDs out of video and terminal output.
9. Start the final packaged Java process from the PowerShell window that will be visible in the presentation. The portable launcher leaves reminders disabled; if using a separate simulator test, identify it as a separate test run.

## Main demonstration storyboard (about 5½ minutes)

This short version focuses on the application and its key stock rules. Prepare the fictional records and allow their Project to become active before recording so the recording does not contain a long wait. Show the actual app and actual result at every step.

| Time | Screen/action | Narration cue and expected actual result |
|---|---|---|
| 0:00–0:25 | Show the real Java console after startup, then the app at `http://127.0.0.1:8080/`. | State what OrgResource does and that it is a local academic prototype for one student organization. Do not call it an Ateneo-endorsed or deployed service. |
| 0:25–0:55 | Open Overview, then About. Point out Departments, Projects, Resources, Requirements, and Allocations. | Explain that the database is the persistent source of truth, and that the UI does not calculate its own inventory. State the authentication, image, and real-SMS boundaries if relevant. |
| 0:55–1:35 | In MySQL Workbench, run `USE orgresource; SHOW TABLES;`; return to the app and show Department and Project records. | Show the five actual entity tables. Briefly explain the project period and its Department relationship. Do not display credentials or private data. |
| 1:35–2:20 | Open Resource Library and Requirements. Show `10 Extension cords` as REUSABLE, `20 A4 paper sheets` as CONSUMABLE, and server-reported availability/coverage/shortage. | Explain that whole-unit amounts are fictional. Pending requests do not reserve inventory. |
| 2:20–3:15 | Open Allocations. Show a PENDING reusable request, approve it, then refresh Resource availability. Issue it after the Project is active; refresh again. | Expected: approval reduces available reusable stock by the request quantity; issue does not deduct a second time. Point to the server response, not a guessed client value. |
| 3:15–3:50 | Return the complete usable reusable batch and refresh. | Expected: outstanding custody is released and reusable availability rises by that quantity. The first Project’s Requirement remains covered as historical fulfillment. |
| 3:50–4:35 | Approve and consume the consumable request; refresh Resource and Requirement views. | Expected: physical consumable total decreases once; availability and requirement coverage reflect the persisted response. A repeated consume is refused. |
| 4:35–5:00 | Attempt a harmless invalid transition or show a dependency-protected delete response from the O14 acceptance collection. | Expected: a controlled 409 JSON response, no false success, and the record remains unchanged. Do not delete meaningful history. |
| 5:00–5:30 | Show the executed current Postman results or actual FakeTwilio test request/response in the simulator console. | Say “local simulator accepted/recorded this request” only if that is what the output says. `SIMULATED`, `PROVIDER_ACCEPTED`, and `DELIVERED` are different outcomes. |
| 5:30–5:50 | Stop Java with Ctrl+C, restart the same JAR, and reload the demonstrated record. | Expected: Java stops, MySQL stays up, and the fictional record remains after restart. Finish with course limitations and the next student action. |

If the actual UI, SQL, or restart takes longer, do not speed up or fabricate a result; use the longer appendix or the time allowed by the current rubric.

## Optional technical appendix (about 10–15 minutes)

Use this when the instructor requests all technical steps or there is more presentation time.

1. **Toolchain and Java 8:** show `java -version`, the Maven Wrapper version, and the exact package command/result. State which machine/runtime actually produced the output.
2. **MySQL:** show the local connection name (no username/password if private), `SELECT VERSION();`, `SHOW TABLES;`, and only a few fictional rows. The schema should contain exactly the five application tables.
3. **Controller path:** open one Jersey controller, its `@Autowired` Spring component, and the matching repository. Explain GET reads, POST commands, DTO validation, and the component boundary.
4. **Department and Project:** create a uniquely named fictional Department; create a Project with a future start and end after it; show the generated IDs and the Project list.
5. **Resource and Requirement:** create the two Resource types, add Requirements, and show server-derived availability and shortage.
6. **Allocation states:** create a PENDING request; compare availability before/after pending; approve; compare again; issue reusable stock; show no second deduction; return the whole usable batch. Then approve/consume a consumable and show the one-time physical decrease.
7. **Rejected operation and history:** show a repeat transition or referenced delete returning 409 and confirm the row still exists. Explain that historically approved Allocation records cannot be physically deleted.
8. **Scheduled reminders and FakeTwilio:** show the actual scheduled/test invocation and the actual loopback HTTP request/response. State whether it was a live scheduler run or an integration test. Use only a fictional test destination. No real SMS was sent.
9. **Postman:** import the current v2.1 executable collections from `postman/` and run them against localhost. The route reference collection is not one ordered mutation runner. Do not import `OrgResource-Demo.postman_collection.json` or `OrgResource-Negative.postman_collection.json`; they describe the older nine-entity application.
10. **Persistence:** stop and restart the same packaged JAR without resetting MySQL; GET the same fictional row again.
11. **Limitations:** no production authentication, campus integration, partial/damaged return workflow, real SMS-delivery evidence, or image subsystem. CLO9 remains unresolved.

## Expected-output checklist and recovery

- Java prints a normal Spring Boot startup completion without fatal database/Jersey errors. Do not narrate a successful start until the service actually answers.
- MySQL returns exactly `allocation`, `department`, `project`, `resource`, and `resource_requirement` for this schema.
- API create calls return JSON with generated IDs and HTTP 201. Successful list/update/lifecycle/delete calls return HTTP 200. Domain/validation failures return the documented JSON 400/404/409 and leave state unchanged.
- A UI action should show success only after the actual HTTP response. For a timeout or lost response, refresh and reconcile the record; do not blindly repeat a non-idempotent POST.
- If port 8080 is already occupied, stop only the OrgResource process you started with Ctrl+C. Do not terminate an unidentified process.
- If a request fails, stop the recording, note the exact error without exposing secrets, correct the local setup, and repeat the real action. Do not edit screenshots, synthesize console output, or splice a failed result into a success.
- If Java stops, use the same JAR and environment configuration to restart. Do not delete/reset MySQL as a recovery step.

## Student-only submission actions

**MANUAL STUDENT ACTION:** perform the actual demonstration with the real group; decide whether a recording or approved early presentation applies; create the real video/link if required; check that the instructor can access it using the current assignment/Canvas direction; finish peer reviews; personally complete the Certificate of Authorship; honestly acknowledge AI, web, course, and other help as required; upload the actual source/release files; and verify the successful Canvas receipt. None of these actions has been performed by this report.
