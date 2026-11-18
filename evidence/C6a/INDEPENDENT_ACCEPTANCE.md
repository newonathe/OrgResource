# C6a independent acceptance receipts

## C5 artifact recheck

Command set on the Windows workstation was `Get-FileHash -Algorithm SHA256` for every entry in the C5 checksum list, followed by Python `zipfile.ZipFile.testzip()` on both the source ZIP and executable. All hashes matched. Both CRC scans returned no failed entry. The manifest identifies `14576b57fff44c6f0112fd37d8c237ca0122efb4`. The current policy is a descendant of the C5 report head, and both protected Course A Git tree IDs matched.

## Acceptance evidence levels

- The initial local command `pwsh -NoProfile -File .\scripts\verify-extended-c1.ps1 -VerifyRecovery` used the guarded integration environment. The observed worktree run executed 47 Java 21/MySQL tests, zero failures/errors/skips, 24 frontend units, 48 legacy Newman requests with 90 assertions, 12 packaged Chromium tests and one standalone Vite test. These are worktree observations and not the final committed source receipt.
- The independent operator command is `pwsh -NoProfile -File .\scripts\verify-extended-c6a-operator.ps1 -JarPath <JAR> -Python <Python with test-only pywinpty>`. It uses genuinely separate loopback MySQL processes and scoped fixture users. It invokes the actual CLI bootstrap with stdout private. The C5 binary timed out at bootstrap. A subsequent C6a worktree binary returned an error on fresh bootstrap. Earlier green integration tests did not exercise that path.
- The independent Course A gate is the Java 8/MySQL hosted CI job, including the frozen packaged JAR, original 28-route collection and original Chromium project. Project source freeze, historical reports and current CI results are different evidence.
- No different physical host, live provider, independent human review, instructor decision or Canvas action is represented by the local evidence.

The [phase report](PHASE_REPORT.md) records the final source SHA, individual command outcomes and scope verdict. The source and requirement CSVs provide the reconstruction index.
