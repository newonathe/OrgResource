# CP27 — Decision register, evidence grades, precise release gates and future Codex staging

**As of 2026-11-16. All P0 choices below are owner-approval candidates, not active user decisions or implementation authorization.**  
Paired authoritative candidate: [48_CP27_CANONICAL_PRECODING_HANDOFF_DRAFT.md](48_CP27_CANONICAL_PRECODING_HANDOFF_DRAFT.md).

## 1. Classification rules
- **COURSE REQUIREMENT:** original 3pp assignment; **SYLLABUS FACT:** original 9pp syllabus. Current Canvas can change applicable instructions.
- **USER DECISION:** D-001–D-031 from `DECISIONS.md` unless explicitly amended.
- **SOURCE VERIFIED / CI OBSERVED / AUTHOR-REPORTED LOCAL / NOT RUN / INSTRUCTOR-OWNED UNKNOWN** are distinct. Test code existence does not establish an HTTP acceptance PASS.
- **DESIGN RECOMMENDATION:** no automatic Codex authority. Every undecided choice remains open in a signed decision register.

## 2. Candidate owner decision table
| ID | Proposed selection | Reason and exact open decision |
|---|---|---|
| OD01 | Two independent executable builds: original literal Javax course academic O14; supported Jakarta-based public P0 | User already chose two active artifacts; public exact technology and functional boundary NOT signed |
| OD02 | Global capacity holds, no time-window optimization | Small coherent model, original source-aligned; owner confirmation needed |
| OD03 | Whole-batch reusable usable return and consumable consumption | Avoid unsupported PARTIALLY_RETURNED and loss/damage |
| OD04 | `endAt <= now` ends effective approved hold before scheduler persisted cancellation | Must reconcile proposal vs source with course and owner review |
| OD05 | Operator-only fictional tenant bootstrap, private invitation | No public self-attested Ateneo org |
| OD06 | Recipient ACTIVE ADMIN accepts ownership; prior OWNER→ADMIN atomically | Competing two-owner policy still possible; select explicitly |
| OD07 | Sole-owner loss/compromise without valid recovery freezes fictional tenant | Avoid invented helpdesk institutional authority |
| OD08 | Local adaptive password, saved one-time recovery, Spring Session JDBC | Provider/Auth and recovery/session tests required; no paid email |
| OD09 | Public R04 archive vs original hard delete, no public A09 SMS; preserve all academic URLs | Explicit business-capability differences and protected deletion policy |
| OD10 | Internal BIGINT / public UUID physical CHAR(36) candidate; UTC DATETIME(6) public | Compare BINARY(16), MySQL index and 2040 tests; academic PHT unchanged |
| OD11 | Java17+/Java21 Boot4.1.2 Jersey4.0.3 Jakarta MySQL8.4 candidate | Effective dependencies, JSON provider, security & ORM integration COMPAT-01 |
| OD12 | Qualified OCI A1 + controllable MySQL8.4 + encrypted off-account restore | Account region/price/card/VM/SSL renewal/restore still unknown |
| OD13 | No image requirement assumption; current Canvas/instructor resolve CLO9 | Do not assert waiver |
| OD14 | Actual Retrofit FakeTwilio simulator ≠ Twilio Virtual Phone account/inbox | Current instructor acceptance required |
| OD15 | Reproduce O14 JAR and 2 source ZIPs+SHA+manifest at pinned source SHA | Local report ≠ independently retrievable file custody |
| OD16 | Owner may authorize isolated COMPAT-01 ONLY | This research push does not authorize it |
| OD17 | Separate full P0 engineering authorization after experiment + policy/DDL acceptance | P-ACCEPT and A-FINAL remain future |
| OD18 | Current Canvas, Certificate of Authorship and student-only submission | Student authoritative LMS observation needed |

