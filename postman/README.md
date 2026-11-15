# Postman collections

## Current API

Import [OrgResource-Current-26-Routes.postman_collection.json](OrgResource-Current-26-Routes.postman_collection.json) for the current API reference. It uses the standard Postman Collection v2.1 format and contains 26 requests: D01–D04, P01–P04, R01–R05, Q01–Q04, and A01–A09. A09 is the proposed consent-revocation addition; the other 25 are the original route set.

This reference collection is not an ordered end-to-end runner. It includes mutually exclusive lifecycle operations and destructive operations that require separate disposable fictional records. Its examples use local Postman variables and contain no credentials. The O1, O3, and O4/O5 execution collections and results remain separate in this folder and under `evidence/`.

## Run current API acceptance locally

Start OrgResource on `127.0.0.1:8080` with the isolated fictional-data test schema, then run this from the repository root:

```powershell
.\scripts\run-api-acceptance.ps1
```

The runner uses Newman 6.2.3 through `npm exec` without adding it to the project's dependency files. It runs the O1, O3 CRUD, Department update, and O4/O5/A09 collections. It uses unique fictional fixtures and does not reset the database. The O4/O5 flow intentionally uses a one-second delay between requests so its Projects, created with a future start time, become active before lifecycle operations. Do not select the generic `OrgResource-Local` environment for the O3 CRUD collection; its legacy IDs shadow the collection's generated fixture IDs.

## Historical material

`OrgResource-API.postman_collection.json` describes the earlier nine-entity, 68-operation proposal. It is marked historical in its metadata and must not be treated as current scope. Older O1/O3/O4-O5 execution collections are preserved as phase evidence; their names indicate the routes or phase they actually exercised.
