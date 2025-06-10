package Dashboards.Dashboards.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen completo de productos más vendidos")
public class TopProductsSummaryDto {
    
    @Schema(description = "Filtros aplicados en la consulta")
    private PaymentFilterDto filtrosAplicados;
    
    @Schema(description = "Número total de productos únicos vendidos", example = "25")
    private Integer totalProductosUnicos;
    
    @Schema(description = "Cantidad total de unidades vendidas", example = "150")
    private Integer totalUnidadesVendidas;
    
    @Schema(description = "Valor total de ventas de productos", example = "450000.00")
    private Double valorTotalVentas;
    
    @Schema(description = "Lista de productos ordenados por cantidad vendida (mayor a menor)")
    private List<TopProductDto> topProductos;
} 