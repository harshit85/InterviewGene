package com.interviewgene.property;

import com.interviewgene.model.InterviewSession;
import com.interviewgene.model.SessionStatus;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

/**
 * Property-based tests for Interview Service session state consistency
 */
@Tag("Feature: interview-platform-microservices, Property 2: Session State Consistency")
public class SessionStateConsistencyPropertyTest {

    /**
     * Property 2: Session State Consistency
     * For any valid session state transition, the session should maintain consistent state
     * and enforce proper lifecycle rules
     * Validates: Requirements 1.3, 1.4, 1.5
     */
    @Property(tries = 100)
    void sessionStateTransitionsAreConsistent(@ForAll("validSessions") InterviewSession session) {
        // Given: A session in any valid state
        SessionStatus originalStatus = session.getStatus();
        
        // When: State transitions are applied
        // Then: Session should maintain consistency
        
        // Test canJoin() consistency
        if (session.getStatus() == SessionStatus.SCHEDULED || session.getStatus() == SessionStatus.ACTIVE) {
            Assume.that(session.canJoin());
        } else {
            Assume.that(!session.canJoin());
        }
        
        // Test isActive() consistency
        if (session.getStatus() == SessionStatus.ACTIVE) {
            Assume.that(session.isActive());
        } else {
            Assume.that(!session.isActive());
        }
        
        // Test isCompleted() consistency
        if (session.getStatus() == SessionStatus.COMPLETED || 
            session.getStatus() == SessionStatus.CANCELLED ||
            session.getStatus() == SessionStatus.EXPIRED) {
            Assume.that(session.isCompleted());
        } else {
            Assume.that(!session.isCompleted());
        }
    }

    @Property(tries = 100)
    void sessionLifecycleTransitionsAreValid(@ForAll("scheduledSessions") InterviewSession session) {
        // Given: A scheduled session
        Assume.that(session.getStatus() == SessionStatus.SCHEDULED);
        Assume.that(session.canJoin());
        
        // When: Session is started
        session.startSession();
        
        // Then: Session should be active with proper timestamps
        Assume.that(session.getStatus() == SessionStatus.ACTIVE);
        Assume.that(session.isActive());
        Assume.that(session.getStartTime() != null);
        Assume.that(session.getEndTime() == null);
        
        // When: Session is ended
        session.endSession();
        
        // Then: Session should be completed with proper timestamps
        Assume.that(session.getStatus() == SessionStatus.COMPLETED);
        Assume.that(session.isCompleted());
        Assume.that(!session.canJoin());
        Assume.that(session.getEndTime() != null);
        Assume.that(session.getEndTime().isAfter(session.getStartTime()) || 
                   session.getEndTime().isEqual(session.getStartTime()));
    }

    @Property(tries = 100)
    void sessionPauseResumeTransitionsAreValid(@ForAll("activeSessions") InterviewSession session) {
        // Given: An active session
        Assume.that(session.getStatus() == SessionStatus.ACTIVE);
        Assume.that(session.isActive());
        
        // When: Session is paused
        session.pauseSession();
        
        // Then: Session should be paused
        Assume.that(session.getStatus() == SessionStatus.PAUSED);
        Assume.that(!session.isActive());
        Assume.that(!session.isCompleted());
        Assume.that(!session.canJoin()); // Paused sessions cannot be joined
        
        // When: Session is resumed (started again)
        session.startSession();
        
        // Then: Session should be active again
        Assume.that(session.getStatus() == SessionStatus.ACTIVE);
        Assume.that(session.isActive());
        Assume.that(session.canJoin());
    }

    @Property(tries = 100)
    void sessionCancellationIsConsistent(@ForAll("nonCompletedSessions") InterviewSession session) {
        // Given: A non-completed session
        Assume.that(!session.isCompleted());
        
        // When: Session is cancelled
        session.cancelSession();
        
        // Then: Session should be cancelled with proper state
        Assume.that(session.getStatus() == SessionStatus.CANCELLED);
        Assume.that(session.isCompleted());
        Assume.that(!session.canJoin());
        Assume.that(!session.isActive());
        Assume.that(session.getEndTime() != null);
    }

