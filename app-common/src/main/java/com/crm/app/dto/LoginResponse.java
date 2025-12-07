package com.crm.app.dto;

public record LoginResponse(String token, Boolean enabled, Boolean hasOpenUploads,
                            ConsumerUploadInfo consumerUploadInfo) {
}