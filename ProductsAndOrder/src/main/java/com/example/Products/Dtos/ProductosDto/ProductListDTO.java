package com.example.Products.Dtos.ProductosDto;

import com.example.Products.Entity.enums.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDTO {

    private Long id;

    private Marca marca;
    
    private String nombre;

    private TipoAlimento tipoAlimento;

    private TipoRaza tipoRaza;
    
    private CategoriaGranja categoriaGranja;

    private String description;

    private Kilos kg;

    private BigDecimal priceMinorista;

    private BigDecimal priceMayorista;

    private Integer stock;

    private String imageUrl;

    private type animalType;

    private Boolean activo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public String getDisplayName() {
        if (animalType == type.GRANJA) {
            return nombre + " - " + (categoriaGranja != null ? categoriaGranja.toString() : "");
        } else if (tipoRaza != null) {
            if (marca != null && tipoAlimento != null) {
                return marca.toString() + " " + tipoAlimento + " " + tipoRaza.toString().replace("_", " ");
            } else {
                return (marca != null ? marca.toString() : "Sin Marca") + " " +
                       (tipoAlimento != null ? tipoAlimento.toString() : "") + " " +
                       tipoRaza.toString().replace("_", " ");
            }
        }
        if (marca != null && tipoAlimento != null) {
            return marca.toString() + " " + tipoAlimento;
        } else {
            return (marca != null ? marca.toString() : "Sin Marca") + " " +
                   (tipoAlimento != null ? tipoAlimento.toString() : "");
        }
    }
}
