package Notifications.Notifications.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Notifications.Notifications.dto.PedidoDTO;
import Notifications.Notifications.entity.Notification;
import Notifications.Notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notificaciones", description = "API para gestionar notificaciones")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    
    @RequestMapping(path = "/user/register/html", method = {RequestMethod.POST})
    @Operation(summary = "Enviar notificación de registro con HTML", description = "Envía un correo de bienvenida en formato HTML al usuario registrado")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación enviada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de notificación inválidos")
    })
    public ResponseEntity<Notification> userRegistrationHtmlNotification(
            @Parameter(description = "Email del usuario", required = true) 
            @RequestParam(value = "email", required = true) String email,
            @Parameter(description = "Nombre del usuario", required = true) 
            @RequestParam(value = "name", required = true) String name) {
        
        if (email == null || email.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendWelcomeEmail(email, name);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }

    @PostMapping("/payment/confirmation")
    @Operation(summary = "Enviar confirmación de pago", description = "Envía un correo con la confirmación del pago y detalles del pedido")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación enviada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de notificación inválidos")
    })
    public ResponseEntity<Notification> paymentConfirmationNotification(
            @Parameter(description = "Email del usuario", required = true) 
            @RequestParam(value = "email", required = true) String email,
            @Parameter(description = "Información del pedido", required = true) 
            @RequestBody PedidoDTO pedido) {
        
        if (email == null || email.isEmpty() || pedido == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendPaymentConfirmationEmail(email, pedido);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }

} 