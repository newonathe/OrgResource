# CSCI 180.05 — Authoritative written academic requirements and compliance specification

> **FINAL TWO-EDITION OVERRIDE (CP44–CP46, 2026-11-16):** This academic canonical document originated in already-uploaded **draft PR #9**, and is consolidated here without merging that PR. Read [Final consolidated handoff](../research-2.0/77_CP46_FINAL_GITHUB_HANDOFF_RELEASE_INDEX.md), [A-first / B-extended decision](../research-2.0/74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md), and the [single active Codex master prompt](../research-2.0/75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt). **A** is a local strict course version; **B** is separate enhanced C descended from frozen A. Any prior wording implying one final release, modernizing A before course acceptance, or production testing before A completion is **historical/superseded**. A image testing is safe local single-organization; B must enforce authorization/tenant isolation. No application tests or course submission were performed by this document copy.


**Version:** A-CANON-1.0, 2026-11-16. **Status:** source-verified written academic requirements; A1/A2 actual acceptance and later C parity NOT RUN. Later CP44–CP46 two-edition decision controls older one-product wording.  
**Primary sources:** 09_ESP_PROJECT_ASSIGNMENT.pdf (3 pages, referred to as A1–A3) and 08_CSCI180_SYLLABUS.pdf (9 pages, S2–S6). Original 10_ORGRESOURCE_CONCISE_PROPOSAL.docx (12 pages, P1–P12) is project-design history, not a new course rule.

**Labels:** CR = explicitly written project requirement; SF = syllabus fact/outcome/policy; PD = original OrgResource proposal commitment; UD = user-directed conservative addition or final-delivery choice; EV = verified existing O14 source; PL = proposed/not yet implemented; ST = student-only action. These classifications are NOT interchangeable.

## A. Academic compliance matrix

