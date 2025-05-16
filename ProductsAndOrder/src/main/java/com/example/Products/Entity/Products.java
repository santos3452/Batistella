package com.example.Products.Entity;

import com.example.Products.Entity.enums.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Marca marca;

    @Column(length = 255)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoAlimento tipoAlimento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoRaza tipoRaza;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CategoriaGranja categoriaGranja;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        if (animalType == type.GRANJA) {
            return nombre + " - " + (categoriaGranja != null ? categoriaGranja.toString() : "");
        } else if (tipoRaza != null) {
            return marca + " " + tipoAlimento + " " + tipoRaza.toString().replace("_", " ");
        }
        return marca + " " + tipoAlimento;
    }
}
