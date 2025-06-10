package Payments.Payments.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Payments.Payments.dto.PagoResponseDTO;
import Payments.Payments.dto.PreferenceDTO;
import Payments.Payments.exception.PagoNotFoundException;
import Payments.Payments.model.Pago;
import Payments.Payments.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagos", description = "API para gestionar pagos")
public class PagoController {

    private final PagoService pagoService;
    
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;
    
    @Value("${app.url.base}")
    private String appBaseUrl;
    
    // Mapa para almacenar tokens por código de pedido
    private final Map<String, String> tokensPorPedido = new ConcurrentHashMap<>();
    
    // Mapa estático para acceso desde otras clases
    private static final Map<String, String> tokensPorPedidoStatic = new ConcurrentHashMap<>();
    
    // Método estático para acceder a los tokens desde otras clases
    public static String getTokenForPedidoStatic(String codigoPedido) {
        return tokensPorPedidoStatic.get(codigoPedido);
    }
    
    /**
     * Crea una preferencia de pago con Mercado Pago
     */
    @Operation(
        summary = "Crear preferencia de pago con Mercado Pago",
        description = "Crea una preferencia de pago en Mercado Pago para un pedido",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preferencia creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado - Se requiere autenticación"),
        @ApiResponse(responseCode = "500", description = "Error al crear la preferencia")
    })
    @PostMapping("/mercadopago/preferencia")
    public ResponseEntity<PagoResponseDTO> crearPreferencia(
            @Parameter(description = "Datos para crear la preferencia de pago") 
            @RequestBody PreferenceDTO preferenceDTO,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Creando preferencia de pago para pedido: {}", preferenceDTO.getCodigoPedido());
        
        // Extraer el token y guardarlo en el mapa
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            tokensPorPedido.put(preferenceDTO.getCodigoPedido(), token);
            // También guardar en el mapa estático
            tokensPorPedidoStatic.put(preferenceDTO.getCodigoPedido(), token);
            log.info("Token guardado para el pedido: {}", preferenceDTO.getCodigoPedido());
            
            // Mantener la lógica para establecer el token en el SecurityContext
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if (currentAuth == null || currentAuth.getCredentials() == null) {
                UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(null, token, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        
        PagoResponseDTO response = pagoService.crearPreferenciaMercadoPago(preferenceDTO);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para procesar las notificaciones (webhooks) de Mercado Pago
     */
    @Operation(
        summary = "Procesar webhooks de Mercado Pago",
        description = "Recibe y procesa notificaciones de cambios en los pagos desde Mercado Pago"
    )
    @PostMapping("/webhooks/mercadopago")
    public ResponseEntity<String> procesarWebhookMercadoPago(
            @Parameter(description = "Datos de la notificación de Mercado Pago") 
            @RequestBody Map<String, Object> payload) {
        log.info("*************************************************************");
        log.info("WEBHOOK RECIBIDO: Recibida notificación de Mercado Pago");
        log.info("Payload completo: {}", payload);
        log.info("*************************************************************");
        
        try {
            // Extraer los datos relevantes del payload
            String action = (String) payload.get("action");
            log.info("Action: {}", action);
            
            if ("payment.created".equals(action) || "payment.updated".equals(action)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                log.info("Data: {}", data);
                
                String paymentId = data.get("id").toString();
                log.info("Payment ID: {}", paymentId);
                
                // Consultamos el estado actual del pago en Mercado Pago
                // Para pruebas, podemos usar "approved" directamente si es "payment.updated"
                String status = "payment.updated".equals(action) ? "approved" : "pending";
                
                log.info("Procesando notificación de pago - ID: {}, Action: {}, Status: {}", 
                        paymentId, action, status);
                
                pagoService.procesarNotificacionPago(paymentId, status);
                log.info("Notificación procesada correctamente");
            } else {
                log.info("Action no reconocida: {}", action);
            }
            
            return ResponseEntity.ok("Webhook procesado correctamente");
        } catch (Exception e) {
            log.error("Error al procesar webhook de Mercado Pago", e);
            log.error("Excepción: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la notificación");
        }
    }
    
    /**
     * Registra un pago manual (efectivo, transferencia, etc.)
     */
    @Operation(
        summary = "Registrar pago manual",
        description = "Registra un pago manual (efectivo, transferencia, etc.)"
    )
    @PostMapping("/manual")
    public ResponseEntity<Pago> registrarPagoManual(
            @Parameter(description = "Código del pedido") @RequestParam String codigoPedido, 
            @Parameter(description = "Monto del pago") @RequestParam BigDecimal monto,
            @Parameter(description = "Método de pago") @RequestParam String metodo) {
        log.info("Registrando pago manual para pedido: {}, método: {}, monto: {}", codigoPedido, metodo, monto);
        Pago pago = pagoService.registrarPagoManual(codigoPedido, monto, metodo);
        return ResponseEntity.ok(pago);
    }
    
    /**
     * Crea un registro de pago por transferencia bancaria (estado PENDIENTE)
     */
    @Operation(
        summary = "Crear pago por transferencia",
        description = "Registra un pago por transferencia bancaria en estado pendiente"
    )
    @PostMapping("/transferencia")
    public ResponseEntity<Pago> crearPagoTransferencia(
            @Parameter(description = "Código del pedido") @RequestParam String codigoPedido,
            @Parameter(description = "Monto del pago") @RequestParam BigDecimal monto,
            @Parameter(description = "Observación (opcional)") @RequestParam(required = false) String observacion) {
        log.info("Creando pago por transferencia para pedido: {}, monto: {}", codigoPedido, monto);
        
        Pago pago = Pago.builder()
                .codigoPedido(codigoPedido)
                .monto(monto)
                .metodo("transferencia")
                .fechaPago(null) // Se actualizará cuando se verifique la transferencia
                .estado("PENDIENTE")
                .mercadoPagoStatusDetail(observacion)
                .build();
        
        pago = pagoService.registrarPago(pago);
        return ResponseEntity.ok(pago);
    }

    /**
     * Obtiene el estado de un pago por su codigo de pedido
     */
    @Operation(
        summary = "Obtener estado de pago",
        description = "Obtiene el estado actual de un pago por su código de pedido"
    )
    @GetMapping("/estado/{codigoPedido}")
    public ResponseEntity<?> obtenerEstadoPago(
            @Parameter(description = "Código del pedido") @PathVariable String codigoPedido) {
        log.info("Obteniendo estado de pago para pedido: {}", codigoPedido);

        PagoResponseDTO estado = pagoService.buscarPorCodigoDePedido(codigoPedido);
        if (estado != null) {
            return ResponseEntity.ok(estado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró el estado del pago para el pedido: " + codigoPedido);
        }
    }

    @Operation(
        summary = "Obtener todos los pagos",
        description = "Obtiene una lista de todos los pagos registrados",
        security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/todosLosPagos")
    public ResponseEntity<List<PagoResponseDTO>> obtenerTodosLosPagos() {
       try{
            log.info("Obteniendo todos los pagos registrados");
            List<PagoResponseDTO> pagos = pagoService.todosLosPagos();
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            log.error("Error al obtener todos los pagos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
       }

    }
    
    /**
     * Cambia el estado de un pago por su código de pedido
     * Requiere autenticación con rol ADMINISTRADOR
     */
    @Operation(
        summary = "Cambiar estado de pago",
        description = "Cambia el estado de un pago por su código de pedido. Requiere rol ADMINISTRADOR.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMINISTRADOR"),
        @ApiResponse(responseCode = "404", description = "No se encontró el pago"),
        @ApiResponse(responseCode = "500", description = "Error al cambiar el estado")
    })
    @PutMapping("/cambiarEstado")
    public ResponseEntity<String> cambiarEstadoPago(
            @Parameter(description = "Código del pedido") @RequestParam String codigoPedido, 
            @Parameter(description = "Nuevo estado del pago") @RequestParam String estado) {
        
        log.info("Cambiando estado de pago para pedido: {} a: {}", codigoPedido, estado);
        
        try {
            // Obtener el token guardado para este pedido
            String token = tokensPorPedido.get(codigoPedido);
            
            // Para cambios manuales, mantenemos el comportamiento original
            pagoService.cambiarEstadoPago(codigoPedido, estado, token);
            return ResponseEntity.ok("Estado del pago actualizado correctamente");
        } catch (PagoNotFoundException e) {
            log.warn("Intento de cambiar estado a pago inexistente: {}", codigoPedido);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al cambiar el estado del pago", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado del pago: " + e.getMessage());
        }
    }
    
    /**
     * Procesa el retorno desde Mercado Pago cuando el pago es exitoso
     */
    @GetMapping("/retorno/exito")
    public ResponseEntity<Pago> procesarRetornoExitoso(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Procesando retorno exitoso - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Intentar establecer el token en el SecurityContext
        establecerTokenEnSecurityContext(authorizationHeader);
        
        // Obtener el token guardado para este pedido
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        if (token != null) {
            log.info("Token recuperado del mapa para el pedido: {}", external_reference);
        }
        
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        if (pago != null) {
            return ResponseEntity.ok(pago);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
    
    /**
     * Procesa el retorno desde Mercado Pago cuando el pago es rechazado
     */
    @GetMapping("/retorno/fracaso")
    public ResponseEntity<Pago> procesarRetornoFracaso(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Procesando retorno fallido - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Intentar establecer el token en el SecurityContext
        establecerTokenEnSecurityContext(authorizationHeader);
        
        // Obtener el token guardado para este pedido
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        if (token != null) {
            log.info("Token recuperado del mapa para el pedido: {}", external_reference);
        }
        
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        if (pago != null) {
            return ResponseEntity.ok(pago);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
    
    /**
     * Procesa el retorno desde Mercado Pago cuando el pago está pendiente
     */
    @GetMapping("/retorno/pendiente")
    public ResponseEntity<Pago> procesarRetornoPendiente(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Procesando retorno pendiente - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Intentar establecer el token en el SecurityContext
        establecerTokenEnSecurityContext(authorizationHeader);
        
        // Obtener el token guardado para este pedido
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        if (token != null) {
            log.info("Token recuperado del mapa para el pedido: {}", external_reference);
        }
        
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        if (pago != null) {
            return ResponseEntity.ok(pago);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
    
    /**
     * Redirige al frontend después de un pago exitoso
     */
    @GetMapping("/redirect/success")
    public String redirectSuccess(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            org.springframework.ui.Model model) {
        
        log.info("Redirigiendo después de pago exitoso - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Intentar establecer el token en el SecurityContext
        establecerTokenEnSecurityContext(authorizationHeader);
        
        // Obtener el token guardado para este pedido
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        if (token != null) {
            log.info("Token recuperado del mapa para el pedido: {}", external_reference);
        }
        
        // Procesar el pago primero
        pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        
        // Pasar parámetros al modelo para el HTML (igual que los nuevos endpoints)
        model.addAttribute("payment_id", payment_id != null ? payment_id : "");
        model.addAttribute("status", status != null ? status : "approved");
        model.addAttribute("order_id", external_reference != null ? external_reference : "");
        model.addAttribute("type", "success");
        
        log.info("ENDPOINT ANTIGUO: Devolviendo template redirect-bridge.html con parámetros: payment_id={}, status={}, order_id={}", 
                payment_id, status, external_reference);
        
        return "redirect-bridge"; // Usar el mismo template que los nuevos endpoints
    }
    
    /**
     * Redirige al frontend después de un pago con error
     */
    @GetMapping("/redirect/error")
    public String redirectError(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            org.springframework.ui.Model model) {
        
        log.info("Redirigiendo después de pago con error - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Intentar establecer el token en el SecurityContext
        establecerTokenEnSecurityContext(authorizationHeader);
        
        // Obtener el token guardado para este pedido
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        if (token != null) {
            log.info("Token recuperado del mapa para el pedido: {}", external_reference);
        }
        
        // Si no hay status, usamos "cancelled" para que se marque como CANCELADO
        String estadoActual = (status == null || status.isEmpty() || "null".equals(status)) ? "cancelled" : status;
        
        // Procesar el pago primero
        pagoService.procesarRetornoPago(payment_id, estadoActual, external_reference, token);
        
        // Si tenemos el código de pedido pero no tenemos payment_id, marcar como CANCELADO directamente
        if (external_reference != null && (payment_id == null || payment_id.isEmpty())) {
            try {
                pagoService.cambiarEstadoPago(external_reference, "CANCELADO", token);
            } catch (Exception e) {
                log.warn("No se pudo actualizar el estado del pago para el pedido: {}", external_reference);
            }
        }
        
        // Pasar parámetros al modelo para el HTML
        model.addAttribute("payment_id", payment_id != null ? payment_id : "");
        model.addAttribute("status", status != null ? status : "cancelled");
        model.addAttribute("order_id", external_reference != null ? external_reference : "");
        model.addAttribute("type", "error");
        
        log.info("ENDPOINT ANTIGUO: Devolviendo template redirect-bridge.html con parámetros: payment_id={}, status={}, order_id={}", 
                payment_id, status, external_reference);
        
        return "redirect-bridge";
    }
    
    /**
     * Redirige al frontend después de un pago pendiente
     */
    @GetMapping("/redirect/pending")
    public String redirectPending(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            org.springframework.ui.Model model) {
        
        log.info("Redirigiendo después de pago pendiente - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Intentar establecer el token en el SecurityContext
        establecerTokenEnSecurityContext(authorizationHeader);
        
        // Obtener el token guardado para este pedido
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        if (token != null) {
            log.info("Token recuperado del mapa para el pedido: {}", external_reference);
        }
        
        // Procesar el pago primero
        pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        
        // Pasar parámetros al modelo para el HTML
        model.addAttribute("payment_id", payment_id != null ? payment_id : "");
        model.addAttribute("status", status != null ? status : "pending");
        model.addAttribute("order_id", external_reference != null ? external_reference : "");
        model.addAttribute("type", "pending");
        
        log.info("ENDPOINT ANTIGUO: Devolviendo template redirect-bridge.html con parámetros: payment_id={}, status={}, order_id={}", 
                payment_id, status, external_reference);
        
        return "redirect-bridge";
    }
    
    /**
     * Método auxiliar para establecer el token en el SecurityContext
     */
    private void establecerTokenEnSecurityContext(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            // Si el SecurityContext no tiene el token, lo establecemos manualmente
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if (currentAuth == null || currentAuth.getCredentials() == null) {
                UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(null, token, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Token establecido manualmente en el SecurityContext para el procesamiento de retorno");
            }
        } else {
            log.warn("No se recibió header de autorización en el retorno del pago");
        }
    }

    /**
     * Obtiene el token almacenado para un código de pedido específico
     * @param codigoPedido Código del pedido
     * @return Token almacenado o null si no existe
     */
    public String getTokenForPedido(String codigoPedido) {
        return tokensPorPedido.get(codigoPedido);
    }
    
    /**
     * Endpoint de diagnóstico para verificar la configuración
     */
    @GetMapping("/diagnostico")
    public ResponseEntity<Map<String, Object>> diagnostico() {
        Map<String, Object> diagnostico = new HashMap<>();
        
        // Información del servidor
        diagnostico.put("backend_url", appBaseUrl);
        diagnostico.put("frontend_url", frontendBaseUrl);
        diagnostico.put("timestamp", LocalDateTime.now().toString());
        
        // URLs de redirección
        diagnostico.put("redirect_success", appBaseUrl + "/api/pagos/redirect/success");
        diagnostico.put("redirect_error", appBaseUrl + "/api/pagos/redirect/error");
        diagnostico.put("redirect_pending", appBaseUrl + "/api/pagos/redirect/pending");
        
        // URL de webhook - esta es la URL que debe configurarse en Mercado Pago
        diagnostico.put("webhook_url", appBaseUrl + "/api/pagos/webhooks/mercadopago");
        diagnostico.put("webhook_note", "Esta URL debe configurarse en Mercado Pago para recibir notificaciones");
        
        // Estado de la conexión con Mercado Pago
        try {
            // Intenta obtener información básica de Mercado Pago (se podría expandir)
            diagnostico.put("mercadopago_connection", "OK");
        } catch (Exception e) {
            diagnostico.put("mercadopago_connection", "ERROR");
            diagnostico.put("mercadopago_error", e.getMessage());
        }
        
        // Información sobre tokens almacenados
        diagnostico.put("tokens_pedidos_count", tokensPorPedido.size());
        diagnostico.put("tokens_pedidos_keys", tokensPorPedido.keySet());
        
        return ResponseEntity.ok(diagnostico);
    }

    /**
     * Página de éxito que Mercado Pago puede cargar directamente
     */
    @GetMapping("/success")
    public ResponseEntity<String> pagoExitosoHtml(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Página de éxito - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesar el pago primero (igual que antes)
        establecerTokenEnSecurityContext(authorizationHeader);
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        if (token != null) {
            log.info("Token recuperado del mapa para el pedido: {}", external_reference);
        }
        
        pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        
        // Crear HTML directamente
        String html = generarHtmlRedireccion(
            payment_id != null ? payment_id : "",
            status != null ? status : "approved",
            external_reference != null ? external_reference : "",
            "success"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        
        return ResponseEntity.ok().headers(headers).body(html);
    }
    
    /**
     * Página de error que Mercado Pago puede cargar directamente
     */
    @GetMapping("/error")
    public ResponseEntity<String> pagoErrorHtml(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Página de error - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesar el pago primero
        establecerTokenEnSecurityContext(authorizationHeader);
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        
        String estadoActual = (status == null || status.isEmpty() || "null".equals(status)) ? "cancelled" : status;
        pagoService.procesarRetornoPago(payment_id, estadoActual, external_reference, token);
        
        // Crear HTML directamente
        String html = generarHtmlRedireccion(
            payment_id != null ? payment_id : "",
            status != null ? status : "cancelled",
            external_reference != null ? external_reference : "",
            "error"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        
        return ResponseEntity.ok().headers(headers).body(html);
    }
    
    /**
     * Página de pendiente que Mercado Pago puede cargar directamente
     */
    @GetMapping("/pending")
    public ResponseEntity<String> pagoPendienteHtml(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Página de pendiente - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesar el pago primero
        establecerTokenEnSecurityContext(authorizationHeader);
        String token = (external_reference != null) ? tokensPorPedido.get(external_reference) : null;
        
        pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        
        // Crear HTML directamente
        String html = generarHtmlRedireccion(
            payment_id != null ? payment_id : "",
            status != null ? status : "pending",
            external_reference != null ? external_reference : "",
            "pending"
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        
        return ResponseEntity.ok().headers(headers).body(html);
    }
    
    /**
     * Genera el HTML de redirección dinámicamente
     */
    private String generarHtmlRedireccion(String paymentId, String status, String orderId, String type) {
        String titulo, mensaje, clase, icono;
        
        switch (type) {
            case "success":
                titulo = "¡Pago Exitoso! ✓";
                mensaje = "Tu pago ha sido procesado correctamente. Serás redirigido automáticamente a la página de confirmación.";
                clase = "success";
                icono = "✓";
                break;
            case "error":
                titulo = "Pago No Completado ✗";
                mensaje = "Hubo un problema con tu pago. Te redirigiremos para que puedas intentar nuevamente.";
                clase = "error";
                icono = "✗";
                break;
            case "pending":
                titulo = "Pago en Procesamiento ⏳";
                mensaje = "Tu pago está siendo verificado. Te notificaremos cuando esté confirmado.";
                clase = "pending";
                icono = "⏳";
                break;
            default:
                titulo = "Procesando...";
                mensaje = "Procesando tu solicitud...";
                clase = "success";
                icono = "⏳";
        }
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Procesando pago...</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        text-align: center;
                        margin: 50px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: white;
                        padding: 30px;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    h1 {
                        color: #003087;
                        margin-bottom: 15px;
                    }
                    .message {
                        color: #666;
                        margin-bottom: 25px;
                        font-size: 16px;
                    }
                    .loader {
                        border: 5px solid #f3f3f3;
                        border-top: 5px solid #009ee3;
                        border-radius: 50%%;
                        width: 50px;
                        height: 50px;
                        animation: spin 1s linear infinite;
                        margin: 20px auto;
                    }
                    .countdown {
                        color: #888;
                        font-size: 14px;
                        margin-top: 15px;
                    }
                    .success { color: #27ae60; }
                    .error { color: #e74c3c; }
                    .pending { color: #f39c12; }
                    
                    @keyframes spin {
                        0%% { transform: rotate(0deg); }
                        100%% { transform: rotate(360deg); }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Batistella & Cía</h1>
                    
                    <div class="%s">
                        <h2>%s</h2>
                        <p class="message">%s</p>
                    </div>
                    
                    <div class="loader"></div>
                    <p class="countdown" id="countdown">Redirigiendo en 3 segundos...</p>
                    
                    <p style="color: #999; font-size: 12px; margin-top: 20px;">
                        Si no eres redirigido automáticamente, 
                        <a href="https://www.batistellaycia.shop/checkout/%s?payment_id=%s&status=%s&order_id=%s">
                            haz clic aquí
                        </a>
                    </p>
                </div>

                <script>
                    document.addEventListener('DOMContentLoaded', function() {
                        const paymentId = '%s';
                        const status = '%s';
                        const orderId = '%s';
                        const type = '%s';
                        
                        console.log('Parámetros recibidos:', { paymentId, status, orderId, type });
                        
                        let redirectUrl = 'https://www.batistellaycia.shop/checkout/' + type;
                        let params = [];
                        
                        if (paymentId) params.push('payment_id=' + paymentId);
                        if (status) params.push('status=' + status);
                        if (orderId) params.push('order_id=' + orderId);
                        
                        if (params.length > 0) {
                            redirectUrl += '?' + params.join('&');
                        }
                        
                        console.log('URL de redirección:', redirectUrl);
                        
                        let seconds = 3;
                        const countdownElement = document.getElementById('countdown');
                        const interval = setInterval(function() {
                            seconds--;
                            countdownElement.textContent = 'Redirigiendo en ' + seconds + ' segundos...';
                            
                            if (seconds <= 0) {
                                clearInterval(interval);
                                console.log('Redirigiendo a:', redirectUrl);
                                window.location.href = redirectUrl;
                            }
                        }, 1000);
                    });
                </script>
            </body>
            </html>
            """, 
            clase, titulo, mensaje, type, paymentId, status, orderId,
            paymentId, status, orderId, type
        );
    }
} 