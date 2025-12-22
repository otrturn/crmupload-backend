package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;

public sealed interface CrmUploadResult
        permits CrmUploadResult.Success, CrmUploadResult.Failure {

    record Success() implements CrmUploadResult {}

    record Failure(ApiError error) implements CrmUploadResult {}
}
