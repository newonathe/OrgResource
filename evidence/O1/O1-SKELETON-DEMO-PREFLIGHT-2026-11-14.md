# O1 Skeleton Demo Preflight — 2026-11-14

## Scope and evidence boundary

- Workspace: `C:\Users\ethan\Downloads\Academic\4th Year\CSCI 180.05\OrgResource`, opened in VS Code.
- Application source commit at preflight start: `39482cca36d1f147cadec25c2ed9a93e23ec3739` (`main`). Baseline requested by the user: `acc3019`.
- This preparation adds local-demo scripts, read-only SQL, a focused Postman collection, and this report. It does not change Java application source, API behavior, schema design, or architecture.
- No video or fabricated runtime evidence was created. The CLO9 image requirement remains unresolved; this O1 demo does not claim to satisfy it.
- Local proof only: build/test, MySQL, HTTP, and Newman are verified. No hosted deployment, physical-device check, Canvas submission, or Postman desktop GUI import is claimed.

## READY checklist

| Check | Result | Evidence |
|---|---|---|
| VS Code and Git | PASS | Workspace is the repository folder above; branch was `main`, initially clean, at source commit `39482cc`. |
| JDK | PASS | `java -version` from the configured Temurin toolchain reported `1.8.0_512`. The launcher rejects a non-Java-8 JDK. |
| Maven Wrapper and backend build | PASS | `scripts/start-skeleton-demo.ps1` ran `backend\mvnw.cmd -o -B -ntp clean package`; it produced `backend\target\orgresource-0.1.0-SNAPSHOT.jar` and started the application. Surefire: 8 tests, 0 failures, 0 errors, 0 skipped. |
| Protected MySQL configuration | PASS | Existing DPAPI-backed helper loaded the local configuration without printing its password. Authenticated client reported database `orgresource`, account `orgresource_dev@127.0.0.1`, MySQL `8.0.46`. |
| Five O1 entity tables | PASS | `SHOW TABLES` returned `allocation`, `department`, `project`, `resource`, `resource_requirement`. All are InnoDB. |
| Read-only SQL walkthrough | PASS | `scripts/run-skeleton-demo-sql.ps1` executed `scripts/skeleton-demo.sql`. Exact counts after the demo insert: department 9, project 7, resource 7, resource_requirement 7, allocation 7. The SQL has no INSERT/UPDATE/DELETE/DDL/reset statement and omits borrower names and phone values. |
| API list endpoints | PASS | Before the demo POST: departments 200/8 rows; projects 200/7; resources 200/7; requirements 200/7; allocations 200/7. Responses were checked as JSON arrays; response bodies were not printed. |
| Committed O1 Postman collection | PASS | `postman/OrgResource-O1-Skeleton.postman_collection.json` declares the Postman Collection v2.1 schema, has a root `item` array (15 requests), and has no root `items` property. It is unchanged from `acc3019`. |
| Focused demo collection | PASS | `postman/OrgResource-O1-Skeleton-Demo.postman_collection.json` is a separate v2.1 collection with two requests and four assertions. Newman 6.2.3 ran POST 201 then GET 200; 2 requests, 4 assertions, 0 failures. The GET assertion matched the same generated ID and unique name. |
| Stop and preserve database | PASS | Ctrl+C ran Spring shutdown hooks: scheduler, JPA EntityManagerFactory, and Hikari pool closed. Afterwards port 8080 was not listening; MySQL on 3308 still accepted the read-only SQL walkthrough. No OrgResource JAR process remained. The two Java 21 processes observed belonged to VS Code's Red Hat Java extension. |
| Git/publish | PASS | The final preflight change set contains only the five listed demo-prep files; Git history records their commit and push to `origin/main`. No merge or Canvas submission is involved. |

## Commands used

```powershell
.\scripts\start-skeleton-demo.ps1
```

The script selected the configured Java 8 toolchain, loaded the existing protected environment, ran the offline Maven Wrapper build, and started the real backend at `http://127.0.0.1:8080/api`. It checks MySQL port 3308 and refuses to take over port 8080. It never hardcodes a password and never deletes or resets a database.

```powershell
.\scripts\run-skeleton-demo-sql.ps1
```

This command loaded the same protected configuration and ran the checked-in SQL through the local MySQL client. It restores process environment variables when it exits.

```powershell
npm exec --yes --package=newman -- newman run postman\OrgResource-O1-Skeleton-Demo.postman_collection.json --reporters cli
```

Newman output: `2 requests`, `4 assertions`, `0 failed`. Node printed a non-fatal `DEP0176` deprecation warning; the collection run passed.

## Database target and Workbench note

- Correct OrgResource database target: TCP/IP `127.0.0.1`, port `3308`, schema `orgresource`, account `orgresource_dev`.
- The saved Workbench profiles named `Local instance MySQL94` and `Local instance MySQL80` target ports 3306 and 3307 respectively; neither is this demo database. Run the included SQL runner for the walkthrough so Workbench does not need the protected password.
- No database reset, deletion, or cleanup was run. The single uniquely named demo Department intentionally remains persisted.

## Current run state

- Java application: stopped cleanly after checks; port 8080 is free.
- MySQL: left running and verified usable on port 3308.
- Exact current row counts: department 9, project 7, resource 7, resource_requirement 7, allocation 7.
