package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.util.CompanyNameNormalizer;
import com.crm.app.util.EmbeddingUtils;
import com.crm.app.worker_common.util.WorkerUtil;
import com.crm.app.worker_duplicate_check_gpu.config.DuplicateCheckGpuProperties;
import com.crm.app.worker_duplicate_check_gpu.dto.CompanyEmbedded;
import com.crm.app.worker_duplicate_check_gpu.error.WorkerDuplicateCheckGpuEmbeddingException;
import com.crm.app.worker_duplicate_check_gpu.error.WorkerDuplicateCheckGpuException;
import com.ki.rag.embedding.client.embed.EmbeddingClient;
import com.ki.rag.embedding.client.embed.EmbeddingClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckGpuWorkerProcessForCheck {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final EmbeddingClientFactory clientFactory;
    private final DuplicateCheckGpuProperties properties;

    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    public void processDuplicateCheckForCheck(DuplicateCheckContent duplicateCheckContent) {
        log.info(String.format("processDuplicateCheckForCheck for %d %s", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem()));
        try {

            log.info("Start embedding ...");
            Instant start = Instant.now();
            List<CompanyEmbedded> companiesEmbedded = getEmbedding(duplicateCheckContent);
            log.info("Finished embedding ...");
            Duration duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

            log.info("Start comparison analysis ...");
            start = Instant.now();
            comparisonAnalysis(companiesEmbedded);
            log.info("Finished comparison analysis ...");
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

            createResultWorkbook(duplicateCheckContent, companiesEmbedded);
            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                duplicateCheckRepositoryPort.markDuplicateCheckChecked(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
            } else {
                log.error(String.format("Customer not found for customerId=%d", duplicateCheckContent.getCustomerId()));
            }
        } catch (Exception ex) {
            log.error(String.format("ERROR=%s", ex.getMessage()), ex);
        }
    }

    private List<CompanyEmbedded> getEmbedding(DuplicateCheckContent duplicateCheckContent) {
        List<CompanyEmbedded> companiesEmbedded = new ArrayList<>();
        final EmbeddingClient client = clientFactory.forModel("tei-bge-m3");
        try (InputStream fis = new ByteArrayInputStream(duplicateCheckContent.getContent());
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet accountsheet = workbook.getSheetAt(0);
            int idx = 0;
            while (idx <= accountsheet.getLastRowNum()) {
                Row row = accountsheet.getRow(idx);
                CompanyEmbedded companyEmbedded = new CompanyEmbedded();
                companyEmbedded.setAccountName(getCellValue(row.getCell(WorkerUtil.IDX_ACCOUNTNAME)));
                companyEmbedded.setNormalisedAccountName(CompanyNameNormalizer.normalizeCompanyName(companyEmbedded.getAccountName()));
                companyEmbedded.setPostalCode(getCellValue(row.getCell(WorkerUtil.IDX_POSTCAL_CODE)));
                companyEmbedded.setStreet(getCellValue(row.getCell(WorkerUtil.IDX_STREET)));
                companyEmbedded.setCity(getCellValue(row.getCell(WorkerUtil.IDX_CITY)));
                companyEmbedded.setCountry(getCellValue(row.getCell(WorkerUtil.IDX_COUNTRY)));
                companyEmbedded.setEmailAddress(getCellValue(row.getCell(WorkerUtil.IDX_EMAIL_ADDRESS)));
                companyEmbedded.setPhoneNumber(getCellValue(row.getCell(WorkerUtil.IDX_PHONE_NUMBER)));
                companyEmbedded.setVectors(client.embedMany(List.of(companyEmbedded.getNormalisedAccountName())));
                companiesEmbedded.add(companyEmbedded);
                idx++;
            }
        } catch (IOException e) {
            log.error(String.format("getEmbedding: %s", e.getMessage()), e);
            throw new WorkerDuplicateCheckGpuEmbeddingException("Cannot get embedding", e);
        }
        return companiesEmbedded;
    }

    private void comparisonAnalysis(List<CompanyEmbedded> companiesEmbedded) {
        for (int i = 0; i < companiesEmbedded.size(); i++) {
            for (int j = i + 1; j < companiesEmbedded.size(); j++) {
                double sim = EmbeddingUtils.cosineSim(companiesEmbedded.get(i).getVectors().get(0), companiesEmbedded.get(j).getVectors().get(0));
                if (sim >= properties.getCosineSimilarityThreshold() && postalCodeAreaEqual(companiesEmbedded.get(i), companiesEmbedded.get(j))) {
                    companiesEmbedded.get(i).getSimilarCompanies().put(companiesEmbedded.get(j), sim);
                }
            }
        }
    }

    private boolean postalCodeAreaEqual(CompanyEmbedded companyEmbedded1, CompanyEmbedded companyEmbedded2) {
        return companyEmbedded1.getPostalCode().charAt(0) == companyEmbedded2.getPostalCode().charAt(0);
    }

    private String getCellValue(Cell cell) {
        return cell != null ? cell.getStringCellValue() : "";
    }

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

                    for (Map.Entry<CompanyEmbedded, Double> similarCompanyEntry : companyEmbedded.getSimilarCompanies().entrySet()) {
                        idxLastRowOfGroup = rowIdx;

                        row = sheet.createRow(rowIdx);
                        CompanyEmbedded companyEmbeddedSimilar = similarCompanyEntry.getKey();

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
}
