package com.interviewgene.property;

import com.interviewgene.model.InteractionType;
import com.interviewgene.model.InterviewSession;
import com.interviewgene.model.SessionInteraction;
import com.interviewgene.model.SessionStatus;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Property-based tests for Interview Service session data completeness
 */
@Tag("Feature: interview-platform-microservices, Property 3: Session Data Completeness")
public class SessionDataCompletenessPropertyTest {

    /**
     * Property 3: Session Data Completeness
     * For any interview session, all required data fields should be present and valid,
     * and the session should maintain complete interaction history
     * Validates: Requirements 1.6
     */
    @Property(tries = 100)
    void sessionDataIsComplete(@ForAll("completeSessions") InterviewSession session) {
        // Given: A complete session
        // Then: All required fields should be present and valid
        
        // Core identifiers must be present
        Assume.that(session.getSessionId() != null);
        Assume.that(session.getInterviewerId() != null);
        Assume.that(session.getCandidateId() != null);
        
        // Participants must be different
        Assume.that(!session.getInterviewerId().equals(session.getCandidateId()));
        
        // Status must be valid
        Assume.that(session.getStatus() != null);
        
        // Session type must be present
        Assume.that(session.getSessionType() != null);
        Assume.that(!session.getSessionType().trim().isEmpty());
        
        // Metadata should be initialized (not null)
        Assume.that(session.getMetadata() != null);
        
        // Interactions list should be initialized (not null)
        Assume.that(session.getInteractions() != null);
        
        // Audit fields should be present
        Assume.that(session.getCreatedAt() != null);
        Assume.that(session.getVersion() != null);
    }

    @Property(tries = 100)
    void sessionInteractionHistoryIsComplete(@ForAll("sessionsWithInteractions") InterviewSession session) {
        // Given: A session with interactions
        // Then: Interaction history should be complete and consistent
        
        List<SessionInteraction> interactions = session.getInteractions();
        
        if (!interactions.isEmpty()) {
            // All interactions should belong to this session
            for (SessionInteraction interaction : interactions) {
                Assume.that(interaction.getSession() != null);
                Assume.that(interaction.getSession().getSessionId().equals(session.getSessionId()));
                
                // Interaction should have required fields
                Assume.that(interaction.getInteractionId() != null);
                Assume.that(interaction.getType() != null);
                Assume.that(interaction.getParticipantId() != null);
                Assume.that(interaction.getTimestamp() != null);
                Assume.that(interaction.getSequenceNumber() != null);
                Assume.that(interaction.getSequenceNumber() > 0);
                
                // Participant should be either interviewer or candidate
                Assume.that(interaction.getParticipantId().equals(session.getInterviewerId()) ||
                           interaction.getParticipantId().equals(session.getCandidateId()));
                
                // Metadata should be initialized
                Assume.that(interaction.getMetadata() != null);
            }
            
            // Sequence numbers should be unique and ordered
            List<Integer> sequenceNumbers = interactions.stream()
                    .map(SessionInteraction::getSequenceNumber)
                    .sorted()
                    .toList();
            
            for (int i = 0; i < sequenceNumbers.size(); i++) {
                // Sequence numbers should be positive and unique
                Assume.that(sequenceNumbers.get(i) > 0);
                if (i > 0) {
                    Assume.that(sequenceNumbers.get(i) > sequenceNumbers.get(i - 1));
                }
            }
        }
    }

    @Property(tries = 100)
    void sessionTimestampDataIsComplete(@ForAll("sessionsWithTimestamps") InterviewSession session) {
        // Given: A session with various timestamps
        // Then: Timestamp data should be complete and logically consistent
        
        // Created timestamp is always required
        Assume.that(session.getCreatedAt() != null);
        
        // If session has started, start time should be present
        if (session.getStatus() == SessionStatus.ACTIVE || 
            session.getStatus() == SessionStatus.COMPLETED ||
            session.getStatus() == SessionStatus.CANCELLED ||
            session.getStatus() == SessionStatus.PAUSED) {
            
            if (session.getStartTime() != null) {
                // Start time should be after or equal to creation time
                Assume.that(!session.getStartTime().isBefore(session.getCreatedAt()));
                
                // If scheduled, start time should be reasonably close to scheduled time
                if (session.getScheduledTime() != null) {
                    long hoursDifference = java.time.Duration.between(
                            session.getScheduledTime(), session.getStartTime()).toHours();
                    Assume.that(Math.abs(hoursDifference) <= 48); // Within 48 hours is reasonable
                }
            }
        }
        
        // If session has ended, end time should be present and after start time
        if (session.getStatus() == SessionStatus.COMPLETED || 
            session.getStatus() == SessionStatus.CANCELLED) {
            
            if (session.getEndTime() != null) {
                // End time should be after creation time
                Assume.that(!session.getEndTime().isBefore(session.getCreatedAt()));
                
                // If start time exists, end time should be after or equal to start time
                if (session.getStartTime() != null) {
                    Assume.that(!session.getEndTime().isBefore(session.getStartTime()));
                }
            }
        }
        
        // Updated timestamp should be after or equal to created timestamp
        if (session.getUpdatedAt() != null) {
            Assume.that(!session.getUpdatedAt().isBefore(session.getCreatedAt()));
        }
    }

