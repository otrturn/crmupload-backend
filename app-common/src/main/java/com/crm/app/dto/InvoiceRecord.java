package com.crm.app.dto;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

@Getter
@Setter
public class InvoiceRecord {
    private CustomerInvoiceProductData customerInvoiceProductData;
    private Customer customer;
    private long invoiceId;
    private Timestamp invoiceDate;
    private Timestamp invoiceDueDate;
    private String invoiceNo;
    private BigDecimal taxValue;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal amount;
    private byte[] invoiceImage;
    private String invoicePdfName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceRecord other)) return false;
        return java.util.Objects.equals(invoiceNo, other.invoiceNo)
                && Arrays.equals(invoiceImage, other.invoiceImage);
    }

    @Override
    public int hashCode() {
        int result = java.util.Objects.hash(invoiceNo);
        result = 31 * result + Arrays.hashCode(invoiceImage);
        return result;
    }

    public @NotNull String toString() {
        return "InvoiceRecord[" +
                "invoiceId=" + invoiceId +
                ", invoiceImage=" + (invoiceImage == null ? "null" : ("byte[" + invoiceImage.length + "]")) +
                ']';
    }
}
