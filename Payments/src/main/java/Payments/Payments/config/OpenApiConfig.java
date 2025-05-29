package Payments.Payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Pagos - Batistella")
                        .description("API para gestionar pagos de Batistella Shop")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Batistella Team")
                                .email("contacto@batistella.com"))
                        .license(new License()
                                .name("Licencia privada")
                                .url("https://batistella.com/terms")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("Introduce el token JWT con el prefijo Bearer"))
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("Introduce el token JWT con el prefijo Bearer")));
    }
} 