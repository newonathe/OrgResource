# Current official zero-cost hosting research, cost ledger and survivability
**Status:** EVIDENCE VERIFIED provider documents as of 2026-11-15; provider/account feasibility, region capacity and uptime UNVERIFIED. **No paid account created and no host provisioned.** Sources E06–E18, E24–E27. Prices/limits are dated snapshots, not binding lifetime rates.

## Hard constraint and scope
**USER DECISION:** all *mandatory* build and ongoing bills = **$0 / ₱0** for a bounded synthetic demo and contemplated future product. No paid domain, paid hosting, paid database, paid message, paid authentication, compulsory purchased storage or silent paid overage. User's existing device, internet, electricity and labor have non-zero real-world opportunity costs; track them separately and do not misrepresent a strict zero *total economic cost*. A verification credit-card authorization hold may not be a charge but still imposes temporary cash availability/eligibility; flag before choosing platform. Theoretically unlimited traffic, permanent availability, no card, real-world SMS and host with a free MySQL VM simultaneously cannot be promised.

## Provider comparison, original official pages accessed 2026-11-15
| Candidate | Quoted free allowance / dated primary evidence | Hard blockers and failure semantics | Compliant with course and live V2? |
|---|---|---|---|
| **Oracle OCI A1 Always Free** | E06: 1,500 OCPU-h and 9,000 GB-h/mo = 2 OCPU/12GB continuous equivalent; 2 AMD E2 micro VM shapes optionally, 200GB block. Home region resources only. | E06 home-region capacity shortages, idle VM reclamation, account/region limits; E07 valid card verification with temporary auth holds; OS/MySQL backup/admin/TLS responsibility. | Can run Java/MySQL if compatible ARM image and build verified. **Best candidate for coherent Java, not proven eligible/available** |
| **Cloudflare Workers + D1** | E08 Workers Free 100k req/day, CPU 10ms/request, 128MB; E09 D1 10 DB, 500MB/DB, 5GB/account, 7-day Time Travel; E10 5m rows read/day and 100k rows written/day; E24 workers.dev hostname. | E11 daily limits enforced → database queries fail until 00:00 UTC reset; free `workers.dev` intended hobby not business-critical; auth and transaction atomicity unverified. | Can serve *separate V2 JS/SQLite* design, NOT original Jersey/Javax course implementation |
| **Render Free** | E12 free web 750 instance-h/month; sleeps after 15 min idle, filesystem ephemeral; free PostgreSQL 1GB but expires 30 days, 14-day paid-upgrade grace; no free Postgres backups. | Fundamental durable-store expiry; outbound bandwidth overage may bill per official doc. | **REJECT** as indefinite free sole database |
| **Supabase Free** | E13/18 free project max 500MB database, low activity pause in 7 days, 2 active free projects; E14 free daily managed backup absent; manual CLI export recommended. | Paused service and backup/egress burden, external API no Java host. | Candidate for bounded isolated prototype DB/auth subject to its security/limits; not a complete course Java host |
| **Google Cloud Free e2-micro** | E15 one e2-micro, up to 30GB standard disk, 1GB outbound data/mo where eligible; 2026 official limits and geography need full qualification. | Tiny shared CPU/memory, billing account verification/overage risk, region constraints, OS/DB/TLS overhead. | Only benchmark candidate; cannot claim usable Java+DB at performance target |
| **Cloudflare Pages static** | E25 up to 500 free builds/mo, 20k files/site, 25MiB/file | Static host only; not database/Java; domain/API CORS issues, review vendor terms | Good optional free asset host for React if API available elsewhere |
| **GitHub repo/Actions** | Existing private repo/CI; GitHub pricing and runner billing need current account-specific verification | Artifact retention, runner minutes, disabled checks and future policies; cannot use repo as private live DB | Source control/CI only, not live store |

**Changes already detected:** Oracle old internet descriptions frequently quote A1 4 OCPU/24GB, but the currently retrieved E06 doc says **2 OCPU/12GB continuous equivalent**. Use E06 and test actual tenancy; do not silently reuse old quota. Render free DB **expires**; cannot cure this with free web restart. Supabase free pause and D1 hard-stop are material service behaviors.

