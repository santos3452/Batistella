package Notifications.Notifications.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Notifications.Notifications.dto.PedidoDTO;
import Notifications.Notifications.dto.ProductoPedidoDTO;
import Notifications.Notifications.service.EmailTemplateService;
import Notifications.Notifications.util.ImageUtil;

/**
 * Implementación del servicio de plantillas de correos electrónicos
 */
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateServiceImpl.class);
    private final ImageUtil imageUtil;
    
    @Autowired
    public EmailTemplateServiceImpl(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    @Override
    public String generateWelcomeEmailContent(String name) {
        // Usar una URL de imagen directamente para mayor compatibilidad
        // Reemplaza esta URL con la URL real de tu logo
        String logoUrl = "https://raw.githubusercontent.com/santos3452/images-email/main/Logo.png"; // Este es un ícono de ejemplo, reemplázalo con tu logo real
        
        return "<div style='font-family:Arial; max-width:600px; margin:0 auto; padding:20px; border:1px solid #e0e0e0'>" +
            // Logo centrado al inicio
            "<div style='text-align:center; margin-bottom:20px;'>" +
            "<img src='" + logoUrl + "' alt='Batistella Logo' style='max-width:200px; height:auto;' />" +
            "</div>" +
            "<h2 style='color:#000000'>¡Bienvenido a Batistella, " + name + "! Tu cuenta ya está lista 🐾</h2>" +
            "<p style='color:#000000'>¡Hola, <b>" + name + "</b>! 👋</p>" +
            "<p style='color:#000000'>Gracias por registrarte en Batistella, tu nuevo espacio para encontrar los mejores productos balanceados.</p>" +
            "<p style='color:#000000'>Tu cuenta fue creada con éxito y ya podés comenzar a explorar todo lo que tenemos para ofrecerte: " +
            "alimentos de calidad, productos seleccionados y un equipo siempre listo para ayudarte.</p>" +
            "<div style='background-color:#f8f9fa; padding:15px; margin:20px 0'>" +
            "<h3 style='color:#000000'>🛒 ¿Listo para empezar?</h3>" +
            "<p style='color:#000000'>Ingresá a tu cuenta y descubrí productos pensados para el bienestar de quienes más querés.</p>" +
            "</div>" +
            "<p style='color:#000000'>Si tenés alguna duda o necesitás asistencia, no dudes en contactarnos.<br>" +
            "Estamos para ayudarte en cada paso.</p>" +
            "<p style='color:#000000'>Gracias por confiar en nosotros.<br>" +
            "El equipo de Batistella</p>" +
            "<p style='text-align:center; font-size:12px; color:#000000'>© " + 
            LocalDateTime.now().getYear() + " Batistella</p>" +
            "</div>";
    }

    @Override
    public String generateAlternativeWelcomeEmailContent(String name) {
        // Usar una URL de imagen directamente para mayor compatibilidad
        // Reemplaza esta URL con la URL real de tu logo
        String logoUrl = "https://raw.githubusercontent.com/santos3452/images-email/main/Logo.png"; // Este es un ícono de ejemplo, reemplázalo con tu logo real
        
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
            "<head>" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
            "<title>Bienvenido a Batistella</title>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
            "</head>" +
            "<body style=\"margin: 0; padding: 0; color: #000000;\">" +
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">" +
            "<tr>" +
            "<td style=\"padding: 20px 0 30px 0;\">" +
            "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"border: 1px solid #e0e0e0;\">" +
            // Logo en la primera fila
            "<tr>" +
            "<td align=\"center\" style=\"padding: 20px 0;\">" +
            "<img src='" + logoUrl + "' alt='Batistella Logo' style='max-width:200px; height:auto;' />" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td align=\"center\" style=\"padding: 20px 0 20px 0; color: #000000; font-family: Arial, sans-serif; font-size: 24px;\">" +
            "¡Bienvenido a Batistella, " + name + "! Tu cuenta ya está lista 🐾" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 20px 30px 20px 30px; font-family: Arial, sans-serif; color: #000000;\">" +
            "<p>¡Hola, <b>" + name + "</b>! 👋</p>" +
            "<p>Gracias por registrarte en Batistella, tu nuevo espacio para encontrar los mejores productos balanceados.</p>" +
            "<p>Tu cuenta fue creada con éxito y ya podés comenzar a explorar todo lo que tenemos para ofrecerte: " +
            "alimentos de calidad, productos seleccionados y un equipo siempre listo para ayudarte.</p>" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 20px 30px; background-color: #f8f9fa; font-family: Arial, sans-serif;\">" +
            "<h3 style=\"color: #000000;\">🛒 ¿Listo para empezar?</h3>" +
            "<p style=\"color: #000000;\">Ingresá a tu cuenta y descubrí productos pensados para el bienestar de quienes más querés.</p>" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 20px 30px 20px 30px; font-family: Arial, sans-serif; color: #000000;\">" +
            "<p>Si tenés alguna duda o necesitás asistencia, no dudes en contactarnos.<br>" +
            "Estamos para ayudarte en cada paso.</p>" +
            "<p>Gracias por confiar en nosotros.<br>" +
            "El equipo de Batistella</p>" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding: 10px 30px; font-family: Arial, sans-serif; font-size: 12px; text-align: center; color: #000000;\">" +
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
        // Usar una URL de imagen directamente para mayor compatibilidad
        // Reemplaza esta URL con la URL real de tu logo
        String logoUrl = "https://raw.githubusercontent.com/santos3452/images-email/main/Logo.png"; // Este es un ícono de ejemplo, reemplázalo con tu logo real
        
        return "<html>" +
            "<body style='color: #000000;'>" +
            "<div style='text-align:center; margin-bottom:20px;'>" +
            "<img src='" + logoUrl + "' alt='Batistella Logo' style='max-width:200px; height:auto;' />" +
            "</div>" +
            "<h1>Hola " + name + "</h1>" +
            "<p>Bienvenido a Batistella</p>" +
            "<p>Tu cuenta ya está lista</p>" +
            "</body>" +
            "</html>";
    }
    
    @Override
    public String generatePaymentConfirmationEmailContent(PedidoDTO pedido) {
        String logoUrl = "https://raw.githubusercontent.com/santos3452/images-email/main/Logo.png";
        
        StringBuilder productosHtml = new StringBuilder();
        
        // Generar filas de productos de forma más simple
        for (ProductoPedidoDTO producto : pedido.getProductos()) {
            productosHtml.append("<div style='padding: 8px 0; border-bottom: 1px solid #e0e0e0;'>")
                .append("<div style='display: inline-block; width: 50%;'>").append(producto.getNombreProducto()).append("</div>")
                .append("<div style='display: inline-block; width: 15%; text-align: center;'>").append(producto.getCantidad()).append("</div>")
                .append("<div style='display: inline-block; width: 15%; text-align: right;'>$").append(producto.getPrecioUnitario()).append("</div>")
                .append("<div style='display: inline-block; width: 15%; text-align: right;'>$").append(producto.getSubtotal()).append("</div>")
                .append("</div>");
        }
        
        return "<html>" +
            "<head>" +
            "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Arial, Helvetica, sans-serif; color: #333;'>" +
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            
            // Logo
            "<div style='text-align: center; padding: 20px 0;'>" +
            "<img src='" + logoUrl + "' alt='Batistella Logo' style='max-width: 180px;'>" +
            "</div>" +
            
            // Título
            "<div style='text-align: center; padding: 20px 0;'>" +
            "<h1 style='color: #333; margin: 0; font-size: 24px;'>¡Tu pago se procesó correctamente!</h1>" +
            "<p style='font-size: 18px; margin: 10px 0;'>Tu pedido ha sido confirmado</p>" +
            "<p style='margin: 10px 0;'>¡Muchas gracias por comprar en Batistella!</p>" +
            "</div>" +
            
            // Detalles del pedido
            "<div style='background-color: #f9f9f9; border: 1px solid #ddd; padding: 15px; margin: 20px 0;'>" +
            "<h2 style='color: #333; margin: 0 0 15px 0; font-size: 18px;'>Detalles del Pedido #" + pedido.getCodigoPedido() + "</h2>" +
            "<p style='margin: 8px 0;'><strong>Fecha:</strong> " + pedido.getFechaPedido() + "</p>" +
            "<p style='margin: 8px 0;'><strong>Cliente:</strong> " + pedido.getNombreCompletoUsuario() + "</p>" +
            "<p style='margin: 8px 0;'><strong>Dirección de entrega:</strong> " + pedido.getDomicilio() + "</p>" +
            
            // Encabezados de productos
            "<div style='margin-top: 20px; background-color: #f1f1f1; padding: 10px 0; font-weight: bold;'>" +
            "<div style='display: inline-block; width: 50%;'>Producto</div>" +
            "<div style='display: inline-block; width: 15%; text-align: center;'>Cant.</div>" +
            "<div style='display: inline-block; width: 15%; text-align: right;'>Precio</div>" +
            "<div style='display: inline-block; width: 15%; text-align: right;'>Subtotal</div>" +
            "</div>" +
            
            // Productos
            productosHtml.toString() +
            
            // Total
            "<div style='margin-top: 15px; text-align: right; font-weight: bold;'>" +
            "Total: $" + pedido.getTotal() +
            "</div>" +
            "</div>" +
            
            // Pie
            "<div style='padding: 20px 0;'>" +
            "<p style='margin: 8px 0;'>Si tenés alguna duda o consulta sobre tu pedido, no dudes en contactarnos.</p>" +
            "<p style='margin: 8px 0;'>¡Gracias por confiar en nosotros!</p>" +
            "<p style='margin: 8px 0;'>El equipo de Batistella</p>" +
            "</div>" +
            
            // Footer
            "<div style='text-align: center; border-top: 1px solid #ddd; padding-top: 15px;'>" +
            "<p style='font-size: 12px; color: #777;'>© " + LocalDateTime.now().getYear() + " Batistella</p>" +
            "</div>" +
            
            "</div>" +
            "</body>" +
            "</html>";
    }
} 