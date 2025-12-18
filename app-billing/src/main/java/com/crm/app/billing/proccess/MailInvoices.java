package com.crm.app.billing.proccess;

import com.crm.app.billing.config.AppBillingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailInvoices {
    private final AppBillingConfig appBillingConfig;

    public void mailInvoices() {
        log.info("Mail invoices ...");
    }
}
