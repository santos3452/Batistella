package com.example.Products.Dtos.PedidosDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoDTO {
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
    
    @NotEmpty(message = "Debe incluir al menos un producto en el pedido")
    @Valid
    private List<PedidoProductoDTO> productos;

    private String domicilioDeEtrega;
} 