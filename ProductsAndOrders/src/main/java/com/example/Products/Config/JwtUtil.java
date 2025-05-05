package com.example.Products.Config;

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

import java.nio.charset.StandardCharsets;
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
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Obtener todos los claims del token
    private Claims extractAllClaims(String token) {
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
} 