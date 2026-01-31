package com.interviewgene.service;

import com.interviewgene.common.exception.ResourceNotFoundException;
import com.interviewgene.common.exception.ValidationException;
import com.interviewgene.dto.SessionCreateRequest;
import com.interviewgene.dto.SessionResponse;
import com.interviewgene.dto.SessionUpdateRequest;
import com.interviewgene.model.InterviewSession;
import com.interviewgene.model.SessionStatus;
import com.interviewgene.repository.SessionRepository;
import com.interviewgene.client.UserServiceClient;
import com.interviewgene.event.SessionEventProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing interview sessions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserServiceClient userServiceClient;
    private final SessionEventProducer sessionEventProducer;

    /**
     * Create a new interview session
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackCreateSession")
    public SessionResponse createSession(SessionCreateRequest request) {
        log.info("Creating new session for interviewer: {} and candidate: {}", 
                request.getInterviewerId(), request.getCandidateId());

        validateCreateRequest(request);

        // Verify users exist
        try {
            userServiceClient.getUser(request.getInterviewerId().toString());
            userServiceClient.getUser(request.getCandidateId().toString());
        } catch (Exception e) {
            log.error("User validation failed: {}", e.getMessage());
            throw new ValidationException("Interviewer or Candidate not found");
        }

        InterviewSession session = InterviewSession.builder()
                .interviewerId(request.getInterviewerId())
                .candidateId(request.getCandidateId())
                .scheduledTime(request.getScheduledTime())
                .sessionType(request.getSessionType())
                .status(SessionStatus.SCHEDULED)
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .build();

        InterviewSession savedSession = sessionRepository.save(session);
        log.info("Created session with ID: {}", savedSession.getSessionId());

        return mapToResponse(savedSession);
    }

    /**
     * Get session by ID
     */
    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID sessionId) {
        log.debug("Retrieving session: {}", sessionId);
        
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        
        return mapToResponse(session);
    }

    /**
     * Update session
     */
    public SessionResponse updateSession(UUID sessionId, SessionUpdateRequest request) {
        log.info("Updating session: {}", sessionId);

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        updateSessionFields(session, request);
        InterviewSession updatedSession = sessionRepository.save(session);

        log.info("Updated session: {}", sessionId);
        return mapToResponse(updatedSession);
    }

    /**
     * Delete session
     */
    public void deleteSession(UUID sessionId) {
        log.info("Deleting session: {}", sessionId);

        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        sessionRepository.deleteById(sessionId);
        log.info("Deleted session: {}", sessionId);
    }

    /**
     * Get sessions by interviewer
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByInterviewer(UUID interviewerId) {
        log.debug("Retrieving sessions for interviewer: {}", interviewerId);
        
        return sessionRepository.findByInterviewerId(interviewerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get sessions by candidate
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByCandidate(UUID candidateId) {
        log.debug("Retrieving sessions for candidate: {}", candidateId);
        
        return sessionRepository.findByCandidateId(candidateId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get sessions by status
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByStatus(SessionStatus status) {
        log.debug("Retrieving sessions with status: {}", status);
        
        return sessionRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Start session
     */
    public SessionResponse startSession(UUID sessionId) {
        log.info("Starting session: {}", sessionId);

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.canJoin()) {
            throw new ValidationException("Session cannot be started in current state: " + session.getStatus());
        }

        session.startSession();
        InterviewSession updatedSession = sessionRepository.save(session);

        log.info("Started session: {}", sessionId);
        return mapToResponse(updatedSession);
    }

    /**
     * End session
     */
    public SessionResponse endSession(UUID sessionId) {
        log.info("Ending session: {}", sessionId);

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.isActive()) {
            throw new ValidationException("Only active sessions can be ended");
        }

        session.endSession();
        InterviewSession updatedSession = sessionRepository.save(session);

        log.info("Ended session: {}", sessionId);

        // Emit event for analytics
        try {
            sessionEventProducer.sendSessionCompletedEvent(
                    updatedSession.getSessionId(),
                    updatedSession.getCandidateId(),
                    new HashMap<>(updatedSession.getMetadata())
            );
        } catch (Exception e) {
            log.error("Failed to send session completed event: {}", e.getMessage());
        }

        return mapToResponse(updatedSession);
    }

    /**
     * Pause session
     */
    public SessionResponse pauseSession(UUID sessionId) {
        log.info("Pausing session: {}", sessionId);

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.isActive()) {
            throw new ValidationException("Only active sessions can be paused");
        }

        session.pauseSession();
        InterviewSession updatedSession = sessionRepository.save(session);

        log.info("Paused session: {}", sessionId);
        return mapToResponse(updatedSession);
    }

    /**
     * Cancel session
     */
    public SessionResponse cancelSession(UUID sessionId) {
        log.info("Cancelling session: {}", sessionId);

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (session.isCompleted()) {
            throw new ValidationException("Cannot cancel completed session");
        }

        session.cancelSession();
        InterviewSession updatedSession = sessionRepository.save(session);

        log.info("Cancelled session: {}", sessionId);
        return mapToResponse(updatedSession);
    }

    /**
     * Get active sessions for user
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessionsForUser(UUID userId) {
        log.debug("Retrieving active sessions for user: {}", userId);
        
        return sessionRepository.findActiveSessionsForUser(userId, SessionStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateCreateRequest(SessionCreateRequest request) {
        if (request.getInterviewerId().equals(request.getCandidateId())) {
            throw new ValidationException("Interviewer and candidate cannot be the same person");
        }

        if (request.getScheduledTime() != null && request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Scheduled time cannot be in the past");
        }
    }

    private void updateSessionFields(InterviewSession session, SessionUpdateRequest request) {
        if (request.getStatus() != null) {
            session.setStatus(request.getStatus());
        }
        if (request.getScheduledTime() != null) {
            session.setScheduledTime(request.getScheduledTime());
        }
        if (request.getSessionType() != null) {
            session.setSessionType(request.getSessionType());
        }
        if (request.getMetadata() != null) {
            session.setMetadata(request.getMetadata());
        }
    }

    private SessionResponse mapToResponse(InterviewSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .interviewerId(session.getInterviewerId())
                .candidateId(session.getCandidateId())
                .status(session.getStatus())
                .scheduledTime(session.getScheduledTime())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .sessionType(session.getSessionType())
                .metadata(session.getMetadata())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .version(session.getVersion())
                .canJoin(session.canJoin())
                .isActive(session.isActive())
                .isCompleted(session.isCompleted())
                .build();
    }

    /**
     * Fallback method for createSession when userService is unavailable
     */
    public SessionResponse fallbackCreateSession(SessionCreateRequest request, Exception e) {
        log.warn("User Service is unavailable, creating session without validation. Error: {}", e.getMessage());
        
        InterviewSession session = InterviewSession.builder()
                .interviewerId(request.getInterviewerId())
                .candidateId(request.getCandidateId())
                .scheduledTime(request.getScheduledTime())
                .sessionType(request.getSessionType())
                .status(SessionStatus.SCHEDULED)
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .build();

        InterviewSession savedSession = sessionRepository.save(session);
        return mapToResponse(savedSession);
    }
}