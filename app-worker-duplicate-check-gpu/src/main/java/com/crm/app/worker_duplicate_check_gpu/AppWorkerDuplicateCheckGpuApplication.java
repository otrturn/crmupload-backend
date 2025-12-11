package com.crm.app.worker_duplicate_check_gpu;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
import com.crm.app.worker_duplicate_check_gpu.config.DuplicateCheckGpuProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.crm")
@EnableConfigurationProperties({AppDataSourceProperties.class, DuplicateCheckGpuProperties.class})
public class AppWorkerDuplicateCheckGpuApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppWorkerDuplicateCheckGpuApplication.class, args);
    }
}
