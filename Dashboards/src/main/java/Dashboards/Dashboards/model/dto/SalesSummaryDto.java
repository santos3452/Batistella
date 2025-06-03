package Dashboards.Dashboards.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen completo de ventas con indicadores y datos para gráficos")
public class SalesSummaryDto {
    
    @Schema(description = "Número total de pedidos en el rango de fechas", example = "135")
    private Integer totalOrders;
    
    @Schema(description = "Monto total vendido en el período", example = "25400.50")
    private Double totalRevenue;
    
    @Schema(description = "Promedio de venta por pedido", example = "188.15")
    private Double averagePerOrder;
    
    @Schema(description = "Datos agrupados por día para generar gráficos")
    private List<ChartDataDto> chartData;
} 