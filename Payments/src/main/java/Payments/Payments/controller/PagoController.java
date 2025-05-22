package Payments.Payments.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Payments.Payments.dto.PagoResponseDTO;
import Payments.Payments.dto.PreferenceDTO;
import Payments.Payments.model.Pago;
import Payments.Payments.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Slf4j
public class PagoController {

    private final PagoService pagoService;
    
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;
    
    @Value("${app.url.base}")
    private String appBaseUrl;
    
    /**
     * Crea una preferencia de pago con Mercado Pago
     */
    @PostMapping("/mercadopago/preferencia")
    public ResponseEntity<PagoResponseDTO> crearPreferencia(@RequestBody PreferenceDTO preferenceDTO) {
        log.info("Creando preferencia de pago para pedido: {}", preferenceDTO.getCodigoPedido());
        PagoResponseDTO response = pagoService.crearPreferenciaMercadoPago(preferenceDTO);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para procesar las notificaciones (webhooks) de Mercado Pago
     */
    @PostMapping("/webhooks/mercadopago")
    public ResponseEntity<String> procesarWebhookMercadoPago(@RequestBody Map<String, Object> payload) {
        log.info("Recibida notificación de Mercado Pago: {}", payload);
        
        try {
            // Extraer los datos relevantes del payload
            String action = (String) payload.get("action");
            if ("payment.created".equals(action) || "payment.updated".equals(action)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String paymentId = data.get("id").toString();
                pagoService.procesarNotificacionPago(paymentId, "pending");
            }
            
            return ResponseEntity.ok("Webhook procesado correctamente");
        } catch (Exception e) {
            log.error("Error al procesar webhook de Mercado Pago", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la notificación");
        }
    }
    
    /**
     * Registra un pago manual (efectivo, transferencia, etc.)
     */
    @PostMapping("/manual")
    public ResponseEntity<Pago> registrarPagoManual(@RequestParam String codigoPedido, 
                                                    @RequestParam BigDecimal monto,
                                                    @RequestParam String metodo) {
        log.info("Registrando pago manual para pedido: {}, método: {}, monto: {}", codigoPedido, metodo, monto);
        Pago pago = pagoService.registrarPagoManual(codigoPedido, monto, metodo);
        return ResponseEntity.ok(pago);
    }
    
    /**
     * Obtiene un pago por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtenerPago(@PathVariable Long id) {
        Optional<Pago> pago = pagoService.buscarPorId(id);
        return pago.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Lista todos los pagos de un pedido
     */
    @GetMapping("/pedido/{codigoPedido}")
    public ResponseEntity<List<Pago>> listarPagosPorPedido(@PathVariable String codigoPedido) {
        List<Pago> pagos = pagoService.listarPagosPorPedido(codigoPedido);
        return ResponseEntity.ok(pagos);
    }
    
    /**
     * Crea un registro de pago por transferencia bancaria (estado PENDIENTE)
     */
    @PostMapping("/transferencia")
    public ResponseEntity<Pago> crearPagoTransferencia(@RequestParam String codigoPedido,
                                                       @RequestParam BigDecimal monto,
                                                       @RequestParam(required = false) String observacion) {
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
     * Procesa el retorno desde Mercado Pago cuando el pago es exitoso
     */
    @GetMapping("/retorno/exito")
    public ResponseEntity<Pago> procesarRetornoExitoso(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference) {
        
        log.info("Procesando retorno exitoso - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference);
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
            @RequestParam(required = false) String external_reference) {
        
        log.info("Procesando retorno fallido - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference);
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
            @RequestParam(required = false) String external_reference) {
        
        log.info("Procesando retorno pendiente - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference);
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
    public ResponseEntity<Void> redirectSuccess(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference) {
        
        log.info("Redirigiendo después de pago exitoso - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesar el pago primero
        pagoService.procesarRetornoPago(payment_id, status, external_reference);
        
        // Construir URL de redirección a la página intermedia
        StringBuilder redirectUrl = new StringBuilder("/redirect.html?type=success");
        if (payment_id != null) {
            redirectUrl.append("&payment_id=").append(payment_id);
        }
        if (status != null) {
            redirectUrl.append("&status=").append(status);
        }
        if (external_reference != null) {
            redirectUrl.append("&order_id=").append(external_reference);
        }
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl.toString())
                .build();
    }
    
    /**
     * Redirige al frontend después de un pago con error
     */
    @GetMapping("/redirect/error")
    public ResponseEntity<Void> redirectError(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference) {
        
        log.info("Redirigiendo después de pago con error - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesar el pago primero
        pagoService.procesarRetornoPago(payment_id, status, external_reference);
        
        // Construir URL de redirección a la página intermedia
        StringBuilder redirectUrl = new StringBuilder("/redirect.html?type=error");
        if (payment_id != null) {
            redirectUrl.append("&payment_id=").append(payment_id);
        }
        if (status != null) {
            redirectUrl.append("&status=").append(status);
        }
        if (external_reference != null) {
            redirectUrl.append("&order_id=").append(external_reference);
        }
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl.toString())
                .build();
    }
    
    /**
     * Redirige al frontend después de un pago pendiente
     */
    @GetMapping("/redirect/pending")
    public ResponseEntity<Void> redirectPending(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference) {
        
        log.info("Redirigiendo después de pago pendiente - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesar el pago primero
        pagoService.procesarRetornoPago(payment_id, status, external_reference);
        
        // Construir URL de redirección a la página intermedia
        StringBuilder redirectUrl = new StringBuilder("/redirect.html?type=pending");
        if (payment_id != null) {
            redirectUrl.append("&payment_id=").append(payment_id);
        }
        if (status != null) {
            redirectUrl.append("&status=").append(status);
        }
        if (external_reference != null) {
            redirectUrl.append("&order_id=").append(external_reference);
        }
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl.toString())
                .build();
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
        
        // Estado de la conexión con Mercado Pago
        try {
            // Intenta obtener información básica de Mercado Pago (se podría expandir)
            diagnostico.put("mercadopago_connection", "OK");
        } catch (Exception e) {
            diagnostico.put("mercadopago_connection", "ERROR");
            diagnostico.put("mercadopago_error", e.getMessage());
        }
        
        return ResponseEntity.ok(diagnostico);
    }
} 