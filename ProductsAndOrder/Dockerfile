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

# Configurar zona horaria para Argentina
RUN apt-get update && apt-get install -y tzdata
ENV TZ=America/Buenos_Aires
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Directorio para imágenes
# RUN mkdir -p /app/images
# El directorio se montará como un volumen, así que no es necesario crearlo aquí

# Copiar el JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8083

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-Duser.timezone=America/Buenos_Aires", "-jar", "app.jar"] 