package com.example.Products.Service;

import com.example.Products.Dtos.PedidosDto.CrearPedidoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoResponseDTO;

public interface PedidoService {
    PedidoResponseDTO crearPedido(CrearPedidoDTO crearPedidoDTO);
} 