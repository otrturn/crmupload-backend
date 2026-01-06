package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import com.crm.app.dto.InvoiceRecord;
import com.crm.app.port.customer.BillingRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailInvoices {
    private final AppBillingConfig appBillingConfig;
    private final BillingRepositoryPort billingRepositoryPort;
    private static final String DURATION_FORMAT_STRING = "Duration: %02d:%02d:%02d";

    public void mailInvoices() {
        log.info("Mail invoices ...");
        try {
            Instant start = Instant.now();
            List<InvoiceRecord> invoiceRecords = billingRepositoryPort.findInvoicesToBeMailed();
            log.info(String.format("%d invoices found", invoiceRecords.size()));
            Duration duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
            for (InvoiceRecord invoiceRecord : invoiceRecords) {
                billingRepositoryPort.setInvoiceToMailed(invoiceRecord.getInvoiceId());
            }
            log.info(String.format("%d invoices mailed", invoiceRecords.size()));
            duration = Duration.between(start, Instant.now());
            log.info(String.format(DURATION_FORMAT_STRING, duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));

        } catch (Exception e) {
            log.error("mailInvoices failed", e);
        }
    }
}
