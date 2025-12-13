package com.crm.app.worker_duplicate_check;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check.config.DuplicateCheckProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckWorker {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final DuplicateCheckProperties properties;
    private final DuplicateCheckProcessingService processingService;

    @Scheduled(fixedDelayString = "${app.duplicate-check.poll-interval-ms:10000}")
    public void pollAndProcessDuplicateCheckForVerification() {
        final List<Long> duplicateCheckIds = duplicateCheckRepositoryPort.claimNextDuplicateChecksForVerification(properties.getBatchSize());

        if (duplicateCheckIds.isEmpty()) {
            return;
        }

        log.info(String.format("Claimed %d duplicate-check job(s) for verification: %s", duplicateCheckIds.size(), String.valueOf(duplicateCheckIds)));

        List<DuplicateCheckContent> duplicateChecks = duplicateCheckRepositoryPort.findDuplicateChecksByIds(duplicateCheckIds);

        for (DuplicateCheckContent duplicateCheck : duplicateChecks) {
            try {
                processingService.processSingleDuplicateCheckForVerification(duplicateCheck);
            } catch (Exception ex) {
                log.error(String.format("Error processing duplicate-check for verification with uploadId=%d", duplicateCheck.getDuplicateCheckId()), ex);
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.duplicate-check.poll-interval-ms:10000}")
    public void pollAndProcessDuplicateCheckForFinalisation() {
        final List<Long> duplicateCheckIds = duplicateCheckRepositoryPort.claimNextDuplicateChecksForFinalisation(properties.getBatchSize());

        if (duplicateCheckIds.isEmpty()) {
            return;
        }

        log.info(String.format("Claimed %d duplicate-check job(s) for finalisation: %s", duplicateCheckIds.size(), String.valueOf(duplicateCheckIds)));

        List<DuplicateCheckContent> duplicateChecks = duplicateCheckRepositoryPort.findDuplicateChecksByIds(duplicateCheckIds);

        for (DuplicateCheckContent duplicateCheck : duplicateChecks) {
            try {
                processingService.processSingleDuplicateCheckForFinalisation(duplicateCheck);
            } catch (Exception ex) {
                log.error(String.format("Error processing duplicate-check for finalisation with uploadId=%d", duplicateCheck.getDuplicateCheckId()), ex);
            }
        }
    }
}
