package com.example.UsersAndLogin.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir solicitudes desde cualquier origen durante desarrollo
        config.addAllowedOriginPattern("*");
        
        // También permitir explícitamente los orígenes que sabemos que se utilizan
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("https://www.batistellaycia.shop");
        config.addAllowedOrigin("https://batistellaycia.shop");
        config.addAllowedOrigin("http://localhost:51369");
        config.addAllowedOrigin("http://localhost:8090");
        
        // Permitir cualquier cabecera
        config.addAllowedHeader("*");
        
        // Permitir todos los métodos HTTP (GET, POST, PUT, DELETE, etc.)
        config.addAllowedMethod("*");
        
        // Permitir credenciales como cookies
        config.setAllowCredentials(true);
        
        // Exponer cabeceras necesarias
        config.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Tiempo máximo de cache para preflight
        config.setMaxAge(3600L);
        
        // Aplicar esta configuración a todas las rutas
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 