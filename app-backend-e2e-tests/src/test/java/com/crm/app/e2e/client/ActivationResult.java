package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;

public sealed interface ActivationResult
        permits ActivationResult.Success, ActivationResult.Failure {

    record Success(String response) implements ActivationResult {
    }

    record Failure(ApiError error) implements ActivationResult {
    }
}
