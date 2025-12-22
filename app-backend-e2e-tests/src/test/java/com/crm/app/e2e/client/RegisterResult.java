package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.RegisterResponse;

public sealed interface RegisterResult
        permits RegisterResult.Success, RegisterResult.Failure {

    record Success(RegisterResponse response) implements RegisterResult {
    }

    record Failure(ApiError error) implements RegisterResult {
    }
}
