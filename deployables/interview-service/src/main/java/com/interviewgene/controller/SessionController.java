package com.interviewgene.controller;

import com.interviewgene.dto.SessionCreateRequest;
import com.interviewgene.dto.SessionResponse;
import com.interviewgene.dto.SessionUpdateRequest;
import com.interviewgene.model.SessionStatus;
import com.interviewgene.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing interview sessions
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    /**
     * Create a new interview session
     */
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody SessionCreateRequest request) {
        log.info("Creating session for interviewer: {} and candidate: {}", 
                request.getInterviewerId(), request.getCandidateId());
        
        SessionResponse response = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get session by ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable UUID sessionId) {
        log.debug("Getting session: {}", sessionId);
        
        SessionResponse response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update session
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SessionUpdateRequest request) {
        log.info("Updating session: {}", sessionId);
        
        SessionResponse response = sessionService.updateSession(sessionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete session
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID sessionId) {
        log.info("Deleting session: {}", sessionId);
        
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get sessions by interviewer
     */
    @GetMapping("/interviewer/{interviewerId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByInterviewer(@PathVariable UUID interviewerId) {
        log.debug("Getting sessions for interviewer: {}", interviewerId);
        
        List<SessionResponse> sessions = sessionService.getSessionsByInterviewer(interviewerId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions by candidate
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByCandidate(@PathVariable UUID candidateId) {
        log.debug("Getting sessions for candidate: {}", candidateId);
        
        List<SessionResponse> sessions = sessionService.getSessionsByCandidate(candidateId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SessionResponse>> getSessionsByStatus(@PathVariable SessionStatus status) {
        log.debug("Getting sessions with status: {}", status);
        
        List<SessionResponse> sessions = sessionService.getSessionsByStatus(status);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Start session
     */
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<SessionResponse> startSession(@PathVariable UUID sessionId) {
        log.info("Starting session: {}", sessionId);
        
        SessionResponse response = sessionService.startSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * End session
     */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<SessionResponse> endSession(@PathVariable UUID sessionId) {
        log.info("Ending session: {}", sessionId);
        
        SessionResponse response = sessionService.endSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Pause session
     */
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<SessionResponse> pauseSession(@PathVariable UUID sessionId) {
        log.info("Pausing session: {}", sessionId);
        
        SessionResponse response = sessionService.pauseSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel session
     */
    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<SessionResponse> cancelSession(@PathVariable UUID sessionId) {
        log.info("Cancelling session: {}", sessionId);
        
        SessionResponse response = sessionService.cancelSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active sessions for user
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<SessionResponse>> getActiveSessionsForUser(@PathVariable UUID userId) {
        log.debug("Getting active sessions for user: {}", userId);
        
        List<SessionResponse> sessions = sessionService.getActiveSessionsForUser(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Session service is healthy");
    }
}