package com.crm.app.duplicate_check_single.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.duplicate_check_common.comparison.ComparisonAnalysis;
import com.crm.app.duplicate_check_common.dto.CompanyEmbedded;
import com.crm.app.duplicate_check_common.embedding.EmbeddingService;
import com.crm.app.duplicate_check_common.error.DuplicateCheckGpuException;
import com.crm.app.duplicate_check_common.workbook.CreateResultWorkbook;
import com.crm.app.duplicate_check_common.workbook.WorkbookUtils;
import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.lexware_excel.dto.LexwareColumn;
import com.crmmacher.lexware_excel.dto.LexwareEntry;
import com.crmmacher.lexware_excel.reader.ReadLexwareExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.crm.app.duplicate_check_common.verification.VerifyAndMapEntries.verifyAndMapEntriesForLexware;
import static com.crm.app.duplicate_check_common.workbook.WorkbookUtils.setNameOfDuplicatesExcelFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleLexware {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;
    private final EmbeddingService embeddingService;
    private final ComparisonAnalysis comparisonAnalysis;
    private final CreateResultWorkbook createResultWorkbook;

    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    public void processFile() {
        log.info(String.format("Processing duplicate check for Lexware [%s]", appDuplicateCheckSingleConfig.getExcelPath()));
        List<ErrMsg> errors = new ArrayList<>();
        List<LexwareEntry> lexwareEntries = new ArrayList<>();
        Map<LexwareColumn, Integer> indexMap = new ReadLexwareExcel().getEntries(Paths.get(appDuplicateCheckSingleConfig.getExcelPath()), lexwareEntries, errors);
        log.info(String.format("processDuplicateCheck: Lexware %d entries read, %d errors", lexwareEntries.size(), errors.size()));
        List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntriesForLexware(lexwareEntries, indexMap, errors);
        log.info(String.format("processDuplicateCheck: Lexware %d entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));

        DuplicateCheckContent duplicateCheckContent = new DuplicateCheckContent(1L, 1L, "Lexware", WorkbookUtils.createVerifiedExcelAsBytes(duplicateCheckEntries));
        Instant start = Instant.now();

        log.info("Start embedding ...");
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
        try {
            Files.write(Path.of(setNameOfDuplicatesExcelFile(appDuplicateCheckSingleConfig.getExcelPath())), duplicateCheckContent.getContent());
        } catch (Exception e) {
            throw new DuplicateCheckGpuException(setNameOfDuplicatesExcelFile(appDuplicateCheckSingleConfig.getExcelPath()), e);
        }
    }
}
