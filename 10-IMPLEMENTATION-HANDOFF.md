# Implementation Handoff

## Binding scope and review status

Implement the OrgResource design in02-07 after the student reviews the proposal. This task produced documentation, contract data, Postman artifacts and documentation checks only. There is no application backend, running MySQL schema, authentication system, verified Twilio account, production deployment, actual SMS delivery or completed course submission. The package is intended to remove business-rule invention from implementation while keeping instructor/account-dependent confirmations visible.

Single organization, flat departments, nine entities, whole-unit pooled resources, one Java process, one InnoDB database, fixed roles and one storage label per pool. Reusable projects may share capacity across non-overlapping windows, subject to physical return before issue. Consumables reserve current stock globally and are consumed at handover. Parent-resource locking serializes every relevant mutation. Immutable ledger and audits preserve history. Exactly-once SMS is not promised.

## Development order and dependencies

1. Verify the working class baseline: Eclipse project, JDK, MySQL version/connector, Boot/Jersey/Javax/Retrofit/GSON dependencies. Create a new isolated application project only when implementation is requested; do not modify the reference pack. Register one Jersey JSON route and confirm Autowired component/repository/database flow.
2. Add exception mapping, DTO validation, ActorResolver DEMO profile and PermissionComponent before trusting actor-dependent commands. Bind local demo to127.0.0.1 and fail startup for an unconfigured authenticated profile. Add injected Clock and configuration guards. Do not build password/SSO infrastructure speculatively.
3. Implement Department/AppUser/Resource/Project entities, repositories, DTOs and basic CRUD with constraints. Bootstrap only an ADMIN or isolated full fictional seed. Validate every custom repository name at startup. Document the actual BOM and database version.
4. Implement InventoryTransaction immutable ledger for receipt, storage damage/loss and officer recount, plus exact effect summation, commandKey uniqueness and AuditEvent. Inventory can now be created through runtime endpoints with auditable source of truth. No writable Resource.quantity.
5. Implement ResourceRequirement and project lifecycle, then Allocation request/approve/reject/cancel/release with the single-resource lock protocol. First prove one complete reusable and consumable scenario through Postman. Unit-test sweep endpoint grouping, coverage and quantity invariants before expanding features.
6. Implement actual reusable issue/partial usable/damaged return/custody loss, consumable consumption, constrained latest-event reversal, and risk flags after physical capacity loss. Use the same typed movement path for direct POST and convenience routes.
7. Implement project cancellation and complete-set unissued rescheduling with sorted multi-resource locks and all-or-nothing validation. These depend on the proved single-resource invariants. Do not implement a partial update loop that can leave some windows changed after failure.
8. Implement availability, shortage consolidation and operational reports from the shared pure calculations. Do not duplicate formulas separately in frontend/controllers/reports. Verify simultaneous vs sequential demand and conditional return dependency.
9. Implement NotificationLog outbox, due/overdue scan, claim/send/finish, bounded safe retry and UNKNOWN recovery; wire Retrofit to FakeTwilio first. Preserve form encoding, dummy fake credentials and no external call inside stock transactions. Check live Twilio feasibility early enough to resolve account restrictions, while leaving actual sends disabled by default.
10. Finish all68 route contracts and Postman examples, then run the ordered demonstration and negative/concurrency/integration tests. Export the running project and record skeleton evidence. Add optional UI only after mandatory backend gates pass. Finally rehearse the final group discussion and actual integration evidence at the documented level.

## Minimum viable complete implementation

Minimum does not remove an academic technology or the selected defining function. It includes all nine justified entities and explicit create/read/delete contracts, one configurable organization, flat departments, current/near-future requirements, partial requests and decisions, reusable interval planning/custody, consumable current stock commitments, receipts/issues/returns/consumption/damage/loss/adjustments/correction, basic project lifecycle, safe cancellation/rescheduling, explanatory shortage views, ledger/audit, local demo identity with actual component permissions, two reminder events, Retrofit/Twilio boundary, Spring scheduler, Postman and meaningful tests. The reused CRUD/DTO/error patterns keep68 operations manageable; they are contracts, not68 independent feature modules.

Important supporting work that can use simple implementations: audit display as paginated JSON; reports computed on demand; notification attempts embedded in one log; one-process single-thread dispatch; a strict latest-event reversal fence rather than general journal surgery; storage/category/unit as strings; server errors as one standard mapper; seeds only in a named demo profile. Optional work: five-view UI, resource photos if needed for course interpretation, CSV exports, barcode labels, serialized assets, repairs, fractional units, rich calendars and measured historical utilization. Never add forecasts without permitted suitable history.

## Milestone and effort plan

Today is3November2026. The written proposal date30October has passed. Check Canvas and submit the reviewed draft according to current instructions; this package cannot grant an extension. Work toward skeleton functionality by13November and its Canvas export/video by14November. Final due5December leaves approximately one month, not an unlimited production programme. The24/27November early-presentation wording requires instructor scheduling confirmation.

