# GitHub and original-source audit; conflict register
**Status:** EVIDENCE VERIFIED (listed observed refs), PARTIAL SOURCE COVERAGE (uninspected material). **As-of:** 2026-11-15 Asia/Manila. Source IDs: [SOURCES.md](SOURCES.md) G01–G11, C01–C05.

## Fresh remote snapshot, not a merge history
Repository `newonathe/OrgResource` PRIVATE. Default `main` observed `16e781c837dd7e1f0481fb7a01e49c27d80a91e8` (O1). Repository read/write permissions checked using connected GitHub account; **no local Codex worktree status can be inferred from remote**.

| Branch or PR | HEAD at audit | Base / status |
|---|---|---|
| main | `16e781c837dd7e1f0481fb7a01e49c27d80a91e8` | old O1; no merged later phases |
| PR #1, `demo/o2-recording-kit-2026-11-14` | `89d61934b145a9139085d782dd4ddfc38f17b474` | main, draft, older overlapping O2 |
| PR #2, `codex/o3-crud-integrity` | `6ced417b0d1ab84fe4f88dceff5fdc79d2d4dd22` | main, open |
| PR #3, `feat/o4-o5-allocation-expiry-2026-11-15` | `65829bcdcd5445d7ff88893032c6f82db6de357e` | #2 branch, open |
| PR #4, `feat/o6-o7-reminders-react-2026-11-15` | `83b02ba01b367b1b6df537aa1652d93922faed48` | #3 branch, open |
| PR #5, `feat/o8-o10-academic-glass-ui-2026-11-15` | `725ad0ad796b787bf63e67db728ff2882f6a7252` | #4 branch, open |
| PR #6, `feat/o11-o13-system-acceptance-release-2026-11-15` | **`fd34a8a66242f795dda758d45f220f2c331472db`** | #5 branch, open |
| PR #7, `docs/orgresource2-research-continuity-2026-11-15` | `67df0585b603f2d92a7ff1cbb3c2ab008442e387` before research | #5 branch, draft, docs-only |
| `docs/conversation-continuity-2026-11-14` | `b95a669209a6c027ba568ebf46084d5dc902ecc3` historical | unabridged master |

PR#6 last commit `fd34a8...` changes `evidence/O11-O13/PHASE_REPORT.md` from pre-release pending to locally passed claims. GitHub Actions **Verify** run #24, workflow run ID `43683241632`, reports SUCCESS on this head. Direct source reads: `AGENTS.md`, `backend/pom.xml`, `frontend/package.json`, `docs/RELEASE_AND_DEMO.md`, `docs/compliance/CLO9_IMAGE_DECISION.md`, `docs/design/ACADEMIC_GLASS_COMMONS_V1.md`, `docs/design/ORGRESOURCE_UI_CONTRACT_AMENDMENTS.md`, `docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md`, `evidence/O11-O13/PHASE_REPORT.md`, PR file-diff list, .github/workflow path listing. Exact 26-route catalog is **documented**; this audit has not independently executed/recounted every Java route. All pull requests unmerged. Neither successful CI nor PR body proves Canvas submission, O14, or full production readiness.

### Reported vs independently run
At the newest O11/O13 phase report, the coding agent reports 55 Maven/JDK8 tests, 15 frontend unit tests, 5 Playwright flows, 73 Newman requests / 105 assertions, five MySQL tables, React-bundled JAR smoke and generated zip/hash manifest. This research chat verified the report and one successful GitHub workflow status, **not** the local commands, screenshot pixels, ZIPs, or the live database. CI workflow may not include Playwright/Newman. Treat each execution as `REPORTED LOCAL PASS` unless CI job logs or independent rerun corroborate it. O14 **UNVERIFIED / NOT COMPLETE BY EVIDENCE**.

## Local source provenance
| Source | What was actually inspected here | What is not established |
|---|---|---|
| `09_ESP_PROJECT_ASSIGNMENT.pdf`, 3 pages | Primary extracted page text, exact mandatory technology, milestone and output sections | Canvas overrides unknown |
| `08_CSCI180_SYLLABUS.pdf`, 9 pages | Original parsed sections on CLOs, academic policies, calendar | Not all page visuals independently re-rendered this pass |
| `10_ORGRESOURCE_CONCISE_PROPOSAL.docx`, 12 pages | Extracted opening and listed entity/API/requirements sections | Not a Java test; long part selective |
| `01_KICKOFF_PROMPT.md` | Full short original prompt | Replaced by current user scope when conflicting |
| `02_MASTER_CONTEXT_DOSSIER.md` | Extracted index/primary context sections | Long dossier not exhaustively semantically inspected |
| `03_COURSE_REFERENCE_CORPUS.txt` | Source-labelled searchable extracted excerpts | 56k+ lines not each read; original relative-path exactness depends on ZIPs |
| `04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md` | Relevant historical version conflict/extracted examples | Historical QA not rerun |
| `06_VIDEO_TRANSCRIPTS.md` | Intro/source map and targeted timestamped transcript passages | Machine audio only; **no video screen/frame** analysis |
| `11_COMPLETE_SOURCE_NONVIDEO_PART1.zip` and `12_COMPLETE_SOURCE_NONVIDEO_PART2.zip` | **Both opened in the file runtime**; 1,227 outer entries and all 1,370 nested file entries streamed through SHA-256/CRC; all 21 nested ZIPs opened, no read errors; see [22](22_PRIMARY_ARCHIVE_VERIFICATION.md) | Byte/path integrity VERIFIED; not every original source semantically read; downloadable complete CSV manifests generated outside GitHub |
| Course slides/prof examples | Extracted Jersey/annotations/JPA/JPQL/Retrofit/validation examples and supplied source references | Every slide frame/code package not exhaustively opened; 2020 teaching versions not production guidance |
| Github `00`, `01`, standing policy | Both transfer texts and 9,461-char policy read | These are instructions/history, not current runtime |
| Historical unabridged master | **Full 319,799-character/4,174-line historical file retrieved and read consecutively in 18 non-overlapping line segments**; exact blob SHA and decisions reconciled in [24](24_HISTORICAL_MASTER_COMPLETE_INSPECTION.md) | HISTORICAL TEXT FULLY READ; not a claim that all original archive entries were semantically inspected |

