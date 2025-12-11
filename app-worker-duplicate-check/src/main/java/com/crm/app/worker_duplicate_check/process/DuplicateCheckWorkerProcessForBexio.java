package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.dto.DuplicateCheckEntry;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check.config.DuplicateCheckProperties;
import com.crmmacher.bexio_excel.dto.BexioColumn;
import com.crmmacher.bexio_excel.dto.BexioEntry;
import com.crmmacher.bexio_excel.reader.ReadBexioExcel;
import com.crmmacher.error.ErrMsg;
import com.crmmacher.espo.importer.bexio_excel.config.BexioCtx;
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
public class DuplicateCheckWorkerProcessForBexio {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final DuplicateCheckProperties properties;
    private final CustomerRepositoryPort customerRepositoryPort;

    private final BexioCtx bexioCtx;

    public void processDuplicateCheck(DuplicateCheckContent duplicateCheckContent) {
        log.info("Processing crm_upload for Bexio duplicateCheckId={} sourceSysten={}", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem());
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<BexioEntry> bexioEntries = new ArrayList<>();
            Map<BexioColumn, Integer> indexMap = new ReadBexioExcel().getEntries(duplicateCheckContent.getContent(), bexioEntries, errors);
            log.info(String.format("Bexio %d entries read, %d errors", bexioEntries.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                List<DuplicateCheckEntry> duplicateCheckEntries = verifyAndMapEntries(bexioEntries, indexMap, errors);
                log.info(String.format("processDuplicateCheck: %d entries mapped, now %d errors", duplicateCheckEntries.size(), errors.size()));
                if (!ErrMsg.containsErrors(errors)) {
                    duplicateCheckRepositoryPort.markDuplicateCheckVerified(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
                } else {
                    duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), "Verification failed");
                }
            } else {
                log.error("Customer not found for customer id={}", duplicateCheckContent.getCustomerId());
            }
        } catch (Exception ex) {
            duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), ex.getMessage());
        }
    }

    private List<DuplicateCheckEntry> verifyAndMapEntries(List<BexioEntry> bexioEntries, Map<BexioColumn, Integer> indexMap, List<ErrMsg> errors) {
        List<DuplicateCheckEntry> duplicateCheckEntries = new ArrayList<>();
        for (int i = 0; i < bexioEntries.size(); i++) {
            BexioEntry bexioEntry = bexioEntries.get(i);
            if (bexioEntry.getAccountName() == null || bexioEntry.getAccountName().isBlank()) {
                DuplicateCheckEntry duplicateCheckEntry = new DuplicateCheckEntry(bexioEntry.getAccountName(),
                        bexioEntry.getAddress().getPostcalCode(),
                        bexioEntry.getAddress().getStreet(),
                        bexioEntry.getAddress().getCity(),
                        bexioEntry.getAddress().getCountry(),
                        !bexioEntry.getEmailAddressData().isEmpty() ? bexioEntry.getEmailAddressData().get(0).getEmailAddress() : "",
                        !bexioEntry.getPhoneNumberData().isEmpty() ? bexioEntry.getPhoneNumberData().get(0).getPhoneNumber() : "");
                duplicateCheckEntries.add(duplicateCheckEntry);
            } else {
                String msg = String.format("[Account] Zeile %d: Firmenname ist leer", i + 1);
                errors.add(new ErrMsg(0, i, indexMap.get(BexioColumn.FIRMENNAME), BexioColumn.FIRMENNAME.name(), msg));
            }
        }
        return duplicateCheckEntries;
    }

}