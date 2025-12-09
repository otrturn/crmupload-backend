package com.crm.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
            byte[] content
    ) {
        super(sourceSystem, crmSystem, crmUrl, crmCustomerId);
        this.uploadId = uploadId;
        this.customerId = customerId;
        this.apiKey = apiKey;
        this.content = content;
    }
}
