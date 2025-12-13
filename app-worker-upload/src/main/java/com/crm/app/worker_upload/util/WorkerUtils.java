package com.crm.app.worker_upload.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class WorkerUtils {

    private WorkerUtils() {
    }

    public static void writeExcelToFile(byte[] data, Path target) {
        try {
            Files.write(target, data);
        } catch (IOException e) {
            log.info(String.format("Failed to write Excel file %s", target.getFileName()));
            throw new IllegalStateException(String.format("Failed to write Excel file %s", target.getFileName()), e);
        }
    }

    public static void removeFile(Path target) {
        try {
            Files.delete(target);
        } catch (IOException e) {
            log.info(String.format("Failed to remove file %s", target.getFileName()));
        }
    }
}
