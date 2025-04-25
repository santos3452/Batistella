package com.example.Products.Dtos;

import com.example.Products.Entity.enums.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDTO {

    private Long id;

    private Marca marca;


    private TipoAlimento tipoAlimento;

    private TipoRaza tipoRaza;

    private String description;


    private Kilos kg;

    private BigDecimal priceMinorista;

    private BigDecimal priceMayorista;


    private Integer stock;

    private String imageUrl;


    private type animalType;

    private Boolean activo;

}
