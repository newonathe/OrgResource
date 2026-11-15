# Portable OrgResource first run

This guide starts a newly packaged OrgResource copy with a local MySQL schema. It never deletes or resets a database. Use a new empty schema named `orgresource`; do not point this demo at `orgresource_test`, a personal database, or a shared server.

## What you need

- Windows 10/11 and PowerShell 5.1 or newer.
- JDK 8 or newer on `PATH` to run the bundled Java 8-compatible JAR. Use JDK 8 for the course-compatible source build.
- Local MySQL Server 8.x with InnoDB and MySQL Workbench or another trusted SQL client. The application listens only on `127.0.0.1`.
- The separate `OrgResource.jar` release artifact. It is not inside either source ZIP.
- To rebuild from source: Node.js 20.19+ and the included Maven Wrapper.

## 1. Create a new local schema and least-privileged account

In MySQL Workbench, connect to your own local MySQL administrator account. Do not use a remote or school database.

Run this non-destructive schema command once:

```sql
CREATE DATABASE orgresource
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

If MySQL reports that the schema already exists, stop and confirm that it is an empty OrgResource demo schema before continuing. Do not drop, truncate, or reset it.

In Workbench’s **Server → Users and Privileges** screen, create a local account named `orgresource_app`, host `127.0.0.1`, and choose a new password. Type the password only into Workbench’s password fields. Under **Schema Privileges**, grant only `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `ALTER`, `INDEX`, and `REFERENCES` on the `orgresource` schema. Do not grant global privileges, `GRANT OPTION`, or administrator access. Do not save the password in the repository.

## 2. Start the packaged JAR

Copy `scripts/` from the complete source export beside the release JAR, or use the script in the extracted repository. Open PowerShell in the repository folder. Set Java 8 on `PATH` if multiple Java versions are installed, then check it:

```powershell
java -version
```

Start the application:

```powershell
.\scripts\start-portable-demo.ps1 -JarPath 'C:\path\to\OrgResource.jar' -PromptForPassword
```

The hidden prompt reads the database password into the current process environment and does not write a credential file. The launcher restricts the database URL to `127.0.0.1:3306/orgresource`, forces reminders to `DISABLED`, binds HTTP to `127.0.0.1:8080`, and refuses to start if that port is occupied. It does not issue database deletion/reset commands.

Open <http://127.0.0.1:8080/>. The React interface and JavaScript/CSS assets are served from the JAR. The JSON API is at `http://127.0.0.1:8080/api/`; for example, `GET /api/departments` returns a JSON array. On the first start, Hibernate creates/updates the five application tables in the new schema. Check in MySQL Workbench:

```sql
USE orgresource;
SHOW TABLES;
```

Expected tables are `allocation`, `department`, `project`, `resource`, and `resource_requirement`. Use fictional names and values in the UI to create demo data. No sample SQL dump is required.

## 3. Configure variables without saving a password

The launcher accepts `ORG_DB_URL`, `ORG_DB_USER`, and `ORG_DB_PASSWORD` from the current PowerShell process. If URL or user are unset, it uses the local `orgresource` defaults. If the password is unset, it prompts securely. To enter the username explicitly:

```powershell
$env:ORG_DB_URL = 'jdbc:mysql://127.0.0.1:3306/orgresource?useSSL=false&serverTimezone=UTC'
$env:ORG_DB_USER = 'orgresource_app'
.\scripts\start-portable-demo.ps1 -JarPath 'C:\path\to\OrgResource.jar' -PromptForPassword
```

The script restores the password and reminder-mode environment values when Java exits. Do not put a password on a command line, in a `.env` file committed to Git, or in a screenshot/log. To run directly without the script, set the same three variables only in the current process and run `java -jar C:\path\to\OrgResource.jar`; clear the password variable after the server stops.

## 4. Rebuild from a source export

The full-source archive includes React, Java, tests, docs, Postman collections, Maven Wrapper files, and lockfiles. From its extracted repository folder, build the React bundle and Java 8 JAR:

```powershell
Push-Location frontend
npm ci
npm run typecheck
npm test
npm run lint
npm run build
Pop-Location
Push-Location backend
.\mvnw.cmd -B -Pbundle-react-ui clean package
Pop-Location
```

The output is `backend\target\orgresource-0.1.0-SNAPSHOT.jar`. The test suite requires protected `ORG_TEST_DB_*` variables targeting the isolated `orgresource_test` schema; it is separate from this first-run `orgresource` schema. A Java-source ZIP can be imported into Eclipse through its included `.project`, `.classpath`, `.settings`, `pom.xml`, and Maven Wrapper, but a clean import in another Eclipse installation must be verified there before claiming it.

## Stop, restart, and common repairs

- **Stop:** press **Ctrl+C** in the PowerShell window that is running the launcher. This stops Java; it does not stop MySQL or delete records.
- **Restart:** run the same launcher command again. Existing database records remain.
- **Access denied:** verify that the local account host is `127.0.0.1`, the password is correct, and the account has only the required `orgresource` schema privileges. Re-run with `-PromptForPassword`.
- **Cannot connect to MySQL:** start the local MySQL service and confirm port `3306`; do not change the URL to a shared database.
- **Port 8080 is busy:** stop only the OrgResource process you started with Ctrl+C, then retry. Do not kill an unidentified process.
- **`orgresource_test` is refused:** this launcher is intentionally limited to the normal `orgresource` schema. The test schema is for protected integration testing only.
- **No SMS is shown:** reminders are intentionally disabled in this portable launcher. The local FakeTwilio test setup is a separate simulator workflow; no real SMS is required or claimed.
- **Java is missing or too old:** install/use JDK 8 or a newer JDK and check `java -version` in the same PowerShell window.
