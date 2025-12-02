package com.crm.app;

import com.crm.app.consumer.worker.config.ConsumerUploadProperties;
import com.crm.app.web.config.AppDataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.crm")
@EnableConfigurationProperties({AppDataSourceProperties.class,ConsumerUploadProperties.class})
public class AppWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppWebApplication.class, args);
    }
}
