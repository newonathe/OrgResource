# CP25–CP26 — Final pre-build decision gate and independent adversarial review

**Dated 2026-11-16. Research-only. Public P0 unbuilt. No engineering/spike/cloud/submission authorization inferred.**

## CP25 — reality and calendar
**USER DECISIONS:** keep academic O14 actively executable, develop supported separate public system, no mandatory spend, no main merge without approval, actual P-ACCEPT followed by independent A-FINAL followed by student-only Canvas; use separate Codex orchestration chat only after research acceptance.

O14 draft PR #8 SHA `2dd336ed39cecd417a8bce3388b303e688e95dee` had actual passing Java8/MySQL 55-test and frontend 15-test CI, five families of 26 Jersey routes, runtime JAR artifact. It has unresolved complete-package custody, JPA Javax direct validation, local extended tests and Canvas/Twilio/CLO9; A-FINAL not run. Public P0 code has not been built; no P-ACCEPT.

**Printed deadline** December 5, 2026; November 16→December 5 = 19 calendar days. Historic scope effort **37–75 expert person-days** is a subjective prior planning band, not measured throughput. Earlier optimistic checkpoints Nov18 stack/host, Nov22 tenant, Nov26 inventory, Nov29 UI, Dec2 hosting/DR, Dec3 P-ACCEPT, Dec4 A-FINAL, Dec5 student Canvas are **targets without demonstrated feasibility**.

## CP25 proposed architecture choices — all owner approval OPEN
| Proposed ID | Candidate choice | Alternative/constraint |
|---|---|---|
| A1 | Original runnable academic Java8/Boot2/Jersey2/Javax executable **plus** separately versioned supported Java/Jersey/Jakarta public executable | Never silently replace original mandatory Javax with Jakarta-only or publicly expose old unauthenticated API |
| A2 | Public Department + Project + Resource + Requirement + Allocation within fictional Organization; global stock holds; whole-batch usable return | Historic V2 temporal and partial return deferred |
| A3 | Operator-provisioned fictional organizations; local invite-only credentials, saved codes, secure server session, current permissions | No public self-OWNER, campus email/organization authority inference or anonymous mutable shared demo |
| A4 | Conditional OCI A1 ARM + controlled MySQL8.4 with independent DR and $0 mandatory charges | Host eligibility/version/account/bill/TLS not proven; no paid autopurchase |

## COMPAT-01 proposed, NOT authorized or run
- COMP01 Java17+/supported Spring Boot4/Jersey4 starts as real Jersey REST.
- COMP02 actual Jersey JSON GET delegates to injected Spring component.
- COMP03 actual invalid Jakarta DTO response 400 and zero DB write.
- COMP04 JPA invalid entity/foreign key, rollback in real MySQL.
- COMP05 actual Jersey HTTP 401/403 and cookie CSRF 403 for mutation.
- COMP06 two conflicting resource writes cannot overcommit.
- COMP07 record exact effective dependency graph, Java/MySQL versions, redacted evidence, security/dependency scan.
- COMP08 original literal Javax academic executable unchanged and independently runnable.
- Entry G1 exact O14 SHA and branch; G2 isolated disposable app+fictional isolated DB, no host/provision; G3 HTTP security boundary, not only unit tests; G4 per-case expected/actual, evidence, PASS/FAIL.
- A failed experiment yields incompatibility report, **not** silent switch to MVC, SQLite/PostgreSQL, unsupported Javax, provider billing or entire product implementation. Separate full-build approval required after experiment.

## CP26 independent adversarial findings
| ID | Finding | Required corrective action |
|---|---|---|
| C01 CRITICAL | Old `08_FUNCTIONAL_SRS.md` implements self-created org, verified email, temporal booking, partial return and in-app-only service despite supersession notes | Canonical P0 active requirement table; forbid old tables as build instructions |
| C02 CRITICAL | Course Javax/Jersey and public Jakarta semantics conflated | Academic executable full mandatory technology tests; public capability mapping and approved differences separately |
| C03 CRITICAL | Sole-owner compromise and transfer ambiguity; account global disable can orphan several orgs | Choose old-owner role, effective last-owner and frozen emergency recovery |
| C04 HIGH | Original proposal global APPROVED hold wording vs O14 `project.endAt > now` JPQL | Explicit effective-hold end-time and scheduler-state rule |
| C05 CRITICAL | Stock locks and composite FKs unexecuted; 24h idempotency insufficient | Real InnoDB race, same-resource FK, durable caller-reused operationId tests |
| C06 CRITICAL | Actual no-charge host, HTTPS renewal, account capacity, independent encrypted restore unproven | Stop public acceptance pending actual authorized evidence |
| C07 HIGH | Old Postman time-window/partial returns test plan contradicts global whole-batch P0 | Replace with J-A (course), J-P0 (global), J-OPTION (future) tests |
| C08 HIGH | CI, reported local tests and ignored release files conflated | Exact SHA/evidence grades, retrievable artifacts, independent extraction |
| C09 CRITICAL | 19 days to Dec5, zero buffer for code/provider/security failures | Early kill switches; no fictional PASS or silent relaxation |

## CP26 proposed resolutions — not approved
- Public capability continuity is required **as a separately mapped/approved business contract**, not falsely claiming every public URL must equal original course path. Original O14 26 routes remain executable.
- Proposed effective holds end at exclusive `project.endAt`; persisted APPROVED can briefly await scheduled `CANCELLED` with reason EXPIRED, but availability/issue refuse expiry immediately. No modification to academic source without review.
- For fictional workspace ownership transfer: target ACTIVE ADMIN accepts, target→OWNER and old OWNER→ADMIN atomically. Compromised sole owner without verification/recovery → freeze tenant; no unverified operator takeover.
- For stock writes: proposed organization root lock first, then ordered resource/requirement/allocation/ledger and durable receipt within one transaction. This may serialize unrelated resources; benchmark and choose isolation after real MySQL execution.
- No new implementation blockers are inferred from human Canvas/cloud enrollment unknowns **for an isolated local COMPAT-01**; they remain blockers to actual public or academic final acceptance.

## Stage-specific no-go matrix
| Issue | Blocks COMPAT-01? | Blocks full P0? | Blocks P-ACCEPT/A-FINAL? |
|---|---|---|---|
| Owner explicit isolated authorization and exact experiment contract | YES | YES | N/A |
| Final owner recovery, scope, API, physical DB schema | NO | YES | YES |
| Modern integrated runtime proof | Goal of COMPAT-01 | YES until proven | YES |
| Current Canvas/Twilio/CLO9 | NO | No for independent product engineering | YES A-FINAL |
| Real free provider/HTTPS/restore | NO | Plan/design must exist; execution later | YES P-ACCEPT |
| Raw non-video ZIP one-path discrepancy | NO | NO | Blocks *complete source-audit claim*, not an assumed instructor deliverable |
| Actual institution/user interviews | NO | NO for fictional test app | Required only for real adoption claims |

**CP26 verdict:** Enough paper evidence for a bounded owner review of isolated COMPAT-01; no demonstrated fundamental paper impossibility; unrestricted public implementation still NO-GO. Codex/testing NOT started.