# OrgResource — Mandatory continuous completeness and independent-evidence standard

**Policy edition:** 2026-11-19 (PHT). **Status:** Proposed repository-wide quality standard, subject to owner review. This standard defines how to *work and prove work*; it does not certify any existing code, research, PR, archive, test, release, or academic submission.

## 0. Binding intent and achievable claim

Apply this standard to **every** subsequent OrgResource source/research audit, plan, correction, Codex prompt, implementation phase, PR, CI repair, migration, integration, packaging, release, and post-release review. High rigor is mandatory on every intermediate pass, not reserved for the final pass. Exhaustiveness is an **auditable pursuit**, not a claim of omniscience: no agent may say "no gaps", "all files reviewed", "100% proven", "production ready", or "passed" absent a bounded definition, a complete coverage register, and qualifying independently inspectable evidence. Unknowns must be explicitly marked. Do not fabricate verification, mark intended tests as executed, or reinterpret an unavailable environment as success.

**Non-negotiable method:** source intake → scope/authority reconciliation → inventory/coverage register → Socratic falsification → explicit decisions and precise contract → smallest feasible coherent implementation → real negative/positive tests → independent second-pass critique → CI/artifact/source evidence → owner gate → next-phase handoff. Re-run the loop for every changed behavior, discovered defect, contradiction, or failed acceptance criterion.

**Stop rules:** Never modify frozen Course Final A `7f8c0746da18fd54b47f430ac1435ac36e605d67`, silently alter existing Flyway migration checksums, assume approval for spending/hosting/live SMS, merge/main/force push, use real personal data, fabricate academic proof, or begin an unapproved next phase. Protect the December 5, 2026 printed academic deadline; verify current Canvas separately.

## 1. Source authority and complete coverage

1. Freshly inspect Git remote, clean worktree, every relevant branch/PR ancestor, exact HEAD/base SHAs, CI runs, actual patch and status. The old `main` and historical research drafts are not automatic implementation bases.
2. Treat current CSCI 180.05 assignment and syllabus as academic authority; current Canvas updates need student verification. Original proposal is a project-design commitment and history. Approved user decisions and current code constrain the engineering plan; earlier drafts are reviewable, not irrevocably binding.
3. Inspect attached source map, academic and product specs, course corpus/slides/labs/example code, the relevant audio transcripts, and source manifests **as needed for the exact claim**. Source ZIPs are inventories, not proof every entry was read. Unattached video frames remain unseen. Never claim watched videos, user interviews, real inventory/demand, provider sends, academic approval, or measured impact without evidence.
4. Build a source-coverage register containing every relevant original path, Git blob/hash or attachment identity, type, size/lines, reason for inclusion, examination level (FULL / RELEVANT RANGES / PATCH / METADATA / UNREAD / INACCESSIBLE), specific ranges reviewed, cross-references, and open questions. Mark archived/historical/irrelevant files with a concrete rationale; do not pretend all are deeply reviewed.
5. For EVERY changed PR file, enumerate its full path, change ranges, interfaces/data fields/functions/queries/permissions, affected callers/tests/migrations, authority, risk, and KEEP/FIX/DEFER verdict. Also audit unchanged **high-risk transitive dependencies** and record the chosen closure boundary. A PR-file list alone is not a file-by-file semantic audit.
6. Maintain a conflict and supersession register for entity counts, 26 business + 2 image academic operations, versions, DTOs, Javax/Jakarta, original vs v2, stock states/formulas, authorization, reminders, project-time semantics, endpoints, academic dates, releases, and research planning. Resolve by evidence; unresolved material conflicts block claiming completion.

## 2. Every-claim traceability

For each material requirement or decision, record:
- ID; classification: **COURSE REQUIREMENT / SYLLABUS FACT / VERIFIED SOURCE / VERIFIED EXTERNAL FACT / USER DECISION / DESIGN RECOMMENDATION / ASSUMPTION / OPEN QUESTION**.
- Exact authority citation (file:line, page, source URL/date, commit SHA) and any conflicting source.
- Contract: target edition, endpoint/method, JSON field/type, entity/column, permission, state machine, calculation, clock/time zone, error status/code, boundary, idempotency, logging/retention.
- Implementation links: Java controller→component→service/repository→SQL/migration→UI/automation, as applicable.
- Acceptance: test ID; fixture and execution command; expected status, response, DB postcondition, denial, rollback/retry/restart behavior; actual observed receipt/log/CI SHA.
- Verdict: VERIFIED / REPORTED_ONLY / SOURCE_ONLY / TEST_FAILED / NOT_RUN / BLOCKED / DEFERRED_WITH_APPROVAL (no generic PASS when a required row remains unproven).
- Owner, priority (P0/P1/P2), dependency, next exact action, and required human consent.

