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
- OWNER and ADMIN require an active account, active membership and explicit selection to read audit or activity. The export POST also requires current CSRF and same origin. The queries are bounded and scoped, and the runtime database account is granted append-only access to the two fact tables. Actor and target internal identifiers and all contact/identity secrets are excluded. Event IDs encode the fact source and row number and must be treated as public references without authorization value. Sequential event numbers can reveal coarse global growth across gaps. If hiding that signal becomes a requirement, use a dedicated design decision for stable opaque IDs and cursor rotation.

## Defects and falsification

- The earlier packaged JAR did not complete a fresh offline-bootstrap probe within its initial timeout. A subsequent controlled probe identified servlet-only security attempting to initialize without an HTTP server. The narrow web condition preserves runtime security and lets the bootstrap process exit. The operator drill also checks that a refused second bootstrap leaves expired receipt fixture rows untouched.
- A missing protected-login entry formerly risked an unintended connection fallback. A partially privileged identity could see only part of the schema. The scripts now check the named login and all expected tables. The test deliberately creates a partial SQL import on a separately named disposable target.
- Earlier packaging wrote directly to the final destination. The new package uses a sibling staging directory and publishes only after validation. An invalid child JDK path on a committed implementation caused exit 1. The final destination and marker stayed absent.
- The frozen Course A reference collection is explicitly documented as unordered in `postman/README.md`. Making its Manila pre-request script Newman-compatible exposed the expected sequence conflict in CI. D04 deleted the record later passed to P01, which returned 404. Removed the temporary adapter and used the six existing ordered acceptance collections in independent frozen Java 8 CI. The reference collection, environment and frozen Java and React trees were preserved. [First hosted attempt](https://github.com/newonathe/OrgResource/actions/runs/44392680547) [Second hosted attempt](https://github.com/newonathe/OrgResource/actions/runs/44393852292)

## Runtime and release receipts

Source candidate `63ad94c43d5230d9c9c1cbe68fa2b8db88c99947` was pushed to the verified feature branch. [Draft PR #20](https://github.com/newonathe/OrgResource/pull/20) [Exact-source hosted CI](https://github.com/newonathe/OrgResource/actions/runs/44395027563)

The separate [acceptance receipts](INDEPENDENT_ACCEPTANCE.md) record the exact command outcomes, evidence locations, artifact SHA-256 values and independent environment boundaries.

Fresh full and production-only npm audits returned zero findings from the unchanged Extended lockfile on November 19, 2026. Picomatch remained 4.0.7. The earlier advisory is now marked withdrawn. This updates the advisory status and does not establish a source patch or a production release decision. [GitHub advisory](https://github.com/advisories/GHSA-fc5q-39xc-vjpf)

Primary external sources retrieved November 19, 2026 were the MySQL 8.0 login-path documentation and RFC 4180 CSV definition. [MySQL 8.0 reference](https://dev.mysql.com/doc/refman/8.0/en/mysql-config-editor.html) [RFC 4180](https://www.rfc-editor.org/rfc/rfc4180#section-2)

## Remaining risks and decision ownership

- Public release dependencies remain owner decisions. Independent off-host encrypted restore, HTTPS and real-provider acceptance were outside this local run. No paid or live traffic was authorized.
- Pending product decisions are borrower-to-account linkage, Project LEAD assignment and record scope, export redaction for Requirements and Allocations, governance UI coverage, searchable history and retention. The [gap register](FULL_EXTENDED_SCOPE_GAP_REGISTER.md) identifies storage and integration dependencies. The user has authorized local C6a only.
- The internal second pass used the actual source and hosted and local receipts. A separate human review and a full accessibility audit were not performed. Open course interpretation, Canvas and academic sign-off are student-owned.

## Next dependency once authorized

> Continue from the verified OrgResource C6a feature head and its evidence files. Read AGENTS.md and docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md first. Do not reduce audit depth or acceptance criteria to satisfy time/token limits. Complete all safe possible work and mark anything blocked or not run. Close any mandatory C6a gaps before approving a later phase. Use the Full Extended Scope Gap Register to propose the smallest explicitly authorized next batch. Present borrower identity, Project LEAD assignment, scoped reads and retention as decisions with real schema and authorization implications. Preserve Course A 7f8c0746da18fd54b47f430ac1435ac36e605d67, Flyway V1–V5, and the original academic contract. Use disposable MySQL, fictional fixtures, actual packaged HTTP and Chromium, exact committed source evidence, and a draft PR against the verified parent. Stop at the approved boundary and report independent checks and remaining uncertainty.

Student-only obligations remain current Canvas/rubric/deadline verification, original Course A demonstration and recording, instructor access, authorship disclosure and any peer or final submission steps.
