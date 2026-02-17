package com.crm.app.duplicate_check_single.process;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import com.crmmacher.bexio_excel.dto.BexioColumn;
import com.crmmacher.bexio_excel.dto.BexioEntry;
import com.crmmacher.bexio_excel.reader.ReadBexioExcel;
import com.crmmacher.error.ErrMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.crm.app.duplicate_check_common.verification.VerifyAndMapEntries.verifyAndMapEntriesForBexio;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleBexio {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;

    public void processFile() {
        log.info(String.format("Processing duplicate check for Bexio [%s]", appDuplicateCheckSingleConfig.getExcelPath()));
        List<ErrMsg> errors = new ArrayList<>();

        List<BexioEntry> bexioEntries = new ArrayList<>();
        Map<BexioColumn, Integer> indexMap = new ReadBexioExcel().getEntries(Paths.get(appDuplicateCheckSingleConfig.getExcelPath()), bexioEntries, errors);
        log.info(String.format("processDuplicateCheck: Bexio %d entries read, %d errors", bexioEntries.size(), errors.size()));
        List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntriesForBexio(bexioEntries, indexMap, errors);
        log.info(String.format("processDuplicateCheck: Bexio %d entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
    }
}
