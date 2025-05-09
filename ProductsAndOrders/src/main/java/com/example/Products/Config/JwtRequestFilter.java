package com.example.Products.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        logger.info("==== JwtRequestFilter iniciando para: " + request.getRequestURI() + " ====");
        
        try {
            // Extraer token del header si existe
            String token = extractTokenFromRequest(request);
            logger.info("Authorization header: " + request.getHeader("Authorization"));
            
            if (token != null) {
                logger.info("Token JWT extraído: " + token);
                
                // Procesar el token y autenticar al usuario
                authenticateUser(token, request);
                
                // Verificar si la autenticación se estableció correctamente
                logger.info("¿Se estableció la autenticación? " + 
                    (SecurityContextHolder.getContext().getAuthentication() != null ? "SÍ" : "NO"));
                
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    if (principal instanceof JwtUserDetails) {
                        JwtUserDetails userDetails = (JwtUserDetails) principal;
                        logger.info("Usuario autenticado: " + userDetails.getUsername() + 
                            ", UserId: " + userDetails.getUserId());
                    }
                }
            } else {
                logger.info("No se encontró token JWT en la solicitud");
            }
        } catch (Exception e) {
            logger.error("Error al procesar token JWT", e);
            // No se lanza excepción para permitir que la solicitud continúe sin autenticación
        }

        // Continuar con la cadena de filtros
        logger.info("==== Continuando con la cadena de filtros ====");
        chain.doFilter(request, response);
    }
    
    /**
     * Extrae el token JWT del encabezado Authorization
     * Maneja el caso especial de un "bearer" adicional en el token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        logger.info("Authorization header original: " + authorizationHeader);
        
        if (authorizationHeader != null) {
            String token = authorizationHeader;
            
            // Eliminar "Bearer " si existe
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
                logger.info("Token después de eliminar 'Bearer ': " + token);
            }
            
            // Eliminar otro posible "bearer " adicional
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
                logger.info("Token después de eliminar segundo 'bearer ': " + token);
            }
            
            // Verificar si es un token JWT válido (debería comenzar con "eyJ")
            if (token.startsWith("eyJ")) {
                logger.info("Token JWT válido extraído: " + token);
                return token;
            } else {
                logger.warn("Token extraído no parece ser un JWT válido: " + token);
            }
        }
        
        logger.info("No se encontró token JWT válido en la solicitud");
        return null;
    }
    
    /**
     * Autentica al usuario basado en el token JWT
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            String username = jwtUtil.extractUsername(token);
            logger.info("Username extraído del token: " + username);
            
            Long userId = jwtUtil.extractUserId(token);
            logger.info("UserId extraído del token: " + userId);
            
            String name = jwtUtil.extractName(token);
            logger.info("Name extraído del token: " + name);
            
            String lastname = jwtUtil.extractLastname(token);
            logger.info("Lastname extraído del token: " + lastname);
            
            boolean tokenExpirado = jwtUtil.isTokenExpired(token);
            logger.info("¿Token expirado? " + (tokenExpirado ? "SÍ" : "NO"));
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && !tokenExpirado) {
                // Extraer el rol del token
                String role = jwtUtil.extractRole(token);
                logger.info("Rol extraído del token: " + role);
                
                // Guardar el nombre completo en caché
                boolean cached = jwtService.cacheUserFullName(token);
                logger.info("Nombre completo guardado en caché: " + (cached ? "SÍ" : "NO"));
                
                // Para depuración, verificamos si el nombre se guardó correctamente
                if (userId != null) {
                    String cachedName = jwtService.getUserFullNameFromCache(userId);
                    logger.info("Nombre completo en caché para userId " + userId + ": " + cachedName);
                }
                
                // Crear objeto de usuario principal con la información del token
                JwtUserDetails userDetails = new JwtUserDetails(username, userId, role);
                logger.info("Creando objeto JwtUserDetails: " + username + ", userId: " + userId + ", role: " + role);
                
                // Crear token de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Establecer autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Autenticación establecida en el contexto de seguridad");
            } else {
                logger.info("No se estableció la autenticación: username=" + username + 
                    ", auth existente=" + (SecurityContextHolder.getContext().getAuthentication() != null) + 
                    ", token expirado=" + tokenExpirado);
            }
        } catch (Exception e) {
            logger.error("Error durante la autenticación del usuario", e);
            e.printStackTrace();
        }
    }
} 