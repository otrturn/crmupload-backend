package com.crm.app.worker_duplicate_check.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.duplicate-check")
public class DuplicateCheckProperties {
    private int pollIntervalMs = 10000;
    private int batchSize = 5;
    private String workdir;
}

