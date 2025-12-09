package com.crm.app.dto;

import java.util.List;

public record LoginResponse(String token, Boolean enabled, Boolean hasOpenCrmUploads,
                            CrmUploadCoreInfo crmUploadCoreInfo, List<String> products) {
}