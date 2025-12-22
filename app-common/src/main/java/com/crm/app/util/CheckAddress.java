package com.crm.app.util;

import org.apache.commons.lang3.StringUtils;

public class CheckAddress {
    private CheckAddress() {
    }

    public static boolean checkPostalCode(String country, String postalCode) {
        if (StringUtils.isBlank(country) || StringUtils.isBlank(postalCode)) {
            return false;
        }

        if (!StringUtils.isNumeric(postalCode)) {
            return false;
        }

        return switch (country.toUpperCase()) {
            case "DE", "DEUTSCHLAND" -> postalCode.length() == 5;
            case "CH", "SCHWEIZ", "AT", "ÖSTERREICH" -> postalCode.length() == 4;
            default -> false;
        };

    }

}
