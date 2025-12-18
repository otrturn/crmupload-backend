package com.crm.app.dto;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

@Getter
@Setter
public class InvoiceRecord {
    private CustomerBillingData customerBillingData;
    private Customer customer;
    private long invoiceNo;
    private Instant billingdate;
    private String invoiceNoText;
    private BigDecimal taxValue;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal amount;
    private byte[] invoiceImage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceRecord other)) return false;
        return java.util.Objects.equals(invoiceNoText, other.invoiceNoText)
                && Arrays.equals(invoiceImage, other.invoiceImage);
    }

    @Override
    public int hashCode() {
        int result = java.util.Objects.hash(invoiceNoText);
        result = 31 * result + Arrays.hashCode(invoiceImage);
        return result;
    }

    public @NotNull String toString() {
        return "InvoiceRecord[" +
                "invoiceNo=" + invoiceNoText +
                ", invoiceImage=" + (invoiceImage == null ? "null" : ("byte[" + invoiceImage.length + "]")) +
                ']';
    }
}
