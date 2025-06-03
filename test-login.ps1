Write-Host "Probando el endpoint de login..." -ForegroundColor Cyan

# Probar health check
try {
    $health = Invoke-RestMethod -Uri "http://localhost/health" -Method GET
    Write-Host "Health check: OK" -ForegroundColor Green
} catch {
    Write-Host "Health check: FAILED - $($_.Exception.Message)" -ForegroundColor Red
}

# Probar endpoint de login con OPTIONS (CORS preflight)
try {
    $options = Invoke-WebRequest -Uri "http://localhost/auth/login" -Method OPTIONS -UseBasicParsing
    Write-Host "Auth OPTIONS (CORS): OK - Status: $($options.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Auth OPTIONS: FAILED - Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# Probar endpoint de login con POST (sin credenciales válidas, solo para ver si acepta POST)
try {
    $body = @{
        email = "test@test.com"
        password = "test123"
    } | ConvertTo-Json

    $headers = @{
        "Content-Type" = "application/json"
    }

    $response = Invoke-RestMethod -Uri "http://localhost/auth/login" -Method POST -Body $body -Headers $headers -TimeoutSec 10
    Write-Host "Auth POST: ACCEPTED (aunque las credenciales sean incorrectas)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode
    if ($statusCode -eq "Unauthorized" -or $statusCode -eq "BadRequest") {
        Write-Host "Auth POST: OK - Endpoint acepta POST (Status: $statusCode)" -ForegroundColor Green
    } elseif ($statusCode -eq "MethodNotAllowed") {
        Write-Host "Auth POST: FAILED - Método no permitido (Status: $statusCode)" -ForegroundColor Red
    } else {
        Write-Host "Auth POST: Respuesta: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Verificación de login completada." -ForegroundColor Yellow 