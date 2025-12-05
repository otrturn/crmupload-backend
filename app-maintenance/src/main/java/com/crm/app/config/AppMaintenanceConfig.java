package com.crm.app.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.maintenance")
@Slf4j
public class AppMaintenanceConfig {
    private String baseUrl;

    @PostConstruct
    public void init() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("baseUrl is blank");
        }
    }
}
