# OrgResource O2: video recording without Codex or Claude Code

**For the actual CSCI 180.05 skeleton demonstration due November 14, 2026.** This guide and scripts were prepared from the source in newonathe/OrgResource, but have NOT been executed on your Windows machine from this chat. The real screen recording must show your actual running application; no fabricated console, database or screen output is acceptable.

## What is already done

- The original repository already contains a five-entity Java/Jersey/Spring Data JPA skeleton.
- The saved O1 execution report records Java/Maven build success, five MySQL tables, and a 15-request Newman run with 29 passing assertions.
- The exported Java source archive already exists at deliverables/OrgResource-Java-Skeleton-2026-11-13.zip.
- This new branch adds scripts and a corrected standard Postman v2.1 collection WITHOUT changing backend source, main branch or data.
- A real Eclipse import, today’s local app restart, and an actual demo video were NOT performed by ChatGPT.

## Your only preparation: get the kit onto your Windows PC

1. Sign in to your GitHub account newonathe in a browser.
2. Open your private repository: https://github.com/newonathe/OrgResource
3. Select the branch called demo/o2-recording-kit-2026-11-14.
4. Click Code > Download ZIP. Extract the ZIP into a normal folder, such as Documents/OrgResource-Demo.
5. Open VS Code. Choose File > Open Folder and select the extracted folder containing backend, scripts, docs and postman.
6. Keep MySQL Workbench and Postman available. Use the SAME Windows computer that previously had the O1 database/credentials provisioned, if possible.
7. Do NOT use the old historical 68-route Postman collection. Use postman/OrgResource-O2-Video-Demo.postman_collection.json.

## Preflight (BEFORE clicking Record)

The prior O1 report recorded MySQL Server 8.0.46 at 127.0.0.1:3308 and database orgresource. It also reports a local DPAPI-protected password outside GitHub under %LOCALAPPDATA%\OrgResourceDev. We cannot access or decrypt it from this chat. The startup script accepts existing ORG_DB_* variables or asks for the local MySQL password with hidden input. If you do not know the password or the local database is absent, this is a genuine blocker, not something ChatGPT may pretend to fix remotely.

1. In VS Code, select Terminal > New Terminal.
2. Verify terminal is PowerShell and at repository root (folder containing backend and scripts).
3. Check Java 8 is available. If it is not, select the actual Java 8 JDK that the O1 run used.
4. Make sure the original local MySQL instance is running before Java startup.
5. Paste ONE command (shown below). If prompted for password, enter the existing LOCAL database password; the input is hidden. Do not share it in chat.
6. Confirm real output shows BUILD SUCCESS (if building), Spring Boot, Hibernate/JPA, Jersey and Tomcat listening on port 8080.
7. Open a second VS Code terminal for read-only API checks if useful.
8. Open MySQL Workbench and connect to the orgresource schema at actual configured port. Open scripts/skeleton-demo.sql and run it. Confirm five tables and fictional rows.
9. Import the O2 Postman collection and run it. It requires at least one existing Resource row for availability test; saved O1 test database had four.
10. Stop Java cleanly with Ctrl+C if you want to show an authentic fresh startup on camera. Keep MySQL running.

Startup command (paste exactly from repository root in PowerShell):
~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-skeleton-demo.ps1
~~~

Optional check command in SECOND terminal while Java runs:
~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-skeleton-demo.ps1
~~~

IMPORTANT: The startup script checks Java 8 and builds the original backend using the Maven Wrapper. It does not reset or delete any DB. It DOES NOT know an absent real password. If prompted, your input is kept out of logs/source, but as with any environment variable, processes with sufficient local privileges may inspect the process; use only your isolated development computer.

## Postman import and run

1. Launch Postman.
2. Click Import > Files, select postman/OrgResource-O2-Video-Demo.postman_collection.json.
3. Look for collection "OrgResource O2 ACTUAL Skeleton Video Demo — 12 Requests".
4. The collection has its own default baseUrl http://127.0.0.1:8080/api, so no external environment import is required.
5. Open the collection's three-dot menu and choose Run collection. Use the original saved request order 01 through 12.
6. Click Run. Wait for REAL statuses/results. The suite creates one uniquely named fictional Department and later deletes that same newly created record, without modifying existing Departments/Projects.
7. Expected statuses: 01 GET200; 02 POST201; 03 GET200; 04 GET200; 05 GET200; 06 GET200 (Resource must exist); 07 GET200; 08 GET200; 09 POST400 invalid quantity; 10 POST409 duplicate; 11 POST200 delete; 12 GET200. Error statuses 400/409 are EXPECTED negative-test successes, not server crashes.
8. If any assertion fails, do not hide it or claim all tests pass. Show actual result, or use the manual GET/POST fallback below.

