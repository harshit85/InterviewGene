package com.interviewgene.common.exception;

import java.util.Map;

/**
 * Exception thrown for AI API and external service failures
 */
public class ExternalServiceException extends InterviewPlatformException {
    
    public ExternalServiceException(String message) {
        super(message, "EXTERNAL_SERVICE_ERROR");
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, "EXTERNAL_SERVICE_ERROR", cause);
    }

    public ExternalServiceException(String message, Map<String, Object> context) {
        super(message, "EXTERNAL_SERVICE_ERROR", context);
    }

    public ExternalServiceException(String message, Map<String, Object> context, Throwable cause) {
        super(message, "EXTERNAL_SERVICE_ERROR", context, cause);
    }
}