# Dot-sourced, FixtureOnly. The real importer bean and Spring profile, unmodified JAR, under the owner-only provider and the exact
# importer grants, adding a generated FICTIONAL eight-workspace plan to the fictional originals. Never the approved plan.
$add = "orgresource_c8_additive_ci_$token"
. (Join-Path $PSScriptRoot 'scoped_additive.ps1')
$addUuid = $roles.mixed.Uuid; $addPort = $roles.mixed.Port
Assert-C9AdditiveScope $add $addUuid $addPort
if ((Invoke-Root 'mixed' "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='$add'") -cne '0' -or
    (Invoke-Root 'mixed' "SELECT COUNT(*) FROM mysql.user WHERE User IN ('c9_add_importer','c9_add_ro')") -cne '0') { Fail 'R06B2_ADDITIVE_SCOPE' }
$h05MixedBefore = Save-Rows 'mixed' 'h05-mixed-before'
$dump = "$work\dumps\additive-original.sql"
if ((Invoke-Contained 'restore' "$mysqlHome\bin\mysqldump.exe" @('--no-defaults', '--protocol=tcp', '--host=127.0.0.1', "--port=$($roles.restore.Port)", '--user=root', '--skip-comments', '--skip-dump-date', '--hex-blob', '--skip-lock-tables', '--no-tablespaces', '--set-gtid-purged=OFF', '--skip-column-statistics', '--single-transaction', "--result-file=$drive\restore\additive.sql", $t) @{ MYSQL_PWD = $roles.restore.Password } '' '' (New-Scratch 'd.err') 120) -ne 0) { Fail 'R06B2_ADDITIVE_FIXTURE' }
Move-Item -LiteralPath "$work\restore\additive.sql" -Destination $dump
Assert-C9AdditiveScope $add $addUuid $addPort
$null = Invoke-Root 'mixed' "CREATE DATABASE $add CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
$null = Invoke-Root 'mixed' ([IO.File]::ReadAllText($dump)) $add
$addImporter = New-Secret; $addRo = New-Secret
$null = Invoke-Root 'mixed' "CREATE USER 'c9_add_importer'@'%' IDENTIFIED BY '$addImporter'; CREATE USER 'c9_add_ro'@'%' IDENTIFIED BY '$addRo'; GRANT SELECT ON $add.* TO 'c9_add_ro'@'%'; GRANT SELECT ON $add.* TO 'c9_add_importer'@'%';"
foreach ($table in 'organization', 'user_account', 'membership', 'department', 'project', 'resource', 'resource_requirement', 'allocation') { $null = Invoke-Root 'mixed' "GRANT INSERT,UPDATE ON $add.$table TO 'c9_add_importer'@'%';" }
foreach ($table in 'stock_movement', 'audit_event') { $null = Invoke-Root 'mixed' "GRANT INSERT ON $add.$table TO 'c9_add_importer'@'%';" }
[IO.File]::WriteAllBytes("$work\dumps\generate-c8-demo.py", [Text.Encoding]::UTF8.GetBytes(((& git -C $repo show "${SourceSha}:scripts/generate-c8-demo.py") -join "`n") + "`n"))
& $Python -I "$work\dumps\generate-c8-demo.py" --profile intermediate --seed c8-fixture-proof-v1 --output "$work\mixed\fictional-plan.json" | Out-Null
if ($LASTEXITCODE -ne 0) { Fail 'R06B2_ADDITIVE_FIXTURE' }
$addDigest = Get-Sha "$work\mixed\fictional-plan.json"
$addFp = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes("$add`0$($roles.mixed.Uuid)"))).ToLowerInvariant()
$addUrl = "jdbc:mysql://127.0.0.1:$($roles.mixed.Port)/$add`?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$addBase = @{ EXT_DB_URL = $addUrl; EXT_DB_USER = 'c9_add_ro'; EXT_DB_PASSWORD = $addRo; EXT_C8_HOSTED_EXPECTED_SCHEMA = $add; EXT_C8_HOSTED_EXPECTED_FINGERPRINT = $addFp; EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID = $originalOrg
    EXT_C8_HOSTED_OWNER_PUBLIC_ID = $ownerId; EXT_C8_BASELINE_HMAC_HEX = (New-Secret 32); EXT_C8_BASELINE_FILE = "$drive\mixed\baseline.properties"; EXT_C8_BASELINE_MODE = 'CAPTURE' }
