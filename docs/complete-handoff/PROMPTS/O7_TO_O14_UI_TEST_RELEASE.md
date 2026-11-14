# Staged frontend, integration, conditional image, release prompts O7–O14

All prompts inherit and require actually readable docs/ORGRESOURCE_MASTER_HANDOFF.md and current repo. A phase can begin only when its dependencies are verified. You may work on separate worktrees in parallel after API DTO contract is frozen. Each phase ends with full report: scope, source consulted, actual changes, commands/results, DB/HTTP/UI evidence, failures, security, diff and Git state, gate PASS/FAIL/BLOCKED/NOT_RUN. No unauthorized pushes, merges, Canvas submissions or fabricated screenshots.

## O7 — React/Vite/TypeScript foundation

AGENT: Claude Code or Codex assigned separate frontend worktree.
PRECONDITION: At least stable backend DTO contract and Java basic CRUD.
GOAL: Genuine modern React application skeleton using Vite, TypeScript strict, HashRouter (hash not sent to server), relative /api JSON client, Vite dev proxy preserving /api, responsive layout, sidebar/nav, header, 8 screen routes, error/loading/empty/retry/confirmation components, reusable forms/tables/dialogs, accessible focus and keyboard handling. All screen placeholders must be clearly labeled not connected. Do not fake numbers. Pin npm lockfile and verify node/Vite versions. Do not introduce Node backend, second database, Next.js, authentication, payments or a sixth JPA entity. Frontend does not calculate authoritative stock. Run npm ci after lockfile creation, TypeScript build, lint/unit tests, route navigation, 375px mobile and desktop usability, keyboard focus. Gate real frontend build and shell behavior; no claim backend integration without actual HTTP.

## O8 — Functional Departments, Projects and Resources screens

AGENT: frontend writer; backend CRUD O3 verified.
Implement Department list/search/create/update/delete via original D01-D04, confirm server response and explain 409 duplicate/dependency.
Projects list/filter, create future PHT period, name-only edit preserving dates, guarded date changes, read linked Requirements and Allocations using existing GET lists, safe delete and 400/404/409.
Resources list/filter REUSABLE/CONSUMABLE, create (NO unit field), show server total/available and semantics, update with expectedTotalQuantity precondition, 409 stale refresh and force user review rather than overwriting, delete total0/unreferenced only.
State: loading, empty, field errors, server unavailable, retry, pending mutation, success only after confirmed response, stale warning, destructive confirmation. No invented GET-by-ID routes, no localStorage stock. Run real backend browser/API tests, keyboard/mobile, refresh/persistence. Gate screens actually work against Java.

## O9 — Functional Requirements and Allocations workflows

AGENT: frontend writer; O4 business engine verified.
Requirements create/select Project and Resource; one pair unique; display quantity/allocated/shortage; modify demand with largest-pending rule; safe delete only no history.
Allocations list/filter by Requirement/status, link to related Project and Resource through existing list endpoints; create PENDING with borrower fictional and optional controlled test SMS destination/consent; approve/issue/return/consume/cancel/delete/revoke consent only valid actions. Always server-authoritative despite client-side button states. Confirm whole usable batch on RETURN and whole quantity on CONSUME; avoid pretending partial/damaged handling. Refresh all relevant data after mutating POST. Handle 409 state and 503 contention, 404, 400; after network timeout do NOT auto POST retry, re-fetch and reconcile.
Display lifecycle timestamps, consent boolean, outcomes without phone. Browser E2E: pending no hold, approve reserve, issue no second deduction, return restores, consume once, repeated commands refused, history deletion blocked. No fake success. Gate real UI+DB evidence.

## O10 — Dashboard, reminder activity and responsive finishing

AGENT: frontend writer; backend O6 and O9 dependencies.
Dashboard actual GET data; departments count, Resource TYPES (not sum unlike physical quantities), active/upcoming Projects, PENDING count, ISSUED batches, overdue ISSUED, Requirements shortage count, CONSUMED batch count. Show recent allocations and contextual actions; no fabricated metrics, no inconsistent hidden errors. The dashboard may read multiple endpoints at different instants; provide refresh and don't claim one transaction snapshot.
Reminders Activity: compute due/overdue from issued Project end, show persisted reminderOutcome SIMULATED vs PROVIDER_ACCEPTED vs UNKNOWN, no invented send-history endpoint/manual SMS button. About screen clearly says fictional local academic prototype without auth/public deployment or campus endorsement.
Polish visual hierarchy, accessible focus, mobile drawer, responsive table, positive/warning/error labels; test small screens and keyboard. Gate all UI screens connected and real browser tests.

