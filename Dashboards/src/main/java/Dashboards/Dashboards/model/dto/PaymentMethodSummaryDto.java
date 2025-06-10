package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de pagos por método de pago")
public class PaymentMethodSummaryDto {
    
    @Schema(description = "Método de pago", example = "mercadopago")
    private String metodo;
    
    @Schema(description = "Cantidad de pagos con este método", example = "3")
    private Integer cantidad;
    
    @Schema(description = "Porcentaje del total", example = "60.0")
    private Double porcentaje;
    
    @Schema(description = "Monto total con este método", example = "11113.77")
    private Double montoTotal;
} 