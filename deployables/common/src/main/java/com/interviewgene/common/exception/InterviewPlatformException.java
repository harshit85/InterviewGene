package com.interviewgene.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all Interview Platform microservices
 */
@Getter
public abstract class InterviewPlatformException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> context;

    protected InterviewPlatformException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    protected InterviewPlatformException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    protected InterviewPlatformException(String message, String errorCode, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
    }

    protected InterviewPlatformException(String message, String errorCode, Map<String, Object> context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
    }

    public void addContext(String key, Object value) {
        this.context.put(key, value);
    }
}