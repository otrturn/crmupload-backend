package com.crm.app.worker_duplicate_check;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check.config.DuplicateCheckProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckWorker {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final DuplicateCheckProperties properties;
    private final DuplicatecheckProcessingService processingService;

    @Scheduled(fixedDelayString = "${app.duplicate-check.poll-interval-ms:10000}")
    public void pollAndProcess() {
        final List<Long> duplicateCheckIds = duplicateCheckRepositoryPort.claimNextDuplicateChecksForCheck(properties.getBatchSize());

        if (duplicateCheckIds.isEmpty()) {
            return;
        }

        log.info("Claimed {} duplicate-check job(s): {}", duplicateCheckIds.size(), duplicateCheckIds);

        List<DuplicateCheckContent> duplicateChecks = new ArrayList<>();

        for (DuplicateCheckContent duplicateCheck : duplicateChecks) {
            try {
                processingService.processSingleDuplcateCheckForVerification(duplicateCheck);
            } catch (Exception ex) {
                log.error("Error processing crm_upload with uploadId={}", duplicateCheck.getDuplicateCheckId(), ex);
                duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheck.getDuplicateCheckId(), ex.getMessage());
            }
        }
    }
}
