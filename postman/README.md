# Postman collections

## Current API

Import [OrgResource-Course-A-28-Routes.postman_collection.json](OrgResource-Course-A-28-Routes.postman_collection.json) for the current API reference. It uses standard Postman Collection v2.1 and includes all 28 requests: D01–D04, P01–P04, R01–R05, Q01–Q04, A01–A09, IMAGE-01 upload and IMAGE-02 retrieval. A09 is the consent-revocation amendment; the other original 25 operations plus the two Course A image operations complete the current set.

This reference collection is not an ordered end-to-end runner. It includes mutually exclusive lifecycle operations and destructive operations that require separate disposable fictional records. Its examples use local Postman variables and contain no credentials. IMAGE-01/02 expect a Resource that already exists and `imageFile` set to the included synthetic PNG fixture. The separate [Course A image HTTP execution collection](OrgResource-Course-A-Image-HTTP.postman_collection.json) creates and safely deletes its own zero-stock fixture. The previous 26-route O14 reference remains preserved as [OrgResource-Current-26-Routes.postman_collection.json](OrgResource-Current-26-Routes.postman_collection.json) for historical route traceability.

## Run current API acceptance locally

Start OrgResource on `127.0.0.1:8080` with the isolated fictional-data test schema, then run this from the repository root:

```powershell
.\scripts\run-api-acceptance.ps1
```

The runner uses Newman 6.2.3 through `npm exec` without adding it to the project's dependency files. It runs O1, O3 CRUD, Department update, O4/O5/A09, O14 dependency-integrity, and the Course A image HTTP collection. It uses unique fictional fixtures and does not reset the database. The image collection requires the tracked file `postman/fixtures/academic-demo-image.png`, supplied by the runner as an absolute local path; no course video or external upload is needed. The O4/O5 flow intentionally uses a one-second delay between requests so its Projects, created with a future start time, become active before lifecycle operations. Do not select the generic `OrgResource-Local` environment for the O3 CRUD collection; its legacy IDs shadow the collection's generated fixture IDs.

## Historical material

`OrgResource-API.postman_collection.json` describes the earlier nine-entity, 68-operation proposal. It is marked historical in its metadata and must not be treated as current scope. Older O1/O3/O4-O5 execution collections are preserved as phase evidence; their names indicate the routes or phase they actually exercised.
