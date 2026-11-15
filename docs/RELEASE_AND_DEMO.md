# OrgResource release and demonstration

This document describes repeatable technical packaging. The portable database setup is in [`PORTABLE_FIRST_RUN.md`](PORTABLE_FIRST_RUN.md); the student-operated storyboard is in [`DEMO_SCRIPT_O14.md`](DEMO_SCRIPT_O14.md).

## Build the release

Use the existing release script from the repository root after the source changes are committed. It runs frontend install/typecheck/unit tests/lint/build, then Maven `clean verify` with the bundled UI and the guarded local `orgresource_test` database. It creates the executable JAR, complete tracked-source ZIP, Java/Eclipse source ZIP, and SHA-256 manifest.

```powershell
.\scripts\package-release.ps1 -ArtifactsDirectory deliverables\O14
```

Before running, stop the local Java preview with Ctrl+C and ensure the protected `ORG_TEST_DB_*` setup is available. The test database is fictional and separate from the portable `orgresource` database. The script refuses a non-test schema and does not reset the database. Do not run `clean verify` or these tests against a personal/shared database.

The output names are:

- `deliverables/O14/OrgResource.jar`
- `deliverables/O14/OrgResource-complete-source.zip`
- `deliverables/O14/OrgResource-java-source.zip`
- `deliverables/O14/SHA256SUMS.txt`
- `deliverables/O14/RELEASE-MANIFEST.json` (source branch/commit, tool versions, artifact sizes and hashes)

Build artifacts are ignored by Git and remain outside the source ZIP. The full-source ZIP comes from the exact committed Git `HEAD`; the Java ZIP includes Java source/tests/resources, `pom.xml`, Maven Wrapper, and the provided Eclipse metadata. Neither source archive contains the separate executable JAR or the workstation credential script.

## Verify clean extraction and hashes

After packaging, independently read the hash and release manifests and compare each file:

```powershell
Get-Content deliverables\O14\SHA256SUMS.txt
Get-Content deliverables\O14\RELEASE-MANIFEST.json
Get-FileHash deliverables\O14\OrgResource.jar -Algorithm SHA256
Get-FileHash deliverables\O14\OrgResource-complete-source.zip -Algorithm SHA256
Get-FileHash deliverables\O14\OrgResource-java-source.zip -Algorithm SHA256
```

Extract both source ZIPs into new, uniquely named directories under `$env:TEMP`; list their entries and verify the expected wrappers, Java sources, frontend sources, lockfiles, docs, and `.project` / `.classpath` / `.settings` metadata. From the complete-source extraction, run the frontend build and Java 8 Maven build with a local isolated test schema. Start the separate JAR from that extraction with process-local `ORG_DB_*` variables and verify `/`, bundled assets, a HashRouter path, and all five list APIs. Do not claim a separate-machine setup or live Eclipse import unless one was actually performed. If external access/account provisioning prevents a clean empty-schema first run, report it as **NOT_RUN** and retain the documented steps rather than using an existing schema destructively.

Record artifact sizes, hashes, source commit, JDK/Maven/Node versions, commands, outcomes, and limitations in [`../evidence/O14/FINAL_ACCEPTANCE_REPORT.md`](../evidence/O14/FINAL_ACCEPTANCE_REPORT.md). If any tracked file changes after packaging, rebuild the archives from the new committed `HEAD` before calling them the final source snapshot.

## Start the packaged app

For this workstation’s protected fictional test database:

```powershell
.\scripts\start-release-demo.ps1
```

This local script loads the existing DPAPI-protected credential helper and starts only the `orgresource_test` test profile. It does not reveal the password, reset MySQL, or represent portable setup. For a clean computer, use [`PORTABLE_FIRST_RUN.md`](PORTABLE_FIRST_RUN.md) and `scripts/start-portable-demo.ps1` with a new `orgresource` schema/account.

Open <http://127.0.0.1:8080/>. The bundled React app is served at `/`, the API under `/api/`, and Vite development mode is unnecessary for the packaged demonstration. Stop Java with Ctrl+C in the PowerShell window that started it. MySQL stays running; restarting Java preserves database rows.

## Acceptance boundaries

- Newman requests execute against a local service and fictional database rows; they are not a remote deployment test.
- Playwright acceptance runs headless from the command line. The in-app browser is not used on this Windows installation.
- FakeTwilio is a local HTTP simulator. `SIMULATED` or `PROVIDER_ACCEPTED` is not delivery evidence.
- No real SMS, personal phone number, student-authored recording, instructor approval, peer review, authorship certificate, or Canvas submission is created by packaging.
- CLO9 image behavior remains **UNRESOLVED**. Do not state full academic compliance while this and the student-only actions are open.
- No official GitHub Release or version tag is created as part of O14.
