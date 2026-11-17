param([switch] $SkipFrontend)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$experiment = Join-Path $repoRoot 'extended\backend'
$frontend = Join-Path $repoRoot 'frontend'
$pom = Join-Path $experiment 'pom.xml'
$mvnw = Join-Path $repoRoot 'backend\mvnw.cmd'
$mysqlHome = if ($env:MYSQL_HOME) { $env:MYSQL_HOME } else { Join-Path $env:ProgramFiles 'MySQL\MySQL Server 8.0' }
$mysqlBin = Join-Path $mysqlHome 'bin'
$mysqld = Join-Path $mysqlBin 'mysqld.exe'
$mysql = Join-Path $mysqlBin 'mysql.exe'
$mysqladmin = Join-Path $mysqlBin 'mysqladmin.exe'
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java.exe' }
$dbProcess = $null
$appProcess = $null
$databasePort = 0
$appPort = 0
$appLaunch = 0
$dataDirectory = $null
$savedEnvironment = @{}
$environmentNames = @(
    'EXT_DB_URL', 'EXT_DB_USER', 'EXT_DB_PASSWORD',
    'EXT_BOOTSTRAP_SLUG', 'EXT_BOOTSTRAP_ORG_NAME', 'EXT_BOOTSTRAP_USERNAME',
    'EXT_BOOTSTRAP_DISPLAY_NAME', 'EXT_BOOTSTRAP_PASSWORD',
    'EXT_SMS_ENABLED', 'EXT_SCHEDULER_DELAY_MS', 'EXT_SERVER_PORT',
    'SPRING_PROFILES_ACTIVE'
)

function Assert-ExitCode([string] $Label) {
    if ($LASTEXITCODE -ne 0) { throw "$Label failed with exit code $LASTEXITCODE." }
}

function Get-FreeLoopbackPort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    $listener.Start()
    try { return ([System.Net.IPEndPoint]$listener.LocalEndpoint).Port }
    finally { $listener.Stop() }
}

function New-RandomHex([int] $Bytes = 24) {
    $buffer = New-Object byte[] $Bytes
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($buffer) } finally { $rng.Dispose() }
    return ([System.BitConverter]::ToString($buffer)).Replace('-', '').ToLowerInvariant()
}

function Stop-C1App {
    if ($script:appProcess -and -not $script:appProcess.HasExited) {
        Stop-Process -Id $script:appProcess.Id -Force
        $null = $script:appProcess.WaitForExit()
    }
    $script:appProcess = $null
}

function Invoke-MysqlBootstrap([string] $Sql) {
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $mysql
    $startInfo.Arguments = "--protocol=tcp --host=127.0.0.1 --port=$databasePort --user=root --batch --raw"
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $process = [System.Diagnostics.Process]::Start($startInfo)
    $process.StandardInput.WriteLine($Sql)
    $process.StandardInput.Close()
    $output = $process.StandardOutput.ReadToEnd()
    $errorOutput = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if ($process.ExitCode -ne 0) {
        throw "Isolated MySQL bootstrap SQL failed with exit code $($process.ExitCode): $errorOutput"
    }
}

