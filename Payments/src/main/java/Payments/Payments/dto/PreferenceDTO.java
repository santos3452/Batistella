package Payments.Payments.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceDTO {
    private String codigoPedido;
    private BigDecimal montoTotal;
    private String descripcion;
    private List<ItemDTO> items;
    private String urlExito;
    private String urlFracaso;
    private String urlPendiente;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {
        private String titulo;
        private String descripcion;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}