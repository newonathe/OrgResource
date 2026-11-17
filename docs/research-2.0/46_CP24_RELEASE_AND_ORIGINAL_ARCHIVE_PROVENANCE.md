# CP24 — O14 release reproducibility and original source path provenance

**Dated 2026-11-16. Source/repository inspection, NOT new ZIP byte rehash or independent extraction.**

## O14 release design (source verified)
Current pinned `scripts/package-release.ps1` requires a clean worktree, archives Git HEAD, builds Java 8 JAR and source packages and produces:
1. `deliverables/O14/OrgResource.jar`
2. `deliverables/O14/OrgResource-complete-source.zip`
3. `deliverables/O14/OrgResource-java-source.zip`
4. `deliverables/O14/SHA256SUMS.txt`
5. `deliverables/O14/RELEASE-MANIFEST.json`

`.gitignore` explicitly ignores `/deliverables/O14/`. All five are **absent from the pinned tracked Git tree**; local generation/extraction passes are asserted by O14 report, not independently repeated in CP24. O14 GitHub Actions uploaded a distinct `orgresource-executable-java8` artifact with `OrgResource.jar` (~37.2MB): [workflow](https://github.com/newonathe/OrgResource/actions/runs/43691405530), [artifact](https://github.com/newonathe/OrgResource/actions/runs/43691405530/artifacts/14152318812). Artifact ZIP digest is not automatically the JAR digest.

The legacy `tools/package_deliverables.py` belongs to the older proposal workflow, NOT the supported current academic release entrypoint.

## Source archive inventory and discrepancy
Historical `04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md` embeds a CSV manifest from around line 53,755, explicitly stating the same CSV is stored inside Part 1 non-video source ZIP. Earlier research `22_PRIMARY_ARCHIVE_VERIFICATION.md` documented streamed file byte/SHA/CRC auditing. Raw ZIPs were inaccessible to current container in CP24/CP27; **do not claim a new rehash**.

| Inventory layer | Recorded value |
|---|---:|
| Original filesystem entries including raw video | 1,241 |
| Raw video path entries | 15 |
| Subtraction expected original nonvideo | 1,226 |
| Part 1 outer nonvideo entries | 615 |
| Part 2 outer nonvideo entries | 612 |
| Combined outer nonvideo | 1,227 |
| Nested ZIP file entries | 1,370 |
| Nested ZIP directory markers | 184 |
| Outer + nested file instances | 2,597 |

**MC67 OPEN:** the +1 original/outer mismatch may be generated source manifest CSV, but exact extra member *name+hash* has not been verified. Count arithmetic is not a path-set reconciliation; do not edit counts to force equality.

## Source-path reconciliation contract
SRC-01 compare historical `filesystem_file` path set to actual outer archive entries; SRC-02 compare nested members with their parent archives; SRC-03 list extra/new/absent paths by exact raw name, normalized name and hash; SRC-04 sizes and SHA256; SRC-05 preserve duplicate path instances with same bytes; SRC-06 case, Unicode, ZIP slash normalization recorded separately; SRC-07 raw videos remain unobserved; SRC-08 distinguish byte verified, text read, visual inspected and unreviewed per file.

## Git archive content scope
The O14 tree has 488 tracked files totaling roughly 86.5 MB before compression, including 217 historic `qa/` files, course reference corpus, source proposals, project planning notes and older ZIP deliverables. An unfiltered `git archive HEAD` contains all of that. This is not by itself a discovered credential leak, but submission package material should be reviewed for relevance/private information and kept separate from the internal complete-history archive. **MC68** preserve full private provenance and review smaller instructor-facing export; **MC69** avoid conflating legacy and current packaging scripts.

## Reproducibility requirements REL-01–REL-12, NOT independently executed
REL01 record exact intended 40-character source SHA/clean tree; REL02 build frontend+Java8 with actual versions; REL03 generate five files; REL04 recompute three raw archive/JAR SHA256 and match manifest; REL05 source ZIP corresponds to Git HEAD; REL06 verify entries/secret exposure; REL07 clean extract both; REL08 build/verify extracted sources on isolated MySQL; REL09 fresh DB+least privilege+startup; REL10 real create/list/delete and restart persistence; REL11 run 96/142 executable Newman assertions with DB postconditions; REL12 retain the exact artifacts and receipt in independent accessible custody.

## Stop/decision
No release PASS from a lone CI JAR or author-reported manifest path. No automatic public-upload of complete course corpus, raw archives, credentials or internal context. Preserve existing history privately, prepare and approve explicit academic distribution subset, then prove all artifact checks. Original raw recordings not provided/visually analyzed.

See `docs/PORTABLE_FIRST_RUN.md`, `docs/RELEASE_AND_DEMO.md`, O14 `evidence/O14/FINAL_ACCEPTANCE_REPORT.md`, historical `04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md`, and `docs/research-2.0/22_PRIMARY_ARCHIVE_VERIFICATION.md`.