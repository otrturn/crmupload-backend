package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_common.util.WorkerUtil;
import com.crm.app.worker_duplicate_check.mail.DuplicatecheckMailService;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.importer.lexware_excel.config.LexwareCtx;
import com.crmmacher.lexware_excel.dto.LexwareColumn;
import com.crmmacher.lexware_excel.dto.LexwareEntry;
import com.crmmacher.lexware_excel.reader.ReadLexwareExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckWorkerProcessForLexware {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final DuplicatecheckMailService duplicatecheckMailService;

    private final LexwareCtx lexwareCtx;

    public void processDuplicateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        log.info("Processing crm_upload for Lexware duplicateCheckId={} sourceSysten={}", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem());
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<LexwareEntry> lexwareEntries = new ArrayList<>();
            Map<LexwareColumn, Integer> indexMap = new ReadLexwareExcel().getEntries(duplicateCheckContent.getContent(), lexwareEntries, errors);
            log.info(String.format("Lexware %d entries read, %d errors", lexwareEntries.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntries(lexwareEntries, indexMap, errors);
                log.info(String.format("processDuplicateCheck: %d entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
                if (!ErrMsg.containsErrors(errors)) {
                    duplicateCheckContent.setContent(WorkerUtil.createVerifiedExcelAsBytes(duplicateCheckEntries));
                    duplicateCheckRepositoryPort.markDuplicateCheckVerified(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
                } else {
                    duplicatecheckMailService.sendErrorMail(customer.get(), duplicateCheckContent, errors, WorkerUtil.markExcelFile(duplicateCheckContent.getContent(), errors));
                    duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), "Verification failed");
                }
            } else {
                log.error("Customer not found for customer id={}", duplicateCheckContent.getCustomerId());
            }
        } catch (Exception ex) {
            log.error("ERROR=[" + ex.getMessage() + "]");
            duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), ex.getMessage());
        }
    }

    private List<DuplicateCheckEntry> verifyAndMapEntries(List<LexwareEntry> lexwareEntries, Map<LexwareColumn, Integer> indexMap, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        for (int i = 0; i < lexwareEntries.size(); i++) {
            LexwareEntry lexwareEntry = lexwareEntries.get(i);
            if (!(lexwareEntry.getAccountName() == null || lexwareEntry.getAccountName().isBlank())) {
                DuplicateCheckEntry duplicateCheckEntry = new DuplicateCheckEntry(lexwareEntry.getAccountName(),
                        lexwareEntry.getAddress().getPostcalCode(),
                        lexwareEntry.getAddress().getStreet(),
                        lexwareEntry.getAddress().getCity(),
                        lexwareEntry.getAddress().getCountry(),
                        !lexwareEntry.getEmailAddressData().isEmpty() ? lexwareEntry.getEmailAddressData().get(0).getEmailAddress() : "",
                        !lexwareEntry.getPhoneNumberData().isEmpty() ? lexwareEntry.getPhoneNumberData().get(0).getPhoneNumber() : "");
                duplicateCheckEntries.add(duplicateCheckEntry);
            } else {
                String msg = String.format("[Account] Zeile %d: Firmenname ist leer", i + 1);
                errors.add(new ErrMsg(0, i, indexMap.get(LexwareColumn.FIRMENNAME), LexwareColumn.FIRMENNAME.name(), msg));
            }
        }
        return duplicateCheckEntries;
    }

}