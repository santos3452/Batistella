-- Eliminar datos existentes
TRUNCATE TABLE usuarios;

-- Reiniciar la secuencia
ALTER SEQUENCE usuarios_id_seq RESTART WITH 1;

-- Insertar usuarios predefinidos solo si no existen
INSERT INTO usuarios (nombre, apellido, email, password, rol, tipo_usuario, activo)
SELECT 'Admin', 'Sistema', 'admin@sistema.com', '$2a$10$bvWRBZBdxKh4kFXh8EHh/.I7O26rwD7/djV6x11GAz3Y4yZmGealW', 'ROLE_ADMIN', 'FINAL', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@sistema.com');

INSERT INTO usuarios (nombre, apellido, email, password, rol, tipo_usuario, activo)
SELECT 'Empresa', 'Demo', 'empresa@ejemplo.com', '$2a$10$0T6LJDPf.U9Z.qCsVAMgq.Uq.CKuNYo6pjI8TFsmwHgZYe5Tabuoa', 'ROLE_EMPRESA', 'EMPRESA', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'empresa@ejemplo.com');

INSERT INTO usuarios (nombre, apellido, email, password, rol, tipo_usuario, activo)
SELECT 'Cliente', 'Demo', 'cliente@ejemplo.com', '$2a$10$aIGlQV5QfhbgXbTzkeU6qO9ARcWFkI86QwgwO1.cxRrg88XlTDZAG', 'ROLE_CLIENTE', 'FINAL', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'cliente@ejemplo.com');

INSERT INTO usuarios (nombre, apellido, email, password, rol, tipo_usuario, activo)
SELECT 'John', 'Doe', 'usuario@ejemplo.com', '$2b$12$HOOaBLmTsmUC462f0VwsZufGJfjKdDDA3/PnpdTJL5Ar1jR5xl6rO', 'ROLE_ADMIN', 'FINAL', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'usuario@ejemplo.com'); 