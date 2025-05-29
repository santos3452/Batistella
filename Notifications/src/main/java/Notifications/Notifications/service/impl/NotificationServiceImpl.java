package Notifications.Notifications.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Notifications.Notifications.dto.PedidoDTO;
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
    public Notification sendNotification(String recipient, String type, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        
        // Para tipos de mensaje HTML, guardamos un mensaje genérico
        if (type.equals("HTML") || type.equals("WELCOME") || type.equals("PAYMENT_CONFIRMATION")) {
            notification.setMessage("Mensaje enviado correctamente");
        } else {
            // Para mensajes de texto plano, limitamos la longitud
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
            
            // Enviar todos los mensajes como HTML
            emailService.sendHtmlMessage(recipient, subject, message);
            
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
            case "PAYMENT_CONFIRMATION":
                return "¡Tu pago ha sido confirmado! - Batistella";
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
    public Notification sendPaymentConfirmationEmail(String email, PedidoDTO pedido) {
        String htmlContent = emailTemplateService.generatePaymentConfirmationEmailContent(pedido);
        return sendNotification(email, "PAYMENT_CONFIRMATION", htmlContent);
    }
} 