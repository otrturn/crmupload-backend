package com.crm.app.port.customer;

import com.crm.app.dto.CustomerInvoiceData;
import com.crm.app.dto.InvoiceRecord;

import java.util.List;
import java.util.Optional;

public interface BillingRepositoryPort {

    Optional<CustomerInvoiceData> getCustomerProductsByCustomerId(long customerId);

    List<CustomerInvoiceData> getCustomersWithProducts();

    long nextInvoiceId();

    void insertInvoiceRecord(InvoiceRecord invoiceRecord);
}