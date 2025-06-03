# Guía de Despliegue - Batistella & Cía

## Configuración para batistellaycia.shop

### Prerrequisitos
- Docker y Docker Compose instalados
- Dominio batistellaycia.shop configurado en Cloudflare apuntando a tu servidor

### Pasos de Despliegue

#### 1. Preparar el entorno
```bash
# Clonar el repositorio
git clone <tu-repositorio>
cd Batistella

# Dar permisos de ejecución a scripts (si los hay)
chmod +x scripts/*.sh
```

#### 2. Configurar variables de entorno
Crear archivo `.env` en la raíz del proyecto:
```env
DOMAIN=batistellaycia.shop
NODE_ENV=production
JWT_SECRET=batistella-jwt-secret-key-2024
PGADMIN_EMAIL=admin@batistella.com
PGADMIN_PASSWORD=admin123
```

#### 3. Construir y ejecutar los contenedores
```bash
# Construir todos los servicios
docker-compose build

# Ejecutar todos los servicios
docker-compose up -d

# Verificar que todos los contenedores estén corriendo
docker-compose ps
```

#### 4. Configuración en Cloudflare
En tu panel de Cloudflare para batistellaycia.shop:

1. **DNS Records:**
   - A record: `@` → IP de tu servidor
   - A record: `www` → IP de tu servidor

2. **SSL/TLS:**
   - Modo: "Full" o "Full (strict)"
   - Edge Certificates: Habilitado

3. **Page Rules (opcional):**
   - `www.batistellaycia.shop/*` → `https://batistellaycia.shop/$1` (Redirect 301)

#### 5. Verificar el despliegue
```bash
# Verificar logs de nginx
docker logs batistella-gateway

# Verificar logs del frontend
docker logs batistella-frontend

# Verificar logs de los microservicios
docker logs batistella-auth
docker logs batistella-users
docker logs batistella-api
```

#### 6. URLs de acceso
- **Frontend:** https://batistellaycia.shop
- **API Gateway:** https://batistellaycia.shop/api/
- **Autenticación:** https://batistellaycia.shop/auth/
- **PgAdmin:** https://batistellaycia.shop:5050

### Comandos útiles

#### Gestión de contenedores
```bash
# Detener todos los servicios
docker-compose down

# Reiniciar un servicio específico
docker-compose restart nginx-gateway

# Ver logs en tiempo real
docker-compose logs -f

# Actualizar un servicio
docker-compose up -d --no-deps --build <nombre-servicio>
```

#### Base de datos
```bash
# Backup de base de datos
docker exec batistella-postgres-auth pg_dump -U auth_user auth_db > backup_auth.sql

# Restaurar base de datos
docker exec -i batistella-postgres-auth psql -U auth_user auth_db < backup_auth.sql
```

### Troubleshooting

#### Problema: Frontend no carga
```bash
# Verificar logs del frontend
docker logs batistella-frontend

# Verificar configuración de nginx
docker exec batistella-gateway nginx -t
```

#### Problema: API no responde
```bash
# Verificar conectividad entre servicios
docker exec batistella-gateway ping auth-service

# Verificar logs del gateway
docker logs batistella-gateway
```

#### Problema: Base de datos no conecta
```bash
# Verificar estado de PostgreSQL
docker logs batistella-postgres-auth

# Verificar variables de entorno
docker exec batistella-auth env | grep DATABASE
```

### Monitoreo
```bash
# Verificar uso de recursos
docker stats

# Verificar volúmenes
docker volume ls | grep batistella

# Verificar red
docker network ls | grep batistella
```

### Backup y Restauración
```bash
# Backup completo
./scripts/backup.sh

# Restauración
./scripts/restore.sh backup-file.tar.gz
``` 