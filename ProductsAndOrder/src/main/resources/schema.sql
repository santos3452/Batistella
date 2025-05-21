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
    price_minorista DECIMAL(12,2) NOT NULL,
    price_mayorista DECIMAL(12,2) NOT NULL,
    stock INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_url VARCHAR(255),
    animal_type VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true
);

-- Crear tabla de pedidos si no existe
CREATE TABLE IF NOT EXISTS pedidos (
    id SERIAL PRIMARY KEY,
    codigo_pedido VARCHAR(20) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    fecha_pedido TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de pedidos_productos si no existe
CREATE TABLE IF NOT EXISTS pedidos_productos (
    id SERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);

