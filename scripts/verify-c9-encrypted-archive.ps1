# C9 Stage 1C: exercise the ACTUAL C5 backup utility, never a second codec.
# Invoked only by the guarded cross-server acceptance harness.
$ErrorActionPreference = 'Stop'
if ($env:GITHUB_ACTIONS -cne 'true' -or $env:RUNNER_OS -cne 'Linux') {
    throw 'C9_ARCHIVE_TEST_CI_ONLY'
}
$source = 'orgresource_c8_additive_ci_fixture_intermediate'
$target = 'orgresource_c8_additive_ci_fixture_c9_encrypted'
$dir = $env:C9_ARCHIVE_DIR
if ($env:C9_DB_SCHEMA -cne $source -or -not $dir -or
    -not $env:C9_RESTORE_PASSWORD -or -not (Test-Path -LiteralPath $dir -PathType Container)) {
    throw 'C9_ARCHIVE_TEST_FIXTURE_REQUIRED'
}
$backupScript = Join-Path $PSScriptRoot 'extended-c-backup.ps1'
$archive = Join-Path $dir 'c5-intermediate.aesgcm'
$headerLength = [Text.Encoding]::ASCII.GetByteCount('ORGRESOURCE-C5-AES256GCM-1')
$overhead = $headerLength + 12 + 16
$restorer = "orgresource-c9-restorer-$($env:GITHUB_RUN_ID)-$($env:GITHUB_RUN_ATTEMPT)"
$backupParameters = @{ Mode='Backup'; Schema=$source; Port=3306; Archive=$archive; CiIntermediateAcceptance=$true }
$restoreParameters = @{ Mode='Restore'; Schema=$target; Port=3306; Archive=$archive; CiIntermediateAcceptance=$true }

