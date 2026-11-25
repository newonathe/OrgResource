# Continuation audit checklist, state reconciliation, decisions and risks

## 1. Exact status by provenance
- **COURSE REQUIREMENT:** Assignment dated 2026 course project, 5 entities, Jersey+Spring+JPA+Javax+Retrofit/Twilio+Scheduling, GET/POST, controller injection, Postman, Part II export/video, final project due printed Dec5. Current Canvas may supersede.
- **SYLLABUS FACT:** Eclipse/Java, Jersey, JPA/Hibernate/MySQL, JPQL, validation, Retrofit/GSON, image receive/serve CLO9, authorship/assistance obligation.
- **USER DECISION:** Current O3 prompt still running at handoff request. O2 video DONE by user report (not independently opened or submitted). VS Code, Codex coding, private newonathe repo, final working React UI, exact five entities, efficient grouped builds, mandatory fresh GitHub audits and feature-branch push of all legitimate changes.
- **OBSERVED CONNECTED GITHUB:** Main `16e781c`, 13/25 original routes, O3 D03 tested, eight Java tests saved, 0 CI runs, draft PR #1 old O2 branch, no implemented frontend. This is a point-in-time snapshot.
- **SAVED EXECUTION EVIDENCE:** O1 Maven/MySQL/Newman/restart, O3 Maven/8 tests/newman tests and clean source export, current demo preflight real startup; saved records not equal to fresh independent rerun by new ChatGPT conversation.
- **WORK IN PROGRESS:** O3 six CRUD routes + coverage correction, completed/pushed status unknown.
- **NOT YET OBSERVED/OPEN:** full Allocation lifecycle, actual @Scheduled jobs and FakeTwilio HTTP, React app, live MySQL concurrency, final Eclipse GUI import, final bundled UI/JAR, CLI/CD, image obligation, current Canvas, user-authored original O2 video file/Canvas receipt, final Dec5 academic deliverables.

