[CmdletBinding()]
param(
    [Parameter(Mandatory)][string] $Plan,
    [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{64}$')][string] $Sha256,
    [string] $ReuseProofFolder
)

$ErrorActionPreference = 'Stop'
$Plan = (Resolve-Path -LiteralPath $Plan).Path
if ((Get-FileHash -LiteralPath $Plan -Algorithm SHA256).Hash -ne $Sha256) { throw 'The source plan checksum differs.' }
$repo = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $repo 'extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar'
if (!(Test-Path -LiteralPath $jar -PathType Leaf)) { throw 'Build and verify the Extended Java 21 JAR first.' }
$mysqlHome = if ($env:MYSQL_HOME) { $env:MYSQL_HOME } else { Join-Path $env:ProgramFiles 'MySQL/MySQL Server 8.0' }
$mysql = Join-Path $mysqlHome 'bin/mysql.exe'
$mysqlAdmin = Join-Path $mysqlHome 'bin/mysqladmin.exe'
$mysqlServer = Join-Path $mysqlHome 'bin/mysqld.exe'
$dump = Join-Path $mysqlHome 'bin/mysqldump.exe'
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java.exe' }
$suffix = [Guid]::NewGuid().ToString('N').Substring(0,12)
$rootDirectory = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-C8-Demo-' + $suffix)
if ($ReuseProofFolder) {
    $rootDirectory = (Resolve-Path -LiteralPath $ReuseProofFolder).Path
    $leaf = Split-Path -Leaf $rootDirectory
    if ($leaf -notmatch '^OrgResource-C8-Demo-([a-f0-9]{12})$') { throw 'Select a guarded disposable demo proof folder.' }
    $suffix = $Matches[1]
    $expected = Join-Path ([IO.Path]::GetTempPath()) $leaf
    if ($rootDirectory -ne $expected -or
        !(Select-String -LiteralPath (Join-Path $rootDirectory 'import.log') -SimpleMatch 'C8 local demo import reconciled.')) {
        throw 'The demo proof folder is outside the temporary directory or lacks its completed import log.'
    }
}
$target = 'orgresource_c8_demo_' + $suffix
$datadir = Join-Path $rootDirectory 'mysql'
$environmentNames = @('EXT_C8_DEMO_TARGET','EXT_C8_DEMO_CONFIRM_TARGET','EXT_C8_DEMO_PORT','EXT_C8_DEMO_PLAN',
    'EXT_C8_DEMO_PLAN_SHA256','EXT_C8_DEMO_OWNER_PASSWORD','EXT_DB_USER','EXT_DB_PASSWORD','EXT_DB_MIGRATION_USER',
    'EXT_DB_MIGRATION_PASSWORD','EXT_BACKUP_KEY','MYSQL_PWD')
$saved = @{}
foreach ($name in $environmentNames) { $saved[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }
$dbProcess = $null
$restoreProcess = $null
$sqlDump = Join-Path $rootDirectory 'disposable-backup.sql'
$timer = [Diagnostics.Stopwatch]::new()
function Require-NativeExit([string] $Action) {
    if ($LASTEXITCODE -ne 0) { throw "$Action failed with exit code $LASTEXITCODE." }
}
function Get-LoopbackPort {
    $listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback,0)
    try { $listener.Start(); return $listener.LocalEndpoint.Port } finally { $listener.Stop() }
}
function Invoke-RootSql([string] $Sql, [int] $Port = $databasePort) {
    $Sql | & $mysql --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$Port" --user=root --batch --skip-column-names --raw
    Require-NativeExit 'Disposable MySQL query'
}
function Assert-DatabaseCount([string] $Sql, [long] $Expected, [int] $Port = $databasePort) {
    $actual = Invoke-RootSql "USE $target; $Sql" $Port
    if ([long]$actual -ne $Expected) { throw "Database verification expected $Expected, observed $actual." }
}

function Start-DisposableMysql([string] $Location, [int] $Port, [bool] $Existing = $false) {
    if ($Existing) {
        if (!(Test-Path -LiteralPath (Join-Path $Location 'mysql.ibd') -PathType Leaf)) {
            throw 'The selected proof has no MySQL datadir.'
        }
    } else {
        New-Item -ItemType Directory -Path $Location | Out-Null
        $init = Start-Process -FilePath $mysqlServer -ArgumentList @('--no-defaults',('--basedir="' + $mysqlHome + '"'),
            ('--datadir="' + $Location + '"'),'--initialize-insecure','--console') -WindowStyle Hidden -Wait -PassThru
        if ($init.ExitCode -ne 0) { throw 'Fresh demo verification MySQL initialization failed.' }
    }
    $process = Start-Process -FilePath $mysqlServer -ArgumentList @('--no-defaults',('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $Location + '"'),"--port=$Port",'--bind-address=127.0.0.1','--mysqlx=0',
        ('--log-error="' + (Join-Path $Location 'mysql-error.log') + '"')) -WindowStyle Hidden -PassThru
    try {
        for ($attempt = 0; $attempt -lt 90; $attempt++) {
            if ($process.HasExited) { throw 'Disposable MySQL exited before import.' }
            & $mysqlAdmin --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$Port" --user=root ping *> $null
            if ($LASTEXITCODE -eq 0) { return $process }
            Start-Sleep -Milliseconds 500
        }
        throw 'Disposable MySQL startup timed out.'
    } catch {
        if (!$process.HasExited) { Stop-Process -Id $process.Id -Force }
        throw
    }
}
function Stop-DisposableMysql($Process, [int] $Port) {
    if ($Process -and !$Process.HasExited) {
        & $mysqlAdmin --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$Port" --user=root shutdown *> $null
        if (!$Process.WaitForExit(15000)) { Stop-Process -Id $Process.Id -Force }
    }
}

try {
    $env:MYSQL_PWD = $null
    if (!$ReuseProofFolder) { New-Item -ItemType Directory -Path $rootDirectory | Out-Null }
    $databasePort = Get-LoopbackPort
    $dbProcess = Start-DisposableMysql $datadir $databasePort ([bool]$ReuseProofFolder)
    if (!$ReuseProofFolder) {
        $appPassword = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
        $migrationPassword = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
        Invoke-RootSql @"
CREATE DATABASE $target CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'c8_demo_runtime'@'127.0.0.1' IDENTIFIED BY '$appPassword';
CREATE USER 'c8_demo_migrator'@'127.0.0.1' IDENTIFIED BY '$migrationPassword';
GRANT ALL PRIVILEGES ON $target.* TO 'c8_demo_migrator'@'127.0.0.1';
GRANT SELECT,INSERT,UPDATE,DELETE ON $target.* TO 'c8_demo_runtime'@'127.0.0.1';
"@
        $env:EXT_C8_DEMO_TARGET = $target
        $env:EXT_C8_DEMO_CONFIRM_TARGET = "IMPORT $target"
        $env:EXT_C8_DEMO_PORT = "$databasePort"
        $env:EXT_C8_DEMO_PLAN = $Plan
        $env:EXT_C8_DEMO_PLAN_SHA256 = $Sha256
        $env:EXT_C8_DEMO_OWNER_PASSWORD = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
        $env:EXT_DB_USER = 'c8_demo_runtime'
        $env:EXT_DB_PASSWORD = $appPassword
        $env:EXT_DB_MIGRATION_USER = 'c8_demo_migrator'
        $env:EXT_DB_MIGRATION_PASSWORD = $migrationPassword
        $output = Join-Path $rootDirectory 'import.log'
        Write-Output "Guarded import proof log $output"
        $timer.Start()
        & $java -Xmx768m -jar $jar --c8-local-demo-import *> $output
        Require-NativeExit 'Guarded full historical import'
        $timer.Stop()
    }
    Assert-DatabaseCount "SELECT COUNT(*) FROM organization WHERE demo_plan_sha='$Sha256';" 96
    Assert-DatabaseCount 'SELECT COUNT(*) FROM membership;' 462
    Assert-DatabaseCount 'SELECT COUNT(*) FROM local_credential;' 1
    Assert-DatabaseCount 'SELECT COUNT(*) FROM department;' 248
    Assert-DatabaseCount 'SELECT COUNT(*) FROM project;' 713
    Assert-DatabaseCount 'SELECT COUNT(*) FROM resource;' 1312
    Assert-DatabaseCount 'SELECT COUNT(*) FROM resource_requirement;' 2139
    Assert-DatabaseCount 'SELECT COUNT(*) FROM allocation;' 21060
    Assert-DatabaseCount 'SELECT COUNT(*) FROM allocation WHERE sms_consent OR phone_ciphertext IS NOT NULL;' 0
    Assert-DatabaseCount @'
SELECT COUNT(*) FROM resource r
LEFT JOIN (SELECT organization_id,resource_id,SUM(delta_total) AS total,SUM(delta_issued) AS issued
    FROM stock_movement GROUP BY organization_id,resource_id) m
    ON m.organization_id=r.organization_id AND m.resource_id=r.id
LEFT JOIN (SELECT a.organization_id,a.resource_id,
    SUM(CASE WHEN a.status='APPROVED' AND p.end_at>'2026-11-19 00:00:00' THEN a.quantity ELSE 0 END) AS holds,
    SUM(CASE WHEN a.status='ISSUED' THEN a.quantity ELSE 0 END) AS issued
    FROM allocation a JOIN resource_requirement q ON q.id=a.requirement_id AND q.organization_id=a.organization_id
    JOIN project p ON p.id=q.project_id AND p.organization_id=a.organization_id
    GROUP BY a.organization_id,a.resource_id) a ON a.organization_id=r.organization_id AND a.resource_id=r.id
WHERE r.total_quantity<>COALESCE(m.total,0) OR COALESCE(m.issued,0)<>COALESCE(a.issued,0)
    OR COALESCE(a.issued,0)+COALESCE(a.holds,0)>r.total_quantity;
'@ 0
    Assert-DatabaseCount @'
SELECT COUNT(*) FROM resource_requirement q JOIN (
    SELECT a.organization_id,a.requirement_id,SUM(a.quantity) AS covered
    FROM allocation a JOIN resource_requirement rr ON rr.id=a.requirement_id AND rr.organization_id=a.organization_id
    JOIN project p ON p.id=rr.project_id AND p.organization_id=a.organization_id
    WHERE a.status IN ('ISSUED','RETURNED','CONSUMED') OR (a.status='APPROVED' AND p.end_at>'2026-11-19 00:00:00')
    GROUP BY a.organization_id,a.requirement_id) c ON c.organization_id=q.organization_id AND c.requirement_id=q.id
WHERE c.covered>q.quantity;
'@ 0
    $totals = [Text.Json.JsonDocument]::Parse([IO.File]::ReadAllText($Plan))
    try {
        $states = $totals.RootElement.GetProperty('final_states')
        $expectedMovements = 1312 + $states.GetProperty('CONSUMED').GetInt32() +
            $states.GetProperty('ISSUED').GetInt32() + 2 * $states.GetProperty('RETURNED').GetInt32()
        Assert-DatabaseCount 'SELECT COUNT(*) FROM stock_movement;' $expectedMovements
        foreach ($state in @('APPROVED','CANCELLED','CONSUMED','ISSUED','PENDING','RETURNED')) {
            Assert-DatabaseCount "SELECT COUNT(*) FROM allocation WHERE status='$state';" $states.GetProperty($state).GetInt32()
        }
    } finally { $totals.Dispose() }
    if ($ReuseProofFolder) { Write-Output 'Full independent SQL counts and Allocation states passed after MySQL restart.' }
    else { Write-Output "Full independent SQL counts and Allocation states passed in $($timer.Elapsed.TotalSeconds) import seconds." }

    if (!$ReuseProofFolder) {
        $blocked = Join-Path $rootDirectory 'repeat-refusal.log'
        & $java -Xmx256m -jar $jar --c8-local-demo-import *> $blocked
        if ($LASTEXITCODE -eq 0 -or !(Select-String -LiteralPath $blocked -SimpleMatch 'already contains tables or does not match')) {
            throw 'A repeated import did not refuse the populated schema before writes.'
        }
        Assert-DatabaseCount 'SELECT COUNT(*) FROM allocation;' 21060
    }
    $measure = [Diagnostics.Stopwatch]::StartNew()
    $dumpArgs = @('--no-defaults','--protocol=tcp','--host=127.0.0.1',"--port=$databasePort",'--user=root',
        '--single-transaction','--skip-lock-tables','--no-tablespaces','--set-gtid-purged=OFF',"--result-file=$sqlDump",$target)
    & $dump @dumpArgs
    Require-NativeExit 'Disposable raw backup size measurement'
    $measure.Stop()
    $bytes = (Get-Item -LiteralPath $sqlDump).Length
    Write-Output "Raw MySQL dump bytes $bytes. Dump seconds $($measure.Elapsed.TotalSeconds). Existing backup ceiling 67108864 bytes."
    if ($bytes -gt 64MB) { throw 'The full dataset exceeds the existing authenticated backup size limit.' }
    Remove-Item -LiteralPath $sqlDump
    $restorePort = Get-LoopbackPort
    $restoreProcess = Start-DisposableMysql (Join-Path $rootDirectory 'restore-mysql') $restorePort
    Invoke-RootSql "CREATE DATABASE $target CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;" $restorePort
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    $archive = Join-Path $rootDirectory 'full-demo-recovery.aesgcm'
    $recovery = [Diagnostics.Stopwatch]::StartNew()
    & (Join-Path $PSScriptRoot 'extended-c-backup.ps1') -Mode Backup -Schema $target -Port $databasePort -Archive $archive -DisposableDemoAcceptance
    & (Join-Path $PSScriptRoot 'extended-c-backup.ps1') -Mode Restore -Schema $target -Port $restorePort -Archive $archive -DisposableDemoAcceptance
    $recovery.Stop()
    $fingerprint = @"
USE $target;
SELECT COUNT(*),SUM(total_quantity),BIT_XOR(CRC32(CONCAT(public_id,':',total_quantity))) FROM resource;
SELECT COUNT(*),SUM(quantity),BIT_XOR(CRC32(CONCAT(public_id,':',status,':',quantity,':',borrower_membership_id))) FROM allocation;
SELECT COUNT(*),SUM(delta_total),SUM(delta_issued),BIT_XOR(CRC32(CONCAT(operation_public_id,':',kind,':',occurred_at))) FROM stock_movement;
SELECT COUNT(*),BIT_XOR(CRC32(CONCAT(public_id,':',demo_reference_key,':',demo_plan_sha))) FROM organization;
"@
    if (@(Compare-Object @(Invoke-RootSql $fingerprint) @(Invoke-RootSql $fingerprint $restorePort)).Count) {
        throw 'Restored row counts, stock sums, or logical checksums differ.'
    }
    Assert-DatabaseCount 'SELECT COUNT(*) FROM allocation;' 21060 $restorePort
    Write-Output "Separate MySQL encrypted full-fixture backup and restore passed in $($recovery.Elapsed.TotalSeconds) seconds."
    Write-Output "Import proof logs and stopped disposable MySQL datadir $rootDirectory"
} finally {
    if (Test-Path -LiteralPath $sqlDump -PathType Leaf) { Remove-Item -LiteralPath $sqlDump }
    Stop-DisposableMysql $restoreProcess $restorePort
    Stop-DisposableMysql $dbProcess $databasePort
    foreach ($name in $environmentNames) { [Environment]::SetEnvironmentVariable($name, $saved[$name], 'Process') }
}
