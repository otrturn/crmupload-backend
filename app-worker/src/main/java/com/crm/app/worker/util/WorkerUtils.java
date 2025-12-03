package com.crm.app.worker.util;

import com.crm.app.port.consumer.Consumer;
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
            log.info("Failed to write Excel file {}", target.getFileName());
            throw new IllegalStateException("Failed to write Excel file {}" + target.getFileName(), e);
        }
    }

    public static void removeFile(Path target) {
        try {
            Files.delete(target);
        } catch (IOException e) {
            log.info("Failed to remove file {}", target.getFileName());
        }
    }

    public static String getFullname(Consumer consumer) {
        if (consumer == null) {
            return "(Kein Name)";
        }
        String firstName = "";
        if (consumer.firstname() != null && !consumer.firstname().isEmpty()) {
            firstName = consumer.firstname();
        }
        String lastName = "";
        if (consumer.lastname() != null && !consumer.lastname().isEmpty()) {
            lastName = consumer.lastname();
        }
        return (!firstName.isEmpty() ? firstName + " " : "") + lastName;
    }

}
