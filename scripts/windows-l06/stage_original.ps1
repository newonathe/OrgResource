# Dot-sourced stage: actual bootstrap, C5 original backup and independent restore, actual baseline, importer grants.
$bootstrapPassword = New-Secret; $migratePassword = New-Secret; $ownerPassword = New-Secret; $roPassword = New-Secret; $hmacKey = New-Secret 32
$backupKey = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)); $contactKey = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))

$script:phase = 'bootstrap'
Start-Server 'source'; Start-Server 'restore'
# Assert-Role must refuse a listener that is not the held process.
$realPort = $roles.source.Port; $foreign0 = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0); $foreign0.Start()
$refused = $false; try { $roles.source.Port = $foreign0.LocalEndpoint.Port; Assert-Role 'source' } catch { $refused = $_.Exception.GetBaseException().Message -ceq 'R06B1_CONTAINER_BOUNDARY' } finally { $roles.source.Port = $realPort; $foreign0.Stop() }
if (-not $refused) { Fail 'R06B1_ROLE_SELF_TEST' }
Assert-Role 'source'
if ($roles.source.Uuid -ceq $roles.restore.Uuid) { Fail 'R06B1_SERVER_UUID' }
$s = $roles.source.Schema; $t = $roles.restore.Schema
$null = Invoke-Root 'source' "CREATE USER 'c9_bootstrap'@'%' IDENTIFIED BY '$bootstrapPassword'; CREATE USER 'c9_migrator'@'%' IDENTIFIED BY '$migratePassword'; GRANT SELECT,INSERT,UPDATE,DELETE ON $s.* TO 'c9_bootstrap'@'%'; GRANT ALL PRIVILEGES ON $s.* TO 'c9_migrator'@'%';"
$log = "$work\dumps\bootstrap.log"
$code = Invoke-Jar 'source' @{ EXT_DB_URL = (Get-Url 'source'); EXT_DB_MIGRATION_URL = (Get-Url 'source'); EXT_DB_USER = 'c9_bootstrap'; EXT_DB_PASSWORD = $bootstrapPassword; EXT_DB_MIGRATION_USER = 'c9_migrator'; EXT_DB_MIGRATION_PASSWORD = $migratePassword
    EXT_CONTACT_ENCRYPTION_KEY = $contactKey; EXT_BOOTSTRAP_SLUG = 'isolated-original-workspace'; EXT_BOOTSTRAP_ORG_NAME = 'Isolated Original Workspace'; EXT_BOOTSTRAP_USERNAME = 'additive-original-owner'
    EXT_BOOTSTRAP_DISPLAY_NAME = 'Fictional Original Owner'; EXT_BOOTSTRAP_PASSWORD = $ownerPassword } '--bootstrap' $log
