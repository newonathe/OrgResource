# 04 — Exhaustive domain and relational data contract

## Current academic baseline schema: five JPA entities

This section specifies the present academic baseline. The assignment requires at least five justified entity types; it does not set a maximum. The current five-entity model is not a permanent cap on a later, separately reviewed production product. O14 keeps this baseline intact and does not start that production expansion.

Department (1) -> many Project.
Project (1) -> many ResourceRequirement.
Resource (1) -> many ResourceRequirement.
ResourceRequirement (1) -> many Allocation.
A Project may need several Resource types; uniqueness is one Requirement for each (project_id,resource_id) pair. Allocation is a request/commitment toward one Requirement. Do not introduce StockMovement, User, Notification, AuditLog or other JPA entity for baseline.

Use explicit @Table physical names and @Column names for repeatable video/SQL evidence: department, project, resource, resource_requirement, allocation. Actual schema must be inspected, not assumed.

Entity: Department
- id Long generated positive PK NOT NULL, never client writable.
- name String 1–100, trim + normalize display.
- nameKey String server-computed normalized unique value, NOT NULL, indexed unique.
Meaning: a fictional organization department/team that owns Projects.
Creation uniqueness must be enforced by DB, not only a preflight SELECT. Update duplicate check excludes same record.

Entity: Project
- id Long generated PK.
- department @ManyToOne Department, required FK.
- name String 1–100 trimmed, NOT NULL.
- startAt LocalDateTime NOT NULL, future on create or when date changed; local Philippine time.
- endAt LocalDateTime NOT NULL, strictly after startAt.
Meaning: organizational activity interval. On update department is immutable. A name-only edit retains unchanged dates even if historical; changing either date requires a future new start and NO historically approved Allocation under Project. Delete only if no Requirements.

Entity: Resource
- id Long generated PK.
- name String 1–100 trimmed, unique normalized nameKey.
- nameKey String NOT NULL unique index.
- type ResourceType REUSABLE | CONSUMABLE, NOT NULL, immutable after create.
- totalQuantity Integer 0–1,000,000, NOT NULL.
Meaning: fungible whole units. REUSABLE total is usable units owned (including out on loan); CONSUMABLE total is current remaining stock. No mandatory unit column (the original 12-page proposal did not include one). Name must clarify measurement, e.g. "A4 paper sheets".
- availableQuantity derived in output, NOT stored.
- Delete only with totalQuantity=0 AND zero Requirement references (intentional stricter rule than original, which only checked Requirements).
- Resource updates must send expectedTotalQuantity from last read; after Resource lock, reject mismatch and refused lower-than-commitment total. This is optimistic expected-value protection, NOT full idempotency or movement audit.

Entity: ResourceRequirement
- id Long generated PK.
- project required @ManyToOne Project FK.
- resource required @ManyToOne Resource FK.
- quantity Integer 1–1,000,000.
- unique(project_id,resource_id) in DB.
- allocatedQuantity/shortageQuantity derived in JSON, NOT stored.
- Cannot reduce below MAX(current effective coverage, largest live PENDING Allocation quantity).
- Cannot update quantity after Project ended; can delete only if no Allocation records.
- Once a never-approved CANCELLED Allocation is deleted, it no longer constrains demand; historically returned/consumed count remains.

