package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.worker_duplicate_check_gpu.dto.AddressMatchCategory;
import com.crm.app.worker_duplicate_check_gpu.dto.CompanyEmbedded;
import com.crm.app.worker_duplicate_check_gpu.dto.SimilarCompany;
import com.crm.app.worker_duplicate_check_gpu.error.WorkerDuplicateCheckGpuException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateResultWorkbook {

    public void createResultWorkbook(DuplicateCheckContent duplicateCheckContent, List<CompanyEmbedded> companiesEmbedded) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] ocker = new byte[]{
                    (byte) 0xFF,
                    (byte) 0xFF,
                    (byte) 0xE6,
                    (byte) 0x99
            };

            XSSFColor ockerColor = new XSSFColor(ocker, new DefaultIndexedColorMap());

            XSSFCellStyle cellStyleHeaderCell = (XSSFCellStyle) workbook.createCellStyle();
            cellStyleHeaderCell.setFillForegroundColor(IndexedColors.GOLD.index);
            cellStyleHeaderCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle cellStyleHeaderCellLightBlue = (XSSFCellStyle) workbook.createCellStyle();
            cellStyleHeaderCellLightBlue.setFillForegroundColor(ockerColor);
            cellStyleHeaderCellLightBlue.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle cellStyleHeaderCellLightGreen = (XSSFCellStyle) workbook.createCellStyle();
            cellStyleHeaderCellLightGreen.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.index);
            cellStyleHeaderCellLightGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Sheet sheet = workbook.createSheet("Dubletten");
            int rowIdx = 0;
            int partnerCounter = 0;

            Row row;
            Cell cell;

            row = sheet.createRow(rowIdx);

            cell = row.createCell(0, CellType.STRING);
            cell.setCellValue("Firmenname");
            cell.setCellStyle(cellStyleHeaderCell);

            cell = row.createCell(1, CellType.STRING);
            cell.setCellValue("Ähnliche Firma");
            cell.setCellStyle(cellStyleHeaderCell);

            cell = row.createCell(2, CellType.STRING);
            cell.setCellValue("PLZ");
            cell.setCellStyle(cellStyleHeaderCell);

            cell = row.createCell(3, CellType.STRING);
            cell.setCellValue("Strasse");
            cell.setCellStyle(cellStyleHeaderCell);

            cell = row.createCell(4, CellType.STRING);
            cell.setCellValue("Ort");
            cell.setCellStyle(cellStyleHeaderCell);

            cell = row.createCell(5, CellType.STRING);
            cell.setCellValue("Land");
            cell.setCellStyle(cellStyleHeaderCell);

            rowIdx++;

            int idxFirstRowOfGroup;
            int idxLastRowOfGroup;

            for (CompanyEmbedded companyEmbedded : companiesEmbedded) {
                if (!companyEmbedded.getSimilarCompanies().isEmpty()) {
                    row = sheet.createRow(rowIdx);

                    cell = row.createCell(0, CellType.STRING);
                    cell.setCellValue(companyEmbedded.getAccountName());

                    cell = row.createCell(2, CellType.STRING);
                    cell.setCellValue(companyEmbedded.getPostalCode());

                    cell = row.createCell(3, CellType.STRING);
                    cell.setCellValue(companyEmbedded.getStreet());

                    cell = row.createCell(4, CellType.STRING);
                    cell.setCellValue(companyEmbedded.getCity());

                    cell = row.createCell(5, CellType.STRING);
                    cell.setCellValue(companyEmbedded.getCountry());

                    rowIdx++;

                    idxFirstRowOfGroup = rowIdx;
                    idxLastRowOfGroup = rowIdx;

                    for (SimilarCompany similarCompany : companyEmbedded.getSimilarCompanies()) {
                        idxLastRowOfGroup = rowIdx;

                        row = sheet.createRow(rowIdx);
                        CompanyEmbedded companyEmbeddedSimilar = similarCompany.getCompanyEmbedded();

                        cell = row.createCell(1, CellType.STRING);
                        cell.setCellValue(companyEmbeddedSimilar.getAccountName());
                        cell.setCellStyle(partnerCounter % 2 == 0 ? cellStyleHeaderCellLightBlue : cellStyleHeaderCellLightGreen);

                        cell = row.createCell(2, CellType.STRING);
                        cell.setCellValue(companyEmbeddedSimilar.getPostalCode());

                        cell = row.createCell(3, CellType.STRING);
                        cell.setCellValue(companyEmbeddedSimilar.getStreet());

                        cell = row.createCell(4, CellType.STRING);
                        cell.setCellValue(companyEmbeddedSimilar.getCity());

                        cell = row.createCell(5, CellType.STRING);
                        cell.setCellValue(companyEmbeddedSimilar.getCountry());

                        markRowWithMatchType(similarCompany, row);

                        rowIdx++;
                    }

                    sheet.groupRow(idxFirstRowOfGroup, idxLastRowOfGroup);

                    partnerCounter++;
                }

                rowIdx++;
            }

            workbook.write(bos);
            duplicateCheckContent.setContent(bos.toByteArray());
        } catch (IOException e) {
            log.error(String.format("createResultWorkbook: %s", e.getMessage()), e);
            throw new WorkerDuplicateCheckGpuException("Fehler beim Erzeugen des Excel-Workbooks: ", e);
        }
    }

    @SuppressWarnings("squid:S1194")
    private static void markRowWithMatchType(SimilarCompany similarCompany, Row row) {
        Cell cell;
        if (similarCompany.getMatchType().isAccountNameMatch()) {
            cell = row.createCell(6, CellType.STRING);
            cell.setCellValue("Wahrscheinlich");
        }
        if (similarCompany.getMatchType().getAddressMatchCategory().equals(AddressMatchCategory.MATCH)) {
            cell = row.createCell(7, CellType.STRING);
            cell.setCellValue("Wahrscheinlich");
        } else if (similarCompany.getMatchType().getAddressMatchCategory().equals(AddressMatchCategory.POSSIBLE)) {
            cell = row.createCell(7, CellType.STRING);
            cell.setCellValue("Möglich");
        }
    }
}