| ID | Authority | Exact required capability or deliverable | Acceptance proof in Course Final A (and independently repeated on Extended C where applicable) | Baseline O14 / remaining gap |
|---|---|---|---|---|
| CR-01 | A1 | Build a coherent service useful to the Ateneo community; project group of 1–3 | Working OrgResource operations with truthful fictional use case; actual group roster only | EV service; no campus validation/adoption claim |
| CR-02 | A1, S2 CLO3 | Jersey REST web controllers | Executed @Path Jersey GET and POST on the deployed final server; real HTTP JSON | EV five Jersey Controller classes; must rerun on final runtime |
| CR-03 | A1, S2 CLO8 | JSON request/response | Strict successful and erroneous JSON shapes; importable documented requests | EV DTO layer; final security and v2 changes NOT RUN |
| CR-04 | A1 | Spring-based annotations and @Autowired Controller→Component delegation, controller only handles input/output | Inspect every Controller→Component mapping; make real request and assert Component side effect | EV five Spring-managed controllers; preserve |
| CR-05 | A1, S2 CLO5 | JPA / Spring Data repositories over Hibernate and MySQL | Five JPA core entities mapped to actual tables, real FK/query/CRUD and rollback on MySQL, not H2-only | EV five mapped domain entities; final supported-runtime proof OPEN |
| CR-06 | S2 CLO6 | Java Persistence Query Language | Custom JPQL positive/edge/empty tests, DB-observed query results | EV allocation aggregate JPQL; earlier intermittent CI failure unresolved |
| CR-07 | A1, S2 CLO7 | Literal Javax Validation API | Actual javax.validation annotation classes, provider and negative DTO/entity tests; record explicit vs automatic JPA lifecycle enforcement | EV javax validator in O14; one supported final runtime compatibility UNPROVEN |
| CR-08 | A1 (“RetroJit”), S2 CLO8 | External HTTP call through Retrofit and Gson, with at least Twilio-specific integration | Actual Retrofit/Gson outbound request to local FakeTwilio plus a real official Messages REST protocol adapter tested against isolated protocol server; official account test only if separately authorized | EV FakeTwilio; real Twilio protocol adapter PL; provider acceptance NOT RUN |
| CR-09 | A1, S2 CLO10 | Scheduling | Genuine invoked @Scheduled methods, disabled default, expiry and due reminder tests, crash/race/one-time semantics | EV expiry and reminders in O14; rerun final |
| CR-10 | A1 | At least five justified entity types; database schema, relationships and data | Department, Project, Resource, ResourceRequirement, Allocation are genuine separately justified JPA domain types; justify every added infrastructure table | EV exactly five O14; additional types allowed, not course-mandated |
| CR-11 | A1 | Basic create/read for *all* entity types | HTTP + MySQL proof for each original core type, not @PostConstruct-only | EV 5 entity families/CRUD; rerun after authorization/tenant protection |
| CR-12 | A1, user CP43 interpretation | Runtime create/delete rows, not only initializer | For EACH of five original domain types, demonstrate eligible POST create, GET read, valid physical delete and GET absent; controlled 409 on referenced/protected rows | EV original deletes; final ledger Resource deletion must protect history while allowing never-used zero-stock delete |
| CR-13 | A1 | GET for retrieval, POST for submits | Original 25 GET/POST plus A09 remain real executable Jersey operations; new final API documented separately | EV 26 unique handlers; no method replacement |
| CR-14 | A1, S2 CLO4 | Postman definition for all REST calls | Importable collection + environment; run dynamic-ID positive and negative Newman requests for every implemented academic/v2/image/account route | EV five prior collections locally reported 96 requests /142 assertions; final all-routes run OPEN |
| CR-15 | A1/A2 Part I | Word proposal includes actual group member(s), prose application description | Student-reviewed Word file with accurate group roster and scope | PD historical 12-page proposal; final unified product proposal NOT YET PREPARED |
| CR-16 | A1/A2 Part I | For *each* controller operation: URL, parameters, input, JSON output, error messages, forwarded Component method | Final Word operations matrix covering original 26 + every new REST operation; no omitted image/auth endpoints | PD 25; EV 26; final expanded routes PL |
| CR-17 | A2 Part I | Components and important method step sequences, including invoked @PostConstruct if any | Corresponding current as-built Component descriptions, read/write/lock/external work, error and state effects | PD descriptions historical; final product must update |
| CR-18 | A2 Part I | Entity/repository descriptions: purpose, all fields/types/nullability/validation and every custom repository method/parameter/purpose | Full field dictionary, FKs, uniqueness, JPA validation, real query definitions; changes tracked from 5-entity baseline | PD 5 types; EV schema differs in known amendments; final must match |
| CR-19 | A2 Part II | Executable skeleton with DB connection, generated tables, optional seed records, working repositories and controller/component reachability | Preserve O14 earlier skeleton evidence and a clean final build; actual current app runs beyond skeleton | EV evidence, not proof previous Canvas submission occurred |
| CR-20 | A2 Part II | Project skeleton source export + runnable video evidence to Canvas; peer reviews of other group members | Only actual past student submission/receipt and peer acts establish compliance | ST / NOT VERIFIED; do not reconstruct fictional receipts |
| CR-21 | A2/A3 Part III | Complete actual application logic, final source/project by **Dec 5, 2026** printed date | Actual completed application and final test/export/hashes; student submits to correct Canvas module | EV O14 partial baseline; final production expansion still PL; no extension inferred |
| CR-22 | A2/A3 Part III | Recorded discussion of the real project, accessible to instructor; or applicable authorized early presentation | Genuine recording and accessible link from student, actual roster representation; no generated claim that recording exists | ST NOT VERIFIED; early option tied to Nov24/27 class time |
| CR-23 | A1/A2/A3 | Peer reviews for all **other** group members, numeric 1–100 and comments | Student actually sends reviews where other members exist; never invent peers | PD lists one student, but actual group roster must be checked by student |
| CR-24 | A3 | Bonus UI capable of interacting with backend and demonstrated | Real React controls reaching server, not screenshot-only mock | EV eight connected O14 pages; final secured UI PL |
| SF-01 | S2 CLO1/CLO2 | Eclipse setup, Java project/library familiarity | Source compatible with Eclipse import and real student debugging demonstration where applicable | O14 source export exists; independent Eclipse GUI import NOT RUN |
| SF-02 | S2 CLO9/S3 image unit | Image receive and serve over HTTP | **Conservative inclusion:** authenticated, tenant-scoped Resource image upload/store/get, bounded size/type, byte-for-byte download, negative tests | PL; O14 no actual image endpoint |
| SF-03 | S4 rubric | Coherent, compiling, complete error-free operation, bonus additional functionality | Exact final source SHA builds and passes real HTTP/DB/UI course acceptance, records defects without inflating grade | No instructor grade claimed |
| SF-04 | S5/S6 | Check actual Canvas for administrative notices; submit deliverables through module; no corruption/malware | Student performs actual upload/access check and retains receipt; planning user chose written syllabus+assignment as feature scope | ST; not automatically done |
| SF-05 | S6 | Certificate of Authorship and disclosure of outside assistance | Student signs truthful declaration and specifies relevant Web/AI/tool assistance; no fabricated authorship | ST unfulfilled in this documentation work |
| UD-01 | Latest USER DECISION / CP44–CP46 | TWO independently runnable editions from ONE Git lineage: Course A first; Extended C descends from frozen A, without rendering course code inert | A independently passes its real original + image/Twilio course tests; C later repeats actual course parity on its own executable and proves supported security | A acceptance pending; C implementation NOT RUN |
| UD-02 | User decision / CP42 | Authenticated private multi-organization operation | Server-side org scoping for every repository/API, roles, sessions, CSRF, last owner protection, cross-org negative tests | PL; NOT a written course requirement |
| UD-03 | User decision / CP43 | Full runnable Twilio Messages REST adapter (disabled by default) + FakeTwilio | Protocol server test validates form, Basic auth, parsing, timeout/unknown, never real SMS without consent/authorization | PL |
| UD-04 | User decision / CP43 | Real CLO9 image feature and protected history safety | Upload bytes read back accurately; safe delete of never-used Resource, conflict on historically used | PL |
| UD-05 | User decision | Cost $0 mandatory build/host operations, protected private data, repeatable backups/restore | Verified provider eligibility/quotas, no incurred charge, TLS, off-host encrypted restore and fail-closed limits | NOT RUN; provider forever-free claim prohibited |
| UD-06 | Latest USER DECISION / CP44–CP46 | Course A must be completely accepted and protected BEFORE Extended C development. Extended C production acceptance is separately required before claiming C ready; student submits actually completed and permissible deliverables before deadline. | A1/A2 actual acceptance → A3 immutable artifact → C extension from exact A SHA → C independent production/course acceptance → real student Canvas action | Neither release is complete/frozen; no actual Canvas submission |

