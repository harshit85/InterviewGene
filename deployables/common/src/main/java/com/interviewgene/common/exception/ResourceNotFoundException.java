package com.interviewgene.common.exception;

import java.util.Map;

/**
 * Exception thrown for missing resources
 */
public class ResourceNotFoundException extends InterviewPlatformException {
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, "RESOURCE_NOT_FOUND", cause);
    }

    public ResourceNotFoundException(String message, Map<String, Object> context) {
        super(message, "RESOURCE_NOT_FOUND", context);
    }

    public ResourceNotFoundException(String message, Map<String, Object> context, Throwable cause) {
        super(message, "RESOURCE_NOT_FOUND", context, cause);
    }
}