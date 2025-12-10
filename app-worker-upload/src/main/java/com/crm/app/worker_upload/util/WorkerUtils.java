package com.crm.app.worker_upload.util;

import com.crm.app.port.customer.Customer;
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

    public static String getFullname(Customer customer) {
        if (customer == null) {
            return "(Kein Name)";
        }
        String firstName = "";
        if (customer.firstname() != null && !customer.firstname().isEmpty()) {
            firstName = customer.firstname();
        }
        String lastName = "";
        if (customer.lastname() != null && !customer.lastname().isEmpty()) {
            lastName = customer.lastname();
        }
        return (!firstName.isEmpty() ? firstName + " " : "") + lastName;
    }

}
