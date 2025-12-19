package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import com.crm.app.billing.util.BillingRules;
import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerInvoiceData;
import com.crm.app.dto.CustomerProduct;
import com.crm.app.dto.InvoiceRecord;
import com.crm.app.port.customer.BillingRepositoryPort;
import com.crm.app.port.customer.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.crm.app.util.IdentityNumberCreator.createInvoiceNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateInvoices {
    private final AppBillingConfig appBillingConfig;
    private final BillingRepositoryPort billingRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final GeneratePdfWithHtmlTemplate generatePdfWithHtmlTemplate;

    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    public void generateInvoices() {
        log.info("Generate invoices ...");
        try {
            Instant start = Instant.now();
            List<CustomerInvoiceData> customerInvoiceDataList = billingRepositoryPort.getCustomersWithProducts();
            log.info(String.format("%d costumers found", customerInvoiceDataList.size()));
            Duration duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

            start = Instant.now();
            for (CustomerInvoiceData customerInvoiceData : customerInvoiceDataList) {
                Optional<Customer> customer = customerRepositoryPort.findCustomerByCustomerId(customerInvoiceData.customerId());
                if (customer.isPresent()) {
                    long invoiceId = billingRepositoryPort.nextInvoiceId();

                    InvoiceRecord invoiceRecord = new InvoiceRecord();
                    invoiceRecord.setCustomerInvoiceData(customerInvoiceData);
                    invoiceRecord.setCustomer(customer.get());
                    invoiceRecord.setInvoiceId(invoiceId);
                    invoiceRecord.setInvoiceDate(Timestamp.from(Instant.now()));
                    invoiceRecord.setInvoiceDueDate(Timestamp.from(
                            invoiceRecord.getInvoiceDate().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .plusDays(10)
                                    .toInstant()));
                    invoiceRecord.setInvoiceNo(createInvoiceNumber(invoiceId));
                    setItemPrices(invoiceRecord);
                    byte[] invoiceImage = generatePdfWithHtmlTemplate.generatePDFForCustomer(invoiceRecord);
                    invoiceRecord.setInvoiceImage(invoiceImage);

                    billingRepositoryPort.insertInvoiceRecord(invoiceRecord);
                } else {
                    log.error(String.format("Customer not found for customerId=%d", customerInvoiceData.customerId()));
                }
            }
            log.info(String.format("%d invoices generated", customerInvoiceDataList.size()));
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        } catch (Exception e) {
            log.error("generateInvoices failed", e);
        }
    }

    private void setItemPrices(InvoiceRecord invoiceRecord) {

        List<CustomerProduct> products = invoiceRecord.getCustomerInvoiceData().products();
        BigDecimal price = products.size() == 1 ? BigDecimal.valueOf(250) : BigDecimal.valueOf(200);
        BigDecimal taxValue = new BigDecimal("0.19");

        products.forEach(p -> {
            p.setNetAmount(BillingRules.roundMoney(price));
            p.setTaxValue(taxValue);

            BigDecimal taxAmount = BillingRules.roundMoney(
                    p.getNetAmount().multiply(p.getTaxValue())
            );
            p.setTaxAmount(taxAmount);

            BigDecimal grossAmount = BillingRules.roundMoney(
                    p.getNetAmount().add(p.getTaxAmount())
            );
            p.setAmount(grossAmount);
        });

        invoiceRecord.setTaxValue(taxValue);

        invoiceRecord.setTaxAmount(
                BillingRules.roundMoney(
                        products.stream()
                                .map(CustomerProduct::getTaxAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
        );

        invoiceRecord.setNetAmount(
                BillingRules.roundMoney(
                        products.stream()
                                .map(CustomerProduct::getNetAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
        );

        invoiceRecord.setAmount(
                BillingRules.roundMoney(
                        products.stream()
                                .map(CustomerProduct::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
        );
    }
}
