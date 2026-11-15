# Independent adversarial review and revisions
**Status:** INDEPENDENT DESIGN CRITIQUE / PROVISIONAL REVISIONS, no implementation proofs. **Date:** 2026-11-15. Sources C01–C03, A01–A07, E01–E27.

## Attack the premises first
1. **Market problem may not exist.** A06 indicates a narrow past budgeting coordination instance, while A07 proves at least one org already advertises digital inventory. A02/A04 show official project/venue procedures. Revised promise: *fictional product demonstrating a testable resource workflow*, not "Ateneo urgently needs it" or quantified savings; compare spreadsheet and Snipe-IT before custom work (H1–H5).
2. **Course preservation can be undermined by a “free stack” swap.** Cloudflare Worker/D1 is incompatible as the *sole* Jersey/Javax/Spring Data runtime. Revised: permanent frozen V1 + distinct supported V2 track with shared invariant fixtures; deploy neither V1 old runtime nor masquerading Workers as course compliant.
3. **No 24×7 perpetual free guarantee.** Oracle capacity/reclaim/card holds, D1 quota shutdown, Render DB expiry, Supabase pause, Google egress all challenge the claim. Revised: $0 for demonstrably bounded workload if an eligible provider and recovery plan pass; otherwise local-only, no paid fallback.
4. **V1 inventory arithmetic isn't future-booking-safe.** Original global holds ignore time-window overlap and late physical custody. Revised [11](11_WORKFLOWS_STATES_AND_INVARIANTS.md) separates planned window holds and all outstanding issues; approved later booking can be at-risk from late return, which no booking algorithm prevents without human custody completion.
5. **Tenant isolation isn't "put org_id in every table."** FK and query joins can still read someone else's rows. Revised composite `(org_id,id)` FK, global principal bound only via active membership, domain-level authorization and per-route cross-tenant tests.
6. **Initial V2 scope too broad.** Eleven tables, auth, partial returns, audit, hosted free deployment and comprehensive UI may exceed available time. Revised: prioritize invitation/tenant/security and conservative whole-batch or explicitly supported usable partial return, defer damage, barcode, email, images, accounting and campus integration. If complexity exceeds budget, cut hosted demo before core correctness or course compliance.
7. **Hosted free backups may be imaginary.** A database on one reclaimed Oracle VM with same-volume snapshot is not a recovery system; Supabase free has no daily managed backups. Revised: encrypted off-host export, independent key and measured restore; if no independently free storage/recovery, live tenant gate fails.
8. **Auth bootstrap is a trust problem.** Allowing any registrant to name a real Ateneo org and self-appoint OWNER creates impersonation. Revised F02 organization creation should be **disabled in any real institutional namespace** pending trusted invitation/verification. Only fictitious/demo orgs may be self-created by arbitrary accounts. Real org claim requires verified authorized representative; no email-suffix-only auto-approval.
9. **No neutral 'all orgs' hierarchy.** Registered/accredited, COA-M/LIONS and student-led are different public facts/dimensions. Revised optional claimed affiliation with unverified label and provenance, not rigid exclusive enum.
10. **CSV export is not necessarily full restore.** It can omit permissions, event ordering, privacy rules and IDs. Revised separate versioned machine-readable full tenant backup, with CSV as user-friendly interchange, not sole DR format.
11. **Audit events consume scarce storage and may contain PII.** Proposed 0.5m events could exceed 500MB D1 with indexes; retention policy may conflict with historic stock trace. Revised measure actual row sizes, compact/archive without breaking stock invariant; never hard delete unexpired compliance history silently.
12. **Idempotency for stock writes is subtler than 24h keys.** A late client retry after expiration might double apply. Revised expose durable business operation receipts/references and refuse duplicate terminal state even beyond receipt TTL; transaction uniqueness on movement/allocation action where feasible. Document unsupported indefinite exactly-once guarantee.
13. **MySQL and D1 concurrency aren't interchangeable.** `FOR UPDATE` only applicable to supporting transactional DB. Revised D1 requires documented atomic conditional-update/batch proof or reject, no syntactic substitution.
14. **"No production personal data" and "multi-tenant app" clash initially.** Identity provider integration could collect real addresses during demo by accident. Revised public D-READONLY with fictional principals or isolated sessions; real account opt-in only after privacy policy, auth recovery and host/security gates.
15. **Visual v5 lock could mask accessibility defects.** Token-level contrast from G10 does not certify glass over all gradients. Revised rendered screenshot/automated and keyboard tests on actual composited backgrounds, with solid fallback, reduced motion, no changes to identity.
16. **Published 2026 provider and campus pages can shift.** Dated source ledger and terms need check before enrollment/deployment; no indefinite contract claim.

## Revision log for the preceding specifications
| Revised item | Before potential assumption | Correction / acceptance |
|---|---|---|
| R-D01 | A public signup can create an org and claim Ateneo membership | Only demo/unverified self-created organization; **real org owner-claim gated** to authorization evidence later; auth tests forbid generic org claim |
| R-D02 | Future booked date means equipment available at future date | Outstanding unreturned issues block issuance, atRisk flag for later reservations; named custodian reconciliation |
| R-D03 | Stock model "total usable" is automatically perfect | Ledger/snapshot dual reconciliation plus explicit damage/loss **unsupported** until built; asset serial-level tracing optional |
| R-D04 | Key TTL gives perpetual exactly-once | Key+body within TTL and terminal state uniqueness beyond; lost response read-back |
| R-D05 | Any `@ateneo.edu` identity proves staff/officer authority | Email is identity hint only; active membership & authorized inviter determine role |
| R-D06 | Cloudflare/Oracle guarantees $0 24×7 | Account and quota measured; local-only fallback; no business SLA |
| R-D07 | Snipe-IT lacks OrgResource features | Explicitly verify against current released alternative before bespoke justification |
| R-D08 | Five academic entities can be directly transformed into eleven | V1 frozen, V2 new schema and opt-in migration fixture; never rewrite course artifact |
| R-D09 | One read-only file export is backup | Encrypted offsite full format+restore test and key custody |
| R-D10 | No current stakeholder meetings blocks project | Desk research and synthetic tests proceed; optional pilot human gate later |
| R-D11 | All project statuses called approved mean OSA approval | UI label **internally approved resource allocation only**, never institutional event approval |
| R-D12 | Feature planned means it exists | Explicit source/test/publish evidence and PR commit required for each status |

## Independent go/no-go verdict
**Research path: PROCEED** with source-grounded V2 design and private local synthetic tests. **Public real-user production: NO-GO** today (auth/tenant code unimplemented, provider/free cost unverified, backups/restore not tested, organization authority unverified). **O14 academic complete: NOT VERIFIED**. **Public fictional demo: only after implementation, isolation, cost and explicit owner release authorization.** Residual errors after critique should be tracked in [RISKS](RISKS.md), not hidden.

## Outstanding technical research challenge
Formal proof and independent real DB test that time-window reservation and partial physical custody meet all invariants for both MySQL/InnoDB and optional D1 transaction engine; a production-supported dependency BOM that runs on eligible $0 host with TLS/auth and recovery; and security ownership without actual org approvals. These are actionable gates, not excuses to invent pass results.
