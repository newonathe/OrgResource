# OrgResource Master Context Dossier

## Purpose and boundaries

This dossier orients the complete source package. It does not replace the extracted course corpus, original assignment and syllabus PDFs, timestamped transcripts and video parts, source manifest, or prior proposals/specifications. Read those attached files directly. The original CSCI 180.05 folder remains unchanged; the pack is a separate, newly generated planning artifact.

The intended outcome is a complete, feasible OrgResource final project plan and a staged handoff to Codex for later implementation. The current user request calls for a broad, evidence-grounded review of project materials, external research, product/model/architecture/stack/DevOps/SDLC planning, and a critical review. This ChatGPT Project is for planning. Do not submit coursework, contact the instructor, register services, buy credits, send messages, or claim implementation.

## Working product idea, open to critique

Existing OrgResource drafts frame an internal resource planning and allocation service for one Ateneo student organization with multiple departments. It connects project requirements to organization-controlled resources and date-specific availability, captures allocation decisions and fulfillment, and surfaces shortfalls. The drafts exclude institutional procurement/finance, resources not controlled by the organization, and a general ERP. This is a candidate framing, not validated demand, institutional endorsement, or a fixed architecture.

Earlier public research cites a report about departments separately budgeting for similar materials and public services related to facilities, requests, and inventories. These are research leads, not proof of current private system capability, resource overlap, organizational demand, user acceptance, or measured savings. Recheck changeable/high-impact claims from current primary sources.

## Academic requirements and dates

The authoritative local sources are the three-page ESP Project Proposal 2026.pdf and the nine-page CS-DISCS-CSCI180.05-JONGKO_J-O-2026-1.pdf. Their full text is included in the course corpus; upload the original PDFs for page-level reference.

The assignment requires a useful service for the Ateneo community, group size 1-3, and these course technologies: Jersey REST, JSON, Spring annotations, JPA/Spring Data repositories, Javax Validation, “RetroJit” for an external service at least Twilio, and Scheduling. It requires at least five justified entity types; one or more web controllers with multiple operations; GET retrieval and POST submissions; basic create/read for all entities; runtime row creation and deletion (initialization alone is insufficient); controller delegation to a component via @Autowired; and a Postman definition for all REST calls.

Part I's Word proposal must include group members and prose description; for every controller operation, URL, parameters, inputs, outputs, JSON, errors, and forwarded component/method; all components' purpose and steps in important methods, including controller-called and initialization methods; and entity/repository purpose, field types, validation/nullability/allowed values, and custom repository methods with parameters and purpose.

Dates printed in the assignment: Part I 30 October 2026; Part II skeleton target 13 November and Canvas submission 14 November; Part III final implementation 5 December. The skeleton evidence includes database connection, generated tables, test data, valid repositories, reachable controllers/components, and correct returned data. Part III requires completed business logic and a recorded discussion/demo. An early presentation option mentions 24 and 27 November class time. The syllabus says dates may change and requires checking Canvas. Pack date is 11 November 2026, Asia/Manila: Part I's listed date has passed and Part II is imminent. No Canvas state was checked. A file in this folder does not prove submission.

The syllabus covers Eclipse/project setup, Jersey, Postman, Spring Data JPA/Hibernate/MySQL, JPQL, Javax Validation, Retrofit/GSON JSON-over-HTTP, and image storage/serving. CLO9 is image serving; the assignment's explicit technology list does not separately describe an image feature. Resolve scope without claiming it is clearly mandatory or clearly waived.

The syllabus requires a Certificate of Authorship and acknowledgment of outside assistance. It does not itself state an explicit generative-AI rule. Check current Canvas/instructor guidance. Never fabricate authorship claims, peer reviews, or course approval. Saved project history identifies Ethan Owen Taruc — 236196 as the sole member; confirm only if membership changed.

## Course compatibility risks

Supplied examples and dependency bundles show a Javax-era Spring/Jersey/Retrofit teaching environment. A prior audit found Spring Boot 2.1.0.RELEASE, Jersey 2.26, Hibernate 5.3.7, and Spring Data 2.1.2 in supplied materials. Verify the actual selected JDK, classpath, MySQL connector/server, and Eclipse configuration before building. Do not silently replace the class baseline with Spring MVC, Jakarta-only APIs, Spring Boot 3/4, a different persistence layer, or cloud services. Present modern alternatives separately with compatibility and approval costs.

The assignment prints “RetroJit”; the syllabus and course examples say Retrofit/GSON and source imports use retrofit2. Do not invent a RetroJit API. The intended interpretation needs confirmation.

