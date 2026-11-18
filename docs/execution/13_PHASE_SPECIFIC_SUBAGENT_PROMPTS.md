# OrgResource — Phase-specific complete subagent dispatch prompts C6–C11

2026-11-19. PLANNING ONLY. These are executable task contracts once actual approved Git refs, source files, DTOs and tests have been filled by the root. Not evidence a worker ran, a model was available, a build passed or deployment occurred. READ: root AGENTS.md, ENTIRE docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md, all relevant 00–13 execution docs, actual course assignment/syllabus/slides/examples, applicable phase master prompt (04/05), and real code. Do not summarize away those sources. Models/efforts below are explicit; project custom roles (.codex/agents) also enforce them where supported.

## Mandatory dispatch envelope — fill before ANY subagent

Root must prepend these actual resolved fields, never placeholders: TASK_ID and explicitly authorized C6–C11 PHASE; exact repository newonathe/OrgResource, current remote, base/head SHA, branch, PR, clean worktree; immutable Course A 7f8c0746da18fd54b47f430ac1435ac36e605d67; complete authoritative source paths+blob hashes+relevant ranges; exhaustive READ and OWNED_WRITE whitelist and FORBIDDEN path list; independently verified locked API/role/SQL/domain/Clock contract; every action in order; expected test command/fixture/HTTP response+MySQL state postconditions and denial/restart/rollback; exact report paths and status categories; exact chosen model+reasoning; no nested spawn and phase STOP. If a field is unknown, the agent returns NEEDS_CONTRACT or STOP_CONTRACT_UNRESOLVED instead of inventing it. All workers read full QA gate themselves.

Root enforces ZERO default spawns, initial at most ONE worker, TWO concurrently, maximum FOUR distinct spawned sessions per pass including one later independent critic. Root alone owns shared integration and mandatory final test execution. Script for deterministic tree/hash/JSON/CSV checks rather than summon an agent. No automatic retries/escalations; log any approved exceptional higher-effort single-issue dispatch. Every worker reports exact SHA/file/range, attempted and actual tests with exit/postconditions, P0/P1/P2 and true SOURCE_ONLY/REPORTED_ONLY/NOT_RUN/VERIFIED status, and token numbers only if exposed.

## C6 — audit: two sequential workers maximum

### C6-S — org_source, gpt-6-luna, medium, READ ONLY

MISSION: produce accurate academic/source and public Ateneo fact audit, not edit application code.
READ: original assignment PDF, syllabus PDF, available course corpus and original examples, frozen Course A backend/frontend/Postman paths and academic evidence; Extended backend entities/services/REST/JPA/migrations and React references; existing source audit; expanded 2026 RecWeek directory JSON (72 mixed COA-M page entries, 24 LIONS 2026, eight historic-only excluded), MISA/Sanggunian public context, earlier 12 sample+alias map. Root supplies exact SHA/paths after Git inventory; worker must open actual source.
STEPS: (1) Inspect exact published source pages/file sections, record page/line/date/URL and coverage FULL/RANGES/METADATA/UNREAD. (2) Trace 26 business + 2 image academic as-built routes and original five-domain entity/types, Java8 Jersey+Spring+JPA/MySQL+Retrofit/Gson+scheduling+Postman+React, note original proposal vs amendment conflict. (3) Identify 72 COA DIRECTORY entries that include Sanggunian, service/umbrella/publication and cannot imply 72 accredited members; confirm MISA context, Sanggunian government, at least 24 independently cited LIONS, all eight old-only names uncertain and 53/55 COA published count discrepancy. (4) Do not infer member-size rankings or real inventories/usage from demo tiers. (5) Produce requirement/source coverage/claim truth and conflict ledger with file hashes and next C6 decision tests. (6) STOP at source handoff; no uninspected-as-FULL claim.
OUTPUT: C6 SOURCE_COVERAGE.csv; COURSE_AUTHORITY_MATRIX.csv; DEMO_SOURCE_AND_RIGHTS.csv; SOURCE_CONFLICTS_AND_DECISIONS.md. Mark missing files or unattached video frames UNOBSERVED.

