package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.Customer;
import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.duplicate_check_common.workbook.WorkbookUtils;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_common.dto.StatisticsError;
import com.crm.app.worker_duplicate_check.mail.DuplicatecheckMailService;
import com.crmmacher.bexio_excel.dto.BexioColumn;
import com.crmmacher.bexio_excel.dto.BexioEntry;
import com.crmmacher.bexio_excel.reader.ReadBexioExcel;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.importer.bexio_excel.config.BexioCtx;
import com.crmmacher.util.ExcelUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.crm.app.duplicate_check_common.verification.VerifyAndMapEntriesBexio.verifyAndMapEntriesForBexio;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckWorkerProcessForBexio {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final DuplicatecheckMailService duplicatecheckMailService;

    private final BexioCtx bexioCtx;

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public void processDuplicateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        log.info(String.format("Processing crm_upload for Bexio duplicateCheckId=%d sourceSysten=%s", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem()));
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<BexioEntry> bexioEntries = new ArrayList<>();
            Map<BexioColumn, Integer> indexMap = new ReadBexioExcel().getEntries(duplicateCheckContent.getContent(), bexioEntries, errors);
            log.info(String.format("Bexio %d entries read, %d errors", bexioEntries.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntriesForBexio(bexioEntries, indexMap, errors);
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
