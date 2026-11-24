# Requires PowerShell 7.4+ and the MySQL 8 client tools.
[CmdletBinding(DefaultParameterSetName = 'ProtectedLogin')]
param(
    [Parameter(Mandatory)][ValidateSet('Backup', 'Restore')][string] $Mode,
    [Parameter(Mandatory)][ValidatePattern('^orgresource_[a-z0-9_]{1,48}$')][string] $Schema,
    [Parameter(Mandatory)][ValidateRange(1, 65535)][int] $Port,
    [Parameter(Mandatory)][string] $Archive,
    [Parameter(Mandatory, ParameterSetName = 'ProtectedLogin')]
    [ValidatePattern('^[A-Za-z0-9_-]{1,48}$')][string] $LoginPath,
    [Parameter(Mandatory, ParameterSetName = 'DisposableAcceptance')]
    [switch] $DisposableAcceptance,
    [Parameter(Mandatory, ParameterSetName = 'DisposableDemoAcceptance')]
    [switch] $DisposableDemoAcceptance,
    [Parameter(Mandatory, ParameterSetName = 'CiIntermediateAcceptance')]
    [switch] $CiIntermediateAcceptance,
    [Parameter(Mandatory, ParameterSetName = 'LocalOriginalAcceptance')]
    [switch] $LocalOriginalAcceptance,
    [Parameter(Mandatory, ParameterSetName = 'LocalMixedAcceptance')]
    [switch] $LocalMixedAcceptance
)

$ErrorActionPreference = 'Stop'
$maxSqlBytes = 64MB
$header = [Text.Encoding]::ASCII.GetBytes('ORGRESOURCE-C5-AES256GCM-1')
$nonceOffset = $header.Length
$tagOffset = $nonceOffset + 12
$cipherOffset = $tagOffset + 16
$archivePath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Archive)
$requiredTables = @('account_recovery_code', 'allocation', 'audit_event', 'department', 'flyway_schema_history',
    'idempotency_key', 'invitation', 'local_credential', 'membership', 'organization', 'ownership_transfer',
    'project', 'resource', 'resource_requirement', 'spring_session', 'spring_session_attributes', 'stock_movement', 'user_account')
$clientUser = 'root'
$localTransport = $LocalOriginalAcceptance -or $LocalMixedAcceptance
$disposable = $DisposableAcceptance -or $DisposableDemoAcceptance -or $CiIntermediateAcceptance -or $localTransport
$dockerTransport = $CiIntermediateAcceptance -or $localTransport
# Full approved data must never be processed in public CI, including Restore.
if ($LocalMixedAcceptance -and $env:GITHUB_ACTIONS -ceq 'true') { throw 'C9_LOCAL_MIXED_CI_REFUSED' }

if (-not [Security.Cryptography.AesGcm]::IsSupported) { throw 'AES-GCM is unavailable on this runtime.' }
if ($DisposableAcceptance -and $Schema -ne 'orgresource_c1_acceptance') {
    throw 'Disposable authentication is restricted to the guarded Extended C acceptance schema.'
}
if ($DisposableDemoAcceptance -and ($Schema -notmatch '^orgresource_c8_demo_([a-f0-9]{12})$' -or $Port -lt 1024)) {
    throw 'Demo acceptance authentication requires the guarded C8 disposable schema and port.'
}
$demoSuffix = if ($DisposableDemoAcceptance) { $Matches[1] }

