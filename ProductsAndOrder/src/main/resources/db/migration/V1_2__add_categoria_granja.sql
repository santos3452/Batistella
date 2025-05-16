-- Crear el tipo enum categoria_granja si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'categoria_granja') THEN
        CREATE TYPE categoria_granja AS ENUM (
            'AVES',
            'PONEDORAS', 
            'CONEJOS', 
            'PORCINOS', 
            'EQUINOS', 
            'VACUNOS', 
            'VARIOS', 
            'CEREAL'
        );
    END IF;
END $$;

-- Agregar la columna nombre si no existe
ALTER TABLE productos 
ADD COLUMN IF NOT EXISTS nombre VARCHAR(255);

-- Agregar la columna categoria_granja si no existe
ALTER TABLE productos 
ADD COLUMN IF NOT EXISTS categoria_granja categoria_granja;

-- Hacer que la columna marca sea opcional
ALTER TABLE productos 
ALTER COLUMN marca DROP NOT NULL;

-- Hacer que la columna tipo_alimento sea opcional
ALTER TABLE productos 
ALTER COLUMN tipo_alimento DROP NOT NULL; 