# C8 acceptance closeout — verification batch report

**Snapshot:** November 23, 2026, Asia/Manila. **C8 status remains BLOCKED.**
Feature baseline: `6be1b2e0c3f52077ad5f2fe02594ac96a089d6e9`; last source revision reviewed for this batch: `3d094157f2ede297e9aa3b3db4d3c7d5e23ed8f3`. The exact next CI SHA may change when this report is committed. PR: [#25](https://github.com/newonathe/OrgResource/pull/25).

## Authorized objective, controls and limits

Owner asked for one accelerated plan → copy-ready prompt → implementation batch that keeps all public-safe work and context on GitHub. Preserve Course Final A (frozen `7f8c0746da18fd54b47f430ac1435ac36e605d67`), existing Flyway versions, staging MySQL, first-owner credentials and previously tested Northflank service. No merge, destructive cloud action, real SMS, release approval or live import is inferred.

**SOURCE/TEST GOAL:** Improve C8’s 93-route registration proof, strengthen the connected browser requirement/cleanup test and make Java 8 CI provisioning resilient **without changing the academic Java version**. This batch intentionally changes only test/CI files. It does not close the remaining independent 93-route authorization acceptance contract.

## Source coverage and file-by-file review

| Path | Review depth | Old → new | Dependency and adversarial test | Current result/limit |
| --- | --- | --- | --- | --- |
| `extended/backend/src/test/java/edu/orgresource/extended/config/C8JerseyRegistrationTest.java` (new) | Full authored file plus `C1JerseyConfiguration.java` and all 15 Jersey resource classes' route and `@Produces` declarations | Verifies config contains 15 registered resource classes; 93 unique HTTP verb/path identities; media types for JSON, JSON+CSV reporting and PNG/JPEG images | Fail on missing/extra registration, duplicate route, altered count, incorrect media declaration; does not prove runtime dispatch or authorization | CI failures exposed overgeneralized media assumptions, corrected using real endpoint source; final exact-SHA run remains to be checked |
| `extended/frontend/e2e/orgresource.spec.ts` | Relevant lines in connected O8–O10 MySQL/Playwright workflow and `RequirementsPage.tsx`, `ResourceRequirementResource.java`, `C1Service.java` | Checks specific persisted Project–Resource Requirement after each asynchronous browser submit, then verifies unrelated temporary deletion does not remove original Requirements | Guards stale success-toast race, missing record identity, accidental delete cross-effect; a real absent row must still fail within 15 seconds | Source test strengthened; root cause of earlier missing ID remains an inference until rerun/trace |
| `.github/workflows/verify.yml` | Complete Java 8 setup section, Extended C and React CI steps | Temurin Java 8 preferred; Zulu Java 8 fallback only if Temurin setup fails; explicit `javac 1.8` required | Fail if neither distribution provides genuine Java 8, do not skip tests or relax vendor security; runtime Java 21 remains unchanged | Prior Java 8 failures were external invalid gzip before tests; multiple exact-source Java 8 jobs subsequently passed |

**Unchanged high-risk dependencies inspected (relevant ranges):** `extended/backend/src/main/java/edu/orgresource/extended/config/C1JerseyConfiguration.java`, `extended/backend/src/main/java/edu/orgresource/extended/rest/ReportingResource.java`, `ResourceResource.java`, `ResourceRequirementResource.java`, `extended/backend/src/main/java/edu/orgresource/extended/service/C1Service.java`, `extended/frontend/src/pages/RequirementsPage.tsx`, `evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md`, `docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md`, `AGENTS.md`. The full restricted evidence packet was inaccessible in this session, and every uninspected route delegate/policy remains UNREVIEWED.

## Evidence and recorded failures

- Prior completed baseline `c0409a98a3bd`: [all three CI jobs green](https://github.com/newonathe/OrgResource/actions/runs/45062378023). This establishes previous code, not this batch.
- `6be1b2e0c3f5` [CI failure](https://github.com/newonathe/OrgResource/actions/runs/45063324259): Java 8 setup downloaded non-gzip data, and a single connected Playwright workflow attempted to dereference an absent reusable Requirement at its Allocation step. Newman and previous Maven backend assertions in that run passed. Neither failure is silently reclassified as a product bug.
- New Java registration test first failed on `ReportingResource` because reporting explicitly declares JSON/CSV at method level, then on `ResourceResource.getImage` because it deliberately emits PNG/JPEG instead of JSON. Both false assumptions were removed after inspecting real source.
- A revised Playwright test initially caused TypeScript nullable-ID compile errors; explicit type narrowing was added. Then a bounded `expect.poll` awaits the exact committed Requirement records after asynchronous UI actions. This is an API-state proof, not a toast-only substitute.
- An explicit Java 8 compiler check and vendor fallback were added. **No production Java or database migration changed.**
- Latest source `3d094157f2ede297e9aa3b3db4d3c7d5e23ed8f3` [CI run](https://github.com/newonathe/OrgResource/actions/runs/45064883139) was queued at report construction. **Result PENDING: do not state PASS until logs confirm it.**

## Formal C8 bounded gate (still open)

1. The 93 source annotations are tracked in `C8_ROUTE_INVENTORY.json` and drift-check CI. This batch tests Jersey `ResourceConfig` registrations. The real packaged HTTP handlers, complete policy oracle, meaningful allowed/denied requests, database postconditions and independent review remain a distinct acceptance gate.
2. Historic IR4 ledger of 199 executed, 48 reviewed exclusions and 22,073 unrun cases retains its reported scope; never convert unrun to passed. Apply owner-approved bounded v1 contract.
3. Existing source-only route ledger `C8_ROUTE_REVIEW_LEDGER.md` does not assign passing roles or SQL proof without source-reconciled receipts.
4. No separate reviewer in this conversation; **independent review NOT_RUN**.
5. Full 96-label live demo remains BLOCKED: restricted approved manifest absent; previous importer is local-only/fresh DB; hosted staging has a valid existing owner; cloud off-host backup/restore not verified. No hosted data import performed.
6. Cloud credentials and recovery codes previously exposed require private rotation. Suppressing a logger does not retroactively fix the incident.
7. Course A Canvas deadline, authorship and submission evidence are student-only and unverified.

## Exact next actions

- Inspect the latest exact-SHA GitHub verification, including Java 8 fallback choice, all 60 Extended backend tests, demo-plan preflight, Newman and Playwright. Preserve any failure log and fix the narrow defect if reproduced.
- Promote this batch to `deploy/northflank-c8-staging` only after a green feature-head run. Compare protected Course A trees before/after; do not assume Northflank build status proves deployment.
- Retrieve the restricted route proof packet **privately**, map each real existing receipt to the 93-route policy ledger, and execute targeted missing allowed/denied and denied-write MySQL checks.
- Prepare (but do not execute) additive demo import only after approved source manifest and isolated staging backup/restore are available.
- Record exact final SHA, actual CI outcome, independent-review outcome and remaining C8 gaps. Keep C9 on hold until explicit C8 acceptance/authorization.

**Verdict:** This is a bounded engineering verification improvement, not C8 closure or a production-security certification.
