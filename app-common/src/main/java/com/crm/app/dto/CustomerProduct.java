package com.crm.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
public class CustomerProduct {
    private String product;
    private Timestamp activationDate;
    private BigDecimal taxValue;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal amount;

    public CustomerProduct(String product, Timestamp activationDate) {
        this.product = product;
        this.activationDate = activationDate;
    }
}

