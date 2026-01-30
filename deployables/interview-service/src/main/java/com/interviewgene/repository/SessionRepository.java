package com.interviewgene.repository;

import com.interviewgene.model.InterviewSession;
import com.interviewgene.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for InterviewSession entity
 */
@Repository
public interface SessionRepository extends JpaRepository<InterviewSession, UUID> {

    /**
     * Find sessions by interviewer ID
     */
    List<InterviewSession> findByInterviewerId(UUID interviewerId);

    /**
     * Find sessions by candidate ID
     */
    List<InterviewSession> findByCandidateId(UUID candidateId);

    /**
     * Find sessions by status
     */
    List<InterviewSession> findByStatus(SessionStatus status);

    /**
     * Find sessions by interviewer and status
     */
    List<InterviewSession> findByInterviewerIdAndStatus(UUID interviewerId, SessionStatus status);

    /**
     * Find sessions by candidate and status
     */
    List<InterviewSession> findByCandidateIdAndStatus(UUID candidateId, SessionStatus status);

    /**
     * Find sessions scheduled between dates
     */
    @Query("SELECT s FROM InterviewSession s WHERE s.scheduledTime BETWEEN :startTime AND :endTime")
    List<InterviewSession> findSessionsScheduledBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find active sessions for a user (either as interviewer or candidate)
     */
    @Query("SELECT s FROM InterviewSession s WHERE (s.interviewerId = :userId OR s.candidateId = :userId) AND s.status = :status")
    List<InterviewSession> findActiveSessionsForUser(
            @Param("userId") UUID userId,
            @Param("status") SessionStatus status
    );

    /**
     * Find sessions by session type
     */
    List<InterviewSession> findBySessionType(String sessionType);

    /**
     * Count sessions by status
     */
    long countByStatus(SessionStatus status);

    /**
     * Find sessions that can be joined (SCHEDULED or ACTIVE)
     */
    @Query("SELECT s FROM InterviewSession s WHERE s.status IN ('SCHEDULED', 'ACTIVE')")
    List<InterviewSession> findJoinableSessions();
}