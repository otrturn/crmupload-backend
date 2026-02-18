package com.crm.app.duplicate_check_single.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.duplicate_check_common.comparison.ComparisonAnalysis;
import com.crm.app.duplicate_check_common.dto.CompanyEmbedded;
import com.crm.app.duplicate_check_common.embedding.EmbeddingService;
import com.crm.app.duplicate_check_common.workbook.CreateResultWorkbook;
import com.crm.app.duplicate_check_common.workbook.WorkbookUtils;
import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import com.crmmacher.error.ErrMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.crm.app.duplicate_check_common.workbook.WorkbookUtils.flushDuplicatesFile;
import static com.crm.app.duplicate_check_common.workbook.WorkbookUtils.flushMarkedFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleEmbeddAndCompare {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;
    private final EmbeddingService embeddingService;
    private final ComparisonAnalysis comparisonAnalysis;
    private final CreateResultWorkbook createResultWorkbook;

    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    public void processFile(List<DuplicateCheckEntry> duplicateCheckEntries, List<ErrMsg> errors) {
        if (ErrMsg.containsErrors(errors)) {
            flushMarkedFile(appDuplicateCheckSingleConfig.getExcelPath(), errors);
            return;
        }

        Instant start = Instant.now();
        log.info("Start embedding ...");
        DuplicateCheckContent duplicateCheckContent = new DuplicateCheckContent(1L, 1L, appDuplicateCheckSingleConfig.getSourceSystem(), WorkbookUtils.createVerifiedExcelAsBytes(duplicateCheckEntries));
        List<CompanyEmbedded> companiesEmbedded = embeddingService.getEmbedding(duplicateCheckContent);
        log.info("Finished embedding ...");
        Duration duration = Duration.between(start, Instant.now());
        log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

        log.info("Start comparison analysis ...");
        start = Instant.now();
        comparisonAnalysis.comparisonAnalysis(companiesEmbedded);
        int totalSimilarCompanies = companiesEmbedded.stream()
                .mapToInt(company -> company.getSimilarCompanies().size())
                .sum();
        log.info("Finished comparison analysis " + totalSimilarCompanies + " Ähnlichkeiten");
        duration = Duration.between(start, Instant.now());
        log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

        log.info("Start email analysis ...");
        start = Instant.now();
        Map<String, List<CompanyEmbedded>> emailDuplicates = comparisonAnalysis.emailAnalysis(companiesEmbedded);
        log.info("Finished email analysis " + emailDuplicates.size() + " E-Mails");
        duration = Duration.between(start, Instant.now());
        log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

        createResultWorkbook.create(duplicateCheckContent, companiesEmbedded, emailDuplicates);

        flushDuplicatesFile(appDuplicateCheckSingleConfig.getExcelPath(), duplicateCheckContent);
    }
}
