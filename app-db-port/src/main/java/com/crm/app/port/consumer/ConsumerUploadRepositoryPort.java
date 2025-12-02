package com.crm.app.port.consumer;

public interface ConsumerUploadRepositoryPort {
    long nextUploadId();

    long findConsumerIdByEmail(String email);

    void insertConsumerUpload(
            long uploadId,
            long consumerId,
            String sourceSystem,
            String crmSystem,
            String crmCustomerId,
            String apiKey,
            byte[] content
    );
}