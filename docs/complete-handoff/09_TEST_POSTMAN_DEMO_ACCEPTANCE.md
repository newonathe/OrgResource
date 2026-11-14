# 09 — Complete acceptance testing, fixtures, Postman and demonstration

## Test philosophy

A planning specification or mock reference model is not a running Java test. Each required code feature must be accepted based on real execution: compiler, actual relational DB, HTTP/Postman, real browser, actual scheduler, external HTTP simulator, and clean export. Every test result PASS / FAIL / BLOCKED / NOT_RUN with command, timestamp, output evidence and commit. A generated Postman JSON file is not evidence that any request executed. Static route coverage can be checked independently from runtime.

## Reproducible fictional fixture

Department "Fictional Logistics" and Project "Welcome Workshop" created with startAt in future and endAt>startAt. TWO Resources: REUSABLE "Extension cords" totalQuantity 10, CONSUMABLE "A4 paper sheets" totalQuantity 100. Requirements: 6 cords and 40 sheets. Allocations: A=4 cords, B=2 cords, C=30 sheets. A and B cannot be issued until Project actually becomes active; do not run setup and fulfillment back-to-back expecting future-start to have passed. Use injected fixed Clock in automated integration tests and real clock in video.

Expected state checkpoints:
- Initial cords available 10, covered 0; paper available100,covered0.
- A PENDING no change. A APPROVED: cords available6,covered4.
- A ISSUED: cords available6,covered4 (no second deduction).
- A RETURNED: cords available10,covered4.
- B APPROVED: cords available8,covered6.
- B ISSUED: cords available8,covered6. Leave B ISSUED through real scheduled FakeTwilio demonstration if eligible.
- B RETURNED: cords available10,covered6.
- C APPROVED qty30 paper: paper available70, covered30, total still100.
- C CONSUMED: paper available70, covered30, total becomes70 (not 40!).
- Paper Resource updated with total=120 expectedTotalQuantity=70: paper available120,covered30.
- Requirement shortage: cords0, paper10.
No callback can imply physical return when only SQL status updated.

## Disposable CRUD fixture

Separate new Department, new Project, zero-stock Resource, Requirement, never-approved Allocation. Create/list/rename each, cancel pending, revoke SMS if testing optional route, delete Allocation -> Requirement -> Resource -> Project -> Department; verify DB rows gone and main workflow unchanged. Resource delete requires total0 and no Requirement reference. This sequence was statically dependency checked, not run in Java.

## Route coverage

Department D01-D04; Project P01-P04; Resource R01-R05; Requirement Q01-Q04; Allocation A01-A09. Original proposal 25 + reviewed A09=26. See 05_API_26_ROUTES_AND_RESPONSES.md for exact path, body and errors. Postman template supplied in ZIP is importable scaffolding only; agents must add generated-ID scripts, negative assertions and run it against actual backend.

Postman environment variable names:
baseUrl; runTag; projectStartAt; projectEndAt;
crudDepartmentId,crudProjectId,crudResourceId,crudRequirementId,crudAllocationId;
workflowDepartmentId,workflowProjectId,cordResourceId,paperResourceId,cordRequirementId,paperRequirementId,allocationAId,allocationBId,paperAllocationId.
Store created IDs from real JSON response, never assume 1. For 'create' expected 201; list expected 200 array; mutators 200. Responses include code/message on failure, deletion message on success. Use unique runTag per run to avoid duplicates; check no residual environment IDs from a failed earlier run; never automatically replay POST after network uncertainty.

Example Postman post-response script for Department create:
~~~javascript
pm.test("Department created", function() {
  pm.response.to.have.status(201);
  const body = pm.response.json();
  pm.expect(body.id).to.be.a("number").above(0);
  pm.expect(body.name).to.be.a("string");
});
if (pm.response.code === 201) {
  const body = pm.response.json();
  if (Number.isInteger(body.id) && body.id>0)
    pm.environment.set("workflowDepartmentId",String(body.id));
}
~~~

Example availability:
~~~javascript
pm.test("Four approved cords reserve exactly four",function(){
 pm.response.to.have.status(200);
 const r=pm.response.json();
 pm.expect(r.availableQuantity).to.eql(6);
 pm.expect(r.resourceId).to.eql(Number(pm.environment.get("cordResourceId")));
});
~~~
Availability JSON has ONLY resourceId and availableQuantity. To independently prove Resource.total and commitments, query Resource GET and DB/Allocation history, not same function twice.

Example refusal:
~~~javascript
pm.test("Repeat CONSUME refused",function(){
 pm.response.to.have.status(409);
 pm.expect(pm.response.json().code).to.eql("INVALID_ALLOCATION_STATE");
});
~~~
Then query DB Resource total to ensure unchanged on repeat. This is not an injected rollback test.

### Time fixture

Project must be created with future start and later end. Postman variables can be generated dynamically in Philippine wall-time, but avoid browser/device timezone assumptions; use Intl DateTimeFormat with Asia/Manila or explicitly offset for demo date only. Use a 3-minute future start and a few hours duration for video after waiting; never pretend immediate fulfillment allowed. Fixed Clock tests use exact instants without wall clock wait.

## Backend test cases