A provisional planning budget for a developing1-3person team is90-140 focused team-hours for the specified backend, tests and demonstrations, excluding learning/setup delays and provider-account resolution. This is an engineering estimate, not measured completion data or a guarantee. The earlier five-entity ProjectStock estimate does not apply to this expanded master prompt. Reserve at least the last20percent of available effort for integration, correction, export and explanation. If time becomes short, drop optional UI/analytics first, keep runtime routes and actual final rules, and seek an explicitly revised scope before omitting a mandatory core behavior. Do not spend the schedule implementing infrastructure unrelated to this local service.

## Skeleton checklist

- Class-compatible dependency tree documented and application starts without errors.
- MySQL connects; all nine entity tables and collection table are generated; FK/unique constraints and enum strings verified.
- Demo seed is repeatable and can be disabled; runtime endpoints create data independently.
- All repository methods resolve; explicit JPQL includes the correct enum parameters.
- Every intended route is registered, forwards through Autowired component and returns documented JSON/status; all68 have Postman definitions.
- Exported project contains source/config examples and no credentials/private data.
- Video shows app, tables, fictional test data and URL/Postman results. Any skeleton stubs are explicitly marked.
- Authorship/help acknowledgement and member peer reviews prepared by students.

## Final implementation checklist

- Actual business logic replaces every skeleton stub.
- Resource locks and read isolation verified on actual MySQL; concurrency losers roll back cleanly.
- Ledger balance, coverage, temporal sweep, late return and damaged capacity scenarios pass.
- Metadata changes cannot bypass permissions, state/version checks, history or stock authority.
- Every create/list/get/delete/update/action route matches the reviewed catalogue and Postman collection.
- Safe deletion, cancellation, release, reschedule and reversal rules are demonstrated with realistic failures.
- Meaningful Spring scheduling and Retrofit external calls work; Twilio evidence is reported accurately and accepted method confirmed.
- Notification duplicates, consent recheck, cap, retries, UNKNOWN recovery and outage isolation tested.
- Database backup/export restore verified for the fictional demo.
- Final project export and recorded group discussion prepared; instructor access checked; optional UI demonstrated if built.
- Students review ownership/certification, disclose assistance and send their own peer evaluations.

## Risks and mitigations

Availability complexity: keep pools/whole units/one interval per requirement, use shared sweep functions and exact fixtures. Actual stock differs from promises: record physical observations, show raw negative capacity and impacted IDs, do not overwrite promises silently. Partial custody: derive counters from signed immutable events; no mutable status shortcut. Concurrency: lock existing parent resource before reading child rows, useREAD_COMMITTED writes and test actual interleaving. Multi-resource deadlocks: sorted lock order plus clean409 rollback. Unknown send outcome: never automatic resend; durable UNKNOWN and known-SID refresh. Credentials/trial restrictions: check account early, use safe fake HTTP tests and explicit live gate. Scope pressure: no tenancy, optimizer, payment, serial maintenance or ML; backend-first plan. Academic ambiguity: surface five confirmation points without pretending approval. Legacy dependencies: isolated local demo and coherent course baseline, no arbitrary framework upgrades.

## Confirmation register

Before coding, verify exact working JDK/BOM/MySQL connector and profile registration in class examples. Before claiming academic compliance, confirm printed RetroJit means supplied Retrofit, whether FakeTwilio alone is accepted, image requirement under all-tools wording, and safe immutable-history deletion interpretation. Before submitting, fill actual group members/date, check Canvas, complete authentic authorship/help disclosure and peer reviews. Before any real shared deployment, provide authenticated principal mapping, TLS/access/retention/backups and an organization permission basis. Before live SMS, configure an eligible account/sender/consented permitted recipient and distinguish provider acceptance from observed delivery. None requires a pilot to finish this design.

## Copy paste coding agent handoff

Read OrgResource/README.md, then01-SOURCE-AND-REQUIREMENTS.md and02-COMPLETE-PROJECT-SPECIFICATION.md. Treat03-07 plus contracts/domain-model.json and contracts/operations.json as one reviewed version1.0 specification. Do not reopen the selected concept or turn it into a full ERP. Start with class dependency verification and a Jersey-to-Autowired-component-to-JPA/MySQL slice; preserve javax packages and the supplied Retrofit pattern. Implement in the dependency order above. Do not invent missing provider methods or claim a simulator is live Twilio. Enforce every stock/approval mutation under the parent Resource lock and derive quantities from immutable InventoryTransaction rows. Run actual MySQL concurrency tests. Keep demo identity explicitly insecure/local and fail closed for authenticated mode without a real resolver. Do not deploy, submit, contact the instructor, purchase an account or send SMS without a separate user instruction. Track actual completion separately from this design. Update contracts and documentation together when an approved change is necessary; no hidden stubs in the final milestone. Use08 for executable scenarios and acceptance criteria; report test evidence and limitations honestly. Student review of the proposal is required before this coding handoff is acted on.


