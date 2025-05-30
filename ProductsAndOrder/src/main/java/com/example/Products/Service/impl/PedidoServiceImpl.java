package com.example.Products.Service.impl;

import com.example.Products.Config.Secutiry.JwtService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.example.Products.Dtos.PagoDto.PagoResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
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

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${payment.service.url}")
    private String paymentServiceUrl;

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
        String role = jwtService.getCurrentUserRole();


        if(role.equals("ROLE_EMPRESA")){
            for (int i = 0; i < crearPedidoDTO.getProductos().size(); i++) {
                PedidoProductoDTO productoDTO = crearPedidoDTO.getProductos().get(i);
                Products producto = productos.get(i);

                // Usamos el precio minorista
                BigDecimal precioUnitario = producto.getPriceMayorista();
                BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(productoDTO.getCantidad()));
                total = total.add(subtotal);
            }



        }
        else{
            for (int i = 0; i < crearPedidoDTO.getProductos().size(); i++) {
                PedidoProductoDTO productoDTO = crearPedidoDTO.getProductos().get(i);
                Products producto = productos.get(i);

                // Usamos el precio minorista
                BigDecimal precioUnitario = producto.getPriceMinorista();
                BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(productoDTO.getCantidad()));
                total = total.add(subtotal);
            }

        }


        // Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(crearPedidoDTO.getUsuarioId());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(total);
        pedido.setDomicilioDeEtrega(crearPedidoDTO.getDomicilioDeEtrega());
        
        // Generar y asignar el código de pedido único
        pedido.setCodigoPedido(generarCodigoPedido());

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Crear los items del pedido y actualizar stock
        List<PedidoProducto> pedidoProductos = new ArrayList<>();
        for (int i = 0; i < crearPedidoDTO.getProductos().size(); i++) {
            PedidoProductoDTO productoDTO = crearPedidoDTO.getProductos().get(i);
            Products producto = productos.get(i);

            BigDecimal precio = producto.getPriceMinorista(); // Usar el precio unitario según el rol del usuario

            // Usar el precio unitario según el rol del usuario

            // Obtener el precio unitario actual
            if(role.equals("ROLE_EMPRESA")){
                precio = producto.getPriceMayorista();
                producto.setPriceMinorista(precio);
            }



            PedidoProducto pedidoProducto = new PedidoProducto();
            pedidoProducto.setPedido(pedidoGuardado);
            pedidoProducto.setProducto(producto);
            pedidoProducto.setCantidad(productoDTO.getCantidad());
            pedidoProducto.setPrecioUnitario(precio); // Guardar el precio actual

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
        
        // Obtener el email del usuario desde la caché
        responseDTO.setEmail(jwtService.getUserEmailFromCache(pedidoGuardado.getUsuarioId()));
        
        // Mapear manualmente los productos del pedido porque aún no están asociados en la entidad pedidoGuardado
        List<PedidoProductoResponseDTO> productosResponse = pedidoProductos.stream()
            .map(pedidoProducto -> {
                PedidoProductoResponseDTO productoDTO = modelMapperConfig.modelMapper().map(pedidoProducto, PedidoProductoResponseDTO.class);
                
                // Obtener el producto y usar su método getFullName()
                Products producto = pedidoProducto.getProducto();
                
                // Establecer el nombre del producto utilizando el método getFullName() que maneja la lógica según el tipo de animal
                productoDTO.setNombreProducto(producto.getFullName());
                
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
        // Ordenar por fecha de pedido descendente (más recientes primero)
        pedidos = pedidos.stream()
            .sorted((p1, p2) -> p2.getFechaPedido().compareTo(p1.getFechaPedido()))
            .collect(Collectors.toList());
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
    public List<PedidoResponseDTO> obtenerTodosLosPedidos(String codigoPedido, LocalDateTime fecha, EstadoPedido estado, Integer cantDatos, Integer pagina) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        
        // Aplicar filtros si se proporcionan
        if (codigoPedido != null) {
            pedidos = pedidos.stream()
                .filter(p -> p.getCodigoPedido().contains(codigoPedido))
                .collect(Collectors.toList());
        }
        
        if (fecha != null) {
            pedidos = pedidos.stream()
                .filter(p -> {
                    // Si se proporcionó fecha con hora, comparar fecha y hora
                    // Si la hora es 00:00:00, entonces solo comparar la fecha
                    if (fecha.getHour() == 0 && fecha.getMinute() == 0 && fecha.getSecond() == 0) {
                        // Solo comparar la fecha
                        return p.getFechaPedido().toLocalDate().equals(fecha.toLocalDate());
                    } else {
                        // Comparar fecha y hora
                        return p.getFechaPedido().equals(fecha);
                    }
                })
                .collect(Collectors.toList());
        }
        
        if (estado != null) {
            pedidos = pedidos.stream()
                .filter(p -> p.getEstado() == estado)
                .collect(Collectors.toList());
        }
        
        // Ordenar por fecha de pedido descendente (más recientes primero)
        pedidos = pedidos.stream()
            .sorted((p1, p2) -> p2.getFechaPedido().compareTo(p1.getFechaPedido()))
            .collect(Collectors.toList());
        
        // Aplicar paginación si se proporcionan los parámetros
        if (cantDatos != null && pagina != null) {
            int startIndex = (pagina - 1) * cantDatos;
            int endIndex = Math.min(startIndex + cantDatos, pedidos.size());
            
            // Validar índices
            if (startIndex >= 0 && startIndex < pedidos.size()) {
                pedidos = pedidos.subList(startIndex, endIndex);
            } else if (startIndex >= pedidos.size()) {
                pedidos = List.of(); // Lista vacía si la página está fuera de rango
            }
        }
        
        return pedidos.stream()
            .map(this::mapPedidoToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void cambiarEstadoPedido(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        // Verificar si el nuevo estado es válido
        try {
            EstadoPedido estado = EstadoPedido.valueOf(nuevoEstado.toUpperCase());
            pedido.setEstado(estado);
            pedidoRepository.save(pedido);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de pedido inválido: " + nuevoEstado);
        }
    }

    /**
     * Método auxiliar para mapear un Pedido a su DTO de respuesta
     */
    private PedidoResponseDTO mapPedidoToDTO(Pedido pedido) {
        ModelMapper mapper = modelMapperConfig.modelMapper();
        PedidoResponseDTO responseDTO = mapper.map(pedido, PedidoResponseDTO.class);
        
        // Obtener el nombre completo del usuario desde la caché
        responseDTO.setNombreCompletoUsuario(jwtService.getUserFullNameFromCache(pedido.getUsuarioId()));
        
        // Obtener el email del usuario desde la caché
        responseDTO.setEmail(jwtService.getUserEmailFromCache(pedido.getUsuarioId()));
        
        // Mapear productos del pedido
        List<PedidoProductoResponseDTO> productosResponse = pedido.getPedidoProductos().stream()
            .map(pedidoProducto -> {
                PedidoProductoResponseDTO productoDTO = mapper.map(pedidoProducto, PedidoProductoResponseDTO.class);
                
                // Obtener el producto y usar su método getFullName()
                Products producto = pedidoProducto.getProducto();
                
                // Establecer el nombre del producto utilizando el método getFullName() que maneja la lógica según el tipo de animal
                productoDTO.setNombreProducto(producto.getFullName());
                
                BigDecimal cantidad = new BigDecimal(pedidoProducto.getCantidad());
                productoDTO.setSubtotal(pedidoProducto.getPrecioUnitario().multiply(cantidad));
                return productoDTO;
            })
            .collect(Collectors.toList());
        
        responseDTO.setProductos(productosResponse);
        
        // Obtener los datos de pago desde el servicio externo
        try {
            // Construir la URL reemplazando el placeholder {codigoPedido} con el código real
            String url = paymentServiceUrl.replace("{codigoPedido}", pedido.getCodigoPedido());
            System.out.println("Consultando servicio de pagos en URL: " + url);
            
            ResponseEntity<PagoResponseDTO> response = restTemplate.getForEntity(url, PagoResponseDTO.class);
            
            System.out.println("Respuesta del servicio de pagos: " + response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PagoResponseDTO pagoInfo = response.getBody();
                responseDTO.setEstadoPago(pagoInfo.getEstado());
                responseDTO.setMetodoPago(pagoInfo.getMetodo());
                responseDTO.setFechaPago(pagoInfo.getFechaPago());
                System.out.println("Datos de pago obtenidos: Estado=" + pagoInfo.getEstado() + 
                                  ", Método=" + pagoInfo.getMetodo() + 
                                  ", Fecha=" + pagoInfo.getFechaPago());
                
                // Actualizar automáticamente el estado del pedido si el pago está CANCELADO o RECHAZADO
                if (pagoInfo.getEstado() != null && 
                    (pagoInfo.getEstado().equals("CANCELADO") || pagoInfo.getEstado().equals("RECHAZADO")) && 
                    pedido.getEstado() != EstadoPedido.CANCELADO) {
                    
                    // Actualizar el estado del pedido en la base de datos
                    pedido.setEstado(EstadoPedido.CANCELADO);
                    pedidoRepository.save(pedido);
                    
                    // Actualizar el DTO de respuesta
                    responseDTO.setEstado(EstadoPedido.CANCELADO);
                    System.out.println("Pedido " + pedido.getCodigoPedido() + " actualizado a CANCELADO porque el pago está " + pagoInfo.getEstado());
                }
                
                // Actualizar automáticamente el estado del pedido si el pago está COMPLETADO
                if (pagoInfo.getEstado() != null && 
                    pagoInfo.getEstado().equals("COMPLETADO") && 
                    pedido.getEstado() == EstadoPedido.PENDIENTE) {
                    
                    // Actualizar el estado del pedido en la base de datos
                    pedido.setEstado(EstadoPedido.CONFIRMADO);
                    pedidoRepository.save(pedido);
                    
                    // Actualizar el DTO de respuesta
                    responseDTO.setEstado(EstadoPedido.CONFIRMADO);
                    System.out.println("Pedido " + pedido.getCodigoPedido() + " actualizado a CONFIRMADO porque el pago está COMPLETADO");
                }
            } else {
                System.out.println("La respuesta del servicio de pagos no contiene datos o no es exitosa");
            }
        } catch (Exception e) {
            // Si hay un error en la comunicación con el servicio de pago, 
            // simplemente continuamos sin los datos de pago
            // Registramos el error con más detalle
            System.err.println("Error al obtener datos de pago: " + e.getMessage());
            e.printStackTrace();
        }
        
        return responseDTO;
    }
} 