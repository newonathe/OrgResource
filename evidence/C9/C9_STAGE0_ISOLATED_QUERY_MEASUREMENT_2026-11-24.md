# C9 Stage 0 — disposable MySQL 8 query-plan and iterator-time probe

**Date:** 2026-11-24 PHT. **Classification:** USER DECISION — the user explicitly authorized C9 on November 24. **Implementation status:** source committed for **CI TESTING**, no C9 performance acceptance yet. **Release status:** no merge, index migration, Northflank access, SMS, hosted database mutation or public publication action.

## Scope and upstream boundary

- Exact source base: `f0cf2ace7623a38fda29ab6b209fcb898d8740a8` (green draft PR #40, GitHub Actions [#45253959642](https://github.com/newonathe/OrgResource/actions/runs/45253959642)); frozen Course Final A `7f8c0746da18fd54b47f430ac1435ac36e605d67` is immutable. V1–V7 Flyway content and all Extended C Java runtime code remain unchanged.
- [C9 handoff](../C8/C9_IMPLEMENTATION_HANDOFF.md) defines performance/scale measurements: real MySQL plans and query latency, import, archive limits, restore and browser journeys **against the accepted exact fixture**, with only empirically justified index changes. **C8 bounded authorization issue #34 is still BLOCKED**, though 93/93 source-to-test mappings now exist in draft PR #40. Independent route-level acceptances and restricted IR4 receipts are absent. Hosted P0 issue #26 remains `LIVE_IMPORT_NOT_RUN`.
- **Authority decision:** authorization to begin C9 permits **preparatory, non-release measurement infrastructure** on an existing disposable synthetic fixture. It does not make the pending C8 independent acceptance or **exact private full-96 fixture** complete, does not imply an accepted SLA, and does not authorize a merge, deployment, live migration, payment or real messaging. Future C9 certification still requires C8 closure + approved full-size source with independent review and operator-controlled clone.

## Changed paths and source coverage

| Changed path | Baseline / exact source | Design / effect | Test and risk |
| --- | --- | --- | --- |
| `scripts/measure-c9-isolated-queries.py` | NEW; `C1Service.java:447–581`, `ResourceRepository.java:32–40`, `AllocationRepository.java:100–124,181–215`, `ResourceRequirementRepository.java:63–83` | **SELECT/EXPLAIN only** in an approved disposable GitHub Actions MySQL Docker container; validates exact intermediate schema/account, grants, 8-tagged/960-allocation fixture and source-selected fictional organization, then reports JSON access types, indexes, estimated rows and 3-run median/max MySQL iterator times for five SQL analogues. No user input into the queries except integer/org and as-of date retrieved under validation; no output of row contents, passwords or account IDs. | Refuses non-CI, other schema, non-SELECT account, incompatible MySQL engine, wrong fixture. Queries are SQL analogues, **not exact emitted Hibernate SQL**. Timings are MySQL `EXPLAIN ANALYZE` top iterator values, **not HTTP p95 or an SLA**. |
| `scripts/verify-c8-additive-isolated.sh` | Existing guarded 8-workspace fixture with SELECT-granted verifier; schema created at runtime in GitHub Actions MySQL 8.0.46; C8 postflight and preservation proofs existing | **+26 conditional lines** for `profile=intermediate` only, after pre-existing C8 data integrity/repeated-import refusal; run schema-name denial, write-capable-account denial, then read-only SQL sample, followed by original-data HMAC verification and independent SELECT-only postflight. No C8 mutation/check changed or reduced. | Mandatory CI small (3), intermediate (8), partial/restore (3), Java8 frozen Course A, Java21/MySQL, React, Newman and Playwright. Failure stops the draft PR. |

**Source-inspection boundary:** fully examined `AGENTS.md`, `C9_IMPLEMENTATION_HANDOFF.md`, repository quality gate, relevant Superpowers skills, `scripts/verify-c8-additive-isolated.sh` setup/target guard and tail, `C1Service` workspace service methods, the four relevant repository query definitions, `.github/workflows/verify.yml` CI environment. Remaining 894-tree historical/source files have not been individually inspected. Do not convert this targeted source audit into a claimed all-repo inspection.

## Five source-related query analogues

| C9 probe | Application method / repository source | Contract checked by the preliminary probe |
| --- | --- | --- |
| `resource_page` | `C1Service.resourcePage` → `ResourceRepository.findWorkspacePage` | Selected organization ID and keyset `id>after`, ascending ID, 51-row representative page; SQL analogue omits optional filters and Hibernate field-mapping work. |
| `allocation_page` | `C1Service.allocationPage` → `AllocationRepository.findWorkspacePage` | Selected tenant + real Requirement/Project joins; ascending keyset; 51 rows. Does not include optional role/period predicates and JPA entity materialization. |
| `requirement_page` | `C1Service.requirementPage` → `ResourceRequirementRepository.findWorkspacePage` | Same-org Project+Resource relationship, 51-row keyset; no API JSON materialization. |
| `stock_ledger_aggregate` | `AllocationRepository.INVENTORY_LEDGER_AGGREGATE` | By-resource `SUM(delta_total)` and `SUM(delta_issued)`, one tenant. Does not include the full inventory custody/holds query or semantics of ledger verification. |
| `overview_status_counts` | `AllocationRepository.countWorkspaceStatuses` | One org status and overdue aggregate with fixture-derived `demo_as_of` date at midnight; no API response overhead, privacy/access checks or operational now-clock sampling. |

### Evidence criteria and adversarial checks

1. **Boundary:** require `GITHUB_ACTIONS=true`, exact disposable schema `orgresource_c8_additive_ci_fixture_intermediate`, exact `c8_add_ro_intermediate` reader with current authenticated user and grants limited to USAGE + SELECT on that schema, Docker container hex ID and local TCP MySQL 8.0.x. A schema mismatch and a writer-user request must fail before issuing SQL. A non-CI or missing-secrets invocation fails closed.
2. **Fixture:** exactly eight demo-tagged fictional orgs, 960 simulated allocations, no original fixture allocations; select the actual workspace with the most allocations. The original real-person dataset and 96-plan are never opened. The validated demo-as-of date supplies the overdue comparison. Any mismatch aborts measurement without altering data.
3. **Plans:** actual MySQL `EXPLAIN FORMAT=JSON` returns a parsed table operator per query, and `EXPLAIN ANALYZE` executes only the static SELECT analogue with warm-up + 3 measured samples. Print only bounded index/access/estimated row and median/max iterator statistics. Do not treat an optimizer plan estimated row count as an actual scanned count or count CLI process startup as server query time.
4. **No-writes:** preserve previously verified C8 original 15-table HMAC and postflight data checks **after** the probe; no DDL/DML is present in the script. Refusal failures must not be relabeled as CI successes. Do not set an arbitrary time SLA, run `ANALYZE TABLE` or introduce a Flyway V8 index without a measured accepted workload.
5. **Independent review:** additional qualified reviewer must inspect the SQL analogue-vs-Hibernate limitation, metadata sensitivity and findings before acceptance; source author or CI alone does not provide that reviewer.

## C9 staged backlog (strict dependencies)

- **C9-0 [CURRENT]:** isolate the read-only instrumentation and prove the deny + positive gate on MySQL8 CI. Record exact SHA/job/log/latency/index observations. If CI is red, fix only the demonstrated script/setup defect.
- **C9-1 [BLOCKED until C8 independent evidence and owner-controlled approved fixture]:** collect *actual emitted Hibernate SQL* and `EXPLAIN ANALYZE` on an independently restored, private, full-96 fixture clone. Bind org sizes, selectivity, index choice, wall-clock/API p50/p95/p99, warm/cold caches and bounded concurrent reads to reproducible workload IDs. Preserve existing owner and credentials.
- **C9-2 [BLOCKED]:** measure import size/time/memory at 96 orgs, export/archive row/byte limits, backup, independent off-host restore and failure recovery. Prove operational cutoffs with actual byte/record receipts. Do not upload raw plan or database backup to public GitHub.
- **C9-3 [BLOCKED]:** run signed-in browser journeys and permission/privacy regressions for large resources, allocations, Project/Requirement history, selected vs path workspace, paging and reload/retry. Check responsive/a11y and permitted fields.
- **C9-4 [AFTER reviewed evidence]:** propose a minimal index/migration **only** if real plans identify a bottleneck and measurable benefit on the accepted full-size fixture; verify write amplification, MySQL grants, Flyway replay/checksums, restore, rollback/forward-fix and exact full CI. No speculative index now.
- **C9 acceptance:** independent review, accepted fixture and documented measurements, owner-approved thresholds/deferrals, all required exact-head gates green. **C9 is not complete from C9-0. C10 remains separate authorization.**

## Explicit negative conclusions

No query plan, latency, archive, full private 96 rehearsal, off-host backup/restore, Northflank image, deployment or browser result is claimed **until** a linked executed result supplies it. No public database metrics or institutional participation claims. No course submission, author approval, or live adoption proof. Return to issue #34 for C8 independent review and #26 for hosted P0 prior to any release.

## Actual executed CI measurement (VERIFIED CI; first code-only probe SHA)

**Source SHA:** `79e98d02d21c3e042e13cf4489ca7acccb327a8f` · [GitHub Actions #45260222540](https://github.com/newonathe/OrgResource/actions/runs/45260222540) **all 3 jobs completed SUCCESS** on 2026-11-24 PHT. This is evidence for the exact script/invocation/source at that SHA; later status/handoff-only commits require their **own** exact-head CI.

- **VERIFIED CI:** Java 8/Course A and React success; Extended Java21/MySQL **91 Maven tests, including 66 ordered**, no failures/errors/skips; C8 small `3/36/97` (orgs/allocations/events), intermediate `8/960/3013`, and partial restored-copy proof all passed; Newman 48 requests/90 assertions + 12/20, zero failures; Playwright 19/19. The C9 probe executed inside the existing **intermediate** stage, with forbidden non-fixture schema and writer-identity invocations verified to fail.
- **VERIFIED CI MySQL 8.0.46 execution result:** five source-derived SQL analogues against the **one synthetic organization selected by allocation count**. Each has one warm-up and three `EXPLAIN ANALYZE` samples. The following values are *root iterator milliseconds* from one GitHub runner, not user-facing HTTP time:

| SQL analogue | Median ms | Maximum ms | Primary access plan at sample |
| --- | ---: | ---: | --- |
| `resource_page` | 0.053 | 0.053 | `range` on `PRIMARY` (resource) |
| `allocation_page` | 0.130 | 0.133 | `range` on `uk_allocation_org_id_resource` (allocation), `eq_ref` on PRIMARY for requirement and project |
| `requirement_page` | 0.155 | 0.174 | `index` on `uk_requirement_id_org` (requirement), `eq_ref` on PRIMARY for project and resource |
| `stock_ledger_aggregate` | 0.141 | 0.151 | `ref` on `ix_stock_movement_resource_page` |
| `overview_status_counts` | 1.140 | 1.140 | `ref` requirement `uk_requirement_project_resource`, `eq_ref` project PRIMARY, `ref` allocation `uk_allocation_org_id_resource` |

**Interpretation / independent critique:** The slowest *sampled SQL analogue* was overview status counts at 1.140 ms, but it processed a very small fixture, included no Hibernate/HTTP serialization, cache-control experiment, concurrency or accepted full-96 distribution, and had no preapproved SLA. An automatic index or query rewrite based on these numbers would be unjustified. SQL's optimizer-estimated row counts are not actual table reads; three samples are insufficient for a p95/p99. The test is safety/probe instrumentation, **not C9 production performance certification**.

**VERIFIED EXTERNAL FACT (checked 2026-11-24):** Official [MySQL 8.0 EXPLAIN documentation](https://dev.mysql.com/doc/refman/8.0/en/explain.html) states that `EXPLAIN FORMAT=JSON` reports plan properties and `EXPLAIN ANALYZE` executes the SELECT, producing `TREE` iterator timing in milliseconds; the latter requires the statement's execution privilege. This confirms the probe's separation of estimated access plan and measured iterator time. It does not validate the user application's end-to-end latency.

**Remaining gates:** latest **final documentation-and-probe SHA** exact-head CI must be checked separately; accepted exact private full-96 fixture/real Hibernate SQL/API p95/archives/off-host restore/browser-scale and independent C8 route acceptance are **NOT RUN / BLOCKED**. No speculative migration, index or cloud release is approved by this measurement.

## Stage 0b — exact native Inventory page-balance query shape (proposed CI extension)

**USER DECISION:** C9 work authorized on November 24; Stage 0b is still **preparatory**. This additional C9 query sample uses the same fictional eight-organization, 960-Allocation, disposable and SELECT-only MySQL8 fixture. It is **not** a full-size/hosted benchmark, a migration, or a release approval.

**VERIFIED SOURCE / transitive dependency:** `C1Service.resourcePage:499–516` pages selected-organization Resources, extracts the *visible* Resource IDs, and calls `AllocationRepository.findInventoryPageBalances`. The actual native query in `AllocationRepository:185–215` projects **resource ID, ledger total, ledger issued, approved holds, issued custody** by combining two separately grouped left joins for `stock_movement` and `allocation/resource_requirement/project`, restricted by `organizationId` and `ids`. The existing `stock_ledger_aggregate` in Stage 0 sampled only one of those ingredients; it does **not** measure the complete stock-balance call made by the Resource page.

**IMPLEMENTED IN DRAFT / unverified until exact CI:** `scripts/measure-c9-isolated-queries.py` now selects a maximum 50 visible Resource IDs from the chosen synthetic organization, validates positive, unique integer IDs, expands only those source-selected IDs and the schema-validated `demo_as_of` date into the **native query shape**, and checks that the actual SELECT returns exactly one 5-column row per visible Resource. It then runs `EXPLAIN FORMAT=JSON` and three warmed `EXPLAIN ANALYZE` timings for the additional `native_resource_page_balances` query. SQL is still manually instantiated, not a captured Hibernate/driver prepared-statement trace. Source values, generated IDs, rows, credentials and plans' actual row data are never printed. **Six named queries** should run in the complete fixture. Existing refusal tests, original-data HMAC and independent postflight execute unchanged.

**Reproduction and source coverage:** GitHub Actions Extended C invokes `bash scripts/verify-c8-additive-isolated.sh intermediate` after building/bootstrapping the disposable MySQL8 container and a Java21 JAR. The script invokes this Python probe with the generated SELECT-only `c8_add_ro_intermediate` principal and exits nonzero if the native query is incomplete, unparsable, unauthorized or over its statement timeout. Check exact-head CI and the `C9_ISOLATED_READ_ONLY_MEASUREMENT_OK` line for the six individual records, as well as **91 Maven / 66 ordered**, C8 small/medium/restore, Newman and Playwright. Do not infer that the new query ran from an older five-query CI result.

### Reproducibility caveat identified across two successful Stage 0 CI runs

The SQL analogues and MySQL 8.0.46 fixture size were the same, yet [first completed run #45260222540](https://github.com/newonathe/OrgResource/actions/runs/45260222540) chose `uk_allocation_org_id_resource` for the allocation page (median **0.130 ms**) and measured overview status counts at **1.140 ms**, while [final-head Stage 0 run #45261711771](https://github.com/newonathe/OrgResource/actions/runs/45261711771) used `ix_allocation_org_requirement_status` (median **0.174 ms**) and recorded overview status counts at **0.158 ms**. Other index choices also varied. **VERIFIED CI OBSERVATION:** differing plan choices and timings on two independent disposable fixtures. **OPEN CAUSE:** this evidence does not distinguish optimizer statistics, fixture row-ID/cardinality differences, cache/warm-up or runner conditions. A small-sample single-run median is not a stable optimization target. Do not attribute a performance regression, assume deterministic plans, or create an index from these results.

**Stage 0b acceptance:** require executed exact-head CI and review of (a) 6/6 SELECT plan/timing reports, (b) native query one-row-per-resource check, (c) zero C8 protection failures, (d) matched frozen Course A/Flyway trees and (e) a separate qualified reviewer. Existing 96-org accepted plan, actual emitted Hibernate SQL, real API/browser latency p95, archive and off-host restore remain **NOT RUN**; formal C8 authorization review and host release are **BLOCKED**. No merge, deploy or C10.
