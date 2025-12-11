package com.crm.app.worker_duplicate_check;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.SourceSystem;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicatecheckProcessingService {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;

    private static final String UNKNOWN_SOURCE_SYSTEM = "Unknown sourceSystem: ";

    @Transactional
    public void processSingleDuplcateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        SourceSystem sourceSystem = SourceSystem.fromString(duplicateCheckContent.getSourceSystem());

        switch (sourceSystem) {
            case BEXIO -> handleBexio(duplicateCheckContent);
            case LEXWARE -> handleLexware(duplicateCheckContent);
            case MYEXCEL -> handleMyExcel(duplicateCheckContent);
            default -> {
                log.warn("Unknown sourceSystem '{}' for duplicateCheckId={}", duplicateCheckContent.getSourceSystem(), duplicateCheckContent.getDuplicateCheckId());
                duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), UNKNOWN_SOURCE_SYSTEM + duplicateCheckContent.getSourceSystem());
            }
        }
    }

    private void handleBexio(DuplicateCheckContent duplicateCheckContent) {
        // @TODO to be implemented
        log.info("BEXIO duplicate-check {}", duplicateCheckContent.getDuplicateCheckId());
    }

    private void handleLexware(DuplicateCheckContent duplicateCheckContent) {
        // @TODO to be implemented
        log.info("LEXWARE duplicate-check {}", duplicateCheckContent.getDuplicateCheckId());
    }

    private void handleMyExcel(DuplicateCheckContent duplicateCheckContent) {
        // @TODO to be implemented
        log.info("MYEXCEL duplicate-check {}", duplicateCheckContent.getDuplicateCheckId());
    }
}
