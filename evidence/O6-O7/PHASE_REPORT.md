# OrgResource O6–O7 phase report

Captured: 2026-11-15 (Asia/Manila)
Branch: `feat/o6-o7-reminders-react-2026-11-15`
Base: `65829bcdcd5445d7ff88893032c6f82db6de357e` (`feat/o4-o5-allocation-expiry-2026-11-15`)

## Delivered scope

- Added a durable, one-time reminder claim and dispatcher using Retrofit/Gson to the supplied course FakeTwilio endpoint. `DISABLED` remains the default. `FAKE_TWILIO` requires a loopback HTTP endpoint and uses fixed dummy Authorization. Real Twilio mode fails closed; no live SMS was sent.
- Rechecked status, project-end window, consent, and destination before dispatch. Claim, HTTP, and result persistence are separate; uncertain/stale claims become `UNKNOWN` and are not retried. Outcome updates lock and refresh the current Allocation so a concurrent lifecycle change is preserved.
- Optimized `ResourceComponent.list()` to use one grouped MySQL aggregate query and one Asia/Manila Clock sample for the entire list.
- Added a guarded test profile that only permits the isolated `orgresource_test` schema. O5 expiry scheduling is opted into only in its scheduler integration test; other test contexts keep scheduled jobs off.
- Added the O7 React/TypeScript/Vite app with HashRouter, one typed client for all five DTOs, and working Department and Resource list/create/update/delete screens. The Resource stale-total conflict reloads current server values, preserves the user draft, and requires an explicit review before resubmitting.
- Added the current Postman v2.1 reference for 25 original routes plus A09; marked the nine-entity/68-operation collection and contract artifacts historical. Added PR CI for Java 8 + isolated MySQL and frontend typecheck/test/lint/build.

## Verification evidence

| Area | Command or observation | Result |
| --- | --- | --- |
| Java 8 backend + real MySQL | From `backend/`, dot-source `%LOCALAPPDATA%\OrgResourceDev\Set-OrgResourceTestEnvironment.ps1`; run `mvnw.cmd clean verify` with the provisioned Temurin JDK 8 | PASS: Maven `BUILD SUCCESS`; 55 tests, 0 failures, 0 errors, 0 skipped. MySQL integration tests initialized the guarded `orgresource_test` schema. |
| Grouped Resource query | `ResourceListAggregationMySqlTest` | PASS: MySQL query initialization, active/expired Project-end boundary, reusable/consumable availability, untouched Resource zero aggregate, and exactly one Clock sample for the list. |
| Reminder lifecycle | `ReminderLifecycleMySqlTest` | PASS: four MySQL tests covered persisted SIMULATED outcome, repeat suppression, consent revocation before send, stale-claim UNKNOWN/no resend, invalid destination suppression, exclusive 24-hour upper bound, and two concurrent claimers yielding exactly one claim. |
| Retrofit transport | `FakeTwilioTransportTest` | PASS: three loopback HTTP tests covered form fields and dummy Authorization, accepted response parsing, 4xx REJECTED, 5xx/malformed success UNKNOWN without retry, and rejection of non-loopback targets. |
| Real course FakeTwilio integration | Built the seven supplied FakeTwilio Java source files with JDK 8 into a temporary directory, started the actual course app on `127.0.0.1:9997`, then started the built OrgResource JAR in `test` profile with the scheduled `FAKE_TWILIO` scan at 500 ms. Created fictional records through the real HTTP API, waited for the Project start, approved and issued a reusable Allocation, then polled GET `/api/allocations`. | PASS: API returned `status=ISSUED`, `reminderOutcome=SIMULATED`, `reminderAttempted=true`, `contactPresent=true`; public JSON contained neither `phoneNumber` nor its value. Direct MySQL query on Allocation 97 showed `status=ISSUED`, `reminder_outcome=SIMULATED`, `http_status=200`, `provider_message_sid=NULL`. This proves simulator acceptance only, not delivery. Both Java processes were stopped after the check; MySQL remains available. The simulator's temporary output logs were redacted; backend output was streamed to the task terminal. |
| Frontend install and static checks | Node `v24.14.1`, npm `11.11.0`; `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, `npm run build` from `frontend/` | PASS: clean lockfile install (241 packages); 2 component tests passed; typecheck, lint, and production build passed. The pinned jsdom 26.1.0 supports this installed Node version. Vite 8's runtime requirements are documented in the [official Vite 8 announcement](https://vite.dev/blog/announcing-vite8.html). |
| Real-browser frontend flow | `npm run e2e` from `frontend/`, using command-line headless Playwright Chromium against the actual Java backend and isolated MySQL test profile | PASS: one browser test created/removed a Department, created a Resource, induced a real stale-quantity 409, verified the draft remained and save was blocked until review, saved the revised Resource, changed stock to zero, and deleted it. |
| Postman import | Newman `postman-collection` SDK parsed `postman/OrgResource-Current-26-Routes.postman_collection.json` without running requests | PASS: standard Collection v2.1 schema and all 26 requests parsed. No endpoint was executed by this import check. |

The user-level test helper stores the isolated MySQL password outside the repository using the workstation's protected credential store. No database password or real provider credential was printed, committed, or placed in the Postman collection/CI. No test used the regular `orgresource` schema.

## Scope boundaries and remaining work

- The API remains 25/25 original routes plus A09. O6 adds no route or entity. The current application still has exactly five JPA entities.
- O7 implements Department and Resource UI only. Project, Requirement, Allocation, dashboard, and packaging UI phases remain.
- CLO9 image upload/serve requirements remain unresolved against the supplied sources. No image behavior was invented here.
- Real Twilio authorization/delivery, handset delivery, deployed hosting, production migrations, and instructor/Canvas acceptance were not tested or claimed.

## Git and CI

This report records local pre-push verification. The pushed commit, dependent PR, and remote CI result are recorded in the final continuation update after publication; the PR is not to be merged in this phase.
