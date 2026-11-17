# CODEX MASTER PROMPT — C0 Supported Extended-C Runtime Compatibility (Free Luna)

**Issued:** 2026-11-17 · **Authorized purpose:** actual software engineering only, NOT research or broad redesign. **Repository:** private \`newonathe/OrgResource\`. **Previous Course A candidate SHA:** \`7f8c0746da18fd54b47f430ac1435ac36e605d67\` at \`feat/course-final-a3-freeze-2026-11-17\` / draft PR #12; must refresh first. **Course A1+A2 parent:** PR #11. **Research decision authority:** \`docs/research-2.0/74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md\`, \`75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt\`, \`77_CP46_FINAL_GITHUB_HANDOFF_RELEASE_INDEX.md\`, and latest \`78_CP47_FINAL_NEXT_ACTIONS_RESEARCH_CLOSED.md\`, at research SHA \`ab32f3fc2aa9afa36953007ac147c0dba0f4e882\`. **Academic execution docs:** \`docs/academic-final/07_COURSE_A_EXECUTABLE_SPEC_FREEZE_2026-11-16.md\`, 09 A3 handoff.

# MISSION

**Build, run, measure and test C0 — an ISOLATED, BOUNDED MODERN-JAVA COMPATIBILITY PROOF for OrgResource Extended C, deriving from the EXACT Course Final A Git source SHA, while keeping the entire Course A independently runnable, unchanged and genuinely course-compliant.**

A is the locally runnable course edition; C is a separately deliverable extension built along the same source lineage. The eventual C itself must **actively** demonstrate Jersey REST/JSON, Java Spring \`@Autowired\` Controller→Component, real MySQL/JPA/Hibernate/Spring Data/JPQL, **literal executable \`javax.validation\`**, Retrofit/Gson including the safe Twilio protocol and fake simulation, scheduling, original five entity types, 26 course routes plus two images, current JSON/stock semantics, Postman, and Academic Glass UI. Carrying a dormant A source archive is NOT enough to call C course-compliant.

C0 is a temporary **runnable proof**, not a second maintained full product and not authorization to begin C1–C6. No account/organization feature build, public hosting, provider charges, send, destructive SQL, Canvas, merge or release tag.

**Work allocation:** Codex BUILDS/TESTS/DEBUGS/COMMITS/PUSHES; research chat performs all market/vendor/external compatibility research; prompt-planning chat sets architecture. Inspect build tooling and resolved dependencies as normal engineering, but do NOT select a different production stack or do open-ended research.

**Efficiency:** Quality, precision, security, completeness and actual correctness FIRST; then maximize useful tested code per free Luna token/tool call/elapsed time and minimize unnecessary stored artifacts. One dependency-coherent medium-sized coding cycle, three internal checkpoints, one primary integrator. Optional genuine subagents only when offered by actual free Codex tooling and cheaper than doing it yourself: (H1) fast read-only dependency/course-source crosscheck, (H2) medium read-only test/result critique. No fictitious tool calls, agent availability or reasoning levels; no duplicate writers/POM edits; highest available reasoning handles validation/security/JPA concurrency.

# 0. FRESH REMOTE/LOCAL AUDIT — MANDATORY

Before touching files:
\`\`\`bash
git status --short --branch
git remote -v
git fetch origin --prune
git branch -vv
git ls-remote --heads origin
git log -12 --oneline --decorate
\`\`\`
Check open PR #12 A3 and #11 A1+A2, latest heads/bases and CI. The old \`main\` is O1 and NOT a valid C0 parent. Read current root \`AGENTS.md\` on actual A3 source plus the newer Course A-first agent policy via \`git show origin/docs/course-a-final-spec-build-handoff-2026-11-16:AGENTS.md\`; current user prompt/latest two-edition decision supersedes old root “O14 only” phase wording. Read only relevant exact A3 report, five core JPA/controller/repository source families, A3 release script, canonical 07, CP44/CP46/CP47 and C0 UC matrix. Avoid rereading 80 full research documents or 319K transcript.

Preserve all unrelated uncommitted changes/worktrees. Never force-push, reset, clean, auto-stash, merge, alter \`main\`, modify A source commit, remove a Git ref, kill an unidentified process, or reset personal/shared data.

## 0A. CRITICAL FROZEN-A PROVENANCE PREFLIGHT — bounded; no gratuitous re-running of A

The planning audit **verified** A3 exact-head CI on \`7f8c0746...\`: Java8/MySQL and frontend jobs SUCCESS; GitHub executable JAR artifact exists. The actual Course A locally generated \`OrgResource-Course-A-As-Built-Proposal.docx\`, both source ZIPs, \`SHA256SUMS.txt\`, \`RELEASE-MANIFEST.json\`, and external \`A3_RELEASE_RECEIPT.md\` were **not attached to GitHub PR #12**, so their bytes/hashes have NOT been independently inspected.

If this Codex session has legitimate access to the already generated **external A3 release folder**:
- Locate that folder without guessing or overwriting paths. Read receipt and manifest; confirm source SHA equals the current selected A3 K SHA; recompute SHA-256 for EVERY listed payload, verify ZIP structure and DOCX \`docProps/core.xml\` source SHA. Verify JAR/source archives are outside Git and do not contain secrets. Reuse previous A3 local acceptance evidence and current exact-head CI; do not waste a full rebuild unless hashes/source mismatch.
- Persist a **small sanitized** provenance summary to the new C0 branch under \`evidence/C0/A3_PROVENANCE_CHECK.md\` (exact K, archive filenames/sizes/hashes, checks actually run, path provenance with no secrets). Optionally add PR #12 comment linking that evidence; do not alter A3 branch/K or upload personal release bundles without authorization.
- If external artifacts are inaccessible to this session, state **LOCAL_RELEASE_ARTIFACTS_NOT_VERIFIED**. A3 accepted source/CI evidence remains real, but do NOT falsely claim the complete A3 artifact set independently verified. **STOP before an architecture-dependent C0 implementation if the A3 source/manifest mismatch or any artifact required for a complete A release actually fails verification.** If only access to an otherwise reported artifact folder is unavailable (not a demonstrated failure), preserve source anchor and continue the **isolated C0 experiment** with a prominently conditional A-artifact gate; do not declare A released or start C1. This balances efficiency with truthful release status.
- The A3 source anchor K is the exact commit object, not a branch name that may move. Do not add receipt data by editing K.

Create C0 feature branch \`feat/extended-c-c0-compat-2026-11-17\` (or collision-free equivalent) from verified K. Its base PR is **PR #12** unless that PR has already merged. Never branch from a research/docs branch.

# 1. FIXED, RESEARCH-REVIEWED TECHNICAL CANDIDATES — NO AUTONOMOUS STACK SELECTION

Only these research-derived candidate inputs are authorized for C0:
- Java **21 LTS**.
- Spring Boot **4.1.2**.
- Spring Boot-managed Jersey **4.0.3** (use the actual compatible Jersey starter and explicit endpoint registration, not Spring MVC-only).
- Boot-managed Spring Security, Jakarta JPA/Hibernate, Jakarta Validation/provider, MySQL connector and servlet runtime.
- **Literal Javax API:** \`javax.validation:validation-api:2.0.1.Final\`.
- **Explicit Javax provider experiment:** Apache BVal \`org.apache.bval:bval-jsr:2.0.6\` (actual \`javax.validation.spi.ValidationProvider\`), isolated explicit bootstrap, alongside Boot-managed Jakarta validation.
- Retrofit/Gson already course-proven in A; choose a compatible supported set within the authorized BOM and record resolution, no new messaging provider.
- MySQL/InnoDB test instance with **dedicated disposable schema** and separate credentials; PostgreSQL/SQLite is NOT a substitute for required MySQL proof.
- Existing React built assets for same-process static hosting (do not redesign/rebuild entire UI).

Do not claim a supported modern JPA provider automatically recognizes javax.validation annotations—Jakarta and Javax are distinct packages. Explicitly distinguish actual \`javax.validation\` DTO checks, manual Javax entity validation, native Jakarta Bean Validation, automatic JPA callback validation, and SQL NOT NULL/FK constraints; an imported library JAR proves NONE of those by itself.

Do not replace course-required Javax with only Jakarta, Jersey with MVC-only, relational JPA with direct SQLite, or source-derived APIs with fake in-memory examples. No Docker/Kubernetes/microservices/cache/broker, paid provider or cloud resources. Use official Maven dependency resolution/compiler outputs only for engineering reproduction; if this fixed stack has a fundamental conflict, document exact logs and **STOP** rather than researching/guessing alternative frameworks.

# 2. IMPLEMENT ONE ISOLATED, EXECUTABLE C0 EXPERIMENT

Create \`experiments/extended-c-runtime-compat/\` (or one equivalent minimal path), with Maven wrapper or a documented reuse path, actual JDK21 runtime, standalone Spring/Jersey app, application/component/repository/security/HTTP integration tests, limited Java test fixtures and safe test configuration.

**Protect A-owned paths:** current \`backend/**\`, \`frontend/**\`, current Postman Course A, current A scripts and all original course entities/controllers stay byte-identical against K. Any necessary change to them is an **OWNER APPROVAL REQUIRED** stop point, not ordinary C0 work. C0 should add isolated files/new CI only if needed. Do not mutate A's real MySQL schema, import phone numbers or produce fake inventory history.

Keep file layout simple (one temporary experiment, one test configuration, only essential dependencies, one CI proof job if free/private Actions allowance permits without billing). Use one real MySQL connection source. No persistent credentials, test-only login enabled outside tests, public binding, remote APIs, or live Twilio.

## C0 acceptance matrix — 12 real cases, grouped into 3 engineering checkpoints

**Checkpoint C0-I — Framework/JPA/validation integration:**
- **UC01 — Jersey HTTP:** one Spring-managed Jersey \`ResourceConfig\`; real JAX-RS \`@Path\` GET 200 JSON and POST 201 JSON; explicit registration; protected error JSON; same Java process can serve built React \`/\`/assets at least as a static smoke.
- **UC02 — @Autowired chain:** HTTP Jersey resource → injected Spring \`@Component\` → transactional service → Spring Data JPA repository → MySQL; read committed row from a fresh DB connection; one invalid/duplicate failure.
- **UC03 — Genuine literal Javax validation:** a DTO has literal \`javax.validation.constraints.*\`; bootstrap Apache BVal explicitly; assert actual Javax API/provider class names and that blank name/invalid quantity violate constraints; valid DTO passes; controller converts violations to controlled 400. No \`jakarta.validation\` relabel.
- **UC04 — Javax/JPA boundary:** source-equivalent Javax-annotated entity; test actual JPA \`saveAndFlush\` of invalid data and record *why* it fails or succeeds. If explicit Javax check is used before persistence, prove it rejects with rollback and label **MANUAL_JAVAX_PRE_SAVE**, NOT \`JPA_AUTO_VALIDATION\`. SQL constraint proof separately. Do not assert provider-driven ORM callbacks unless actually triggered.
- **UC06 — Five-family relational model:** minimal mapping for Department, Project, Resource, ResourceRequirement, Allocation with correct \`resource_requirement\` table name and FK/unique contracts; no additional production business schema. Run actual MySQL create/read, one JPQL join/aggregate, FK rejection, rollback, migration/schema-version tracking in disposable test DB. Avoid full 26 route duplication.
- **UC11 — Dependency integrity:** print effective resolved graph/provider implementation versions; no duplicate API ambiguity/startup collision; record vulnerability scan availability/results without falsely claiming supported old Javax library simply because it loads.

**Checkpoint C0-II — Security and original behavior feasibility:**
- **UC05 — Security+Jakarta:** Boot-managed Jakarta Validation works concurrently in same JVM; Spring Security real filter chain rejects anonymous 401 and authenticated wrong-role 403, requires CSRF for authenticated mutations, rotates test sessions as implemented and accepts correct CSRF. Test user credentials only in isolated test profile. Never accept caller-supplied \`X-Organization-Id\` as authority.
- **UC08 — Original Jersey families:** five representative real route operations (one from EACH D/P/R/Q/A controller family), source-equivalent method/status/JSON DTO shape, and \`@Autowired\` delegation; use database state, not mocked repositories. Full 28 A route parity is a **later C3/C6 gate**, so the other operations are NOT_RUN in C0.
- **UC09 — Inventory and clock oracle:** T=10 reusable; APPROVED effective q3, ISSUED q2 (even after due), APPROVED project ending **exactly** sampled Philippine \`now\` q4. Actual MySQL query must return available 5; expiring pending cleanup does not phantom-release ISSUED. One atomic rollback and one limited concurrent q4-vs-stock5 conflict test: exactly one commit (or clearly label if complex full concurrency not implemented in C0). Approved not yet active can reserve; issue/consume requires active half-open period.
- **UC10 — One real database for old/new API:** a minimal original-style Jersey \`/api\` mutation and protected \`/api/v2\` read must see SAME committed MySQL row; attempts to forge another org context cannot expose data. This proves only narrow shared-store feasibility, NOT complete tenancy or authorization product.

**Checkpoint C0-III — External tools, scheduling, packaging:**
- **UC07 — Retrofit/Gson/Scheduling:** actual outbound HTTP from Retrofit/Gson to dedicated loopback FakeTwilio-compatible stub; separate official Twilio Messages form+Basic-auth local protocol stub, no provider send; scheduled method actually invoked with disabled/default guard. No SMS acceptance = delivery. Bound timeouts/no non-idempotent automatic retries/secret logging.
- **UC12 — Reproducible artifact:** compile/test on real Java21+MySQL, start packaged executable, check GET/POST and root/static asset, clean source extraction smoke, secret scan, verify same-JVM class/provider identity and stable test fixture cleanup. Classify full 28-route business acceptance, public release, hosting, offhost backups, real accounts, cross-tenant real product and real Twilio as NOT_RUN.

The 12 cases are one coherent proof, not 12 separately billed micro-sessions. Reuse same application/test fixtures and run focused tests during coding, ONE complete C0 suite at end. More tests where a specific security/transaction defect warrants them; no fabricated claims.

# 3. HARD STOP CONDITIONS AND HONEST VERDICT

STOP dependent code after a **material actual incompatibility**, and report:
- Specific Java/Maven library, error, stack trace excerpt (redacted), dependency tree, failed UC ID, counterexample against course constraint.
- Why a default Jakarta-only implementation would NOT meet literal Javax requirement.
- Whether the source A is still intact and its exact frozen SHA.
- The smallest reproducer committed to C0 branch and what research/owner decision is needed. Do not autonomously adopt another backend, dual JVM, unsafe classloader hack or public exposure of Boot2.1.

Classify \`javax.validation\` DTO execution and Javax ORM auto-lifecycle separately. An unsupported auto-lifecycle without an explicit written course requirement is a **caveat**, not necessarily disqualifying if a genuine transactional Javax validation path exists and course compatibility is independently demonstrated.

A failed exact stack is a successful *experiment* but NOT a successful C0 compatibility gate. STOP before C1.

# 4. TEST, FILES, STORAGE, SECURITY

Use \`evidence/C0/COMPATIBILITY_REPORT.md\` with exact preflight A3 provenance status, tested K source/feature branch, current CI, 12-case PASS/FAIL/BLOCKED/NOT_RUN matrix, effective dependency graph, provider class names, MySQL schema/queries/rollback, HTTP/Jersey/Security/CSRF results, stock oracle and failed counterexamples, Retrofit/Scheduler, final packaged artifact, security limitations, runtime/liveness, and unresolved course-to-C behavior. Update \`docs/engineering/C0_COMPATIBILITY_DECISION_INPUT.md\` only with actual tested facts, no open-ended research/opinion. If tests fail, preserve minimal reproducible source and truthful logs instead of claiming a production plan.

A3 release receipt info can be embedded in C0 evidence to avoid a documentation-only turn. No private phone/account SID/password, secrets, real database exports, large test artifacts, \`target\`/\`dist\`/\`node_modules\`, duplicate copied React builds or full prior research archive committed. Keep tests isolated, maintainable and source-controlled.

No public port. No real provider/account, account creation, charges, deployment, Cloudflare/OCI validation, e-mail outreach, Canvas upload, PR merge/tag. Do not delete/change A history. Avoid excessive CI runs or relying on unstated free private Actions budget.

# 5. AUTOMATIC GIT PUSH/PR

Start with fresh audit. Branch **from the exact A3 K SHA**; A3 branch itself remains unchanged. Inspect \`git diff --check\`, full status/diff and potential secrets. Stage ALL legitimate C0 experiment code, tests, minimal build files, concise evidence and (if accessible) sanitized A3 provenance receipt. Commit/push WITHOUT force; verify \`git rev-parse HEAD\` == GitHub remote feature SHA. Open/update one draft C0 PR **against A3 feature branch** if still unmerged, otherwise the correctly integrated base. Never merge automatically or tag an official Course Final release.

If code is blocked, push the safe reproducible failure and exact blocked status. Do not give fictional passing tests, provider status or locally inaccessible file hashes.

# 6. FINAL REPORT / ACCEPTANCE

Return only grounded facts:
1. A3 K parent SHA + provenance receipt availability/validation, current PR12+CI.
2. C0 branch/PR and exact verified pushed SHA.
3. Actual implemented files/features and minimal execution instructions.
4. Effective Java21/Spring4/Jersey4/Javax/Jakarta/MySQL/Retrofit versions.
5. All UC01–UC12 actual test outcomes and failure cases.
6. Narrow original course parity proved vs unimplemented in C0; immutable A status.
7. Runtime security/auth/CSRF and MySQL stock transaction evidence.
8. Exact unresolved risk and whether the research/owner must choose another supported boundary.
9. Next C1 readiness, **not** authorization to implement C1 automatically.

**Use one verdict:** \`C0 COMPATIBILITY PROVEN — OWNER REVIEW FOR C1\` / \`C0 CONDITIONAL — SPECIFIC UNPROVEN GATES\` / \`C0 BLOCKED — MATERIAL STACK/COURSE INCOMPATIBILITY\`.

**STOP after C0. No C1 or public deployment.** Begin executing now—software implementation, not another research/architecture-planning pass.
