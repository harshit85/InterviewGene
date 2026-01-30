package com.interviewgene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing education information from resume
 */
@Entity
@Table(name = "educations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "education_id")
    private UUID educationId;

    @Column(name = "resume_id", nullable = false)
    private UUID resumeId;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "degree_type")
    private String degreeType; // Bachelor's, Master's, PhD, Certificate, etc.

    @Column(name = "field_of_study")
    private String fieldOfStudy;

    @Column(name = "major")
    private String major;

    @Column(name = "minor")
    private String minor;

    @Column(name = "gpa")
    private Double gpa;

    @Column(name = "max_gpa")
    private Double maxGpa;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "location")
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(
        name = "education_honors",
        joinColumns = @JoinColumn(name = "education_id")
    )
    @Column(name = "honor")
    @Builder.Default
    private List<String> honors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "education_activities",
        joinColumns = @JoinColumn(name = "education_id")
    )
    @Column(name = "activity")
    @Builder.Default
    private List<String> activities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "education_relevant_courses",
        joinColumns = @JoinColumn(name = "education_id")
    )
    @Column(name = "course")
    @Builder.Default
    private List<String> relevantCourses = new ArrayList<>();

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = true;

    // Helper methods
    public boolean isGraduated() {
        return Boolean.TRUE.equals(isCompleted) && graduationDate != null;
    }

    public boolean hasHonors() {
        return honors != null && !honors.isEmpty();
    }

    public String getFormattedGpa() {
        if (gpa != null) {
            if (maxGpa != null) {
                return String.format("%.2f/%.1f", gpa, maxGpa);
            } else {
                return String.format("%.2f", gpa);
            }
        }
        return null;
    }
}