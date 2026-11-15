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
| R13 | 5×5 | The new production-before-Canvas critical path overruns Dec 5 or Canvas changes | Student verifies Canvas/CLO9 early, Nov18 host kill switch, daily evidence gates | Report NO-GO and genuine choices; student; no silent submit-first |
| R14 | 4×3 | Maintenance stops after graduation | Named maintainer, documented runbook, test restores, export portability | Archive safely or migrate, notify users if any; PO |
| R15 | 3×3 | Visual glass background compromises contrast | Token+rendered composite contrast checks, solid fallback, reduced motion | Switch accessibility mode, no aesthetic rollback; QA |
| R16 | 4×3 | Stale provider price/availability claims | Source dates and eligibility test/quarterly recheck | Defer live hosting; research owner |
| R17 | 4×2 | SMS messages sent without consent or billed | Disable live adapter, no phone in public demo, Twilio course simulator only | Revoke credential, disable provider; PO |
| R18 | 4×3 | Source archive entries wrongly marked inspected | Per-file SHA manifest/status and ZIP entry verification | Label gap; no proof assertion; research owner |

## 2026-11-15 added deadline and production-acceptance risks (before/after R13 in [25](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md))
| ID | Sev×Lik | Failure trigger | Early detection and control | Honest contingency |
|---|---|---|---|---|
| R19 | 5×5 | 37–75 expert person-days V2 prior bands with 20 calendar days left and unknown team velocity | Tight optional-scope freeze, daily CP receipt, early O14 baseline/Canvas check | No schedule guarantee; disclose deadline conflict |
| R20 | 5×4 | Free Java host/DNS/TLS/card/region/backup cannot be provisioned | Actual account eligibility + supported JVM/DB + off-host DR feasibility before Nov18 target | P-ACCEPT BLOCKED, no paid upgrade/local false pass |
| R21 | 5×4 | Jakarta-supported V2 replaces/breaks Javax/Jersey/Retrofit course executable | Independent snapshot+SHA+two builds+post-V2 course rerun | A-FINAL FAIL until original requirements proven |
| R22 | 5×4 | Successful CI or local demo is misrepresented as real security/restore/live acceptance | Per-gate T1–T11 evidence on target DB, HTTPS, full clean restore, threat checks | P-ACCEPT NO-GO, source evidence retained |
| R23 | 4×4 | New version/host causes DB locking/scheduler/state inconsistency | Early supported BOM, 100-way real DB race, bounded scheduler and reboot test | Reject architecture or remediate before release |
| R24 | 4×4 | CLO9, video, authorship or Canvas instructions left until after rushed production | CP0 asks student to inspect current Canvas and notes exceptions; Dec4 academic acceptance reserved | A-FINAL FAIL; no invented waiver/submission |
| R25 | 4×3 | Synthetic technical deployment falsely portrayed as approved Ateneo pilot | Independent demo/non-affiliation label, no real contacts; opt-in gate | Withdraw claim; real adoption deferred |
These severities/likelihoods are qualitative expert estimates only, not incident rates. R13 changed specifically because **final** academic acceptance comes **after** P-ACCEPT; early course preservation still happens first.

## 2026-11-15 research gate and feasibility risks
| R26 | 5×5 | Unsupported Jakarta modernization is misrepresented as fully preserving literal javax Validation | Architecture BLOCKED, active course path test and user-reviewed namespace execution boundary [28] | No Codex product rewrite without approved compliance resolution |
| R27 | 5×4 | Research design is called “E2E proven” even though code/cloud never run | Separate [R0–R10 research gate](29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md) and future runtime test receipts | Report NOT RUN; never infer PASS |
| R28 | 5×5 | Exhaustive research consumes available Dec 5 time but does not establish host/free account | Dated critical path and no-spend provider conditional evidence; student checks Canvas and support policies | Disclose likely deadline conflict, do not invent waiver/buy/swap order |

## CP4 added material risks — 2026-11-15
| R29 | 5×4 | Open Liberty JavaEE8 support misread as supported Jersey/SpringBoot4 javax integration | [31](31_JAVAX_RUNTIME_AND_EMAIL_FREE_IDENTITY_RESEARCH.md) distinguishes provider availability from Boot/Jersey integration and JDK; block deployment without actual executed proof | Avoid unsupported Frankenstack |
| R30 | 5×5 | Legacy Apache BVal2/HV6.2 manually loaded into public Boot4 without maintenance or provider-conflict review | Effective BOM, actual injection, XXE/expression/CVE/EL behavior, secure negative tests; independent review | Architecture BLOCKED until risk acceptable |
| R31 | 5×4 | No free mail/real identity provider leaves owner unprovable or unrecoverable | Operator-controlled synthetic bootstrap, high entropy saved codes, two-party transfer, deny official affiliation; real accounts later gate | No real-org claim or first-visitor ownership |
| R32 | 4×4 | Missing credential/recovery/ownership-transfer schema causes unrecoverable or privilege-escalated accounts | New justified identity tables/session decision and transaction acceptance | Do not build against provisional 11 tables |
