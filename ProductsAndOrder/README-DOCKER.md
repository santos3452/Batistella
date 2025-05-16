# Dockerización de Products and Order

Este documento explica cómo dockerizar y ejecutar la aplicación Products and Order.

## Requisitos

- Docker y Docker Compose instalados
- Base de datos PostgreSQL ejecutándose en el puerto 5434
- Microservicio de usuarios ejecutándose en el puerto 8081

## Configuración con variables de entorno

La aplicación utiliza variables de entorno para configurar distintos aspectos como la conexión a la base de datos y al microservicio de usuarios.

### Archivo .env

Se proporciona un archivo `env-example` como plantilla. Para comenzar:

1. Copia el archivo de ejemplo a un nuevo archivo llamado `.env`:
   ```bash
   cp env-example .env
   ```

2. Edita el archivo `.env` según tus necesidades, especialmente:
   - Configuración de la base de datos (DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD)
   - URL del microservicio de usuarios (USER_SERVICE_URL)
   - Token de autenticación (USER_SERVICE_TOKEN)

Las variables definidas en el archivo `.env` se utilizarán automáticamente en el `docker-compose.yml` y se pasarán a la aplicación.

### Variables de entorno principales

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| SPRING_PROFILES_ACTIVE | Perfil de Spring activo | prod |
| APP_BASE_URL | URL base de la aplicación | http://localhost:8083 |
| DB_HOST | Host de la base de datos | host.docker.internal |
| DB_PORT | Puerto de la base de datos | 5434 |
| DB_NAME | Nombre de la base de datos | ProductsAndOrders |
| USER_SERVICE_URL | URL del microservicio de usuarios | http://host.docker.internal:8081/api/users |

## Construir y ejecutar con Docker Compose

Para construir y ejecutar la aplicación con Docker Compose:

```bash
# Construir la imagen
docker-compose build

# Ejecutar la aplicación
docker-compose up -d
```

Para detener la aplicación:

```bash
docker-compose down
```

## Construir y ejecutar manualmente

Si prefieres construir y ejecutar la aplicación manualmente, necesitarás crear primero el archivo `.env` y luego ejecutar:

```bash
# Cargar variables de entorno
export $(cat .env | grep -v '^#' | xargs)

# Construir la imagen
docker build -t products-and-order .

# Ejecutar el contenedor
docker run -d --name products-and-order \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE \
  -e APP_BASE_URL=$APP_BASE_URL \
  -e JWT_SECRET=$JWT_SECRET \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME \
  -e SPRING_DATASOURCE_USERNAME=$DB_USERNAME \
  -e SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
  -e MICROSERVICE_USUARIOS_URL=$USER_SERVICE_URL \
  -e TOKEN=$USER_SERVICE_TOKEN \
  --volume product-images:/app/images \
  products-and-order
```

## Verificar la aplicación

Una vez que la aplicación esté en ejecución, puedes acceder a ella en:

- API: http://localhost:8083/api
- Swagger UI: http://localhost:8083/swagger-ui.html

## Solución de problemas

### Conexión a la base de datos

Si tienes problemas para conectarte a la base de datos, verifica:

1. Que la base de datos PostgreSQL esté ejecutándose en el puerto especificado en `.env` (por defecto 5434)
2. Que la base de datos especificada en `.env` exista
3. Que las credenciales en `.env` sean correctas

### Conexión al microservicio de usuarios

Asegúrate de que el microservicio de usuarios esté ejecutándose y accesible desde el contenedor Docker. La URL está configurada en la variable `USER_SERVICE_URL` en el archivo `.env`. 