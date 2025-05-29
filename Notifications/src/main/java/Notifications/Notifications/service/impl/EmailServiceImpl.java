package Notifications.Notifications.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import Notifications.Notifications.service.EmailService;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

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
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            
            // Configurar el contenido directamente
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            
            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo HTML: " + e.getMessage(), e);
        }
    }
} 