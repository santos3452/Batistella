package Dashboards.Dashboards.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import Dashboards.Dashboards.model.dto.MostUsedPaymentMethodDto;
import Dashboards.Dashboards.model.dto.PagoDto;
import Dashboards.Dashboards.model.dto.PaymentFilterDto;
import Dashboards.Dashboards.model.dto.PaymentMethodSummaryDto;
import Dashboards.Dashboards.model.dto.PaymentStatusSummaryDto;
import Dashboards.Dashboards.model.dto.PaymentsSummaryDto;
import Dashboards.Dashboards.service.PaymentsDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentsDashboardServiceImpl implements PaymentsDashboardService {

    private final WebClient webClient;

    @Value("${microservices.payments.base-url}")
    private String paymentsBaseUrl;

    @Value("${microservices.payments.endpoint.todos-pagos}")
    private String todosLosPagosEndpoint;

    @Override
    public PaymentsSummaryDto getPaymentsSummary(LocalDate from, LocalDate to, String authToken) {
        log.info("Obteniendo resumen de pagos desde {} hasta {}", from, to);
        
        try {
            List<PagoDto> todosPagos = obtenerTodosLosPagos(authToken);
            log.info("Se obtuvieron {} pagos totales", todosPagos.size());
            
            List<PagoDto> pagosFiltrados = filtrarPagosPorFecha(todosPagos, from, to);
            log.info("Se filtraron {} pagos para el rango de fechas", pagosFiltrados.size());
            
            return calcularResumenPagos(pagosFiltrados, from, to);
            
        } catch (Exception e) {
            log.error("Error al obtener resumen de pagos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar datos de pagos: " + e.getMessage());
        }
    }

    private List<PagoDto> obtenerTodosLosPagos(String authToken) {
        log.info("Comunicándose con microservicio de pagos");

        try {
            String fullUrl = paymentsBaseUrl + todosLosPagosEndpoint;
            log.debug("URI para pagos: {}", fullUrl);

            List<PagoDto> pagos = webClient.get()
                .uri(fullUrl)
                .header("Authorization", authToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Error 4xx del microservicio de pagos: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                        .map(body -> new RuntimeException("Error 4xx: " + response.statusCode() + " - " + body));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Error 5xx del microservicio de pagos: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                        .map(body -> new RuntimeException("Error 5xx: " + response.statusCode() + " - " + body));
                })
                .bodyToMono(new ParameterizedTypeReference<List<PagoDto>>() {})
                .onErrorMap(throwable -> {
                    log.error("Error al llamar al microservicio de pagos: {}", throwable.getMessage());
                    return new RuntimeException("Error de comunicación con el servicio de pagos: " + throwable.getMessage());
                })
                .block();

            return pagos != null ? pagos : new ArrayList<>();

        } catch (Exception e) {
            log.error("Error obteniendo pagos: {}", e.getMessage());
            throw e;
        }
    }

    private List<PagoDto> filtrarPagosPorFecha(List<PagoDto> pagos, LocalDate from, LocalDate to) {
        return pagos.stream()
            .filter(pago -> {
                // Si tiene fechaPago, validar que esté en el rango
                if (pago.getFechaPago() != null && !pago.getFechaPago().trim().isEmpty()) {
                    try {
                        // Parsear la fecha en formato ISO: 2025-05-29T16:08:38.564273
                        LocalDateTime fechaPago = LocalDateTime.parse(pago.getFechaPago());
                        LocalDate fechaPagoDate = fechaPago.toLocalDate();
                        
                        return !fechaPagoDate.isBefore(from) && !fechaPagoDate.isAfter(to);
                    } catch (DateTimeParseException e) {
                        log.warn("Error parseando fecha del pago {}: {} - {}", pago.getId(), pago.getFechaPago(), e.getMessage());
                        return false;
                    }
                } else {
                    // Para pagos sin fechaPago (CANCELADOS, PENDIENTES, etc.), incluirlos en el análisis
                    // Asumimos que son recientes y relevantes para las estadísticas
                    log.debug("Pago {} sin fecha de pago (estado: {}), se incluye en el análisis", 
                             pago.getId(), pago.getEstado());
                    return true;
                }
            })
            .collect(Collectors.toList());
    }

    private PaymentsSummaryDto calcularResumenPagos(List<PagoDto> pagos, LocalDate from, LocalDate to) {
        // Filtrar solo pagos completados para estadísticas monetarias
        List<PagoDto> pagosCompletados = pagos.stream()
            .filter(pago -> "COMPLETADO".equalsIgnoreCase(pago.getEstado()))
            .collect(Collectors.toList());

        // Calcular métricas básicas
        int totalPagos = pagos.size();
        Double totalMontoPagado = pagosCompletados.stream()
            .mapToDouble(PagoDto::getMonto)
            .sum();

        // Crear filtros aplicados
        PaymentFilterDto filtros = new PaymentFilterDto(from.toString(), to.toString());

        // Calcular resumen por método
        List<PaymentMethodSummaryDto> resumenPorMetodo = calcularResumenPorMetodo(pagos);

        // Calcular resumen por estado
        List<PaymentStatusSummaryDto> resumenPorEstado = calcularResumenPorEstado(pagos);

        // Encontrar método más usado
        MostUsedPaymentMethodDto medioPagoMasUsado = encontrarMetodoMasUsado(resumenPorMetodo);

        return new PaymentsSummaryDto(
            filtros,
            totalPagos,
            totalMontoPagado,
            medioPagoMasUsado,
            resumenPorMetodo,
            resumenPorEstado
        );
    }

    private List<PaymentMethodSummaryDto> calcularResumenPorMetodo(List<PagoDto> pagos) {
        int totalPagos = pagos.size();
        
        Map<String, List<PagoDto>> pagosPorMetodo = pagos.stream()
            .collect(Collectors.groupingBy(PagoDto::getMetodo));

        return pagosPorMetodo.entrySet().stream()
            .map(entry -> {
                String metodo = entry.getKey();
                List<PagoDto> pagosDelMetodo = entry.getValue();
                
                int cantidad = pagosDelMetodo.size();
                double porcentaje = totalPagos > 0 ? (double) cantidad / totalPagos * 100 : 0.0;
                
                // Calcular monto total solo de pagos completados
                double montoTotal = pagosDelMetodo.stream()
                    .filter(pago -> "COMPLETADO".equalsIgnoreCase(pago.getEstado()))
                    .mapToDouble(PagoDto::getMonto)
                    .sum();

                return new PaymentMethodSummaryDto(metodo, cantidad, porcentaje, montoTotal);
            })
            .sorted(Comparator.comparing(PaymentMethodSummaryDto::getCantidad).reversed())
            .collect(Collectors.toList());
    }

    private List<PaymentStatusSummaryDto> calcularResumenPorEstado(List<PagoDto> pagos) {
        int totalPagos = pagos.size();
        
        Map<String, Long> pagosPorEstado = pagos.stream()
            .collect(Collectors.groupingBy(PagoDto::getEstado, Collectors.counting()));

        return pagosPorEstado.entrySet().stream()
            .map(entry -> {
                String estado = entry.getKey();
                long cantidad = entry.getValue();
                double porcentaje = totalPagos > 0 ? (double) cantidad / totalPagos * 100 : 0.0;

                return new PaymentStatusSummaryDto(estado, (int) cantidad, porcentaje);
            })
            .sorted(Comparator.comparing(PaymentStatusSummaryDto::getCantidad).reversed())
            .collect(Collectors.toList());
    }

    private MostUsedPaymentMethodDto encontrarMetodoMasUsado(List<PaymentMethodSummaryDto> resumenPorMetodo) {
        return resumenPorMetodo.stream()
            .max(Comparator.comparing(PaymentMethodSummaryDto::getCantidad))
            .map(metodo -> new MostUsedPaymentMethodDto(
                metodo.getMetodo(),
                metodo.getCantidad(),
                metodo.getPorcentaje()
            ))
            .orElse(new MostUsedPaymentMethodDto("N/A", 0, 0.0));
    }
} 