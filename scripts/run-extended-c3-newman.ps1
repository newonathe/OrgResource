param(
    [Parameter(Mandatory = $true)][string] $Username,
    [Parameter(Mandatory = $true)][string] $Password,
    [string] $BaseUrl = 'http://127.0.0.1:8080',
    [string] $NewmanVersion = '6.2.3',
    [switch] $Reporting
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$collectionName = if ($Reporting) { 'OrgResource-Extended-C6a-Reporting' } else { 'OrgResource-Extended-C3-28-Operations' }
$collection = Join-Path $repoRoot "postman\$collectionName.postman_collection.json"
$imagePath = Join-Path $repoRoot 'postman\fixtures\academic-demo-image.png'

if (-not (Test-Path -LiteralPath $collection)) { throw 'The requested Extended Postman collection is missing.' }
if (-not (Test-Path -LiteralPath $imagePath)) { throw 'The tracked fictional PNG fixture is missing.' }
if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Password)) {
    throw 'A local fictional test account is required; credentials are never read from or written to repository files.'
}
$parsedBase = [Uri]::new($BaseUrl)
if ($parsedBase.Scheme -ne 'http' -or $parsedBase.Host -notin @('127.0.0.1', 'localhost', '::1')) {
    throw 'Extended C3 Newman acceptance is restricted to HTTP loopback.'
}

$tempDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('OrgResource-C3-Newman-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $tempDirectory
$environmentPath = Join-Path $tempDirectory 'local.postman_environment.json'
$environment = [ordered]@{
    id = [guid]::NewGuid().ToString()
    name = 'Temporary Extended C3 local run'
    values = @(
        @{ key = 'baseUrl'; value = $BaseUrl.TrimEnd('/'); type = 'default'; enabled = $true },
        @{ key = 'username'; value = $Username; type = 'secret'; enabled = $true },
        @{ key = 'password'; value = $Password; type = 'secret'; enabled = $true },
        @{ key = 'imagePath'; value = $imagePath; type = 'default'; enabled = $true }
    )
    _postman_variable_scope = 'environment'
    _postman_exported_using = 'Newman'
}

try {
    $environment | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $environmentPath -Encoding utf8
    Push-Location $repoRoot
    try {
        $output = & npx --yes "newman@$NewmanVersion" run $collection `
            --environment $environmentPath --reporters cli --bail --delay-request 1000 2>&1
        $exitCode = $LASTEXITCODE
    } finally { Pop-Location }
    if ($exitCode -ne 0) {
        $output | Select-Object -Last 100 | ForEach-Object { Write-Output $_ }
        throw "$collectionName Newman acceptance failed with exit code $exitCode."
    }
    $summary = @($output | Where-Object { $_ -match 'requests|assertions|iterations|requests|total|failed|error' })
    $summary | ForEach-Object { Write-Output $_ }
    Write-Output "$collectionName Newman acceptance passed against the supplied loopback server."
} finally {
    if (Test-Path -LiteralPath $environmentPath) { Remove-Item -LiteralPath $environmentPath -Force }
    if (Test-Path -LiteralPath $tempDirectory) { Remove-Item -LiteralPath $tempDirectory -ErrorAction SilentlyContinue }
}
