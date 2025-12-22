package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.LoginResponse;

public sealed interface LoginResult
        permits LoginResult.Success, LoginResult.Failure {

    record Success(LoginResponse response) implements LoginResult {
    }

    record Failure(ApiError error) implements LoginResult {
    }
}
