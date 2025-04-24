-- Crear tabla de marcas
CREATE TABLE IF NOT EXISTS marca (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

-- Crear tabla de productos
CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(50) NOT NULL,
    tipo_alimento VARCHAR(20) NOT NULL,
    tipo_raza VARCHAR(20),
    description VARCHAR(255) NOT NULL,
    kg VARCHAR(50) NOT NULL,
    price_minorista DECIMAL(10,2) NOT NULL,
    price_mayorista DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL,
    image_url VARCHAR(255),
    animal_type VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true
    );

