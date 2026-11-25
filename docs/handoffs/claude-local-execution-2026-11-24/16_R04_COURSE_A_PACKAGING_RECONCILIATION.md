# R04 — Course A packaging and compliance reconciliation, partial
Date 2026-11-25 PHT. Scope: parallel independent R04 source/CI slice while R06B3a CI runs; no frozen A change, no new release, no instructor/Canvas action.

## Authority and actual observations
Assignment pp1–3: course technologies, >=5 entities, GET/POST, runtime CRUD, Autowired component forwarding, Postman, final Dec 5 and video. Syllabus pp2, 4, 6: course learning outcomes including image HTTP, assessment, authorship/outside-assistance. Original supplied assignment and syllabus excerpts inspected; no Canvas check. Frozen A 7f8c0746da18fd54b47f430ac1435ac36e605d67. Active candidate PR52 d3e8a8550e00e3bb9423d84f51f8141783e71fd1. Compared actual recursive tree path/blob identity: backend 75/75, frontend 30/30, current Course-A Postman files 3/3, fixture 1/1, compliance 3/3; no changes in these compared sets. No complete repository semantic review implied.

Actual [CI run 45437263264](https://github.com/newonathe/OrgResource/actions/runs/45437263264), Java8 job 138598356335 completed success, raw logs inspected for test totals/collection summaries and packaged browser test: Maven 61/0 failures/0 errors/0 skipped; six Newman collections 15+20+11+27+23+4=100 requests, 29+30+19+27+37+9=151 assertions, zero failures; Playwright packaged Java application 1 passed. This is current bounded runtime evidence, not the entire Windows package or independent review. Frontend job 138598356828 completed success (job metadata only). Extended job 138598356841 in progress at observed checkpoint; R06B3a remains pending actual receipt.

## Per-source review
| Path/ref | Blob | Review | Verdict |
| --- | --- | --- | --- |
| scripts/package-release.ps1 at frozen A | 317d19ab31d243e85e737887e297cd1b6d8fa8be | full | FIX in separate packaging layer, never edit frozen A |
| docs/RELEASE_AND_DEMO.md at frozen A | 905bd54dfbb528992248cf27b82180d5d177fb98 | full | KEEP historical; source export caution |
| evidence/COURSE-A/A3_TECHNICAL_GATE.md at frozen A | 1bf229605077ac64caabdfbccdff6790927e11f8 | relevant ranges (historical gate and limitations) | KEEP, historical reported evidence |
| evidence/COURSE-A/ROUTE_ACCEPTANCE_MATRIX.md at frozen A | f87604f7302af71e62a15fa028cc3cf7d1e85cec | 28 route rows | KEEP mapping, not new independent per-case test |
| docs/academic-final/COURSE_FINAL_A_AS_BUILT.md at frozen A | 9b40d9bcdecd06db9ce77a2d008d9693876e4f12 | sections 1–3.3 partial | KEEP |
| postman/OrgResource-Course-A-28-Routes.postman_collection.json | 2619f53ca5df2d245ce0c24f8a5735cead253449 | parsed all 28 names/methods/URLs and image body | KEEP |
| postman/fixtures/academic-demo-image.png | 97dcd7f9bac25be1962d53636bc4d81340c34cec | tree metadata, 137 bytes; CI image test receipt | KEEP; do not claim independent PNG inspection |
| .github/workflows/verify.yml at PR52 | 07211e17037233db806488b3542162ac53846737 | relevant Java8 build/package/CI steps | KEEP; no Windows release package gate |
| evidence/C6/COURSE_A_28_FULL_DELEGATION_SOURCE_MATRIX.csv at 727c270f0a5f2d34bb3b049124d2d5cfca218f35 | b9b108c8ab53ffbef40d012b4139c16cc4ecda57 | header and initial route entries inspected, prior 28-source map locator | REUSE, source-only |
| evidence/C6/COURSE_COMPLIANCE_AND_SOURCE_CONFLICTS.md at 727c270f0a5f2d34bb3b049124d2d5cfca218f35 | 8fbf03ef170c4b25561c73c5358d3ba8108a3e8f | course crosswalk and scope | KEEP, historical |
| evidence/C6/SAFE_CI_AND_PUBLIC_MIRROR_DECISION.md at 727c270f0a5f2d34bb3b049124d2d5cfca218f35 | 6180cc38655c1571876a5f197a7846bf2e77de0e | full | KEEP prior deny-by-default policy |
| evidence/C6/PUBLICATION_SAFETY_GATE.md at 727c270f0a5f2d34bb3b049124d2d5cfca218f35 | exact ref | full | KEEP; publication risks remain |

## Requirement / evidence
28 documented operations counted: Department 4, Project 4, Resource 7, Requirement 4, Allocation 9; 21 POST/7 GET. 25 original + A09 + IMAGE-01/02. The assignment requires basic create/read and runtime deletion but does not mandate 28. Existing route matrix lists positive/refusal tests. The current CI actual run corroborates aggregate execution, including ResourceImageHttpMySqlTest and ReminderLifecycleMySqlTest, without falsely crediting every case independently. Historic five-entity and delegation source mapping retained. Live Twilio/instructor proof remains separate; no real SMS. Separate Course A deliverable from Extended C.

## Concrete packaging findings
P1 R04-PKG01: historical package-release.ps1 copies 10 payload files plus hashes/manifest, including reference collection and environment; does not separately copy PNG referenced by IMAGE-01's form file variable imageFile. It exists in tracked source archive and the guide refers to bundled synthetic PNG. Standalone release user may need to extract the fixture from source first. Proposed separate delivery assembly: include approved fictional fixture at documented relative path, set explicit file variable in instructions, and hash it in manifest. Test extraction with actual Newman image upload/download (byte equality) from staging; no frozen modification.
P0 R04-PKG02 publication boundary: the historical package command uses git archive HEAD, and actual frozen tree contains nine blobs matching course pack uploads or old specification ZIP; no tracked .gitattributes found in this tree. Do not reuse whole-HEAD archive as new distributable public package. Historical C6 allow/deny policy requires owner rights review, authored code/tests/fictional fixtures/lockfiles/minimal safe docs, exclude course corpus, private history/evidence, original proposals, generated files and secrets. A student-private submission and public export have distinct permissions. No archive contents distributed here; historical package existence not proof of present clearance.
P1 R04-PKG03: CI uploads only compiled Java8 JAR; package-release.ps1 Windows run, DOCX provenance/render, complete/source Java ZIP inspection, clean extraction/rebuild and hash verification are separate. Prior A3 external receipt is referenced but not retrieved this cycle; do not claim fresh package verification.

## Next R04 work (CHAT before R09 where feasible)
Use frozen exact Git blobs, generate explicit source allowlist and deny paths by default; determine actual release-required file set via POM/wrapper, frontend lockfile, import metadata, docs/SQL/portable launcher and Postman fixture dependencies; hash compare, archive path safety, clean extract/build, negative omitted/extra path tests; keep original archives and A untouched. Distinguish sanitized candidate from actual student-approved distribution. Check current artifact receipt if available. Independent review, actual Windows packaging and student obligations remain open. No new archive or executable produced in this cycle.

## Falsification / limits
A green build may coexist with a missing standalone fixture or overbroad archive. Exact checked paths exclude mutable historical docs; no claim all files identical. A clean archive must not contain denied paths, must contain all required files, and must reproduce build from clean extraction. No private-source inspection or full history secret clearance. This is SOURCE_ONLY findings plus VERIFIED_BOUNDED existing CI results; R04 IN_PROGRESS; R09 false.
