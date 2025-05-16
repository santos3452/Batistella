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
            return marca + " " + tipoAlimento + " " + tipoRaza.toString().replace("_", " ");
        }
        return marca + " " + tipoAlimento;
    }
}
