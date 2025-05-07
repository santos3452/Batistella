-- Eliminar tablas si existen
DROP TABLE IF EXISTS domicilio;
DROP TABLE IF EXISTS usuarios;

-- Crear tabla de usuarios
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(255) NOT NULL,
                          apellido VARCHAR(255) NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          rol VARCHAR(50) NOT NULL,
                          tipo_usuario VARCHAR(50),
                          activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Crear tabla de domicilio
CREATE TABLE domicilio (
                           id BIGSERIAL PRIMARY KEY,
                           calle VARCHAR(255) NOT NULL,
                           numero VARCHAR(255) NOT NULL,
                           ciudad VARCHAR(255) NOT NULL,
                           codigo_postal INT NOT NULL,
                           usuario_id BIGINT NOT NULL,
                           CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);