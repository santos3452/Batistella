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
        if (auth == null) {
            System.out.println("DEBUG - No hay autenticación en el contexto de seguridad");
            return null;
        }
        
        System.out.println("DEBUG - Tipo de autenticación: " + auth.getClass().getName());
        System.out.println("DEBUG - Principal: " + (auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null"));
        System.out.println("DEBUG - Autoridades: " + auth.getAuthorities());
        
        if (auth.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) auth.getPrincipal();
            String role = userDetails.getRole();
            System.out.println("DEBUG - Rol extraído del JwtUserDetails: " + role);
            return role;
        }
        
        // Si el principal no es JwtUserDetails, intentamos extraer el rol de las autoridades
        if (!auth.getAuthorities().isEmpty()) {
            String authority = auth.getAuthorities().iterator().next().getAuthority();
            System.out.println("DEBUG - Autoridad extraída: " + authority);
            return authority;
        }
        
        System.out.println("DEBUG - No se pudo extraer el rol");
        return null;
    }

    /**
     * Verifica si el usuario autenticado tiene rol de administrador
     * @return true si el usuario tiene rol "admin" o "ROLE_ADMIN", false en caso contrario
     */
    public boolean isAdmin() {
        String role = getCurrentUserRole();
        return role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("ROLE_ADMIN"));
    }
    
    /**
     * Verifica si el usuario está autorizado para acceder a recursos de otro usuario
     * Un usuario está autorizado si:
     * 1. Es el mismo usuario (su ID coincide con el solicitado)
     * 2. Tiene rol de administrador
     * 
     * @param requestedUserId ID del usuario que se está solicitando
     * @return true si está autorizado, false en caso contrario
     */
    public boolean isUserAuthorizedOrAdmin(Long requestedUserId) {
        return isUserAuthorized(requestedUserId) || isAdmin();
    }
} 