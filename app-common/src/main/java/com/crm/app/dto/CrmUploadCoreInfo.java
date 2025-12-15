package com.crm.app.dto;

import lombok.Getter;

public class CrmUploadCoreInfo {

    @Getter
    private final String sourceSystem;
    @Getter
    private final String crmSystem;
    private final String crmUrl;
    private final String crmCustomerId;

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

    public String getCrmUrl() {
        return crmUrl == null ? "" : crmUrl;
    }

    public String getCrmCustomerId() {
        return crmCustomerId == null ? "" : crmCustomerId;
    }
}
