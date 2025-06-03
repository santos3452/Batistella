-- Script de inicialización para crear todas las bases de datos de Batistella
-- Se ejecuta automáticamente cuando PostgreSQL inicia por primera vez

-- Crear base de datos para Users and Login
CREATE DATABASE "UsersBatistella_db";

-- Crear base de datos para Products and Orders  
CREATE DATABASE "ProductsAndOrders";

-- Crear base de datos para Payments
CREATE DATABASE "Payments";

-- Crear base de datos para Notifications
CREATE DATABASE "notification_db";

-- Otorgar todos los permisos al usuario postgres en todas las bases
GRANT ALL PRIVILEGES ON DATABASE "UsersBatistella_db" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ProductsAndOrders" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "Payments" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "notification_db" TO postgres;

-- Configurar extensiones útiles para cada base de datos
\c "UsersBatistella_db";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c "ProductsAndOrders";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c "Payments";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c "notification_db";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Volver a la base principal
\c postgres; 