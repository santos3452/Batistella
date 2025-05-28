FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar el archivo pom.xml para descargar dependencias
COPY pom.xml .
# Descargar dependencias (aprovechando caché de Docker)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn package -DskipTests

# Imagen final
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8084

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"] 