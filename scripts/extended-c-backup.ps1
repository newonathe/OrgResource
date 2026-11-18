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
    [switch] $DisposableAcceptance
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

if (-not [Security.Cryptography.AesGcm]::IsSupported) { throw 'AES-GCM is unavailable on this runtime.' }
if ($DisposableAcceptance -and $Schema -ne 'orgresource_c1_acceptance') {
    throw 'Disposable authentication is restricted to the guarded Extended C acceptance schema.'
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
    $info.FileName = Find-BackupMysqlTool $Tool
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardError = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardInput = $null -ne $InputBytes
    foreach ($name in @('EXT_BACKUP_KEY', 'EXT_CONTACT_ENCRYPTION_KEY', 'MYSQL_PWD', 'EXT_DB_PASSWORD',
            'EXT_DB_MIGRATION_PASSWORD', 'EXT_BOOTSTRAP_PASSWORD', 'EXT_TWILIO_AUTH_TOKEN', 'EXT_E2E_PASSWORD')) {
        $null = $info.Environment.Remove($name)
    }
    $info.ArgumentList.Add('--no-defaults')
    if ($DisposableAcceptance) {
        $info.ArgumentList.Add('--user=root')
        $info.ArgumentList.Add('--skip-password')
    } else {
        $info.ArgumentList.Add("--login-path=$LoginPath")
        $info.ArgumentList.Add("--user=$clientUser")
    }
    foreach ($arg in @('--protocol=tcp', '--host=127.0.0.1', "--port=$Port", '--default-character-set=utf8mb4') + $Options) {
        $info.ArgumentList.Add($arg)
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

function Read-BackupSqlScalar([string] $Sql) {
    $bytes = Invoke-BackupMysql 'mysql' @('--batch', '--skip-column-names', "--database=$Schema", "--execute=$Sql") $null 4096
    return [Text.Encoding]::UTF8.GetString($bytes).Trim()
}

function Assert-BackupSchema {
    $tables = (Read-BackupSqlScalar 'SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY LOWER(table_name)') -split '\r?\n'
    if (@(Compare-Object $requiredTables $tables).Count -ne 0) {
        throw 'Schema inventory differs from the supported Extended schema, or metadata is hidden by insufficient privileges.'
    }
}

$cipher = [Security.Cryptography.AesGcm]::new($key, 16)
$sql = $null
$dump = $null
try {
    if (-not $DisposableAcceptance) {
        # The editor masks the password. Check that an explicit path exists before a
        # MySQL client could fall back to its ordinary client login group.
        $editor = Find-BackupMysqlTool 'mysql_config_editor'
        $settings = (& $editor print "--login-path=$LoginPath" 2>$null) -join "`n"
        $section = [regex]::Escape($LoginPath)
        $user = [regex]::Match($settings, '(?m)^user\s*=\s*([A-Za-z0-9_]{1,32})\s*$')
        if ($LASTEXITCODE -ne 0 -or $settings -notmatch "(?m)^\[$section\]\s*$" -or
                -not $user.Success -or $settings -notmatch '(?m)^password\s*=\s*\*+\s*$' -or
                $settings -notmatch '(?m)^host\s*=\s*127\.0\.0\.1\s*$') {
            throw 'Protected login path is unavailable or lacks an explicit loopback user and password.'
        }
        $clientUser = $user.Groups[1].Value
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

        if (-not $DisposableAcceptance -and (Read-BackupSqlScalar @'
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
