# C8-IR4 technical status

**C8 BLOCKED — MANDATORY AUTHORIZATION PROOF REMAINS.** The application tested at `fc752e33821a06f962ede1f8fe74efa9c88ded0d` is unchanged from `0b8da10d76d746b315d9601e411acbb2ca48882b`. [Draft PR #25](https://github.com/newonathe/OrgResource/pull/25)

- **New packaged HTTP/MySQL proof.** The reviewed batches passed 96 Allocation transition cases and 29 borrower and receipt cases. The 94 denied mutations each have an unchanged database snapshot. The new receipt tests include denial after the original stock operator's demotion and the HTTP receipt's expiry.
- **Controlled stock concurrency.** MySQL recorded both record waits in each receive-versus-adjust order. The first authorized request committed once. The second returned stale-version denial with no movement or committed receipt.
- **Reconciled ledger.** All 93 declared routes and 22,320 original IDs remain present. The ledger records 199 executed passes, 48 independently reviewed structural exclusions, and 22,073 remaining unrun cases. No equivalence was credited.
- **Recovered Java 8 CI.** The exact-source rerun passed Java 8/MySQL, Extended Java 21/MySQL and React. The earlier Java 8 archive extraction failure occurred before tests. [Successful retry](https://github.com/newonathe/OrgResource/actions/runs/44662915834/attempts/2)

Course A and the existing restore proof remain intact. C9 remains on hold. The case ledger, fixture data and detailed receipts stay in the restricted local packet. Public release remains blocked.
