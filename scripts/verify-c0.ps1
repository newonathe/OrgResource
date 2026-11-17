$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$experiment = Join-Path $repoRoot 'experiments\extended-c-runtime-compat'
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
    'ORG_C0_DB_URL', 'ORG_C0_DB_USER', 'ORG_C0_DB_PASSWORD',
    'ORG_C0_EDITOR_PASSWORD', 'ORG_C0_VIEWER_PASSWORD',
    'ORG_C0_SMS_ENABLED', 'ORG_C0_SCHEDULER_DELAY_MS', 'C0_SERVER_PORT',
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

function Stop-C0App {
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

function Start-C0PackagedApp {
    $jar = Join-Path $experiment 'target\extended-c-runtime-compat-0.1.0-SNAPSHOT.jar'
    if (-not (Test-Path -LiteralPath $jar)) { throw "Packaged C0 JAR is missing: $jar" }
    $script:appLaunch++
    $script:appProcess = Start-Process -FilePath $java -ArgumentList @('-jar', ('"' + $jar + '"')) `
        -WorkingDirectory $experiment -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $dataDirectory "c0-app-$appLaunch.stdout.log") `
        -RedirectStandardError (Join-Path $dataDirectory "c0-app-$appLaunch.stderr.log")
    $base = "http://127.0.0.1:$appPort"
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        if ($script:appProcess.HasExited) { throw "Packaged JAR exited early; inspect the C0 app log in the temporary run directory." }
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
                if ([int]$api.StatusCode -ne 401 -or $apiBody -notmatch 'UNAUTHENTICATED') {
                    throw 'Packaged JAR API did not return its expected unauthenticated JSON response.'
                }
            } finally { $probe.Dispose() }
            return
        }
        Start-Sleep -Milliseconds 500
    }
    throw 'Packaged JAR did not serve localhost within 45 seconds.'
}

function New-C0EditorSession {
    $handler = [System.Net.Http.HttpClientHandler]::new()
    $handler.CookieContainer = [System.Net.CookieContainer]::new()
    $client = [System.Net.Http.HttpClient]::new($handler)
    $base = "http://127.0.0.1:$appPort"
    $loginPage = $client.GetAsync("$base/login").GetAwaiter().GetResult()
    $html = $loginPage.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    if (-not $loginPage.IsSuccessStatusCode) { throw 'Packaged JAR login form was not served.' }
    $match = [regex]::Match($html, 'name="_csrf"[^>]*value="([^"]+)"')
    if (-not $match.Success) { throw 'Packaged JAR login form omitted its CSRF token.' }
    $form = [System.Collections.Generic.Dictionary[string,string]]::new()
    $form.Add('username', 'c0-editor')
    $form.Add('password', $env:ORG_C0_EDITOR_PASSWORD)
    $form.Add('_csrf', $match.Groups[1].Value)
    $loginContent = [System.Net.Http.FormUrlEncodedContent]::new($form)
    $login = $client.PostAsync("$base/login", $loginContent).GetAwaiter().GetResult()
    $login.Dispose()
    $loginContent.Dispose()
    $csrfResponse = $client.GetAsync("$base/api/v2/csrf").GetAwaiter().GetResult()
    $csrfJson = $csrfResponse.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json
    if (-not $csrfResponse.IsSuccessStatusCode -or [string]::IsNullOrWhiteSpace($csrfJson.token)) {
        throw 'Packaged JAR authenticated CSRF endpoint did not return a token.'
    }
    return @{ Client = $client; Token = $csrfJson.token; Base = $base }
}

function Add-PersistedDemoDepartment($Session, [string] $Name) {
    $content = [System.Net.Http.StringContent]::new((@{ name = $Name } | ConvertTo-Json -Compress),
        [System.Text.Encoding]::UTF8, 'application/json')
    $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post,
        "$($Session.Base)/api/departments")
    $request.Headers.Add('X-CSRF-TOKEN', $Session.Token)
    $request.Content = $content
    $response = $Session.Client.SendAsync($request).GetAwaiter().GetResult()
    $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    $content.Dispose()
    $request.Dispose()
    if ([int]$response.StatusCode -ne 201) { throw "Packaged JAR department POST returned $([int]$response.StatusCode)." }
    return ($body | ConvertFrom-Json).id
}

