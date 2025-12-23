package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import org.springframework.http.HttpHeaders;

public sealed interface DuplicateCheckDownloadExcelResult
        permits DuplicateCheckDownloadExcelResult.Success, DuplicateCheckDownloadExcelResult.Failure {

    record Success(byte[] fileContent, HttpHeaders headers) implements DuplicateCheckDownloadExcelResult {
        public String contentDisposition() {
            return headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        }
    }

    record Failure(ApiError error) implements DuplicateCheckDownloadExcelResult {
    }
}
