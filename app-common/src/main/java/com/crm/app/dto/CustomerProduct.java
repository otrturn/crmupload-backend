package com.crm.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class CustomerProduct{
    private String product;
    private Timestamp activationDate ;
    private double taxValue;
    private double taxAmount;
    private double netAmount;
    private double amount;

    public CustomerProduct(String product, Timestamp activationDate) {
        this.product = product;
        this.activationDate = activationDate;
    }
}

