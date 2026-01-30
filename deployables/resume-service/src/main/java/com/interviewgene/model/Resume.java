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

/**
 * Entity representing a resume document and its processed data
 */
@Entity
@Table(name = "resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resume_id")
    private UUID resumeId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "s3_bucket")
    private String s3Bucket;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ResumeStatus status = ResumeStatus.UPLOADED;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @Column(name = "last_analyzed_at")
    private LocalDateTime lastAnalyzedAt;

    @Embedded
    private ResumeData parsedData;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ResumeAnalysis> analyses = new ArrayList<>();

    @OneToMany(mappedBy = "resumeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "resumeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Education> educations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "resume_metadata",
        joinColumns = @JoinColumn(name = "resume_id")
    )
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Helper methods for managing relationships
    public void addAnalysis(ResumeAnalysis analysis) {
        analyses.add(analysis);
        analysis.setResume(this);
    }

    public void removeAnalysis(ResumeAnalysis analysis) {
        analyses.remove(analysis);
        analysis.setResume(null);
    }

    public void addWorkExperience(WorkExperience experience) {
        workExperiences.add(experience);
        experience.setResumeId(this.resumeId);
    }

    public void addEducation(Education education) {
        educations.add(education);
        education.setResumeId(this.resumeId);
    }

    // Status helper methods
    public boolean isParsed() {
        return ResumeStatus.PARSED.equals(status) || 
               ResumeStatus.ANALYZING.equals(status) || 
               ResumeStatus.ANALYZED.equals(status);
    }

    public boolean isAnalyzed() {
        return ResumeStatus.ANALYZED.equals(status);
    }

    public boolean isProcessing() {
        return ResumeStatus.PARSING.equals(status) || 
               ResumeStatus.ANALYZING.equals(status);
    }

    public boolean hasErrors() {
        return ResumeStatus.PARSE_FAILED.equals(status) || 
               ResumeStatus.ANALYSIS_FAILED.equals(status);
    }

    public boolean canBeAnalyzed() {
        return isParsed() && !isProcessing();
    }

    // File helper methods
    public String getFileExtension() {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return null;
    }

    public boolean isPdfFile() {
        return "pdf".equals(getFileExtension()) || "application/pdf".equals(contentType);
    }

    public boolean isWordFile() {
        String ext = getFileExtension();
        return "doc".equals(ext) || "docx".equals(ext) || 
               "application/msword".equals(contentType) ||
               "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType);
    }

    // Analysis helper methods
    public ResumeAnalysis getLatestAnalysis() {
        return analyses.stream()
                .max((a1, a2) -> a1.getAnalyzedAt().compareTo(a2.getAnalyzedAt()))
                .orElse(null);
    }

    public ResumeAnalysis getAnalysisByType(String analysisType) {
        return analyses.stream()
                .filter(analysis -> analysisType.equals(analysis.getAnalysisType()))
                .max((a1, a2) -> a1.getAnalyzedAt().compareTo(a2.getAnalyzedAt()))
                .orElse(null);
    }

    public boolean hasAnalysisType(String analysisType) {
        return getAnalysisByType(analysisType) != null;
    }

    // Experience and education helper methods
    public int getTotalWorkExperienceYears() {
        if (parsedData != null && parsedData.getTotalExperienceYears() != null) {
            return parsedData.getTotalExperienceYears();
        }
        
        return workExperiences.stream()
                .mapToInt(exp -> (int) exp.getDurationInMonths())
                .sum() / 12;
    }

    public WorkExperience getCurrentJob() {
        return workExperiences.stream()
                .filter(WorkExperience::isCurrentPosition)
                .findFirst()
                .orElse(null);
    }

    public Education getHighestEducation() {
        return educations.stream()
                .filter(Education::isGraduated)
                .max((e1, e2) -> {
                    // Simple degree level comparison
                    int level1 = getDegreeLevel(e1.getDegreeType());
                    int level2 = getDegreeLevel(e2.getDegreeType());
                    return Integer.compare(level1, level2);
                })
                .orElse(null);
    }

    private int getDegreeLevel(String degreeType) {
        if (degreeType == null) return 0;
        String degree = degreeType.toLowerCase();
        if (degree.contains("phd") || degree.contains("doctorate")) return 4;
        if (degree.contains("master")) return 3;
        if (degree.contains("bachelor")) return 2;
        if (degree.contains("associate")) return 1;
        return 0;
    }
}