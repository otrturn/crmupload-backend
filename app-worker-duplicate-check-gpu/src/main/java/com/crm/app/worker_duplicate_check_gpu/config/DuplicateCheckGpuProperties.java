package com.crm.app.worker_duplicate_check_gpu.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.duplicate-check-gpu")
public class DuplicateCheckGpuProperties {
    private int pollIntervalMs = 10000;
    private int batchSize = 5;
    private String workdir;
}

