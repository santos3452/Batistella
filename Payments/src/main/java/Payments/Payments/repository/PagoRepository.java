package Payments.Payments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Payments.Payments.model.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    // Buscar pagos por codigoPedido
    List<Pago> findByCodigoPedido(String codigoPedido);
    
    // Buscar el pago más reciente por código de pedido
    @Query(value = "SELECT * FROM pagos p WHERE p.codigo_pedido = :codigoPedido ORDER BY p.id DESC LIMIT 1", nativeQuery = true)
    Optional<Pago> findLatestByCodigoPedido(@Param("codigoPedido") String codigoPedido);
    
    // Buscar pagos por estado
    List<Pago> findByEstado(String estado);
    
    // Buscar pagos por método de pago
    List<Pago> findByMetodo(String metodo);
    
    // Buscar pagos por ID de preferencia de Mercado Pago
    Pago findByMercadoPagoPreferenceId(String preferenceId);
    
    // Buscar pagos por ID de pago de Mercado Pago
    Pago findByMercadoPagoPaymentId(String paymentId);
} 