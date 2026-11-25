# Dot-sourced stage, FixtureOnly: fictional data only. No approved plan byte is read in this mode.
. (Join-Path $PSScriptRoot 'mixed_schema.ps1')
$script:phase = 'importer_grants'
$importerPassword = New-Secret; Grant-Importer $importerPassword; Assert-ImporterGrants $importerPassword
$null = Invoke-Root 'restore' "DROP USER 'c9_local_importer'@'%';"
Assert-Same 'R06B2_GRANT_TEST_CHANGED_ROWS' $targetBefore (Save-Rows 'restore' 'target-grant-after')

$script:phase = 'third_server_fixture'
Start-Server 'mixed'
if ($roles.mixed.Uuid -cin @($roles.source.Uuid, $roles.restore.Uuid) -or (Get-Scalar 'mixed' 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()') -cne '0') { Fail 'R06B3A_THIRD_FIXTURE_BOUNDARY' }
Assert-Same 'R06B3A_THIRD_FIXTURE_CHANGED_ROWS' $sourceBefore (Save-Rows 'source' 'source-third')
Assert-Same 'R06B3A_THIRD_FIXTURE_CHANGED_ROWS' $targetBefore (Save-Rows 'restore' 'target-third')
Write-Marker 'R06B3A_THIRD_SERVER_FIXTURE_OK_WIN:distinct_containers=true distinct_uuids=true empty_schema=true original_rows_unchanged=true private_plan_used=false'

$script:phase = 'third_server_recovery'
# The same C5 transport the mixed recovery uses, proven here with the fictional original rows only.
$thirdKey = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
Assert-Identity 'restore', 'mixed'
Grant-Transport 'restore' 'backup'; Grant-Transport 'mixed' 'restore'
if ((Invoke-C5 'Backup' 'restore' 'c9-backup' $thirdKey 'third.aesgcm' "$work\dumps\c5-third-backup.log") -ne 0) { Fail 'R06B3A_THIRD_BACKUP_FAILED' }
$thirdSha = Get-Sha "$work\restore\third.aesgcm"
if (-not (Test-OwnerOnly "$work\restore\third.aesgcm" @($roles.restore.Sid))) { Fail 'R06B3A_THIRD_ARCHIVE_MODE' }
Copy-Archive 'third.aesgcm' 'restore' 'mixed' $thirdSha
if ((Invoke-C5 'Restore' 'mixed' 'c9-restore' $thirdKey 'third.aesgcm' "$work\dumps\c5-third-restore.log") -ne 0) { Fail 'R06B3A_THIRD_RESTORE_FAILED' }
Assert-Same 'R06B3A_THIRD_ROWS_MISMATCH' $targetBefore (Save-Rows 'mixed' 'third-restored')
if ((Get-Scalar 'mixed' 'SELECT COUNT(*) FROM SPRING_SESSION') -cne '0' -or (Get-Scalar 'mixed' 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES') -cne '0') { Fail 'R06B3A_SESSIONS' }
Revoke-Transport 'restore' 'backup'; Revoke-Transport 'mixed' 'restore'
# The private expectation builder must refuse this original-only third server.
$mixedRoPassword = New-Secret; $m = $roles.mixed.Schema
$null = Invoke-Root 'mixed' "CREATE USER 'c9_mixed_ro'@'%' IDENTIFIED BY '$mixedRoPassword'; GRANT SELECT ON $m.* TO 'c9_mixed_ro'@'%';"
Save-Projection 'mixed' 'c9_mixed_ro' $mixedRoPassword 'mixed-projection.jsonl'
$mixedFp = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes("$m`0$($roles.mixed.Uuid)"))).ToLowerInvariant()
$log = "$work\dumps\build-refusal.log"
if ((Invoke-PythonIn 'mixed' @("$drive\rt\repo\scripts\build-c9-private-mixed-expectations.py") @{ C9_CONTROL_PRIVATE_MIXED = 'true'; C9_MIXED_PROJECTION = "$drive\mixed\mixed-projection.jsonl"; C9_MIXED_EXPECTED = "$drive\mixed\mixed-expected.json"
            C9_MIXED_SCHEMA = $m; C9_MIXED_SERVER_UUID = $roles.mixed.Uuid; C9_MIXED_OWNER = $ownerId; C9_MIXED_ORIGINAL = $originalOrg; C9_MIXED_FINGERPRINT = $mixedFp } $log) -eq 0 -or
    -not (Test-Log $log 'R06B3B_MIXED_EXPECTATIONS_FAILED:ValueError') -or (Test-Path -LiteralPath "$work\mixed\mixed-expected.json")) { Fail 'R06B3B_ORIGINAL_ACCEPTED_AS_MIXED' }
. (Join-Path $PSScriptRoot 'fixture_additive.ps1')
$null = Invoke-Root 'mixed' "DROP USER 'c9_mixed_ro'@'%'; DROP DATABASE $m; CREATE DATABASE $m CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
Assert-Same 'R06B3A_THIRD_FIXTURE_CHANGED_ROWS' $sourceBefore (Save-Rows 'source' 'source-third-recovery')
Assert-Same 'R06B3A_THIRD_FIXTURE_CHANGED_ROWS' $targetBefore (Save-Rows 'restore' 'target-third-recovery')
Write-Marker 'R06B3A_THIRD_SERVER_RECOVERY_FIXTURE_OK_WIN:independent_third_restore=true exact_rows=true sessions_empty=true owner_only_archive=true original_only_rejected_as_mixed=true sources_unchanged=true'

$script:phase = 'public_recovered_control'
$foreign = Get-Scalar 'restore' "SELECT BIN_TO_UUID(public_id,0) FROM organization WHERE slug='isolated-other-original'"
$resource = Get-Scalar 'restore' "SELECT BIN_TO_UUID(public_id,0) FROM resource WHERE name='Existing fictional projector'"
Save-Projection 'restore' 'c9_local_ro' $roPassword 'public-projection.jsonl'
$log = "$work\dumps\projection.log"
if ((Invoke-PythonIn 'restore' @("$drive\rt\repo\scripts\test-c9-public-sql-projection.py", "$drive\restore\public-projection.jsonl", $t, $roles.restore.Uuid, $ownerId, $originalOrg, $foreign, $resource) @{ C9_WINDOWS_PUBLIC_FIXTURE = 'true' } $log) -ne 0 -or -not (Test-Log $log 'R06B3B_PUBLIC_SQL_PROJECTION_OK')) { Fail 'R06B3B_PUBLIC_SQL_ORACLE' }
Write-Marker (@(Get-Content $log) -match '^R06B3B_PUBLIC_SQL_PROJECTION_OK' | Select-Object -First 1)
Assert-Same 'R06B3B_PUBLIC_PROJECTION_CHANGED_ROWS' $targetBefore (Save-Rows 'restore' 'projection-after')
$denied = Invoke-Runtime 'restore' 'c9_observer' 'R06B3B_CONTROL' @{ C9_CONTROL_PUBLIC_ONLY = 'true'; C9_CONTROL_ORIGINAL = $originalOrg; C9_CONTROL_FOREIGN = $foreign; C9_CONTROL_RESOURCE = $resource } 'verify-c9-public-recovered-control.py' 'verify-c9-public-recovered-control.mjs' @('R06B3B_CONTROL_API_LOGIN_OK', 'R06B3B_CONTROL_API_LOGOUT_OK') 'R06B3B_CONTROL_BROWSER_OK' "$work\dumps"
Assert-Same 'R06B3B_CONTROL_SOURCE_CHANGED' $sourceBefore (Save-Rows 'source' 'control-source-after')
Write-Marker "R06B3B_CONTROL_ROWS_OK_WIN:non_session_rows_equal=true original_rows_equal=true scheduler_denial_log_matches=$denied flyway_runtime_disabled=true"

$script:phase = 'private_oracle_refusals'
# The private-only entry points must refuse the public fixture before reading any expectation.
$log = "$work\dumps\refusal.log"
if ((Invoke-PythonIn 'restore' @("$drive\rt\repo\scripts\verify-c9-private-mixed-api.py", 'login') @{ C9_RUNTIME_ISOLATED = 'true'; C9_RUNTIME_STATE = "$drive\restore\no.json" } $log) -eq 0 -or -not (Test-Log $log 'R06B3B_MIXED_API_FAILED:INTAKE')) { Fail 'R06B3B_PRIVATE_REFUSAL' }
if ((Invoke-Contained 'restore' "$env:ProgramFiles\nodejs\node.exe" (Get-NodeArgs "$drive\rt\repo\scripts\verify-c9-private-mixed-browser.mjs") @{} '' $log '' 60) -eq 0 -or -not (Test-Log $log 'R06B3B_MIXED_BROWSER_FAILED:INTAKE')) { Fail 'R06B3B_PRIVATE_REFUSAL' }
# The mixed assertion set must reject an original-only database.
. (Join-Path $PSScriptRoot 'mixed_schema.ps1')
$code = 'NONE'; try { Assert-MixedSchema 'restore' } catch { $code = $_.Exception.GetBaseException().Message }
if ($code -cne 'R06B3A_MIXED_FIXTURE_REQUIRED') { Fail 'R06B3A_ORIGINAL_ACCEPTED_AS_MIXED' }
# Every mixed SQL check really executes and returns the known original-only value.
$values = @((Get-MixedChecks).Keys | ForEach-Object { Get-Scalar 'restore' $_ }) -join ','
if ($values -cne '2,0,0,2,1,0,0,0,0') { Fail 'R06B3A_MIXED_SQL_SHAPE' }
# The derived guard must refuse the same oracle outside a container.
$savedFixture = $env:C9_WINDOWS_PUBLIC_FIXTURE; $env:C9_WINDOWS_PUBLIC_FIXTURE = 'true'
try { & $Python -B "$work\rt\repo\scripts\test-c9-public-sql-projection.py" "$work\restore\public-projection.jsonl" $t $roles.restore.Uuid $ownerId $originalOrg $foreign $resource *> "$work\dumps\uncontained.log"; $outside = $LASTEXITCODE } finally { $env:C9_WINDOWS_PUBLIC_FIXTURE = $savedFixture }
if ($outside -eq 0) { Fail 'R06B3B_UNCONTAINED_ACCEPTED' }
Write-Marker 'R06B3B_PRIVATE_ENTRYPOINT_REFUSALS_OK_WIN:api=true browser=true original_rejected_as_mixed=true uncontained_oracle_refused=true'

$script:phase = 'public_aggregate_projection'
$schemaFile = "$work\dumps\aggregate-schema.sql"
if ((Invoke-Contained 'restore' "$mysqlHome\bin\mysqldump.exe" @('--no-defaults', '--protocol=tcp', '--host=127.0.0.1', "--port=$($roles.restore.Port)", '--user=root', '--no-data', '--skip-comments', '--skip-dump-date', '--skip-lock-tables', '--no-tablespaces', '--set-gtid-purged=OFF', '--skip-column-statistics', $t) @{ MYSQL_PWD = $roles.restore.Password } '' $schemaFile (New-Scratch 'a.err') 120) -ne 0) { Fail 'R06B3B_AGGREGATE_FIXTURE' }
$null = Invoke-Root 'mixed' ([IO.File]::ReadAllText($schemaFile)) $roles.mixed.Schema
$null = Invoke-Root 'mixed' ([IO.File]::ReadAllText("$work\rt\repo\scripts\c9-public-aggregate-fixture.sql")) $roles.mixed.Schema
if ((Get-Scalar 'mixed' 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL') -cne '0' -or (Get-Scalar 'mixed' 'SELECT COUNT(*) FROM stock_movement') -cne '9' -or (Get-Scalar 'mixed' 'SELECT COUNT(*) FROM allocation') -cne '6') { Fail 'R06B3B_AGGREGATE_FIXTURE' }
$aggPassword = New-Secret
$null = Invoke-Root 'mixed' "CREATE USER 'c9_aggregate_ro'@'%' IDENTIFIED BY '$aggPassword'; GRANT SELECT ON $($roles.mixed.Schema).* TO 'c9_aggregate_ro'@'%';"
$aggBefore = Save-Rows 'mixed' 'aggregate-before'
Assert-Denied 'R06B3B_AGGREGATE' 'mixed' 'c9_aggregate_ro' $aggPassword 'UPDATE resource SET total_quantity=0 WHERE 1=0'
Save-Projection 'mixed' 'c9_aggregate_ro' $aggPassword 'aggregate-projection.jsonl'
$log = "$work\dumps\aggregate.log"
if ((Invoke-PythonIn 'mixed' @("$drive\rt\repo\scripts\test-c9-public-sql-aggregates.py", "$drive\mixed\aggregate-projection.jsonl", $roles.mixed.Schema, $roles.mixed.Uuid) @{ C9_WINDOWS_PUBLIC_FIXTURE = 'true' } $log) -ne 0) { Fail 'R06B3B_AGGREGATE_ORACLE' }
Write-Marker (@(Get-Content $log) -match 'AGGREGATES_OK' | Select-Object -First 1)
Assert-Same 'R06B3B_AGGREGATE_CHANGED_ROWS' $aggBefore (Save-Rows 'mixed' 'aggregate-after')
$null = Invoke-Root 'mixed' "DROP USER 'c9_aggregate_ro'@'%';"
Assert-Same 'R06B3B_AGGREGATE_OTHER_SERVER_CHANGED' "$work\dumps\R06B3B_CONTROL-after-domain.sql" (Save-Rows 'restore' 'aggregate-restored-after' -Domain)
Assert-Same 'R06B3B_AGGREGATE_OTHER_SERVER_CHANGED' $sourceBefore (Save-Rows 'source' 'aggregate-source-after')
Write-Marker 'R06B3B_PUBLIC_SQL_AGGREGATE_ROWS_OK_WIN:select_only=true original_restored_unchanged=true fixture_unchanged=true'
