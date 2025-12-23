package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;

public sealed interface UpdateCustomerResult
        permits UpdateCustomerResult.Success, UpdateCustomerResult.Failure {

    record Success() implements UpdateCustomerResult {
    }

    record Failure(ApiError error) implements UpdateCustomerResult {
    }
}
