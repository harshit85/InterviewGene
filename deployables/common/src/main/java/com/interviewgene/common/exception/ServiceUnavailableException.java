package com.interviewgene.common.exception;

import java.util.Map;

/**
 * Exception thrown when a service is unavailable (circuit breaker scenarios)
 */
public class ServiceUnavailableException extends InterviewPlatformException {
    
    public ServiceUnavailableException(String message) {
        super(message, "SERVICE_UNAVAILABLE");
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, "SERVICE_UNAVAILABLE", cause);
    }

    public ServiceUnavailableException(String message, Map<String, Object> context) {
        super(message, "SERVICE_UNAVAILABLE", context);
    }

    public ServiceUnavailableException(String message, Map<String, Object> context, Throwable cause) {
        super(message, "SERVICE_UNAVAILABLE", context, cause);
    }
}