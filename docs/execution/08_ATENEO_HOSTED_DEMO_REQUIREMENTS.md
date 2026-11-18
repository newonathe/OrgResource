# Hosted OrgResource — Real Ateneo organization reference facts + clearly simulated eleven-month usage

**Edition:** 2026-11-19 (PHT). **Type:** USER DECISION / DESIGN RECOMMENDATION (future product requirement). **Status:** PLANNED; actual hosted dataset, permission, seed engine, tests, deployment and approval are **NOT IMPLEMENTED / NOT RUN**. This specification supplements **all** existing AGENTS.md and docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md criteria; none may be shortened or waived for demo appearance or speed.

## 0. Requirement and principled interpretation

**USER DECISION:** At the first owner-approved hosted demo release, present a populated, credible Ateneo de Manila student-organization resource-management workspace reflecting approximately eleven months of **simulated** realistic usage. Draw the organization identity and publicly documented organization type/activities from actual accessible, dated sources, spanning:
1. Council of Organizations of the Ateneo – Manila (COA-M) listings;
2. League of Independent Organizations (LIONS) listings;
3. Other student-led or independent entities **only when reliable public sources establish their independent status** or the organization authorizes inclusion.

**CRITICAL:** COA-M and LIONS are not three disjoint official classes with "Independent". LIONS is itself the umbrella for independent student organizations. Do not double-count or invent an unverified third class. Source recweek2026 directory lists named organizations in COA-M and LIONS categories. The RecWeek 2026 homepage says COA-M has 55 accredited organizations while its dedicated COA-M profile says 53. Neither number should become an acceptance target before resolving this **source conflict**. Registration category is year-scoped; do not claim permanent or current organizational status beyond the cited 2026 listing.

**Boundary:** Publicly observed names/descriptions are real public reference facts. Resource inventories, balances, project records, event dates unless separately sourced, members/officers, transactions, approvals, requests, borrowings, stock movements, reminders, analytics, age of use and adoption are **fictional simulated demo records**. Even if a name is real, combining it with a manufactured transaction does not make the transaction real. No "Ateneo has used this since 2025", "X org manages Y cameras" or equivalent false implication. Display a prominent permanent DEMO/SIMULATED banner, startup/about disclosure, and per-record provenance in UI/API/export.

## 1. Public sources checked 2026-11-19

**VERIFIED EXTERNAL FACT (bounded to retrieved public pages):**
- Ateneo RecWeek 2026 Home: https://ateneorecweek.org/ — describes COA-M and League of Independent Organizations; conflicting count note above.
- Ateneo RecWeek 2026 COA-M directory: https://ateneorecweek.org/coa-m-orgs — lists e.g. Computer Society of the Ateneo, Ateneo Consultants for Organization Development and Empowerment, Company of Ateneo Dancers, Ateneo Celadon, Ateneo Musicians' Pool and Ateneo Blue Repertory.
- Named COA profiles: https://ateneorecweek.org/coa-m-orgs/compsat ; https://ateneorecweek.org/coa-m-orgs/ateneo-code ; https://ateneorecweek.org/coa-m-orgs/cads .
- 2026 LIONS profiles: https://ateneorecweek.org/lions-orgs/plume ; https://ateneorecweek.org/lions-orgs/laro-loyola ; https://ateneorecweek.org/lions-orgs/blue-bird-improv ; https://ateneorecweek.org/lions-orgs/mokoa-animation ; https://ateneorecweek.org/lions-orgs/blue-symphony-orchestra ; https://ateneorecweek.org/lions-orgs/league-of-ateneo-historians .
- LIONS information: https://admulions.wixsite.com/admu-lions/about-us — describes LIONS as an autonomous body of independent organizations. It is corroborating context; recheck currency before production.
- Ateneo Loyola Schools OSA organization page: https://sites.google.com/ateneo.edu/ateneolsosa/student-life/student-organizations — organization administration context; its 2019–20 data should not be treated as 2026 roster.
- Philippine NPC, Republic Act 10173: https://privacy.gov.ph/data-privacy-act/ ; IRR https://privacy.gov.ph/implementing-rules%20regulations-data-privacy-act-2012/ — principles of transparency, legitimate purpose, proportionality. These are privacy constraints, not blanket permission to collect public individuals' records.
- Ateneo OSA resources page refers to symbol/branding guidance: https://sites.google.com/ateneo.edu/ateneolsosa/student-life/download-resources — request review before using marks/logos and official-looking branding. Public listing alone does not grant organization endorsement.

