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


## 2026-11-15 authoritative user decision update — earlier records retained unchanged
| ID | Authority | New decision and before/after | Gate |
|---|---|---|---|
| D-018 | USER DECISION | Supersedes former post-academic V2 timing in 07/18. **O14 academic baseline → production engineering → P-ACCEPT → A-FINAL → student manual Canvas**, before printed Dec 5 deadline. | [18](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md), [25](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md) |
| D-019 | USER DECISION | Keep original **runnable** Jersey/Javax/JPA/Retrofit/Gson/FakeTwilio/scheduling five-entity academic executable alongside supported V2. Modern Jakarta work is separate, never a substitute. | Academic rerun after P-ACCEPT |
| D-020 | USER DECISION | Real technical production acceptance includes auth/tenancy, transactional/race, security, real DB/API/UI, off-host restore, actual no-mandatory-spend provider/HTTPS feasibility; old CI and local demo do not count. | Real T1–T11 evidence, no NO-GO |
| D-021 | USER DECISION | Academic compliance acceptance is **after** technical production acceptance and **before** only the student's manual Canvas submission. | Current Canvas, CLO9, video, authorship and original course code |
| D-022 | USER DECISION | No silent feasibility, test, deployment or lifetime-free claims; schedule 20 calendar days is very high risk. If production blocked, state NO-GO rather than submit-first or buy. | Explicit new user decision required to change order |
| D-023 | USER DECISION | Real org interview/institutional permission/adoption may be later; **fictional technical acceptance** may precede these stakeholder gates. | No invented campus pilot/endorsement |
| D-024 | USER DECISION | Research docs branch/PR only now; no code changes, merge, deploy, enroll/purchase, Canvas or outreach. | Remote SHA and PR verified |
**D-001–D-017 are left as dated history.** D-018 supersedes *only sequence interpretations*, not ongoing preservation, v5 design, $0 mandatory spending or provisional technical choices.

## 2026-11-15 strict research mandate (original D-001..D-024 preserved)
| ID | Classification | Decision/finding | Condition |
|---|---|---|---|
| D-025 | **USER DECISION** | This chat is only research and independent verification; another chat writes Codex master prompts, Codex builds/tests; research docs branch/PR only. | No production implementation, cloud/service accounts, outgoing contacts. |
| D-026 | **USER DECISION** | Fully preserve and exercise all course stack including actual javax.validation, Jersey, @Autowired, JPA/MySQL, Retrofit/Gson/Twilio, scheduling, 5 entities/26 routes and Postman; old archived code alone does not approve a different public backend. | Explicit active execution evidence and signed architecture boundary. |
| D-027 | **VERIFIED EXTERNAL FACT; MATERIAL CONFLICT** | Spring Boot 4/Jersey4 standard Jakarta runtime does NOT directly execute original javax validation annotations; Hibernate Validator6.2 retains javax limited support on declared Java8/11/17; Spring Boot2/Spring5 OSS unsupported. | **Architecture BLOCKED** until credible fully compliant resolution. |
| D-028 | **PROVISIONAL RECOMMENDATION, UNAPPROVED** | Possible dual executable + supported public Jersey requires genuine 26-route mapping and separate runtime tests, but user may reject javax only being active in course build. | Specific user approval before implementation/stack rewrite. |
| D-029 | **RESEARCH FINDING** | MySQL tenant nullable FK, stale snapshot, late idempotency replay, undefined active name index, unverified owner and session flow are specification blockers. | Revised 08/10/11/12/13 and actual negative/concurrency tests. |
| D-030 | **OPEN QUESTION** | Canvas/CLO9 and Twilio instructor acceptance inaccessible; actual free host/backup not verified. | No academic completion, $0 guarantee or production GO assumed. |
Details and dates: [28](28_STRICT_COURSE_STACK_COMPATIBILITY_AND_CP2_CONFLICTS.md).

## 2026-11-15 explicit research-first acceptance control
| D-031 | **USER PREFERENCE / RESEARCH GOVERNANCE** | Complete and verify a coherent pre-build research packet and ask for user sign-off before Codex produces product features. **Technical end-to-end behavior cannot be claimed proven before any code exists**; only support/contract/design can be checked without actual execution. | [R0–R10 checklist](29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md), then narrow authorized feasibility tests, not fabricated production pass. |
| D-032 | **RESEARCH STATUS / OPEN** | The strengthened literal `javax.validation` and public supported Boot/Jersey namespace constraint is not resolved. Identity/governance, relational exact design, eligible zero-cost infrastructure and original Canvas interpretation remain open. | Architecture **BLOCKED**; no “research done” statement yet. |

