# Run from a clean committed tree with PowerShell 7.4+ on Windows.
param(
    [Parameter(Mandatory)][string] $Destination,
    [string] $Python = 'python.exe'
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$output = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Destination)
if (Test-Path -LiteralPath $output) { throw 'Choose a new directory outside the source tree.' }
$parent = (Resolve-Path -LiteralPath ([IO.Path]::GetDirectoryName($output))).Path
for ($ancestor = Get-Item -LiteralPath $parent; $null -ne $ancestor; $ancestor = $ancestor.Parent) {
    if ($ancestor.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw 'Choose a release parent without junctions or symbolic links.'
    }
}
$output = Join-Path $parent ([IO.Path]::GetFileName($output))
if ($output.Equals($root, [StringComparison]::OrdinalIgnoreCase) -or
        $output.StartsWith($root + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'The release directory must be outside the source tree.'
}
$stage = Join-Path $parent ('.' + [IO.Path]::GetFileName($output) + '.staging-' + [guid]::NewGuid().ToString('N'))

function Assert-ReleaseCommand([string] $Step) {
    if ($LASTEXITCODE -ne 0) { throw "$Step failed with exit code $LASTEXITCODE." }
}

# Compare complete migration inventory and bytes, not only version prefixes.
function Assert-PackagedMigrations([string] $JarPath, [System.Collections.IDictionary] $Expected) {
    $prefix = 'BOOT-INF/classes/db/migration/'
    $zip = [IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        $actual = @($zip.Entries | Where-Object {
            $_.FullName.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and $_.FullName -cne $prefix
        })
        if ($actual.Count -ne $Expected.Count) { throw 'Executable JAR migration inventory count differs.' }
        foreach ($name in $Expected.Keys) {
            $matches = @($actual | Where-Object { $_.FullName -ceq ($prefix + $name) })
            if ($matches.Count -ne 1) { throw "Executable JAR migration missing or duplicated: $name" }
            $stream = $matches[0].Open()
            try {
                $digest = (Get-FileHash -InputStream $stream -Algorithm SHA256).Hash
                if ($digest -cne $Expected[$name]) { throw "Executable JAR migration bytes differ: $name" }
            } finally { $stream.Dispose() }
        }
    } finally { $zip.Dispose() }
}

function Assert-FrozenMigrationSource([string] $Revision, [string] $MigrationDir, [System.Collections.IDictionary] $Pins) {
    $migrationEntries = @(git ls-tree -r --name-only $Revision -- $MigrationDir)
    Assert-ReleaseCommand 'Migration inventory'
    $expectedPaths = @($Pins.Keys | ForEach-Object { "$MigrationDir/$_" })
    if ($migrationEntries.Count -ne $expectedPaths.Count -or
        @(Compare-Object $expectedPaths $migrationEntries -CaseSensitive).Count) {
        throw 'Tracked migrations differ from frozen V1–V7 inventory.'
    }
    foreach ($name in $Pins.Keys) {
        $blob = git rev-parse "${Revision}:$MigrationDir/$name"
        Assert-ReleaseCommand 'Frozen migration blob'
        if ($blob -cne $Pins[$name]) { throw "Frozen migration changed: $name" }
    }
}

function Get-FrozenMigrationDigests([string] $SourceRoot, [string] $MigrationDir, [System.Collections.IDictionary] $Pins) {
    $digests = [ordered]@{}
    foreach ($name in $Pins.Keys) {
        $path = Join-Path $SourceRoot "$MigrationDir/$name"
        $blob = git hash-object --no-filters -- $path
        Assert-ReleaseCommand 'Extracted migration blob'
        if ($blob -cne $Pins[$name]) { throw "Extracted migration bytes differ: $name" }
        $digests[$name] = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash
    }
    return $digests
}

# Frozen V1–V7 Git blob identities; changing this list requires the separately approved migration gate.
$migrationPins = [ordered]@{
    'V1__extended_c1_baseline.sql' = '22b1e0f589ab05d2b0721e5de39bb29b371a2382'
    'V2__spring_session_jdbc_mysql.sql' = '4dd537e2bcfb22898fb144860cb5881afe904790'
    'V3__atomic_inventory_ledger.sql' = '37ce7fffc3a2a5aae6556aec22f5c8f7755b363c'
    'V4__c3_contact_images_and_reminders.sql' = '56d09c0c7f46a23d0d29f8fd5eb236548e86f29d'
    'V5__c4_reminder_dispatch_boundary.sql' = 'ff9f7330de52b7f6aba6f4f21c29382896eeb5a5'
    'V6__c8_project_leads_and_linked_borrowers.sql' = '187e5aeff4481d250db9e45f6f7d7cb685e683d4'
    'V7__c8_local_demo_provenance.sql' = '7072ea7d77a7d8d4f03b790e5516e4f7fd470cb0'
}
$migrationDir = 'extended/backend/src/main/resources/db/migration'

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
    Assert-FrozenMigrationSource -Revision $sha -MigrationDir $migrationDir -Pins $migrationPins
    $archive = Join-Path $stage 'OrgResource-Extended-C6a-Source.zip'
    $included = @('AGENTS.md', 'extended', 'backend/mvnw', 'backend/mvnw.cmd', 'backend/.mvn',
        'docs/EXTENDED_C_RELEASE.md', 'docs/QUALITY_EVIDENCE_CONTINUOUS_GATE.md', 'docs/EXTENDED_C_REPORTING.md',
        'evidence/C5', 'evidence/C6a', 'scripts/extended-c-backup.ps1', 'scripts/package-extended-c.ps1',
        'scripts/verify-extended-c1.ps1', 'scripts/verify-extended-c-recovery.ps1', 'scripts/verify-extended-c6a-operator.ps1',
        'scripts/run-extended-c3-newman.ps1',
        'postman/OrgResource-Extended-C3-28-Operations.postman_collection.json',
        'postman/OrgResource-Extended-C6a-Reporting.postman_collection.json',
        'postman/OrgResource-Extended-C3-Local.postman_environment.json', 'postman/fixtures/academic-demo-image.png')
    $entries = @(git ls-tree -r --name-only $sha -- @included)
    Assert-ReleaseCommand 'Tracked source enumeration'
    $contents = $entries | Where-Object { $_ -match '(?i)(^|/)(\.env($|\.)|target/|node_modules/|dist/|\.git/|\.mylogin\.cnf$)|\.(aesgcm|key|pem|jks|p12|jar|log|cnf|db|sqlite3?|dump|bak|zip|pfx)$' }
    if ($contents) { throw 'Release source includes a generated or private path.' }
    $tree = @(git ls-tree -r $sha -- @included)
    Assert-ReleaseCommand 'Tracked source mode inspection'
    if ($tree -match '^(120000|160000) ') { throw 'Release source includes a symlink or submodule.' }
    $secretPatterns = '-----BEGIN ([A-Z0-9]+ )?PRIVATE KEY-----|gh[pousr]_[A-Za-z0-9]{30,}|AKIA[A-Z0-9]{16}|://[^/[:space:]:]+:[^/@[:space:]]+@'
    $flagged = @(git grep -IlE -e $secretPatterns $sha -- @included)
    if ($LASTEXITCODE -gt 1) { throw 'Source secret-marker scan did not complete.' }
    if ($flagged.Count) { throw 'Release source contains a private-key, token, or credential-bearing URL marker.' }
    $null = New-Item -ItemType Directory -Path $stage
    Write-Output "Unpublished staging directory: $stage"
    git archive --format=zip "--output=$archive" $sha -- @included
    Assert-ReleaseCommand 'Exact-commit source ZIP'
} finally { Pop-Location }

