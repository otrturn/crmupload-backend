package com.crm.app.dto;

import java.util.List;

public record LoginResponse(String token, Boolean enabled, Boolean hasOpenCrmUploads, Boolean hasOpenDuplicateChecks,
                            CrmUploadCoreInfo crmUploadCoreInfo, List<CustomerProduct> products) {
}