package com.crm.app.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {
    private UrlUtils() {
    }

    public static boolean isValidHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(value);

            if (uri.getScheme() == null || uri.getHost() == null) {
                return false;
            }

            return uri.getScheme().equalsIgnoreCase("http")
                    || uri.getScheme().equalsIgnoreCase("https");

        } catch (URISyntaxException e) {
            return false;
        }
    }
}