# The archive contains Windows wrapper metadata and resolves all relative paths on its own.
$extracted = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-C6a-SOURCE-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $extracted
tar -xf $archive -C $extracted
Assert-ReleaseCommand 'Clean ZIP extraction'
$extractedFiles = @(Get-ChildItem -LiteralPath $extracted -File -Recurse -Force | ForEach-Object {
    [IO.Path]::GetRelativePath($extracted, $_.FullName).Replace('\', '/')
})
if (@(Compare-Object $entries $extractedFiles -CaseSensitive).Count) {
    throw 'Extracted files differ from the tracked-source inventory.'
}

# Bind the actual archived migration bytes before build tools execute.
Push-Location $root
try {
    $migrationDigests = Get-FrozenMigrationDigests -SourceRoot $extracted -MigrationDir $migrationDir -Pins $migrationPins
} finally { Pop-Location }

$verifier = Join-Path $extracted 'scripts/verify-extended-c1.ps1'
$log = Join-Path $stage 'VERIFICATION.log'
& pwsh -NoProfile -File $verifier -VerifyRecovery *>&1 | Tee-Object -FilePath $log
Assert-ReleaseCommand 'Clean-extracted Java/MySQL, frontend, Newman, Chromium and recovery'

$builtJar = Join-Path $extracted 'extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar'
$operator = Join-Path $extracted 'scripts/verify-extended-c6a-operator.ps1'
& pwsh -NoProfile -File $operator -JarPath $builtJar -Python $Python *>&1 | Tee-Object -FilePath $log -Append
Assert-ReleaseCommand 'Clean-extracted independent operator and protected-login recovery'
$jar = Join-Path $stage 'OrgResource-Extended-C6a-Java21.jar'
Copy-Item -LiteralPath $builtJar -Destination $jar
$jarContents = @(tar -tf $jar)
Assert-ReleaseCommand 'Executable JAR archive inventory'
foreach ($pattern in @('^BOOT-INF/classes/static/index.html$', '^BOOT-INF/classes/static/assets/.*\.js$',
        '^BOOT-INF/classes/static/assets/.*\.css$')) {
    if (-not ($jarContents -match $pattern)) { throw "Executable JAR lacks required asset: $pattern" }
}
Assert-PackagedMigrations -JarPath $jar -Expected $migrationDigests

# This release lane is frozen at V1–V7. Check exact names, not just version prefixes.
$migrationRoot = 'extended/backend/src/main/resources/db/migration/'
$jarMigrationRoot = 'BOOT-INF/classes/db/migration/'
$expectedMigrationNames = @(
    'V1__extended_c1_baseline.sql',
    'V2__spring_session_jdbc_mysql.sql',
    'V3__atomic_inventory_ledger.sql',
    'V4__c3_contact_images_and_reminders.sql',
    'V5__c4_reminder_dispatch_boundary.sql',
    'V6__c8_project_leads_and_linked_borrowers.sql',
    'V7__c8_local_demo_provenance.sql'
)
$expectedSourceMigrations = @($expectedMigrationNames | ForEach-Object { $migrationRoot + $_ })
$expectedJarMigrations = @($expectedMigrationNames | ForEach-Object { $jarMigrationRoot + $_ })
$sourceMigrations = @($entries | Where-Object { $_.StartsWith($migrationRoot, [StringComparison]::Ordinal) })
$jarMigrations = @($jarContents | Where-Object {
    $_.StartsWith($jarMigrationRoot, [StringComparison]::Ordinal) -and -not $_.EndsWith('/')
})
if ($sourceMigrations.Count -ne $expectedSourceMigrations.Count -or
        @(Compare-Object $expectedSourceMigrations $sourceMigrations -CaseSensitive).Count) {
    throw 'Source migration inventory differs from the approved V1–V7 release lane.'
}
if ($jarMigrations.Count -ne $expectedJarMigrations.Count -or
        @(Compare-Object $expectedJarMigrations $jarMigrations -CaseSensitive).Count) {
    throw 'Executable JAR migration inventory differs from the approved V1–V7 release lane.'
}

$artifacts = foreach ($path in @($archive, $jar, $log)) {
    [ordered]@{
        name = [IO.Path]::GetFileName($path)
        bytes = [IO.FileInfo]::new($path).Length
        sha256 = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}
$manifest = Join-Path $stage 'RELEASE-MANIFEST.json'
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
$hashPath = Join-Path $stage 'SHA256SUMS.txt'
$hashes | Set-Content -LiteralPath $hashPath -Encoding utf8
foreach ($row in $hashes) {
    $expected, $file = $row -split '  ', 2
    $actual = (Get-FileHash -LiteralPath (Join-Path $stage $file) -Algorithm SHA256).Hash
    if ($actual -ne $expected) { throw "Hash verification failed: $file" }
}
$manifestListHash = (Get-FileHash -LiteralPath $hashPath -Algorithm SHA256).Hash.ToLowerInvariant()
[ordered]@{ sourceSha = $sha; checksumsSha256 = $manifestListHash } |
    ConvertTo-Json | Set-Content -LiteralPath (Join-Path $stage 'READY.json') -Encoding utf8
[IO.Directory]::Move($stage, $output)
Write-Output "Local release candidate for $sha verified at $output"
