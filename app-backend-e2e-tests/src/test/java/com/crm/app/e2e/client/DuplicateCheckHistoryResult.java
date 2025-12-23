package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.DuplicateCheckHistoryResponse;

public sealed interface DuplicateCheckHistoryResult
        permits DuplicateCheckHistoryResult.Success, DuplicateCheckHistoryResult.Failure {

    record Success(DuplicateCheckHistoryResponse response) implements DuplicateCheckHistoryResult {}

    record Failure(ApiError error) implements DuplicateCheckHistoryResult {}
}
