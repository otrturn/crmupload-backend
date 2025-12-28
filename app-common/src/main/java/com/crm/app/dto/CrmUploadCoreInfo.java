package com.crm.app.dto;

import lombok.Getter;

public class CrmUploadCoreInfo {

    @Getter
    private final String sourceSystem;
    @Getter
    private final String crmSystem;
    private final String crmUrl;
    private final String crmCustomerId;
    @Getter
    private final boolean isTest;

    public CrmUploadCoreInfo(
            String sourceSystem,
            String crmSystem,
            String crmUrl,
            String crmCustomerId,
            boolean isTest
    ) {
        this.sourceSystem = sourceSystem;
        this.crmSystem = crmSystem;
        this.crmUrl = crmUrl;
        this.crmCustomerId = crmCustomerId;
        this.isTest = isTest;
    }

    public String getCrmUrl() {
        return crmUrl == null ? "" : crmUrl;
    }

    public String getCrmCustomerId() {
        return crmCustomerId == null ? "" : crmCustomerId;
    }
}
