package com.crm.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class CrmUploadHistory extends CrmUploadCoreInfo {

    private Timestamp ts;
    private String status;

    public CrmUploadHistory(
            Timestamp ts,
            String sourceSystem,
            String crmSystem,
            String crmUrl,
            String crmCustomerId,
            String status
    ) {
        super(sourceSystem, crmSystem, crmUrl, crmCustomerId);
        this.ts = ts;
        this.status = status;
    }
}