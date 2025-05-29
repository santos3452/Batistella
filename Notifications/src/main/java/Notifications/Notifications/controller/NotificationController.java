package Notifications.Notifications.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Notifications.Notifications.entity.Notification;
import Notifications.Notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @PostMapping
    @Operation(summary = "Crear una notificación", description = "Crea una nueva notificación en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de notificación inválidos")
    })
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        return new ResponseEntity<>(notificationService.saveNotification(notification), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener todas las notificaciones", description = "Retorna una lista de todas las notificaciones")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.findAllNotifications());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener notificación por ID", description = "Retorna una notificación según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificación encontrada"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<Notification> getNotificationById(
            @Parameter(description = "ID de la notificación", required = true)
            @PathVariable Long id) {
        return notificationService.findNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recipient/{recipient}")
    @Operation(summary = "Obtener notificaciones por destinatario", description = "Retorna las notificaciones enviadas a un destinatario específico")
    public ResponseEntity<List<Notification>> getNotificationsByRecipient(
            @Parameter(description = "Email o ID del destinatario", required = true)
            @PathVariable String recipient) {
        return ResponseEntity.ok(notificationService.findNotificationsByRecipient(recipient));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Obtener notificaciones por tipo", description = "Retorna las notificaciones de un tipo específico")
    public ResponseEntity<List<Notification>> getNotificationsByType(
            @Parameter(description = "Tipo de notificación (WELCOME, ALERT, REMINDER, etc.)", required = true)
            @PathVariable String type) {
        return ResponseEntity.ok(notificationService.findNotificationsByType(type));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Obtener notificaciones por rango de fechas", description = "Retorna las notificaciones enviadas dentro de un rango de fechas")
    public ResponseEntity<List<Notification>> getNotificationsByDateRange(
            @Parameter(description = "Fecha de inicio (formato ISO)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "Fecha de fin (formato ISO)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(notificationService.findNotificationsBySentAtBetween(start, end));
    }

    @GetMapping("/success/{success}")
    @Operation(summary = "Obtener notificaciones por estado", description = "Retorna las notificaciones según su estado de envío")
    public ResponseEntity<List<Notification>> getNotificationsBySuccess(
            @Parameter(description = "Estado de envío (true=exitoso, false=fallido)", required = true)
            @PathVariable boolean success) {
        return ResponseEntity.ok(notificationService.findNotificationsBySuccess(success));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificación", description = "Elimina una notificación según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notificación eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "ID de la notificación a eliminar", required = true)
            @PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send")
    @Operation(summary = "Enviar notificación genérica", description = "Envía una notificación al destinatario especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación enviada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de notificación inválidos")
    })
    public ResponseEntity<Notification> sendNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la notificación", required = true,
                    content = @Content(schema = @Schema(implementation = Object.class)))
            @RequestBody Map<String, String> notificationRequest) {
        String recipient = notificationRequest.get("recipient");
        String type = notificationRequest.get("type");
        String message = notificationRequest.get("message");
        
        if (recipient == null || type == null || message == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendNotification(recipient, type, message);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @PostMapping("/send/welcome")
    public ResponseEntity<Notification> sendWelcomeNotification(@RequestBody Map<String, String> request) {
        String recipient = request.get("recipient");
        String message = request.getOrDefault("message", 
            "¡Bienvenido a Batistella! Gracias por registrarte en nuestra plataforma.");
        
        if (recipient == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendNotification(recipient, "WELCOME", message);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @PostMapping("/send/alert")
    public ResponseEntity<Notification> sendAlertNotification(@RequestBody Map<String, String> request) {
        String recipient = request.get("recipient");
        String message = request.get("message");
        
        if (recipient == null || message == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendNotification(recipient, "ALERT", message);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @PostMapping("/send/reminder")
    public ResponseEntity<Notification> sendReminderNotification(@RequestBody Map<String, String> request) {
        String recipient = request.get("recipient");
        String message = request.get("message");
        
        if (recipient == null || message == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendNotification(recipient, "REMINDER", message);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @PostMapping("/send/html")
    public ResponseEntity<Notification> sendHtmlNotification(@RequestBody Map<String, String> request) {
        String recipient = request.get("recipient");
        String htmlContent = request.get("htmlContent");
        
        if (recipient == null || htmlContent == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendNotification(recipient, "HTML", htmlContent);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @RequestMapping(path = "/user/register/html", method = {RequestMethod.GET, RequestMethod.POST})
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

    @PostMapping("/user/register/html/json")
    @Operation(summary = "Enviar notificación de registro con HTML (JSON)", description = "Envía un correo de bienvenida en formato HTML al usuario registrado usando JSON")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación enviada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de notificación inválidos")
    })
    public ResponseEntity<Notification> userRegistrationHtmlNotificationJson(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del usuario para registro", 
                required = true,
                content = @Content(schema = @Schema(implementation = Object.class)))
            @RequestBody Map<String, String> request) {
        
        String email = request.get("email");
        String name = request.get("name");
        
        if (email == null || email.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendWelcomeEmail(email, name);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @GetMapping("/test/welcome-email")
    @Operation(summary = "Probar correo de bienvenida", description = "Envía un correo de prueba con el nuevo formato de bienvenida")
    public ResponseEntity<Notification> testWelcomeEmail(
            @RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "name", required = true) String name) {
        
        if (email == null || email.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendWelcomeEmail(email, name);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @GetMapping("/test/simple-html")
    @Operation(summary = "Probar correo con HTML simple", description = "Envía un correo de prueba con formato HTML simple")
    public ResponseEntity<Notification> testSimpleHtml(
            @RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "name", required = true) String name) {
        
        if (email == null || email.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendSimpleEmail(email, name);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @GetMapping("/test/alternative-welcome")
    @Operation(summary = "Probar correo de bienvenida alternativo", description = "Envía un correo de prueba con formato alternativo")
    public ResponseEntity<Notification> testAlternativeWelcome(
            @RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "name", required = true) String name) {
        
        if (email == null || email.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendAlternativeWelcomeEmail(email, name);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
    
    @GetMapping("/test/super-simple")
    @Operation(summary = "Probar correo extremadamente simple", description = "Envía un correo con formato mínimo para diagnosticar problemas")
    public ResponseEntity<Notification> testSuperSimple(
            @RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "name", required = true) String name) {
        
        if (email == null || email.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.sendSimpleEmail(email, name);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }
} 