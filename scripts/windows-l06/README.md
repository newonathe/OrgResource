# Native Windows mixed rehearsal harness (Report54, verification only)

Separate, additive port of `scripts/verify-c9-local-mixed-preparation.sh`. It adds files only below `scripts/windows-l06/`.
The application, migrations, Linux harness, Course A and Windows security settings are unchanged. No WSL, Docker, VM, Windows
Sandbox, administrator right, firewall rule or service change is used. Windows Sandbox is not available on Windows Home.

**Status: engineering proof on fictional fixtures. Not accepted, not independent review, not L06, not off-host (L07).**

## Control equivalence

| Linux control | Windows control | Proven on fictional data by |
| --- | --- | --- |
| Nonroot UID | Not administrator, not SYSTEM; children have no elevation | preflight, `c9_windows_guard.contained_nonadmin` |
| `--network none` + netns per server, loopback only | One AppContainer **per server role** with **zero capabilities** (kernel WFP). Checked every run: TCP v4 denied (v6 reported `denied` or `no_route_not_proven`), UDP undelivered, direct resolver unanswered and system resolver unresolved (each reported `not_proven` without its host control), LAN denied, container to host and host to container loopback denied (probe, MySQL, CDP, application ports), all six role pairs denied, other roles' files unreadable, runtime read-only | `Assert-Containment`, `Assert-HostCannotReach`, with connect, receive and router positive controls |
| Container ID, labels, namespace identity | Held process HANDLE, exact AppContainer SID, exact Job Object, zero capabilities, listener PID equals the held process, `@@server_uuid`, datadir | `Assert-Role` before every statement |
| PIDFD stop, no bare PID | Termination by held HANDLE after `Verify`; no PID is ever signalled | `Invoke-Runtime` finally |
| Container removal, reap ordering | Job Object kill-on-close, no breakaway. Breakaway refused; unrelated sentinel survives; killing only the runner ends every child; `Verify` refuses four wrong shapes | `R06B1_JOB_OBJECT_OK_WIN`, `R06B1_VERIFIER_REFUSALS_OK_WIN`, `R06B1_INTERRUPTION_OK_WIN` |
| Modes 0700/0600, owner UID | Protected NTFS DACL containing only the owner (and, where required, that run's own container SID). Reparse, UNC, relative and `..` paths refused and tested; cloud, offline and non-NTFS refusals are coded but not tested | negative matrix |
| `C8HostedOriginalBaseline` POSIX 0600 | Unmodified JAR. `OwnerOnlyPosixProvider` (a JVM default file-system provider for the baseline command and for the two import commands that call it) reports `rw-------` only when owner and decoded DACL are owner-only. The JDK cannot decode conditional ACEs, so .NET re-checks the full DACL before every trusting command of the owner run; the fixture proves the same check refuses a widened manifest | self-test 5/5, widened-ACL refusal by the unmodified command, fictional additive import under the provider |
| C5 `LocalOriginal/LocalMixedAcceptance` over Docker | Unmodified `extended-c-backup.ps1` in its production `ProtectedLogin` mode with least-privilege loopback users, inside the role container. The Docker modes' identity, fixture, plan, size, digest and custody checks run in the harness before both operations | archive refusals, exact-row restores |
| Three servers, distinct UUIDs | Three fresh datadirs in three containers | bootstrap, third-server fixture and third-server recovery |
| API/browser/SQL oracles | Same pinned blobs; `derive_oracles.py` applies 20 counted guard/attach edits, records source and derived SHA-256, and fails on any other shape. Chromium attaches over loopback CDP because Node pipes are unavailable in an AppContainer | public control, aggregates, refusals |
| Three TTYs, exact challenge, 300 s, CI refusal | Same, plus typed-ahead discarded and an **advisory** agent-session refusal (environment based, not a security boundary) | `test_gate.py` (reader echo, matcher, refusal cases; the approval function is run only where it must refuse) |
| Approved plan size/SHA/mode, snapshot, recheck | Same, plus `WINDOWS_HARNESS_ACCEPTED.txt` bound to the exact harness commit and checked before any plan byte is read | custody unit cases in the negative matrix (fictional bytes) |

A temporary per-session `subst` drive points at the restricted work directory because InnoDB queries its volume root. It is removed in cleanup.

## Residual differences a reviewer must accept or reject

1. All roles share the host TCP port space; isolation between them is by container SID, not by separate network stacks.
2. Windows administrators and SYSTEM can override any DACL, as root can on Linux.
3. `lower_case_table_names=1` on Windows; row comparisons are Windows-to-Windows only.
4. The POSIX provider and the derived oracle edits are new trusted code.
5. `stage_approved_import.ps1` has no public positive: it needs the exact private plan and the owner. The fixture executes its C5 transport, projection, builder refusal and runtime helpers on fictional originals, and the importer bean on a generated fictional plan. The private API and browser oracles run only to their intake refusal, as on Linux. The exact hosted import, 96-workspace postflight and private rows run first in the owner's run.
6. The Linux runtime JVM gets SIGTERM then SIGKILL at 480 s. Here the bound is enforced between steps and the stop is `TerminateProcess`.
7. The derived browser oracle takes containment from the runner (`C9_WINDOWS_CONTAINED`); the Python oracles read the real token.
8. After a hard interruption, owner-only files remain for inspection (datadirs, archives, login file, plan copies, scratch SQL). The owner disposes of the work directory.
9. Every run sends one DNS question from the host to the local router and one through the system resolver as positive controls. Contained name resolution is probed through c-ares and the system resolver. Other brokered Windows services were not enumerated.

## Run

Fixture (fictional only) and negatives: `verify-c9-local-mixed-windows.ps1 -Mode FixtureOnly ...`, `test-c9-local-mixed-windows.ps1 ...`.
After an interruption: `recover-c9-windows.ps1 -Root <root> -Token <12 hex>` (removes the mapping and profiles, keeps the files).

`-Mode ApprovedImport` is for the owner only, in their own terminal, after the harness commit is reviewed and accepted. The owner
(not an agent) places `ACCEPTED <harness commit>` in `WINDOWS_HARNESS_ACCEPTED.txt` inside the private root, then reviews the real
receipt and types the generated challenge. Required: the three `_WIN` result markers, `R06B1_CLEANUP_OK`, `exit=0`, `phase=cleanup`. Any failure is final for
that run: fresh servers next time, never a retry on a partial target.
