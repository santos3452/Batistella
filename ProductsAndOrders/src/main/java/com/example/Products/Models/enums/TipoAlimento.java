package com.example.Products.Models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoAlimento {
    ADULTO,
    CACHORRO,
    SENIOR,
    LIGHT,
    URINARY;

    @JsonCreator
    public static TipoAlimento fromString(String value) {
        if (value == null) {
            return null;
        }

        for (TipoAlimento tipo : TipoAlimento.values()) {
            if (tipo.name().equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de alimento no válido: " + value);
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
}
