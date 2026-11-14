# 02 — IMMEDIATE SUBMISSION: imported Eclipse Java project + genuine video

COURSE SOURCE: 09_ESP_PROJECT_ASSIGNMENT.pdf states project skeleton due Nov 13, Canvas exported project and recording due Nov 14, 2026; check live Canvas for updates. User specifically wants recorded startup, created tables/test data, and repository/controller overview. Assignment additionally wants URLs returning JSON in Postman. This is immediate P0 work, distinct from final React UI due Dec 5.

## What must be real

1. Java backend starts with no fatal errors.
2. MySQL connection is real.
3. Five entity tables generated from JPA: department, project, resource, resource_requirement, allocation (names subject to actual @Table mapping).
4. Actual foreign keys and unique constraints inspected.
5. Test data genuinely loaded, if enabled; no invented rows.
6. Spring Data repositories initialize; custom query names/JPQL don't prevent application launch.
7. Five Jersey controllers and Spring @Autowired components are reachable.
8. GET/POST return actual appropriate JSON; skeleton stubs may exist but must be labeled.
9. Exported Java project contains source/build/dependencies and has an Eclipse-compatible import route.
10. Video depicts actual terminal, SQL client, source and Postman; no fabricated success.
11. Actual submission must be done by user or explicitly authorized tools. No claim of Canvas receipt without proof.

## Video recording storyboard (suggested 5–7 min, not instructor mandate)

00:00 Show VS Code folder tree, purpose of OrgResource and Java backend.
00:25 Start backend from terminal and show successful startup. Show port and data source initialized; no fatal exception.
01:10 SQL: SELECT VERSION(), SHOW TABLES, SHOW CREATE TABLE for five entities and FK structure.
02:25 SELECT sample rows if present. Explain fictional organization and data scope.
03:10 Show repository source including one custom derived query and JPQL method; show actual result of a route/test invoking it.
04:05 Show Jersey @Path / @GET / @POST controller, @Autowired component delegation, and repository call. Explain service separation.
05:00 Postman POST /api/departments with fictional record, then GET /api/departments, prove matching generated ID in DB. Optionally demonstrate safe delete.
06:05 Show ZIP content/source and import procedure, distinguish verified Maven build from actual Eclipse import. State unfinished final lifecycle/UI honestly.
Do not record secrets, actual phone numbers, notifications, unrelated private browser tabs or student IDs. Show source from same tested Git commit.

## SQL inspection script

~~~sql
SELECT VERSION() AS mysql_server_version;
USE orgresource;
SHOW TABLES;
SHOW CREATE TABLE department;
SHOW CREATE TABLE project;
SHOW CREATE TABLE resource;
SHOW CREATE TABLE resource_requirement;
SHOW CREATE TABLE allocation;
SELECT COUNT(*) FROM department;
SELECT COUNT(*) FROM project;
SELECT COUNT(*) FROM resource;
SELECT COUNT(*) FROM resource_requirement;
SELECT COUNT(*) FROM allocation;
SELECT * FROM department ORDER BY id;
SELECT * FROM project ORDER BY id;
SELECT * FROM resource ORDER BY id;
SELECT * FROM resource_requirement ORDER BY id;
SELECT id, requirement_id, quantity, status FROM allocation ORDER BY id;
~~~
These are candidate names; inspect actual schema and update script rather than presenting failing SQL as proof.

## HTTP proof

POST http://127.0.0.1:8080/api/departments
Content-Type application/json
Body:
~~~json
{"name":"Fictional Logistics Demo"}
~~~
Expected status 201 only when implemented, and actual generated ID. Then GET /api/departments should include matching database row. If controller returns a placeholder, label it as skeleton stub; do not claim persistence.

## Eclipse-compatible source export from VS Code

The Java source archive MUST contain all Java .java source, pom.xml (or tested portable course JAR setup), necessary project config, dependencies resolution and README. If course demands literal Eclipse .project/.classpath/.settings, generate correct portable metadata and test import if feasible; otherwise state NOT_RUN. Maven+m2e import is one route but not proof of literal instructor interpretation. Exclude real credentials, local absolute paths, compiled caches, node_modules and unrelated source archive ZIPs. Build from clean extraction. Record ZIP SHA-256 and tested commit.

## Mandatory skeleton acceptance evidence

S01 source exists; S02 Java compile; S03 startup; S04 DB connection; S05 five tables; S06 fictional test data if any; S07 repository query initializes and runs; S08 controller paths accessible; S09 injected Component called; S10 JSON returned from real HTTP; S11 runtime create/delete; S12 restart persists; S13 exported source ZIP produced; S14 actual Eclipse import PASS or explicitly NOT_RUN; S15 actual video produced or NOT_CREATED; S16 no exposed secrets; S17 correct current Canvas instruction manually checked.

## Adaptable narration

"This is OrgResource, a proposed academic resource-planning application for student organizations. The project is developed in VS Code and its Java source is prepared for Eclipse-compatible import. [Show successful startup.] The application connects to MySQL and defines Department, Project, Resource, ResourceRequirement and Allocation tables. [Show real tables and fictional data.] Spring Data JPA repositories provide persistence, while Jersey controllers forward HTTP requests to injected Spring components. [Show real code and Postman response.] This is the skeleton milestone. Its unfinished business logic and React interface will be implemented and verified in subsequent phases. [Show export artifact.] Thank you."

If evidence differs from narration, revise narration. Neither Codex nor Claude may fabricate a video from screenshots of expected output. If recording capability missing, provide exact shot list and mark VIDEO NOT_CREATED.
