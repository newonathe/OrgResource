# C8 Project Work and Reporting — source-based route oracle (11 operations)

**Date:** 2026-11-23, Asia/Manila. **State:** SOURCE-REVIEWED by assistant; independent reviewer NOT_RUN; exact per-operation receipts UNMAPPED. This is preparatory evidence, not C8 acceptance.

## Sources and common requirements

Checked directly in source at feature revision e72e32b2b7ff: ProjectWorkResource.java, ProjectWorkComponent.java, InventoryService.java (requireProjectWriterRole, requireProjectWriter, version parsing), ReportingResource.java, ReportingComponent.java, and C1OrganizationContext.java.

All eleven handlers use a path organization, a current authenticated account, and active path membership. A nonexistent or inaccessible path organization is 404. Write requests require current CSRF, non-frozen organization, allowed role, and valid object. In Project Work, the selected workspace is not the authority: the path organization is. Reporting explicitly requires the selected organization to match the path organization; mismatch is 404. Previously passing anonymous 401 coverage is one negative class only.

## Project Work — 8 routes

| Operation (relative to /api/v2/orgs/{organizationId}) | Source rule / review oracle | Required acceptance cases |
| --- | --- | --- |
| GET /projects | OWNER, ADMIN, CUSTODIAN, or LEAD read; LEAD sees only assigned Projects | Positive page; unassigned LEAD hidden; MEMBER 403; foreign membership 404; cursor org/role scoped; no DB write |
| GET /requirements | Same read roles; LEAD only assigned Project Requirements | Same; optional project filter must be same org, unassigned LEAD 404, cursor scoped |
| GET /requirements/{requirementId} | Same read roles; Requirement must belong to readable org/Project | ETag/version, missing or cross-org Requirement 404, unassigned LEAD 404 |
| POST /projects/{projectId}/update | OWNER, ADMIN, assigned LEAD; current If-Match; strict Project fields | Success; CUSTODIAN/MEMBER 403, foreign/unassigned LEAD 404, stale 412/missing 428; unchanged Project row/version after refusal |
| POST /projects/{projectId}/delete | OWNER, ADMIN, assigned LEAD; current If-Match; domain constraints | Success; tenant, role, stale version, linked records and unchanged DB postcondition on denied delete |
| POST /requirements | OWNER, ADMIN, assigned LEAD; existing same-org Project and Resource; active Project | Positive create, invalid quantity, ended Project 409, cross-org resource and unassigned LEAD 404; denied count unchanged |
| POST /requirements/{requirementId}/update | OWNER, ADMIN, assigned LEAD; locked same-org Requirement, Project, Resource; matching If-Match | Positive quantity update; stale/foreign/unassigned refusal; quantity, version, coverage unchanged on denial |
| POST /requirements/{requirementId}/delete | OWNER, ADMIN, assigned LEAD; same-org requirement and valid If-Match | Positive permitted deletion; reject allocated/linked invalid state; denied Requirement/Allocation/stock rows unchanged |

**Delegates reviewed:** ProjectWorkComponent.readScope permits OWNER, ADMIN, CUSTODIAN, LEAD. Its leadScope binds LEAD page results to membership ID. writeScope first locks the path organization, then InventoryService.requireProjectWriterRole permits OWNER, ADMIN, LEAD. A LEAD must also match the particular Project's leadMembershipId or receives 404 from requireProjectWriter. Versioned writes use strong quoted If-Match and return 428 missing, 412 stale. REST DTOs reject unknown Project or Requirement fields; actual error serialization still requires HTTP proof. The REST delete operations are POST, not DELETE.

## Reporting — 3 routes

| Operation (relative to /api/v2/orgs/{organizationId}) | Source rule / review oracle | Required acceptance cases |
| --- | --- | --- |
| GET /audit | OWNER or ADMIN, and selected organization equals path organization | Positive bounded audit-only feed; mismatch 404, foreign 404, CUSTODIAN/LEAD/MEMBER 403; no cross-org events |
| GET /activity | Same, audit plus stock movement events | Positive combined timeline, cursor integrity, page size 1–100, response redaction and 404/403 paths |
| POST /exports | Same; CSRF required despite read-only execution | Valid RESOURCES/MOVEMENTS/AUDIT, CSV/TENANT_JSON, attachment MIME/filename/no-store; invalid payload, cross-org/no-member/role denials, no DB mutation |

**Delegates reviewed:** ReportingComponent.requireScope gets path membership, compares path organization to currentOrganizationId, then enforces OWNER or ADMIN. Resource exports use organization_id=? in SQL; audit events use org_id and stock movements use organization_id. Export JSON body must contain exactly dataset and format, no duplicates/trailing tokens, max 1024 bytes; output capped at 1000 rows and 4 MiB. The method is transactionally read-only. Cursor parsing validates source and exact per-org event identity. These are source observations, not claims of tested limits or complete redaction.

## Evidence-to-acceptance work not yet executed

1. For each of these eleven operations, reconcile the existing ID/C8 integration tests, Newman, Playwright and private IR4 receipts against exact source SHA, actor, real fictional target, HTTP status/code, field exposure, and SQL before/after. Do not infer coverage from a test title.
2. Add cases only for evidence gaps: OWNER/ADMIN positives; CUSTODIAN read and write-denial; MEMBER, assigned/unassigned/demoted LEAD; foreign org and object; revoked membership; If-Match; transaction rollback; pagination, export redaction, cursor misuse.
3. Independent human or genuinely separate review is still NOT_RUN. The assistant's source audit is not that review.
4. Formal C8 remains BLOCKED under BOUNDED_AUTHORIZATION_CONTRACT_V1.md, including all remaining 62 not yet source-oracled operations. This document does not change historical IR4 case results or authorize the protected 96-organization live import.

**Labels:** VERIFIED SOURCE FACT (delegates and scope), DESIGN RECOMMENDATION (tests), OPEN QUESTION (unmapped receipts/independent review).
