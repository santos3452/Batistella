package com.example.Products.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:src/main/resources/static/images}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convertir ruta relativa a absoluta
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString();
        
        // Imprimir la ruta para depuración
        System.out.println("Configurando acceso a imágenes en: " + location);
        
        // Configurar el acceso a las imágenes que ya están en el directorio estático
        registry.addResourceHandler("/images/**")
                .addResourceLocations(
                    "classpath:/static/images/", 
                    location,
                    "file:/app/images/"  // Agregar la ruta específica para Docker
                )
                .setCachePeriod(0); // Desactivar caché para evitar problemas
        
        // Configurar el acceso a otros recursos estáticos
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Methods", 
                               "Access-Control-Allow-Headers", "Access-Control-Max-Age", 
                               "Access-Control-Request-Headers", "Access-Control-Request-Method")
                .maxAge(3600);
    }
} 