package com.example.Products.Repository;

import com.example.Products.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);
    
    boolean existsByCodigoPedido(String codigoPedido);
    
    Optional<Pedido> findByCodigoPedido(String codigoPedido);
} 