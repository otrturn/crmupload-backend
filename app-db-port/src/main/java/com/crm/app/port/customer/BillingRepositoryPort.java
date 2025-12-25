package com.crm.app.port.customer;

import com.crm.app.dto.CustomerInvoiceData;
import com.crm.app.dto.InvoiceRecord;

import java.util.List;

public interface BillingRepositoryPort {

    List<CustomerInvoiceData> getCustomersWithActiveProducts();

    long nextInvoiceId();

    void insertInvoiceRecord(InvoiceRecord invoiceRecord);
}