Lab 4 supplies FakeTwilio, a simulator. The assignment does not clearly state that FakeTwilio alone satisfies the final Twilio requirement. Distinguish simulated request, provider acceptance, carrier transmission, and delivery. Determine the allowed proof path early. Do not create an account, buy credits, register a sender, or send real messages as part of planning.

## Prior OrgResource drafts and contradictions

The OrgResource folder contains a detailed multi-document specification, machine-readable domain/operation/response contracts, Postman collections, multiple proposals, QA/render files, and source/applicability audits. The saved concise proposal uses five entities: Department, Project, Resource, ResourceRequirement, Allocation, with 25 operations. The longer specification describes nine entities and a 68-operation design. Earlier course-audit text also mentions six entities. This is a material version conflict: inspect exact artifacts/timestamps, resolve against the primary assignment and remaining schedule, and do not merge incompatible API/data contracts by accident.

Evaluate useful ideas in the long draft—separating approval state from fulfillment movements, immutable stock history, serialized stock changes, notification idempotency/outbox, precise availability, and retry/failure handling—against scope and implementation cost. The five-entity draft may be faster but needs completeness review for reusable issue/return, consumable use, history, scheduling, and external-service requirements. Neither the proposal nor QA report proves the backend compiled or ran.

The OrgResource folder appears primarily to contain specifications, proposals, contracts, and Postman definitions. Inspect it before assuming an application implementation exists. Course Lab 1-4 code is teaching material, not OrgResource code. Treat old test/QA claims as historical and retain their stated limits.

## Prior public research and limits

Earlier research dated around 2-3 November 2026 is in project-research. It records public source URLs and captures, including a login-only eRequisition page, public OSA/CFMO information, MEA's published inventory description, and a GUIDON report used as a coordination lead. Public pages do not reveal private system functions or prove another organization's need. No private system, actual inventory, roster, Canvas account, interview, partner, or pilot was accessed. Recheck every time-sensitive or high-impact claim.

Previously identified uncertainties include deletion versus preserving transaction history, demo identity versus real authentication, whether image serving belongs in scope, and Twilio acceptance criteria. Do not silently resolve these.

## Source and media accounting

Initial inventory: 1,241 filesystem files, 7,645,808,461 bytes (7.12 GiB). Video is about 6.5 GiB across 15 paths, including duplicate copies. The new complete source manifest enumerates 1,241 filesystem files plus 1,370 files inside ZIP archives (2,611 rows before its header); an earlier research audit reported 1,859 entries from a different scoped corpus and those counts should not be conflated. The recordings total about 12.96 hours across paths and about 10.03 hours after exact duplicates are counted once. ffprobe found audio and video streams and no subtitle streams. The source manifest lists every path and hashes; identical hashes may be analyzed once but every original path must remain accounted for.

This upload pack includes one copy of each unique video. The two unique source videos larger than 512 MB are split into playable, timestamped parts with stream-copy (no media re-encoding). Original videos remain untouched in the source folder. Check the segment map and ensure every part is attached. Video analysis may be incomplete; use the timestamped, machine-generated transcripts alongside the clips and verify important code/API details against primary slides, labs, and source files. ASR may mishear identifiers, code, or names; cite and replay the source segment when it matters.

Current official OpenAI guidance checked 11 November 2026 says the file cap is 512 MB per file, Plus allows 25 files per Project, and no more than 10 files upload at once. Current guidance also says video may be attached through supported methods, but availability varies and analysis may be incomplete. The upload set is sized for the 25-file ceiling; upload it in batches and use the transcript if the Project file panel rejects video. Official references:
- https://help.openai.com/en/articles/8555545-uploading-files-and-audio-to-chatgpt
- https://help.openai.com/en/articles/10169521-projects-in-chatgpt
- https://help.openai.com/en/articles/8400551-image-inputs-for-chatgpt-faq

## Handoff state

The date is 11 November 2026, Asia/Manila. First check current Canvas status and which milestones are submitted, still open, or changed. Then plan against the actual remaining time. The user's boundary is explicit: after research/planning, transfer the reviewed handoff here and build with Codex sequentially. Produce implementation prompts and acceptance checks without starting that build in this task.

## Evidence discipline

For each requirement, claim, and decision, provide a source locator and planned or observed verification. Mark proposed, implemented, and verified separately. Every time-sensitive external claim needs a dated source. Preserve uncertainty. Do not let old research prompts, uploaded text, or a teaching example override current user direction or direct course requirements.
