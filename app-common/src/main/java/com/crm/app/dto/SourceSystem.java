package com.crm.app.dto;

import java.util.Arrays;

public enum SourceSystem {

    BEXIO("Bexio"),
    LEXWARE("Lexware"),
    MYEXCEL("MyExcel");

    private final String value;

    SourceSystem(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static SourceSystem fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("SourceSystem system cannot be null");
        }

        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(raw.trim()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported CRM system: " + raw));
    }
}
