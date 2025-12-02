package com.crm.app.port.consumer;

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