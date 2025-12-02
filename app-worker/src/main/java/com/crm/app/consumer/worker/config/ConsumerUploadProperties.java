package com.crm.app.consumer.worker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.consumer-upload")
public class ConsumerUploadProperties {

    private int pollIntervalMs = 10000;
    private int batchSize = 5;

}

