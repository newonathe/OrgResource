param([switch] $SkipFrontend, [switch] $VerifyRecovery)

if ($SkipFrontend -and $VerifyRecovery) { throw 'Recovery verification requires the complete connected browser gate.' }

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$experiment = Join-Path $repoRoot 'extended\backend'
$frontend = Join-Path $repoRoot 'extended\frontend'
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
$sourceDatabasePort = 0
$appPort = 0
$appLaunch = 0
$dataDirectory = $null
$savedEnvironment = @{}
$environmentNames = @(
    'EXT_DB_URL', 'EXT_DB_USER', 'EXT_DB_PASSWORD',
    'EXT_DB_MIGRATION_URL', 'EXT_DB_MIGRATION_USER', 'EXT_DB_MIGRATION_PASSWORD',
    'EXT_BOOTSTRAP_SLUG', 'EXT_BOOTSTRAP_ORG_NAME', 'EXT_BOOTSTRAP_USERNAME',
    'EXT_BOOTSTRAP_DISPLAY_NAME', 'EXT_BOOTSTRAP_PASSWORD',
    'EXT_CONTACT_ENCRYPTION_KEY',
    'EXT_SMS_ENABLED', 'EXT_REMINDER_MODE', 'EXT_SCHEDULER_DELAY_MS', 'EXT_SERVER_PORT',
    'SPRING_PROFILES_ACTIVE', 'MYSQL_TEST_LOGIN_FILE', 'MYSQL_PWD'
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

# MySQL 8.0 reads .mylogin.cnf even with --no-defaults. Redirect to a
# deliberately absent file inside the newly created disposable datadir.
# Do not read, copy or modify the operator's actual login configuration.
function Initialize-C1MysqlClientIsolation([string] $Directory) {
    if (-not [IO.Path]::IsPathFullyQualified($Directory) -or
        -not (Test-Path -LiteralPath $Directory -PathType Container)) { throw 'C1_MYSQL_ISOLATION_DIRECTORY' }
    $item = Get-Item -LiteralPath $Directory -Force
    if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'C1_MYSQL_ISOLATION_DIRECTORY' }
    $login = Join-Path $item.FullName '.orgresource-no-login.cnf'
    if (Test-Path -LiteralPath $login) { throw 'C1_MYSQL_ISOLATION_LOGIN_EXISTS' }
    $env:MYSQL_TEST_LOGIN_FILE = $login
    $env:MYSQL_PWD = $null
}

# Snapshot the Windows listener table without connecting to an unknown endpoint.
# This is a point-in-time check, not a reservation or a race-free guarantee.
function Test-C1OwnedListener([System.Diagnostics.Process] $Process, [int] $Port) {
    if (-not $Process -or $Port -lt 1 -or $Port -gt 65535) { throw 'C1_LISTENER_INVALID' }
    $Process.Refresh()
    if ($Process.HasExited) { throw 'C1_LISTENER_PROCESS_EXITED' }
    $listeners = @(Get-NetTCPConnection -State Listen -ErrorAction Stop | Where-Object { $_.LocalPort -eq $Port })
    if ($listeners.Count -eq 0) { return $false }
    if ($listeners.Count -ne 1 -or $listeners[0].LocalAddress -cne '127.0.0.1' -or
        $listeners[0].OwningProcess -ne $Process.Id) { throw 'C1_LISTENER_NOT_OWNED' }
    $Process.Refresh()
    if ($Process.HasExited) { throw 'C1_LISTENER_PROCESS_EXITED' }
    return $true
}

function Assert-C1OwnedListener([System.Diagnostics.Process] $Process, [int] $Port) {
    if (-not (Test-C1OwnedListener -Process $Process -Port $Port)) { throw 'C1_LISTENER_MISSING' }
}

function Assert-C1DatabasePort([int] $Port) {
    if ($Port -eq $sourceDatabasePort) {
        Assert-C1OwnedListener -Process $dbProcess -Port $Port
    } elseif ($Port -eq $recoveryPort -and $target) {
        Assert-C1OwnedListener -Process $target -Port $Port
    } else { throw 'C1_DATABASE_PORT_UNKNOWN' }
}

