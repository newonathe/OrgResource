# Dot-sourced by verify-c9-local-mixed-windows.ps1 only. Custody, containment, MySQL and C5 helpers.

# --- private custody (0700/0600 equivalents read from the real DACL) ---
function Test-PlainPath([string] $Path, [string] $Type) {
    if (-not [IO.Path]::IsPathFullyQualified($Path) -or $Path -cnotmatch '^[A-Za-z]:\\' -or [IO.Path]::GetFullPath($Path).TrimEnd('\') -ine $Path.TrimEnd('\')) { return $false }
    if (-not (Test-Path -LiteralPath $Path -PathType $Type)) { return $false }
    $volume = [IO.DriveInfo]::new($Path.Substring(0, 1))
    if ($volume.DriveType -ne 'Fixed' -or $volume.DriveFormat -ne 'NTFS') { return $false }
    $item = Get-Item -LiteralPath $Path -Force
    while ($item) {
        $bits = [int]$item.Attributes
        if (($bits -band 0x400) -or ($bits -band 0x1000) -or ($bits -band 0x40000) -or ($bits -band 0x400000)) { return $false } # reparse, offline, recall
        $item = if ($item -is [IO.FileInfo]) { $item.Directory } else { $item.Parent }
    }
    foreach ($cloud in $env:OneDrive, $env:OneDriveConsumer, $env:OneDriveCommercial) {
        if ($cloud -and ("$Path\").StartsWith($cloud.TrimEnd('\') + '\', [StringComparison]::OrdinalIgnoreCase)) { return $false }
    }
    return $true
}
function Test-OwnerOnly([string] $Path, [string[]] $Extra = @()) {
    $acl = Get-Acl -LiteralPath $Path
    if ($acl.GetOwner([Security.Principal.SecurityIdentifier]).Value -cne $me.User.Value) { return $false }
    $rules = @($acl.GetAccessRules($true, $true, [Security.Principal.SecurityIdentifier]))
    if ($rules.Count -lt 1) { return $false }
    foreach ($rule in $rules) {
        if ($rule.AccessControlType -ne 'Allow' -or (@($me.User.Value) + $Extra) -cnotcontains $rule.IdentityReference.Value) { return $false }
    }
    return $true
}
function Assert-HarnessAccepted([string] $RootPath, [string] $Commit) {
    $file = Join-Path $RootPath 'WINDOWS_HARNESS_ACCEPTED.txt'
    if (-not (Test-PlainPath $file 'Leaf') -or -not (Test-OwnerOnly $file) -or [IO.File]::ReadAllText($file).Trim() -cne "ACCEPTED $Commit") { Fail 'R06B2_HARNESS_NOT_ACCEPTED' }
}
function Assert-ApprovedPlan([string] $Path, [string] $Outside, [string[]] $Extra = @()) {
    if (-not $Path -or -not (Test-PlainPath $Path 'Leaf') -or -not (Test-OwnerOnly $Path $Extra) -or ($Outside -and $Path.StartsWith("$Outside\", [StringComparison]::OrdinalIgnoreCase))) { Fail 'R06B1_PRIVATE_PLAN_REQUIRED' }
    if ((Get-Item -LiteralPath $Path).Length -ne 19265850 -or (Get-Sha $Path) -cne $approvedPlanSha) { Fail 'R06B1_APPROVED_PLAN_MISMATCH' }
}
function Set-PrivateAcl([string] $Path, [hashtable[]] $Grants) {
    $acl = [Security.AccessControl.DirectorySecurity]::new(); $acl.SetAccessRuleProtection($true, $false); $acl.SetOwner($me.User)
    $acl.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new($me.User, 'FullControl', 'ContainerInherit,ObjectInherit', 'None', 'Allow'))
    foreach ($g in $Grants) {
        $inherit = if ($g.Inherit) { 'ContainerInherit,ObjectInherit' } else { 'None' }
        $acl.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new([Security.Principal.SecurityIdentifier]::new($g.Sid), $g.Rights, $inherit, 'None', 'Allow'))
    }
    Set-Acl -LiteralPath $Path -AclObject $acl
}

# --- contained execution ---
function Get-RoleEnv([string] $Role, [hashtable] $Extra) {
    $home0 = "$drive\$Role"
    $e = @{ SystemRoot = $env:SystemRoot; windir = $env:windir; SystemDrive = $env:SystemDrive; ProgramFiles = $env:ProgramFiles; COMPUTERNAME = 'C9ISOLATED'; PATHEXT = '.COM;.EXE;.BAT;.CMD'
        PATH = "$env:SystemRoot\System32"; TEMP = $home0; TMP = $home0; USERPROFILE = $home0; HOME = $home0; LOCALAPPDATA = $home0; APPDATA = $home0
        EXT_SMS_ENABLED = 'false'; EXT_REMINDER_MODE = 'DISABLED'; POWERSHELL_TELEMETRY_OPTOUT = '1' }
    if ($Extra) { foreach ($k in $Extra.Keys) { $e[$k] = $Extra[$k] } }
    return $e
}
function Start-Contained([string] $Role, [string] $Exe, [string[]] $Arguments, [hashtable] $Extra, [string] $In, [string] $Out, [string] $Err) {
    $r = $roles[$Role]; if (-not $r) { Fail 'R06B1_ROLE' }
    return $r.Container.Start($Role, $Exe, $Arguments, (Get-RoleEnv $Role $Extra), "$drive\$Role", $In, $Out, $(if ($Err) { $Err } else { $Out }))
}
function Invoke-Contained([string] $Role, [string] $Exe, [string[]] $Arguments, [hashtable] $Extra = @{}, [string] $In = '', [string] $Out = '', [string] $Err = '', [int] $Seconds = 240) {
    $p = Start-Contained $Role $Exe $Arguments $Extra $In $Out $Err
    # Short waits keep Ctrl+C responsive; the finally block ends the child by handle.
    try {
        for ($n = 0; $n -lt $Seconds * 4; $n++) { if ($p.Wait(250)) { return $p.ExitCode } }
        $p.Terminate(); return 124
    } finally { if (-not $p.HasExited) { $p.Terminate() }; $p.Dispose() }
}
function New-Scratch([string] $Suffix) { $script:seq++; return (Join-Path $work "dumps\s$($script:seq)-$Suffix") }
function Get-NodeArgs([string] $Script) { return @('--preserve-symlinks', '--preserve-symlinks-main', $Script) }
function Get-FreePort { $l = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0); $l.Start(); try { return $l.LocalEndpoint.Port } finally { $l.Stop() } }
function Get-Listeners([int] $Port) {
    # 'No such object' means no listener. Any other query failure must not look like an empty result.
    for ($try = 1; ; $try++) {
        try { return , @(Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction Stop) }
        catch {
            if ($_.FullyQualifiedErrorId -like 'CmdletizationQuery_NotFound*') { return , @() }
            if ($try -ge 4) { Fail 'R06B1_LISTENER_QUERY' }
            Start-Sleep -Milliseconds 500
        }
    }
}
function Assert-HostCannotReach([int] $Port) {
    $c = [Net.Sockets.TcpClient]::new(); $reach = $false
    try { $reach = $c.ConnectAsync('127.0.0.1', $Port).Wait(3000) -and $c.Connected } catch { } finally { $c.Dispose() }
    if ($reach) { Fail 'R06B1_EXTERNAL_REACHABLE' }
}
function New-Role([string] $Role) {
    $c = [C9Container]::new("orgresource.c9.$(New-Secret 6)")
    $roles[$Role] = @{ Container = $c; Sid = $c.SidString; Name = $c.Name; Process = $null; Port = 0; Password = (New-Secret); Schema = "orgresource_c9_local_${token}_$Role"; Uuid = '' }
}
function Assert-Role([string] $Role) {
    $r = $roles[$Role]
    if (-not $r -or -not $r.Process -or $r.Process.HasExited) { Fail 'R06B1_CONTAINER_BOUNDARY' }
    $r.Container.Verify($r.Process) # exact job, exact container SID, zero capabilities; held HANDLE, so no PID reuse
    $l = Get-Listeners $r.Port
    if ($l.Count -ne 1 -or $l[0].LocalAddress -cne '127.0.0.1' -or $l[0].OwningProcess -ne $r.Process.Pid) { Fail 'R06B1_CONTAINER_BOUNDARY' }
}

# --- MySQL ---
function Invoke-Sql([string] $Role, [string] $User, [string] $Password, [string] $Sql, [string] $Database = '') {
    Assert-Role $Role
    $in = New-Scratch 'in.sql'; $out = New-Scratch 'out.txt'; $err = New-Scratch 'err.txt'
    [IO.File]::WriteAllText($in, $Sql)
    $a = @('--no-defaults', '--protocol=tcp', '--host=127.0.0.1', "--port=$($roles[$Role].Port)", "--user=$User", '--batch', '--raw', '--skip-column-names', '--default-character-set=utf8mb4')
    if ($Database) { $a += "--database=$Database" }
    try {
        $code = Invoke-Contained $Role "$mysqlHome\bin\mysql.exe" $a @{ MYSQL_PWD = $Password } $in $out $err 300
        return [pscustomobject]@{ Code = $code; Out = [IO.File]::ReadAllText($out).Replace("`r`n", "`n").TrimEnd("`n"); Err = [IO.File]::ReadAllText($err) }
    } finally { Remove-Item -LiteralPath $in, $out, $err -ErrorAction SilentlyContinue }
}
function Invoke-Root([string] $Role, [string] $Sql, [string] $Database = '') {
    $r = Invoke-Sql $Role 'root' $roles[$Role].Password $Sql $Database
    if ($r.Code -ne 0) { Fail 'R06B1_SQL_FAILED' }
    return $r.Out
}
function Get-Scalar([string] $Role, [string] $Sql) { return (Invoke-Root $Role $Sql $roles[$Role].Schema) }
function Save-Rows([string] $Role, [string] $Name, [switch] $Domain) {
    Assert-Role $Role
    $schema = $roles[$Role].Schema; $file = Join-Path $work "dumps\$Name.sql"; $err = New-Scratch 'dump.err'
    $a = @('--no-defaults', '--protocol=tcp', '--host=127.0.0.1', "--port=$($roles[$Role].Port)", '--user=root', '--skip-comments', '--skip-dump-date', '--hex-blob',
        '--skip-lock-tables', '--skip-add-drop-table', '--no-tablespaces', '--set-gtid-purged=OFF', '--skip-column-statistics', '--single-transaction', '--quick',
        '--no-create-info', '--order-by-primary', '--skip-add-locks', '--skip-disable-keys')
    if ($Domain) { foreach ($t in 'SPRING_SESSION', 'SPRING_SESSION_ATTRIBUTES') { $a += "--ignore-table=$schema.$t"; $a += "--ignore-table=$schema.$($t.ToLowerInvariant())" } }
    if ((Invoke-Contained $Role "$mysqlHome\bin\mysqldump.exe" ($a + $schema) @{ MYSQL_PWD = $roles[$Role].Password } '' $file $err 600) -ne 0) { Fail 'R06B1_ROW_DUMP_FAILED' }
    Remove-Item -LiteralPath $err
    if ((Get-Item -LiteralPath $file).Length -eq 0) { Fail 'R06B1_ROW_DUMP_FAILED' }
    if ($Domain -and (Select-String -LiteralPath $file -Pattern '^INSERT INTO `spring_session' -Quiet)) { Fail 'R06B1_DOMAIN_DUMP_SCOPE' }
    return $file
}
function Assert-Same([string] $Code, [string] $A, [string] $B) { if ((Get-Sha $A) -cne (Get-Sha $B)) { Fail $Code } }
function Start-Server([string] $Role) {
    $r = $roles[$Role]; $dir = Join-Path $work $Role
    if (Test-Path -LiteralPath "$dir\data") { Fail 'R06B1_EXISTING_DATADIR' }
    $common = @('--no-defaults', "--basedir=$mysqlHome", "--datadir=$drive\$Role\data", "--tmpdir=$drive\$Role", '--console')
    if ((Invoke-Contained $Role "$mysqlHome\bin\mysqld.exe" ($common + '--initialize-insecure') @{} '' (Join-Path $work "dumps\$Role-init.log") '' 240) -ne 0) { Fail 'R06B1_MYSQL_INITIALIZE' }
    # Root receives its random password from the init file, before connections are accepted.
    [IO.File]::WriteAllText("$dir\first.sql", "ALTER USER 'root'@'localhost' IDENTIFIED BY '$($r.Password)';`n")
    $r.Port = Get-FreePort
    $r.Process = Start-Contained $Role "$mysqlHome\bin\mysqld.exe" ($common + @("--port=$($r.Port)", '--bind-address=127.0.0.1', '--mysqlx=0', '--no-monitor', '--default-time-zone=+00:00', '--local-infile=0', "--init-file=$drive\$Role\first.sql")) @{} '' (Join-Path $work "dumps\$Role-server.log") ''
    $probe = $null
    try {
    for ($i = 0; $i -lt 180; $i++) {
        if ($r.Process.HasExited) { Fail 'R06B1_MYSQL_READY_TIMEOUT' }
        if ((Get-Listeners $r.Port).Count) { $probe = Invoke-Sql $Role 'root' $r.Password 'SELECT VERSION();'; if ($probe.Code -eq 0) { break } }
        Start-Sleep -Milliseconds 500
    }
    if (-not $probe -or $probe.Code -ne 0) { Fail 'R06B1_MYSQL_READY_TIMEOUT' }
    } finally { Remove-Item -LiteralPath "$dir\first.sql" -ErrorAction SilentlyContinue }
    if ($probe.Out -cne '8.0.46') { Fail 'R06B1_MYSQL_VERSION' }
    $null = Invoke-Root $Role "CREATE DATABASE $($r.Schema) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
    $r.Uuid = Get-Scalar $Role 'SELECT @@server_uuid'
    if ($r.Uuid -cnotmatch '^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$') { Fail 'R06B1_SERVER_UUID' }
    if (((Get-Scalar $Role 'SELECT @@datadir') -replace '[\\/]+', '/').TrimEnd('/') -ine "$drive/$Role/data") { Fail 'R06B1_CONTAINER_BOUNDARY' }
    Assert-HostCannotReach $r.Port
}
function Get-Url([string] $Role) { return "jdbc:mysql://127.0.0.1:$($roles[$Role].Port)/$($roles[$Role].Schema)?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" }
function Assert-Denied([string] $Code, [string] $Role, [string] $User, [string] $Password, [string] $Sql) {
    $r = Invoke-Sql $Role $User $Password $Sql $roles[$Role].Schema
    if ($r.Code -eq 0) { Fail "${Code}_UNEXPECTED_GRANT" }
    if ($r.Err -cnotmatch 'ERROR 1142 .*command denied') { Fail "${Code}_WRONG_REFUSAL" }
}
function Assert-Identity([string[]] $Names) { foreach ($x in $Names) { if ((Get-Scalar $x "SELECT CONCAT(VERSION(),' ',@@server_uuid)") -cne "8.0.46 $($roles[$x].Uuid)") { Fail 'R06B1_SERVER_UUID' } } }
function Assert-OriginalFixture([string] $Role) {
    $n = @('SELECT COUNT(*) FROM organization', 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL OR demo_plan_sha IS NOT NULL',
        "SELECT COUNT(*) FROM organization WHERE slug IN ('isolated-original-workspace','isolated-other-original')",
        "SELECT COUNT(*) FROM organization o JOIN membership m ON m.org_id=o.id JOIN user_account u ON u.id=m.user_id JOIN local_credential c ON c.user_id=u.id WHERE o.slug='isolated-original-workspace' AND u.username_key='additive-original-owner' AND o.status='ACTIVE' AND u.status='ACTIVE' AND m.role='OWNER' AND m.status='ACTIVE'") | ForEach-Object { Get-Scalar $Role $_ }
    if (($n -join ',') -cne '2,0,2,1') { Fail 'R06B1_ORIGINAL_FIXTURE' }
}

# --- unmodified JAR, C5 archive, oracles ---
function Invoke-Jar([string] $Role, [hashtable] $Settings, [string] $Command, [string] $Log) {
    Assert-Role $Role
    $limit = switch ($Command) { '--c8-hosted-demo-import' { 1800 } '--c8-hosted-demo-postflight' { 900 } default { 240 } }
    $a = @()
    # The import command itself verifies the baseline manifest before and after its writes, so it needs the same provider.
    if ($Command -cin @('--c8-hosted-original-baseline', '--c8-hosted-demo-import', '--c8-additive-demo-isolated-proof')) {
        $manifest = Join-Path $work "$Role\baseline.properties"
        if ((Test-Path -LiteralPath $manifest) -and $Mode -ceq 'ApprovedImport' -and -not (Test-OwnerOnly $manifest @($roles[$Role].Sid))) { Fail 'R06B1_BASELINE_MODE' }
        $a += "-Xbootclasspath/a:$drive\rt\posixfs.jar", '-Djava.nio.file.spi.DefaultFileSystemProvider=c9win.OwnerOnlyPosixProvider', "-Dc9.acl.allowed=$($me.Name)|$($roles[$Role].Sid)"
    }
    return (Invoke-Contained $Role $javaBin ($a + @('-Xmx768m', '-jar', "$drive\rt\verified.jar", $Command)) $Settings '' $Log '' $limit)
}
function Test-Log([string] $Path, [string] $Text) { return [bool](Select-String -LiteralPath $Path -SimpleMatch -CaseSensitive $Text -Quiet) }
function Test-Line([string] $Path, [string] $Line) { return (@(Get-Content -LiteralPath $Path) -ccontains $Line) }
function Grant-Transport([string] $Role, [string] $Kind) {
    $user = "c9_$Kind"; $password = New-Secret; $schema = $roles[$Role].Schema
    $rights = if ($Kind -ceq 'backup') { 'SELECT, SHOW VIEW, TRIGGER' } else { 'ALL PRIVILEGES' }
    $null = Invoke-Root $Role "CREATE USER '$user'@'%' IDENTIFIED BY '$password'; GRANT $rights ON $schema.* TO '$user'@'%';"
    $saved = $env:MYSQL_TEST_LOGIN_FILE; $env:MYSQL_TEST_LOGIN_FILE = Join-Path $work "$Role\.mylogin.cnf"; $env:C9W_EDITOR_PASSWORD = $password
    try {
        & $Python -I (Join-Path $PSScriptRoot 'protected_login.py') "$mysqlHome\bin\mysql_config_editor.exe" "c9-$Kind" $user
        if ($LASTEXITCODE -ne 0) { Fail 'R06B1_PROTECTED_LOGIN' }
    } finally { $env:C9W_EDITOR_PASSWORD = $null; $env:MYSQL_TEST_LOGIN_FILE = $saved }
    if (-not (Test-OwnerOnly (Join-Path $work "$Role\.mylogin.cnf") @($roles[$Role].Sid))) { Fail 'R06B1_PROTECTED_LOGIN' }
}
function Revoke-Transport([string] $Role, [string] $Kind) { $null = Invoke-Root $Role "DROP USER 'c9_$Kind'@'%';"; Remove-Item -LiteralPath (Join-Path $work "$Role\.mylogin.cnf") }
# Unmodified extended-c-backup.ps1 in its real ProtectedLogin mode, inside the role's container.
function Invoke-C5([string] $Operation, [string] $Role, [string] $Section, [string] $Key, [string] $ArchiveName, [string] $Log) {
    Assert-Role $Role
    return (Invoke-Contained $Role "$env:ProgramFiles\PowerShell\7\pwsh.exe" @('-NoProfile', '-NonInteractive', '-File', "$drive\rt\repo\scripts\extended-c-backup.ps1", '-Mode', $Operation,
            '-Schema', $roles[$Role].Schema, '-Port', "$($roles[$Role].Port)", '-Archive', "$drive\$Role\$ArchiveName", '-LoginPath', $Section) @{
            EXT_BACKUP_KEY = $Key; MYSQL_HOME = $mysqlHome; MYSQL_TEST_LOGIN_FILE = "$drive\$Role\.mylogin.cnf" } '' $Log '' 900)
}
function Copy-Archive([string] $Name, [string] $From, [string] $To, [string] $Sha) {
    Copy-Item -LiteralPath (Join-Path $work "$From\$Name") -Destination (Join-Path $work "$To\$Name")
    if ((Get-Sha (Join-Path $work "$To\$Name")) -cne $Sha -or -not (Test-OwnerOnly (Join-Path $work "$To\$Name") @($roles[$To].Sid))) { Fail 'R06B3A_ARCHIVE_CHANGED' }
}
function Invoke-PythonIn([string] $Role, [string[]] $Arguments, [hashtable] $Extra, [string] $Log, [int] $Seconds = 150) {
    return (Invoke-Contained $Role "$drive\rt\python\python.exe" (@('-B') + $Arguments) (@{ PYTHONUTF8 = '1'; PYTHONNOUSERSITE = '1' } + $Extra) '' $Log '' $Seconds)
}
function Save-Projection([string] $Role, [string] $User, [string] $Password, [string] $Name) {
    Assert-Role $Role
    $err = New-Scratch 'projection.err'; $file = "$work\$Role\$Name"
    $code = Invoke-Contained $Role "$mysqlHome\bin\mysql.exe" @('--no-defaults', '--protocol=tcp', '--host=127.0.0.1', "--port=$($roles[$Role].Port)", "--user=$User", '--batch', '--raw', '--skip-column-names', '--default-character-set=utf8mb4', "--database=$($roles[$Role].Schema)") @{ MYSQL_PWD = $Password } "$work\rt\repo\scripts\c9-private-mixed-expectations.sql" $file $err 120
    if ($code -ne 0) { Fail 'R06B3B_PROJECTION' }
    [IO.File]::WriteAllText($file, [IO.File]::ReadAllText($file).Replace("`r`n", "`n")) # Windows client text-mode newlines only
}
function Read-Datagrams($Client) { $n = 0; try { while ($true) { $ep = [Net.IPEndPoint]::new(0, 0); $null = $Client.Receive([ref]$ep); $n++ } } catch { }; return $n }
function Invoke-Probe([string] $Role, [hashtable] $Spec) {
    $arg = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes(($Spec | ConvertTo-Json -Compress -Depth 5))); $out = New-Scratch 'probe.txt'
    if ((Invoke-Contained $Role "$env:ProgramFiles\nodejs\node.exe" ((Get-NodeArgs "$drive\rt\win\containment_probe.cjs") + @('probe', $arg)) @{} '' $out '' 120) -ne 0) { Fail 'R06B1_NETWORK_PROBE' }
    return (Convert-ProbeJson ([IO.File]::ReadAllText($out)))
}
# Reject duplicate properties before PowerShell dictionary conversion can discard them.
function Convert-ProbeJson([string] $Json) {
    $doc = $null
    try {
        $doc = [System.Text.Json.JsonDocument]::Parse($Json)
        if ($doc.RootElement.ValueKind -ne [System.Text.Json.JsonValueKind]::Object) { Fail 'R06B1_NETWORK_PROBE_SHAPE' }
        $keys = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
        $result = [hashtable]::new([StringComparer]::Ordinal)
        foreach ($property in $doc.RootElement.EnumerateObject()) {
            if (-not $keys.Add($property.Name) -or $property.Value.ValueKind -ne [System.Text.Json.JsonValueKind]::String) { Fail 'R06B1_NETWORK_PROBE_SHAPE' }
            $result.Add($property.Name, $property.Value.GetString())
        }
        return $result
    } catch { Fail 'R06B1_NETWORK_PROBE_SHAPE' }
    finally { if ($null -ne $doc) { $doc.Dispose() } }
}
# Pure evaluation, unit-tested with altered results in the negative matrix.
function Assert-ProbeResult([hashtable] $Seen, [hashtable] $Spec) {
    # Every requested target must produce exactly one correctly cased, recognized result.
    $tcpErrors = @('CONNECTED','TIMEOUT','ETIMEDOUT','ECONNREFUSED','EACCES','EPERM','ENETUNREACH','EHOSTUNREACH','EADDRNOTAVAIL')
    $udpErrors = @('REPLY','NO_REPLY','EACCES','EPERM','ENETUNREACH','EHOSTUNREACH','EADDRNOTAVAIL','ECONNREFUSED','ETIMEDOUT')
    $dnsErrors = @('RESOLVED','ENOTFOUND','ENODATA','ESERVFAIL','EREFUSED','ECONNREFUSED','ETIMEOUT','ETIMEDOUT','TIMEOUT','EACCES','EPERM','ENETUNREACH','EHOSTUNREACH','EAI_AGAIN')
    $expected = [hashtable]::new([StringComparer]::OrdinalIgnoreCase)
    $expected.Add('self', $tcpErrors); $expected.Add('external4', $tcpErrors); $expected.Add('external6', $tcpErrors)
    $expected.Add('dnsDirect', $dnsErrors); $expected.Add('dnsSystem', $dnsErrors)
    try {
        foreach ($k in $Spec.tcpTargets.Keys) { $expected.Add($k, $tcpErrors) }
        foreach ($v in $Spec.udpTargets) { $expected.Add("udp $($v.name)", $udpErrors) }
        foreach ($p in $Spec.paths) { $expected.Add("path $p", @('READ','EPERM','EACCES')) }
        foreach ($p in $Spec.writePaths) { $expected.Add("write $p", @('WRITTEN','EPERM','EACCES')) }
    } catch { Fail 'R06B1_NETWORK_PROBE_SHAPE' }
    if ($Seen.Count -ne $expected.Count -or @($expected.Keys | Where-Object { @($Seen.Keys) -cnotcontains $_ }).Count) { Fail 'R06B1_NETWORK_PROBE_SHAPE' }
    if (@($expected.Keys) -cnotcontains 'ownListener' -or @($expected.Keys) -cnotcontains 'hostLoopback' -or @($expected.Keys) -cnotcontains 'udp loopback') { Fail 'R06B1_NETWORK_PROBE_SHAPE' }
    foreach ($k in $expected.Keys) {
        if ($Seen[$k] -isnot [string] -or $Seen[$k] -cnotin $expected[$k]) { Fail 'R06B1_NETWORK_PROBE_SHAPE' }
    }
    if ($Seen.self -cne 'CONNECTED' -or $Seen.ownListener -cne 'CONNECTED') { Fail 'R06B1_LOOPBACK_UNAVAILABLE' }
    if ($Seen.external4 -cne 'EACCES' -or $Seen.external6 -cnotin @('EACCES', 'ENETUNREACH', 'EADDRNOTAVAIL')) { Fail 'R06B1_EXTERNAL_REACHABLE' }
    foreach ($k in @($Seen.Keys | Where-Object { $_ -clike 'role *' -or $_ -cin @('hostLoopback', 'lan') })) { if ($Seen[$k] -ceq 'CONNECTED') { Fail 'R06B1_EXTERNAL_REACHABLE' } }
    foreach ($k in @($Seen.Keys | Where-Object { $_ -clike 'udp *' })) { if ($Seen[$k] -ceq 'REPLY') { Fail 'R06B1_EXTERNAL_REACHABLE' } }
    # Any resolver answer, positive or negative, means a name query left the container.
    if ($Seen.dnsDirect -cin @('RESOLVED', 'ENOTFOUND', 'ENODATA', 'ESERVFAIL', 'EREFUSED') -or $Seen.dnsSystem -ceq 'RESOLVED') { Fail 'R06B1_EXTERNAL_REACHABLE' }
    foreach ($k in @($Seen.Keys | Where-Object { $_ -clike 'path *' })) { if ($Seen[$k] -cnotin @('EPERM', 'EACCES')) { Fail 'R06B1_CROSS_ROLE_FILES' } }
    foreach ($k in @($Seen.Keys | Where-Object { $_ -clike 'write *' })) { if ($Seen[$k] -cnotin @('EPERM', 'EACCES')) { Fail 'R06B1_RUNTIME_WRITABLE' } }
}
function Assert-Containment {
    $names = @($roles.Keys); $node = "$env:ProgramFiles\nodejs\node.exe"; $probeFile = "$drive\rt\win\containment_probe.cjs"
    $hostListener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Any, 0); $hostListener.Start(); $hostPort = $hostListener.LocalEndpoint.Port
    $udpHost = [Net.Sockets.UdpClient]::new([Net.IPEndPoint]::new([Net.IPAddress]::Any, 0)); $udpPort = $udpHost.Client.LocalEndPoint.Port; $udpHost.Client.ReceiveTimeout = 1000
    $servers = @()
    try {
        $up = @([Net.NetworkInformation.NetworkInterface]::GetAllNetworkInterfaces() | Where-Object OperationalStatus -EQ 'Up' | ForEach-Object { $_.GetIPProperties() })
        $lan = @($up | ForEach-Object { $_.UnicastAddresses } | Where-Object { $_.Address.AddressFamily -eq 'InterNetwork' -and -not [Net.IPAddress]::IsLoopback($_.Address) } | Select-Object -First 1 -ExpandProperty Address)
        $gateway = @($up | ForEach-Object { $_.GatewayAddresses } | Where-Object { $_.Address.AddressFamily -eq 'InterNetwork' } | Select-Object -First 1 -ExpandProperty Address)
        # Positive controls from the host: TCP connect, UDP receive, and (when a router exists) a real DNS answer from it.
        $control = [Net.Sockets.TcpClient]::new(); $control.Connect('127.0.0.1', $hostPort); if (-not $control.Connected) { Fail 'R06B1_NETWORK_CONTROL' }; $control.Dispose()
        $send = [Net.Sockets.UdpClient]::new(); $null = $send.Send([byte[]](1, 2), 2, '127.0.0.1', $udpPort); $send.Dispose()
        if ((Read-Datagrams $udpHost) -ne 1) { Fail 'R06B1_NETWORK_CONTROL' }
        $gatewayControl = $false
        if ($gateway.Count) {
            $g = [Net.Sockets.UdpClient]::new(); $g.Client.ReceiveTimeout = 3000
            try { $q = [Convert]::FromHexString('123401000001000000000000076578616d706c6507696e76616c69640000010001'); $null = $g.Send($q, $q.Length, "$($gateway[0])", 53); $ep = [Net.IPEndPoint]::new(0, 0); $null = $g.Receive([ref]$ep); $gatewayControl = $true } catch { } finally { $g.Dispose() }
        }
        $systemControl = $false; try { $systemControl = [Net.Dns]::GetHostAddresses('example.com').Count -gt 0 } catch { }
        $ports = @{}
        foreach ($role in $names) {
            $ports[$role] = Get-FreePort
            $servers += Start-Contained $role $node ((Get-NodeArgs $probeFile) + @('serve', "$($ports[$role])")) @{} '' (New-Scratch 'peer.txt') ''
            for ($i = 0; $i -lt 80 -and -not (Get-Listeners $ports[$role]).Count; $i++) { Start-Sleep -Milliseconds 250 }
            if (-not (Get-Listeners $ports[$role]).Count) { Fail 'R06B1_NETWORK_CONTROL' }
            Assert-HostCannotReach $ports[$role]
        }
        $v6 = @()
        foreach ($role in $names) {
            $tcp = @{ hostLoopback = @{ host = '127.0.0.1'; port = $hostPort }; ownListener = @{ host = '127.0.0.1'; port = $ports[$role] } }
            foreach ($other in $names | Where-Object { $_ -cne $role }) { $tcp["role $other"] = @{ host = '127.0.0.1'; port = $ports[$other] } }
            if ($lan.Count) { $tcp.lan = @{ host = "$($lan[0])"; port = $hostPort } }
            $udp = @(@{ name = 'loopback'; host = '127.0.0.1'; port = $udpPort })
            if ($lan.Count) { $udp += @{ name = 'lan'; host = "$($lan[0])"; port = $udpPort } }
            if ($gateway.Count) { $udp += @{ name = 'gateway'; host = "$($gateway[0])"; port = 53 } }
            $paths = @($names | Where-Object { $_ -cne $role } | ForEach-Object { "$drive\$_" }) + @("$drive\dumps", $Root, $env:USERPROFILE)
            $spec = @{ tcpTargets = $tcp; udpTargets = $udp; paths = $paths; writePaths = @("$drive\rt"); dnsServer = $(if ($gatewayControl) { "$($gateway[0])" } else { '' }) }
            $seen = Invoke-Probe $role $spec
            Assert-ProbeResult $seen $spec
            $v6 += $seen.external6
            if ((Read-Datagrams $udpHost) -ne 0) { Fail 'R06B1_EXTERNAL_REACHABLE' }
        }
        # Every peer must still have been listening when the last pair was probed, or a refusal would prove nothing.
        foreach ($role in $names) { if (-not (Get-Listeners $ports[$role]).Count) { Fail 'R06B1_NETWORK_CONTROL' } }
        $v6state = if (@($v6 | Where-Object { $_ -ceq 'EACCES' }).Count -eq $names.Count) { 'denied' } else { 'no_route_not_proven' }
        Write-Marker "R06B1_NETWORK_ISOLATION_OK_WIN:roles=$($names.Count) capabilities=0 self_loopback=true external_tcp_v4_denied=true external_tcp_v6=$v6state udp_undelivered=true udp_receiver_control=true router_dns_control=$gatewayControl direct_resolver=$(if ($gatewayControl) { 'unanswered' } else { 'not_proven' }) system_resolver=$(if ($systemControl) { 'unresolved' } else { 'not_proven_offline' }) host_to_container_denied=true container_to_host_denied=true lan_denied=$([bool]$lan.Count) cross_role_pairs_denied=$($names.Count * ($names.Count - 1)) cross_role_files_denied=true runtime_readonly=true"
    } finally { foreach ($s0 in $servers) { $s0.Terminate(); $s0.Dispose() }; $hostListener.Stop(); $udpHost.Dispose() }
}
# Real renderer, context and page over loopback CDP inside the container; no HTTP request, no download.
function Assert-Browser([string] $Role) {
    $cdp = Get-FreePort; $ud = "ud-$(New-Secret 4)"; New-Item -ItemType Directory -Path "$work\$Role\$ud" | Out-Null
    $p = Start-Contained $Role "$drive\rt\chromium\chrome-headless-shell.exe" @('--headless', '--no-sandbox', '--disable-gpu', '--no-first-run', '--disable-background-networking', "--remote-debugging-port=$cdp", '--remote-debugging-address=127.0.0.1', "--user-data-dir=$drive\$Role\$ud", 'about:blank') @{} '' (New-Scratch 'chromium.log') ''
    try {
        for ($i = 0; $i -lt 80 -and -not (Get-Listeners $cdp).Count; $i++) { if ($p.HasExited) { Fail 'R06B3B_PRIVATE_BROWSER_PREFLIGHT' }; Start-Sleep -Milliseconds 500 }
        Assert-HostCannotReach $cdp
        if ((Invoke-Contained $Role "$env:ProgramFiles\nodejs\node.exe" (Get-NodeArgs "$drive\rt\repo\scripts\browser_preflight.cjs") @{ C9_WINDOWS_CDP = "http://127.0.0.1:$cdp" } '' (New-Scratch 'browser.txt') '' 60) -ne 0) { Fail 'R06B3B_PRIVATE_BROWSER_PREFLIGHT' }
    } finally { if (-not $p.HasExited) { $roles[$Role].Container.Verify($p); $p.Terminate() }; $ended = $p.HasExited; $p.Dispose() }
    if (-not $ended) { Fail 'R06B3B_PRIVATE_BROWSER_STOP' }
}

# --- cleanup: only handles, containers, mapping and directory created by this run ---
function Stop-Role([string] $Role) {
    $r = $roles[$Role]
    if ($r.Process -and -not $r.Process.HasExited) {
        try { $null = Invoke-Contained $Role "$mysqlHome\bin\mysqladmin.exe" @('--no-defaults', '--protocol=tcp', '--host=127.0.0.1', "--port=$($r.Port)", '--user=root', 'shutdown') @{ MYSQL_PWD = $r.Password } '' '' '' 20; $null = $r.Process.Wait(20000) } catch { }
    }
    if (-not $r.Container.KillAll()) { return $false }
    if ($r.Process) { $r.Process.Dispose(); $r.Process = $null }
    $r.Container.Dispose(); $r.Container = $null
    if ($r.Port) { return ((Get-Listeners $r.Port).Count -eq 0) }
    return $true
}
function Invoke-Cleanup {
    $ok = $true
    foreach ($role in @($roles.Keys)) { try { if ($roles[$role].Container -and -not (Stop-Role $role)) { $ok = $false } } catch { $ok = $false } }
    if ($drive) {
        $map = @(& "$env:SystemRoot\System32\subst.exe") | Where-Object { $_ -like "$drive\*" }
        if ($map) {
            if ("$map".EndsWith("=> $work", [StringComparison]::OrdinalIgnoreCase)) { & "$env:SystemRoot\System32\subst.exe" $drive /D | Out-Null; if ($LASTEXITCODE -ne 0) { $ok = $false } }
            else { $ok = $false }
        }
    }
    if ($script:lock) { $script:lock.Dispose(); $script:lock = $null }
    if ($ok -and $work -and (Test-Path -LiteralPath $work)) {
        for ($i = 0; $i -lt 20 -and (Test-Path -LiteralPath $work); $i++) { try { Remove-Item -LiteralPath $work -Recurse -Force } catch { Start-Sleep -Milliseconds 500 } }
        if (Test-Path -LiteralPath $work) { $ok = $false }
    }
    return $ok
}

# --- observation-only packaged runtime on a recovered server (shared by the public control and the private run) ---
function Invoke-Runtime([string] $Role, [string] $User, [string] $Kind, [hashtable] $OracleEnv, [string] $ApiScript, [string] $BrowserScript, [string[]] $ApiMarkers, [string] $BrowserMarker, [string] $LogDir, [scriptblock] $BeforeStart = $null) {
    $schema = $roles[$Role].Schema; $password = New-Secret
    $null = Invoke-Root $Role "CREATE USER '$User'@'%' IDENTIFIED BY '$password'; GRANT SELECT ON $schema.* TO '$User'@'%'; GRANT INSERT,UPDATE,DELETE ON $schema.SPRING_SESSION TO '$User'@'%'; GRANT INSERT,UPDATE,DELETE ON $schema.SPRING_SESSION_ATTRIBUTES TO '$User'@'%';"
    $orgDb = Get-Scalar $Role "SELECT id FROM organization WHERE slug='isolated-original-workspace'"
    if ($orgDb -cnotmatch '^[1-9][0-9]*$') { Fail "${Kind}_LOCK_ID" }
    $memberDb = Get-Scalar $Role "SELECT m.id FROM membership m JOIN user_account u ON u.id=m.user_id WHERE m.org_id=$orgDb AND u.username_key='additive-original-owner'"
    if ($memberDb -cnotmatch '^[1-9][0-9]*$') { Fail "${Kind}_LOCK_ID" }
    $lock = "START TRANSACTION; SELECT id FROM organization WHERE id=$orgDb FOR UPDATE; SELECT id FROM membership WHERE id=$memberDb FOR UPDATE; ROLLBACK;"
    Assert-Denied "${Kind}_LOCK" $Role $User $password $lock
    $null = Invoke-Root $Role "GRANT LOCK TABLES ON $schema.* TO '$User'@'%';"
    $r = Invoke-Sql $Role $User $password $lock $schema
    if ($r.Code -ne 0 -or $r.Out -cne "$orgDb`n$memberDb") { Fail "${Kind}_LOCK_READ" }
    foreach ($q in 'UPDATE organization SET id=id WHERE 1=0', 'UPDATE allocation SET id=id WHERE 1=0', 'DELETE FROM idempotency_key WHERE 1=0', 'INSERT INTO audit_event SELECT * FROM audit_event WHERE 1=0', 'UPDATE stock_movement SET id=id WHERE 1=0', 'UPDATE membership SET id=id WHERE 1=0', 'DELETE FROM membership WHERE 1=0') { Assert-Denied $Kind $Role $User $password $q }
    Write-Marker "${Kind}_GRANTS_OK_WIN:lock_refusal_positive=true seven_domain_denials=true session_dml_only=true"
    if ($BeforeStart) { $null = & $BeforeStart } # full recheck after the grant tests, immediately before the JVM
    $before = Save-Rows $Role "$Kind-before-domain" -Domain
    if ((Get-Listeners 8085).Count) { Fail 'R06B1_UNEXPECTED_LISTENER' }
    $appLog = Join-Path $LogDir "$Kind-runtime-private.log"
    $app = Start-Contained $Role $javaBin @('-Xmx768m', '-Duser.timezone=Asia/Manila', '-jar', "$drive\rt\verified.jar", '--spring.flyway.enabled=false') @{ EXT_DB_URL = (Get-Url $Role); EXT_DB_USER = $User; EXT_DB_PASSWORD = $password
        EXT_CONTACT_ENCRYPTION_KEY = $contactKey; SPRING_PROFILES_ACTIVE = 'c1-test'; EXT_SERVER_PORT = '8085'; EXT_SCHEDULER_DELAY_MS = '600000' } '' $appLog ''
    $browser = $null
    try {
        $ready = $false; $index = New-Scratch 'index.html'; $curlErr = New-Scratch 'curl.err'; $clock = [Diagnostics.Stopwatch]::StartNew()
        while (-not $ready -and $clock.Elapsed.TotalSeconds -lt 150) {
            if ($app.HasExited) { Fail "${Kind}_JAR_EXITED" }
            if ((Invoke-Contained $Role "$env:SystemRoot\System32\curl.exe" @('--noproxy', '*', '--silent', '--fail', '--max-time', '2', 'http://127.0.0.1:8085/') @{} '' $index $curlErr 10) -eq 0) { $ready = $true } else { Start-Sleep 1 }
        }
        $l = Get-Listeners 8085
        if (-not $ready -or -not (Select-String -LiteralPath $index -Pattern 'assets/index-[^"]+\.js' -Quiet) -or $l.Count -lt 1 -or @($l | Where-Object OwningProcess -ne $app.Pid).Count) { Fail "${Kind}_JAR_READY" }
        Assert-HostCannotReach 8085
        $startSeconds = [Math]::Round($clock.Elapsed.TotalSeconds, 1)
        $cdp = Get-FreePort
        New-Item -ItemType Directory -Path "$work\$Role\ud" | Out-Null
        $browser = Start-Contained $Role "$drive\rt\chromium\chrome-headless-shell.exe" @('--headless', '--no-sandbox', '--disable-gpu', '--no-first-run', '--disable-background-networking', "--remote-debugging-port=$cdp", '--remote-debugging-address=127.0.0.1', "--user-data-dir=$drive\$Role\ud", 'about:blank') @{} '' (Join-Path $LogDir "$Kind-chromium-private.log") ''
        $up = $false
        for ($i = 0; $i -lt 60 -and -not $up; $i++) { if ($browser.HasExited) { Fail "${Kind}_BROWSER_PREFLIGHT" }; $up = (Get-Listeners $cdp).Count -eq 1; if (-not $up) { Start-Sleep -Milliseconds 500 } }
        if (-not $up) { Fail "${Kind}_BROWSER_PREFLIGHT" }
        Assert-HostCannotReach $cdp
        $shared = @{ C9_RUNTIME_ISOLATED = 'true'; C9_WINDOWS_CONTAINED = 'true'; C9_RUNTIME_OWNER_PASSWORD = $ownerPassword; C9_RUNTIME_STATE = "$drive\$Role\session.json"; C9_WINDOWS_CDP = "http://127.0.0.1:$cdp" } + $OracleEnv
        $apiLog = Join-Path $LogDir "$Kind-api-private.log"; $out = New-Scratch 'api.log'
        $code = Invoke-PythonIn $Role @("$drive\rt\repo\scripts\$ApiScript", 'login') $shared $out; Get-Content $out | Add-Content $apiLog
        if ($code -ne 0) { Fail "${Kind}_API" }
        if (-not (Test-OwnerOnly "$work\$Role\session.json" @($roles[$Role].Sid))) { Fail "${Kind}_SESSION_ACL" }
        $session = ([IO.File]::ReadAllText("$work\$Role\session.json") | ConvertFrom-Json).cookie
        if ($session -cnotmatch '^[a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12}$') { Fail "${Kind}_SESSION_ID" }
        if ((Get-Scalar $Role "SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID='$session' AND EXPIRY_TIME>UNIX_TIMESTAMP()*1000") -cne '1' -or
            [int](Get-Scalar $Role "SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES a JOIN SPRING_SESSION s ON s.PRIMARY_ID=a.SESSION_PRIMARY_ID WHERE s.SESSION_ID='$session'") -le 0) { Fail "${Kind}_SESSION_NOT_PERSISTED" }
        Write-Marker "${Kind}_PERSISTED_SESSION_OK_WIN"
        $code = Invoke-PythonIn $Role @("$drive\rt\repo\scripts\$ApiScript", 'logout') $shared $out; Get-Content $out | Add-Content $apiLog
        if ($code -ne 0) { Fail "${Kind}_LOGOUT" }
        if ((Get-Scalar $Role "SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID='$session'") -cne '0') { Fail "${Kind}_SESSION_NOT_RETIRED" }
        $browserLog = Join-Path $LogDir "$Kind-browser-private.log"
        if ((Invoke-Contained $Role "$env:ProgramFiles\nodejs\node.exe" (Get-NodeArgs "$drive\rt\repo\scripts\$BrowserScript") $shared '' $browserLog '' 180) -ne 0) { Fail "${Kind}_BROWSER" }
        foreach ($m in $ApiMarkers) { if (-not (Test-Log $apiLog $m)) { Fail "${Kind}_MARKERS" } }
        if (-not (Test-Log $browserLog $BrowserMarker)) { Fail "${Kind}_MARKERS" }
        if ($clock.Elapsed.TotalSeconds -gt 480) { Fail "${Kind}_RUNTIME_LIMIT" } # same overall JVM bound as the Linux runner
        Write-Marker "${Kind}_API_BROWSER_OK_WIN:app_start_seconds=$startSeconds"
    } finally {
        # Signal only the held handles of this run's own children, after re-checking their container and job.
        foreach ($p in $browser, $app) {
            if (-not $p) { continue }
            if (-not $p.HasExited) { $roles[$Role].Container.Verify($p); $p.Terminate() }
            if (-not $p.HasExited) { Fail "${Kind}_STOP" }
            $p.Dispose()
        }
    }
    for ($i = 0; $i -lt 40 -and (Get-Listeners 8085).Count; $i++) { Start-Sleep -Milliseconds 250 }
    if ((Get-Listeners 8085).Count) { Fail "${Kind}_STOP" }
    Assert-Same "${Kind}_DOMAIN_CHANGED" $before (Save-Rows $Role "$Kind-after-domain" -Domain)
    $null = Invoke-Root $Role "DROP USER '$User'@'%';"
    return @(Select-String -LiteralPath $appLog -Pattern 'command denied|receipt cleanup failed|Expiry processing failed|Stale reminder claim recovery failed').Count
}
