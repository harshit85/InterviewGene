package com.interviewgene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Embeddable class containing parsed resume data
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeData {

    @Embedded
    private PersonalInfo personalInfo;

    @ElementCollection
    @CollectionTable(
        name = "resume_skills",
        joinColumns = @JoinColumn(name = "resume_id")
    )
    @Column(name = "skill")
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "resume_certifications",
        joinColumns = @JoinColumn(name = "resume_id")
    )
    @Column(name = "certification")
    @Builder.Default
    private List<String> certifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "resume_languages",
        joinColumns = @JoinColumn(name = "resume_id")
    )
    @Column(name = "language")
    @Builder.Default
    private List<String> languages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "resume_projects",
        joinColumns = @JoinColumn(name = "resume_id")
    )
    @Column(name = "project", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> projects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "resume_awards",
        joinColumns = @JoinColumn(name = "resume_id")
    )
    @Column(name = "award")
    @Builder.Default
    private List<String> awards = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "resume_publications",
        joinColumns = @JoinColumn(name = "resume_id")
    )
    @Column(name = "publication", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> publications = new ArrayList<>();

    @Column(name = "total_experience_years")
    private Integer totalExperienceYears;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    // Helper methods
    public boolean hasSkills() {
        return skills != null && !skills.isEmpty();
    }

    public boolean hasCertifications() {
        return certifications != null && !certifications.isEmpty();
    }

    public boolean hasProjects() {
        return projects != null && !projects.isEmpty();
    }

    public int getSkillCount() {
        return skills != null ? skills.size() : 0;
    }

    public int getCertificationCount() {
        return certifications != null ? certifications.size() : 0;
    }
}