[CmdletBinding()]
param(
    [string]$ArtifactsDirectory,
    [string]$AsBuiltProposalDocx
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $repoRoot 'frontend'
$backendRoot = Join-Path $repoRoot 'backend'
if (-not $ArtifactsDirectory) {
    $ArtifactsDirectory = Join-Path $repoRoot 'deliverables\COURSE-A'
}
if (-not $AsBuiltProposalDocx -or -not (Test-Path -LiteralPath $AsBuiltProposalDocx -PathType Leaf)) {
    throw 'Pass -AsBuiltProposalDocx with the rendered Course A as-built proposal generated from the clean committed source HEAD.'
}
$AsBuiltProposalDocx = [IO.Path]::GetFullPath($AsBuiltProposalDocx)
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
$asBuiltProposal = Join-Path $ArtifactsDirectory 'OrgResource-Course-A-As-Built-Proposal.docx'
$demoGuide = Join-Path $ArtifactsDirectory 'OrgResource-Course-A-Demo-Guide.md'
$portableGuide = Join-Path $ArtifactsDirectory 'PORTABLE_FIRST_RUN.md'
$demoSql = Join-Path $ArtifactsDirectory 'OrgResource-Course-A-Demo.sql'
$postmanCollection = Join-Path $ArtifactsDirectory 'OrgResource-Course-A-28-Routes.postman_collection.json'
$postmanEnvironment = Join-Path $ArtifactsDirectory 'OrgResource-Course-A-Local.postman_environment.json'
$portableScriptDirectory = Join-Path $ArtifactsDirectory 'scripts'
$portableScript = Join-Path $portableScriptDirectory 'start-portable-demo.ps1'
$repoChanges = & git -C $repoRoot status --porcelain
if ($LASTEXITCODE -ne 0 -or $repoChanges) {
    throw 'Commit the reviewed source changes before creating source ZIPs so the archives match the repository HEAD.'
}
$sourceCommit = (& git -C $repoRoot rev-parse HEAD).Trim()
$docxArchive = [IO.Compression.ZipFile]::OpenRead($AsBuiltProposalDocx)
try {
    $coreProperties = $docxArchive.GetEntry('docProps/core.xml')
    if (-not $coreProperties) {
        throw 'The as-built proposal DOCX has no core properties for source verification.'
    }
    $coreReader = [IO.StreamReader]::new($coreProperties.Open())
    try {
        $coreText = $coreReader.ReadToEnd()
    } finally {
        $coreReader.Dispose()
    }
} finally {
    $docxArchive.Dispose()
}
if (-not $coreText.Contains($sourceCommit)) {
    throw 'The as-built proposal DOCX was not generated from this exact Git HEAD.'
}
Invoke-CheckedCommand 'git' @('-C', $repoRoot, 'archive', '--format=zip', '--prefix=OrgResource/', "--output=$fullSourceZip", 'HEAD')
Copy-Item -LiteralPath $jarPath -Destination $releaseJar -Force
Copy-Item -LiteralPath $AsBuiltProposalDocx -Destination $asBuiltProposal -Force
Copy-Item -LiteralPath (Join-Path $repoRoot 'docs\COURSE_A_FINAL_DEMO_GUIDE.md') -Destination $demoGuide -Force
Copy-Item -LiteralPath (Join-Path $repoRoot 'docs\PORTABLE_FIRST_RUN.md') -Destination $portableGuide -Force
Copy-Item -LiteralPath (Join-Path $repoRoot 'scripts\course-a-demo.sql') -Destination $demoSql -Force
Copy-Item -LiteralPath (Join-Path $repoRoot 'postman\OrgResource-Course-A-28-Routes.postman_collection.json') -Destination $postmanCollection -Force
Copy-Item -LiteralPath (Join-Path $repoRoot 'postman\OrgResource-Course-A-Local.postman_environment.json') -Destination $postmanEnvironment -Force
New-Item -ItemType Directory -Force -Path $portableScriptDirectory | Out-Null
Copy-Item -LiteralPath (Join-Path $repoRoot 'scripts\start-portable-demo.ps1') -Destination $portableScript -Force

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

$releaseFiles = @($releaseJar, $fullSourceZip, $javaSourceZip, $asBuiltProposal, $demoGuide, $portableGuide, $demoSql,
    $postmanCollection, $postmanEnvironment, $portableScript)
$artifactPaths = @{}
foreach ($releaseFile in $releaseFiles) {
    $artifactPaths[$releaseFile] = Split-Path -Leaf $releaseFile
}
$artifactPaths[$portableScript] = 'scripts/start-portable-demo.ps1'
$hashes = $releaseFiles | ForEach-Object {
    $hash = Get-FileHash -LiteralPath $_ -Algorithm SHA256
    '{0}  {1}' -f $hash.Hash.ToLowerInvariant(), $artifactPaths[$_]
}
Set-Content -LiteralPath (Join-Path $ArtifactsDirectory 'SHA256SUMS.txt') -Value $hashes -Encoding ascii
$sourceCommit = (& git -C $repoRoot rev-parse HEAD).Trim()
$sourceBranch = (& git -C $repoRoot branch --show-current).Trim()
$javaVersion = (& java -version 2>&1 | Out-String).Trim()
$mavenVersion = (& (Join-Path $backendRoot 'mvnw.cmd') -version 2>&1 | Select-Object -First 1 | Out-String).Trim()
$nodeVersion = (& node --version 2>&1 | Out-String).Trim()
$npmVersion = (& npm --version 2>&1 | Out-String).Trim()
$artifactInventory = $releaseFiles | ForEach-Object {
    $hash = Get-FileHash -LiteralPath $_ -Algorithm SHA256
    $file = Get-Item -LiteralPath $_
    [ordered]@{
        name = $file.Name
        path = $artifactPaths[$_]
        sizeBytes = $file.Length
        sha256 = $hash.Hash.ToLowerInvariant()
    }
}
$manifest = [ordered]@{
    schemaVersion = 1
    generatedAtLocal = (Get-Date).ToString('o')
    sourceBranch = $sourceBranch
    sourceCommit = $sourceCommit
    javaVersion = $javaVersion
    mavenWrapperVersion = $mavenVersion
    nodeVersion = $nodeVersion
    npmVersion = $npmVersion
    artifacts = $artifactInventory
    manifestNote = 'SHA256SUMS and this manifest describe the listed payload files; the manifest does not hash itself.'
}
($manifest | ConvertTo-Json -Depth 6) | Set-Content -LiteralPath (Join-Path $ArtifactsDirectory 'RELEASE-MANIFEST.json') -Encoding utf8
Write-Output "Release artifacts created in $ArtifactsDirectory"
Get-ChildItem -LiteralPath $ArtifactsDirectory -File | Select-Object Name, Length
