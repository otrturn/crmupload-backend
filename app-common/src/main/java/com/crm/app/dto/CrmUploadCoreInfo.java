package com.crm.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CrmUploadCoreInfo {

    private String sourceSystem;
    private String crmSystem;
    private String crmUrl;
    private String crmCustomerId;

    public CrmUploadCoreInfo(
            String sourceSystem,
            String crmSystem,
            String crmUrl,
            String crmCustomerId
    ) {
        this.sourceSystem = sourceSystem;
        this.crmSystem = crmSystem;
        this.crmUrl = crmUrl;
        this.crmCustomerId = crmCustomerId;
    }
}