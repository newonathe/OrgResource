# Authoritative roadmap change log — 2026-11-15
**Status:** USER DECISION for order; VERIFIED COURSE SOURCE for Dec 5 deadline; RECOMMENDATION for dates; UNVERIFIED for technical readiness. Dated before/after rather than silent revision.

## Current private GitHub source audit
- `newonathe/OrgResource` `main=16e781c837dd7e1f0481fb7a01e49c27d80a91e8`; PRs #2–#7 open, unmerged. PR#6 O11/O13 `fd34a8a66242f795dda758d45f220f2c331472db`, CI Verify 43683241632 success. Author local report says 55 Java / 15 UI / 5 Playwright and 73 Newman requests/105 assertions; not independently rerun here.
- Research PR#7 DRAFT, starting head `fb29c38ceded8ffd69d389b9bb18bd648f8e86c1`, base O8–O10 PR#5 (therefore does not automatically include #6 source). No O14 branch observed; no verified complete course rubric, V2 source, provider account, deployment or production tests.
- CSCI assignment Part III: printed final Canvas **Dec 5, 2026**. Syllabus CLO9 image receive/serve and Certificate of Authorship/outside help. Student-only Canvas could supersede. Nov15→Dec5 = 20 calendar days.

## Dated history preserved
| Original 2026-11-15 draft statement | New 2026-11-15 authoritative direction | Document and effect |
|---|---|---|
| 07: “P0 academic ... P1 V2 local security/model ... P2 hosted fictional demo” | **O14 early academic baseline → V2 engineering and production acceptance → final academic acceptance → student's Canvas** | 07 replacement release boundaries |
| 18: “Post academic completion: Start V2 source changes” | V2 implementation must be completed **before** Canvas; academic final gate after production | 18 replaced sequence/backlog/critical path |
| 17: “T0 academic preservation”, “G0 Academic” first | Early baseline T0 is not A-FINAL; **T1–T11 real P-ACCEPT first** | 17 production/academic separate gate addendum |
| 14 ADR-01: preserve old source/tag after O14 | Keep a verified O14 **baseline** artifact, test it again at A-FINAL; separate supported Jakarta product | 14 ADR-08 |
| 19 release/deploy only post academic | Current research does not deploy; **future** operations trial and acceptance pre-Canvas | 19 release-runbook addendum |
| 20 critique and 15 contingency: cut hosted demo → local-only acceptable | Local-only remains safe fallback/evidence but is **NOT production PASS** if external production claim required | 15/20 NO-GO addenda |
| 21 referred to separate Codex planning chat | Continue in this project conversation, exact same source trace and gates | 21 amendment |
| DECISIONS D-001–D-017 | **Unchanged historically**, D-018–D-024 new user decisions with explicit precedence | DECISIONS new section |
| RISKS R13 said “Prioritize course before V2” | Early course preservation yes; final academic acceptance only **after** prod PASS | R13 updated, R19–R25 added |

## Non-negotiable acceptance
**P-ACCEPT** requires actual supported secure V2 build, real tenant auth, database constraints, concurrent reservation and idempotent custody, real API/UI, security and accessibility evidence, qualified free hosted HTTPS if external production claimed, provider bill/quota checks, off-host encrypted **clean restore**, rollback/operator evidence. Local-only/CI-only cannot pass. **A-FINAL (after P-ACCEPT)** requires new execution of preserved Jersey2/Javax Validation/JPA/Retrofit/Gson/FakeTwilio/scheduling and 5-entity CRUD/Postman, current Canvas/CLO9 clarification, video/export/authorship/assistance. **Student manual Canvas** last. No approval or test claimed.

## Critical path feasibility
Old engineering bands B02–B09: **37–75 expert person-days** (not observed productivity), plus O14, A-FINAL and unknown provider/repair. Only 20 calendar days remain; **RED HIGH RISK**, no guarantee. Aggressive target CP0 Nov16, host/BOM CP1 Nov18, tenant CP2 Nov22, engine CP3 Nov26, UI CP4 Nov29, live/DR CP5 Dec2, P-ACCEPT Dec3, A-FINAL Dec4, manual Canvas Dec5. Detail/stop-lines in [18](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md).

**Honest contingency:** cut feature extras, not auth/DB invariants/security/DR/course; if no $0 eligible host, declare production BLOCKED and deadline conflict—do not automatically pay, pretend local equals prod, contact university, or silently submit before P-ACCEPT. Real org interviews/adoption remain future optional, not production-engineering blockers.

## This documentation-only change
Update existing `docs/orgresource2-research-continuity-2026-11-15` research branch and draft PR#7; no source code, releases, host, test execution, merges, charges, student/Canvas activity or real user contact.
