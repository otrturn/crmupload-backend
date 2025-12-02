package com.crm.app.web.upload;

public record UploadRequest(
        String emailAddress,
        String sourceSystem,
        String crmSystem,
        String crmCustomerId,
        String crmApiKey
) {
}