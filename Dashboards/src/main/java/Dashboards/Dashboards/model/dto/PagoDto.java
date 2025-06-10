package Dashboards.Dashboards.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoDto {
    private Long id;
    private String codigoPedido;
    private Double monto;
    private String metodo;
    private String fechaPago;
    private String estado;
    private String mercadoPagoPreferenceId;
    private String urlPago;
} 