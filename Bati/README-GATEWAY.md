# 🚀 Batistella API Gateway

Un gateway API robusto y escalable implementado con NGINX para la aplicación Batistella.

## 📁 Estructura del Proyecto

```
Batistella/
├── nginx-gateway/
│   ├── conf/
│   │   ├── nginx.conf          # Configuración principal de NGINX
│   │   └── gateway.conf        # Configuración del API Gateway
│   ├── ssl/                    # Certificados SSL (vacía por ahora)
│   ├── logs/                   # Logs de NGINX
│   └── html/
│       └── index.html          # Página de documentación del API
├── services/
│   ├── auth-service/           # Servicio de autenticación
│   ├── user-service/           # Servicio de gestión de usuarios
│   └── api-service/            # API principal
├── gateway-config/
│   ├── routes/                 # Configuraciones de rutas
│   ├── middleware/             # Middleware personalizado
│   └── security/               # Configuraciones de seguridad
└── docker-compose/
    └── docker-compose.yml      # Orquestación de servicios
```

## 🛠️ Configuración Inicial

### Requisitos Previos
- Docker y Docker Compose instalados
- Puerto 80, 443, 8080, 8081, 8082 disponibles
- Java 17+ (para los servicios Spring Boot)

### 1. Configurar el entorno

```bash
# Navegar al directorio de docker-compose
cd docker-compose

# Construir e iniciar todos los servicios
docker-compose up --build -d

# Verificar que todos los servicios estén corriendo
docker-compose ps
```

### 2. Verificar el funcionamiento

```bash
# Probar el endpoint de salud
curl http://localhost/health

# Acceder a la documentación del API
# Abrir en el navegador: http://localhost
```

## 🔌 Endpoints Disponibles

### Servicio de Autenticación (`/auth/`)
- `POST /auth/login` - Iniciar sesión
- `POST /auth/register` - Registrar nuevo usuario
- `GET /auth/verify` - Verificar token
- `POST /auth/logout` - Cerrar sesión

### Servicio de Usuarios (`/users/`)
- `GET /users/{id}` - Obtener usuario por ID
- `PUT /users/{id}` - Actualizar usuario
- `GET /users/profile` - Obtener perfil del usuario actual
- `DELETE /users/{id}` - Eliminar usuario

### API Principal (`/api/`)
- `GET /api/data` - Obtener datos
- `POST /api/process` - Procesar información
- `GET /api/reports` - Generar reportes

### Utilidades del Gateway
- `GET /health` - Estado del gateway
- `GET /` - Documentación del API

## 🔒 Seguridad

### Rate Limiting
- **API endpoints**: 100 requests/minuto por IP
- **Login endpoints**: 5 requests/minuto por IP

### Headers de Seguridad
- X-Frame-Options: SAMEORIGIN
- X-Content-Type-Options: nosniff
- X-XSS-Protection: 1; mode=block
- Referrer-Policy: no-referrer-when-downgrade

### CORS
Configurado para permitir:
- Origins: * (configurar según necesidades de producción)
- Methods: GET, POST, PUT, DELETE, OPTIONS
- Headers: Authorization, Content-Type, etc.

## 🏗️ Servicios de Infraestructura

### Bases de Datos PostgreSQL
- **auth-service**: Puerto 5432 (interno)
- **user-service**: Puerto 5432 (interno)
- **api-service**: Puerto 5432 (interno)

### Redis Cache
- Puerto: 6379
- Usado para sesiones y caché

### PgAdmin
- Puerto: 5050
- Usuario: admin@batistella.com
- Contraseña: admin123

## 🐳 Comandos Docker Útiles

```bash
# Ver logs del gateway
docker-compose logs nginx-gateway

# Ver logs de un servicio específico
docker-compose logs auth-service

# Reiniciar un servicio
docker-compose restart nginx-gateway

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Reconstruir un servicio específico
docker-compose build auth-service
docker-compose up auth-service -d
```

## 📊 Monitoreo

### Logs
Los logs se almacenan en:
- `nginx-gateway/logs/access.log` - Logs de acceso
- `nginx-gateway/logs/error.log` - Logs de errores

### Health Checks
```bash
# Gateway principal
curl http://localhost/health

# Servicios individuales
curl http://localhost:8080/actuator/health  # Auth Service
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # API Service
```

## 🔧 Configuración Avanzada

### SSL/TLS (Para Producción)
1. Colocar certificados en `nginx-gateway/ssl/`
2. Modificar `gateway.conf` para incluir configuración SSL
3. Actualizar docker-compose.yml para exponer puerto 443

### Escalabilidad
- Los servicios upstream pueden escalarse agregando más instancias
- NGINX balanceará automáticamente la carga
- Redis puede configurarse en cluster para alta disponibilidad

### Variables de Entorno
Configurar en `docker-compose.yml`:
- `JWT_SECRET` - Clave secreta para tokens JWT
- `DATABASE_*` - Configuraciones de base de datos
- `REDIS_*` - Configuraciones de Redis

## 🚨 Troubleshooting

### Error: Puerto en uso
```bash
# Verificar qué proceso usa el puerto
netstat -tulpn | grep :80

# Detener Docker Compose y reiniciar
docker-compose down
docker-compose up -d
```

### Error: Conexión a base de datos
```bash
# Verificar estado de PostgreSQL
docker-compose logs postgres-auth

# Reiniciar servicios de BD
docker-compose restart postgres-auth postgres-users postgres-api
```

### Error: Gateway no responde
```bash
# Verificar configuración de NGINX
docker-compose exec nginx-gateway nginx -t

# Recargar configuración
docker-compose exec nginx-gateway nginx -s reload
```

## 📈 Próximos Pasos

1. **Implementar servicios Spring Boot** en las carpetas correspondientes
2. **Configurar SSL/TLS** para producción
3. **Agregar autenticación JWT** en los endpoints protegidos
4. **Implementar logging centralizado** con ELK Stack
5. **Configurar alertas** y monitoreo con Prometheus/Grafana
6. **Implementar CI/CD** pipeline

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

---

**Desarrollado para Batistella** 🏢 