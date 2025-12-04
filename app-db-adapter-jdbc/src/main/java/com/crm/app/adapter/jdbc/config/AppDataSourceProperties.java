package com.crm.app.adapter.jdbc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource")
@Getter
@Setter
public class AppDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
