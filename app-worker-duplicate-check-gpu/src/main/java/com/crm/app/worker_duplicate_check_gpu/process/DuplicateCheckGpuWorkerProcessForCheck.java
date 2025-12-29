package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.dto.Customer;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.util.AccountNameEmbeddingNormalizer;
import com.crm.app.util.EmbeddingUtils;
import com.crm.app.worker_common.util.WorkerUtil;
import com.crm.app.worker_duplicate_check_gpu.config.DuplicateCheckGpuProperties;
import com.crm.app.worker_duplicate_check_gpu.dto.*;
import com.crm.app.worker_duplicate_check_gpu.error.WorkerDuplicateCheckGpuEmbeddingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckGpuWorkerProcessForCheck {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final EmbeddingClientFactory clientFactory;
    private final DuplicateCheckGpuProperties properties;
    private final CreateResultWorkbook createResultWorkbook;

    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

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

            log.info("Start email analysis ...");
            start = Instant.now();
            Map<String, List<CompanyEmbedded>> emailDuplicates = emailAnalysis(companiesEmbedded);
            log.info("E-Mail duplicates");
            emailDuplicates.forEach((email, companies) -> {
                log.info("E-Mail: " + email);
                companies.forEach(c ->
                        log.info("  - " + c.getAccountName()+", "+c.getCExternalReference())
                );
            });
            log.info("Finished email analysis ...");
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

            createResultWorkbook.create(duplicateCheckContent, companiesEmbedded);
            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                duplicateCheckRepositoryPort.markDuplicateCheckChecked(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent(), GSON.toJson(setStatistics(companiesEmbedded)));
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
                companyEmbedded.setCExternalReference(getCellValue(row.getCell(WorkerUtil.IDX_EXTERNAL_REFERENCE)));

                companyEmbedded.setVectorsAccountName(client.embedMany(List.of(companyEmbedded.getNormalisedAccountName())));

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
                    EmbeddingMatchType embeddingMatchType;
                    if (properties.isPerformAddressAnalysis()) {
                        embeddingMatchType = evaluateMatchWithAddress(a, b);
                    } else {
                        embeddingMatchType = evaluateMatchAccountNameOnly(a, b);
                    }

                    if (entryMatches(embeddingMatchType))
                        a.getSimilarCompanies().add(new SimilarCompany(embeddingMatchType, b));
                }
            }
        }
    }

    private boolean entryMatches(EmbeddingMatchType embeddingMatchType) {
        return embeddingMatchType.isAccountNameMatch()
                || embeddingMatchType.getAddressMatchCategory().equals(AddressMatchCategory.POSSIBLE)
                || embeddingMatchType.getAddressMatchCategory().equals(AddressMatchCategory.MATCH);
    }

    private EmbeddingMatchType evaluateMatchAccountNameOnly(CompanyEmbedded a, CompanyEmbedded b) {
        return cosineAccountName(a, b) < properties.getCosineSimilarityThresholdAccountName() ?
                new EmbeddingMatchType(false, AddressMatchCategory.NO_MATCH) :
                new EmbeddingMatchType(true, AddressMatchCategory.NO_MATCH);
    }

    @SuppressWarnings("squid:S1194")
    private EmbeddingMatchType evaluateMatchWithAddress(CompanyEmbedded a, CompanyEmbedded b) {
        double nameSim = cosineAccountName(a, b);
        boolean nameMatch = nameSim >= properties.getCosineSimilarityThresholdAccountName();

        AddressMatcher.AddressKey addressKeyA = AddressMatcher.of(a.getStreet(), a.getCity());
        AddressMatcher.AddressKey addressKeyB = AddressMatcher.of(b.getStreet(), b.getCity());
        AddressMatcher.MatchResult addressMatchResult = AddressMatcher.match(addressKeyA, addressKeyB);

        return new EmbeddingMatchType(nameMatch, addressMatchResult.category());
    }

    private double cosineAccountName(CompanyEmbedded a, CompanyEmbedded b) {
        return EmbeddingUtils.cosineSim(
                a.getVectorsAccountName().get(0),
                b.getVectorsAccountName().get(0)
        );
    }

    private boolean postalCodeAreaEqual(CompanyEmbedded companyEmbedded1, CompanyEmbedded companyEmbedded2) {
        return companyEmbedded1.getPostalCode().charAt(0) == companyEmbedded2.getPostalCode().charAt(0);
    }

    private String getCellValue(Cell cell) {
        return cell != null ? cell.getStringCellValue() : "";
    }

    private StatisticsDuplicateCheck setStatistics(List<CompanyEmbedded> companiesEmbedded) {
        StatisticsDuplicateCheck statisticsDuplicateCheck = new StatisticsDuplicateCheck();
        statisticsDuplicateCheck.setNEntries(companiesEmbedded.size());
        long accountNameMatches = 0;
        long possibleAddressMatches = 0;
        long addressMatches = 0;

        for (CompanyEmbedded company : companiesEmbedded) {
            for (SimilarCompany similar : company.getSimilarCompanies()) {
                EmbeddingMatchType mt = similar.getMatchType();

                if (mt.isAccountNameMatch()) {
                    accountNameMatches++;
                }

                if (mt.getAddressMatchCategory() == AddressMatchCategory.POSSIBLE) {
                    possibleAddressMatches++;
                } else if (mt.getAddressMatchCategory() == AddressMatchCategory.MATCH) {
                    addressMatches++;
                }
            }
        }
        statisticsDuplicateCheck.setNDuplicateAccountNames(accountNameMatches);
        statisticsDuplicateCheck.setNAddressesPossible(possibleAddressMatches);
        statisticsDuplicateCheck.setNAddressesMatch(addressMatches);
        return statisticsDuplicateCheck;
    }

    private Map<String, List<CompanyEmbedded>> emailAnalysis(List<CompanyEmbedded> companiesEmbedded) {
        if (companiesEmbedded == null || companiesEmbedded.isEmpty()) {
            return Map.of();
        }

        Map<String, List<CompanyEmbedded>> grouped = companiesEmbedded.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getEmailAddress() != null)
                .map(c -> new AbstractMap.SimpleEntry<>(normalizeEmail(c.getEmailAddress()), c))
                .filter(e -> !e.getKey().isBlank())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
