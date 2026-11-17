# What OrgResource becomes — plain-English explanation of EVERY current screen, addition and remaining work

**November 16, 2026 | PLANNING/REVIEW ONLY.** This is an explanation, **not** a newly built or tested feature. Compared with the source of O14 academic [draft PR #8](https://github.com/newonathe/OrgResource/pull/8) at `2dd336ed39cecd417a8bce3388b303e688e95dee` (not with earlier `main`). Future scope: [CP35](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md), [CP42](69_CP42_FINAL_OWNER_DECISION_REVIEW_AND_CURRENT_BUILD_DELTA.md), [CP43](70_CP43_FIXED_WRITTEN_COURSE_SCOPE_AND_HANDOFF_GO_NO_GO.md). The course's final written directions are the supplied syllabus and ESP project assignment. Everything below called **PLANNED** must still be written, tested and reviewed.

## The point of the project, without computer jargon

Imagine organizing a workshop. You need eight chairs, a projector, and disposable cups. There are several committees, overlapping events and multiple people borrowing supplies. A spreadsheet can list quantities but won't necessarily explain whether an item is promised to somebody, physically lent out, returned or used up. **OrgResource puts the inventory, planned event needs, approvals, people responsible and history into a single consistent workspace.** An organization's records remain private from another organization.

For example, with **10 reusable chairs**, a project requests **8**. While PENDING, none are reserved. When approved, 8 are promised and 2 are left for new approvals. When issued, 8 are recorded with a borrower; the organization still owns 10, but only 2 are currently free. When returned, all 10 are usable again. With **50 disposable cups**, approving 20 holds 20; consuming 20 reduces the physical total to 30 **once**, not twice. The intended final system leaves a clear explanation of every stock movement.

**What it is NOT:** an official Ateneo inventory, a replacement for university permission systems, a proven deployed service or measured community need. The first version uses fictional data and identifies roles within fictional groups; it does not verify anyone's Ateneo enrollment or leadership.

## The current app: eight real screens, and what changes on EACH

| Current screen | What actually exists in O14 | What would change in the one final product | Remaining to do / simple acceptance test |
|---|---|---|---|
| **Overview** | Shows current totals from local Java/MySQL: pending requests, active projects, shortages, issued/overdue/consumed batches, departments, resource types. Refresh and loading/error states exist. | Show only selected organization's data, with permitted actions and clearer overdue/stock-change signals. | Connect account/org-scoped results; log in as two organizations and prove changing workspace doesn't show the previous one's numbers. |
| **Departments** | Add, list, edit/search, and physically delete when no Project depends on it. | Each organization has separate teams; names may repeat across organizations; change permissions. | Org-specific name validation and true create/read/eligible-delete tests; other organization's IDs must be invisible. |
| **Projects** | Schedule events linked to Departments in Philippine local time; add/edit/list/delete and guard bad dates. | Assigned project leads, role-safe edits and meaningful upcoming/running/ended labels derived from time, not unimplemented status buttons. | Build lead assignment and enforce date boundary rules; check a project just before, at and after start/end. |
| **Resource Library** | Create reusable/consumable items, view physical and available quantities, search/filter, edit stock total and delete qualifying rows. | Record every **received item**, **count correction**, **issue**, **return**, **consumption**, and its reason in a permanent stock log. Add private photo upload/view. Archive historically used items instead of deleting their history. | Build inventory-history database, receive/adjust forms, image storage/HTTP GET, safe retries/locks, return/consume tests, real deletion for never-used zero-stock fixture. |
| **Requirements** | A project states quantity needed from one Resource. Shows required, already covered and shortage, supports edit/delete. | Keep this simple, but use reliable effective holds and per-org data; an approval must not oversubscribe shared stock. | Simulate competing projects requesting more than available, verify shortage and right role, test expiry boundary. |
| **Allocations** | Request whole batch with borrower name, quantity and optional *fictional* reminder contact; approve, issue, return, consume or cancel depending on type/state; revoke SMS consent. | Choose eligible organizational member for public reusable requests; enforce who may approve/hand over; show audit and ownership; prevent accidental double-click issuing/returning. | Member selector, role rules, immutable stock movements and replay protection; reusable and consumable demonstrations with correct stock after restart. |
| **Reminder Activity** | List soon-due/overdue issued reusable items and scheduler results. Current FakeTwilio is a local simulator, reminder sending OFF by default. | Continue due reminders; add safe **real Twilio Messages API integration code** as requested by assignment, separate from local simulator, with messages still disabled until authorized/configured. | Retrofit/Gson API adapter and error tests, scheduled no-duplicate attempt, consent revocation, truthful SIMULATED/TEST/PROVIDER status; no unapproved real phone messages. |
| **About** | Explains the five original business concepts and whole-batch life cycle, plus local fictional-data caveat. | Explain new privacy/roles, limitations, real version/source state and truthful demo information. | Update only after features actually work; test labels accurately reflect code. |

**Current eight-screen visual language:** academic blue/gold translucent panels, light/dark preference, responsive layouts, accessible controls and motion settings. These are **existing academic UI source features**; don't throw away functional design to make a second app. Improvements must be connected to the real server, not mock pictures.

## New pages/experiences missing from O14

| New user-visible feature | Everyday description | Work needed before claiming it is ready |
|---|---|---|
| **Login / logout** | A user signs in and sees only actions for which they have permission. | Actual secure password/session/CSRF setup; wrong password/locked account/logout/replay tests; no fake login screen disconnected from server. |
| **Account recovery** | A user who loses their password has a supported, private way to regain access. | One-time recovery codes, secure replacement, abuse limits, session revocation, accurate success/failure screens. |
| **Invitations / first owner** | An owner invites members using limited-life, single-use invitations; a new user can create a fictional account. | Token hashing/expiry/replay resistance and safe initial owner setup; cannot treat invite possession as university identity verification. |
| **Workspace / organization switcher** | One person can be in more than one organization, but each group has a separate inventory cabinet. | Organization ownership rules, private DB filtering and cleared UI caches; demonstrate another org cannot read photos, history, reports or numeric IDs. |
| **Member list & roles** | Different people can coordinate projects without everyone being able to alter stock or ownership. | Owner, Admin, Custodian, Lead, Member; invitation, change/suspend/transfer and last-owner protection tests. |
| **Account settings** | Change password, manage security, view fictional profile and safely end sessions. | Real API operations, no password leaking into the UI/logs or local storage. |
| **Activity & stock history** | See what changed, who performed it, when, and whether a counted item was received, consumed or returned. | Immutable append-only movement and audit records, scoped history views and reconciliation to current totals. |
| **Exports** | Authorized staff can download their own group's information; a copy of data is **not** the same as a reliable disaster backup. | Tenant-safe exports and verified external backup/restoration including correctness/security checks. |
| **Resource image** | Upload/view a real photo of equipment to prevent confusion. | Upload and GET exact image bytes, MIME and size limits, unsafe file rejection and privacy checks. This satisfies the syllabus's CLO9 learning outcome in the conservative implementation. |

## What changes behind the scenes, in simple words

1. **A real lock on your organization's cabinet.** Existing O14 code has unrestricted organization-wide database searches. The new code must filter every request by the organization the logged-in person belongs to—down to the database, not just hiding the wrong buttons.
2. **A real receipt book for physical quantities.** Instead of merely changing a number from 10 to 8, the final app records each stock receipt/correction/handout/return/consumption and ensures the history adds up. Approved reservations and items out with borrowers are different.
3. **A safety net against a double click.** Two staff members approving four of five remaining chairs must not both succeed. If internet briefly disconnects after clicking **Issue**, the user must be able to review what happened without issuing twice.
4. **An updated technical foundation, but only if verified.** O14's old academic runtime meets many course examples but should not be publicly exposed. The new runtime must still execute the *literal* required course technologies (including Javax Validation and actual Jersey). The plan expressly requires a small test (S1) before a whole rewrite; a document cannot prove it compiles.
5. **A plan to recover when something breaks.** Source branches, CI, a real clean-start demo, backups, restoring a separate database, and rollback—not merely a green page or spreadsheet export.

## What we will deliberately NOT promise in this first build

No partial returns, lost/damaged item accounting, booking by overlapping hourly time slots, QR/barcode integration, procurement/finance, public unrestricted signup, real campus account verification, university-controlled physical stock, automatic real SMS, free-forever hosting, or measured deployment/adoption. These are later design possibilities, not missing course deliverables.

## Course work and future implementation stages

- **Actual source-preserving course work:** 5 original domain entities, 26 current Jersey routes, create/read/eligible delete on all, `@Autowired` controller→Component, real MySQL/JPA/JPQL and literal `javax.validation`, scheduling, JSON and full Postman. Keep Retrofit/Gson FakeTwilio simulation AND implement a real Twilio REST API client (no live sending unless permitted). Add actual image receive/serve. Written source: syllabus and ESP assignment. The current O14 code does NOT yet include image receive/serve or real provider transport.
- **S0:** verify repository facts, 26 routes, five source entity classes, tests and risks; **read-only**.
- **S1:** run isolated bounded technology proof; STOP if required old/new technologies cannot coexist in a safe supported application.
- **S2:** choose exact tested versions/database/API design.
- **S3:** implement account/org/permissions safely.
- **S4:** implement the stock ledger and correct concurrent operations.
- **S5:** implement original+new API workflows, image and Twilio integrations and full Postman validation.
- **S6:** extend the connected React UI and test real workflows across screens.
- **S7:** secure release/build/host/backup, subject to account/cost approval.
- **S8:** independently test production-style safety and restore.
- **S9:** independently test every academic requirement on the **same product**.
- **S10:** student actually records/discusses the project, truthfully acknowledges help in the Certificate of Authorship, conducts peer reviews only if other group members exist and submits to Canvas.

The assignment's **printed final deadline is December 5, 2026**, and the syllabus's general grace language is NOT a guaranteed project-specific extension. A production-first full build is ambitious in this timeframe; no agent should silently cut academic requirements or claim completion.

## Current repository and local folder — avoid accidental overwrites

- `main`: an **earlier** branch, NOT the latest academic O14 implementation.
- Academic `feat/o14-academic-final-acceptance-2026-11-15` (draft PR#8): current reference application, should be preserved.
- Research `docs/orgresource2-research-continuity-2026-11-15` (draft PR#7): planning and implementation prompts, not proof code exists.
- Future experiments/implementation belong on **new isolated branches** and small reviewed PRs.
- Cloud Codex cannot update a Windows directory automatically. To update a local folder safely, first inspect the exact folder, `git remote -v`, `git status --short --branch`, then `git fetch origin --prune`. If the desired checkout is clean, `git pull --ff-only origin <exact-checked-out-branch>`. If dirty, STOP—no auto-stash, reset, clean or overwrite. To inspect another unmerged branch without disturbing the original checkout, offer a separate `git worktree` in a sibling folder. See [copy-ready Codex master handoff](71_CODEX_MASTER_HANDOFF_STAGED_EXECUTION_AND_LOCAL_WINDOWS_SYNC.txt).

**Evidence boundary:** This explainer crosschecks source and plan; it is not a record of new Java/SQL execution, cloud deployment, student submission or personal data. Actual release claims require later stage-specific test artifacts.
