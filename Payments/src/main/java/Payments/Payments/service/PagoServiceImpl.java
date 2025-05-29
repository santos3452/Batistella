package Payments.Payments.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PreferenceClient preferenceClient;
    private final Environment environment;
    private final HttpClientService httpClientService;
    
    @Value("${app.url.base}")
    private String appBaseUrl;
    
    @Override
    @SecurityRequirement(name = "bearerAuth")
    public PagoResponseDTO crearPreferenciaMercadoPago(PreferenceDTO preferenceDTO) {
        try {
            // Obtener token del Bearer Authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String token = null;
            
            if (authentication != null && authentication.getCredentials() != null) {
                token = authentication.getCredentials().toString();
                log.info("Token Bearer recibido correctamente");
            }
            
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
                    .estado("CANCELADO")
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
                case "pending":
                    pago.setEstado("PENDIENTE");
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
    public Pago procesarRetornoPago(String paymentId, String status, String externalReference, String authToken) {
        log.info("Procesando retorno de pago - paymentId: {}, status: {}, pedido: {}", 
                paymentId, status, externalReference);
        
        Pago pago = null;
        if (paymentId != null && !paymentId.isEmpty()) {
            pago = pagoRepository.findByMercadoPagoPaymentId(paymentId);
            if (pago != null) {
                pago = actualizarEstadoPago(pago, paymentId, status);
                
                // Si el pago está completado, obtener detalles del pedido y enviar notificación
                if ("COMPLETADO".equals(pago.getEstado())) {
                    try {
                        enviarNotificacionPedidoCompletado(pago, authToken);
                    } catch (Exception e) {
                        log.error("Error al enviar notificación de pedido completado: {}", e.getMessage());
                    }
                }
                
                return pago;
            }
        }
        
        if (externalReference != null && !externalReference.isEmpty()) {
            Optional<Pago> pagoOpt = pagoRepository.findLatestByCodigoPedido(externalReference);
            if (pagoOpt.isPresent()) {
                pago = pagoOpt.get();
                pago = actualizarEstadoPago(pago, paymentId, status);
                
                // Si el pago está completado, obtener detalles del pedido y enviar notificación
                if ("COMPLETADO".equals(pago.getEstado())) {
                    try {
                        enviarNotificacionPedidoCompletado(pago, authToken);
                    } catch (Exception e) {
                        log.error("Error al enviar notificación de pedido completado: {}", e.getMessage());
                    }
                }
                
                return pago;
            }
        }
        
        log.warn("No se encontró registro de pago para actualizar: paymentId={}, pedido={}", 
                paymentId, externalReference);
        return null;
    }

    @Override
    public void cambiarEstadoPago(String codigoPedido, String status, String authToken) {
        log.info("Cambiando estado de pago para pedido: {} a: {}", codigoPedido, status);
        Optional<Pago> pagoOpt = pagoRepository.findLatestByCodigoPedido(codigoPedido);
        
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            pago.setEstado(status);
            
            // Actualizar fecha de pago si el estado es COMPLETADO
            if ("COMPLETADO".equals(status)) {
                pago.setFechaPago(LocalDateTime.now());
                
                // Enviar notificación de pago completado
                try {
                    enviarNotificacionPedidoCompletado(pago, authToken);
                } catch (Exception e) {
                    log.error("Error al enviar notificación de pedido completado: {}", e.getMessage());
                }
            }
            
            pagoRepository.save(pago);
            log.info("Estado de pago actualizado para pedido: {}", codigoPedido);
        } else {
            log.warn("No se encontró registro de pago para el código de pedido: {}", codigoPedido);
            throw new PagoNotFoundException(codigoPedido);
        }
    }

    /**
     * Método para enviar notificación de pedido completado
     * @param pago Objeto pago con los datos del pago completado
     * @param authToken Token de autorización a usar
     */
    private void enviarNotificacionPedidoCompletado(Pago pago, String authToken) {
        log.info("Enviando notificación de pedido completado para el pedido: {}", pago.getCodigoPedido());
        
        // Usar el token proporcionado o intentar obtenerlo del contexto de seguridad si no se proporcionó
        String token = authToken;
        
        if (token == null || token.isEmpty()) {
            // Intentar obtener token del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() != null) {
                token = authentication.getCredentials().toString();
                log.info("Token obtenido del SecurityContext para la notificación");
            } else {
                log.warn("No se encontró token de autorización para consultar el pedido");
                return; // No hay token, no podemos continuar
            }
        } else {
            log.info("Usando token proporcionado para la notificación");
        }
        
        // Consultar detalles del pedido
        Map<String, Object> pedidoData = httpClientService.obtenerDetallesPedido(pago.getCodigoPedido(), token);
        
        if (pedidoData == null) {
            log.warn("No se pudo obtener información del pedido: {}", pago.getCodigoPedido());
            return;
        }
        
        // Actualizar datos del estado de pago en el objeto pedido
        pedidoData.put("estadoPago", pago.getEstado());
        pedidoData.put("metodoPago", pago.getMetodo());
        pedidoData.put("fechaPago", pago.getFechaPago() != null ? pago.getFechaPago().toString() : null);
        
        // Extraer email del usuario desde los datos del pedido
        String email = null;
        if (pedidoData.containsKey("email")) {
            email = (String) pedidoData.get("email");
        } else if (pedidoData.containsKey("sub")) {
            email = (String) pedidoData.get("sub");
        }
        
        // Si no hay email en los datos del pedido, intentar extraerlo del token
        if (email == null || email.isEmpty()) {
            try {
                // Extraer email del payload del token (suponiendo que está en formato JWT)
                String[] tokenParts = token.split("\\.");
                if (tokenParts.length == 3) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[1]));
                    // Intentar extraer el email del payload (esto depende del formato exacto del token)
                    if (payload.contains("\"sub\":")) {
                        String[] parts = payload.split("\"sub\":");
                        if (parts.length > 1) {
                            String subPart = parts[1].split(",")[0];
                            email = subPart.replaceAll("\"", "").trim();
                            log.info("Email extraído del token JWT: {}", email);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo extraer el email del token: {}", e.getMessage());
            }
        }
        
        // Si no se encontró email, no podemos enviar la notificación
        if (email == null || email.isEmpty()) {
            log.warn("No se pudo determinar el email del usuario para el pedido: {}", pago.getCodigoPedido());
            return;
        }
        
        // Enviar notificación
        boolean enviado = httpClientService.enviarNotificacionPago(email, pedidoData);
        
        if (enviado) {
            log.info("Notificación de pago enviada exitosamente al email: {}", email);
        } else {
            log.warn("No se pudo enviar la notificación de pago al email: {}", email);
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
            case "pending":
                pago.setEstado("PENDIENTE");
                break;
            default:
                pago.setEstado("CANCELADO");
                break;
        }
        log.info("Actualizando pago ID: {}, Código pedido: {}, Estado: {}", 
                pago.getId(), pago.getCodigoPedido(), pago.getEstado());
        return pagoRepository.save(pago);
    }
} 