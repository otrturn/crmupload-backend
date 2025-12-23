package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;

public sealed interface UpdatePasswordResult
        permits UpdatePasswordResult.Success, UpdatePasswordResult.Failure {

    record Success() implements UpdatePasswordResult {
    }

    record Failure(ApiError error) implements UpdatePasswordResult {
    }
}
