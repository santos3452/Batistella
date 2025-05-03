package com.example.Products.Service.impl;

import com.example.Products.Dtos.PedidosDto.CrearPedidoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoProductoDTO;
import com.example.Products.Dtos.PedidosDto.PedidoProductoResponseDTO;
import com.example.Products.Dtos.PedidosDto.PedidoResponseDTO;
import com.example.Products.Entity.Pedido;
import com.example.Products.Entity.PedidoProducto;
import com.example.Products.Entity.Products;
import com.example.Products.Entity.enums.EstadoPedido;
import com.example.Products.Repository.PedidoProductoRepository;
import com.example.Products.Repository.PedidoRepository;
import com.example.Products.Repository.productRepository;
import com.example.Products.Service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoProductoRepository pedidoProductoRepository;

    @Autowired
    private productRepository productoRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(CrearPedidoDTO crearPedidoDTO) {
        // Verificar que todos los productos existen y tienen stock suficiente
        List<Products> productos = new ArrayList<>();
        for (PedidoProductoDTO productoDTO : crearPedidoDTO.getProductos()) {
            Products producto = productoRepository.findById(productoDTO.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + productoDTO.getProductoId()));

            if (producto.getStock() < productoDTO.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getMarca() + " " + producto.getTipoAlimento());
            }

            productos.add(producto);
        }

        // Calcular el total del pedido
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < crearPedidoDTO.getProductos().size(); i++) {
            PedidoProductoDTO productoDTO = crearPedidoDTO.getProductos().get(i);
            Products producto = productos.get(i);

            // Usamos el precio minorista
            BigDecimal precioUnitario = producto.getPriceMinorista();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(productoDTO.getCantidad()));
            total = total.add(subtotal);
        }

        // Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(crearPedidoDTO.getUsuarioId());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(total);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Crear los items del pedido y actualizar stock
        List<PedidoProducto> pedidoProductos = new ArrayList<>();
        for (int i = 0; i < crearPedidoDTO.getProductos().size(); i++) {
            PedidoProductoDTO productoDTO = crearPedidoDTO.getProductos().get(i);
            Products producto = productos.get(i);

            // Obtener el precio unitario actual
            BigDecimal precioUnitario = producto.getPriceMinorista();

            PedidoProducto pedidoProducto = new PedidoProducto();
            pedidoProducto.setPedido(pedidoGuardado);
            pedidoProducto.setProducto(producto);
            pedidoProducto.setCantidad(productoDTO.getCantidad());
            pedidoProducto.setPrecioUnitario(precioUnitario); // Guardar el precio actual

            pedidoProductos.add(pedidoProducto);

            // Actualizar stock
            producto.setStock(producto.getStock() - productoDTO.getCantidad());
            productoRepository.save(producto);
        }

        pedidoProductoRepository.saveAll(pedidoProductos);

        // Construir la respuesta
        PedidoResponseDTO responseDTO = new PedidoResponseDTO();
        responseDTO.setId(pedidoGuardado.getId());
        responseDTO.setUsuarioId(pedidoGuardado.getUsuarioId());
        responseDTO.setFechaPedido(pedidoGuardado.getFechaPedido());
        responseDTO.setEstado(pedidoGuardado.getEstado());
        responseDTO.setTotal(pedidoGuardado.getTotal());
        responseDTO.setCreatedAt(pedidoGuardado.getCreatedAt());
        responseDTO.setUpdatedAt(pedidoGuardado.getUpdatedAt());
        
        // Agregar productos al pedido
        List<PedidoProductoResponseDTO> productosResponse = new ArrayList<>();
        for (int i = 0; i < pedidoProductos.size(); i++) {
            PedidoProducto pedidoProducto = pedidoProductos.get(i);
            Products producto = productos.get(i);
            
            PedidoProductoResponseDTO productoDTO = new PedidoProductoResponseDTO();
            productoDTO.setId(pedidoProducto.getId());
            productoDTO.setProductoId(producto.getId());
            productoDTO.setNombreProducto(producto.getDescription());
            productoDTO.setCantidad(pedidoProducto.getCantidad());
            productoDTO.setPrecioUnitario(pedidoProducto.getPrecioUnitario()); // Usar el precio guardado
            BigDecimal cantidad = new BigDecimal(pedidoProducto.getCantidad());
            productoDTO.setSubtotal(pedidoProducto.getPrecioUnitario().multiply(cantidad)); // Calcular subtotal con precio guardado
            
            productosResponse.add(productoDTO);
        }
        
        responseDTO.setProductos(productosResponse);
        return responseDTO;
    }
    
    @Override
    public List<PedidoResponseDTO> obtenerPedidosPorUsuario(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(usuarioId);
        return pedidos.stream().map(pedido -> {
            PedidoResponseDTO responseDTO = new PedidoResponseDTO();
            responseDTO.setId(pedido.getId());
            responseDTO.setUsuarioId(pedido.getUsuarioId());
            responseDTO.setFechaPedido(pedido.getFechaPedido());
            responseDTO.setEstado(pedido.getEstado());
            responseDTO.setTotal(pedido.getTotal());
            responseDTO.setCreatedAt(pedido.getCreatedAt());
            responseDTO.setUpdatedAt(pedido.getUpdatedAt());
            
            // Obtener los productos del pedido
            List<PedidoProductoResponseDTO> productosResponse = pedido.getPedidoProductos().stream()
                .map(pedidoProducto -> {
                    PedidoProductoResponseDTO productoDTO = new PedidoProductoResponseDTO();
                    productoDTO.setId(pedidoProducto.getId());
                    productoDTO.setProductoId(pedidoProducto.getProducto().getId());
                    productoDTO.setNombreProducto(pedidoProducto.getProducto().getDescription());
                    productoDTO.setCantidad(pedidoProducto.getCantidad());
                    productoDTO.setPrecioUnitario(pedidoProducto.getPrecioUnitario()); // Usar el precio guardado en lugar del actual
                    BigDecimal cantidad = new BigDecimal(pedidoProducto.getCantidad());
                    productoDTO.setSubtotal(pedidoProducto.getPrecioUnitario().multiply(cantidad)); // Calcular subtotal con precio guardado
                    return productoDTO;
                })
                .collect(Collectors.toList());
            
            responseDTO.setProductos(productosResponse);
            return responseDTO;
        }).collect(Collectors.toList());
    }
} 