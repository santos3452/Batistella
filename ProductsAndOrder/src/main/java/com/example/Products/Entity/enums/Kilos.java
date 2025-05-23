package com.example.Products.Entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Kilos {
    EIGHTEEN_KG(18.0, "18kg"),
    THREE_KG(3.0, "3kg"),
    SEVEN_POINT_FIVE_KG(7.5, "7.5kg"),
    TWENTY_KG(20.0, "20kg"),
    FIFTEEN_KG(15.0, "15kg"),
    EIGHT_KG(8.0, "8kg"),
    EIGHTEEN_PLUS_THREE_KG(21.0, "18+3kg"),
    ONE_POINT_FIVE(1.5, "1.5kg"),
    FIFTEEN_PLUS_THREE_KG(18.0, "15+3kg"),
    TWENTY_TWO_KG(22.0, "22kg"),
    TWENTY_TWO_PLUS_THREE_KG(25.0, "22+3kg"),
    ONE_KG(1.0, "1kg"),
    TWENTY_FIVE_KG(25.0, "25kg"),
    THIRTY_KG(30.0, "30kg"),
    TEN_KG(10.0, "10kg"),
    TWO_KG(2.0, "2kg"),
    FIVE_KG(5.0, "5kg");


    private final double weight;
    private final String displayName;

    Kilos(double weight, String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }

    public double getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Kilos fromString(String value) {
        if (value == null) {
            return null;
        }

        // Primero intentamos hacer match con displayName
        for (Kilos kilos : Kilos.values()) {
            if (kilos.getDisplayName().equalsIgnoreCase(value)) {
                return kilos;
            }
        }

        // Si no encontramos match con displayName, intentamos con el nombre del enum
        for (Kilos kilos : Kilos.values()) {
            if (kilos.name().equalsIgnoreCase(value)) {
                return kilos;
            }
        }
        
        throw new IllegalArgumentException("Kilos no válidos: " + value);
    }

    @JsonValue
    public String getValue() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
