# Extended C6a phase report

November 19, 2026, Asia/Manila

## Authority and preflight

- Branch `feat/extended-c-c6a-independent-acceptance-2026-11-19` starts at policy `29efe86298431db16a780ae2e582b2276a699530`. The immediate parent of the policy tip is documentation commit `819bace525c4ff08235793766804735157b8a711`. This policy branch adds the gate and AGENTS reference across two commits and descends from C5 `9755ef63bd4c02a7042cb48823bf759e609357cd`. Worktree was clean before the switch. [Policy PR #19](https://github.com/newonathe/OrgResource/pull/19) [C5 PR #18](https://github.com/newonathe/OrgResource/pull/18)
- The C5 package was built from `14576b57fff44c6f0112fd37d8c237ca0122efb4`, verified against its hashes and ZIP/JAR CRCs again in C6a. Relevant remote branch tips, stacked PRs and historical CI were checked. [C5 source CI](https://github.com/newonathe/OrgResource/actions/runs/44342137298) [Policy CI](https://github.com/newonathe/OrgResource/actions/runs/44384085249)
- Protected Course A is `7f8c0746da18fd54b47f430ac1435ac36e605d67`. Its backend tree is `f2b522f88214a9cfba908cac6b0bcda70171875c`; its frontend tree is `052e5dbc5a0e84823c7dda06ccee83f27b1818c9`. Both matched the policy baseline. The original academic collection and Flyway V1–V5 stay separate from the new work.
- The source hierarchy is the assignment and syllabus, the submitted proposal, source snapshots, and explicit user-approved minimum C6a contract. The course authority was read from PDFs and proposal text. The transcript excerpt and corpus paths are recorded with bounded review depth. Unattached video frames and current Canvas were not inspected.
- Research authorities came from CP34–36, CP41, and CP44–49 at `dfda6434450c3e57a621bf23373ce6f4f68aa148`, with the earlier academic/C3 specification at `4a137b8acb4d1dda486bf03a4151835613240fd5`.

## Working contract and evidence index

Reporting reads the two immutable fact families with current role and workspace checks. The exact API, byte and field contracts are documented in [Extended C reporting](../../docs/EXTENDED_C_REPORTING.md). Resource, Movement and Audit are the supported exports. Requirement and Allocation export privacy, borrower linkage, assigned LEAD visibility, retention and production hosting are separate owner decisions.

The academic model remains Department, Project, Resource, ResourceRequirement and Allocation. Project references Department, ResourceRequirement references Project and Resource, and Allocation references ResourceRequirement. Java 8 retains the original Spring/Jersey component delegation, Javax Validation, JPA/JPQL, MySQL, Retrofit and Gson. Extended retains the approved Java 21-compatible runtime and actual Javax/Jakarta validation with additional identity, session and ledger records. The academic operation count is 25 original operations plus consent withdrawal and two image operations. The modern reporting paths use separate tenant DTOs. Read the two-edition [route matrix](REQUIREMENT_TO_TEST_MATRIX.csv) for operation-level request, response and execution mapping.

- [Source coverage register](SOURCE_COVERAGE_REGISTER.csv)
- [Requirement to test matrix](REQUIREMENT_TO_TEST_MATRIX.csv)
- [C5 file-by-file audit](FILE_BY_FILE_AUDIT.md)
- [Socratic findings and falsifiers](SOCRATIC_FINDINGS_AND_FALSIFIERS.md)
- [Full Extended scope gap and conflict register](FULL_EXTENDED_SCOPE_GAP_REGISTER.md)
- [Independent acceptance receipts](INDEPENDENT_ACCEPTANCE.md)

## Changes and security boundary

- Backend: `ReportingResource.java`, `ReportingComponent.java` and `C1JerseyConfiguration.java` register and implement the three tenant reports. `C1IdentityAcceptanceTest.java` adds role, cursor, failed-query, bounded export, CSV and snapshot checks on actual MySQL. `C1Application.java` and `C1SecurityConfiguration.java` separate offline bootstrap from servlet security and scheduled workers.
- React: `App.tsx` and `AcademicShell.tsx` add scoped navigation. `api/client.ts`, `api/extended.ts` and `pages/ReportingPages.tsx` handle real reads and downloads. `e2e/orgresource.spec.ts` covers the connected reporting journey, generated files, responsive widths and keyboard activation.
- Operations: `extended-c-backup.ps1`, `package-extended-c.ps1`, `verify-extended-c1.ps1`, `verify-extended-c6a-operator.ps1` and `run-extended-c3-newman.ps1` implement staged publication, scoped protected-login recovery and both Extended Newman gates. The new reporting collection is separate from the 28-route compatibility collection. `.github/workflows/verify.yml` adds independent frozen Course A and Extended runtime checks. The release and reporting manuals plus these seven evidence files document the changes.
- OWNER and ADMIN require an active account, active membership and explicit selection to read audit or activity. The export POST also requires current CSRF and same origin. The queries are bounded and scoped, and the runtime database account is granted append-only access to the two fact tables. Actor and target internal identifiers and all contact/identity secrets are excluded. Event IDs encode the fact source and row number and are public references without authorization value. Sequential event numbers can reveal coarse global growth across gaps. Hiding that signal would require a separate event reference contract.
- The row-cap negative case uses 1,001 or more actual rows. Current bounded field widths keep every row well under 4,000 bytes even with escaped UTF-8 or JSON/CSV delimiters, so 1,000 rows fit below 4 MiB. The separate byte check remains capped as a guard on future schema changes. No current fixture can reach that branch without changing the accepted field contract.

## Defects and falsification

- The earlier packaged JAR did not complete a fresh offline-bootstrap probe within its initial timeout. A subsequent controlled probe identified servlet-only security attempting to initialize without an HTTP server. The narrow web condition preserves runtime security and lets the bootstrap process exit. The operator drill also checks that a refused second bootstrap leaves expired receipt fixture rows untouched.
- A missing protected-login entry formerly risked an unintended connection fallback. A partially privileged identity could see only part of the schema. The scripts now check the named login and all expected tables. The test deliberately creates a partial SQL import on a separately named disposable target.
- Earlier packaging wrote directly to the final destination. The new package uses a sibling staging directory and publishes only after validation. An invalid child JDK path on a committed implementation caused exit 1. The final destination and marker stayed absent.
- A dual member can already see their organization list, but the new reports use selection before evaluating the path role. The added HTTP/MySQL regression fixes the earlier 403 for a different workspace where the owner held only MEMBER. Such a request returns 404 on all three routes. The selected-workspace role test still requires 403 for nonstaff.
- A first local protected-login restore successfully started the restored JAR and signed in with Chromium. The subsequent database outage probe timed out at 25 seconds and the packaging gate withheld the candidate. The final verifier allows 75 seconds to observe the real HTTP 5xx and still requires the same Java process to recover after MySQL restarts.
- The frozen Course A reference collection is explicitly documented as unordered in `postman/README.md`. Making its Manila pre-request script Newman-compatible exposed the expected sequence conflict in CI. D04 deleted the record later passed to P01, which returned 404. Removed the temporary adapter and used the six existing ordered acceptance collections in independent frozen Java 8 CI. The reference collection, environment and frozen Java and React trees were preserved. [First hosted attempt](https://github.com/newonathe/OrgResource/actions/runs/44392680547) [Second hosted attempt](https://github.com/newonathe/OrgResource/actions/runs/44393852292)

## Runtime and release receipts

Implementation source `dcd903ce78e7dab869f67da0cd029f98b3b7c405` was pushed to the verified feature branch. The policy remains the immediate stack base. The artifact uses this source revision, while any subsequent evidence-only head is visible in the draft PR. [Draft PR #20](https://github.com/newonathe/OrgResource/pull/20) [Exact-source hosted CI](https://github.com/newonathe/OrgResource/actions/runs/44396603940)

The separate [acceptance receipts](INDEPENDENT_ACCEPTANCE.md) record the exact command outcomes, evidence locations, artifact SHA-256 values and independent environment boundaries.

All three hosted jobs passed. Clean Windows extraction passed 47 Java 21/MySQL tests, 24 Vitest tests, 60 Extended Newman requests with 110 assertions, 12 packaged Chromium journeys, the standalone Vite test and the fixture-root restore smoke. The separately scoped operator passed the 28-operation compatibility collection again, actual first-run and repeat-bootstrap checks, protected restore, HTTP 500 during MySQL outage, authenticated recovery in the same Java process, and refusal of an oversized dump. Frozen Course A independently passed 61 Java 8/MySQL tests, 100 Newman requests with 151 assertions, and its packaged Chromium smoke. These counts refer to distinct runs where indicated in the receipt.

The local candidate was staged and validated before publication to `../deliverables/OrgResource-Extended-C6a-2026-11-19-dcd903c`. Its source file bytes match Git for all 166 ZIP entries representing files. SHA-256, CRCs, required JAR assets, migrations and the READY marker were verified after the atomic move. The exact-source failure injection left the final path absent. The ZIP contains the archived evidence snapshot at the implementation SHA. The newer Git evidence describes the verification of those bytes.

Fresh full and production-only npm audits returned zero findings from the unchanged Extended lockfile on November 19, 2026. Picomatch remained 4.0.7. The earlier advisory is now marked withdrawn. This updates the advisory status and does not establish a source patch or a production release decision. [GitHub advisory](https://github.com/advisories/GHSA-fc5q-39xc-vjpf)

Primary external sources retrieved November 19, 2026 were the MySQL 8.0 login-path documentation and RFC 4180 CSV definition. [MySQL 8.0 reference](https://dev.mysql.com/doc/refman/8.0/en/mysql-config-editor.html) [RFC 4180](https://www.rfc-editor.org/rfc/rfc4180#section-2)

## Remaining risks and decision ownership

- **P0.** All enumerated local C6a acceptance gates executed successfully on the implementation revision. The project remains a local release candidate.
- **P1 for broader product release.** Borrower-to-account linkage, Project LEAD assignment and row-level access, Requirement and Allocation export redaction, retention, production HTTPS, protected off-host restore and live provider approval are owner dependencies. The [gap register](FULL_EXTENDED_SCOPE_GAP_REGISTER.md) identifies storage and integration consequences.
- **P2.** Event references can reveal coarse growth by sequence gaps. Broader governance UI, searchable history, assistive technology and physical-device review remain open.
- The internal second pass used source and independent execution receipts. A separate human review was unavailable. The current Canvas requirements, sign-off and academic actions are student-owned.

## Next dependency once authorized

> Continue from the verified OrgResource C6a feature head and its evidence files. Read AGENTS.md and docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md first. Do not reduce audit depth or acceptance criteria to satisfy time/token limits. Complete all safe possible work and mark anything blocked or not run. Close any mandatory C6a gaps before approving a later phase. Use the Full Extended Scope Gap Register to propose the smallest explicitly authorized next batch. Present borrower identity, Project LEAD assignment, scoped reads and retention as decisions with real schema and authorization implications. Preserve Course A 7f8c0746da18fd54b47f430ac1435ac36e605d67, Flyway V1–V5, and the original academic contract. Use disposable MySQL, fictional fixtures, actual packaged HTTP and Chromium, exact committed source evidence, and a draft PR against the verified parent. Stop at the approved boundary and report independent checks and remaining uncertainty.

Student-only obligations remain current Canvas/rubric/deadline verification, original Course A demonstration and recording, instructor access, authorship disclosure and any peer or final submission steps.

**C6a VERIFIED — All enumerated mandatory C6a requirements and execution gates pass on the exact final implementation SHA; explicitly identified broader product work remains pending.**
