package Payments.Payments.config;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extraer solo el token, sin el prefijo "Bearer "
        String token = authHeader.substring(7);
        
        // Detectar y corregir "Bearer Bearer" duplicado
        if (token.trim().startsWith("Bearer ")) {
            token = token.trim().substring(7);
        }
        
        try {
            if (jwtUtil.validateToken(token)) {
                Collection<? extends GrantedAuthority> authorities = jwtUtil.extractRoles(token);
                
                // Guardar el token como credentials en el objeto Authentication
                // Principal: null (no tenemos un usuario específico)
                // Credentials: token (para poder accederlo después)
                // Authorities: roles extraídos del token
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            null, // principal (no extraemos username ya que no está implementado)
                            token, // credentials (token completo para accederlo después)
                            authorities // authorities
                        );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT válido procesado. Roles extraídos: {}", 
                        authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
            }
        } catch (Exception e) {
            log.error("Error al procesar el token JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
} 