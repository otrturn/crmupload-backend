package com.crm.app.dto;

public record CustomerUploadInfo(
        String sourceSystem,
        String crmSystem,
        String crmCustomerId
) {
}