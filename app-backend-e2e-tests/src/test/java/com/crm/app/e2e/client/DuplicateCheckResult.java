package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;

public sealed interface DuplicateCheckResult
        permits DuplicateCheckResult.Success, DuplicateCheckResult.Failure {

    record Success() implements DuplicateCheckResult {
    }

    record Failure(ApiError error) implements DuplicateCheckResult {
    }
}
