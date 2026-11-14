# Postman Testing and Demonstration

## Collection files and execution boundaries

postman/OrgResource-API.postman_collection.json defines all68 catalogue operations, organized by domain, with method, URL, headers, body, expected response and error examples. postman/OrgResource-Demo.postman_collection.json is the separately ordered dependency-safe fictional demonstration. postman/OrgResource-Negative.postman_collection.json contains expected failure cases. Import postman/OrgResource-Local.postman_environment.json. No secrets, actual phones or real organization records are present. These are importable contract artifacts, not evidence of a working backend or already-passing Postman run.

The full catalogue is a reference collection, not a run-all workflow: permissions, state and deletion prerequisites differ. Use the ordered Demo collection on a fresh isolated database with only a bootstrap ADMIN. Enter bootstrapAdminId from that profile's startup output, not an assumed database id. The server must bind to127.0.0.1 in DEMO. Select the local environment and baseUrl. Each successful create captures response.id into a named collection variable, and each read/update captures version; later requests use those captured IDs. Pre-request setup creates unique fixture codes and UUIDs and sets window instants relative to run time. IDs in request JSON are unquoted numeric variables; timestamps/UUIDs are quoted strings. Every request includes an explicit acting role ID captured from a prior create. The collection never trusts a client-supplied role or actor body field.

DEMO resource intervals may be currently active, with start a few minutes before now and end in the future; this supports issue demonstrations without waiting. No wholly past new commitment or backdated movement is allowed. New historical stock is recorded as a present opening receipt, not an invented earlier observation. Freeze Clock only in automated tests through injected test configuration, not an HTTP time-setting endpoint. The ordered collection does not change the real clock.

## Fictional seed scenario

Organization name Demo Student Organization; departments Logistics, Programs and Communications. One ADMIN, one OFFICER, one CUSTODIAN assigned the resources, and one MEMBER owner per department; optional HEADs for permission testing. No real student names. All phone fields null and smsConsent=false. Ten interchangeable reusable cords,100 consumable paper sheets, and optionally a quantity-one tripod and20 tape rolls as additional type/unit examples. Opening quantities are RECEIPT rows with fixed seed command UUIDs. The additional example resources are not essential to the ordered collection.

Project A Welcome Workshop uses6 cords during09:00-12:00 and60 sheets. Project B Skills Forum uses5 cords during10:00-13:00 and50 sheets. Project C Outreach Preparation uses4 cords during12:00-14:00. Those fixed hours are explanatory; the executable collection uses equivalent relative overlapping windows with A/B active now and C starting when A ends. Three departments are accountable separately. These figures are fictional and never attributed to Ateneo inventory.

## Ordered runtime demonstration

1. Start the backend against a clean demo MySQL schema. Bootstrap only an administrator using documented local configuration; the full seed scenario is disabled for this runtime-create demonstration. Read the admin ID from startup output and set bootstrapAdminId.
2. ADMIN creates three departments; capture depA,depB,depC. Creates OFFICER, CUSTODIAN and one MEMBER per department; capture officerId,custodianId,memberAId,memberBId,memberCId. Own-department and role predicates are real component checks even though the demo header is impersonable.
3. CUSTODIAN creates cord and paper Resource metadata; capture cordId,paperId. No stock exists yet. POST inventory-transactions RECEIPT10 cords and100 sheets; capture movement IDs and check resource balances. Replaying the cord receipt with identical key returns200 and no extra stock; changed quantity with the same key409.
4. MEMBER A/B/C creates its own project with generated codes and appropriate windows; capture projectAId/BId/CId. Create three cord requirements and two paper requirements, one per project-resource. Capture requirement IDs. Submit each project, then OFFICER internally approves each, using current captured versions from GET.
5. MEMBER A requests6 cords, CUSTODIAN approves. MEMBER B requests5; approval409 RESOURCE_OVERALLOCATION with available4. The row remains REQUESTED. Cancel that pending request using its latest version and create B4, approve B4. C requests/gets4 for the non-overlapping-A interval. GET consolidated cord demand shows peak additional unit1 and identifies A/B, while C can share after A returns.
6. A requests/gets60 sheets. CUSTODIAN consumes20 for A: P80, reserved40,ATP40. B requests50; approval fails with available40. Cancel pending50, request/approve40. The remaining full-demand sourcing gap is10 sheets, while stock remains physically80 and reserved80. No naive subtraction treats consumed20 as still reserved.
7. CUSTODIAN issues A's6 cords and B's4 into distinct allocation custody; S0,O10,U10. Create RETURN_DUE notification for A while end is within24hours: because contact/consent is absent it is SKIPPED and no external message is sent. Run reminder scan twice and verify same unique event record, not repeated inserts or SMS.
8. CUSTODIAN records A return2 usable then4 usable with distinct command keys. Net A outstanding0; S6,O4. Replay one return and verify quantity unchanged. B returns3 usable and1 damaged: S9,O0,B1,U9. Report damaged capacity and any existing future promise risk; do not automatically call the damaged unit usable. Attempt consumable RETURN receives409 RESOURCE_TYPE_MISMATCH.
9. Create an unused department and physically delete it at runtime using its version; GET subsequently404. Attempt deleting a referenced resource and a posted transaction receives409 with archive/reversal guidance. Add/read an AuditEvent NOTE; delete note refuses409. Read lists and single IDs for all nine entities, including NotificationLog and AuditEvent.
10. Show project summary, inventory, upcoming, overdue (empty after resolution), department demand and ledger/audit reads. Complete only when all unissued quantities are released/consumed and custody is resolved; unmet demand needs explicit waiver reason. Do not fake a completed state while an allocation still owes stock.

