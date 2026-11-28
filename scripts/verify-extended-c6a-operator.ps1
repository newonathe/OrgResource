# Independent Windows operator drill. Use a verified JAR, and Python with pywinpty
# installed in a test-only location to answer mysql_config_editor's console prompt.
param(
    [Parameter(Mandatory)][string] $JarPath,
    [string] $Python = 'python.exe'
)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$jar = (Resolve-Path -LiteralPath $JarPath).Path
$mysqlHome = if ($env:MYSQL_HOME) { $env:MYSQL_HOME } else { Join-Path $env:ProgramFiles 'MySQL/MySQL Server 8.0' }
$bin = Join-Path $mysqlHome 'bin'
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java.exe' }
$temp = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-C6a-Operator-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $temp
$servers = [Collections.Generic.List[object]]::new()
$app = $null
$applicationPort = 0
$saved = @{}
$names = @('MYSQL_HOME','MYSQL_TEST_LOGIN_FILE','MYSQL_PWD','EXT_DB_URL','EXT_DB_USER','EXT_DB_PASSWORD','EXT_DB_MIGRATION_URL',
    'EXT_DB_MIGRATION_USER','EXT_DB_MIGRATION_PASSWORD','EXT_BOOTSTRAP_SLUG','EXT_BOOTSTRAP_ORG_NAME',
    'EXT_BOOTSTRAP_USERNAME','EXT_BOOTSTRAP_DISPLAY_NAME','EXT_BOOTSTRAP_PASSWORD','EXT_BACKUP_KEY',
    'EXT_CONTACT_ENCRYPTION_KEY','EXT_SMS_ENABLED','EXT_REMINDER_MODE','EXT_SERVER_PORT','SPRING_PROFILES_ACTIVE',
    'EXT_E2E_USERNAME','EXT_E2E_PASSWORD','EXT_E2E_BASE_URL','ORGRESOURCE_E2E_SCREENSHOT_DIR','C6A_EDITOR_PASSWORD')
