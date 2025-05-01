-- Crear tabla de marcas si no existe
CREATE TABLE IF NOT EXISTS marca (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

-- Crear tabla de productos si no existe
CREATE TABLE IF NOT EXISTS productos (
    id SERIAL PRIMARY KEY,
    marca VARCHAR(50) NOT NULL,
    tipo_alimento VARCHAR(20) NOT NULL,
    tipo_raza VARCHAR(20),
    description VARCHAR(255) NOT NULL,
    kg VARCHAR(50) NOT NULL,
    price_minorista DECIMAL(10,2) NOT NULL,
    price_mayorista DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_url VARCHAR(255),
    animal_type VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true
);

