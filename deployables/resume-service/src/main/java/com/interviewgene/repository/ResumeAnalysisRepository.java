package com.interviewgene.repository;

import com.interviewgene.model.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ResumeAnalysis entity operations
 */
@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, UUID> {

    /**
     * Find all analyses for a specific resume
     */
    List<ResumeAnalysis> findByResumeResumeIdOrderByAnalyzedAtDesc(UUID resumeId);

    /**
     * Find the latest analysis for a resume
     */
    Optional<ResumeAnalysis> findTopByResumeResumeIdOrderByAnalyzedAtDesc(UUID resumeId);

    /**
     * Find analyses by type for a resume
     */
    List<ResumeAnalysis> findByResumeResumeIdAndAnalysisTypeOrderByAnalyzedAtDesc(UUID resumeId, String analysisType);

    /**
     * Find the latest analysis of a specific type for a resume
     */
    Optional<ResumeAnalysis> findTopByResumeResumeIdAndAnalysisTypeOrderByAnalyzedAtDesc(UUID resumeId, String analysisType);

    /**
     * Find analyses by overall score range
     */
    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.overallScore >= :minScore AND ra.overallScore <= :maxScore ORDER BY ra.overallScore DESC")
    List<ResumeAnalysis> findByOverallScoreBetween(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Find high-quality analyses (score >= 0.8)
     */
    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.overallScore >= 0.8 ORDER BY ra.overallScore DESC")
    List<ResumeAnalysis> findHighQualityAnalyses();

    /**
     * Find analyses by confidence level
     */
    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.confidenceLevel >= :minConfidence ORDER BY ra.confidenceLevel DESC")
    List<ResumeAnalysis> findByConfidenceLevelGreaterThanEqual(@Param("minConfidence") Double minConfidence);

    /**
     * Find analyses created within a date range
     */
    List<ResumeAnalysis> findByAnalyzedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find analyses by AI model version
     */
    List<ResumeAnalysis> findByAiModelVersionOrderByAnalyzedAtDesc(String aiModelVersion);

    /**
     * Count analyses by type
     */
    long countByAnalysisType(String analysisType);

    /**
     * Get average score by analysis type
     */
    @Query("SELECT AVG(ra.overallScore) FROM ResumeAnalysis ra WHERE ra.analysisType = :analysisType")
    Double getAverageScoreByType(@Param("analysisType") String analysisType);

    /**
     * Find analyses with processing time above threshold
     */
    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.processingTimeMs > :thresholdMs ORDER BY ra.processingTimeMs DESC")
    List<ResumeAnalysis> findSlowAnalyses(@Param("thresholdMs") Long thresholdMs);

    /**
     * Find analyses for performance monitoring
     */
    @Query("""
        SELECT ra FROM ResumeAnalysis ra 
        WHERE ra.analyzedAt >= :since 
        ORDER BY ra.processingTimeMs DESC
        """)
    List<ResumeAnalysis> findRecentAnalysesForMonitoring(@Param("since") LocalDateTime since);

    /**
     * Get analysis statistics by type
     */
    @Query("""
        SELECT ra.analysisType, 
               COUNT(ra), 
               AVG(ra.overallScore), 
               AVG(ra.confidenceLevel), 
               AVG(ra.processingTimeMs)
        FROM ResumeAnalysis ra 
        WHERE ra.analyzedAt >= :since 
        GROUP BY ra.analysisType
        """)
    List<Object[]> getAnalysisStatistics(@Param("since") LocalDateTime since);

    /**
     * Delete old analyses (for cleanup)
     */
    void deleteByAnalyzedAtBefore(LocalDateTime cutoffDate);
}