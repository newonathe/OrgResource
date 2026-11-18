# OrgResource C4 acceptance and course audit

**Review date** — November 18, 2026 (Asia/Manila)
**Implementation parent** — `95439de8492ef1a8192afc8b4ec45b981c99b032`, [C3 draft PR #16](https://github.com/newonathe/OrgResource/pull/16)
**Course Final A** — `7f8c0746da18fd54b47f430ac1435ac36e605d67`, [A3 draft PR #12](https://github.com/newonathe/OrgResource/pull/12)
**Tested C4 implementation** — `664f2d83db0de568ac54f7c5245503ac83a81e1a`. The [implementation commit](https://github.com/newonathe/OrgResource/commit/664f2d83db0de568ac54f7c5245503ac83a81e1a) records all 52 changed source, test, workflow and instruction paths.
**C4 branch** — `feat/extended-c-c4-connected-react-2026-11-18`. The final source and CI receipt belongs in the draft PR, which GitHub updates against its head.

## Source authority and review coverage

1. The [assignment](../../ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf), [syllabus](../../ChatGPT-Project-Pack/UPLOAD/08_CSCI180_SYLLABUS.pdf), [original proposal](../../ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx), [course reference corpus](../../ChatGPT-Project-Pack/UPLOAD/03_COURSE_REFERENCE_CORPUS.txt) and [12-video transcript pack](../../ChatGPT-Project-Pack/UPLOAD/06_VIDEO_TRANSCRIPTS.md) establish course rules, teaching patterns, scope and historical design. The proposal text was inspected locally and personal fields are omitted from this report.
2. [Root instructions](../../AGENTS.md) govern the implementation tree. The instructions inside `docs/complete-handoff/` describe the original Course A plan. The latest explicit scope authorizes C4 work and a wider audit. It retains the stop before C5 implementation.
3. [Academic specifications and research at 4a137b8](https://github.com/newonathe/OrgResource/tree/4a137b8acb4d1dda486bf03a4151835613240fd5/docs) inform the approved phases. CP34 supplies schema/invariant candidates. CP35 supplies authorization and UI candidates. CP36 supplies compatibility and cutover gates. CP41 ties requirements to SQL proofs. CP44/45 establish the two editions. CP49 is the later audit checklist on the research branch. Candidate routes and paper infrastructure decisions need separate approval and runtime proof.
4. The automated inventory opened 655 tracked or unignored working files during the audit, including archives, with 283,067 detected text lines. It also read 103 docs/research blobs at the verified specification SHA, and 91 research blobs at the research branch head. Those totals include overlapping documents and generated evidence. **Coverage limit** — this is a byte and line inventory plus targeted semantic review of governing contracts, relevant source paths, and executable gates. A manual line-by-line review of every historical file, duplicate, transcript segment and binary was not completed. Original videos were not reprocessed.

## How the system fits together

```text
Course A: Java 8 JAR → packaged Course A React → Jersey /api → @Autowired components
         → Spring Data JPA / JPQL / Hibernate → Course A MySQL
         → @Scheduled expiry and Retrofit/Gson reminder integration

Extended: Java 21 JAR → packaged Extended React → same-origin /api and /api/v2
         → Spring Security + Spring Session JDBC → Jersey → Spring components
         → current membership/role checks → tenant predicates and transactions
         → MySQL and Flyway V1–V5
```

- A Department owns Projects. A Project and a Resource form a unique Requirement. A Requirement has Allocations. An allocation moves as one whole batch. Pending allocations do not reserve stock. Approval reserves stock. Issue transfers the hold into custody. Usable return releases custody. Consumption reduces the physical total once. Expired approvals cease holding stock and issued custody remains outstanding. Availability and shortage come from saved facts.
- Compatibility requests use numeric IDs and the selected organization. The versioned inventory API takes organization and record UUIDs, checks current membership in the URL organization, and uses expected-version and operation identities for mutations. A person with memberships in both organizations may access both explicitly scoped APIs. Switching clears old rendered state and cancels pending requests. A person without membership in the target gets a controlled denial.
- V1 establishes identity and tenant-scoped domain relationships. V2 persists HTTP sessions. V3 adds append-only stock movements and expiring HTTP receipts. V4 adds encrypted contact fields, images and reminder state. V5 adds the persisted dispatch boundary. A durable movement remains after a short-lived HTTP receipt is pruned.
- Reminder runs claim and validate consent under separate short transactions. The authorization transaction commits before loopback HTTP starts. A revocation before that boundary suppresses the send. A request authorized at that boundary may already be in flight. A temporary key issue may be retried before sending. Invalid contacts and uncertain transport outcomes do not trigger automatic sends.

## Course conventions traced to the implementation

| Course rule or lesson | Where it is applied | Evidence and limits |
|---|---|---|
| Java and Eclipse project structure, dependency imports and debugging (CLO1–2, Java/Eclipse slides, V12) | Frozen Java 8 source ZIP includes Eclipse project metadata and Maven Wrapper. Extended sources live in their own workspace. | ZIP CRC and source provenance verified. A separate Eclipse workstation and student debugging demonstration were not run in this audit. |
| Spring Boot, registered Jersey resources, GET for reads, POST for submissions, JSON and controller-to-component delegation (assignment p. 1, CLO3, V01–02 and V04/V09) | `backend/…/rest` and `extended/backend/…/rest` plus corresponding Spring components. Business checks live in components and services. Image GET returns bytes. | Course A uses Jersey 2 with `javax.ws.rs`. Extended uses the separately approved C0 Jersey 4 with `jakarta.ws.rs`. The framework and handler pattern are retained. The namespaces are different. |
| Web container (syllabus p. 1) | Course A JAR contains Tomcat 9.0.12. Extended JAR contains Tomcat 11.0.27. | **UNRESOLVED** — the syllabus mentions Jetty. The assignment's enumerated checklist specifies Jersey and does not list Jetty. A Jetty-specific requirement needs written clarification before either edition is called an exact stack match. |
| Postman definitions and runtime create/read/delete (assignment p. 1, CLO4) | 25 proposal routes, A09 and two image routes test all five entity families. Refusals preserve referenced and historically committed rows. | Authenticated Newman covers 28 academic operations. Modern v2 routes have HTTP/MySQL and selected browser tests. The collection is not an exhaustive Postman definition for modern v2. |
| Hibernate/MySQL persistence, generated identifiers, cardinality, repositories and JPQL (CLO5–6, V05–07) | JPA entity relationships, derived methods, JPQL query repositories and MySQL FKs. Extended adds guarded Flyway SQL and ledger transactions. | Real MySQL and concurrency checks. JPA updates use persistent records. Optional fixture initialization does not replace runtime CRUD. |
| Javax validation (assignment p. 1, CLO7, V03) | Course A uses Javax Bean Validation. Extended runs literal Javax validation using Apache BVal, alongside framework Jakarta validation. | C0 and Extended tests distinguish explicit validation, ORM validation and SQL constraint enforcement. C4 adds no replacement validator. |
| Retrofit/Gson JSON over HTTP, Twilio and scheduling (assignment p. 1, CLO8, V08 and Lab 4) | `@Scheduled` lifecycle processing and separate Retrofit/Gson simulator and provider-shaped clients. | Local simulator and protocol checks pass. The course's Twilio Virtual Phone provider behavior and instructor interpretation remain unverified. |
| Image receive, database save and serve (CLO9, syllabus pp. 2–3, V10) | Resource image bytes and type on the existing Resource, two Jersey operations, binary no-store response. Extended UI adds upload and preview. | PNG/JPEG, larger than 70 KiB, exact size boundary, invalid uploads, tenant denial, CSRF, and restart checks. Syllabus p. 4 maps the project to CLO9 even though p. 2 defines the final-project outcome as CLO10. Feature coverage addresses both without resolving the rubric wording. |
| As-built scope, proposal documentation, final demonstration and integrity (CLO10, assignment pp. 1–3, syllabus p. 6) | Frozen A3 proposal DOCX, complete source, Java source, JAR, Postman and demo guide. | Written assignment lists Dec 5 for the final. Current Canvas, roster, presentation arrangement, recording access, Certificate of Authorship, assistance acknowledgment, peer work, and submission receipt remain student actions. |

Course A owns course submission parity. Extended C demonstrates the same academic operation families, with deliberate security additions to request context and errors. The original proposal includes an optional phone field in the Allocation response and looser deletion wording. The reviewed implementation omits the contact, protects previously committed history, adds A09 and two image operations, and expires approved holds at the project end. These are documented amendments. Do not infer byte-identical API compatibility from route coverage.

## Socratic audit and remaining decisions

| Challenge | Finding and action |
|---|---|
| Would default CI build the same app as the local C4 gate? | Before this audit it built the Course A UI for the Extended JAR. The workflow now builds `extended/frontend`, installs headless Chromium, runs the authenticated 28-operation collection, seeds a second fictional tenant on the CI service, and runs Playwright on the packaged app. Remote execution needs a result from the C4 PR. |
| Can an apparently green mobile layout still hide a needed action? | Yes. The 768px screenshot clipped Sign out, and 360px had no organization switch. The tablet toolbar wraps, the mobile More menu supplies the controls, and the browser suite checks containment and performs a real mobile switch. Dialog surfaces are solid for readability while the workspace retains its glass treatment. |
| Does a server session failure reach the relevant UI? | The 401 handler could abort the response it was processing. Cleanup now happens after decoding. A missing organization 409 returns to the picker. A sign-out request whose response is lost preserves an explicit unconfirmed state. Invite and recovery requests refresh their CSRF value before submitting. |
| Can a role change expose old receipts or borrower records? | C4-A tests recheck current stock role after demotion and pruned receipt expiry, with a separate authorized custodian, suspended/foreign/anonymous denials and JVM restart. MEMBER and unassigned LEAD allocation activity fails closed. Own-record and assigned-project views need authoritative identity links. |
| Does a failed write appear as saved or get repeated? | No automatic retry follows an uncertain mutation. The browser checks saved state for one lost CRUD confirmation and recovers the same operation's receipt after a lost stock response. Permanent evidence covers stock movements. Non-physical approval/cancel have only short-lived HTTP receipts if no movement was appended. |
| Is a single green pipeline enough to approve public release? | No. The npm dev/build advisory remains open. Operating a public service, restoring an encrypted backup off-host, rotating credentials/contact keys, retrying restore and migration procedures, and real provider confirmation are unverified. |
| Are all proposed future features required now? | No. CP35 and CP49 include product candidates beyond C4. Keep the exact existing route and role policy until borrower ownership, project assignments, export rules and data ownership are approved. Avoid adding parallel implementations for these gaps. |

## C4-A evidence and supported roles

| Gate | Implemented proof |
|---|---|
| A1 — image-protected deletion | `C4_A1` proves an empty never-moved Resource with an image returns `RESOURCE_IMAGE_EXISTS`, retains its data, and an eligible unpictured row deletes. |
| A2 — receipt authorization | `C4_A2` plus the packaged verifier prove current-role checks for actor and separately authorized custodian. The runner checks demotion, expiry, prune and restart without ledger loss. |
| A3 — consent boundary | `C4_A3` coordinates the prepare/dispatch boundary and verifies revocation before authorization, no locks spanning transport and distinct outcomes. |
| A4 — loopback transport | `C4_A4` verifies redirects are rejected for both loopback transports and simulator failure is recorded as rejected. |
| A5 — tenant queries | `C4_A5` and `C3_SECURITY02` cover borrower activity restrictions and scoped requests. Resource-balance and pending-quantity queries carry explicit organization predicates. |
| A6 — images | `C3_IMAGE01` expanded with valid JPEG, max-size and failure cases. Packaged restart checks exact bytes. |
| Session and concurrency | `C3_SESSION01` verifies 12-hour absolute expiry, CSRF rotation and no reauthentication on workspace change. `C2_CONCURRENCY01` and `C2_IDEMP02` use MySQL to check no double commitment and rollback. |

OWNER and ADMIN manage Department, Project, Requirement and permitted governance actions. OWNER and CUSTODIAN manage inventory, allocation actions and stock receipts. ADMIN can read allocation activity. MEMBER and unassigned LEAD can use the permitted basic workspace reads and cannot browse borrower allocations. Backend gates enforce the roles independently of the UI. Governance actions still obey restrictions on grant roles, suspensions and ownership transfers.

## Operation and screen coverage

The following course operation table names the requests in the [authenticated Newman collection](../../postman/OrgResource-Extended-C3-28-Operations.postman_collection.json). It applies to the freshly exercised C4 backend. The UI uses modern operations for selected stock transitions while the original operations remain executable.
| ID | Jersey operation | Newman request / outcome |
|---|---|---|
| D01 | `POST /api/departments` | `D01 POST Department` — create |
| D02 | `GET /api/departments` | `D02 GET Departments` — list |
| D03 | `POST /api/departments/{id}/update` | `D03 POST Department update` — update |
| D04 | `POST /api/departments/{id}/delete` | `D04 POST eligible Department delete` — delete |
| P01 | `POST /api/projects` | `P01 POST Project` — create |
| P02 | `GET /api/projects?departmentId=…` | `P02 GET Projects` — scoped/filterable list |
| P03 | `POST /api/projects/{id}/update` | `P03 POST Project update` — update |
| P04 | `POST /api/projects/{id}/delete` | `P04 POST eligible Project delete` — delete |
| R01 | `POST /api/resources` | `R01 POST reusable Resource` — create |
| R02 | `GET /api/resources?type=…` | `R02 GET Resources by type` — filtered list |
| R03 | `POST /api/resources/{id}/update` | `R03 POST Resource update`; `R03-STALE` asserts stale-version refusal |
| R04 | `POST /api/resources/{id}/delete` | `R04-REFUSED` asserts nonempty refusal; `R04` deletes eligible empty resource |
| R05 | `GET /api/resources/{id}/availability` | `R05 GET Resource availability` — server-derived inventory |
| Q01 | `POST /api/requirements` | `Q01 POST main Requirement` — create |
| Q02 | `GET /api/requirements?projectId=…` | `Q02 GET Requirements by project` — filtered list |
| Q03 | `POST /api/requirements/{id}/update` | `Q03 POST Requirement update` — update |
| Q04 | `POST /api/requirements/{id}/delete` | `Q04-REFUSED` asserts reference protection; `Q04` deletes eligible unreferenced requirement |
| A01 | `POST /api/allocations` | `A01 POST reusable Allocation`; consumable and cancellation fixtures also exercise create |
| A02 | `GET /api/allocations?status=…&requirementId=…` | Combined, status-only and requirement-only requests verify each filter |
| A03 | `POST /api/allocations/{id}/approve` | `A03 POST approve reusable Allocation`; consumable approval also exercised |
| A04 | `POST /api/allocations/{id}/issue` | `A04 POST issue reusable Allocation` — reusable stock/custody transition |
| A05 | `POST /api/allocations/{id}/return` | `A05 POST return reusable Allocation` — custody return |
| A06 | `POST /api/allocations/{id}/consume` | `A06 POST consume consumable Allocation` — physical stock transition |
| A07 | `POST /api/allocations/{id}/cancel` | `A07 POST cancel Allocation` — cancellation |
| A08 | `POST /api/allocations/{id}/delete` | `A08-REFUSED` asserts issued-history protection; `A08` deletes eligible cancelled record |
| A09 | `POST /api/allocations/{id}/revoke-sms-consent` | `A09 POST revoke SMS consent` — contact/consent erasure |
| IMAGE-01 | `POST /api/resources/{id}/image` multipart `file` | `IMAGE-01 POST PNG upload` — validated database-backed PNG |
| IMAGE-02 | `GET /api/resources/{id}/image` | `IMAGE-02 GET PNG bytes` — binary response; byte-for-byte rechecked after restart |

| Screen | Connected acceptance |
|---|---|
| Overview | Tenant-scoped snapshots from the five list endpoints, with borrower activity omitted for restricted roles. |
| Departments | Create, list, rename, protected delete and successful eligible delete. Lost confirmation shows saved-state review. |
| Projects | Create, filter, read, rename, and dependency checks. |
| Resource Library | CRUD, server availability, protected deletion, stock receipts/corrections, movement history, and exact image bytes. |
| Requirements | Create, filter, update, and delete with protected reference checks. |
| Allocations | Request, approve, issue, return, consume, cancel, delete, consent withdrawal, and browser filter history. |
| Reminder Activity | Saved upcoming/overdue and reminder state. Backend simulator tests establish the external HTTP boundary. |
| About | Grounded explanation of the five-record workflow, academic scope, and real server behavior. |

**Modern API available** — session/CSRF, account and recovery, invitations, member governance, resource and allocation state, stock receive/adjust, lifecycle transitions, movement history and operation receipt. The browser connects the workflows above. Existing invitation/transfer revocation is backend-only. Governance members and ledger views request bounded pages. The UI does not yet traverse every cursor. Modern CRUD replacements for every numeric route, exporter/audit stream, borrower account linkage and LEAD project assignment remain deferred. The full CP35 candidate API is broader than the implemented surface.

## Reproduction and evidence

| Gate | Command and scope |
|---|---|
| Extended packaged local | `.\scripts\verify-extended-c1.ps1` creates isolated MySQL 8.0.46, builds the Extended UI, runs Java 21 Maven verification and Flyway V1–V5, starts the JAR, runs Newman and Playwright, and verifies persistence after restart. |
| Frontend checks | Fresh npm installation, typecheck, lint, and production build passed. Vitest ran 21 tests and all passed. Coverage includes a missing active organization, preserved 401 response, and an unconfirmed sign-out response. |
| Java/MySQL | 43 tests, zero failures, errors or skips in the verified runs. Surefire evidence at `extended/backend/target/surefire-reports/`. The suites include intentional refusal of incompatible V3/V4 legacy fixtures. |
| Authenticated Newman | 48 requests and 90 assertions, zero failures against the packaged JAR. The 28 distinct course operations have the traceability above. |
| Playwright Chromium | Nine packaged journeys. The two-tenant case switches on desktop and mobile, cancels a stale read and verifies an isolated member receives 404. The visual case covers the eight academic pages in light/dark at 1440/360 and checks tablet toolbar containment, keyboard focus, reduced motion, dialogs and loading/empty/error states. |
| Visual samples | [Desktop dark](screenshots/overview-desktop-dark.png), [768px tablet](screenshots/allocations-tablet-light.png), [360px mobile](screenshots/resources-mobile-light.png). The verifier writes the full local screenshot set. |
| Course A source | Git tree hashes for `backend/` and `frontend/` match frozen A3. Values are `f2b522f88214a9cfba908cac6b0bcda70171875c` and `052e5dbc5a0e84823c7dda06ccee83f27b1818c9`. The Java 8 job continues to build those paths separately. |
| Frozen release integrity | Independently checked 10 payload sizes and SHA-256 hashes against both manifests. CRC checks passed for 254 JAR entries, 571 complete-source entries, 74 Java ZIP entries and 19 DOCX entries. All 504 complete-source files matched the A3 Git blob hashes. No new A3 freeze or separate-machine run occurred. |

## Dependency and release boundary

Fresh `npm audit --json` returned 12 high severity development dependency records and no other severities. They trace to one advisory affecting `picomatch@4.0.7` (GHSA-fc5q-39xc-vjpf). The other records are five `@typescript-eslint/*` packages, `typescript-eslint`, `@vitejs/plugin-react`, `fdir`, `tinyglobby`, `vite` and `vitest`. `npm audit --omit=dev --json` returned zero. The registry returned `4.0.7` as the latest available version in this environment and the advisory lists no patched version. Suggested fixes include major tool downgrades. The lock was retained. [Advisory](https://github.com/advisories/GHSA-fc5q-39xc-vjpf)

Before a C5 release decision, review these dependencies and secure a compatible mitigation or explicit risk acceptance. Build reproducible source/JAR artifacts from an exact commit, run clean-install and supported upgrades from controlled fixture states, document runtime versus migration grants and rollback, and prove encrypted off-host backup restore against separate MySQL. The restore must verify tenant isolation, sessions/credential treatment, image bytes, ledger totals, durable receipts and key availability. Hosting/TLS, budget and authorized provider tests are separate gates. Course A submission and Extended deployment have separate evidence.
