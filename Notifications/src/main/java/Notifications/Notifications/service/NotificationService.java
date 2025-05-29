package Notifications.Notifications.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import Notifications.Notifications.entity.Notification;

public interface NotificationService {

    Notification saveNotification(Notification notification);
    
    List<Notification> findAllNotifications();
    
    Optional<Notification> findNotificationById(Long id);
    
    List<Notification> findNotificationsByRecipient(String recipient);
    
    List<Notification> findNotificationsByType(String type);
    
    List<Notification> findNotificationsBySentAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Notification> findNotificationsBySuccess(boolean success);
    
    void deleteNotification(Long id);
    
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
    Notification sendAlternativeWelcomeEmail(String email, String name);
    
    /**
     * Envía un correo con formato simple para pruebas
     * 
     * @param email Email del usuario
     * @param name Nombre del usuario
     * @return La notificación creada
     */
    Notification sendSimpleEmail(String email, String name);
} 