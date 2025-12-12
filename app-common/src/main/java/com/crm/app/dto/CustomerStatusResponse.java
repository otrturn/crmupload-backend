package com.crm.app.dto;

public record CustomerStatusResponse(Boolean enabled, Boolean hasOpenCrmUploads, Boolean hasOpenDuplicateChecks) {
}