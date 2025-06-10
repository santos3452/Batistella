package Payments.Payments.service;

import java.math.BigDecimal;
import java.util.List;

import Payments.Payments.dto.PagoResponseDTO;
import Payments.Payments.dto.PreferenceDTO;
import Payments.Payments.model.Pago;

public interface PagoService {

    PagoResponseDTO crearPreferenciaMercadoPago(PreferenceDTO preferenceDTO);
    
    void procesarNotificacionPago(String paymentId, String status);

    PagoResponseDTO buscarPorCodigoDePedido(String codigoPedido);

    Pago registrarPagoManual(String codigoPedido, BigDecimal monto, String metodo);

    Pago registrarPago(Pago pago);

    Pago procesarRetornoPago(String paymentId, String status, String externalReference, String authToken);

    void cambiarEstadoPago(String codigoPedido, String status, String authToken);

    List<PagoResponseDTO> todosLosPagos();

} 