# Documentación del Sistema de Autenticación JWT

## Descripción General

Este microservicio implementa la validación de tokens JWT (JSON Web Tokens) emitidos por el microservicio de usuarios. El sistema permite:

1. Validar la autenticidad y vigencia de los tokens
2. Extraer el ID de usuario desde el token
3. Restringir el acceso a recursos específicos basado en la identidad del usuario

## Componentes Principales

### JwtUtil

Clase utilitaria que proporciona funciones básicas para el manejo de JWT:
- Extracción de claims (username, userId, fecha de expiración)
- Validación de tokens
- Verificación de fechas de expiración

### JwtUserDetails

Implementación de `UserDetails` de Spring Security que almacena información relevante del usuario:
- Username (email)
- ID del usuario
- Roles/permisos

### JwtRequestFilter

Filtro que intercepta todas las peticiones HTTP para:
- Extraer y validar el token JWT del encabezado "Authorization"
- Autenticar al usuario si el token es válido
- Establecer la autenticación en el contexto de seguridad de Spring

### JwtService

Servicio centralizado para operaciones relacionadas con JWT que facilita:
- Obtener el ID del usuario autenticado actual
- Verificar la autorización del usuario para acciones específicas
- Extraer información del token

## Configuración

La clave secreta para la firma y validación de los tokens se configura en `application.properties`:

```properties
jwt.secret=${JWT_SECRET:valor_por_defecto}
```

## Flujo de Autenticación

1. El microservicio de usuarios genera un token JWT con el ID del usuario como claim
2. El cliente envía peticiones al microservicio de productos/pedidos con el token en el encabezado "Authorization"
3. El `JwtRequestFilter` intercepta la petición, extrae y valida el token
4. Si el token es válido, el filtro establece la autenticación en el contexto de seguridad
5. Los controladores pueden verificar si el usuario está autorizado para acceder a recursos específicos

## Ejemplo de Uso en Controladores

```java
@GetMapping("/usuario/{usuarioId}")
public ResponseEntity<?> obtenerPedidosPorUsuario(@PathVariable Long usuarioId) {
    // Verificar que el usuario autenticado coincida con el ID solicitado
    if (!jwtService.isUserAuthorized(usuarioId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
    }
    
    // El usuario está autorizado, procesar la solicitud
    return ResponseEntity.ok(pedidoService.obtenerPedidosPorUsuario(usuarioId));
}
```

## Migración a API Gateway

Este diseño modular facilita la migración futura a un API Gateway:

1. Simplemente se deshabilitaría la configuración de seguridad en el microservicio
2. Se eliminaría la verificación de autenticación en los controladores
3. El API Gateway se encargaría de la validación de tokens y propagación de la identidad del usuario 