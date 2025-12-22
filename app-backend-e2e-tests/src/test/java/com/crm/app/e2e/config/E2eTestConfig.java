package com.crm.app.e2e.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(E2eProperties.class)
public class E2eTestConfig {
}
