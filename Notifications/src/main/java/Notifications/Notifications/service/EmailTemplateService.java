package Notifications.Notifications.service;

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
} 