package com.crm.app.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.duplicate-check")
@Getter
@Setter
public class AppWebDuplicatecheckProperties {
    /*
     *    volumes:
            - /opt/crmupload-deploy/downloads:/data/generated:ro
     */
    private String excelSampleFile;
    private String excelSampleFileAnswer;
}