## B. Actual entity justification (not optional arbitrary tables)

- **Department:** responsible functional team organizing projects. Without it, a Project has no owning team for the original five-entity concept.
- **Project:** scheduled activity that consumes or borrows resources during a controlled time window.
- **Resource:** pool of interchangeable reusable equipment or remaining consumable stock.
- **ResourceRequirement:** unique Project–Resource requested quantity and shortage; not identical to a reservation.
- **Allocation:** individual request, approval/custody/consumption state, historical fulfillment and consent-reminder details.

Any added User, Organization, Membership, Invitation, Session, Image, Audit or StockMovement is a **justified production extension** only when its own integrity/security/workflow tests exist. Do not claim tables count as distinct course CRUD entities without the corresponding user-facing requirement and proof. Session infrastructure tables need not receive public CRUD APIs merely because the schema contains them; the course's “all entities” issue must be explicitly explained against five core course entities.

## C. Explicit requirements not found in the written assignment

These are neither waivers nor new alleged faculty instructions: Spring Security, OAuth/SSO, React framework, cloud deployment, uptime SLA, enterprise multi-tenancy, inventory-ledger history, 18 physical tables, partial returns, QR codes, procurement or email. They may be USER DECISION / DESIGN RECOMMENDATION, but must not be mislabeled COURSE REQUIREMENT. Conversely, do not use these extra features as a substitute for a required course technology.