if ($code -ne 0) { Fail 'R06B1_BOOTSTRAP_FAILED' }
if (-not (Test-Log $log 'owner initialized')) { Fail 'R06B1_BOOTSTRAP_MARKER' }
Remove-Item -LiteralPath $log
$null = Invoke-Root 'source' "DROP USER 'c9_bootstrap'@'%','c9_migrator'@'%';"
# The fictional original rows come verbatim from the pinned Linux runner, not from a second copy.
$linux = (& git -C $repo show "${SourceSha}:scripts/verify-c9-local-mixed-preparation.sh") -join "`n"
$fixture = [regex]::Match($linux, "(?s)mysql_at `"\`$source_id`" `"\`$src_password`" `"\`$source_schema`" <<'SQL'\n(.*?)\nSQL\n")
if (-not $fixture.Success -or -not $fixture.Groups[1].Value.StartsWith('INSERT INTO department')) { Fail 'R06B1_ORIGINAL_FIXTURE' }
$null = Invoke-Root 'source' $fixture.Groups[1].Value $s
$originalOrg = Get-Scalar 'source' "SELECT BIN_TO_UUID(public_id,0) FROM organization WHERE slug='isolated-original-workspace'"
$ownerId = Get-Scalar 'source' "SELECT BIN_TO_UUID(public_id,0) FROM user_account WHERE username_key='additive-original-owner'"
if ($originalOrg -cnotmatch '^[a-f0-9-]{36}$' -or $ownerId -cnotmatch '^[a-f0-9-]{36}$' -or (Get-Scalar 'source' 'SELECT COUNT(*) FROM organization') -cne '2' -or
    (Get-Scalar 'source' "SELECT COUNT(*) FROM audit_event WHERE action='BOOTSTRAP_OWNER_CREATED'") -cne '1' -or (Get-Scalar 'source' 'SELECT COUNT(*) FROM SPRING_SESSION') -cne '0' -or
    (Get-Scalar 'source' 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES') -cne '0') { Fail 'R06B1_ORIGINAL_FIXTURE' }
Write-Marker 'R06B1_BOOTSTRAP_OK_WIN:original_orgs=2 actual_bootstrap=true bootstrap_audit=true sessions_empty=true'

$script:phase = 'original_restore'
$sourceBefore = Save-Rows 'source' 'source-before'
Assert-Identity 'source', 'restore'; Assert-OriginalFixture 'source'
Grant-Transport 'source' 'backup'; Grant-Transport 'restore' 'restore'
if ((Invoke-C5 'Backup' 'source' 'c9-backup' $backupKey 'original.aesgcm' "$work\dumps\c5-backup.log") -ne 0) { Fail 'R06B1_BACKUP_FAILED' }
$archive = "$work\source\original.aesgcm"; $archiveSha = Get-Sha $archive; $archiveBytes = (Get-Item $archive).Length
if ($archiveBytes -le 54 -or $archiveBytes -gt $maxArchive -or -not (Test-OwnerOnly $archive @($roles.source.Sid))) { Fail 'R06B1_BACKUP_FAILED' }
# Archive refusals on the still-empty independent server, each leaving it empty.
$bytes = [IO.File]::ReadAllBytes($archive)
foreach ($case in 'tamper', 'header', 'truncated', 'key', 'cap') {
    $bad = [byte[]]$bytes.Clone(); $key = $backupKey
    switch ($case) {
        'tamper' { $bad[$bad.Length - 1] = $bad[$bad.Length - 1] -bxor 1 }
        'header' { $bad[0] = $bad[0] -bxor 1 }
        'truncated' { $bad = [byte[]]$bad[0..53] }
        'key' { $key = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) }
    }
    if ($case -ceq 'cap') { $fs = [IO.File]::Create("$work\restore\bad.aesgcm"); $fs.SetLength($maxArchive + 1); $fs.Dispose() } else { [IO.File]::WriteAllBytes("$work\restore\bad.aesgcm", $bad) }
    $log = "$work\dumps\c5-$case.log"
    if ((Invoke-C5 'Restore' 'restore' 'c9-restore' $key 'bad.aesgcm' $log) -eq 0) { Fail 'R06B1_ARCHIVE_REFUSAL' }
    $want = switch ($case) { 'tamper' { 'Backup authentication failed' } 'key' { 'Backup authentication failed' } 'header' { 'Backup container header is invalid' } default { 'Backup container size is invalid' } }
    if (-not (Test-Log $log $want) -or (Get-Scalar 'restore' 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()') -cne '0') { Fail 'R06B1_ARCHIVE_REFUSAL' }
    Remove-Item -LiteralPath "$work\restore\bad.aesgcm"
}
Write-Marker 'R06B1_ARCHIVE_REFUSALS_OK_WIN:tamper=true header=true truncated=true wrong_key=true over_cap=true target_still_empty=true'
Copy-Archive 'original.aesgcm' 'source' 'restore' $archiveSha
Assert-Identity 'source', 'restore'; Assert-OriginalFixture 'source'
if ((Invoke-C5 'Restore' 'restore' 'c9-restore' $backupKey 'original.aesgcm' "$work\dumps\c5-restore.log") -ne 0) { Fail 'R06B1_RESTORE_FAILED' }
Assert-Identity 'source', 'restore'; Assert-OriginalFixture 'restore'
# A populated target and an existing archive path must both refuse.
if ((Invoke-C5 'Restore' 'restore' 'c9-restore' $backupKey 'original.aesgcm' "$work\dumps\c5-again.log") -eq 0 -or -not (Test-Log "$work\dumps\c5-again.log" 'Restore requires a newly created empty schema')) { Fail 'R06B1_RESTORE_REPEAT' }
if ((Invoke-C5 'Backup' 'source' 'c9-backup' $backupKey 'original.aesgcm' "$work\dumps\c5-exists.log") -eq 0 -or -not (Test-Log "$work\dumps\c5-exists.log" 'Choose a new archive path')) { Fail 'R06B1_BACKUP_OVERWRITE' }
$targetBefore = Save-Rows 'restore' 'target-before'
Assert-Same 'R06B1_RESTORED_ROWS_MISMATCH' $sourceBefore $targetBefore
Revoke-Transport 'source' 'backup'; Revoke-Transport 'restore' 'restore'
Write-Marker "R06B1_ORIGINAL_RESTORE_OK_WIN:archive_bytes=$archiveBytes distinct_servers=true exact_rows=true sessions_empty=true populated_restore_refused=true archive_overwrite_refused=true"

$script:phase = 'baseline'
$null = Invoke-Root 'restore' "CREATE USER 'c9_local_ro'@'%' IDENTIFIED BY '$roPassword'; GRANT SELECT ON $t.* TO 'c9_local_ro'@'%';"
$fp = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes("$t`0$($roles.restore.Uuid)"))).ToLowerInvariant()
$baseline = "$work\restore\baseline.properties"
$ro = @{ EXT_DB_URL = (Get-Url 'restore'); EXT_DB_USER = 'c9_local_ro'; EXT_DB_PASSWORD = $roPassword; EXT_C8_HOSTED_EXPECTED_SCHEMA = $t; EXT_C8_HOSTED_EXPECTED_FINGERPRINT = $fp
    EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID = $originalOrg; EXT_C8_HOSTED_OWNER_PUBLIC_ID = $ownerId; EXT_C8_BASELINE_HMAC_HEX = $hmacKey; EXT_C8_BASELINE_FILE = "$drive\restore\baseline.properties"; EXT_C8_BASELINE_MODE = 'CAPTURE' }
if ($Mode -ceq 'ApprovedImport') {
    $script:phase = 'approved_preflight'
    $ro.EXT_C8_DEMO_PLAN = "$drive\restore\approved-plan.json"; $log = "$work\dumps\preflight.log"
    if ((Invoke-Jar 'restore' $ro '--c8-hosted-demo-preflight' $log) -ne 0) { Fail 'R06B1_APPROVED_PREFLIGHT_FAILED' }
    foreach ($line in 'HOSTED_DEMO_PREFLIGHT_READ_ONLY_OK', "target_fingerprint_sha256=$fp", 'original_organization_count=2', 'flyway_version=7', 'approved_full_plan=96_workspaces_21060_requests', 'database_writes=0') { if (-not (Test-Line $log $line)) { Fail 'R06B1_APPROVED_PREFLIGHT_MARKER' } }
    Write-Marker 'R06B1_APPROVED_PREFLIGHT_OK_WIN:exact_plan=true fingerprint_match=true read_only=true'
} else { Write-Marker 'R06B1_APPROVED_PREFLIGHT_NOT_RUN:public_fixture_only' }
$script:phase = 'baseline'
$log = "$work\dumps\baseline.log"
if ((Invoke-Jar 'restore' $ro '--c8-hosted-original-baseline' $log) -ne 0) { Fail 'R06B1_CAPTURE_FAILED' }
if (-not (Test-Log $log 'C8_ORIGINAL_BASELINE_CAPTURED')) { Fail 'R06B1_CAPTURE_MARKER' }
if (-not (Test-OwnerOnly $baseline @($roles.restore.Sid))) { Fail 'R06B1_BASELINE_MODE' }
$ro.EXT_C8_BASELINE_MODE = 'VERIFY'
function Test-Baseline([string] $LogPath) { return ((Invoke-Jar 'restore' $ro '--c8-hosted-original-baseline' $LogPath) -eq 0 -and (Test-Log $LogPath 'C8_ORIGINAL_BASELINE_READ_ONLY_OK')) }
if (-not (Test-Baseline $log)) { Fail 'R06B1_VERIFY_FAILED' }
if ($Mode -ceq 'FixtureOnly') {
    # The unmodified verifier must refuse a widened manifest ACL, then pass again once it is restored.
    & "$env:SystemRoot\System32\icacls.exe" $baseline /grant '*S-1-1-0:R' /Q | Out-Null
    if (Test-OwnerOnly $baseline @($roles.restore.Sid)) { Fail 'R06B1_BASELINE_MODE' } # the .NET check must also see it
    if ((Invoke-Jar 'restore' $ro '--c8-hosted-original-baseline' $log) -eq 0 -or -not (Test-Log $log 'not private mode 0600')) { Fail 'R06B1_BASELINE_MODE' }
    & "$env:SystemRoot\System32\icacls.exe" $baseline /remove:g '*S-1-1-0' /Q | Out-Null
    if (-not (Test-Baseline $log)) { Fail 'R06B1_VERIFY_FAILED' }
    # A changed original row must be detected; the row is then put back exactly.
    $null = Invoke-Root 'restore' "UPDATE department SET version=version+1 WHERE name_key='existing fictional operations';" $t
    if ((Invoke-Jar 'restore' $ro '--c8-hosted-original-baseline' $log) -eq 0 -or -not (Test-Log $log 'C8_ORIGINAL_BASELINE_MISMATCH')) { Fail 'R06B1_BASELINE_TAMPER' }
    $null = Invoke-Root 'restore' "UPDATE department SET version=version-1 WHERE name_key='existing fictional operations';" $t
    if (-not (Test-Baseline $log)) { Fail 'R06B1_VERIFY_FAILED' }
    # A fingerprint of a different server must be refused.
    $wrong = $ro.Clone(); $wrong.EXT_C8_HOSTED_EXPECTED_FINGERPRINT = '0' * 64
    if ((Invoke-Jar 'restore' $wrong '--c8-hosted-original-baseline' $log) -eq 0 -or -not (Test-Log $log 'connected to a different server or schema')) { Fail 'R06B1_BASELINE_IDENTITY' }
    Write-Marker 'R06B1_BASELINE_REFUSALS_OK_WIN:wide_acl=true changed_original_row=true wrong_fingerprint=true'
}
Assert-Same 'R06B1_PREPARATION_CHANGED_ROWS' $sourceBefore (Save-Rows 'source' 'source-after')
Assert-Same 'R06B1_PREPARATION_CHANGED_ROWS' $targetBefore (Save-Rows 'restore' 'target-after')
Write-Marker 'R06B1_BASELINE_OK_WIN:actual_v2=true readonly=true owner_only_acl=true source_and_target_rows_unchanged=true'
Write-Marker 'R06B1_PREPARATION_OK:importer_run=false attestation_created=false'

function Grant-Importer([string] $Password) {
    $null = Invoke-Root 'restore' "CREATE USER 'c9_local_importer'@'%' IDENTIFIED BY '$Password'; GRANT SELECT ON $t.* TO 'c9_local_importer'@'%';"
    foreach ($table in 'organization', 'user_account', 'membership', 'department', 'project', 'resource', 'resource_requirement', 'allocation') { $null = Invoke-Root 'restore' "GRANT INSERT,UPDATE ON $t.$table TO 'c9_local_importer'@'%';" }
    foreach ($table in 'stock_movement', 'audit_event') { $null = Invoke-Root 'restore' "GRANT INSERT ON $t.$table TO 'c9_local_importer'@'%';" }
}
function Assert-ImporterGrants([string] $Password) {
    $r = Invoke-Sql 'restore' 'c9_local_importer' $Password 'SELECT COUNT(*) FROM organization' $t
    if ($r.Code -ne 0 -or $r.Out -cne '2') { Fail 'R06B2_IMPORTER_SELECT' }
    foreach ($q in 'UPDATE stock_movement SET id=id WHERE 1=0', 'UPDATE audit_event SET id=id WHERE 1=0', 'DELETE FROM allocation WHERE 1=0', 'INSERT INTO local_credential SELECT * FROM local_credential WHERE 1=0') { Assert-Denied 'R06B2' 'restore' 'c9_local_importer' $Password $q }
    Write-Marker 'R06B2_IMPORTER_GRANTS_OK_WIN:select=true ledger_update_denied=true delete_denied=true credential_insert_denied=true'
}
