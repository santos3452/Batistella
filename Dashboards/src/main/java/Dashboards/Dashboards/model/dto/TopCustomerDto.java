package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cliente en el ranking de más frecuentes")
public class TopCustomerDto {
    
    @Schema(description = "ID del usuario", example = "123")
    private Long usuarioId;
    
    @Schema(description = "Nombre completo del cliente", example = "Juan Carlos Pérez")
    private String nombreCompleto;
    
    @Schema(description = "Email del cliente", example = "juan.perez@email.com")
    private String email;
    
    @Schema(description = "Cantidad total de órdenes realizadas", example = "15")
    private Integer cantidadOrdenes;
    
    @Schema(description = "Dinero total gastado por el cliente", example = "125000.00")
    private Double dineroTotalGastado;
    
    @Schema(description = "Promedio gastado por orden", example = "8333.33")
    private Double promedioGastadoPorOrden;
    
    @Schema(description = "Cantidad total de productos (unidades) comprados", example = "87")
    private Integer cantidadProductosComprados;
    
    @Schema(description = "Fecha de la primera compra", example = "2025-01-15")
    private String primeraCompra;
    
    @Schema(description = "Fecha de la última compra", example = "2025-05-28")
    private String ultimaCompra;
} 