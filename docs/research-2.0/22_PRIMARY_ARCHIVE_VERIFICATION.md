# Independent non-video source ZIP integrity and provenance audit
**Status: VERIFIED SOURCE BYTES/HASHES; SEMANTIC INSPECTION TARGETED ONLY.** **Audit date: 2026-11-15, Asia/Manila.** Project original uploaded files: `11_COMPLETE_SOURCE_NONVIDEO_PART1.zip`, `12_COMPLETE_SOURCE_NONVIDEO_PART2.zip`; Project source guide C04. This is a **new independent archive audit**, not an assertion inherited from earlier manifests.

## Actual procedure
Opened both exact supplied non-video ZIP files in a local Python file runtime using the standard-library `zipfile.ZipFile`. Enumerated each outer entry and streamed every decompressed byte through SHA-256. Python's ZIP member decompression automatically validates the stored CRC-32; every file was read to EOF. Opened **all 21 nested ZIPs**, enumerated their entries (including empty directories), and independently streamed/hash-verified their **1,370 files**. Did **not execute** original archived Java/Python code or install dependencies. Did not open any unattached video. No nested ZIP read/open exceptions occurred.

| Measured property | PART1 | PART2 | Combined |
|---|---:|---:|---:|
| Outer archive raw bytes | 331,483,304 | 330,620,664 | 662,103,968 |
| Original archive file entries (no directory entries) | 615 | 612 | **1,227** |
| Outer entries containing nested ZIP | 11 | 10 | **21** |
| Original outer uncompressed bytes | 331,322,500 | 330,460,456 | 661,782,956 |
| Original outer SHA-256-distinct content | — | — | **654** |
| Duplicate outer-path file instances by hash | — | — | **573** |
| Nested ZIP entries including dirs | — | — | **1,554** |
| Nested file entries | — | — | **1,370** |
| Nested directory entries | — | — | **184** |
| Distinct nested-file SHA-256 hashes | — | — | **492** |
| CRC and streamed SHA checks | 0 reading errors | 0 reading errors | All file entries checked |

**Counting caution:** 1,227 original nonvideo *outer* file instances plus 1,370 nested *file* members = **2,597 file-instance paths**, not 2,597 unique conceptual sources. Nested 184 directory markers are also inventoried. Earlier reports cite 1,241 filesystem entries including video files and 2,611 combined with nested entries—those have a different scope, and should not be called contradictions of this non-video-only audit without side-by-side file manifests. Original full source pack includes 15 video paths not available here as raw recordings. Hashing JARs proves bytes can be read, not semantic review or licensing clearance.

## Full exact path/hash artifacts
The audit generated two machine-readable CSV manifests from the original uploaded ZIP bytes:
1. `OrgResource_original_source_hash_manifest_2026-11-15.csv`: outer archive name, **exact original relative path**, uncompressed bytes, CRC32, SHA-256, nested-ZIP marker, explicit inspection status for all 1,227 entries.
2. `OrgResource_nested_zip_hash_manifest_2026-11-15.csv`: outer archive name, enclosing nested-ZIP relative path, exact member relative path, uncompressed bytes, CRC32, SHA-256, inspection status for all 1,370 nested files.

These CSV files were generated as **downloadable conversation artifacts**, not committed to this private repo as part of this research documentation push. Do not invent a GitHub URL for them. The original non-video ZIP bytes and exact paths remain the canonical source; two CSVs are independently reproducible if those ZIPs remain accessible. A permanent repo-level path/hash manifest would require explicitly transferring these CSVs into the GitHub branch, not merely referencing their filenames here.

## Original Java source inspected directly, not inferred from prior summaries
| Canonical provenance: outer→inner | Source-level observation | Risk/correct course interpretation |
|---|---|---|
| `source/files/FakeTwilio.zip` → `FakeTwilio/src/app/rest/controllers/FakeTwilioController.java` | Jersey `@POST @Path("/fake_twilio/send")`, form fields `To,MessagingServiceSid,Body`, header `Authorization`, delegates `@Autowired` to component; logs auth value with `System.out.println(auth)` | Never use real secrets, only dummy Authorization; local simulator acceptance is **not** carrier delivery |
| Same → `FakeTwilio/src/app/components/FakeTwilioComponent.java` | Returns reply with `status="accepted"`, prints destination and body | Do not use real person or phone data in tests |
| `source/files/RetrofitTwilioDemo.zip` → `TwilioTestSample/src/TwilioRequests.java` | Real `retrofit2` interface with `@FormUrlEncoded @POST @Field @Header @Url` | Confirms printed assignment "RetroJit" interpreted as Retrofit/Gson via syllabus and code, not invented RetroJit API |
| Same → `TwilioTestSample/src/TwilioTester.java` | Test makes Retrofit `Call<ResponseBody>`, executes HTTP; example commented out Gson converter and expects 201 provider response | Current FakeTwilio returned acceptance may be different status; separate simulator/provider proof |
| `source/files/SchedulingApp (1).zip` → `SchedulingApp/src/app/SpringBootApp.java` | `@EnableScheduling`, but sample explicitly excludes DataSource, transaction manager and Hibernate JPA auto-config | Do **not** copy database exclusions to OrgResource's DB-backed scheduler |
| Same → `SchedulingApp/src/app/components/ScheduleTestComponent.java` | `@Scheduled(fixedRate=1000)` with console print | Syntax demonstration, not proof OrgResource has real durable expiry/reminders |
| `source/files/ImageServerDemo (1).zip` → `ImageServer/src/app/rest/controllers/ImageController.java` | `@POST /tester/upload` multipart `@FormDataParam("file")`; `@GET /tester/image?id=...` returns `byte[]`, `@Produces("image/jpeg")` | Evidence CLO9 course pattern, **not** proof image transfer required in final OrgResource |
| Same → `ImageServer/src/app/entity/UploadedFile.java` | Separate `@Entity` with id and filename | Copying unchanged adds sixth entity to five-entity V1; first resolve the course question |

Relevant original source files were **read verbatim in the working audit**; 1,227+1,370 other paths were NOT semantically individually reviewed. No code was executed or compiled in these steps.

## Follow-up coverage control
Before claiming full semantic source audit, classify each canonical SHA into course-critical source, previously extracted text, compiled binary, duplicate, reference-only or unreadable; inspect course-critical Java/config/slide/diagram originals, not merely the 56k-line `03_COURSE_REFERENCE_CORPUS.txt`; record page/entry and independent tests. Never claim video visual analysis from `06_VIDEO_TRANSCRIPTS.md`. The complete outer/nested path CSVs satisfy **byte-level source path accounting**, not all source-content acceptance.
