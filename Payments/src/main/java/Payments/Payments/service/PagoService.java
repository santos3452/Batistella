package Payments.Payments.service;

import java.math.BigDecimal;

import Payments.Payments.dto.PagoResponseDTO;
import Payments.Payments.dto.PreferenceDTO;
import Payments.Payments.model.Pago;

public interface PagoService {

    PagoResponseDTO crearPreferenciaMercadoPago(PreferenceDTO preferenceDTO);
    
    void procesarNotificacionPago(String paymentId, String status);

    PagoResponseDTO buscarPorCodigoDePedido(String codigoPedido);

    Pago registrarPagoManual(String codigoPedido, BigDecimal monto, String metodo);

    Pago registrarPago(Pago pago);

    Pago procesarRetornoPago(String paymentId, String status, String externalReference);

    void cambiarEstadoPago(String codigoPedido, String status);
    
    /**
     * Cancela un pago buscándolo por su ID de preferencia de Mercado Pago
     * @param preferenceId ID de preferencia de Mercado Pago
     */
    void cancelarPagoPorPreferenceId(String preferenceId);
    
    /**
     * Actualiza los pagos pendientes con status null a CANCELADO
     * @return Número de pagos actualizados
     */
    int actualizarPagosPendientesNulos();
} 