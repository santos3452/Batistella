package Dashboards.Dashboards.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Dashboards.Dashboards.model.dto.SalesSummaryDto;
import Dashboards.Dashboards.service.SalesDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard de Ventas", description = "Endpoints para obtener reportes y estadísticas de ventas")
public class SalesDashboardController {

    private final SalesDashboardService salesDashboardService;

    @Operation(
        summary = "Obtener resumen de ventas",
        description = "Obtiene un resumen completo de ventas para un rango de fechas específico. " +
                     "Incluye total de pedidos, monto total vendido, promedio por pedido y datos para gráficos. " +
                     "**Solo accesible para usuarios administradores.**",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Resumen de ventas obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SalesSummaryDto.class),
                examples = @ExampleObject(
                    name = "Ejemplo de respuesta",
                    value = """
                    {
                      "totalOrders": 135,
                      "totalRevenue": 25400.50,
                      "averagePerOrder": 188.15,
                      "chartData": [
                        { "label": "01-Jun", "value": 750.00, "count": 5 },
                        { "label": "02-Jun", "value": 320.00, "count": 3 },
                        { "label": "03-Jun", "value": 1200.00, "count": 8 }
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Parámetros de fecha inválidos (fecha 'from' posterior a 'to')",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token de autorización ausente o inválido",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Token de autorización requerido\"}")
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado. Solo administradores pueden acceder",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Acceso denegado. Solo administradores pueden acceder a este recurso\"}")
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor o error de comunicación con microservicios",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/sales-summary")
    public ResponseEntity<SalesSummaryDto> getSalesSummary(
            @Parameter(
                description = "Fecha de inicio del reporte (formato: YYYY-MM-DD)", 
                example = "2025-06-01",
                required = true
            )
            @RequestParam String from,
            
            @Parameter(
                description = "Fecha de fin del reporte (formato: YYYY-MM-DD)", 
                example = "2025-06-30",
                required = true
            )
            @RequestParam String to,
            
            HttpServletRequest request) {
        
        try {
            log.info("Generando resumen de ventas desde {} hasta {} para usuario: {}", 
                    from, to, request.getAttribute("userName"));
            
            // Convertir strings a LocalDate
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);
            
            // Validar fechas
            if (fromDate.isAfter(toDate)) {
                log.warn("Fechas inválidas: from {} es posterior a to {}", fromDate, toDate);
                return ResponseEntity.badRequest().build();
            }
            
            // Obtener token del header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Header de autorización inválido");
                return ResponseEntity.status(401).build();
            }
            
            SalesSummaryDto summary = salesDashboardService.getSalesSummary(fromDate, toDate, authHeader);
            
            log.info("Resumen de ventas generado exitosamente: {} pedidos, ${} total", 
                    summary.getTotalOrders(), summary.getTotalRevenue());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error al generar resumen de ventas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
} 