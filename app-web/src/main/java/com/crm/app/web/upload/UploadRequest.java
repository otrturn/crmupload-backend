package com.crm.app.web.upload;

public record UploadRequest(
        String emailAddress,
        String crmCustomerId,
        String crmApiKey
) {
}