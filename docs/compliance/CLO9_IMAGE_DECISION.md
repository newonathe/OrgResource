# CLO9 image-scope decision

> **Course A status (2026-11-17):** Resource image receive/store/serve is implemented locally under the user-approved Course A scope. `ResourceImageHttpMySqlTest` exercises upload/retrieval through HTTP against MySQL, including PNG/JPEG, a >64 KiB image, the exact 2 MiB limit, oversize/invalid/truncated inputs, MySQL BLOB bytes and SHA-256. Implementation evidence is recorded in `evidence/COURSE-A/A1_A2_ACCEPTANCE_REPORT.md`. Whether the course rubric requires this feature in OrgResource, and whether the instructor accepts it, remain **NOT_VERIFIED**; Canvas was not checked.

**Current technical status: IMPLEMENTED LOCALLY. Academic interpretation and instructor acceptance: NOT_VERIFIED.**

## Primary-source findings

| Source locator | What it says | Effect on OrgResource scope |
|---|---|---|
| `ChatGPT-Project-Pack/UPLOAD/08_CSCI180_SYLLABUS.pdf`, p. 2, Section C | CLO9 says students create a web application that receives and serves images via HTTP. CLO10 says students define an application using previously discussed functionality for the final project. | Establishes CLO9 as a course outcome and CLO10 as the final-project outcome, but does not say every project must implement every preceding CLO. |
| Same syllabus, p. 3, Section D | The course outline lists “Discussion of Image saving to the Database” under CLO9 and “Final Project proposal and implementation” under CLO10. | Separates image persistence from the final project in the outline. |
| Same syllabus, p. 4, Sections E/G | The project proposal and two-stage implementation assessment rows are mapped to CLO9; the teaching-method table also maps “Project” to CLO9. | Conflicts with p. 2 and p. 3, which map the final project to CLO10. This is a material syllabus inconsistency. |
| `ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf`, p. 1 | Requires use of the tools used to date and enumerates Jersey REST, JSON, Spring annotations, JPA/Spring Data, Javax Validation, “RetroJit” (at least Twilio), and scheduling. It requires at least five justified entity types, runtime create/delete operations, controller-to-component `@Autowired` forwarding, and Postman definitions. The enumerated list does not name image upload/storage. | The broad “all tools” wording could include images taught before the project, while the explicit tool list and project checklist do not specify an image operation. It does not settle the scope. |
| Same assignment, pp. 2–3 | Part III requires completing the actual logic of the proposed application and submitting the final project and a recorded group discussion; p. 3 makes the UI optional bonus and describes an early-presentation alternative. | Does not name a separate image endpoint or image-storage behavior. Current Canvas instructions still need student verification. |
| `ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx`, all 12 pages | Defines Department, Project, Resource, ResourceRequirement, Allocation, and the original 25 API operations. It has no image field, image route, image ownership, or image persistence contract. | The implemented five-entity, 25-route baseline does not include images. |
| Local course source `..\files\ImageServerDemo (1).zip` and `ChatGPT-Project-Pack/UPLOAD/06_VIDEO_TRANSCRIPTS.md`, V10 | The example is a separate image server with a separate `UploadedFile` entity and upload/retrieval endpoints. The inspected implementation writes incoming filenames to `temp/` without adequate type, size, or path validation. The transcript describes integration as optional. | Demonstrates a course pattern, but does not say it is mandatory for this project; it is unsafe to copy unchanged. No raw video was reprocessed. |

## O14 decision (historical; superseded for implementation status)

At O14, OrgResource had five JPA entities and 26 operations (25 proposal routes plus A09), without image receive/serve. The syllabus’s CLO mappings conflict internally, the assignment’s broad “all tools” sentence is not limited by a clear image exception, and the current Canvas rubric was unavailable through the connected tools. These sources still do not establish whether image functionality is required in OrgResource or assessed separately.

The later user-approved Course A specification authorizes the two image routes. Their local implementation evidence does not itself settle the academic interpretation or constitute instructor sign-off.

## Student clarification to check in Canvas or ask the instructor

> For CSCI 180.05, must the OrgResource final project itself receive and serve images over HTTP and save them to a database to satisfy CLO9, or is that outcome assessed separately? The syllabus p. 2 and p. 3 map CLO9 to images and CLO10 to the final project, while the p. 4 assessment table maps the project to CLO9. If OrgResource images are required, what record should own the image, what storage design is expected, and are additional API operations or database fields allowed?

No message was sent. The student must check the current Canvas rubric and obtain any needed written clarification before claiming full course compliance or adding an image feature.