$log = "$work\dumps\additive-baseline.log"
if ((Invoke-Jar 'mixed' $addBase '--c8-hosted-original-baseline' $log) -ne 0 -or -not (Test-Log $log 'C8_ORIGINAL_BASELINE_CAPTURED')) { Fail 'R06B2_ADDITIVE_BASELINE' }
$addBase.EXT_C8_BASELINE_MODE = 'VERIFY'
$addOriginal = Invoke-Root 'mixed' "SELECT COUNT(*),BIT_XOR(CRC32(CONCAT_WS(':',id,HEX(public_id),slug,version))) FROM organization WHERE demo_dataset_id IS NULL;" $add
# The hosted import command must refuse this unapproved fictional plan before any write, with the provider active.
$h05HostedBefore = Save-C9AdditiveSnapshot $add $addUuid $addPort
$hosted = $addBase.Clone(); $hosted.EXT_DB_USER = 'c9_add_importer'; $hosted.EXT_DB_PASSWORD = $addImporter; $hosted.EXT_CONTACT_ENCRYPTION_KEY = $contactKey; $hosted.EXT_C8_DEMO_PLAN = "$drive\mixed\fictional-plan.json"
$hosted.EXT_C8_HOSTED_CONFIRM = "IMPORT 96 INTO $addFp"; $hosted.EXT_C8_HOSTED_BACKUP_ID = "fictional-refusal-$token"; $hosted.EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256 = '0' * 64; $hosted.EXT_C8_HOSTED_RESTORE_ATTESTATION = 'NOT AN ATTESTATION'
$log = "$work\dumps\hosted-refusal.log"
$hostedExit = Invoke-Jar 'mixed' $hosted '--c8-hosted-demo-import' $log
if ($hostedExit -in @(0,124) -or -not (Test-Log $log 'Only the operator can attest')) { Fail 'R06B2_HOSTED_REFUSAL' }
Assert-C9AdditiveSnapshotEqual 'R06B2_HOSTED_REFUSAL_CHANGED' $h05HostedBefore (Save-C9AdditiveSnapshot $add $addUuid $addPort)
Assert-C9AdditiveScope $add $addUuid $addPort
$prof = @{ EXT_C8_PROOF_SCHEMA = $add; EXT_C8_PROOF_CONFIRM = "TEST ADDITIVE $add"; EXT_C8_PROOF_PORT = "$($roles.mixed.Port)"; EXT_C8_DEMO_PLAN = "$drive\mixed\fictional-plan.json"; EXT_C8_DEMO_PLAN_SHA256 = $addDigest
    EXT_C8_PROOF_ORIGINAL_ORG_ID = $originalOrg; EXT_C8_PROOF_OWNER_ACCOUNT_ID = $ownerId; EXT_DB_URL = $addUrl; EXT_DB_USER = 'c9_add_importer'; EXT_DB_PASSWORD = $addImporter; EXT_CONTACT_ENCRYPTION_KEY = $contactKey }
Assert-C9AdditiveScope $add $addUuid $addPort
$clock0 = [Diagnostics.Stopwatch]::StartNew(); $log = "$work\dumps\additive-import.log"
if ((Invoke-Jar 'mixed' $prof '--c8-additive-demo-isolated-proof' $log) -ne 0) { Fail 'R06B2_ADDITIVE_IMPORT' }
$addSeconds = [int]$clock0.Elapsed.TotalSeconds
if (-not (Select-String -LiteralPath $log -Pattern '^ISOLATED_DEMO_POSTFLIGHT_READ_ONLY_OK: 8 orgs, 960 allocations, ' -Quiet)) { Fail 'R06B2_ADDITIVE_MARKER' }
$log = "$work\dumps\additive-verify.log"
if ((Invoke-Jar 'mixed' $addBase '--c8-hosted-original-baseline' $log) -ne 0 -or -not (Test-Log $log 'C8_ORIGINAL_BASELINE_READ_ONLY_OK')) { Fail 'R06B2_ADDITIVE_ORIGINALS' }
if ((Invoke-Root 'mixed' "SELECT COUNT(*),BIT_XOR(CRC32(CONCAT_WS(':',id,HEX(public_id),slug,version))) FROM organization WHERE demo_dataset_id IS NULL;" $add) -cne $addOriginal) { Fail 'R06B2_ADDITIVE_ORIGINALS' }
if ((Invoke-Root 'mixed' 'SELECT COUNT(*),SUM(demo_dataset_id IS NOT NULL) FROM organization' $add) -cne "10`t8") { Fail 'R06B2_ADDITIVE_COUNTS' }
# A repeated import of the populated cohort must refuse and leave every row as it was.
$sum = Invoke-Root 'mixed' 'SELECT (SELECT COUNT(*) FROM allocation),(SELECT COUNT(*) FROM stock_movement),(SELECT COUNT(*) FROM audit_event)' $add
$h05RepeatBefore = Save-C9AdditiveSnapshot $add $addUuid $addPort
$repeatLog = "$work\dumps\additive-repeat.log"
$repeatExit = Invoke-Jar 'mixed' $prof '--c8-additive-demo-isolated-proof' $repeatLog
Assert-C9AdditiveRepeatRefusal $repeatExit $repeatLog
if ((Invoke-Root 'mixed' 'SELECT (SELECT COUNT(*) FROM allocation),(SELECT COUNT(*) FROM stock_movement),(SELECT COUNT(*) FROM audit_event)' $add) -cne $sum) { Fail 'R06B2_ADDITIVE_REPEAT' }
Assert-C9AdditiveSnapshotEqual 'R06B2_ADDITIVE_REPEAT_CHANGED' $h05RepeatBefore (Save-C9AdditiveSnapshot $add $addUuid $addPort)
# Deliberate committed same-count value change in one generated tagged organization only.
# Restore the original bytes from HEX, never interpolate arbitrary text or touch an original org.
Assert-C9AdditiveScope $add $addUuid $addPort
$chosen = (Invoke-Root 'mixed' 'SELECT id,HEX(display_name) FROM organization WHERE demo_dataset_id IS NOT NULL ORDER BY id LIMIT 1' $add) -split '\t'
if ($chosen.Count -ne 2 -or $chosen[0] -cnotmatch '\A[1-9][0-9]*\z' -or $chosen[1] -cnotmatch '\A(?:[0-9A-F]{2}){1,480}\z') { Fail 'R06B2_ADDITIVE_SENSITIVITY' }
$changedHex = if ($chosen[1] -ceq '4830352066696374696F6E616C') { '4830352066696374696F6E616C2032' } else { '4830352066696374696F6E616C' }
$guard = "id=$($chosen[0]) AND demo_dataset_id IS NOT NULL"
$changed = Invoke-Root 'mixed' "UPDATE organization SET display_name=CONVERT(UNHEX('$changedHex') USING utf8mb4) WHERE $guard AND HEX(display_name)='$($chosen[1])'; SELECT ROW_COUNT();" $add
if ($changed -cne '1') { Fail 'R06B2_ADDITIVE_SENSITIVITY' }
$h05Changed = Save-C9AdditiveSnapshot $add $addUuid $addPort
if ((Get-Sha $h05Changed.Domain) -ceq (Get-Sha $h05RepeatBefore.Domain) -or
    (Invoke-Root 'mixed' 'SELECT COUNT(*),SUM(demo_dataset_id IS NOT NULL) FROM organization' $add) -cne (@('10','8') -join [char]9)) { Fail 'R06B2_ADDITIVE_SENSITIVITY' }