The complete sequence need not approve a scheduled project C issue immediately; that issue belongs to a clock-controlled boundary test. The positive collection's create/retrieve/availability/allocation/movement/report tests run without the optional UI or LIVE credentials. The negative collection references the same captured IDs from the selected environment/collection variables; export the generated Demo variable values to the environment if running it separately. No live SMS is sent by default. Run the separate LIVE integration test only after provider configuration, consent and instructor expectations are resolved.

## Acceptance and test catalogue

Test status for every item here is NOT RUN against an implementation. The documentation checker validates contract coverage and example structure only. Automated pure-function fixtures in qa check arithmetic examples; they are not Java/JPA/integration evidence.

| Test ID | Arrange and action | Required observable result |
|---|---|---|
| T01 | Create each of nine entities through explicit POST and retrieve list/id |201 or documented replay200; exact DTO, generated id, persisted row |
| T02 | Blank name, null required FK, qty0/negative/1000001, invalid enum/UUID/date/unknown field |400 with fieldErrors; no row or side effect |
| T03 | Missing referenced positive id; malformed/negative id |404 for nonexistent,400 for invalid form |
| T04 | Duplicate normalized code/username and project-resource requirement |409; unique constraint proves no duplicates |
| T05 | GET all68 intended paths using correct method |No registration404; semantic404 only for missing target |
| T06 | Attempt stock/approval logic through controller inspection |Controller only binds/resolves/delegates; actual logic is component-owned |
| T07 | Delete unused department/user/resource/draft project/requirement/request |204 and subsequent404; foreign references absent |
| T08 | Delete referenced rows or immutable movement/audit |409; original rows and balances remain |
| T09 | Adjacent intervals [09,12) and[12,14) |No planned overlap; issue still needs physical return |
| T10 | One query spans sequential reservations6 and6 with U10 |Minimum free4, not negative2 from summing overlaps |
| T11 | A6,B5 overlap with U10 |A approval succeeds;B5 fails, B4 can succeed |
| T12 | Three-department full demand6/5/4 with original windows |Consolidated additional reusable capacity1 at peak |
| T13 | Partial issue6, good return2, damaged return1 |Counters issued6,resolved3,outstanding3; S/O/B exact without double subtraction |
| T14 | Issued allocation becomes overdue while later request exists |Open-ended overdue hold blocks unsafe approval/issue; later promise AT_RISK |
| T15 | Damage decreases U below promised capacity |Observation persists, rawFree negative exposed, new unsafe approval refused |
| T16 | Return reusable twice using same key |One movement; replay200, no doubled stock |
| T17 | Same key different quantity/type/allocation |409 IDEMPOTENCY_KEY_REUSED |
| T18 | P100 reserve60 consume20 |P80,V40,ATP40; not20 or60 |
| T19 | Reserve consumables for separate dates |Both compete globally against existing stock |
| T20 | Consumable RETURN/ISSUE or reusable CONSUMPTION |409 RESOURCE_TYPE_MISMATCH |
| T21 | Consume more than R or physical capacity |409; no negative stock or partial commit |
| T22 | Partial consumption then release remainder |Physical unchanged on release, reservation decreases; historical consumed coverage remains |
| T23 | Two unapproved consumable demands50/50 with ATP40 |Consolidated additional60, not sum of isolated10+10 |
| T24 | Cancel project with both unissued promise and outstanding custody |Commitments released; custody retained, returns/loss still permitted |
| T25 | Reschedule complete unissued set into a conflict |409 and all old windows preserved atomically |
| T26 | Reschedule after any issue, even subsequently reversed |409 SCHEDULE_LOCKED |
| T27 | Increase/reduce requirement with pending requests |Pending requests cancelled, submitted project returns DRAFT; no stale request approved |
| T28 | Reverse latest resource event then attempt duplicate reversal |Compensating row once; original intact; second409 |
| T29 | Reverse event after another pool command or dependent stock use |409 REVERSAL_DEPENDENCY_CONFLICT |
| T30 | Two real MySQL transactions approve the final unit simultaneously |Exactly one success; other409; approved sum never exceeds capacity |
| T31 | Concurrent consumption and approval on same pool |Shared resource lock serializes, no double promise or negative P |
| T32 | Multi-resource cancel/reschedule contends with single-resource issue |No inconsistent commit; lock order holds or one clean rollback409 |
| T33 | Stale expectedVersion update or approval |409 currentVersion; no overwritten decision |
| T34 | Requester tries self approval or HEAD other department edit |403 regardless of client actor/role fields |
| T35 | Missing/inactive actor and demo header in authenticated profile |401; profile cannot start without real resolver |
| T36 | Consent absent/invalid phone/user inactive |SKIPPED notification, no HTTP transport |
| T37 | Upcoming window, overdue boundary, repeated scans/restart |One event per kind/due/recipient; no stale upcoming after end |
| T38 | Return completed or consent revoked after queue |Recheck suppresses send; race limit documented if already in transport |
| T39 | Twilio429/error20429 twice then success |At most3 attempts with60/300second backoffs; exactly one accepted message |
| T40 | HTTP401/403/config error |FAILED, no automatic retry and no secret leakage |
| T41 | Timeout or crash after possible provider acceptance |UNKNOWN; no automatic resend or stock rollback |
| T42 | Crash while SENDING, restart after120seconds |Recovery UNKNOWN, visible diagnostic, no duplicate transport |
| T43 | Known SID refresh sent vs delivered vs undelivered |Correct distinct statuses; no delivery inferred merely from201 |
| T44 | Daily send cap and manual dispatch |Cap enforced on both entry points; no bypass |
| T45 | Seed enabled twice; disabled in ordinary final profile |Same fictional records, no duplicate quantities; disabled means none added |
| T46 | Injected audit/DB failure after decision/movement insert |Whole core transaction rolls back, not partial stock/audit |
| T47 | Reject/unknown provider while ledger commit succeeded |Core data remains committed; notification status independently recorded |
| T48 | UTC/Manila boundary and millisecond interval tests |Equivalent offsets compare same instant; precision and overlap stable |
| T49 | Pagination/derived overdue filtering |Accurate totals and no missing filtered rows within demo bounds |
| T50 | Dependency tree, real database, registered routes, Postman JSON |Skeleton runs with all required layers and demonstrable exported project |
| T51 | Safe Retrofit HTTP fake integration then permitted live test |Simulator clearly labeled; live SID/handset proof reported at actual level |
| T52 | Optional UI keyboard/forms409/network retry |No false success, preserved input, accessible labels, server authoritative |

