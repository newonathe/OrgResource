# Dot-sourced after verify-extended-c1.ps1 has passed its entire runtime gate.
if (-not $VerifyRecovery -or -not $dbProcess -or $dbProcess.HasExited -or
        -not ([IO.Path]::GetFileName($dataDirectory) -match '^OrgResource-C1-[a-f0-9]{32}$')) {
    throw 'Run the guarded Extended C verifier with -VerifyRecovery.'
}

function Get-RecoveryRows([int] $Port, [string] $Sql) {
    Assert-C1DatabasePort -Port $Port
    $rows = & $mysql --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$Port" --user=root --skip-password --database=orgresource_c1_acceptance --batch --raw --skip-column-names "--execute=$Sql"
    Assert-ExitCode 'disposable recovery read'
    return $rows
}

function Assert-RecoveryRefusal([string] $Path, [string] $ExpectedError) {
    Assert-C1DatabasePort -Port $recoveryPort
    try {
        $null = & $backupScript -Mode Restore -Schema orgresource_c1_acceptance -Port $recoveryPort -Archive $Path -DisposableAcceptance
        throw 'An invalid backup unexpectedly restored.'
    } catch {
        if ($_.Exception.Message -notlike "$ExpectedError*") { throw }
    }
    if ((Get-RecoveryRows $recoveryPort 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()') -ne '0') {
        throw 'A rejected restore changed the empty target schema.'
    }
}

function Assert-RestoredContact([string] $Row, [byte[]] $CorrectKey) {
    $columns = $Row.Split([char]9)
    $encrypted = [Convert]::FromHexString($columns[2])
    $nonce = [Convert]::FromHexString($columns[3])
    $aad = [Text.Encoding]::UTF8.GetBytes("orgresource-contact-v1|$($columns[0])|$($columns[1])")
    $ciphertext = [byte[]]$encrypted[0..($encrypted.Length - 17)]
    $tag = [byte[]]$encrypted[($encrypted.Length - 16)..($encrypted.Length - 1)]
    $plaintext = [byte[]]::new($ciphertext.Length)
    $wrongKey = [Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
    $cipher = [Security.Cryptography.AesGcm]::new($CorrectKey, 16)
    $wrong = [Security.Cryptography.AesGcm]::new($wrongKey, 16)
    try {
        $cipher.Decrypt($nonce, $ciphertext, $tag, $plaintext, $aad)
        if ($columns[4] -ne 'test-only-v1' -or [Text.Encoding]::ASCII.GetString($plaintext) -ne '+15555550111') {
            throw 'The restored fictional reminder contact did not match.'
        }
        try {
            $wrong.Decrypt($nonce, $ciphertext, $tag, $plaintext, $aad)
            throw 'An unrelated contact key unexpectedly authenticated.'
        } catch [Security.Cryptography.CryptographicException] { }
    } finally {
        [Array]::Clear($plaintext)
        [Array]::Clear($wrongKey)
        $cipher.Dispose()
        $wrong.Dispose()
    }
}

$sourcePort = $databasePort
$recoveryPort = Get-FreeLoopbackPort
$recoveryDir = Join-Path $dataDirectory 'restore-instance'
$backupScript = Join-Path $PSScriptRoot 'extended-c-backup.ps1'
$archive = Join-Path $dataDirectory 'orgresource-c5-isolated.aesgcm'
$corrupted = Join-Path $dataDirectory 'orgresource-c5-tampered.aesgcm'
$envNames = @('EXT_DB_URL', 'EXT_DB_MIGRATION_URL', 'MYSQL_HOME', 'EXT_BACKUP_KEY', 'EXT_E2E_BASE_URL',
    'EXT_E2E_USERNAME', 'EXT_E2E_PASSWORD', 'ORGRESOURCE_E2E_SCREENSHOT_DIR', 'EXT_E2E_RESTORED_PROJECT')
$savedRecoveryEnvironment = @{}
foreach ($name in $envNames) { $savedRecoveryEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }
$target = $null
$restored = $null
$otherActor = $null
$oldSession = $null
$disabledMember = $null
$backupKey = [Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
$contactKey = [Convert]::FromBase64String($env:EXT_CONTACT_ENCRYPTION_KEY)
try {
    $oldSession = New-C1Session $adminUsername $recoveredPassword
    $suspendedUsername = 'c5-suspended-' + (New-RandomHex -Bytes 6)
    $suspendedPassword = New-RandomHex
    $createdInvite = Invoke-C1Post $oldSession "/api/v2/orgs/$orgId/invitations" @{ role = 'MEMBER' }
    if ($createdInvite.StatusCode -ne 201) { throw 'Could not create the recovery suspension fixture invitation.' }
    $account = New-C1AnonymousSession
    try {
        $joined = Invoke-C1Post $account '/api/v2/invitations/redeem' @{
            bearerToken = $createdInvite.Data.bearerToken; username = $suspendedUsername
            password = $suspendedPassword; displayName = 'Fictional suspended recovery member'
        }
        if ($joined.StatusCode -ne 201) { throw 'Could not create the recovery suspension fixture account.' }
        $membership = $joined.Data.membershipId
        $suspended = Invoke-C1Post $oldSession "/api/v2/orgs/$orgId/members/$membership/suspend" @{} '"v0"'
        if ($suspended.StatusCode -ne 200 -or $suspended.Data.status -ne 'SUSPENDED') {
            throw 'Could not suspend the recovery fixture membership.'
        }
    } finally { $account.Client.Dispose() }
    Stop-C1App
    $env:MYSQL_HOME = $mysqlHome
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String($backupKey)
    Assert-C1DatabasePort -Port $sourcePort
    $backupRecord = & $backupScript -Mode Backup -Schema orgresource_c1_acceptance -Port $sourcePort -Archive $archive -DisposableAcceptance
    $null = New-Item -ItemType Directory -Path $recoveryDir
    $init = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $recoveryDir + '"'), '--initialize-insecure') -Wait -PassThru -WindowStyle Hidden
    if ($init.ExitCode -ne 0) { throw 'Restore MySQL initialization failed.' }
    $target = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $recoveryDir + '"'), "--port=$recoveryPort", '--bind-address=127.0.0.1', '--mysqlx=0',
        ('--log-error="' + (Join-Path $recoveryDir 'mysql.err.log') + '"')) -PassThru -WindowStyle Hidden
    Wait-C1DatabaseReady -Process $target -Port $recoveryPort
    $databasePort = $recoveryPort
    Invoke-MysqlBootstrap 'CREATE DATABASE orgresource_c1_acceptance CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;'

    $changed = [IO.File]::ReadAllBytes($archive)
    $changed[$changed.Length - 1] = $changed[$changed.Length - 1] -bxor 1
    [IO.File]::WriteAllBytes($corrupted, $changed)
    Assert-RecoveryRefusal $corrupted 'Backup authentication failed.'
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    Assert-RecoveryRefusal $archive 'Backup authentication failed.'
    $env:EXT_BACKUP_KEY = $null
    Assert-RecoveryRefusal $archive 'Protected backup key unavailable.'
    $env:EXT_BACKUP_KEY = [Convert]::ToBase64String($backupKey)
    Assert-C1DatabasePort -Port $recoveryPort
    $restoreRecord = & $backupScript -Mode Restore -Schema orgresource_c1_acceptance -Port $recoveryPort -Archive $archive -DisposableAcceptance
    if ($restoreRecord.Sha256 -ne $backupRecord.Sha256) { throw 'Encrypted backup identity changed.' }

    # Validate database objects before boot can alter idle-session or reminder rows.
    $tables = @(Get-RecoveryRows $sourcePort 'SHOW TABLES')
    if (@(Compare-Object $tables @(Get-RecoveryRows $recoveryPort 'SHOW TABLES')).Count -ne 0) { throw 'Restored table set differs.' }
    $grant = @("CREATE USER 'c1_app'@'127.0.0.1' IDENTIFIED BY '$dbPassword';")
    foreach ($table in $tables) {
        if ($table -notmatch '^[A-Za-z_]+$') { throw 'Unexpected identifier in restore fixture.' }
        if ($table -in @('SPRING_SESSION', 'SPRING_SESSION_ATTRIBUTES')) {
            if ((Get-RecoveryRows $recoveryPort "SELECT COUNT(*) FROM $table") -ne '0') { throw 'Historical sessions were restored.' }
        } else {
            $query = "CHECKSUM TABLE $table EXTENDED"
            if ((Get-RecoveryRows $sourcePort $query) -cne (Get-RecoveryRows $recoveryPort $query)) {
                throw "Restored table contents differ for $table."
            }
        }
        $privileges = switch ($table) {
            'stock_movement' { 'SELECT, INSERT' }
            'audit_event' { 'SELECT, INSERT' }
            'flyway_schema_history' { 'SELECT' }
            default { 'SELECT, INSERT, UPDATE, DELETE' }
        }
        $grant += "GRANT $privileges ON orgresource_c1_acceptance.$table TO 'c1_app'@'127.0.0.1';"
    }
    $foreignKeys = 'SELECT TABLE_NAME,CONSTRAINT_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME
        FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL
        ORDER BY TABLE_NAME,CONSTRAINT_NAME,ORDINAL_POSITION'
    if (@(Compare-Object @(Get-RecoveryRows $sourcePort $foreignKeys) @(Get-RecoveryRows $recoveryPort $foreignKeys)).Count -ne 0) {
        throw 'Restored foreign keys differ.'
    }
    $contact = Get-RecoveryRows $recoveryPort "SELECT organization_id,BIN_TO_UUID(public_id,0),HEX(phone_ciphertext),HEX(phone_nonce),phone_key_id
        FROM allocation WHERE phone_key_id='test-only-v1' AND reminder_outcome='SIMULATED' ORDER BY id LIMIT 1"
    if (-not $contact) { throw 'The restored encryption fixture was absent.' }
    Assert-RestoredContact $contact $contactKey

    Invoke-MysqlBootstrap ($grant -join [Environment]::NewLine)
    $env:EXT_DB_URL = $savedRecoveryEnvironment.EXT_DB_URL.Replace(":$sourcePort/", ":$recoveryPort/")
    $env:EXT_DB_MIGRATION_URL = $env:EXT_DB_URL
    Start-C1PackagedApp
    if ((Invoke-C1Get $oldSession '/api/v2/session').StatusCode -ne 401) { throw 'A pre-backup session remained authenticated.' }
    $restored = New-C1Session $adminUsername $recoveredPassword
    if ($restored.OrgId -ne $orgId) {
        if ((Invoke-C1Post $restored '/api/v2/session/active-org' @{ organizationId = $orgId }).StatusCode -ne 200) {
            throw 'Restored owner could not select the original workspace.'
        }
        $restored.Token = Get-C1Csrf $restored.Client $restored.Base
    }
    $resourceBefore = Invoke-C1Get $restored $resourcePath
    $receiptBefore = Invoke-C1Get $restored $c4OperationPath
    if ($resourceBefore.Data.totalQuantity -ne 12 -or $resourceBefore.Data.availableQuantity -ne 12 -or
            $receiptBefore.StatusCode -ne 200 -or $receiptBefore.Data.kind -ne 'RECEIPT') {
        throw 'The restored inventory or permanent receipt differs.'
    }
    if ((Get-RecoveryRows $recoveryPort "SELECT COUNT(*) FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN('$c4OperationId',0)") -ne '0') {
        throw 'The durable replay fixture retained a temporary receipt.'
    }
    $replayed = Invoke-C1Post $restored "$receiptResourcePath/receive" $c4OperationBody $receiptResourceState.ETag $c4OperationKey
    $resourceAfter = Invoke-C1Get $restored $receiptResourcePath
    $receiptMoves = Invoke-C1Get $restored "$receiptResourcePath/movements?limit=10"
    if ($replayed.StatusCode -ne 200 -or $replayed.Data.kind -ne 'RECEIPT' -or $replayed.Data.operationId -ne $c4OperationId -or
            $resourceAfter.StatusCode -ne 200 -or $resourceAfter.Data.totalQuantity -ne 1 -or $resourceAfter.Data.availableQuantity -ne 1 -or
            $receiptMoves.StatusCode -ne 200 -or @($receiptMoves.Data.items).Count -ne 1 -or
            $receiptMoves.Data.items[0].operationId -ne $c4OperationId) {
        throw 'Replay after recovery changed permanent stock history.'
    }
    $otherActor = New-C1Session $receiptUsername $receiptPassword
    if ((Invoke-C1Get $otherActor $c4OperationPath).StatusCode -ne 403) { throw 'The restored MEMBER could read a protected receipt.' }
    $disabledMember = New-C1AnonymousSession
    $login = Invoke-C1Post $disabledMember '/api/v2/auth/login' @{ username = $suspendedUsername; password = $suspendedPassword }
    $disabledMember.Token = $login.Data.csrfToken
    if ($login.StatusCode -ne 200 -or @( (Invoke-C1Get $disabledMember '/api/v2/orgs').Data ).Count -ne 0 -or
            (Invoke-C1Get $disabledMember $c4OperationPath).StatusCode -ne 404) {
        throw 'A restored suspended membership gained workspace access.'
    }
    $foreignOrg = @( (Invoke-C1Get $restored '/api/v2/orgs').Data | Where-Object { $_.id -ne $orgId } )[0].id
    if (-not $foreignOrg -or (Invoke-C1Get $otherActor "/api/v2/orgs/$foreignOrg/resources").StatusCode -ne 404) {
        throw 'The restored MEMBER crossed an organization boundary.'
    }
    $image = $restored.Client.GetAsync("$($restored.Base)/api/resources/$imageResourceId/image").GetAwaiter().GetResult()
    try {
        $restoredHash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($image.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult())).ToLowerInvariant()
        if ([int]$image.StatusCode -ne 200 -or $restoredHash -ne $imageUpload.sha256) { throw 'Restored image bytes differ.' }
    } finally { $image.Dispose() }

    $env:EXT_E2E_BASE_URL = $restored.Base
    $env:EXT_E2E_USERNAME = $adminUsername
    $env:EXT_E2E_PASSWORD = $recoveredPassword
    $env:ORGRESOURCE_E2E_SCREENSHOT_DIR = Join-Path $recoveryDir 'screenshots'
    $env:EXT_E2E_RESTORED_PROJECT = 'C1 cross-timezone 2040 project'
    Push-Location $frontend
    try {
        & npm run e2e -- --grep 'the packaged Java application serves'
        Assert-ExitCode 'authenticated Chromium on restored MySQL'
    } finally { Pop-Location }
    Write-Output "C5 recovery PASS: independent MySQL processes, $($tables.Count) tables, Flyway/JAR startup, Chromium, retired sessions, durable replay, and key/tamper refusal."
    Write-Output "Encrypted archive: $($backupRecord.Bytes) bytes; SHA-256 $($backupRecord.Sha256)"
} finally {
    try {
        try {
            Stop-C1App
        } finally {
            try {
                foreach ($session in @($oldSession, $restored, $otherActor, $disabledMember)) { if ($session) { $session.Client.Dispose() } }
            } finally {
                Stop-C1Database -Process $target -Port $recoveryPort
            }
        }
    } finally {
        $databasePort = $sourcePort
        try {
            foreach ($name in $envNames) { [Environment]::SetEnvironmentVariable($name, $savedRecoveryEnvironment[$name], 'Process') }
        } finally {
            [Array]::Clear($contactKey)
            [Array]::Clear($backupKey)
        }
    }
}
