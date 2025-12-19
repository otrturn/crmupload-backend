package com.crm.app.util;

import java.time.Year;
import java.time.ZoneId;

public class IdentityNumberCreator {
    private IdentityNumberCreator() {
    }

    public static String createCustomerNumber(long customerId) {
        return String.format("KD-%04d-%06d", Year.now(ZoneId.of("Europe/Berlin")).getValue(), customerId);
    }

    public static String createInvoiceNumber(long invoiceNo) {
        return String.format("RG-%04d-%06d", Year.now(ZoneId.of("Europe/Berlin")).getValue(), invoiceNo);
    }
}
