package Payments.Payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

    @Value("${app.url.base}")
    private String appBaseUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                            "http://localhost:4200",  // Angular dev server
                            "http://localhost:3000",  // React dev server
                            "http://localhost:8080",  // Local Spring Boot
                            "https://www.batistellaycia.shop",  // Producción principal
                            "https://batistellaycia.shop",      // Producción sin www
                            appBaseUrl,               // Ngrok URL
                            "https://e20a-181-4-8-15.ngrok-free.app",  // URL actual de Ngrok
                            "https://api.mercadopago.com",             // Mercado Pago API
                            "https://www.mercadopago.com"              // Sitio de Mercado Pago
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
} 