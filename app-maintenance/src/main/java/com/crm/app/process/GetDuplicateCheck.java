package com.crm.app.process;

import com.crm.app.config.AppMaintenanceConfig;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.error.MaintenanceException;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
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
public class GetDuplicateCheck {

    private final DuplicateCheckRepositoryPort repository;
    private final AppMaintenanceConfig appMaintenanceConfig;

    public void get(long duplicateCheckId) {
        log.info("Get Duplicate Check File for {}.", duplicateCheckId);
        List<DuplicateCheckContent> duplicateChecks = repository.findDuplicateChecksByIds(List.of(duplicateCheckId));
        if (duplicateChecks.isEmpty()) {
            throw new MaintenanceException("no duplicate check found for id " + duplicateCheckId);
        }
        DuplicateCheckContent duplicateCheck = duplicateChecks.get(0);
        Path excelSourceFile = Paths.get(String.format("%s/Maintenance_Duplicate_Check_%s_%06d.xlsx", appMaintenanceConfig.getWorkdir(), duplicateCheck.getSourceSystem(), duplicateCheck.getDuplicateCheckId()));
        writeExcelToFile(duplicateCheck.getContent(), excelSourceFile);
        log.info("{} written ", excelSourceFile);
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
