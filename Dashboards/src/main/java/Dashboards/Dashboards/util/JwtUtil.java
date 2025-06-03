package Dashboards.Dashboards.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        try {
            // Si secretKey no está en Base64, convertirla
            byte[] decodedKey = Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            // Si falla la decodificación Base64, usar la clave tal como está
            log.debug("Secret key no está en Base64, usando directamente");
            return Keys.hmacShaKeyFor(secretKey.getBytes());
        }
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error validando token JWT: {}", e.getMessage());
            throw new RuntimeException("Token inválido", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String extractUsername(String token) {
        return validateToken(token).getSubject();
    }

    public String extractRole(String token) {
        return validateToken(token).get("role", String.class);
    }

    public String extractName(String token) {
        return validateToken(token).get("name", String.class);
    }

    public Long extractUserId(String token) {
        return validateToken(token).get("userId", Long.class);
    }

    public boolean isAdmin(String token) {
        String role = extractRole(token);
        return "ROLE_ADMIN".equals(role);
    }
} 