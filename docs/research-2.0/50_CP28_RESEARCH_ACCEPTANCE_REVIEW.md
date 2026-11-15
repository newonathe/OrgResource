# CP28 — Independent research acceptance review after CP27 upload

**Date: November 16, 2026 (Asia/Manila). Status: REVIEW COMPLETED AT PAPER LEVEL; RESEARCH OWNER ACCEPTANCE OPEN; COMPAT-01 NOT AUTHORIZED; FULL PUBLIC BUILD NO-GO.** This pass did not implement/execute tests or use any accounts.

## 1. Repo and evidence integrity
- Exact research upload starting from `856a2fceabcbbf95b569d8965bd6b4a23d418c54` created commit `5a2c7d7048ec273a3b27dfe110a72d87325334fa` adding exactly eight research Markdown files (one read-first index, numbered 43–49). GitHub comparison verified all eight additions and no non-research changes. All 23 relative Markdown links in these eight documents point to existing tracked paths.
- At verification: private `newonathe/OrgResource`; `main` `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`, academic draft PR#8 `2dd336ed39cecd417a8bce3388b303e688e95dee` unchanged; research PR#7 contains the new commit and remains draft/unmerged.
- The *uploaded documents* are an independently verifiable Git fact; their proposed architectural/runtime assertions remain unexecuted. No original source archive, raw video, credentials, code, provider account or deployment was uploaded/modified by this research publication.

## 2. Research acceptance decision — what is and isn't complete
| R gate | Evidence now available | Still necessary | Verdict |
|---|---|---|---|
| R0 primary sources | Reopened assignment, syllabus, proposal, slides, code, prior manifest; CP22/24 audits | Current Canvas and original ZIP extra-path reconciliation | PARTIAL |
| R1 academic course technology | Pinned O14 source, 26 handlers, actual 55 Java/15 UI CI and JAR artifact | Separate direct javax entity save test, Twilio/CLO9, full export, final student evidence, intermittent test explanation | STRONG SOURCE/CI, A-FINAL OPEN |
| R2 supported public architecture | Versioned Jersey4/Boot4/Jakarta/MySQL candidate and 8-case COMPAT-01 | Owner boundary signoff, actual effective dependency/HTTP/MySQL proof | CONDITIONAL CANDIDATE; NOT PROVEN |
| R3 fictional users/governance | Role/invite/recovery/owner proposal | Accept exact ownership transfer, emergency compromise, credential/session policy | PAPER DECISION BLOCKER |
| R4 public SRS/APIs | Global/whole P0 candidate and 26-route family map | Sign public intentional R04/A09 differences and exact DTO/error/role contract | PAPER DECISION BLOCKER |
| R5 physical SQL/data integrity | Composite FK, snapshot/ledger, idempotency and lock design | UUID physical choice, schema/migrations/indexes and actual MySQL tests | FULL BUILD BLOCKER |
| R6 security/privacy | Threat-to-control-to-test plan, synthetic scope and per-role concepts | Signed response, session and access policy; enforce and test in runtime | PARTIAL; NO PUBLIC RELEASE |
| R7 host/ops | Official zero-mandatory-spend candidate analysis | Authorized actual account/VM/billing, TLS renew/reload, off-account encrypted clean restore, maintainer | RUNTIME/ACCOUNT DEPENDENCY |
| R8 acceptance | Specific O14/COMP/P cases, evidence tiers, release order | Freeze signed per-route/source test matrix, execute later | PLAN ADVANCED |
| R9 independent critique | CP13, CP20, CP26, CP27 and this CP28 | Integrate points below into signed canonical revision | REVIEW PERFORMED, SIGNOFF OPEN |
| R10 owner decision | Explicit request for research update and GitHub upload; no permission to code | Separate approval for COMPAT-01 and eventual full build | NOT APPROVED |

**Conclusion:** Detailed research is adequate for an isolated feasibility **authorization decision**, NOT general research acceptance, NO-GO full P0, P-ACCEPT NOT RUN, A-FINAL NOT RUN.

## 3. New precision blockers found by adversarial read
### CP28-MC71 — The public route map is not yet an executable per-route contract (HIGH)
CP27 gives 26 business capability families and example public suffixes; it does **not** fully freeze each public route's complete JSON input/output, nullable fields, role, status codes, version header, deletion behavior and Postman fixture. Do not let Codex fill those from 2025 temporal/partial-return draft. This is a full-build blocker, not a COMPAT-01 blocker. Required: one row per original academic op with approved public equivalent/difference, DTO schema and positive/negative tests.

### CP28-MC72 — Public deletion/archive versus history references needs final invariant (HIGH)
Original academic A08 can delete never-approved PENDING/CANCELLED, guarded by `approvedAt == null`. New public ledger and append-only audit may reference an allocation even if current state is CANCELLED. The candidate public API lists DELETE eligible but has not specified which immutable references prevent hard deletion, who can archive, whether returns have issue movements, or how a soft-deleted row remains queryable in an export/restore. **Recommended:** reject hard deletion after approval, physical movement or required audit reference; never cascade history. Treat the precise target state/API as OPEN, not implemented.

