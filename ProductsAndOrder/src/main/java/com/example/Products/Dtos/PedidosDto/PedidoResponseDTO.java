package com.example.Products.Dtos.PedidosDto;

import com.example.Products.Entity.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {
    private Long id;
    private String codigoPedido;
    private Long usuarioId;
    private String nombreCompletoUsuario;
    private String email;
    private LocalDateTime fechaPedido;
    private EstadoPedido estado;
    private BigDecimal total;
    private Integer costoEnvio;
    private String domicilio;
    private List<PedidoProductoResponseDTO> productos;
    private String estadoPago;
    private String metodoPago;
    private LocalDateTime fechaPago;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 