foreach ($name in $names) { $saved[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }

function Assert-Native([string] $Step) {
    if ($LASTEXITCODE -ne 0) { throw "$Step failed with exit code $LASTEXITCODE." }
}
function Free-C6aPort {
    $listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
    $listener.Start()
    try { return $listener.LocalEndpoint.Port } finally { $listener.Stop() }
}
function New-FixtureSecret {
    return [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
}
# Point-in-time attribution; this does not reserve a port.
function Test-C6aOwnedListener([System.Diagnostics.Process] $Process,[int] $Port) {
    if (-not $Process -or $Port -lt 1 -or $Port -gt 65535) { throw 'C6A_LISTENER_INVALID' }
    $Process.Refresh()
    if ($Process.HasExited) { throw 'C6A_LISTENER_PROCESS_EXITED' }
    $rows = @(Get-NetTCPConnection -State Listen -ErrorAction Stop | Where-Object { $_.LocalPort -eq $Port })
    if ($rows.Count -eq 0) { return $false }
    if ($rows.Count -ne 1 -or $rows[0].LocalAddress -cne '127.0.0.1' -or
        $rows[0].OwningProcess -ne $Process.Id) { throw 'C6A_LISTENER_NOT_OWNED' }
    $Process.Refresh()
    if ($Process.HasExited) { throw 'C6A_LISTENER_PROCESS_EXITED' }
    return $true
}
function Assert-C6aPort([int] $Port) {
    $live = @($servers | Where-Object { $_.Port -eq $Port -and -not $_.Process.HasExited })
    if ($live.Count -ne 1) { throw 'C6A_SERVER_IDENTITY_UNKNOWN' }
    if (-not (Test-C6aOwnedListener $live[0].Process $Port)) { throw 'C6A_LISTENER_MISSING' }
}
function Stop-C6aServer($Server) {
    if ($Server.Process.HasExited) { return }
    if (-not (Test-C6aOwnedListener $Server.Process $Server.Port)) { throw 'C6A_LISTENER_MISSING' }
    & "$bin/mysqladmin.exe" --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$($Server.Port)" --user=root --skip-password shutdown *> $null
    if (-not $Server.Process.WaitForExit(15000)) {
        $Server.Process.Kill()
        if (-not $Server.Process.WaitForExit(15000)) { throw 'C6A_SERVER_NOT_STOPPED' }
    }
}
function Start-C6aMysql([string] $Name, [int] $ReusePort = 0) {
    $dir = Join-Path $temp $Name
    $common = @('--no-defaults', ('--basedir="' + $mysqlHome + '"'), ('--datadir="' + $dir + '"'))
    if ($ReusePort -eq 0) {
        $null = New-Item -ItemType Directory -Path $dir
        $init = Start-Process -FilePath "$bin/mysqld.exe" -ArgumentList ($common + '--initialize-insecure') -Wait -PassThru -WindowStyle Hidden
        if ($init.ExitCode -ne 0) { throw 'Isolated MySQL initialization failed.' }
        $port = Free-C6aPort
    } else {
        if (-not (Test-Path -LiteralPath (Join-Path $dir 'mysql') -PathType Container)) { throw 'Restart requires the initialized data directory from this drill.' }
        $port = $ReusePort
    }
    $process = Start-Process -FilePath "$bin/mysqld.exe" -ArgumentList ($common + @("--port=$port",
        '--bind-address=127.0.0.1','--mysqlx=0',('--log-error="' + (Join-Path $dir 'mysql.log') + '"'))) -PassThru -WindowStyle Hidden
    $servers.Add(@{ Port = $port; Process = $process })
    for ($i=0; $i -lt 60; $i++) {
        if (Test-C6aOwnedListener $process $port) {
            $answer = @(& "$bin/mysql.exe" --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$port" --user=root --skip-password --connect-timeout=2 --batch --raw --skip-column-names '--execute=SELECT 1' 2>$null)
            if ($LASTEXITCODE -eq 0 -and $answer.Count -eq 1 -and $answer[0] -ceq '1') {
                Assert-C6aPort $port
                return $port
            }
        }
        Start-Sleep -Milliseconds 300
    }
    throw 'Isolated MySQL did not start.'
}
function Invoke-LocalSql([int] $Port, [string] $Sql) {
    Assert-C6aPort $Port
    $info = [Diagnostics.ProcessStartInfo]::new("$bin/mysql.exe")
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardInput = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    foreach ($arg in @('--no-defaults','--protocol=tcp','--host=127.0.0.1',"--port=$Port",'--user=root',
            '--skip-password','--batch','--skip-column-names','--raw')) { $info.ArgumentList.Add($arg) }
    $process = [Diagnostics.Process]::Start($info)
    try {
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.StandardInput.WriteLine($Sql)
        $process.StandardInput.Close()
        if (-not $process.WaitForExit(20000)) { throw 'Local fixture SQL timed out.' }
        $null = $stderr.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) { throw "Local fixture SQL failed with exit $($process.ExitCode)." }
        return $stdout.GetAwaiter().GetResult().Trim()
    } finally {
        if (-not $process.HasExited) { $process.Kill() }
        $process.Dispose()
    }
}
function New-ProtectedLogin([string] $Name, [string] $User, [string] $Password) {
    $env:C6A_EDITOR_PASSWORD = $Password
    try {
        & $Python -c @'
import os, sys, time
from winpty import PtyProcess
executable, section, user = sys.argv[1:]
password = os.environ.pop("C6A_EDITOR_PASSWORD")
editor = PtyProcess.spawn([executable, "set", f"--login-path={section}", "--host=127.0.0.1", f"--user={user}", "--password"], env=os.environ.copy())
time.sleep(0.2)
editor.write(password + "\r")
cutoff = time.monotonic() + 10
while editor.isalive() and time.monotonic() < cutoff:
    time.sleep(0.05)
if editor.isalive():
    editor.close(force=True)
    raise SystemExit("mysql_config_editor did not exit after its protected input")
editor.close()
'@ "$bin/mysql_config_editor.exe" $Name $User
        Assert-Native 'mysql_config_editor protected entry'
        $masked = (& "$bin/mysql_config_editor.exe" print "--login-path=$Name" 2>$null) -join "`n"
        $hasSection = $masked -match ('(?m)^\[' + [regex]::Escape($Name) + '\]\s*$')
        $hasUser = $masked -match ('(?m)^[ \t]*user\s*=\s*"?'+ [regex]::Escape($User) +'"?\s*$')
        $hasPassword = $masked -match '(?m)^[ \t]*password\s*=\s*"?\*+"?\s*$'
        $hasHost = $masked -match '(?m)^[ \t]*host\s*=\s*"?127\.0\.0\.1"?\s*$'
        if (-not ($hasSection -and $hasUser -and $hasPassword -and $hasHost)) {
            throw "Native protected-login provisioning failed for the fictional profile $Name (section=$hasSection, user=$hasUser, masked password=$hasPassword, host=$hasHost)."
        }
    } finally { $env:C6A_EDITOR_PASSWORD = $null }
}
function Invoke-Bootstrap([bool] $ExpectSuccess) {
    Assert-C6aPort $applicationPort
    $info = [Diagnostics.ProcessStartInfo]::new($java)
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    foreach ($arg in @('-jar',$jar,'--bootstrap')) { $info.ArgumentList.Add($arg) }
    $process = [Diagnostics.Process]::Start($info)
    try {
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        if (-not $process.WaitForExit(90000)) {
            $process.Kill()
            $null = $process.WaitForExit()
            if ($stdout.GetAwaiter().GetResult() -match 'owner initialized') {
                throw 'Offline bootstrap initialized the owner but did not exit automatically.'
            }
            throw 'Offline bootstrap did not complete within 90 seconds.'
        }
        $privateOutput = $stdout.GetAwaiter().GetResult() + $stderr.GetAwaiter().GetResult()
        if ($ExpectSuccess -and ($process.ExitCode -ne 0 -or $privateOutput -notmatch 'owner initialized')) {
            $diagnostics = [regex]::Matches($privateOutput,
                '(?im)required a bean of type ''[A-Za-z0-9.$]+''|SQL State\s*:\s*[A-Z0-9]+|Error Code\s*:\s*[0-9]+|Caused by:\s*[\w.$]+|[A-Za-z]+ denied (?:to user|for table)') |
                ForEach-Object { $_.Value } | Select-Object -Unique
            throw "Fresh bootstrap did not report success (exit $($process.ExitCode)). $($diagnostics -join ', ')"
        }
        if (-not $ExpectSuccess -and ($process.ExitCode -eq 0 -or $privateOutput -notmatch 'already initialized; bootstrap refused')) {
            throw 'Second bootstrap did not refuse the initialized workspace.'
        }
    } finally {
        if (-not $process.HasExited) { $process.Kill() }
        $process.Dispose()
        $privateOutput = $null
    }
}
function Set-ApplicationDatabase([int] $Port, [string] $Schema) {
    Assert-C6aPort $Port
    $script:applicationPort = $Port
    $env:EXT_DB_URL = "jdbc:mysql://127.0.0.1:$Port/$Schema" + '?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
    $env:EXT_DB_MIGRATION_URL = $env:EXT_DB_URL
}
function Set-RuntimeGrant([int] $Port, [string] $Schema, [string] $Password) {
    $tables = (Invoke-LocalSql $Port "SHOW TABLES FROM $Schema") -split '\r?\n'
    $grants = @("CREATE USER 'c6_runtime'@'127.0.0.1' IDENTIFIED BY '$Password';")
    foreach ($table in $tables) {
        if ($table -notmatch '^[A-Za-z_]+$') { throw 'Unexpected schema identifier.' }
        $privileges = switch ($table) {
            'audit_event' { 'SELECT, INSERT' }
            'stock_movement' { 'SELECT, INSERT' }
            'flyway_schema_history' { 'SELECT' }
            default { 'SELECT, INSERT, UPDATE, DELETE' }
        }
        $grants += "GRANT $privileges ON $Schema.$table TO 'c6_runtime'@'127.0.0.1';"
    }
    $null = Invoke-LocalSql $Port ($grants -join [Environment]::NewLine)
}
function Start-OperatorJar {
    Assert-C6aPort $applicationPort
    $script:app = Start-Process -FilePath $java -ArgumentList @('-jar', ('"' + $jar + '"')) -WindowStyle Hidden -PassThru
    for ($i=0; $i -lt 180; $i++) {
        if ($script:app.HasExited) { throw 'Operator JAR exited during startup.' }
        if (-not (Test-C6aOwnedListener $script:app ([int]$env:EXT_SERVER_PORT))) {
            Start-Sleep -Milliseconds 500
            continue
        }
        try {
            $response = Invoke-WebRequest -Uri "$base/" -TimeoutSec 2
        } catch { Start-Sleep -Milliseconds 500; continue }
        if ($response.StatusCode -eq 200) {
            if (-not (Test-C6aOwnedListener $script:app ([int]$env:EXT_SERVER_PORT))) { throw 'C6A_LISTENER_MISSING' }
            return
        }
        Start-Sleep -Milliseconds 500
    }
    throw 'Operator JAR did not serve its packaged root.'
}
function Stop-OperatorJar {
    if ($script:app -and -not $script:app.HasExited) { Stop-Process -Id $script:app.Id -Force; $null = $script:app.WaitForExit() }
    $script:app = $null
}
function Assert-WebRead([switch] $Inventory) {
    if (-not (Test-C6aOwnedListener $script:app ([int]$env:EXT_SERVER_PORT))) { throw 'C6A_LISTENER_MISSING' }
    $grep = if ($Inventory) { 'the packaged Java application serves|connected inventory UI recovers' } else { 'the packaged Java application serves' }
    Push-Location (Join-Path $root 'extended/frontend')
    try {
        & npm run e2e -- --grep $grep
        Assert-Native 'Independent first-run authenticated Chromium'
    } finally { Pop-Location }
}
function Invoke-C6aBackup([string] $Mode,[int] $Port,[string] $Schema,[string] $LoginPath,[string] $Archive) {
    Assert-C6aPort $Port
    & $backup @PSBoundParameters
}
function Assert-Refused([scriptblock] $Action, [string] $Expected) {
    try { $null = & $Action; throw 'Unexpected success.' }
    catch { if ($_.Exception.Message -notlike "$Expected*") { throw } }
}

try {
    & $Python -c 'from winpty import PtyProcess' 2>$null
    Assert-Native 'Test-only Python pywinpty availability'
    $env:MYSQL_HOME = $mysqlHome
    $env:MYSQL_TEST_LOGIN_FILE = Join-Path $temp '.mylogin.cnf'
    $env:MYSQL_PWD = $null
    $source = Start-C6aMysql 'source'
    $target = Start-C6aMysql 'target'
    $db = 'orgresource_c6a_source'
    $restored = 'orgresource_c6a_restored'
    $partial = 'orgresource_c6a_partial'
    $migratePassword = New-FixtureSecret
    $runtimePassword = New-FixtureSecret
    $backupPassword = New-FixtureSecret
    $restorePassword = New-FixtureSecret
    $limitedPassword = New-FixtureSecret
    New-ProtectedLogin 'c6-backup' 'c6_backup' $backupPassword
    New-ProtectedLogin 'c6-restore' 'c6_restore' $restorePassword
    New-ProtectedLogin 'c6-limited' 'c6_limited' $limitedPassword
    $null = Invoke-LocalSql $source @"
CREATE DATABASE $db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'c6_migrate'@'127.0.0.1' IDENTIFIED BY '$migratePassword';
GRANT ALL ON $db.* TO 'c6_migrate'@'127.0.0.1';
CREATE USER 'c6_backup'@'127.0.0.1' IDENTIFIED BY '$backupPassword';
GRANT SELECT, SHOW VIEW, TRIGGER ON $db.* TO 'c6_backup'@'127.0.0.1';
"@
    $null = Invoke-LocalSql $target @"
CREATE DATABASE $restored CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE $partial CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'c6_restore'@'127.0.0.1' IDENTIFIED BY '$restorePassword';
GRANT ALL ON $restored.* TO 'c6_restore'@'127.0.0.1';
CREATE USER 'c6_limited'@'127.0.0.1' IDENTIFIED BY '$limitedPassword';
GRANT SELECT, CREATE, REFERENCES ON $partial.* TO 'c6_limited'@'127.0.0.1';
CREATE USER 'c6_migrate'@'127.0.0.1' IDENTIFIED BY '$migratePassword';
GRANT ALL ON $restored.* TO 'c6_migrate'@'127.0.0.1';
"@
    Set-ApplicationDatabase $source $db
    $env:EXT_DB_MIGRATION_USER = 'c6_migrate'
    $env:EXT_DB_MIGRATION_PASSWORD = $migratePassword
    $env:EXT_DB_USER = $env:EXT_DB_MIGRATION_USER
    $env:EXT_DB_PASSWORD = $env:EXT_DB_MIGRATION_PASSWORD
    $env:EXT_BOOTSTRAP_SLUG = 'c6a-fictional-operator'
    $env:EXT_BOOTSTRAP_ORG_NAME = 'Fictional independent operator'
    $env:EXT_BOOTSTRAP_USERNAME = 'c6a-operator'
    $env:EXT_BOOTSTRAP_DISPLAY_NAME = 'Fictional C6a Owner'
    $env:EXT_BOOTSTRAP_PASSWORD = New-FixtureSecret
    $env:EXT_CONTACT_ENCRYPTION_KEY = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    $env:EXT_SMS_ENABLED = 'false'
    $env:EXT_REMINDER_MODE = 'DISABLED'
    $env:SPRING_PROFILES_ACTIVE = 'c1-test'
    $env:EXT_SERVER_PORT = [string](Free-C6aPort)
    $base = "http://127.0.0.1:$env:EXT_SERVER_PORT"
    Invoke-Bootstrap $true
    $null = Invoke-LocalSql $source "CREATE USER 'c6_limited'@'127.0.0.1' IDENTIFIED BY '$limitedPassword'; GRANT SELECT ON $db.department TO 'c6_limited'@'127.0.0.1';"
    Set-RuntimeGrant $source $db $runtimePassword
    $env:EXT_DB_USER = 'c6_runtime'
    $env:EXT_DB_PASSWORD = $runtimePassword
    $env:EXT_E2E_USERNAME = $env:EXT_BOOTSTRAP_USERNAME
    $env:EXT_E2E_PASSWORD = $env:EXT_BOOTSTRAP_PASSWORD
    $env:EXT_E2E_BASE_URL = $base
    $env:ORGRESOURCE_E2E_SCREENSHOT_DIR = Join-Path $temp 'screenshots'
    Start-OperatorJar
    if (-not (Test-C6aOwnedListener $script:app ([int]$env:EXT_SERVER_PORT))) { throw 'C6A_LISTENER_MISSING' }
    & "$PSScriptRoot/run-extended-c3-newman.ps1" -BaseUrl $base -Username $env:EXT_E2E_USERNAME -Password $env:EXT_E2E_PASSWORD
    Assert-WebRead -Inventory
    Stop-OperatorJar
    $null = Invoke-LocalSql $source "UPDATE $db.idempotency_key SET created_at=UTC_TIMESTAMP(6)-INTERVAL 25 HOUR,expires_at=UTC_TIMESTAMP(6)-INTERVAL 1 HOUR WHERE state='COMPLETED';"
    $receiptQuery = "SELECT COUNT(*) FROM $db.idempotency_key WHERE state='COMPLETED' AND expires_at<=UTC_TIMESTAMP(6);"
    $expiredBefore = [int](Invoke-LocalSql $source $receiptQuery)
    if ($expiredBefore -lt 1) { throw 'Expected persisted receipt fixture from the operator-owned workspace.' }
    Invoke-Bootstrap $false
    if ((Invoke-LocalSql $source "SELECT COUNT(*) FROM $db.organization;") -ne '1' -or
            [int](Invoke-LocalSql $source $receiptQuery) -ne $expiredBefore) {
        throw 'The refused offline bootstrap changed existing workspace or receipt records.'
    }
    Write-Output 'Operator first run passed: separate database identities, real bootstrap and refusal, academic Newman and authenticated Chromium.'

    $backup = "$PSScriptRoot/extended-c-backup.ps1"
    $file = Join-Path $temp 'c6-operator.aesgcm'
    Assert-Refused { Invoke-C6aBackup -Mode Backup -Port $source -Schema $db -LoginPath 'does-not-exist' -Archive $file } 'Protected login path is unavailable'
    Assert-Refused { Invoke-C6aBackup -Mode Backup -Port $source -Schema $db -LoginPath 'c6-limited' -Archive $file } 'Schema inventory differs'
    if (Test-Path -LiteralPath $file) { throw 'An incomplete backup was published.' }
    $archive = Invoke-C6aBackup -Mode Backup -Port $source -Schema $db -LoginPath 'c6-backup' -Archive $file
    $truncated = Join-Path $temp 'truncated.aesgcm'
    [IO.File]::WriteAllBytes($truncated, [byte[]]::new(16))
    Assert-Refused { Invoke-C6aBackup -Mode Restore -Port $target -Schema $restored -LoginPath 'c6-restore' -Archive $truncated } 'Backup container size is invalid'
    $oversized = Join-Path $temp 'oversized.aesgcm'
    $oversizedStream = [IO.File]::Open($oversized, [IO.FileMode]::CreateNew)
    try { $oversizedStream.SetLength(65MB) } finally { $oversizedStream.Dispose() }
    try {
        Assert-Refused { Invoke-C6aBackup -Mode Restore -Port $target -Schema $restored -LoginPath 'c6-restore' -Archive $oversized } 'Backup container size is invalid'
    } finally { Remove-Item -LiteralPath $oversized -Force }
    if ((Invoke-LocalSql $target "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$restored';") -ne '0') {
        throw 'An out-of-bounds archive modified the target.'
    }
    Assert-Refused { Invoke-C6aBackup -Mode Restore -Port $target -Schema $restored -LoginPath 'c6-limited' -Archive $file } 'mysql failed'
    Assert-Refused { Invoke-C6aBackup -Mode Restore -Port $target -Schema $partial -LoginPath 'c6-limited' -Archive $file } 'Restore did not complete'
    $partialCount = [int](Invoke-LocalSql $target "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$partial';")
    if ($partialCount -lt 1 -or $partialCount -ge 18) { throw 'SQL fixture did not exercise a genuinely partial import.' }
    $roundtrip = Invoke-C6aBackup -Mode Restore -Port $target -Schema $restored -LoginPath 'c6-restore' -Archive $file
    if ($roundtrip.Sha256 -ne $archive.Sha256) { throw 'Scoped restore archive hash changed.' }
    Assert-Refused { Invoke-C6aBackup -Mode Restore -Port $target -Schema $restored -LoginPath 'c6-restore' -Archive $file } 'Restore requires a newly created empty schema'
    $sourceCounts = Invoke-LocalSql $source "SELECT (SELECT COUNT(*) FROM $db.department),(SELECT COUNT(*) FROM $db.stock_movement),(SELECT COUNT(*) FROM $db.audit_event);"
    $targetCounts = Invoke-LocalSql $target "SELECT (SELECT COUNT(*) FROM $restored.department),(SELECT COUNT(*) FROM $restored.stock_movement),(SELECT COUNT(*) FROM $restored.audit_event);"
    if ($sourceCounts -ne $targetCounts) { throw 'The independent restored row counts differ.' }
    if ((Invoke-LocalSql $target "SELECT COUNT(*) FROM $restored.SPRING_SESSION;") -ne '0') { throw 'Sessions were restored.' }
    Set-RuntimeGrant $target $restored $runtimePassword
    Set-ApplicationDatabase $target $restored
    Start-OperatorJar
    Assert-WebRead
    Assert-C6aPort $target
    $originalTarget = $servers | Where-Object { $_.Port -eq $target -and -not $_.Process.HasExited } | Select-Object -Last 1
    & "$bin/mysqladmin.exe" --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$target" --user=root --skip-password shutdown *> $null
    Assert-Native 'Disposable target shutdown while Java is live'
    if (-not $originalTarget.Process.WaitForExit(15000)) { throw 'Disposable target did not stop.' }
    # Spring Session and the servlet error path may make separate 10-second pool acquisitions.
    if (-not (Test-C6aOwnedListener $script:app ([int]$env:EXT_SERVER_PORT))) { throw 'C6A_LISTENER_MISSING' }
    $interrupted = Invoke-WebRequest -Uri "$base/api/v2/auth/csrf" -SkipHttpErrorCheck -TimeoutSec 75
    if ($interrupted.StatusCode -lt 500) { throw 'A database-backed CSRF initialization succeeded during the outage.' }
    if ($script:app.HasExited) { throw 'Java exited during the MySQL outage.' }
    Write-Output "Stopped target MySQL refused CSRF initialization with HTTP $($interrupted.StatusCode)."
    $target = Start-C6aMysql 'target' $target
    Assert-WebRead
    Write-Output 'Java remained live through target MySQL unavailability and accepted a new authenticated browser session after restart.'
    $null = Invoke-LocalSql $source @"
INSERT INTO $db.resource (public_id, organization_id, name, name_key, type, total_quantity, image_bytes, image_content_type)
WITH RECURSIVE fixture(n) AS (SELECT 1 UNION ALL SELECT n+1 FROM fixture WHERE n<17)
SELECT UUID_TO_BIN(UUID(),0), o.id, CONCAT('C6a oversized fixture ', n), CONCAT('c6a oversized fixture ', n),
       'REUSABLE', 0, REPEAT(0x5a,2097152), 'image/png'
FROM fixture CROSS JOIN $db.organization o
WHERE o.slug='c6a-fictional-operator';
"@
    $oversizedBackup = Join-Path $temp 'oversized-backup.aesgcm'
    Assert-Refused { Invoke-C6aBackup -Mode Backup -Port $source -Schema $db -LoginPath 'c6-backup' -Archive $oversizedBackup } 'MySQL output exceeded the local recovery size limit'
    if (Test-Path -LiteralPath $oversizedBackup) { throw 'An oversized backup was published.' }
    Write-Output 'Oversized MySQL output was refused before an archive was published.'
    Write-Output "Protected-login restore passed. Archive bytes: $($archive.Bytes), SHA-256 $($archive.Sha256). Evidence directory: $temp"
} finally {
    $cleanupFailed = $false
    try {
        try { Stop-OperatorJar } catch { $cleanupFailed = $true }
        foreach ($server in $servers) {
            try { Stop-C6aServer $server } catch { $cleanupFailed = $true }
        }
        if ($cleanupFailed) { throw 'C6A_CLEANUP_INCOMPLETE: inspect retained fixture processes and protected login file privately.' }
        $loginFile = Join-Path $temp '.mylogin.cnf'
        if (Test-Path -LiteralPath $loginFile) { Remove-Item -LiteralPath $loginFile -Force }
    } finally {
        foreach ($name in $names) { [Environment]::SetEnvironmentVariable($name, $saved[$name], 'Process') }
    }
}