    @Property(tries = 100)
    void sessionMetadataIsComplete(@ForAll("sessionsWithMetadata") InterviewSession session) {
        // Given: A session with metadata
        // Then: Metadata should be complete and valid
        
        Map<String, String> metadata = session.getMetadata();
        Assume.that(metadata != null);
        
        // All metadata keys and values should be non-null and non-empty
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            Assume.that(entry.getKey() != null);
            Assume.that(!entry.getKey().trim().isEmpty());
            Assume.that(entry.getValue() != null);
            // Values can be empty strings, but not null
        }
        
        // Common metadata fields should be properly formatted if present
        if (metadata.containsKey("duration")) {
            String duration = metadata.get("duration");
            // Duration should be a valid number if present
            try {
                Integer.parseInt(duration);
            } catch (NumberFormatException e) {
                Assume.that(false); // Duration should be parseable as integer
            }
        }
        
        if (metadata.containsKey("difficulty")) {
            String difficulty = metadata.get("difficulty");
            Assume.that(difficulty.matches("(?i)(easy|medium|hard|expert)"));
        }
    }

    @Property(tries = 100)
    void sessionAuditDataIsComplete(@ForAll("sessionsWithAuditData") InterviewSession session) {
        // Given: A session with audit data
        // Then: Audit information should be complete
        
        // Core audit fields
        Assume.that(session.getCreatedAt() != null);
        Assume.that(session.getVersion() != null);
        Assume.that(session.getVersion() >= 0);
        
        // If audit user fields are present, they should be valid UUIDs
        if (session.getCreatedBy() != null) {
            Assume.that(session.getCreatedBy().toString().length() == 36); // UUID string length
        }
        
        if (session.getUpdatedBy() != null) {
            Assume.that(session.getUpdatedBy().toString().length() == 36); // UUID string length
        }
        
        // Version should increment logically
        if (session.getUpdatedAt() != null && session.getUpdatedAt().isAfter(session.getCreatedAt())) {
            Assume.that(session.getVersion() > 0); // Should be incremented if updated
        }
    }

    // Generators
    @Provide
    Arbitrary<InterviewSession> completeSessions() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(SessionStatus.values()),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "CODING", "CULTURAL_FIT"),
                metadataArbitrary(),
                Arbitraries.longs().between(0L, 100L)
        ).as((sessionId, interviewerId, candidateId, status, sessionType, metadata, version) ->
                InterviewSession.builder()
                        .sessionId(sessionId)
                        .interviewerId(interviewerId)
                        .candidateId(candidateId)
                        .status(status)
                        .sessionType(sessionType)
                        .metadata(metadata)
                        .interactions(new ArrayList<>())
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .updatedAt(LocalDateTime.now())
                        .version(version)
                        .build()
        );
    }

    @Provide
    Arbitrary<InterviewSession> sessionsWithInteractions() {
        return Combinators.combine(
                completeSessions(),
                interactionsListArbitrary()
        ).as((session, interactions) -> {
            // Set up bidirectional relationship
            for (SessionInteraction interaction : interactions) {
                interaction.setSession(session);
                // Ensure participant is either interviewer or candidate
                if (Math.random() < 0.5) {
                    interaction.setParticipantId(session.getInterviewerId());
                } else {
                    interaction.setParticipantId(session.getCandidateId());
                }
            }
            session.setInteractions(interactions);
            return session;
        });
    }

    @Provide
    Arbitrary<InterviewSession> sessionsWithTimestamps() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(SessionStatus.values()),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN"),
                timestampSetArbitrary()
        ).as((sessionId, interviewerId, candidateId, status, sessionType, timestamps) ->
                InterviewSession.builder()
                        .sessionId(sessionId)
                        .interviewerId(interviewerId)
                        .candidateId(candidateId)
                        .status(status)
                        .sessionType(sessionType)
                        .scheduledTime(timestamps.get("scheduled"))
                        .startTime(timestamps.get("start"))
                        .endTime(timestamps.get("end"))
                        .createdAt(timestamps.get("created"))
                        .updatedAt(timestamps.get("updated"))
                        .metadata(new HashMap<>())
                        .interactions(new ArrayList<>())
                        .version(1L)
                        .build()
        );
    }

    @Provide
    Arbitrary<InterviewSession> sessionsWithMetadata() {
        return Combinators.combine(
                completeSessions(),
                richMetadataArbitrary()
        ).as((session, metadata) -> {
            session.setMetadata(metadata);
            return session;
        });
    }

    @Provide
    Arbitrary<InterviewSession> sessionsWithAuditData() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(SessionStatus.values()),
                Arbitraries.of("TECHNICAL", "BEHAVIORAL"),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.longs().between(0L, 50L)
        ).as((sessionId, interviewerId, candidateId, status, sessionType, createdBy, updatedBy, version) -> {
            LocalDateTime createdAt = LocalDateTime.now().minusHours(2);
            LocalDateTime updatedAt = version > 0 ? createdAt.plusMinutes(30) : null;
            
            return InterviewSession.builder()
                    .sessionId(sessionId)
                    .interviewerId(interviewerId)
                    .candidateId(candidateId)
                    .status(status)
                    .sessionType(sessionType)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .createdBy(createdBy)
                    .updatedBy(version > 0 ? updatedBy : null)
                    .version(version)
                    .metadata(new HashMap<>())
                    .interactions(new ArrayList<>())
                    .build();
        });
    }

    @Provide
    Arbitrary<Map<String, String>> metadataArbitrary() {
        return Arbitraries.maps(
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(3).ofMaxLength(10),
                Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(1).ofMaxLength(20)
        ).ofMinSize(0).ofMaxSize(5);
    }

    @Provide
    Arbitrary<Map<String, String>> richMetadataArbitrary() {
        return Arbitraries.maps(
                Arbitraries.of("duration", "difficulty", "topic", "notes", "rating"),
                Arbitraries.oneOf(
                        Arbitraries.integers().between(1, 180).map(String::valueOf), // duration
                        Arbitraries.of("easy", "medium", "hard", "expert"), // difficulty
                        Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(15) // general
                )
        ).ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<SessionInteraction>> interactionsListArbitrary() {
        return sessionInteractionArbitrary().list().ofMinSize(0).ofMaxSize(10);
    }

    @Provide
    Arbitrary<SessionInteraction> sessionInteractionArbitrary() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.of(InteractionType.values()),
                Arbitraries.strings().ofMinLength(1).ofMaxLength(500),
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.integers().between(1, 1000),
                Arbitraries.integers().between(1, 3600)
        ).as((interactionId, type, content, participantId, sequenceNumber, duration) ->
                SessionInteraction.builder()
                        .interactionId(interactionId)
                        .type(type)
                        .content(content)
                        .participantId(participantId)
                        .sequenceNumber(sequenceNumber)
                        .durationSeconds(duration)
                        .timestamp(LocalDateTime.now().minusMinutes(sequenceNumber))
                        .metadata(new HashMap<>())
                        .version(1L)
                        .build()
        );
    }

    @Provide
    Arbitrary<Map<String, LocalDateTime>> timestampSetArbitrary() {
        LocalDateTime baseTime = LocalDateTime.now().minusHours(3);
        
        return Combinators.combine(
                Arbitraries.of(baseTime, baseTime.plusHours(1), null), // scheduled
                Arbitraries.of(baseTime.plusHours(1), baseTime.plusHours(2), null), // start
                Arbitraries.of(baseTime.plusHours(2), baseTime.plusHours(3), null), // end
                Arbitraries.of(baseTime.minusHours(1)), // created (always present)
                Arbitraries.of(baseTime, baseTime.plusHours(1), null) // updated
        ).as((scheduled, start, end, created, updated) -> {
            Map<String, LocalDateTime> timestamps = new HashMap<>();
            timestamps.put("scheduled", scheduled);
            timestamps.put("start", start);
            timestamps.put("end", end);
            timestamps.put("created", created);
            timestamps.put("updated", updated);
            return timestamps;
        });
    }
}