package com.crm.app.duplicate_check_single.process;

import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleMyExcel {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;

    public void processFile() {
        log.info(String.format("Processing duplicate check for MyExcel [%s]", appDuplicateCheckSingleConfig.getExcelPath()));
    }
}
