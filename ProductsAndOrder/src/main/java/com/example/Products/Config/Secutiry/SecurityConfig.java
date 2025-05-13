package com.example.Products.Config.Secutiry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configurando reglas de seguridad");
        
        http
            .cors().and()
            .csrf().disable()
            .authorizeHttpRequests(authorize -> {
                try {
                    // Rutas protegidas - SOLO el endpoint de pedidos por usuario
                    authorize.requestMatchers("/api/pedidos/usuario/**").authenticated()
                    // Todas las demás rutas son públicas
                    .anyRequest().permitAll();
                    
                    logger.info("Reglas de autorización configuradas");
                } catch (Exception e) {
                    logger.error("Error al configurar reglas de autorización", e);
                    throw new RuntimeException(e);
                }
            })
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions -> {
                exceptions.authenticationEntryPoint((request, response, authException) -> {
                    logger.warn("Acceso no autorizado: {} para URI: {}", authException.getMessage(), request.getRequestURI());
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"No autenticado\",\"message\":\"" + authException.getMessage() + "\"}");
                });
                
                exceptions.accessDeniedHandler((request, response, accessDeniedException) -> {
                    logger.warn("Acceso denegado: {} para URI: {}", accessDeniedException.getMessage(), request.getRequestURI());
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Acceso denegado\",\"message\":\"" + accessDeniedException.getMessage() + "\"}");
                });
            });

        logger.info("Configuración de seguridad completada");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configurando CORS");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 