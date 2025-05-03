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
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            
            // Eliminar "bearer" adicional si existe (para manejar el caso "Bearer bearer ...")
            if (token.toLowerCase().startsWith("bearer ")) {
                logger.info("Detectado formato 'Bearer bearer' - Corrigiendo formato del token");
                token = token.substring(7).trim();
            }
            
            return token;
        }
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
            
            boolean tokenExpirado = jwtUtil.isTokenExpired(token);
            logger.info("¿Token expirado? " + (tokenExpirado ? "SÍ" : "NO"));
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && !tokenExpirado) {
                // Crear objeto de usuario principal con la información del token
                JwtUserDetails userDetails = new JwtUserDetails(username, userId);
                logger.info("Creando objeto JwtUserDetails: " + username + ", userId: " + userId);
                
                // Crear token de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
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
        }
    }
} 