package com.interviewgene.repository;

import com.interviewgene.model.InteractionType;
import com.interviewgene.model.SessionInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for SessionInteraction entity
 */
@Repository
public interface SessionInteractionRepository extends JpaRepository<SessionInteraction, UUID> {

    /**
     * Find interactions by session ID ordered by timestamp
     */
    List<SessionInteraction> findBySessionSessionIdOrderByTimestampAsc(UUID sessionId);

    /**
     * Find interactions by session ID and type
     */
    List<SessionInteraction> findBySessionSessionIdAndType(UUID sessionId, InteractionType type);

    /**
     * Find interactions by participant
     */
    List<SessionInteraction> findByParticipantIdOrderByTimestampAsc(UUID participantId);

    /**
     * Find interactions by session and participant
     */
    List<SessionInteraction> findBySessionSessionIdAndParticipantIdOrderByTimestampAsc(
            UUID sessionId, UUID participantId);

    /**
     * Find interactions within time range
     */
    @Query("SELECT i FROM SessionInteraction i WHERE i.session.sessionId = :sessionId " +
           "AND i.timestamp BETWEEN :startTime AND :endTime ORDER BY i.timestamp ASC")
    List<SessionInteraction> findBySessionAndTimeRange(
            @Param("sessionId") UUID sessionId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Count interactions by session
     */
    long countBySessionSessionId(UUID sessionId);

    /**
     * Count interactions by session and type
     */
    long countBySessionSessionIdAndType(UUID sessionId, InteractionType type);

    /**
     * Find latest interaction by session
     */
    @Query("SELECT i FROM SessionInteraction i WHERE i.session.sessionId = :sessionId " +
           "ORDER BY i.timestamp DESC LIMIT 1")
    SessionInteraction findLatestBySessionId(@Param("sessionId") UUID sessionId);

    /**
     * Find interactions by sequence number range
     */
    @Query("SELECT i FROM SessionInteraction i WHERE i.session.sessionId = :sessionId " +
           "AND i.sequenceNumber BETWEEN :startSeq AND :endSeq ORDER BY i.sequenceNumber ASC")
    List<SessionInteraction> findBySessionAndSequenceRange(
            @Param("sessionId") UUID sessionId,
            @Param("startSeq") Integer startSeq,
            @Param("endSeq") Integer endSeq);

    /**
     * Get next sequence number for session
     */
    @Query("SELECT COALESCE(MAX(i.sequenceNumber), 0) + 1 FROM SessionInteraction i " +
           "WHERE i.session.sessionId = :sessionId")
    Integer getNextSequenceNumber(@Param("sessionId") UUID sessionId);
}