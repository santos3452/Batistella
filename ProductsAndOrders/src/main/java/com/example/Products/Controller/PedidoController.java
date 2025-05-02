package com.example.Products.Controller;

import com.example.Products.Dtos.PedidosDto.CrearPedidoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoResponseDTO;
import com.example.Products.Service.PedidoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
} 