package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerBillingData;
import com.crm.app.dto.InvoiceRecord;
import com.crm.app.port.customer.BillingRepositoryPort;
import com.crm.app.port.customer.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateInvoices {
    private final AppBillingConfig appBillingConfig;
    private final BillingRepositoryPort billingRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;

    private static final String LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID = "No customer found for customerId '%s'";

    public void generateInvoices() {
        log.info("Generate invoices ...");
        try {
            List<CustomerBillingData> customerBillingDataList = billingRepositoryPort.getCustomersWithProducts();
            log.info(String.format("%d costumers found", customerBillingDataList.size()));
            for (CustomerBillingData customerBillingData : customerBillingDataList) {
                Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(customerBillingData.customerId());
                if (customer.isPresent()) {
                    byte[] invoiceImage = new byte[10];
                    long invoiceNo = billingRepositoryPort.nextInvoiceNo();
                    InvoiceRecord invoiceRecord = new InvoiceRecord();
                    invoiceRecord.setInvoiceNo(String.format("%03d.%03d", invoiceNo / 1000, invoiceNo % 1000));
                    invoiceRecord.setInvoiceImage(invoiceImage);
                    billingRepositoryPort.insertInvoiceRecord(customerBillingData, customer.get(), invoiceRecord);
                } else {
                    log.error(String.format("Customer not found for customerId=%d", customerBillingData.customerId()));
                }
            }
        } catch (Exception e) {
            log.error("generateInvoices failed", e);
        }
    }
}