The canonical matricies must cover course compliance, all academic operations, modern Extended routes, five-domain data, role×endpoint×organization, all mutations, data lifecycle/retention, consent/phone, build/release, and user-visible workflows. Reconcile every interface and every supported role; don't infer data ownership from a name.

## 3. Mandatory Socratic and adversarial challenges

Ask, answer, and **try to falsify** for every consequential changed behavior:
1. **Authority:** Why does it exist? Course requirement, verified user decision, or optional research? What alternative has a smaller risk/scope?
2. **Correctness:** What observable precondition/postcondition and mathematical invariant define success? Does the real code, not its comment, satisfy them?
3. **Identity/tenancy:** What does anonymous/MEMBER/LEAD/CUSTODIAN/ADMIN/OWNER see or change before/after demotion, suspension, organization switch, stale session, and foreign ID?
4. **Validation:** How do missing/null/empty/oversized/duplicate/malformed/unknown fields, Unicode, date boundaries, image bytes and numerical overflow fail?
5. **Failure:** What if network drops before/after commit, response is delayed or duplicated, provider returns 200 but rejects, JVM crashes, MySQL deadlocks, or credentials/keys disappear?
6. **Concurrency:** What if two requests mutate one resource at once; an idempotency receipt expires; a different actor replays; a scheduler races with consent revocation?
7. **Persistence:** What survives reload, restart, migration, restore, key rotation and backup corruption? How are ledger total/holds/issued, audit immutability and history protected?
8. **Integration:** Does actual MySQL/HTTP/React/Jersey/Retrofit/Postman work from the packaged executable, not just mocks or a disconnected page?
9. **Security/privacy:** How could roles, broken object-level authorization, injection, spreadsheet formulas, image parsing, logs, cookies, CSRF, redirects, secrets, exports and backups leak or alter data?
10. **Usability/accessibility:** Are pending/error/empty/stale/partial states truthful? Can keyboard/mobile/reduced-motion/screen-reader users finish core workflows without hidden controls?
11. **Compatibility:** Does Extended still actually execute original course semantics while frozen Course A stays byte/hash intact? Are all version, dependency and migration assumptions proven?
12. **Evidence:** Could a green job be a false positive because the relevant test was skipped, ordered, mocked, ran against the wrong port, shared fixtures, or checked only a snapshot?
13. **Operations/cost:** Can a first-time operator install, bootstrap, back up, restore and restart without private developer setup? Is public deployment, paid service or institutional integration falsely implied?
14. **Adversarial re-review:** What strongest independent objection remains, what minimal reproduction could disprove our conclusion, and which assumption is weakest?

**For each P0/P1**, reproduce a failure where safe, then add a regression with an asserted behavioral/database postcondition. Design-specific P2 can be deferred only with explicit scope and why it cannot invalidate a mandatory outcome.

## 4. Required validation matrix, not optional ritual

- Frozen Course A separate Java 8/Maven, original five JPA entities and constraints, Jersey + Spring `@Autowired` component forwarding, Javax Validation, real MySQL/JPA/JPQL, Retrofit/Gson/Twilio-safe protocol and scheduler, five entities/26 business+2 image route checks, exact binary image verification, Postman, UI and independent packaging.
- Extended Java 21 real MySQL 8 clean install/upgrade, Flyway checksum verification, tenant-scoped R/W, role/demotion/foreign-resource denials, CSRF/cookie/session aging, password/recovery/invitation, transaction rollback and conservation, durable stock fact/replay after receipt prune and restart, expiry and consent races, exact images, bounded pagination.
- React unit/typecheck/lint/build and real packaged Playwright at desktop/tablet/mobile, keyboard/reduced motion, all eight academic pages, auth/governance/stock, error/loading/partial/uncertain-write behavior, organization switch and browser restart.
- 28 academic operations exercised against Extended JAR with real MySQL; public v2 operations individually matched to actual implementation and meaningful negative cases; Newman and browser checks.
- Offline first-run with restricted migration/runtime accounts, no previously prepared fixtures; authenticated backup and **real restore** including wrong key/tamper/partial import and independent process/storage boundary; report off-host NOT_RUN unless performed.
- Source ZIP/JAR provenance/manifest/hash, no secrets, clean extraction/rebuild, CI job logs and SHA, exact user-accessible artifact paths. A green CI run is one observation, not a system-wide certificate.
- Dependency and primary-source security research dated against exact lockfiles; accepted advisories and missing patches remain risks, not silently resolved.
- Academic recording, instructor access, Certificate of Authorship/outside-assistance acknowledgment, peer reviews if applicable, Canvas status and receipts are STUDENT_ONLY; never invent.

