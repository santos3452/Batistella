package Payments.Payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pagos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo_pedido", nullable = false)
    private String codigoPedido;
    
    @Column(nullable = false)
    private BigDecimal monto;
    
    @Column(nullable = false)
    private String metodo;
    
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
    
    @Column(nullable = false)
    private String estado;
    
    // Campos adicionales para la transacción con Mercado Pago
    @Column(name = "mp_payment_id")
    private String mercadoPagoPaymentId;
    
    @Column(name = "mp_preference_id")
    private String mercadoPagoPreferenceId;
    
    @Column(name = "mp_status")
    private String mercadoPagoStatus;
    
    @Column(name = "mp_status_detail")
    private String mercadoPagoStatusDetail;
} 