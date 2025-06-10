package Dashboards.Dashboards.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Dashboards.Dashboards.model.dto.PaymentsSummaryDto;
import Dashboards.Dashboards.service.PaymentsDashboardService;
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
@Tag(name = "Dashboard de Pagos", description = "Endpoints para obtener reportes y estadísticas de pagos")
public class PaymentsDashboardController {

    private final PaymentsDashboardService paymentsDashboardService;

    @Operation(
        summary = "Obtener resumen de pagos",
        description = "Obtiene un resumen completo de pagos para un rango de fechas específico. " +
                     "Incluye total de pagos, monto total pagado, método más usado, y análisis por método y estado. " +
                     "**Solo accesible para usuarios administradores.**",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Resumen de pagos obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentsSummaryDto.class),
                examples = @ExampleObject(
                    name = "Ejemplo de respuesta",
                    value = """
                    {
                      "filtrosAplicados": {
                        "fechaDesde": "2025-05-01",
                        "fechaHasta": "2025-05-31"
                      },
                      "totalPagos": 5,
                      "totalMontoPagado": 11113.77,
                      "medioPagoMasUsado": {
                        "metodo": "mercadopago",
                        "cantidad": 3,
                        "porcentaje": 60.0
                      },
                      "resumenPorMetodo": [
                        {
                          "metodo": "mercadopago",
                          "cantidad": 3,
                          "porcentaje": 60.0,
                          "montoTotal": 11113.77
                        },
                        {
                          "metodo": "Transferencia",
                          "cantidad": 1,
                          "porcentaje": 20.0,
                          "montoTotal": 4500.0
                        }
                      ],
                      "resumenPorEstado": [
                        {
                          "estado": "COMPLETADO",
                          "cantidad": 3,
                          "porcentaje": 60.0
                        },
                        {
                          "estado": "CANCELADO",
                          "cantidad": 1,
                          "porcentaje": 20.0
                        },
                        {
                          "estado": "PENDIENTE",
                          "cantidad": 1,
                          "porcentaje": 20.0
                        }
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
    @GetMapping("/payments-summary")
    public ResponseEntity<?> getPaymentsSummary(
            @Parameter(
                description = "Fecha de inicio del reporte (formato: YYYY-MM-DD)", 
                example = "2025-05-01",
                required = true
            )
            @RequestParam String from,
            
            @Parameter(
                description = "Fecha de fin del reporte (formato: YYYY-MM-DD)", 
                example = "2025-05-31",
                required = true
            )
            @RequestParam String to,
            
            HttpServletRequest request) {
        
        try {
            log.info("Generando resumen de pagos desde {} hasta {} para usuario: {}", 
                    from, to, request.getAttribute("userName"));
            
            // Convertir strings a LocalDate con validación
            LocalDate fromDate;
            LocalDate toDate;
            
            try {
                fromDate = LocalDate.parse(from);
            } catch (Exception e) {
                log.warn("Fecha 'from' inválida: {} - {}", from, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Fecha 'from' inválida: " + from + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            try {
                toDate = LocalDate.parse(to);
            } catch (Exception e) {
                log.warn("Fecha 'to' inválida: {} - {}", to, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Fecha 'to' inválida: " + to + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            // Validar fechas
            if (fromDate.isAfter(toDate)) {
                log.warn("Fechas inválidas: from {} es posterior a to {}", fromDate, toDate);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La fecha 'from' no puede ser posterior a la fecha 'to'."));
            }
            
            // Obtener token del header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Header de autorización inválido");
                return ResponseEntity.status(401).build();
            }
            
            PaymentsSummaryDto summary = paymentsDashboardService.getPaymentsSummary(fromDate, toDate, authHeader);
            
            log.info("Resumen de pagos generado exitosamente: {} pagos, ${} total", 
                    summary.getTotalPagos(), summary.getTotalMontoPagado());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error al generar resumen de pagos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
} 