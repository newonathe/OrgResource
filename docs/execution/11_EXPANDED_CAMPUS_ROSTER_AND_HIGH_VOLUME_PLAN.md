# OrgResource — Expanded 2026 Ateneo COA-M, LIONS, MISA and Sanggunian demo coverage and high-volume plan

Edition: 2026-11-19 Asia/Manila. **USER DECISION**: maximize coverage of the verified public organization directory and generate substantially richer fictional history for MISA, Sanggunian and a selected cohort of high-visibility COA-M listings. **STATUS: SOURCE REFERENCE AND PROPOSED CONFIG ONLY**; no historical dataset has been generated, imported, tested or hosted. All earlier protections, source completeness and quality gate still apply.

**Binding read-first:** root `AGENTS.md` in full; `docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md` in full; `docs/execution/00_START_HERE.md` through 10; this document; `data/reference/ateneo-recweek-2026-expanded-directory.json`; `data/demo/ateneo-volume-profiles-2026.json`; earlier `ateneo-public-organizations-2026.json` and `ateneo-public-programs-2026.json`; current assignment/syllabus/source files; actual Extended implementation and C5–C11 reports.

## 1. Scope revision: original 12-record sample → larger sourced directory

The prior **12-name** sample remains a historical v1 research artifact; it is no longer the target for the full hosted demonstration. The proposed versioned v2 source and profile contain:

| Source-backed cohort | Count | Evidence | Meaning |
| --- | ---: | --- | --- |
| COA-M RecWeek 2026 DIRECTORY ENTRIES | **72** | https://ateneorecweek.org/coa-m-orgs, checked 2026-11-19 | Entries displayed on one official 2026 RecWeek directory page, **not** a claim of 72 accredited COA-M organizations |
| LIONS 2026 specifically identifiable via RecWeek point-person list and/or 2026 individual profile | **24** | https://ateneorecweek.org/about and individually linked profile pages, checked 2026-11-19 | A conservative proven subset, **NOT a claim that there are only 24 LIONS organizations** |
| Historic/undated independent-org directory, with no 2026 corroboration found in this pass | **8** | https://admuorglife.carrd.co/, historical/undated | SOURCE REFERENCE ONLY, excluded from default seed until current evidence is confirmed |
| Full proposed 2026 **demo entries** | **96** | 72 + 24 as above | Includes Sanggunian as a **student-government entry**, COA-M umbrella, service bodies and publications as their own types; does not represent 96 COA-M/LIONS accredited member orgs |

The COA-M 2026 site itself contradicts its counts: homepage cites **55 accredited**, COA-M profile **53**, secondary undated org-life directory **56**. Because the COA-M **directory page includes Sanggunian and student service/publication/governance bodies**, 72 displayed entries is not an accurate accredited-membership denominator. The roster file explicitly records each `institution_kind` and individual accreditation as `NOT_INDIVIDUALLY_VERIFIED_BY_DIRECTORY`.

LIONS is the umbrella for independent student organizations. **Do not invent a parallel third class called "Student-Led"** simply for the data model. 2026 RecWeek point-person listing confirms many current LIONS entries, but its list is not demonstrably complete. C6-D must try to obtain an authoritative complete 2026 LIONS index/approval from publicly accessible primary source, reconcile moves/renames/duplicates, and append only with exact dated evidence. Until then, 24 is current covered subset and incompleteness is an explicit OPEN QUESTION.

Cross-year movement: 2026 COA-M directory displays Aspiring Restaurant Entrepreneurs Association and Habitat for Humanity Blue Chapter, though an older directory listed those under LIONS. Laan Ateneo and older Project LAAN may be connected, but equivalence is not verified. Current year-specific source takes precedence for category; record potential aliases and never duplicate into both groups.

## 2. MISA and Sanggunian as separately highlighted flagships

**MISA:** Ateneo Management Information Systems Association is explicitly listed in 2026 COA-M RecWeek directory and has its own organization profile at https://misa.org.ph/about/ and home at https://misa.org.ph/. The public home describes organizational functions such as communications, operations, marketing, events, eServices, finance and HR; these are public contextual descriptors (do not infer actual items owned, transactions, membership counts or historical usage). In the mock demo, use distinct fictional department/team labels aligned with *general* functions: Demo Digital Events, Demo Training, Demo Equipment, Demo Operations, Demo Outreach, Demo Media. No real project date or stock claim is established.

**Sanggunian:** https://ateneorecweek.org/coa-m-orgs/sanggunian explicitly describes it as college student government and includes school community activities, governance/policy reform and community engagement. Even though its profile is under the RecWeek /coa-m-orgs URL, do not count it as an ordinary accredited COA-M member organization. It is its own `STUDENT_GOVERNMENT` reference and a dedicated demo tenant. Its mocked workflows should be realistic generic public-sector style: Demo Student Services Kits, Demo Campus Outreach Logistics, Demo General Assembly Setup, Demo Publications Support, Demo Event Supply Handover. No genuine government document, student case, council record, fund, or member can be inferred or imported.

