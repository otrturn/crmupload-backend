package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.dto.Customer;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.duplicate_check_common.comparison.ComparisonAnalysis;
import com.crm.app.duplicate_check_common.dto.CompanyEmbedded;
import com.crm.app.duplicate_check_common.embedding.EmbeddingService;
import com.crm.app.duplicate_check_common.workbook.CreateResultWorkbook;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckGpuWorkerProcessForCheck {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final CreateResultWorkbook createResultWorkbook;
    private final EmbeddingService embeddingService;
    private final ComparisonAnalysis comparisonAnalysis;

    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public void processDuplicateCheckForCheck(DuplicateCheckContent duplicateCheckContent) {
        log.info(String.format("processDuplicateCheckForCheck for %d %s", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem()));
        try {

            log.info("Start embedding ...");
            Instant start = Instant.now();
            List<CompanyEmbedded> companiesEmbedded = embeddingService.getEmbedding(duplicateCheckContent);
            log.info("Finished embedding ...");
            Duration duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

            log.info("Start comparison analysis ...");
            start = Instant.now();
            comparisonAnalysis.comparisonAnalysis(companiesEmbedded);
            log.info("Finished comparison analysis ...");
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

            log.info("Start email analysis ...");
            start = Instant.now();
            Map<String, List<CompanyEmbedded>> emailDuplicates = comparisonAnalysis.emailAnalysis(companiesEmbedded);
            log.info("E-Mail duplicates");
            log.info("Finished email analysis ...");
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

            createResultWorkbook.create(duplicateCheckContent, companiesEmbedded, emailDuplicates);
            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                duplicateCheckRepositoryPort.markDuplicateCheckChecked(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent(), GSON.toJson(comparisonAnalysis.setStatistics(companiesEmbedded, emailDuplicates)));
            } else {
                log.error(String.format("Customer not found for customerId=%d", duplicateCheckContent.getCustomerId()));
            }
        } catch (Exception ex) {
            log.error(String.format("ERROR=%s", ex.getMessage()), ex);
        }
    }

}
