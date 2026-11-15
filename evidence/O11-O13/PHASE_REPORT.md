# O11 + O13 continuation report

**Status: LOCAL ACCEPTANCE PASSED; RELEASE PACKAGE AND REMOTE CI PENDING.** CLO9 image scope remains unresolved and is excluded from any completion claim.

## Verified local evidence

- Branch: `feat/o11-o13-system-acceptance-release-2026-11-15`; base: `725ad0ad796b787bf63e67db728ff2882f6a7252`.
- Local Java service ran with protected workstation credentials against the isolated MySQL schema `orgresource_test`. All five list endpoints returned HTTP 200. `SHOW TABLES` returned exactly `allocation`, `department`, `project`, `resource`, and `resource_requirement`.
- Frontend gates passed: `npm ci`, `npm run typecheck`, `npm test` (15 passed), `npm run lint`, and `npm run build`.
- `npm run e2e` passed all five Playwright tests against the real Java/MySQL application: POST response loss and review safety, browser history, connected O8–O10 workflows, responsive light/dark screenshots, and pointer/motion behavior.
- All four current Newman v2.1 acceptance collections passed against local Java/MySQL: 73 requests and 105 assertions, zero failures. Route mapping and executed counts are in `ROUTE_PARITY.md`.
- Browser regression verified that the screenshot-identified “Local academic prototype” and “CSCI 180.05 / Philippine time · Asia/Manila” labels and duplicate prototype caption are absent.
- Newman, Java HTTP, Playwright, and direct JDBC checks used fictional data in the isolated test schema. No database reset or credential disclosure occurred.
- The development process was stopped cleanly. Ports 8080 and 5173 were confirmed closed before packaging.

## Release gates still pending

- Commit the reviewed source changes, then run `scripts/package-release.ps1`. This produces the executable JAR, complete tracked-source ZIP, Java/Eclipse source ZIP, and SHA-256 manifest under ignored `deliverables/O11-O13/`.
- Start the packaged JAR, verify `/`, its bundled JavaScript asset, and the five API list routes, capture and inspect a current localhost screenshot, then leave the service running for the user.
- Push the feature branch and open its stacked pull request. The workflow's Java 8 build and packaged-JAR smoke must complete successfully before reporting remote CI as passed.
- Confirm artifact sizes and recompute each SHA-256 from the manifest.

## Academic acceptance boundary

- CLO9 image scope remains **UNRESOLVED** in `docs/compliance/CLO9_IMAGE_DECISION.md`; no image subsystem was added.
- Canvas/current deadline and rubric were not checked. No video was fabricated, no Canvas submission was made, and no instructor or course acceptance is claimed.
- The student must complete the authorship certificate, accurately acknowledge assistance, and complete any required peer/group evidence.
