package com.example.Products.Controller;

import com.example.Products.Config.JwtService;
import com.example.Products.Dtos.Error.ErrorDto;
import com.example.Products.Dtos.PedidosDto.CrearPedidoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoResponseDTO;
import com.example.Products.Service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    
    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private JwtService jwtService;

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido")
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody CrearPedidoDTO crearPedidoDTO) {
        PedidoResponseDTO response = pedidoService.crearPedido(crearPedidoDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    @Operation(
        summary = "Obtener pedidos por ID de usuario", 
        description = "Obtiene todos los pedidos realizados por un usuario específico. El usuario solo puede ver sus propios pedidos.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedidos encontrados correctamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - No puedes ver pedidos de otros usuarios",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<?> obtenerPedidosPorUsuario(@PathVariable Long usuarioId) {
        logger.info("==== Solicitud recibida: obtenerPedidosPorUsuario para ID: " + usuarioId + " ====");
        
        // Verificar estado de autenticación
        logger.info("Estado de autenticación: " + 
            (SecurityContextHolder.getContext().getAuthentication() != null ? "AUTENTICADO" : "NO AUTENTICADO"));
        
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.info("Detalles de autenticación - Principal: " + 
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getName());
            logger.info("Detalles de autenticación - Autoridades: " + 
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        }
        
        try {
            // Verificar que el usuario autenticado coincida con el ID solicitado
            Long currentUserId = jwtService.getCurrentUserId();
            logger.info("ID de usuario autenticado: " + currentUserId);
            logger.info("ID de usuario solicitado: " + usuarioId);
            
            if (!jwtService.isUserAuthorized(usuarioId)) {
                logger.warn("ACCESO DENEGADO: El usuario autenticado no coincide con el ID solicitado");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ErrorDto.of(
                                HttpStatus.FORBIDDEN.value(),
                                "Acceso denegado",
                                "No tienes permiso para acceder a los pedidos de este usuario"
                        ));
            }
            
            logger.info("Usuario autorizado, obteniendo pedidos...");
            List<PedidoResponseDTO> pedidos = pedidoService.obtenerPedidosPorUsuario(usuarioId);
            logger.info("Se encontraron " + pedidos.size() + " pedidos");
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            logger.error("Error al procesar la solicitud", e);
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