## Proposed zero-dollar budget ledger (not operational cost proof)
| Cost category | Mandatory spend ceiling | Proposed path | Proof required |
|---|---|---|---|
| Domain and TLS | $0 | Provider subdomain (`workers.dev`/Pages if choosing compatible deployment); Java VM DNS+cert still OPEN | Actual owned free hostname + HTTPS end-to-end and renewal test |
| API compute | $0 | OCI A1 **conditional** or Workers Free for separate edge track | Real account eligibility, region capacity, 30d CPU request test |
| Primary DB | $0 | Self-hosted compatible DB on A1 **conditional**, or D1 free with validated locking | TPC-like functional race tests, disks/rows/write quota |
| Identity and signup | $0 | **UNRESOLVED** secure authentication/provider route; demo read-only until verified | No paid reset/email requirement and revocation proof |
| Transactional email/SMS | $0 | In-app notifications only; **no real SMS or mandatory paid email** | Application operates with all outbound channels disabled |
| Backups | $0 | Encrypted external export via free storage under verified quota; user-owned offline backup alternate | Actual off-host restore and key backup; provider/account limits |
| Monitoring/alerts | $0 | Built-in structured logs, conservative free metrics, local scheduled review | Measured log quota and incident alert delivery |
| CI/test | $0 | Existing GitHub Actions within approved included minutes or local tools | Workflow quota dashboard; never auto-purchase overage |
| Payment fees/overages | $0 | No payment collection/paid upgrade; provider hard quota monitoring | Invoice/plan and spend alerts, enrollment terms |
| Device/data/power | Not software-provider mandatory | Existing user's equipment/internet/electricity | Document external variable costs honestly, not counted as invoices |

**Total provider mandatory recurring spend TARGET: $0/month. STATUS: NOT PROVED FOR DEPLOYED PRODUCT** due to eligibility/backup/auth/free DNS/recovery unknown. Even when provider terms say Free, they may change, services may reclaim resources, and users can exceed quotas. Never claim “free forever.”

## Bounded free-workload plan
Assume synthetic P0 ceiling **50 orgs, 1k memberships, 10k resources, up to 64k lifetime allocations in fixture, peak 25 concurrent active users** (NFR assumptions). Rate guard per org 120 write mutations/hour and user 60 requests/min by default, with server-global daily cap initially 50k requests/day and 10k D1 write rows/day *if Workers selected* (far below published free quotas), and a 2× operational margin. These caps are proposed, not guaranteed safe until typical SQL row reads/writes per route measured. Compute query complexity and storage per event/table, log growth and retention; incident retries must not amplify D1 usage. If actual managed provider enforces caps automatically, fail closed before hard quota to preserve data and require operator reset/reduction.

## Recovery portability with no hidden service bill
1. Versioned schema migrations + v2 contract version, UTC timestamps and tenant relationship checks, no embedded platform-specific business rules without replacement.
2. Nightly export (if service has reliable scheduler) to encrypted **different failure domain**; copy to user-owned offline encrypted medium (device storage and transfer costs not zero economic cost). Hash SHA-256, store manifest schemaVersion, highest event ID, org/resource row totals, fixture-only public demo marker and rotation.
3. Weekly import dry-run into local compatible engine; monthly clean **full restore** to different VM/account if legally eligible and available without buying anything. If no independent free online region, state RTO cannot be guaranteed.
4. Simulate lost primary VM/disk, lost credentials and provider account suspension; check restore without proprietary locked key. Backups on same VM do not count as disaster recovery.
5. Proposed RPO <=24h and RTO <=48h **targets contingent on tests and host availability**, no current result.
6. If quota approaching 80%: warn operator; at 90% block noncritical writes; if hard stop reached show status/read-only until reset; never attempt a paid upgrade automatically.
7. If no free production host satisfies all gates, **safe outcome = private/local functional synthetic demo, not a production multi-user release.**

## Cost/hosting acceptance checklist
No provider purchase/enrollment now. Before choosing: published plan & TOS and eligibility PH, card/temporary hold, region/hardware capacity, host CPU-memory-disk and TLS, usage stop behavior, off-host exports and restore, responsible maintainer, plan cancellation/export, incident/security responsibility, authentication and license compatibility, check quotas at one synthetic month, and explicit $0 billing proof. Call selected candidate APPROVED only after these tests and user choice. Multiple hosted alternatives evaluated, none asserted deployed.
