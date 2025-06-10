package Dashboards.Dashboards.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de error")
public class ErrorResponseDto {
    
    @Schema(description = "Mensaje de error", example = "Fecha 'from' inválida: 2025-06-31. Use formato YYYY-MM-DD con fechas válidas.")
    private String error;
} 