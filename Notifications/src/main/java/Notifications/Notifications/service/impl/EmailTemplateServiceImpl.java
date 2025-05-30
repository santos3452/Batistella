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
        
        // Generar filas de productos usando tabla simple para máxima compatibilidad
        for (ProductoPedidoDTO producto : pedido.getProductos()) {
            productosHtml.append("<tr>")
                .append("<td style='padding: 10px; border-bottom: 1px solid #e0e0e0;'>").append(producto.getNombreProducto()).append("</td>")
                .append("<td style='padding: 10px; border-bottom: 1px solid #e0e0e0; text-align: center;'>").append(producto.getCantidad()).append("</td>")
                .append("<td style='padding: 10px; border-bottom: 1px solid #e0e0e0; text-align: right;'>$").append(producto.getPrecioUnitario()).append("</td>")
                .append("<td style='padding: 10px; border-bottom: 1px solid #e0e0e0; text-align: right;'>$").append(producto.getSubtotal()).append("</td>")
                .append("</tr>");
        }
        
        return "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>" +
            "<html xmlns='http://www.w3.org/1999/xhtml'>" +
            "<head>" +
            "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
            "<style type='text/css'>" +
            "@media screen and (max-width: 525px) {" +
            "  table[class='responsive-table'] {width: 100% !important;}" +
            "  td[class='padding'] {padding: 10px 5% 15px 5% !important;}" +
            "  td[class='padding-copy'] {padding: 10px 5% 10px 5% !important; text-align: center;}" +
            "  td[class='padding-meta'] {padding: 30px 5% 0px 5% !important; text-align: center;}" +
            "  td[class='no-pad'] {padding: 0 0 10px 0 !important;}" +
            "  td[class='no-padding'] {padding: 0 !important;}" +
            "  td[class='section-padding'] {padding: 10px 15px 10px 15px !important;}" +
            "  td[class='section-header'] {padding: 10px 15px 10px 15px !important;}" +
            "  td[class='mobile-wrapper'] {padding: 10px 5% 15px 5% !important;}" +
            "  table[class='mobile-button-container'] {margin: 0 auto; width: 100% !important;}" +
            "  a[class='mobile-button'] {width: 80% !important; padding: 15px !important; border: 0 !important; font-size: 16px !important;}" +
            "  td[class='mobile-hide'] {display: none !important;}" +
            "  td[class='mobile-show'] {display: block !important;}" +
            "}" +
            "</style>" +
            "</head>" +
            "<body style='margin: 0; padding: 0;'>" +
            
            "<!-- ESTRUCTURA PRINCIPAL -->" +
            "<table border='0' cellpadding='0' cellspacing='0' width='100%'>" +
            "  <tr>" +
            "    <td align='center' bgcolor='#ffffff'>" +
            
            "      <!-- CONTENEDOR PRINCIPAL -->" +
            "      <table border='0' cellpadding='0' cellspacing='0' width='600' class='responsive-table'>" +
            "        <tr>" +
            "          <td>" +
            
            "            <!-- BLOQUE DEL LOGO -->" +
            "            <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
            "              <tr>" +
            "                <td align='center' class='padding'>" +
            "                  <table border='0' cellspacing='0' cellpadding='0'>" +
            "                    <tr>" +
            "                      <td>" +
            "                        <a href='#' target='_blank'>" +
            "                          <img src='" + logoUrl + "' width='180' border='0' alt='Batistella Logo' style='display: block; color: #666666; font-family: Helvetica, Arial, sans-serif; font-size: 16px;' />" +
            "                        </a>" +
            "                      </td>" +
            "                    </tr>" +
            "                  </table>" +
            "                </td>" +
            "              </tr>" +
            "            </table>" +
            
            "            <!-- BLOQUE DEL TÍTULO -->" +
            "            <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
            "              <tr>" +
            "                <td align='center' class='padding'>" +
            "                  <table width='100%' border='0' cellspacing='0' cellpadding='0' class='mobile-button-container'>" +
            "                    <tr>" +
            "                      <td align='center' style='font-size: 22px; font-family: Helvetica, Arial, sans-serif; color: #333333; padding-top: 10px;' class='padding-copy'>" +
            "                        ¡Tu pago se procesó correctamente!" +
            "                      </td>" +
            "                    </tr>" +
            "                    <tr>" +
            "                      <td align='center' style='font-size: 16px; font-family: Helvetica, Arial, sans-serif; color: #333333; padding-top: 10px;' class='padding-copy'>" +
            "                        Tu pedido ha sido confirmado" +
            "                      </td>" +
            "                    </tr>" +
            "                    <tr>" +
            "                      <td align='center' style='font-size: 14px; font-family: Helvetica, Arial, sans-serif; color: #333333; padding-top: 10px;' class='padding-copy'>" +
            "                        ¡Muchas gracias por comprar en Batistella!" +
            "                      </td>" +
            "                    </tr>" +
            "                  </table>" +
            "                </td>" +
            "              </tr>" +
            "            </table>" +
            
            "            <!-- BLOQUE DE DETALLES DEL PEDIDO -->" +
            "            <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
            "              <tr>" +
            "                <td align='center' class='padding'>" +
            "                  <table width='100%' border='0' cellspacing='0' cellpadding='0' bgcolor='#f8f8f8' style='border: 1px solid #e0e0e0; border-radius: 4px;'>" +
            "                    <tr>" +
            "                      <td class='padding-copy' style='font-size: 16px; font-family: Helvetica, Arial, sans-serif; color: #333333; padding: 15px; border-bottom: 1px solid #e0e0e0;'>" +
            "                        <strong>Detalles del Pedido #" + pedido.getCodigoPedido() + "</strong>" +
            "                      </td>" +
            "                    </tr>" +
            "                    <tr>" +
            "                      <td class='padding-copy' style='font-size: 14px; font-family: Helvetica, Arial, sans-serif; color: #333333; padding: 15px;'>" +
            "                        <p style='margin: 5px 0;'><strong>Fecha:</strong> " + pedido.getFechaPedido() + "</p>" +
            "                        <p style='margin: 5px 0;'><strong>Cliente:</strong> " + pedido.getNombreCompletoUsuario() + "</p>" +
            "                        <p style='margin: 5px 0;'><strong>Dirección de entrega:</strong> " + pedido.getDomicilio() + "</p>" +
            
            "                        <!-- TABLA DE PRODUCTOS -->" +
            "                        <table width='100%' border='0' cellspacing='0' cellpadding='0' style='margin-top: 15px;'>" +
            "                          <tr bgcolor='#f1f1f1'>" +
            "                            <th align='left' style='padding: 10px; border-bottom: 2px solid #e0e0e0; font-family: Helvetica, Arial, sans-serif; font-size: 14px;'>Producto</th>" +
            "                            <th align='center' style='padding: 10px; border-bottom: 2px solid #e0e0e0; font-family: Helvetica, Arial, sans-serif; font-size: 14px;'>Cant.</th>" +
            "                            <th align='right' style='padding: 10px; border-bottom: 2px solid #e0e0e0; font-family: Helvetica, Arial, sans-serif; font-size: 14px;'>Precio</th>" +
            "                            <th align='right' style='padding: 10px; border-bottom: 2px solid #e0e0e0; font-family: Helvetica, Arial, sans-serif; font-size: 14px;'>Subtotal</th>" +
            "                          </tr>" +
            
            productosHtml.toString() +
            
            "                          <tr>" +
            "                            <td colspan='3' align='right' style='padding: 10px; border-top: 2px solid #e0e0e0; font-family: Helvetica, Arial, sans-serif; font-size: 14px; font-weight: bold;'>Total:</td>" +
            "                            <td align='right' style='padding: 10px; border-top: 2px solid #e0e0e0; font-family: Helvetica, Arial, sans-serif; font-size: 14px; font-weight: bold;'>$" + pedido.getTotal() + "</td>" +
            "                          </tr>" +
            "                        </table>" +
            "                      </td>" +
            "                    </tr>" +
            "                  </table>" +
            "                </td>" +
            "              </tr>" +
            "            </table>" +
            
            "            <!-- BLOQUE DE MENSAJE FINAL -->" +
            "            <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
            "              <tr>" +
            "                <td class='padding'>" +
            "                  <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
            "                    <tr>" +
            "                      <td style='padding: 10px 0; font-family: Helvetica, Arial, sans-serif; font-size: 14px; color: #333333;'>" +
            "                        <p style='margin: 5px 0;'>Si tenés alguna duda o consulta sobre tu pedido, no dudes en contactarnos.</p>" +
            "                        <p style='margin: 5px 0;'>¡Gracias por confiar en nosotros!</p>" +
            "                        <p style='margin: 5px 0;'>El equipo de Batistella</p>" +
            "                      </td>" +
            "                    </tr>" +
            "                  </table>" +
            "                </td>" +
            "              </tr>" +
            "            </table>" +
            
            "            <!-- BLOQUE DE FOOTER -->" +
            "            <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
            "              <tr>" +
            "                <td align='center'>" +
            "                  <table width='100%' border='0' cellspacing='0' cellpadding='0'>" +
            "                    <tr>" +
            "                      <td align='center' style='padding: 20px 0 0 0; border-top: 1px solid #e0e0e0;'>" +
            "                        <table border='0' cellspacing='0' cellpadding='0'>" +
            "                          <tr>" +
            "                            <td align='center' style='font-family: Helvetica, Arial, sans-serif; font-size: 12px; color: #666666;'>" +
            "                              © " + LocalDateTime.now().getYear() + " Batistella" +
            "                            </td>" +
            "                          </tr>" +
            "                        </table>" +
            "                      </td>" +
            "                    </tr>" +
            "                  </table>" +
            "                </td>" +
            "              </tr>" +
            "            </table>" +
            
            "          </td>" +
            "        </tr>" +
            "      </table>" +
            
            "    </td>" +
            "  </tr>" +
            "</table>" +
            
            "</body>" +
            "</html>";
    }
} 