function Start-C1PackagedApp([string] $Timezone = 'Asia/Manila') {
    $jar = Join-Path $experiment 'target\extended-backend-0.1.0-SNAPSHOT.jar'
    if (-not (Test-Path -LiteralPath $jar)) { throw "Packaged C1 JAR is missing: $jar" }
    $script:appLaunch++
    $script:appProcess = Start-Process -FilePath $java -ArgumentList @("-Duser.timezone=$Timezone", '-jar', ('"' + $jar + '"')) `
        -WorkingDirectory $experiment -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $dataDirectory "c1-app-$appLaunch.stdout.log") `
        -RedirectStandardError (Join-Path $dataDirectory "c1-app-$appLaunch.stderr.log")
    $base = "http://127.0.0.1:$appPort"
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        if ($script:appProcess.HasExited) { throw "Packaged JAR exited early; inspect the C1 app log in the temporary run directory." }
        try {
            $response = Invoke-WebRequest -Uri "$base/" -TimeoutSec 2 -UseBasicParsing
        } catch { Start-Sleep -Milliseconds 500; continue }
        if ($response.StatusCode -eq 200) {
            $assets = [regex]::Matches($response.Content, '(?:src|href)="([^"]*/assets/[^"]+\.(?:js|css))"')
            if ($assets.Count -lt 2) { throw 'Packaged React entry did not reference both JS and CSS assets.' }
            foreach ($asset in $assets) {
                $assetResponse = Invoke-WebRequest -Uri ($base + $asset.Groups[1].Value) -TimeoutSec 5 -UseBasicParsing
                if ($assetResponse.StatusCode -ne 200 -or $assetResponse.Content.Length -lt 100) {
                    throw "Packaged React asset failed: $($asset.Groups[1].Value)"
                }
            }
            $probe = [System.Net.Http.HttpClient]::new()
            try {
                $api = $probe.GetAsync("$base/api/departments").GetAwaiter().GetResult()
                $apiBody = $api.Content.ReadAsStringAsync().GetAwaiter().GetResult()
                if ([int]$api.StatusCode -ne 401 -or $apiBody -notmatch 'AUTH_REQUIRED') {
                    throw 'Packaged JAR API did not return its expected unauthenticated JSON response.'
                }
            } finally { $probe.Dispose() }
            return
        }
        Start-Sleep -Milliseconds 500
    }
    throw 'Packaged JAR did not serve localhost within 45 seconds.'
}

function New-C1Client {
    $handler = [System.Net.Http.HttpClientHandler]::new()
    $handler.CookieContainer = [System.Net.CookieContainer]::new()
    return [System.Net.Http.HttpClient]::new($handler)
}

function Get-C1Csrf($Client, [string] $Base) {
    $response = $Client.GetAsync("$Base/api/v2/auth/csrf").GetAwaiter().GetResult()
    $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json
    if (-not $response.IsSuccessStatusCode -or [string]::IsNullOrWhiteSpace([string]$body.data.csrfToken)) {
        throw 'Packaged JAR did not provide a pre-authentication CSRF token.'
    }
    return [string]$body.data.csrfToken
}

function Invoke-C1Post($Session, [string] $Path, [object] $Body, [string] $IfMatch = $null) {
    $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post, "$($Session.Base)$Path")
    $request.Headers.Add('Origin', $Session.Base)
    if (-not [string]::IsNullOrWhiteSpace([string]$Session.Token)) { $request.Headers.Add('X-CSRF-TOKEN', [string]$Session.Token) }
    if (-not [string]::IsNullOrWhiteSpace($IfMatch)) { $request.Headers.Add('If-Match', $IfMatch) }
    if ($null -ne $Body) {
        $json = ConvertTo-Json -InputObject $Body -Depth 8 -Compress
        $request.Content = [System.Net.Http.StringContent]::new($json, [System.Text.Encoding]::UTF8, 'application/json')
    }
    $response = $Session.Client.SendAsync($request).GetAwaiter().GetResult()
    $raw = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    $parsed = if ([string]::IsNullOrWhiteSpace($raw)) { $null } else { $raw | ConvertFrom-Json }
    $etag = if ($response.Headers.ETag) { [string]$response.Headers.ETag } else { $null }
    $data = if ($parsed) { $parsed.data } else { $null }
    $statusCode = [int]$response.StatusCode
    $request.Dispose()
    $response.Dispose()
    return @{ StatusCode = $statusCode; Data = $data; ETag = $etag; Raw = $raw }
}

function Invoke-C1Get($Session, [string] $Path) {
    $response = $Session.Client.GetAsync("$($Session.Base)$Path").GetAwaiter().GetResult()
    $raw = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    $parsed = if ([string]::IsNullOrWhiteSpace($raw)) { $null } else { $raw | ConvertFrom-Json }
    $data = if ($parsed) { $parsed.data } else { $null }
    $statusCode = [int]$response.StatusCode
    $response.Dispose()
    return @{ StatusCode = $statusCode; Data = $data; Raw = $raw }
}

