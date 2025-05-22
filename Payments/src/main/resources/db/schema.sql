-- Esquema de base de datos para el microservicio de pagos
-- Este script crea las tablas necesarias en la base de datos PostgreSQL 'payments'

-- Tabla de pagos
CREATE TABLE IF NOT EXISTS pagos (
    id SERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    monto DECIMAL(12, 2) NOT NULL,
    metodo VARCHAR(50) NOT NULL,
    fecha_pago TIMESTAMP,
    estado VARCHAR(20) NOT NULL,
    mp_payment_id VARCHAR(100),
    mp_preference_id VARCHAR(100),
    mp_status VARCHAR(50),
    mp_status_detail VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_pagos_pedido_id ON pagos(pedido_id);
CREATE INDEX IF NOT EXISTS idx_pagos_estado ON pagos(estado);
CREATE INDEX IF NOT EXISTS idx_pagos_mp_payment_id ON pagos(mp_payment_id);
CREATE INDEX IF NOT EXISTS idx_pagos_mp_preference_id ON pagos(mp_preference_id);

-- Tabla de histórico de pagos (para auditoría)
CREATE TABLE IF NOT EXISTS pagos_historico (
    id SERIAL PRIMARY KEY,
    pago_id BIGINT NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20) NOT NULL,
    mp_status_anterior VARCHAR(50),
    mp_status_nuevo VARCHAR(50),
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(100) DEFAULT 'system',
    observaciones TEXT
);

-- Crear índice para la tabla histórica
CREATE INDEX IF NOT EXISTS idx_pagos_historico_pago_id ON pagos_historico(pago_id);

-- Función para actualizar el timestamp de última actualización
CREATE OR REPLACE FUNCTION update_ultima_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.ultima_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar el timestamp automáticamente
DROP TRIGGER IF EXISTS update_pagos_ultima_actualizacion ON pagos;
CREATE TRIGGER update_pagos_ultima_actualizacion
BEFORE UPDATE ON pagos
FOR EACH ROW
EXECUTE FUNCTION update_ultima_actualizacion();

-- Función y trigger para registrar cambios de estado en el histórico
CREATE OR REPLACE FUNCTION registrar_cambio_estado_pago()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.estado <> NEW.estado OR OLD.mp_status <> NEW.mp_status OR 
       (OLD.mp_status IS NULL AND NEW.mp_status IS NOT NULL) THEN
        INSERT INTO pagos_historico (
            pago_id, 
            estado_anterior, 
            estado_nuevo, 
            mp_status_anterior, 
            mp_status_nuevo
        ) VALUES (
            NEW.id,
            OLD.estado,
            NEW.estado,
            OLD.mp_status,
            NEW.mp_status
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_registrar_cambio_estado_pago ON pagos;
CREATE TRIGGER trg_registrar_cambio_estado_pago
AFTER UPDATE ON pagos
FOR EACH ROW
EXECUTE FUNCTION registrar_cambio_estado_pago();

-- Comentarios de tabla
COMMENT ON TABLE pagos IS 'Almacena todos los pagos procesados por el sistema';
COMMENT ON TABLE pagos_historico IS 'Registro histórico de cambios de estado de los pagos para auditoría'; 