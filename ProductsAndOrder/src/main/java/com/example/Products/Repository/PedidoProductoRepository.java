package com.example.Products.Repository;

import com.example.Products.Entity.PedidoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoProductoRepository extends JpaRepository<PedidoProducto, Long> {
    List<PedidoProducto> findByPedidoId(Long pedidoId);
} 