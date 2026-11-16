# CP39 — New direct CI evidence and last material pre-Codex contract corrections

**As-of:** 2026-11-16 PHT. **Status:** verified source/log facts and **RECOMMENDED** remediation; no test run, Java/SQL implementation, modern compatibility proof, branch merge, Codex execution or application deployment. This addendum is deliberately narrow: it addresses specific verifiable defects discovered **after** [CP38](65_CP38_INDEPENDENT_PRE_CODEX_FREEZE_REVIEW_AND_OPEN_EVIDENCE.md). It does NOT reopen the entire project's research scope.

## 1. Exact independent CI evidence: a narrow, reproducible symptom

**VERIFIED GITHUB FACT:** two GitHub Actions runs on **the same earlier source SHA** `f224e000361bb2214a59234634faa784855f1e61`:
- [failed push run 43732859018](https://github.com/newonathe/OrgResource/actions/runs/43732859018), Java/MySQL job `132868965724`, test summary **55 tests, 1 failure, 0 errors/skips**.
- [successful PR run 43732864033](https://github.com/newonathe/OrgResource/actions/runs/43732864033), Java/MySQL job `132868981895`, same SHA, test summary **55 tests, 0 failures**.
- Failure line: `ResourceListAggregationMySqlTest.oneClockSampleAndOneGroupedQueryProduceExactAvailabilityAtProjectEndBoundary:107 expected:<5> but was:<8>`. Both jobs used MySQL image `mysql:8.0.46` (actual logs), on GitHub-hosted runners, with repository-root `.github/workflows/verify.yml` and Maven test.
- The **later protected O14 baseline** SHA `2dd336ed39cecd417a8bce3388b303e688e95dee` is **not** the same SHA as `f224e...`. Later green O14 CI cannot establish why the earlier identical-SHA push/PR outcomes differed. Do not imply this flaky test is permanently fixed.

**VERIFIED TEST SOURCE** at O14 path `backend/src/test/java/app/components/ResourceListAggregationMySqlTest.java`: real MySQL `@SpringBootTest`, `@MockBean Clock philippineClock` fixed at 2026-11-15 12:00 Asia/Manila, two reusable Resources (target T=10 and untouched T=7) and one consumable Resource (T=20); test's target REUSABLE T=10 has **active APPROVED q3**, **ISSUED q2**, and a second APPROVED q4 whose project ends at the exact sampled time. Expected H=3,I=2,T=10, available=5. Failure available=8 is **consistent with H3 being missed**, but the returned value alone does not prove query predicate, JPA flush, mock or test fixture failure. Source `ResourceComponent.list` uses one clock sample and grouped `AllocationRepository.sumResourceCommitments` JPQL.

**VERIFIED TEST PROFILE** `backend/src/main/resources/application-test.properties`: `orgresource.expiry.scheduler-enabled=false` and `orgresource.reminders.scheduler-enabled=false`; scheduled jobs are **not a leading hypothesis** for this specific CI test. Test code explicitly calls `reset(philippineClock)` and stubs `instant` and `getZone` before the list; but this alone does not prove what Hibernate actually bound.

**SOURCE-FOCUSED FUTURE INVESTIGATION, NOT PERFORMED:**
1. Pin the actual prior SHA `f224e...` and the later O14 SHA; record exact test class/test ordering and Maven Surefire versions rather than mix logs.
2. Run this one test in an isolated MySQL 8.0.46 database repeatedly, then a clean full 55-test suite, then randomized order, exact JVM/build options; avoid just rerunning green.
3. Within test transaction, `flush` and `clear` JPA before list, then explicitly select all three approved/issued rows and `Project.endAt` **from the database**, not only in-memory entities; check q3 active, q4 ending now, q2 issued.
4. Log/inspect the single injected instant, resulting `LocalDateTime`, JVM/server/session timezones, generated SQL/JPQL and bound timestamp parameters under secure TEST logging; compare the isolated query for the resource to the grouped query.
5. Assert `sumEffectiveHolds(reusable,APPROVED,NOW) ==3`, `sumIssuedCustody(reusable,ISSUED)==2` and grouped query columns H=3,I=2 **before** `ResourceComponent.list`; identify which layer first diverges. In later rewrite, use explicit `Clock.fixed` and deterministic time fixture where suitable.
6. Check for `@MockBean` shared Spring-test context state contamination, repository fixtures leaked from prior classes, stale Hibernate persistence context and any apparent dual transaction visibility. Do not assume any one is the root cause.
7. If a cause is found, make a minimal isolated fix and demonstrate identical failing case now PASS in repeated push/PR with preserved test expectation. No deleting the test, changing 5→8, or hiding a failure by `@Disabled`/retry alone.

**Result class:** evidence **diagnostic refined**; **ROOT CAUSE STILL OPEN**, **FIX NOT CLAIMED**.

## 2. Circular pre-build gate corrected by evidence-based sequencing

**CRITIQUE:** old CP31 UC08 and CP37 S1 appeared to require **all 26 protected original routes fully built and tested** before full feature implementation phases S3–S5. That makes an “isolated feasibility spike” equivalent to prematurely implementing the product.

**RECOMMENDED CORRECTION COMMITTED:** S1 proves only *minimal supported Java/Spring/Jersey/JPA/MySQL/Javax/Jakarta/security/Retrofit integration* with five representative **real routes**, plus exact static identification of the 26 real original source signatures. S1 can prove one DB is shared by representative original/new APIs. S4 does full inventory transaction/race tests; S5 implements and tests all 26 actual handlers and complete Newman's collection. No course feature is cut; execution evidence is deferred to its logically possible stage. See updated [CP31 §9](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md) and [CP37 S1/S5](64_CP37_SEALED_STAGE_BY_STAGE_CODEX_PROMPTS.md).

**S1 GO criteria:** valid supported actual effective dependency graph, one genuine Jersey `@Path` per academic entity family, Spring `@Autowired` Controller→Component, MySQL five entities/one JPQL, literal `javax.validation.Validator` invalid DTO, actual JPA invalid entity behavior documented as automatic or explicit, concurrent `jakarta` validation+session/CSRF, representative Retrofit/Gson real HTTP FakeTwilio and scheduler, minimal shared-DB state transition, no public unsupported runtime exposure. Anything untested marked PARTIAL. Full 26 POSTs, UI, deployment and production operations remain **NOT RUN** until S5–S8.

Official facts current Nov 16, 2026: [Spring Boot Jersey auto-configuration](https://docs.spring.io/spring-boot/reference/web/servlet.html) explicitly uses `jakarta.ws.rs`, Spring `@Component` and `@Autowired`. [Hibernate Validator 9.1](https://hibernate.org/validator/releases/9.1/) targets Jakarta Validation 3.1/Boot4.1; [Validator 6.2](https://hibernate.org/validator/releases/6.2/) keeps `javax.*` API namespace but is limited-support. This supports the **need for a real local feasibility test**, not a promised dual-runtime result. Do not call an explicit `javax` service validation check “native automatic ORM lifecycle.” The printed assignment requires Javax Validation but doesn't literally mandate a specific JPA lifecycle event; no instructor direction is invented.

## 3. Public CreateAllocation lacked its required borrowerNameSnapshot

**SOURCE-VERIFIED CONFLICT:** the original O14 `Allocation` entity has `borrowerName` **non-null**, required by academic A01 free-text DTO. The proposed modern `allocation.borrower_name_snapshot` is also mandatory, but CP35's public CreateAllocation DTO previously accepted only `requirementId,quantity,borrowerMembershipId?`, leaving no defined source for the snapshot.

**RECOMMENDED RESOLUTION COMMITTED:**
- **Legacy protected A01** retains required `borrowerName` supplied as free text; stored as snapshot but **not** authenticated principal identity.
- **Public v2 reusable A01:** requires ACTIVE same-org `borrowerMembershipId`; server derives `borrowerNameSnapshot` from that member's current explicitly non-verified fictional display label under transaction, stores an immutable bounded snapshot. Never trust client-supplied `borrowerNameSnapshot` to impersonate borrower.
- **Public v2 consumable A01:** if optional verified borrower membership is present, derive same way; otherwise default snapshot to current authenticated requester's display label, not an invented third-party borrower.
- Name label is **not verified legal identity/Ateneo affiliation**; future display name edits do not rewrite historical snapshot; snapshot is private to permitted project/staff and redacted for ordinary unrelated members.
- The server must ensure nonblank normalized 1..100 characters; if no acceptable display label, refuse 422 rather than store NULL or synthetic “Unknown Verified Student.” Add `ALLOCATION_NAME_01` test to verify each branch and malicious injected field rejection.

Updated [CP35 DTO](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md) and [CP34 allocation schema](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md). No actual code or SQL migration written.

## 4. Essential source coverage limitation that still has no new byte-level result

A previous independent archival report [22](22_PRIMARY_ARCHIVE_VERIFICATION.md) states byte/CRC checks on 1,227 outer non-video source file entries + 1,370 nested file entries from 21 ZIPs. Its generated two full CSV path/hash manifests were **not committed** to the private GitHub tree. Historical dossier count 1,241 including 15 raw video paths suggests 1,226 non-video paths but actual ZIP report says 1,227. This review attempted direct original ZIP programmatic access but container/Python returned `ClientError`; **no new file hash, exact extra path, or complete path-set diff** established. Existing [CP24 source reconciliation contract](46_CP24_RELEASE_AND_ORIGINAL_ARCHIVE_PROVENANCE.md) remains open; possible auto-generated manifest member is an unverified hypothesis, not resolution.

**Raw video limitation:** only timestamped audio transcripts present; not one raw video frame watched/validated. Do not ask user to reupload originals routinely; where a critical uncorroborated visual is needed, cite the precise audio timestamp and ask student to inspect local original.

## 5. Bounded remaining review work versus evidence gates

**PAPER NOW RECONCILED:** original five JPA classes/tables and 26 current route signatures; one-product domain/inventory arithmetic, real auth/invite/role/public route concept; future-vs-active Project; exact original bodyless transitions vs durable ledger; no fake legacy import; newly explicit borrower snapshot; bounded feasibility experiment; CI flaky defect narrowed.

**Cannot be paper-concluded:** actual source ZIP +1 path diff, real supported Javax/Jakarta entity validation, O14 grouped-query flake cause, Canvas rules, R04 course-equivalence waiver, new 18-table SQL/identity implementation, provider/free hosting account, clean DR/CI/CD, true video/group/COA/Canvas submission. Keep these as distinct source/execution/instructor/owner gates; don't issue another generalized research document solely to repeat them.

**Authoritative next planning read order:** CP31 → CP32 → CP33 → CP34 → CP35 → CP36 → CP37 → CP38 → **CP39**; CP39 is the latest direct-evidence correction only where explicit. This is a **single controlled paper baseline**, not nine competing products. Codex prompts remain SEALED and untouched by user actions.

**Sign-off:** document owner approval remains open. Research-only artifacts are not the user's signature or instructor approval; no broad Codex handoff or deployment authorization is implied.
