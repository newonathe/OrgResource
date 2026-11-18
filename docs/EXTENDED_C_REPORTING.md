# Extended C6a reporting

The Activity and Exports pages are local Extended C features. They use the existing tenant-scoped `audit_event` and `stock_movement` facts. The 26 business and two image operations for the course remain separate.

## Access and paging

All three routes require a signed-in OWNER or ADMIN and an explicit selection of the path organization. A nonmember or suspended member receives 404. A member with a different role receives 403. An eligible member without a selection receives 409 `ORG_CONTEXT_REQUIRED`. A different or inaccessible path workspace receives 404. Exports also require same-origin POST and the current CSRF token. The routes perform reads and attachments, with no job record or stock mutation.

| Method and path under `/api/v2/orgs/{organizationId}` | Contract |
|---|---|
| GET `/audit` | Recorded organization audit actions. Account-global events are excluded. Most existing writers record successful actions. |
| GET `/activity` | Audit actions plus permanent stock movements. The source field distinguishes these facts. Entries make no cross-source claim about actor identity or causality. |
| POST `/exports` | Exactly `{"format":"CSV","dataset":"RESOURCES"}` with an allowed format and dataset. See the file contract below. |

The GET responses use `{data:{items,nextCursor},requestId}`. Default limit is 50 and valid limits are 1–100. Follow the returned cursor for older items. The order is UTC event time descending, then movement before audit at an exact timestamp tie, then the immutable row ID descending inside each source. The event IDs and cursors are opaque encodings of source and row ID. A cursor is always checked against the selected organization and feed. It carries no authorization.

Each page reads from one MySQL repeatable-read transaction. Concurrent events ordered before the cursor appear when the caller returns to the newest page. Each next page is a new read with a strict lower ordering boundary. Unknown cursor, wrong feed or malformed cursor receives 400 `INVALID_CURSOR`. Negative and excessive limits receive 400 `INVALID_PAGE_SIZE`. Failure of either source fails the whole activity page. A successful response never substitutes one source for the two-source feed.

The response has `id`, `source`, `action`, `targetType`, `outcome`, `requestId`, `resourceId`, `quantity`, `deltaTotal`, `deltaIssued` and ISO UTC `occurredAt`. Inapplicable fields are null. Movement resources use public IDs. Audit target identifiers and actor IDs are omitted. The feed does not claim to record every failed or denied request.

## Exports

Supported formats are `CSV` and `TENANT_JSON`. Datasets are `RESOURCES`, `MOVEMENTS` and `AUDIT`. Requests may contain up to 1 KiB of JSON with two distinct keys and exact enum values. Invalid syntax, duplicate or unknown keys, malformed values and unsupported datasets receive 400 `INVALID_EXPORT_REQUEST`.

The export uses one transaction snapshot and returns at most 1,000 rows and 4 MiB. It checks for the 1,001st row and refuses excess with 413 `EXPORT_TOO_LARGE`. There is no truncation or partial file. This version has no downloadable page cursor. Each request downloads one complete bounded dataset. Empty output keeps its headers or an empty JSON rows list. There are no on-disk server export files.

`Content-Disposition` supplies `orgresource-{dataset}-{UTC timestamp}.{csv|json}`. Responses carry `Cache-Control: no-store`, `X-Content-Type-Options: nosniff`, `X-Export-Schema-Version: 1`, and `X-Exported-At`. Media type is UTF-8 `text/csv` or `application/json`. JSON includes schema version, organization public ID, dataset, exported time and rows. Exported Resources contain public ID, name, type and physical total. Movement and audit exports use the fields documented above.

CSV uses a header, CRLF records, quoted text and doubled interior quotes. Schema version and exported time are included per data row, and in response headers for empty files. A text cell with leading control or format characters, or a leading formula trigger after whitespace, gets a literal apostrophe prefix before its original value. Typed numeric deltas remain numeric. The JSON representation preserves original text. [RFC 4180 §2](https://www.rfc-editor.org/rfc/rfc4180#section-2)

Exports exclude actor user identifiers, borrower names, contacts, credentials, invitations, recovery tokens, sessions and provider settings. Requirement and Allocation exports remain outside this minimum until the owner approves their field-level privacy rules.

## Verification

Run `scripts/verify-extended-c1.ps1` for Java 21/MySQL, both Newman collections and packaged Chromium. The new Postman collection is `postman/OrgResource-Extended-C6a-Reporting.postman_collection.json`. Use the existing Extended local environment with a fictional owner or admin. See the [C6a phase evidence](../evidence/C6a/PHASE_REPORT.md) for command receipts and limits.
