package com.crm.app.duplicate_check_single.process;

import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.lexware_excel.dto.LexwareColumn;
import com.crmmacher.lexware_excel.dto.LexwareEntry;
import com.crmmacher.lexware_excel.reader.ReadLexwareExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleLexware {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;

    public void processFile() {
        log.info(String.format("Processing duplicate check for Lexware [%s]", appDuplicateCheckSingleConfig.getExcelPath()));
        List<ErrMsg> errors = new ArrayList<>();
        List<LexwareEntry> lexwareEntries = new ArrayList<>();
        Map<LexwareColumn, Integer> indexMap = new ReadLexwareExcel().getEntries(Paths.get(appDuplicateCheckSingleConfig.getExcelPath()), lexwareEntries, errors);
        log.info(String.format("Lexware %d entries read, %d errors", lexwareEntries.size(), errors.size()));
    }
}