function Wait-C1DatabaseReady([System.Diagnostics.Process] $Process, [int] $Port) {
    $clock = [Diagnostics.Stopwatch]::StartNew()
    while ($clock.Elapsed.TotalSeconds -lt 45) {
        if (Test-C1OwnedListener -Process $Process -Port $Port) {
            # mysqladmin ping can return zero on Access denied. Require a real query.
            $answer = @(& $mysql --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$Port" --user=root --skip-password --connect-timeout=2 --batch --raw --skip-column-names '--execute=SELECT 1' 2>$null)
            if ($LASTEXITCODE -eq 0 -and $answer.Count -eq 1 -and $answer[0] -ceq '1') {
                Assert-C1OwnedListener -Process $Process -Port $Port
                return
            }
        }
        Start-Sleep -Milliseconds 500
    }
    throw 'C1_AUTHENTICATED_READINESS_TIMEOUT'
}

# The caller supplies the port bound to this specific process, never the
# mutable active database port used during recovery.
function Stop-C1Database([System.Diagnostics.Process] $Process, [int] $Port) {
    if (-not $Process -or $Process.HasExited) { return }
    if ($Port -lt 1 -or $Port -gt 65535) { throw 'C1_DB_CLEANUP_PORT_INVALID' }
    Assert-C1OwnedListener -Process $Process -Port $Port
    & $mysqladmin --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$Port" --user=root --skip-password shutdown *> $null
    if (-not $Process.WaitForExit(15000)) {
        $Process.Kill()
        if (-not $Process.WaitForExit(15000)) { throw 'C1_DB_CLEANUP_NOT_STOPPED' }
    }
}

function Invoke-MysqlBootstrap([string] $Sql) {
    Assert-C1DatabasePort -Port $databasePort
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $mysql
    $startInfo.Arguments = "--no-defaults --protocol=tcp --host=127.0.0.1 --port=$databasePort --user=root --skip-password --batch --raw"
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
    Assert-C1DatabasePort -Port $databasePort
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

function Invoke-C1Post($Session, [string] $Path, [object] $Body, [string] $IfMatch = $null,
        [string] $IdempotencyKey = $null) {
    $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post, "$($Session.Base)$Path")
    $request.Headers.Add('Origin', $Session.Base)
    if (-not [string]::IsNullOrWhiteSpace([string]$Session.Token)) { $request.Headers.Add('X-CSRF-TOKEN', [string]$Session.Token) }
    if (-not [string]::IsNullOrWhiteSpace($IfMatch)) { $request.Headers.Add('If-Match', $IfMatch) }
    if (-not [string]::IsNullOrWhiteSpace($IdempotencyKey)) { $request.Headers.Add('Idempotency-Key', $IdempotencyKey) }
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
    $etag = if ($response.Headers.ETag) { [string]$response.Headers.ETag } else { $null }
    $statusCode = [int]$response.StatusCode
    $response.Dispose()
    return @{ StatusCode = $statusCode; Data = $data; ETag = $etag; Raw = $raw }
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
    if ($null -ne $login.Data.session.activeOrgId) { throw 'Login unexpectedly selected an organization.' }
    $availableOrganizations = Invoke-C1Get $session '/api/v2/orgs'
    if ($availableOrganizations.StatusCode -ne 200 -or @($availableOrganizations.Data).Count -lt 1) {
        throw 'Authenticated account has no selectable organization.'
    }
    $session.OrgId = [string]$availableOrganizations.Data[0].id
    $selected = Invoke-C1Post $session '/api/v2/session/active-org' @{ organizationId = $session.OrgId }
    if ($selected.StatusCode -ne 200 -or [string]$selected.Data.activeOrgId -ne $session.OrgId) {
        throw 'Explicit organization selection failed.'
    }
    $session.Token = Get-C1Csrf $client $base
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

foreach ($name in $environmentNames) {
    $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}
try {
    if (-not (Test-Path -LiteralPath $mvnw)) { throw 'Maven Wrapper is missing.' }
    foreach ($file in @($mysqld, $mysql, $mysqladmin)) {
        if (-not (Test-Path -LiteralPath $file)) { throw "MySQL 8 executable is missing: $file" }
    }
    $version = & $java -version 2>&1 | Out-String
    if ($version -notmatch '21\.') { throw 'C1 requires the approved Java 21 runtime.' }
    if (-not (Test-Path -LiteralPath (Join-Path $frontend 'package-lock.json'))) { throw 'Frontend lockfile is missing.' }

    $sourceDatabasePort = Get-FreeLoopbackPort
    $databasePort = $sourceDatabasePort
    $appPort = Get-FreeLoopbackPort
    while ($appPort -eq $databasePort) { $appPort = Get-FreeLoopbackPort }
    $dataDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('OrgResource-C1-' + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Path $dataDirectory | Out-Null
    Initialize-C1MysqlClientIsolation $dataDirectory

    $initialize = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $dataDirectory + '"'), '--initialize-insecure', '--console') -Wait -PassThru -NoNewWindow
    if ($initialize.ExitCode -ne 0) { throw 'Fresh isolated MySQL datadir initialization failed.' }

    $dbProcess = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $dataDirectory + '"'), "--port=$databasePort", '--bind-address=127.0.0.1', '--mysqlx=0',
        ('--pid-file="' + (Join-Path $dataDirectory 'c1.pid') + '"'),
        ('--log-error="' + (Join-Path $dataDirectory 'mysql.err.log') + '"')) -WindowStyle Hidden -PassThru
    Wait-C1DatabaseReady -Process $dbProcess -Port $sourceDatabasePort

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
    $env:EXT_DB_MIGRATION_URL = $env:EXT_DB_URL
    # Root is confined to this disposable, freshly initialized local MySQL instance.
    # It is used only by Flyway and the acceptance test that narrows the runtime role.
    $env:EXT_DB_MIGRATION_USER = 'root'
    $env:EXT_DB_MIGRATION_PASSWORD = ''
    $env:EXT_BOOTSTRAP_SLUG = 'c1-seed-workspace'
    $env:EXT_BOOTSTRAP_ORG_NAME = 'Fictional C1 Workspace'
    $env:EXT_BOOTSTRAP_USERNAME = 'c1-owner'
    $env:EXT_BOOTSTRAP_DISPLAY_NAME = 'C1 Owner'
    $env:EXT_BOOTSTRAP_PASSWORD = $bootstrapPassword
    $contactKeyBytes = New-Object byte[] 32
    $contactKeyGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $contactKeyGenerator.GetBytes($contactKeyBytes)
        $env:EXT_CONTACT_ENCRYPTION_KEY = [Convert]::ToBase64String($contactKeyBytes)
    } finally {
        $contactKeyGenerator.Dispose()
        [Array]::Clear($contactKeyBytes, 0, $contactKeyBytes.Length)
    }
    $env:EXT_SMS_ENABLED = 'false'
    $env:EXT_REMINDER_MODE = 'DISABLED'
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
    & (Join-Path $repoRoot 'scripts\run-extended-c3-newman.ps1') `
        -BaseUrl "http://127.0.0.1:$appPort" `
        -Username $env:EXT_BOOTSTRAP_USERNAME `
        -Password $env:EXT_BOOTSTRAP_PASSWORD
    & (Join-Path $repoRoot 'scripts\run-extended-c3-newman.ps1') -Reporting `
        -BaseUrl "http://127.0.0.1:$appPort" -Username $env:EXT_BOOTSTRAP_USERNAME -Password $env:EXT_BOOTSTRAP_PASSWORD
    $ownerSession = New-C1OwnerSession
    $orgId = $ownerSession.OrgId
    if ([string]::IsNullOrWhiteSpace($orgId)) { throw 'Bootstrap OWNER session did not select its organization.' }

    # C4-A2: prove current-role authorization for a durable movement receipt across prune and JVM restart.
    $receiptInvitation = Invoke-C1Post $ownerSession "/api/v2/orgs/$orgId/invitations" @{ role = 'CUSTODIAN' }
    if ($receiptInvitation.StatusCode -ne 201) { throw 'C4 receipt test could not create a fictional CUSTODIAN invitation.' }
    $receiptUsername = 'c4-receipt-' + (New-RandomHex -Bytes 6)
    $receiptPassword = 'Custodian-C4-' + (New-RandomHex -Bytes 24)
    $receiptDisplayName = 'C4 Receipt Custodian'
    $receiptAnonymous = New-C1AnonymousSession
    $receiptRedemption = Invoke-C1Post $receiptAnonymous '/api/v2/invitations/redeem' @{
        bearerToken = [string]$receiptInvitation.Data.bearerToken; username = $receiptUsername
        password = $receiptPassword; displayName = $receiptDisplayName
    }
    if ($receiptRedemption.StatusCode -ne 201) { throw 'C4 receipt test could not redeem the fictional CUSTODIAN invitation.' }
    $c4CustodianSession = New-C1Session $receiptUsername $receiptPassword
    $receiptResourceName = 'C4 receipt resource ' + [guid]::NewGuid().ToString('N')
    $receiptResource = Invoke-C1Post $ownerSession '/api/resources' @{
        name = $receiptResourceName; type = 'CONSUMABLE'; totalQuantity = 0
    }
    if ($receiptResource.StatusCode -ne 201) { throw 'C4 receipt test could not create its fictional zero-stock Resource.' }
    $receiptResourceId = [long](($receiptResource.Raw | ConvertFrom-Json).id)
    $receiptResources = Invoke-C1Get $c4CustodianSession "/api/v2/orgs/$orgId/resources?limit=100"
    $receiptResourceRow = @($receiptResources.Data.items | Where-Object { $_.name -eq $receiptResourceName } | Select-Object -First 1)
    if ($receiptResources.StatusCode -ne 200 -or $receiptResourceRow.Count -ne 1) {
        throw 'C4 CUSTODIAN could not read the Resource public ID for its authorized stock operation.'
    }
    $receiptResourcePath = "/api/v2/orgs/$orgId/resources/$($receiptResourceRow[0].id)"
    $receiptResourceState = Invoke-C1Get $c4CustodianSession $receiptResourcePath
    $c4OperationId = [guid]::NewGuid().ToString()
    $c4OperationBody = @{ operationId = $c4OperationId; quantity = 1 }
    $c4OperationKey = New-RandomHex
    $c4Committed = Invoke-C1Post $c4CustodianSession "$receiptResourcePath/receive" $c4OperationBody `
        $receiptResourceState.ETag $c4OperationKey
    if ($c4Committed.StatusCode -ne 201) { throw 'C4 CUSTODIAN stock receipt did not commit before role demotion.' }
    $c4OperationPath = "/api/v2/orgs/$orgId/operations/$c4OperationId"
    $receiptMembers = Invoke-C1Get $ownerSession "/api/v2/orgs/$orgId/members"
    $receiptMember = @($receiptMembers.Data.items | Where-Object { $_.displayName -eq $receiptDisplayName -and $_.role -eq 'CUSTODIAN' } | Select-Object -First 1)
    if ($receiptMembers.StatusCode -ne 200 -or $receiptMember.Count -ne 1) { throw 'C4 receipt test could not resolve the CUSTODIAN membership.' }
    $demoted = Invoke-C1Post $ownerSession "/api/v2/orgs/$orgId/members/$($receiptMember[0].id)/role" `
        @{ role = 'MEMBER' } ('"v' + [long]$receiptMember[0].version + '"')
    if ($demoted.StatusCode -ne 200) { throw 'Owner could not demote the fictional CUSTODIAN through the governance API.' }
    if ((Invoke-C1Get $c4CustodianSession $c4OperationPath).StatusCode -ne 403) {
        throw 'A demoted original actor retained access to a permanent movement receipt.'
    }
    $expireReceipt = @"
