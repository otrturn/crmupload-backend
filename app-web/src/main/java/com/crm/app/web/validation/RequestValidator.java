package com.crm.app.web.validation;

public final class RequestValidator {

    private RequestValidator() {
    }

    public static boolean isNotValidGermanTaxId(String input) {
        if (input == null) return true;

        String normalized = input.trim()
                .replace(" ", "")
                .replace("/", "")
                .replace("-", "")
                .replace(".", "");

        if (normalized.isEmpty()) return true;

        for (int i = 0; i < normalized.length(); i++) {
            if (!Character.isDigit(normalized.charAt(i))) {
                return true;
            }
        }

        int len = normalized.length();
        return len != 10 && len != 11 && len != 13;
    }

    public static boolean isNotValidGermanVatId(String s) {
        if (s == null) return true;
        return !s.matches("^DE\\d{9}$");
    }
}

