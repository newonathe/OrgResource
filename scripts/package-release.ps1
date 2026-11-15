[CmdletBinding()]
param(
    [string]$ArtifactsDirectory
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $repoRoot 'frontend'
$backendRoot = Join-Path $repoRoot 'backend'
if (-not $ArtifactsDirectory) {
    $ArtifactsDirectory = Join-Path $repoRoot 'deliverables\O11-O13'
}
$ArtifactsDirectory = [IO.Path]::GetFullPath($ArtifactsDirectory)
New-Item -ItemType Directory -Force -Path $ArtifactsDirectory | Out-Null

function Invoke-CheckedCommand {
    param([string]$Program, [string[]]$ArgumentList)
    & $Program @ArgumentList
    if ($LASTEXITCODE -ne 0) {
        throw "$Program failed with exit code $LASTEXITCODE."
    }
}

$portProbe = [Net.Sockets.TcpClient]::new()
$portInUse = $false
try {
    $connect = $portProbe.BeginConnect('127.0.0.1', 8080, $null, $null)
    if ($connect.AsyncWaitHandle.WaitOne(500)) {
        try { $portProbe.EndConnect($connect); $portInUse = $true } catch { $portInUse = $false }
    }
} finally {
    $portProbe.Close()
}
if ($portInUse) {
    throw 'Stop the OrgResource Java process on port 8080 before packaging.'
}

$testEnvironment = Join-Path $env:LOCALAPPDATA 'OrgResourceDev\Set-OrgResourceTestEnvironment.ps1'
if (Test-Path -LiteralPath $testEnvironment -PathType Leaf) {
    . $testEnvironment | Out-Null
}
if ($env:ORG_TEST_DB_URL -notmatch '^jdbc:mysql://[^/]+/orgresource_test([?;]|$)' -or
    -not $env:ORG_TEST_DB_USER -or -not $env:ORG_TEST_DB_PASSWORD) {
    throw 'A protected ORG_TEST_DB_* configuration for the isolated orgresource_test schema is required to run the release tests.'
}

Push-Location $frontendRoot
try {
    Invoke-CheckedCommand 'npm' @('ci')
    Invoke-CheckedCommand 'npm' @('run', 'typecheck')
    Invoke-CheckedCommand 'npm' @('test')
    Invoke-CheckedCommand 'npm' @('run', 'lint')
    Invoke-CheckedCommand 'npm' @('run', 'build')
} finally {
    Pop-Location
}

Push-Location $backendRoot
try {
    Invoke-CheckedCommand '.\mvnw.cmd' @('-B', '-Pbundle-react-ui', 'clean', 'verify')
} finally {
    Pop-Location
}

$jarPath = Join-Path $backendRoot 'target\orgresource-0.1.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
    throw 'Maven did not produce the executable OrgResource JAR.'
}
$jarEntries = & jar tf $jarPath
if ($LASTEXITCODE -ne 0 -or $jarEntries -notcontains 'BOOT-INF/classes/static/index.html' -or
    -not ($jarEntries | Where-Object { $_ -match '^BOOT-INF/classes/static/assets/index-.+\.js$' })) {
    throw 'The executable JAR is missing the bundled React entry point or JavaScript asset.'
}

$fullSourceZip = Join-Path $ArtifactsDirectory 'OrgResource-complete-source.zip'
$javaSourceZip = Join-Path $ArtifactsDirectory 'OrgResource-java-source.zip'
$releaseJar = Join-Path $ArtifactsDirectory 'OrgResource.jar'
$repoChanges = & git -C $repoRoot status --porcelain
if ($LASTEXITCODE -ne 0 -or $repoChanges) {
    throw 'Commit the reviewed source changes before creating source ZIPs so the archives match the repository HEAD.'
}
Invoke-CheckedCommand 'git' @('-C', $repoRoot, 'archive', '--format=zip', '--prefix=OrgResource/', "--output=$fullSourceZip", 'HEAD')
Copy-Item -LiteralPath $jarPath -Destination $releaseJar -Force

$stagingParent = [IO.Path]::GetFullPath((Join-Path $env:TEMP ('OrgResourceJavaSource-' + [guid]::NewGuid().ToString('N'))))
$tempRoot = [IO.Path]::GetFullPath($env:TEMP).TrimEnd('\') + '\'
if (-not $stagingParent.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Temporary Java source staging path is outside the system temporary directory.'
}
$javaRoot = Join-Path $stagingParent 'OrgResource-Java-Source'
New-Item -ItemType Directory -Force -Path (Join-Path $javaRoot 'src') | Out-Null
Copy-Item -LiteralPath (Join-Path $backendRoot 'pom.xml'), (Join-Path $backendRoot 'mvnw'), (Join-Path $backendRoot 'mvnw.cmd'),
    (Join-Path $backendRoot '.project'), (Join-Path $backendRoot '.classpath'), (Join-Path $backendRoot 'README.md') -Destination $javaRoot
Copy-Item -LiteralPath (Join-Path $backendRoot '.mvn') -Destination (Join-Path $javaRoot '.mvn') -Recurse
Copy-Item -LiteralPath (Join-Path $backendRoot '.settings') -Destination (Join-Path $javaRoot '.settings') -Recurse
Copy-Item -LiteralPath (Join-Path $backendRoot 'src\main') -Destination (Join-Path $javaRoot 'src\main') -Recurse
Copy-Item -LiteralPath (Join-Path $backendRoot 'src\test') -Destination (Join-Path $javaRoot 'src\test') -Recurse
$zipType = [IO.Compression.CompressionLevel]::Optimal
if (Test-Path -LiteralPath $javaSourceZip -PathType Leaf) {
    Remove-Item -LiteralPath $javaSourceZip -Force
}
[IO.Compression.ZipFile]::CreateFromDirectory($javaRoot, $javaSourceZip, $zipType, $false)
$stagingResolved = (Resolve-Path -LiteralPath $stagingParent).Path
if (-not $stagingResolved.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Resolved temporary staging path is outside the system temporary directory.'
}
Remove-Item -LiteralPath $stagingResolved -Recurse -Force

$hashes = @($releaseJar, $fullSourceZip, $javaSourceZip) | ForEach-Object {
    $hash = Get-FileHash -LiteralPath $_ -Algorithm SHA256
    '{0}  {1}' -f $hash.Hash.ToLowerInvariant(), (Split-Path -Leaf $_)
}
Set-Content -LiteralPath (Join-Path $ArtifactsDirectory 'SHA256SUMS.txt') -Value $hashes -Encoding ascii
Write-Output "Release artifacts created in $ArtifactsDirectory"
Get-ChildItem -LiteralPath $ArtifactsDirectory -File | Select-Object Name, Length
