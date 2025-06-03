package Dashboards.Dashboards.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDto {
    private Long id;
    private String codigoPedido;
    private Long usuarioId;
    private String nombreCompletoUsuario;
    private String email;
    private String fechaPedido;
    private String estado;
    private Double total;
    private String domicilio;
    private List<ProductoDto> productos;
    private String estadoPago;
    private String metodoPago;
    private String fechaPago;
    private String createdAt;
    private String updatedAt;
} 