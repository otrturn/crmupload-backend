package com.crm.app.worker.process;

import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.worker.config.ConsumerUploadProperties;
import com.crm.app.worker.util.WorkerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.crm.app.worker.util.WorkerUtils.writeExcelToFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadWorkerProcessForLexware {

    private final ConsumerUploadRepositoryPort repository;
    private final ConsumerUploadProperties properties;

    public void processUpload(ConsumerUploadContent upload) {
        Path excelFile = Paths.get(String.format("%s/Upload_Lexware_%06d.xlsx", properties.getWorkdir(), upload.uploadId()));
        log.info("Processing consumer_upload for Lexware uploadId={} sourceSysten={} crmSystem={}", upload.uploadId(), upload.sourceSystem(), upload.crmSystem());
        try {
            writeExcelToFile(upload.content(), excelFile);
            repository.markUploadDone(upload.uploadId());
        } catch (Exception ex) {
            repository.markUploadFailed(upload.uploadId(), ex.getMessage());
        }
        WorkerUtils.removeFile(excelFile);
    }
}