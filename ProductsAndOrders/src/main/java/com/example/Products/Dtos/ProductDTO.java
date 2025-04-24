package com.example.Products.Dtos;

import com.example.Products.Entity.enums.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    
    @NotNull(message = "La marca es obligatoria")
    private Marca marca;
    
    @NotNull(message = "El tipo de alimento es obligatorio")
    private TipoAlimento tipoAlimento;
    
    private TipoRaza tipoRaza;
    
    private String description;
    
    @NotNull(message = "El peso es obligatorio")
    private Kilos kg;
    
    private BigDecimal priceMinorista;
    
    private BigDecimal priceMayorista;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    private Integer stock;
    
    private String imageUrl;
    
    @NotNull(message = "El tipo de animal es obligatorio")
    private type animalType;
    
    private Boolean activo;

    public String getFullName() {
        if (tipoRaza != null) {
            return marca + " " + tipoAlimento + " " + tipoRaza.toString().replace("_", " ");
        }
        return marca + " " + tipoAlimento;
    }

    @Override
    public String toString() {
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