BE01 Fresh startup five tables.
BE02 Department 201 and persisted.
BE03 GET Department row.
BE04 normalized duplicate 409.
BE05 referenced Department delete 409.
BE06 Project invalid interval 400.
BE07 Project foreign key absent 404.
BE08 Project rename while historical dates remain unchanged allowed.
BE09 Project date change after historically approved Allocation 409.
BE10 Resource zero-stock create, available0.
BE11 Approval 4 of available5 reserves4.
BE12 Parallel Resource approvals qty4+4 of total5: exactly one success.
BE13 Parallel Requirement approvals qty4+4 of demand6: exactly one success.
BE14 Pending Allocation doesn't reserve.
BE15 Reusable ISSUE no double deduction.
BE16 Return releases custody once.
BE17 Second return 409/no change.
BE18 Consumable CONSUME decrements total once.
BE19 Second consume 409/no change.
BE20 Reduce Requirement below current coverage 409.
BE21 Requirement10 with pending9, reduce to5 =>409.
BE22 stale Resource update =>409/no overwrite.
BE23 expired APPROVED before cleanup =>effective hold0.
BE24 expired PENDING cleanup => CANCELLED/EXPIRED no stock effect.
BE25 expired APPROVED cleanup=>CANCELLED/EXPIRED no second release.
BE26 overdue ISSUED remains unavailable.
BE27 approved then cancelled deletion =>409.
BE28 never-approved cancelled/pending deletion succeeds.
BE29 positive stock Resource delete =>409.
BE30 zero-stock unreferenced Resource delete succeeds.
BE31 duplicate Project+Resource Requirement =>409.
BE32 forced DB exception during consume rolls back Resource and Allocation.
BE33 empty aggregate quantity0.
BE34 nonexistent required parent FK =>404 no save.
BE35 malformed JSON=>400.
BE36 client-controlled generated ID/status rejected or safely ignored per documented contract, never applied.
BE37 restart preserves all data.
BE38 clean Java source import/build.
BE39 duplicate Resource/Department create concurrently => single row due unique constraint.
BE40 Project update vs approval race preserves historical-date rule.
BE41 Resource correction vs consume race preserves invariant.
BE42 Query results across status and endAt boundary coherent.
BE43 Read-only response across multiqueries stable or documented snapshot.
BE44 Client lost response cannot trigger blind duplicate mutation.

## SMS/scheduler tests

SMS01 Disabled startup with no SMS config.
SMS02 Expiry scheduled independent of SMS.
SMS03 Eligibility exactly [endAt-24h,endAt).
SMS04 One durable claim and no second scanner claim.
SMS05 RETURNED before claim suppress.
SMS06 Revoked consent before claim suppress.
SMS07 Return/revoke observed before dispatch suppress.
SMS08 Actual FakeTwilio accepted HTTP and typed Gson parsed => SIMULATED.
SMS09 Definitive HTTP rejection => REJECTED.
SMS10 Timeout/uncertain => UNKNOWN and no automatic resend.
SMS11 Restart with CLAIMED/UNKNOWN no repeated send.
SMS12 Overdue endAt<=now no upcoming reminder.
SMS13 Real provider success != handset delivery.
SMS14 Missing destination no call.
SMS15 Only simulator dummy Authorization; check no tokens in log.
SMS16 Reminder result persistence never overwrites RETURNED status.
SMS17 Expired PENDING cleanup no stock change.
SMS18 Expired APPROVED cleanup no double stock change.

## UI acceptance tests

UI01 Empty DB shows empty state/no fake stats.
UI02 Department create through real API.
UI03 Project date input PHT roundtrip.
UI04 Resource types and availability.
UI05 Requirement coverage/shortage.
UI06 Pending allocation stock unchanged.
UI07 Approval reserves.
UI08 Issue doesn't double deduct.
UI09 Return restores custody.
UI10 Consume reduces physical once.
UI11 Stale Resource update shows 409 and refresh.
UI12 Unsafe delete explanatory 409.
UI13 Consent revoke clears destination.
UI14 Browser reload shows persistent rows.
UI15 Backend restart data retained.
UI16 Backend outage yields error, no false success.
UI17 Keyboard-only core workflows.
UI18 Mobile/narrow controls accessible.
UI19 Browser E2E uses actual backend, not only mocks.
UI20 Bundled JAR serves UI and API.
UI21 Partial dashboard fetch failure not silently zero.
UI22 Lost POST response does not auto-retry.
UI23 Exact action gating by Allocation status/resource type.
UI24 Responsive table scroll, dialogs and focus restore.

## Acceptance levels and evidence

- STATIC: source reads.
- COMPILE: javac/Maven output, exit code.
- DB: real schema and query.
- HTTP: real Postman/curl against backend.
- CONCURRENCY: parallel requests and final persistent state.
- SCHEDULE: observed @Scheduled invocation.
- EXTERNAL: actual Retrofit HTTP to simulator/provider.
- BROWSER: real React UI and Java backend.
- PACKAGING: real source ZIP, checksums and tested import.
- SUBMISSION: actual Canvas receipt/video link, not performed here.

Use independent assertions, not simply calling the same helper function that could be wrong. If CHECK constraint unsupported by older MySQL, Javax validation + DB FKs/unique still required and document exact enforcement.

## Postman source-generation caveat

The included postman collection is a ROUTE TEMPLATE, not a final full test suite. Every endpoint definition has method, path, placeholder body where needed. Coding agents must amend dynamic IDs, create-flow scripts, negative tests and actual requests after implementation. Report import status separately from execution status.
