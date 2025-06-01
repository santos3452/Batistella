package Notifications.Notifications.service;

import Notifications.Notifications.dto.PedidoDTO;
import Notifications.Notifications.entity.Notification;

public interface NotificationService {


    
    Notification sendNotification(String recipient, String type, String message);
    
    /**
     * Envía un correo de bienvenida al usuario
     * 
     * @param email Email del usuario
     * @param name Nombre del usuario
     * @return La notificación creada
     */
    Notification sendWelcomeEmail(String email, String name);
    
    /**
     * Envía un correo de bienvenida con formato alternativo
     * 
     * @param email Email del usuario
     * @param name Nombre del usuario
     * @return La notificación creada
     */
    
    /**
     * Envía un correo de confirmación de pago con los detalles del pedido
     * 
     * @param email Email del usuario
     * @param pedido Información del pedido confirmado
     * @return La notificación creada
     */
    Notification sendPaymentConfirmationEmail(String email, PedidoDTO pedido);
    
    /**
     * Envía un correo de notificación de cambio de estado del pedido
     * 
     * @param email Email del usuario
     * @param estadoPedido Estado actual del pedido
     * @param codigoPedido Código identificador del pedido
     * @return La notificación creada
     */
    Notification sendOrderStatusChangeEmail(String email, String estadoPedido, String codigoPedido);

} 