**Source snapshot:** data/reference/ateneo-public-organizations-2026.json — twelve **name+2026 listing category only** entries, each carrying URL, date and evidence classification. It contains no authenticated org source, inventory measurement, real user, real contact, financial data, or simulation history. Re-fetch and revalidate at actual seed-release date; if public page is gone, retain dated provenance with archival proof or mark source stale/blocked.

## 2. Production UI truthfulness contract

Across anonymous preview, signed-in demo, exports and any seeded analytics:
- Persistent visible badge/banner: **"DEMO ENVIRONMENT — Simulated operational data. Organization names are sourced from public 2026 listings; inventories and activity are fictional. No affiliation or adoption is implied."**
- Header/subtitle for historical panels: **"Illustrative activity, Dec 2025–Nov 2026 (generated for demonstration)"** (adjust display only if actual configured generation dates change).
- Organization profile source note with listing year, source link, checked date, independently sourced facts and any uncertainty; a public name cannot be interpreted as a customer.
- Each fictional Project/Resource/Requirement/Allocation/Movement/Activity must carry at least an effective dataset-level provenance of SYNTHETIC_DEMO; on API/export include a truthful machine-readable dataset identifier and indicator where technically feasible. No fictitious officer may be displayed as a real person's name. Use aliases ("Demo Custodian A", "Demo Project Lead B") and no college email that may be someone else's real mailbox.
- No real Ateneo seal/official organization logos/real people's photos without reviewed rights and authorization. Use app-original generic icons, abstract item images, no unlicensed web imagery. Distinguish public reference names from alleged official Ateneo partnership.
- Public overview views must not reveal a normal live tenant's data or allow anonymous mutation of demonstration organization records. If interactive role scenarios are allowed, run in **isolated ephemeral demonstration tenants** with automated bounded cleanup and user controls; never share one writable public owner account. Keep external SMS disabled; no real email, SMS, carrier, Twilio credentials or org officer outreach.

## 3. Data segregation and safety requirements

- DATASET MODE: Demo is an explicit, separately provisioned database/schema or logically fenced tenant cohort with verified DB/API isolation; **prefer separately managed demonstration deployment/database** for public showroom and a clean production deployment for real users. Never silently insert 2025–26 fictional history into user-owned tenants, production org data or frozen Course A.
- IMPORT AUTHORIZATION: explicit operator action, brand-new demo-only target, environment signature/allowlist, db name/host exact confirmation, migration/checksum verification, named dataset ID/version, dry-run preview and optimistic uniqueness check. No auto-import on every JVM startup. Runtime DB credentials remain DML restricted and cannot rewrite append-only movements/audit or drop schemas. A separate single-purpose fixture provisioning identity may be privileged only for a fresh isolated demo schema, with all use audited.
- REPEATABILITY: determinism by immutable seed and generator version, fixed DEMO_AS_OF_DATE, fixture profile, and stable external reference keys. Repeat import into an already seeded target must be safe no-op, with identical dataset hash; mismatched version must fail with clear instructions or an explicit migration after backup; partial failure must roll back or be recoverable in a new empty target. Never reset or prune genuine user records. A public demo reset only acts on explicitly ephemeral demo tenants with typed operator approval and destructive-target guard.
- AUTH: each fictional account is a synthetic persona with no real Ateneo account implication; bootstrap owner flow is one-time and guarded by C1BootstrapService. For multi-org fixtures design a dedicated restricted **demo initialization workflow**, not repeated first-owner bootstrap and not direct blind SQL mutation. Role assignments must be genuine memberships/tenant permissions.
- HISTORY: avoid forged past audit timestamps by simply modifying immutable events after posting. The fixture engine must use explicit simulated event-time boundaries and the normal business services with an injectable clock/test-controlled scheduler or dedicated transactional fixture importer **on a disposable, clearly demo-only database**, validate resulting stock/audit and never bypass integrity constraints. Distinguish time-created (actual import) from simulated effective event-time where possible.
- No production UI analytics should accidentally aggregate demo data into real customer activity, active user count, operational savings, institutional impact or adoption. Demo metrics are marked SIMULATED, with formulas and deterministic fixture count.