Entity: Allocation
- id Long PK.
- requirement required @ManyToOne ResourceRequirement.
- quantity Integer 1–1,000,000, <= Requirement quantity at create, IMMUTABLE thereafter.
- borrowerName String 1–100 trimmed, NOT NULL; a fictional label, no verified identity.
- phoneNumber String nullable; if non-null must pass E.164-like + plus 8–15 digits, first digit 1–9; require for smsConsent true. With consent false, do not retain a phone number. Use fictional controlled test destination, no actual person.
- smsConsent Boolean NOT NULL default false. The fact a JSON Boolean says true does NOT prove actual human consent; demo only.
- status AllocationStatus NOT NULL one of PENDING APPROVED ISSUED RETURNED CONSUMED CANCELLED, persisted as STRING.
- createdAt LocalDateTime NOT NULL, server set.
- approvedAt LocalDateTime nullable, immutable historical marker after first approval.
- issuedAt LocalDateTime nullable, set once.
- returnedAt LocalDateTime nullable, set once.
- consumedAt LocalDateTime nullable, set once.
- cancelledAt LocalDateTime nullable, set when closed/cancelled.
- cancellationReason Enum nullable: USER_REQUEST or EXPIRED.
- reminderClaimedAt LocalDateTime nullable, durable mark for one automatic SMS attempt.
- reminderOutcome Enum NOT NULL default NONE: NONE, CLAIMED, SUPPRESSED, SIMULATED, PROVIDER_ACCEPTED, REJECTED, UNKNOWN.
- reminderHttpStatus Integer nullable, actual received code.
- providerMessageSid String nullable, only when actual provider returns it; no invented SID.
Derived response:
- contactPresent Boolean = stored valid destination present.
- reminderAttempted Boolean = reminderClaimedAt != null.
- expiredPendingCleanup Boolean when status PENDING/APPROVED and now >= associated Project.endAt.
The public response must never return phoneNumber. Do not persist derived fields unnecessarily.

### Java enums

ResourceType = REUSABLE, CONSUMABLE.
AllocationStatus = PENDING, APPROVED, ISSUED, RETURNED, CONSUMED, CANCELLED.
CancellationReason = USER_REQUEST, EXPIRED.
ReminderOutcome = NONE, CLAIMED, SUPPRESSED, SIMULATED, PROVIDER_ACCEPTED, REJECTED, UNKNOWN.
Use EnumType.STRING, NOT ORDINAL.

### Name normalization

Input "  Logistics   " -> display "Logistics", nameKey "logistics".
Collapse internal multi-spaces consistently; lower-case with fixed Locale.ROOT and trim. Protect DB unique index and handle constraint violation races. Do not use case-insensitive Java check as a substitute for DB uniqueness. Verify collation and Unicode behavior; document chosen comparator.

### Date and time

Public Project strings format YYYY-MM-DDTHH:mm:ss interpreted as Asia/Manila local time. Use an injected java.time.Clock for time-dependent test determinism and ZoneId.of("Asia/Manila"). Do not call local Java clock separately in repeated query phases and inadvertently cross end boundary. Sample a common 'now' per business operation. A Project's active time is [startAt,endAt): inclusive start, exclusive end. Project creation requires future start; name-only update does not require moving existing dates into the future.

### Reservation and stock mathematics

Let T = Resource.totalQuantity.
Let H = sum quantity of Allocation where status APPROVED and Project.endAt > now, regardless of Project startAt.
Let I = sum quantity of ISSUED reusable Allocations, even after Project end.
Reusable: available = T - H - I.
Consumable: available = T - H (physical total deducted on consume).
Never clamp negative availability to zero. Negative is DB integrity failure. For an APPROVED consumable of quantity q, on CONSUME total drops T->T-q and H->H-q so available stays unchanged at that instant, not deducted twice.

For Requirement Q:
Covered = effective APPROVED + ISSUED + RETURNED + CONSUMED amounts.
Shortage = max(0, Q.quantity - Covered).
PENDING and CANCELLED count zero. Effective APPROVED stops counting when Project ends even if stored status remains APPROVED awaiting cleanup. RETURNED/CONSUMED remain historical fulfillment after end.
Invariant: 0 <= Covered <= Q.quantity, 0 <= Available <= T subject to reusable issued custody.
Important semantics: returned reusable equipment is available for another Project but the first Project's Requirement stays covered. Approved future Project reserves globally even before it begins; this is conservative capacity behavior and no double future booking based on hoped return.

### Allocation transitions

