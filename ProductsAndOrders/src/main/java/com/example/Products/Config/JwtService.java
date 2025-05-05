package com.example.Products.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para operaciones relacionadas con JWT
 * Facilita la extracción de información del usuario y la validación del token
 */
@Service
public class JwtService {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Extrae el ID del usuario del token JWT en el contexto de seguridad actual
     * @return ID del usuario o null si no está autenticado
     */
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) auth.getPrincipal();
            return userDetails.getUserId();
        }
        return null;
    }

    /**
     * Verifica si el ID de usuario solicitado coincide con el usuario autenticado
     * @param requestedUserId ID del usuario que se está solicitando
     * @return true si coincide, false en caso contrario
     */
    public boolean isUserAuthorized(Long requestedUserId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(requestedUserId);
    }

    /**
     * Extrae el ID de usuario directamente de un token JWT
     * @param token Token JWT (sin el prefijo "Bearer ")
     * @return ID del usuario o null si hay un error
     */
    public Long extractUserIdFromToken(String token) {
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene el rol del usuario autenticado actual
     * @return Rol del usuario o null si no está autenticado
     */
    public String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) auth.getPrincipal();
            return userDetails.getRole();
        }
        return null;
    }
} 