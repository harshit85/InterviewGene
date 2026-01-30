package com.interviewgene.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced API error response format for all microservices
 */
@Data
@Builder
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    private String requestId;
    private Map<String, Object> details;

    public static ApiError.ApiErrorBuilder builder() {
        return new ApiError.ApiErrorBuilder()
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString());
    }
}