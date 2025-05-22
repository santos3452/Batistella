package Payments.Payments.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    
    /**
     * Página de pago exitoso
     * Esta URL debe coincidir con la URL configurada en el BackURLs de Mercado Pago
     */
    @GetMapping("/exito")
    public RedirectView pagoExitoso(@RequestParam(required = false) String payment_id,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String external_reference,
                              @RequestParam(required = false) String preference_id) {
        log.info("Pago exitoso recibido - Payment ID: {}, Status: {}, External Reference: {}, Preference ID: {}", 
                payment_id, status, external_reference, preference_id);
        
        // Actualizar el estado del pago si tenemos un ID de pago o preferencia
        if (payment_id != null) {
            pagoService.procesarNotificacionPago(payment_id, status);
        } else if (preference_id != null) {
            // Podríamos buscar el pago por preference_id y actualizarlo
            log.info("Actualizando estado por preference_id: {}", preference_id);
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
    public RedirectView pagoFallido(@RequestParam(required = false) String payment_id,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String external_reference,
                             @RequestParam(required = false) String preference_id) {
        log.info("Pago fallido recibido - Payment ID: {}, Status: {}, External Reference: {}, Preference ID: {}", 
                payment_id, status, external_reference, preference_id);
        
        // Actualizar el estado del pago si es necesario
        if (payment_id != null) {
            pagoService.procesarNotificacionPago(payment_id, "rejected");
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
    public RedirectView pagoPendiente(@RequestParam(required = false) String payment_id,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String external_reference,
                              @RequestParam(required = false) String preference_id) {
        log.info("Pago pendiente recibido - Payment ID: {}, Status: {}, External Reference: {}, Preference ID: {}", 
                payment_id, status, external_reference, preference_id);
        
        // Actualizar el estado del pago si es necesario
        if (payment_id != null) {
            pagoService.procesarNotificacionPago(payment_id, "in_process");
        }
        
        // Redireccionar a la página de pendiente
        String redirectUrl = "/pago-pendiente.html" +
                "?payment_id=" + (payment_id != null ? payment_id : "") + 
                "&status=" + (status != null ? status : "") + 
                "&external_reference=" + (external_reference != null ? external_reference : "");
        
        return new RedirectView(redirectUrl);
    }
} 