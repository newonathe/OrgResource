# Run while the Java app is still running in ANOTHER terminal.
# powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-skeleton-demo.ps1
# This is read-only: it sends GET requests and prints actual HTTP statuses/row counts.
$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8080/api"
$checks = @(
    @{ Name = "Departments"; Route = "/departments" },
    @{ Name = "Projects"; Route = "/projects" },
    @{ Name = "Resources"; Route = "/resources" },
    @{ Name = "Requirements"; Route = "/requirements" },
    @{ Name = "Allocations"; Route = "/allocations" }
)
$pass = 0
Write-Host "OrgResource O1 read-only HTTP check (local running server)" -ForegroundColor Cyan
foreach ($check in $checks) {
    $url = $base + $check.Route
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
        $payload = $response.Content | ConvertFrom-Json
        $rows = @($payload)
        if ([int]$response.StatusCode -ne 200) {
            throw "Expected status 200; got $($response.StatusCode)"
        }
        Write-Host ("PASS  200  " + $check.Name + "  rows=" + $rows.Count) -ForegroundColor Green
        $pass++
    } catch {
        Write-Host ("FAIL  " + $check.Name + "  " + $_.Exception.Message) -ForegroundColor Red
    }
}
Write-Host ""
Write-Host "$pass/5 GET endpoint checks passed."
if ($pass -ne 5) { exit 1 }
Write-Host "Use MySQL Workbench for real database table and row demonstration."
