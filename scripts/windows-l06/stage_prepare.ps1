# Dot-sourced stage: private work area, snapshots, containers, containment proof. No database exists yet.
Add-Type -TypeDefinition ([IO.File]::ReadAllText((Join-Path $PSScriptRoot 'C9Contain.cs')))
$token = New-Secret 6
$work = Join-Path $Root "orgresource-c9w-$token"
New-Item -ItemType Directory -Path $work | Out-Null
# Held open for the life of the run: recovery refuses while it cannot take this file.
$script:lock = [IO.File]::Open((Join-Path $work 'run.lock'), 'CreateNew', 'ReadWrite', 'None')
foreach ($role in 'source', 'restore', 'mixed') { New-Role $role }
Set-PrivateAcl $work @($roles.Values | ForEach-Object { @{ Sid = $_.Sid; Rights = 'ReadAndExecute'; Inherit = $false } })
foreach ($dir in 'rt', 'dumps') { New-Item -ItemType Directory -Path "$work\$dir" | Out-Null }
Set-PrivateAcl "$work\dumps" @()
Set-PrivateAcl "$work\rt" @($roles.Values | ForEach-Object { @{ Sid = $_.Sid; Rights = 'ReadAndExecute'; Inherit = $true } })
foreach ($role in @($roles.Keys)) {
    New-Item -ItemType Directory -Path "$work\$role" | Out-Null
    Set-PrivateAcl "$work\$role" @(@{ Sid = $roles[$role].Sid; Rights = 'FullControl'; Inherit = $true })
    New-Item -ItemType Directory -Path "$work\$role\Packages\$($roles[$role].Name)\AC\Temp" -Force | Out-Null
    if (-not (Test-OwnerOnly "$work\$role" @($roles[$role].Sid))) { Fail 'R06B1_PRIVATE_ROOT_REQUIRED' }
}
# InnoDB inspects its volume root. A per-session drive mapping makes that root this restricted directory; no system ACL changes.
$used = @([IO.DriveInfo]::GetDrives().Name | ForEach-Object { $_.Substring(0, 2) })
$drive = 'Q:', 'R:', 'U:', 'V:', 'W:' | Where-Object { $used -cnotcontains $_ } | Select-Object -First 1
if (-not $drive) { Fail 'R06B1_DRIVE' }
& "$env:SystemRoot\System32\subst.exe" $drive $work
if ($LASTEXITCODE -ne 0) { Fail 'R06B1_DRIVE' }
Copy-Item -LiteralPath $Jar -Destination "$work\rt\verified.jar"
if ((Get-Sha "$work\rt\verified.jar") -cne $JarSha256 -or (Get-Item -LiteralPath "$work\rt\verified.jar").Length -ne $jarLength) { Fail 'R06B1_JAR_HASH_MISMATCH' }
New-Item -ItemType Directory -Path "$work\rt\win", "$work\rt\repo\scripts", "$work\rt\repo\extended\frontend\node_modules\@playwright", "$work\rt\python", "$work\dumps\jc" | Out-Null
foreach ($f in 'containment_probe.cjs', 'breakaway_probe.ps1') { Copy-Item (Join-Path $PSScriptRoot $f) "$work\rt\win\$f" }
$derived = & $Python -I (Join-Path $PSScriptRoot 'derive_oracles.py') $repo $SourceSha "$work\rt\repo\scripts"
if ($LASTEXITCODE -ne 0) { Fail 'R06B1_ORACLE_DERIVATION' }
Write-Marker "$derived"
foreach ($f in 'c9_windows_guard.py', 'guard_probe.py', 'browser_preflight.cjs') { Copy-Item (Join-Path $PSScriptRoot $f) "$work\rt\repo\scripts\$f" }
Copy-Item (Join-Path $repo 'extended\frontend\package.json') "$work\rt\repo\extended\frontend\package.json"
foreach ($m in 'playwright', 'playwright-core') { Copy-Item -Recurse "$NodeModules\$m" "$work\rt\repo\extended\frontend\node_modules\$m" }
Copy-Item -Recurse "$NodeModules\@playwright\test" "$work\rt\repo\extended\frontend\node_modules\@playwright\test"
Copy-Item -Recurse (Split-Path $chromiumExe[0].FullName) "$work\rt\chromium"
$pyHome = (& $Python -I -c 'import sys; print(sys.base_prefix)').Trim()
foreach ($f in Get-ChildItem -LiteralPath $pyHome -File | Where-Object Name -Match '\.(exe|dll)$') { Copy-Item $f.FullName "$work\rt\python\" }
& robocopy "$pyHome\DLLs" "$work\rt\python\DLLs" /E /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { Fail 'R06B1_RUNTIME_COPY' }
& robocopy "$pyHome\Lib" "$work\rt\python\Lib" /E /XD site-packages test idlelib tkinter __pycache__ ensurepip /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { Fail 'R06B1_RUNTIME_COPY' }
& (Join-Path (Split-Path $javaBin) 'javac.exe') --release 21 -d "$work\dumps\jc" @(Get-ChildItem (Join-Path $PSScriptRoot 'java\c9win') -Filter *.java | ForEach-Object FullName)
if ($LASTEXITCODE -ne 0) { Fail 'R06B1_POSIX_PROVIDER' }
& (Join-Path (Split-Path $javaBin) 'jar.exe') --create --file "$work\dumps\posixfs.jar" -C "$work\dumps\jc" .
if ($LASTEXITCODE -ne 0) { Fail 'R06B1_POSIX_PROVIDER' }
Copy-Item -LiteralPath "$work\dumps\posixfs.jar" -Destination "$work\rt\posixfs.jar" # a copy inherits the rt ACL; a moved temp file would not
Assert-Containment
# The verifier itself must refuse: every case stays suspended, is checked, and is ended without running.
$selfTest = [C9Container]::SelfTest($roles.source.Container, $roles.restore.Container, "$env:SystemRoot\System32\PING.EXE", $work)
if ($selfTest -cne 'C9W_NOT_IN_JOB,C9W_NOT_APPCONTAINER,C9W_WRONG_CONTAINER,C9W_CAPABILITIES') { Fail 'R06B1_VERIFIER_SELF_TEST' }
Write-Marker 'R06B1_VERIFIER_REFUSALS_OK_WIN:not_in_job=true not_appcontainer=true wrong_container=true extra_capability=true'

# Job Object: a breakaway request must be refused and the whole tree must die, without touching an unrelated process.
$sentinel = Start-Process "$env:SystemRoot\System32\PING.EXE" -ArgumentList '-n', '120', '127.0.0.1' -WindowStyle Hidden -PassThru
try {
    $jobContainer = [C9Container]::new("orgresource.c9.$(New-Secret 6)")
    try {
        New-Item -ItemType Directory -Path "$work\jobtest" | Out-Null
        Set-PrivateAcl "$work\jobtest" @(@{ Sid = $jobContainer.SidString; Rights = 'FullControl'; Inherit = $true })
        New-Item -ItemType Directory -Path "$work\jobtest\Packages\$($jobContainer.Name)\AC\Temp" -Force | Out-Null
        Copy-Item (Join-Path $PSScriptRoot 'breakaway_probe.ps1') "$work\jobtest\probe.ps1"
        $log = "$work\dumps\job.log"; $home0 = "$work\jobtest"
        $p = $jobContainer.Start('job', "$env:ProgramFiles\PowerShell\7\pwsh.exe", @('-NoProfile', '-NonInteractive', '-ExecutionPolicy', 'Bypass', '-File', "$home0\probe.ps1"), @{ SystemRoot = $env:SystemRoot; windir = $env:windir; SystemDrive = $env:SystemDrive; ProgramFiles = $env:ProgramFiles
                PATH = "$env:SystemRoot\System32"; TEMP = $home0; TMP = $home0; USERPROFILE = $home0; LOCALAPPDATA = $home0; APPDATA = $home0; POWERSHELL_TELEMETRY_OPTOUT = '1' }, $home0, '', $log, $log)
        for ($i = 0; $i -lt 120 -and -not (Select-String -LiteralPath $log -Pattern '^ORDINARY=' -Quiet); $i++) { Start-Sleep -Milliseconds 500 }
        $lines = @(Get-Content -LiteralPath $log)
        if ($lines -cnotcontains 'BREAKAWAY=False/5' -or @($lines -cmatch '^ORDINARY=True/').Count -ne 1) { Fail 'R06B1_JOB_BREAKAWAY' }
        $treeBefore = $jobContainer.ActiveProcesses
        if ($treeBefore -lt 2) { Fail "R06B1_JOB_TREE_$treeBefore" }
        if (-not $jobContainer.KillAll() -or -not $p.Wait(10000)) { Fail 'R06B1_JOB_KILL' }
        $p.Dispose()
    } finally { $jobContainer.Dispose() }
    Remove-Item -LiteralPath "$work\jobtest" -Recurse -Force
    if ($sentinel.HasExited) { Fail 'R06B1_JOB_SENTINEL' }
    Write-Marker "R06B1_JOB_OBJECT_OK_WIN:breakaway_refused=true tree_before=$treeBefore tree_after=0 unrelated_sentinel_alive=true"
} finally { if (-not $sentinel.HasExited) { $sentinel.Kill() }; $sentinel.Dispose() }

# Owner-only POSIX provider: positive and negative, in the restore container, before it is trusted.
New-Item -ItemType Directory -Path "$work\restore\broad" | Out-Null
& "$env:SystemRoot\System32\icacls.exe" "$work\restore\broad" /grant '*S-1-1-0:(OI)(CI)R' /Q | Out-Null
[IO.File]::WriteAllText("$work\restore\broad\b.txt", 'x')
$log = "$work\dumps\posix.log"
if ((Invoke-Contained 'restore' $javaBin @('-cp', "$drive\rt\posixfs.jar", '-Djava.nio.file.spi.DefaultFileSystemProvider=c9win.OwnerOnlyPosixProvider', "-Dc9.acl.allowed=$($me.Name)|$($roles.restore.Sid)", 'c9win.SelfTest', "$drive\restore\posix.txt", "$drive\restore\broad\b.txt") @{} '' $log '' 60) -ne 0) { Fail 'R06B1_POSIX_PROVIDER' }
foreach ($m in 'C9W_POSIX_PRIVATE_CREATE_OK', 'C9W_POSIX_OVERWRITE_REFUSED_OK', 'C9W_POSIX_BROAD_ACL_REPORTED_OK', 'C9W_POSIX_BROAD_CREATE_REFUSED_OK', 'C9W_POSIX_WIDE_MODE_REFUSED_OK') { if (-not (Test-Line $log $m)) { Fail 'R06B1_POSIX_PROVIDER' } }
# The Python guards: positive on a real private file in the container, negative on a widened one.
$log = "$work\dumps\guard.log"
if ((Invoke-PythonIn 'restore' @("$drive\rt\repo\scripts\guard_probe.py", "$drive\restore\posix.txt", "$drive\restore\broad\b.txt", "$drive\restore", "$drive\restore\broad") @{} $log 60) -ne 0 -or -not (Test-Line $log 'C9W_GUARD_PROBE_OK')) { Fail 'R06B1_PYTHON_GUARD' }
Remove-Item -Recurse -LiteralPath "$work\restore\broad", "$work\restore\posix.txt"
Assert-Browser 'restore'
Write-Marker 'R06B1_GUARD_AND_BROWSER_PREFLIGHT_OK_WIN:python_guard_positive_negative=true contained_renderer_page=true'
Write-Marker 'R06B1_POSIX_OWNER_ONLY_OK_WIN:real_dacl=true broad_refused=true overwrite_refused=true wide_mode_refused=true'
# Private copies only after non-private containment and runtime preflight.
if ($Mode -ceq 'ApprovedImport') {
    foreach ($role in 'restore', 'mixed') {
        Copy-Item -LiteralPath $Plan -Destination "$work\$role\approved-plan.json"
        Assert-ApprovedPlan "$work\$role\approved-plan.json" '' @($roles[$role].Sid)
    }
}
