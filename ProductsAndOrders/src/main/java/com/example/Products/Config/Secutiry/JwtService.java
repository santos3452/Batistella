package com.example.Products.Config.Secutiry;

import com.example.Products.Dtos.UsuarioDto.UsuarioDTO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio centralizado para operaciones relacionadas con JWT
 * Facilita la extracción de información del usuario y la validación del token
 */
@Service
public class JwtService {



    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservice.usuarios.url}")
    private String usuariosServiceUrl;
    
    // Caché para almacenar los nombres completos de los usuarios
    private ConcurrentHashMap<Long, String> userFullNameCache = new ConcurrentHashMap<>();

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
     * Extrae y almacena en caché el nombre completo de un usuario a partir de su token JWT
     * @param token Token JWT del usuario
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public boolean cacheUserFullName(String token) {
        try {
            // Obtener y registrar todos los claims para depuración
            Claims claims = jwtUtil.extractAllClaims(token);
            System.out.println("DEBUG - Claims extraídos del token: " + claims);
            
            Long userId = jwtUtil.extractUserId(token);
            System.out.println("DEBUG - UserId extraído: " + userId);
            
            String name = jwtUtil.extractName(token);
            System.out.println("DEBUG - Name extraído: " + name);
            
            String lastname = jwtUtil.extractLastname(token);
            System.out.println("DEBUG - Lastname extraído: " + lastname);
            
            if (userId != null) {
                if (name != null || lastname != null) {
                    String fullName = "";
                    if (name != null) {
                        fullName += name;
                    }
                    if (lastname != null) {
                        if (!fullName.isEmpty()) {
                            fullName += " ";
                        }
                        fullName += lastname;
                    }
                    
                    System.out.println("DEBUG - Nombre completo construido: " + fullName);
                    userFullNameCache.put(userId, fullName);
                    return true;
                } else {
                    System.out.println("DEBUG - No se encontraron claims de nombre o apellido");
                }
            } else {
                System.out.println("DEBUG - No se pudo extraer el ID de usuario");
            }
            return false;
        } catch (Exception e) {
            System.out.println("DEBUG - Error al procesar el token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el nombre completo de un usuario desde la caché
     * Si no está en caché, intenta obtenerlo del microservicio de usuarios
     * @param userId ID del usuario
     * @return Nombre completo o "Usuario {userId}" si no se puede obtener
     */
    public String getUserFullNameFromCache(Long userId) {
        // Primero intentamos obtener el nombre de la caché
        String nombreUsuario = userFullNameCache.get(userId);

        // Si no está en caché, intentamos obtenerlo del microservicio de usuarios
        if (nombreUsuario == null) {
            try {
                String url = usuariosServiceUrl + "/" + userId;


                UsuarioDTO usuario = restTemplate.getForObject(url, UsuarioDTO.class);

                if (usuario != null) {
                    nombreUsuario = usuario.getNombreCompleto();
                    // Guardamos en caché para futuras consultas
                    userFullNameCache.put(userId, nombreUsuario);

                } else {

                    nombreUsuario = "Usuario " + userId;
                }
            } catch (RestClientException e) {

                nombreUsuario = "Usuario " + userId;
            }
        }

        return nombreUsuario;
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