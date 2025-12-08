package com.crm.app.worker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.customer-upload")
public class CustomerUploadProperties {

    private int pollIntervalMs = 10000;
    private int batchSize = 5;
    private String workdir;

}

