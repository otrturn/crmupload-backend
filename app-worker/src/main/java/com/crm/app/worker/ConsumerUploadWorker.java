package com.crm.app.worker;

import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.worker.config.ConsumerUploadProperties;
import com.crm.app.worker.process.UploadWorkerProcessForBexio;
import com.crm.app.worker.process.UploadWorkerProcessForLexware;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerUploadWorker {

    private final ConsumerUploadRepositoryPort repository;
    private final ConsumerUploadProperties properties;
    private final UploadWorkerProcessForBexio uploadWorkerProcessForBexio;
    private final UploadWorkerProcessForLexware uploadWorkerProcessForLexware;

    @Scheduled(fixedDelayString = "${app.consumer-upload.poll-interval-ms:10000}")
    @Transactional
    public void pollAndProcess() {
        final List<Long> uploadIds = repository.claimNextUploads(properties.getBatchSize());

        if (uploadIds.isEmpty()) {
            return;
        }

        log.info("Claimed {} consumer_upload job(s): {}", uploadIds.size(), uploadIds);

        List<ConsumerUploadContent> uploads = repository.findUploadsByIds(uploadIds);

        for (ConsumerUploadContent upload : uploads) {
            try {
                switch (upload.sourceSystem()) {
                    case "Bexio": {
                        uploadWorkerProcessForBexio.processUpload(upload);
                        break;
                    }
                    case "Lexware": {
                        uploadWorkerProcessForLexware.processUpload(upload);
                        break;
                    }
                    default: {
                        repository.markUploadFailed(upload.uploadId(), "Unknown sourceSystem" + upload.sourceSystem());
                    }
                }
            } catch (Exception ex) {
                log.error("Error processing consumer_upload with uploadId={}", upload.uploadId(), ex);
                repository.markUploadFailed(upload.uploadId(), ex.getMessage());
            }
        }
    }
}