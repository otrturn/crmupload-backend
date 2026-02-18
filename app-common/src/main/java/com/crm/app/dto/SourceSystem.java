package com.crm.app.dto;

import java.util.Arrays;
import java.util.List;

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
                        new IllegalArgumentException("Unsupported SourceSystem system: " + raw + ", available: " + availableSourceSystems()));
    }

    public static List<String> availableSourceSystems() {
        return Arrays.stream(values())
                .map(SourceSystem::value)
                .toList();
    }
}
