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
    private String invoiceDir;

    @PostConstruct
    public void init() {
        if (invoiceDir == null || invoiceDir.isBlank()) {
            throw new IllegalStateException("invoiceDir is blank");
        }
    }
}

