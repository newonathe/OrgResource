# OrgResource — START HERE for the next chat or Codex session

## C8 next verification checkpoint — 2026-11-23 (source at `5a14714e868c`)

- **Stage promotion complete:** `8d849c256e163783639408e322cbf9bfc96a0226` carries the previous three-job-green registration/Playwright/Java-8 batch to `deploy/northflank-c8-staging`. GitHub Northflank service **build succeeded**; the exact running container SHA and hosted browser smoke are still unverified.
- **New coverage on feature:** `C1IdentityAcceptanceTest.C8AnonymousGateCoversEveryProtectedRegisteredJerseyOperationWithoutBusinessWrites` dynamically finds the 93 registered route declarations and probes **88 protected operations** with an unauthenticated browser client, valid pre-login CSRF for writes, 401 AUTH_REQUIRED response, non-disclosure and unchanged organization/resource/allocation/stock_movement row counts. The other **five public operations are not claimed covered by this test**; their separate acceptance remains in prior ID/Invitations cases.
- **Direct execution receipt:** [GitHub run 45065909845](https://github.com/newonathe/OrgResource/actions/runs/45065909845), `Verify Extended C Java 21 backend against isolated MySQL` step passed with this test at `5a14714e868c`. The full Newman/Playwright job was still running at this report snapshot; check latest status before claiming full green.
- **Source-reviewed governance oracle:** [C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md](evidence/C8/C8_IDENTITY_GOVERNANCE_ORACLE_2026-11-23.md) covers 20 of 93 operations as an independently **unaccepted** policy draft. Read underlying service and routes; validate each candidate receipt and denied-write postcondition.
- **Still open:** owner-approved bounded policy oracle for remaining 73 operations; meaningful authorized and role/object/tenant negative checks per route; verified SQL effects; protected local IR4 receipt reconciliation; genuine separate review; hosted runtime verification; safe additive 96-org demo and private secret rotation. C8 remains BLOCKED. No live DB was modified in this batch.

**Date/authority:** 2026-11-23 Asia/Manila. **PUBLIC-SAFE CONTINUATION RECORD.**
This is a current pointer, not a replacement for source code, course assignments, independent test receipts or live GitHub/hosting checks. Each new session must fetch current Git refs and test results. Original private source material, secrets, full local C8 evidence and account recovery codes are deliberately excluded.

## Current user goal and working agreement

Move quickly toward a complete, academically defensible OrgResource. Keep one coordinated corrective backlog, push every repository-safe change and its context to GitHub so new chats can continue without reconstructing work. Finish the bounded Extended C8 authorization gate and prepare a clearly fictional live demonstration using verified public student-organization names. Protect the functioning deployment and frozen academic implementation. Never mark a test passed without execution or declare completion merely because CI/build/login works.

User asks for a real working hosted app and has observed UI text overflow and empty Project work. User authorized GitHub implementation/documentation pushes to feature and staging branches; this is **not** an instruction to merge into main, bypass the C8 gate, rewrite Git history, destroy data, expose the restricted roster or send real SMS. Keep using fictional data and minimize personal information.

## Repository and branches — latest checked before this handoff commit

- GitHub: https://github.com/newonathe/OrgResource
- Draft PR #25: https://github.com/newonathe/OrgResource/pull/25 ; head feature branch feat/extended-c-c8-domain-workflows-2026-11-19; base feat/extended-c-c7-security-privacy-2026-11-19. **Draft/unmerged.**
- Feature branch checked SHA: c0409a98a3bdfa0f6608c6bdaf7fccd6ed5c06b0 before the new handoff/inventory commit.
- Northflank tracked branch: deploy/northflank-c8-staging ; checked SHA: 89c28614e24f585f4b76b29b6efacad54442005b before corresponding promotion.
- Frozen academic Course Final A reference: 7f8c0746da18fd54b47f430ac1435ac36e605d67. Do not modify frozen Course A implementation/history during C8 work.
- Previous working Northflank checkpoint: b957e3ee3303425ae9699dff6c0583ba71de123c (prior to UI/CI/security fixes). Newer staging builds have succeeded; inspect the deployed/running image digest/SHA instead of equating build success to live rollout.
- Do not assume GitHub changes to the C8 feature branch deploy to Northflank. Northflank tracks the separate staging branch, and CI/CD toggles need checking. GitHub verify.yml previously runs on PR heads and main pushes, not automatically on staging pushes; this is not a release gate.

## Course authority and architecture

The original CSCI 180.05 assignment (ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf), syllabus (08_CSCI180_SYLLABUS.pdf), concise proposal (10_ORGRESOURCE_CONCISE_PROPOSAL.docx) and current Canvas instructions are the academic authorities. Current Canvas announcements, uploads and receipts have not been inspected. Assignment prints December 5, 2026 final implementation deadline; check Canvas.

**Course Final A:** separate Java 8 Spring Boot 2.1/Jersey javax/JPA/Spring Data/MySQL implementation, five entities (Department, Project, Resource, ResourceRequirement, Allocation), 28 HTTP operations after consent-revocation/image amendments, React screens and Postman. See README.md, docs/academic-final/COURSE_FINAL_A_AS_BUILT.md, evidence/COURSE-A/A3_TECHNICAL_GATE.md and docs/COURSE_A_FINAL_DEMO_GUIDE.md. Original proposal was five entities/25 operations; old nine-entity/68-operation drafts are history. No instructor acceptance, final Canvas submission, authorship certificate or authentic demo video has been verified. Acknowledge assistance honestly.

**Extended C:** Java 21, Spring Boot 4.1.2, Jersey, authenticated React/TypeScript, MySQL 8.0.46, Flyway V1–V7, memberships and multi-organization roles (OWNER, ADMIN, CUSTODIAN, LEAD, MEMBER), inventory ledger, borrower/Lead and reporting features. Code lives under extended/. The 93 annotated Jersey HTTP operations are now enumerated in evidence/C8/C8_ROUTE_INVENTORY.json with a drift-check script at scripts/verify-c8-routes.mjs and a review ledger at evidence/C8/C8_ROUTE_REVIEW_LEDGER.md. Static inventory is not package-registration or security proof.

## Observed deployment facts (not full site acceptance)

Northflank project Default, service OrgResource, free Sandbox (512 MiB / 0.2 shared vCPU during setup), MySQL addon orgresource-mysql, database schema orgresource_staging, shared secret group orgresource-staging-secrets, manual bootstrap job orgresource-owner-bootstrap. The Java/React site loaded via public HTTPS, Flyway V1–V7 validated, first fictional owner was initialized exactly once, login/recovery worked, and the user navigated the application. Hosted end-to-end transactions, secure backups, independent restore, runtime/migration grants, idle performance, accurate container SHA, disaster recovery and 24/7 uptime are NOT verified. SMS/reminders remain disabled. Use only fictional data.

**Sensitive incident:** Earlier bootstrap logs exposed a live MySQL credential and five recovery codes into conversation text. Treat those as compromised. Rotate the affected DB credentials and account recovery codes, remove the bootstrap password/old secret from the finished job, configure password-free JDBC URLs and verify server logging. A recent source change suppressed Hibernate connection-pooling INFO diagnostics but has NOT itself rotated credentials or erased old logs. Do not paste password-bearing URLs, recovery codes, encryption keys or unrestricted log dumps into chats or GitHub. The existing protected EXT_CONTACT_ENCRYPTION_KEY is required for encrypted contact operations; exact runtime configuration was not independently verified.

## Last completed tests and recent UI fixes

- All three GitHub Actions jobs passed on feature SHA c0409a98a3bd: https://github.com/newonathe/OrgResource/actions/runs/45062378023 . Jobs: Course A Java 8 isolated MySQL; Extended C Java 21 MySQL/Newman/Playwright; React typecheck/tests/lint/build.
- Java 8 checksum-provisioning failures were fixed with actions/setup-java@v4 using Temurin 8. A Playwright regression discovered from changed LEAD empty-state wording was corrected; last complete run green.
- Northflank reported successful builds for staging SHA 89c28614e24f. **Successful image build alone is not proof that running deployment has updated or that real user flows work.**
- UI screenshots showed sidebar links escaping their containers and Project work panel headings/text without inset padding. Current staging/feature code adds sidebar overflow auto with min-height 0, text wrapping and inner padding for seven Project Work/Borrowed panels; a Playwright responsive regression passed in the green run. Check actual deployed page on desktop/narrow/mobile.
- The Project work page is empty in the newly bootstrapped organization because no fictional projects have been created. The empty state is not itself an API defect.

## C8 current gate — STOP claiming accepted

Authority: evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md plus evidence/C8/C8_FINAL_STATUS.md and related docs. Source inventory: 93 Jersey operations, 15 resource classes. Historical ledger retains 199 executed passes, 48 reviewed exclusions and 22,073 unrun; those historical unrun cases are not automatically required under the bounded v1 contract, but their status must not be changed retroactively.

Unfinished mandatory evidence: verify actual packaged routes and handlers; pre-review each route's auth/tenant/role/object/lifecycle/field rule oracle; independently execute appropriate allowed and denied requests with real fictional targets; exact HTTP/error/minimal disclosure; SQL postconditions on denied high-risk writes; selected vs path tenant and revoked membership; borrower/Project Lead; redaction; inventory concurrency/replay/receipts/restart/rollback; reporting/export; independent human or genuinely separate review; exact-final-SHA green CI. The restricted local IR4 evidence packet has not been supplied to this chat. The new route ledger is **UNREVIEWED/UNMAPPED** until independently reconciled and populated with genuine receipts. C8 remains BLOCKED. C9 stays on hold.

## The 96-organization fictional live demo — user intent and technical blocker

**User decision:** populate the live demo using actual public directory organization names, but fictional internal departments, members, projects, stock, requests, histories and phone/SMS data; label every activity synthetic, without implying actual institutional approval or measured real use.

Approved reference cohort: 96 labels (72 COA-M, 24 LIONS) with historical alias/exclusion rules; see evidence/C8/DEMO_GENERATOR_AND_IMPORT_CONTRACT.md. The **local private source-manifest** SHA-256 is 57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2. Local generator scripts/generate-c8-demo.py generated a separately verified local-only full dry-run plan SHA 3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c with 96 orgs, 248 departments, 713 projects, 1,312 resources, 2,139 requirements, 462 simulated personas, 21,060 allocations and 67,370 historical actions. Historical as-of 2026-11-19, twelve months and six statuses; these are generated data counts, not observed real org activity.

**Important limitation:** The approved manifest, full plan and private raw receipts are deliberately not in public GitHub and were not supplied in this chat. The existing Java offline importer C8DemoCommand.java and C8DemoImporter.java purposely accept only a *fresh*, *local-loopback*, *empty* demo schema. They reject a normal Northflank DB, and the importer explicitly refuses any existing organization/account. The live orgresource_staging database already contains the owner's account and must NOT be reset to satisfy the old importer. The 512 MiB free Java service is not a verified fit for the full 21k request import.

**Implementation path (not yet executed):** inspect approved local source manifest privately and hash-verify; design an additive, transactional, idempotent staging importer that explicitly requires separate target confirmation and independently verified off-host backup, preserves existing account/organization data, attaches the demo cohort to a controlled operator membership, enforces uniqueness/provenance and cross-tenant stock invariants, supports dry-run and bounded small-fixture resource profiling, never logs secrets/contact fields, and gives rollback/recovery evidence. Reject missing/mismatched source and existing conflicting demo provenance. Test against isolated cloned staging first; no direct SQL bulk paste, live SMS, unexplained rights assumptions or blind running of --c8-local-demo-import against Northflank. Live import is NOT_DONE.

Read docs/deployment/LIVE_C8_DEMO_IMPORT_DECISION_2026-11-23.md for the guarded acceptance criteria.

## Security and publishing

The repository is public and tracks some original course and contextual materials under ChatGPT-Project-Pack/UPLOAD/. Rights/publication review remains open. Do not publish additional restricted raw evidence, private course materials, private transcripts, credentials, source manifests, generated org records containing individual data, real borrower names or phone details. Do not misrepresent demo public labels as campus partners or approved systems. Use protected secrets. No source-control history rewrite or visibility changes without explicit user authorization.

## Urgent work order — continuous iteration with evidence, not repetitive planning

1. Check latest feature/staging SHA, PR #25, CI and Northflank build+running deployment. Retain a known-working rollback image.
2. Verify the 93-route inventory drift check in CI; create and review the route policy ledger, map existing protected receipts to exact SHA/scenario, run meaningful missing positive/negative/auth/tenant tests. Fix proven defects minimally. C8 accepted only after independent review and all mandatory evidence.
3. Perform safe live-hosted fictional workflow smoke (login, create Department/Project/Resource/Requirement, request, approve, issue/return/consume, refresh, persistence, cross-tenant denial); check responsive UI and non-overflowing headings and nav.
4. Rotate leaked credentials/recovery codes, verify correct logged JDBC URL handling and encryption configuration. Verify DB grants, hosted backup/restore and port/ingress policy.
5. Obtain restricted approved manifest through authorized local access; design/review small-then-full additive staging demo loader with explicit owner-run consent, backup and rollback. Do not claim hosted 96 organizations until actual data is present and independently checked.
6. Preserve Course A; prepare Postman/video/authorship final requirements before printed December 5 deadline; check latest Canvas announcements.
7. C9 remains deferred until C8 accepted and user specifically authorizes it.

**Every future chat or coding agent should:** start with this page, AGENTS.md, current C8 bounded contract and actual files; fetch fresh SHAs; perform meaningful source review; push repository-safe code and updated handoff; run CI; report PASS/FAIL/BLOCKED/NOT_RUN with exact SHA; never invent test/hosting results. Do not assume the historical big master document dated Nov 13 reflects current implementation.

## Latest conversation-level factual recap

The user first enabled a Northflank job with Docker command --bootstrap, then signed in successfully to the Extended C workspace. Subsequent screenshots showed sidebar overflow and an empty Project work panel. Corrections were pushed to both branches and the full CI run passed. The Java 8 setup step and Hibernate INFO leak risk were subsequently corrected. The user explicitly requested rapid C8 closure and a populated live 96-org fictional demonstration. The last assistant did not execute the live import. In this session we created a static 93-operation inventory and a CI drift gate; those are preparatory acceptance evidence, not C8 closure. This GitHub document is intended to carry the context across future chats.

## Copy-ready continuation command for a new chat

Read NEXT_CHAT_START_HERE.md, AGENTS.md, evidence/C8/C8_FINAL_STATUS.md, evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md, evidence/C8/C8_ROUTE_REVIEW_LEDGER.md, evidence/C8/DEMO_GENERATOR_AND_IMPORT_CONTRACT.md and docs/deployment/LIVE_C8_DEMO_IMPORT_DECISION_2026-11-23.md. Inspect GitHub branch heads and exact CI status. Finish C8 bounded authorization by testing and correcting real gaps, verify hosted UI/flows, and prepare a guarded additive fictional demo importer only when private manifest and safe backup/test target are available. Push all public-safe work and updates to this handoff to GitHub. Do not merge, deploy destructively, send SMS, reveal credentials, represent fictional activity as real or claim acceptance without independent evidence.
