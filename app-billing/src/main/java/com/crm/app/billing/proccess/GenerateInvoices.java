package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import com.crm.app.dto.CustomerBillingData;
import com.crm.app.port.customer.BillingRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateInvoices {
    private final AppBillingConfig appBillingConfig;
    private final BillingRepositoryPort billingRepositoryPort;

    public void generateInvoices() {
        log.info("Generate invoices ...");
        try {
            List<CustomerBillingData> customerBillingData = billingRepositoryPort.getCustomersWithProducts();
            log.info(String.format("%d costumers found", customerBillingData.size()));
        } catch (Exception e) {
            log.error("Billing failed", e);
        }
    }
}
