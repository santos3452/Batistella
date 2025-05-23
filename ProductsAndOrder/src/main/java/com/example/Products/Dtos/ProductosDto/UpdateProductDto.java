package com.example.Products.Dtos.ProductosDto;

import com.example.Products.Entity.enums.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductDto {

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

    private LocalDateTime updatedAt;
    
    // Método para establecer el tipo de animal y manejar la marca según el tipo
    public void setAnimalType(type animalType) {
        this.animalType = animalType;
        // Si es un producto de granja, asegurarnos de que la marca sea nula
        if (animalType == type.GRANJA) {
            this.marca = null;
        }
    }
}
