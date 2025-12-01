package com.crm.app.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.datasource")
@Getter
@Setter
public class AppDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
