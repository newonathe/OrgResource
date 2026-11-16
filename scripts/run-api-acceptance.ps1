$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$apiBase = 'http://127.0.0.1:8080/api'
$probe = Invoke-WebRequest -Uri "$apiBase/departments" -TimeoutSec 10
if ($probe.StatusCode -ne 200) {
    throw 'The local OrgResource API did not return HTTP 200. Start it before running acceptance.'
}

function Invoke-NewmanCollection {
    param([string]$Collection, [string[]]$ExtraArguments = @())
    $collectionPath = Join-Path $repoRoot $Collection
    if (-not (Test-Path -LiteralPath $collectionPath -PathType Leaf)) {
        throw "Postman collection is missing: $Collection"
    }
    $arguments = @('exec', '--yes', '--package=newman@6.2.3', '--', 'newman', 'run', $collectionPath) + $ExtraArguments
    & npm @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Newman failed for $Collection with exit code $LASTEXITCODE."
    }
}

$runSuffix = 'O11_' + (Get-Date -Format 'yyyyMMdd_HHmmss')
$runTag = 'CourseA_' + (Get-Date -Format 'yyyyMMddHHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8)
Invoke-NewmanCollection 'postman\OrgResource-O1-Skeleton.postman_collection.json' @(
    '--environment', (Join-Path $repoRoot 'postman\OrgResource-O1-Local.postman_environment.json'),
    '--env-var', "baseUrl=$apiBase",
    '--env-var', "runSuffix=$runSuffix"
)
Invoke-NewmanCollection 'postman\OrgResource-O3-CRUD.postman_collection.json' @('--env-var', "baseUrl=$apiBase")
Invoke-NewmanCollection 'postman\OrgResource-O3-Department-Update.postman_collection.json' @('--env-var', "baseUrl=$apiBase")
Invoke-NewmanCollection 'postman\OrgResource-O4-O5-Allocation-Expiry.postman_collection.json' @(
    '--env-var', "baseUrl=$apiBase",
    '--delay-request', '1000'
)
Invoke-NewmanCollection 'postman\OrgResource-O14-Dependency-Integrity.postman_collection.json' @(
    '--env-var', "baseUrl=$apiBase",
    '--env-var', "runTag=$runTag"
)
$imageFixture = Join-Path $repoRoot 'postman\fixtures\academic-demo-image.png'
if (-not (Test-Path -LiteralPath $imageFixture -PathType Leaf)) {
    throw 'The synthetic Course A image fixture is missing.'
}
Invoke-NewmanCollection 'postman\OrgResource-Course-A-Image-HTTP.postman_collection.json' @(
    '--env-var', 'baseUrl=http://127.0.0.1:8080',
    '--env-var', "imageFile=$imageFixture",
    '--working-dir', $repoRoot
)
Write-Output 'All O1, O3, O4/O5, A09, O14 dependency-integrity, and Course A image HTTP Newman acceptance collections passed.'
