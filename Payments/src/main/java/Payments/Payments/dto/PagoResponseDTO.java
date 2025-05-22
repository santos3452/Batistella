package Payments.Payments.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {
    private Long id;
    private Long pedidoId;
    private BigDecimal monto;
    private String metodo;
    private LocalDateTime fechaPago;
    private String estado;
    private String mercadoPagoPreferenceId;
    private String urlPago;
} 