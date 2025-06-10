package Dashboards.Dashboards.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Dashboards.Dashboards.model.dto.SalesSummaryDto;
import Dashboards.Dashboards.model.dto.TopCustomersSummaryDto;
import Dashboards.Dashboards.model.dto.TopProductsSummaryDto;
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
    public ResponseEntity<?> getSalesSummary(
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
            
            // Convertir strings a LocalDate con validación
            LocalDate fromDate;
            LocalDate toDate;
            
            try {
                fromDate = LocalDate.parse(from);
            } catch (Exception e) {
                log.warn("Fecha 'from' inválida: {} - {}", from, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Fecha 'from' inválida: " + from + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            try {
                toDate = LocalDate.parse(to);
            } catch (Exception e) {
                log.warn("Fecha 'to' inválida: {} - {}", to, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Fecha 'to' inválida: " + to + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            // Validar fechas
            if (fromDate.isAfter(toDate)) {
                log.warn("Fechas inválidas: from {} es posterior a to {}", fromDate, toDate);
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "La fecha 'from' no puede ser posterior a la fecha 'to'."));
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
    
    @Operation(
        summary = "Obtener ranking de productos más vendidos",
        description = "Obtiene un ranking de productos ordenados por cantidad vendida para un rango de fechas específico. " +
                     "Incluye estadísticas detalladas de cada producto como cantidad vendida, número de pedidos, ingresos y precio promedio. " +
                     "**Solo accesible para usuarios administradores.**",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Ranking de productos obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TopProductsSummaryDto.class),
                examples = @ExampleObject(
                    name = "Ejemplo de respuesta",
                    value = """
                    {
                      "filtrosAplicados": {
                        "fechaDesde": "2025-01-01",
                        "fechaHasta": "2025-06-30"
                      },
                      "totalProductosUnicos": 25,
                      "totalUnidadesVendidas": 150,
                      "valorTotalVentas": 450000.00,
                      "topProductos": [
                        {
                          "productoId": 123,
                          "nombreProducto": "Smartphone Samsung Galaxy",
                          "cantidadTotalVendida": 45,
                          "numeroPedidos": 12,
                          "ingresosTotales": 125000.00,
                          "precioUnitarioPromedio": 2777.78
                        },
                        {
                          "productoId": 456,
                          "nombreProducto": "Auriculares Bluetooth",
                          "cantidadTotalVendida": 32,
                          "numeroPedidos": 8,
                          "ingresosTotales": 48000.00,
                          "precioUnitarioPromedio": 1500.00
                        }
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Parámetros de fecha inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token de autorización ausente o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado. Solo administradores pueden acceder",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/top-products")
    public ResponseEntity<?> getTopProducts(
            @Parameter(
                description = "Fecha de inicio del reporte (formato: YYYY-MM-DD)", 
                example = "2025-01-01",
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
            log.info("Generando ranking de productos más vendidos desde {} hasta {} para usuario: {}", 
                    from, to, request.getAttribute("userName"));
            
            // Convertir strings a LocalDate con validación
            LocalDate fromDate;
            LocalDate toDate;
            
            try {
                fromDate = LocalDate.parse(from);
            } catch (Exception e) {
                log.warn("Fecha 'from' inválida: {} - {}", from, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Fecha 'from' inválida: " + from + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            try {
                toDate = LocalDate.parse(to);
            } catch (Exception e) {
                log.warn("Fecha 'to' inválida: {} - {}", to, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Fecha 'to' inválida: " + to + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            // Validar fechas
            if (fromDate.isAfter(toDate)) {
                log.warn("Fechas inválidas: from {} es posterior a to {}", fromDate, toDate);
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "La fecha 'from' no puede ser posterior a la fecha 'to'."));
            }
            
            // Obtener token del header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Header de autorización inválido");
                return ResponseEntity.status(401).build();
            }
            
            TopProductsSummaryDto summary = salesDashboardService.getTopProducts(fromDate, toDate, authHeader);
            
            log.info("Ranking de productos generado exitosamente: {} productos únicos, {} unidades totales", 
                    summary.getTotalProductosUnicos(), summary.getTotalUnidadesVendidas());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error al generar ranking de productos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @Operation(
        summary = "Obtener ranking de clientes más frecuentes",
        description = "Obtiene un ranking de clientes ordenados por dinero gastado para un rango de fechas específico. " +
                     "Incluye estadísticas detalladas de cada cliente como cantidad de órdenes, dinero gastado, promedio por orden, y fechas de compras. " +
                     "**Solo accesible para usuarios administradores.**",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Ranking de clientes obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TopCustomersSummaryDto.class),
                examples = @ExampleObject(
                    name = "Ejemplo de respuesta",
                    value = """
                    {
                      "filtrosAplicados": {
                        "fechaDesde": "2025-01-01",
                        "fechaHasta": "2025-06-30"
                      },
                      "totalClientesUnicos": 45,
                      "totalOrdenesProcesadas": 230,
                      "ingresosTotales": 1250000.00,
                      "promedioOrdenesPorCliente": 5.11,
                      "promedioGastadoPorCliente": 27777.78,
                      "topClientes": [
                                                 {
                           "usuarioId": 123,
                           "nombreCompleto": "Juan Carlos Pérez",
                           "email": "juan.perez@email.com",
                           "cantidadOrdenes": 15,
                           "dineroTotalGastado": 125000.00,
                           "promedioGastadoPorOrden": 8333.33,
                           "cantidadProductosComprados": 87,
                           "primeraCompra": "2025-01-15",
                           "ultimaCompra": "2025-05-28"
                         },
                         {
                           "usuarioId": 456,
                           "nombreCompleto": "María González",
                           "email": "maria.gonzalez@email.com",
                           "cantidadOrdenes": 12,
                           "dineroTotalGastado": 98000.00,
                           "promedioGastadoPorOrden": 8166.67,
                           "cantidadProductosComprados": 64,
                           "primeraCompra": "2025-02-01",
                           "ultimaCompra": "2025-06-15"
                         }
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Parámetros de fecha inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token de autorización ausente o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado. Solo administradores pueden acceder",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/top-customers")
    public ResponseEntity<?> getTopCustomers(
            @Parameter(
                description = "Fecha de inicio del reporte (formato: YYYY-MM-DD)", 
                example = "2025-01-01",
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
            log.info("Generando ranking de clientes más frecuentes desde {} hasta {} para usuario: {}", 
                    from, to, request.getAttribute("userName"));
            
            // Convertir strings a LocalDate con validación
            LocalDate fromDate;
            LocalDate toDate;
            
            try {
                fromDate = LocalDate.parse(from);
            } catch (Exception e) {
                log.warn("Fecha 'from' inválida: {} - {}", from, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Fecha 'from' inválida: " + from + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            try {
                toDate = LocalDate.parse(to);
            } catch (Exception e) {
                log.warn("Fecha 'to' inválida: {} - {}", to, e.getMessage());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Fecha 'to' inválida: " + to + ". Use formato YYYY-MM-DD con fechas válidas."));
            }
            
            // Validar fechas
            if (fromDate.isAfter(toDate)) {
                log.warn("Fechas inválidas: from {} es posterior a to {}", fromDate, toDate);
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "La fecha 'from' no puede ser posterior a la fecha 'to'."));
            }
            
            // Obtener token del header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Header de autorización inválido");
                return ResponseEntity.status(401).build();
            }
            
            TopCustomersSummaryDto summary = salesDashboardService.getTopCustomers(fromDate, toDate, authHeader);
            
            log.info("Ranking de clientes generado exitosamente: {} clientes únicos, {} órdenes totales", 
                    summary.getTotalClientesUnicos(), summary.getTotalOrdenesProcesadas());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error al generar ranking de clientes: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
} 