package com.crm.app.worker_duplicate_check_gpu;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check_gpu.config.DuplicateCheckGpuProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckGpuWorker {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final DuplicateCheckGpuProperties properties;
    private final DuplicateCheckGpuProcessingService processingService;

    @Scheduled(fixedDelayString = "${app.duplicate-check.poll-interval-ms:10000}")
    public void pollAndProcessDuplicateCheckForVerification() {
        final List<Long> duplicateCheckIds = duplicateCheckRepositoryPort.claimNextDuplicateChecksForCheck(properties.getBatchSize());

        if (duplicateCheckIds.isEmpty()) {
            return;
        }

        log.info(String.format("Claimed %d duplicate-check job(s) for check: %s", duplicateCheckIds.size(), String.valueOf(duplicateCheckIds)));

        List<DuplicateCheckContent> duplicateChecks = duplicateCheckRepositoryPort.findDuplicateChecksByIds(duplicateCheckIds);

        for (DuplicateCheckContent duplicateCheck : duplicateChecks) {
            try {
                processingService.processSingleDuplicateCheckForCheck(duplicateCheck);
            } catch (Exception ex) {
                log.error(String.format("Error processing duplicate-check for check with uploadId=%d", duplicateCheck.getDuplicateCheckId()), ex);
            }
        }
    }
}
