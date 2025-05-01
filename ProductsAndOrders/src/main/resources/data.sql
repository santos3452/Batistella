-- Inserción de marcas
INSERT INTO marca (nombre) VALUES ('TopNutrition') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Kenl') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Odwalla') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('9Lives') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Amici') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Zimpi') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('ProPlan') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Ganacat') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Ganacan') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Compinches') ON CONFLICT DO NOTHING;
INSERT INTO marca (nombre) VALUES ('Exact') ON CONFLICT DO NOTHING;


-- Inserción de productos
INSERT INTO productos (marca, tipo_alimento, tipo_raza, description, kg, price_minorista, price_mayorista, stock, image_url, animal_type, activo, created_at, updated_at)
VALUES
    ('TOPNUTRITION', 'ADULTO', 'RAZA_GRANDE', 'Alimento balanceado TopNutrition para perros adultos de raza grande', 'FIFTEEN_KG', 25000.00, 22000.00, 100, 'http://localhost:8083/images/TopNutrition/TopADULTORAZAMEDIANA.webp', 'PERROS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('KENL', 'ADULTO', 'RAZA_GRANDE', 'Alimento balanceado KenL para perros adultos de raza grande', 'FIFTEEN_KG', 25000.00, 22000.00, 100, 'http://localhost:8083/images/Kenl/KenlADULTORAZAGRANDE.webp', 'PERROS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;