function New-C1Session([string] $Username, [string] $Password) {
    $client = New-C1Client
    $base = "http://127.0.0.1:$appPort"
    $preCsrf = Get-C1Csrf $client $base
    $session = @{ Client = $client; Token = $preCsrf; Base = $base; Username = $Username; Password = $Password }
    $login = Invoke-C1Post $session '/api/v2/auth/login' @{ username = $Username; password = $Password }
    if ($login.StatusCode -ne 200 -or [string]::IsNullOrWhiteSpace([string]$login.Data.csrfToken)) {
        throw 'Packaged JAR JSON login failed.'
    }
    $session.Token = [string]$login.Data.csrfToken
    $session.OrgId = [string]$login.Data.session.activeOrgId
    return $session
}

function New-C1OwnerSession {
    $session = New-C1Session 'c1-owner' $env:EXT_BOOTSTRAP_PASSWORD
    return $session
}

function New-C1AnonymousSession {
    $client = New-C1Client
    $base = "http://127.0.0.1:$appPort"
    return @{ Client = $client; Token = (Get-C1Csrf $client $base); Base = $base }
}

<#
The helper functions deliberately keep credentials and bearer tokens in process memory only;
they never print request bodies or response payloads.
#>
function Add-PersistedDemoDepartment($Session, [string] $Name) {
    $content = [System.Net.Http.StringContent]::new((@{ name = $Name } | ConvertTo-Json -Compress),
        [System.Text.Encoding]::UTF8, 'application/json')
    $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post,
        "$($Session.Base)/api/departments")
    $request.Headers.Add('Origin', $Session.Base)
    $request.Headers.Add('X-CSRF-TOKEN', $Session.Token)
    $request.Content = $content
    $response = $Session.Client.SendAsync($request).GetAwaiter().GetResult()
    $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    $content.Dispose()
    $request.Dispose()
    if ([int]$response.StatusCode -ne 201) { throw "Packaged JAR department POST returned $([int]$response.StatusCode)." }
    return ($body | ConvertFrom-Json).id
}

function Add-PersistedDemoProject($Session, [long] $DepartmentId) {
    $startAt = '2040-01-01T00:30:15'
    $endAt = '2040-01-01T02:45:30'
    $created = Invoke-C1Post $Session '/api/projects' @{
        departmentId = $DepartmentId; name = 'C1 cross-timezone 2040 project'
        startAt = $startAt; endAt = $endAt
    }
    $project = if ($created.StatusCode -eq 201) { $created.Raw | ConvertFrom-Json } else { $null }
    if ($created.StatusCode -ne 201 -or $project.startAt -ne $startAt -or $project.endAt -ne $endAt) {
        throw "Packaged JAR failed to create the fictional 2040 project with unchanged local date-time values (HTTP $($created.StatusCode))."
    }
    return @{ Id = [long]$project.id; StartAt = $startAt; EndAt = $endAt }
}

try {
    if (-not (Test-Path -LiteralPath $mvnw)) { throw 'Maven Wrapper is missing.' }
    foreach ($file in @($mysqld, $mysql, $mysqladmin)) {
        if (-not (Test-Path -LiteralPath $file)) { throw "MySQL 8 executable is missing: $file" }
    }
    $version = & $java -version 2>&1 | Out-String
    if ($version -notmatch '21\.') { throw 'C1 requires the approved Java 21 runtime.' }
    if (-not (Test-Path -LiteralPath (Join-Path $frontend 'package-lock.json'))) { throw 'Frontend lockfile is missing.' }

    foreach ($name in $environmentNames) {
        $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
    }

    $databasePort = Get-FreeLoopbackPort
    $appPort = Get-FreeLoopbackPort
    while ($appPort -eq $databasePort) { $appPort = Get-FreeLoopbackPort }
    $dataDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('OrgResource-C1-' + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Path $dataDirectory | Out-Null

    $initialize = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $dataDirectory + '"'), '--initialize-insecure', '--console') -Wait -PassThru -NoNewWindow
    if ($initialize.ExitCode -ne 0) { throw 'Fresh isolated MySQL datadir initialization failed.' }

    $dbProcess = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $dataDirectory + '"'), "--port=$databasePort", '--bind-address=127.0.0.1', '--mysqlx=0',
        ('--pid-file="' + (Join-Path $dataDirectory 'c1.pid') + '"'),
        ('--log-error="' + (Join-Path $dataDirectory 'mysql.err.log') + '"')) -WindowStyle Hidden -PassThru
    $ready = $false
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        if ($dbProcess.HasExited) { throw 'Isolated MySQL exited during startup; inspect its temporary error log.' }
        & $mysqladmin --protocol=tcp --host=127.0.0.1 --port=$databasePort --user=root ping *> $null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Milliseconds 500
    }
    if (-not $ready) { throw 'Isolated MySQL did not become ready within 45 seconds.' }

    $dbPassword = New-RandomHex
    $bootstrapPassword = New-RandomHex
    $sql = @"
