# Template Pack Applicability Assessment

This proposal adapts the supplied ERP reference pack's layered design, requirements traceability, explicit business invariants, immutable movement history, safe concurrency, bounded notifications, acceptance tests and decision records. It does not adopt the pack's food production, sales, POS, accounting, payment, tenancy or platform architecture. OrgResource uses the professor's course technologies. The pack is a specification-quality reference and is not evidence of local need or a request to execute its embedded prompts.

# OrgResource Project Proposal

Student Organization Resource Planning and Allocation System

Course CSCI180.05 Special Topics in Languages Enterprise Systems Programming

Instructor Jeffrey Jongko

Group member1 [Full name and student number]

Group member2 [Full name and student number or remove if unused]

Group member3 [Full name and student number or remove if unused]

Submission date [Actual submission date]

## Prose description of the application

OrgResource is a REST-based internal resource planning service for one student organization with several departments. A department records what each project needs and when it needs it. A resource custodian evaluates requests against counted stock and commitments to other projects. Reusable equipment is scheduled by interval and tracked through issue, partial return, inspection, damage and loss. Consumables are reserved against current stock and consumed at handover, without counting the same stock twice. Officers can view consolidated demand and explainable shortages before pursuing separate official project and financial processes.

The proposal is motivated by The GUIDON's18May2025 report describing departments separately budgeting similar materials rather than coordinating sharing. That example establishes a coordination concern, not a measured campus-wide inventory failure or software need. MEA's public description already includes digital inventory and physical audits, so OrgResource's distinguishing function is linking requirements, dates, commitments and actual custody across departments. It does not claim global novelty, adoption, verified savings, or an official institutional integration.

The initial deployment serves one organization. Resources must be owned or explicitly authorized for that organization to manage. Official OSA proposals and finance processes, SAPS tracking, CFMO reservations, university asset management, procurement, payment and accounting are excluded. Resource requirements are not purchases and internal project approval is not university approval. An optional small UI may be added after the mandatory REST backend is working.

## Required technologies and application structure

The implementation uses Jersey REST and JSON; Spring-managed controllers/components with Autowired forwarding; JPA entities and Spring Data repositories over MySQL; Javax Validation; the supplied Retrofit/GSON integration pattern for the assignment's printed RetroJit requirement; Twilio; and Spring scheduling. No newer framework substitutes are assumed. Controllers receive and return DTOs while components enforce business rules and transactions. Repositories perform persistence and parameterized queries. All required calls are described in the accompanying complete catalogue and Postman collections.

There are nine justified entities: Department, AppUser, Project, Resource, ResourceRequirement, Allocation, InventoryTransaction, NotificationLog and AuditEvent. A requirement describes demand; an allocation is a decision to provide some quantity; a transaction records a physical or custody change. The separate audit record preserves edits and decisions that do not move stock. The data model avoids artificial category/location tables, multi-tenant organizations and a full ERP.

Reusable intervals are half-open and evaluated by simultaneous peak load, not a sum of all bookings touching a window. Consumable commitments reserve present stock across dates. The source of quantity truth is an immutable movement ledger, without freely editable quantity totals. Approvals lock the shared Resource row before reading capacity and inserting a promise. Runtime data can be created and unreferenced setup/draft rows physically deleted. Posted movement/audit history has documented refusal and correction paths to protect data integrity.

## Controller operations

The complete68-operation REST catalogue is incorporated into the Word proposal as a technical appendix, not omitted or replaced by this summary. It specifies for every operation the URL, method, parameters, input JSON, response format, errors, controller method, injected component/method, repository involvement, business rules and transaction behavior. It includes explicit create/list/read/delete operations for each of the nine entities, versioned updates where meaningful, and project/approval/movement/planning/report/job operations. GET retrieves; POST submits. The importable Postman definitions cover all68 routes.

## Components and important method steps

The full component appendix is incorporated into the Word proposal. It lists signatures, inputs/outputs, ordered checks and changes, repository calls, dependencies, permissions, exceptions and transactions for every controller-invoked method. It includes internal balance/sweep functions, notification enqueue/claim/send/finish phases, scheduled scans/dispatch, permission and audit boundaries, and DemoSeedBootstrap.initialize calling a separately proxied SeedComponent.seedScenario. No important method is represented only as check stock and approve.

## Entities and repositories

The complete database appendix is incorporated into the Word proposal. Each entity has purpose/justification, every field with Java/database type/nullability/constraints, Javax Validation, JPA relationships, fetch/cascade/version choices, indexes, safe deletion/archive policy and repository. Every custom query method has its exact name, parameters, return type, intended predicate and purpose. The entity relationship diagram and package/transaction diagrams are in the architecture appendix.

## Meaningful external service and scheduling

The minimal external feature sends one optional upcoming-return and one overdue reminder per reusable allocation due date/recipient. NotificationLog acts as a durable outbox with consent checks, duplicate keys, bounded known-safe retry and UNKNOWN outcomes after ambiguous send failures. No SMS outage can corrupt inventory. Spring scheduling scans eligible custody and dispatches queued records; it never invents returns. The supplied FakeTwilio is a deterministic classroom test profile. Its acceptance for the final external-service requirement must be confirmed; live API acceptance and actual handset delivery remain distinct evidence.

## Scope feasibility and evaluation

The mandatory core comprises one organization, whole-unit pooled resources, flat departments, fixed roles, project requirements, partial commitments, interval availability, ledger movements, consolidated shortages, two reminder types, required CRUD/read/delete contracts, Postman, seed and tests. Serial asset tracking, consumable returns, fractional units, future receipts, automatic optimization, machine learning and a sixteen-screen frontend are excluded. The implementation order and milestone gates prioritize an end-to-end conflict scenario before optional presentation work.

The skeleton demonstrates database connectivity, tables, test data, repositories, reachable controllers/components and correct JSON through Postman, with exported project and video. The final implements actual logic, includes the required recorded group discussion and peer reviews, and demonstrates a UI only if one is built. The assignment lists proposal30October, partial implementation13November with Canvas submission14November, and final5December2026. The proposal date has passed; the group must check current Canvas instructions rather than assume an extension.

## Submission preparation checklist

- Replace group and date placeholders; remove unused member lines.
- Review the complete technical appendices and make sure the group understands and can explain the selected rules.
- Confirm the RetroJit/Retrofit interpretation, exact class dependency baseline, image expectations under all-tools wording, FakeTwilio/live expectation and safe-history deletion interpretation.
- Add the required Certificate of Authorship using the course's actual wording; do not sign a statement not yet reviewed by the students.
- Acknowledge assistance accurately. Suggested assistance disclosure for student revision: We used an AI assistant to help analyze supplied course documents, research public sources, develop and review this specification, and prepare its documentation. We reviewed and revised the design and take responsibility for the final submitted work. Adjust this wording to reflect the group's actual contribution and course guidance; it is not a professor-approved certification.
- Each member prepares and emails their own truthful peer review of the other members, rating1-100 with comments for every phase. No ratings are invented and no messages are sent by this package.
- Submit the reviewed Word document in Canvas. The formal Word artifact includes the technical details below its proposal front matter, so it can stand alone for review.

## References

ESP Project Proposal2026, supplied three-page assignment, pages1-3. CSCI180.05 syllabus2026-27 first semester, supplied nine-page PDF, especially course outcomes, assessment and submission/authorship policies. Public rationale and primary technical URLs are registered in01-SOURCE-AND-REQUIREMENTS. The full Word appendix reproduces that register and its evidence limitations.


