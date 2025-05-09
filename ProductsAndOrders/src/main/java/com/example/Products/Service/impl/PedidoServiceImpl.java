package com.example.Products.Service.impl;

import com.example.Products.Config.JwtService;
import com.example.Products.Config.ModelMapperConfig;
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

import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
    private ModelMapperConfig modelMapperConfig;
    
    @Autowired
    private JwtService jwtService;

    /**
     * Genera un código de pedido único con el formato: PED-XXNNNNN
     * Donde XX son dos letras aleatorias y NNNNN son cinco números aleatorios
     */
    private String generarCodigoPedido() {
        String codigoPedido;
        do {
            // Generar dos letras aleatorias
            String letras = RandomStringUtils.randomAlphabetic(2).toUpperCase();
            
            // Generar cinco números aleatorios
            String numeros = String.format("%05d", new Random().nextInt(100000));
            
            // Crear el código completo
            codigoPedido = "PED-" + letras + numeros;
            
            // Verificar si ya existe
        } while (pedidoRepository.existsByCodigoPedido(codigoPedido));
        
        return codigoPedido;
    }

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(CrearPedidoDTO crearPedidoDTO) {
        // Obtener el ID del usuario autenticado
        Long usuarioAutenticadoId = jwtService.getCurrentUserId();
        if (usuarioAutenticadoId == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        
        // Verificar que el usuario autenticado coincida con el del pedido o sea administrador
        boolean isAdmin = jwtService.isAdmin();
        if (!isAdmin && !usuarioAutenticadoId.equals(crearPedidoDTO.getUsuarioId())) {
            throw new AccessDeniedException("No puedes crear pedidos para otro usuario");
        }
        
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
        
        // Generar y asignar el código de pedido único
        pedido.setCodigoPedido(generarCodigoPedido());

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

        // Guardar los productos del pedido
        pedidoProductoRepository.saveAll(pedidoProductos);
        
        // Preparar respuesta con los productos que acabamos de guardar para evitar problemas de carga perezosa
        PedidoResponseDTO responseDTO = modelMapperConfig.modelMapper().map(pedidoGuardado, PedidoResponseDTO.class);
        
        // Establecer el nombre completo del usuario desde la caché
        responseDTO.setNombreCompletoUsuario(jwtService.getUserFullNameFromCache(pedidoGuardado.getUsuarioId()));
        
        // Mapear manualmente los productos del pedido porque aún no están asociados en la entidad pedidoGuardado
        List<PedidoProductoResponseDTO> productosResponse = pedidoProductos.stream()
            .map(pedidoProducto -> {
                PedidoProductoResponseDTO productoDTO = modelMapperConfig.modelMapper().map(pedidoProducto, PedidoProductoResponseDTO.class);
                
                // Construir el nombre del producto con el formato: MARCA + TIPO ALIMENTO + TIPO RAZA
                Products producto = pedidoProducto.getProducto();
                String nombreProducto = producto.getMarca().toString();
                nombreProducto += " " + producto.getTipoAlimento().toString();
                if (producto.getTipoRaza() != null) {
                    nombreProducto += " " + producto.getTipoRaza().toString();
                }
                
                productoDTO.setNombreProducto(nombreProducto);
                BigDecimal cantidad = new BigDecimal(pedidoProducto.getCantidad());
                productoDTO.setSubtotal(pedidoProducto.getPrecioUnitario().multiply(cantidad));
                return productoDTO;
            })
            .collect(Collectors.toList());
        
        responseDTO.setProductos(productosResponse);
        return responseDTO;
    }
    
    @Override
    public List<PedidoResponseDTO> obtenerPedidosPorUsuario(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(usuarioId);
        return pedidos.stream()
            .map(this::mapPedidoToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<PedidoResponseDTO> buscarPorCodigoPedido(String codigoPedido) {
        return pedidoRepository.findByCodigoPedido(codigoPedido)
            .map(this::mapPedidoToDTO);
    }

    @Override
    public List<PedidoResponseDTO> obtenerTodosLosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        return pedidos.stream()
            .map(this::mapPedidoToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Método auxiliar para mapear un Pedido a su DTO de respuesta
     */
    private PedidoResponseDTO mapPedidoToDTO(Pedido pedido) {
        ModelMapper mapper = modelMapperConfig.modelMapper();
        PedidoResponseDTO responseDTO = mapper.map(pedido, PedidoResponseDTO.class);
        
        // Obtener el nombre completo del usuario desde la caché
        responseDTO.setNombreCompletoUsuario(jwtService.getUserFullNameFromCache(pedido.getUsuarioId()));
        
        // Mapear productos del pedido
        List<PedidoProductoResponseDTO> productosResponse = pedido.getPedidoProductos().stream()
            .map(pedidoProducto -> {
                PedidoProductoResponseDTO productoDTO = mapper.map(pedidoProducto, PedidoProductoResponseDTO.class);
                
                // Construir el nombre del producto con el formato: MARCA + TIPO ALIMENTO + TIPO RAZA
                Products producto = pedidoProducto.getProducto();
                String nombreProducto = producto.getMarca().toString();
                nombreProducto += " " + producto.getTipoAlimento().toString();
                if (producto.getTipoRaza() != null) {
                    nombreProducto += " " + producto.getTipoRaza().toString();
                }
                
                productoDTO.setNombreProducto(nombreProducto);
                BigDecimal cantidad = new BigDecimal(pedidoProducto.getCantidad());
                productoDTO.setSubtotal(pedidoProducto.getPrecioUnitario().multiply(cantidad));
                return productoDTO;
            })
            .collect(Collectors.toList());
        
        responseDTO.setProductos(productosResponse);
        return responseDTO;
    }
} 