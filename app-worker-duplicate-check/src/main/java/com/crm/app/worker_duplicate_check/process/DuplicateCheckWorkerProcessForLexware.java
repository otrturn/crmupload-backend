package com.crm.app.worker_duplicate_check.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import com.crm.app.worker_duplicate_check.config.DuplicateCheckProperties;
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
    private final DuplicateCheckProperties properties;
    private final CustomerRepositoryPort customerRepositoryPort;

    private final LexwareCtx lexwareCtx;

    public void processDuplicateCheck(DuplicateCheckContent duplicateCheckContent) {
        log.info("Processing crm_upload for Lexware duplicateCheckId={} sourceSysten={}", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem());
        try {
            List<ErrMsg> errors = new ArrayList<>();

            List<LexwareEntry> lexwareEntries = new ArrayList<>();
            Map<LexwareColumn, Integer> indexMap = new ReadLexwareExcel().getEntries(duplicateCheckContent.getContent(), lexwareEntries, errors);
            log.info(String.format("Lexware %d entries read, %d errors", lexwareEntries.size(), errors.size()));

            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                if (!ErrMsg.containsErrors(errors)) {
                    duplicateCheckRepositoryPort.markDuplicateCheckVerified(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
                }
            } else {
                log.error("Customer not found for customer id={}", duplicateCheckContent.getCustomerId());
            }
        } catch (Exception ex) {
            duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), ex.getMessage());
        }
    }
}