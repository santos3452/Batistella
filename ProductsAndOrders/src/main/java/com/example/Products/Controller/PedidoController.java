package com.example.Products.Controller;

import com.example.Products.Dtos.Error.ErrorDto;
import com.example.Products.Dtos.PedidosDto.CrearPedidoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoResponseDTO;
import com.example.Products.Service.PedidoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody CrearPedidoDTO crearPedidoDTO) {
        PedidoResponseDTO response = pedidoService.crearPedido(crearPedidoDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPedidosPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.obtenerPedidosPorUsuario(usuarioId);
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al obtener pedidos: " + e.getMessage()
                    ));
        }
    }
} 