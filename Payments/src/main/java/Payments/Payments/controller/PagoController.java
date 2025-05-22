package Payments.Payments.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    
    /**
     * Crea una preferencia de pago con Mercado Pago
     */
    @PostMapping("/mercadopago/preferencia")
    public ResponseEntity<PagoResponseDTO> crearPreferencia(@RequestBody PreferenceDTO preferenceDTO) {
        log.info("Creando preferencia de pago para pedido: {}", preferenceDTO.getPedidoId());
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
    public ResponseEntity<Pago> registrarPagoManual(@RequestParam Long pedidoId, 
                                                    @RequestParam BigDecimal monto,
                                                    @RequestParam String metodo) {
        log.info("Registrando pago manual para pedido: {}, método: {}, monto: {}", pedidoId, metodo, monto);
        Pago pago = pagoService.registrarPagoManual(pedidoId, monto, metodo);
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
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<Pago>> listarPagosPorPedido(@PathVariable Long pedidoId) {
        List<Pago> pagos = pagoService.listarPagosPorPedido(pedidoId);
        return ResponseEntity.ok(pagos);
    }
    
    /**
     * Crea un registro de pago por transferencia bancaria (estado PENDIENTE)
     */
    @PostMapping("/transferencia")
    public ResponseEntity<Pago> crearPagoTransferencia(@RequestParam Long pedidoId,
                                                       @RequestParam BigDecimal monto,
                                                       @RequestParam(required = false) String observacion) {
        log.info("Creando pago por transferencia para pedido: {}, monto: {}", pedidoId, monto);
        
        Pago pago = Pago.builder()
                .pedidoId(pedidoId)
                .monto(monto)
                .metodo("transferencia")
                .fechaPago(null) // Se actualizará cuando se verifique la transferencia
                .estado("PENDIENTE")
                .mercadoPagoStatusDetail(observacion)
                .build();
        
        pago = pagoService.registrarPago(pago);
        return ResponseEntity.ok(pago);
    }
} 