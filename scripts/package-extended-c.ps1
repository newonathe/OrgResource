# Run from a clean committed tree with PowerShell 7.4+ on Windows.
param([Parameter(Mandatory)][string] $Destination)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$output = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Destination)
if (Test-Path -LiteralPath $output) { throw 'Choose a new directory outside the source tree.' }
if ($output.StartsWith($root + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'The release directory must be outside the source tree.'
}

function Assert-ReleaseCommand([string] $Step) {
    if ($LASTEXITCODE -ne 0) { throw "$Step failed with exit code $LASTEXITCODE." }
}

Push-Location $root
try {
    $changes = @(git status --porcelain --untracked-files=all)
    Assert-ReleaseCommand 'Git status'
    if ($changes.Count) { throw 'Commit intended source or use a clean checkout before packaging.' }
    $sha = git rev-parse HEAD
    Assert-ReleaseCommand 'Source revision'
    $courseSha = '7f8c0746da18fd54b47f430ac1435ac36e605d67'
    git merge-base --is-ancestor $courseSha $sha
    Assert-ReleaseCommand 'Protected Course A ancestry'
    $courseTrees = [ordered]@{}
    foreach ($path in @('backend', 'frontend')) {
        $courseTree = git rev-parse "${courseSha}:$path"
        Assert-ReleaseCommand 'Frozen Git tree'
        $currentTree = git rev-parse "${sha}:$path"
        Assert-ReleaseCommand 'Current Git tree'
        if ($courseTree -ne $currentTree) { throw "Protected Course A tree changed: $path" }
        $courseTrees[$path] = $courseTree
    }
    $null = New-Item -ItemType Directory -Path $output
    $archive = Join-Path $output 'OrgResource-Extended-C5-Source.zip'
    $included = @('AGENTS.md', 'extended', 'backend/mvnw', 'backend/mvnw.cmd', 'backend/.mvn',
        'docs/EXTENDED_C_RELEASE.md', 'evidence/C5', 'scripts/extended-c-backup.ps1', 'scripts/package-extended-c.ps1',
        'scripts/verify-extended-c1.ps1', 'scripts/verify-extended-c-recovery.ps1', 'scripts/run-extended-c3-newman.ps1',
        'postman/OrgResource-Extended-C3-28-Operations.postman_collection.json',
        'postman/OrgResource-Extended-C3-Local.postman_environment.json', 'postman/fixtures/academic-demo-image.png')
    $entries = @(git ls-tree -r --name-only $sha -- @included)
    Assert-ReleaseCommand 'Tracked source enumeration'
    $contents = $entries | Where-Object { $_ -match '(?i)(^|/)(\.env($|\.)|target/|node_modules/|dist/|\.git/)|\.(aesgcm|key|pem|jks|p12|jar|log)$' }
    if ($contents) { throw 'Release source includes a generated or private path.' }
    git archive --format=zip "--output=$archive" $sha -- @included
    Assert-ReleaseCommand 'Exact-commit source ZIP'
} finally { Pop-Location }

# The archive contains Windows wrapper metadata and resolves all relative paths on its own.
$extracted = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-C5-SOURCE-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $extracted
tar -xf $archive -C $extracted
Assert-ReleaseCommand 'Clean ZIP extraction'
$extractedFiles = @(Get-ChildItem -LiteralPath $extracted -File -Recurse -Force | ForEach-Object {
    [IO.Path]::GetRelativePath($extracted, $_.FullName).Replace('\', '/')
})
if (@(Compare-Object $entries $extractedFiles -CaseSensitive).Count) {
    throw 'Extracted files differ from the tracked-source inventory.'
}

$verifier = Join-Path $extracted 'scripts/verify-extended-c1.ps1'
$log = Join-Path $output 'VERIFICATION.log'
& pwsh -NoProfile -File $verifier -VerifyRecovery *>&1 | Tee-Object -FilePath $log
Assert-ReleaseCommand 'Clean-extracted Java/MySQL, frontend, Newman, Chromium and recovery'

$builtJar = Join-Path $extracted 'extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar'
$jar = Join-Path $output 'OrgResource-Extended-C5-Java21.jar'
Copy-Item -LiteralPath $builtJar -Destination $jar
$jarContents = @(tar -tf $jar)
Assert-ReleaseCommand 'Executable JAR archive inventory'
foreach ($pattern in @('^BOOT-INF/classes/static/index.html$', '^BOOT-INF/classes/static/assets/.*\.js$',
        '^BOOT-INF/classes/static/assets/.*\.css$', '^BOOT-INF/classes/db/migration/V5__')) {
    if (-not ($jarContents -match $pattern)) { throw "Executable JAR lacks required asset or migration: $pattern" }
}

$artifacts = foreach ($path in @($archive, $jar, $log)) {
    [ordered]@{
        name = [IO.Path]::GetFileName($path)
        bytes = [IO.FileInfo]::new($path).Length
        sha256 = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}
$manifest = Join-Path $output 'RELEASE-MANIFEST.json'
[ordered]@{
    status = 'local-release-candidate'
    sourceSha = $sha
    protectedCourseSha = $courseSha
    courseTrees = $courseTrees
    extractedSourceRoot = $extracted
    environment = [ordered]@{ windows = [Environment]::OSVersion.VersionString; powershell = [string]$PSVersionTable.PSVersion; java = (& java -version 2>&1 | Select-Object -First 1).ToString(); node = (& node --version); npm = (& npm --version) }
    artifacts = @($artifacts)
} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $manifest -Encoding utf8
$manifestHash = (Get-FileHash -LiteralPath $manifest -Algorithm SHA256).Hash.ToLowerInvariant()
$hashes = @($artifacts | ForEach-Object { "$($_.sha256)  $($_.name)" }) + @("$manifestHash  RELEASE-MANIFEST.json")
$hashes | Set-Content -LiteralPath (Join-Path $output 'SHA256SUMS.txt') -Encoding utf8
foreach ($row in $hashes) {
    $expected, $file = $row -split '  ', 2
    $actual = (Get-FileHash -LiteralPath (Join-Path $output $file) -Algorithm SHA256).Hash
    if ($actual -ne $expected) { throw "Hash verification failed: $file" }
}
Write-Output "Local release candidate for $sha verified at $output"
