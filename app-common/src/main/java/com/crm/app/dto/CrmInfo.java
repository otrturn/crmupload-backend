package com.crm.app.dto;

public record CrmInfo(
        String sourceSystem,
        String crmSystem,
        String crmUrl,
        String crmCustomerId
) {
}