No evidence of live Ateneo org enrollment, interviews, surveys, official partnership, real-stock testing, production auth, tenant isolation or external SMS delivery.

## Conflict register — resolution and test gate
| Conflict ID | Competing accounts | Current resolution | Decisive evidence / gate |
|---|---|---|---|
| X01 | Historical 9 entities/68 operations vs concise 5/25 | Academic base **five actual JPA types**, current documented 26 with A09; V2 may add types independently | C01 minimum 5, C03 selected proposal, G05 actual branch; inspect all Java `@Entity` and routes |
| X02 | Course paper says “RetroJit” vs syllabus/code Retrofit/Gson | Interpret as Retrofit/Gson while preserving literal wording; do not invent `RetroJit` library | C01 p1; C02 CLO8; C05 teaching source; instructor clarification if contested |
| X03 | Image CLO9 versus no image API in final assignment | **OPEN COURSE QUESTION**: cannot mark either required or waived; no sixth entity silently added | C02 CLO9, C01 pp1–3, G09; current Canvas |
| X04 | O1 and early handoff says no O11, actual PR#6 now exists | Latest connected PR#6 source outranks old snapshot | G05, observed head `fd34...` |
| X05 | O11 report pre-release pending versus latest report local package passed | Latest report fixes stale wording; requires independent artifact/check verification | G08, CI workflow and untracked deliverables |
| X06 | Course Javax/Boot2 vs production supported Jakarta/Boot3+ | Freeze course artifact; production migration/dual-track **decision pending verification**, never public unauthenticated Boot2 | C01/G06/E16, security gate |
| X07 | Reusable global holds versus future time-window bookings | V1 math not valid for V2 overlapping future time ranges; design a new invariant and migration | C03/G08, [11](11_WORKFLOWS_STATES_AND_INVARIANTS.md) |
| X08 | FakeTwilio result vs delivered SMS | Fake provider acceptance simulated only; no real messaging cost assumed | Course Lab4/G08/Twilio acceptance; no live message |
| X09 | Free hosting “forever” vs provider terms | Free at bounded usage conditional, lifetime not guaranteed; host selection pending | E06–E18, account eligibility/backup drill |
| X10 | Public school procedures vs asserted org demand | Secondary public evidence gives constraints, not problem validation | A01–A07; no pilot |
| X11 | Demo identity header vs real auth/tenancy | Academic prototype **must not be public multi-user prod** | G05–G09; auth gate |
| X12 | Source inventory totals 1,859 vs 2,611 | Different counting scopes include nested/deduped paths; do not equate | C04 manifests/ZIP-independent rehash required |

## CP10 source-bytes prior audit versus current inability to rerun (2026-11-15)
Original [22](22_PRIMARY_ARCHIVE_VERIFICATION.md) documents an independently executed earlier ZIP Python audit (all 1,227 outer /1,370 nested paths, SHA256+CRC, 21 nested ZIPs), separate machine CSV outputs not committed; preserve it as **prior recorded evidence**, not fabricate raw path hashes in current turn. This CP10 tool session attempted Python/container access to both Project ZIP uploads and received ClientError; **NO fresh raw ZIP CRC/hash**. Need exact raw/dossier path-set reconciliation: 1,241 total filesystem -15 video =1,226, but audit shows1,227 outer nonvideo (possible differently scoped path). No corruption conclusion from aggregates.
Full native text of 6 specific original source slide decks **directly re-opened**; V03 validation timestamped audio text; targeted course corpus and manifest. Course slide's Twilio Virtual Phone is **provider testing**, FakeTwilio local simulator distinct; Jersey/ORM/Retrofit/Javax semantics corroborated; source/visual slide screenshots and all JARs semantically NOT fully reviewed. [37](37_COURSE_SLIDES_TWILIO_VIRTUAL_AND_ENTITY_VALIDATION_AUDIT.md).
