[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$backendRoot = Join-Path $repoRoot 'backend'
$credentialHelper = Join-Path $env:LOCALAPPDATA 'OrgResourceDev\Set-OrgResourceDbEnvironment.ps1'
$credentialFile = Join-Path $env:LOCALAPPDATA 'OrgResourceDev\orgresource-app.credential.xml'
$jarPath = Join-Path $backendRoot 'target\orgresource-0.1.0-SNAPSHOT.jar'

function Test-LocalTcpPort {
    param(
        [Parameter(Mandatory = $true)][string]$HostName,
        [Parameter(Mandatory = $true)][int]$Port,
        [int]$TimeoutMilliseconds = 2500
    )

    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $result = $client.BeginConnect($HostName, $Port, $null, $null)
        if (-not $result.AsyncWaitHandle.WaitOne($TimeoutMilliseconds)) {
            return $false
        }
        $client.EndConnect($result)
        return $true
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Restore-ProcessEnvironment {
    param([hashtable]$SavedValues)
    foreach ($name in $SavedValues.Keys) {
        if ($SavedValues[$name].WasSet) {
            [Environment]::SetEnvironmentVariable($name, $SavedValues[$name].Value, 'Process')
        } else {
            [Environment]::SetEnvironmentVariable($name, $null, 'Process')
        }
    }
}

$environmentNames = @('JAVA_HOME', 'PATH', 'ORG_DB_URL', 'ORG_DB_USER', 'ORG_DB_PASSWORD')
$savedEnvironment = @{}
foreach ($name in $environmentNames) {
    $value = [Environment]::GetEnvironmentVariable($name, 'Process')
    $savedEnvironment[$name] = @{
        WasSet = ($null -ne $value)
        Value = $value
    }
}

try {
    $jdkCandidates = @()
    if ($env:ORGRESOURCE_JAVA8_HOME) { $jdkCandidates += $env:ORGRESOURCE_JAVA8_HOME }
    if ($env:JAVA_HOME) { $jdkCandidates += $env:JAVA_HOME }
    if ($env:USERPROFILE) {
        $jdkCandidates += (Join-Path $env:USERPROFILE '.codex\toolchains\orgresource-temurin8-8u512')
    }

    $jdkHome = $null
    foreach ($candidate in ($jdkCandidates | Select-Object -Unique)) {
        $javaCandidate = Join-Path $candidate 'bin\java.exe'
        $javacCandidate = Join-Path $candidate 'bin\javac.exe'
        if ((Test-Path -LiteralPath $javaCandidate) -and (Test-Path -LiteralPath $javacCandidate)) {
            $versionOutput = (& $javaCandidate -version 2>&1 | Out-String)
            if ($versionOutput -match 'version "1\.8\.') {
                $jdkHome = $candidate
                break
            }
        }
    }
    if (-not $jdkHome) {
        throw 'A JDK 8 installation with java.exe and javac.exe was not found. Set ORGRESOURCE_JAVA8_HOME to its folder.'
    }

    $env:JAVA_HOME = $jdkHome
    $env:PATH = (Join-Path $jdkHome 'bin') + [IO.Path]::PathSeparator + $savedEnvironment.PATH.Value
    $javaExe = Join-Path $jdkHome 'bin\java.exe'
    Write-Host 'Java 8 selected for the O1 skeleton.'

    if (-not (Test-Path -LiteralPath $credentialHelper -PathType Leaf) -or
        -not (Test-Path -LiteralPath $credentialFile -PathType Leaf)) {
        throw 'The existing protected local database configuration is missing. No database was changed.'
    }
    if (-not (Test-LocalTcpPort -HostName '127.0.0.1' -Port 3308)) {
        throw 'The local OrgResource MySQL server is not listening on 127.0.0.1:3308. Start the existing database first; this script will not create, reset, or delete it.'
    }
    if (Test-LocalTcpPort -HostName '127.0.0.1' -Port 8080) {
        throw 'Port 8080 is already in use. Stop the existing local application before starting this demo; this script will not terminate other processes.'
    }

    . $credentialHelper | Out-Null
    if (-not $env:ORG_DB_URL -or -not $env:ORG_DB_USER -or -not $env:ORG_DB_PASSWORD) {
        throw 'The protected local database configuration did not load completely. No secret values were displayed.'
    }

    Push-Location $backendRoot
    try {
        Write-Host 'Building the existing backend with the Maven Wrapper (offline mode).'
        & .\mvnw.cmd -o -B -ntp clean package
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed with exit code $LASTEXITCODE."
        }
    } finally {
        Pop-Location
    }

    if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
        throw 'The expected O1 backend JAR was not produced by the Maven build.'
    }

    Write-Host 'Starting OrgResource at http://127.0.0.1:8080/api.'
    Write-Host 'Keep this window open during the demo. Press Ctrl+C here to stop Java cleanly; MySQL remains running.'
    & $javaExe -jar $jarPath
    if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne 130) {
        throw "The Java application exited with code $LASTEXITCODE."
    }
} catch {
    Write-Error $_
    exit 1
} finally {
    Restore-ProcessEnvironment -SavedValues $savedEnvironment
}
