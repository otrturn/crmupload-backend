package com.crm.app.worker_upload;

import com.crm.app.dto.CrmUploadContent;
import com.crm.app.port.customer.CrmUploadRepositoryPort;
import com.crm.app.worker_upload.config.CrmUploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrmUploadWorker {

    private final CrmUploadRepositoryPort repository;
    private final CrmUploadProperties properties;
    private final CrmUploadProcessingService processingService;

    @Scheduled(fixedDelayString = "${app.crm-upload.poll-interval-ms:10000}")
    public void pollAndProcessCrmUpload() {
        final List<Long> uploadIds = repository.claimNextUploads(properties.getBatchSize());

        if (uploadIds.isEmpty()) {
            return;
        }

        log.info("Claimed {} crm_upload job(s): {}", uploadIds.size(), uploadIds);

        List<CrmUploadContent> uploads = repository.findUploadsByIds(uploadIds);

        for (CrmUploadContent upload : uploads) {
            try {
                processingService.processSingleUpload(upload);
            } catch (Exception ex) {
                log.error("Error processing crm_upload with uploadId={}", upload.getUploadId(), ex);
                repository.markUploadFailed(upload.getUploadId(), ex.getMessage());
            }
        }
    }
}
