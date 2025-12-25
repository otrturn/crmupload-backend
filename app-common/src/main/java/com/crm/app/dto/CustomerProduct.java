package com.crm.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static com.crm.app.dto.AppConstants.PRODUCT_CRM_UPLOAD;
import static com.crm.app.dto.AppConstants.PRODUCT_DUPLICATE_CHECK;

@Getter
@Setter
public class CustomerProduct {
    private String product;
    boolean enabled;
    private Timestamp activationDate;
    private BigDecimal taxValue;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal amount;

    public CustomerProduct(String product, boolean enabled, Timestamp activationDate) {
        this.product = product;
        this.enabled = enabled;
        this.activationDate = activationDate;
    }

    public static String getProductTranslated(String product) {
        if (product == null) {
            return "";
        }
        if (product.equalsIgnoreCase(PRODUCT_CRM_UPLOAD)) {
            return "CRM-Upload – Import von Kundendaten in das CRM-System";
        } else if (product.equalsIgnoreCase(PRODUCT_DUPLICATE_CHECK)) {
            return "CRM-Dublettenprüfung – Dublettenanalyse von Kundendaten";
        }
        return "";
    }
}

