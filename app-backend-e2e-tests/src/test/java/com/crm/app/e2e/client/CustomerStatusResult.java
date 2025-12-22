package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.CustomerStatusResponse;

public sealed interface CustomerStatusResult
        permits CustomerStatusResult.Success, CustomerStatusResult.Failure {

    record Success(CustomerStatusResponse response) implements CustomerStatusResult {
    }

    record Failure(ApiError error) implements CustomerStatusResult {
    }
}