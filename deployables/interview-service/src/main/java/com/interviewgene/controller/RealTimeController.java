package com.interviewgene.controller;

import com.interviewgene.dto.InteractionMessage;
import com.interviewgene.model.SessionInteraction;
import com.interviewgene.service.RealTimeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * WebSocket controller for handling real-time interview interactions
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RealTimeController {

    private final RealTimeHandler realTimeHandler;

    /**
     * Handle new interaction messages
     */
    @MessageMapping("/session/{sessionId}/interaction")
    public void handleInteraction(@DestinationVariable UUID sessionId, 
                                 @Payload InteractionMessage message,
                                 Principal principal) {
        log.info("Received interaction for session: {} from user: {}", sessionId, principal.getName());

        // Set session ID and timestamp
        message.setSessionId(sessionId);
        message.setTimestamp(LocalDateTime.now());
        
        // Process the interaction
        SessionInteraction savedInteraction = realTimeHandler.processInteraction(message);
        
        log.debug("Processed interaction: {}", savedInteraction.getInteractionId());
    }

    /**
     * Handle typing indicators
     */
    @MessageMapping("/session/{sessionId}/typing")
    public void handleTyping(@DestinationVariable UUID sessionId,
                           @Payload TypingIndicator typingIndicator,
                           Principal principal) {
        log.debug("Received typing indicator for session: {} from user: {}", sessionId, principal.getName());

        UUID participantId = UUID.fromString(principal.getName());
        realTimeHandler.handleTypingIndicator(sessionId, participantId, typingIndicator.isTyping());
    }

    /**
     * Handle participant joining session
     */
    @MessageMapping("/session/{sessionId}/join")
    public void handleJoinSession(@DestinationVariable UUID sessionId, Principal principal) {
        log.info("User {} joining session: {}", principal.getName(), sessionId);

        UUID participantId = UUID.fromString(principal.getName());
        realTimeHandler.handleParticipantJoined(sessionId, participantId);
    }

    /**
     * Handle participant leaving session
     */
    @MessageMapping("/session/{sessionId}/leave")
    public void handleLeaveSession(@DestinationVariable UUID sessionId, Principal principal) {
        log.info("User {} leaving session: {}", principal.getName(), sessionId);

        UUID participantId = UUID.fromString(principal.getName());
        realTimeHandler.handleParticipantLeft(sessionId, participantId);
    }

    /**
     * Subscribe to session interactions - returns existing interactions
     */
    @SubscribeMapping("/topic/session/{sessionId}/interactions")
    public List<InteractionMessage> subscribeToSessionInteractions(@DestinationVariable UUID sessionId) {
        log.info("Client subscribing to interactions for session: {}", sessionId);
        
        return realTimeHandler.getSessionInteractions(sessionId);
    }

    /**
     * DTO for typing indicators
     */
    public static class TypingIndicator {
        private boolean typing;
        private String content;

        public TypingIndicator() {}

        public TypingIndicator(boolean typing, String content) {
            this.typing = typing;
            this.content = content;
        }

        public boolean isTyping() {
            return typing;
        }

        public void setTyping(boolean typing) {
            this.typing = typing;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}