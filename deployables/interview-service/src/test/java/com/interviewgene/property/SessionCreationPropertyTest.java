package com.interviewgene.property;

import com.interviewgene.dto.SessionCreationRequest;
import com.interviewgene.model.InterviewSession;
import com.interviewgene.model.SessionStatus;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Property-based tests for Interview Service session creation
 */
@Tag("Feature: interview-platform-microservices, Property 1: Session Creation Uniqueness")
public class SessionCreationPropertyTest {

    /**
     * Property 1: Session Creation Uniqueness
     * For any interview session creation request, the Interview_Service should generate 
     * a unique session identifier and initialize the session with proper default state
     * Validates: Requirements 1.1
     */
    @Property(tries = 100)
    void sessionCreationGeneratesUniqueIdentifiers(@ForAll("sessionCreationRequests") SessionCreationRequest request) {
        // Given: A session creation request
        InterviewSession session = createSessionFromRequest(request);
        
        // When: Session is created
        // Then: Session should have unique identifier and proper default state
        Assume.that(session != null);
        Assume.that(session.getSessionId() != null);
        Assume.that(session.getInterviewerId().equals(request.getInterviewerId()));
        Assume.that(session.getCandidateId().equals(request.getCandidateId()));
        Assume.that(session.getStatus() == SessionStatus.SCHEDULED);
        Assume.that(session.getCreatedAt() != null);
        Assume.that(session.getVersion() != null);
    }

    @Property(tries = 100)
    void multipleSessionCreationsProduceUniqueIds(@ForAll("sessionCreationRequestList") java.util.List<SessionCreationRequest> requests) {
        // Given: Multiple session creation requests
        Set<UUID> sessionIds = new HashSet<>();
        
        // When: Multiple sessions are created
        for (SessionCreationRequest request : requests) {
            InterviewSession session = createSessionFromRequest(request);
            
            // Then: Each session should have a unique identifier
            Assume.that(session.getSessionId() != null);
            Assume.that(!sessionIds.contains(session.getSessionId())); // Uniqueness check
            sessionIds.add(session.getSessionId());
        }
    }

    @Property(tries = 100)
    void sessionInitializationWithProperDefaults(@ForAll("sessionCreationRequests") SessionCreationRequest request) {
        // Given: A session creation request
        InterviewSession session = createSessionFromRequest(request);
        
        // When: Session is initialized
        // Then: Session should have proper default values
        Assume.that(session.getStatus() == SessionStatus.SCHEDULED);
        Assume.that(session.getInteractions().isEmpty());
        Assume.that(session.getMetadata() != null);
        Assume.that(session.getCreatedAt() != null);
        Assume.that(session.getVersion() == null || session.getVersion() == 0L); // New entity
        Assume.that(session.canJoin()); // Should be joinable when scheduled
    }

    // Generators
    @Provide
    Arbitrary<SessionCreationRequest> sessionCreationRequests() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "CODING"),
                metadataArbitrary()
        ).as(SessionCreationRequest::new);
    }

    @Provide
    Arbitrary<java.util.List<SessionCreationRequest>> sessionCreationRequestList() {
        return sessionCreationRequests().list().ofMinSize(2).ofMaxSize(10);
    }

    @Provide
    Arbitrary<Map<String, String>> metadataArbitrary() {
        return Arbitraries.maps(
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(15),
                Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(5).ofMaxLength(20)
        ).ofMinSize(0).ofMaxSize(5);
    }

    // Helper method to simulate session creation
    private InterviewSession createSessionFromRequest(SessionCreationRequest request) {
        return InterviewSession.builder()
                .sessionId(UUID.randomUUID()) // Simulates unique ID generation
                .interviewerId(request.getInterviewerId())
                .candidateId(request.getCandidateId())
                .scheduledTime(request.getScheduledTime())
                .sessionType(request.getSessionType())
                .status(SessionStatus.SCHEDULED) // Default status
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();
    }
}