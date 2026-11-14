[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$credentialRoot = Join-Path $env:LOCALAPPDATA 'OrgResourceDev'
$credentialHelper = Join-Path $credentialRoot 'Set-OrgResourceDbEnvironment.ps1'
$credentialFile = Join-Path $credentialRoot 'orgresource-app.credential.xml'
$sqlFile = Join-Path $PSScriptRoot 'skeleton-demo.sql'
$mysqlExe = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'

$environmentNames = @('ORG_DB_URL', 'ORG_DB_USER', 'ORG_DB_PASSWORD', 'MYSQL_PWD')
$savedEnvironment = @{}
foreach ($name in $environmentNames) {
    $value = [Environment]::GetEnvironmentVariable($name, 'Process')
    $savedEnvironment[$name] = @{ WasSet = ($null -ne $value); Value = $value }
}

try {
    foreach ($path in @($credentialHelper, $credentialFile, $sqlFile, $mysqlExe)) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            throw "Required local demo file or MySQL client is missing: $path"
        }
    }

    . $credentialHelper | Out-Null
    if (-not $env:ORG_DB_USER -or -not $env:ORG_DB_PASSWORD) {
        throw 'The protected local database configuration did not load completely. No secret values were displayed.'
    }

    $env:MYSQL_PWD = $env:ORG_DB_PASSWORD
    $sql = Get-Content -LiteralPath $sqlFile -Raw
    Write-Host 'Running the read-only O1 database walkthrough against 127.0.0.1:3308/orgresource.'
    $sql | & $mysqlExe --connect-timeout=5 --host=127.0.0.1 --port=3308 --user=$env:ORG_DB_USER --database=orgresource --batch --raw
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL demo query failed with exit code $LASTEXITCODE."
    }
} catch {
    [Console]::Error.WriteLine($_.Exception.Message)
    exit 1
} finally {
    foreach ($name in $savedEnvironment.Keys) {
        if ($savedEnvironment[$name].WasSet) {
            [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name].Value, 'Process')
        } else {
            [Environment]::SetEnvironmentVariable($name, $null, 'Process')
        }
    }
}
