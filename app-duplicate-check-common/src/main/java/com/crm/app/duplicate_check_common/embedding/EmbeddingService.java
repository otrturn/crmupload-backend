package com.crm.app.duplicate_check_common.embedding;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.duplicate_check_common.dto.CompanyEmbedded;
import com.crm.app.duplicate_check_common.error.DuplicateCheckGpuEmbeddingException;
import com.crm.app.duplicate_check_common.workbook.WorkbookUtils;
import com.crm.app.util.AccountNameEmbeddingNormalizer;
import com.ki.rag.embedding.client.embed.EmbeddingClient;
import com.ki.rag.embedding.client.embed.EmbeddingClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingService {
    private final EmbeddingClientFactory clientFactory;

    public List<CompanyEmbedded> getEmbedding(DuplicateCheckContent duplicateCheckContent) {
        List<CompanyEmbedded> companiesEmbedded = new ArrayList<>();
        final EmbeddingClient client = clientFactory.forModel("tei-bge-m3");
        try (InputStream fis = new ByteArrayInputStream(duplicateCheckContent.getContent());
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet accountsheet = workbook.getSheetAt(0);
            int idx = 0;
            while (idx <= accountsheet.getLastRowNum()) {
                Row row = accountsheet.getRow(idx);
                CompanyEmbedded companyEmbedded = new CompanyEmbedded();
                companyEmbedded.setAccountName(getCellValue(row.getCell(WorkbookUtils.IDX_ACCOUNTNAME)));
                companyEmbedded.setNormalisedAccountName(AccountNameEmbeddingNormalizer.normalizeCompanyName(companyEmbedded.getAccountName()));
                companyEmbedded.setPostalCode(getCellValue(row.getCell(WorkbookUtils.IDX_POSTCAL_CODE)));
                companyEmbedded.setStreet(getCellValue(row.getCell(WorkbookUtils.IDX_STREET)));
                companyEmbedded.setCity(getCellValue(row.getCell(WorkbookUtils.IDX_CITY)));
                companyEmbedded.setCountry(getCellValue(row.getCell(WorkbookUtils.IDX_COUNTRY)));
                companyEmbedded.setEmailAddress(getCellValue(row.getCell(WorkbookUtils.IDX_EMAIL_ADDRESS)));
                companyEmbedded.setPhoneNumber(getCellValue(row.getCell(WorkbookUtils.IDX_PHONE_NUMBER)));
                companyEmbedded.setCExternalReference(getCellValue(row.getCell(WorkbookUtils.IDX_EXTERNAL_REFERENCE)));

                companyEmbedded.setVectorsAccountName(client.embedMany(List.of(companyEmbedded.getNormalisedAccountName())));

                companiesEmbedded.add(companyEmbedded);
                idx++;
            }
        } catch (IOException e) {
            log.error(String.format("getEmbedding: %s", e.getMessage()), e);
            throw new DuplicateCheckGpuEmbeddingException("Cannot get embedding", e);
        }
        return companiesEmbedded;
    }

    private String getCellValue(Cell cell) {
        return cell != null ? cell.getStringCellValue() : "";
    }

}
