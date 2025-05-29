package Payments.Payments.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import Payments.Payments.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/pagos-redirect")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {
    
    private final PagoService pagoService;
    private final PagoController pagoController; // Para acceder al mapa de tokens
    
    /**
     * Página de pago exitoso
     * Esta URL debe coincidir con la URL configurada en el BackURLs de Mercado Pago
     */
    @GetMapping("/exito")
    public RedirectView pagoExitoso(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestParam(required = false) String preference_id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Pago exitoso recibido - Payment ID: {}, Status: {}, External Reference: {}, Preference ID: {}", 
                payment_id, status, external_reference, preference_id);
        
        // Obtener token del header o del mapa en PagoController
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else if (external_reference != null) {
            token = pagoController.getTokenForPedido(external_reference);
        }
        
        // Actualizar el estado del pago si tenemos un ID de pago o preferencia
        if (payment_id != null) {
            pagoService.procesarRetornoPago(payment_id, status, external_reference, token);
        } else if (preference_id != null && external_reference != null) {
            // Podríamos buscar el pago por preference_id y actualizarlo
            log.info("Actualizando estado por preference_id: {}", preference_id);
            try {
                pagoService.cambiarEstadoPago(external_reference, "PENDIENTE", token);
            } catch (Exception e) {
                log.warn("No se pudo actualizar el estado del pago: {}", e.getMessage());
            }
        }
        
        // Redireccionar a la página de éxito
        String redirectUrl = "/pago-completado.html" +
                "?payment_id=" + (payment_id != null ? payment_id : "") + 
                "&status=" + (status != null ? status : "") + 
                "&external_reference=" + (external_reference != null ? external_reference : "");
        
        return new RedirectView(redirectUrl);
    }
    
    /**
     * Página de pago fallido
     */
    @GetMapping("/fracaso")
    public RedirectView pagoFallido(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestParam(required = false) String preference_id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Pago fallido recibido - Payment ID: {}, Status: {}, External Reference: {}, Preference ID: {}", 
                payment_id, status, external_reference, preference_id);
        
        // Obtener token del header o del mapa en PagoController
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else if (external_reference != null) {
            token = pagoController.getTokenForPedido(external_reference);
        }
        
        // Actualizar el estado del pago si es necesario
        if (payment_id != null) {
            pagoService.procesarRetornoPago(payment_id, "rejected", external_reference, token);
        } else if (external_reference != null) {
            try {
                pagoService.cambiarEstadoPago(external_reference, "RECHAZADO", token);
            } catch (Exception e) {
                log.warn("No se pudo actualizar el estado del pago: {}", e.getMessage());
            }
        }
        
        // Redireccionar a la página de fracaso
        String redirectUrl = "/pago-fallido.html" +
                "?payment_id=" + (payment_id != null ? payment_id : "") + 
                "&status=" + (status != null ? status : "") + 
                "&external_reference=" + (external_reference != null ? external_reference : "");
        
        return new RedirectView(redirectUrl);
    }
    
    /**
     * Página de pago pendiente
     */
    @GetMapping("/pendiente")
    public RedirectView pagoPendiente(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestParam(required = false) String preference_id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        log.info("Pago pendiente recibido - Payment ID: {}, Status: {}, External Reference: {}, Preference ID: {}", 
                payment_id, status, external_reference, preference_id);
        
        // Obtener token del header o del mapa en PagoController
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else if (external_reference != null) {
            token = pagoController.getTokenForPedido(external_reference);
        }
        
        // Actualizar el estado del pago si es necesario
        if (payment_id != null) {
            pagoService.procesarRetornoPago(payment_id, "in_process", external_reference, token);
        } else if (external_reference != null) {
            try {
                pagoService.cambiarEstadoPago(external_reference, "PENDIENTE", token);
            } catch (Exception e) {
                log.warn("No se pudo actualizar el estado del pago: {}", e.getMessage());
            }
        }
        
        // Redireccionar a la página de pendiente
        String redirectUrl = "/pago-pendiente.html" +
                "?payment_id=" + (payment_id != null ? payment_id : "") + 
                "&status=" + (status != null ? status : "") + 
                "&external_reference=" + (external_reference != null ? external_reference : "");
        
        return new RedirectView(redirectUrl);
    }
} 