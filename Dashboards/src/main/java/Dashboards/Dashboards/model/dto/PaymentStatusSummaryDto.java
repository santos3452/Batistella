package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de pagos por estado")
public class PaymentStatusSummaryDto {
    
    @Schema(description = "Estado del pago", example = "COMPLETADO")
    private String estado;
    
    @Schema(description = "Cantidad de pagos con este estado", example = "3")
    private Integer cantidad;
    
    @Schema(description = "Porcentaje del total", example = "60.0")
    private Double porcentaje;
} 