Assert-Same 'R06B2_ADDITIVE_SENSITIVITY' $h05RepeatBefore.Sessions $h05Changed.Sessions
Assert-C9AdditiveScope $add $addUuid $addPort
$restored = Invoke-Root 'mixed' "UPDATE organization SET display_name=CONVERT(UNHEX('$($chosen[1])') USING utf8mb4) WHERE $guard AND HEX(display_name)='$changedHex'; SELECT ROW_COUNT();" $add
if ($restored -cne '1') { Fail 'R06B2_ADDITIVE_SENSITIVITY' }
Assert-C9AdditiveSnapshotEqual 'R06B2_ADDITIVE_SENSITIVITY' $h05RepeatBefore (Save-C9AdditiveSnapshot $add $addUuid $addPort)
$log = "$work\dumps\additive-final-baseline.log"
if ((Invoke-Jar 'mixed' $addBase '--c8-hosted-original-baseline' $log) -ne 0 -or -not (Test-Log $log 'C8_ORIGINAL_BASELINE_READ_ONLY_OK')) { Fail 'R06B2_ADDITIVE_ORIGINALS' }
Assert-C9AdditiveScope $add $addUuid $addPort
$null = Invoke-Root 'mixed' "DROP USER 'c9_add_importer'@'%','c9_add_ro'@'%'; DROP DATABASE $add;"
Assert-C9AdditiveScope $add $addUuid $addPort
if ((Invoke-Root 'mixed' "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='$add'") -cne '0' -or
    (Invoke-Root 'mixed' "SELECT COUNT(*) FROM mysql.user WHERE User IN ('c9_add_importer','c9_add_ro')") -cne '0') { Fail 'R06B2_ADDITIVE_CLEANUP' }
Assert-Same 'R06B2_ADDITIVE_ORIGINALS' $h05MixedBefore (Save-Rows 'mixed' 'h05-mixed-after')
Assert-Same 'R06B2_ADDITIVE_ORIGINALS' $sourceBefore (Save-Rows 'source' 'h05-source-after')
Assert-Same 'R06B2_ADDITIVE_ORIGINALS' $targetBefore (Save-Rows 'restore' 'h05-restore-after')
Remove-Item -LiteralPath "$work\mixed\baseline.properties", "$work\mixed\fictional-plan.json"
Write-Marker "R06B2_FICTIONAL_ADDITIVE_IMPORT_OK_WIN:importer_bean=true provider_active=true importer_grants_sufficient=true workspaces=8 originals=2 allocations=960 original_baseline=true repeat_refused=true hosted_command_refused_unattested=true exact_scoped_rows_equal=true sensitivity_detected=true scoped_cleanup_verified=true seconds=$addSeconds approved_plan_used=false"