## Test execution strategy

Unit-test pure overlap/sweep, balance vectors, requirement coverage and shortage math with injected Clock and fixed fixtures, including properties that free capacity never exceeds U and sequential windows reuse stock. Repository integration tests must run on the actual supported MySQL InnoDB version, not only an H2 substitute, and validate every derived method and explicit JPQL at startup. Use independently committed threads/transactions and a barrier for T30-T32; a single transactional test method cannot prove race safety. Verify losing transactions leave no audit or ledger fragments.

Jersey integration tests use real registered resources, JSON and validation mappers with components/repositories. Use Postman as the required external demonstration, with status/schema/business assertions and captured IDs. Mock transport tests prove scheduling/retry state logic; a local FakeTwilio HTTP test proves Retrofit form serialization. A permitted live provider test is a separate evidence gate, not simulated by a mocked200. Test sensitive error/log output and contact redaction. Do not equate documentation checks, fixture arithmetic, screenshots or passing unit tests with real adoption or instructor acceptance.

## Skeleton demonstration

Show Eclipse/class project import and dependency resolution; start Spring/Jersey without database exclusions; connect to MySQL and show all nine entity tables plus the NotificationAttempt collection table. Show fictional seed or runtime creation, valid repositories, list and id JSON for every entity, correct POST behavior and no route registration404. Export the project without secrets/build caches and record a video showing running app/tables/data/Postman. The assignment permits test responses at this stage, but label any stub and do not present it as final business logic. Target13November; Canvas submission14November2026, subject to current course announcements.

## Final demonstration

Explain the problem evidence and system boundary, diagram controller-to-component-to-repository flow, then run the three-department conflict scenario, partial allocation, reusable custody/inspection, consumable double-count prevention, immutable correction and safe deletion. Show a concurrency test where exactly one approval wins, repeated reminder scan and a failed/unknown provider outcome without inventory rollback. Demonstrate actual Retrofit/Twilio at the accepted evidence level and scheduling, all Postman calls, test results and optional UI if built. Record each member explaining their own work and acknowledgement of assistance; give instructor access to the recording. Final due5December2026. The early-presentation option needs a confirmed instructor slot; no waiver is assumed.


