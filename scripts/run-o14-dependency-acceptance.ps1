[CmdletBinding()]
param(
    [string]$ApiBase = 'http://127.0.0.1:8080/api'
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$collection = Join-Path $repoRoot 'postman\OrgResource-O14-Dependency-Integrity.postman_collection.json'
if (-not (Test-Path -LiteralPath $collection -PathType Leaf)) {
    throw 'The O14 dependency-integrity Postman collection is missing.'
}

$probe = Invoke-WebRequest -Uri "$ApiBase/departments" -TimeoutSec 10
if ($probe.StatusCode -ne 200) {
    throw 'The selected OrgResource API did not return HTTP 200 for GET departments.'
}

$runTag = 'O14-DI-' + (Get-Date -Format 'yyyyMMddHHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8)
& npm exec --yes --package=newman@6.2.3 -- newman run $collection --env-var "baseUrl=$ApiBase" --env-var "runTag=$runTag"
if ($LASTEXITCODE -ne 0) {
    throw "O14 dependency-integrity Newman acceptance failed with exit code $LASTEXITCODE. Review only the fictional fixtures prefixed O14-DI in the isolated test schema before any cleanup."
}

Write-Output 'O14 dependency-integrity HTTP acceptance passed; the collection deleted only its own fictional fixtures.'
