package com.example.Products.Controller;

import com.example.Products.Config.Secutiry.JwtService;
import com.example.Products.Dtos.Error.ErrorDto;
import com.example.Products.Dtos.PedidosDto.CrearPedidoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoResponseDTO;
import com.example.Products.Service.PedidoService;
import com.example.Products.Entity.enums.EstadoPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    
    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private JwtService jwtService;

    @PostMapping
    @Operation(
        summary = "Crear un nuevo pedido", 
        description = "Crea un nuevo pedido. El usuario solo puede crear pedidos para sí mismo, mientras que los administradores pueden crear pedidos para cualquier usuario.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<?> crearPedido(@Valid @RequestBody CrearPedidoDTO crearPedidoDTO) {
        logger.info("==== Solicitud recibida: crearPedido para usuarioId: " + crearPedidoDTO.getUsuarioId() + " ====");
        
        try {
            // Verificar que el usuario autenticado coincida con el usuario para el que se crea el pedido o sea admin
            Long currentUserId = jwtService.getCurrentUserId();
            boolean isAdmin = jwtService.isAdmin();

            if (!jwtService.isUserAuthorizedOrAdmin(crearPedidoDTO.getUsuarioId())) {
                logger.warn("ACCESO DENEGADO: El usuario autenticado no puede crear pedidos para otro usuario");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ErrorDto.of(
                                HttpStatus.FORBIDDEN.value(),
                                "Acceso denegado",
                                "No tienes permiso para crear pedidos para este usuario"
                        ));
            }
            
            PedidoResponseDTO response = pedidoService.crearPedido(crearPedidoDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Error al procesar el pedido: " + e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorDto.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error en los datos del pedido",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Error interno al procesar el pedido: " + e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al crear el pedido: " + e.getMessage()
                    ));
        }
    }
    
    @GetMapping("/usuario/{usuarioId}")
    @Operation(
        summary = "Obtener pedidos por ID de usuario", 
        description = "Obtiene todos los pedidos realizados por un usuario específico. El usuario solo puede ver sus propios pedidos, mientras que los administradores pueden ver los pedidos de cualquier usuario.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )

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
            // Verificar que el usuario autenticado coincida con el ID solicitado o sea admin
            Long currentUserId = jwtService.getCurrentUserId();
            boolean isAdmin = jwtService.isAdmin();
            logger.info("ID de usuario autenticado: " + currentUserId);
            logger.info("ID de usuario solicitado: " + usuarioId);
            logger.info("¿Es administrador?: " + (isAdmin ? "SÍ" : "NO"));
            
            if (!jwtService.isUserAuthorizedOrAdmin(usuarioId)) {
                logger.warn("ACCESO DENEGADO: El usuario autenticado no tiene permisos para ver estos pedidos");
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

    @GetMapping("/todosLosPedidos")
    @Operation(
            summary = "Obtener todos los pedidos",
            description = "Obtiene todos los pedidos en el sistema con posibilidad de filtros y paginación. Solo accesible para administradores.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<?> getAllPedidos(
            @Parameter(description = "Código de pedido (opcional, búsqueda parcial).")
            @RequestParam(value = "codigoPedido", required = false) String codigoPedido,
            
            @Parameter(description = "Fecha del pedido en formato DD/MM/AAAA o DD/MM/AAAA HH:MM:SS (opcional).")
            @RequestParam(value = "fecha", required = false) String fechaStr,
            
            @Parameter(description = "Estado del pedido (opcional).")
            @RequestParam(value = "estado", required = false) EstadoPedido estado,
            
            @Parameter(description = "Cantidad de datos por página. Valor por defecto: 50")
            @RequestParam(value = "cantDatos", required = false, defaultValue = "50") Integer cantDatos,
            
            @Parameter(description = "Número de página. Valor por defecto: 1")
            @RequestParam(value = "pagina", required = false, defaultValue = "1") Integer pagina
    ) {
        logger.info("==== Solicitud recibida: getAllPedidos ====");
        
        try {
            String role = jwtService.getCurrentUserRole();
            boolean isAdmin = jwtService.isAdmin();
            
            if (!isAdmin) {
                logger.warn("ACCESO DENEGADO: El usuario no tiene rol de administrador");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ErrorDto.of(
                                HttpStatus.FORBIDDEN.value(),
                                "Acceso denegado",
                                "Solo los administradores pueden acceder a esta función"
                        ));
            }
            
            // Convertir la fecha si se proporciona
            LocalDateTime fechaDateTime = null;
            if (fechaStr != null && !fechaStr.isEmpty()) {
                try {
                    // Intentar primero con formato DD/MM/AAAA HH:MM:SS
                    DateTimeFormatter formatterConHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    try {
                        fechaDateTime = LocalDateTime.parse(fechaStr, formatterConHora);
                    } catch (DateTimeParseException e) {
                        // Si falla, intentar con formato DD/MM/AAAA (sin hora)
                        DateTimeFormatter formatterSinHora = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate fechaDate = LocalDate.parse(fechaStr, formatterSinHora);
                        // Convertir a LocalDateTime con hora 00:00:00
                        fechaDateTime = fechaDate.atStartOfDay();
                    }
                } catch (Exception e) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ErrorDto.of(
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Formato de fecha incorrecto",
                                    "El formato de fecha debe ser DD/MM/AAAA o DD/MM/AAAA HH:MM:SS"
                            ));
                }
            }
            
            List<PedidoResponseDTO> pedidos = pedidoService.obtenerTodosLosPedidos(codigoPedido, fechaDateTime, estado, cantDatos, pagina);
            
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            logger.error("Error al procesar la solicitud", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al obtener todos los pedidos: " + e.getMessage()
                    ));
        }
    }
    @PutMapping("/actualizarEstado/")
    @Operation(
            summary = "Actualizar Estado de pedido",
            description = "Actualiza el estado de pedido, solo lo peude hacer un administrador.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )

    public ResponseEntity<?> updateEstado(@RequestParam Long pedidoId, @RequestParam String nuevoEstado) {


        try {

            String role = jwtService.getCurrentUserRole();
            boolean isAdmin = jwtService.isAdmin();



            if (!isAdmin) {
                logger.warn("ACCESO DENEGADO: El usuario no tiene rol de administrador");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ErrorDto.of(
                                HttpStatus.FORBIDDEN.value(),
                                "Acceso denegado",
                                "Solo los administradores pueden acceder a esta función"
                        ));
            }


             pedidoService.cambiarEstadoPedido(pedidoId, nuevoEstado);


            return ResponseEntity.ok("Estado del pedido actualizado correctamente");
        } catch (Exception e) {
            logger.error("Error al procesar la solicitud", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al actualizar el pedido: " + e.getMessage()
                    ));
        }
    }

    
    @GetMapping("/codigo/{codigoPedido}")
    @Operation(
        summary = "Buscar pedido por código", 
        description = "Busca un pedido específico por su código único generado automáticamente. El pedido puede ser consultado por su propietario o por un administrador.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<?> buscarPorCodigoPedido(@PathVariable String codigoPedido) {
        logger.info("==== Solicitud recibida: buscarPorCodigoPedido: " + codigoPedido + " ====");
        
        try {
            Optional<PedidoResponseDTO> pedidoOpt = pedidoService.buscarPorCodigoPedido(codigoPedido);
            
            if (pedidoOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ErrorDto.of(
                                HttpStatus.NOT_FOUND.value(),
                                "Pedido no encontrado",
                                "No se encontró un pedido con el código: " + codigoPedido
                        ));
            }
            
            PedidoResponseDTO pedido = pedidoOpt.get();
            
            // Verificar que el usuario autenticado tiene permisos para ver este pedido
            // Se permite acceso si es el propietario O si tiene rol de admin
            if (!jwtService.isUserAuthorizedOrAdmin(pedido.getUsuarioId())) {
                logger.warn("ACCESO DENEGADO: El usuario autenticado no tiene permisos para ver este pedido");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ErrorDto.of(
                                HttpStatus.FORBIDDEN.value(),
                                "Acceso denegado",
                                "No tienes permiso para acceder a este pedido"
                        ));
            }
            
            return ResponseEntity.ok(pedido);
            
        } catch (Exception e) {
            logger.error("Error al procesar la solicitud", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error Interno del Servidor",
                            "Error al buscar el pedido: " + e.getMessage()
                    ));
        }
    }
} 