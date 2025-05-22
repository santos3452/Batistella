package Payments.Payments.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

import Payments.Payments.dto.PagoResponseDTO;
import Payments.Payments.dto.PreferenceDTO;
import Payments.Payments.model.Pago;
import Payments.Payments.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PreferenceClient preferenceClient;
    private final Environment environment;
    
    @Value("${app.url.base}")
    private String appBaseUrl;
    
    /**
     * Crea una preferencia de pago en Mercado Pago
     */
    public PagoResponseDTO crearPreferenciaMercadoPago(PreferenceDTO preferenceDTO) {
        try {
            log.info("Creando preferencia de pago para pedido: {}", preferenceDTO.getPedidoId());
            log.info("Cantidad de ítems: {}", preferenceDTO.getItems().size());
            
            // Preparar los ítems para la preferencia de Mercado Pago con más detalles
            List<PreferenceItemRequest> items = preferenceDTO.getItems().stream()
                    .map(item -> {
                        log.info("Ítem: {}, Cantidad: {}, Precio: {}", 
                            item.getTitulo(), item.getCantidad(), item.getPrecioUnitario());
                        return PreferenceItemRequest.builder()
                            .title(item.getTitulo())
                            .description(item.getDescripcion() != null && !item.getDescripcion().isEmpty() 
                                ? item.getDescripcion() 
                                : "Producto: " + item.getTitulo())
                            .quantity(item.getCantidad())
                            .unitPrice(item.getPrecioUnitario())
                            .id(item.getTitulo().replaceAll("\\s+", "_").toLowerCase())
                            .build();
                    })
                    .toList();
            
            // Verificar si estamos en modo desarrollo y validar la URL base
            if (appBaseUrl.contains("localhost") || appBaseUrl.contains("127.0.0.1")) {
                log.warn("¡ATENCIÓN! Estás usando localhost como URL base. Mercado Pago NO aceptará estas URLs para redirecciones.");
                log.warn("Configura ngrok u otra herramienta similar para obtener una URL pública. Ver README-NGROK.md para más información.");
            }
            
            // Configurar URLs de retorno
            String urlExito = preferenceDTO.getUrlExito() != null ? 
                    preferenceDTO.getUrlExito() : appBaseUrl + "/pagos/exito";
            String urlFracaso = preferenceDTO.getUrlFracaso() != null ? 
                    preferenceDTO.getUrlFracaso() : appBaseUrl + "/pagos/fracaso";
            String urlPendiente = preferenceDTO.getUrlPendiente() != null ? 
                    preferenceDTO.getUrlPendiente() : appBaseUrl + "/pagos/pendiente";
            
            log.info("URLs de redirección configuradas:");
            log.info("URL Éxito: {}", urlExito);
            log.info("URL Fracaso: {}", urlFracaso);
            log.info("URL Pendiente: {}", urlPendiente);
                    
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(urlExito)
                    .failure(urlFracaso)
                    .pending(urlPendiente)
                    .build();
            
            // Crear metadatos adicionales para mejorar la visualización
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("order_id", preferenceDTO.getPedidoId().toString());
            metadata.put("order_type", "ecommerce_purchase");
            if (preferenceDTO.getDescripcion() != null) {
                metadata.put("description", preferenceDTO.getDescripcion());
            }
            
            // Crear la preferencia con más información
            var preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(preferenceDTO.getPedidoId().toString())
                    .statementDescriptor("Batistella Shop")
                    .metadata(metadata)
                    .autoReturn("approved")
                    .build();
            
            var preference = preferenceClient.create(preferenceRequest);
            
            // Guardar el registro de pago en base de datos
            Pago pago = Pago.builder()
                    .pedidoId(preferenceDTO.getPedidoId())
                    .monto(preferenceDTO.getMontoTotal())
                    .metodo("mercadopago")
                    .fechaPago(null) // Se actualizará cuando se complete el pago
                    .estado("PENDIENTE")
                    .mercadoPagoPreferenceId(preference.getId())
                    .build();
            
            pago = pagoRepository.save(pago);
            
            // Construir y retornar la respuesta
            return PagoResponseDTO.builder()
                    .id(pago.getId())
                    .pedidoId(pago.getPedidoId())
                    .monto(pago.getMonto())
                    .metodo(pago.getMetodo())
                    .fechaPago(pago.getFechaPago())
                    .estado(pago.getEstado())
                    .mercadoPagoPreferenceId(pago.getMercadoPagoPreferenceId())
                    .urlPago(preference.getInitPoint())
                    .build();
            
        } catch (MPException | MPApiException e) {
            log.error("Error al crear la preferencia en Mercado Pago", e);
            throw new RuntimeException("Error al procesar el pago con Mercado Pago", e);
        }
    }
    
    /**
     * Procesa una notificación de pago (webhook) de Mercado Pago
     */
    public void procesarNotificacionPago(String paymentId, String status) {
        Pago pago = pagoRepository.findByMercadoPagoPaymentId(paymentId);
        
        if (pago != null) {
            pago.setMercadoPagoStatus(status);
            
            switch (status) {
                case "approved":
                    pago.setEstado("COMPLETADO");
                    pago.setFechaPago(LocalDateTime.now());
                    break;
                case "rejected":
                    pago.setEstado("RECHAZADO");
                    break;
                case "in_process":
                    pago.setEstado("EN_PROCESO");
                    break;
                default:
                    pago.setEstado("PENDIENTE");
                    break;
            }
            
            pagoRepository.save(pago);
        }
    }
    
    /**
     * Encuentra un pago por su ID
     */
    public Optional<Pago> buscarPorId(Long id) {
        return pagoRepository.findById(id);
    }
    
    /**
     * Lista todos los pagos de un pedido
     */
    public List<Pago> listarPagosPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId);
    }
    
    /**
     * Registra un pago manual (efectivo, transferencia, etc.)
     */
    public Pago registrarPagoManual(Long pedidoId, BigDecimal monto, String metodo) {
        Pago pago = Pago.builder()
                .pedidoId(pedidoId)
                .monto(monto)
                .metodo(metodo)
                .fechaPago(LocalDateTime.now())
                .estado("COMPLETADO")
                .build();
        
        return pagoRepository.save(pago);
    }
    
    /**
     * Guarda una entidad Pago en la base de datos
     */
    public Pago registrarPago(Pago pago) {
        return pagoRepository.save(pago);
    }
} 