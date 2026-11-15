[CmdletBinding()]
param(
    [string]$JarPath,
    [switch]$PromptForPassword
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$oldUrl = [Environment]::GetEnvironmentVariable('ORG_DB_URL', 'Process')
$oldUser = [Environment]::GetEnvironmentVariable('ORG_DB_USER', 'Process')
$oldPassword = [Environment]::GetEnvironmentVariable('ORG_DB_PASSWORD', 'Process')
$oldReminderMode = [Environment]::GetEnvironmentVariable('ORGRESOURCE_REMINDERS_MODE', 'Process')
$loadedUrl = $false
$loadedUser = $false
$loadedPassword = $false
if (-not $JarPath) {
    $JarPath = Join-Path $repoRoot 'deliverables\O14\OrgResource.jar'
}
$JarPath = [IO.Path]::GetFullPath($JarPath)
if (-not (Test-Path -LiteralPath $JarPath -PathType Leaf)) {
    throw "Executable JAR not found at the supplied path. Pass -JarPath with the separate release JAR file."
}

$javaVersionText = (& java -version 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0 -or $javaVersionText -notmatch 'version "(?<version>[^"]+)"') {
    throw 'Java could not report its runtime version. Install a JDK or JRE 8 or newer and check PATH.'
}
$version = $Matches.version
$major = if ($version.StartsWith('1.')) { [int]($version.Split('.')[1]) } else { [int]($version.Split('.')[0]) }
if ($major -lt 8) {
    throw "Java 8 or newer is required. The detected runtime is $version."
}

try {
    if (-not $env:ORG_DB_URL) {
        $env:ORG_DB_URL = 'jdbc:mysql://127.0.0.1:3306/orgresource?useSSL=false&serverTimezone=UTC'
        $loadedUrl = $true
    }
    if ($env:ORG_DB_URL -notmatch '^jdbc:mysql://127\.0\.0\.1:3306/orgresource(?:[?;]|$)') {
        throw 'For safety, the portable demo must use the local orgresource schema at 127.0.0.1:3306. No database was changed.'
    }
    if (-not $env:ORG_DB_USER) {
        $env:ORG_DB_USER = 'orgresource_app'
        $loadedUser = $true
    }

    $portProbe = [Net.Sockets.TcpClient]::new()
    try {
        $connect = $portProbe.BeginConnect('127.0.0.1', 8080, $null, $null)
        if ($connect.AsyncWaitHandle.WaitOne(500)) {
            try {
                $portProbe.EndConnect($connect)
                throw 'Port 8080 is already in use. Stop the existing OrgResource process with Ctrl+C before starting another copy.'
            } catch [System.Net.Sockets.SocketException] {
                # A refused connection means the port is available.
            }
        }
    } finally {
        $portProbe.Close()
    }

    if ($PromptForPassword -or -not $env:ORG_DB_PASSWORD) {
        $securePassword = Read-Host 'Enter the local orgresource_app database password (input is hidden)' -AsSecureString
        $passwordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $env:ORG_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPtr)
            $loadedPassword = $true
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPtr)
            $securePassword.Dispose()
        }
    }
    if (-not $env:ORG_DB_PASSWORD) {
        throw 'No database password was supplied.'
    }

    $env:ORGRESOURCE_REMINDERS_MODE = 'DISABLED'
    Write-Host "Starting the local OrgResource JAR with Java $version at http://127.0.0.1:8080/"
    Write-Host 'Reminders are disabled. Press Ctrl+C to stop Java; the MySQL database remains available.'
    & java -jar $JarPath --server.address=127.0.0.1 --server.port=8080
    if ($LASTEXITCODE -ne 0) {
        throw "OrgResource exited with code $LASTEXITCODE. Check Java output and the local MySQL service; the password was not printed."
    }
} finally {
    if ($loadedUrl) {
        [Environment]::SetEnvironmentVariable('ORG_DB_URL', $oldUrl, 'Process')
    }
    if ($loadedUser) {
        [Environment]::SetEnvironmentVariable('ORG_DB_USER', $oldUser, 'Process')
    }
    if ($loadedPassword) {
        [Environment]::SetEnvironmentVariable('ORG_DB_PASSWORD', $oldPassword, 'Process')
    }
    [Environment]::SetEnvironmentVariable('ORGRESOURCE_REMINDERS_MODE', $oldReminderMode, 'Process')
}
