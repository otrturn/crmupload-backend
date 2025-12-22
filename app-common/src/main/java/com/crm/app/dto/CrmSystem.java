package com.crm.app.dto;

import java.util.Arrays;
import java.util.List;

public enum CrmSystem {

    ESPOCRM("EspoCRM"),
    PIPEDRIVE("Pipedrive");

    private final String value;

    CrmSystem(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CrmSystem fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("CRM system cannot be null");
        }

        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(raw.trim()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported CRM system: " + raw));
    }

    public static List<String> availableCrmSystems() {
        return Arrays.stream(values())
                .map(CrmSystem::value)
                .toList();
    }
}
