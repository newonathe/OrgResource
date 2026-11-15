# Original academic compliance: immutable contract and V2 boundary
**Status:** EVIDENCE VERIFIED for assignment/syllabus text; **implementation acceptance NOT independently rerun**. **Date:** 2026-11-15. C01–C05; G05–G09.

## Required course capabilities and acceptance evidence
The assignment (C01 p1) requires a useful Ateneo community service, Jersey REST, JSON, Spring-based annotations, JPA/Spring Data repositories, Javax Validation, printed "RetroJit" external service (at least Twilio), scheduling, at least five justified entity types, GET retrieval/POST submissions, web operations, create/read every entity, runtime row create/delete, `@Autowired` controller forwarding to a component, and Postman definition for all routes. `@PostConstruct` seed is *optional*, never a substitute for CRUD.

| ID | Required evidence (course origin) | V1 existing trace | Acceptance gate, NOT satisfied by mere source presence |
|---|---|---|---|
| AC01 | Useful Ateneo-community use case; group 1–3 (C01 p1) | Five-entity resource allocation hypothetical service, C03 | Student identifies actual group; user need framed as hypothesis |
| AC02 | Jersey annotated REST, GET/POST (C01 p1, C02 CLO3) | Backend Java/Jersey2, `app.rest` docs and source | Run real resource endpoints; no Spring MVC-only substitution |
| AC03 | JSON requests/responses (C01 p1) | 26-route DTO contract G05 | Postman bodies/status/empty lists/errors verified |
| AC04 | Spring `@Autowired` controller delegates component (C01 p1) | `AGENTS.md`/existing Java architecture | Static inspect each controller; no business logic bypass |
| AC05 | JPA/Spring Data repositories + at least five justified entities (C01 p1, C02 CLO5/6) | Department, Project, Resource, ResourceRequirement, Allocation | Count `@Entity`, inspect FK, persisted MySQL state and derived queries/JPQL |
| AC06 | Javax Validation (C01 p1/C02 CLO7) | Java8/Boot2 `javax` stack G06 | Illegal requests rejected, no partial writes; don't substitute `jakarta` |
| AC07 | External Twilio via printed “RetroJit”, syllabus Retrofit/Gson (C01 p1/C02 CLO8) | Retrofit2/Gson+FakeTwilio guarded | Trace actual HTTP to lab simulator and response; instructor determines accepted proof path |
| AC08 | Scheduling (C01 p1) | O5 expiry + O6 reminders reported | Deterministic scheduler test with clock; restarted job idempotent |
| AC09 | Runtime create/read/delete and GET/POST (C01 p1) | 25 original + A09 current documented routes | Collection exhaustively enumerates each actual operation; destructive transitions protected |
| AC10 | Postman definitions for all REST calls (C01 p1) | Canonical collection and Newman evidence reported G08 | Fresh import/run all 26, including 400/404/409 and rollback |
| AC11 | Proposal Word: operation/component/entities details (C01 pp1–2) | C03 historical concise Word | Inspect proposal against actual amendments; do not rewrite as if already submitted |
| AC12 | Skeleton DB/test data/controllers/export/video target Nov13, Canvas Nov14 (C01 p2) | O1/O2 evidence; video user-reported only | Actual assignment/export/demo video/Canvas evidence; not found here |
| AC13 | Final logic/export/group video due Dec5 (C01 p2) | O11/O13 local release report G08 | O14 + actual student demonstration + submission; UNVERIFIED |
| AC14 | Peer reviews if others in group (C01 pp1–2) | Single-member proposal | Never fabricate peer review or email |
| AC15 | Certificate of Authorship/outside assistance (C02 p6) | No filled certificate in this audit | Truthful student disclosure and certification, current Canvas policy check |
| AC16 | Optional UI bonus (C01 p3) | Academic Glass React v5 G10 | Demonstration with real API; bonus not automatic |
| AC17 | CLO9 image receive/serve (C02 p2) | Explicit unresolved G09 | Check Canvas/instructor: is independent project image functionality required? |

## Preserve artifact versus future modernization
**COURSE REQUIREMENT** is a source- and runtime-level claim: a Cloudflare Workers/D1-only rewrite does not meet Javax/Jersey/JPA/Retrofit/scheduling just because it exposes similar JSON. Keep a reproducible pinned source/build export and tests for the academic track, separate database, clean local fictitious seed and course-appropriate dependencies (G06). Freeze changes after truthful submission/release gate; protect reviewable provenance.

