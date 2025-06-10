package Dashboards.Dashboards.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen completo de pagos con estadísticas y análisis")
public class PaymentsSummaryDto {
    
    @Schema(description = "Filtros aplicados en la consulta")
    private PaymentFilterDto filtrosAplicados;
    
    @Schema(description = "Número total de pagos en el rango de fechas", example = "5")
    private Integer totalPagos;
    
    @Schema(description = "Monto total pagado en el período", example = "11113.77")
    private Double totalMontoPagado;
    
    @Schema(description = "Método de pago más usado")
    private MostUsedPaymentMethodDto medioPagoMasUsado;
    
    @Schema(description = "Resumen detallado por método de pago")
    private List<PaymentMethodSummaryDto> resumenPorMetodo;
    
    @Schema(description = "Resumen detallado por estado de pago")
    private List<PaymentStatusSummaryDto> resumenPorEstado;
} 