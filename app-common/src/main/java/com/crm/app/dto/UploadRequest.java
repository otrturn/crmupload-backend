package com.crm.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("squid:S6218")
public class UploadRequest extends CrmUploadCoreInfo {

    private String emailAddress;
    private String crmApiKey;

    public UploadRequest(
            String emailAddress,
            String sourceSystem,
            String crmSystem,
            String crmUrl,
            String crmCustomerId,
            String crmApiKey
    ) {
        super(sourceSystem, crmSystem, crmUrl, crmCustomerId);
        this.emailAddress = emailAddress;
        this.crmApiKey = crmApiKey;
    }
}
