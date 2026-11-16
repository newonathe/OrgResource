# OrgResource — Codex A3 Master Prompt: Independent Course A Release Freeze

**Dated:** 2026-11-17 Asia/Manila · **Task owner:** Codex (BUILD / TEST / DEBUG / PACKAGE ONLY) · **Model:** user's free Luna Codex, highest reasoning actually available · **Status:** authorized when owner sends this exact prompt to Codex; no execution claimed by this file.

## Current measured baseline and authorization boundary

Last fresh GitHub audit: private \`newonathe/OrgResource\`, latest Course A1+A2 branch \`feat/course-final-a1-a2-2026-11-17\` SHA \`af2a405ebe893268c54f463ec37da6a37cf6b917\`, draft PR #11 based on \`feat/o14-academic-final-acceptance-2026-11-15\` SHA \`2dd336ed39cecd417a8bce3388b303e688e95dee\`. \`main\` still old O1. Latest Java 8/MySQL CI \`43947966407\` (plus \`43947956821\`) passed **61 tests, 0 failures/errors/skips** and Java JAR smoke; React CI passed. Local author report \`evidence/COURSE-A/A1_A2_ACCEPTANCE_REPORT.md\` reports Newman 100 requests/151 assertions, CLI Playwright six tests, clean source ZIP/extraction and restart persistence, but those local actions were **not independently rerun in planning chat**. Core actual source has 5 \`@Entity\` classes, 25 proposal routes + A09, and 2 Resource image GET/POST; standalone 28-route Postman v2.1 collection exists. Real Twilio protocol stub tested; no provider account/send/Canvas acceptance.

**MISSION:** Do the final bounded **A3 technical acceptance, as-built academic documentation, clean reproducible packaging, frozen source identity and owner-ready release handoff** for the existing local course edition. **NO further general research; NO Extended C coding; NO modern/Jakarta migration; NO public hosting; NO real SMS; NO Canvas submission.** A3 must not accidentally transform a feature-branch CI success into a false instructor approval.

**Critical source:** Original assignment/syllabus and historical proposal from repository \`ChatGPT-Project-Pack/UPLOAD/\`; academic canonical contract under docs ref \`docs/course-a-final-spec-build-handoff-2026-11-16\`, particularly \`AGENTS.md\`, \`docs/academic-final/07_COURSE_A_EXECUTABLE_SPEC_FREEZE_2026-11-16.md\`, \`01_WRITTEN_REQUIREMENTS_AND_COMPLIANCE.md\`, \`02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md\`, \`05_EERD_REPOSITORIES_AND_METHOD_SEQUENCES.md\`, \`03_TEST_POSTMAN_DEMO_AND_SUBMISSION.md\`. Read *only necessary sections* after initial 07 intake. User's latest decision: **Course Final A first, independently runnable and frozen, Extended C derived later from A exact SHA and independently re-proving the course functions**.

## 0. Fresh audit and safe Git branch — mandatory

BEFORE editing:
\`\`\`
git status --short --branch
git remote -v
git fetch origin --prune
git branch -vv
git ls-remote --heads origin
git log -12 --oneline --decorate
\`\`\`
Review PR #11 head/base and current CI. If PR11 has progressed, use newest legitimate source, not the historical SHA here. Do not start from old \`main\`, PR10 docs, or O14 alone.

Read repository root \`AGENTS.md\` plus the NEW academic docs \`AGENTS.md\` and 07 via \`git show origin/docs/course-a-final-spec-build-handoff-2026-11-16:<path>\`. Source user/academic A-first instructions supersede O14-era “interim” wording. Preserve all unrelated changes; if the worktree is dirty, isolate safely in a sibling worktree or stop only the destructive operation. NEVER reset --hard, git clean, force-push, merge main, auto-stash, drop/truncate a non-disposable DB or alter another user branch.

Create correctly based feature branch, e.g. \`feat/course-final-a3-freeze-2026-11-17\`, from the latest PR11 implementation head. Feature PR base must be PR11 unless actually merged. Keep the five domain tables and original 28 current A routes unchanged except a proven release-blocking defect. **A3 is not permission for redesign.**

## 1. Verify the actual acceptance baseline (fail-fast, not full reimplementation)

Record original A1+A2 implementation SHA, CI job URLs, evidence/report lineage, local toolchain and real external package path. DO NOT claim any ZIP/JAR in a user's Windows directory is visible merely because it is mentioned in a Git report.

Mechanically confirm:
- Exactly five JPA core business entities: Department, Project, Resource, ResourceRequirement, Allocation.
- Original 26 Jersey operations (D01–D04, P01–P04, R01–R05, Q01–Q04, A01–A09) and **two added image endpoints**, no API contract regression.
- Controller \`@Autowired\` → Spring Component → JPA/Spring Data/Hibernate/MySQL; literal \`javax.validation\`, JPQL, scheduling, Retrofit/Gson.
- Image \`POST /api/resources/{id}/image\` accepts bounded PNG/JPEG multipart and stores \`MEDIUMBLOB\`; binary GET exact bytes/content-type and safe headers; no extra JPA entity.
- Real Twilio protocol transport exists and is **disabled by default**; FakeTwilio remains; no provider/carrier claims.
- A1+A2 report's former MySQL aggregation expected=5/observed=8 intermittent risk remains **root cause unproven** unless a deterministic new failure is reproduced and fixed. Do not hide it with assertion change, skip or broad retry.
- Server remains bound to localhost only. No real data/accounts/phone numbers.

Run targeted tests only when a new suspicion/changed source warrants them; avoid repeating 61-test builds after every documentation edit. After the final committed source is assembled, execute one complete relevant validation gate.

## 2. Correct academic as-built evidence and deliverable completeness

Prepare **ONE authoritative Course A as-built technical proposal/addendum** based on exact actual source, not an invented faculty submission. Preserve the original 12-page historical proposal intact.

Include:
- Title, real original proposal's listed group member(s) as **UNCONFIRMED PENDING STUDENT REVIEW** if current actual roster is unknown; no invented contributors, peer reviews or approval.
- Concise problem hypothesis/use case and fictional demo disclaimer.
- Five core entity justifications and **actual field dictionary** (types, nullability, validation, FKs/uniques), including Resource nullable image columns; exact five-table EERD/cardinalities.
- All **28** current Jersey operations: method, URL, path/query/body, success JSON/status (image GET = binary, not JSON), all meaningful failures, forwarded Component method. Differentiate the original 25, A09 amendment and 2 CLO9 user-selected additions.
- Component jobs/ordered method sequences, side effects, transaction/clock/stock invariants, scheduled expiry and reminder, two distinct Retrofit transports, safety defaults.
- Actual custom Spring Data repository/JPQL signatures and purpose, not conjectured future C schema/ledger.
- Original proposal vs as-built amendments: effective approval-hold expiry, \`expectedTotalQuantity\` stale-write CAS, consent phone redaction and A09, structured errors, no unsupported claim of real Twilio delivery.
- Java 8/Jersey2/Javax/Spring Boot2.1 MySQL versioned course stack, setup, Postman and real runtime evidence.
- Student review and AI/outside assistance acknowledgment area, clearly unsigned/not substituted for personal COA.

**Generate a professional, readable DOCX as an A3 artifact** from canonical source Markdown or a minimal repeatable local generator; do not overwrite or backdate the original proposal. Keep one editable canonical Markdown source in Git and the derived DOCX in release deliverables; avoid a second enormous manually duplicated master document. If DOCX generation fails, report exact tool blocker and leave complete reviewed canonical content, not a fake DOCX. Do not self-certify student authorship.

Update \`docs/compliance/FINAL_COURSE_COMPLIANCE_MATRIX.md\`, release/demo guide and Postman README ONLY where facts changed. Fix stale 26/28 counts or commit references. Retain source-grounded distinction between CR (assignment), SF (syllabus CLO), PD (historical proposal), user extras and MANUAL student acts. Student Canvas check/instructor response still UNVERIFIED.

## 3. Fresh standalone Course A acceptance (complete once after source finalization)

Only against isolated local \`orgresource_test\` or a newly created **disposable isolated MySQL instance/schema**, not an existing personal/shared DB. No destructive data reset of any other schema.

**Java/MySQL:** exact Java 8 Maven \`-Pbundle-react-ui clean verify\`; report actual pass/fail/skip counts, real five tables, \`MEDIUMBLOB\`, FK validation and JPQL, stale-CAS/protected deletions, rollback, expired APPROVED H=0 and issued custody I, concurrent approval refusal, real executed schedulers and disabled SMS default. If the prior flake recurs, isolate source/clock/DB state, correct the defect minimally and retest. Do not change oracle to fit failure.

**HTTP/Postman:** run existing dynamic-ID suites against the exact candidate packaged server. Verify actual importable **28-operation current reference collection**, route parity to Controller methods, request/response contracts, negative codes and non-idempotent failure recovery. Note the 28-route reference collection and the 100-request multi-collection acceptance suite are different assets; do not claim all 28 endpoints exercised just because the 28-item JSON parses—map the actual executed paths and add cases for any real coverage holes. Image GET response must match byte-for-byte, not be mistaken for JSON.

**Frontend:** actual React typecheck, tests, lint, build and CLI Playwright against packaged Java/MySQL; eight screens, Academic Glass light/dark/responsive, forms, keyboard/reduced-motion where covered; clearly identify any manual visual/independent browser test NOT_RUN. No redesign or styling expansion.

**Portable first run:** verify from *fresh extraction* and (where feasible) empty new **disposable** MySQL database account/schema—not only the developer's existing protected test schema. Reuse existing \`scripts/start-portable-demo.ps1\`, \`scripts/package-release.ps1\`, Maven Wrapper and README; avoid second packaging toolchain. Record how Maven, Eclipse files, Node/React and DB settings are configured. If a genuine second machine or Eclipse GUI isn't accessible, mark them NOT_RUN rather than inventing proof.

**No live Twilio:** separately verify FakeTwilio and official Messages REST adapter against loopback stub; report \`SIMULATED\` vs locally stubbed \`PROVIDER_ACCEPTED\` vs real official provider acceptance/delivery. No authorized real account means provider test BLOCKED; not a software-unit failure.

If a substantive defect appears, fix only that defect in the A3 branch, write regression test, rerun the affected tests and full final gate. Never weaken/delete tests, assert no problem exists without actual evidence, or install unrelated new stack.

## 4. THE IMMUTABLE SOURCE/ARTIFACT CONSISTENCY PROTOCOL — IMPORTANT

Avoid circular “report changes ZIP source SHA, so report changes ZIP source SHA again” loops.

1. Finish all **source, scripts, tests and canonical as-built Markdown** and a concise \`evidence/COURSE-A/A3_TECHNICAL_GATE.md\` with commands/results but WITHOUT claiming hashes not yet generated. Commit them on the correctly based A3 feature branch. This is **candidate source commit K**.
2. Confirm A3 branch is clean and pushed; verify remote K SHA. Wait for CI on **K** to pass. If a real defect requires further edits, create new K and repeat, never relabel a failing source as accepted.
3. From exactly K, use existing release script to generate **OrgResource-Course-Final A** artifacts OUTSIDE repository: executable \`OrgResource.jar\`, complete tracked-source ZIP, Java/Eclipse source ZIP, full 28-operation Postman reference and environment (safe examples), as-built DOCX, user/demo guide, independent \`SHA256SUMS.txt\` and \`RELEASE-MANIFEST.json\`. The generator should not modify tracked files. Source ZIP from \`git archive K\`, not an old branch or uncommitted state.
4. Verify every artifact's hash/byte size, archive entries, wrapper and lockfiles, JAR static entry/assets and no passwords/private data. Do not put live test password, DPAPI-protected helper, real personal details or caches inside ZIP/JAR/DOCX.
5. Run the EXACT generated release JAR via safe process environment, verify /, assets, all five list JSON endpoints, representative data mutation and persistence across Java restart; test one image upload/read after restart, no real SMS. Stop Java gracefully and verify only fictional fixture cleanup.
6. Rebuild from the source ZIP in a new folder where practical and check semantic/version parity; distinguish identical byte hash versus reproducible functionality—JAR build hashes may differ due to timestamps unless build reproducibility is explicitly configured and measured.
7. The **release source SHA remains K**; the external manifest's final hashes describe K's packaged outputs. Store any AFTER-the-fact hash receipt in the **separate release evidence artifact or draft PR comment / docs-only evidence branch**, NOT as an additional tracked commit on K which would invalidate the source snapshot. If code changes, start over with new K and regenerate everything.
8. Preserve K as the **Course Final A source anchor**. Do not force move or rewrite it. An annotated release tag or official GitHub Release requires the user's explicit approval; this prompt alone authorizes a candidate freeze and package, **not automatic publication or merging**. If the user separately authorizes a tag later, point it exactly at K (not the evidence note commit).

Build two independently usable editions only later: Extended C **must branch from exactly K** when separately authorized and independently rerun original course acceptance. Do not start C here.

## 5. Free-Luna efficiency, optional subagents and quality review

Primary Codex is the single build/test/release integrator and sole writer to shared files. No open-ended research, vendor pricing, framework choice, or cloud provider evaluation. Use targeted \`git show\`, \`grep\` and only directly affected source from \`AGENTS.md\` and the academic 07 spec. Do not reread 80 research files or 319K historical master.

Use **at most two scoped real subagents IF the free Codex environment exposes them and work is separable enough to save time/tokens**:
- Fast/light read-only file/route/28 Postman trace and missing evidence check; no writing.
- Medium read-only independent artifact/source/hash/secret scan after K; no conflicting file ownership.
Keep highest available reasoning in the primary for image/Twilio/security/transaction defect triage and overall release decision. If actual tools/subagents unavailable, do it sequentially yourself; never pretend delegation. No microtask agent for simple typos.

Minimize repeated clean builds: focused regression after actual fixes; ONE complete acceptance gate at K; one package and one delivered-JAR smoke. Avoid repeated full-doc rewriting, speculative refactors, duplicate artifacts or giant screenshots. Do not skip high-risk tests to save tokens.

**Security:** keep local bind \`127.0.0.1\` for old Java8/Boot2.1, \`DISABLED\` SMS default, bounded image upload, sanitized logs, no public accounts/PII, no forced schema overwrite. Git diff-check and secret-check before K. No free-tier spending, provider trial, public deployment, real outreach or Canvas.

## 6. Source control, factual reporting and STOP

Commit/push all legitimate A3 source/tests/canonical academic docs to A3 feature branch based on PR #11, verify remote K SHA matches local HEAD, and create/update correctly based **draft PR**. No automatic merge to main, squash/rebase that loses frozen source, branch reset, release tag, or deployment.

Create concise \`evidence/COURSE-A/A3_TECHNICAL_GATE.md\` (before K) and external release-time \`A3_RELEASE_RECEIPT.md\` beside artifacts (after K). Report:
- Baseline O14/PR11 and final immutable source K.
- Exact five entities + 26 original business + 2 image operations; actual no-regression Postman evidence and source mappings.
- Actual Java8/MySQL test counts and CI run links; Newman/Playwright and binary image/restart results; any unresolved flake.
- Accurate current DOCX as-built source, full proposal authority and student roster status.
- Released artifact names, bytes, SHA256 and location, independent archive/source verification, no secrets.
- True differences: provider stub vs provider account, local machine vs independent host/Eclipse, CI vs self-reported local tests.
- Confirmed pushed SHA and draft PR URL; no unapproved actions.
- One of: **A3 SOURCE AND ARTIFACTS FROZEN — TECHNICALLY ACCEPTED / A3 CONDITIONAL — EXACT GAPS / A3 BLOCKED — MATERIAL FAILURE**.
- Student-only video, actual group/peer reviews, signed COA/outside/AI acknowledgement and Canvas actual upload remain NOT_DONE unless human confirms them.

**STOP after A3 technical candidate freeze and release receipt. Do not start Extended C compatibility/product work or make an outside academic submission.** If fundamental acceptance fails, push safe progress and a truthful defect report, do not label A frozen. When owner reviews A3 SHA and artifacts, next prompt will be C0 supported-runtime compatibility validation from frozen K.
