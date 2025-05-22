package Payments.Payments.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Payments.Payments.model.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    // Buscar pagos por pedidoId
    List<Pago> findByPedidoId(Long pedidoId);
    
    // Buscar pagos por estado
    List<Pago> findByEstado(String estado);
    
    // Buscar pagos por método de pago
    List<Pago> findByMetodo(String metodo);
    
    // Buscar pagos por ID de preferencia de Mercado Pago
    Pago findByMercadoPagoPreferenceId(String preferenceId);
    
    // Buscar pagos por ID de pago de Mercado Pago
    Pago findByMercadoPagoPaymentId(String paymentId);
} 