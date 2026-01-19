package com.crm.app.web.error;

import com.crm.app.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;

final class ApiErrorFactory {

    private ApiErrorFactory() {
    }

    static ApiError build(HttpStatus status,
                          String message,
                          HttpServletRequest request,
                          String code) {
        return new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                Instant.now(),
                code
        );
    }
}
