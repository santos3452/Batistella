package Payments.Payments.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpClientService {

    private final RestTemplate restTemplate;
    
    @Value("${microservice.pedidos.url:http://productsandorder:8083}")
    private String pedidosServiceUrl;
    
    @Value("${microservice.notifications.url:http://notifications:8085}")
    private String notificationsServiceUrl;
    
    /**
     * Obtiene los detalles de un pedido desde el microservicio de pedidos
     * @param codigoPedido Código del pedido a consultar
     * @param authToken Token de autorización (Bearer)
     * @return Datos del pedido en formato Map
     */
    public Map<String, Object> obtenerDetallesPedido(String codigoPedido, String authToken) {
        String url = pedidosServiceUrl + "/api/pedidos/codigo/" + codigoPedido;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            log.info("Consultando detalles del pedido {} en microservicio: {}", codigoPedido, url);
            log.info("Token usado (primeros 20 caracteres): {}", 
                    authToken.substring(0, Math.min(authToken.length(), 20)) + "...");
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity,
                    Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Pedido {} obtenido exitosamente", codigoPedido);
                @SuppressWarnings("unchecked")
                Map<String, Object> pedidoData = response.getBody();
                return pedidoData;
            } else {
                log.warn("No se pudo obtener el pedido {} - Respuesta: {}", codigoPedido, response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.error("Error de cliente HTTP al consultar el pedido {}: {} - {}", 
                    codigoPedido, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (HttpServerErrorException e) {
            log.error("Error de servidor HTTP al consultar el pedido {}: {} - {}", 
                    codigoPedido, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Error al consultar el pedido {}: {}", codigoPedido, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Envía una notificación de confirmación de pago al microservicio de notificaciones
     * @param email Email del usuario
     * @param pedidoData Datos del pedido
     * @return true si la notificación se envió correctamente
     */
    public boolean enviarNotificacionPago(String email, Map<String, Object> pedidoData) {
        String url = notificationsServiceUrl + "/api/notifications/payment/confirmation?email=" + email;
        
        try {
            log.info("Enviando notificación de pago para el pedido {} al email {} - URL: {}", 
                    pedidoData.get("codigoPedido"), email, url);
            
            ResponseEntity<Void> response = restTemplate.postForEntity(url, pedidoData, Void.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notificación de pago enviada exitosamente");
                return true;
            } else {
                log.warn("No se pudo enviar la notificación de pago - Respuesta: {}", response.getStatusCode());
                return false;
            }
        } catch (HttpClientErrorException e) {
            log.error("Error de cliente HTTP al enviar notificación: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Error de servidor HTTP al enviar notificación: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Error al enviar notificación de pago: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 