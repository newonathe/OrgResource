# OrgResource O1 video demonstration startup.
# Run from REPOSITORY ROOT:
# powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-skeleton-demo.ps1
# This script does NOT delete/reset the database, change source code, or upload anything.
# It needs Java 8 and the locally configured MySQL service. It prompts for a missing
# database password with hidden input; it never prints that password.
param([switch]$SkipBuild)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $repoRoot "backend"
$jar = Join-Path $backend "target\orgresource-0.1.0-SNAPSHOT.jar"

Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " OrgResource O1 - REAL skeleton startup for video" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Repository: $repoRoot"
Write-Host "No database reset or cleanup will be performed."

if (-not (Test-Path -LiteralPath (Join-Path $backend "pom.xml"))) {
    throw "Cannot find backend\pom.xml. Open the actual OrgResource repository folder."
}
if (-not (Test-Path -LiteralPath (Join-Path $backend "mvnw.cmd"))) {
    throw "Cannot find backend\mvnw.cmd. Restore the original Java skeleton files."
}

# Respect an existing JAVA_HOME if configured. Do not guess absolute machine paths.
if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    $javaExe = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (-not (Test-Path -LiteralPath $javaExe)) {
        throw "JAVA_HOME is set but bin\java.exe is missing. Correct JAVA_HOME to your JDK 8 installation."
    }
    $env:Path = (Join-Path $env:JAVA_HOME "bin") + ";" + $env:Path
}
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Java is not installed or unavailable in this terminal. The O1 project requires Java 8."
}
$javaOutput = @(cmd.exe /d /c "java -version 2>&1") -join " "
Write-Host "Detected Java: $javaOutput"
if ($javaOutput -notmatch 'version "1\.8\.') {
    throw "The Java runtime is not Java 8. Select the verified JDK 8 (set JAVA_HOME), then rerun."
}

if ([string]::IsNullOrWhiteSpace($env:ORG_DB_URL)) {
    $env:ORG_DB_URL = "jdbc:mysql://127.0.0.1:3308/orgresource?useSSL=false&serverTimezone=UTC"
    Write-Host "Using the O1 workstation's recorded MySQL endpoint 127.0.0.1:3308; change ORG_DB_URL if your local DB differs." -ForegroundColor Yellow
}
if ([string]::IsNullOrWhiteSpace($env:ORG_DB_USER)) {
    $env:ORG_DB_USER = "orgresource_dev"
}
if ([string]::IsNullOrWhiteSpace($env:ORG_DB_PASSWORD)) {
    Write-Host "The Git repository deliberately has no database password." -ForegroundColor Yellow
    Write-Host "If your Windows local setup already has one, provide it here. The characters will be hidden."
    $secure = Read-Host "Local MySQL orgresource_dev password" -AsSecureString
    $handle = [IntPtr]::Zero
    try {
        $handle = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
        $env:ORG_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($handle)
    } finally {
        if ($handle -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($handle)
        }
    }
    if ([string]::IsNullOrEmpty($env:ORG_DB_PASSWORD)) {
        throw "No local database password provided; no Java application was started. Restore the previous local O1 database configuration."
    }
}
Write-Host "DB endpoint configured. Database password is not shown."
Write-Host "HTTP API will bind to http://127.0.0.1:8080/api/"

$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($listener) {
    Write-Host "Port 8080 is already in use. Another Java instance may be running." -ForegroundColor Red
    throw "Stop the existing application safely before recording a fresh startup; this script will not kill processes."
}

if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "[1/2] Building Java backend using bundled Maven Wrapper..." -ForegroundColor Cyan
    Push-Location $backend
    try {
        & ".\mvnw.cmd" -B -ntp clean package
        if ($LASTEXITCODE -ne 0) { throw "Maven build failed. Check the actual log above; do not record this as a successful startup." }
    } finally {
        Pop-Location
    }
}
if (-not (Test-Path -LiteralPath $jar)) {
    throw "Runnable JAR not found at backend\target\orgresource-0.1.0-SNAPSHOT.jar"
}
Write-Host ""
Write-Host "[2/2] Launching the actual Java app. LEAVE THIS TERMINAL OPEN." -ForegroundColor Green
Write-Host "The application will continue running until you press Ctrl+C."
Write-Host "Show the real Spring Boot, Jersey and MySQL startup lines in your video."
Write-Host ""
& java -jar $jar
if ($LASTEXITCODE -ne 0) {
    throw "Java process exited with error code $LASTEXITCODE. Check the log."
}
