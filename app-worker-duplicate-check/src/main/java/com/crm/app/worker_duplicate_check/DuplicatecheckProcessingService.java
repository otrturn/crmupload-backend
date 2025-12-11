package com.crm.app.worker_duplicate_check;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.SourceSystem;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check.process.DuplicateCheckWorkerProcessForBexio;
import com.crm.app.worker_duplicate_check.process.DuplicateCheckWorkerProcessForLexware;
import com.crm.app.worker_duplicate_check.process.DuplicateCheckWorkerProcessForMyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicatecheckProcessingService {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final DuplicateCheckWorkerProcessForLexware duplicateCheckWorkerProcessForLexware;
    private final DuplicateCheckWorkerProcessForBexio duplicateCheckWorkerProcessForBexio;
    private final DuplicateCheckWorkerProcessForMyExcel duplicateCheckWorkerProcessForMyExcel;

    private static final String UNKNOWN_SOURCE_SYSTEM = "Unknown sourceSystem: ";

    @Transactional
    public void processSingleDuplicateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        SourceSystem sourceSystem = SourceSystem.fromString(duplicateCheckContent.getSourceSystem());

        switch (sourceSystem) {
            case BEXIO -> duplicateCheckWorkerProcessForBexio.processDuplicateCheck(duplicateCheckContent);
            case LEXWARE -> duplicateCheckWorkerProcessForLexware.processDuplicateCheck(duplicateCheckContent);
            case MYEXCEL -> duplicateCheckWorkerProcessForMyExcel.processDuplicateCheck(duplicateCheckContent);
            default -> {
                log.warn("Unknown sourceSystem '{}' for duplicateCheckId={}", duplicateCheckContent.getSourceSystem(), duplicateCheckContent.getDuplicateCheckId());
                duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), UNKNOWN_SOURCE_SYSTEM + duplicateCheckContent.getSourceSystem());
            }
        }
    }
}
