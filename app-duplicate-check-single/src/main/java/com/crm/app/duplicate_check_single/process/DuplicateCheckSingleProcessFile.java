package com.crm.app.duplicate_check_single.process;

import com.crm.app.dto.SourceSystem;
import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.crm.app.dto.SourceSystem.availableSourceSystems;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleProcessFile {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;
    private final DuplicateCheckSingleBexio duplicateCheckSingleBexio;
    private final DuplicateCheckSingleLexware duplicateCheckSingleLexware;
    private final DuplicateCheckSingleMyExcel duplicateCheckSingleMyExcel;

    public void processFile() {
        SourceSystem sourceSystem = SourceSystem.fromString(appDuplicateCheckSingleConfig.getSourceSystem());

        switch (sourceSystem) {
            case BEXIO -> duplicateCheckSingleBexio.processFile();
            case LEXWARE -> duplicateCheckSingleLexware.processFile();
            case MYEXCEL -> duplicateCheckSingleMyExcel.processFile();
            default -> {
                String msg = String.format("Unknown sourceSystem '%s'", appDuplicateCheckSingleConfig.getSourceSystem() + " available: " + availableSourceSystems());
                log.error(msg);
            }
        }

    }
}
