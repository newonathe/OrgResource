# C8 bounded cycle 1 — packaged Requirements POST diagnostic

**Date:** 2026-11-23 (Asia/Manila). **Classification:** code change made, behavior NOT VERIFIED at time of recording. **C8 verdict: BLOCKED**.

## Exact Git source and failure

- Draft PR #25, feature branch `feat/extended-c-c8-domain-workflows-2026-11-19`, parent pre-edit SHA `900f52fc51b3652cb91993fd6a34c2346883c5a7`.
- That parent had [CI 45070699372](https://github.com/newonathe/OrgResource/actions/runs/45070699372) **FAILURE** in the Extended C packaged Playwright job: `connected O8–O10 workflows persist through the isolated Java/MySQL backend`, first reusable requirement POST follow-up was absent from the specific `GET /api/requirements` project/resource pair after 15 seconds. At this source: 62 Java tests passed, 36 frontend unit tests in the Extended job passed, Newman runs had 90 and 20 assertions without failures, and Playwright recorded 17/18 passing. Java 8 and standalone React CI jobs succeeded. These results do not explain whether the incorrect pair was submitted, a stale notification was read, the server response was unexpected, or a row was lost.
- Test source actually reviewed: `extended/frontend/e2e/orgresource.spec.ts` test from line 459 through the first two requirement creations (previous blob `44c470ccc1df3607391d096511aee38cf725efa6`). UI form inspected: `extended/frontend/src/pages/RequirementsPage.tsx` (`createRequirement`, form/selector), `extended/frontend/src/RelatedSelect.tsx`; API client: `extended/frontend/src/api/client.ts`. Backend component/transaction and DTO reviewed at `ResourceRequirementResource.java`, `C1Component.java`, `C1Service.java:258-273,329-339`, `ApiDtos.java:151-188`.
- The UI's `Requirement saved` notice follows `api.createRequirement`; it is not sufficient evidence of *which* project/resource pair was submitted. The server's `createRequirement` uses a transactional save-and-flush and returns IDs; its API list is scoped to selected organization. The failure therefore remains a **diagnostic uncertainty**, not a proven backend write defect.

## Implemented narrow test change

Commit `afd5682b7b230fb82d694d83028c37f90224240d` changes **only** `extended/frontend/e2e/orgresource.spec.ts`. For both reusable and consumable requirement creation it:

1. Confirms the selected Project and Resource option values.
2. Starts an exact POST `/api/requirements` response observer **before** submitting.
3. Asserts HTTP 201 and returned `projectId`, `resourceId`, and `quantity` match the selected pair.
4. Preserves the existing separate 15-second `GET /api/requirements` persistence check.

No product logic, Java, migration, stock ledger, course artifact, hosting or permissions changed. A response mismatch will now fail with the earlier actionable cause instead of an unexplained missing-row poll. It does **not** itself prove or repair a product defect.

**Execution evidence:** GitHub Actions [45071413400](https://github.com/newonathe/OrgResource/actions/runs/45071413400) was **IN_PROGRESS** when this file was prepared; local runtime execution unavailable. Do not mark this cycle green until that exact SHA's final job output is checked. Independent reviewer: **NOT_RUN**.

## Next actions before trusting this test

- [ ] **D1:** Inspect final run 45071413400 and packaged Playwright trace/error context. If it fails, identify actual submitted body, response status and returned pair; reproduce a deterministic defect before changing product code.
- [ ] **D2:** If it passes, repeat/rerun the single flaky scenario on a clean isolated MySQL environment to estimate intermittence; a single green run does not close the C8 reliability concern.
- [ ] **D3:** Distinguish backend failure, UI selection race, wrong test expectation, and prior notification. Add a focused regression only for a supported causal defect; preserve current strict persistence assertions.
- [ ] **D4:** Complete independent review, check exact full CI on the final pushed commit and report failures/skips.
- [ ] **C8-NEXT:** Continue the source-based 33-route InventoryResource/AllocationResource policy inventory and per-route real positive/meaningful refusal/SQL receipts. Existing 31 source-oracled operations remain unaccepted; 62 lacked source oracles at the prior snapshot. Complete the remaining 29 after this batch.
- [ ] **ACADEMIC / HOST:** Independently check Canvas/export/video/authorship deadlines; rotate secrets privately; verify deployed image and browser smoke read-only. No hosted import, live SMS, merge, or public release.

**Stop rule:** C8 remains BLOCKED under `evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md`, regardless of this diagnostic's outcome.
