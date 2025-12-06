package com.crm.app.dto;

@SuppressWarnings("squid:S6218")
public record ConsumerUploadContent(
        long uploadId,
        long consumerId,
        String sourceSystem,
        String crmSystem,
        String crmCustomerId,
        String apiKey,
        byte[] content
) {
}