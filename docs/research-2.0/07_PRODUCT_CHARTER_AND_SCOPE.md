# V2 product charter, use-case contract and scope
**Status:** PROVISIONAL RECOMMENDATION for review; no user-approved implementation design. **Date:** 2026-11-15. Sources C01–C03, G05–G10, A01–A07.

## Product claim and non-claim
**Goal:** independent student-organization-owned resource planning, reservations and custody with private multi-organization workspaces and deterministic auditability, first demonstrated using entirely fictional data. A visitor sees a functioning software demo, *not* a campus service. No measured need or adoption is asserted. University facilities, procurement, tuition, payments, event permits and OSA/CFMO approvals are **outside** product authority.

## Workspaces and roles — local permission levels only
- Product operator: incident/restore/service admin; ordinarily no access to tenant contents without logged exceptional process.
- Workspace admin: invites/removes members, changes role assignment, reassigns officers, configures resource policy; cannot prove Ateneo status.
- Custodian: creates/adjusts org-owned stock; issues/receives items; runs counts and approves requested quantities under workspace rules.
- Project lead: makes internal projects, requirements and reservation requests; can inspect only authorized tenant records.
- Member/borrower: sees permissions-limited catalog and own custody; cannot self-approve.
- Public demo viewer: no credentials to production, isolated synthetic sandbox only; no real contact or text messages.

One user may have distinct roles in multiple orgs, no cross-org view by default. Two workspace admins recommended to mitigate lost account; invite acceptance must verify invite-token and authenticated identity. Affiliation name and campus email **do not** confer admin rights.

## Minimum coherent real first release (proposed P0)
Invited accounts + membership/RBAC; create/private organization; resource stock with type and unit; project and date-range demand; atomic capacity checks; request→approve→issue→return or consume; cancellation/expiry and audit; lightweight in-app activity feed; CSV export and recoverable backups; accessible Academic Glass v5 responsive UI; safe fictional public demo separate from real tenancy; health/metrics and bounded pagination. Transactions, history, tenant isolation and backups are non-negotiable.

## Excluded from P0 or not automatic
No campus single-sign-on, institutional project approvals, CFMO equipment/venue reservation, real SMS/email delivery, official org registry verification, payments, physical barcode equipment, large-file images, automated procurement, forecasting/ML, accounting, public searchable student directory, arbitrary self-service org claim, webhooks, mobile native apps, microservices or paid infrastructure. Optional later: multi-custodian approval, damage/loss workflow, scheduled item-level maintenance, advanced booking recurrence, audit exports, full import. Distinguish *not built yet* from *deliberately deferred*.

## Journey and release criteria
J1 Admin creates demo workspace (or seeds it), invites fictional custodian and project lead; acceptance: cross-workspace IDs never authorize data. J2 Lead requests 4 of 5 reusable units for future window A; custodian approves; another project overlapping A requesting 3 is refused; nonoverlapping B may be approved subject to custody rules. J3 Custodian issues 4, returns 3 and documents 1 outstanding/lost **only if partial-return model is implemented**; while P0 design may defer damage, it must never silently treat a partial physical return as full. J4 Admin exports tenant JSON/CSV+schema/version, restores test replica and compares invariant checksum. J5 Demo visitor cannot invite a real email, access another sandbox or retain writes indefinitely. UI has keyboard/contrast/reduced-motion checks. All journeys are synthetic acceptance plans, not results.

## Release phase boundaries
**P0 academic:** preserve current V1 separately (course deadline Dec5). **P1 V2 local security/model:** redesign tenant model and tests, no public real users. **P2 hosted fictional demo:** only after account/quota/restore and isolation verification, owner permission for public publishing. **P3 optional pilot:** explicit representative authorization and privacy/legal review. No public deployment action authorized in this research.