try {
    if (-not (Test-Path -LiteralPath $mvnw)) { throw 'Maven Wrapper is missing.' }
    foreach ($file in @($mysqld, $mysql, $mysqladmin)) {
        if (-not (Test-Path -LiteralPath $file)) { throw "MySQL 8 executable is missing: $file" }
    }
    $version = & $java -version 2>&1 | Out-String
    if ($version -notmatch '21\.') { throw 'C0 requires the approved Java 21 runtime.' }
    if (-not (Test-Path -LiteralPath (Join-Path $frontend 'package-lock.json'))) { throw 'Frontend lockfile is missing.' }

    foreach ($name in $environmentNames) {
        $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
    }

    $databasePort = Get-FreeLoopbackPort
    $appPort = Get-FreeLoopbackPort
    while ($appPort -eq $databasePort) { $appPort = Get-FreeLoopbackPort }
    $dataDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('OrgResource-C0-' + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Path $dataDirectory | Out-Null

    $initialize = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $dataDirectory + '"'), '--initialize-insecure', '--console') -Wait -PassThru -NoNewWindow
    if ($initialize.ExitCode -ne 0) { throw 'Fresh isolated MySQL datadir initialization failed.' }

    $dbProcess = Start-Process -FilePath $mysqld -ArgumentList @('--no-defaults', ('--basedir="' + $mysqlHome + '"'),
        ('--datadir="' + $dataDirectory + '"'), "--port=$databasePort", '--bind-address=127.0.0.1', '--mysqlx=0',
        ('--pid-file="' + (Join-Path $dataDirectory 'c0.pid') + '"'),
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
    $editorPassword = New-RandomHex
    $viewerPassword = New-RandomHex
    $sql = @"
CREATE DATABASE orgresource_c0_acceptance CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'c0_app'@'127.0.0.1' IDENTIFIED BY '$dbPassword';
GRANT ALL PRIVILEGES ON orgresource_c0_acceptance.* TO 'c0_app'@'127.0.0.1';
FLUSH PRIVILEGES;
"@
    Invoke-MysqlBootstrap $sql

    $env:ORG_C0_DB_URL = "jdbc:mysql://127.0.0.1:$databasePort/orgresource_c0_acceptance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila"
    $env:ORG_C0_DB_USER = 'c0_app'
    $env:ORG_C0_DB_PASSWORD = $dbPassword
    $env:ORG_C0_EDITOR_PASSWORD = $editorPassword
    $env:ORG_C0_VIEWER_PASSWORD = $viewerPassword
    $env:ORG_C0_SMS_ENABLED = 'false'
    $env:ORG_C0_SCHEDULER_DELAY_MS = '100'
    $env:C0_SERVER_PORT = [string]$appPort
    $env:SPRING_PROFILES_ACTIVE = 'c0-test'

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

    Push-Location $repoRoot
    try {
        & $mvnw -B -f $pom clean verify
        Assert-ExitCode 'C0 Maven clean verify'
        $dependencyFile = Join-Path $experiment 'target\c0-effective-dependencies.txt'
        & $mvnw -B -f $pom dependency:tree -Dverbose `
            '-Dincludes=org.springframework.boot:*,org.springframework:*,org.springframework.security:*,org.glassfish.jersey:*,org.hibernate.orm:*,org.hibernate.validator:*,jakarta.validation:*,javax.validation:*,org.apache.bval:*,com.mysql:*,org.flywaydb:*,com.squareup.retrofit2:*,com.google.code.gson:*,com.squareup.okhttp3:*' `
            *> $dependencyFile
        Assert-ExitCode 'C0 effective dependency tree'
    } finally { Pop-Location }

    Start-C0PackagedApp
    $name = 'C0 packaged persistence ' + [guid]::NewGuid().ToString('N')
    $first = New-C0EditorSession
    $departmentId = Add-PersistedDemoDepartment $first $name
    $first.Client.Dispose()
    Stop-C0App

    Start-C0PackagedApp
    $second = New-C0EditorSession
    $response = $second.Client.GetAsync("$($second.Base)/api/departments").GetAwaiter().GetResult()
    $departments = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json
    if (-not $response.IsSuccessStatusCode -or -not ($departments | Where-Object { $_.id -eq $departmentId -and $_.name -eq $name })) {
        throw 'Packaged JAR failed to read the Department persisted before its restart.'
    }
    $second.Client.Dispose()
    Stop-C0App

    Write-Output 'C0 PASS: frontend checks; MySQL-backed UC01-UC11 acceptance; Maven executable package; packaged React/API smoke; authenticated create and persistence after JAR restart.'
    Write-Output "C0 run evidence and stopped isolated datadir: $dataDirectory"
    Write-Output "Packaged JAR: $(Join-Path $experiment 'target\extended-c-runtime-compat-0.1.0-SNAPSHOT.jar')"
} finally {
    Stop-C0App
    if ($dbProcess -and -not $dbProcess.HasExited) {
        & $mysqladmin --protocol=tcp --host=127.0.0.1 --port=$databasePort --user=root shutdown *> $null
        try { $null = $dbProcess.WaitForExit(15000) } catch { }
        if (-not $dbProcess.HasExited) { Stop-Process -Id $dbProcess.Id -Force }
    }
    foreach ($name in $environmentNames) {
        [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name], 'Process')
    }
}
