# C8-IR3 technical status

**C8 BLOCKED — MANDATORY AUTHORIZATION PROOF REMAINS.** The new IR3 tests ran at `0dc1ea87432db8b139448082e57957111919bb10`, with application code unchanged from `0b8da10d76d746b315d9601e411acbb2ca48882b`. [Draft PR #25](https://github.com/newonathe/OrgResource/pull/25)

- **New IR3 proof.** The source and packaged Jersey manifests matched all 93 declared operations. A separately reviewed packaged HTTP/MySQL batch passed 74 original authorization cases, including 38 denied mutations with equal per-request database snapshots. The reviewer also reproduced two existing Java/MySQL integration scenarios on isolated databases. The reconciled ledger retains all 22,320 original IDs. It has 74 passes and 22,246 without complete proof. No provisional exclusion was approved.
- **New ordered role-change check.** A queued request body was released after the assigned Lead's demotion and assignment removal committed. The create returned HTTP 403 with unchanged compared database rows. Simultaneous database lock contention was outside this check.
- **Retained gates.** Prior independently reviewed full-row restore evidence remains valid for the unchanged application and recovery code. The tested snapshot passed hosted Course A Java 8/MySQL with 61 tests and Extended Java 21/MySQL with 59 tests. [Hosted verification](https://github.com/newonathe/OrgResource/actions/runs/44659370210)

C9 remains on hold. Public release remains blocked. The authorization ledger, detailed local receipts and fixture data stayed in the restricted evidence packet.
