package com.crm.app.dto;

import java.sql.Timestamp;

public record ConsumerUploadHistory(Timestamp ts, String sourceSystem, String crmSystem, String status) {
}