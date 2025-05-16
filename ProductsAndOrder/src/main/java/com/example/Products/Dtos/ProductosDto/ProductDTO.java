package com.example.Products.Dtos.ProductosDto;

import com.example.Products.Entity.enums.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    
    // Este campo es opcional para productos de granja
    private Marca marca;
    
    // Este campo es para productos de granja
    private String nombre;
    
    // Este campo es opcional para productos de granja
    private TipoAlimento tipoAlimento;
    
    private TipoRaza tipoRaza;
    
    // Este campo es para productos de granja
    private CategoriaGranja categoriaGranja;
    
    @NotNull(message = "La descripción es obligatoria")
    private String description;
    
    @NotNull(message = "El peso es obligatorio")
    private Kilos kg;
    
    @NotNull(message = "El precio minorista es obligatorio")
    private BigDecimal priceMinorista;
    
    @NotNull(message = "El precio mayorista es obligatorio")
    private BigDecimal priceMayorista;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    private Integer stock;
    
    private String imageUrl;
    
    @NotNull(message = "El tipo de animal es obligatorio")
    private type animalType;
    
    private Boolean activo;

    public String getFullName() {
        if (animalType == type.GRANJA) {
            return nombre + " - " + (categoriaGranja != null ? categoriaGranja.toString() : "");
        } else if (tipoRaza != null) {
            return marca + " " + tipoAlimento + " " + tipoRaza.toString().replace("_", " ");
        }
        return marca + " " + tipoAlimento;
    }

    @Override
    public String toString() {
        if (animalType == type.GRANJA) {
            return "ProductDTO{" +
                    "nombre='" + nombre + '\'' +
                    ", categoriaGranja=" + categoriaGranja +
                    ", description='" + description + '\'' +
                    ", kg=" + (kg != null ? kg.getDisplayName() : "N/A") +
                    ", priceMinorista=" + priceMinorista +
                    ", priceMayorista=" + priceMayorista +
                    ", stock=" + stock +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", activo=" + activo +
                    '}';
        } else {
            return "ProductDTO{" +
                    "name='" + getFullName() + '\'' +
                    ", description='" + description + '\'' +
                    ", kg=" + (kg != null ? kg.getDisplayName() : "N/A") +
                    ", priceMinorista=" + priceMinorista +
                    ", priceMayorista=" + priceMayorista +
                    ", stock=" + stock +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", animalType='" + animalType + '\'' +
                    ", activo=" + activo +
                    '}';
        }
    }
}
