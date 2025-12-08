package com.crm.app.dto;

@SuppressWarnings("squid:S6218")
public record CrmUploadContent(
        long uploadId,
        long customerId,
        String sourceSystem,
        String crmSystem,
        String crmCustomerId,
        String apiKey,
        byte[] content
) {
}