package com.crm.app.web.validation;

public final class RequestValidator {

    private RequestValidator() {
    }

    public static boolean stringIsEmpty(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidGermanTaxId(String input) {
        if (input == null) return false;

        String normalized = input.trim()
                .replace(" ", "")
                .replace("/", "")
                .replace("-", "")
                .replace(".", "");

        if (normalized.isEmpty()) return false;

        for (int i = 0; i < normalized.length(); i++) {
            if (!Character.isDigit(normalized.charAt(i))) {
                return false;
            }
        }

        int len = normalized.length();
        return len == 10 || len == 11 || len == 13;
    }

    public static boolean isValidGermanVatId(String s) {
        if (s == null) return false;
        return s.matches("^DE\\d{9}$");
    }
}

