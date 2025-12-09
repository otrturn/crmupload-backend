package com.crm.app.dto;

import java.sql.Timestamp;

public record CrmUploadHistory(Timestamp ts, String sourceSystem, String crmSystem, String crmUrl, String crmCustomerId,
                               String status) {
}