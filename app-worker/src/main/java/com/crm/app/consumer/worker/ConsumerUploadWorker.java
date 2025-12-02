package com.crm.app.consumer.worker;

import com.crm.app.consumer.worker.config.ConsumerUploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class ConsumerUploadWorker {

    private final com.crm.app.port.consumer.ConsumerUploadRepositoryPort repository;
    private final ConsumerUploadProperties properties;

    public ConsumerUploadWorker(com.crm.app.port.consumer.ConsumerUploadRepositoryPort repository, ConsumerUploadProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.consumer-upload.poll-interval-ms:10000}")
    @Transactional
    public void pollAndProcess() {
        // 1) Jobs claimen
        final List<Long> uploadIds = repository.claimNextUploads(properties.getBatchSize());

        if (uploadIds.isEmpty()) {
            return;
        }

        log.info("Claimed {} consumer_upload job(s): {}", uploadIds.size(), uploadIds);

        // 2) Verarbeitung
        for (Long uploadId : uploadIds) {
            try {
                processUpload(uploadId);
                repository.markUploadDone(uploadId);
            } catch (Exception ex) {
                log.error("Error processing consumer_upload with uploadId={}", uploadId, ex);
                repository.markUploadFailed(uploadId, ex.getMessage());
            }
        }
    }

    private void processUpload(long uploadId) {
        // TODO: hier später Excel aus app.consumer_upload lesen und verarbeiten
        // z.B.:
        // 1) Datensatz per uploadId laden
        // 2) content (BYTEA) als InputStream / ByteArray verarbeiten
        // 3) Firmen/Leads erzeugen etc.
        log.info("Processing consumer_upload uploadId={}", uploadId);
    }
}