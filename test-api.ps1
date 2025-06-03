try {
    Write-Host "🔄 Probando API de productos..." -ForegroundColor Yellow
    $response = Invoke-WebRequest -Uri "http://localhost/api/products/getAllProducts" -Method GET -UseBasicParsing
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "✅ API funcionando correctamente!" -ForegroundColor Green
    
    Write-Host "`n🔄 Probando API de marcas..." -ForegroundColor Yellow
    $response2 = Invoke-WebRequest -Uri "http://localhost/api/products/getAllMarcas" -Method GET -UseBasicParsing
    Write-Host "✅ Status: $($response2.StatusCode)" -ForegroundColor Green
    Write-Host "✅ API de marcas funcionando correctamente!" -ForegroundColor Green
    
    Write-Host "`n🔄 Probando health check..." -ForegroundColor Yellow
    $health = Invoke-WebRequest -Uri "http://localhost/health" -Method GET -UseBasicParsing
    Write-Host "✅ Health check: $($health.StatusCode) - $($health.Content)" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "❌ Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
} 