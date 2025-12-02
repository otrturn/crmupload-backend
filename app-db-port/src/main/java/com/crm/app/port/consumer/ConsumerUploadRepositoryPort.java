package com.crm.app.port.consumer;

import java.util.List;

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

    List<Long> claimNextUploads(int limit);

    void markUploadDone(long uploadId);

    void markUploadFailed(long uploadId, String errorMessage);

    List<ConsumerUploadContent> findUploadsByIds(List<Long> uploadIds);
}