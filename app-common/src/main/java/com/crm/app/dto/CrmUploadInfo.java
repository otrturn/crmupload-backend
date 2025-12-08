package com.crm.app.dto;

public record CrmUploadInfo(
        String sourceSystem,
        String crmSystem,
        String crmCustomerId
) {
}