package Dashboards.Dashboards.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import Dashboards.Dashboards.model.dto.ChartDataDto;
import Dashboards.Dashboards.model.dto.PaymentFilterDto;
import Dashboards.Dashboards.model.dto.PedidoDto;
import Dashboards.Dashboards.model.dto.ProductoDto;
import Dashboards.Dashboards.model.dto.SalesSummaryDto;
import Dashboards.Dashboards.model.dto.TopCustomerDto;
import Dashboards.Dashboards.model.dto.TopCustomersSummaryDto;
import Dashboards.Dashboards.model.dto.TopProductDto;
import Dashboards.Dashboards.model.dto.TopProductsSummaryDto;
import Dashboards.Dashboards.service.SalesDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesDashboardServiceImpl implements SalesDashboardService {

    private final WebClient webClient;

    @Value("${microservices.pedidos.base-url}")
    private String pedidosBaseUrl;

    @Value("${microservices.pedidos.endpoint.todos-pedidos}")
    private String todosLosPedidosEndpoint;

    @Override
    public SalesSummaryDto getSalesSummary(LocalDate from, LocalDate to, String authToken) {
        log.info("Obteniendo resumen de ventas desde {} hasta {}", from, to);
        
        try {
            List<PedidoDto> todosPedidos = obtenerTodosLosPedidos(authToken);
            log.info("Se obtuvieron {} pedidos totales", todosPedidos.size());
            
            List<PedidoDto> pedidosFiltrados = filtrarPedidosPorFecha(todosPedidos, from, to);
            log.info("Se filtraron {} pedidos para el rango de fechas", pedidosFiltrados.size());
            
            return calcularResumenVentas(pedidosFiltrados);
            
        } catch (Exception e) {
            log.error("Error al obtener resumen de ventas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar datos de ventas: " + e.getMessage());
        }
    }

    private List<PedidoDto> obtenerTodosLosPedidos(String authToken) {
        List<PedidoDto> todosPedidos = new ArrayList<>();
        int paginaActual = 1;
        final int cantDatos = 100;
        boolean hayMasPaginas = true;

        log.info("Iniciando comunicación con microservicio de pedidos");

        while (hayMasPaginas) {
            final int pagina = paginaActual;
            
            try {
                List<PedidoDto> pedidosPagina = webClient.get()
                    .uri(uriBuilder -> {
                        String fullUrl = pedidosBaseUrl + todosLosPedidosEndpoint + 
                                       "?cantDatos=" + cantDatos + "&pagina=" + pagina;
                        log.debug("URI construida: {}", fullUrl);
                        return java.net.URI.create(fullUrl);
                    })
                    .header("Authorization", authToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response -> {
                        log.error("Error 4xx del microservicio de pedidos: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                            .map(body -> new RuntimeException("Error 4xx: " + response.statusCode() + " - " + body));
                    })
                    .onStatus(status -> status.is5xxServerError(), response -> {
                        log.error("Error 5xx del microservicio de pedidos: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                            .map(body -> new RuntimeException("Error 5xx: " + response.statusCode() + " - " + body));
                    })
                    .bodyToMono(new ParameterizedTypeReference<List<PedidoDto>>() {})
                    .onErrorMap(throwable -> {
                        log.error("Error al llamar al microservicio de pedidos: {}", throwable.getMessage());
                        return new RuntimeException("Error de comunicación con el servicio de pedidos: " + throwable.getMessage());
                    })
                    .block();

                if (pedidosPagina != null && !pedidosPagina.isEmpty()) {
                    todosPedidos.addAll(pedidosPagina);
                    paginaActual++;
                    
                    if (pedidosPagina.size() < cantDatos) {
                        hayMasPaginas = false;
                    }
                } else {
                    hayMasPaginas = false;
                }
                
            } catch (Exception e) {
                log.error("Error en página {}: {}", pagina, e.getMessage());
                throw e;
            }
        }

        return todosPedidos;
    }

    private List<PedidoDto> filtrarPedidosPorFecha(List<PedidoDto> pedidos, LocalDate from, LocalDate to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        
        return pedidos.stream()
            .filter(pedido -> {
                try {
                    LocalDate fechaPedido = LocalDate.parse(pedido.getFechaPedido(), formatter);
                    return !fechaPedido.isBefore(from) && !fechaPedido.isAfter(to);
                } catch (Exception e) {
                    log.warn("Error parseando fecha del pedido {}: {}", pedido.getId(), e.getMessage());
                    return false;
                }
            })
            .collect(Collectors.toList());
    }

    private SalesSummaryDto calcularResumenVentas(List<PedidoDto> pedidos) {
        int totalOrders = pedidos.size();
        Double totalRevenue = pedidos.stream()
            .mapToDouble(PedidoDto::getTotal)
            .sum();
        Double averagePerOrder = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        List<ChartDataDto> chartData = generarDatosGrafico(pedidos);

        return new SalesSummaryDto(totalOrders, totalRevenue, averagePerOrder, chartData);
    }

    private List<ChartDataDto> generarDatosGrafico(List<PedidoDto> pedidos) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy");

        // Agrupar pedidos por fecha para calcular tanto el valor total como la cantidad
        Map<LocalDate, List<PedidoDto>> pedidosPorDia = pedidos.stream()
            .collect(Collectors.groupingBy(
                pedido -> {
                    try {
                        return LocalDate.parse(pedido.getFechaPedido(), formatter);
                    } catch (Exception e) {
                        log.warn("Error parseando fecha para gráfico del pedido {}: {}", pedido.getId(), e.getMessage());
                        return LocalDate.now();
                    }
                }
            ));

        return pedidosPorDia.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                LocalDate fecha = entry.getKey();
                List<PedidoDto> pedidosDelDia = entry.getValue();
                
                // Calcular valor total de ventas del día
                Double valorTotal = pedidosDelDia.stream()
                    .mapToDouble(PedidoDto::getTotal)
                    .sum();
                
                // Calcular cantidad de pedidos del día
                Integer cantidadPedidos = pedidosDelDia.size();
                
                return new ChartDataDto(
                    fecha.format(labelFormatter),
                    valorTotal,
                    cantidadPedidos
                );
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public TopProductsSummaryDto getTopProducts(LocalDate from, LocalDate to, String authToken) {
        log.info("Obteniendo ranking de productos más vendidos desde {} hasta {}", from, to);
        
        try {
            List<PedidoDto> todosPedidos = obtenerTodosLosPedidos(authToken);
            log.info("Se obtuvieron {} pedidos totales", todosPedidos.size());
            
            List<PedidoDto> pedidosFiltrados = filtrarPedidosPorFecha(todosPedidos, from, to);
            log.info("Se filtraron {} pedidos para el rango de fechas", pedidosFiltrados.size());
            
            return calcularTopProductos(pedidosFiltrados, from, to);
            
        } catch (Exception e) {
            log.error("Error al obtener ranking de productos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar datos de productos: " + e.getMessage());
        }
    }
    
    private TopProductsSummaryDto calcularTopProductos(List<PedidoDto> pedidos, LocalDate from, LocalDate to) {
        // Extraer todos los productos de todos los pedidos
        List<ProductoDto> todosLosProductos = pedidos.stream()
            .flatMap(pedido -> pedido.getProductos().stream())
            .collect(Collectors.toList());
        
        // Agrupar por productoId y calcular estadísticas
        Map<Long, List<ProductoDto>> productosPorId = todosLosProductos.stream()
            .collect(Collectors.groupingBy(ProductoDto::getProductoId));
        
        // Crear lista de TopProductDto
        List<TopProductDto> topProductos = productosPorId.entrySet().stream()
            .map(entry -> {
                Long productoId = entry.getKey();
                List<ProductoDto> productos = entry.getValue();
                
                // Calcular estadísticas del producto
                String nombreProducto = productos.get(0).getNombreProducto();
                Integer cantidadTotal = productos.stream()
                    .mapToInt(ProductoDto::getCantidad)
                    .sum();
                Integer numeroPedidos = (int) pedidos.stream()
                    .filter(pedido -> pedido.getProductos().stream()
                        .anyMatch(producto -> producto.getProductoId().equals(productoId)))
                    .count();
                Double ingresosTotales = productos.stream()
                    .mapToDouble(ProductoDto::getSubtotal)
                    .sum();
                Double precioPromedio = productos.stream()
                    .mapToDouble(ProductoDto::getPrecioUnitario)
                    .average()
                    .orElse(0.0);
                
                return new TopProductDto(
                    productoId,
                    nombreProducto,
                    cantidadTotal,
                    numeroPedidos,
                    ingresosTotales,
                    precioPromedio
                );
            })
            .sorted(Comparator.comparing(TopProductDto::getCantidadTotalVendida).reversed())
            .collect(Collectors.toList());
        
        // Calcular métricas generales
        Integer totalProductosUnicos = topProductos.size();
        Integer totalUnidadesVendidas = topProductos.stream()
            .mapToInt(TopProductDto::getCantidadTotalVendida)
            .sum();
        Double valorTotalVentas = topProductos.stream()
            .mapToDouble(TopProductDto::getIngresosTotales)
            .sum();
        
        // Crear filtros aplicados
        PaymentFilterDto filtros = new PaymentFilterDto(from.toString(), to.toString());
        
        return new TopProductsSummaryDto(
            filtros,
            totalProductosUnicos,
            totalUnidadesVendidas,
            valorTotalVentas,
            topProductos
        );
    }
    
    @Override
    public TopCustomersSummaryDto getTopCustomers(LocalDate from, LocalDate to, String authToken) {
        log.info("Obteniendo ranking de clientes más frecuentes desde {} hasta {}", from, to);
        
        try {
            List<PedidoDto> todosPedidos = obtenerTodosLosPedidos(authToken);
            log.info("Se obtuvieron {} pedidos totales", todosPedidos.size());
            
            List<PedidoDto> pedidosFiltrados = filtrarPedidosPorFecha(todosPedidos, from, to);
            log.info("Se filtraron {} pedidos para el rango de fechas", pedidosFiltrados.size());
            
            return calcularTopClientes(pedidosFiltrados, from, to);
            
        } catch (Exception e) {
            log.error("Error al obtener ranking de clientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar datos de clientes: " + e.getMessage());
        }
    }
    
    private TopCustomersSummaryDto calcularTopClientes(List<PedidoDto> pedidos, LocalDate from, LocalDate to) {
        // Agrupar pedidos por usuarioId
        Map<Long, List<PedidoDto>> pedidosPorUsuario = pedidos.stream()
            .collect(Collectors.groupingBy(PedidoDto::getUsuarioId));
        
        // Crear lista de TopCustomerDto
        List<TopCustomerDto> topClientes = pedidosPorUsuario.entrySet().stream()
            .map(entry -> {
                Long usuarioId = entry.getKey();
                List<PedidoDto> pedidosDelUsuario = entry.getValue();
                
                // Obtener información básica del usuario (del primer pedido)
                PedidoDto primerPedido = pedidosDelUsuario.get(0);
                String nombreCompleto = primerPedido.getNombreCompletoUsuario();
                String email = primerPedido.getEmail();
                
                // Calcular estadísticas del cliente
                Integer cantidadOrdenes = pedidosDelUsuario.size();
                Double dineroTotalGastado = pedidosDelUsuario.stream()
                    .mapToDouble(PedidoDto::getTotal)
                    .sum();
                Double promedioGastadoPorOrden = cantidadOrdenes > 0 ? dineroTotalGastado / cantidadOrdenes : 0.0;
                
                // Calcular cantidad total de productos comprados
                Integer cantidadProductosComprados = pedidosDelUsuario.stream()
                    .flatMap(pedido -> pedido.getProductos().stream())
                    .mapToInt(ProductoDto::getCantidad)
                    .sum();
                
                // Encontrar fechas de primera y última compra
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
                String primeraCompra = pedidosDelUsuario.stream()
                    .map(PedidoDto::getFechaPedido)
                    .filter(fecha -> fecha != null && !fecha.trim().isEmpty())
                    .map(fecha -> {
                        try {
                            return LocalDate.parse(fecha, formatter);
                        } catch (Exception e) {
                            return LocalDate.now();
                        }
                    })
                    .min(LocalDate::compareTo)
                    .map(LocalDate::toString)
                    .orElse("N/A");
                
                String ultimaCompra = pedidosDelUsuario.stream()
                    .map(PedidoDto::getFechaPedido)
                    .filter(fecha -> fecha != null && !fecha.trim().isEmpty())
                    .map(fecha -> {
                        try {
                            return LocalDate.parse(fecha, formatter);
                        } catch (Exception e) {
                            return LocalDate.now();
                        }
                    })
                    .max(LocalDate::compareTo)
                    .map(LocalDate::toString)
                    .orElse("N/A");
                
                return new TopCustomerDto(
                    usuarioId,
                    nombreCompleto,
                    email,
                    cantidadOrdenes,
                    dineroTotalGastado,
                    promedioGastadoPorOrden,
                    cantidadProductosComprados,
                    primeraCompra,
                    ultimaCompra
                );
            })
            .sorted(Comparator.comparing(TopCustomerDto::getDineroTotalGastado).reversed())
            .collect(Collectors.toList());
        
        // Calcular métricas generales
        Integer totalClientesUnicos = topClientes.size();
        Integer totalOrdenesProcesadas = pedidos.size();
        Double ingresosTotales = topClientes.stream()
            .mapToDouble(TopCustomerDto::getDineroTotalGastado)
            .sum();
        Double promedioOrdenesPorCliente = totalClientesUnicos > 0 ? 
            (double) totalOrdenesProcesadas / totalClientesUnicos : 0.0;
        Double promedioGastadoPorCliente = totalClientesUnicos > 0 ? 
            ingresosTotales / totalClientesUnicos : 0.0;
        
        // Crear filtros aplicados
        PaymentFilterDto filtros = new PaymentFilterDto(from.toString(), to.toString());
        
        return new TopCustomersSummaryDto(
            filtros,
            totalClientesUnicos,
            totalOrdenesProcesadas,
            ingresosTotales,
            promedioOrdenesPorCliente,
            promedioGastadoPorCliente,
            topClientes
        );
    }
} 