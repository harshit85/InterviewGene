package com.interviewgene.repository;

import com.interviewgene.model.Resume;
import com.interviewgene.model.ResumeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Resume entity operations
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    /**
     * Find all resumes for a specific candidate
     */
    List<Resume> findByCandidateIdAndIsActiveTrue(UUID candidateId);

    /**
     * Find resumes by status
     */
    List<Resume> findByStatusAndIsActiveTrue(ResumeStatus status);

    /**
     * Find resumes by status with pagination
     */
    Page<Resume> findByStatusAndIsActiveTrue(ResumeStatus status, Pageable pageable);

    /**
     * Find the latest resume for a candidate
     */
    Optional<Resume> findTopByCandidateIdAndIsActiveTrueOrderByUploadedAtDesc(UUID candidateId);

    /**
     * Find resumes uploaded within a date range
     */
    List<Resume> findByUploadedAtBetweenAndIsActiveTrue(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find resumes that need parsing (uploaded but not parsed)
     */
    @Query("SELECT r FROM Resume r WHERE r.status = 'UPLOADED' AND r.isActive = true ORDER BY r.uploadedAt ASC")
    List<Resume> findResumesNeedingParsing();

    /**
     * Find resumes that need analysis (parsed but not analyzed)
     */
    @Query("SELECT r FROM Resume r WHERE r.status = 'PARSED' AND r.isActive = true ORDER BY r.parsedAt ASC")
    List<Resume> findResumesNeedingAnalysis();

    /**
     * Search resumes by skills
     */
    @Query("SELECT DISTINCT r FROM Resume r JOIN r.parsedData.skills s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :skill, '%')) AND r.isActive = true")
    List<Resume> findBySkillsContainingIgnoreCase(@Param("skill") String skill);

    /**
     * Search resumes by experience years
     */
    @Query("SELECT r FROM Resume r WHERE r.parsedData.totalExperienceYears >= :minYears AND r.parsedData.totalExperienceYears <= :maxYears AND r.isActive = true")
    List<Resume> findByExperienceYearsBetween(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    /**
     * Find resumes by company name in work experience
     */
    @Query("SELECT DISTINCT r FROM Resume r JOIN r.workExperiences we WHERE LOWER(we.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')) AND r.isActive = true")
    List<Resume> findByWorkExperienceCompanyContainingIgnoreCase(@Param("companyName") String companyName);

    /**
     * Find resumes by job title in work experience
     */
    @Query("SELECT DISTINCT r FROM Resume r JOIN r.workExperiences we WHERE LOWER(we.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%')) AND r.isActive = true")
    List<Resume> findByWorkExperienceJobTitleContainingIgnoreCase(@Param("jobTitle") String jobTitle);

    /**
     * Find resumes by education institution
     */
    @Query("SELECT DISTINCT r FROM Resume r JOIN r.educations e WHERE LOWER(e.institutionName) LIKE LOWER(CONCAT('%', :institution, '%')) AND r.isActive = true")
    List<Resume> findByEducationInstitutionContainingIgnoreCase(@Param("institution") String institution);

    /**
     * Find resumes by education degree type
     */
    @Query("SELECT DISTINCT r FROM Resume r JOIN r.educations e WHERE LOWER(e.degreeType) LIKE LOWER(CONCAT('%', :degreeType, '%')) AND r.isActive = true")
    List<Resume> findByEducationDegreeTypeContainingIgnoreCase(@Param("degreeType") String degreeType);

    /**
     * Count resumes by status
     */
    long countByStatusAndIsActiveTrue(ResumeStatus status);

    /**
     * Count total active resumes
     */
    long countByIsActiveTrue();

    /**
     * Find resumes with failed processing that can be retried
     */
    @Query("SELECT r FROM Resume r WHERE (r.status = 'PARSE_FAILED' OR r.status = 'ANALYSIS_FAILED') AND r.isActive = true AND r.updatedAt < :retryAfter")
    List<Resume> findFailedResumesForRetry(@Param("retryAfter") LocalDateTime retryAfter);

    /**
     * Complex search query for resume matching
     */
    @Query("""
        SELECT DISTINCT r FROM Resume r 
        LEFT JOIN r.parsedData.skills s 
        LEFT JOIN r.workExperiences we 
        LEFT JOIN r.educations e 
        WHERE r.isActive = true 
        AND (:skills IS NULL OR EXISTS (SELECT 1 FROM r.parsedData.skills skill WHERE LOWER(skill) IN :skills))
        AND (:minExperience IS NULL OR r.parsedData.totalExperienceYears >= :minExperience)
        AND (:maxExperience IS NULL OR r.parsedData.totalExperienceYears <= :maxExperience)
        AND (:jobTitle IS NULL OR EXISTS (SELECT 1 FROM r.workExperiences exp WHERE LOWER(exp.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))))
        AND (:company IS NULL OR EXISTS (SELECT 1 FROM r.workExperiences exp WHERE LOWER(exp.companyName) LIKE LOWER(CONCAT('%', :company, '%'))))
        AND (:degree IS NULL OR EXISTS (SELECT 1 FROM r.educations edu WHERE LOWER(edu.degreeType) LIKE LOWER(CONCAT('%', :degree, '%'))))
        ORDER BY r.uploadedAt DESC
        """)
    Page<Resume> searchResumes(
            @Param("skills") List<String> skills,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            @Param("jobTitle") String jobTitle,
            @Param("company") String company,
            @Param("degree") String degree,
            Pageable pageable
    );

    /**
     * Find resumes by S3 key for cleanup operations
     */
    Optional<Resume> findByS3KeyAndIsActiveTrue(String s3Key);

    /**
     * Soft delete resume by setting isActive to false
     */
    @Query("UPDATE Resume r SET r.isActive = false, r.status = 'DELETED', r.updatedAt = CURRENT_TIMESTAMP WHERE r.resumeId = :resumeId")
    void softDeleteResume(@Param("resumeId") UUID resumeId);
}