## 3. Evidence and acceptance ledger
| Case | Required real proof | Current classification | Failure stop |
|---|---|---|---|
| A01 | Existing 26 Jersey handlers, `@Autowired`, JSON, GET/POST | SOURCE VERIFIED; partial CI, complete Newman local reported | Wrong course stack or missing route |
| A02 | Five JPA entities, real MySQL, Spring Data JPQL, FK, runtime CRUD | SOURCE+CI, direct local DB schema reported | Not independently reproducible |
| A03 | Invalid `javax.validation` DTO AND invalid JPA entity `saveAndFlush` reject/rollback | DTO source/CI, JPA direct test UNVERIFIED | Silent invalid entity write |
| A04 | Retrofit/Gson loopback HTTP T-A and real Twilio T-B if required | T-A SOURCE/CI, T-B UNKNOWN | Incorrect provider acceptance claim |
| A05 | Scheduled expiry/reminders exactly once after restart, stable clock | SOURCE/CI reported lifecycle; operational final UNVERIFIED | Duplicate physical stock/event |
| A06 | 26 handler coverage, full Newman 96 requests/142 assertions | Static definitions verified; local runtime PASS author-reported | Missing/invalid actual acceptance |
| A07 | Exact clean O14 package, Java8/Eclipse independent build and demonstration | CI JAR verified; complete ZIPs local reported | Missing/corrupt release files |
| A08 | CLO9, Canvas, final video, peer when applicable, COA/assistance | Course text verified, human decisions/actions UNKNOWN | False final academic PASS |
| COMP01–08 | Pinned local Boot/Jersey/JPA/MySQL/auth/CSRF/lock runtime tests, O14 unchanged | NOT RUN | No full public build on failure |
| P01 | Fictional offline first owner + invitation single-use/revocation/race | NOT RUN | Unauthorized OWNER |
| P02 | Credential reset+CSRF+session revocation/restart and role enforcement | NOT RUN | Session/role bypass |
| P03 | 2-org BOLA/IDOR through list/details/exports/activity/operation receipts | NOT RUN | Any cross-tenant data leak |
| P04 | Direct SQL wrong org/wrong resource FK and non-null/CHECK failures | NOT RUN | DB accepts invalid relationship |
| P05 | T5, q4+q4 concurrent approval exactly one, 100 adversarial races | NOT RUN | Overcommit/negative balance |
| P06 | Reusable T-H-I, whole usable return; consumable T-H/CONSUMED | NOT RUN | Double stock/incorrect coverage |
| P07 | Same durable operation ID after 48h+restart, no double physical receipt | NOT RUN | Late replay doubles quantities |
| P08 | Multi-org global account disable, simultaneous role changes/close race, last owner | NOT RUN | Orphaned ACTIVE org |
| P09 | Real API/DB/UI Playwright, keyboard, dark/light and responsive view | NOT RUN | Mock-only UI/inaccessibility |
| P10 | Authorized $0 provider, actual trusted HTTPS, renewal/reload, private port3306 | NOT RUN | Billing/TLS/secret leak |
| P11 | Encrypted off-provider backup, independent compatible clean MySQL restore | NOT RUN | Backup-only without restore |
| P12 | New maintainer reproduces, rollback/upgrade/maintenance | NOT RUN | Nonmaintainable public service |

## 4. Precise future acceptance fixtures
- Global reusable: T5, approve q4 -> available1; issue -> H0/I4/available1; whole return -> I0/available5; partial-return rejected.
- Consumable: T10, approve q4 -> available6; consume -> T6/H0/available6; second consume rejected.
- Concurrency: two q4 approvals against reusable T5; only one committed. Inject failed transaction after ledger insert -> no partial changes. Expiry vs issue -> one winner.
- Tenancy: two orgs A/B with duplicate display names; cross-org URL/JSON/SQL fake references denied, without ID information disclosure.
- Same-tenant wrong-resource: movement ISSUE references valid allocation from Resource A but Resource B -> MySQL composite FK denies.
- Idempotency: RECEIVE q3 `operation_public_id=K`, delete 24h HTTP receipt, restart, retry same K/body -> exactly one +3; same K/new quantity ->409.
- Governance: disable account active OWNER across two orgs where one is sole effective owner -> all-or-nothing refusal; two simultaneous ownership changes -> never 0 owners.
- Time: project end at exact `endAt`, 2040 UTC DATETIME(6), PHT UI conversion; scheduler lag cannot release issued stock.
- Backup: encrypted backup key held separately, export complete roles/ledger/sessions per approved policy, restore on clean target, compare counts, checksums and invariants.

