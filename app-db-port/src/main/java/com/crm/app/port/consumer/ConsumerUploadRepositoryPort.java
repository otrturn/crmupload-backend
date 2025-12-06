package com.crm.app.port.consumer;

import com.crm.app.dto.ConsumerUploadContent;
import com.crm.app.dto.ConsumerUploadHistory;

import java.util.List;
import java.util.Optional;

public interface ConsumerUploadRepositoryPort {
    long nextUploadId();

    long findConsumerIdByEmail(String email);

    Optional<Consumer> findConsumerByConsumerId(long consumerId);

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