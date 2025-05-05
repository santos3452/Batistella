package com.example.Products.Service;

import com.example.Products.Dtos.PedidosDto.CrearPedidoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoResponseDTO;

import java.util.List;

public interface PedidoService {
    PedidoResponseDTO crearPedido(CrearPedidoDTO crearPedidoDTO);
    List<PedidoResponseDTO> obtenerPedidosPorUsuario(Long usuarioId);
} 