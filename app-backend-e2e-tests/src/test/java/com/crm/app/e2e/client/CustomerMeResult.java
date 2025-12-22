package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.CustomerProfile;

public sealed interface CustomerMeResult
        permits CustomerMeResult.Success, CustomerMeResult.Failure {

    record Success(CustomerProfile response) implements CustomerMeResult {
    }

    record Failure(ApiError error) implements CustomerMeResult {
    }
}