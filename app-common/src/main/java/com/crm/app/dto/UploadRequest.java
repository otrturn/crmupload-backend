package com.crm.app.dto;

public record UploadRequest(
        String emailAddress,
        String sourceSystem,
        String crmSystem,
        String crmUrl,
        String crmCustomerId,
        String crmApiKey
) {
}