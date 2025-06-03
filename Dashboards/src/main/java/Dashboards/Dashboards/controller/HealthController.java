package Dashboards.Dashboards.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Sistema", description = "Endpoints del sistema y health checks")
public class HealthController {

    @Operation(
        summary = "Health Check",
        description = "Verifica el estado del microservicio de dashboards"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Servicio funcionando correctamente",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                {
                  "service": "Dashboards Service",
                  "status": "UP",
                  "version": "1.0.0"
                }
                """
            )
        )
    )
    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of(
            "service", "Dashboards Service",
            "status", "UP",
            "version", "1.0.0"
        );
    }
} 