package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerBillingData;
import com.crm.app.dto.CustomerProduct;
import com.crm.app.dto.InvoiceRecord;
import com.crm.app.port.customer.BillingRepositoryPort;
import com.crm.app.port.customer.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateInvoices {
    private final AppBillingConfig appBillingConfig;
    private final BillingRepositoryPort billingRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final GeneratePDF generatePDF;

    private static final String LITERAL_NO_CUSTOMER_FOR_CUSTOMER_ID = "No customer found for customerId '%s'";

    public void generateInvoices() {
        log.info("Generate invoices ...");
        try {
            List<CustomerBillingData> customerBillingDataList = billingRepositoryPort.getCustomersWithProducts();
            log.info(String.format("%d costumers found", customerBillingDataList.size()));
            for (CustomerBillingData customerBillingData : customerBillingDataList) {
                Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(customerBillingData.customerId());
                if (customer.isPresent()) {
                    long invoiceNo = billingRepositoryPort.nextInvoiceNo();

                    InvoiceRecord invoiceRecord = new InvoiceRecord();
                    invoiceRecord.setCustomerBillingData(customerBillingData);
                    invoiceRecord.setCustomer(customer.get());
                    invoiceRecord.setInvoiceNo(invoiceNo);
                    invoiceRecord.setBillingdate(Instant.now());
                    invoiceRecord.setInvoiceNoText(String.format("%03d.%03d", invoiceNo / 1000, invoiceNo % 1000));
                    setItemPrices(invoiceRecord);
                    byte[] invoiceImage = generatePDF.generatePDFForCustomer(invoiceRecord);
                    invoiceRecord.setInvoiceImage(invoiceImage);

                    billingRepositoryPort.insertInvoiceRecord(invoiceRecord);
                } else {
                    log.error(String.format("Customer not found for customerId=%d", customerBillingData.customerId()));
                }
            }
        } catch (Exception e) {
            log.error("generateInvoices failed", e);
        }
    }

    private void setItemPrices(InvoiceRecord invoiceRecord) {
        List<CustomerProduct> products = invoiceRecord.getCustomerBillingData().products();
        BigDecimal price = products.size() == 1 ? BigDecimal.valueOf(250) : BigDecimal.valueOf(200);
        BigDecimal taxValue = BigDecimal.valueOf(0.19d);
        products.forEach(p -> {
            p.setNetAmount(price);
            p.setTaxValue(taxValue);
            p.setTaxAmount(p.getNetAmount().multiply(p.getTaxValue()));
            p.setAmount(p.getNetAmount().add(p.getTaxAmount()));
        });

        invoiceRecord.setTaxValue(taxValue);
        invoiceRecord.setTaxAmount(products.stream()
                .filter(Objects::nonNull)
                .map(CustomerProduct::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        invoiceRecord.setNetAmount(products.stream()
                .filter(Objects::nonNull)
                .map(CustomerProduct::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        invoiceRecord.setAmount(products.stream()
                .filter(Objects::nonNull)
                .map(CustomerProduct::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

    }
}