# CI-only transport; existing protected-login and Windows modes retain their
# schema guards, credential policy, loopback TCP and executable resolution.
$ciContainer = $null
$ciPassword = $null
function Read-C9DockerField([string] $Format, [string] $Container) {
    $value = & docker inspect --type container --format $Format $Container 2>$null
    if ($LASTEXITCODE -ne 0) { throw 'C9_CI_CONTAINER_INSPECTION_FAILED' }
    return ($value -join [Environment]::NewLine).Trim()
}
if ($CiIntermediateAcceptance) {
    if (-not $IsLinux -or $env:GITHUB_ACTIONS -cne 'true' -or $env:RUNNER_OS -cne 'Linux' -or
        $Port -ne 3306 -or $env:EXT_SMS_ENABLED -cne 'false') {
        throw 'C9_CI_LINUX_ACTIONS_REQUIRED'
    }
    $ciSource = 'orgresource_c8_additive_ci_fixture_intermediate'
    $ciTarget = 'orgresource_c8_additive_ci_fixture_c9_encrypted'
    if ($env:C9_DB_SCHEMA -cne $ciSource -or
        ($Mode -eq 'Backup' -and $Schema -cne $ciSource) -or
        ($Mode -eq 'Restore' -and $Schema -cne $ciTarget)) {
        throw 'C9_CI_SCHEMA_REQUIRED'
    }
    if ($env:MYSQL_CONTAINER -cnotmatch '^[0-9a-f]{64}$' -or
        $env:GITHUB_RUN_ID -cnotmatch '^[1-9][0-9]{1,17}$' -or
        $env:GITHUB_RUN_ATTEMPT -cnotmatch '^[1-9][0-9]{0,3}$' -or
        -not $env:EXT_DB_MIGRATION_PASSWORD -or -not $env:C9_RESTORE_PASSWORD) {
        throw 'C9_CI_RUN_IDENTITY_REQUIRED'
    }
    if (-not $env:RUNNER_TEMP -or -not $env:C9_ARCHIVE_DIR) { throw 'C9_CI_ARCHIVE_PATH_REQUIRED' }
    $runnerRoot = [IO.Path]::TrimEndingDirectorySeparator([IO.Path]::GetFullPath($env:RUNNER_TEMP))
    $ciDir = [IO.Path]::TrimEndingDirectorySeparator([IO.Path]::GetFullPath($env:C9_ARCHIVE_DIR))
    if ([IO.Path]::GetDirectoryName($ciDir) -cne $runnerRoot -or
        [IO.Path]::GetFileName($ciDir) -cnotmatch '^c9-cross-server\.[A-Za-z0-9]{8}$' -or
        -not (Test-Path -LiteralPath $ciDir -PathType Container) -or
        (Get-Item -LiteralPath $ciDir -Force).LinkType -or
        [IO.Path]::GetDirectoryName($archivePath) -cne $ciDir -or
        [IO.Path]::GetFileName($archivePath) -cnotmatch '^[a-z0-9-]+\.aesgcm$' -or
        ((Test-Path -LiteralPath $archivePath) -and (Get-Item -LiteralPath $archivePath -Force).LinkType)) {
        throw 'C9_CI_ARCHIVE_PATH_REQUIRED'
    }
    $expectedPlan = Join-Path $runnerRoot 'c8-additive-intermediate-private.json'
    if ($env:C9_PLAN_PATH -cne $expectedPlan -or $env:C9_PLAN_SHA -cnotmatch '^[0-9a-f]{64}$' -or
        -not (Test-Path -LiteralPath $expectedPlan -PathType Leaf) -or
        (Get-Item -LiteralPath $expectedPlan -Force).LinkType -or
        (Get-FileHash -LiteralPath $expectedPlan -Algorithm SHA256).Hash.ToLowerInvariant() -cne $env:C9_PLAN_SHA) {
        throw 'C9_CI_PLAN_REQUIRED'
    }
    if ((Read-C9DockerField '{{.Id}}' $env:MYSQL_CONTAINER) -cne $env:MYSQL_CONTAINER -or
        (Read-C9DockerField '{{.Config.Image}}' $env:MYSQL_CONTAINER) -cne 'mysql:8.0.46') {
        throw 'C9_CI_SOURCE_CONTAINER_REQUIRED'
    }
    if ($Mode -eq 'Backup') {
        $ciContainer = $env:MYSQL_CONTAINER
        $ciPassword = $env:EXT_DB_MIGRATION_PASSWORD
    } else {
        $ciContainer = "orgresource-c9-restorer-$($env:GITHUB_RUN_ID)-$($env:GITHUB_RUN_ATTEMPT)"
        $ciPassword = $env:C9_RESTORE_PASSWORD
        if ((Read-C9DockerField '{{.Id}}' $ciContainer) -ceq $env:MYSQL_CONTAINER -or
            (Read-C9DockerField '{{.Config.Image}}' $ciContainer) -cne 'mysql:8.0.46' -or
            (Read-C9DockerField '{{.HostConfig.NetworkMode}}' $ciContainer) -cne 'none' -or
            (Read-C9DockerField '{{index .Config.Labels "orgresource.c9.run"}}' $ciContainer) -cne "$($env:GITHUB_RUN_ID)-$($env:GITHUB_RUN_ATTEMPT)" -or
            (Read-C9DockerField '{{len .HostConfig.Binds}}' $ciContainer) -cne '0') {
            throw 'C9_CI_ISOLATED_RESTORER_REQUIRED'
        }
        $published = & docker port $ciContainer 2>$null
        if ($LASTEXITCODE -ne 0 -or $published) { throw 'C9_CI_ISOLATED_RESTORER_REQUIRED' }
    }
}