**"Biggest COA" definition:** There is NO validated 2026 member-count/asset-count ranking across COA-M organizations in available sources. Consequently `FEATURED_SHOWCASE` denotes a deliberately high-volume **demonstration workload**, selected to provide diversity of task workflows, not a factual size rating. Do not display "biggest Ateneo org" as a real fact, claim highest membership or publish fake member headcounts. If dated official org-by-org member counts become available legally and comparably, create a separate sourced quantitative ranking register with year/definition/denominator and revise featured tiers after owner approval.

**Fourteen featured showroom entries (priority by example scenario, NOT headcount)** include: AIESEC in Ateneo, Ateneo Blue Repertory, Ateneo Celadon, Ateneo CODE, Ateneo College Glee Club, AMA, MEA, Musicians' Pool, Student Exchange Council, CADs, CompSAt, COA-M Council (umbrella, not member), Kythe-Ateneo and The GUIDON (publication, not silently COA member). Exact IDs in source profiles. Other organizations still receive usable demo data.

## 3. Explicit synthetic volume contract and how to interpret it

The versioned **design config** `data/demo/ateneo-volume-profiles-2026.json` is a set of proposed bounded target ranges for the **2025-12-01 through 2026-11-18** illustrative period with 2026-11-19 as-of date. It is NOT measured demand, member count or a preexisting data dump.

| Tier | Cohort count | Synthetic requests/org | Synthetic projects/org | Synthetic resources/org |
| --- | ---: | ---: | ---: | ---: |
| Sanggunian flagship | 1 | 2,500–4,500 | 45–90 | 75–150 |
| MISA flagship | 1 | 2,000–4,000 | 40–75 | 65–130 |
| Featured showcase | 14 | 600–1,400 | 18–40 | 30–65 |
| Standard 2026 COA directory entries | 56 | 120–400 | 5–15 | 10–30 |
| Standard verified 2026 LIONS examples | 24 | 60–240 | 4–12 | 8–24 |

This means **21,060–56,260 fictional allocation requests in total** as a target *range*, with extra ledger/audit events determined by actual valid service transitions. Counts reflect the assigned synthetic workload, **not authentic organizational operations**. This scale must be benchmarked against **current 64 MiB backup-size bound**, MySQL indexes, import/reconciliation duration, browser page cursors, realistic storage growth and recovery; adjust bounded fixture size **only with owner-approved scope and real performance evidence**. No artificial history deletion just to meet a benchmark.

Separate profiles are required:
1. `TINY_CI_DISPOSABLE`: small subset with full status/role/negative-path coverage for short inner-loop feedback and no hidden 96-tenant burden.
2. `FULL_96_ORG_SHOWCASE`: owner-facing hosted reference experience, all 96 current-directory entries once the full importer and real MySQL acceptance pass.
3. `SEPARATE_STRESS_TEST_NOT_SHIPPED`: optional larger volume for limits and concurrency; cannot be counted as installed public data.

Every entry has source fact fields, simulation tier, and **explicit fictional operational provenance**. Synthetic accounts (real users not contacted or impersonated), departments, items, fixtures and images must be fictional and source-rights-safe. Public program titles may be shown only as sourced explanatory context; **never tie invented inventory movements to factual program names** so that they appear to have actually happened.

## 4. Generator scaling and algorithms