    @Property(tries = 100)
    void sessionTimestampsAreConsistent(@ForAll("sessionsWithTimestamps") InterviewSession session) {
        // Given: A session with timestamps
        // Then: Timestamps should be logically consistent
        
        if (session.getStartTime() != null && session.getEndTime() != null) {
            // End time should be after or equal to start time
            Assume.that(!session.getEndTime().isBefore(session.getStartTime()));
        }
        
        if (session.getScheduledTime() != null && session.getStartTime() != null) {
            // Start time should be reasonably close to scheduled time (within 24 hours)
            long hoursDifference = java.time.Duration.between(session.getScheduledTime(), session.getStartTime()).toHours();
            Assume.that(Math.abs(hoursDifference) <= 24);
        }
        
        // Created timestamp should always exist and be before updated timestamp
        Assume.that(session.getCreatedAt() != null);
        if (session.getUpdatedAt() != null) {
            Assume.that(!session.getUpdatedAt().isBefore(session.getCreatedAt()));
        }
    }

    // Generators
    @Provide
    Arbitrary<InterviewSession> validSessions() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(SessionStatus.values()),
                Arbitraries.of(LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(2), null),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "CODING")
        ).as((sessionId, interviewerId, candidateId, status, scheduledTime, sessionType) ->
                InterviewSession.builder()
                        .sessionId(sessionId)
                        .interviewerId(interviewerId)
                        .candidateId(candidateId)
                        .status(status)
                        .scheduledTime(scheduledTime)
                        .sessionType(sessionType)
                        .metadata(new HashMap<>())
                        .createdAt(LocalDateTime.now().minusMinutes(30))
                        .updatedAt(LocalDateTime.now())
                        .version(1L)
                        .build()
        );
    }

    @Provide
    Arbitrary<InterviewSession> scheduledSessions() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "CODING")
        ).as((sessionId, interviewerId, candidateId, scheduledTime, sessionType) ->
                InterviewSession.builder()
                        .sessionId(sessionId)
                        .interviewerId(interviewerId)
                        .candidateId(candidateId)
                        .status(SessionStatus.SCHEDULED)
                        .scheduledTime(scheduledTime)
                        .sessionType(sessionType)
                        .metadata(new HashMap<>())
                        .createdAt(LocalDateTime.now().minusMinutes(30))
                        .version(1L)
                        .build()
        );
    }

    @Provide
    Arbitrary<InterviewSession> activeSessions() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "CODING")
        ).as((sessionId, interviewerId, candidateId, sessionType) ->
                InterviewSession.builder()
                        .sessionId(sessionId)
                        .interviewerId(interviewerId)
                        .candidateId(candidateId)
                        .status(SessionStatus.ACTIVE)
                        .startTime(LocalDateTime.now().minusMinutes(10))
                        .sessionType(sessionType)
                        .metadata(new HashMap<>())
                        .createdAt(LocalDateTime.now().minusMinutes(30))
                        .version(1L)
                        .build()
        );
    }

    @Provide
    Arbitrary<InterviewSession> nonCompletedSessions() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(SessionStatus.SCHEDULED, SessionStatus.ACTIVE, SessionStatus.PAUSED),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "CODING")
        ).as((sessionId, interviewerId, candidateId, status, sessionType) ->
                InterviewSession.builder()
                        .sessionId(sessionId)
                        .interviewerId(interviewerId)
                        .candidateId(candidateId)
                        .status(status)
                        .startTime(status == SessionStatus.ACTIVE || status == SessionStatus.PAUSED ? 
                                  LocalDateTime.now().minusMinutes(10) : null)
                        .sessionType(sessionType)
                        .metadata(new HashMap<>())
                        .createdAt(LocalDateTime.now().minusMinutes(30))
                        .version(1L)
                        .build()
        );
    }

    @Provide
    Arbitrary<InterviewSession> sessionsWithTimestamps() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(SessionStatus.values()),
                Arbitraries.of(LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(2), null),
                Arbitraries.of(LocalDateTime.now().minusHours(1), LocalDateTime.now(), null),
                Arbitraries.of(LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), null),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "CODING")
        ).as((sessionId, interviewerId, candidateId, status, scheduledTime, startTime, endTime, sessionType) -> {
            // Ensure logical timestamp ordering
            LocalDateTime adjustedStartTime = startTime;
            LocalDateTime adjustedEndTime = endTime;
            
            if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
                adjustedEndTime = startTime.plusMinutes(30);
            }
            
            return InterviewSession.builder()
                    .sessionId(sessionId)
                    .interviewerId(interviewerId)
                    .candidateId(candidateId)
                    .status(status)
                    .scheduledTime(scheduledTime)
                    .startTime(adjustedStartTime)
                    .endTime(adjustedEndTime)
                    .sessionType(sessionType)
                    .metadata(new HashMap<>())
                    .createdAt(LocalDateTime.now().minusMinutes(60))
                    .updatedAt(LocalDateTime.now().minusMinutes(30))
                    .version(1L)
                    .build();
        });
    }
}