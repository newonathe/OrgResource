# COPY-READY CODEX MASTER — A1+A2 COURSE FINAL A BUILD AND REAL ACCEPTANCE

**User execution target:** Free Codex plan, Luna model, highest reasoning setting actually available. **Execution is authorized only when the user pastes this prompt to Codex.** Role = BUILD/TEST/DEBUG/RELEASE PREPARATION; **NO market/cloud/provider/academic outside research, autonomous technology choice or fictional test claims**. This master is a medium-large dependency-coherent **academic-completion** cycle, NOT a whole Extended C implementation and NOT an invitation to launch a cloud host.

## 0 — MISSION AND ABSOLUTE ACCEPTANCE

Finish **OrgResource-Course-Final A** as an actual complete, reproducible local CSCI 180.05 application from the existing O14 baseline. Do not start Extended C or the earlier S1 modern-Jakarta spike. Preserve Java8-era real course tools: Spring Boot 2.1.0.RELEASE, Jersey 2.26, JSON, Spring \`@Autowired\` Controller→Component, five core JPA/Spring Data/Hibernate/MySQL entities, JPQL, literal \`javax.validation\`, Retrofit/Gson outbound HTTP, existing FakeTwilio + real official Twilio REST-compatible transport, scheduled expiry/reminders, 26 original live business routes, runtime create/read/eligible delete of all five, importable Postman, real Resource image upload/store/serve for syllabus CLO9, and all eight connected React Academic Glass screens.

**Do not try to make A a public production server.** Legacy Boot2.1 is local/loopback only; Extended C later has an independently supported-runtime gate and will derive from the exact frozen A revision after separate approval.

**Optimize** correctness, security, accuracy, completeness and maintainability FIRST, then maximum verified working code per Luna/free-tier tokens, tool calls, elapsed time and stored bytes. Prefer cohesive medium-to-large commits, not many tiny coding turns or one unreviewed multi-product rewrite.

## 1 — MANDATORY FRESH GIT AUDIT AND SOURCE PINS

Before changing any file, run:
\`\`\`
git status --short --branch
git remote -v
git fetch origin --prune
git branch -vv
git ls-remote --heads origin
git log -12 --oneline --decorate
\`\`\`
Inspect current PR parents and latest relevant GitHub CI. **Do not reset, clean, force-push, auto-stash or overwrite dirty user work.** If a worktree is dirty, preserve it and use a safe isolated worktree/branch after verifying paths; stop destructive actions. Do not choose old \`main\` as implementation source.

**Last observed O14 source:** \`feat/o14-academic-final-acceptance-2026-11-15\` SHA \`2dd336ed39cecd417a8bce3388b303e688e95dee\` PR #8. Re-pin newest legitimate O14 if changed. Branch e.g. \`feat/course-final-a1-a2-2026-11-16\` from that source. Never modify O14 ref directly.

**Documentation must be read from separate docs ref, NOT merged wholesale into code:**
\`\`\`
git show origin/docs/course-a-final-spec-build-handoff-2026-11-16:AGENTS.md
git show origin/docs/course-a-final-spec-build-handoff-2026-11-16:docs/academic-final/07_COURSE_A_EXECUTABLE_SPEC_FREEZE_2026-11-16.md
\`\`\`
**Only as needed for the affected change/test**, use narrow path-specific `git show`, file search and source slices from:
- `docs/academic-final/02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md` §1–4 when editing a DTO/route; the entire file need not be recopied into context.
- `docs/academic-final/03_TEST_POSTMAN_DEMO_AND_SUBMISSION.md` §2–4 when writing acceptance tests.
- `docs/academic-final/05_EERD_REPOSITORIES_AND_METHOD_SEQUENCES.md` for changed entity/repository/Component methods.
- `docs/academic-final/01_WRITTEN_REQUIREMENTS_AND_COMPLIANCE.md` for a disputed course requirement.
- `docs/academic-final/04_AGENT_AND_EFFICIENCY_POLICY.md` for actual delegation/effort rules only.

The 07 document is the concise active **execution contract**. Do not fully ingest the 80-file research tree, seven academic documents or 319k historic master at start merely to establish context. Retrieve an exact additional section only when a concrete contract ambiguity or failing test warrants it.

Read the original course PDFs/proposal from the repository's \`ChatGPT-Project-Pack/UPLOAD\` when available and necessary. Cite exact paths but do not repeatedly ingest massive history. Read current O14 source, \`backend/pom.xml\`, Jersey registration, five entities/controllers/repositories, relevant Components, \`application.properties\` and test profile, FakeTwilio/Reminder code, relevant tests, Maven/React release scripts and Postman files.

**Hierarchy:** Original 3-page assignment + 9-page syllabus > authentic historical 12-page proposal for project-specific behavior > approved current O14 code amendments > concise course-A spec 07 > historical research. CP44/46 two-edition user decision overrides old one-modern-runtime-first agent plans.

## 2 — INTELLIGENT OPTIONAL DELEGATION (ACTUAL CAPABILITIES ONLY)

**If free Codex exposes legitimate subagent delegation:** primary Luna agent is the sole integrator/owner of pom.xml, \`AGENTS.md\`, configs, Git PR and release evidence. At most two nonoverlapping helpers:
- **Helper H1 (fast/light, low-medium)**: read-only audit exact 26 source routes vs Postman, test gap IDs, report 1 table with path/line/test; NO edits or independent architecture selection.
- **Helper H2 (balanced, medium)**: optionally implement isolated Resource image endpoint + focused tests **only if** its files are exclusively assigned and the primary does NOT concurrently edit them; return changed paths and exact test outputs.
- **Primary (highest available reasoning)**: own official Twilio transport, scheduling correctness, historical MySQL clock/JPQL flake, integrated acceptance, changes to POM, final validation, Git.
- Optional final independent READ-ONLY critique on a committed diff only if actual capability and value exceed setup/token overhead.

**If no subagent feature exists:** do all tasks sequentially with the same Luna agent; do not simulate spawned workers or spend effort trying to install tools. For every real helper give exact starting SHA, owned paths, forbidden edits, source contract, test command, output format and stop conditions. Never have two writers to the same source/config/DB.

## 3 — BUILD TASK A1.1: GENUINE RESOURCE IMAGE HTTP + MYSQL

Preserve the exact original 26 paths. Add precisely the 2 additional course-image operations:

\`POST /api/resources/{id}/image\`:
- multipart/form-data with required file field; no trusted filename or arbitrary URL.
- Read at most 2,097,153 bytes, reject empty/oversize and mismatch between allowed content type and JPEG/PNG magic. Reject SVG/script/unrecognized/truncated content; no filesystem path or remote fetch.
- Store optional bytes and validated MIME on the existing Resource row (no sixth entity); MySQL column must support >= 2 MiB, test beyond 64 KiB. Transactional update, missing ID 404, invalid media/size 400/413, controlled safe JSON error.
- Return 200 JSON \`{resourceId,contentType,sizeBytes,sha256}\` (sha256 may be derived without stored duplicate).
\`GET /api/resources/{id}/image\`:
- 200 exact binary bytes, matching MIME; 404 missing Resource or image. No body leakage in logs; no cache of private data.
- Use actual Jersey 2.26 multipart feature/registration and compatible dependency if needed. Don't create another image business entity, object storage, paid CDN or unsolicited image editor.

Tests: valid JPEG/PNG, >64KiB PNG, max-size boundary, missing file, invalid magic/MIME, oversize, nonexistent Resource, overwrite, exact hash after GET, real MySQL persistence after restart. Ensure \`/api/resources\` JSON existing DTO stays stable.

**Efficiency:** one small controller path extension + one Component + nullable Resource columns + focused integration tests. No unnecessary general-purpose media service.

## 4 — BUILD TASK A1.2: ACTUAL TWILIO PROTOCOL TRANSPORT, SAFE BY DEFAULT

Keep existing \`FakeTwilioTransport\` loopback Retrofit/Gson behavior. Add independent \`TwilioRestTransport\` using Retrofit+Gson for official Messages REST contract:
- \`POST /2010-04-01/Accounts/{AccountSid}/Messages.json\`, form \`From,To,Body\`, HTTP Basic auth, configured base URL/credentials from environment ONLY, bounded connect/read/write timeout.
- Parse success HTTP response/SID and error JSON/status; redact credentials/phone/body from logs.
- Do not send to public Twilio by default. Keep existing DISABLED mode unchanged and make live mode an explicit opt-in guarded config with no real credentials in repository. Test with a **separate loopback protocol server**, not the FakeTwilio shape and not any real Twilio account.
- On connection timeout/lost body/ambiguous result, mark UNKNOWN and do not auto-retry or falsely label delivered. Enforce consent/issued REUSABLE/due-window claim/one-time scheduling safeguards of current code; no HTTP inside DB transaction.
- Protocol test asserts exact method/path/form/Basic auth header under fictional test credentials, accepted and rejected responses, malformed JSON, timeout and ambiguous outcome. Existing FakeTwilio tests must remain green.

No real phone numbers, no provider account registration, paid test, virtual phone use or SMS transmission. \`PROVIDER_TEST_BLOCKED\` is honest if no owner-authorized account exists; a functional disabled real transport with local contract tests still satisfies this implementation scope.

## 5 — BUILD TASK A1.3: INVESTIGATE ACTUAL MYSQL AGGREGATION TEST FLAKE

One historical exact-SHA CI run on \`ResourceListAggregationMySqlTest.oneClockSampleAndOneGroupedQueryProduceExactAvailabilityAtProjectEndBoundary\` reported expected=5, actual=8; another same-source run passed. Root cause UNKNOWN. Do NOT change expected5 to8, delete/disable/quarantine the test or hide it with retries.

- Execute focused test repeatedly in isolated \`orgresource_test\`, then one full suite; capture enough sanitized MySQL/JPQL/Clock and transaction context to discriminate active APPROVED q3, ISSUED q2, expired APPROVED q4, single sampled PHT now.
- Add independent DB-row/aggregate assertions BEFORE mapping returned views, with JPA flush/clear where transaction boundaries justify it; confirm query bound timestamps and status columns.
- Fix a reproduced real source/testing defect minimally; write a regression test verifying it. If it never reproduces, do not fabricate root cause; record repeated-run count and remaining intermittent risk, rather than weakening acceptance.
- Concurrency/rollback must remain correct and no incorrect double-deduction.

## 6 — A1 INTEGRATION AND SOURCE REGRESSION

Keep \`Department,Project,Resource,ResourceRequirement,Allocation\` exactly as real core JPA types. Preserve original route/JSON/HTTP and source component methods as documented. Protect O14 redacted phone, \`expectedTotalQuantity\` CAS, PHT times, effective holds, protected delete history, scheduler and backup local simulation. Use version-compatible Maven dependency changes ONLY for above features.

Do not add authentication/multi-organization/migrations/ledger/C public API during A. Those belong to Extended C after A frozen. No obsolete Boot2 internet exposure.

## 7 — A2 EXECUTE COMPLETE ACADEMIC ACCEPTANCE

A passing test report MUST be grounded in actual actions and fresh database state, not prior O14 55/15/5/96 historical counts.

**7.1 Java/MySQL:** JDK8 Maven \`clean verify\` with isolated MySQL; inspect five real tables/FKs and image columns, Javax DTO validation positive/negative, Repository custom JPQL and rollback, all core CRUD safe deletes. Validate one clock sample and effective ended holds; expiry @Scheduled physically invoked and repeat-safe; reminder scheduler runs in simulator mode and disabled default works. Track actual test count and failures.

**7.2 Original 26 routes:** Import and actually run Postman/Newman with dynamic IDs against the real packaged Java/MySQL server:
D01-D04, P01-P04, R01-R05, Q01-Q04, A01-A09; verify GET/POST status, JSON fields, actual server Component/database mutations, list filters, missing/invalid IDs, duplicate names/pairs, stale CAS, protected 409, consent redaction/revocation and no accidental replays.
Run **both additional IMAGE routes**, so **28 distinct A operations** must exist in the complete reference collection and executable acceptance (26 original + two image), each with tested success and negative cases. Existing collections can be extended; do not replace with fake examples or hardcoded stale IDs. Test JSON on business APIs and binary content for image GET.

**7.3 Inventory:** Reusable T10 q4 Q6 PENDING10/0 → APPROVED6/4 → ISSUED6/4 → RETURNED10/4; consumable T100 q30 Q40 PENDING100/0 → APPROVED70/30 → CONSUMED T70/available70/covered30. Two conflicting q4 approvals vs stock5 exactly one success. No negative quantities, double use or historical deletion; expired APPROVED q4 at exact end time no H, overdue ISSUED remains I.

**7.4 React:** existing eight-screen Academic Glass UI connected to real Java/MySQL. Typecheck, unit tests, lint, production build and actual Playwright; desktop/mobile light/dark and keyboard/reduced-motion. If a minimal image preview fits existing Resource page without regressions, add it; otherwise image works via Postman and do not invent a mock UI.

**7.5 Portable release:** bundle React in executable Java JAR, test HTTP \`/\`, static assets and all five list APIs; genuine safe first run without protected developer private file, clean-source ZIP extraction/rebuild, restart persistence, Java/Eclipse source export. Do not commit binaries or secrets into source; record SHA256 and reproducible script. Eclipse GUI import if unavailable -> NOT_RUN, not fake PASS.

**7.6 Twilio:** actual FakeTwilio HTTP and official Messages protocol against separate loopback server, no provider send; report SIMULATED vs STUB_PROTOCOL_ACCEPTED vs official provider acceptance vs carrier delivery accurately. E.164/consent/suppression/UNKNOWN no retry tested.

**7.7 Student-only course duties:** prepare factual demonstration checklist, correct as-built Word proposal outline tied to exact final operation names/components/entities/repositories, but do not invent group roster, peer reviews, student signature, video, instructor approval or Canvas upload. Original printed due 2026-12-05; no invented extension.

## 8 — QA CHECKPOINTS, SCOPE AND TOKEN BUDGET

One coherent coding cycle with three internal checkpoints, not 20 micro-prompts:
- **C1 (bounded)** image + tests → focused Maven gate.
- **C2 (bounded)** Twilio protocol + tests + intermittent aggregation investigation → focused Maven/MySQL.
- **C3 (integration)** updated Postman + end-to-end acceptance + release packaging + authoritative final report.

Work on C1/C2 in whichever sequence minimizes shared-file changes. Keep one writer for shared POM and configs. When tests expose defects, fix in relevant area and rerun focused tests; one full Maven+React+Newman+Playwright gate near end. No extra docs research phase; no custom infrastructure or provider experiments. Reuse existing scripts and reports. After each checkpoint, preserve a coherent tested commit if useful; final Git push mandatory even when BLOCKED by genuine incompatible dependency.

**Security and quality review:** media size/MIME, SQL constraints, non-idempotent response-loss safety, no real secret/phone logging, no public listener of unsupported stack, no destructive schema reset. No source-only fake test success. Prefer realistic code/test evidence to verbose generated reports.

If fundamental Java8/Jersey dependency or MySQL incompatibility remains after focused diagnostic effort, STOP dependent coding, preserve safe changes, report exact blocker and test evidence; do not silently migrate to Jakarta.

## 9 — GIT COMMIT, PUSH, PR

Before edits audit current branch/remote/status. Branch from latest verified O14 implementation; do NOT cherry-pick entire research/docs branch into running application. Read contracts via \`git show origin/docs/course-a-final-spec-build-handoff-2026-11-16:path\`.

At completion:
\`\`\`
git diff --check
git status --short
git diff --stat
\`\`\`
Inspect actual changes and potential credentials; stage ALL legitimate A1/A2 source/tests/Postman/scripts/concise evidence, exclude environment credentials/DB dumps/node_modules/target/dist/screenshots en masse/other user files. Commit coherent changes and push without force. Verify local HEAD equals remote \`git ls-remote origin <feature-branch>\`. Open/update feature PR with **O14 feature branch as base unless legitimately merged**. No auto main merge, tag, source deletion, local Windows folder reset, live SMS, account or Canvas action.

Create \`evidence/COURSE-A/A1_A2_ACCEPTANCE_REPORT.md\` containing:
- Base SHA/doc SHA/feature SHA and exact remote/PR ancestry.
- Implemented code paths and full 26+2 operation count, original vs added distinction.
- Test matrix with commands/env/JDK/Node/MySQL versions, actual counts, PASS/FAIL/BLOCKED/NOT_RUN and database postconditions.
- Image actual binary/DB test hashes; Twilio FakeTwilio/stub/provider tier; actual @Scheduled invocation.
- Flaky MySQL investigation actual observations and residual risk.
- Java8, Postman/Newman, React/Playwright, clean-zip, JAR/startup evidence.
- Course authority/acceptance trace (assignment pp1–3, syllabus CLO1–CLO10, original proposal and O14 amendments).
- Security/privacy/storage impacts, reproducibility/portability and remaining A3 release blockers.
- Exact local SHA, verified pushed SHA and PR URL.

## 10 — FINAL VERDICT AND STOP

Use exactly one:
- **A1+A2 VERIFIED COMPLETE — READY FOR SEPARATE A3 IMMUTABLE FREEZE REVIEW**
- **A1+A2 CONDITIONAL — LIST EXACT UNPASSED/UNTESTED ACADEMIC GATES**
- **A1+A2 BLOCKED — MATERIAL SOURCE/BUILD/SECURITY FAILURE**

**Do not automatically freeze/tag Release A, build Extended C, deploy, send real SMS, merge main or submit Canvas.** A3 requires separately reviewed acceptance evidence and owner approval. Source preservation and honest measurement are mandatory. Start implementing A1/A2 now.
