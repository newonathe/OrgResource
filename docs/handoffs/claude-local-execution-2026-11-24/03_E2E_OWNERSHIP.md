# 03 — Current end-to-end ownership and dependencies

**Current authority (Report49, 2026-11-25 PHT):** R09 technical gate VERIFIED_BOUNDED; exact reviewed source `2c626cb251fc3e0a5e3304f9ea5b03f89f55e024`, draft PR56, full CI45494597345 and dedicated CI45494597313. Owner explicitly initiates local execution. Private/independent/host/student gates remain open. Historical text is preserved in the previous handoff commit `cef3b1d566d5d480156e114a1947089518022f0f`; current 09/11/12/45 supersede it. No native execution claimed.

Graph 12 is machine authority. The following table is generated from current task nodes; historical detailed ownership/oracles preserved at prior handoff commit and report48. Use report27/11 for L06 and 02/07/08 for native task acceptance. No formal waiver.

| ID | Executor | State | Dependencies |
|---|---|---|---|
| R00 | CHAT | VERIFIED_BOUNDED | None |
| R01 | CHAT | VERIFIED_BOUNDED | R00 |
| R02 | CHAT | SOURCE_RECONCILED_REVIEW_OPEN | R00 |
| R03 | CHAT | VERIFIED_BOUNDED | R02 |
| R04 | CHAT | VERIFIED_BOUNDED | R00 |
| R05 | CHAT | VERIFIED_BOUNDED | None |
| R06A | CHAT | VERIFIED_BOUNDED | R05, R01 |
| R06B | CHAT | VERIFIED_BOUNDED | R06B1, R06B2, R06B3 |
| R06B1 | CHAT | VERIFIED_BOUNDED | R06A |
| R06B2 | CHAT | VERIFIED_BOUNDED | R06B1 |
| R06B3 | CHAT | VERIFIED_BOUNDED | R06B3a, R06B3b |
| R06B3a | CHAT | VERIFIED_BOUNDED | R06B2 |
| R06B3b | CHAT | VERIFIED_BOUNDED | R06B3a |
| R07 | CHAT | VERIFIED_BOUNDED | R01, R03 |
| R08 | CHAT | VERIFIED_BOUNDED | R07 |
| R09 | CHAT | VERIFIED_BOUNDED | R01, R03, R04, R06B, R07, R08 |
| L00 | CLAUDE_LOCAL | NOT_RUN | R09 |
| L00B | CLAUDE_LOCAL | PRIVATE_INPUT_REQUIRED | L00 |
| L03 | CLAUDE_LOCAL | NOT_RUN | L00 |
| L01 | CLAUDE_LOCAL | NESTED_IN_L03 | L03 |
| L02 | CLAUDE_LOCAL | NESTED_IN_L03 | L03 |
| L04 | CLAUDE_LOCAL | WAITING_FOR_PRIVATE_INPUT | L03, L00B |
| L05 | CLAUDE_LOCAL | PRIVATE_INPUT_REQUIRED | L00 |
| L06 | CLAUDE_LOCAL | BLOCKED | L00B, L03, R06B |
| L07 | OWNER_OPERATOR | NOT_RUN | L06 |
| L08 | CLAUDE_LOCAL | WAITING_FOR_LOCAL_QUEUE | L00 |
| G01 | INDEPENDENT_REVIEWER_OWNER | BLOCKED | R03, L05 |
| G02 | OWNER_OPERATOR | LIVE_IMPORT_NOT_RUN | L06, L07 |
| G03 | OWNER_REVIEWER | BLOCKED | G01, R07, R08, L06 |
| G04 | OWNER_AUTHORIZATION | NOT_RUN | G03 |
| G05 | OWNER_AUTHORIZATION | NOT_RUN | G04 |
| G06 | OWNER | PROPOSED_ONLY | None |
| G07 | STUDENT | NOT_VERIFIED | None |

R02 source-only reconciliation suffices for bounded R03 remote scope, independent G01 remains open. G03 formal C9 requires G01, private full positive, required performance/independent proof. L01/L02 nested L03, L05 independent, L07 operator, L08 return blocked statuses. G02 #26 live import NOT_RUN, G06 #27 proposed only, G07 student only.
