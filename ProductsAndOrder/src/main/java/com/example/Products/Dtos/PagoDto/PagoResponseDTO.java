package com.example.Products.Dtos.PagoDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {
    private String codigoPedido;
    private String metodo;
    private LocalDateTime fechaPago;
    private String estado;
} 