## 4. Minimum launch-ready dataset profile (DESIGN TARGETS, not observed Ateneo facts)

- Initial public-reference profiles: **12** sourced organizations: 6 COA-M and 6 LIONS; additional independent names only with source and rights review. This is a *curated sample*, not all Ateneo organizations.
- Historical window: **2025-12-01 00:00 to 2026-11-18 23:59** Manila time, approximately 11½ months; anchor DEMO_AS_OF_DATE=2026-11-19. This is an **illustrative simulation period**, not actual platform uptime. If deployment is after that date, preserve explicit labeled historical simulation or regenerate to a newly approved fixed date with refreshed sources and deterministic version.
- Default size target: 2–5 fictional operational departments per org (24–60 total); 3–8 simulated projects/org (36–96); 8–20 generic resource types/org (96–240); 2–5 requirements/project; 40–100 allocation requests/org over simulation (480–1200 total), each with coherent status progression and custody outcomes; movement/audit counts arise from real state transitions, **never hardcode ledger counts**.
- Mix reusable equipment and consumable supplies without claiming org-owned assets. Vary activity to demonstrate cross-department scheduling, seasonal event workload, restocking, pending/approved/issued/returned/consumed/cancelled and long-tail zero usage. Distribution is a simulation design, not research about true demand.
- Fictional personas: 4–7 synthetic non-personal identities per demo org with tested OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER permissions; public preview cannot authenticate as real named officers. Reuse the same fake personas only where role boundaries tested.
- Public preview cannot expose raw free-text borrower phone, reminders, authentication tokens, password/recovery codes, API secrets or image with real person.
- Low-capacity CI fixture: 2–3 orgs, one month, ~10–40 requests to finish quickly while preserving every state/role branch. Full fixture: 12 orgs, full history. Stress profile separate and not automatically shipped.
- Visual differentiation: realistic generic resource labels by mission domain (for example, audio kit for a performing arts scenario) are **fictional**; project titles should be original demo titles prefixed/marked as fictional, not copies of undocumented real org event names. Publicly documented events may appear as sourced descriptive context only; never attach invented allocations to them as if documented.

## 5. Core acceptance and user journeys

1. Clean demo host responds with preloaded tenant selector, full twelve organization reference profiles and eleven-month historical chart after **explicit authorized fixture import**, without requiring local developer-only secrets.
2. At least six COA-M and six LIONS examples are available as separate scoped organizations; category data matches the dated public roster file, with clear source badges and no invented third classification.
3. Demo owner/custodian/lead/member can view realistic permitted workflows; tenant switch cannot leak data from prior organization. Anonymous preview read-only. Any interactive trial is isolated from every other trial and auto-expires by defined policy.
4. Every dashboard number reconciles with database resources/holds/issued/reusable/consumable stock; historical event timeline is chronological and consistent with the service status machine and project dates.
5. Resource stock is conserved; all movements are append-only; no replayed allocations or double consumption; no negative availability; historical holds do not inappropriately remain active in the latest snapshot; all datetimes valid and timezone-aware at display boundary.
6. Generator rerun same seed/version and identical fixed as-of produces identical canonical fixture manifest/event graph (DB generated IDs can vary but external keys, logical outcomes and canonical hash match); repeat import safe; different version fail closed.
7. Browsers show demo/synthetic label on every relevant route, page, export and API; accessibility keyboard/mobile/reduced motion; no claim of real adoption, savings or actual asset custody.
8. Security tests assert no real data or services, no outbound SMS, no real-world identity, no default internet-accessible owner credentials, no unauthorized org mutation; secrets only as protected operator input.
9. Independent source provenance audit validates citations/date/category; rights and privacy review documented; removed/stale org names can be disabled with transparent dataset version without silently changing historical evidence.
10. Both tiny and full datasets pass real MySQL 8 migration, Java 21 packaged API, auth, Newman, Playwright, backup/off-host restore and cleanup tests; immutable Java 8 Course A bytewise unchanged.
11. Benchmark full-fixture build and import time, DB/index/dump bytes, p95 relevant list/dashboard queries, backup size; if unmeasured mark NOT_RUN. Performance is tuned based on real tests, not by dropping rows or history.

