# V2 product charter, use-case contract and pre-Canvas production mandate
**Status:** USER-APPROVED RELEASE ORDER; functional/architecture design remains PROVISIONAL. **Revised:** 2026-11-15. Sources C01–C03, G05–G10, A01–A07.

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

## Release phase boundaries — superseded 2026-11-15 (USER DECISION)
**The former “P0 academic → later V2” sequencing is obsolete; historical before-state is preserved in [25](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md).**

1. **O14 early academic baseline** — verify actual source/CI/Postman/React JAR, CLO9/Twilio questions, preserve a **runnable** Java8/Jersey2/Javax Validation/JPA/Retrofit/Gson/scheduling course artifact. O14 is NOT final academic acceptance and is not yet verified done.
2. **Post-O14, PRE-Canvas production engineering** — build supported, secure, separately versioned V2, authenticated tenant isolation, transactional inventory/custody/booking, v5 UI, migrations, actual hosted $0 feasibility and DR tests. Separate supported Jakarta candidate may not replace the literal academic executable.
3. **Production engineering acceptance (P-ACCEPT)** — actual target binary/DB security, race/idempotency, browser/Postman, TLS and verified live free host if production deployment is claimed, off-host backup + clean restore, operator and cost evidence. CI green or local-only demo is not a pass.
4. **Final independent academic compliance acceptance (A-FINAL)** — newly verify original executable, five entities/CRUD, Jersey/Javax/JPA/Retrofit-Twilio simulator/scheduling, all Postman routes, required course video/export, CLO9 and authorship/assistance. The production build cannot substitute.
5. **Student MANUAL Canvas submission** — only after P-ACCEPT then A-FINAL, by printed **2026-12-05** deadline subject to actual Canvas announcements. No automatic submission or invented instructor approval.
6. **Optional later organizational adoption** — actual Ateneo representative authorization, privacy/legal review and pilot are NOT prerequisites to technically accept a fictional-data production-capable product.

**Failure policy:** scope-cut optional integrations/analytics only with SRS/API/test revisions; do not cut auth, tenant isolation, stock correctness, backups/restore, course compliance or truthful cost proof. No $0 host = P-ACCEPT BLOCKED, **not** “local production.” Under 20 calendar days from Nov15, original 37–75 person-day bands make the schedule RED/high risk; reveal the consequence rather than silently reverse the order. See revised [18](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md).
