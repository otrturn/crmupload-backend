package com.crm.app.billing.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.billing")
@Slf4j

public class AppBillingConfig {
    private String workdir;

    @PostConstruct
    public void init() {
        if (workdir == null || workdir.isBlank()) {
            throw new IllegalStateException("workdir is blank");
        }
    }
}