None -> create -> PENDING (no hold).
PENDING -> approve -> APPROVED (requires Project not ended, sufficient stock and uncovered demand; timestamp approvedAt).
PENDING -> cancel -> CANCELLED/USER_REQUEST.
PENDING -> expiry cleanup after Project end -> CANCELLED/EXPIRED.
PENDING -> delete allowed (never approved).
APPROVED reusable -> issue -> ISSUED during Project active interval; no second availability deduction; timestamp issuedAt.
ISSUED reusable -> return -> RETURNED at any time, including after end; officer explicitly declares whole batch returned usable; timestamp returnedAt; custody removed; coverage preserved.
APPROVED consumable -> consume -> CONSUMED during active interval; subtract Resource.totalQuantity exactly q once in same transaction; timestamp consumedAt.
APPROVED -> cancel -> CANCELLED/USER_REQUEST before expiry; after end mark reason EXPIRED (no second stock release); preserve approvedAt.
APPROVED expired -> expiry cleanup -> CANCELLED/EXPIRED, effective hold already zero.
CANCELLED never-approved -> delete allowed when approvedAt null.
CANCELLED historically approved -> never physically delete.
RETURNED, CONSUMED and ISSUED cannot cancel/delete through current API.
A09 withdraw consent changes no stock lifecycle status; clears phone and sets smsConsent=false; repeated revocation on existing record harmless.
All prohibited transitions return controlled 409 without change.

### Guarded Resource correction

Request: name,totalQuantity,expectedTotalQuantity. Lock Resource and check actual total==expected, else 409 STALE_RESOURCE_QUANTITY. Compute H/I at same time after lock. For reusable new T >= H+I; consumable new T >= H. Proposed total in 0..1e6. Preserve type. On lost HTTP response, do NOT blindly retry modifying POST, because commit might have succeeded. Re-fetch and reconcile.

### Guarded Requirement reduction

Require 1..1e6 and Project not ended. Lock Project then Resource, obtain accurate covered and MAX single live PENDING quantity. New Q >= max(Covered,maxPending). Pending sum not used since competing pending requests do not reserve demand. A historically CANCELLED/never-approved Allocation does not constrain Q after cancellation.

### Protected Project dates

If either date changes: new start future, new end>new start, no Allocation with nonnull approvedAt under any Requirement of Project. This preserves historical approved context even if previously approved Allocation now CANCELLED. Name-only update may preserve old date, subject to name validation. No department change.

### Deletion constraints

Department: no Projects.
Project: no Requirements.
Resource: totalQuantity==0 and no Requirements.
Requirement: no Allocation records.
Allocation: status PENDING or CANCELLED with approvedAt==null.
The database enforces foreign-key integrity; components return domain conflicts when known, and must handle DB integrity races without inappropriate 500 if recognized.

### Historical limitations and truthful UI descriptions

No partial return/damage, physical verification, stock movement ledger, serialized devices, procurement or multi-org ownership. A RETURN is officer-recorded usable whole-batch receipt. No real authentication or identity verification. A Resource total correction lacks event ledger; Git history is not data audit. Clearly disclose in About/Demo.

### Invariant IDs for tests

INV01 Resource total nonnegative.
INV02 Resource availability never negative.
INV03 Requirement coverage <= demand.
INV04 Allocation quantity immutable.
INV05 ISSUE once.
INV06 RETURN once.
INV07 CONSUME once.
INV08 RETURN only after valid ISSUE.
INV09 RETURN removes custody, not historical coverage.
INV10 CONSUME deducts physical total exactly once.
INV11 CANCELLED never reserves.
INV12 approvedAt remains historical.
INV13 expired unfulfilled approved hold zero.
INV14 overdue ISSUED custody remains unavailable.
INV15 stale total correction refuses overwrite.
INV16 business errors/rollback do not partially commit.
INV17 Project date edit doesn't alter historical approved periods.
INV18 FK references valid.
INV19 restart persistence.
INV20 scheduler one automatic claim per Allocation, not guaranteed handset delivery.
