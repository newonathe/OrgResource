# OrgResource Java skeleton (O1)

This backend is the first, deliberately bounded implementation slice for the CSCI 180.05 OrgResource project. It uses the supplied course-era stack: Java 8, Spring Boot 2.1.0, Jersey/JAX-RS `javax`, Spring Data JPA, Javax Validation, and MySQL. The Eclipse metadata supports Java 8 and m2e; day-to-day editing can remain in VS Code.

## Implemented and verified in O1 and O3

- Exactly five JPA entity types: `Department`, `Project`, `Resource`, `ResourceRequirement`, and `Allocation`.
- Five Spring Data repositories, five Spring components, and five Jersey controller families.
- JSON create and list endpoints for all five entities, Department rename, Resource availability, and guarded Department delete.
- Generated positive IDs, explicit table/column names, InnoDB foreign keys, unique normalized Department/Resource names, and the unique Project/Resource Requirement pair.
- DTOs separate the public API from the persistence graph. Allocation responses never contain `phoneNumber`.
- Javax Validator checks JSON request DTOs. Project date-times are local Philippine wall times in `YYYY-MM-DDTHH:mm:ss` format.
- The local MySQL connection is environment-configured. No password is stored in source or this project folder.

Implemented routes:

| Method | Route | Current behavior |
| --- | --- | --- |
| POST / GET | `/api/departments` | Create and list |
| POST | `/api/departments/{id}/update` | Rename; normalized duplicate conflicts return JSON 409 |
| POST / GET | `/api/projects` | Create; list optionally filtered by `departmentId` |
| POST / GET | `/api/resources` | Create; list optionally filtered by `type` |
| GET | `/api/resources/{id}/availability` | Original two-field availability response |
| POST / GET | `/api/requirements` | Create; list optionally filtered by `projectId`; computed coverage query |
| POST / GET | `/api/allocations` | Create; redacted list optionally filtered by `requirementId` and `status` |
| POST | `/api/departments/{id}/delete` | Safe example: refuses deletion while Projects reference the Department |

Create responses use HTTP 201; list and delete successes use HTTP 200. Invalid request fields return a JSON 400; duplicate normalized names and known dependency conflicts return JSON 409.

The Department rename route is covered by unit tests and `../postman/OrgResource-O3-Department-Update.postman_collection.json`. The O1 routes are covered by `../postman/OrgResource-O1-Skeleton.postman_collection.json`.

## Not part of this skeleton

The remaining original Project, Resource, Requirement and Allocation update/delete/state-transition routes, complete allocation stock lifecycle, concurrency controls, scheduler work, Retrofit/Gson provider integration, React UI, image upload/serve behavior, production migrations, and hosted deployment are not implemented here. See `../evidence/O3/ROUTE_PARITY.md` for the complete route map. Do not treat this skeleton as the finished project.

The syllabus maps the final project to CLO9 (receiving and serving images over HTTP), while the concise proposal's 25-route API does not specify image endpoints. The instructor/Canvas requirement remains unresolved. The image feature is therefore not silently added to this O1 skeleton; resolve the scope before final-project acceptance.

`spring.jpa.hibernate.ddl-auto=update` is used only to generate this local skeleton schema. Replace it with reviewed migrations before broader development/release.

## Local build and run

Use a JDK 8 installation. Maven 3.9+ runs on Java 8. The project build targets Java 8 bytecode and the executable JAR uses the supplied database environment variables.

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk8'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:ORG_DB_URL = 'jdbc:mysql://127.0.0.1:3308/orgresource?useSSL=false&serverTimezone=UTC'
$env:ORG_DB_USER = 'orgresource_dev'
$env:ORG_DB_PASSWORD = '<set locally; never commit it>'
.\mvnw.cmd clean package
java -jar target\orgresource-0.1.0-SNAPSHOT.jar
# Run the Java unit tests
.\mvnw.cmd test
```

The HTTP server binds to `127.0.0.1:8080`. For this workstation, the database and password are provisioned separately under `%LOCALAPPDATA%\OrgResourceDev`; its DPAPI-protected credential file is not portable and is intentionally excluded from source archives. The O1 report records the actual schema, HTTP responses, build result, and restart check.

## Source constraints

The course assignment and syllabus outrank proposal and planning documents. The O1 implementation preserves the five proposed entity types and the original proposal's route shapes where implemented. The broader handoff is a reviewed design plan, not proof that those future features already exist.
