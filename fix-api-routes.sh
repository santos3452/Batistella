#!/bin/bash

echo "🔧 Solucionando problemas de rutas de API..."

# Verificar si Docker está ejecutándose
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está ejecutándose"
    exit 1
fi

echo "📋 Estado actual de contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo "🔄 Recreando el gateway nginx con la nueva configuración..."

# Detener el gateway
docker stop batistella-gateway

# Eliminar el contenedor anterior
docker rm batistella-gateway

# Recrear el gateway con la nueva configuración
docker run -d \
    --name batistella-gateway \
    --network batistella_batistella-network \
    -p 80:80 \
    -v "$(pwd)/Bati/nginx-gateway/conf/nginx.conf:/etc/nginx/nginx.conf:ro" \
    -v "$(pwd)/Bati/nginx-gateway/conf/gateway.conf:/etc/nginx/conf.d/default.conf:ro" \
    -v "$(pwd)/Bati/nginx-gateway/html:/usr/share/nginx/html:ro" \
    -v "$(pwd)/Bati/nginx-gateway/logs:/var/log/nginx" \
    nginx:1.25-alpine

echo "⏳ Esperando que el gateway se inicie..."
sleep 5

echo "🔍 Verificando estado de servicios:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep batistella

echo "✅ Gateway nginx recreado con la nueva configuración"

# Verificar que el gateway responde
echo "🌐 Verificando conectividad del gateway..."
if curl -s http://localhost/health > /dev/null; then
    echo "✅ Gateway responde correctamente"
else
    echo "⚠️  El gateway puede tardar unos segundos en estar listo"
fi

echo "📝 Para verificar que todo funcione:"
echo "   - Abrir: http://localhost"
echo "   - Verificar que no haya errores 502 en las APIs"
echo "   - Revisar logs: docker logs batistella-gateway" 