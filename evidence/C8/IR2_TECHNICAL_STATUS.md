# C8-IR2 technical status

**C8 BLOCKED — MANDATORY PROOF REMAINS.** The implementation reviewed was `0b8da10d76d746b315d9601e411acbb2ca48882b`. [Draft PR #25](https://github.com/newonathe/OrgResource/pull/25)

- **Authorization gate open.** The source and packaged Jersey inventory matched all 93 declared operations. On isolated MySQL, 146 packaged boundary checks and nine focused scenarios passed. The route, role and state ledger retains 12,449 unexecuted cells. The independent review confirmed missing service-level authorization and per-request database proof.
- **Restore gate passed.** Both the historical and fresh restore matched 118,867 eligible rows across 16 tables. Two session schemas matched and restored empty. The fresh restore refused a wrong key, a tampered archive and a nonempty target. Durable stock replay passed on a separate restored copy.
- **Course A preservation verified.** The exact-source hosted Java 8/MySQL run passed 61 tests. Ten local A3 release checksums matched. Windows-local Java 8 was NOT_RUN. [Exact-source CI run](https://github.com/newonathe/OrgResource/actions/runs/44595020728)

C9 remains on hold. Public release remains blocked. The proofs, fixture data and verification scripts are retained in the restricted local evidence packet.
