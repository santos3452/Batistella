package com.example.Products.Models;

import com.example.Products.Models.enums.Marca;
import com.example.Products.Models.enums.TipoAlimento;
import com.example.Products.Models.enums.TipoRaza;
import com.example.Products.Models.enums.type;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Marca marca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlimento tipoAlimento;

    @Enumerated(EnumType.STRING)
    private TipoRaza tipoRaza;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer kg;

    @Column(nullable = false)
    private BigDecimal priceMinorista;

    @Column(nullable = false)
    private BigDecimal priceMayorista;

    @Column(nullable = false)
    private Integer stock;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
