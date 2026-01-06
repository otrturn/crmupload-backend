package com.crm.app.port.customer;

import com.crm.app.dto.CustomerInvoiceProductData;
import com.crm.app.dto.InvoiceRecord;

import java.util.List;

public interface BillingRepositoryPort {

    List<CustomerInvoiceProductData> getCustomersWithActiveProducts();

    long nextInvoiceId();

    void insertInvoiceRecord(InvoiceRecord invoiceRecord);

    List<InvoiceRecord> findInvoicesToBeMailed();

    void setInvoiceToMailed(long invoiceId);
}