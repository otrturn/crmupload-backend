package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.util.AccountNameEmbeddingNormalizer;
import com.crm.app.util.AddressEmbeddingNormalizer;
import com.crm.app.util.EmbeddingUtils;
import com.crm.app.worker_common.util.WorkerUtil;
import com.crm.app.worker_duplicate_check_gpu.config.DuplicateCheckGpuProperties;
import com.crm.app.worker_duplicate_check_gpu.dto.CompanyEmbedded;
import com.crm.app.worker_duplicate_check_gpu.dto.EmbeddingMatchType;
import com.crm.app.worker_duplicate_check_gpu.error.WorkerDuplicateCheckGpuEmbeddingException;
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
import java.time.Duration;
import java.time.Instant;
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

            new CreateResultWorkbook().createResultWorkbook(duplicateCheckContent, companiesEmbedded);
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
                companyEmbedded.setNormalisedAccountName(AccountNameEmbeddingNormalizer.normalizeCompanyName(companyEmbedded.getAccountName()));
                companyEmbedded.setPostalCode(getCellValue(row.getCell(WorkerUtil.IDX_POSTCAL_CODE)));
                companyEmbedded.setStreet(getCellValue(row.getCell(WorkerUtil.IDX_STREET)));
                companyEmbedded.setCity(getCellValue(row.getCell(WorkerUtil.IDX_CITY)));
                companyEmbedded.setCountry(getCellValue(row.getCell(WorkerUtil.IDX_COUNTRY)));
                companyEmbedded.setEmailAddress(getCellValue(row.getCell(WorkerUtil.IDX_EMAIL_ADDRESS)));
                companyEmbedded.setPhoneNumber(getCellValue(row.getCell(WorkerUtil.IDX_PHONE_NUMBER)));
                companyEmbedded.setVectorsAccountName(client.embedMany(List.of(companyEmbedded.getNormalisedAccountName())));
                if (properties.isPerformAddressAnalysis()) {
                    companyEmbedded.setNormalisedAddress(AddressEmbeddingNormalizer.normalize(companyEmbedded.getStreet(), companyEmbedded.getCity()));
                    companyEmbedded.setVectorsAddress(client.embedMany(List.of(companyEmbedded.getNormalisedAddress())));
                }
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
            CompanyEmbedded a = companiesEmbedded.get(i);

            for (int j = i + 1; j < companiesEmbedded.size(); j++) {
                CompanyEmbedded b = companiesEmbedded.get(j);

                if (postalCodeAreaEqual(a, b)) {
                    MatchResult match;
                    if (properties.isPerformAddressAnalysis()) {
                        match = evaluateMatchWithAddress(a, b);
                    } else {
                        match = evaluateMatchAccountNameOnly(a, b);
                    }

                    if (match.isMatch()) {
                        a.getSimilarCompanies().put(
                                new CompanyEmbedded.SimilarCompany(match.type(), b),
                                match.score()
                        );
                    }
                }
            }
        }
    }

    private MatchResult evaluateMatchAccountNameOnly(CompanyEmbedded a, CompanyEmbedded b) {
        double nameSim = cosineAccountName(a, b);

        if (nameSim < properties.getCosineSimilarityThresholdAccountName()) {
            return MatchResult.noMatch();
        }

        return MatchResult.match(
                EmbeddingMatchType.ACCOUNT_NAME,
                nameSim
        );
    }

    @SuppressWarnings("squid:S1194")
    private MatchResult evaluateMatchWithAddress(CompanyEmbedded a, CompanyEmbedded b) {
        double nameSim = cosineAccountName(a, b);
        boolean nameMatch = nameSim >= properties.getCosineSimilarityThresholdAccountName();

        double addressSim = cosineAddress(a, b);
        boolean addressMatch = addressSim >= properties.getCosineSimilarityThresholdAddress();

        if (!nameMatch && !addressMatch) {
            return MatchResult.noMatch();
        }

        EmbeddingMatchType type = resolveMatchType(nameMatch, addressMatch);
        double score = resolveScore(nameMatch, addressMatch, nameSim, addressSim);

        return MatchResult.match(type, score);
    }

    private double cosineAccountName(CompanyEmbedded a, CompanyEmbedded b) {
        return EmbeddingUtils.cosineSim(
                a.getVectorsAccountName().get(0),
                b.getVectorsAccountName().get(0)
        );
    }

    private double cosineAddress(CompanyEmbedded a, CompanyEmbedded b) {
        return EmbeddingUtils.cosineSim(
                a.getVectorsAddress().get(0),
                b.getVectorsAddress().get(0)
        );
    }

    private EmbeddingMatchType resolveMatchType(boolean nameMatch, boolean addressMatch) {
        if (nameMatch && addressMatch) {
            return EmbeddingMatchType.ACCOUNT_NAME_AND_ADDRESS;
        }
        return nameMatch ? EmbeddingMatchType.ACCOUNT_NAME : EmbeddingMatchType.ADDRESS;
    }

    private double resolveScore(boolean nameMatch, boolean addressMatch, double nameSim, double addressSim) {
        if (nameMatch && addressMatch) {
            return Math.max(nameSim, addressSim); // oder min/avg – je nach gewünschter Logik
        }
        return nameMatch ? nameSim : addressSim;
    }

    /**
     * kleines Result-Objekt, damit die Hauptmethode flach bleibt
     */
    private record MatchResult(boolean isMatch, EmbeddingMatchType type, double score) {
        static MatchResult noMatch() {
            return new MatchResult(false, null, 0.0);
        }

        static MatchResult match(EmbeddingMatchType type, double score) {
            return new MatchResult(true, type, score);
        }
    }

    private boolean postalCodeAreaEqual(CompanyEmbedded companyEmbedded1, CompanyEmbedded companyEmbedded2) {
        return companyEmbedded1.getPostalCode().charAt(0) == companyEmbedded2.getPostalCode().charAt(0);
    }

    private String getCellValue(Cell cell) {
        return cell != null ? cell.getStringCellValue() : "";
    }

}
