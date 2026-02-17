package com.crm.app.duplicate_check_single.config;

import com.crm.app.dto.SourceSystem;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.duplicate-check")
@Slf4j
public class AppDuplicateCheckSingleConfig {
    private String excelPath;
    private String sourceSystem;

    @PostConstruct
    public void init() {
        if (excelPath == null || excelPath.isBlank()) {
            throw new IllegalStateException("excelPath is blank");
        }
        if (sourceSystem == null || sourceSystem.isBlank()) {
            throw new IllegalStateException("sourceSystem is blank");
        }
        SourceSystem.fromString(sourceSystem);
    }
}
