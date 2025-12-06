package com.crm.app.process;

import com.crm.app.config.AppMaintenanceConfig;
import com.crm.app.error.MaintenanceException;
import com.crm.app.dto.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetUploadFile {

    private final ConsumerUploadRepositoryPort repository;
    private final AppMaintenanceConfig appMaintenanceConfig;

    public void get(long uploadId) {
        log.info("Get Upload File for {}.", uploadId);
        List<ConsumerUploadContent> uploads = repository.findUploadsByIds(List.of(uploadId));
        if (uploads.isEmpty()) {
            throw new MaintenanceException("no upload found for id " + uploadId);
        }
        ConsumerUploadContent upload = uploads.get(0);
        Path excelSourceFile = Paths.get(String.format("%s/Maintenance_%s_%s_%06d.xlsx", appMaintenanceConfig.getWorkdir(), upload.sourceSystem(), upload.crmSystem(), upload.uploadId()));
        writeExcelToFile(upload.content(), excelSourceFile);
    }

    public static void writeExcelToFile(byte[] data, Path target) {
        try {
            Files.write(target, data);
        } catch (IOException e) {
            log.info("Failed to write Excel file {}", target.getFileName());
            throw new IllegalStateException("Failed to write Excel file {}" + target.getFileName(), e);
        }
    }
}
