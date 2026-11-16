# OrgResource Course Final A demonstration guide

This guide is a rehearsal aid. It is not a recording, Canvas submission, instructor approval, or proof that a live presentation has happened. Use fictional data only. Confirm current Canvas instructions, actual participants, and required video/link format before recording.

## Before the recording

1. Open the extracted OrgResource source folder in VS Code. Keep the release folder outside the repository.
2. Confirm MySQL is running. For the presentation, create a new empty local `orgresource` schema and least-privileged account using [the portable first-run guide](PORTABLE_FIRST_RUN.md). Do not use or clean the accumulated `orgresource_test` acceptance database.
3. Confirm JDK 8 is active with java -version. The package receipt records the exact Java version used for the release.
4. Stop any old OrgResource process using Ctrl+C in the PowerShell window that started it. Do not kill an unidentified process.
5. Start the exact packaged JAR:

   .\scripts\start-portable-demo.ps1 -JarPath 'C:\path\to\OrgResource.jar' -PromptForPassword

   The script prompts for the local schema account password without saving or printing it, starts Java on 127.0.0.1:8080, and leaves MySQL running. The caller should replace the example path with the final A3 release JAR path.
6. Wait for the Spring Boot started message. Open http://127.0.0.1:8080/ and confirm the Overview screen loads.
7. In MySQL Workbench choose the new local `orgresource` connection and open scripts/course-a-demo.sql. Run it as read-only. Confirm five tables, InnoDB relationships, row counts, and fictional rows. Do not show phone numbers or borrower names. The SQL file uses the currently selected schema and does not switch databases.
8. Import postman/OrgResource-Course-A-28-Routes.postman_collection.json and postman/OrgResource-Course-A-Local.postman_environment.json. Set the environment as active. The 28-route file is a reference with variables, not the ordered Newman runner. For automated execution use scripts/run-api-acceptance.ps1.
9. Use names prefixed with “Course A Demo” and today's short date. Do not use a real person, phone, organization account, or live Twilio credentials.

## Main demonstration — approximately 5 minutes

| Time | Screen and action | Narration points | Expected observable result |
|---|---|---|---|
| 0:00–0:25 | Show the local app URL and Overview. | Identify OrgResource as a local CSCI 180.05 academic prototype for one student organization. State that records are fictional and the app is not an institutional production service. | Overview page and local URL are visible. |
| 0:25–0:50 | Show Workbench selection and run the read-only SQL. | Explain the five business records and their links. | Tables are department, project, resource, resource_requirement, and allocation; row counts display without exposing contact data. |
| 0:50–1:35 | In Departments create a fictional department. In Projects create a future workshop assigned to it. | Show that each record is persisted through the API and that a Project belongs to a Department. | New Department and Project appear in their lists. |
| 1:35–2:05 | In Resource Library create a reusable Resource with 8 whole units and a consumable Resource with 20. | Explain that availability is calculated on the server and the two resource types have different lifecycles. | The initial availability equals total quantity for each new Resource. |
| 2:05–2:35 | In Requirements create a requirement for 4 reusable units. In Allocations create a pending request for 3. | State that pending requests record demand but do not reserve stock. | Requirement shows shortage; reusable availability remains 8 while request is PENDING. |
| 2:35–3:15 | Approve the request, then issue it. | Show approval creates a hold. Explain that issue moves the allocation to custody without a second stock deduction. | Availability becomes 5 on approval and remains 5 on issue. |
| 3:15–3:40 | Return the full reusable batch. | Explain that this workflow accepts the complete returned batch as usable; partial or damaged returns are not represented. | Status becomes RETURNED and reusable availability returns to 8. |
| 3:40–4:15 | Create a requirement for 5 consumable units, create and approve the request, then consume it. | Explain that consumption decreases physical stock once and changes the Allocation state in one transaction. | Consumable total and availability each change from 20 to 15; coverage reflects the fulfilled quantity. |
| 4:15–4:35 | Attempt a protected delete or invalid lifecycle action using an existing fictional record. | Explain that dependencies and approved history are preserved, and the API returns a controlled conflict. | A visible 409/error message appears; the protected record remains. |
| 4:35–5:00 | Show one Postman request and the UI refresh. | Explain that Postman provides the REST call reference; automated regression is a separate runner. Mention that reminders are disabled in the demo and no real SMS is sent. | Postman shows a real local response; the UI refresh shows persisted server state. |

If a step fails, stop the narration, read the visible status/message, and recover from the checklist below. Do not claim the operation succeeded if it did not.

## Optional technical appendix — 5 to 8 minutes

- Show the exact JDK 8 version and Maven Wrapper; do not run a clean build while the JAR is running.
- Show MySQL SHOW TABLES and the foreign-key query from scripts/course-a-demo.sql.
- Use the Postman reference to show create/list/update request shapes. Use the ordered Newman runner only after selecting the local application and test database.
- Demonstrate the image contract only with the tracked synthetic PNG: upload a bounded image, show JSON metadata and SHA-256, retrieve the image, and compare its bytes using the documented acceptance script. Do not imply the UI has image upload.
- Explain notification outcomes accurately: FakeTwilio is a simulator; loopback Twilio protocol acceptance is not delivery. Reminder mode defaults to DISABLED.
- Restart Java with Ctrl+C and the same startup command, then reload a fictional record to show MySQL persistence.
- Show any limitations relevant to the course: no authentication, no institutional deployment, no live SMS, no confirmed instructor interpretation of CLO9.

## Recovery and safe stop

- If the app does not load, confirm the startup window reports Java started and that MySQL is running. Read the error without displaying environment variables or secrets.
- If port 8080 is busy, identify and stop only the OrgResource process started for this demonstration. Do not terminate an unknown process.
- If a test fixture cannot be created because a duplicate name exists, choose a new fictional suffix. Do not delete unrelated rows.
- If Postman reports an unresolved ID, create the prerequisite record or use the ordered acceptance runner; do not paste a guessed database ID.
- If SQL selects the wrong database, stop and select the newly created local `orgresource` connection. Never run the walkthrough against a personal/shared database or the accumulated `orgresource_test` acceptance schema.
- Stop Java with Ctrl+C in the startup PowerShell. MySQL stays available and records remain. Restart with the same command.

## Recording and student-controlled checks

Before recording, the student must confirm the live due date, rubric, presentation/video requirement, actual group participants, and submission instructions in Canvas. The student must perform the real demonstration, record the actual participants if required, review the recording, set and verify instructor link access, complete authorship and peer-review obligations personally, upload the actual files, and verify a Canvas receipt. No recording or submission is included in the A3 technical package.
