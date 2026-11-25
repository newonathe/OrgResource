# OrgResource Course Final A release and demonstration

The A3 release is assembled from one clean, committed source SHA. The release output is generated outside the repository. Do not overwrite the earlier A1+A2 release folder.

## Build the release from source

Requirements are JDK 8, MySQL 8/InnoDB and Node.js 20.19 or newer. The integration tests require the protected local orgresource_test database configuration. The release script refuses to package with a running OrgResource process, a dirty Git tree, missing test credentials, or a missing as-built DOCX. It does not reset or drop MySQL data.

After committing the final source and recording its SHA, generate the DOCX from that exact commit with the tracked builder. Use the bundled workspace Python and its python-docx package when it is available; render every DOCX page to images and inspect the output before packaging.

    $docx = Join-Path $env:TEMP 'OrgResource-Course-A-As-Built-Proposal.docx'
    $sourceSha = (git rev-parse HEAD).Trim()
    & 'C:\path\to\python.exe' scripts/build-course-final-a-docx.py --source-commit $sourceSha --output $docx
    .\scripts\package-release.ps1 -ArtifactsDirectory 'C:\path\to\OrgResource-Course-Final-A3-Release' -AsBuiltProposalDocx $docx

The release script runs frontend npm ci, typecheck, unit tests, lint and production build, then Maven clean verify with React bundled. It makes the executable JAR, complete tracked-source ZIP, Java/Eclipse source ZIP, and copies the as-built DOCX, demo guide, read-only SQL, Postman reference and local environment into the release folder. SHA256SUMS and RELEASE-MANIFEST.json cover each payload file; the manifest does not hash itself.

The output contains:

- OrgResource.jar
- OrgResource-complete-source.zip
- OrgResource-java-source.zip
- OrgResource-Course-A-As-Built-Proposal.docx
- OrgResource-Course-A-Demo-Guide.md
- PORTABLE_FIRST_RUN.md
- OrgResource-Course-A-Demo.sql
- OrgResource-Course-A-28-Routes.postman_collection.json
- OrgResource-Course-A-Local.postman_environment.json
- scripts/start-portable-demo.ps1
- SHA256SUMS.txt and RELEASE-MANIFEST.json

The complete-source ZIP comes from Git archive at HEAD. The Java/Eclipse ZIP includes backend source/tests, Maven Wrapper, POM, README, and Eclipse metadata. No release ZIP contains the developer workstation credential helper or a database password.

## Verify artifacts and clean extraction

Compare every payload with SHA256SUMS.txt and RELEASE-MANIFEST.json. Then extract both source archives into unique temporary directories. Inspect archive entries and confirm the Maven Wrapper, frontend lockfile/source, Java source/tests, Eclipse metadata, docs, Postman files and SQL guide are present. Run the relevant frontend and Java 8 build from the complete-source extraction using the protected isolated test schema. A separate computer or clean-account setup is NOT_RUN unless actually performed.

The Java/Eclipse ZIP is an export for import into Eclipse; no independent Eclipse installation/import is claimed unless completed. The portable first-run guide describes a new empty local orgresource schema and least-privileged local account. Do not drop, truncate or reset an existing database to simulate a clean machine.

## Run the packaged app for a student demonstration

For a visually clean demonstration, use a newly created empty local orgresource schema and the documented portable launcher. The protected orgresource_test integration schema intentionally accumulates fictional acceptance fixtures and can make overview counts and lists crowded; it is not a clean presentation database. Do not delete those test fixtures or reset that schema.

In the release folder, set up the empty local schema using PORTABLE_FIRST_RUN.md and run:

.\scripts\start-portable-demo.ps1 -PromptForPassword

The launcher finds the adjacent JAR. If MySQL is not listening on default port 3306, set ORG_DB_URL to the observed local port before starting; the launcher allows only 127.0.0.1 and the exact orgresource schema. Open http://127.0.0.1:8080/. Add only fictional records. Stop Java with Ctrl+C in the PowerShell window; MySQL remains running and rows persist. Restart with the same command.

For the protected developer test database only, the local script can be run with:

    .\scripts\start-release-demo.ps1 -JarPath 'C:\path\to\OrgResource.jar'

This script uses the private local helper and starts the isolated orgresource_test profile; it is not a portable first-run path. Never display environment variables, password prompts, or credentials in a recording.

## Postman

Import the current 28-operation reference and the OrgResource Course A Local environment from the release folder. Select the environment, keep baseUrl at http://127.0.0.1:8080, and use a fictional Resource for IMAGE-01/02 with the bundled synthetic PNG file. The reference is importable but is not a dependency-ordered run: it includes mutually exclusive and destructive operations. Use scripts/run-api-acceptance.ps1 for ordered Newman acceptance against the isolated fictional test database.

## Demonstration and evidence limits

The student storyboard and expected results are in docs/COURSE_A_FINAL_DEMO_GUIDE.md. The SQL file is read-only and uses the schema already selected by the MySQL client; for the student demonstration, select the newly created local `orgresource` schema. It contains no DDL or mutation statements and omits borrower names and phone numbers. The route acceptance matrix separately records the earlier read-only query against the protected `orgresource_test` schema.

Newman, MySQL integration tests, CLI Playwright, packaged-JAR smoke checks, and SHA checks establish only the local behavior actually exercised. FakeTwilio is a simulator. A loopback Twilio protocol test does not prove provider acceptance or SMS delivery. The local image endpoint works, but whether CLO9 must be implemented in this application and whether the instructor accepts the evidence remain NOT_VERIFIED. No video, group discussion, instructor approval, peer review, authorship declaration, Canvas submission, or receipt is created by these scripts.

Manual student actions include checking live Canvas requirements/deadline, confirming the actual group roster, recording the real demonstration if required, verifying video permissions, completing authorship and peer-review requirements truthfully, uploading the actual deliverables, and checking the submission receipt.
