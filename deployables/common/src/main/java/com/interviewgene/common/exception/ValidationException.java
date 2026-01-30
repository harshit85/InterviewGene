package com.interviewgene.common.exception;

import java.util.Map;

/**
 * Exception thrown for input validation failures
 */
public class ValidationException extends InterviewPlatformException {
    
    public ValidationException(String message) {
        super(message, "VALIDATION_FAILED");
    }

    public ValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_FAILED", cause);
    }

    public ValidationException(String message, Map<String, Object> context) {
        super(message, "VALIDATION_FAILED", context);
    }

    public ValidationException(String message, Map<String, Object> context, Throwable cause) {
        super(message, "VALIDATION_FAILED", context, cause);
    }
}