If test execution is impossible, report why, identify precise commands to run later, and mark the gate NOT_RUN or BLOCKED. Do not weaken a gate, delete an assertion, rename a test, use a mock as proof of real MySQL, or change fixtures merely to make red green.

## 5. How to plan and implement each phase

A phase plan must have an explicit scope boundary, dependency graph, full task backlog, named files/routes/DB fields/constraints, nonfunctional requirements, alternatives/tradeoffs, threat and privacy model, exact versions checked against supplied examples, errors, test fixtures, reproducible commands, estimate and schedule priority, risk register, contingency cuts, rollback, deliverable paths, and independent acceptance checklist. Include the **minimal course-compliant route** and separately the **authorized Extended optional route**.

A prompt to Codex must state exact repo/ref/branch and immutable source, read-first sources and registers, allowed vs forbidden edits/actions, deterministic sequence with STOP conditions, detailed interface contracts, required positive/negative/crash tests, exact report output, Git restrictions, and no unauthorized next phase. Do not promise complete implementation beyond actual tool/time/environment limits.

Implement only smallest justifiable coherent work with independent tests and evidence at each gate. Prioritize the academic deadline and essential data/security before optional UX, cloud/host, elaborate infrastructure or unverified institutional integrations.

## 6. Mandatory per-file and phase report format

For **each changed path** record:
`path | blob SHA | reviewed ranges | role/authority | old→new | dependencies | attack/failure cases | tests+observed results | remaining gaps | KEEP/FIX/DEFER`.

For **each phase** create a `PHASE_REPORT.md` and machine-checkable coverage registers, with:
1. exact source, parent SHA, branch, PR, CI runs, release/source hashes, protected A Git tree comparisons;
2. scope/assumptions/current decisions/conflicts and precise contract;
3. source inventory and per-file review depth (including not-read);
4. requirement→code→test→evidence matrix;
5. state/data/permissions/endpoint/migration/role invariants;
6. Socratic falsification questions and adversarial tests/results;
7. positive/negative/concurrency/restart/restore/API/browser/security test receipts; commands, exit codes, environment, skip counts;
8. external sources and retrieval dates for changeable compatibility, vulnerability, provider and hosting claims;
9. ranked P0/P1/P2 defects, deferrals, exact blockers, owner decisions, deadlines and stop conditions;
10. independent reviewer critique of the report itself (counterexamples and false-positive review);
11. complete next-phase handoff with copy-ready Codex prompt and acceptance checks;
12. final verdict and what evidence would change it.

Use explicitly bounded classifications:
- **VERIFIED:** all **defined mandatory** requirements for this phase have direct evidence on its exact SHA, with no blocking failures; it does not prove global perfection.
- **CONDITIONAL:** non-blocking accepted deferrals remain; name owner and deadline; never describe missing mandatory behavior as verified.
- **BLOCKED:** material P0/P1, unexecuted mandatory gate, or missing authorization that prevents phase acceptance.
- **NOT_RUN:** the check was not run; must be visible even if other checks pass.

## 7. Governance, propagation, and future-cycle enforcement

1. The repository root `AGENTS.md` links this document. Every Codex pass explicitly reads it before designing or editing and must use it as a preflight and postflight checklist.
2. Every future implementation prompt cites this policy and states, "Do not reduce audit depth or acceptance criteria to satisfy time/token limits; complete all safe possible work and mark anything blocked or not run."
3. If a later instruction attempts to declare unconditional completeness without receipts, **reject that claim** and report the specific missing evidence. Owner approval may re-scope **optional** requirements but cannot manufacture tests, legal/policy approval or completed student actions.
4. Existing historical phase reports remain historical; this policy is prospective and does not retroactively label old work as exhaustively reviewed.
5. This quality gate is independent of GitHub branch protection: until adopted and enforced by checked-in CI/review rules, it is a human/agent instruction, **not** an automatic GitHub server-side block.
