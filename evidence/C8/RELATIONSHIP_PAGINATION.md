# C8-PAGE-08 relationship reads

- Projects uses the bounded Department selector for filtering and creation. The Project detail dialog pages Requirements and Allocation history independently, each bound to the same selected Project and authorized role.
- Requirements uses searchable paged Project and Resource selectors. Allocations uses a paged Requirement chooser for create and filtering. Reminders loads issued Allocations and displays the associated Project deadline from the page.
- Workspace results carry the related labels. The server limits relationship and coverage queries to the visible page IDs. React preserves the current selection across searches and pages, cancels stale reads and offers a retry when the page fails.

## Verification

Run `pwsh -NoLogo -NoProfile -File scripts/verify-extended-c1.ps1 -VerifyRecovery` from the repository root on Windows. The local November 19 run passed all 53 Maven tests, 25 Vitest tests, 13 connected packaged Chromium tests, the independent Vite and restored-package smokes, and both Newman collections with 60 requests and 110 assertions. Recovery verified 18 tables, retired sessions, durable replay and archive/key rejection in a separate MySQL process. The application used disposable fictional data and the verifier stopped its isolated processes.

- `C1IdentityAcceptanceTest.C8WorkspacePagesReturnEveryResourceAndAllocationWithScopedContinuation` verifies exact SQL IDs, 101 Project Requirements, Project-bound continuation, tenant boundary, nested relationship fields and contact omission. Existing 1,001-record inventory/allocation cases remain in the same test.
- The Playwright test named `Resource Library traverses 101 matching resources with bounded server pages` creates and selects a Resource beyond page one, independently pages Project Requirements, finds an Allocation Requirement beyond page one, creates the linked request, and verifies that the five compatibility list endpoints see zero reads in that journey.
- Compared Course Final A backend and frontend trees with `7f8c0746da18fd54b47f430ac1435ac36e605d67`. Compared the migrations and Postman tree with the previous C8 checkpoint. These checks showed no differences.

These results cover the implemented UI paths and fixture sizes. C8-D operational scale, approved demo data and any expanded borrower/LEAD permissions have separate acceptance gates.
