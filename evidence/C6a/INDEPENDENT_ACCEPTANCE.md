# C6a independent acceptance receipts

## C5 artifact recheck

Command set on the Windows workstation was `Get-FileHash -Algorithm SHA256` for every entry in the C5 checksum list, followed by Python `zipfile.ZipFile.testzip()` on both the source ZIP and executable. All hashes matched. Both CRC scans returned no failed entry. The manifest identifies `14576b57fff44c6f0112fd37d8c237ca0122efb4`. The current policy is a descendant of the C5 report head, and both protected Course A Git tree IDs matched.

## Acceptance evidence levels

- The initial local command `pwsh -NoProfile -File .\scripts\verify-extended-c1.ps1 -VerifyRecovery` used the guarded integration environment. The observed worktree run executed 47 Java 21/MySQL tests, zero failures/errors/skips, 24 frontend units, 48 legacy Newman requests with 90 assertions, 12 packaged Chromium tests and one standalone Vite test. These are worktree observations and not the final committed source receipt.
- The independent operator command is `pwsh -NoProfile -File .\scripts\verify-extended-c6a-operator.ps1 -JarPath <JAR> -Python <Python with test-only pywinpty>`. It uses genuinely separate loopback MySQL processes and scoped fixture users. It invokes the actual CLI bootstrap with stdout private. The C5 binary timed out at bootstrap. A subsequent C6a worktree binary returned an error on fresh bootstrap. Earlier green integration tests did not exercise that path.
- Hosted Course A independently built the frozen sources with Temurin 8.0.512+9 and MySQL 8.0.46 using `bash mvnw -B -Pbundle-react-ui clean verify`. It ran **61 tests**, zero failures, errors or skips. The test source covers the five JPA entities, validation, JPQL, image handling, scheduling and simulator behavior. The JAR served the original React bundle, five list APIs and a refreshable Projects hash route in one passed Chromium journey. [Java 8 job 135122529687](https://github.com/newonathe/OrgResource/actions/runs/44395027563/job/135122529687)
- `pwsh -NoProfile -File scripts/run-api-acceptance.ps1` ran six frozen Course A collections through Newman 6.2.3 against that JAR. O1 15/29, O3 CRUD 20/30, O3 Department 11/19, O4–O5 27/27, O14 dependencies 23/37 and images 4/9. Total **100 requests and 151 assertions**, zero failures. The route matrix ties each of the 28 operations to these ordered fixtures. The 28-route reference collection and its environment are kept as originally delivered. Its Department folder passed four requests and eight assertions in the earlier hosted attempt before the full-reference sequence failed at P01. That reference is a route catalog with mutually exclusive lifecycles, as documented in the original Postman README.
- The committed `b5e526b6df4da5c9aaf28dc8f558502adf8b3e0e` clean extraction passed 47 Java/MySQL tests, Extended compatibility Newman 48/90, C6a reporting Newman 12/20, 12 packaged Chromium, one standalone Vite browser test and the fixture-root recovery drill. The independently hosted Extended job for that commit also passed. This is an earlier source receipt. [Earlier Extended hosted job](https://github.com/newonathe/OrgResource/actions/runs/44393852292/job/135118370558)
- The b5e526b package's separate protected-login drill exercised its own source and target schemas, scoped account restore and a new authenticated Chromium session from the restored data. Its further outage request timed out after 25 seconds. The staging verification failed, and no candidate was published to the intended destination. See the b5e526b staging directory's `VERIFICATION.log` for the timeout. The final verifier keeps the requirement to observe HTTP 5xx and uses a larger time budget.
- `pwsh -NoProfile -File <Temp>/c6a-dual-workspace-status-reproduction.ps1 -JarPath <b5e526b packaged JAR>` ran a new fictional loopback MySQL schema, real bootstrap, sign-in, CSRF and explicit selection. It reproduced HTTP 200 for the chosen owner workspace and 403 `FORBIDDEN` for a separate member-only workspace. The final database-backed regression requires 404 on that alternate workspace for audit, activity and exports. The disposable reproduction folder ended in `08572aa2743a40e5b0e4b2693f0f4a6e`.
- Python `zipfile` tested the final staged source's CRCs and ZIP comment, then independently reconstructed the Git blob hash for each of its 166 tracked files. All matched. The 102 Git-located source-register entries also matched their written revision/object pairs.
- With the exact final implementation checkout, `pwsh -NoProfile -File scripts/package-extended-c.ps1 -Destination <new disposable path>` ran in a child process whose `JAVA_HOME` pointed to a missing test path. It staged the source ZIP, failed the clean-extracted verification with exit 1, and left the final path and its READY marker absent. Raw local log `%TEMP%/OrgResource-C6a-FinalFailure-595a7ddd58d44390ac7a1de379483d44.log`. The temporary Git checkout was removed after the negative proof.

## Recovery cases and oracles

- Protected operator path, C6a §7 cases 1–5 and 8–10. Separate source and target MySQL data directories, alternate ports and generated schema-scoped accounts exercise native protected backup and restore. Missing login, insufficient grants, truncated and oversized containers, nonempty target and intentionally partial SQL are separate checks. The partial target must contain some but fewer than all 18 tables, and the verifier never uses it for the restored application.
- Fixture-root verification, cases 6–7 and 11–18. A different disposable MySQL data directory checks wrong and unavailable archive key and changed ciphertext before import. Table and foreign-key shapes, persisted tenant rows, audit and movements, stock, exact PNG bytes, encrypted contact and wrong contact key, and exclusion of JDBC sessions are compared against the source.
- Both restore paths, cases 19–22. The restored JAR accepts an authenticated Chromium session. Fixture-root verification exercises permanent receipt replay and checked stock totals. The scoped procedure adds MySQL outage and restart while Java remains running, plus an actual dump over the backup size limit. Provider mode and SMS are disabled in both recovery environments.

## Exact implementation receipts

Implementation `dcd903ce78e7dab869f67da0cd029f98b3b7c405`, November 19, 2026, Asia/Manila.

- **Hosted Java 8 course gate** ran Maven with `-Pbundle-react-ui clean verify`, passed 61/61 with zero failures or skips, then ran `scripts/run-api-acceptance.ps1` and the prescribed packaged Chromium smoke. Newman observed 100 requests, 151 assertions, no failures. The live React smoke passed. [Exact Java 8 job](https://github.com/newonathe/OrgResource/actions/runs/44396603940/job/135128067043)
- **Hosted Extended gate** ran `bash ./backend/mvnw -B -f extended/backend/pom.xml clean verify` with Temurin 21.0.13 and MySQL 8.0.46, passed 47/47 with zero failures or skips. Academic compatibility Newman passed 48 requests and 90 assertions. C6a reporting Newman passed 12 requests and 20 assertions. The packaged Chromium suite passed all 12 journeys. [Exact Java 21 job](https://github.com/newonathe/OrgResource/actions/runs/44396603940/job/135128067124)
- **Hosted original React** passed the independent `npm ci`, typecheck, test, lint and build job. [Original React job](https://github.com/newonathe/OrgResource/actions/runs/44396603940/job/135128066807)
- **Supplemental exact-JAR browser acceptance** used `pwsh -NoProfile -File <Temp>/c6a-reporting-connected-proof.ps1 -JarPath <exact extracted JAR>` and the accompanying `c6a-reporting-connected-proof.cjs`. Standard login, explicit selection and Resource creation ran against a new MySQL workspace. Chromium delayed a real activity response to verify loading, temporarily aborted a request to test error and retry, and switched to an empty workspace. The real mixed feed had two different source IDs at the same microsecond. Its first and second page returned Movement then Audit, and fresh authentication after Java restart returned the same IDs and UTC values. Local proof directory `%TEMP%/OrgResource-C6a-Operator-f1f65b4e6f9240c293eac363e0f353ae`.
- **Windows tooling** for clean extraction and operator proofs was Java 21.0.3, MySQL 8.0.46, PowerShell 7.6.9, Node 24.14.1, npm 11.11.0 and CLI Playwright. The protected-login verifier used test-only PyWinPty 3.1.2 for the native console prompt.
- **Visual sample.** Desktop Activity correctly identified the two fact sources and showed UTC timestamps. At 375 pixels, Exports showed labeled fields and its primary action with the mobile navigation in view. The first resize capture sampled the sidebar transition. A fresh run waited for the drawer to reach its hidden position with reduced motion and verified the download control before capturing. The stable sample and feed proof are under `%TEMP%/OrgResource-C6a-Operator-0edc2815a3f74dca9964c4ce115a41ab`. Automated 768- and 375-pixel layout checks supplement the samples. No physical-device or assistive-technology pass is claimed.

### Clean local release and separate operator

From the repository root, the packaging script archived the exact implementation commit. It built and tested from a fresh extraction with:

```powershell
$env:PYTHONPATH = Join-Path ([IO.Path]::GetTempPath()) 'OrgResource-C6a-WinPty-3.1.2'
pwsh -NoProfile -File .\scripts\package-extended-c.ps1 -Destination '<new C6a deliverables path>' -Python '<Windows Python with pywinpty>'
```

The command exited 0. It used clean ZIP extraction and internally ran `verify-extended-c1.ps1 -VerifyRecovery` and `verify-extended-c6a-operator.ps1`. The final log records 47 Java/MySQL tests with zero failures/errors/skips, 24 Vitest tests, Extended compatibility Newman 48 requests/90 assertions, reporting Newman 12 requests/20 assertions, 12 packaged Chromium journeys, one Vite browser test and one fixture-root restored browser smoke. The two Newman suites had zero failures.

The independent scoped operator ran the 48/90 compatibility checks again and two first-run Chromium journeys. It checked different migration and runtime users, one-time offline bootstrap, and refusal with the existing workspace and expired receipts intact. It exercised the 28 academic operations, stale Resource update refusal, a real image and stock receipt in Chromium, absence of duplicate movement, and a separate authenticated start after protected restore. Hosted and local integration tests cover unauthorized operations and role changes on the same implementation.

Protected-login backup and restore passed on two MySQL processes with separate data roots and generated scoped accounts. Both restoration paths passed with 18 expected tables. After target MySQL shutdown, the Java process stayed up and the CSRF request returned HTTP 500. Restarting the same MySQL data directory restored authenticated Chromium access in the original Java process. Invalid login, insufficient grants, truncated and oversized archives, nonempty target, partial SQL and a dump over the size cap each refused. The protected-login archive was 35,932 bytes with SHA-256 `fef6bf0fa6fc7f1dbac5988a3fc8d782ee838a7c8e125ff344ce234f4ab2a5e5`. The separate fixture-root archive was 4,509,564 bytes with SHA-256 `ebcb4fce79e6287c1ae9cbb548a01b9e99d7985bcef2d4285e7b0c37b016a456`. These are test snapshots, not release deliverables.

### Artifact integrity

Local directory `../deliverables/OrgResource-Extended-C6a-2026-11-19-dcd903c`. Keep the ZIP, JAR, verification log, manifest, checksums and READY marker together.

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| OrgResource-Extended-C6a-Source.zip | 398,835 | `675a6d59a9320eeb994aea3f5ea363e89104039cdb5aaded737d20d7c43e7729` |
| OrgResource-Extended-C6a-Java21.jar | 72,325,329 | `55dd31cb963ca2e49d0722379cb09fcd3f8b023335cc11df809b6e605cc8ac4e` |
| VERIFICATION.log | 38,401 | `059170741a3b49b5225b937f8510a73927280c70aaa3cee4de9bdc298ff58ad5` |
| RELEASE-MANIFEST.json | 1,193 | `4db66c55ca78e28e330311d0abdf6359981632db889ba03725d72df20012b8d2` |

An independent comparison after publication matched the four file hashes, the READY pointer to `SHA256SUMS.txt` (`fd8e95d4f5ef3fedd82087552d628c6dd89e66e803656f53ae60ab4eaddc6640`), both ZIP/JAR CRC scans, the source ZIP's commit comment, all five nested migrations, and React index, JavaScript and CSS in the executable. The approved candidate used Windows NT 10.0.26300.0. This independently validates contents and execution from the committed source, without claiming bit-identical rebuilds of the generated JAR on other machines.
- No different physical host, live provider, independent human review, instructor decision or Canvas action is represented by the local evidence.

The [phase report](PHASE_REPORT.md) records the final source SHA, individual command outcomes and scope verdict. The source and requirement CSVs provide the reconstruction index.
