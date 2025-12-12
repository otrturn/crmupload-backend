package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check_gpu.dto.CompanyEmbedded;
import com.crm.app.worker_duplicate_check_gpu.error.WorkerDuplicateCheckGpuEmbeddingException;
import com.crm.app.worker_duplicate_check_gpu.error.WorkerDuplicateCheckGpuException;
import com.ki.rag.embedding.client.embed.EmbeddingClient;
import com.ki.rag.embedding.client.embed.EmbeddingClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckGpuWorkerProcessForCheck {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final EmbeddingClientFactory clientFactory;

    public void processDuplicateCheckForCheck(DuplicateCheckContent duplicateCheckContent) {
        log.info("processDuplicateCheckForCheck for {} {}", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem());
        try {
            List<CompanyEmbedded> companiesEmbedded = getEmbedding(duplicateCheckContent);
            createResultWorkbook(duplicateCheckContent, companiesEmbedded);
            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                duplicateCheckRepositoryPort.markDuplicateCheckChecked(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
            } else {
                log.error("Customer not found for customer id={}", duplicateCheckContent.getCustomerId());
            }
        } catch (Exception ex) {
            log.error("ERROR=" + ex.getMessage());
            duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), ex.getMessage());
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
                String accountName = row.getCell(0).getStringCellValue();
                CompanyEmbedded companyEmbedded = new CompanyEmbedded(accountName, client.embedMany(List.of(accountName)));
                companiesEmbedded.add(companyEmbedded);
                idx++;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new WorkerDuplicateCheckGpuEmbeddingException("Cannot get embedding");
        }
        return companiesEmbedded;
    }

    public void createResultWorkbook(DuplicateCheckContent duplicateCheckContent, List<CompanyEmbedded> companiesEmbedded) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            XSSFCellStyle cellStyleHeaderCell = (XSSFCellStyle) workbook.createCellStyle();
            cellStyleHeaderCell.setFillForegroundColor(IndexedColors.GOLD.index);
            cellStyleHeaderCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Sheet sheet = workbook.createSheet("Dubletten");
            Row row = sheet.createRow(0);

            Cell cell;
            cell = row.createCell(0, CellType.STRING);
            cell.setCellValue("Gruppe");
            cell.setCellStyle(cellStyleHeaderCell);
            cell = row.createCell(1, CellType.STRING);
            cell.setCellValue("Firmenname");
            cell.setCellStyle(cellStyleHeaderCell);

            int idx = 1;
            for (CompanyEmbedded companyEmbedded : companiesEmbedded) {
                row = sheet.createRow(idx);
                cell = row.createCell(1, CellType.STRING);
                cell.setCellValue(companyEmbedded.getAccountName());
                idx++;
            }

            workbook.write(bos);
            duplicateCheckContent.setContent(bos.toByteArray());
        } catch (IOException e) {
            throw new WorkerDuplicateCheckGpuException("Fehler beim Erzeugen des Excel-Workbooks: " + e.getMessage());
        }
    }
}