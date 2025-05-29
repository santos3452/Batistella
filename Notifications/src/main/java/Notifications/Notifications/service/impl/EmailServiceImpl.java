package Notifications.Notifications.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import Notifications.Notifications.service.EmailService;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            message.setFrom(fromEmail);
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            
            // Crear una parte multipart
            Multipart multipart = new MimeMultipart("alternative");
            
            // Crear la parte HTML con codificación base64
            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            ((MimeBodyPart) htmlPart).setHeader("Content-Transfer-Encoding", "base64");
            
            // Agregar la parte HTML al multipart
            multipart.addBodyPart(htmlPart);
            
            // Establecer el contenido del mensaje
            message.setContent(multipart);
            
            // Establecer encabezados adicionales
            message.setHeader("MIME-Version", "1.0");
            message.saveChanges();
            
            // Enviar el mensaje
            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo HTML: " + e.getMessage(), e);
        }
    }
} 