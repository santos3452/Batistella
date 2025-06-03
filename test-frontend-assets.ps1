# Test Frontend Assets
Write-Host "Testing Frontend Assets..." -ForegroundColor Cyan

$baseUrl = "http://localhost:4200"
$assets = @(
    "/assets/Images/Logo.png",
    "/assets/Images/BatistellaLogo.jpg", 
    "/assets/Images/BatistellaLogo2.jpg",
    "/assets/Images/favicon.svg",
    "/assets/Images/Animals.webp",
    "/assets/Images/icons8-mercado-pago-48.png",
    "/assets/Images/IconsMarcas/TopNutrition_Brand_white_vFF.ico"
)

foreach ($asset in $assets) {
    $url = $baseUrl + $asset
    Write-Host "`nTesting: $asset" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method Get -TimeoutSec 10
        Write-Host "✅ Status: $($response.StatusCode) - Size: $($response.Content.Length) bytes - Type: $($response.Headers['Content-Type'])" -ForegroundColor Green
    } catch {
        Write-Host "❌ Status: $($_.Exception.Response.StatusCode) - Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nTesting through Gateway..." -ForegroundColor Cyan
$gatewayUrl = "http://localhost"

foreach ($asset in $assets) {
    $url = $gatewayUrl + $asset
    Write-Host "`nTesting: $asset (Gateway)" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method Get -TimeoutSec 10
        Write-Host "✅ Status: $($response.StatusCode) - Size: $($response.Content.Length) bytes - Type: $($response.Headers['Content-Type'])" -ForegroundColor Green
    } catch {
        Write-Host "❌ Status: $($_.Exception.Response.StatusCode) - Error: $($_.Exception.Message)" -ForegroundColor Red
    }
} 