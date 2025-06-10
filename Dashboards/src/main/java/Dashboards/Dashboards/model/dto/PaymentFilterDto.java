package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros aplicados en la consulta")
public class PaymentFilterDto {
    
    @Schema(description = "Fecha desde", example = "2025-05-01")
    private String fechaDesde;
    
    @Schema(description = "Fecha hasta", example = "2025-05-31")
    private String fechaHasta;
} 