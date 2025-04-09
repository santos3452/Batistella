package Entity;

import Entity.enums.PaymentsStatus;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "Pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "orden_id")
    private OrderEntity orden;

    @Enumerated(EnumType.STRING)
    private PaymentsStatus status;

    @Column(nullable = false)
    private double monto;
}
