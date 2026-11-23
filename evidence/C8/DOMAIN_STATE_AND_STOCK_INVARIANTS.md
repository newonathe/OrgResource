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

## Current assignment and borrower model

Extended Flyway V6 persists one optional assigned LEAD on each Project and two distinct optional Allocation links for the borrower and requester. The links use tenant-composite foreign keys. Current role and assignment govern access. OWNER and ADMIN manage Project assignments through the versioned operation. Governance clears assignments when the assignee loses LEAD eligibility.

New linked reusable requests require an active same-organization borrower. Consumables may omit that link. The borrower name is captured at creation. Existing text-only records keep their original labels and empty account links. The application never infers an account from a name. A linked borrower sees only their own minimal records and a boolean describing current SMS consent. Linked creation leaves consent inactive. Withdrawal preserves physical stock and history. [Approved borrower and Lead policy](BORROWER_LEAD_POLICY_DECISIONS.md)

## Superseded C7 design

C7 used officer-entered borrower descriptions and had no stored Project Lead or borrower membership links. That model still describes legacy unlinked records. V6 supplies the approved relationships for C8.
