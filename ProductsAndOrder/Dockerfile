FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar los archivos pom.xml y descargar dependencias
COPY pom.xml .
COPY src ./src

# Compilar la aplicación (saltando tests)
RUN mvn clean package -DskipTests

# Crear imagen de ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app

# Directorio para imágenes
RUN mkdir -p /app/images

# Copiar el JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8083

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"] 