## O11 — Whole system acceptance and contradiction audit

AGENT: Codex primary QA, Claude independent review read-only.
Do not add features. Validate 5 entity DB schema, unique FKs, exactly 26 intended routes, DTO JSON/TypeScript equality, Java JPA JPQL initialization, Javax validation and return-value error mapping, DB transactions/parallel approval/rollback, stale Resource update, Requirement coverage/shortage, PHT time boundaries, expiry scheduler, Retrofit/Gson FakeTwilio actual request, consent no duplicate, Postman import/real 26 routes plus negative cases, React component/browser E2E with real backend, restart persistence, HashRouter final jar, clean source import, security/credentials, no false evidence. Run tests, log test identifiers, commands, outputs, failures and unrun. Gate no unresolved critical correctness/academic/security defect. Static reference-model checks from planning do NOT count as Java tests.

## O12 — Conditional syllabus image feature (not automatic)

ONLY IF current final-project Canvas/instructor instruction requires receiving and serving images, add nullable Resource.imageData byte[] / imageMimeType String (same five JPA types) and POST /api/resources/{id}/image + GET /api/resources/{id}/image. Test PNG/JPEG magic bytes, MIME, small upload size, genuine database byte round trip, correct Content-Type, 404 no image, invalid/large payload, UI display. Follow actually inspected course multipart/Jersey image examples. Update proposal, Postman and route count 26->28. Do not add cloud object store or sixth entity. Gate real upload/download byte equality. If ambiguity unresolved, record decision as OPEN QUESTION, do not assert instructor waived it.

## O13 — Reproducible bundled app, Eclipse-compatible source export and ZIPs

AGENT: Codex with Claude review.
Freeze and record tested Git commit. Run real Maven JDK8 clean verify, MySQL integration, frontend npm ci/lint/test/build, browser E2E, Postman. Copy built React static assets into packaged Java app; verify executable JAR GET /, /assets, /api/departments, HashRouter refresh/navigation. Generate Java-only source ZIP containing all .java/POM/wrapper/config/example deps, tested m2e Eclipse route and portable .project/.classpath/.settings if course literal, no absolute machine paths. Generate full source ZIP Java+React+docs+Postman+lockfile. SHA-256 checksums, re-extract, secret scan, actual fresh import if feasible; mark import NOT_RUN if not. No unsupported "Eclipse import PASS" claims. Do not include node_modules, target, secrets, private student data, original course archives unless authorized.
Report file paths, sizes, checksums, exact command outputs, failures. Gate reproducible artifacts.

## O14 — Final academic evidence, proposal reconciliation and release

AGENT: Codex primary, Claude review.
Check current assignment/syllabus/Canvas if accessible; preserve original 25 endpoints and explicitly amended 26th, Resource update expectedTotalQuantity, phone redaction, expiry etc. Document actual implementation matches proposal or list deviations. Verify tested final release has course required Jersey JSON Spring JPA Javax Retrofit/Gson Scheduling Postman, 5 entities, real UI, actual restart, DB, concurrency, SMS simulator. Create truthful video storyboard and capture genuine recording only with authorized screen tool. Ensure accessible final recording link, correct instructor permissions (if applicable), Certificate of Authorship and outside AI help acknowledgments; no invented receipt, peer review, instructor approval or pilot. No final release claims without real evidence.
Final output artifact inventory and gate VERIFIED/CONDITIONALLY_READY/BLOCKED, commit SHA, checksums, setup, test matrix, limitations, instructions for actual student Canvas submission. Stop; do not submit or push without user approval.

## General acceptance verdict

PASS/VERIFIED only when suitable observed evidence exists. FAIL if executed and failed. BLOCKED if dependency/tool unavailable. NOT_RUN if never attempted. Report specific tests, no generic "everything tested" claim. Keep test scope precise: static code inspection, compilation, real DB transaction, HTTP, browser E2E, scheduler, external provider, actual import/export and academic submission are distinct evidence classes.
