package com.crm.app.duplicate_check_single.config;

import com.crm.app.dto.SourceSystem;
import com.crm.app.duplicate_check_single.dto.MyExcelSwitch;
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
    private String myExcelSwitch;

    @PostConstruct
    public void init() {
        if (excelPath == null || excelPath.isBlank()) {
            throw new IllegalStateException("excelPath is blank");
        }
        if (sourceSystem == null || sourceSystem.isBlank()) {
            throw new IllegalStateException("sourceSystem is blank");
        }
        SourceSystem thesourceSystem = SourceSystem.fromString(sourceSystem);
        if (thesourceSystem.equals(SourceSystem.MYEXCEL) && (myExcelSwitch == null || myExcelSwitch.isBlank())) {
            throw new IllegalStateException("sourceSystem is " + SourceSystem.MYEXCEL.name() + " and myExcelSwitch is blank");
        }
        if (myExcelSwitch != null) {
            MyExcelSwitch.fromString(myExcelSwitch);
        }
    }
}