# Separate local Linux original-only proof. Explicit run-generated identities
# are accidental-target guards, not protection against a hostile Docker owner.
# No arbitrary server/port or existing production schema is accepted.
$localDockerHost = 'unix:///var/run/docker.sock'
if ($localTransport) {
    if (-not $IsLinux -or $Port -ne 3306 -or $env:EXT_SMS_ENABLED -cne 'false' -or
            $env:EXT_REMINDER_MODE -cne 'DISABLED') { throw 'C9_LOCAL_LINUX_REQUIRED' }
    if (($env:DOCKER_HOST -and $env:DOCKER_HOST -cne $localDockerHost) -or
            ($env:DOCKER_CONTEXT -and $env:DOCKER_CONTEXT -cne 'default') -or
            $env:DOCKER_TLS_VERIFY -or $env:DOCKER_CERT_PATH) { throw 'C9_LOCAL_DOCKER_ENDPOINT_REQUIRED' }
    & /usr/bin/test -S /var/run/docker.sock
    if ($LASTEXITCODE -ne 0) { throw 'C9_LOCAL_DOCKER_ENDPOINT_REQUIRED' }
    $token = $env:C9_LOCAL_TOKEN
    $localSourceRole = if ($LocalMixedAcceptance) { 'restore' } else { 'source' }
    $localRestoreRole = if ($LocalMixedAcceptance) { 'mixed' } else { 'restore' }
    $localConfirmation = if ($LocalMixedAcceptance) { "TEST MIXED $token" } else { "TEST ORIGINAL $token" }
    $uuidPattern = '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
    if ($token -cnotmatch '^[0-9a-f]{12}$' -or
            $env:C9_LOCAL_CONFIRM -cne $localConfirmation -or
            $env:C9_LOCAL_SOURCE_ID -cnotmatch '^[0-9a-f]{64}$' -or
            $env:C9_LOCAL_RESTORE_ID -cnotmatch '^[0-9a-f]{64}$' -or
            $env:C9_LOCAL_SOURCE_ID -ceq $env:C9_LOCAL_RESTORE_ID -or
            $env:C9_LOCAL_SOURCE_UUID -cnotmatch $uuidPattern -or
            $env:C9_LOCAL_RESTORE_UUID -cnotmatch $uuidPattern -or
            $env:C9_LOCAL_SOURCE_UUID -ceq $env:C9_LOCAL_RESTORE_UUID -or
            $env:C9_LOCAL_SOURCE_PASSWORD -cnotmatch '^[0-9a-f]{48}$' -or
            $env:C9_LOCAL_RESTORE_PASSWORD -cnotmatch '^[0-9a-f]{48}$' -or
            $env:C9_LOCAL_SOURCE_PASSWORD -ceq $env:C9_LOCAL_RESTORE_PASSWORD) {
        throw 'C9_LOCAL_IDENTITY_REQUIRED'
    }
    $localSourceSchema = "orgresource_c9_local_${token}_${localSourceRole}"
    $localRestoreSchema = "orgresource_c9_local_${token}_${localRestoreRole}"
    if (($Mode -eq 'Backup' -and $Schema -cne $localSourceSchema) -or
            ($Mode -eq 'Restore' -and $Schema -cne $localRestoreSchema)) {
        throw 'C9_LOCAL_SCHEMA_REQUIRED'
    }
    $localUid = (& /usr/bin/id -u)
    if ($LASTEXITCODE -ne 0 -or $localUid -notmatch '^[1-9][0-9]*$' -or
            -not $env:C9_LOCAL_ROOT -or -not [IO.Path]::IsPathFullyQualified($env:C9_LOCAL_ROOT)) {
        throw 'C9_LOCAL_ARCHIVE_PATH_REQUIRED'
    }
    $localRoot = [IO.Path]::TrimEndingDirectorySeparator([IO.Path]::GetFullPath($env:C9_LOCAL_ROOT))
    $localWork = Join-Path $localRoot "orgresource-c9-local-$token"
    foreach ($directory in @($localRoot, $localWork)) {
        $canonical = (& /usr/bin/realpath -e -- $directory 2>$null)
        if ($LASTEXITCODE -ne 0 -or $canonical -cne $directory -or
                -not (Test-Path -LiteralPath $directory -PathType Container)) {
            throw 'C9_LOCAL_ARCHIVE_PATH_REQUIRED'
        }
        $privateMode = (& /usr/bin/stat -c '%a:%u' -- $directory 2>$null)
        if ($LASTEXITCODE -ne 0 -or $privateMode -cne "700:${localUid}") {
            throw 'C9_LOCAL_ARCHIVE_PATH_REQUIRED'
        }
    }
    if ([IO.Path]::GetDirectoryName($archivePath) -cne $localWork -or
            [IO.Path]::GetFileName($archivePath) -cnotmatch '^[a-z0-9-]+\.aesgcm$') {
        throw 'C9_LOCAL_ARCHIVE_PATH_REQUIRED'
    }
    & /usr/bin/test -L $archivePath
    if ($LASTEXITCODE -eq 0 -or ((Test-Path -LiteralPath $archivePath) -and
            -not (Test-Path -LiteralPath $archivePath -PathType Leaf))) {
        throw 'C9_LOCAL_ARCHIVE_PATH_REQUIRED'
    }
    if ($LocalMixedAcceptance) {
        # Exact approved plan bytes, never accept an arbitrary tagged fixture.
        $approvedPlanSha = '3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c'
        $approvedSourceSha = '57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2'
        $approvedDataset = 'orgresource-c8-plan-v1-57f1f0c68004-20deeda9e2af'
        $planPath = Join-Path $localWork 'approved-plan.json'
        $canonical = (& /usr/bin/realpath -e -- $planPath 2>$null)
        if ($LASTEXITCODE -ne 0 -or $canonical -cne $planPath -or
                -not (Test-Path -LiteralPath $planPath -PathType Leaf) -or
                (Get-Item -LiteralPath $planPath -Force).LinkType) { throw 'C9_LOCAL_MIXED_PLAN_REQUIRED' }
        $planMode = (& /usr/bin/stat -c '%a:%u' -- $planPath 2>$null)
        if ($LASTEXITCODE -ne 0 -or $planMode -cne "600:$localUid" -or
                [IO.FileInfo]::new($planPath).Length -ne 19265850 -or
                (Get-FileHash -LiteralPath $planPath -Algorithm SHA256).Hash.ToLowerInvariant() -cne $approvedPlanSha) {
            throw 'C9_LOCAL_MIXED_PLAN_REQUIRED'
        }
        if ([IO.Path]::GetFileName($archivePath) -cne 'mixed.aesgcm') { throw 'C9_LOCAL_MIXED_ARCHIVE_REQUIRED' }
        if ($Mode -eq 'Restore' -and ($env:C9_LOCAL_MIXED_ARCHIVE_SHA -cnotmatch '^[0-9a-f]{64}$' -or
                -not (Test-Path -LiteralPath $archivePath -PathType Leaf) -or
                [IO.FileInfo]::new($archivePath).Length -gt $maxSqlBytes + $cipherOffset -or
                (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant() -cne $env:C9_LOCAL_MIXED_ARCHIVE_SHA)) {
            throw 'C9_LOCAL_MIXED_ARCHIVE_REQUIRED'
        }
    }
    foreach ($role in @($localSourceRole, $localRestoreRole)) {
        $id = if ($role -eq $localSourceRole) { $env:C9_LOCAL_SOURCE_ID } else { $env:C9_LOCAL_RESTORE_ID }
        $raw = & docker "--host=$localDockerHost" inspect --type container $id 2>$null
        if ($LASTEXITCODE -ne 0) { throw 'C9_LOCAL_CONTAINER_REQUIRED' }
        try { $records = @($raw -join [Environment]::NewLine | ConvertFrom-Json) }
        catch { throw 'C9_LOCAL_CONTAINER_REQUIRED' }
        if ($records.Count -ne 1) { throw 'C9_LOCAL_CONTAINER_REQUIRED' }
        $container = $records[0]
        if ($container.Id -cne $id -or $container.Name -cne "/orgresource-c9-local-$token-$role" -or
                $container.Config.Image -cne 'mysql:8.0.46' -or -not $container.State.Running -or
                $container.Config.Labels.'orgresource.c9.local' -cne $token -or
                $container.Config.Labels.'orgresource.c9.role' -cne $role -or
                $container.HostConfig.NetworkMode -cne 'none' -or $container.HostConfig.Privileged -or
                $container.HostConfig.PidMode -eq 'host' -or $container.HostConfig.IpcMode -eq 'host' -or
                @($container.HostConfig.Binds | Where-Object { $_ }).Count -ne 0 -or
                @($container.HostConfig.Mounts | Where-Object { $_ }).Count -ne 0 -or
                @($container.HostConfig.VolumesFrom | Where-Object { $_ }).Count -ne 0 -or
                @($container.Mounts).Count -ne 1 -or $container.Mounts[0].Type -cne 'volume' -or
                $container.Mounts[0].Destination -cne '/var/lib/mysql' -or
                $container.Mounts[0].Name -cnotmatch '^[a-f0-9]{64}$') {
            throw 'C9_LOCAL_CONTAINER_REQUIRED'
        }
        $published = & docker "--host=$localDockerHost" port $id 2>$null
        if ($LASTEXITCODE -ne 0 -or $published) { throw 'C9_LOCAL_CONTAINER_REQUIRED' }
    }
    $ciContainer = if ($Mode -eq 'Backup') { $env:C9_LOCAL_SOURCE_ID } else { $env:C9_LOCAL_RESTORE_ID }
    $ciPassword = if ($Mode -eq 'Backup') { $env:C9_LOCAL_SOURCE_PASSWORD } else { $env:C9_LOCAL_RESTORE_PASSWORD }
}

if (-not $env:EXT_BACKUP_KEY) { throw 'Protected backup key unavailable.' }
try { $key = [Convert]::FromBase64String($env:EXT_BACKUP_KEY) } catch { throw 'Protected backup key is invalid.' }
if ($key.Length -ne 32) {
    [Array]::Clear($key)
    throw 'Protected backup key must contain 32 bytes.'
}

function Find-BackupMysqlTool([string] $Tool) {
    $executable = "$Tool.exe"
    if ($env:MYSQL_HOME) { $executable = Join-Path $env:MYSQL_HOME "bin/$executable" }
    elseif ($env:ProgramFiles) {
        $standard = Join-Path $env:ProgramFiles "MySQL/MySQL Server 8.0/bin/$executable"
        if (Test-Path -LiteralPath $standard -PathType Leaf) { $executable = $standard }
    }
    return $executable
}

function Invoke-BackupMysql([string] $Tool, [string[]] $Options, [byte[]] $InputBytes, [long] $OutputLimit = $maxSqlBytes) {
    $info = [Diagnostics.ProcessStartInfo]::new()
    $info.FileName = if ($dockerTransport) { 'docker' } else { Find-BackupMysqlTool $Tool }
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardError = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardInput = $null -ne $InputBytes
    foreach ($name in @('EXT_BACKUP_KEY', 'EXT_CONTACT_ENCRYPTION_KEY', 'MYSQL_PWD', 'EXT_DB_PASSWORD',
            'EXT_DB_MIGRATION_PASSWORD', 'EXT_BOOTSTRAP_PASSWORD', 'EXT_TWILIO_AUTH_TOKEN', 'EXT_E2E_PASSWORD',
            'C9_RESTORE_PASSWORD', 'C9_LOCAL_SOURCE_PASSWORD', 'C9_LOCAL_RESTORE_PASSWORD', 'EXT_C8_BASELINE_HMAC_HEX')) {
        $null = $info.Environment.Remove($name)
    }
    if ($dockerTransport) {
        if ($localTransport) {
            foreach ($name in @('DOCKER_HOST','DOCKER_CONTEXT','DOCKER_TLS_VERIFY','DOCKER_CERT_PATH')) {
                $null = $info.Environment.Remove($name)
            }
            $info.ArgumentList.Add("--host=$localDockerHost")
        }
        if ($Tool -cnotin @('mysql','mysqldump')) { throw 'C9_CI_TOOL_REFUSED' }
        $info.Environment['MYSQL_PWD'] = $ciPassword
        foreach ($arg in @('exec','-i','--env','MYSQL_PWD',$ciContainer,$Tool,
                '--no-defaults','--protocol=socket','--user=root','--default-character-set=utf8mb4') + $Options) {
            $info.ArgumentList.Add($arg)
        }
    } else {
        $info.ArgumentList.Add('--no-defaults')
        if ($disposable) {
            $info.ArgumentList.Add('--user=root')
            $info.ArgumentList.Add('--skip-password')
        } else {
            $info.ArgumentList.Add("--login-path=$LoginPath")
            $info.ArgumentList.Add("--user=$clientUser")
        }
        foreach ($arg in @('--protocol=tcp', '--host=127.0.0.1', "--port=$Port", '--default-character-set=utf8mb4') + $Options) {
            $info.ArgumentList.Add($arg)
        }
    }
    $process = [Diagnostics.Process]::Start($info)
    $output = [IO.MemoryStream]::new()
    try {
        $stderr = $process.StandardError.ReadToEndAsync()
        if ($null -ne $InputBytes) {
            $write = $process.StandardInput.BaseStream.WriteAsync($InputBytes, 0, $InputBytes.Length)
        }
        $buffer = [byte[]]::new(65536)
        while ($true) {
            $read = $process.StandardOutput.BaseStream.ReadAsync($buffer, 0, $buffer.Length)
            if ($null -ne $InputBytes) {
                # Close stdin after the complete authenticated SQL has been sent.
                if (-not $write.Wait([TimeSpan]::FromMinutes(2))) { throw 'MySQL input timed out.' }
                $process.StandardInput.Close()
                $InputBytes = $null
            }
            if (-not $read.Wait([TimeSpan]::FromMinutes(2))) { throw 'MySQL output timed out.' }
            $length = $read.Result
            if ($length -eq 0) { break }
            if ($output.Length + $length -gt $OutputLimit) { throw 'MySQL output exceeded the local recovery size limit.' }
            $output.Write($buffer, 0, $length)
        }
        if (-not $process.WaitForExit(10000)) { throw 'MySQL did not exit after completing the backup command.' }
        $diagnostic = $stderr.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            $mysqlCode = [regex]::Match($diagnostic, '(?i)(?:got error:|error)\s+(\d{4})').Groups[1].Value
            $detail = if ($mysqlCode) { " MySQL error $mysqlCode." } else { "" }
            throw "$Tool failed with exit code $($process.ExitCode).$detail Inspect the dedicated MySQL server log securely."
        }
        return ,$output.ToArray()
    } finally {
        if (-not $process.HasExited) { $process.Kill() }
        $process.Dispose()
        $output.Dispose()
    }
}