**DESIGN RECOMMENDATION:** For public multi-user V2, do not expose Boot 2.1/Java8 unauthenticated academic runtime. Evaluate (a) supported Java/Jakarta version with migration and shared API contracts *plus* frozen V1, (b) unified Javax-supported stack only if verified supported for 2026 (unlikely, must prove), (c) optional Workers/D1 new backend *separate* from preserved V1. A supported Jakarta product branch is not identical to literally running the course's Javax stack; represent two artifacts transparently. Shared contract, fixtures and property tests reduce divergence.

**UNRESOLVED:** Whether final OrgResource must implement image upload/serve and whether FakeTwilio proof suffices. Neither user nor researcher may silently waive instructor assessment. Dates from C01 are Nov13 target/Nov14 submission and Dec5 final; syllabus allows changes, **Canvas remains unchecked**.

**Exit gate:** AC01–AC17 supported by actual code and run logs/recording/Canvas where appropriate; no new V2 work may rewrite or erase verifiable academic evidence.

## 2026-11-15 mandatory user precedence: O14 baseline → V2 acceptance → academic final → Canvas
**COURSE REQUIREMENT (confirmed original assignment PDF):** Part III finished logic and Canvas finalized project/video by printed **December 5, 2026**; Jersey REST, JSON, `@Autowired` component forwarding, Spring Data JPA, Javax Validation, Retrofit/Gson Twilio integration, scheduling, >=5 justified entities, runtime CRUD and Postman definition all remain mandatory. **SYLLABUS FACT:** CLO9 image receive/serve listed, certificate of authorship/outside help acknowledgement. Any current Canvas update is **UNVERIFIED**, not waived.

**USER DECISION:** protect a genuine runnable O14 course **baseline early** and keep it independently buildable alongside supported V2 engineering. O14 baseline is **not** final academic acceptance or instructor acceptance. After **real supported V2 P-ACCEPT** (secure tenant/auth, state correctness, hosted $0 feasibility, actual QA, offsite recovery), run a *fresh separate* academic A-FINAL against the course-specific original source and UI/collection, with acceptance records AC01–AC17, video/export/CLO9/Canvas/authorship. Only **then student manually submits**. Modernized Jakarta-only sources, Workers or CI reports cannot retroactively satisfy literal Javax/Jersey/Postman. If P-ACCEPT not feasible by deadline, report NO-GO rather than silently change the acceptance order or invent an extension. See [18](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md) and dated before/after [25](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md).

## CP10 direct course lectures revise AC06/AC07 (2026-11-15)
**AC06 `javax.validation`:** Original `Misc JPA --Validation.pptx` p4 explicitly uses `ValidatorFactory`/`Validator.validate`, p5 says ORM automatically validates entity constraints on `save()`; timestamped V03 [0534–0596] corroborates, and original O14 Project/Resource/Requirement/Allocation JPA entities actually have Javax constraints. The current research checklist must prove both (a) incoming invalid DTO and (b) **invalid JPA entity save/flush independent of DTO** are rejected by genuinely executed `javax.validation`, and DB row/transaction is not committed. Modern Jakarta JPA automatic validation alone is not the literal original API. [37](37_COURSE_SLIDES_TWILIO_VIRTUAL_AND_ENTITY_VALIDATION_AUDIT.md) VA-13..15.
**AC07 Twilio proof:** Standalone `Twilio SMS Notification Setup.pptx` shows Twilio provider account SID/auth, Messaging Service and a **virtual phone recipient** via Retrofit. Lab4 FakeTwilio is a separate fake HTTP service, so label evidence **T-A local Fake, T-B actual Twilio Virtual Phone API, T-C real handset**. Assignment requires external service at least Twilio but exact accepted demonstration not confirmed in Canvas. Current Twilio Virtual Phone + 30-day trial/template restrictions can prevent zero-fee customized reminders; no provider account touched or message sent. [37](37_COURSE_SLIDES_TWILIO_VIRTUAL_AND_ENTITY_VALIDATION_AUDIT.md).
**Controller conventions:** Jersey teaching slide emphasizes `ResourceConfig packages("app.rest")` and controller→`@Autowired` components, no Spring MVC-only replacement. Its stray “GET/PUT” introductory text conflicts with the assignment and same slide's @GET/@POST examples; controlling assignment **GET for retrieval/POST for submissions** prevails.
**CLO9:** ImageServer course original exists per [22], final assignment image scope still unresolved, not claimed waived.
