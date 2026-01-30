package com.interviewgene.dto;

import com.interviewgene.model.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for session-level event messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEventMessage {

    private UUID sessionId;
    private SessionEventType eventType;
    private SessionStatus sessionStatus;
    private UUID triggeredBy;
    private String message;
    private LocalDateTime timestamp;
    private Object eventData;
    
    public enum SessionEventType {
        SESSION_STARTED,
        SESSION_ENDED,
        SESSION_PAUSED,
        SESSION_RESUMED,
        SESSION_CANCELLED,
        PARTICIPANT_JOINED,
        PARTICIPANT_LEFT,
        INTERACTION_ADDED,
        STATUS_CHANGED
    }
}