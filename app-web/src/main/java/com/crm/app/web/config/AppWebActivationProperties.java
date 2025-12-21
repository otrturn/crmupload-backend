package com.crm.app.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.activation")
@Getter
@Setter
public class AppWebActivationProperties {
    private String baseUrl;
    private String uri;
    private List<String> allowedTermsVersions;
}

