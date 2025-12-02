package com.crm.app.port.consumer;

public record ConsumerUploadContent(
        long uploadId,
        long consumerId,
        String sourceSystem,
        String crmSystem,
        String crmCustomerId,
        String apiKey,
        byte[] content
) {}