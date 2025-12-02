package com.crm.app.worker;

import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.worker.config.ConsumerUploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        final List<Long> uploadIds = repository.claimNextUploads(properties.getBatchSize());

        if (uploadIds.isEmpty()) {
            return;
        }

        log.info("Claimed {} consumer_upload job(s): {}", uploadIds.size(), uploadIds);

        List<ConsumerUploadContent> uploads = repository.findUploadsByIds(uploadIds);

        for (ConsumerUploadContent upload : uploads) {
            try {
                processUpload(upload);
                repository.markUploadDone(upload.uploadId());
            } catch (Exception ex) {
                log.error("Error processing consumer_upload with uploadId={}", upload.uploadId(), ex);
                repository.markUploadFailed(upload.uploadId(), ex.getMessage());
            }
        }
    }

    private void processUpload(ConsumerUploadContent upload) {
        log.info("Processing consumer_upload uploadId={}", upload.uploadId());
        writeExcelToFile(upload.content(), Paths.get(String.format("%s/Upload_%06d.xlsx", properties.getWorkdir(), upload.uploadId())));
    }

    public void writeExcelToFile(byte[] data, Path target) {
        try {
            Files.write(target, data);
        } catch (IOException e) {
            log.info("Failed to write Excel file {}", target.getFileName());
            throw new IllegalStateException("Failed to write Excel file {}" + target.getFileName(), e);
        }
    }
}