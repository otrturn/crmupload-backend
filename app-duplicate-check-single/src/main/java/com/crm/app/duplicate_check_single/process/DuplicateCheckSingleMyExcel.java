package com.crm.app.duplicate_check_single.process;

import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.duplicate_check_single.config.AppDuplicateCheckSingleConfig;
import com.crm.app.duplicate_check_single.dto.MyExcelSwitch;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.my_excel.dto.MyExcelAccount;
import com.crmmacher.my_excel.dto.MyExcelLead;
import com.crmmacher.my_excel.reader.MyExcelReadAccounts;
import com.crmmacher.my_excel.reader.MyExcelReadLeads;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.crm.app.duplicate_check_common.verification.VerifyAndMapEntriesMyExcel.verifyAndMapEntriesForMyExcelAccounts;
import static com.crm.app.duplicate_check_common.verification.VerifyAndMapEntriesMyExcel.verifyAndMapEntriesForMyExcelLeads;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateCheckSingleMyExcel {
    private final AppDuplicateCheckSingleConfig appDuplicateCheckSingleConfig;
    private final DuplicateCheckSingleEmbeddAndCompare duplicateCheckSingleEmbeddAndCompare;

    public void processFile() {
        log.info(String.format("Processing duplicate check for MyExcel [%s]", appDuplicateCheckSingleConfig.getExcelPath()));
        List<ErrMsg> errors = new ArrayList<>();
        List<DuplicateCheckEntry> duplicateCheckEntriesAll = new ArrayList<>();
        MyExcelSwitch myExcelSwitch = MyExcelSwitch.fromString(appDuplicateCheckSingleConfig.getMyExcelSwitch());
        if (myExcelSwitch.equals(MyExcelSwitch.ACCOUNTS) || myExcelSwitch.equals(MyExcelSwitch.ALL)) {
            List<MyExcelAccount> myExcelAccounts = new MyExcelReadAccounts().getAccounts(Paths.get(appDuplicateCheckSingleConfig.getExcelPath()), errors);
            log.info(String.format("processDuplicateCheck: MyExcel %d account entries read, %d errors", myExcelAccounts.size(), errors.size()));
            List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntriesForMyExcelAccounts(myExcelAccounts, errors);
            log.info(String.format("processDuplicateCheck: MyExcel %d account entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
            duplicateCheckEntriesAll.addAll(duplicateCheckEntries);
        }
        if (myExcelSwitch.equals(MyExcelSwitch.LEADS) || myExcelSwitch.equals(MyExcelSwitch.ALL)) {
            List<MyExcelLead> myExcelLeads = new MyExcelReadLeads().getLeads(Paths.get(appDuplicateCheckSingleConfig.getExcelPath()), errors);
            log.info(String.format("processDuplicateCheck: MyExcel %d lead entries read, %d errors", myExcelLeads.size(), errors.size()));
            List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntriesForMyExcelLeads(myExcelLeads, errors);
            log.info(String.format("processDuplicateCheck: MyExcel %d lead entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
            duplicateCheckEntriesAll.addAll(duplicateCheckEntries);
        }
        log.info(String.format("processDuplicateCheck: MyExcel %d entries mapped, now %d errors", duplicateCheckEntriesAll.size(), errors.size()));

        duplicateCheckSingleEmbeddAndCompare.processFile(duplicateCheckEntriesAll, errors, myExcelSwitch.value());
    }
}
