package Notifications.Notifications.service;

import Notifications.Notifications.dto.PedidoDTO;

/**
 * Servicio para la gestión de plantillas de correos electrónicos
 */
public interface EmailTemplateService {
    
    /**
     * Genera el contenido HTML para un correo de bienvenida
     * 
     * @param name Nombre del usuario
     * @return Contenido HTML del correo
     */
    String generateWelcomeEmailContent(String name);
    
    /**
     * Genera el contenido HTML para un correo de bienvenida con formato alternativo basado en tablas
     * 
     * @param name Nombre del usuario
     * @return Contenido HTML del correo
     */
    String generateAlternativeWelcomeEmailContent(String name);
    
    /**
     * Genera el contenido HTML para un correo simple de prueba
     * 
     * @param name Nombre del usuario
     * @return Contenido HTML del correo
     */
    String generateSimpleEmailContent(String name);
    
    /**
     * Genera el contenido HTML para un correo de confirmación de pago
     * 
     * @param pedido Información del pedido confirmado
     * @return Contenido HTML del correo
     */
    String generatePaymentConfirmationEmailContent(PedidoDTO pedido);
    
    /**
     * Genera el contenido HTML para un correo de notificación de cambio de estado de pedido
     * 
     * @param estadoPedido Estado actual del pedido
     * @param codigoPedido Código identificador del pedido
     * @return Contenido HTML del correo
     */
    String generateOrderStatusChangeEmailContent(String estadoPedido, String codigoPedido);
} 