# A3 Provenance and Artifact Check for C0

Date checked: 2026-11-17 (Asia/Manila)

## Protected source

- A3 branch: `feat/course-final-a3-freeze-2026-11-17`
- A3 source commit declared by the receipt and manifest: `7f8c0746da18fd54b47f430ac1435ac36e605d67`
- Fresh remote audit: `origin/feat/course-final-a3-freeze-2026-11-17` also resolves to that commit. PR #12 remains open and targets the A1+A2 branch.
- C0 was branched from this exact commit. No C0 implementation was made on the A3 branch.

The accessible release folder was `OrgResource-Course-Final-A3-Release`. Each artifact's byte count and SHA-256 was recalculated and matched both `RELEASE-MANIFEST.json` and `SHA256SUMS.txt`:

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| `OrgResource.jar` | 41,656,814 | `5f80c5a21ffb89251b8e762e63da241dd6c9dc789996bef85b39ab03e0a8eff5` |
| `OrgResource-complete-source.zip` | 66,440,216 | `4956c2e6b1f2679751ccf6e6be7360d0600186ded957352601ba8107f0ccfdf3` |
| `OrgResource-java-source.zip` | 89,788 | `601fbf6c3bd326fbf619dbd8fd22bdfb99e1180fded406c9a03c8880fb4d9a8e` |
| `OrgResource-Course-A-As-Built-Proposal.docx` | 52,480 | `58dc90b989c1ca62b8e74df1fb5f21801dabffc86fbe51c624659007b60147b8` |
| `OrgResource-Course-A-Demo-Guide.md` | 8,092 | `6260cd16b770d5d6ab655eaf3ce961f7b9cbed466ba00cdaa57d2e4c42a9dcfd` |
| `PORTABLE_FIRST_RUN.md` | 6,603 | `a3875f7e5219898f9b051b80355ab5682501ab57a70e9e602d4e62b98ba8d463` |
| `OrgResource-Course-A-Demo.sql` | 2,570 | `5c7985ba00e5c4e0692f5524404670ef49e12ca2d39377cd36c4b50ff70e35f5` |
| `OrgResource-Course-A-28-Routes.postman_collection.json` | 30,945 | `aa23c92f44fba6c1b0c447b39a13992833818201172fd963e5c66674fe859fa0` |
| `OrgResource-Course-A-Local.postman_environment.json` | 531 | `43dc6bc111d47dc8f93dbfaec00408d6a85976bbc1c3cda1f74f37847a9ff2e7` |
| `scripts/start-portable-demo.ps1` | 4,498 | `86cf502b1383d651dc6cbcf81578a23e8a68d4c5fd905199e31afde21bd0d06d` |

## Container and document checks

- `OrgResource-complete-source.zip`: all 571 entries passed ZIP CRC verification. Checked that it contains no `.git`, `node_modules`, `target`, `dist`, protected workstation credential script, or local database files.
- `OrgResource-java-source.zip`: all 74 entries passed ZIP CRC verification; Eclipse `.project` and `.classpath` metadata are present.
- `OrgResource-Course-A-As-Built-Proposal.docx`: all ZIP entries passed CRC verification; `word/document.xml` parses; the document package contains source SHA `7f8c0746da18fd54b47f430ac1435ac36e605d67` in document properties and body.

These checks establish integrity and inspectability of the accessible A3 files. They do not re-run A3's historical builds, prove acceptance on a separate machine, or establish instructor/Canvas acceptance. A3's release receipt remains the source for its previously recorded build and package test claims.
