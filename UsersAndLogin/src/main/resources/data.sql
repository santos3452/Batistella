-- Eliminar datos existentes
TRUNCATE TABLE usuarios;
ALTER TABLE usuarios ALTER COLUMN id RESTART WITH 1;

-- Crear usuarios predefinidos (las contraseñas ya están encriptadas con BCrypt)
-- contraseña: admin123
INSERT INTO usuarios (nombre, apellido, email, password, rol, tipo_usuario)
VALUES ('Admin', 'Sistema', 'admin@sistema.com', '$2a$10$bvWRBZBdxKh4kFXh8EHh/.I7O26rwD7/djV6x11GAz3Y4yZmGealW', 'ROLE_ADMIN', 'FINAL');

-- contraseña: empresa123
INSERT INTO usuarios (nombre, apellido, email, password, rol, tipo_usuario)
VALUES ('Empresa', 'Demo', 'empresa@ejemplo.com', '$2a$10$0T6LJDPf.U9Z.qCsVAMgq.Uq.CKuNYo6pjI8TFsmwHgZYe5Tabuoa', 'ROLE_EMPRESA', 'EMPRESA');

-- contraseña: cliente123
INSERT INTO usuarios (nombre, apellido, email, password, rol, tipo_usuario)
VALUES ('Cliente', 'Demo', 'cliente@ejemplo.com', '$2a$10$aIGlQV5QfhbgXbTzkeU6qO9ARcWFkI86QwgwO1.cxRrg88XlTDZAG', 'ROLE_CLIENTE', 'FINAL'); 