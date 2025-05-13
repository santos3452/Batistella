package com.example.Products.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Configuración del esquema de seguridad para JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Requerimiento de seguridad global
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // Crear un ejemplo de ProductDTO
        Example productExample = new Example();
        productExample.setValue("""
            {
              "name": "TopNutritionRAZAGRANDCE",
              "description": "string",
              "kg": 17,
              "priceMinorista": 18000.00,
              "priceMayorista": 0.00,
              "stock": 20,
              "imageUrl": "string",
              "type": "PERROS",
              "activo": true
            }
            """);

        // Crear el componente de ejemplo
        Components components = new Components()
            .addExamples("productExample", productExample)
            .addSecuritySchemes("bearerAuth", securityScheme);

        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("Products API")
                        .version("1.0")
                        .description("""
                            API documentation for the Products application.
                            
                            Example Product JSON:
                            ```json
                            {
                              "name": "TopNutritionRAZAGRANDCE",
                              "description": "string",
                              "kg": 17,
                              "priceMinorista": 18000.00,
                              "priceMayorista": 0.00,
                              "stock": 20,
                              "imageUrl": "string",
                              "type": "PERROS",
                              "activo": true
                            }
                            ```
                            
                            ## Autenticación
                            Para usar los endpoints protegidos, haz clic en el botón "Authorize" e ingresa tu JWT token con el formato:
                            ```
                            Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                            ```
                            """));
    }
}