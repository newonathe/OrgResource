# 01 — Setup from zero: Windows VS Code, Java, Maven, MySQL, Node, GitHub, Codex, Claude Code

These are instructions to run on your local machine; they were NOT executed here. Use the exact OS shell. For macOS/Linux translate PowerShell and wrapper commands. Inspect existing files before creating or replacing anything.

## A. Inspect workspace and machine first

Open VS Code terminal (PowerShell on Windows). Choose where OrgResource will be developed, e.g. C:\Development\OrgResource, but use actual user-preferred path. Do not overwrite any existing repository.

~~~powershell
Get-Location
Get-ChildItem -Force
git status --short --branch
git remote -v
git --version
java -version
javac -version
mvn -version
node --version
npm --version
mysql --version
code --version
gh --version
where.exe java
where.exe javac
where.exe mvn
where.exe node
~~~

Missing commands are environment findings, not proof work was done. MySQL client version is not server version. Do not print tokens.

## B. Install and configure as needed

1. VS Code: https://code.visualstudio.com/ ; Java Extension Pack, Maven for Java, Java test/debug support, GitHub PRs (optional), ESLint (for React) and official Codex/Claude integrations if authorized.
2. Java: inspected course-era libs are Java 8/Jersey 2/Boot 2.1 based. Install a JDK 8 toolchain for backend compilation and runtime after checking actual lab examples. VS Code Java language server may run on newer JDK; configure project JDK separately via Java runtime settings. Do not confuse IDE JDK with backend JDK.
3. Maven: https://maven.apache.org/install.html. A Maven wrapper is recommended after actual project generation. It does not exist until created; do not claim '.\mvnw.cmd' already works.
4. MySQL server and SQL client: https://dev.mysql.com/downloads/ ; verify server with SELECT VERSION(); check InnoDB, foreign keys, unique indexes and datasource privileges.
5. Git: https://git-scm.com/downloads . GitHub CLI optional https://cli.github.com/. Verify account newonathe before any repo creation, remote, push or pull request. Prefer private visibility until user approves publication.
6. Node/npm: https://nodejs.org . Choose actual Vite-supported Node LTS; official Vite docs list Node 20.19+ or 22.12+ as base requirement, with some templates requiring more. Modern Node is separate from Java 8 backend.
7. Codex/Claude: follow current authenticated vendor instructions, give access only to chosen repo/source files, keep secrets outside prompts. Never let agents write same worktree at once.

## C. Workspace creation

If repository already exists remotely, use authenticated git clone/gh repo clone on its verified URL. If folder already has work, inspect before copying files. If NO repo and user authorizes creation:
~~~powershell
New-Item -ItemType Directory -Force -Path .\OrgResource
Set-Location .\OrgResource
git init -b main
git status --short --branch
code .
~~~
The above is an example, not a command to run without first checking path. Do NOT silently create a public repo.

Unzip handoff to 'docs/handoff/', copy its complete master to 'docs/ORGRESOURCE_MASTER_HANDOFF.md', use AGENTS.md and CLAUDE.md only after checking existing instructions. Do not automatically commit private original course ZIPs/slides to a public repo.

## D. Database first

Use dedicated fictional-data schema. After connecting to actual MySQL server:
~~~sql
SELECT VERSION() AS mysql_server_version;
SHOW VARIABLES LIKE 'default_storage_engine';
CREATE DATABASE IF NOT EXISTS orgresource
 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
~~~
Create dedicated local app user with least permissions using your DB admin, credentials only in local environment. The exact SQL user creation depends on version/security preferences. No real password in Git or chat.

Suggested app environment variables:
- ORG_DB_URL
- ORG_DB_USER
- ORG_DB_PASSWORD
- ORG_SMS_MODE (DISABLED by default)
- ORG_FAKE_TWILIO_BASE_URL
- ORG_FAKE_TWILIO_SERVICE_ID
- ORG_FAKE_TWILIO_DUMMY_AUTH
- ORG_REMINDER_SCAN_MS, ORG_EXPIRY_SCAN_MS

Example terminal env values (placeholders must be replaced LOCALLY; never publish):
~~~powershell
$env:ORG_DB_URL="jdbc:mysql://127.0.0.1:3306/orgresource?serverTimezone=Asia/Manila"
$env:ORG_DB_USER="<LOCAL_DB_USER>"
$env:ORG_DB_PASSWORD="<LOCAL_SECRET>"
$env:ORG_SMS_MODE="DISABLED"
~~~

Application properties template:
~~~properties
server.address=127.0.0.1
server.port=8080
spring.datasource.url=${ORG_DB_URL}
spring.datasource.username=${ORG_DB_USER}
spring.datasource.password=${ORG_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
orgresource.sms.mode=DISABLED
orgresource.reminder.scan-ms=3600000
orgresource.expiry.scan-ms=3600000
~~~
The 'update' schema mode is academic convenience only; do not use it as migration system. Do not switch to destructive drop/create in ordinary demo config. Prove LocalDateTime round trips and actual JDBC driver.

## E. Backend build

Recommended backend/ with pom.xml, Maven Wrapper, src/main/java/app and src/main/resources. Original course library inventory includes Boot 2.1.0.RELEASE, Jersey 2.26, Hibernate 5.3.7, Spring Data JPA 2.1.2, MySQL Connector/J 8.0.13; select a compatible dependency graph only after inspecting original code. Java 8 source/target, javax.* imports, Jersey not Spring MVC.

Once project generated:
~~~powershell
Set-Location backend
.\mvnw.cmd -version
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
~~~
Use 'mvn' if the wrapper was not generated and local Maven is verified. On macOS/Linux use './mvnw'. Runtime target API is http://127.0.0.1:8080/api/departments. Wrong '/api/api' mapping is a common error: ResourceConfig @ApplicationPath("/api"), controllers @Path("/departments").

## F. React frontend after skeleton

Frontend should be Vite + React + TypeScript with pinned package-lock.json. Scaffold when O7 begins; do not claim it exists beforehand. Use 'npm ci' only after lockfile creation.
~~~powershell
Set-Location frontend
npm ci
npm run dev
npm run build
npm run test
~~~
Vite dev target http://127.0.0.1:5173, proxy '/api' -> Java server 127.0.0.1:8080. Never use frontend localStorage as inventory authority. Final static assets bundled into backend only after verifying packaged / and /api routes. Use React HashRouter for simple Java servlet coexistence.

## G. Multi-agent workflow

Codex primary backend, Claude independent reviewer/React track, but one writer per worktree. Use separate git branches/worktrees for simultaneous edits. Review Git diff and tests before merge. Agents may make useful local edits, run commands and prepare export/video if tools support them. They must not invent video, Canvas receipt, test result, repository or submission. Ask user permission for push/merge/submission/destructive actions. Do not claim continuous GitHub monitoring without actually creating a monitoring workflow.

## H. Final setup checkboxes

[ ] Git, author, branch, remote and visibility verified.
[ ] Original assignment/syllabus/proposal accessible to agents.
[ ] Correct course JDK, Maven and dependencies identified.
[ ] Actual MySQL server, InnoDB and dedicated schema work.
[ ] Jersey/Spring/Repository startup proven with real POST/GET.
[ ] Five principal JPA tables and FKs observed.
[ ] npm and React setup reserved for after skeleton gate.
[ ] No credentials in project or logs.
[ ] Eclipse-compatible source archive strategy explicitly verified.
