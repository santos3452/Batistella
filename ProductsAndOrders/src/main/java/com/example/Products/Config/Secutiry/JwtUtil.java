package com.example.Products.Config.Secutiry;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;
    
    private Key key;
    
    @PostConstruct
    public void init() {
        try {
            logger.info("Inicializando clave JWT a partir de la clave secreta");
            byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes());
            key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("Clave JWT inicializada correctamente");
        } catch (Exception e) {
            logger.error("Error al inicializar la clave JWT", e);
            throw new RuntimeException("No se pudo inicializar la clave JWT", e);
        }
    }

    // Extraer username del token
    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.info("Username extraído del token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Error al extraer username del token", e);
            return null;
        }
    }

    // Extraer fecha de expiración
    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.error("Error al extraer fecha de expiración", e);
            return new Date(); // Retorna fecha actual para que isTokenExpired retorne true
        }
    }

    // Extraer información (claims) del token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            logger.info("Extrayendo claim específico de: {}", claims);
            T result = claimsResolver.apply(claims);
            logger.info("Resultado de la extracción del claim: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error al extraer claim del token: {}", e.getMessage());
            return null;
        }
    }

    // Obtener todos los claims del token
    public Claims extractAllClaims(String token) {
        try {
            logger.info("Decodificando token JWT");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.info("Claims extraídos del token: {}", claims);
            return claims;
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            logger.error("Error al decodificar token JWT: {}", e.getMessage());
            throw e;
        }
    }

    // Verificar si el token ha expirado
    public Boolean isTokenExpired(String token) {
        try {
            boolean expired = extractExpiration(token).before(new Date());
            logger.info("¿Token expirado? {}", expired);
            return expired;
        } catch (Exception e) {
            logger.error("Error al verificar expiración del token", e);
            return true; // Si hay error, asumimos que el token está expirado
        }
    }

    // Validar token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        logger.info("Validación de token para usuario {}: {}", userDetails.getUsername(), isValid);
        return isValid;
    }
    
    // Extraer ID de usuario del token (suponiendo que el ID se guarda en el claim "userId")
    public Long extractUserId(String token) {
        try {
            Long userId = extractClaim(token, claims -> claims.get("userId", Long.class));
            logger.info("UserId extraído del token: {}", userId);
            return userId;
        } catch (Exception e) {
            logger.error("Error al extraer userId del token", e);
            return null;
        }
    }

    // Extraer rol del token
    public String extractRole(String token) {
        try {
            String role = extractClaim(token, claims -> claims.get("role", String.class));
            logger.info("Rol extraído del token: {}", role);
            return role;
        } catch (Exception e) {
            logger.error("Error al extraer rol del token", e);
            return null;
        }
    }

    // Extraer nombre del token
    public String extractName(String token) {
        try {
            // Intenta extraer el claim "name"
            String name = extractClaim(token, claims -> claims.get("name", String.class));
            if (name == null) {
                // Si no existe "name", intenta extraer "nombre" (por si acaso está en español)
                name = extractClaim(token, claims -> claims.get("nombre", String.class));
            }
            logger.info("Nombre extraído del token: {}", name);
            return name;
        } catch (Exception e) {
            logger.error("Error al extraer nombre del token", e);
            return null;
        }
    }

    // Extraer apellido del token
    public String extractLastname(String token) {
        try {
            // Intenta extraer el claim "lastname"
            String lastname = extractClaim(token, claims -> claims.get("lastname", String.class));
            if (lastname == null) {
                // Si no existe "lastname", intenta extraer "apellido" (por si acaso está en español)
                lastname = extractClaim(token, claims -> claims.get("apellido", String.class));
            }
            logger.info("Apellido extraído del token: {}", lastname);
            return lastname;
        } catch (Exception e) {
            logger.error("Error al extraer apellido del token", e);
            return null;
        }
    }
    
    // Extraer nombre completo del token
    public String extractFullName(String token) {
        try {
            String name = extractName(token);
            String lastname = extractLastname(token);
            
            if (name != null && lastname != null) {
                return name + " " + lastname;
            } else if (name != null) {
                return name;
            } else if (lastname != null) {
                return lastname;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error al extraer nombre completo del token", e);
            return null;
        }
    }
} 