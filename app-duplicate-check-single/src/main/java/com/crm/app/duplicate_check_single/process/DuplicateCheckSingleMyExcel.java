package com.crm.app.duplicate_check_single.process;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.my_excel.dto.MyExcelAccount;
import com.crmmacher.my_excel.reader.MyExcelReadAccounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.crm.app.duplicate_check_common.verification.VerifyAndMap.verifyAndMapEntriesForMyExcel;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleMyExcel {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;

    public void processFile() {
        log.info(String.format("Processing duplicate check for MyExcel [%s]", appDuplicateCheckSingleConfig.getExcelPath()));
        List<ErrMsg> errors = new ArrayList<>();
        List<MyExcelAccount> myExcelAccounts = new MyExcelReadAccounts().getAccounts(Paths.get(appDuplicateCheckSingleConfig.getExcelPath()), errors);
        log.info(String.format("processDuplicateCheck: MyExcel %d entries read, %d errors", myExcelAccounts.size(), errors.size()));
        List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntriesForMyExcel(myExcelAccounts, errors);
        log.info(String.format("processDuplicateCheck: MyExcel %d entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
    }
}
