package com.crm.app.worker_common.util;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.worker_common.error.WorkerUtilCheckException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public class WorkerUtil {

    public static final int IDX_ACCOUNTNAME = 0;
    public static final int IDX_POSTCAL_CODE = 1;
    public static final int IDX_STREET = 2;
    public static final int IDX_CITY = 3;
    public static final int IDX_COUNTRY = 4;
    public static final int IDX_EMAIL_ADDRESS = 5;
    public static final int IDX_PHONE_NUMBER = 6;
    public static final int IDX_EXTERNAL_REFERENCE = 7;

    private WorkerUtil() {
    }

    public static byte[] createVerifiedExcelAsBytes(List<DuplicateCheckEntry> duplicateCheckEntries) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Firmen");

            int i = 0;
            for (DuplicateCheckEntry duplicateCheckEntry : duplicateCheckEntries) {
                Row row = sheet.createRow(i);
                row.createCell(IDX_ACCOUNTNAME, CellType.STRING).setCellValue(duplicateCheckEntry.getAccountName());
                row.createCell(IDX_POSTCAL_CODE, CellType.STRING).setCellValue(duplicateCheckEntry.getPostalCode());
                row.createCell(IDX_STREET, CellType.STRING).setCellValue(duplicateCheckEntry.getStreet());
                row.createCell(IDX_CITY, CellType.STRING).setCellValue(duplicateCheckEntry.getCity());
                row.createCell(IDX_COUNTRY, CellType.STRING).setCellValue(duplicateCheckEntry.getCountry());
                row.createCell(IDX_EMAIL_ADDRESS, CellType.STRING).setCellValue(duplicateCheckEntry.getEmailAddress());
                row.createCell(IDX_PHONE_NUMBER, CellType.STRING).setCellValue(duplicateCheckEntry.getPhoneNumber());
                row.createCell(IDX_EXTERNAL_REFERENCE, CellType.STRING).setCellValue(duplicateCheckEntry.getCExternalReference());
                i++;
            }
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Cannot process excel file [byteArray][byteArray]", e);
            throw new WorkerUtilCheckException("Error creating the excel workbook: ", e);
        }
    }

}
