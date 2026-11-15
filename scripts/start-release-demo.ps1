param([string]$JarPath)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $JarPath) {
    $JarPath = Join-Path $repoRoot 'deliverables\O14\OrgResource.jar'
    if (-not (Test-Path -LiteralPath $JarPath -PathType Leaf)) {
        $JarPath = Join-Path $repoRoot 'deliverables\O11-O13\OrgResource.jar'
    }
}
$jarPath = [IO.Path]::GetFullPath($JarPath)
$environmentScript = Join-Path $env:LOCALAPPDATA 'OrgResourceDev\Set-OrgResourceTestEnvironment.ps1'

if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
    throw 'Build the release artifacts first with scripts\package-release.ps1.'
}
if (-not (Test-Path -LiteralPath $environmentScript -PathType Leaf)) {
    throw 'The protected local OrgResource test-database setup is unavailable.'
}

. $environmentScript | Out-Null
$env:SPRING_PROFILES_ACTIVE = 'test'
& java -jar $jarPath --server.address=127.0.0.1
if ($LASTEXITCODE -ne 0) {
    throw "OrgResource exited with code $LASTEXITCODE."
}
