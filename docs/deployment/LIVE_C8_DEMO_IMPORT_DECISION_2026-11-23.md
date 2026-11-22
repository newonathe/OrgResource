# Live C8 demonstration data — safe import decision and implementation acceptance

Date: 2026-11-23 Asia/Manila. Status: **DESIGN/SAFETY GATE — NOT IMPLEMENTED, NOT EXECUTED**.

## Goal / authority

The owner wants the **actual public directory organization labels** (96 selected COA-M/LIONS references) represented in the internet-accessible demonstration. Every internal member, project, item, allocation and historical activity must be **fictional**, with visible synthetic provenance. Do not present any organization as an adopter, tester, participating partner or endorser.

Authoritative source for original fixture contract: [C8 synthetic workload](../../evidence/C8/DEMO_GENERATOR_AND_IMPORT_CONTRACT.md). Full local manifest SHA-256: 57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2. Approved full generated plan SHA-256: 3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c. These are expected values from local evidence, not proof the files are mounted here.

## Existing code cannot perform this hosted import

The command in extended/backend/src/main/java/edu/orgresource/extended/demo/C8DemoCommand.java uses --c8-local-demo-import. It constructs a JDBC URL using loopback 127.0.0.1 and demands an empty schema with a restricted orgresource_c8_demo_ name and explicit matching confirmation. C8DemoImporter.java refuses any existing accounts/organizations and expects Flyway version 7. The Northflank staging DB already has a bootstrapped owner and organization. **Do not run the old command, drop the schema, re-bootstrap or modify its existing protective constraints.**

The generated full fixture is approximately 19.3 MB of plan JSON, 96 workspaces, 21,060 requests and 67,370 actions; 512 MiB/0.2 shared vCPU staging service can be inadequate for this task. Plan size is not DB size. Do not guess import time, capacity, backup size or monthly cost.

## Mandatory design and acceptance requirements for an additive hosted importer

1. Require a **separate command and explicit flag**; never reuse or relax the local-only importer. Refuse the default web process and require an operator-approved, exact known target identifier. No network endpoint to trigger import.
2. Verify the local manifest bytes and expected SHA-256, the complete generated plan SHA-256, schema version, synthetic provenance, 96 unique reference keys, approved tier counts, alias/exclusion rules, 21,060 requests and permitted simulated actor labels. Never copy private source bytes to a public GitHub repository.
3. Require a reviewed, timestamped and actually tested **off-host backup and isolated restore** before any live change. Prove existing organization/account/membership data is intact in the restore.
4. Validate schema state: Flyway version, existing owner account, no existing demo cohort conflict, MySQL grants, capacity and encryption/provenance fields. Default to dry-run preflight with a no-mutation database transaction if feasible. Refuse unknown/partial state.
5. Preserve existing rows. Use conflict-free IDs/slugs, deterministic import identity, unique cohort provenance, no duplicate creation on rerun, and an explicit recovery path for partial failure. A failed retry must not double-mutate stock or receipts.
6. Make the intended demo owner a member with authorized access to imported organizations without reusing fictional persons as real accounts. Enforce individual tenant isolation, selected organization and role semantics.
7. Reuse application inventory functions and write invariants (stock movement, approved holds, issued custody, consumption/returns, audit events, receipts). Never direct-SQL fabricate ledger success or disable transactions/constraints.
8. Import in bounded batches with measured memory, MySQL connections, CPU and wall time; test the small/intermediate profiles in an isolated clone first. Validate sampled and aggregate stock/ledger/receipt states after each cohort and at completion.
9. Label API, UI, export and generated user records as synthetic. No real SMS, live phone numbers, emails, private individuals, organizational logos or unlicensed photos. Source URLs are attribution, not evidence of institutional approval.
10. Record exact code SHA, source SHA, fixture SHA, operator action, backup identifier (never credential), inserted row counts, reconciliation totals, timing and failure/recovery outcome. Verify the old owner can still log in and access the original org.
11. Require explicit authorization before performing the actual **live database mutation**, separate from pushing code or creating a plan. Never auto-run via CI, Northflank deploy or app startup.
12. If no manifest, backup/restore or safe resource plan is available, mark LIVE_IMPORT_BLOCKED and continue C8 authorization plus standard small fictional app UI testing.

## Stepwise execution only after all gates

(a) Reproduce original local fixture validation; (b) test separate importer on isolated cloned staging database; (c) compare before/after database snapshots and all invariants; (d) approve production-safe bounded execution; (e) execute operator-controlled job once; (f) independently verify app UI, 96 names, tenancy and simulated provenance; (g) record restoration procedure.

**Current status:** Manifest not mounted here; no cloud backup/restore proved; no additive importer exists; no cloud import performed. It would be inaccurate to claim the live site is populated.


## 2026-11-23 user reprioritization and concrete next work

Main P0 is now exact approved demo data on the **existing Northflank deployment**, safely preserving original owner and organization. [Canonical issue #26](https://github.com/newonathe/OrgResource/issues/26) and [current safety/preflight runbook](P0_NORTHFLANK_DEMO_POPULATION_2026-11-23.md). A fail-closed mixed-cohort classifier and nonmutating preflight code were pushed as feature commits `6c7fb265` and `aa757ef9`; their execution/CI status must be verified against current refs. **No additive importer or live database mutation was executed by those commits.** Do not automatically promote them to staging; check existing tests that deliberately reject incomplete mixed cohorts and retain all original organizations and permissions.
