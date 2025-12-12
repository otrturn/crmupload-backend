package com.crm.app.worker_duplicate_check_gpu.process;

import com.crm.app.dto.DuplicateCheckContent;
import com.crm.app.port.customer.Customer;
import com.crm.app.port.customer.CustomerRepositoryPort;
import com.crm.app.port.customer.DuplicateCheckRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCheckGpuWorkerProcessForCheck {

    private final DuplicateCheckRepositoryPort duplicateCheckRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;

    public void processDuplicateCheckForCheck(DuplicateCheckContent duplicateCheckContent) {
        log.info("processDuplicateCheckForCheck for {} {}", duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getSourceSystem());
        try {
            Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(duplicateCheckContent.getCustomerId());
            if (customer.isPresent()) {
                duplicateCheckRepositoryPort.markDuplicateCheckChecked(duplicateCheckContent.getDuplicateCheckId(), duplicateCheckContent.getContent());
            } else {
                log.error("Customer not found for customer id={}", duplicateCheckContent.getCustomerId());
            }
        } catch (Exception ex) {
            duplicateCheckRepositoryPort.markDuplicateCheckFailed(duplicateCheckContent.getDuplicateCheckId(), ex.getMessage());
        }
    }

}