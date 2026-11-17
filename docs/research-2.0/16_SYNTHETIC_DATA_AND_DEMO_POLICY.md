# Fictional dataset, public demonstration and data-isolation policy
**Status:** PROVISIONAL RECOMMENDATION; datasets specified, not seeded or hosted. **Date:** 2026-11-15. Sources A01–A07, E02–E05, G10.

## Honest public framing
Every visitor must see: **“OrgResource is an independent software demonstration using fictional organizations, people and resources. It is not affiliated with or approved by Ateneo de Manila University, OSA, COA-M, LIONS or any student organization.”** Do not use university seals, logos, impersonating domains, real student IDs, campus contact lists, real inventory quantities, actual program schedules, unsourced venue-specific assets, or copied brand assets. Built from verified public process boundaries, not observations of real org software.

## Seed fixture v2.0.0 (proposed, all fictional)
| Workspace | Fictional members, proposed roles | Sample resource / initial stock | Internal projects, Philippine local time |
|---|---|---|---|
| `demo-aurora` — Aurora Materials Guild | Ana Demo OWNER; Eli Demo ADMIN; Bea Demo CUSTODIAN; Cal Demo LEAD; Dee Demo MEMBER | Extension cables REUSABLE 5 PIECE; Paper sheets CONSUMABLE 100 SHEET; Folding stands REUSABLE 3 PIECE | Exhibit A Nov20 09:00–12:00 2026; Exhibit C Nov20 12:00–14:00 2026 (example dates become historic after period; seed clock frozen Nov16 for tests) |
| `demo-harbor` — Harbor Service Collective | Hal Demo OWNER; Ivo Demo CUSTODIAN; Jia Demo LEAD | Extension cables REUSABLE 2 PIECE (same name, different tenant); Labels CONSUMABLE 30 SHEET | Orientation B Nov20 10:00–11:00 2026 |
| `demo-lumen` — Lumen Workshop Circle | Lan Demo OWNER; Mei Demo CUSTODIAN; Noa Demo LEAD | Portable screens REUSABLE 1 PIECE; Markers CONSUMABLE 12 PIECE | Design Circle Nov21 13:00–15:00 2026 |

**Names are invented fictional labels, not representations of actual groups or individuals.** Avoid real-looking phone numbers entirely, even fictitious E.164 strings; no live SMS. No public email sends or real welcome invitations; seed principal IDs as `demo_user_...`, not personal email accounts. All fiscal, venue, permission and attendance values must be fictional labeled fixtures.

### Deterministic acceptance scenarios
S-01 approval 4/5 reusable from Nov20 09–12, overlapping 3/5 Nov20 11–13 refused; adjacent Nov20 12–14 permitted as planned window, but issue at 12 must inspect outstanding actual returns.
S-02 reusable issued4, return3, status PARTIALLY_RETURNED and physical custody1; final return1 clears at-risk downstream requirement; ledger T remains5.
S-03 consumable T100 approve30 and consume30, physical T70, available70; duplicate consume refuses.
S-04 cross-tenant replay `demo-harbor` user attempts `demo-aurora` ID and receives 404/403, never any content.
S-05 owner invites two distinct fictional identities and suspends one; membership effect immediate; no email emitted.
S-06 browser API response loss after mutation; operation receipt reveals committed state and no double movement.
S-07 expiry scan after frozen clock passes end: PENDING/APPROVED expire; ISSUED remains outstanding.
S-08 export/restore + event checksum + composite FK check; no demo/real account mixing.

## Public demonstration deployment modes
Preferred **MODE D-READONLY:** static client bound to deterministic server-snapshot fixture; any apparent modification must say read-only and not imply saved changes. But user requires **genuinely functioning** demo; therefore follow with **MODE D-ISOLATED:** real DB-backed per-session disposable workspace with guest session, rate limits and TTL <=24h. Authenticate guests as `demo:sessionUUID` without untrusted global admin claims, strip all outbound communications, separate database/schema from real tenants, reset table sets atomically and prevent sharing between visitors. No globally mutable common “demo-admin” credentials or single writable tenant. If free resources cannot support guest sandboxes, choose a documented per-session read-only explorer for public viewing and offer a local functional full DB demo; do NOT mislabel the read-only explorer as writable backend.

### Production-data separation
Every real organization would live in a separate identity/tenant namespace not accessible to demo guests. Separate credentials, database binding, backups and logs; prohibit copy/import of real roster into demos. Demo dataset licensing and provenance: user-created fictional seed; no personal/organization facts, email addresses, copyrighted campus images or institutional approvals.

## Demo accessibility, visual, and abuse acceptance
- Preserve Academic Glass Commons v5 (G10), plain independent disclosure in UI header/about.
- Full keyboard workflow, large click targets, dark/light and reduced motion/transparency; no strobing/backdrop-induced low contrast.
- Tests run with 2 simultaneous guests on separate session tokens and inspect DB+response for data isolation; cross-session 404; simultaneous reset mid-request safely refused.
- Test daily quota, app suspension, restoration and no possible paid provider step via public demo clicks.
- Seed function idempotent and environment guarded; source sample Postman path, expected HTTP/JSON and client navigation.
All tests NOT RUN; there are no existing V2 demo seed records.
