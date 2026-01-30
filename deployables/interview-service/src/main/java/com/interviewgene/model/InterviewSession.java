package com.interviewgene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "interview_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "interviewer_id", nullable = false)
    private UUID interviewerId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "session_type", nullable = false)
    private String sessionType;

    @ElementCollection
    @CollectionTable(
        name = "session_metadata",
        joinColumns = @JoinColumn(name = "session_id")
    )
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SessionInteraction> interactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    // Helper methods for managing interactions
    public void addInteraction(SessionInteraction interaction) {
        interactions.add(interaction);
        interaction.setSession(this);
    }

    public void removeInteraction(SessionInteraction interaction) {
        interactions.remove(interaction);
        interaction.setSession(null);
    }

    // Helper methods for session lifecycle
    public void startSession() {
        this.status = SessionStatus.ACTIVE;
        this.startTime = LocalDateTime.now();
    }

    public void endSession() {
        this.status = SessionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    public void pauseSession() {
        this.status = SessionStatus.PAUSED;
    }

    public void cancelSession() {
        this.status = SessionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    public boolean isActive() {
        return SessionStatus.ACTIVE.equals(this.status);
    }

    public boolean isCompleted() {
        return SessionStatus.COMPLETED.equals(this.status) || 
               SessionStatus.CANCELLED.equals(this.status) ||
               SessionStatus.EXPIRED.equals(this.status);
    }

    public boolean canJoin() {
        return SessionStatus.SCHEDULED.equals(this.status) || 
               SessionStatus.ACTIVE.equals(this.status);
    }
}