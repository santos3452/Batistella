package Payments.Payments.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import Payments.Payments.model.Pago;
import Payments.Payments.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
@Slf4j
public class PaginasController {

    private final PagoService pagoService;

    /**
     * Página de pago exitoso
     */
    @GetMapping("/exito")
    public String paginaExito(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            Model model) {
        
        log.info("Mostrando página de éxito - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesamos y actualizamos el pago primero
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference);
        
        // Añadimos datos al modelo para la vista
        model.addAttribute("titulo", "¡Pago Completado!");
        model.addAttribute("mensaje", "Tu pago ha sido procesado exitosamente.");
        model.addAttribute("paymentId", payment_id);
        model.addAttribute("estado", status);
        model.addAttribute("pedido", external_reference);
        model.addAttribute("pago", pago);
        
        return "pago-completado";
    }
    
    /**
     * Página de pago rechazado
     */
    @GetMapping("/fracaso")
    public String paginaFracaso(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            Model model) {
        
        log.info("Mostrando página de fracaso - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesamos y actualizamos el pago primero
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference);
        
        // Añadimos datos al modelo para la vista
        model.addAttribute("titulo", "Pago Rechazado");
        model.addAttribute("mensaje", "Lo sentimos, tu pago ha sido rechazado.");
        model.addAttribute("paymentId", payment_id);
        model.addAttribute("estado", status);
        model.addAttribute("pedido", external_reference);
        model.addAttribute("pago", pago);
        
        return "pago-rechazado";
    }
    
    /**
     * Página de pago pendiente
     */
    @GetMapping("/pendiente")
    public String paginaPendiente(
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            Model model) {
        
        log.info("Mostrando página de pendiente - payment_id: {}, status: {}, pedido: {}", 
                payment_id, status, external_reference);
        
        // Procesamos y actualizamos el pago primero
        Pago pago = pagoService.procesarRetornoPago(payment_id, status, external_reference);
        
        // Añadimos datos al modelo para la vista
        model.addAttribute("titulo", "Pago en Procesamiento");
        model.addAttribute("mensaje", "Tu pago está siendo procesado. Te notificaremos cuando se complete.");
        model.addAttribute("paymentId", payment_id);
        model.addAttribute("estado", status);
        model.addAttribute("pedido", external_reference);
        model.addAttribute("pago", pago);
        
        return "pago-pendiente";
    }
} 