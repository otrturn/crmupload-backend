package com.crm.app.dto;

import java.util.List;

public record CustomerInvoiceData(
        Long customerId,
        List<CustomerProduct> products
) {
}