The earlier 09_DEMO_SEED_ALGORITHM_AND_ACCEPTANCE.md defines the safety rules; this v2 high-volume addendum overrides only the 12-entry/low-volume targets. Require:
- Strict source fact/alias validation; each reference key globally unique; category `institution_kind` is separate from directory section and membership rank. All 72 2026 COA-M page entries and 24 2026 LIONS names get an explicit row unless a source correction provides auditable rationale. Historic-only 8 remain out of default.
- Deterministic stable sub-seeds by `dataset_version || organization_reference_key || month || local_object_key`. Independent org generation does not drift when new orgs are added. Use canonical manifests and hash validation excluding runtime-generated crypto nonces, bcrypt salts, database identity values and import timestamps.
- Generate a chronological event graph with a controlled fixture business clock in a dedicated demo-only import process. Preserve actual identity/security real-time; do not rewrite append-only audit or movement timestamps. New safe historical replay seam may require approved architecture and migration; no direct illicit SQL modifications.
- Stock math: opening stock + receipts - legitimate decreases equals current resource totals; holds/issued custody reconcile by real state; no negative available, overbooking, fabricated supplier/purchase facts, duplicate issue or return, illegal transition, missing FK or cross-org actor.
- Avoid naïve `O(A×R)` resource scans and N+1 DB loops. Use per-org keyed maps, sorted event streams (`O(A log A)` worst-case), bounded database reads/pages and indexed resource-ID/version operations; lock same-stock transactions deterministically, preserve idempotency keys. Do not introduce generic CQRS/queues/Redis absent proven need.
- Record historical simulated month buckets and activity distribution; no implied actual semester/organization volume; ensure all 12 requested month buckets and both active and completed scenarios as-of, but no event may violate project start/end rules. Distribution and variable event count are algorithm design, not claimed Ateneo telemetry.
- Ensure generated **MISA and Sanggunian** flagships each have multiple rich dashboards, operation event types, timestamps, denials/expiry/repair scenarios and long paged histories. Every 2026 listed entry gets enough varied records to exercise browse/search/filter, as compatible with the actual approved schema.
- Test performance with indexes and EXPLAIN; require bounded history cursors, stable deterministic sort, separate MySQL whole-dataset verification, memory/CPU and backup efficiency, not unbounded ORM eager loading.
- Parallel seed generation per independent org for throughput if safe; serialize importing into same stock resource, protect one migration owner, one import owner and isolate operator credentials. A separate high-reasoning adversarial reviewer recomputes math from SQL.

## 5. Security, privacy and non-affiliation

- Public demo should never imply all organizations already use OrgResource. Mandatory banner including on statistics/exports: **"DEMONSTRATION — Public Ateneo organization references; all operations, dates, accounts, inventories and transaction history are simulated. No organizational endorsement or live system usage is implied."**
- Student government/committee/court names require particular care: no fake case outcomes, elections, disciplinary history, confidential financials, named student files or official reports. Store only generic fictional resource workflows. Real officer names and personal contact data are prohibited.
- Public listing is not permission to copy logos, seals, event photography or bulk directory content verbatim. Use source links, short factual labels and original graphic elements. No source-derived member names, real emails or physical identifiers.
- Prefer a demo-only datastore, dedicated read-only public preview and carefully isolated private interactive trial. Real production tenants must not share or inherit fabricated historical records. Manual guarded import only, never at JVM startup. No public admin password committed; keep live SMS/email disabled.
- Security tests cover stale categories, new 2026 org overlap, attempt to impersonate a government office or officer, tenancy within 96+ orgs, unbounded public API use, forged guest role, write and idempotency replay, invalid dataset target, partial/import failure, clock drift and source-rights mismatch.

## 6. Expanded mandatory gate and honest proof

In addition to **all** earlier C6–C11 and 08–10 QA requirements:
- [ ] **RSTR-01**: full 72-entry COA-M 2026 directory included, each name matches directory; government/arms/services/publications correctly typed, no claim all 72 COA accredited.
- [ ] **RSTR-02**: at least 24 2026 LIONS source-evidenced entries included and unique, each has link/date/evidence type; eight historic-only excluded by default. Search for additional verified 2026 names, document completeness boundaries. No claim complete LIONS roster without independent authoritative evidence.
- [ ] **RSTR-03**: Sanggunian properly STUDENT_GOVERNMENT, MISA properly COA directory-listed, both high-volume flagships with unique company-free demo slugs, fictional users.
- [ ] **RSTR-04**: sector/category changes and duplicates across years triaged (AREA, Habitat, Laan/Project LAAN), no two tenant references for same verified identity without approved reason.
- [ ] **RSTR-05**: snapshot source hashes, URLs, retrieval date, source list count conflict and historical-only statuses preserved; no real officer/student personal data.
- [ ] **SCALE-01**: selected 96 distinct demo tenants correctly initialized from **fresh demo-only** MySQL, no bootstrap replay misuse, no real table reset, no hidden 64 MiB backup regression.
- [ ] **SCALE-02**: MISA/Sanggunian allocation and project counts meet **approved** declared generated ranges or explicit documented adjusted bound; all records have real business-transition receipts and source/synthetic labeling.
- [ ] **SCALE-03**: featured 14 and standard 80 have proportional configured histories, proper pagination and accessible UI; no fabrication of membership size claim.
- [ ] **SCALE-04**: independent MySQL inventory recomputation and tenant permissions on all orgs, collision of stable IDs, no cross-org writes, no hidden data corruption/abandoned imports.
- [ ] **SCALE-05**: cold/warm import, memory, query p50/p95 and EXPLAIN, source ZIP/JAR, database/index size, encrypted backup/recovery time measured; no fake speed/storage claims.
- [ ] **SCALE-06**: real hosted user can browse historic synthetic data and factual organization references; source attribution and permanent demo disclaimers tested in desktop/mobile/keyboard/API/export.
- [ ] **SCALE-07**: full mandatory existing Course A / Extended real-MySQL/Newman/Playwright/backup gates on exact SHA, with independent second-pass reviewer; course protected SHA unmodified, no migration checksum editing, no uncontrolled internet resources.
- [ ] **SCALE-08**: if app/DB not deployed, all hosted/real dataset checks NOT_RUN/BLOCKED. A JSON directory and load profile **are planning artifacts, not seeded database evidence**.

