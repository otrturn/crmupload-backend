package com.crm.app.web.util;

import jakarta.servlet.http.HttpServletRequest;

public class WebUtils {
    private WebUtils() {
    }

    public static String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
