package com.example.Products.Entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoAlimento {
    ADULTO,
    CACHORRO,
    SENIOR,
    ADULTO_SENIOR,
    LIGHT,
    PIEL_SENSIBLE,
    PREMIUM,
    CARNE,
    MIX_CARNE_HIGADO_POLLO,
    PESCADO,
    MIX_PESCADO_CARNE_POLLO,
    CARNE_POLLO_ATUN,
    CARNE_POLLO_VERDURAS,
    PEQUEÑAS,
    CARNE_Y_LECHE,
    SALMON_Y_ATUN,
    CARNES_SELECCIONADAS,
    HOGAREÑOS_ESTERILIZADOS,
    GATITO_KITTEN,
    PERRO_CACHORRO,
    PERRO_ADULTO,
    PREMIUM_PERRO_CACHORRO,
    PREMIUM_PERRO_ADULTO,
    PREMIUM_GATO_ADULTO_URINARIO;

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
