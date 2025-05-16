package com.example.Products.Entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Marca {
    TOPNUTRITION("TopNutrition"),
    KENL("Ken-L"),
    ODWALLA("Odwalla"),
    NINELIVES("9Lives"),
    AMICI("Amici"),
    ZIMPI("Zimpi"),
    FISHY("Fishy"),
    GANACAT("Ganacat"),
    GANACAN("Ganacan"),
    COMPINCHES("Compinches"),
    EXACT("Exact");


    private final String displayName;

    Marca(String displayName) {
        this.displayName = displayName;
    }

    @JsonCreator
    public static Marca fromString(String value) {
        if (value == null || value.equalsIgnoreCase("null") || value.isEmpty() || 
            value.equalsIgnoreCase("SIN_MARCA") || value.equalsIgnoreCase("Sin Marca")) {
            return null;
        }

        // Primero intentamos hacer match con displayName
        for (Marca marca : Marca.values()) {
            if (marca.getDisplayName().equalsIgnoreCase(value)) {
                return marca;
            }
        }

        // Si no encontramos match con displayName, intentamos con el nombre del enum
        for (Marca marca : Marca.values()) {
            if (marca.name().equalsIgnoreCase(value)) {
                return marca;
            }
        }
        
        throw new IllegalArgumentException("Marca no válida: " + value);
    }

    @JsonValue
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
} 