## 7. Codex sequencing and subagents (additive C6-D to C11-D)

**C6-D:** Source/completeness specialist reads original 2026 RecWeek COA directory and individual organization pages, LIONS 2026 about point-person listing, previous org directory and organization-specific profiles, rechecks 72+24+8 classification and curates every reference with source/date; security specialist analyzes MISA/Sanggu identity and false affiliation risk; DB specialist audits true first-owner bootstrap and 96-tenant fixture-safe data model, Ledger/clock/replay; independent high-reasoning critic checks factual roster source coverage and largest-rank claims. Output full source/line/path evidence and resolved scope; no implementation.

**C8-D:** One schema owner reviews new migrations for source/provenance/demo run identity; one domain agent implements deterministic huge mock event graph with actual invariants and guarded one-time import; UI agent adds 96-org selection, large-history browsing and factual-vs-fictional source notes; integration owner runs multi-tenant state acceptance; critic separately derives stock and auth counterexamples. Work may be grouped as one coherent slice if the shared schema/API is frozen, or split at a safe transaction boundary as required. MISA/Sanggu are flagship acceptance fixtures. No change to academic A.

**C9-D:** Independently stress both flagships and full 96-org target; prove no N+1, duplicate event, excessive memory, unbounded export, forgotten tenant predicate, broken cursor or stale backup; measure and tune SQL, import algorithm and tests. Full suites not skipped for speed.

**C10-D:** After explicit user authorization, deploy segregated demo datastore and real HTTPS public showroom with populated 96-org source dataset and 11-month **simulated** usage; off-host recovery at approved volume. No live SMS, source logo infringement, real student data, public shared writable tenant or unapproved paid action. If not authorized, no actual deployment.

**C11-D:** Independent full source and simulation truth audit, public user-testing of prominent MISA/Sanggu, 14 featured plus all standard groups, real SQL and browser metrics/denial, full course compatibility and immutable release/restore audit. Only call **bounded** hosted product accepted after exact release SHA and owner authorization.

## 8. Open questions and blockers

- 2026 LIONS total cannot be equated to 24. Need independently verifiable full directory or explicit limited-coverage claim.
- 2026 **biggest** membership/asset size ranking not established; featured labels are deliberate test-data profiles, never empirical rankings.
- Current real host and DB backup design has a documented ~64 MiB raw SQL bound. High-volume example may break it, requiring safe streaming/encrypted backups and actual restore tests. Do not silently remove rows or unbounded process-memory import.
- Independent-database public product and user-owned real data must remain isolated, with operator and source-rights approval.
- A versioned generator is not inherently valid; test the actual existing MySQL transaction state machine and immutable ledger after event replay.
- Year-specific category ambiguities and roster conflicts require documented source comparison and correction path. Never claim completeness just because the current file has 96 entries.

**Governance:** Requirements are proposed for review and owner-specific implementation phase approval. The current document does **not** authorize C8 code, database migration/import, merge, live credentials/SMS, paid hosting or public release. Every subsequent Codex prompt must copy and enforce the full specialized controls and the main quality gate.


## 9. Flagship public-context fact supplement

Read [data/reference/ateneo-misa-sanggunian-public-context-2026.json](../../data/reference/ateneo-misa-sanggunian-public-context-2026.json) as a source-linked 2026 factual context supplement. MISA's visible public team abbreviations **Comms, OSG, Marketing, OET, Events, eServs, HR** and names **LEAD Month 2026** and **IM Summit 2026** are not proof of asset custody or attendance. Sanggunian's descriptions of **school community activities, governance/policy reform, community/student engagement**, and its public references to **One Big Pride** and **TALAB**, do not establish an item-level project history. Original simulated Projects need distinct, clearly DEMO-prefixed labels and cannot claim association with a real public program unless independently documented and specifically permitted. Do not expand source abbreviations without a source. This supplement needs renewed dated verification at C6-D/C10-D.


**Source key reconciliation:** [data/reference/ateneo-v1-v2-reference-aliases.json](../../data/reference/ateneo-v1-v2-reference-aliases.json) resolves all 12 historic v1 reference keys to the v2 names. Ten match exactly by name; two resolve by identical public organization-profile URL (Mokoa Animation / Mokoa Animation Studio; League of Ateneo Historians / League of Ateneo Historians (LAHi)). C6-D must reverify and C8-D must never create duplicate demo organizations for renamed/aliased source references. This is an alias **documentation** file, not a SQL migration.
