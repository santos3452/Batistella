Write-Host "Verificando que las correcciones de rutas de API esten funcionando..." -ForegroundColor Cyan

# Probar health check
try {
    $health = Invoke-RestMethod -Uri "http://localhost/health" -Method GET
    Write-Host "Health check: OK" -ForegroundColor Green
} catch {
    Write-Host "Health check: FAILED - $($_.Exception.Message)" -ForegroundColor Red
}

# Probar API productos
try {
    $products = Invoke-RestMethod -Uri "http://localhost/api/products/getAllProducts" -Method GET -TimeoutSec 30
    Write-Host "API productos: OK - Productos encontrados: $($products.Length)" -ForegroundColor Green
} catch {
    Write-Host "API productos: FAILED - Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# Probar API marcas
try {
    $marcas = Invoke-RestMethod -Uri "http://localhost/api/products/getAllMarcas" -Method GET -TimeoutSec 30
    Write-Host "API marcas: OK - Marcas encontradas: $($marcas.Length)" -ForegroundColor Green
} catch {
    Write-Host "API marcas: FAILED - Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Verificacion completada. Si todos los tests muestran 'OK', los errores 502 han sido solucionados." -ForegroundColor Yellow 