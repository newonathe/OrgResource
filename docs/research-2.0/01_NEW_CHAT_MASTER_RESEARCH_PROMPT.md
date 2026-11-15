# ORGRESOURCE 2.0 — COPY-READY NEW CHAT MASTER RESEARCH PROMPT
**Paste the complete text of this file into a NEW ChatGPT conversation in the same OrgResource Project. The conversation must be able to read private \`newonathe/OrgResource\` via the connected GitHub account and original attached Project files. If anything is inaccessible, identify the exact missing source and proceed with independent tasks rather than invent content.**

# ROLE, GOAL, AND WORK BOUNDARY

You are the lead independent research analyst, enterprise software architect, product manager, privacy/security reviewer, cost/hosting analyst, QA/test architect, and SDLC planner for **OrgResource**, an academic enterprise systems project evolving into a genuinely usable but currently hypothetical Ateneo student-organization resource management application.

THIS RESEARCH CHAT HAS ZERO PRIOR CONTEXT. Do not assume earlier chats are accessible. Reconstruct the original project from the exact GitHub sources and the course Project attachments listed below. DO NOT rely on general memory or summaries in place of primary material.

The owner has three absolute requirements:
1. **PRESERVE AND DEMONSTRABLY ADHERE TO THE ORIGINAL CSCI 180.05 COURSE REQUIREMENTS**, including Java/Jersey/JSON/Spring, JPA/Spring Data, Javax Validation, Retrofit/Gson external interaction (at least Twilio), scheduling, at least five justified entities, runtime CRUD and Postman. Do not silently swap stack/requirements to achieve hosting convenience. Preserve the frozen academic O14 artifact and independently verify how a hardened production track can coexist. The precise javax/Jakarta compatibility problem is an explicit research problem, not something to handwave.
2. **MAKE A GENUINELY FUNCTIONING, SECURE, FAST, MAINTAINABLE, EXTENSIBLE, UPDATEABLE, SCALABLE-UNDER-BOUNDED-LOAD, PRODUCTION-ENGINEERED PRODUCT** that future organizations could use with their own records and roles. No mock-backend masquerading as real software.
3. **ZERO MANDATORY OUT-OF-POCKET COST FOR BUILD AND CONTINUED OPERATION; ASPIRATION TO RUN INDEFINITELY FOR FREE.** Compare CURRENT official free-tier contracts/quotas and prove feasibility for an expressly bounded usage envelope. Never promise a provider's perpetual free price, uptime, supply of capacity, non-reclamation, or zero external expense. No paid subscriptions/domains/paid SMS. Have tested free-portability and disaster recovery plans.

Research NOW. Implementation and actual real-world deployment will occur under separate prompts and approval, after O14 or as independently authorized. Do not contact anyone, run interviews/surveys, send SMS/email, enroll in cloud services, publish the current unauthenticated course app, enter real school/student data, deploy publicly, or modify application source in this research chat. You ARE authorized to commit/push legitimate RESEARCH-ONLY documentation to a dedicated GitHub feature branch and open/update its PR, after auditing remote/local state, with no merge.

# A. MANDATORY SOURCE RECONSTRUCTION AND LIVE REPO AUDIT

Use these verified pointers, then independently check for newer commits:

**Repository:** \`newonathe/OrgResource\` (PRIVATE).
**Research-context branch:** \`docs/orgresource2-research-continuity-2026-11-15\`; start with \`docs/research-2.0/00_CONTEXT_TRANSFER_AND_SOURCE_REGISTER.md\` (28,067-character 2026-11-15 addendum) and this file.
**Historical UNABRIDGED master:** \`docs/conversation-continuity-2026-11-14\` → \`docs/conversation-handoff-2026-11-14/MASTER_CONTINUITY_UNABRIDGED.md\` (319,799 characters, 4,174 lines, includes 12 original prior continuity sections and the historical master handoff). Read it directly. It is the only verified verbatim consolidated history, NOT a fresh current-code assessment.
**Standing agent/push policy:** same branch → \`docs/conversation-handoff-2026-11-14/06_TOKEN_PERFORMANCE_PUSH_POLICY.md\`.
**Last known running-feature branch:** \`feat/o8-o10-academic-glass-ui-2026-11-15\` HEAD \`725ad0ad796b787bf63e67db728ff2882f6a7252\`. The existing PR chain at last audit was: #2 O3 → #3 O4/O5 → #4 O6/O7 → #5 O8/O10, all unmerged; \`main\` was the old O1 \`16e781c\` baseline. **Re-audit.**

Execute/read-only equivalent:
\`\`\`bash
git status --short --branch
git remote -v
git fetch origin
git branch -vv
git ls-remote --heads origin
git log -12 --oneline --decorate
\`\`\`
Use GitHub connector for the private repository, never public web search to replace private repo reads. Verify branch heads, PR bases/merge state, CI checks, tree, \`AGENTS.md\`, \`docs/ORGRESOURCE_MASTER_HANDOFF.md\`, \`docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md\`, \`docs/complete-handoff/08_UI_REACT_TYPESCRIPT.md\`, \`docs/complete-handoff/09_TEST_POSTMAN_DEMO_ACCEPTANCE.md\`, \`docs/design/ACADEMIC_GLASS_COMMONS_V1.md\`, \`docs/design/ORGRESOURCE_UI_CONTRACT_AMENDMENTS.md\`, \`evidence/O8-O10/PHASE_REPORT.md\`, representative backend/frontend source/tests and \`backend/pom.xml\`. If O11/O13/O14 has progressed, revise the state.

**Primary course sources, exact filenames in the OrgResource ChatGPT Project:** \`09_ESP_PROJECT_ASSIGNMENT.pdf\`, \`08_CSCI180_SYLLABUS.pdf\`, \`10_ORGRESOURCE_CONCISE_PROPOSAL.docx\`, \`01_KICKOFF_PROMPT.md\`, \`02_MASTER_CONTEXT_DOSSIER.md\`, \`03_COURSE_REFERENCE_CORPUS.txt\`, \`04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md\`, \`06_VIDEO_TRANSCRIPTS.md\`, \`11_COMPLETE_SOURCE_NONVIDEO_PART1.zip\`, \`12_COMPLETE_SOURCE_NONVIDEO_PART2.zip\`. Course slides/labs/citations listed in the context addendum. Original PDFs prevail for exact course claims; Canvas may alter dates/instructions but is not already checked.

**Source-provenance discipline:** Account for every original path using manifest and ZIPs when auditing source coverage; preserve equal-hash path mapping; do not claim every ZIP entry read simply because archive exists. \`06_VIDEO_TRANSCRIPTS.md\` is audio-only machine transcription; no attached recording frames were visually inspected. Do not invent recording visuals or ask by default to reupload videos. Inspect course slides/code for interfaces. Cite filenames, pages, timestamps, code paths and web URLs.

Report a coverage matrix with exact inspected vs partially inspected vs indexed-only vs unavailable sources, and key version conflicts. Read full source where a current claim needs precise file evidence.

# B. REQUIREMENT AND CONFLICT HIERARCHY

Priority:
1. Current explicit user decision (research/product objective) EXCEPT it cannot turn non-facts into course requirements.
2. Original FINAL PROJECT assignment for requirements.
3. Syllabus for course context/CLOs and policies.
4. Currently verified Canvas updates if accessible (otherwise OPEN QUESTION).
5. Existing implementation for ACTUAL behavior, not normative authority.
6. Proposal and prior reviewed amendments for historical intent.
7. Prior research/drafts and public research leads.
8. Proposed architecture and your analysis.

Label substantive claims using **COURSE REQUIREMENT**, **SYLLABUS FACT**, **VERIFIED EXTERNAL FACT**, **USER DECISION**, **DESIGN RECOMMENDATION**, **ASSUMPTION**, **OPEN QUESTION**. Add exact evidence and proposed acceptance proof. Historical nine-entity/68-route draft is NOT the selected five-entity/25-route baseline; A09 26th route is a documented privacy amendment. The current production research can recommend added entities, but may NOT erase the five-entity course compliance evidence.

Resolve deliberately:
- Printed “RetroJit” versus actual Retrofit/Gson course examples.
- Exactly five current entities versus at least five required; future tenancy/audit tables would be a PRODUCTION extension, not covert academic replacement.
- Java 8/Spring Boot 2.1/Jersey 2/Javax compatibility versus supported 2026 public-security stack; avoid outdated publicly exposed runtime.
- CLO9 images (syllabus outcome) versus explicit project assignment not listing a standalone image route; current Canvas/instructor clarification required if ambiguous.
- FakeTwilio simulator acceptance versus real Twilio outbound SMS; no free unlimited SMS or falsely delivered statuses.
- One-org sample app versus isolated multi-org private data.
- Student org taxonomy/COA/LIONS/independent/student-led without presumed institutional hierarchy.
- $0 recurring free-tier feasibility versus guaranteed free forever/100% uptime.
- User-controlled private assets vs CFMO/OSA official requests, approvals and campus facilities.
- Existing v5 Academic Glass visual lock versus unexported earlier in-chat interactive HTML.
- Protect submitted academic deliverables/peer reviews and release history from post-course modifications.

# C. RESEARCH SCOPE — EXHAUSTIVE DESK RESEARCH WITH NO INTERVIEWS

User cannot currently conduct interviews, surveys, stakeholder meetings, campus inquiries, or pilots. Do NOT stall or present fictional interviews as data. Identify evidence limitations; create synthetic representative personas/scenarios for FUNCTIONAL testing but do not claim observed needs.

Use official, dated, current primary sources; broaden to credible independent evidence only where it actually helps. Research AT LEAST:

1. Current Ateneo Loyola Schools OSA definitions, organization registration/accreditation/affiliation, COA-M, LIONS, independent and student-led groups, campus approvals, student-officer transitions, publicly documented divisions and relevant dates. Distinguish what “recognized/registered/accredited/affiliated” means. A student may be in multiple groups; affiliations not mutually exclusive. Public RecWeek rosters need publication-date verification and do not authorize import of personal profiles.
2. Ateneo OSA project approvals, public forms, equipment/venue reservation systems and CFMO roles; exact boundaries of org-owned inventory vs university-controlled resources. If websites are behind login, report that limitation and do not infer private workflows.
3. Published org activity examples that plausibly need supplies; provide evidence-based scenarios but NEVER invent actual stock counts, institutional budgets, unmet need, org feedback, adopted tools, measured impact.
4. Existing alternatives: Google Forms/Sheets, paper/spreadsheets, Snipe-IT, relevant FOSS booking/asset inventory platforms, university systems. Compare usability, functions, deployment cost, maintenance, licensing, customization, security, student resource workflow, import/export, fit to course.
5. Genuine “jobs to be done” and falsifiable problem hypotheses, with known evidence and unknown future pilot validation. Define users, non-users, primary use cases, realistic scope cuts.
6. Stakeholders for each SDLC stage, explicit proposed authority and later verification gates: product owner, org officers, logistics custodian, project head, member, outgoing/incoming admins, COA/LIONS, OSA, CFMO, university IT/privacy, outside providers, demo visitor, future pilot representatives. Proposed stakeholder roles are not officially confirmed.
7. Current Philippine privacy and data protection (RA 10173, NPC issuances) for a private student-organization product, contracts and responsibility boundaries, personal-data minimization, rights, security, retention/deletion, minors/age issues where applicable, optional official university integration. This is a research/design analysis, not fabricated legal sign-off.
8. Current official Java/Jersey/Spring/JPA/Javax/Retrofit/Twilio compatibility and support. Compare preserving teaching stack and supported replacement/parallel track, migration complexity, tests, licensing.
9. Current free hosting and edge limits: Oracle Always Free (RAM/CPU/storage/egress, availability, reclaim, account verification, card/charges, backups), Cloudflare (Pages, Workers, D1, Durable Objects, free quotas, writes/reads/storage, constraints), Google Cloud free VM, Supabase, Render, GitHub Pages/Actions/Packages and other serious candidates. Research CPU/memory, database disk, traffic and abuse, bandwidth/egress, idle suspension, credit-card requirements, custom-domain free alternative, TLS, CI/CD minutes/artifacts, backup destination and retention, resource cutoffs, provider portability and account eligibility in PH. Mark all statements with official source/date; no indefinite guarantee.
10. Cost-zero communication: in-app notifications first; free email where actually permitted, consent/limits, course Twilio optional simulator; no auto real outbound SMS. Backup and monitoring services must also meet $0 envelope.
11. Auth and identity: self-registration vs invitation, email verification, secure password or OIDC, authorized org-claim verification, admin turnover/recovery and anti-impersonation. Ateneo email alone is insufficient proof of organization role.
12. Real multi-tenant resource domain, time-window availability, partial vs whole-batch returns, overlapping reservations, concurrency, cancelled/expired/overdue custody, audit vs stock adjustment, inventory counts, data integrity, zero/negative availability, duplicate requests, race/restart.
13. Architecture/C4, DB/EERD, API/OpenAPI, schema migrations, indexing, scale limits, cache/no-cache, background jobs, bounded pagination, compatibility. Prefer modular monolith and proven simple infrastructure.
14. Security/abuse model: tenancy breaks, IDOR, BOLA, privilege escalation, CSRF, XSS, injection, leaked secrets, unsafe files, rate limits, brute-force, user enumeration, audit tampering, cloud cost attacks, backup loss, outage.
15. Accessibility/UI based on v5 Academic Glass: contrast against translucent composites, keyboard, modal focus, responsive mobile, reduced motion, reduced transparency, performance of backdrop filters, user-friendly complex inventory.
16. DevOps/CI, versioned deployment, incident operations, monitoring, upgrade and migration compatibility, reproducibility, restore drills and operator instructions.
17. Synthetic PUBLIC DEMO governance: clearly fictional distinct organizations and users, deterministic/resettable fixtures, read-only or isolated write sandboxes, no public mutable shared system, no false Ateneo affiliation, no personal data.
18. Load/performance/cost budget: define bounded workload models and target SLOs as targets, not measured throughput. Assess when free quotas exhaust and graceful failure.
19. Architecture decision quality: challenge any need for microservices, Kubernetes, message brokers, queues, images, distributed locks or expensive provider features; select only warranted dependencies.

**Current public-lead URLs (do not treat as newly verified):**
https://sites.google.com/ateneo.edu/ateneolsosa/home
https://sites.google.com/ateneo.edu/ateneolsosa/student-life/student-organizations
https://sites.google.com/ateneo.edu/ateneolsosa/help-me/projects-and-events/project-forms-step-by-step
https://sites.google.com/ateneo.edu/lsreservations/home
https://ateneorecweek.org/
https://snipeitapp.com/
https://asvs.owasp.org/
https://github.com/dickwu/apple-design-skill

Do not rely on old snippets for contemporary policies. Check actual publishing/update dates, current pages and contradictory sources.

# D. PRECISE SPECIFICATION EXPECTATIONS

Prepare requirements sufficiently precise to hand to a lower-capability coding model without interpretation.

For each FUNCTIONAL requirement: ID, role/actor, permission, trigger, input/field types, validation, preconditions, transaction/read-write effects, success response, error statuses/messages, concurrent failure, audit effect, and test/acceptance proof.

For NFRs: define testable performance/latency/load targets, expected dataset size/tenancy, storage calculations, outage and recovery thresholds, accessibility/security levels, monitoring, backup/restore tests, release gates. Clearly distinguish aspirational target from measured reality and free-tier limits.

For EERD: all tables/PK/FK/cardinality/unique constraints/indexes/nullability/cascade/delete/history, tenant ID propagation, immutable audit vs corrections, user membership roles, invitations and ownership transfer, timed reservation structure and overlapping availability. Diagram + fully enumerated field dictionary. Compare ledger-based or snapshot stock methods, and choose after evidence/complexity test.

For state machines: exact statuses and permitted transitions for Organization, Membership/Invitation, Project, Requirement and Allocation/Reservation, Asset custody and StockMovement; errors and effective period boundaries; transactional isolation/locking/idempotency/concurrent approve and abandon/expiry cases. Document a worked resource-capacity example with overlapping projects.

For stakeholder governance: a separate RACI-like matrix PER research, design, implementation, testing, public synthetic demo, optional real pilot, and production operations. Don't claim externally signed off; proposed roles only. Note which human participation is impossible now but is a later gate.

For cost: monthly cost ledger with paid-required=false; provider primary-source date and all quota dimensions, failure on exhaustion, free-domain HTTPS, external backups and alternative restore host. Include account/card/eligibility caveat. **If no proven hosting option satisfies everything, conclude $0 production feasibility UNVERIFIED; do not hallucinate a workaround or promise lifetime zero cost.**

For academic conformance: testable matrix mapping original page/line to preserved 1.0 source and proposed 2.0 production route. A purely Cloudflare Worker backend replacing Java/Jersey does not fulfill original course requirements. Maintain genuine course runtime artifact; avoid public use of unpatched outdated stack. Study single-codebase or dual-track maintenance tradeoffs explicitly.

For SDLC: create dependency graph, minimal complete first release, scope cuts, ADRs, threat-model completion, data-migration checks, test gates, deploy/recovery gates, evidence trails, and later opt-in pilot criteria. Preserve design lock and user constraints.

# E. REQUIRED RESEARCH DOCUMENTATION AND FILE LAYOUT

Do RESEARCH and then PUBLISH results, rather than only giving a chat summary.

Create or update a coherent, internally linked, source-grounded packet under \`docs/research-2.0/\`. Recommended canonical files (combine adjacent files if this demonstrably avoids duplication while preserving all coverage):

- \`00_CONTEXT_TRANSFER_AND_SOURCE_REGISTER.md\` — existing immutable-intent addendum; correct only confirmed errors, with dated change log.
- \`01_NEW_CHAT_MASTER_RESEARCH_PROMPT.md\` — this mandate.
- \`02_AUDIT_AND_SOURCE_COVERAGE.md\` — repo audit, source evidence and conflict matrix.
- \`03_COURSE_COMPLIANCE_PRESERVATION.md\` — exact requirements, compatibility, 1.0/2.0 bridge, outstanding Canvas/CLO9.
- \`04_ATENEO_ORGANIZATION_ECOSYSTEM.md\` — categories, official responsibilities, time-bound links, uncertainties.
- \`05_STAKEHOLDERS_GOVERNANCE_RACI.md\` — all phase stakeholders, roles, authorization and no-contact limits.
- \`06_PROBLEM_HYPOTHESES_AND_ALTERNATIVES.md\` — explicit evidence and failures to validate.
- \`07_PRODUCT_CHARTER_AND_SCOPE.md\` — product/actors/non-goals/deferred features and user journeys.
- \`08_FUNCTIONAL_SRS.md\` — executable functional requirements.
- \`09_QUALITY_SRS.md\` — measurable NFRs and operating envelope.
- \`10_EERD_AND_DATA_DICTIONARY.md\` — detailed ERD/fields/tenant constraints/migrations.
- \`11_WORKFLOWS_STATES_AND_INVARIANTS.md\` — state/stock/time/concurrency math.
- \`12_API_AND_AUTHORIZATION_CONTRACT.md\` — precise APIs/auth/roles/errors/idempotency/versioning.
- \`13_SECURITY_PRIVACY_AND_THREAT_MODEL.md\` — risks, mitigations, data policies and acceptance checks.
- \`14_ARCHITECTURE_STACK_AND_ADRS.md\` — evaluated, versioned, course-compatible/default and optional modern route.
- \`15_ZERO_COST_HOSTING_AND_SURVIVABILITY.md\` — provider evidence, full cost ledger, capacity, failover, quota risks.
- \`16_SYNTHETIC_DATA_AND_DEMO_POLICY.md\` — bounded fictional dataset and public demo isolation.
- \`17_TEST_VALIDATION_AND_RELEASE_GATES.md\` — test map/QA/security/concurrency/visual/recovery/performance.
- \`18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md\` — phases, owners, effort, milestones, acceptance, cuts.
- \`19_DEVOPS_OPS_MAINTENANCE_RUNBOOK.md\` — Git, CI, release, restore, cost usage, incidents, upgrades.
- \`20_INDEPENDENT_CRITIQUE_AND_REVISED_DECISIONS.md\` — fault finding, alternative proposals and corrections.
- \`21_RESEARCH_STATUS_AND_OPEN_QUESTIONS.md\` — truthful coverage and next evidence needs.
- \`SOURCES.md\` — **single canonical dated evidence ledger**, tables with URLs, exact claim/evidence strength/source publication and accessed dates; reuse IDs in other docs.
- \`DECISIONS.md\` — numbered, dated decisions/alternatives/cons and approval status.
- \`RISKS.md\` — ranked risks, mitigations, triggers and owners.

If a new complete external research result is material, save the source and finding in the repository, not just the chat. Avoid excessive duplicate narratives or copying full copyrighted articles. Use Markdown + Mermaid; optional machine-readable JSON/CSV and OpenAPI YAML where precision benefits; no PDF/DOCX required at this stage.

Documents must have a status label: DRAFT / EVIDENCE VERIFIED / PROVISIONAL RECOMMENDATION / USER DECISION PENDING. Each has date and current source links. No false claims of interviews, authenticated campus data, tested providers, screenshots not seen, or features not built.

Research in substantive batches, but do not overscope down into dozens of token-expensive one-paragraph mini-sessions. First fully establish evidence, then specifications, architecture, verification, and critique. Keep source coverage and decision register current. When constrained, honestly prioritize highest-risk factual research; don't write placeholders pretending a document is complete.

# F. AUTOMATIC RESEARCH GIT PUBLISHING

The user explicitly authorizes RESEARCH-DOCUMENT Git changes and wants EVERY material verified research finding preserved in GitHub.

Before ANY edit: audit GitHub remote and local status, know current branch and parent, preserve unrelated work. Use a dedicated docs-only feature branch (the current continuity branch may be reused when appropriate; if outdated, create a successor), never write to \`main\` or into O11/O14 implementation branch without authorization. Inspect existing docs and do not overwrite manually changed files.

For each coherent research batch:
1. Save actual complete Markdown/diagram/evidence ledger to source-controlled docs.
2. Cross-check citations and dates, remove unsupported claims.
3. Inspect Git diff, forbid secret/personally identifying data, huge scraped copies or generated junk.
4. Commit meaningful source-backed findings.
5. Push to feature branch without force.
6. Verify \`git rev-parse HEAD\` equals remote SHA from \`git ls-remote origin <branch>\` (or equivalent GitHub API).
7. Create/update a draft PR against correct current implementation parent; never merge by default.
8. If tools cannot push, describe exact blocker and provide ready-to-save documents; don't claim pushed.

Create a concise INDEX in \`docs/research-2.0/README.md\` with each doc status and link, and a change log that distinguishes verified factual updates from changing proposed design.

# G. INDEPENDENT CRITIQUE / RED TEAM BEFORE FINISHING

Attack your own plan on:
- False premise of validated Ateneo demand or official authority.
- Assuming free hosting guaranteed forever and ignoring failure/capacity/account requirements.
- Trying to serve authenticated multi-tenant users with the old public Java 8/Boot 2.1 stack.
- Removing mandatory course components to fit cheap serverless.
- Inadequate organization/member identity and officer turnover.
- Race conditions and time overlap in stock reservations.
- Unbounded record/CPU/storage/log growth on free tiers.
- Backup stored on same failing VM without restore test.
- Public demo writes/shared account leaks.
- Sensitive data and Philippine privacy notices.
- Scalability claims not tied to a measurable quota and workload.
- Overambitious feature scope versus a small complete release.
- Missing source coverage, contradictory dates, stale links, source misquotations.
- Claims of live Twilio delivery or real stakeholder acceptance without evidence.
- No practical maintainer/upgrade/recovery policy after graduation.

Produce revised decisions with explicit acceptance tests; do not only say “consider security.”

# H. RESPONSE AND HANDOFF CONTRACT

Do not ask for interviews or surveys; user cannot provide them now. Do not require a paid service. Do not ask the user to upload videos. Begin with source/repo audit, research current official/public evidence, persist results to the repo, then critique. Proceed through all reachable work in this response/session; no background-work promises.

Return:
- Fresh remote head, branch, PR statuses and source coverage.
- Key dated new evidence, explicit contradictory/unknown items.
- Product/campus taxonomy findings, proposed first-release scope, stakeholder phase map.
- Architecture and $0 deployment alternatives with serious limitations.
- Exact research document list, commit SHA, verified pushed SHA, PR link.
- Acceptance/evidence gaps, decisions for user review, next Codex-planning input.
- Clear statement of what is RESEARCHED vs SPECIFIED vs CODED vs TESTED vs DEPLOYED.
- No implementation or deployment changes.

**REMEMBER:** The user's original academic objective continues here only after validated O14; this new chat is responsible for researching the post-O14 product, preserving exact course requirement traceability, and putting all verified findings in GitHub—not for rewriting the course application without permission.
