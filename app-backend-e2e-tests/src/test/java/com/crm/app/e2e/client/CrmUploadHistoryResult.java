package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.CrmUploadHistoryResponse;

public sealed interface CrmUploadHistoryResult
        permits CrmUploadHistoryResult.Success, CrmUploadHistoryResult.Failure {

    record Success(CrmUploadHistoryResponse response) implements CrmUploadHistoryResult {
    }

    record Failure(ApiError error) implements CrmUploadHistoryResult {
    }
}
