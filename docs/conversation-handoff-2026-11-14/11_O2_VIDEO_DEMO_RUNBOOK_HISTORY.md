# O2 Skeleton video: completed user action, runbook, exact commands and course-grounded explanation

This retains the presentation engineering context, even though it is NOT the next coding task. USER later explicitly reported: **“done with the video.”** No video file, recording playback or Canvas submission receipt was independently inspected during this chat. The repo's older \`VIDEO_NOT_CREATED.md\` represents a dated stale state before the user recording.

## What user asked and why the detailed runbook existed
User nontechnical, requested a **very fast but complete, unambiguous** genuine Part II video. After initial straightforward instructions, user asked to **elaborate what the code actually does grounded in course materials and actual project significance**, without going overboard. Then user requested **literally all clicks, pasteable commands and filename paths** integrated into narration while keeping this ChatGPT conversation open and hidden from OBS recording.
The script used a 6–8 minute target, VS Code only, OBS Window Capture rather than Display Capture (so user could read the ChatGPT window without recording it), microphone audio test, Save MKV/Hybrid MP4 and Remux if needed. It used existing PowerShell scripts and a two-request Newman collection; no fake console screenshot, invented rows, raw passwords or live SMS. All code shown was actual five-entity skeleton; speech explained implemented vs proposed functionalities honestly.

