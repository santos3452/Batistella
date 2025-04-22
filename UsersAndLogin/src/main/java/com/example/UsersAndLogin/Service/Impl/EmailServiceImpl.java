package com.example.UsersAndLogin.Service.Impl;

import com.example.UsersAndLogin.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            System.out.println("\n=== Iniciando envío de email ===");
            System.out.println("Destinatario: " + to);
            System.out.println("Remitente configurado: " + fromEmail);
            System.out.println("Host SMTP: " + mailSender.toString());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            System.out.println("From configurado correctamente");
            
            helper.setTo(to);
            System.out.println("To configurado correctamente");
            
            helper.setSubject("Restablecimiento de Contraseña");
            System.out.println("Asunto configurado correctamente");
            
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            System.out.println("Link generado: " + resetLink);
            
            String htmlContent = String.format("""
                <html>
                    <body>
                        <h2>Restablecimiento de Contraseña</h2>
                        <p>Has solicitado restablecer tu contraseña.</p>
                        <p>Haz clic en el siguiente enlace para crear una nueva contraseña:</p>
                        <p><a href="%s">Restablecer Contraseña</a></p>
                        <p>Si no solicitaste este cambio, puedes ignorar este correo.</p>
                        <p>Este enlace expirará en 1 hora.</p>
                    </body>
                </html>
                """, resetLink);
            
            helper.setText(htmlContent, true);
            System.out.println("Contenido HTML configurado correctamente");
            
            System.out.println("Intentando enviar email...");
            mailSender.send(message);
            System.out.println("¡Email enviado exitosamente!");
            System.out.println("=== Fin del proceso de envío ===\n");
            
        } catch (MessagingException e) {
            System.err.println("\n=== Error en el envío de email ===");
            System.err.println("Tipo de error: " + e.getClass().getName());
            System.err.println("Mensaje de error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa raíz: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            System.err.println("=== Fin del error ===\n");
            throw new RuntimeException("Error al enviar el email de restablecimiento: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n=== Error inesperado ===");
            System.err.println("Tipo de error: " + e.getClass().getName());
            System.err.println("Mensaje de error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== Fin del error inesperado ===\n");
            throw new RuntimeException("Error inesperado al enviar el email: " + e.getMessage());
        }
    }
} 