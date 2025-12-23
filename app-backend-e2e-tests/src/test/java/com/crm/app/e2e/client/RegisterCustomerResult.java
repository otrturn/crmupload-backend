package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.RegisterResponse;

public sealed interface RegisterCustomerResult
        permits RegisterCustomerResult.Success, RegisterCustomerResult.Failure {

    record Success(RegisterResponse response) implements RegisterCustomerResult {
    }

    record Failure(ApiError error) implements RegisterCustomerResult {
    }
}
