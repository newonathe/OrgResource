# Lifecycle stakeholder and decision-authority map
**Status:** PROVISIONAL RECOMMENDATION; no external stakeholder appointments or approvals. **Date:** 2026-11-15. Sources A01–A05, C01–C02, user mandate.

Roles in table are **proposed**: PO = student product owner/researcher; ENG = future implementation maintainer; QA = independent tester/test automation; OPS = future service operator; ORG = prospective authorized organization representative; CAMPUS = OSA/CFMO/IT/privacy as applicable; PUBLIC = anonymous demo visitor. R = does work, A = accountable decision, C = consulted later, I = informed. This is an internal governance plan, **not** university authorization.

| Phase | PO | ENG | QA | OPS | ORG | CAMPUS | PUBLIC | Gate/evidence |
|---|---|---|---|---|---|---|---|---|
| Public desk discovery now | A/R | C | C | — | unavailable | public sources only | — | Source ledger + unvalidated hypotheses |
| Problem/scope definition | A/R | C | C | C | later C | later C where official flows | — | Problem charter and explicit falsifiers; no fake approval |
| UX/accessibility design | A | R | C | C | later C | later only if branded/official | — | Locked v5 review; keyboard/contrast/motion tests |
| Architecture and tenant security | A | R | C | C | later C | later C for real institutional data | — | ADR, threat model, no identity bypass |
| Synthetic development | A | R | C | C | — | — | — | Tests with seeded fictional actors; feature PRs |
| Security/functional validation | A | R | R | C | later optional UAT | no assumed review | — | Cross-tenant, concurrency, recovery, Postman gates |
| Public synthetic demonstration | A | R | R | R | — | no implied endorsement | I | Isolation, reset, disclosure, abuse limit |
| Optional real pilot go/no-go | A | R | R | C | **A for their own data/participants** | as actually required | — | Written scoped authorization, notice, governance, exit |
| Continued real operation | A for service | R for fixes | C | R for incidents/backup | A for their own records/roles | C when actually applicable | I per notice | Named responsible operator + privacy/incident workflow |
| End-of-service/officer turnover | A | R | C | R | A for their org exports | where legally required | I | Export/delete controls, two-admin transfer, restore proof |

**Authority separation:** Org admin can manage *their own workspace* only; product operator may repair infrastructure, not inspect users' resources without justified logged support access. Campus organizations are not product customers/approvers by default. The course instructor controls academic assessment, not hypothetical future product deployment. A demo user controls no real tenant. Future real pilot requires ORG participation; this is **not required** to research, implement or publish isolated fictional demo.

**Decision log rule:** Every ADR records proposer, approver (today PO for draft only), competing options, source evidence, security/cost impact, rollback conditions. No unilateral provider signup, outreach, paid service, SMS/email send, production merge, real data ingestion or campus submission.
