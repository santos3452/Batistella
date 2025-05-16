package com.example.Products.Entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoRaza {
    RAZA_GRANDE,
    RAZA_MEDIANA,
    RAZA_PEQUENA,
    RAZA_MEDIANA_GRANDE;

    @JsonCreator
    public static TipoRaza fromString(String value) {
        if (value == null) {
            return null;
        }

        for (TipoRaza tipo : TipoRaza.values()) {
            if (tipo.name().equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de raza no válido: " + value);
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }
} 