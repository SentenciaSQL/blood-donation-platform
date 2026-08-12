package com.afriasdev.dds.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum BloodType {
    O_NEG("O-"),
    O_POS("O+"),
    A_NEG("A-"),
    A_POS("A+"),
    B_NEG("B-"),
    B_POS("B+"),
    AB_NEG("AB-"),
    AB_POS("AB+");

    private final String code;

    BloodType(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static BloodType fromCode(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(bt -> bt.code.equalsIgnoreCase(value) || bt.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid blood type: " + value));
    }

    @Override
    public String toString() {
        return code;
    }
}
