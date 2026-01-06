package com.crm.app.dto;

import java.util.List;

public record CustomerInvoiceProductData(
        Long customerId,
        List<CustomerProduct> products
) {
}

