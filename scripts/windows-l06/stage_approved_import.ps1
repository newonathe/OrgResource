# Dot-sourced stage, ApprovedImport only. Owner terminal, one import, no retry. Mirrors the pinned Linux runner step for step.
. (Join-Path $PSScriptRoot 'mixed_schema.ps1')
$postflightMarker = 'HOSTED_DEMO_POSTFLIGHT_READ_ONLY_OK organizations=96 allocations=21060 events=67370;'
$script:phase = 'private_runtime_preflight'
# Isolated Chromium must start before consent or import, without any HTTP request or download.
Assert-Browser 'restore'
Write-Marker 'R06B3B_PRIVATE_RUNTIME_PREFLIGHT_OK_WIN:handle_job_control=true isolated_chromium=true'

$script:phase = 'operator_confirmation'
$baselineSha = Get-Sha $baseline; $rowSha = Get-Sha $sourceBefore
$restoreReceipt = "$work\dumps\restore-receipt.txt"
[IO.File]::WriteAllText($restoreReceipt, @"
scope=LOCAL_DISPOSABLE_ONLY
harness=WINDOWS_NATIVE $head
source_sha=$SourceSha
jar_sha256=$JarSha256
plan_sha256=$approvedPlanSha
source_server_uuid=$($roles.source.Uuid)
restore_server_uuid=$($roles.restore.Uuid)
target_fingerprint_sha256=$fp
archive_sha256=$archiveSha
archive_bytes=$archiveBytes
original_rows_sha256=$rowSha
restored_rows_sha256=$(Get-Sha $targetBefore)
baseline_sha256=$baselineSha
scope_after_consent=ONE_DISPOSABLE_IMPORT_RECOVERY_AND_READ_SESSION_RUNTIME
actual_c5_restore=true
independent_server=true
exact_rows_equal=true
sessions_empty=true
readonly_preflight=true
baseline_verify=true
original_source_unchanged=true
hosted_import=false
off_host_restore=false
"@)
$receiptSha = Get-Sha $restoreReceipt
Write-Marker ([IO.File]::ReadAllText($restoreReceipt))
Write-Marker "restore_receipt_sha256=$receiptSha"
if (-not (Confirm-C9Operator $fp $receiptSha 300)) { Fail 'R06B2_OPERATOR_DECLINED' }
# Bind the owner's decision to this same live, quiescent target.
Assert-Identity 'source', 'restore'
if ((Get-Sha $restoreReceipt) -cne $receiptSha -or (Get-Sha "$work\restore\original.aesgcm") -cne $archiveSha -or (Get-Sha $baseline) -cne $baselineSha -or
    (Get-Sha "$work\rt\verified.jar") -cne $JarSha256) { Fail 'R06B2_APPROVED_IDENTITY_CHANGED' }
foreach ($x in 'restore', 'mixed') { Assert-ApprovedPlan "$work\$x\approved-plan.json" '' @($roles[$x].Sid) }
Assert-Same 'R06B2_APPROVED_ROWS_CHANGED' $sourceBefore (Save-Rows 'source' 'source-confirmed')
Assert-Same 'R06B2_APPROVED_ROWS_CHANGED' $targetBefore (Save-Rows 'restore' 'target-confirmed')
if (-not (Test-Baseline "$work\dumps\confirm-baseline.log")) { Fail 'R06B2_APPROVED_BASELINE_CHANGED' }
$candidate = Join-Path $Root "orgresource-c9w-receipt-$token"; New-Item -ItemType Directory -Path $candidate | Out-Null; Set-PrivateAcl $candidate @(); $receiptDir = $candidate
Copy-Item -LiteralPath $restoreReceipt -Destination "$receiptDir\restore-receipt.txt"
Copy-Item -LiteralPath "$work\rt\repo\scripts\derived-manifest.json" -Destination "$receiptDir\derived-manifest.json"
[IO.File]::WriteAllText("$receiptDir\operator-receipt.txt", "operator_confirmation_received=true`nrestore_receipt_sha256=$receiptSha`nscope=LOCAL_DISPOSABLE_ONLY`n")

$script:phase = 'importer_grants'
$importerPassword = New-Secret; Grant-Importer $importerPassword; Assert-ImporterGrants $importerPassword
$import = $ro.Clone()
$import.EXT_DB_USER = 'c9_local_importer'; $import.EXT_DB_PASSWORD = $importerPassword; $import.EXT_CONTACT_ENCRYPTION_KEY = $contactKey
$import.EXT_C8_HOSTED_CONFIRM = "IMPORT 96 INTO $fp"; $import.EXT_C8_HOSTED_BACKUP_ID = "local-original-$token"
$import.EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256 = $receiptSha; $import.EXT_C8_HOSTED_RESTORE_ATTESTATION = 'I VERIFIED ISOLATED RESTORE'
$script:phase = 'import'
$clock = [Diagnostics.Stopwatch]::StartNew()
# Exactly one real import attempt. Never retry an incomplete cohort.
$importExit = Invoke-Jar 'restore' $import '--c8-hosted-demo-import' "$receiptDir\import-private.log"
$importSeconds = [int]$clock.Elapsed.TotalSeconds
$import.Clear()
$null = Invoke-Root 'restore' "DROP USER 'c9_local_importer'@'%';"
[IO.File]::WriteAllText("$receiptDir\import-result.txt", "import_exit=$importExit`nimport_elapsed_seconds=$importSeconds`n")
$script:phase = 'post_import_baseline'
# Independent SELECT-only verification even if the command timed out or failed.
if (-not (Test-Baseline "$receiptDir\baseline-private.log")) { Fail 'R06B2_ORIGINAL_BASELINE_FAILED' }
Assert-Same 'R06B2_ORIGINAL_SOURCE_CHANGED' $sourceBefore (Save-Rows 'source' 'source-post-import')
if ($importExit -ne 0) { Fail 'R06B2_IMPORT_INCOMPLETE_NO_RETRY' }
if (-not (Select-String -LiteralPath "$receiptDir\import-private.log" -Pattern '^HOSTED_IMPORT_COMPLETE:' -CaseSensitive -Quiet)) { Fail 'R06B2_IMPORT_MARKER_MISSING' }
$script:phase = 'postflight'
$importedBefore = Save-Rows 'restore' 'imported-before-postflight'
if ((Get-Scalar 'restore' 'SELECT COUNT(*) FROM organization') -cne '98' -or (Get-Scalar 'restore' "SELECT COUNT(*) FROM organization WHERE demo_plan_sha='$approvedPlanSha'") -cne '96' -or
    (Get-Scalar 'restore' 'SELECT COUNT(*) FROM SPRING_SESSION') -cne '0' -or (Get-Scalar 'restore' 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES') -cne '0') { Fail 'R06B2_MIXED_COUNTS' }
if ((Invoke-Jar 'restore' $ro '--c8-hosted-demo-postflight' "$receiptDir\postflight-private.log") -ne 0) { Fail 'R06B2_POSTFLIGHT_FAILED' }
if (-not (Test-Log "$receiptDir\postflight-private.log" $postflightMarker)) { Fail 'R06B2_POSTFLIGHT_MARKER' }
# Preflight must reject the populated cohort. The importer is never invoked a second time.
if ((Invoke-Jar 'restore' $ro '--c8-hosted-demo-preflight' "$receiptDir\repeat-refusal-private.log") -eq 0) { Fail 'R06B2_POPULATED_PREFLIGHT_ACCEPTED' }
if (-not (Test-Log "$receiptDir\repeat-refusal-private.log" 'existing or partial demo cohort')) { Fail 'R06B2_WRONG_REPEAT_REFUSAL' }
$importedAfter = Save-Rows 'restore' 'imported-after-postflight'
Assert-Same 'R06B2_POSTFLIGHT_CHANGED_ROWS' $importedBefore $importedAfter
function Add-Result([string] $Text) { Write-Marker $Text; [IO.File]::AppendAllText("$receiptDir\result.txt", "$Text`n") }
Add-Result "R06B2_LOCAL_MIXED_IMPORT_OK_WIN:organizations=98 tagged=96 allocations=21060 events=67370 original_baseline=true independent_readonly_postflight=true populated_preflight_refused=true import_seconds=$importSeconds"

$script:phase = 'mixed_recovery'
# A third freshly initialized server, independent of both original servers.
Start-Server 'mixed'
if ($roles.mixed.Uuid -cin @($roles.source.Uuid, $roles.restore.Uuid)) { Fail 'R06B3A_SERVER_UUID' }
$mixedKey = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)); $mixedRoPassword = New-Secret
function Assert-MixedPlans { foreach ($x in 'restore', 'mixed') { Assert-ApprovedPlan "$work\$x\approved-plan.json" '' @($roles[$x].Sid) } }
Assert-Identity 'restore', 'mixed'; Assert-MixedPlans; Assert-MixedSchema 'restore'
Grant-Transport 'restore' 'backup'; Grant-Transport 'mixed' 'restore'
$clock.Restart()
if ((Invoke-C5 'Backup' 'restore' 'c9-backup' $mixedKey 'mixed.aesgcm' "$receiptDir\mixed-backup-private.log") -ne 0) { Fail 'R06B3A_MIXED_BACKUP_FAILED' }
$backupSeconds = [int]$clock.Elapsed.TotalSeconds
$mixedArchive = "$work\restore\mixed.aesgcm"; $mixedBytes = (Get-Item $mixedArchive).Length; $mixedArchiveSha = Get-Sha $mixedArchive
if (-not (Test-OwnerOnly $mixedArchive @($roles.restore.Sid))) { Fail 'R06B3A_MIXED_ARCHIVE_MODE' }
if ($mixedBytes -gt $maxArchive) { Fail 'R06B3A_MIXED_ARCHIVE_CAP' }
Copy-Archive 'mixed.aesgcm' 'restore' 'mixed' $mixedArchiveSha
Assert-Identity 'restore', 'mixed'; Assert-MixedPlans; Assert-MixedSchema 'restore'
$clock.Restart()
if ((Invoke-C5 'Restore' 'mixed' 'c9-restore' $mixedKey 'mixed.aesgcm' "$receiptDir\mixed-restore-private.log") -ne 0) { Fail 'R06B3A_MIXED_RESTORE_FAILED' }
$restoreSeconds = [int]$clock.Elapsed.TotalSeconds
if ((Get-Sha $mixedArchive) -cne $mixedArchiveSha -or (Get-Sha "$work\mixed\mixed.aesgcm") -cne $mixedArchiveSha) { Fail 'R06B3A_ARCHIVE_CHANGED' }
Assert-MixedSchema 'mixed'
Revoke-Transport 'restore' 'backup'; Revoke-Transport 'mixed' 'restore'
$mixedRestored = Save-Rows 'mixed' 'mixed-restored'
Assert-Same 'R06B3A_MIXED_ROWS_MISMATCH' $importedAfter $mixedRestored
if ((Get-Scalar 'mixed' 'SELECT COUNT(*) FROM SPRING_SESSION') -cne '0' -or (Get-Scalar 'mixed' 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES') -cne '0') { Fail 'R06B3A_SESSIONS' }
$m = $roles.mixed.Schema
$null = Invoke-Root 'mixed' "CREATE USER 'c9_mixed_ro'@'%' IDENTIFIED BY '$mixedRoPassword'; GRANT SELECT ON $m.* TO 'c9_mixed_ro'@'%';"
$mixedFp = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes("$m`0$($roles.mixed.Uuid)"))).ToLowerInvariant()
$mixedRo = @{ EXT_DB_URL = (Get-Url 'mixed'); EXT_DB_USER = 'c9_mixed_ro'; EXT_DB_PASSWORD = $mixedRoPassword; EXT_C8_HOSTED_EXPECTED_SCHEMA = $m; EXT_C8_HOSTED_EXPECTED_FINGERPRINT = $mixedFp
    EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID = $originalOrg; EXT_C8_HOSTED_OWNER_PUBLIC_ID = $ownerId; EXT_C8_DEMO_PLAN = "$drive\mixed\approved-plan.json" }
if ((Invoke-Jar 'mixed' $mixedRo '--c8-hosted-demo-postflight' "$receiptDir\mixed-postflight-private.log") -ne 0) { Fail 'R06B3A_MIXED_POSTFLIGHT_FAILED' }
if (-not (Test-Log "$receiptDir\mixed-postflight-private.log" $postflightMarker)) { Fail 'R06B3A_MIXED_POSTFLIGHT_MARKER' }
$mixedAfter = Save-Rows 'mixed' 'mixed-after-postflight'
Assert-Same 'R06B3A_ROWS_CHANGED' $mixedRestored $mixedAfter
Assert-Same 'R06B3A_ROWS_CHANGED' $importedAfter (Save-Rows 'restore' 'imported-after-recovery')
Assert-Same 'R06B3A_ROWS_CHANGED' $sourceBefore (Save-Rows 'source' 'original-after-recovery')
Add-Result "R06B3A_FULL_MIXED_RECOVERY_OK_WIN:archive_bytes=$mixedBytes backup_seconds=$backupSeconds restore_seconds=$restoreSeconds three_independent_servers=true exact_rows=true sessions_empty=true readonly_postflight=true sources_unchanged=true"

$script:phase = 'private_mixed_runtime'
if (-not (Test-Line "$receiptDir\operator-receipt.txt" 'operator_confirmation_received=true')) { Fail 'R06B3B_PRIVATE_CONFIRMATION_REQUIRED' }
Save-Projection 'mixed' 'c9_mixed_ro' $mixedRoPassword 'mixed-projection.jsonl'
if ((Invoke-PythonIn 'mixed' @("$drive\rt\repo\scripts\build-c9-private-mixed-expectations.py") @{ C9_CONTROL_PRIVATE_MIXED = 'true'; C9_MIXED_PROJECTION = "$drive\mixed\mixed-projection.jsonl"; C9_MIXED_EXPECTED = "$drive\mixed\mixed-expected.json"
            C9_MIXED_SCHEMA = $m; C9_MIXED_SERVER_UUID = $roles.mixed.Uuid; C9_MIXED_OWNER = $ownerId; C9_MIXED_ORIGINAL = $originalOrg; C9_MIXED_FINGERPRINT = $mixedFp } "$receiptDir\mixed-expectations-private.log") -ne 0) { Fail 'R06B3B_PRIVATE_EXPECTATIONS' }
if (-not (Test-OwnerOnly "$work\mixed\mixed-expected.json" @($roles.mixed.Sid))) { Fail 'R06B3B_PRIVATE_EXPECTATIONS' }
Assert-Same 'R06B3B_PRIVATE_PRE_RUNTIME_ROWS' $mixedAfter (Save-Rows 'mixed' 'mixed-before-runtime-all')
$mixedDomain = Save-Rows 'mixed' 'mixed-before-runtime-domain' -Domain
$denied = Invoke-Runtime 'mixed' 'c9_mixed_observer' 'R06B3B_PRIVATE' @{ C9_CONTROL_PRIVATE_MIXED = 'true'; C9_MIXED_EXPECTED = "$drive\mixed\mixed-expected.json"; C9_WINDOWS_EXPECTED_SHA256 = (Get-Sha "$work\mixed\mixed-expected.json") } 'verify-c9-private-mixed-api.py' 'verify-c9-private-mixed-browser.mjs' @('R06B3B_MIXED_API_LOGIN_OK', 'R06B3B_MIXED_API_LOGOUT_OK') 'R06B3B_MIXED_BROWSER_OK' $receiptDir {
    Assert-C9OwnerTerminal
    if (-not (Test-Log "$receiptDir\mixed-postflight-private.log" $postflightMarker)) { Fail 'R06B3B_PRIVATE_POSTFLIGHT_REQUIRED' }
    Assert-Identity 'mixed'
    if ((Get-Scalar 'mixed' 'SELECT COUNT(*) FROM SPRING_SESSION') -cne '0' -or (Get-Scalar 'mixed' 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES') -cne '0') { Fail 'R06B3B_PRIVATE_SESSION_BASELINE' }
    Assert-Same 'R06B3B_PRIVATE_PRE_RUNTIME_ROWS' $mixedAfter (Save-Rows 'mixed' 'mixed-before-runtime-final')
}
if ((Invoke-Jar 'mixed' $mixedRo '--c8-hosted-demo-postflight' "$receiptDir\mixed-after-runtime-postflight-private.log") -ne 0) { Fail 'R06B3B_PRIVATE_POSTFLIGHT' }
if (-not (Test-Log "$receiptDir\mixed-after-runtime-postflight-private.log" $postflightMarker)) { Fail 'R06B3B_PRIVATE_POSTFLIGHT_MARKER' }
Assert-Same 'R06B3B_PRIVATE_SOURCE_OR_DOMAIN_CHANGED' $mixedDomain (Save-Rows 'mixed' 'mixed-after-runtime-postflight-domain' -Domain)
Assert-Same 'R06B3B_PRIVATE_SOURCE_OR_DOMAIN_CHANGED' $importedAfter (Save-Rows 'restore' 'target-after-runtime')
Assert-Same 'R06B3B_PRIVATE_SOURCE_OR_DOMAIN_CHANGED' $sourceBefore (Save-Rows 'source' 'source-after-runtime')
Add-Result "R06B3B_PRIVATE_MIXED_RUNTIME_OK_WIN:metadata_owner_scope=true api_browser=true persisted_logout=true non_session_rows_equal=true sources_unchanged=true readonly_postflight=true scheduler_denial_log_matches=$denied flyway_runtime_disabled=true"
[IO.File]::AppendAllText("$receiptDir\result.txt", "post_import_c5=VERIFIED_LOCAL`nnormal_recovered_browser=VERIFIED_LOCAL_OBSERVATION_ONLY`nhosted_import=NOT_RUN`nindependent_reviewer=OPEN`n")
