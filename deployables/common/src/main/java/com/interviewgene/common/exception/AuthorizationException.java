package com.interviewgene.common.exception;

import java.util.Map;

/**
 * Exception thrown for access control violations
 */
public class AuthorizationException extends InterviewPlatformException {
    
    public AuthorizationException(String message) {
        super(message, "AUTHORIZATION_FAILED");
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, "AUTHORIZATION_FAILED", cause);
    }

    public AuthorizationException(String message, Map<String, Object> context) {
        super(message, "AUTHORIZATION_FAILED", context);
    }

    public AuthorizationException(String message, Map<String, Object> context, Throwable cause) {
        super(message, "AUTHORIZATION_FAILED", context, cause);
    }
}