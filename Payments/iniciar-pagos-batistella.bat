@echo off
echo ==========================================
echo Iniciando Servicio de Pagos Batistella
echo ==========================================

echo.
echo 1. Verificando Docker...
docker info > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Docker no está en ejecución. Iniciando Docker Desktop...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo Esperando 60 segundos a que Docker inicie completamente...
    timeout /t 60 /nobreak > nul
) else (
    echo Docker ya está en ejecución.
)

echo.
echo 2. Iniciando contenedor de pagos...
cd %~dp0
docker-compose down
docker-compose up -d
echo Contenedor iniciado correctamente.

echo.
echo 3. Iniciando túnel Cloudflare...
echo Conectando pagos.batistellaycia.shop con el servicio local...
start "Cloudflare Tunnel" cmd /c "cloudflared.exe tunnel run batistella-payments"

echo.
echo ==========================================
echo Servicio de pagos iniciado correctamente!
echo - URL pública: https://pagos.batistellaycia.shop
echo - Panel de Docker: http://localhost:8084
echo ==========================================
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
pause > nul 