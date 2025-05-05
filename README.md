# BATISTELLA Y CIA.

## Introducción

**BATISTELLA Y CIA.** es una **distribuidora de comida balanceada** y este proyecto consiste en el desarrollo de una **plataforma tipo e-commerce** que permitirá la compra de alimento balanceado tanto para **perros, gatos** como para **animales de granja**.

El sistema está orientado tanto a **usuarios finales** como a **empresas**, ofreciendo beneficios exclusivos para estas últimas, como **descuentos por compras en grandes cantidades**. Además, contará con **integración con Mercado Pago** para facilitar las compras y con **seguimiento de envíos** para mejorar la experiencia postventa.

El proyecto se realiza como parte de una tesis académica y busca digitalizar y optimizar el proceso comercial de la empresa, brindando un sistema moderno, seguro y escalable.

## Arquitectura

El sistema se estructura en **microservicios**, entre los cuales se encuentran:

- **Servicio de Autenticación:** Maneja el login y la generación/validación de tokens JWT.
- **Servicios de Dominio:** Gestionan productos, usuarios, compras, pagos y envíos.
- **Base de datos:** Persistencia en PostgreSQL, utilizada por cada microservicio.

## 🔐 Seguridad

- Autenticación basada en **JWT**
- Control de acceso con **Spring Security**
- Roles diferenciados: usuario común y empresa
- Permisos por operación (por ejemplo, acceso a compras por volumen para empresas)

##  Tecnologías utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Security**
- **JWT**
- **PostgreSQL**
- **Maven**
- **Swagger**
- **H2** (para entorno de desarrollo)
- **Mercado Pago API** (integración de pagos)



