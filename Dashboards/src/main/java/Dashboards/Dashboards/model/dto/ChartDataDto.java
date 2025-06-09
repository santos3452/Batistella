package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para gráficos con etiqueta, valor y cantidad")
public class ChartDataDto {
    
    @Schema(description = "Etiqueta del punto de datos (fecha formateada)", example = "01-Jun")
    private String label;
    
    @Schema(description = "Valor de ventas para ese punto", example = "750.00")
    private Double value;
    
    @Schema(description = "Cantidad de pedidos/ventas realizadas ese día", example = "15")
    private Integer count;
} 