package com.example.Products.Entity;

import com.example.Products.Entity.enums.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;

@Entity
@Table(name = "productos", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"marca", "tipo_alimento", "tipo_raza", "description", "animal_type", "price_minorista", "price_mayorista"})
    })
@Data
@NoArgsConstructor
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Marca marca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlimento tipoAlimento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoRaza tipoRaza;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)

    private Kilos kg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMinorista;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMayorista;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private type animalType;

    @Column(nullable = false)
    private Boolean activo = true;

    public String getFullName() {
        if (tipoRaza != null) {
            return marca + " " + tipoAlimento + " " + tipoRaza.toString().replace("_", " ");
        }
        return marca + " " + tipoAlimento;
    }
}
