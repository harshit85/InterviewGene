package com.interviewgene.dto;

import com.interviewgene.model.ResumeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for resume response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private UUID resumeId;
    private UUID candidateId;
    private String originalFilename;
    private Long fileSize;
    private String contentType;
    private ResumeStatus status;
    private LocalDateTime uploadedAt;
    private LocalDateTime parsedAt;
    private LocalDateTime lastAnalyzedAt;
    private ResumeDataResponse parsedData;
    private List<ResumeAnalysisResponse> analyses;
    private Map<String, String> metadata;
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeDataResponse {
        private PersonalInfoResponse personalInfo;
        private List<String> skills;
        private List<String> certifications;
        private List<String> languages;
        private List<String> projects;
        private List<String> awards;
        private List<String> publications;
        private Integer totalExperienceYears;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfoResponse {
        private String fullName;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private String linkedinUrl;
        private String githubUrl;
        private String portfolioUrl;
        private String summary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeAnalysisResponse {
        private UUID analysisId;
        private String analysisType;
        private Double overallScore;
        private Double confidenceLevel;
        private String analysisSummary;
        private String strengths;
        private String weaknesses;
        private String recommendations;
        private Map<String, Double> categoryScores;
        private LocalDateTime analyzedAt;
        private String aiModelVersion;
        private Long processingTimeMs;
    }
}