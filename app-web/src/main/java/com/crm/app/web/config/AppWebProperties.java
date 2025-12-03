package com.crm.app.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.activation")
@Getter
@Setter
public class AppWebProperties {
    private String baseUrl;
    private String uri;
}

