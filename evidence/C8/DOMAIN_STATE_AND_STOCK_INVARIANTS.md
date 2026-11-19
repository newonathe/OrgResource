# C8 domain and stock invariants

Department owns many Projects. A Project belongs to one Department and defines a start-inclusive, end-exclusive Philippines-local business period. One ResourceRequirement links each Project/Resource pair. Allocation records a whole-batch request against its Requirement. Current Extended organization predicates apply to every query. Existing Flyway composite constraints preserve tenant-local relations and protected history.

| Fact | Authoritative effect |
|---|---|
| Physical total | Stored whole units, reconciled against the sum of movement total deltas |
| Pending | Creates no hold or custody |
| Approved, effective while Project end is in the future | Holds the approved quantity |
| Issued reusable | Moves from approved hold to outstanding custody without a second reduction in available units |
| Returned usable | Releases issued custody once |
| Consumed | Reduces physical consumable stock once and remains covered |
| Cancelled/expired approval | Releases eligible hold; existing issued custody stays unavailable until return |
| Requirement coverage | Current approved plus issued, returned and consumed, then shortage is max(0, quantity minus coverage) |
| Replayed stock writing operation | Existing movement/receipt transaction and role checks govern the result |

The C8 reads reuse the same approved calculations; schema and writes stay on C7 lineage. New regression fixtures assert partial completion, 1,001 pending rows with no additional holds, ledger/allocation/receipt postconditions, and tenant-scoped reads. The SQL page contract uses the fixed number of returned rows. Availability mismatches return a controlled failure.

Borrower labels remain officer-entered descriptions. No user account is inferred from a name. There is no persisted assigned-Project LEAD or borrower-membership grant; adding either depends on an approved foreign-key, permissions and history contract.