## 6. Implementation placement in grouped pass plan

- **C6 (source+contract):** audit public roster, source-license/rights, code boundaries, schema/clock/fixture insertion strategy, dataset size/trace and privacy decision. This new requirement adds DEC-13 public org fact roster and DEC-14 demo disclosure/reset authorization. C6 source coverage register must now include this file and the JSON roster.
- **C8-D (within C8, behind authorization):** build deterministic fixture generator, immutable provenance, guarded explicit importer, full domain/role/UI demo experience and history. Enforce exact C8 domain invariants; no fake service method, no direct bypass of protected stock. Treat it as a **coherent bounded vertical slice**, not a standalone parallel writer competing with inventory migration authors.
- **C9:** adversarial validation, perf/build/storage measurements on tiny/full fixtures; DB size and query/load benchmarks; independence/isolation and correction.
- **C10-D:** after explicit hosting authority, provision demo environment separately with protected credentials and strict read-only public preview, import once, test backup/recovery and no live SMS.
- **C11:** independent verification of fact/synthetic distinction, UI source attribution, no implied org endorsement, full source and data math, real hosted browsing, owner-approved release criteria.

## 7. Scope cuts and STOP conditions

Optional cuts: number of roster profiles after source provenance review, number of generated projects and transactions (subject to a credible eleven-month dated history), generic decorative images, detailed historical chart beyond minimum required workflow. **Never** cut truthful labeling, provenance, tenant isolation, protected academic implementation, transactional movement consistency, identity protections, genuine role denial, source rights review, critical tests or independent audit.

**Hard STOP** if an organization is not publicly verifiable for its asserted listing status; any real contact/member asset records are requested without authorization; website rights unclear for a logo/image; import points at non-demo schema; fake history appears as verified activity; balance/replay mismatch; org impersonation/admin access risk; bad migration checksum; unknown hosted policy or legal authority; unresolved P0/P1. Mark precise BLOCKED/NOT_RUN evidence; do not pretend these are completed.

## 8. Owner decisions

DEC-13: Confirm scope of 12 referenced real public org names (6 COA-M, 6 LIONS), whether any additional separately independent group will be curated; classification year and public source only. Default: curated twelve.
DEC-14: Confirm permanent demo/synthetic attribution and explicit "not affiliated" note for public host. Default: mandatory.
DEC-15: Authorize dedicated public demo database and determine public guest read-only vs optional isolated temporary trial sessions; default: read-only preview + no shared writable tenant.
DEC-16: Authorize original-only placeholder branding and whether external org logos require permission. Default: no logos.
DEC-17: Approve fixed as-of 2026-11-19 with simulated period 2025-12-01–2026-11-18 and full fixture size; default as design target, cannot be declared actual observed traffic.
DEC-18: Define approved preservation/reset/TTL strategy and backing up demo database. Default: explicit operator import and isolated backups.
DEC-19: Real organizational inventory/contacts/transactions would require separately obtained permitted source plus consent/authority, documented privacy assessment and distinct genuine-data production tenant; no automatic import from social media, public posts or inferred assets.

**Final governing sentence:** Source real organizations from verifiable public information; synthesize transparent and mathematically valid operational history; never counterfeit organizational adoption, use, membership, inventory, personal data, authorization or university endorsement.
