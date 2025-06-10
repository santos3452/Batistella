package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Método de pago más usado")
public class MostUsedPaymentMethodDto {
    
    @Schema(description = "Método de pago más usado", example = "mercadopago")
    private String metodo;
    
    @Schema(description = "Cantidad de usos", example = "3")
    private Integer cantidad;
    
    @Schema(description = "Porcentaje de uso", example = "60.0")
    private Double porcentaje;
} 