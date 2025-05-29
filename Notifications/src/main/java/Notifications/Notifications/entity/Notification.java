package Notifications.Notifications.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa una notificación enviada")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la notificación", example = "1")
    private Long id;

    @Schema(description = "Destinatario de la notificación (email, userId, etc.)", example = "usuario@ejemplo.com")
    private String recipient;
    
    @Schema(description = "Tipo de notificación (WELCOME, ALERT, REMINDER, etc.)", example = "WELCOME")
    private String type;
    
    @Schema(description = "Contenido del mensaje de la notificación", example = "Bienvenido a la plataforma")
    private String message;
    
    @Schema(description = "Fecha y hora de envío de la notificación", example = "2023-06-15T14:30:00")
    private LocalDateTime sentAt;
    
    @Schema(description = "Indica si la notificación se envió correctamente", example = "true")
    private boolean success;
    
    @Schema(description = "Mensaje de error en caso de fallo en el envío", example = "Error al enviar el correo: destinatario no válido")
    private String errorMessage;
} 