package com.crm.app.dto;

import java.sql.Timestamp;

public record CustomerUploadHistory(Timestamp ts, String sourceSystem, String crmSystem, String crmCustomerId,
                                    String status) {
}