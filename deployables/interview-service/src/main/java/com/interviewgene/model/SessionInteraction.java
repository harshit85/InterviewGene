package com.interviewgene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_interactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "interaction_id")
    private UUID interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InteractionType type;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @ElementCollection
    @CollectionTable(
        name = "interaction_metadata",
        joinColumns = @JoinColumn(name = "interaction_id")
    )
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    @Builder.Default
    private java.util.Map<String, String> metadata = new java.util.HashMap<>();

    @Column(name = "created_by")
    private UUID createdBy;

    @Version
    @Column(name = "version")
    private Long version;

    // Helper methods
    public boolean isFromInterviewer(UUID interviewerId) {
        return interviewerId != null && interviewerId.equals(this.participantId);
    }

    public boolean isFromCandidate(UUID candidateId) {
        return candidateId != null && candidateId.equals(this.participantId);
    }

    public boolean isSystemEvent() {
        return InteractionType.SYSTEM_EVENT.equals(this.type);
    }

    public boolean isUserGenerated() {
        return !isSystemEvent();
    }

    // Convenience methods for specific interaction types
    public boolean isQuestion() {
        return InteractionType.QUESTION.equals(this.type);
    }

    public boolean isAnswer() {
        return InteractionType.ANSWER.equals(this.type);
    }

    public boolean isNote() {
        return InteractionType.NOTE.equals(this.type);
    }

    public boolean isChatMessage() {
        return InteractionType.CHAT_MESSAGE.equals(this.type);
    }
}