## 2026-11-15 exact course-route and modern JSON findings
| D-033 | **VERIFIED COURSE/REPO FACT** | Assignment mandates Jersey REST but does not explicitly pin the `javax.ws.rs` namespace/version; syllabus names **javax.validation** explicitly. Historical Java8/Boot2.1/Jersey2.26 are current O14 source pins, not literal course-prescribed versions. | [30](30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md); course runnable preservation remains mandatory. |
| D-034 | **MATERIAL DESIGN CONFLICT** | V2 omits **Department** and changes global reservations to time-window bookings: current 26-route crosswalk finds 10 no-endpoint, 15 changed-contract and 1 archive-vs-delete counterpart. | Original five entities/global formula/whole-batch routes remain executable, V2 requires user-approved conceptual and API parity; no silent replacement. |
| D-035 | **VERIFIED 2026 VENDOR FACT / CORRECTION** | Jersey4.0.3 (Oct2026) adds Jackson3 and Gson provider modules; Spring Boot4.1.2 manages them. Spring Boot Jackson2 bridge deprecated. Earlier 'Jackson2 required' is no longer a universal Jersey4.0.3 statement. | Test actual HTTP provider before choosing; this does NOT resolve javax.validation. |
| D-036 | **VERIFIED 2026 VENDOR FACT** | Hibernate Validator6.2.6.Final released Oct13, 2026 is newest limited-support javax series; Java8/11/17 declared; older 6.2 page examples are stale. | No fully supported single-process javax+Jakarta provider assumed. |

## 2026-11-15 CP4 runtime and identity evidence
| D-037 | **VERIFIED EXTERNAL FACT; NOT CODE PROOF** | Maintained Open Liberty JavaEE8 features support `javax.validation` Bean Validation2 and JAX-RS2.1 on Java21. But Liberty Spring Boot feature does not auto-integrate EE provider features and Jersey is not automatically its built-in JAX-RS implementation. | [31](31_JAVAX_RUNTIME_AND_EMAIL_FREE_IDENTITY_RESEARCH.md); no direct runtime approval. |
| D-038 | **PROVISIONAL RECOMMENDATION, MATERIAL RISK** | Boot4/Jersey4/Java17 with explicitly invoked `javax.validation.Validator` on real DTOs is a narrower single-public-service research candidate, but javax providers HV6.2.6 limited-support or Apache BVal2.0.6 (2022); neither auto-integrated nor CVE/test proven. | No product build authorized; provider coexistence/security and original 26 routes must be evidenced. |
| D-039 | **PROVISIONAL DESIGN RECOMMENDATION** | For fictional-only multi-tenant technical acceptance, local invite-only accounts + offline one-time owner bootstrap, saved recovery codes, current role checks and no paid email/SMS/real identities. No official Ateneo org claim or verified-email inference. | Owner/provider/recovery and compliance review, real negative tests, no personal-data pilot claim. |
| D-040 | **MODEL CONFLICT / OPEN** | Existing eleven-table V2 EERD lacks password credentials, saved recovery codes, durable ownership-transfer records and final session storage. | Revise schema and auth SRS with fields/state/errors; add only justified tables and separate course unchanged. |

## 2026-11-15 CP5 new source-verified constraints and open choices
| D-041 | **OPEN / DESIGN RECOMMENDATION** | Local invite-only credentials, saved offline recovery codes and operator-only bootstrap for fictional tenant tests; cannot authenticate real org authority. | Require owner-selected identity option, data privacy design and actual runtime proof. [32](32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md) |
| D-042 | **OPEN / MATERIAL USER CHOICE** | Proposed ownership transfer atomically promotes recipient ACTIVE ADMIN to OWNER and demotes initiator OWNER→ADMIN; alternative retains two OWNERS until voluntary stepdown. | Choose policy before final API/DDL; every path maintains >=1 active owner. |
| D-043 | **VERIFIED EXTERNAL FACT / SCHEMA CORRECTION** | MySQL 8.4 TIMESTAMP ends 2038 and is connection-timezone-converted. UTC DATETIME(6) proposed for V2, old course PHT LocalDateTime unchanged. | Date/zone/2040 tests on actual DB. |
| D-044 | **VERIFIED EXTERNAL FACT / SCHEMA CORRECTION** | Spring Session JDBC persists using SPRING_SESSION and SPRING_SESSION_ATTRIBUTES (2 physical tables). MySQL CHECK cannot count active owners across membership rows; transaction root lock required. | Vendor MySQL migration + 2 concurrent last-owner changes + restart/session revocation tests. |
| D-045 | **MODEL ACCOUNTING / OPEN** | 11 prior V2 tables + local credential + recovery + ownership transfer + 2 Spring Session tables = >=16, and public Department parity would add >=1; do not pretend 11 is final or fully course-compatible. | Identity/session provider & Department/API continuity user decision, migration proof. |

