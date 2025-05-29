package Notifications.Notifications.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Notifications.Notifications.entity.Notification;
import Notifications.Notifications.repository.NotificationRepository;
import Notifications.Notifications.service.EmailService;
import Notifications.Notifications.service.EmailTemplateService;
import Notifications.Notifications.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, 
                                  EmailService emailService,
                                  EmailTemplateService emailTemplateService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> findAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public Optional<Notification> findNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public List<Notification> findNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipient(recipient);
    }

    @Override
    public List<Notification> findNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    @Override
    public List<Notification> findNotificationsBySentAtBetween(LocalDateTime start, LocalDateTime end) {
        return notificationRepository.findBySentAtBetween(start, end);
    }

    @Override
    public List<Notification> findNotificationsBySuccess(boolean success) {
        return notificationRepository.findBySuccess(success);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public Notification sendNotification(String recipient, String type, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        
        // Para tipos HTML o WELCOME, solo guardamos un mensaje genérico
        if (type.equals("HTML") || type.equals("WELCOME")) {
            notification.setMessage("Mensaje enviado correctamente");
        } else {
            // Para mensajes de texto plano, podemos limitar la longitud para evitar errores
            if (message != null && message.length() > 250) {
                notification.setMessage(message.substring(0, 247) + "...");
            } else {
                notification.setMessage(message);
            }
        }
        
        notification.setSentAt(LocalDateTime.now());
        
        try {
            // Enviar el correo electrónico según el tipo de notificación
            String subject = getSubjectByType(type);
            
            // Enviar como HTML si es tipo HTML o WELCOME
            if (type.equals("HTML") || type.equals("WELCOME")) {
                emailService.sendHtmlMessage(recipient, subject, message);
            } else {
                emailService.sendSimpleMessage(recipient, subject, message);
            }
            
            notification.setSuccess(true);
            return notificationRepository.save(notification);
        } catch (Exception e) {
            notification.setSuccess(false);
            
            // Limitar la longitud del mensaje de error para evitar problemas con VARCHAR
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 250) {
                errorMsg = errorMsg.substring(0, 247) + "...";
            }
            notification.setErrorMessage(errorMsg);
            
            return notificationRepository.save(notification);
        }
    }
    
    private String getSubjectByType(String type) {
        switch (type) {
            case "WELCOME":
                return "¡Bienvenido a Batistella! Tu cuenta ya está lista 🐾";
            case "ALERT":
                return "Alerta importante de Batistella";
            case "REMINDER":
                return "Recordatorio de Batistella";
            case "HTML":
                return "Notificación HTML de Batistella";
            default:
                return "Notificación de Batistella";
        }
    }

    @Override
    public Notification sendWelcomeEmail(String email, String name) {
        String htmlContent = emailTemplateService.generateWelcomeEmailContent(name);
        return sendNotification(email, "WELCOME", htmlContent);
    }
    
    @Override
    public Notification sendAlternativeWelcomeEmail(String email, String name) {
        String htmlContent = emailTemplateService.generateAlternativeWelcomeEmailContent(name);
        return sendNotification(email, "WELCOME", htmlContent);
    }
    
    @Override
    public Notification sendSimpleEmail(String email, String name) {
        String htmlContent = emailTemplateService.generateSimpleEmailContent(name);
        return sendNotification(email, "WELCOME", htmlContent);
    }
} 