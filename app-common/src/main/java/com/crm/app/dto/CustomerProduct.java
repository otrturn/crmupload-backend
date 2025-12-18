package com.crm.app.dto;

import java.sql.Timestamp;

public record CustomerProduct(
        String product,
        Timestamp activationDate) {
}

