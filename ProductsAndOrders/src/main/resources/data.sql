

-- Inserción de marcas
INSERT INTO marca (nombre) VALUES ('TopNutrition');
INSERT INTO marca (nombre) VALUES ('Kenl');
INSERT INTO marca (nombre) VALUES ('Odwalla');
INSERT INTO marca (nombre) VALUES ('9Lives');
INSERT INTO marca (nombre) VALUES ('Amici');
INSERT INTO marca (nombre) VALUES ('Zimpi');
INSERT INTO marca (nombre) VALUES ('ProPlan');
INSERT INTO marca (nombre) VALUES ('Ganacat');
INSERT INTO marca (nombre) VALUES ('Ganacan');
INSERT INTO marca (nombre) VALUES ('Compinches');
INSERT INTO marca (nombre) VALUES ('Exact');


-- Example of correct insert statement
-- Example of correct insert statement
INSERT INTO productos (marca, tipo_alimento, tipo_raza, description, kg, price_minorista, price_mayorista, stock, image_url, animal_type, activo)
VALUES
    ('TOPNUTRITION', 'ADULTO', 'RAZA_GRANDE', 'Alimento balanceado TopNutrition para perros adultos de raza grande', 'FIFTEEN_KG', 25000.00, 22000.00, 100, 'http://localhost:8083/images/TopNutrition/TopADULTORAZAMEDIANA.webp', 'PERROS', true),
    ('KENL', 'ADULTO', 'RAZA_GRANDE', 'Alimento balanceado KenL para perros adultos de raza grande', 'FIFTEEN_KG', 25000.00, 22000.00, 100, 'http://localhost:8083/images/Kenl/KenlADULTORAZAGRANDE.webp', 'PERROS', true);