## 2. First 15-minute remote inspection in new conversation
1. Check GitHub account `newonathe` selected (other connected GitHub accounts exist; don't use wrong account).
2. Fetch repo metadata and `main` HEAD, recent commits (date + subject), branch list and HEADs, open PRs including draft #1, workflows/checks.
3. Find O3 feature branches with recent commit and compare diff from old head. Do not use old branch as merged unless Git actually shows it.
4. Read exact updated `evidence/O3/PHASE_REPORT.md`, `evidence/O3/ROUTE_PARITY.md` from its branch; if not updated search for `evidence/O3*` new files or Codex response.
5. Enumerate actual `@POST` and `@GET` paths across five controller Java files. Count 25 original paths; separately A09 (and conditional image routes). Show implemented/untested/missing.
6. Read actual modified code: Project/Resource/Requirement components/repositories/DTO/controller and JPQL, tests, Postman; pay special attention to `expectedTotalQuantity` and effective COVERED query.
7. Inspect test types and saved outputs to distinguish Java unit, MySQL integration, HTTP Newman, concurrency, clean packaging. If changes not pushed despite local Codex output, request push/commit and don't treat repo stale as code failure.
8. Check PR status and branch protection; no automatic merges.
9. Return succinct acceptance verdict with critical issues and exact next step. If current O3 STILL actively running, don't interfere or send overlapping new coding prompt until user is ready.

## 3. O3 audit verdict format
```
GitHub audit time (PHT):
Repository / visibility:
Remote main SHA:
O3 feature branch SHA / PR:
User local modifications visible? (yes/no; evidence):
Changed Java/API/DB source:
Routes now implemented: D__/P__/R__/Q__/A__, total original __/25, extra A09 __.
Tests: Java unit __ PASS/__ FAIL/__ NOT_RUN; MySQL __; Newman __; concurrency __; clean package __; Eclipse import __.
Coverage query: Correct / Incorrect / Not verified; evidence:
Resource stale guard: Correct / Incorrect / Not verified:
Project historical approval date lock: Correct / Incorrect / Not verified:
Requirement below max(PENDING,covered): Correct / Incorrect / Not verified:
Current unresolved P0/P1 defects:
Remote push verified SHA:
Decision: PASS / CONDITIONALLY_READY / FAIL / BLOCKED.
Next batch: O4+O5 or one coherent O3 repair.
```

## 4. Working decision register
ADR-01 Exact 5 entities, no StockMovement. [USER DECISION]
ADR-02 Original 25 method/path contract, added A09 separately. [USER DECISION/AMENDMENT]
ADR-03 Whole batch reusable/consumable, no damaged/partial returns. [DESIGN RECOMMENDATION]
ADR-04 Reusable totals owned vs consumable remaining stock. [DESIGN RECOMMENDATION]
ADR-05 Effective APPROVED hold expires at Project end, not indefinitely. [AMENDMENT]
ADR-06 Pending never holds; approved future Project holds globally. [AMENDMENT]
ADR-07 Issued custody persists after Project end; late manual return. [AMENDMENT]
ADR-08 Historical approvedAt never erased; protects Project date edits and physical deletion. [AMENDMENT]
ADR-09 Resource expectedTotalQuantity guard, no version table; do not auto retry lost HTTP. [AMENDMENT]
ADR-10 Requirement lower bound effective Covered and max individual pending. [AMENDMENT]
ADR-11 Public API redacted phone and derived contactPresent; only store when consent. [PRIVACY]
ADR-12 A09 revoke consent clears destination independent of lifecycle. [PRIVACY]
ADR-13 SMS claim once, no automatic retry UNKNOWN, simulated != delivered. [SAFETY]
ADR-14 Local trusted operator, no fake auth; bind loopback. [PROTOTYPE LIMIT]
ADR-15 Java8 SpringBoot2.1/Jersey/Javax, VSCode, Eclipse export. [USER DECISION/COURSE]
ADR-16 React strict TS/Vite real UI despite optional bonus. [USER DECISION]
ADR-17 Conditional CLO9 Resource image extension only if authoritative requirement established. [OPEN QUESTION]
ADR-18 One master source read then targeted; larger coherent grouped cycles. [USER DECISION]
ADR-19 Every NEW cycle fresh GitHub remote and Codex local audit, every cycle pushes all legitimate work to safe feature branch and verifies remote. [USER DECISION]
ADR-20 No blind merge main, forced Git destructive operations, DB reset, public release, Canvas submission or real SMS without authorization. [SAFETY]
ADR-21 No real-world Ateneo institution/workflow/adoption/participant data inferred. [ACADEMIC]
ADR-22 Don't request raw videos; audio transcript not frame data. [SOURCE LIMIT]
ADR-23 No fake code/test/evidence; independent rerun statuses separately. [ACADEMIC]

## 5. Critical risks to keep actively tested
R1 MySQL JDK8/Jersey/Hibernate dependency conflicts; course source compatibility.
R2 MVC refactor breaks required Jersey and explicit @Autowired component structure.
R3 Racing approvals overbook stock/requirement; flawed Project→Resource→Allocation lock order/deadlocks.
R4 Incorrect expired approved coverage and H/I at endAt boundary; stale query after lock.
R5 Consumable double deduction on consume; duplicate network POST after timeout.
R6 Resource expected-value guard race; naive stock total update or inconsistent response.
R7 Project historical approvedAt/date edit vs approval race.
R8 Requirements pending largest vs sum, and actual constrained coverage.
R9 Deletion cascades erase immutable history; FKs not respected.
R10 Scheduler auto-releases issued custody or assumes return; background job duplicate claims.
R11 Retrofit FakeTwilio vs real Twilio response differences and leaked Authorization in simulator logs.
R12 Time zone conversion PHT↔UTC across JS browser and Java LocalDateTime.
R13 Frontend fake demo data, frontend local stock authority, missing mobile/focus and no actual live API.
R14 CI/Java export/Eclipse compatibility untested but claimed.
R15 Historical 9-entity/68-route docs accidentally used as specification.
R16 Canvas policy/date and CLO9 image uncertainty.
R17 Secrets/private contacts committed by automated agents; broad CORS/auth misrepresented.
R18 Git concurrent writers, old O2 PR rebased into latest main, accidental main merge.
R19 Lossless continuity request: primary source files and actual code must be reopened, not replaced by this overlay.

## 6. Evidence labeling convention
**PLAN/SPECIFIED** = design only; **IMPLEMENTED** = actual source present; **TEST REPORTED** = saved result evidence inspected but not independently rerun in this chat; **VERIFIED EXECUTION** = appropriate test actually executed under tool-observed environment; **USER REPORTED** = explicit user's action claim; **FAILED** = executed and failed; **BLOCKED** = dependency unavailable; **NOT_RUN** = no execution attempted; **OPEN QUESTION** = source conflict/decision. Do not compress these into a generic “working”.

## 7. Git branch and merge practice
A feature branch push is authorized as a **coding-cycle** end action. Main merge and release remain separate review gates. PR #1 is OLD O2 demo kit (main is newer); don't merge by default. This continuity pack itself is on `docs/conversation-continuity-2026-11-14` specifically to avoid interference with active Codex O3 edits; it does not contain Java implementation changes and doesn't assert application tests passed. User can use its links in a new ChatGPT conversation in the same Project.

## 8. What the new chat should DO immediately, not do
DO audit actual changed GitHub branch and source, review O3 code-level behavior/test evidence, then draft grouped O4+O5 prompt if ready. DO call connected GitHub tool as necessary. DO preserve separate primary source authority. DO optimize for actual verified software per token, performance and storage, and automatic push. DON'T start editing code from ChatGPT without user ask, DON'T re-explain O2 video by default, DON'T make plans as if 13/25 still current after Codex push, DON'T claim O3 checks passed from old reports, DON'T merge or release without explicit instruction.

## 9. User handoff UX
Simplest new conversation starter: copy file `07_NEW_CHAT_COPY_READY_PROMPT.md` into new chat, or point new chat directly to private GitHub repo branch/directory and ask it to read `MASTER_CONTINUITY_UNABRIDGED.md` plus fresh GitHub audit. This is the smallest repeated context input; detailed evidence remains in committed files. If a GitHub connector isn't connected in the new chat, attaching downloaded master markdown also works. Never guess sandbox download path.