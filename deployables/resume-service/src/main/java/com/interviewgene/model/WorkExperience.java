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
 * Entity representing work experience from resume
 */
@Entity
@Table(name = "work_experiences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "experience_id")
    private UUID experienceId;

    @Column(name = "resume_id", nullable = false)
    private UUID resumeId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(name = "location")
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(
        name = "work_experience_responsibilities",
        joinColumns = @JoinColumn(name = "experience_id")
    )
    @Column(name = "responsibility", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> responsibilities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "work_experience_achievements",
        joinColumns = @JoinColumn(name = "experience_id")
    )
    @Column(name = "achievement", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> achievements = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "work_experience_technologies",
        joinColumns = @JoinColumn(name = "experience_id")
    )
    @Column(name = "technology")
    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    @Column(name = "employment_type")
    private String employmentType; // Full-time, Part-time, Contract, Internship

    @Column(name = "industry")
    private String industry;

    @Column(name = "company_size")
    private String companySize;

    // Helper methods
    public boolean isCurrentPosition() {
        return Boolean.TRUE.equals(isCurrent);
    }

    public long getDurationInMonths() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        if (startDate != null) {
            return java.time.Period.between(startDate, end).toTotalMonths();
        }
        return 0;
    }
}