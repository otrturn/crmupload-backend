package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.worker_duplicate_check.error.WorkerDuplicateCheckException;
import com.crmmacher.bexio_excel.error.BexioReaderException;
import com.crmmacher.error.ErrMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
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

    public static byte[] markExcelFile(byte[] excelBytes, List<ErrMsg> errors) {
        try (InputStream fis = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(fis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            colourCells(errors, workbook);
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BexioReaderException("Cannot process excel files [byteArray][byteArray]");
        }
    }

    public static void colourCells(List<ErrMsg> errors, Workbook workbook) {
        XSSFCellStyle cellStyleMarkedCell = (XSSFCellStyle) workbook.createCellStyle();
        cellStyleMarkedCell.setFillForegroundColor(IndexedColors.RED.index);
        cellStyleMarkedCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (ErrMsg errMsg : errors) {
            Sheet sheet = workbook.getSheetAt(errMsg.getSheetNum());
            Row row = sheet.getRow(errMsg.getRowNum());
            Cell cell = row.getCell(errMsg.getColNum());
            if (cell == null) {
                cell = row.createCell(errMsg.getColNum());
            }

            cell.setCellStyle(cellStyleMarkedCell);
        }
    }
}
