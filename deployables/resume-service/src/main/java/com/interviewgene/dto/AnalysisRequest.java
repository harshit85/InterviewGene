package com.interviewgene.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for resume analysis request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {

    @NotNull(message = "Resume ID is required")
    private UUID resumeId;

    @NotBlank(message = "Analysis type is required")
    private String analysisType;

    private Map<String, Object> parameters;
    
    private boolean forceReanalysis = false;
}