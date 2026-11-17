# Falsifiable problem framing and alternatives
**Status:** PROVISIONAL RECOMMENDATION, NOT validated market demand. **Date:** 2026-11-15. Sources A02–A07, E01/E19.

## Evidence is narrower than the pitch
A06 documents an instance of separate budgeting for similar Sanggunian items. A07 describes one organization's existing digital inventory audits. A02/A03/A04 show existing official project/venue systems. None proves that reusable stock shortages, double bookings, missing item histories, adoption interest, financial savings, or willingness to migrate are common. The core hypothesis is **testable**: an org that controls reusable equipment and consumables may benefit from linking resource availability to dated projects, custody and accountable officers.

### Hypotheses and falsifiers
| H | Hypothesis (not fact) | Disconfirming evidence to seek in later optional pilot | Immediate synthetic evaluation |
|---|---|---|---|
| H1 | Overlapping planned projects can compete for shared org-owned items | Existing centralized booking handles this transparently | Two simultaneous requests, one stock ledger, no overbooking |
| H2 | Custody tracking is useful across officer turnover | Org has no reusables or only a stable formal custodian | Borrow → partial/late return → export/reassign |
| H3 | A consolidated project/stock view reduces coordination burden | Simple spreadsheet is already sufficient at comparable effort | Count clicks/error paths; no invented “time saved” |
| H4 | Separate org workspaces allow safe distinct ownership | All inventory actually centrally controlled by another body | Cross-tenant security tests, multiple memberships |
| H5 | Future low-trust self-service can be governed without false org authorization | No trusted mechanism to appoint org admins | Invite-only default; disallow claims without owner action |

## Build-versus-adopt alternatives
| Approach | Evidence of capabilities | Advantages | Failures/limitations and decision |
|---|---|---|---|
| Shared spreadsheet, Forms and manually scheduled handoff | Generic tool capability, not a live campus audit | Almost zero deployment/maintenance complexity, editable, exportable | Formula races/permission mistakes possible; legitimate **baseline** for small org |
| Paper sign-out + spreadsheet | Simple process design | Offline, low cost, familiar | Weak server-enforced conflicts/history; reasonable for tiny needs |
| Snipe-IT self-hosted (E01/E19) | Official asset checkin/checkout, history, multi-company and CSV import | Mature FOSS, avoid reinventing asset records | Need host/admin and comparative verification of project demand, future reservations and free ops; cannot assert gap until hands-on evaluation |
| Other FOSS room/equipment booking tool | Search/selection **pending** | Could outperform bespoke project calendar | Must verify current license, role model and host feasibility; do not invent product capabilities |
| Current academic V1 as real multi-org host | G06–G09 | Real course-compliant CRUD/concurrency core and UI | **REJECT PUBLIC:** no production auth/tenant model, old runtime |
| Bespoke V2 modular monolith | Design proposal | Project requirements+booking+custody can be integrated; full contract control | Security, backups and maintainer burden significant; justify only if bounded product demonstrably adds benefit |

**Decision recommendation:** Produce synthetic demonstration comparing spreadsheet baseline and Snipe-IT on a reproducible checklist (org isolation, project demand, future intervals, approval, custody, returns, exports, operating cost). If existing tools satisfy core needs without unacceptable hosting burden, further bespoke scope is a discretionary learning product, not a validated necessity.

**No invented research:** No surveys, interviews, migration trial, observed campus inventory quantities, official pilots, quantified impact or public partnerships.
