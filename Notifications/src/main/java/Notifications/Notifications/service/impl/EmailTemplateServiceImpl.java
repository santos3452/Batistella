package Notifications.Notifications.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import Notifications.Notifications.service.EmailTemplateService;

/**
 * Implementación del servicio de plantillas de correos electrónicos
 */
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    @Override
    public String generateWelcomeEmailContent(String name) {
        return "<div style='font-family:Arial; max-width:600px; margin:0 auto; padding:20px; border:1px solid #e0e0e0'>" +
            "<h2 style='color:#2c3e50'>¡Bienvenido a Batistella, " + name + "! Tu cuenta ya está lista 🐾</h2>" +
            "<p>¡Hola, <b>" + name + "</b>! 👋</p>" +
            "<p>Gracias por registrarte en Batistella, tu nuevo espacio para encontrar los mejores productos balanceados.</p>" +
            "<p>Tu cuenta fue creada con éxito y ya podés comenzar a explorar todo lo que tenemos para ofrecerte: " +
            "alimentos de calidad, productos seleccionados y un equipo siempre listo para ayudarte.</p>" +
            "<div style='background-color:#f8f9fa; padding:15px; margin:20px 0'>" +
            "<h3 style='color:#2c3e50'>🛒 ¿Listo para empezar?</h3>" +
            "<p>Ingresá a tu cuenta y descubrí productos pensados para el bienestar de quienes más querés.</p>" +
            "</div>" +
            "<p>Si tenés alguna duda o necesitás asistencia, no dudes en contactarnos.<br>" +
            "Estamos para ayudarte en cada paso.</p>" +
            "<p>Gracias por confiar en nosotros.<br>" +
            "El equipo de Batistella</p>" +
            "<p style='text-align:center; font-size:12px; color:#6c757d'>© " + 
            LocalDateTime.now().getYear() + " Batistella</p>" +
            "</div>";
    }

    @Override
    public String generateAlternativeWelcomeEmailContent(String name) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
            "<head>" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
            "<title>Bienvenido a Batistella</title>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
            "</head>" +
            "<body style=\"margin: 0; padding: 0;\">" +
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">" +
            "<tr>" +
            "<td style=\"padding: 20px 0 30px 0;\">" +
            "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"border: 1px solid #e0e0e0;\">" +
            "<tr>" +
            "<td align=\"center\" style=\"padding: 20px 0 20px 0; color: #2c3e50; font-family: Arial, sans-serif; font-size: 24px;\">" +
            "¡Bienvenido a Batistella, " + name + "! Tu cuenta ya está lista 🐾" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 20px 30px 20px 30px; font-family: Arial, sans-serif;\">" +
            "<p>¡Hola, <b>" + name + "</b>! 👋</p>" +
            "<p>Gracias por registrarte en Batistella, tu nuevo espacio para encontrar los mejores productos balanceados.</p>" +
            "<p>Tu cuenta fue creada con éxito y ya podés comenzar a explorar todo lo que tenemos para ofrecerte: " +
            "alimentos de calidad, productos seleccionados y un equipo siempre listo para ayudarte.</p>" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 20px 30px; background-color: #f8f9fa; font-family: Arial, sans-serif;\">" +
            "<h3 style=\"color: #2c3e50;\">🛒 ¿Listo para empezar?</h3>" +
            "<p>Ingresá a tu cuenta y descubrí productos pensados para el bienestar de quienes más querés.</p>" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 20px 30px 20px 30px; font-family: Arial, sans-serif;\">" +
            "<p>Si tenés alguna duda o necesitás asistencia, no dudes en contactarnos.<br>" +
            "Estamos para ayudarte en cada paso.</p>" +
            "<p>Gracias por confiar en nosotros.<br>" +
            "El equipo de Batistella</p>" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 10px 30px; font-family: Arial, sans-serif; font-size: 12px; text-align: center; color: #6c757d;\">" +
            "© " + LocalDateTime.now().getYear() + " Batistella" +
            "</td>" +
            "</tr>" +
            "</table>" +
            "</td>" +
            "</tr>" +
            "</table>" +
            "</body>" +
            "</html>";
    }

    @Override
    public String generateSimpleEmailContent(String name) {
        return "<html>" +
            "<body>" +
            "<h1>Hola " + name + "</h1>" +
            "<p>Bienvenido a Batistella</p>" +
            "<p>Tu cuenta ya está lista</p>" +
            "</body>" +
            "</html>";
    }
} 