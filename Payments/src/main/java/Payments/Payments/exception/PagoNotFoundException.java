package Payments.Payments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción que se lanza cuando no se encuentra un pago
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PagoNotFoundException extends RuntimeException {
    
    public PagoNotFoundException(String codigoPedido) {
        super("No se encontró registro de pago para el código de pedido: " + codigoPedido);
    }
} 