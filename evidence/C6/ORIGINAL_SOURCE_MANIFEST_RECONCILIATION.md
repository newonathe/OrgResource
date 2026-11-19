# C6 — original non-video source-manifest reconciliation, exact inventory level

**Date:** 2026-11-19 PHT. **Evidence depth:** original supplied manifest fully enumerated by rendered lines, **METADATA/HASH AS REPORTED IN SOURCE**, not independent raw-byte verification, archive decompression or semantic inspection. **Scope:** preserves original filesystem relative paths and source ZIP inner paths for course and prior work, but DOES NOT imply every original file is open, tested, clean for public redistribution or identical to current Git blobs.

## Canonical material read and how

- Uploaded **`04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md`**, original Project file ID `file_0000000045dc82118d5b2bbc5538ed70`, **56,374 rendered lines** and current Git blob `0b1a164a0dfe62f2a2ba8f7ff2aad414caabd0fd`, Git tree size **4,081,076 bytes**. This exceeds ordinary GitHub connector content size, which returned no text, but **is not an empty file**. The exact underlying material was reopened using the Project Files read ranges, not substituted with a filename or older summary.
- The **original source manifest CSV starts near line 53,755** with the explicit heading “Complete source manifest (CSV).” Three contiguous Project Files reads covering rendered **lines 53,758–56,374** were parsed to the CSV's 8 columns: `record_type,path,parent_archive,size_bytes,modified,sha256,duplicate_of,status`. Every parsed entry matched the expected 64-character SHA-256 metadata format; no malformed entry was detected by this shape check. This is a parser check of **the recorded hashes**, not a recomputation from original bytes.
- The raw non-video source archives `11_COMPLETE_SOURCE_NONVIDEO_PART1.zip` and `12_COMPLETE_SOURCE_NONVIDEO_PART2.zip` remain available in the Project but were **not re-extracted bytewise** in the functioning environment. The **original course raw video recordings are intentionally not attached**; do not infer video pixels from the `06_VIDEO_TRANSCRIPTS.md` machine-generated audio transcription.

## All 2,611 manifest records accounted for at metadata level

| Coverage group | Original path records | What was verified | What remains unverified |
| --- | ---: | --- | --- |
| `filesystem_file` | **1,241** | Every distinct manifest record parsed with its original relative path, SHA-256 metadata, size, modified and possible duplicate target | Raw filesystem byte/hash recomputation and semantic review |
| `archive_member` | **1,370** | Every archive-member path parsed, including `archive.zip::relative/member/path`; parent archives listed | Independently decompressed ZIP central directories, collision or integrity/safety of archive contents |
| **All records** | **2,611** | Exactly **2,611** unique manifest `path` strings, no row parse failures | Some assets may be renamed, binary, duplicates or irrelevant; not 2,611 distinct sources requiring deep semantic reading |
| Records with `duplicate_of` filled | **1,759** | Existing duplicate-pointer metadata exposed | Duplicate SHA equivalence not independently recomputed; paths were not deduplicated in exported source |
| Distinct recorded ZIP parent archive labels | **21** | Source paths preserve archival origin at metadata level | Exact actual ZIP bytes and nested ZIP traversal on original raw archives |

This is evidence that **all original paths have been inventoried from the source manifest**; it is not proof every underlying file was opened. The reported `sha256` values were generated before this C6 read, not during this pass.

## Source type triage from the manifest extension only (does not replace semantic review)

| Extension | Original manifest entries | C6 review rule |
| --- | ---: | --- |
| `.jar` | 1,442 | Preserve hashes for course compatibility, do not treat packaged dependencies as original Java source |
| `.java` | 230 | Prioritize course lab examples that illustrate Jersey/Spring/JPA/validation/Retrofit and relevant transitive domain logic; review actual source, not filenames |
| `.png` | 229 | Binary figure/image assets may help class examples; direct pixel inspection required only for relied-on visual claims |
| `.class` | 172 | Compiled binary evidence; compare bytecode/classpath when relevant, not source-text review |
| `.json` | 58 | Only inspect material course/API/demo/credential/contract instances and review public permissions |
| `.prefs` | 53 | Eclipse/classpath compatibility metadata |
| `.properties` | 44 | Potential config/credential and environment-sensitive source; publication/secret scan required |
| `.classpath` | 40 | Eclipse packaging compatibility |
| `.project` | 40 | Eclipse packaging compatibility |
| `.md` | 38 | Prior research/proposals/manifests; design claims remain historical unless reconciled |
| `.pdf` | 34 | Original course source/publication/embedded image and personally identifying metadata risk |
| `.txt` | 32 | Course corpus/records and reference text |
| `.zip` | 24 | Nested ZIP source and public-license/metadata risk |
| `.docx` | 12 | Historical proposals and potentially private academic information |
| `.ppt` | 11 | Direct class lecture materials; preserve original course methods and inspect actual slides before compatibility changes |

Other file extensions also occur. These counts are the highest relevant extensions, **not an exhaustive list of all types**. `duplicate_of` may include earlier files within the same corpus. Relative original paths are available in the **unmodified original source manifest**, which is intentionally not duplicated into a proposed publicly distributed audit report.

## Source-origin/capability contrasts

- This is separate from the **699 tracked Git blobs** inventoried for PR #23 base and current C6 branch, whose Git SHA-1/size metadata and semantic coverage remain in `evidence/C6/SOURCE_TREE_METADATA.csv`. Git source blobs are not identical to the archive's SHA-256 original path records.
- The **course reference reading layer** is `03_COURSE_REFERENCE_CORPUS.txt` (original Project ref `file_000000003e24821199dba7186ff23290`), 58,209 rendered lines and Git blob `de33619ad34f3b7d2db61f7c8de70bb421d6f4c`, 2,002,052 Git bytes. Original source content remains accessible via Project Files despite GitHub fetch size limits. It is not semantically fully read here.
- The **recordings** `06_VIDEO_TRANSCRIPTS.md` contain only auto-generated timestamped audio and explicitly say no video frames can be inspected here. The complete source pack excludes raw videos by design.
- `02_MASTER_CONTEXT_DOSSIER.md` contains student-identifying information (do not replicate), which makes the user-proposed publishing of the full research repository a **P0 disclosure risk**; see `PUBLICATION_SAFETY_GATE.md`.

## Mandatory completion checks, still outstanding

1. On a functioning local development workstation, **read ZIP1 and ZIP2 central directories**, canonicalize each member path, detect traversal/symlinks/duplicate collisions, recompute SHA-256 of a stratified sample or all relevant extracted originals, and compare all 2,611 manifest records/duplicate targets. This can be deterministic script work, not 2,611 LLM calls.
2. Reconcile exact instructor class examples and lab Java APIs, dependency versions/packaging and course learning-outcome evidence against the frozen Course A source on its immutable SHA, while keeping existing originals untouched.
3. Review all **active critical** Git/Java/React/SQL/CI service files and required transitive callers at semantic level, not just manifest metadata. The full independent critic and runtime tests remain pending.
4. Make a conscious owner/legal choice about publication of private docs/course materials/binary archives and Git reachable history **before** changing repository visibility or creating a sanitized public CI mirror. Do not publicize a copy of the manifest that embeds private student path names without review.

**C6 source-inventory conclusion:** Original **path accounting can be marked METADATA COMPLETE FOR THE AVAILABLE SOURCE MANIFEST (2,611)**. Raw-source archive identity, semantic review, license/privacy and actual test evidence **remain OPEN**; full C6 acceptance cannot be certified.
