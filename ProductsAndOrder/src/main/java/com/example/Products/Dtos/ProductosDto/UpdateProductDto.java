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

    private LocalDateTime updatedAt;
}
