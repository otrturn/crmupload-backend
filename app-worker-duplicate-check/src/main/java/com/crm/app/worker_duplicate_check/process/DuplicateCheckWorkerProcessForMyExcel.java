package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.Customer;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.duplicate_check_common.workbook.WorkbookUtils;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_common.dto.StatisticsError;
import com.crm.app.worker_duplicate_check.mail.DuplicatecheckMailService;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.importer.my_excel.config.MyExcelCtx;
import com.crmmacher.my_excel.dto.MyExcelAccount;
import com.crmmacher.my_excel.reader.MyExcelReadAccounts;
import com.crmmacher.util.ExcelUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.crm.app.duplicate_check_common.verification.VerifyAndMapEntries.verifyAndMapEntriesForMyExcel;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckWorkerProcessForMyExcel {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final DuplicatecheckMailService duplicatecheckMailService;

    private final MyExcelCtx myExcelCtx;

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public void processDuplicateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        log.info(String.format("Processing crm_upload for MyExcel duplicateCheckId=%d sourceSysten=%s", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem()));
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<MyExcelAccount> myExcelAccounts = new MyExcelReadAccounts().getAccounts(duplicateCheckContent.getContent(), errors);

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntriesForMyExcel(myExcelAccounts, errors);
                log.info(String.format("processDuplicateCheck: %d entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
                if (!ErrMsg.containsErrors(errors)) {
                    duplicateCheckContent.setContent(WorkbookUtils.createVerifiedExcelAsBytes(duplicateCheckEntries));
                    duplicateCheckRepositoryPort.markDuplicateCheckVerified(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
                } else {
                    duplicatecheckMailService.sendErrorMail(customer.get(), duplicateCheckContent, errors, ExcelUtils.markExcelFile(duplicateCheckContent.getContent(), errors));
                    StatisticsError statisticsError = new StatisticsError();
                    statisticsError.setFromErrMsg(errors);
                    duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), "Verification failed", GSON.toJson(statisticsError));
                }
            } else {
                log.error(String.format("Customer not found for customerId=%d", duplicateCheckContent.getCustomerId()));
            }
        } catch (Exception ex) {
            log.error(String.format("processDuplicateCheckForVerification: %s", ex.getMessage()), ex);
        }
    }

}