### C6-X — org_review, gpt-6.1-sol, medium, READ ONLY, after C6-S

MISSION: independently falsify C6 source/authority and route/domain conclusions.
READ: actual course files, frozen A source/test/Postman, Extended REST/service/SQL routes tied to C6, 2026 public roster primary pages and source timestamps; do not rely on worker reassurance.
STEPS: (1) Find route-count, wrong Java8/Java21, entity-count, course GET/POST and amended operation mismatches. (2) Independently scrutinize official vs directory category, LIONS incompleteness and MISA/Sanggunian status; any invented largest-org ranking is a defect. (3) Flag high-risk tenant/ledger/identity contract gaps requiring C7/C8 negative tests. (4) Identify metadata-only files incorrectly claimed semantically read and false CI proof. (5) Return exact source line/SHA or dated URL, counterexample, severity, missing evidence, next test/owner and reason. No implementation edits; safe disposable tests only if authorized. STOP before C7.

## C7 — auth/security: at most three sessions, two concurrent

### C7-B — org_auth, gpt-6.1-sol, medium, BACKEND/IDENTITY OWNER

MISSION: implement only approved C6 identity/tenant/security corrections with exact endpoint/role contract.
READ: Extended security/identity/rest/service paths, C1AttemptThrottle, C1SameOriginFilter, C1BootstrapService, Organization, UserAccount, Membership, application.properties, Flyway/DB grants, actual auth tests and C6 role×endpoint×tenant matrix. Root fixes precise owned Java/test paths, SQL migration ownership and DTO schemas before dispatch.
STEPS: (1) Derive exact session/cookie/recovery/invite/auth statuses and active membership role rules. (2) Reproduce known concrete behavior and record old HTTP/DB state. (3) Apply smallest approved file-scoped fix, keep Course A+V1–V5 frozen; do not silently add Redis or unchosen single-vs-multi-instance policy. (4) Test real Java21/MySQL8 login, role MEMBER/LEAD/CUSTODIAN/ADMIN/OWNER, anonymous, foreign tenant IDs, expiry/demotion, wrong origin/Host/CSRF, recovery/invite replay, restart throttle and secret/log leak. Assert status+no unauthorized row mutation. (5) Report changed file/range, observed commands/exits and next UI contract. If target host not present label proxy/TLS NOT_RUN, not VERIFIED. STOP on open authorization/retention decision.

### C7-F — org_ui, gpt-6.1-terra, medium, OPTIONAL FRONTEND OWNER

DISPATCH only if exact C7 HTTP/error/role contract has been frozen and disjoint UI work materially saves time.
READ: Extended React auth pages/context/client/types/unit/Playwright; current C7 auth DTO and role matrix.
STEPS: (1) Audit sign-in/logout/recovery/invitation/tenant switching, disabled account and role-revocation views. (2) Implement assigned UI files only; exact JSON keys/error codes and allowed states. (3) No account enumeration, private source data or shared writable public-demo admin. (4) Test valid/invalid, loading, stale session, failure/retry, keyboard/mobile/reduced-motion; use actual packaged browser when feasible. (5) Return type/lint/test execution evidence and changed paths; unknown backend contract -> STOP. No DB/backend edits.

### C7-X — org_review, gpt-6.1-sol, medium, READ ONLY, after integration

Independently check exact final C7 source SHA, role×HTTP×tenant cases and real MySQL receipts, forged Host/trusted proxy, session/CSRF, demotion, invite/recovery/restart and data secrecy. Reproduce most damaging safe counterexample, verify no source/migration drift, test independence and correct denial/rollback. Return P0/P1 with file/range/HTTP+DB postconditions. If unresolved critical defect, ask root to decide ONE escalation to org_critical/high instead of spawning another reviewer automatically. C8 not authorized by C7 result.
