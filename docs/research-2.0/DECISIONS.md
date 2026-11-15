# Dated design/authority decision register
**Status:** USER DECISIONS recorded separately from PROVISIONAL RECOMMENDATIONS. **Date:** 2026-11-15. Links to [SOURCES](SOURCES.md), [RISKS](RISKS.md).
| ID | Classification | Decision, competing route and rationale | User/stakeholder gate / reversal condition |
|---|---|---|---|
| D-001 | USER DECISION | Preserve original course requirements and runnable artifact regardless of hosting platform; cannot replace literal Javax/Jersey/JPA/Retrofit/scheduling with Workers | Frozen source/test/export and course matrix verified before V2 production |
| D-002 | USER DECISION | v5 Academic Glass Commons locked; no unexported pixel-perfect mockup claim | Compare source spec and current real browser at release |
| D-003 | USER DECISION | Fictional public demo first; real pilot optional and **no** current outreach or invented adoption | Actual representative opt-in later |
| D-004 | USER DECISION | Mandatory monetary cost = $0/₱0; no paid domain/SMS/cloud/service, no lifetime guarantee | Paid-required=false cost ledger and account eligibility tests |
| D-005 | PROVISIONAL | Invite-controlled tenancy, two-admin turnover, server scoped roles; **versus** anonymous/shared/demo-header auth | Threat model review; permission matrix and cross-tenant tests |
| D-006 | PROVISIONAL | Modular monolith and one transactional DB; **versus** microservices/brokers/multiple data stores | Only split for proven bounded constraints |
| D-007 | PROVISIONAL | Support overlapping future windows and actual custody; **versus** V1 coarse global holds | Transactional property/race tests and exact interval semantics |
| D-008 | PROVISIONAL | Do not publish old Boot2.1/Java8 V1 unauthenticated; favor separate supported V2 modern Java product track if $0 host permits; **versus** single old public runtime/Worker-only swap | Provider and supported version compatibility, cost, maintenance benchmark |
| D-009 | PROVISIONAL | In-app notification only initially; preserve disabled FakeTwilio in academic demo; **versus** paid live SMS | Course proof acceptance separately; no send without authorization |
| D-010 | PROVISIONAL | Org-owned/managed items only; **versus** competing with OSA/CFMO authoritative requests | Explicit asset ownership/source/access checks |
| D-011 | PROVISIONAL | CSV/JSON export + off-site encrypted backup and tested restore; **versus** same-host-only backup | Restore check, key handling, free storage quota |
| D-012 | OPEN QUESTION | Real image feature needed for CLO9? no waiver assumed | Canvas or instructor authoritative direction C01/C02 |
| D-013 | OPEN QUESTION | Cloud account eligibility, credit-card temporary authorization, 2026 limits/production 24×7 feasibility | Provider sign-up only after user authorization |
| D-014 | PROVISIONAL | First release retains whole-batch semantics until partial return is independently scoped; never silently mark incomplete as returned | Review state/data proof, test eventual later subquantities |
| D-015 | PROVISIONAL | Public demo either read-only snapshot or per-session isolated expiring sandbox; **vs** single mutable shared test account | Isolation/reset abuse tests |
| D-016 | OPEN QUESTION | Present Canvas deadlines, peer reviews, AI policy, O14 completion | Human checks Canvas; no fabricated submission |
| D-017 | PROVISIONAL | Hold course V1 preserved indefinitely in repo tag/source export, V2 separately versioned and mapped; **vs** modifying prior submitted artifact | Reproducible build and SHA documentation |