## OBS Studio click path
1. Open OBS; Scenes + → name \`OrgResource Demo\`.
2. Sources + → Window Capture → \`VS Code Only\`, select actual Visual Studio Code OrgResource window.
3. Source context menu Transform → Fit to Screen. Disable any Display Capture source (eye icon); test ChatGPT overlap does not appear.
4. Audio Mixer check Mic/Aux meter. Settings→Audio if needed. Settings→Output→Recording path; Hybrid MP4 if available or MKV→File→Remux Recordings later.
5. Do a 10-second actual test including moving this ChatGPT browser window over VS Code, speak into mic, stop, replay to ensure only VS Code and audio captured. Keep VS Code not minimized.
6. During recording, use mouse/Alt+Tab to read ChatGPT privately while OBS captures only VS Code window. If user switched to browser Chrome, it would not appear, so instead REST JSON was shown via VS Code integrated PowerShell. Test actual preview, don't assume Window Capture worked.

## Workstation/environment snapshot for the O2 demo
VS Code open repository root:
\`C:\Users\ethan\Downloads\Academic\4th Year\CSCI 180.05\OrgResource\`.
Select File→Open Folder if necessary. Explorer must show \`backend\`, \`scripts\`, \`postman\`, \`deliverables\`, \`evidence\`. Terminal→New Terminal, two integrated PowerShell terminals; Terminal A keeps Java running, Terminal B SQL/Newman. Terminal start location must be repository root, NOT backend. If mislocated:
\`\`\`powershell
Set-Location 'C:\Users\ethan\Downloads\Academic\4th Year\CSCI 180.05\OrgResource'
\`\`\`
MySQL target \`127.0.0.1:3308\`, database \`orgresource\`, user \`orgresource_dev\`, protected credentials via \`%LOCALAPPDATA%\OrgResourceDev\`; NOT Workbench profiles 3306/3307. Java8 was configured via local Temurin JDK and launcher, HTTP \`127.0.0.1:8080\`. Never type password into video.

## Commands copied into actual integrated terminals
Terminal A launch (kept running until recording ended):
\`\`\`powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-skeleton-demo.ps1
\`\`\`
Recorded criteria: Java 8 selected; Maven build \`BUILD SUCCESS\`, 8 unit tests pass in O3 context, Spring Boot \`Started SpringBootApp\` without fatal exception. If actual command fails, do NOT say pass; script checks protected config/3308/8080 and refuses unsafe port takeover/DB reset.

Terminal B read-only database SQL:
\`\`\`powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-skeleton-demo-sql.ps1
\`\`\`
Shows database name orgresource; five InnoDB tables \`allocation\`, \`department\`, \`project\`, \`resource\`, \`resource_requirement\`; FK relationships Project→Department, Requirement→Project+Resource, Allocation→Requirement; row counts and fictional sample records (counts can differ after POST). No customer phone/borrower name selected by SQL script. Do not assume exact previous saved row counts in new run.

Browser GET was initially suggested, but to keep OBS capturing VS Code, final script used Terminal B:
\`\`\`powershell
Invoke-RestMethod 'http://127.0.0.1:8080/api/departments' | Select-Object -First 3 | ConvertTo-Json -Depth 4
Invoke-RestMethod 'http://127.0.0.1:8080/api/resources' | Select-Object -First 3 | ConvertTo-Json -Depth 4
\`\`\`
Prove real HTTP GET and JSON from running Jersey/Java/MySQL. Details visible may vary; when empty, mention no matching rows honestly.

Terminal B focused Postman/Newman:
\`\`\`powershell
npm exec --yes --package=newman -- newman run postman\OrgResource-O1-Skeleton-Demo.postman_collection.json --reporters cli
\`\`\`
Two actual operations: 1. POST /api/departments with unique fictional \`Skeleton Demo Department ...\`, expect 201+generated ID; 2. GET /api/departments and assert same ID/name persisted. Four assertions; preflight saved PASS 2 requests/4 assertions/0 failures. The video must show fresh results before saying pass. Collection uses timestamp/random suffix, creates an additional safe fiction Department and intentionally does not delete it.

Source code opened through VS Code \`Ctrl+P\` + paste relative repository path:
- \`backend/src/main/java/app/entities/Project.java\`: @Entity/@Id/JPA associations, Department FK, Project start/end.
- \`backend/src/main/java/app/entities/ResourceRequirement.java\`: Project+Resource and quantity; distinct need vs inventory.
- \`backend/src/main/java/app/entities/Allocation.java\`: requirement quantity, borrower name and PENDING etc.
- \`backend/src/main/java/app/repositories/AllocationRepository.java\`: \`findByRequirement_Id\`, \`findByStatus\`, \`findByRequirement_IdAndStatus\`; JPQL \`sumCoveredQuantity\` etc. Do NOT imply full inventory engine done; expired approval coverage bug open.
- \`backend/src/main/java/app/repositories/ResourceRequirementRepository.java\`: \`findByProject_Id\` and \`existsByProject_IdAndResource_Id\`, supports unique Project/Resource.
- \`backend/src/main/java/app/rest/controllers/DepartmentController.java\`: Jersey \`@Path("/departments")\`, GET/POST, Spring \`@Autowired\`, delegate \`component.create/list\`; the new D03 update method.
- \`backend/src/main/java/app/components/DepartmentComponent.java\`: process normalize duplicate, use repository save/list/update.
- \`backend/src/main/java/app/repositories/DepartmentRepository.java\`: extends \`JpaRepository\`.
- \`backend/src/main/java/app/config/JerseyConfig.java\`: \`@ApplicationPath("/api")\`, individually registers all five controllers.
- Show \`deliverables/OrgResource-Java-Skeleton-2026-11-14.zip\`: Maven/Eclipse-compatible source ZIP, clean extraction Maven tests reported; actual Eclipse GUI import not verified.

## Course-grounded spoken narration highlights
Introduction: OrgResource is a hypothetical student organization's shared equipment/supply planning service, not campus-endorsed. Example a workshop needs extension cords and paper. Resource is what organization has, Requirement is project need, Allocation is request/commitment. Five tables justified by relationships.
Spring Boot: Java8 app dependency injection and startup, build tests and connect to MySQL. JPA/Hibernate Java object mappings and relational FK. Course CLO3 Spring/Jersey, CLO5 persistence.
Repositories: Spring Data \`findBy\` methods and JPQL aggregates for holds/custody/coverage; not simply hardcoded return values, but full lifecycle still forthcoming. Course CLO6.
Jersey/Components: \`@Path\` maps route, GET read, POST write, \`@Autowired\` delegates thin controllers to Spring service components, repositories persist; same course Calculator/Animal Sighting patterns. Course CLO3, project explicit requirement.
Postman/JSON: actual GET JSON through PowerShell, Newman POST201/GET200, generated ID persisted, demonstrates realistic create/read. Course CLO4.
Conclusion: part II establishes startup, 5 connected entities/tables, components, repos, live JSON. The complete allocation transitions, scheduling, Retrofit/Twilio, React frontend and final project are future work (not demonstrated).
End: OBS Stop Recording; File→Show Recordings; if MKV File→Remux Recordings; save suggested \`OrgResource-PartII-Skeleton-Demo-2026-11-14.mp4\`; replay to check audio/legibility/no ChatGPT; Terminal A Ctrl+C to gracefully stop Java; source ZIP and actual MP4 to Canvas subject Canvas acceptance. User later stated recording DONE; do not require replay in new chat unless user asks.

## Relevance to next conversation
Do not spend development tokens on a new O2 demo, video narration or OBS configuration unless user specifically asks for revisions. This history exists to preserve the accomplished milestone and its technical/academic evidence boundary. Focus on actual O3 Codex completion and next O4+O5 build.