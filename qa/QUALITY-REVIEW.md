# Specification Quality Review

This review applies to documentation version1.0. It is an adversarial design review, not an implementation approval.

## Problems found and incorporated corrections

1. A naive sum of all allocations touching a query window overcounts sequential bookings. Corrected to a half-open endpoint sweep with grouped ends before starts, checked against75 independently sampled small cases.
2. Reusable issue can be double-subtracted if usable capacity excludes custody and commitments subtract custody again. Corrected to U=S+O, with one remaining commitment contribution for O; issue changes location without reducing usable ownership.
3. Consumable issue followed by consumption can deplete stock twice. Corrected by consuming at handover and forbidding consumable ISSUE/RETURN. Reservation decreases together with physical consumption.
4. Summing isolated shortage results gives every department the same free stock. Corrected by exposing candidate gaps as nonadditive and sweeping all unallocated reusable demand together; consumables aggregate demands against globally reserved current stock.
5. Finite reservations alone let an overdue item reappear. Corrected by extending outstanding overdue custody indefinitely for present/future planning and requiring actual physical return before later issue.
6. Damage can make a prior promise infeasible even when approval was correct. Corrected by preserving physical truth, reporting negative raw capacity/impacted commitments and blocking unsafe new approval/issue; physical observations are not rejected to hide the deficit.
7. One lifecycle enum cannot coherently represent requested approval and partial return progress. Corrected to four stored decision states plus ledger-derived fulfillment/custody/overdue/risk flags.
8. Editing an approved interval in place can silently overbook. Corrected to immutable ordinary allocation dates and a complete-set atomic unissued project reschedule, or explicit release/new request. Any recorded issue locks schedule replacement.
9. Optimistic child versions alone do not serialize two new approvals on the same resource. Corrected to a shared existing parent Resource lock, resource-first ordering, current reads and actual MySQL concurrency tests required.
10. Capturing a provider-managed JPA version for reversal fences can depend on flush timing. Corrected by a separate server-only Resource.revision counter incremented under the parent lock; InventoryTransaction.resourceRevisionAfter captures that ordinary value. JPA Version remains a separate edit conflict mechanism.
11. Arbitrary reversal of an old receipt could resurrect spent stock. Corrected to one latest-event reversal with a pool-revision dependency fence, vector negation and invariant revalidation. Later corrections are present observations with a reference, not silent journal surgery.
12. Historical deletion conflicts with inventory integrity. Corrected to real physical deletion of unreferenced setup/drafts and explicit409 refusal/correction alternatives for posted movements/audit. The stronger instructor interpretation remains a confirmation, not falsely claimed satisfied.
13. SMS send success can be lost before result persistence. Corrected to durable outbox phases, UNKNOWN recovery and no automatic resend after ambiguity; verified429/20429 is the narrow safe retry exception.
14. User IDs/roles in JSON or a header are not authentication. Corrected to resolved Actor plus component permissions and an explicitly insecure local DEMO profile; an authenticated profile fails startup without an actual verified principal adapter.
15. A realistic demonstration cannot create future reservations and immediately issue them without time travel. Corrected to currently active relative intervals for A/B, future C, and no wholly past allocation/backdated movement. Clock manipulation is test injection, never an HTTP endpoint.
16. A source pack's third-party cases and internal instructions are not local proof or current task authority. Corrected by the applicability assessment, provenance/coverage inventory, specific2025 report verification and counterevidence from existing digital tracking.
17. An immutable entity delete endpoint cannot truthfully show a successful204 example. Corrected catalogue/Postman to explicit409 refusal for the two immutable entities, while component methods throw the standard mapped domain exception.
18. DTO examples and Spring Data paths can drift independently across documents. Corrected by a shared operation/model registry generating catalogue, methods and Postman coverage, with automated path/name/bijection checks. This catches structural mismatches; it does not compile Java or prove every domain branch.

19. Initial-state examples on approve/submit/movement actions and digit-leading DTO names would mislead a coding agent. Corrected to action-specific response states/effects and valid Op-prefixed DTO/query identifiers. Checks now assert resulting project/allocation states and movement kinds as well as identifier syntax.
20. A runtime-create demonstration cannot authenticate as its first administrator if all seed data is disabled. Corrected with an explicit local-only, fresh-database bootstrapAdmin method independent of optional full fixtures; its generated id is captured from startup rather than assumed to be1.
21. The first Word render inherited a decorative title border and squeezed horizontal diagrams. Corrected the title styles and reflowed/wrapped diagrams into readable print proportions; full Mermaid source remains in03.

## Remaining limits and confirmations

Academic interpretation: RetroJit/Retrofit, FakeTwilio/live, image coverage and historical-delete wording need instructor/class confirmation. Deployment: exact working dependencies and real MySQL settings need implementation verification. Shared live use requires actual authentication and permission/retention review; a local demo is not production secure. SMS account/sender/recipient eligibility remains unverified. Empirical demand/adoption/savings are not proven and no pilot is requested. Quantity-one resources support distinctive devices, but pooled items do not have serial identity or maintenance history. Fractional consumables, repairs, future receipts and multi-location stock are explicitly excluded.

The static checks verify nine entities,68 route contracts,20 repository methods, complete create/list/read/delete coverage, request example JSON structure, referenced method names and derived field paths,99 ordered demonstration requests,8 negative requests,9 diagrams, and bounded arithmetic fixtures. They are documentation checks. Application tests, actual Postman execution, database transactions and external delivery all remain NOT RUN.
