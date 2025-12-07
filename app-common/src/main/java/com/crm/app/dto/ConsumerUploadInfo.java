package com.crm.app.dto;

public record ConsumerUploadInfo(
        String sourceSystem,
        String crmSystem,
        String crmCustomerId
) {
}