## Record 4–6 minutes, one uncut desktop video

Suggested recorder on Windows 11: Snipping Tool > Record > New > select whole desktop > Start; test microphone and playback first. If it cannot capture microphone or application switching, use OBS Studio with Display Capture and Mic/Aux; do not use a recorder that captures only one window if you need to switch applications. Turn on Do Not Disturb; hide passwords, email tabs and real personal information.

SHOT 1 (0:00–0:25) — VS Code Explorer, open backend/src/main/java/app. Say: "This is OrgResource, our Java/Jersey resource-planning skeleton for CSCI 180.05. I developed the source using VS Code while maintaining an Eclipse-compatible Maven project."

SHOT 2 (0:25–1:15) — VS Code terminal, run the startup command above AFTER stopping any earlier Java instance. Keep the real BUILD SUCCESS and Tomcat port 8080 lines visible. Say: "This starts the Spring Boot backend and connects to MySQL. Jersey exposes the REST services under /api." If actual startup takes longer, let it finish; do not fabricate a quick startup.

SHOT 3 (1:15–2:10) — MySQL Workbench. Open/run scripts/skeleton-demo.sql. Show SHOW TABLES (department,project,resource,resource_requirement,allocation), row counts, foreign keys, fictional rows. Say: "These five tables correspond to my JPA entities and use real foreign keys. This query shows persisted fictional test records, not hardcoded HTTP examples."

SHOT 4 (2:10–3:10) — VS Code Ctrl+P ResourceRequirementRepository.java, show findByProject_Id and existsByProject_IdAndResource_Id. Ctrl+P DepartmentController.java show @Path/@GET/@POST/@Autowired and component calls. Ctrl+P DepartmentComponent.java show repository use. Say: "Repositories handle database access. Jersey controllers forward incoming JSON requests to injected Spring components, which call the repositories."

SHOT 5 (3:10–4:35) — Postman, run the O2 12-request collection, observe actual 200/201 and expected 400/409. Point to newly created Department in real list and its later safe deletion; show GET Resources and availability. Say: "These genuine HTTP calls demonstrate create, read, guarded delete, field validation, and JSON responses across the skeleton. The request variables use real generated database IDs."

SHOT 6 (4:35–5:00) — VS Code Explorer or File Explorer: deliverables/OrgResource-Java-Skeleton-2026-11-13.zip. Say: "This is the Java source export, including Maven and Eclipse-compatible project metadata. The Maven clean-extraction build was recorded successfully. The actual Eclipse IDE import was not tested. Full inventory lifecycle, external SMS, scheduled jobs and React UI are future final-project stages."

Click Stop. Save as OrgResource_O2_Skeleton_Demo_2026-11-14.mp4. Play once, verify audible voice and legible terminal/SQL/Postman. Upload actual exported Java ZIP plus actual recording/video link to Canvas as allowed by live assignment. Retain submission receipt.

## Browser fallback: if Postman fails

While Java runs, open these on the SAME machine:
- http://127.0.0.1:8080/api/departments
- http://127.0.0.1:8080/api/projects
- http://127.0.0.1:8080/api/resources
- http://127.0.0.1:8080/api/requirements
- http://127.0.0.1:8080/api/allocations

They should return actual JSON arrays, possibly empty if no rows. To demonstrate POST manually, use Postman New > HTTP Request > method POST > URL http://127.0.0.1:8080/api/departments > Body > raw > JSON:
~~~json
{"name":"O2 Manual Video Department 20261114-A"}
~~~
Expected 201 and generated id. GET /api/departments again to find it. On another run use a new unique suffix to avoid duplicate 409. Never post statuses/quantities to unfinished operations.

## Recorded evidence and limitations

The current Java source implements O1 skeleton only. Its saved Newman report is from Nov13 local execution; you must record current real output in this video. This kit is a convenience layer, not a backend feature implementation or an independently tested real Windows startup. ChatGPT cannot operate local Windows apps, verify current private DB password, run real Eclipse IDE import, record real desktop or submit Canvas here. If a step fails, fix the real environment rather than stitching a simulated result into the video.
