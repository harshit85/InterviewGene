package com.interviewgene.dto;

import com.interviewgene.model.InteractionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for real-time interaction messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionMessage {

    private UUID sessionId;
    private InteractionType type;
    private String content;
    private UUID participantId;
    private String participantName;
    private LocalDateTime timestamp;
    private Integer sequenceNumber;
    private Integer durationSeconds;
    private Map<String, String> metadata;
    
    // Message routing information
    private String messageId;
    private String correlationId;
    private MessageAction action;
    
    public enum MessageAction {
        CREATE,
        UPDATE,
        DELETE,
        TYPING_START,
        TYPING_STOP,
        PARTICIPANT_JOINED,
        PARTICIPANT_LEFT,
        SESSION_STATE_CHANGED
    }
}