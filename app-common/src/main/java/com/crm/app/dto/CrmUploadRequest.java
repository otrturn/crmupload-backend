package com.crm.app.dto;

public record CrmUploadRequest(
        long uploadId,
        long customerId,
        String sourceSystem,
        String crmSystem,
        String crmUrl,
        String crmCustomerId,
        String apiKey,
        byte[] content
) {}