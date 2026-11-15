# OrgResource 2.0 risk and contingency ledger
**Status:** PROVISIONAL RECOMMENDATION. **Date:** 2026-11-15. Scale: severity 1–5 and likelihood 1–5 are expert planning estimates, NOT measured incident rates. Gate owners are proposed.
| ID | Sev×Lik | Failure trigger | Preemptive control / detection | Contingency / owner |
|---|---|---|---|---|
| R01 | 5×4 | Public unauthenticated old Java backend permits changes | No public runtime until supported auth/tenancy/threat model; deny by default | Disable public ingress; ENG/PO |
| R02 | 5×4 | Tenant IDOR/BOLA leaks resources/members | Per-action org membership authorization and row scoping in queries; cross-tenant tests | Revoke affected sessions, investigate; ENG/OPS |
| R03 | 5×4 | Overlapping approvals overbook stock | Transaction locking and half-open interval count, race test 100 simultaneous approvals | Refuse writes/investigate invariant violation; ENG |
| R04 | 5×3 | Incorrect return double-credits custody | Immutable movement ledger, idempotency keys, partial return invariant | Reconcile and append correction, no history rewrite; ENG/custodian |
| R05 | 5×4 | Free host reclaimed/paused/quota failed | Quota telemetry and secondary encrypted export; weekly checks | Gracefully read-only and restore to alternative host; OPS |
| R06 | 5×4 | Backups only on same VM or never restored | Off-site encrypted versioned restore test; document RPO/RTO target | Pause live writes until recovery tested; OPS |
| R07 | 4×4 | Course Javax components silently removed for supported host | Frozen V1 artifact/matrix, separate supported track | Restore prior academic tag; PO |
| R08 | 4×4 | Wrong org admin claim via campus email | Invite/ownership approval, 2-admin, lost-owner offline verification | Lock disputed org, manual recovery with evidence; PO/OPS |
| R09 | 4×3 | Privacy notice/retention absent for real members | Data-minimal records, retention policy, NPC obligations, logged deletion/export | Disable real enrollment pending review; PO |
| R10 | 4×4 | Shared demo writes persist or disclose other's sandbox | Read-only/demo-expiring tenant + rate limits, no real endpoints | Reset demo and invalidate public session; ENG |
| R11 | 4×3 | False assertion of school approval or validated demand | Prominent independent fictional disclosure, source strength labels | Withdraw claim; PO |
| R12 | 4×3 | Provider free status requires card/costs after limit | Cost ledger, no paid upgrades, quota-fail closed | Stop host and use local demo; OPS |
| R13 | 3×4 | O14/Canvas deadline incorrectly marked complete | Check current Canvas; completion evidence and certificate | Prioritize course before V2; student |
| R14 | 4×3 | Maintenance stops after graduation | Named maintainer, documented runbook, test restores, export portability | Archive safely or migrate, notify users if any; PO |
| R15 | 3×3 | Visual glass background compromises contrast | Token+rendered composite contrast checks, solid fallback, reduced motion | Switch accessibility mode, no aesthetic rollback; QA |
| R16 | 4×3 | Stale provider price/availability claims | Source dates and eligibility test/quarterly recheck | Defer live hosting; research owner |
| R17 | 4×2 | SMS messages sent without consent or billed | Disable live adapter, no phone in public demo, Twilio course simulator only | Revoke credential, disable provider; PO |
| R18 | 4×3 | Source archive entries wrongly marked inspected | Per-file SHA manifest/status and ZIP entry verification | Label gap; no proof assertion; research owner |
