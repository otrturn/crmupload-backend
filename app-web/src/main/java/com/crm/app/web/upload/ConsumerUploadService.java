package com.crm.app.web.upload;

import com.crm.app.dto.ConsumerUploadHistory;
import com.crm.app.dto.UploadRequest;
import com.crm.app.port.consumer.ConsumerRepositoryPort;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.web.error.ConsumerNotFoundException;
import com.crm.app.web.error.UploadNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerUploadService {

    private final ConsumerUploadRepositoryPort repository;
    private final ConsumerRepositoryPort consumerRepositoryPort;

    public void processUpload(
            String emailAddress,
            String sourceSystem,
            String crmSystem,
            String crmCustomerId,
            String crmApiKey,
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("processUpload: Uploaded file must not be empty");
        }

        log.info("Received upload: email={}, crmCustomerId={}", emailAddress, crmCustomerId);

        UploadRequest request = new UploadRequest(
                emailAddress,
                sourceSystem,
                crmSystem,
                crmCustomerId,
                crmApiKey
        );

        long consumerId = repository.findConsumerIdByEmail(request.emailAddress());
        log.info("Resolved consumerId={} for email={}", consumerId, emailAddress);

        boolean enabled = consumerRepositoryPort.isEnabledByConsumerId(consumerId);
        boolean hasOpenUploads = consumerRepositoryPort.isHasOpenUploadsByConsumerId(consumerId);

        if (!enabled) {
            throw new UploadNotAllowedException(String.format("processUpload: Consumer %s is not enabled", emailAddress));
        }
        if (hasOpenUploads) {
            throw new UploadNotAllowedException(String.format("processUpload: Consumer %s has open uploads", emailAddress));
        }

        long uploadId = repository.nextUploadId();
        log.info("Generated uploadId={}", uploadId);

        try {
            repository.insertConsumerUpload(
                    uploadId,
                    consumerId,
                    sourceSystem,
                    crmSystem,
                    crmCustomerId,
                    crmApiKey,
                    file.getBytes()
            );
        } catch (Exception ex) {
            log.error("processUpload: Failed to insert consumer upload: uploadId={}, consumerId={}", uploadId, consumerId, ex);
            throw new IllegalStateException("Upload failed: " + ex.getMessage(), ex);
        }
    }

    public List<ConsumerUploadHistory> getConsumerUploadHistoryByEmail(String emailAddress) {
        List<ConsumerUploadHistory> response = consumerRepositoryPort.findUploadHistoryByEmailAddress(emailAddress);
        if (response == null) {
            throw new ConsumerNotFoundException(emailAddress);
        }
        return response;
    }

}
