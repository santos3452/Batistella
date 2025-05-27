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
import Payments.Payments.exception.PagoNotFoundException;
import Payments.Payments.model.Pago;
import Payments.Payments.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PreferenceClient preferenceClient;
    private final Environment environment;
    
    @Value("${app.url.base}")
    private String appBaseUrl;
    
    @Override
    public PagoResponseDTO crearPreferenciaMercadoPago(PreferenceDTO preferenceDTO) {
        try {
            log.info("Creando preferencia de pago para pedido: {}", preferenceDTO.getCodigoPedido());
            log.info("Cantidad de ítems: {}", preferenceDTO.getItems().size());
            
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
            
            if (appBaseUrl.contains("localhost") || appBaseUrl.contains("127.0.0.1")) {
                log.warn("¡ATENCIÓN! Estás usando localhost como URL base. Mercado Pago NO aceptará estas URLs para redirecciones.");
                log.warn("Configura ngrok u otra herramienta similar para obtener una URL pública. Ver README-NGROK.md para más información.");
            }
            
            String urlExito = preferenceDTO.getUrlExito() != null ? 
                    preferenceDTO.getUrlExito() : appBaseUrl + "/api/pagos/redirect/success";
            String urlFracaso = preferenceDTO.getUrlFracaso() != null ? 
                    preferenceDTO.getUrlFracaso() : appBaseUrl + "/api/pagos/redirect/error";
            String urlPendiente = preferenceDTO.getUrlPendiente() != null ? 
                    preferenceDTO.getUrlPendiente() : appBaseUrl + "/api/pagos/redirect/pending";
            
            log.info("URLs de redirección configuradas:");
            log.info("URL Éxito: {}", urlExito);
            log.info("URL Fracaso: {}", urlFracaso);
            log.info("URL Pendiente: {}", urlPendiente);
                    
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(urlExito)
                    .failure(urlFracaso)
                    .pending(urlPendiente)
                    .build();
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("order_id", preferenceDTO.getCodigoPedido());
            metadata.put("order_type", "ecommerce_purchase");
            if (preferenceDTO.getDescripcion() != null) {
                metadata.put("description", preferenceDTO.getDescripcion());
            }
            
            var preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(preferenceDTO.getCodigoPedido())
                    .statementDescriptor("Batistella Shop")
                    .metadata(metadata)
                    .autoReturn("approved")
                    .build();
            
            var preference = preferenceClient.create(preferenceRequest);
            
            Pago pago = Pago.builder()
                    .codigoPedido(preferenceDTO.getCodigoPedido())
                    .monto(preferenceDTO.getMontoTotal())
                    .metodo("mercadopago")
                    .fechaPago(null)
                    .estado("PENDIENTE")
                    .mercadoPagoPreferenceId(preference.getId())
                    .build();
            
            pago = pagoRepository.save(pago);
            
            return PagoResponseDTO.builder()
                    .id(pago.getId())
                    .codigoPedido(pago.getCodigoPedido())
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

    @Override
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

    @Override
    public PagoResponseDTO buscarPorCodigoDePedido(String codigoPedido) {
        Optional<Pago> pagoOpt = pagoRepository.findLatestByCodigoPedido(codigoPedido);
        if (pagoOpt.isPresent()) {
            PagoResponseDTO pagoResponseDTO = PagoResponseDTO.builder()
                    .codigoPedido(pagoOpt.get().getCodigoPedido())
                    .metodo(pagoOpt.get().getMetodo())
                    .fechaPago(pagoOpt.get().getFechaPago())
                    .estado(pagoOpt.get().getEstado())
                    .build();
            return pagoResponseDTO;
        }
        return null;
    }


    @Override
    public Pago registrarPagoManual(String codigoPedido, BigDecimal monto, String metodo) {
        Pago pago = Pago.builder()
                .codigoPedido(codigoPedido)
                .monto(monto)
                .metodo(metodo)
                .fechaPago(LocalDateTime.now())
                .estado("PENDIENTE")
                .build();
        
        return pagoRepository.save(pago);
    }

    @Override
    public Pago registrarPago(Pago pago) {
        return pagoRepository.save(pago);
    }

    @Override
    public Pago procesarRetornoPago(String paymentId, String status, String externalReference) {
        log.info("Procesando retorno de pago - paymentId: {}, status: {}, pedido: {}", 
                paymentId, status, externalReference);
        
        Pago pago = null;
        if (paymentId != null && !paymentId.isEmpty()) {
            pago = pagoRepository.findByMercadoPagoPaymentId(paymentId);
            if (pago != null) {
                return actualizarEstadoPago(pago, paymentId, status);
            }
        }
        
        if (externalReference != null && !externalReference.isEmpty()) {
            Optional<Pago> pagoOpt = pagoRepository.findLatestByCodigoPedido(externalReference);
            if (pagoOpt.isPresent()) {
                pago = pagoOpt.get();
                return actualizarEstadoPago(pago, paymentId, status);
            }
        }
        
        log.warn("No se encontró registro de pago para actualizar: paymentId={}, pedido={}", 
                paymentId, externalReference);
        return null;
    }

    @Override
    public void cambiarEstadoPago(String codigoPedido, String status) {
        log.info("Cambiando estado de pago para pedido: {} a: {}", codigoPedido, status);
        Optional<Pago> pagoOpt = pagoRepository.findLatestByCodigoPedido(codigoPedido);
        
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            pago.setEstado(status);
            
            // Actualizar fecha de pago si el estado es COMPLETADO
            if ("COMPLETADO".equals(status)) {
                pago.setFechaPago(LocalDateTime.now());
            }
            
            pagoRepository.save(pago);
            log.info("Estado de pago actualizado para pedido: {}", codigoPedido);
        } else {
            log.warn("No se encontró registro de pago para el código de pedido: {}", codigoPedido);
            throw new PagoNotFoundException(codigoPedido);
        }
    }

    private Pago actualizarEstadoPago(Pago pago, String paymentId, String status) {
        if (pago.getMercadoPagoPaymentId() == null && paymentId != null) {
            pago.setMercadoPagoPaymentId(paymentId);
        }
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
        log.info("Actualizando pago ID: {}, Código pedido: {}, Estado: {}", 
                pago.getId(), pago.getCodigoPedido(), pago.getEstado());
        return pagoRepository.save(pago);
    }
} 