function Read-BackupSqlScalar([string] $Sql, [string] $Database = $Schema) {
    $bytes = Invoke-BackupMysql 'mysql' @('--batch', '--skip-column-names', "--database=$Database", "--execute=$Sql") $null 4096
    return [Text.Encoding]::UTF8.GetString($bytes).Trim()
}

function Assert-LocalMixedSchema([string] $Database) {
    $tables = (Read-BackupSqlScalar 'SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY LOWER(table_name)' $Database) -split '\r?\n'
    if (@(Compare-Object $requiredTables $tables).Count -ne 0 -or
        (Read-BackupSqlScalar 'SELECT COUNT(*) FROM organization' $Database) -cne '98' -or
        (Read-BackupSqlScalar "SELECT COUNT(*) FROM organization WHERE demo_dataset_id='$approvedDataset' AND demo_source_sha='$approvedSourceSha' AND demo_plan_sha='$approvedPlanSha' AND demo_as_of='2026-11-19' AND demo_reference_key IS NOT NULL" $Database) -cne '96' -or
        (Read-BackupSqlScalar 'SELECT COUNT(DISTINCT demo_reference_key) FROM organization WHERE demo_dataset_id IS NOT NULL' $Database) -cne '96' -or
        (Read-BackupSqlScalar "SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NULL AND demo_source_sha IS NULL AND demo_plan_sha IS NULL AND demo_as_of IS NULL AND demo_reference_key IS NULL AND slug IN ('isolated-original-workspace','isolated-other-original')" $Database) -cne '2' -or
        (Read-BackupSqlScalar "SELECT COUNT(*) FROM membership m JOIN organization o ON o.id=m.org_id JOIN user_account u ON u.id=m.user_id JOIN local_credential c ON c.user_id=u.id WHERE u.username_key='additive-original-owner' AND u.status='ACTIVE' AND o.status='ACTIVE' AND m.role='OWNER' AND m.status='ACTIVE' AND (o.demo_plan_sha='$approvedPlanSha' OR o.slug='isolated-original-workspace')" $Database) -cne '97' -or
        (Read-BackupSqlScalar 'SELECT COUNT(*) FROM allocation' $Database) -cne '21060' -or
        (Read-BackupSqlScalar "SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id WHERE o.demo_plan_sha='$approvedPlanSha' AND a.action LIKE 'C8_DEMO_%'" $Database) -cne '67370' -or
        (Read-BackupSqlScalar 'SELECT COUNT(*) FROM SPRING_SESSION' $Database) -cne '0' -or
        (Read-BackupSqlScalar 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES' $Database) -cne '0') {
        throw 'C9_LOCAL_MIXED_FIXTURE_REQUIRED'
    }
}

function Assert-BackupSchema {
    $tables = (Read-BackupSqlScalar 'SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY LOWER(table_name)') -split '\r?\n'
    if (@(Compare-Object $requiredTables $tables).Count -ne 0) {
        throw 'Schema inventory differs from the supported Extended schema, or metadata is hidden by insufficient privileges.'
    }
    if ($DisposableDemoAcceptance -and (Read-BackupSqlScalar 'SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL') -ne '96') {
        throw 'The disposable C8 backup does not contain the full imported demo cohort.'
    }
    if ($LocalOriginalAcceptance) {
        if ((Read-BackupSqlScalar "SELECT COUNT(*) FROM organization") -cne '2' -or
            (Read-BackupSqlScalar "SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL OR demo_plan_sha IS NOT NULL") -cne '0' -or
            (Read-BackupSqlScalar "SELECT COUNT(*) FROM organization WHERE slug IN ('isolated-original-workspace','isolated-other-original')") -cne '2' -or
            (Read-BackupSqlScalar "SELECT COUNT(*) FROM organization o JOIN membership m ON m.org_id=o.id JOIN user_account u ON u.id=m.user_id JOIN local_credential c ON c.user_id=u.id WHERE o.slug='isolated-original-workspace' AND u.username_key='additive-original-owner' AND o.status='ACTIVE' AND u.status='ACTIVE' AND m.role='OWNER' AND m.status='ACTIVE'") -cne '1') {
            throw 'C9_LOCAL_ORIGINAL_FIXTURE_REQUIRED'
        }
    }
    if ($LocalMixedAcceptance) { Assert-LocalMixedSchema $Schema }
    if ($CiIntermediateAcceptance) {
        if ((Read-BackupSqlScalar 'SELECT VERSION()') -cne '8.0.46' -or
            (Read-BackupSqlScalar 'SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL') -cne '8' -or
            (Read-BackupSqlScalar "SELECT COUNT(*) FROM organization WHERE demo_plan_sha='$($env:C9_PLAN_SHA)'") -cne '8' -or
            (Read-BackupSqlScalar 'SELECT COUNT(*) FROM allocation') -cne '960') {
            throw 'C9_CI_INTERMEDIATE_FIXTURE_REQUIRED'
        }
    }
}

$cipher = [Security.Cryptography.AesGcm]::new($key, 16)
$sql = $null
$dump = $null
try {
    if ($localTransport) {
        # Read both actual MySQL server identities before any dump/import.
        $selectedContainer = $ciContainer
        $selectedPassword = $ciPassword
        try {
            foreach ($role in @('source','restore')) {
                $ciContainer = if ($role -eq 'source') { $env:C9_LOCAL_SOURCE_ID } else { $env:C9_LOCAL_RESTORE_ID }
                $ciPassword = if ($role -eq 'source') { $env:C9_LOCAL_SOURCE_PASSWORD } else { $env:C9_LOCAL_RESTORE_PASSWORD }
                $expectedUuid = if ($role -eq 'source') { $env:C9_LOCAL_SOURCE_UUID } else { $env:C9_LOCAL_RESTORE_UUID }
                $identityBytes = Invoke-BackupMysql 'mysql' @('--batch','--skip-column-names',"--execute=SELECT CONCAT(VERSION(),' ',@@server_uuid)") $null 4096
                if ([Text.Encoding]::UTF8.GetString($identityBytes).Trim() -cne "8.0.46 $expectedUuid") {
                    throw 'C9_LOCAL_SERVER_IDENTITY_REQUIRED'
                }
            }
        } finally {
            $ciContainer = $selectedContainer
            $ciPassword = $selectedPassword
        }
    }
    if ($LocalMixedAcceptance -and $Mode -eq 'Restore') {
        # Verify the approved mixed source again, before any target writes.
        $selectedContainer = $ciContainer
        $selectedPassword = $ciPassword
        try {
            $ciContainer = $env:C9_LOCAL_SOURCE_ID
            $ciPassword = $env:C9_LOCAL_SOURCE_PASSWORD
            Assert-LocalMixedSchema $localSourceSchema
        } finally {
            $ciContainer = $selectedContainer
            $ciPassword = $selectedPassword
        }
    }
    if (-not $disposable) {
        # The editor masks the password. Check that an explicit path exists before a
        # MySQL client could fall back to its ordinary client login group.
        $editor = Find-BackupMysqlTool 'mysql_config_editor'
        $settings = (& $editor print "--login-path=$LoginPath" 2>$null) -join "`n"
        $section = [regex]::Escape($LoginPath)
        $user = [regex]::Match($settings, '(?m)^[ \t]*user\s*=\s*"?([A-Za-z0-9_]{1,32})"?\s*$')
        if ($LASTEXITCODE -ne 0 -or $settings -cnotmatch "(?m)^\[$section\]\s*$" -or
                -not $user.Success -or $settings -notmatch '(?m)^[ \t]*password\s*=\s*"?\*+"?\s*$' -or
                $settings -notmatch '(?m)^[ \t]*host\s*=\s*"?127\.0\.0\.1"?\s*$') {
            throw 'Protected login path is unavailable or lacks an explicit loopback user and password.'
        }
        $clientUser = $user.Groups[1].Value
    }
    if ($DisposableDemoAcceptance) {
        $serverData = (Read-BackupSqlScalar 'SELECT @@datadir').Replace('\', '/').TrimEnd('/')
        $expectedFolder = '/OrgResource-C8-Demo-' + $demoSuffix + '/'
        if (!(($serverData + '/').Contains($expectedFolder)) -or $serverData -notmatch '/(?:mysql|restore-mysql)$') {
            throw 'The selected server does not use the guarded disposable C8 data directory.'
        }
    }
    if ($Mode -eq 'Backup') {
        if ([IO.File]::Exists($archivePath)) { throw 'Choose a new archive path.' }
        Assert-BackupSchema
        $options = @('--skip-comments', '--skip-dump-date', '--hex-blob', '--skip-lock-tables',
            '--skip-add-drop-table', '--no-tablespaces', '--set-gtid-purged=OFF', '--skip-column-statistics')
        $dump = Invoke-BackupMysql 'mysqldump' ($options + @('--single-transaction', '--quick',
            "--ignore-table=$Schema.SPRING_SESSION", "--ignore-table=$Schema.SPRING_SESSION_ATTRIBUTES", $Schema))
        $sessions = Invoke-BackupMysql 'mysqldump' ($options + @('--no-data', $Schema, 'SPRING_SESSION', 'SPRING_SESSION_ATTRIBUTES')) $null 1MB
        if ($dump.Length -eq 0 -or $sessions.Length -eq 0 -or $dump.Length + $sessions.Length -gt $maxSqlBytes) {
            throw 'Backup SQL is incomplete or exceeds the local recovery size limit.'
        }
        $sql = [byte[]]::new($dump.Length + $sessions.Length)
        [Array]::Copy($dump, $sql, $dump.Length)
        [Array]::Copy($sessions, 0, $sql, $dump.Length, $sessions.Length)
        $nonce = [Security.Cryptography.RandomNumberGenerator]::GetBytes(12)
        $encrypted = [byte[]]::new($sql.Length)
        $tag = [byte[]]::new(16)
        $cipher.Encrypt($nonce, $sql, $encrypted, $tag, $header)
        $stage = $archivePath + '.partial-' + [guid]::NewGuid().ToString('N')
        $file = [IO.File]::Open($stage, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write, [IO.FileShare]::None)
        try {
            foreach ($part in @($header, $nonce, $tag, $encrypted)) { $file.Write($part, 0, $part.Length) }
            $file.Flush($true)
        } finally { $file.Dispose() }
        [IO.File]::Move($stage, $archivePath)
    } else {
        $file = [IO.File]::OpenRead($archivePath)
        try {
            $length = $file.Length
            if ($length -le $cipherOffset -or $length -gt $maxSqlBytes + $cipherOffset) { throw 'Backup container size is invalid.' }
            $fileBytes = [byte[]]::new($length)
            $file.ReadExactly($fileBytes, 0, $fileBytes.Length)
        } finally { $file.Dispose() }
        if ([Text.Encoding]::ASCII.GetString($fileBytes, 0, $header.Length) -cne [Text.Encoding]::ASCII.GetString($header)) {
            throw 'Backup container header is invalid.'
        }
        $nonce = [byte[]]$fileBytes[$nonceOffset..($tagOffset - 1)]
        $tag = [byte[]]$fileBytes[$tagOffset..($cipherOffset - 1)]
        $encrypted = [byte[]]::new($length - $cipherOffset)
        [Array]::Copy($fileBytes, $cipherOffset, $encrypted, 0, $encrypted.Length)
        $sql = [byte[]]::new($encrypted.Length)
        try { $cipher.Decrypt($nonce, $encrypted, $tag, $sql, $header) }
        catch [Security.Cryptography.CryptographicException] { throw 'Backup authentication failed. No restore SQL was sent.' }

        if (-not $disposable -and (Read-BackupSqlScalar @'
SELECT COUNT(*) FROM information_schema.schema_privileges
WHERE table_schema=DATABASE() AND privilege_type='SELECT'
AND grantee=CONCAT(QUOTE(SUBSTRING_INDEX(CURRENT_USER(),'@',1)), '@', QUOTE(SUBSTRING_INDEX(CURRENT_USER(),'@',-1)))
'@) -eq '0') { throw 'Restore identity needs schema-wide SELECT to verify that the entire target is empty.' }
        if ((Read-BackupSqlScalar 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()') -ne '0') {
            throw 'Restore requires a newly created empty schema.'
        }
        try {
            $null = Invoke-BackupMysql 'mysql' @('--batch', '--binary-mode', "--database=$Schema") $sql 1024
            Assert-BackupSchema
        } catch {
            throw "Restore did not complete. The target may contain partially imported SQL and must remain isolated. $($_.Exception.Message)"
        }
    }
    [pscustomobject]@{
        Mode = $Mode
        Schema = $Schema
        Archive = $archivePath
        Bytes = [IO.FileInfo]::new($archivePath).Length
        Sha256 = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant()
    }
} finally {
    if ($sql) { [Array]::Clear($sql) }
    if ($dump) { [Array]::Clear($dump) }
    [Array]::Clear($key)
    $cipher.Dispose()
}