## CP5 cross-organization identity change finding
| D-046 | **DESIGN CORRECTION / SECURITY INVARIANT** | Global account disable can orphan multiple organizations despite each membership row remaining ACTIVE; effective owner requires active account AND active OWNER membership. | Lock all affected org roots in ordered transaction, refuse disable unless each still has active owner; test concurrent disable/demotion ID-16. [32](32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md) |

## 2026-11-15 CP6 relational and reservation decisions
| D-047 | **DESIGN RECOMMENDATION, UNAPPROVED** | First production-sized P0 should preserve Department and original global reservation/whole-batch logic per fictional tenant. Draft V2 time-window sum is overconservative because disjoint holds can each overlap requested W without coinciding; if optional booking chosen use event-sweep peak simultaneous sum. | [33](33_TENANT_MYSQL_RESERVATIONS_LEDGER_AND_GLOBAL_COMPATIBILITY.md) DB-06..08 + user approval |
| D-048 | **DATA-INTEGRITY RECOMMENDATION** | Tenant composite FKs alone permit ledger movement to reference same-tenant allocation for *another resource*. Add resource-consistent parent keys; durable client-reused business operation ID distinct from 24h HTTP receipts. | DB-04/05/09, actual MySQL DDL and replay tests |
| D-049 | **CONCURRENCY RECOMMENDATION** | Unify governance/closure and inventory mutation serialization, potentially org-root lock → resource → requirement → allocation; costs one-org throughput and must be tested. Deadlock vs timeout rollback differs. | DB-10/12, real MySQL isolation/deadlock evidence |
| D-050 | **SOURCE/PROPOSAL REVIEW ITEM** | Academic O14 JPQL immediately excludes ended APPROVED holds from headroom before status cleanup; proposal says holds until cancelled/returned/consumed. Preserve code; verify whether reviewed amendment before A-FINAL rather than call PASS or fail automatically. | Exact repository code and assignment/proposal A-FINAL check |

## CP7 research estimate and validation compatibility decisions (2026-11-15)
| D-051 | **VERIFIED EXTERNAL FACT** | Jakarta EE8 already used `javax`; Jakarta EE9 introduced `jakarta`. A modern JDK does not itself ban Javax. | [34](34_JAVAX_JAKARTA_VALIDATION_SEAM_AND_RESEARCH_ESTIMATE.md) |
| D-052 | **VERIFIED EXTERNAL FACT / MATERIAL CONFLICT** | Spring Framework7's native `LocalValidatorFactoryBean` and Jersey4 bean-validation extension use **jakarta.validation**, while actual course O14 `RequestValidation` injects `javax.validation.Validator`. Ordinary injection/annotations are not automatically equivalent. | Need a real executed Javax provider and negative test on supported runtime |
| D-053 | **RESEARCH CANDIDATE, NO APPROVAL** | Java17 Boot4/Jersey4 with explicit manually bootstrapped Javax provider is conceptually narrower than dual-process, but HV6.2.6 limited-support and Apache BVal2.0.6 older; binding/classloader/security unproven. | No product GO until executable compatibility + maintenance evidence |
| D-054 | **SUBJECTIVE WORKLOAD ESTIMATE** | About **10–14 focused passes from CP6**, including independent final audit; CP7 counts as first, ~9–13 left absent new issues. | Not a deadline or completion guarantee |
| D-055 | **EVIDENCE BOUNDARY** | No software/cloud E2E proof is possible without actually running software and authorized provider access. Pre-build completion means no material *paper-resolvable* contradictions and explicit NOT RUN engineering acceptance gates. | Authorize only separately scoped feasibility proof after research review, not full build automatically |
