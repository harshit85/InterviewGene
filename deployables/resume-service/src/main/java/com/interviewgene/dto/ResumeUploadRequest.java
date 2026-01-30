package com.interviewgene.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for resume upload request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadRequest {

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    private String description;
    
    private boolean autoAnalyze = true;
}