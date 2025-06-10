package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Producto en el ranking de más vendidos")
public class TopProductDto {
    
    @Schema(description = "ID del producto", example = "123")
    private Long productoId;
    
    @Schema(description = "Nombre del producto", example = "Smartphone Samsung Galaxy")
    private String nombreProducto;
    
    @Schema(description = "Cantidad total vendida", example = "45")
    private Integer cantidadTotalVendida;
    
    @Schema(description = "Número de pedidos que incluyen este producto", example = "12")
    private Integer numeroPedidos;
    
    @Schema(description = "Ingresos totales generados por este producto", example = "125000.00")
    private Double ingresosTotales;
    
    @Schema(description = "Precio unitario promedio", example = "2777.78")
    private Double precioUnitarioPromedio;
} 