USE orgresource_c1_acceptance;
UPDATE idempotency_key SET created_at=UTC_TIMESTAMP(6)-INTERVAL 25 HOUR, expires_at=UTC_TIMESTAMP(6)-INTERVAL 1 HOUR
WHERE operation_public_id=UUID_TO_BIN('$c4OperationId',0);
"@
    Invoke-MysqlBootstrap $expireReceipt
    if ((Invoke-C1Get $c4CustodianSession $c4OperationPath).StatusCode -ne 403) {
        throw 'A demoted original actor recovered a movement receipt after the HTTP receipt expired.'
    }
    $pruneReceipt = @"
USE orgresource_c1_acceptance;
DELETE FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN('$c4OperationId',0) AND expires_at<=UTC_TIMESTAMP(6);
"@
    Invoke-MysqlBootstrap $pruneReceipt
    if ((Invoke-C1Get $c4CustodianSession $c4OperationPath).StatusCode -ne 403) {
        throw 'A demoted original actor recovered a movement receipt after the HTTP receipt was pruned.'
    }
    $c4MovementReceipt = Invoke-C1Get $ownerSession $c4OperationPath
    if ($c4MovementReceipt.StatusCode -ne 200 -or $c4MovementReceipt.Data.kind -ne 'RECEIPT') {
        throw 'An active OWNER could not recover permanent movement history after HTTP receipt pruning.'
    }
    $c4Movements = Invoke-C1Get $ownerSession "$receiptResourcePath/movements?limit=10"
    if ($c4Movements.StatusCode -ne 200 -or @($c4Movements.Data.items).Count -ne 1) {
        throw 'Pruning the HTTP receipt removed or duplicated permanent stock movement history.'
    }
    $receiptAnonymous.Client.Dispose()

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
    if ($null -ne $redeemed.Data.session.activeOrgId) { throw 'New-account establishment unexpectedly selected an organization.' }
    $missingContext = Invoke-C1Get @{ Client = $anonymous.Client; Base = $anonymous.Base } '/api/departments'
    if ($missingContext.StatusCode -ne 409 -or $missingContext.Raw -notmatch 'ORG_CONTEXT_REQUIRED') {
        throw 'Legacy route did not require explicit organization selection after invitation redemption.'
    }
    $adminSession = @{ Client = $anonymous.Client; Token = [string]$redeemed.Data.csrfToken; Base = $anonymous.Base }
    $selected = Invoke-C1Post $adminSession '/api/v2/session/active-org' @{ organizationId = $orgId }
    if ($selected.StatusCode -ne 200) { throw 'Invited account could not explicitly select its organization.' }
    $adminSession.Token = Get-C1Csrf $anonymous.Client $anonymous.Base
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
    $resourceName = 'C2 restart-safe receipt ' + [guid]::NewGuid().ToString('N')
    $resourceCreated = Invoke-C1Post $persistedSession '/api/resources' @{
        name = $resourceName; type = 'CONSUMABLE'; totalQuantity = 7
    }
    if ($resourceCreated.StatusCode -ne 201) { throw 'Packaged JAR failed to create the fictional C2 persistence Resource.' }
    $orgRoot = "/api/v2/orgs/$($persistedSession.OrgId)"
    $resourceList = Invoke-C1Get $persistedSession "$orgRoot/resources?limit=100"
    $resource = @($resourceList.Data.items | Where-Object { $_.name -eq $resourceName } | Select-Object -First 1)
    if ($resourceList.StatusCode -ne 200 -or $resource.Count -ne 1) { throw 'Versioned Resource discovery did not expose its public UUID.' }
    $resourcePath = "$orgRoot/resources/$($resource[0].id)"
    $resourceState = Invoke-C1Get $persistedSession $resourcePath
    if ($resourceState.StatusCode -ne 200 -or $resourceState.Data.totalQuantity -ne 7) {
        throw 'Packaged JAR could not read the initial C2 Resource state.'
    }

    $imageResourceName = 'C3 restart image ' + [guid]::NewGuid().ToString('N')
    $imageResource = Invoke-C1Post $persistedSession '/api/resources' @{
        name = $imageResourceName; type = 'CONSUMABLE'; totalQuantity = 0
    }
    if ($imageResource.StatusCode -ne 201) { throw 'Packaged JAR failed to create the fictional restart-image Resource.' }
    $imageResourceId = [long](($imageResource.Raw | ConvertFrom-Json).id)
    $imageBytes = [System.IO.File]::ReadAllBytes((Join-Path $repoRoot 'postman\fixtures\academic-demo-image.png'))
    $imageForm = [System.Net.Http.MultipartFormDataContent]::new()
    $imagePart = [System.Net.Http.ByteArrayContent]::new($imageBytes)
    $imagePart.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse('image/png')
    $imageForm.Add($imagePart, 'file', 'fictional-restart-image.png')
    $imageRequest = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post,
        "$($persistedSession.Base)/api/resources/$imageResourceId/image")
    $imageRequest.Headers.Add('Origin', $persistedSession.Base)
    $imageRequest.Headers.Add('X-CSRF-TOKEN', $persistedSession.Token)
    $imageRequest.Content = $imageForm
    $imageUploadResponse = $persistedSession.Client.SendAsync($imageRequest).GetAwaiter().GetResult()
    $imageUploadBody = $imageUploadResponse.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    if ([int]$imageUploadResponse.StatusCode -ne 200) {
        throw "Packaged JAR failed to store the bounded fictional image (HTTP $([int]$imageUploadResponse.StatusCode))."
    }
    $imageUpload = $imageUploadBody | ConvertFrom-Json
    $expectedImageSha256 = [System.Convert]::ToHexString([System.Security.Cryptography.SHA256]::HashData($imageBytes)).ToLowerInvariant()
    if ($imageUpload.resourceId -ne $imageResourceId -or
            $imageUpload.contentType -ne 'image/png' -or
            $imageUpload.sizeBytes -ne $imageBytes.Length -or
            $imageUpload.sha256 -ne $expectedImageSha256) {
        throw 'Packaged JAR image upload metadata did not match the exact fictional image bytes.'
    }
    $imageUploadResponse.Dispose()
    $imageRequest.Dispose()
    $imageForm.Dispose()
    $imagePart.Dispose()

    $operationId = [guid]::NewGuid().ToString()
    $operationKey = New-RandomHex
    $operationBody = @{ operationId = $operationId; quantity = 5 }
    $receipt = Invoke-C1Post $persistedSession "$resourcePath/receive" $operationBody $resourceState.ETag $operationKey
    if ($receipt.StatusCode -ne 201 -or $receipt.Data.totalQuantity -ne 12 -or $receipt.Data.availableQuantity -ne 12) {
        throw 'Packaged C2 receive operation did not commit its first movement and receipt.'
    }
    $preRestartReplay = Invoke-C1Post $persistedSession "$resourcePath/receive" $operationBody '"v0"' $operationKey
    if ($preRestartReplay.StatusCode -ne 201 -or $preRestartReplay.Data.totalQuantity -ne 12) {
        throw 'Same-key/same-body replay did not return the committed receipt with its original stale ETag.'
    }
    $movementPage = Invoke-C1Get $persistedSession "$resourcePath/movements?limit=10"
    if ($movementPage.StatusCode -ne 200 -or @($movementPage.Data.items).Count -ne 2) {
        throw 'INITIAL and RECEIPT history did not produce exactly two bounded movement rows.'
    }
    $ownerSession.Client.Dispose()
    $recovery.Client.Dispose()
    Stop-C1App

    Start-C1PackagedApp 'UTC'
    $revokedAfterRestart = Invoke-C1Get $adminSession '/api/v2/session'
    if ($revokedAfterRestart.StatusCode -ne 401) { throw 'The recovery-revoked session became valid after the packaged JAR restart.' }
    $persistedState = Invoke-C1Get $persistedSession '/api/v2/session'
    if ($persistedState.StatusCode -ne 200) { throw 'Spring Session JDBC did not preserve the authenticated session across the packaged JAR restart.' }
    if ([string]$persistedState.Data.activeOrgId -ne [string]$persistedSession.OrgId) {
        throw 'Spring Session JDBC did not preserve the explicitly selected organization across the packaged JAR restart.'
    }
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
    $resourceStateAfterRestart = Invoke-C1Get $persistedSession $resourcePath
    if ($resourceStateAfterRestart.StatusCode -ne 200 -or
        $resourceStateAfterRestart.Data.totalQuantity -ne 12 -or
        $resourceStateAfterRestart.Data.availableQuantity -ne 12) {
        throw 'Packaged C2 physical quantity did not persist across restart.'
    }
    $imageAfterRestartResponse = $persistedSession.Client.GetAsync(
        "$($persistedSession.Base)/api/resources/$imageResourceId/image").GetAwaiter().GetResult()
    $imageAfterRestart = $imageAfterRestartResponse.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    if ([int]$imageAfterRestartResponse.StatusCode -ne 200 -or
            $imageAfterRestartResponse.Content.Headers.ContentType.MediaType -ne 'image/png' -or
            $imageAfterRestart.Length -ne $imageBytes.Length) {
        throw 'Packaged JAR did not restore the stored image after restart.'
    }
    for ($imageIndex = 0; $imageIndex -lt $imageBytes.Length; $imageIndex++) {
        if ($imageAfterRestart[$imageIndex] -ne $imageBytes[$imageIndex]) {
            throw "The image served after restart differs at byte $imageIndex."
        }
    }
    $imageAfterRestartResponse.Dispose()
    $receiptAfterRestart = Invoke-C1Get $persistedSession "$orgRoot/operations/$operationId"
    if ($receiptAfterRestart.StatusCode -ne 200 -or $receiptAfterRestart.Data.kind -ne 'RECEIPT' -or
        $receiptAfterRestart.Data.quantity -ne 5 -or $receiptAfterRestart.Data.deltaTotal -ne 5 -or
        $receiptAfterRestart.Data.resourceId -ne [string]$resource[0].id) {
        throw 'Packaged C2 operation receipt was not recoverable after restart.'
    }
    if ((Invoke-C1Get $c4CustodianSession '/api/v2/session').StatusCode -ne 200 -or
            (Invoke-C1Get $c4CustodianSession $c4OperationPath).StatusCode -ne 403) {
        throw 'The packaged JVM restart changed the demoted CUSTODIAN role or exposed its permanent receipt.'
    }
    $authorizedAfterRestart = Invoke-C1Get $persistedSession $c4OperationPath
    if ($authorizedAfterRestart.StatusCode -ne 200 -or $authorizedAfterRestart.Data.kind -ne 'RECEIPT') {
        throw 'The active OWNER could not recover the permanent receipt after HTTP prune and JVM restart.'
    }
    $receiptMovementsAfterRestart = Invoke-C1Get $persistedSession "$receiptResourcePath/movements?limit=10"
    if ($receiptMovementsAfterRestart.StatusCode -ne 200 -or @($receiptMovementsAfterRestart.Data.items).Count -ne 1) {
        throw 'The permanent stock movement ledger did not survive receipt pruning and JVM restart.'
    }
    $restartReplay = Invoke-C1Post $persistedSession "$resourcePath/receive" $operationBody '"v0"' $operationKey
    if ($restartReplay.StatusCode -ne 201 -or $restartReplay.Data.totalQuantity -ne 12) {
        throw 'Packaged C2 idempotent replay after restart changed the committed receipt.'
    }
    $movementAfterRestart = Invoke-C1Get $persistedSession "$resourcePath/movements?limit=10"
    if ($movementAfterRestart.StatusCode -ne 200 -or @($movementAfterRestart.Data.items).Count -ne 2) {
        throw 'C2 HTTP replay after restart duplicated an immutable stock movement.'
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
    $c4CustodianSession.Client.Dispose()
    $wrongPassword.Dispose()
    $verifiedLogin.Client.Dispose()
    $persistedSession.Client.Dispose()

    if (-not $SkipFrontend) {
        # Seed a second tenant only in this fresh disposable MySQL database so the packaged
        # browser acceptance exercises real tenant switching and isolation.
        $secondTenantSql = @"
USE orgresource_c1_acceptance;
INSERT INTO organization(public_id,slug,display_name,status,created_at,updated_at)
VALUES(UUID_TO_BIN(UUID(),0),'c4-e2e-secondary','C4 Tenant Isolation','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
SET @c4_e2e_org_id=LAST_INSERT_ID();
INSERT INTO membership(public_id,org_id,user_id,role,status,created_at,updated_at)
SELECT UUID_TO_BIN(UUID(),0),@c4_e2e_org_id,id,'OWNER','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)
FROM user_account WHERE username_key='$adminUsername';
"@
        Invoke-MysqlBootstrap $secondTenantSql
        $tenantFixtureSession = New-C1Session $adminUsername $recoveredPassword
        $e2eOrganizations = Invoke-C1Get $tenantFixtureSession '/api/v2/orgs'
        $tenantFixtureSession.Client.Dispose()
        if ($e2eOrganizations.StatusCode -ne 200 -or
                @($e2eOrganizations.Data | Where-Object { $_.displayName -eq 'C4 Tenant Isolation' }).Count -ne 1) {
            throw 'The isolated MySQL browser fixture did not give the acceptance account access to a second organization.'
        }
        $e2eEnvironmentNames = @('EXT_E2E_BASE_URL', 'EXT_E2E_USERNAME', 'EXT_E2E_PASSWORD', 'ORGRESOURCE_E2E_SCREENSHOT_DIR', 'EXT_VITE_BACKEND_PORT')
        $e2eSavedEnvironment = @{}
        foreach ($name in $e2eEnvironmentNames) {
            $e2eSavedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
        }
        try {
            $env:EXT_E2E_BASE_URL = "http://127.0.0.1:$appPort"
            $env:EXT_E2E_USERNAME = $adminUsername
            $env:EXT_E2E_PASSWORD = $recoveredPassword
            $env:ORGRESOURCE_E2E_SCREENSHOT_DIR = Join-Path $dataDirectory 'screenshots'
            Push-Location $frontend
            try {
                & npm run e2e
                Assert-ExitCode 'packaged Java/MySQL Playwright acceptance'
                Remove-Item Env:EXT_E2E_BASE_URL
                $env:EXT_VITE_BACKEND_PORT = [string] $appPort
                & npm run e2e -- --grep 'Allocation status filters'
                Assert-ExitCode 'standalone Vite with Java/MySQL Playwright acceptance'
            } finally { Pop-Location }
        } finally {
            foreach ($name in $e2eEnvironmentNames) {
                [Environment]::SetEnvironmentVariable($name, $e2eSavedEnvironment[$name], 'Process')
            }
        }
    }
    if ($VerifyRecovery) {
        . (Join-Path $PSScriptRoot 'verify-extended-c-recovery.ps1')
    }
    Stop-C1App

    if ($SkipFrontend) {
        Write-Output 'Extended C PASS: frontend gates skipped by request. See Surefire for the observed MySQL test count. Maven packaging, academic Newman, inventory and governance, durable replay, and image checks passed.'
    } else {
        Write-Output 'Extended C PASS: React, packaged and Vite Chromium, Maven, academic Newman, inventory and governance, durable replay, and image checks passed. See Surefire for the observed MySQL test count.'
    }
    Write-Output "Extended C run evidence and stopped isolated datadir: $dataDirectory"
    Write-Output "Packaged JAR: $(Join-Path $experiment 'target\extended-backend-0.1.0-SNAPSHOT.jar')"
} finally {
    try {
        try {
            Stop-C1App
        } finally {
            Stop-C1Database -Process $dbProcess -Port $sourceDatabasePort
        }
    } finally {
        foreach ($name in $environmentNames) {
            [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name], 'Process')
        }
    }
}
