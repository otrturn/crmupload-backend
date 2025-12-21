package com.crm.app;

import com.crm.app.adapter.jdbc.config.AppDataSourceProperties;
import com.crm.app.web.config.AppWebActivationProperties;
import com.crm.app.web.config.AppWebDuplicatecheckProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.crm")
@EnableConfigurationProperties({AppDataSourceProperties.class, AppWebActivationProperties.class, AppWebDuplicatecheckProperties.class})
public class AppWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppWebApplication.class, args);
    }
}
