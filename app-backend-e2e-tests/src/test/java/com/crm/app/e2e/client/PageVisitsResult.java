package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;

public sealed interface PageVisitsResult
        permits PageVisitsResult.Success, PageVisitsResult.Failure {

    record Success() implements PageVisitsResult {
    }

    record Failure(ApiError error) implements PageVisitsResult {
    }
}