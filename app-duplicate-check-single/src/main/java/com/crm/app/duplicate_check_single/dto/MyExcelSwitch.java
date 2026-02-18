package com.crm.app.duplicate_check_single.dto;

import java.util.Arrays;
import java.util.List;

public enum MyExcelSwitch {

    ACCOUNTS("accounts"),
    LEADS("leads"),
    ALL("all");

    private final String value;

    MyExcelSwitch(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static MyExcelSwitch fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("MyExcelSwitch system cannot be null");
        }

        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(raw.trim()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported MyExcelSwitch: " + raw + ", available: " + availableMyExcelSwitches()));
    }

    public static List<String> availableMyExcelSwitches() {
        return Arrays.stream(values())
                .map(MyExcelSwitch::value)
                .toList();
    }
}
