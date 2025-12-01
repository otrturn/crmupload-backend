package com.crm.app.port.consumer;

public interface ConsumerUploadRepositoryPort {
    long nextUploadId();

    long findConsumerIdByEmail(String email);

    void insertConsumerUpload(
            long uploadId,
            long consumerId,
            String crmCustomerId,
            String apiKey,
            byte[] content
    );
}