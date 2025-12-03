package com.crm.app.worker.process;

import com.crm.app.port.consumer.Consumer;
import com.crm.app.port.consumer.ConsumerUploadContent;
import com.crm.app.port.consumer.ConsumerUploadRepositoryPort;
import com.crm.app.worker.mail.UploadMailService;
import com.crmmacher.bexio_excel.error.BexioReaderException;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.dto.EspoEntityPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadHandlingForEspo {

    private final ConsumerUploadRepositoryPort repository;
    private final UploadMailService uploadMailService;

    public void processForEspo(ConsumerUploadContent upload, Path excelSourcefile, Path excelTargetfile, List<ErrMsg> errors, Consumer consumer, EspoEntityPool espoEntityPool) {
        if (!ErrMsg.containsErrors(errors)) {
            repository.markUploadDone(upload.uploadId());
            uploadMailService.sendSuccessMailForEspo(consumer, upload, espoEntityPool);
        } else {
            repository.markUploadFailed(upload.uploadId(), "Validation failed");
            markExcelFile(excelSourcefile, excelTargetfile, errors);
            uploadMailService.sendErrorMailForEspo(consumer, upload, errors, excelTargetfile);
        }
    }

    private void markExcelFile(Path excelSourcefile, Path excelTargetfile, List<ErrMsg> errors) {
        try (InputStream fis = Files.newInputStream(excelSourcefile);
             Workbook workbook = new XSSFWorkbook(fis);
             OutputStream os = Files.newOutputStream(excelTargetfile)) {
            colourCells(errors, workbook);
            workbook.write(os);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BexioReaderException("Cannot process excel files [" + excelSourcefile + "][" + excelTargetfile + "]");
        }
    }

    private static void colourCells(List<ErrMsg> errors, Workbook workbook) {
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
