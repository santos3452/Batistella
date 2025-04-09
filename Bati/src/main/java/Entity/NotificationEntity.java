package Entity;

import Entity.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.*;
import org.apache.catalina.User;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "Notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private UserEntity usuario;

    @Enumerated(EnumType.STRING)
    private NotificationType tipo;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;
}
