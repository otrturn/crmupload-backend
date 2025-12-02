package com.crm.app.consumer;

import com.crm.app.consumer.worker.config.AppDataSourceProperties;
import com.crm.app.consumer.worker.config.ConsumerUploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.crm")
@EnableConfigurationProperties({AppDataSourceProperties.class,ConsumerUploadProperties.class})
public class AppWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppWorkerApplication.class, args);
    }
}
