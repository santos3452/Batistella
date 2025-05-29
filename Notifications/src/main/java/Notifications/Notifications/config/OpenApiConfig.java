package Notifications.Notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Notificaciones - Batistella")
                        .description("API REST para el servicio de notificaciones de Batistella")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo Batistella")
                                .email("no.reply.batistella@gmail.com"))
                        .license(new License()
                                .name("Licencia Privada")
                                .url("https://batistella.com/terms")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación del Microservicio de Notificaciones")
                        .url("https://batistella.com/docs/notifications"));
    }
} 