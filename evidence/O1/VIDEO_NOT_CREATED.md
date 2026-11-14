# VIDEO_NOT_CREATED

No screen recording was created for this O1/O2 handoff. Do not represent this shot list as a recorded video or submitted evidence.

## Suggested recording sequence

1. Show the project ZIP filename and SHA-256, then extract it into a clean folder.
2. Show Eclipse importing the folder as an existing Maven project and the JavaSE-1.8/m2e project configuration. This step has not been verified because Eclipse was not available on this workstation.
3. Show a terminal using JDK 8 and run `mvnw.cmd -B -ntp clean package`; wait for `BUILD SUCCESS`.
4. Show the local MySQL schema inspection with the five InnoDB tables and four foreign keys. Do not reveal credentials.
5. Start the generated JAR and show successful Spring Boot, database, Jersey and HTTP startup messages.
6. Run the saved Postman collection against `127.0.0.1:8080`; show the 15 requests and 29 assertions passing.
7. Stop and restart the JAR, repeat representative list requests, and show that records persist. Include the redacted allocation JSON and safe-delete conflict response.
8. End by stating the boundaries: local O1 skeleton only, unit tests skipped, no real Eclipse import, image/CLO9 scope unresolved, no hosted deployment and no course submission.

