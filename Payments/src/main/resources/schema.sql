-- Script de creación de la base de datos para el sistema de pagos

-- Crear la tabla de pagos
CREATE TABLE IF NOT EXISTS pagos (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo VARCHAR(50) NOT NULL,
    fecha_pago TIMESTAMP,
    estado VARCHAR(20) NOT NULL,
    mp_payment_id VARCHAR(100),
    mp_preference_id VARCHAR(100),
    mp_status VARCHAR(50),
    mp_status_detail VARCHAR(100)
);

-- Crear índices para mejorar el rendimiento de las búsquedas comunes
CREATE INDEX IF NOT EXISTS idx_pagos_pedido_id ON pagos(pedido_id);
CREATE INDEX IF NOT EXISTS idx_pagos_estado ON pagos(estado);
CREATE INDEX IF NOT EXISTS idx_pagos_mp_payment_id ON pagos(mp_payment_id);
CREATE INDEX IF NOT EXISTS idx_pagos_mp_preference_id ON pagos(mp_preference_id);

-- Comentarios de la tabla
COMMENT ON TABLE pagos IS 'Tabla que almacena los registros de pagos del sistema';
COMMENT ON COLUMN pagos.id IS 'Identificador único del pago';
COMMENT ON COLUMN pagos.pedido_id IS 'ID del pedido asociado al pago';
COMMENT ON COLUMN pagos.monto IS 'Monto del pago';
COMMENT ON COLUMN pagos.metodo IS 'Método de pago utilizado (mercadopago, efectivo, transferencia, etc.)';
COMMENT ON COLUMN pagos.fecha_pago IS 'Fecha y hora en que se realizó el pago';
COMMENT ON COLUMN pagos.estado IS 'Estado del pago (PENDIENTE, COMPLETADO, RECHAZADO, EN_PROCESO)';
COMMENT ON COLUMN pagos.mp_payment_id IS 'ID de pago de Mercado Pago (si aplica)';
COMMENT ON COLUMN pagos.mp_preference_id IS 'ID de preferencia de Mercado Pago (si aplica)';
COMMENT ON COLUMN pagos.mp_status IS 'Estado del pago en Mercado Pago';
COMMENT ON COLUMN pagos.mp_status_detail IS 'Detalle adicional del estado en Mercado Pago'; 