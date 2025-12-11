package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.worker_duplicate_check.error.WorkerDuplicateCheckException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ProcessUtil {
    private ProcessUtil() {
    }

    public static byte[] createExcelAsBytes(List<DuplicateCheckEntry> duplicateCheckEntries) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Firmen");

            int i = 0;
            for (DuplicateCheckEntry duplicateCheckEntry : duplicateCheckEntries) {
                Row row = sheet.createRow(i);
                row.createCell(0, CellType.STRING).setCellValue(duplicateCheckEntry.getAccountName());
                row.createCell(1, CellType.STRING).setCellValue(duplicateCheckEntry.getPostalCode());
                row.createCell(2, CellType.STRING).setCellValue(duplicateCheckEntry.getStreet());
                row.createCell(3, CellType.STRING).setCellValue(duplicateCheckEntry.getCity());
                row.createCell(4, CellType.STRING).setCellValue(duplicateCheckEntry.getCountry());
                row.createCell(5, CellType.STRING).setCellValue(duplicateCheckEntry.getEmailAddress());
                row.createCell(6, CellType.STRING).setCellValue(duplicateCheckEntry.getPhoneNumber());
                i++;
            }
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new WorkerDuplicateCheckException("Fehler beim Erzeugen des Excel-Workbooks: " + e.getMessage());
        }
    }
}
