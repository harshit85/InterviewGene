package com.interviewgene.service;

import com.interviewgene.common.exception.ResourceNotFoundException;
import com.interviewgene.common.exception.ValidationException;
import com.interviewgene.dto.InteractionMessage;
import com.interviewgene.dto.SessionEventMessage;
import com.interviewgene.model.InterviewSession;
import com.interviewgene.model.SessionInteraction;
import com.interviewgene.repository.SessionInteractionRepository;
import com.interviewgene.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling real-time interactions during interview sessions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RealTimeHandler {

    private final SessionRepository sessionRepository;
    private final SessionInteractionRepository interactionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Process and store a new interaction, then broadcast to session participants
     */
    public SessionInteraction processInteraction(InteractionMessage message) {
        log.info("Processing interaction for session: {} from participant: {}", 
                message.getSessionId(), message.getParticipantId());

        // Validate session exists and is active
        InterviewSession session = validateSessionForInteraction(message.getSessionId());
        
        // Create and save interaction
        SessionInteraction interaction = createInteraction(session, message);
        SessionInteraction savedInteraction = interactionRepository.save(interaction);

        // Broadcast to session participants
        broadcastInteractionToSession(message.getSessionId(), mapToMessage(savedInteraction));

        log.info("Processed interaction: {} for session: {}", 
                savedInteraction.getInteractionId(), message.getSessionId());

        return savedInteraction;
    }

    /**
     * Get all interactions for a session
     */
    @Transactional(readOnly = true)
    public List<InteractionMessage> getSessionInteractions(UUID sessionId) {
        log.debug("Retrieving interactions for session: {}", sessionId);

        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        return interactionRepository.findBySessionSessionIdOrderByTimestampAsc(sessionId)
                .stream()
                .map(this::mapToMessage)
                .collect(Collectors.toList());
    }

    /**
     * Get interactions within a time range
     */
    @Transactional(readOnly = true)
    public List<InteractionMessage> getSessionInteractions(UUID sessionId, 
                                                          LocalDateTime startTime, 
                                                          LocalDateTime endTime) {
        log.debug("Retrieving interactions for session: {} between {} and {}", 
                sessionId, startTime, endTime);

        return interactionRepository.findBySessionAndTimeRange(sessionId, startTime, endTime)
                .stream()
                .map(this::mapToMessage)
                .collect(Collectors.toList());
    }

    /**
     * Broadcast session event to all participants
     */
    public void broadcastSessionEvent(UUID sessionId, SessionEventMessage eventMessage) {
        log.info("Broadcasting session event: {} for session: {}", 
                eventMessage.getEventType(), sessionId);

        // Send to session topic
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/events", eventMessage);

        // Send to individual participants if needed
        InterviewSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            messagingTemplate.convertAndSendToUser(
                    session.getInterviewerId().toString(),
                    "/queue/session-events",
                    eventMessage
            );
            messagingTemplate.convertAndSendToUser(
                    session.getCandidateId().toString(),
                    "/queue/session-events",
                    eventMessage
            );
        }
    }

    /**
     * Handle participant joining session
     */
    public void handleParticipantJoined(UUID sessionId, UUID participantId) {
        log.info("Participant {} joined session: {}", participantId, sessionId);

        SessionEventMessage eventMessage = SessionEventMessage.builder()
                .sessionId(sessionId)
                .eventType(SessionEventMessage.SessionEventType.PARTICIPANT_JOINED)
                .triggeredBy(participantId)
                .message("Participant joined the session")
                .timestamp(LocalDateTime.now())
                .build();

        broadcastSessionEvent(sessionId, eventMessage);
    }

    /**
     * Handle participant leaving session
     */
    public void handleParticipantLeft(UUID sessionId, UUID participantId) {
        log.info("Participant {} left session: {}", participantId, sessionId);

        SessionEventMessage eventMessage = SessionEventMessage.builder()
                .sessionId(sessionId)
                .eventType(SessionEventMessage.SessionEventType.PARTICIPANT_LEFT)
                .triggeredBy(participantId)
                .message("Participant left the session")
                .timestamp(LocalDateTime.now())
                .build();

        broadcastSessionEvent(sessionId, eventMessage);
    }

    /**
     * Handle typing indicators
     */
    public void handleTypingIndicator(UUID sessionId, UUID participantId, boolean isTyping) {
        log.debug("Typing indicator for session: {}, participant: {}, typing: {}", 
                sessionId, participantId, isTyping);

        InteractionMessage typingMessage = InteractionMessage.builder()
                .sessionId(sessionId)
                .participantId(participantId)
                .action(isTyping ? InteractionMessage.MessageAction.TYPING_START : 
                                 InteractionMessage.MessageAction.TYPING_STOP)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .build();

        // Broadcast typing indicator (don't store in database)
        broadcastInteractionToSession(sessionId, typingMessage);
    }

    /**
     * Delete an interaction and broadcast the change
     */
    public void deleteInteraction(UUID interactionId, UUID requestingUserId) {
        log.info("Deleting interaction: {} by user: {}", interactionId, requestingUserId);

        SessionInteraction interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interaction not found: " + interactionId));

        // Validate user can delete this interaction
        if (!interaction.getParticipantId().equals(requestingUserId)) {
            throw new ValidationException("User can only delete their own interactions");
        }

        UUID sessionId = interaction.getSession().getSessionId();
        interactionRepository.delete(interaction);

        // Broadcast deletion
        InteractionMessage deleteMessage = InteractionMessage.builder()
                .sessionId(sessionId)
                .messageId(interactionId.toString())
                .action(InteractionMessage.MessageAction.DELETE)
                .participantId(requestingUserId)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastInteractionToSession(sessionId, deleteMessage);
    }

    private InterviewSession validateSessionForInteraction(UUID sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.canJoin()) {
            throw new ValidationException("Session is not available for interactions: " + session.getStatus());
        }

        return session;
    }

    private SessionInteraction createInteraction(InterviewSession session, InteractionMessage message) {
        Integer nextSequenceNumber = interactionRepository.getNextSequenceNumber(session.getSessionId());

        return SessionInteraction.builder()
                .session(session)
                .type(message.getType())
                .content(message.getContent())
                .participantId(message.getParticipantId())
                .sequenceNumber(nextSequenceNumber)
                .durationSeconds(message.getDurationSeconds())
                .metadata(message.getMetadata() != null ? message.getMetadata() : new HashMap<>())
                .createdBy(message.getParticipantId())
                .build();
    }

    private void broadcastInteractionToSession(UUID sessionId, InteractionMessage message) {
        // Send to session topic for all participants
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/interactions", message);
    }

    private InteractionMessage mapToMessage(SessionInteraction interaction) {
        return InteractionMessage.builder()
                .sessionId(interaction.getSession().getSessionId())
                .type(interaction.getType())
                .content(interaction.getContent())
                .participantId(interaction.getParticipantId())
                .timestamp(interaction.getTimestamp())
                .sequenceNumber(interaction.getSequenceNumber())
                .durationSeconds(interaction.getDurationSeconds())
                .metadata(interaction.getMetadata())
                .messageId(interaction.getInteractionId().toString())
                .action(InteractionMessage.MessageAction.CREATE)
                .build();
    }
}