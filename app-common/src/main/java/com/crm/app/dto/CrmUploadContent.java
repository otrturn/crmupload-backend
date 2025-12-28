package com.crm.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings({"squid:S6218", "squid:S107"})
public class CrmUploadContent extends CrmUploadCoreInfo {

    private long uploadId;
    private long customerId;
    private String apiKey;
    private byte[] content;

    public CrmUploadContent(
            long uploadId,
            long customerId,
            String sourceSystem,
            String crmSystem,
            String crmUrl,
            String crmCustomerId,
            String apiKey,
            byte[] content,
            boolean isTest
    ) {
        super(sourceSystem, crmSystem, crmUrl, crmCustomerId, isTest);
        this.uploadId = uploadId;
        this.customerId = customerId;
        this.apiKey = apiKey;
        this.content = content;
    }
}
