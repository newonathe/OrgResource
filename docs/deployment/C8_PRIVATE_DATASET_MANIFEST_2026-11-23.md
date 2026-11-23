# C8 private approved 96-organization dataset — verified handoff (2026-11-23)

**Purpose:** safely resume the previously approved 96-organization *fictional* demo import using the original exact source. This document contains **checksums and aggregate counts only**. It does **not** contain or authorize publication of the private dataset, real contacts, a database dump or credentials.

## Verified artifact

- Local source filename received in the ChatGPT Project: `fictional-events-v1.local.json`.
- **Format:** one minified JSON document, terminated by a newline in the approved byte stream.
- **Byte length:** `19265850`.
- **Expected/verified SHA-256:** `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`.
- **Original directory roster source SHA-256:** `57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2`.
- **Dataset ID:** `orgresource-c8-plan-v1-57f1f0c68004-20deeda9e2af`.
- **Generator:** `orgresource-c8-plan-v1`.
- **Flags in uploaded JSON:** `synthetic=true`, `scope=LOCAL_ONLY_DRY_RUN`; no actual community usage, measurements or pilot are asserted.
- **Window in the approved plan:** 2025-12-01 through 2026-11-18, zone Asia/Manila.

The owner first used PowerShell `Get-FileHash` on the local Windows file and obtained the exact matching SHA-256. On November 23, the complete 19,265,850-byte document was uploaded to the ChatGPT Project. Its contents were parsed and independently hashed from UTF-8 text with its terminal newline restored; that checksum also matched. Aggregate counts below were independently recomputed by summing all 96 organization objects.

| Synthetic dataset dimension | Verified count |
| --- | ---: |
| Organizations | 96 |
| Departments | 248 |
| Projects | 713 |
| Resources | 1,312 |
| Requirements | 2,139 |
| Fictional personas | 462 |
| Allocations | 21,060 |
| Historical allocation events | 67,370 |

**Final allocation states** declared in the plan: PENDING 212; APPROVED 211; ISSUED 1,123; RETURNED 8,479; CONSUMED 5,699; CANCELLED 5,336. Sum: 21,060. **Organization tiers:** 56 STANDARD_COA_DIRECTORY, 24 STANDARD_LIONS_DIRECTORY, 14 FEATURED_SHOWCASE, 1 FLAGSHIP_MISA and 1 FLAGSHIP_SANGGUNIAN.

## Public GitHub boundary — IMPORTANT

The source repository `newonathe/OrgResource` is **PUBLIC**, so the raw 19.3 MB JSON MUST NOT be committed here or uploaded as an issue/PR attachment, a public release asset, GitHub Actions artifact or build image. The .gitignore on the draft branch excludes the known source filename as defense in depth. GitHub's browser upload can bypass local .gitignore; never use it on the public repository for this data.

**If GitHub custody of the raw file is required:** the owner should create a **separate private repository** with restricted collaborators, and upload the unchanged file there through GitHub's private repository web UI. Do not post the repository's private clone URL if it contains tokens; do not paste GitHub tokens or Northflank secrets into ChatGPT. The operator must still recheck size+SHA in the isolated Northflank job before using it, and should prefer private one-time file transfer rather than letting a production job fetch permanent source-control credentials.

The GitHub connector in this ChatGPT chat can update this existing repository but cannot create a new private repository or directly transfer the uploaded binary into Northflank. No assumption that the private repo has been created.

## Release gates and next operator action

1. **Freeze release candidate:** `9df7099145e324f326106870b1b3fd0788d5510f`, draft PR #25; [all-three-job green CI #45155946849](https://github.com/newonathe/OrgResource/actions/runs/45155946849) (Java8/MySQL, React and Extended C/MySQL/Newman/Playwright including sidebar).
2. **Stage original JSON privately:** use this exact file, verify size/hash, and keep it out of public GitHub. Read the [manual Northflank import runbook](../deployment/P0_NORTHFLANK_MANUAL_JOB_COMMAND_2026-11-23.md).
3. **Host safety gates:** owner-controlled Northflank security-incident credential review/rotation, verified off-host backup and independent MySQL 8 restore rehearsal, correct MySQL/Flyway state, private isolated manual job, tested application-image compatibility, maintenance mode and owner confirmation.
4. **Import gate:** capture a private **V2 original-row HMAC baseline before any writes**. The importer requires baseline VERIFY and exact approved private JSON, refuses wrong target and re-verifies original rows afterward. On partial failure, **STOP** and use reviewed backup recovery.
5. **Acceptance:** independent SELECT-only `--c8-hosted-demo-postflight` must match the exact imported records (not merely the counts), HMAC V2 original state preserved, original owner login works and live UI/access controls pass. No actual import, deployment or Northflank host access is evidenced by this document.

**Status:** EXACT APPROVED PRIVATE FILE AVAILABLE TO OWNER / CHATGPT PROJECT; SAFE METADATA IN GITHUB; **LIVE_IMPORT_NOT_RUN**.
