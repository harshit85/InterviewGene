package com.interviewgene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing AI analysis results for a resume
 */
@Entity
@Table(name = "resume_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "analysis_id")
    private UUID analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "analysis_type", nullable = false)
    private String analysisType; // SKILL_MATCH, EXPERIENCE_LEVEL, QUALITY_SCORE, etc.

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "confidence_level")
    private Double confidenceLevel;

    @Column(name = "analysis_summary", columnDefinition = "TEXT")
    private String analysisSummary;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @ElementCollection
    @CollectionTable(
        name = "analysis_scores",
        joinColumns = @JoinColumn(name = "analysis_id")
    )
    @MapKeyColumn(name = "score_category")
    @Column(name = "score_value")
    @Builder.Default
    private Map<String, Double> categoryScores = new HashMap<>();

    @ElementCollection
    @CollectionTable(
        name = "analysis_metadata",
        joinColumns = @JoinColumn(name = "analysis_id")
    )
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(name = "analyzed_at", nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @Column(name = "analyzed_by")
    private UUID analyzedBy;

    @Column(name = "ai_model_version")
    private String aiModelVersion;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Version
    @Column(name = "version")
    private Long version;

    // Helper methods
    public boolean isHighQuality() {
        return overallScore != null && overallScore >= 0.8;
    }

    public boolean isHighConfidence() {
        return confidenceLevel != null && confidenceLevel >= 0.9;
    }

    public Double getCategoryScore(String category) {
        return categoryScores != null ? categoryScores.get(category) : null;
    }

    public void setCategoryScore(String category, Double score) {
        if (categoryScores == null) {
            categoryScores = new HashMap<>();
        }
        categoryScores.put(category, score);
    }

    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public void setMetadataValue(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
}