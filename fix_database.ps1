# PowerShell script to fix database schema issues
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$user = "root"
$password = "moiz123"
$database = "fundraising_platform"
$sqlFile = "fix_all_database_issues.sql"

Write-Host "Fixing database schema issues..." -ForegroundColor Yellow
Write-Host ""

try {
    $process = Start-Process -FilePath $mysqlPath -ArgumentList "--user=$user", "--password=$password", $database, "--execute=source $sqlFile" -Wait -NoNewWindow -PassThru
    
    if ($process.ExitCode -eq 0) {
        Write-Host "Database fixes applied successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Fixed issues:" -ForegroundColor Cyan
        Write-Host "  1. Added tier_id column to subscriptions table" -ForegroundColor White
        Write-Host "  2. Added credits_used column to redemptions table" -ForegroundColor White
        Write-Host "  3. Added redemption_date column to redemptions table" -ForegroundColor White
    } else {
        Write-Host "Error applying database fixes" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
