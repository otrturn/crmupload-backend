package com.crm.app.dto;

import java.util.List;

public record CustomerBillingData(
        Long customerId,
        List<CustomerProduct> products
) {
}