## 5. Bounded experiment COMPAT-01 — future master-prompt brief, not executable instruction now
**ENTRY:** owner explicitly authorizes local isolated experiment; exact branch/commit and dependencies; no use of existing academic DB, accounts, Twilio, cloud or paid services.
- C01 compile/start version-pinned Java17+/Boot4/Jersey4 service.
- C02 `GET /probe` actual Jersey JSON with Spring `@Autowired` component.
- C03 valid and invalid Jakarta DTO POST, response JSON and zero write on 400.
- C04 actual JPA/MySQL FK, transaction rollback and invalid entity rejection.
- C05 real cookie-auth HTTP 401/403 and missing CSRF header 403, not only Spring mock/security unit.
- C06 concurrent conflicting resource mutation cannot overcommit.
- C07 record effective dependency tree, exact JDK/DB, vulnerabilities and reproducible redacted build/test evidence.
- C08 O14 academic literal Javax binary/source untouched and independently runnable.
**STOP:** any failed case requires report and owner review; no automatic whole build, framework, DB, paid-host or original course rewrite.

## 6. Separate future Codex phases — NOT AUTHORIZED
| Phase | Dependency | Scope | Exit |
|---|---|---|---|
| H0 | Research source decision | READ-ONLY inspect approved SHA, PR ancestry and source coverage | Proven source path/head and inventory |
| H1 | explicit COMPAT-01 authorization | Disposable local compatibility tests only | 8-case report, signed review |
| H2 | owner-approved full build/domain/auth/DDL | Migrate isolated new public MySQL, credentials, ownership, sessions | FK/Auth/last-owner tests |
| H3 | H2 | Global allocation, whole returns, T/H/I ledger, durable operation identity | Real race/rollback/property evidence |
| H4 | H3 | Authenticated API, scheduled expiry/activity, audit/export | Newman/HTTP permission tests |
| H5 | H3–4 | Connected v5 React UI, responsive and accessible | Browser E2E evidence |
| H6 | explicit account authorization, H1–5 | Qualifying free host/HTTPS, off-account encrypted backup/clean restore | Real operational evidence or NO-GO |
| H7 | H2–H6 | Independent P-ACCEPT, then original A-FINAL | Separately signed evidence |
| H8 | H7 | STUDENT-ONLY Canvas submission | Actual receipt |

No guessed branch: research PR #7 and academic PR #8 are draft and have different feature bases. No merged code or approved execution is implied.

## 7. Research acceptance gate R0–R10
- R0 sources/course: largely researched; current Canvas and exact source ZIP extra path open.
- R1 academic source/tests: meaningful CI, final package JPA/Twilio/CLO9 open.
- R2 modern public architecture: paper candidate; real integration NOT RUN.
- R3 identity/ownership: detailed option, final user choice OPEN.
- R4 API/domain: corrected canonical candidate; approved differences OPEN.
- R5 SQL schema: field constraints and lock model documented; DDL NOT FROZEN.
- R6 privacy/security: threat/control tests drafted; no public code.
- R7 free host/operations: documented possibilities; no account or clean DR.
- R8 test specifications: comprehensive; P0 contract approval outstanding.
- R9 independent critique: CP26 complete; cross-document final audit remains.
- R10 owner decision: NOT GIVEN.

**Verdict:** Broad research sufficiently advanced for review; isolated COMPAT-01 eligible for user decision but NOT AUTHORIZED; full engineering NO-GO. Printed Dec5 deadline remains RED HIGH-RISK. Do not convert this document into a permission to execute.