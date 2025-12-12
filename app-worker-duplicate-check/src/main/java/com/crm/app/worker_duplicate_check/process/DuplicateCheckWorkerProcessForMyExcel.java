package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_common.util.WorkerUtil;
import com.crm.app.worker_duplicate_check.mail.DuplicatecheckMailService;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.importer.my_excel.config.MyExcelCtx;
import com.crmmacher.my_excel.dto.MyExcelAccount;
import com.crmmacher.my_excel.reader.MyExcelReadAccounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckWorkerProcessForMyExcel {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final DuplicatecheckMailService duplicatecheckMailService;

    private final MyExcelCtx myExcelCtx;

    public void processDuplicateCheckForVerification(DuplicateCheckContent duplicateCheckContent) {
        log.info("Processing crm_upload for MyExcel duplicateCheckId={} sourceSysten={}", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem());
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<MyExcelAccount> myExcelAccounts = new MyExcelReadAccounts().getAccounts(duplicateCheckContent.getContent(), errors);

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntries(myExcelAccounts, errors);
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

    private List<DuplicateCheckEntry> verifyAndMapEntries(List<MyExcelAccount> myExcelEntries, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        for (int i = 0; i < myExcelEntries.size(); i++) {
            MyExcelAccount myExcelEntry = myExcelEntries.get(i);
            if (!(myExcelEntry.getName() == null || myExcelEntry.getName().isBlank())) {
                DuplicateCheckEntry duplicateCheckEntry = new DuplicateCheckEntry(myExcelEntry.getName(),
                        myExcelEntry.getBillingAddress().getPostcalCode(),
                        myExcelEntry.getBillingAddress().getStreet(),
                        myExcelEntry.getBillingAddress().getCity(),
                        myExcelEntry.getBillingAddress().getCountry(),
                        !myExcelEntry.getEmailAddressData().isEmpty() ? myExcelEntry.getEmailAddressData().get(0).getEmailAddress() : "",
                        !myExcelEntry.getPhoneNumberData().isEmpty() ? myExcelEntry.getPhoneNumberData().get(0).getPhoneNumber() : "");
                duplicateCheckEntries.add(duplicateCheckEntry);
            } else {
                String msg = String.format("[Account] Zeile %d: Firmenname ist leer", i + 1);
                errors.add(new ErrMsg(0, i, 0, "Firmenname", msg));
            }
        }
        return duplicateCheckEntries;
    }

}