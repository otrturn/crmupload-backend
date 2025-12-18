package com.crm.app.port.customer;

import com.crm.app.dto.Customer;
import com.crm.app.dto.CustomerBillingData;
import com.crm.app.dto.InvoiceRecord;

import java.util.List;
import java.util.Optional;

public interface BillingRepositoryPort {

    Optional<CustomerBillingData> getCustomerProductsByCustomerId(long customerId);

    List<CustomerBillingData> getCustomersWithProducts();

    long nextInvoiceNo();

    void insertInvoiceRecord(CustomerBillingData customerBillingData, Customer customer, InvoiceRecord invoiceRecord);
}