## D. Conflict rulings and acceptance boundaries

1. **Exact five vs at least five:** assignment A1 says AT LEAST five. Old research-root AGENTS.md saying “exactly five forever” is obsolete; preserve five original types and trace extra justified entities separately.
2. **“RetroJit” word vs Retrofit:** use the syllabus p2 CLO8 and actual course example to interpret as Retrofit/Gson; record wording, do not silently correct source.
3. **O14 FakeTwilio vs actual Twilio:** local fake proves HTTP integration but not official Twilio API behavior. Final includes a real official API-shaped transport, disabled by default. Account-based test remains conditional and cannot be relabeled carrier delivery.
4. **CLO9:** image receiving/serving is a syllabus fact; CP43 elects conservative inclusion in final application. This is not retroactive proof that O14 had images.
5. **Source date:** printed Dec 5 is the planning deadline. Syllabus says dates may change and Canvas is an administrative source; user fixed supplied docs as feature scope. Never assert an unseen update or extension.
6. **Original 25 vs O14 26:** A09 is a transparent privacy amendment, not a fabricated sixth original proposal operation.
7. **Proposal stock holding:** Original P8 says APPROVED until cancel/return/consume; O14 code immediately excludes APPROVED for Project.endAt <= now even before expiry cleanup. Final reviewed contract uses O14 effective holds, with exact-dated tests, and records this as a reviewed amendment rather than attributing it to P8.
8. **Privacy:** Original P5/P6 includes phoneNumber in sample Allocation JSON; O14 public DTO intentionally omits the raw phone and adds contactPresent/smsConsent and timestamps/outcomes. Preserve this protective amendment.
9. **R04 historical stock:** O14 deletes zero-total unreferenced Resource. Future ledger-safe final product must refuse erasure of Resource with stock history but still demonstrate a real eligible deletion of a new never-used zero-stock Resource.
10. **Unsupported legacy runtime:** O14 Java 8 / Boot 2.1 is course-reference evidence only; not public secure hosting. The single supported production compatibility path remains NOT RUN until real Javax/Jakarta/Jersey/JPA proof.
11. **No actual school adoption:** no interviews, actual org inventory, permissions, institutional identity, partnership or pilot was verified. No fake claims in proposal, demo or README.

## E. Academic approval criteria

Course A academic technical acceptance is permitted only if (repeat all actual course tests independently on Extended C before claiming C also course compliant):
- Every CR-02 through CR-18 applicable implemented operation has a real reproducible test/evidence entry.
- All five original domain types have justified real HTTP create/read and a valid safe runtime delete demonstration.
- Javax validation is actually invoked and enforced; no “Jakarta is close enough” assertion.
- Twilio compatibility tests distinguish simulator acceptance, official provider test and actual handset delivery.
- Real HTTP image receive/serve is proven byte-for-byte with MIME/size/security tests.
- An importable Postman definition includes *every* actual REST call, including new operations.
- The Word proposal accurately describes as-built operations, Components, entities and repositories, with genuine member roster.
- The student-only recording/COA/peer/Canvas actions are separately marked NOT VERIFIED until real evidence exists.
- No A-FINAL PASS depends on production design notes or CI-only source compilation as substitute for runtime proof.

**Evidence vocabulary:** PASS (observed actual test); SOURCE VERIFIED (read code); REPORTED PASS (prior report, not rerun); FAIL (observed); BLOCKED (external dependency); NOT RUN; UNRESOLVED (ambiguous source); STUDENT ACTION. Do not inflate any label.