function Read-TargetScalar([string] $Sql) {
    $oldPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $env:C9_RESTORE_PASSWORD
        $result = & docker exec -i --env MYSQL_PWD $restorer mysql --no-defaults --protocol=socket -uroot --batch --skip-column-names "--database=$target" "--execute=$Sql" 2>$null
        if ($LASTEXITCODE -ne 0) { throw 'C9_ARCHIVE_TEST_TARGET_READ_FAILED' }
        return ($result -join [Environment]::NewLine).Trim()
    } finally { $env:MYSQL_PWD = $oldPassword }
}
function Assert-EmptyTarget {
    if ((Read-TargetScalar 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()') -cne '0') {
        throw 'C9_ARCHIVE_DENIAL_CHANGED_TARGET'
    }
}
function Assert-Refusal([string] $Case, [hashtable] $Parameters, [string] $Expected, [switch] $Populated) {
    $refused = $false
    try { $null = & $backupScript @Parameters }
    catch {
        if (-not $_.Exception.Message.StartsWith($Expected, [StringComparison]::Ordinal)) {
            throw "C9_ARCHIVE_WRONG_REFUSAL:$Case"
        }
        $refused = $true
    }
    if (-not $refused) { throw "C9_ARCHIVE_UNEXPECTED_SUCCESS:$Case" }
    if (-not $Populated) { Assert-EmptyTarget }
    Write-Output "C9_ARCHIVE_REFUSAL_OK:$Case"
}
function With-Archive([string] $Path) {
    $parameters = $restoreParameters.Clone()
    $parameters.Archive = $Path
    return $parameters
}
function New-TamperedArchive([string] $Name, [int] $Offset) {
    $path = Join-Path $dir "$Name.aesgcm"
    $bytes = [byte[]]$archiveBytes.Clone()
    $bytes[$Offset] = $bytes[$Offset] -bxor 1
    [IO.File]::WriteAllBytes($path, $bytes)
    return $path
}
$originalKey = $env:EXT_BACKUP_KEY
$key = [Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
$wrongKey = [Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
$archiveBytes = $null
try {
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String($key)
    Assert-EmptyTarget
    $badSchema = $restoreParameters.Clone()
    $badSchema.Schema = 'orgresource_c1_ci'
    Assert-Refusal 'wrong-schema' $badSchema 'C9_CI_SCHEMA_REQUIRED'
    $outside = $backupParameters.Clone()
    $outside.Archive = Join-Path (Split-Path -Parent $dir) 'c9-outside-workdir.aesgcm'
    Assert-Refusal 'outside-private-workdir' $outside 'C9_CI_ARCHIVE_PATH_REQUIRED'
    $originalDigest = $env:C9_PLAN_SHA
    try {
        $env:C9_PLAN_SHA = '0' * 64
        Assert-Refusal 'wrong-plan-hash' $backupParameters 'C9_CI_PLAN_REQUIRED'
    } finally { $env:C9_PLAN_SHA = $originalDigest }

    $clock = [Diagnostics.Stopwatch]::StartNew()
    $backupRecord = & $backupScript @backupParameters
    $clock.Stop()
    $backupMs = $clock.ElapsedMilliseconds
    $archiveBytes = [IO.File]::ReadAllBytes($archive)
    if ($backupRecord.Bytes -ne $archiveBytes.Length -or
        $archiveBytes.Length -le $overhead -or $archiveBytes.Length -gt 64MB + $overhead -or
        [Text.Encoding]::ASCII.GetString($archiveBytes, 0, $headerLength) -cne 'ORGRESOURCE-C5-AES256GCM-1') {
        throw 'C9_ARCHIVE_FORMAT_OR_SIZE_MISMATCH'
    }
    $archiveSha = (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($backupRecord.Sha256 -cne $archiveSha) { throw 'C9_ARCHIVE_DIGEST_MISMATCH' }
    Assert-Refusal 'existing-archive' $backupParameters 'Choose a new archive path.'
    if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant() -cne $archiveSha) {
        throw 'C9_ARCHIVE_EXISTING_FILE_CHANGED'
    }

    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String($wrongKey)
    Assert-Refusal 'wrong-key' $restoreParameters 'Backup authentication failed.'
    $env:EXT_BACKUP_KEY = $null
    Assert-Refusal 'missing-key' $restoreParameters 'Protected backup key unavailable.'
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String([byte[]]::new(31))
    Assert-Refusal 'invalid-key-length' $restoreParameters 'Protected backup key must contain 32 bytes.'
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String($key)
    Assert-Refusal 'header' (With-Archive (New-TamperedArchive 'header' 0)) 'Backup container header is invalid.'
    Assert-Refusal 'nonce' (With-Archive (New-TamperedArchive 'nonce' $headerLength)) 'Backup authentication failed.'
    Assert-Refusal 'tag' (With-Archive (New-TamperedArchive 'tag' ($headerLength + 12))) 'Backup authentication failed.'
    Assert-Refusal 'ciphertext' (With-Archive (New-TamperedArchive 'ciphertext' ($archiveBytes.Length - 1))) 'Backup authentication failed.'
    $truncated = Join-Path $dir 'truncated.aesgcm'
    $bytes = [byte[]]::new($archiveBytes.Length - 1)
    [Array]::Copy($archiveBytes, $bytes, $bytes.Length)
    [IO.File]::WriteAllBytes($truncated, $bytes)
    Assert-Refusal 'truncated' (With-Archive $truncated) 'Backup authentication failed.'
    $short = Join-Path $dir 'header-only.aesgcm'
    [IO.File]::WriteAllBytes($short, [byte[]]::new($overhead))
    Assert-Refusal 'undersize' (With-Archive $short) 'Backup container size is invalid.'
    $large = Join-Path $dir 'oversize.aesgcm'
    $file = [IO.File]::Open($large, [IO.FileMode]::CreateNew)
    try { $file.SetLength(64MB + $overhead + 1) } finally { $file.Dispose() }
    Assert-Refusal 'oversize' (With-Archive $large) 'Backup container size is invalid.'
    Remove-Item -LiteralPath $large
    $clock.Restart()
    $restoreRecord = & $backupScript @restoreParameters
    $clock.Stop()
    $restoreMs = $clock.ElapsedMilliseconds
    if ($restoreRecord.Sha256 -cne $archiveSha -or $restoreRecord.Bytes -ne $backupRecord.Bytes) {
        throw 'C9_ARCHIVE_RESTORE_IDENTITY_MISMATCH'
    }
    if ((Read-TargetScalar 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()') -cne '18' -or
        (Read-TargetScalar 'SELECT COUNT(*) FROM SPRING_SESSION') -cne '0' -or
        (Read-TargetScalar 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES') -cne '0') {
        throw 'C9_ARCHIVE_RESTORED_SCHEMA_OR_SESSION_MISMATCH'
    }
    Assert-Refusal 'nonempty-target' $restoreParameters 'Restore requires a newly created empty schema.' -Populated
    Write-Output "C9_ACTUAL_C5_ARCHIVE_OK: archive_bytes=$($backupRecord.Bytes) sql_payload_bytes=$($backupRecord.Bytes - $overhead) backup_ms=$backupMs restore_ms=$restoreMs negative_cases=15 empty_sessions=true"
} finally {
    $env:EXT_BACKUP_KEY = $originalKey
    [Array]::Clear($key)
    [Array]::Clear($wrongKey)
    if ($archiveBytes) { [Array]::Clear($archiveBytes) }
}
