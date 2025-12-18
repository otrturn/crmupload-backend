package com.crm.app.dto;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record InvoiceRecord(
        Long invoiceNo,
        byte[] invoiceImage
) {

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
                "invoiceNo=" + invoiceNo +
                ", invoiceImage=" + (invoiceImage == null ? "null" : ("byte[" + invoiceImage.length + "]")) +
                ']';
    }
}
