package com.example.Products.Entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoriaGranja {
    AVES,
    PONEDORAS,
    CONEJOS,
    PORCINOS,
    EQUINOS,
    VACUNOS,
    VARIOS,
    CEREAL;

    @JsonCreator
    public static CategoriaGranja fromString(String value) {
        if (value == null) {
            return null;
        }

        for (CategoriaGranja tipo : CategoriaGranja.values()) {
            if (tipo.name().equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Categoría de granja no válida: " + value);
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
} 