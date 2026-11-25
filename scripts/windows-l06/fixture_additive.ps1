# Dot-sourced, FixtureOnly. The real importer bean and Spring profile, unmodified JAR, under the owner-only provider and the exact
# importer grants, adding a generated FICTIONAL eight-workspace plan to the fictional originals. Never the approved plan.
$add = "orgresource_c8_additive_ci_$token"
$dump = "$work\dumps\additive-original.sql"
if ((Invoke-Contained 'restore' "$mysqlHome\bin\mysqldump.exe" @('--no-defaults', '--protocol=tcp', '--host=127.0.0.1', "--port=$($roles.restore.Port)", '--user=root', '--skip-comments', '--skip-dump-date', '--hex-blob', '--skip-lock-tables', '--no-tablespaces', '--set-gtid-purged=OFF', '--skip-column-statistics', '--single-transaction', "--result-file=$drive\restore\additive.sql", $t) @{ MYSQL_PWD = $roles.restore.Password } '' '' (New-Scratch 'd.err') 120) -ne 0) { Fail 'R06B2_ADDITIVE_FIXTURE' }
Move-Item -LiteralPath "$work\restore\additive.sql" -Destination $dump
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
$hosted = $addBase.Clone(); $hosted.EXT_DB_USER = 'c9_add_importer'; $hosted.EXT_DB_PASSWORD = $addImporter; $hosted.EXT_CONTACT_ENCRYPTION_KEY = $contactKey; $hosted.EXT_C8_DEMO_PLAN = "$drive\mixed\fictional-plan.json"
$hosted.EXT_C8_HOSTED_CONFIRM = "IMPORT 96 INTO $addFp"; $hosted.EXT_C8_HOSTED_BACKUP_ID = "fictional-refusal-$token"; $hosted.EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256 = '0' * 64; $hosted.EXT_C8_HOSTED_RESTORE_ATTESTATION = 'NOT AN ATTESTATION'
$log = "$work\dumps\hosted-refusal.log"
if ((Invoke-Jar 'mixed' $hosted '--c8-hosted-demo-import' $log) -eq 0 -or -not (Test-Log $log 'Only the operator can attest')) { Fail 'R06B2_HOSTED_REFUSAL' }
$prof = @{ EXT_C8_PROOF_SCHEMA = $add; EXT_C8_PROOF_CONFIRM = "TEST ADDITIVE $add"; EXT_C8_PROOF_PORT = "$($roles.mixed.Port)"; EXT_C8_DEMO_PLAN = "$drive\mixed\fictional-plan.json"; EXT_C8_DEMO_PLAN_SHA256 = $addDigest
    EXT_C8_PROOF_ORIGINAL_ORG_ID = $originalOrg; EXT_C8_PROOF_OWNER_ACCOUNT_ID = $ownerId; EXT_DB_URL = $addUrl; EXT_DB_USER = 'c9_add_importer'; EXT_DB_PASSWORD = $addImporter; EXT_CONTACT_ENCRYPTION_KEY = $contactKey }
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
if ((Invoke-Jar 'mixed' $prof '--c8-additive-demo-isolated-proof' "$work\dumps\additive-repeat.log") -eq 0 -or (Invoke-Root 'mixed' 'SELECT (SELECT COUNT(*) FROM allocation),(SELECT COUNT(*) FROM stock_movement),(SELECT COUNT(*) FROM audit_event)' $add) -cne $sum) { Fail 'R06B2_ADDITIVE_REPEAT' }
$null = Invoke-Root 'mixed' "DROP USER 'c9_add_importer'@'%','c9_add_ro'@'%'; DROP DATABASE $add;"
Remove-Item -LiteralPath "$work\mixed\baseline.properties", "$work\mixed\fictional-plan.json"
Write-Marker "R06B2_FICTIONAL_ADDITIVE_IMPORT_OK_WIN:importer_bean=true provider_active=true importer_grants_sufficient=true workspaces=8 originals=2 allocations=960 original_baseline=true repeat_refused=true hosted_command_refused_unattested=true seconds=$addSeconds approved_plan_used=false"
