package com.interviewgene.controller;

import com.interviewgene.dto.InteractionMessage;
import com.interviewgene.service.RealTimeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing session interactions
 */
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/interactions")
@RequiredArgsConstructor
@Slf4j
public class InteractionController {

    private final RealTimeHandler realTimeHandler;

    /**
     * Get all interactions for a session
     */
    @GetMapping
    public ResponseEntity<List<InteractionMessage>> getSessionInteractions(@PathVariable UUID sessionId) {
        log.debug("Getting interactions for session: {}", sessionId);
        
        List<InteractionMessage> interactions = realTimeHandler.getSessionInteractions(sessionId);
        return ResponseEntity.ok(interactions);
    }

    /**
     * Get interactions within a time range
     */
    @GetMapping("/range")
    public ResponseEntity<List<InteractionMessage>> getSessionInteractionsInRange(
            @PathVariable UUID sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.debug("Getting interactions for session: {} between {} and {}", sessionId, startTime, endTime);
        
        List<InteractionMessage> interactions = realTimeHandler.getSessionInteractions(sessionId, startTime, endTime);
        return ResponseEntity.ok(interactions);
    }

    /**
     * Delete an interaction
     */
    @DeleteMapping("/{interactionId}")
    public ResponseEntity<Void> deleteInteraction(
            @PathVariable UUID sessionId,
            @PathVariable UUID interactionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        log.info("Deleting interaction: {} from session: {} by user: {}", interactionId, sessionId, userId);
        
        realTimeHandler.deleteInteraction(interactionId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check for interaction service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Interaction service is healthy");
    }
}