### CP28-MC73 — Effective holds, coverage, and persisted status require one shared time rule (HIGH)
Academic JPQL excludes APPROVED if project ended before cleanup. Public P0 draft adopts `endAt <= now` as effective end but leaves approved record temporarily in database. Availability and Requirement `coveredQuantity` MUST use the same effective predicate; project end must also block later issue/consume atomically. A scheduler must not trigger double hold release or misreport shortage. APPROVED and expired-in-effect may be presented as "awaiting expiry reconciliation", not as physically issued or still reserved. Owner/instructor review remains required. Need controlled clock, stale job, and simultaneous end/issue tests.

### CP28-MC74 — Recovery/restore has a security choice about sessions and keys (HIGH)
An encrypted clean restore must preserve authoritative identities, roles, ledger and durable operation uniqueness, but **must decide** whether active authentication session material is restored, invalidated or rotated on replacement host and how the encryption key remains separately available. Restoring sessions blindly to a differently controlled or public clone can expose accounts; omitting session data without testing logout behavior can create false recovery assumptions. Recommend invalidate/rotate active sessions after disaster recovery unless owner explicitly selects and proves safe continuity. This is a public full-build/DR contract item.

### CP28-MC75 — Identity lock order must include credential reset and disable (MEDIUM/HIGH)
Research proposes org-root → memberships → account for global owner-impacting changes, while credential password/recovery operations naturally take account lock first. A multi-org disable/transfer concurrent with reset may deadlock or cause security-state visibility inconsistencies. Freeze global lock ordering and fail-safe rollback/no privilege access on timeout; execute concurrency tests. This is not proven by paper or library capability.

### CP28-MC76 — Public DOM/Roles scope should not overclaim "course-compatible public" (HIGH)
The two-executable plan is USER DECISION, but public Jakarta application is not a literal javax validation executable and its draft A09 absent/R04 archive changed. Keep academic **full actual course execution** separately and explicitly approve public business-capability differences. Do not call public 26-route contract equivalent or invoke D-019 as blanket approval of any different public stack.

### CP28-MC77 — Release and time gating are distinct (HIGH)
The user-required order remains O14 baseline→public P-ACCEPT→academic A-FINAL→student Canvas (printed December 5). A local compatibility test may pass without any account/host; it does not establish free 24×7 operation, production adoption or academic submission. O14 passing CI does not guarantee separate release ZIP/Eclipse/Newman/Canvas. No automatic 7-day extension is established by syllabus; only actual Canvas/instructor can resolve. Preserve early NO-GO if no cost-controlled host/DR/security evidence.

## 4. Which questions are paper-resolvable now?
**Before full build:** OD02 global holds; OD03 whole returns; OD04 end-of-project effective holds; OD05 operator-only fictional bootstrap; OD06 old-owner role on transfer; OD07 emergency freeze; OD08 credential/session choice; OD09 deletion/archive/A09 differences; OD10 UUID type; and full per-endpoint DTO/auth/error matrix. The choice is the user's, not an implicit research acceptance.

**Before isolated COMPAT-01:** Explicit authorization, restricted local fixtures/code scope, selected candidate versions and an exact eight-case evidence/STOP contract. No cloud, paid provider, ownership-policy or Canvas decision is intrinsically necessary for that local proof.

**After code/provider:** Real boot/Jersey, security, MySQL transaction/races, full UI, HTTPS renew/reload, account fee/quota, independent backup restore, maintenance and release evidence. Documentation cannot pre-certify these.

**Before A-FINAL:** Current Canvas, CLO9 image, Twilio provider acceptance, JPA literal javax save+flush test, repeatable academic stock boundary, 26 Postman real runtime, actual student video/authorship and student-only receipt. Raw video frames of unattached recordings remain unobserved.

## 5. Proposed owner decision sheet — no approval implied
1. Do you approve **fictional-only P0** with Organization+Department and global holds/whole-batch returns, deferring temporal/partial/damage?
2. Do you approve **operator-only first owner**, local login/saved-code/JDBC sessions and recipient-accepted transfer old OWNER→ADMIN, with sole-owner compromise freeze?
3. Do you approve ended approved holds ceasing at `endAt` with scheduler `CANCELLED` reason EXPIRED, consistently applied to availability+coverage?
4. Do you approve public differences from the 26 original course operations: resource archive instead of hard delete, no public SMS consent A09 while original academic endpoint remains fully active, and audited restriction on public history deletion?
5. Do you approve the **candidate** supported Java17+/Boot4/Jersey4/Jakarta/MySQL8.4 architecture for isolated proof only, while independently keeping Java8/Jersey2/javax O14 runnable?
6. Do you authorize **COMPAT-01 only** as a local no-account/no-public-Internet experiment with eight acceptance cases and strict stop-on-fail, without authorizing the full build?
7. Independently, confirm Canvas image/Twilio/current deadlines and whether an eligible zero-mandatory-cost provider/account is acceptable; do not treat these as inferred signoffs.

Do not infer an answer from the user's instruction to upload documentation or "go next."

## 6. CP28 recommended next action
**CP29 should be a focused owner-decision and final-contract revision pass, not another broad research survey.** Prepare a short adjudication matrix and exact public route/role/DTO/error contract from accepted choices; only then formulate and separately request permission for the COMPAT-01 limited engineering handoff. Any authorized engineering is undertaken later in the designated Codex orchestration workflow.

**Final CP28 result: PUBLISHED RESEARCH REVIEW; FULL BUILD NO-GO; COMPAT-01 CONDITIONAL FOR OWNER REVIEW, NOT AUTHORIZED.**