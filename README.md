> **FINAL 2026-11-16 HANDOFF NOTICE — RESEARCH BRANCH ONLY:** This root README contains an older historic specification inventory, not the newest build plan. **START with [CP46 final release index](docs/research-2.0/77_CP46_FINAL_GITHUB_HANDOFF_RELEASE_INDEX.md)**, [academic requirement/26-route packet](docs/academic-final/00_READ_FIRST_AND_DECISION_BOUNDARIES.md), and the [ONLY active Codex master prompt](docs/research-2.0/75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt). The user requires **TWO independent editions**: first finish/freeze a strict course-compatible A, then build full Extended C B from that exact A revision and independently prove original course behavior on B. Original nine-entity/68-route designs and the older single-release implementation prompts are historical. **Neither final edition has been implemented or tested by these documentation commits; no branch merge or Canvas action occurred.**

# Template Pack Applicability Assessment

The reference pack contributed engineering and documentation practices. Its commercial ERP domain and platform were deliberately excluded. OrgResource remains a one-organization student resource-planning service using the professor's stack. Detailed inspection coverage and source limitations appear in01.

# OrgResource Specification Package

## Current implementation status

The current application scope is the five-entity OrgResource backend with 25 original routes plus the proposed A09 consent-revocation route. Start with [the current route contract](docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md), [the current Postman reference](postman/OrgResource-Current-26-Routes.postman_collection.json), and the phase evidence under `evidence/`. The current implementation is in `backend/`; the React/TypeScript app and its startup/check commands are in [frontend/README.md](frontend/README.md).

The original nine-entity, 68-operation contract and its Postman collection are historical planning material. They are retained for provenance and are not current implementation scope. This README's package and proposal inventory below describes that earlier specification handoff; it should not be used to infer current entity or route counts.

Version 1.0 was prepared 3 November 2026 as the complete specification and proposal handoff. A Java 8 / Spring Boot / Jersey O1 backend skeleton has since been implemented under `backend/`. Its build, local MySQL schema, HTTP checks and restart persistence are recorded in `evidence/O1/PHASE_REPORT.md`. The skeleton is not the complete application.

Start with OrgResource-Formal-Proposal.docx for a self-contained Word proposal and technical appendices. Replace the group-member/date placeholders and review the flagged course confirmations before submission. OrgResource-Specification-Package.zip is the transferable source package. The Markdown files remain easier to search and update than the long Word compilation.

Current separate Part I file: OrgResource-Concise-Submission-Proposal.docx is now an independent, simplified proposal with five entities and 25 operations. It contains readable JSON, all required operation descriptions, component jobs and method steps, and entity/repository details. The sole group member is Ethan Owen Taruc, 236196. It describes proposed work only. This smaller design keeps whole-batch resource allocation, availability, project shortages, reusable returns, consumable use, and basic SMS reminders. It does not depend on implementation or tests already existing.

The full 156-page proposal and its nine-entity, 68-operation contracts remain unchanged as the earlier comprehensive specification. They are a different scope from the current concise proposal. The existing ZIP is the earlier package and has not been regenerated for this revision. Use the current standalone concise Word document for this Part I version. Historical checks and generators for the original nine-entity concise draft do not validate or rebuild the new five-entity document.

## Ten deliverables

1. 01-SOURCE-AND-REQUIREMENTS.md: authority, public rationale, evidence limits, full course compliance checklist, assumptions and confirmations.
2. 02-COMPLETE-PROJECT-SPECIFICATION.md: purpose, vocabulary, roles, workflows, state transitions, exact inventory/availability/shortage formulas, scope and optional UI.
3. 03-TECHNICAL-ARCHITECTURE.md: compatible stack, packages, transaction/lock protocol, identity boundary,12 decisions with alternatives,9 Mermaid diagrams.
4. 04-DATABASE-SPECIFICATION.md: all9 entities, fields/types/nullability/constraints, mappings, relationships, indexes, deletion rules and20 custom repository methods.
5. 05-REST-API-CATALOGUE.md: all68 operations with DTOs, input/output/errors, controller/component/repository forwarding and transaction behavior.
6. 06-COMPONENT-SPECIFICATION.md: every controller-invoked method plus internal calculations, notification methods, permissions, seed and scheduled methods.
7. 07-INTEGRATION-AND-SCHEDULING.md: Retrofit interpretation, Twilio/FakeTwilio contracts, consent, reminders, outbox, retries, caps and unknown-outcome recovery.
8. 08-POSTMAN-TESTING-AND-DEMONSTRATION.md: import/run instructions,99-request ordered fictional demonstration,8-request negative collection,52 acceptance scenarios, skeleton/final evidence plans.
9. 09-FORMAL-PROPOSAL-DRAFT.md and the Word document: group placeholders, application prose and all required technical information incorporated as appendices.
10. 10-IMPLEMENTATION-HANDOFF.md: dependency order, minimum scope, milestones, risks, confirmation register and a copy-paste coding-agent handoff.

## Machine readable contracts

contracts/domain-model.json and contracts/operations.json are the structured sources for the generated entity/API/component catalogues. contracts/response-examples.json contains complete response shapes. The Postman directory contains the complete API reference, ordered demonstration, expected failures and local environment. tools/build_contracts.py and tools/build_demo.py author documentation/contracts; they are not Java application code. Keep canonical contract changes and prose aligned. tools/check_specification.py validates coverage and independent arithmetic fixtures, not the eventual backend.

## Important review points

The assignment literally prints RetroJit, while the syllabus and examples use Retrofit/GSON. That interpretation is explicit. Confirm the actual working course BOM/JDK/MySQL environment, final FakeTwilio/live expectation, image expectation under all-tools wording, and acceptance of safe deletion refusal for posted history. No destructive history erasure is hidden behind a successful delete response.

All quantities/users/projects in examples are fictional. Public reports support a bounded coordination rationale, not proven adoption, campus-wide failure or measured savings. The reference pack was inspected by structure and relevant sections; unrelated commercial details and course recordings were not exhaustively reviewed. The source register is candid about that boundary.

The DEMO identity header is impersonable and permitted only on a local isolated demonstration. Authentication for shared live use is an explicit fail-closed boundary, not a completed feature. SMS is disabled by default; no real message was sent. Provider acceptance is distinct from delivered status and observed handset delivery.

The proposal's written deadline has passed. Only the student/instructor/current Canvas can resolve submission timing. No submission, email, peer-review rating or authorship certification was performed on the student's behalf.

## Validation status

See qa/consistency-check.json for the documentation/fixture check count and qa/QUALITY-REVIEW.md for substantive self-review. The Word document and diagrams were checked separately. The O1 skeleton has also been built with Java 8, run against its isolated local MySQL database, checked through Postman/Newman, and restarted to verify persistence; see `evidence/O1/PHASE_REPORT.md`. Unit tests, full allocation lifecycle, MySQL concurrency, live Twilio, a real Eclipse import, instructor acceptance and real-world adoption remain unverified or unimplemented. Do not treat the O1 evidence as final-project acceptance.

Rebuild the current separate Part I proposal with tools/build_beginner_part1.py followed by tools/build_concise_word.py. Check it with tools/check_readable_part1.py. tools/build_part1.py and tools/build_concise.py are earlier draft generators; tools/check_part1.py checks that earlier draft. The current proposal coverage report is qa/readable-part1-check.json.

