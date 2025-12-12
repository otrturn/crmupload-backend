package com.crm.app.worker_duplicate_check_gpu;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.worker_duplicate_check_gpu.process.DuplicateCheckGpuWorkerProcessForCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckGpuProcessingService {

    private final DuplicateCheckGpuWorkerProcessForCheck duplicateCheckGpuWorkerProcessForCheck;

    @Transactional
    public void processSingleDuplicateCheckForCheck(DuplicateCheckContent duplicateCheckContent) {
        log.info("processSingleDuplicateCheckForCheck for {} {}", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem());
        duplicateCheckGpuWorkerProcessForCheck.processDuplicateCheckForCheck(duplicateCheckContent);
    }

}
