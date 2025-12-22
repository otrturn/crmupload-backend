package com.crm.app.e2e.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "e2e")
public record E2eProperties(
        String baseUrl
) {
}
