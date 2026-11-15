# OrgResource local release and demo

## Start the packaged application

After `scripts/package-release.ps1` succeeds, open PowerShell in the repository and run:

```powershell
.\scripts\start-release-demo.ps1
```

The script loads the workstation's protected credentials for the isolated `orgresource_test` schema and starts the executable JAR on `127.0.0.1`. It does not print the password, delete records, or reset the database. The Java service serves both the API and the bundled React interface.

Open <http://127.0.0.1:8080/> in a regular browser. The API is under `/api/`. The local test schema contains fictional development/test records accumulated by integration and browser tests; it is separate from the ordinary OrgResource development schema.

To stop the application, focus its PowerShell window and press **Ctrl+C**. To restart, run the same command again. MySQL remains available.

## Rebuild and verify the release

Stop the Java process first. Then run:

```powershell
.\scripts\package-release.ps1
```

The script runs `npm ci`, frontend typecheck/unit tests/lint/build, and Maven `clean verify` against the guarded local test database. Maven's `bundle-react-ui` profile copies the frontend build into the Java classpath before creating the executable JAR. It creates the JAR, a complete tracked-source ZIP, an Eclipse/Maven Java-source ZIP, and SHA-256 checksums under `deliverables/O11-O13/`.

The Java-source ZIP contains the backend Java source, tests, resources, Maven wrapper, `pom.xml`, and portable Eclipse metadata. The complete-source ZIP is created from the committed Git HEAD, so commit reviewed source changes before packaging.

## Verification boundaries

- Local Java packaging has been run with the installed JDK; the repository CI separately verifies the Java 8 build/runtime target.
- The local demo uses an isolated test schema and fictional records. Do not enter real contact information.
- The SMS integration defaults to disabled. A simulator result is not proof that a phone received a message.
- CLO9 image scope remains unresolved; see [the decision record](compliance/CLO9_IMAGE_DECISION.md).
- Do not represent local verification as hosted deployment, instructor acceptance, Canvas submission, or a completed course demonstration.
