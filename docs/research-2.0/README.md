# OrgResource 2.0 research documentation
**Status:** USER-APPROVED NEW PRE-CANVAS RELEASE ORDER; V2 architecture and implementation still provisional/unbuilt. **As of:** 2026-11-15 Asia/Manila. [Live draft PR #7](https://github.com/newonathe/OrgResource/pull/7).

This directory is a **research and planning layer**, not a production application, completed O14, official Ateneo product, field study, deployment, live user trial, or verified indefinite-free hosting. Do not use the historical documents as a substitute for the actual 2026 code or the course PDF. No real students/organizations have been onboarded.

## Source-to-research navigation
| File | Status / purpose |
|---|---|
| [00_CONTEXT_TRANSFER_AND_SOURCE_REGISTER.md](00_CONTEXT_TRANSFER_AND_SOURCE_REGISTER.md) | Historical source handoff; existing user-approved transfer preserved unchanged |
| [01_NEW_CHAT_MASTER_RESEARCH_PROMPT.md](01_NEW_CHAT_MASTER_RESEARCH_PROMPT.md) | Full user research mandate; original preserved unchanged |
| [02_AUDIT_AND_SOURCE_COVERAGE.md](02_AUDIT_AND_SOURCE_COVERAGE.md) | Verified repo refs, uninspected sources, conflicts; partial source audit explicitly labeled |
| [03_COURSE_COMPLIANCE_PRESERVATION.md](03_COURSE_COMPLIANCE_PRESERVATION.md) | Binding academic traceability and production compatibility boundary |
| [04_ATENEO_ORGANIZATION_ECOSYSTEM.md](04_ATENEO_ORGANIZATION_ECOSYSTEM.md) | Public facts vs hypotheses and official OSA/CFMO boundaries |
| [05_STAKEHOLDERS_GOVERNANCE_RACI.md](05_STAKEHOLDERS_GOVERNANCE_RACI.md) | Proposed lifecycle authority, no fake approvals |
| [06_PROBLEM_HYPOTHESES_AND_ALTERNATIVES.md](06_PROBLEM_HYPOTHESES_AND_ALTERNATIVES.md) | Falsifiers; spreadsheet/Snipe-IT/build comparison |
| [07_PRODUCT_CHARTER_AND_SCOPE.md](07_PRODUCT_CHARTER_AND_SCOPE.md) | User/product goal and bounded P0 |
| [SOURCES.md](SOURCES.md) | Canonical dated primary evidence ledger; other docs cite IDs |
| [DECISIONS.md](DECISIONS.md) | User decisions vs provisional choices |
| [RISKS.md](RISKS.md) | Prioritized failure triggers and mitigations |
| [08_FUNCTIONAL_SRS.md](08_FUNCTIONAL_SRS.md) | Planned V2 behavior with permissions, validation, errors, acceptance |
| [09_QUALITY_SRS.md](09_QUALITY_SRS.md) | Proposed measurable NFR and free operating envelope |
| [10_EERD_AND_DATA_DICTIONARY.md](10_EERD_AND_DATA_DICTIONARY.md) | Tenant-aware proposed schema, cardinality, constraints and migrations |
| [11_WORKFLOWS_STATES_AND_INVARIANTS.md](11_WORKFLOWS_STATES_AND_INVARIANTS.md) | Reservation/custody state math and concurrency |
| [12_API_AND_AUTHORIZATION_CONTRACT.md](12_API_AND_AUTHORIZATION_CONTRACT.md) | Proposed versioned API, roles, idempotency/errors |
| [13_SECURITY_PRIVACY_AND_THREAT_MODEL.md](13_SECURITY_PRIVACY_AND_THREAT_MODEL.md) | Privacy/legal controls, OWASP/tenant threat model |
| [14_ARCHITECTURE_STACK_AND_ADRS.md](14_ARCHITECTURE_STACK_AND_ADRS.md) | Academic preservation, supported production paths |
| [15_ZERO_COST_HOSTING_AND_SURVIVABILITY.md](15_ZERO_COST_HOSTING_AND_SURVIVABILITY.md) | Dated official quotas, false-guarantee controls and restore |
| [16_SYNTHETIC_DATA_AND_DEMO_POLICY.md](16_SYNTHETIC_DATA_AND_DEMO_POLICY.md) | Deterministic fictional tenants and isolated public demo |
| [17_TEST_VALIDATION_AND_RELEASE_GATES.md](17_TEST_VALIDATION_AND_RELEASE_GATES.md) | Acceptance, property/race, security, browser, backup checks |
| [18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md) | Phased dependencies, acceptance and cuts |
| [19_DEVOPS_OPS_MAINTENANCE_RUNBOOK.md](19_DEVOPS_OPS_MAINTENANCE_RUNBOOK.md) | Reproducible Git/CI/release/incident/backup practice |
| [20_INDEPENDENT_CRITIQUE_AND_REVISED_DECISIONS.md](20_INDEPENDENT_CRITIQUE_AND_REVISED_DECISIONS.md) | Fault finding, recommendation revisions |
| [21_RESEARCH_STATUS_AND_OPEN_QUESTIONS.md](21_RESEARCH_STATUS_AND_OPEN_QUESTIONS.md) | Precise documentation, tested/code/deployed status |
| [22_PRIMARY_ARCHIVE_VERIFICATION.md](22_PRIMARY_ARCHIVE_VERIFICATION.md) | Independent SHA-256/CRC of original 1,227 outer +1,370 nested source entries, exact original Java provenance |
| [23_GITHUB_IMPLEMENTATION_AUDIT.md](23_GITHUB_IMPLEMENTATION_AUDIT.md) | Current Java/React source inspection, CI scope, route status and unverified O14 |
| [24_HISTORICAL_MASTER_COMPLETE_INSPECTION.md](24_HISTORICAL_MASTER_COMPLETE_INSPECTION.md) | All 4,174 lines of 319,799-character historical master read and reconciled against live source |
| [25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md) | Dated decision reversal, current private GitHub audit and production-before-Canvas release criteria |

All documents listed are included in this research branch after the verified third batch. The original transferred 00/01 files remain unchanged; no unapproved code changes or main merges.

## Git/review record
2026-11-15: Rechecked private GitHub repo, PR #1–#7 and newer O11 report SHA `fd34a8a...`; read course primary extracted sources and dated 2026 primary public evidence. The research documentation is versioned on draft PR #7; inspect the latest remote SHA to confirm actual file history. V2 implementation/hosting/testing is explicitly NOT completed. Audit limitations remain in 02. No production source, merges, SMS, emails, financial commitments, institutional contacts or deployment occurred.


## 2026-11-15 user-authoritative sequence update
**O14 academic baseline → pre-Canvas supported V2 engineering → REAL production technical acceptance → independent literal-course academic final acceptance → student's manual Canvas submission (printed Dec 5, check Canvas).** Prior post-academic V2 planning is superseded; earlier decisions preserved in [25](25_ROADMAP_CHANGE_LOG_AND_PRE_SUBMISSION_ACCEPTANCE.md). No production code/test/deploy created by research docs.

## Later live CP0 O14 audit (2026-11-15)
New draft PR #8 now exists at 2dd336ed39cecd417a8bce3388b303e688e95dee and is the actual conditional academic baseline candidate. Earlier no-O14 statements are dated historical snapshots, not current. See [26](26_CP0_O14_BASELINE_AUDIT_AND_CODEX_HANDOFF.md).

## CP1 research (2026-11-15)
[27_CP1_PRODUCTION_STACK_AND_ZERO_COST_FEASIBILITY.md](27_CP1_PRODUCTION_STACK_AND_ZERO_COST_FEASIBILITY.md) documents primary-source verified Boot4.1.2/Jersey4.0.3/JSON and auth caveats; conservative OCI A1 quota conflict, free-IP TLS certificate route, verified storage/host alternatives and unproven account/DR feasibility. This is a **research report, not an implemented production stack or a Codex prompt**.

## Strict course-technology priority and CP2 findings (2026-11-15)
**USER DECISION:** all original technologies fully preserved as runnable and evidenced; research only here, another chat master prompts, Codex tests/implementation. **ARCHITECTURE BLOCKED**, because supported Boot4/Jersey4 uses Jakarta rather than literal javax.validation and no supported one-public-runtime literal replacement is verified. No production rewrite authorized; earlier [27](27_CP1_PRODUCTION_STACK_AND_ZERO_COST_FEASIBILITY.md) is conditional research. Exact conflict/acceptance register: [28_STRICT_COURSE_STACK_COMPATIBILITY_AND_CP2_CONFLICTS.md](28_STRICT_COURSE_STACK_COMPATIBILITY_AND_CP2_CONFLICTS.md).

## Pre-build research gate (2026-11-15)
**NOT RESEARCH-ACCEPTED / architecture BLOCKED.** The user requires thorough pre-build review and proof of course stack compatibility, but actual software E2E results cannot exist before executing an implementation. [29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md](29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md) enumerates R0–R10 source/decision/acceptance checks and separates feasible research proofs from future Codex runtime/host evidence. No code or accounts authorized.

## Course execution and exact 26-route compatibility crosswalk (2026-11-15)
[30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md](30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md) directly traces **all 26** academic O14 routes against provisional V2; identifies ten without endpoint counterparts, fifteen with changed contracts and one archive-vs-delete replacement. Department and original stock reservation semantics are not preserved by the draft V2 data model. Updated vendor facts: Jersey4.0.3 includes Jackson3/Gson modules, so prior Jackson2-only bridge recommendation is outdated; javax-validation conflict remains BLOCKED. **Not an implementation approval or test result.**

## CP4 literal javax runtime and secure email-free identity research (2026-11-15)
[31_JAVAX_RUNTIME_AND_EMAIL_FREE_IDENTITY_RESEARCH.md](31_JAVAX_RUNTIME_AND_EMAIL_FREE_IDENTITY_RESEARCH.md) verifies current Open Liberty JavaEE8 `javax.validation` and JAX-RS support on Java21, but also its Spring Boot integration boundary; evaluates supported Boot4 + actual manual javax provider (unbuilt), dated HV6.2 and old Apache BVal2, and defines secure fictional account ownership/recovery gates without assuming paid mail or Ateneo authority. **Architecture remains BLOCKED, no code or tests performed.**

## CP5 identity + MySQL pre-build evidence (2026-11-15)
[32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md](32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md) defines provisional invite, recovery, owner transfer state machines and MySQL table/transaction/permission requirements. New primary-source corrections: MySQL TIMESTAMP ends 2038 and auto-converts time zones; recommend UTC DATETIME(6) for V2; Spring Session JDBC uses TWO tables, not one; cross-row last ACTIVE OWNER cannot be ordinary CHECK. Existing eleven-table V2 draft becomes **at least sixteen physical tables** for local-password/JDBC choice, ≥17 if public V2 restores omitted Department, and still needs approval. Course five-entity executable unchanged. Architecture BLOCKED.

## CP6 course-preserving MySQL and booking research (2026-11-15)
[33_TENANT_MYSQL_RESERVATIONS_LEDGER_AND_GLOBAL_COMPATIBILITY.md](33_TENANT_MYSQL_RESERVATIONS_LEDGER_AND_GLOBAL_COMPATIBILITY.md) finds a mathematical **overconservative** V2 overlap-sum bug (must use peak concurrent holds if future-window booking approved), source nuance about ended O14 holds, missing same-resource ledger/allocation FK, expiring idempotency receipt vs durable operator-supplied operation ID, and org-root lock sequencing. Proposes **global reservations, Department and whole-batch P0** pending user approval. DB-01..14 acceptance specified, none executed. Original course stack/DB untouched, architecture BLOCKED.

## CP7 remaining work estimate and literal Javax integration gate
New [34](34_JAVAX_JAKARTA_VALIDATION_SEAM_AND_RESEARCH_ESTIMATE.md): Jakarta EE8 still used `javax`; Boot4 Spring7/Jersey4 native validation uses `jakarta`, original O14 explicitly executes `javax.validation.Validator`. Java17 + explicit true Javax provider remains a high-risk unbuilt candidate, not automatically compliant. **Estimated 10–14 remaining research passes from CP6** (CP7 counted first; ~9–13 from now), including final independent critique. No proof of live E2E before executable checks; architecture BLOCKED.

## CP8 true Javax provider security and version evidence (2026-11-15)
New [35](35_VALIDATOR_PROVIDER_SECURITY_AND_SUPPORTED_RUNTIME_GATES.md) establishes Boot2.1.0-managed **Hibernate Validator6.0.13**, O14 no override, IBM Oct21 2026 CVE-2026-93879 affected range through **6.2.5**, fixed **6.2.6**. O14's actual effective graph/exploitability NOT RUN. HV6.2.6 remains limited-support with Java17/Boot2.4–2.7 vendor table; BVal2.0.6 Javax last published 2022. A Jakarta Bean Validation2 API artifact may have `jakarta.validation` coordinates but **javax.validation packages**. 12 future compatibility/security acceptance tests proposed, none run. Estimated **8–12 remaining passes** after CP8. **Architecture BLOCKED** and original course code untouched.
