# Configuración de Ngrok para Mercado Pago

Este documento explica cómo configurar Ngrok para trabajar con Mercado Pago en un entorno de desarrollo local.

## ¿Por qué necesitamos Ngrok?

Mercado Pago no permite URLs de redirección usando `localhost`. Cuando el usuario completa un pago, Mercado Pago necesita redireccionar a una URL pública, por lo que necesitamos exponer nuestra aplicación local a internet.

## Pasos para configurar Ngrok

1. **Instala Ngrok**:
   - Descarga Ngrok desde [https://ngrok.com/download](https://ngrok.com/download)
   - O instálalo a través de npm: `npm install -g ngrok`
   - O instálalo a través de chocolatey (Windows): `choco install ngrok`

2. **Regístrate en Ngrok**:
   - Crea una cuenta gratuita en [https://ngrok.com](https://ngrok.com)
   - Obtén tu token de autenticación desde el dashboard

3. **Autentica tu cliente de Ngrok**:
   ```
   ngrok config add-authtoken TU_TOKEN_DE_NGROK
   ```

4. **Inicia el túnel de Ngrok**:
   ```
   ngrok http 8080
   ```

5. **Configura la URL de Ngrok en la aplicación**:
   - Ngrok te proporcionará una URL pública (e.j. `https://1234abcd.ngrok.io`)
   - Actualiza la propiedad `app.url.base` en `application.properties` con esta URL:
     ```
     app.url.base=https://1234abcd.ngrok.io
     ```

6. **Configura las URLs en Mercado Pago**:
   - En tu panel de desarrollador de Mercado Pago, configura las URLs de notificación (webhooks)
   - Utiliza la URL de Ngrok para los webhooks: `https://1234abcd.ngrok.io/api/pagos/webhooks/mercadopago`

## Importante:

- La URL de Ngrok cambia cada vez que inicias el servicio, a menos que tengas una cuenta de pago
- Si reinicias Ngrok, necesitarás actualizar todas las URLs en tu aplicación y en Mercado Pago
- Para desarrollo local, recomendamos usar el entorno de pruebas (Sandbox) de Mercado Pago

## Alternativas a Ngrok:

- **Localtunnel**: `npm install -g localtunnel`
- **cloudflared**: [https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/install-and-setup/tunnel-guide/](https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/install-and-setup/tunnel-guide/)
- **Configurar un servidor de desarrollo**: Si tienes acceso a un servidor con IP pública 