CREATE DATABASE orgresource_c1_acceptance CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'c1_app'@'127.0.0.1' IDENTIFIED BY '$dbPassword';
GRANT ALL PRIVILEGES ON orgresource_c1_acceptance.* TO 'c1_app'@'127.0.0.1';
FLUSH PRIVILEGES;
"@
    Invoke-MysqlBootstrap $sql

    $env:EXT_DB_URL = "jdbc:mysql://127.0.0.1:$databasePort/orgresource_c1_acceptance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    $env:EXT_DB_USER = 'c1_app'
    $env:EXT_DB_PASSWORD = $dbPassword
    $env:EXT_BOOTSTRAP_SLUG = 'c1-seed-workspace'
    $env:EXT_BOOTSTRAP_ORG_NAME = 'Fictional C1 Workspace'
    $env:EXT_BOOTSTRAP_USERNAME = 'c1-owner'
    $env:EXT_BOOTSTRAP_DISPLAY_NAME = 'C1 Owner'
    $env:EXT_BOOTSTRAP_PASSWORD = $bootstrapPassword
    $env:EXT_SMS_ENABLED = 'false'
    $env:EXT_SCHEDULER_DELAY_MS = '60000'
    $env:EXT_SERVER_PORT = [string]$appPort
    $env:SPRING_PROFILES_ACTIVE = 'c1-test'

    if (-not $SkipFrontend) {
        Push-Location $frontend
        try {
            & npm ci
            Assert-ExitCode 'npm ci'
            & npm run typecheck
            Assert-ExitCode 'frontend typecheck'
            & npm test
            Assert-ExitCode 'frontend unit tests'
            & npm run lint
            Assert-ExitCode 'frontend lint'
            & npm run build
            Assert-ExitCode 'frontend production build'
        } finally { Pop-Location }
    }

    Push-Location $repoRoot
    try {
        & $mvnw -B -f $pom clean verify
        Assert-ExitCode 'C1 Maven clean verify'
        $dependencyFile = Join-Path $experiment 'target\c1-effective-dependencies.txt'
        & $mvnw -B -f $pom dependency:tree -Dverbose `
            '-Dincludes=org.springframework.boot:*,org.springframework:*,org.springframework.security:*,org.glassfish.jersey:*,org.hibernate.orm:*,org.hibernate.validator:*,jakarta.validation:*,javax.validation:*,org.apache.bval:*,com.mysql:*,org.flywaydb:*,com.squareup.retrofit2:*,com.google.code.gson:*,com.squareup.okhttp3:*' `
            *> $dependencyFile
        Assert-ExitCode 'C1 effective dependency tree'
    } finally { Pop-Location }

    Start-C1PackagedApp
    $ownerSession = New-C1OwnerSession
    $orgId = $ownerSession.OrgId
    if ([string]::IsNullOrWhiteSpace($orgId)) { throw 'Bootstrap OWNER session did not select its organization.' }

    $invite = Invoke-C1Post $ownerSession "/api/v2/orgs/$orgId/invitations" @{ role = 'ADMIN' }
    if ($invite.StatusCode -ne 201) { throw 'Packaged JAR failed to create the fictional ADMIN invitation.' }
    $adminUsername = 'c1-check-' + (New-RandomHex -Bytes 6)
    $adminPassword = 'Admin-C1-' + (New-RandomHex -Bytes 24)
    $adminDisplayName = 'C1 Acceptance Administrator'
    $anonymous = New-C1AnonymousSession
    $redeemed = Invoke-C1Post $anonymous '/api/v2/invitations/redeem' @{
        bearerToken = [string]$invite.Data.bearerToken; username = $adminUsername
        password = $adminPassword; displayName = $adminDisplayName
    }
    if ($redeemed.StatusCode -ne 201 -or $redeemed.Data.recoveryCodes.Count -ne 5) {
        throw 'Packaged JAR failed to redeem the invitation into a new fictional account with one-time recovery codes.'
    }
    $adminSession = @{ Client = $anonymous.Client; Token = [string]$redeemed.Data.csrfToken; Base = $anonymous.Base }
    $members = Invoke-C1Get $ownerSession "/api/v2/orgs/$orgId/members"
    $target = @($members.Data.items | Where-Object { $_.role -eq 'ADMIN' -and $_.displayName -eq $adminDisplayName } | Select-Object -First 1)
    if ($members.StatusCode -ne 200 -or $target.Count -ne 1) { throw 'Packaged JAR member listing did not show the invited ADMIN account.' }

    $transfer = Invoke-C1Post $ownerSession "/api/v2/orgs/$orgId/transfer-ownership" @{
        toMembershipId = [string]$target[0].id; currentPassword = $env:EXT_BOOTSTRAP_PASSWORD
    }
    if ($transfer.StatusCode -ne 201 -or [string]::IsNullOrWhiteSpace($transfer.ETag)) {
        throw 'Packaged JAR failed to start the fictional ownership transfer.'
    }
    $accepted = Invoke-C1Post $adminSession "/api/v2/orgs/$orgId/accept-transfer" @{
        transferId = [string]$transfer.Data.id
    } $transfer.ETag
    if ($accepted.StatusCode -ne 200) { throw 'Packaged JAR failed to atomically transfer ownership to the invited ADMIN.' }

    $rotation = Invoke-C1Post $adminSession '/api/v2/account/recovery-codes/rotate' @{ currentPassword = $adminPassword }
    if ($rotation.StatusCode -ne 200 -or $rotation.Data.codes.Count -ne 5) {
        throw 'Packaged JAR failed to rotate the invited account recovery codes.'
    }
    $usedRecoveryCode = [string]$rotation.Data.codes[0]
    $recoveredPassword = 'Recovered-C1-' + (New-RandomHex -Bytes 24)
    $recovery = New-C1AnonymousSession
    $recovered = Invoke-C1Post $recovery '/api/v2/auth/recover' @{
        username = $adminUsername; oneTimeCode = $usedRecoveryCode; newPassword = $recoveredPassword
    }
    if ($recovered.StatusCode -ne 200) { throw 'Packaged JAR password recovery did not return its privacy-preserving response.' }
    $revokedSession = Invoke-C1Get $adminSession '/api/v2/session'
    if ($revokedSession.StatusCode -ne 401) { throw 'Password recovery did not revoke the prior packaged-JAR session.' }

    $persistedSession = New-C1Session $adminUsername $recoveredPassword
    $name = 'C1 packaged persistence ' + [guid]::NewGuid().ToString('N')
    $departmentId = Add-PersistedDemoDepartment $persistedSession $name
    $project = Add-PersistedDemoProject $persistedSession $departmentId
    $ownerSession.Client.Dispose()
    $recovery.Client.Dispose()
    Stop-C1App

    Start-C1PackagedApp 'UTC'
    $revokedAfterRestart = Invoke-C1Get $adminSession '/api/v2/session'
    if ($revokedAfterRestart.StatusCode -ne 401) { throw 'The recovery-revoked session became valid after the packaged JAR restart.' }
    $persistedState = Invoke-C1Get $persistedSession '/api/v2/session'
    if ($persistedState.StatusCode -ne 200) { throw 'Spring Session JDBC did not preserve the authenticated session across the packaged JAR restart.' }
    $response = $persistedSession.Client.GetAsync("$($persistedSession.Base)/api/departments").GetAwaiter().GetResult()
    $departments = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json
    if (-not $response.IsSuccessStatusCode -or -not ($departments | Where-Object { $_.id -eq $departmentId -and $_.name -eq $name })) {
        throw 'Packaged JAR failed to read the Department persisted before its restart.'
    }
    $response.Dispose()
    $projectState = Invoke-C1Get $persistedSession "/api/projects?departmentId=$departmentId"
    $projectRows = if ($projectState.StatusCode -eq 200) { @($projectState.Raw | ConvertFrom-Json) } else { @() }
    $reloadedProject = @($projectRows | Where-Object { $_.id -eq $project.Id } | Select-Object -First 1)
    if ($projectState.StatusCode -ne 200 -or $reloadedProject.Count -ne 1 -or
        $reloadedProject[0].startAt -ne $project.StartAt -or $reloadedProject[0].endAt -ne $project.EndAt) {
        throw 'The packaged JAR changed a persisted year-2040 project time after restart from Asia/Manila to UTC.'
    }

    $replay = New-C1AnonymousSession
    $replayPassword = 'Must-Not-Be-Accepted-' + (New-RandomHex -Bytes 24)
    $replayAttempt = Invoke-C1Post $replay '/api/v2/auth/recover' @{
        username = $adminUsername; oneTimeCode = $usedRecoveryCode
        newPassword = $replayPassword
    }
    if ($replayAttempt.StatusCode -ne 200) { throw 'Packaged JAR recovery replay did not preserve its generic privacy response.' }
    $wrongPassword = New-C1Client
    $wrongLoginToken = Get-C1Csrf $wrongPassword $persistedSession.Base
    $wrongSession = @{ Client = $wrongPassword; Token = $wrongLoginToken; Base = $persistedSession.Base }
    $wrongLogin = Invoke-C1Post $wrongSession '/api/v2/auth/login' @{
        username = $adminUsername; password = $replayPassword
    }
    if ($wrongLogin.StatusCode -ne 401) { throw 'The one-time recovery token appeared to change the password again after application restart.' }
    $verifiedLogin = New-C1Session $adminUsername $recoveredPassword
    if ((Invoke-C1Get $verifiedLogin '/api/v2/session').StatusCode -ne 200) {
        throw 'The recovered account password stopped working after restart and recovery-token replay.'
    }
    $replay.Client.Dispose()
    $adminSession.Client.Dispose()
    $wrongPassword.Dispose()
    $verifiedLogin.Client.Dispose()
    $persistedSession.Client.Dispose()
    Stop-C1App

    if ($SkipFrontend) {
        Write-Output 'C1 PASS: frontend gates were skipped by request; MySQL-backed ID01-ID17 acceptance; Maven package; packaged React/API smoke; invitation, ownership transfer and one-time recovery; session/data/recovery replay checks after JAR restart.'
    } else {
        Write-Output 'C1 PASS: frontend checks; MySQL-backed ID01-ID17 acceptance; Maven package; packaged React/API smoke; invitation, ownership transfer and one-time recovery; session/data/recovery replay checks after JAR restart.'
    }
    Write-Output "C1 run evidence and stopped isolated datadir: $dataDirectory"
    Write-Output "Packaged JAR: $(Join-Path $experiment 'target\extended-backend-0.1.0-SNAPSHOT.jar')"
} finally {
    Stop-C1App
    if ($dbProcess -and -not $dbProcess.HasExited) {
        & $mysqladmin --protocol=tcp --host=127.0.0.1 --port=$databasePort --user=root shutdown *> $null
        try { $null = $dbProcess.WaitForExit(15000) } catch { }
        if (-not $dbProcess.HasExited) { Stop-Process -Id $dbProcess.Id -Force }
    }
    foreach ($name in $environmentNames) {
        [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name], 'Process')
    }
}
